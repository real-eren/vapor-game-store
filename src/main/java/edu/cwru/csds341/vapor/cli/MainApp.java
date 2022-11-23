package edu.cwru.csds341.vapor.cli;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MainApp {


    private static void printHelp() {
        System.out.println("Available commands:");
        for (Action value : Action.VALUES) {
            System.out.println(value);
        }
        // todo: further formatting?
    }

    /** Called after user decides to exit, before actually exiting application. */
    private static void onQuit() {
        // todo
        // close database connection
    }

    // todo: refactor later. reduce nesting
    public static void main(String[] args) throws SQLException {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Welcome to the Vapor Game Store CLI");

            // Main loop
            while (true) {
                // request input
                System.out.print("Enter a command: ");
                var line = scanner.nextLine();

                if (line.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                if (line.equalsIgnoreCase("exit")) {
                    // prompt y/n
                    System.out.print("Confirm exit (Y / N): ");
                    var confirmation = scanner.nextLine();
                    // todo: generalize this to y/n
                    if (confirmation.equalsIgnoreCase("yes")) {
                        onQuit();
                        return;
                    }
                }

                // check if valid Action
                var action = Action.get(line);

                // if so, enter Action submenu
                if (action == null) {
                    // else (invalid action) say not valid
                    System.out.println("Invalid action, try again");
                } else { // valid action chosen

                    // todo: implement cancelling
                    //   look for "!cancel"
                    // TODO: think of way to provide "quit" keyword without
                    //  preventing that string from being used as an actual param

                    // has list of parameters it needs from user
                    Map<String, String> userInputs = new HashMap<>(action.parameters.size());
                    for (Action.Parameter parameter : action.parameters) {
                        boolean valid = false;
                        String input = null;
                        while (! valid) {
                            System.out.println("Enter value for '" + parameter.name + "'");
                            input = scanner.nextLine();
                            valid = true;
                            // validate
                            for (Action.Requirement requirement : parameter.requirements) {
                                if (!requirement.accepts(input)) {
                                    valid = false;
                                    System.out.println("Error: " + requirement.message);
                                    break;
                                }
                            }
                        }
                        // what if valid==false
                        // handle: repeat this prompt, or cancel action?
                        assert input != null : "input should have been set before exiting loop";

                        userInputs.put(parameter.name, input);
                    }

                    // run function with those args
                    action.apply(userInputs);
                    // todo: check return val etc
                    action.preparedStatement.execute();
                }

                // repeat
            }
        }
    }
}
