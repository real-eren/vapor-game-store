package edu.cwru.csds341.vapor.cli;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Something that can be done in the application, such as "add a game", "create an account".
 * todo: fill in javadocs
 */
public enum Action {
    EXAMPLE_ADD_GAME(
            "add game", "ag",
            "[schemaName].[storedProcedureName](?,?)",
            new Parameter("game name", SimpleReq.NONEMPTY),
            new Parameter("rating", SimpleReq.NONEMPTY, SimpleReq.POSITIVE_INTEGER)
            ) {
        @Override
        void apply(CallableStatement cs, Map<String, String> args) throws SQLException {
            cs.setString(1, args.get("game name"));
            cs.setString(2, args.get("rating"));
        }
    },
    ;

    final String fullName;
    final String shortName;

    /** The string with which to retrieve a StoredProcedure from a Connection */
    final String storedProcedureString;

    final List<Parameter> parameters;

    Action(String fullName, String shortname, String storedProcedureString, List<Parameter> parameters) {
        this.fullName = fullName;
        this.shortName = shortname;
        this.storedProcedureString = String.format("{call %s}", storedProcedureString);
        this.parameters = parameters;
    }
    Action(String fullName, String shortname, String storedProcedureString, Parameter... parameters) {
        this(fullName, shortname, storedProcedureString, List.of(parameters));
    }


    public static final List<Action> VALUES = List.of(Action.values());


    /**
     * looks up action that matches command
     * @return  Action if command matches one, or null
     */
    public static Action get(String command) {
        for (Action action : VALUES) {
            if (action.accepts(command)) {
                return action;
            }
        }
        return null;
    }

    public CallableStatement getCallableStatement(Connection connection) throws SQLException {
        return connection.prepareCall(storedProcedureString);
    }

    /**
     *
     */
    private boolean accepts(String line) {
        return shortName.equalsIgnoreCase(line) || fullName.equalsIgnoreCase(line);
    }

    /**
     * Set parameters. Assumes map has all necessary fields
     */
    abstract void apply(CallableStatement cs, Map<String, String> args) throws SQLException;


    @Override
    public String toString() {
        // todo: nice format, for display
        return super.toString();
    }

    static class Parameter {
        final String name;
        final List<Requirement> requirements;

        public Parameter(String name, List<Requirement> requirements) {
            this.name = name;
            this.requirements = List.copyOf(requirements);
        }

        public Parameter(String name, Requirement... requirements) {
            this(name, List.of(requirements));
        }
    }

    /**
     * Requirements that may be shared by several Actions
     */
    enum SimpleReq implements Requirement {
        NONEMPTY(Predicate.not(String::isEmpty), "value cannot be empty"),
        POSITIVE_INTEGER(Pattern.compile("\\d+"),"value must be a positive integer"),
        ;

        SimpleReq(Predicate<String> predicate, String message) {
            this.predicate = predicate;
            this.message = message;
        }
        SimpleReq(Pattern pattern, String message) {
            this.predicate = pattern.asMatchPredicate();
            this.message = message;
        }

        private final Predicate<String> predicate;
        private final String message;


        @Override
        public boolean accepts(String str) { return predicate.test(str); }

        @Override
        public String getMessage() { return message; }
    }

}
