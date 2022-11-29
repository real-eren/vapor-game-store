package edu.cwru.csds341.vapor.cli;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Connections;
import edu.cwru.csds341.vapor.common.Requirement;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MainApp {


    private static void greetUser() {
        System.out.println("Welcome to the Vapor Game Store CLI");
        System.out.println("Enter 'help' for a list of commands, 'exit' to quit.");
    }

    /** Displays list of commands to user. */
    private static void printHelp() {
        System.out.println("Available commands: [Command : Description]");
        final String format = "   %-7s|  %s\n";
        System.out.printf(format, "help", "print list of commands");
        System.out.printf(format, "exit", "quit the application");
        System.out.printf(format, "cancel", "while filling out fields, cancel the command");
        for (Action value : Action.VALUES) {
            System.out.printf(format, value.shortName, value.description);
        }
    }

    /**
     * For each parameter, prompts the user for input.
     * Allows the user to cancel, in which case an Empty Optional is returned.
     * Returns the mapping between Parameters and user inputs.
     * @param scanner  where to take user input from
     * @param parameters  the parameters to query the user for
     * @return  Empty if user cancelled, or Map of parameters to the user's response
     */
    private static Optional<Map<Action.Parameter, String>> promptUserForParameters(Scanner scanner, List<Action.Parameter> parameters) {
        Map<Action.Parameter, String> userInputs = new HashMap<>(parameters.size());
        for (Action.Parameter parameter : parameters) {
            boolean valid = false;
            String input = null;
            while (! valid) {
                System.out.print("Enter value for '" + parameter.displayName + "': ");
                input = scanner.nextLine();
                valid = true;
                if (input.equalsIgnoreCase("cancel")) {
                    System.out.println("Cancel the command (Y) or treat as input [default]?: ");
                    if (scanner.nextLine().equalsIgnoreCase("y")) return Optional.empty();
                }

                // validate
                for (Requirement requirement : parameter.requirements) {
                    if (!requirement.accepts(input)) {
                        valid = false;
                        System.out.println("Error: " + requirement.getMessage());
                        break;
                    }
                }
            }
            userInputs.put(parameter, input);
        }
        return Optional.of(userInputs);
    }


    /**
     * Call after validating the Action and userInputs.
     * Retrieves the callable statement, prepares it, executes,
     * and displays results according to the type of statement
     * @param connection  the DB connection
     * @param action  the Action to perform
     * @param userInputs  the parameters
     * @throws SQLException  while getting the CallableStatement, setting the parameters or executing it
     */
    private static void executeAction(Connection connection, Action action, Map<Action.Parameter, String> userInputs) throws SQLException {
        try (CallableStatement cs = action.getCallableStatement(connection)) {
            Action.applyAll(cs, userInputs);
            switch (action.type) {
                case INSERT_ID:
                    // TODO: execute, print generated ID
                    break;
                case UPDATE:
                case INSERT:
                case DELETE:
                    // nothing specific to report
                    cs.executeUpdate();
                    break;
                case QUERY:
                    var resultSet = cs.executeQuery();
                    performQuery(action, resultSet);
                    break;
            }
        }
    }

    private static void performQuery(Action action, ResultSet resultSet) throws SQLException {
        System.out.printf("Results for: %s", action.description);
        // format
        // first show column names?
        var metadata = resultSet.getMetaData();
        List<String> labels = new ArrayList<>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            labels.add(metadata.getColumnLabel(i));
        }
        // todo print labels better
        System.out.printf(labels.toString());
        // TODO: format
        while (resultSet.next()) {
            for (int i = 0; i < columnCount; i++) {
                System.out.printf("%s", resultSet.getString(i));
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Path credentialsFile = (args.length==0)
                ? Connections.CREDENTIALS_DIR.resolve("cli.credentials")
                : Path.of(args[0]);
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = Connections.fromFile(credentialsFile)
        ) {
            greetUser();

            // Main loop
            while (true) {
                System.out.print("Enter a command: ");
                var line = scanner.nextLine();

                if (line.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                if (line.equalsIgnoreCase("exit")) {
                    System.out.print("Confirm exit (yes): ");
                    var confirmation = scanner.nextLine();
                    if (confirmation.equalsIgnoreCase("yes")) break;
                    else continue;
                }

                var action = Action.VALUES.stream()
                        .filter(a -> a.shortName.equalsIgnoreCase(line))
                        .findAny()
                        .orElse(null);

                if (action == null) {
                    System.out.println("Invalid action, try again");
                    continue;
                }

                var userInputs = promptUserForParameters(scanner, action.parameters);
                if (userInputs.isEmpty()) continue;

                executeAction(connection, action, userInputs.get());

                // repeat
            }
        } catch (SQLException sqlE) {
            System.out.println("Database error occurred, exiting.");
            System.out.println(sqlE.getMessage());
        } catch (NoSuchFileException e) {
            System.out.println("Credentials file '" + e.getFile() + "' does not exist");
        } catch (AccessDeniedException e) {
            System.out.println("Could not obtain read permissions for " + e.getFile());
        } catch (IOException e) {
            System.out.println("Failed while trying to open credentials file.");
            System.out.println(e);
        } catch (Connections.DBIllegalConfigException e) {
            System.out.println(e.getMessage());
        } finally {
        }
    }
}
