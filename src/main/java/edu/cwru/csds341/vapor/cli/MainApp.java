package edu.cwru.csds341.vapor.cli;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Requirement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
     * Creates the CallableStatement associated with the Action,
     * and gets input from the user for each field through std-in.
     * @param scanner  where to get user input from
     * @param connection  where to access the database from
     * @param action  which command is being processed.
     * @return  CallableStatement that is ready to execute
     * @throws SQLException  may occur while creating the Statement or setting parameters
     */
    private static CallableStatement prepareStatement(Scanner scanner, Connection connection, Action action) throws SQLException {
        // todo: implement cancelling
        //   look for "!cancel"
        // TODO: think of way to provide "quit" keyword without
        //  preventing that string from being used as an actual param

        // has list of parameters it needs from user
        Map<Action.Parameter, String> userInputs = new HashMap<>(action.parameters.size());
        for (Action.Parameter parameter : action.parameters) {
            boolean valid = false;
            String input = null;
            while (! valid) {
                System.out.print("Enter value for '" + parameter.displayName + "': ");
                input = scanner.nextLine();
                valid = true;
                // validate
                for (Requirement requirement : parameter.requirements) {
                    if (!requirement.accepts(input)) {
                        valid = false;
                        System.out.println("Error: " + requirement.getMessage());
                        break;
                    }
                }
            }
            // what if valid==false
            // handle: repeat this prompt, or cancel action?
            assert input != null : "input should have been set before exiting loop";

            userInputs.put(parameter, input);
        }

        var cs = action.getCallableStatement(connection);
        action.apply(cs, userInputs);
        return cs;
    }

    public static void main(String[] args) throws SQLException {
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

                if (action == null) System.out.println("Invalid action, try again");
                else {
                    try (CallableStatement cs = prepareStatement(scanner, connection, action)) {
                        if (action.type.equals(Action.AType.UPDATE))
                            cs.executeUpdate();
                            // TODO: give user feedback about update

                        else if (action.type.equals(Action.AType.QUERY)) {
                            ResultSet result = cs.executeQuery();

                            // TODO: print output of result to user
                            // print column names, then values?
                        }
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
