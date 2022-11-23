package edu.cwru.csds341.vapor.cli;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * todo: fill in javadocs
 */
public enum Action {
    ADD_GAME("add game", "ag", List.of(), null) {
        @Override
        void apply(Map<String, String> args) throws SQLException {
            preparedStatement.setString(0, args.get("game name"));
        }
    },
    ;

    final String fullName;
    final String shortName;

    final List<Parameter> parameters;

    final PreparedStatement preparedStatement;

    Action(String fullName, String shortname, List<Parameter> parameters, PreparedStatement preparedStatement) {
        this.fullName = fullName;
        this.shortName = shortname;
        this.parameters = parameters;
        this.preparedStatement = preparedStatement;
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
     * Set parametes
     */
    abstract void apply(Map<String, String> args) throws SQLException;


    @Override
    public String toString() {
        // todo: nice format, for display
        return super.toString();
    }

    static class Parameter {
        String name;
        List<Requirement> requirements;

        public Parameter(String name, List<Requirement> requirements) {
            this.name = name;
            this.requirements = requirements;
        }

    }

    static class Requirement {

        public Requirement(Predicate<String> predicate, String message) {
            this.predicate = predicate;
            this.message = message;
        }

        private final Predicate<String> predicate;
        final String message;

        public boolean accepts(String str) {
            return predicate.test(str);
        }
    }
    
}
