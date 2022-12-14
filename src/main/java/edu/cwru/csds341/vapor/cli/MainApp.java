package edu.cwru.csds341.vapor.cli;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Connections;
import edu.cwru.csds341.vapor.common.Requirement;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.*;
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
                input = scanner.nextLine().strip();
                valid = true;
                if (input.equalsIgnoreCase("cancel")) {
                    System.out.println("Cancel the command (Y) or treat as input (i)?: ");
                    while (true) {
                        String confirmation = scanner.nextLine().strip();
                        if (confirmation.equalsIgnoreCase("y")) return Optional.empty();
                        else if (confirmation.equalsIgnoreCase("i")) break;
                    }
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
                    // out param is, by our convention, the last column
                    int colCount = cs.getMetaData().getColumnCount();
                    cs.registerOutParameter(colCount, Types.INTEGER);
                    cs.executeUpdate();
                    System.out.printf("ID of new item: %d\n", cs.getInt(colCount));
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
        } catch (SQLException e) {
            System.out.println("Error occurred while performing command: " + e.getMessage());
        }
    }

    private static void performQuery(Action action, ResultSet resultSet) throws SQLException {
        System.out.printf("Results for: %s%n", action.description);

        // first gather all results, keeping track of the max width for each column (include labels row!)
        // then compute a format string
        // then print each row

        var metadata = resultSet.getMetaData();
        int columnCount = resultSet.getMetaData().getColumnCount();

        List<String[]> rows = new ArrayList<>();
        // max width for each column, updated when collecting each row
        int[] maxWidth = new int[columnCount];

        String[] labels = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            String columnLabel = metadata.getColumnLabel(i);
            labels[i-1] = columnLabel;
            maxWidth[i-1] = Math.max(maxWidth[i-1], columnLabel.length());
        }
        rows.add(labels);

        while (resultSet.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                String columnVal = resultSet.getString(i);
                row[i-1] = columnVal;
                maxWidth[i-1] = Math.max(maxWidth[i-1], columnVal.length());
            }
            rows.add(row);
        }

        if (rows.size() == 1) {
            System.out.println("No results");
            return;
        }

        // format string for each column. computed after all rows were collected
        StringBuilder formatStringBuilder = new StringBuilder(columnCount * 8); // simple heuristic

        for (int columnWidth : maxWidth)
            formatStringBuilder.append("%-").append(columnWidth + 1).append("s  ");

        var formatString = formatStringBuilder.append("\n").toString();

        for (String[] row : rows)
            System.out.printf(formatString, (Object[]) row);
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
                var line = scanner.nextLine().strip();

                if (line.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                if (line.equalsIgnoreCase("exit")) {
                    System.out.print("Confirm exit (yes): ");
                    var confirmation = scanner.nextLine().strip();
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
