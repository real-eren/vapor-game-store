package edu.cwru.csds341.vapor.cli;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Something that can be done in the application, such as "add a game", "create an account".
 * todo: fill in javadocs, figure out how to expose/take in PreparedStatement
 */
public enum Action {
    EXAMPLE_ADD_GAME(
            "add game", "ag",
            null,
            new Parameter("game name", SimpleReq.NONEMPTY),
            new Parameter("rating", SimpleReq.NONEMPTY, SimpleReq.POSITIVE_INTEGER)
            ) {
        @Override
        void apply(Map<String, String> args) throws SQLException {
            preparedStatement.setString(1, args.get("game name"));
            preparedStatement.setString(2, args.get("rating"));
        }
    },
    ;

    final String fullName;
    final String shortName;

    final List<Parameter> parameters;

    final PreparedStatement preparedStatement;

    Action(String fullName, String shortname, PreparedStatement preparedStatement, List<Parameter> parameters) {
        this.fullName = fullName;
        this.shortName = shortname;
        this.parameters = parameters;
        this.preparedStatement = preparedStatement;
    }
    Action(String fullName, String shortname, PreparedStatement preparedStatement, Parameter... parameters) {
        this(fullName, shortname, preparedStatement, List.of(parameters));
    }


    static final List<Action> VALUES = List.of(Action.values());


    /**
     * looks up action that matches line
     */
    public static Action get(String line) {
        for (Action action : VALUES) {
            if (action.accepts(line)) { // line is like "add game"
                return action;
            }
        }
        return null;
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
    abstract void apply(Map<String, String> args) throws SQLException;


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
