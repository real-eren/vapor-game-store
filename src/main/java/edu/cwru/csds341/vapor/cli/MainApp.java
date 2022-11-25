package edu.cwru.csds341.vapor.cli;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Requirement;

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

    /** Called after user decides to exit, before actually exiting application. */
    private static void onQuit() {

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


    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection connection = null; // todo: create connection
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

                try (CallableStatement cs = action.getCallableStatement(connection)) {
                    Action.apply(cs, userInputs.get());
                    if (action.type.equals(Action.AType.UPDATE))
                        cs.executeUpdate();
                        // TODO: give user feedback about update

                    else if (action.type.equals(Action.AType.QUERY)) {
                        ResultSet result = cs.executeQuery();

                        // TODO: print output of result to user
                        //  print column names, then values?
                    }
                }

                // repeat
            }
        } catch (SQLException sqlE) {
            System.out.println("Error occurred, exiting.");
            System.out.println(sqlE.getMessage());
        } finally {
            onQuit();
        }
    }
}
