package edu.cwru.csds341.vapor.cli;

import edu.cwru.csds341.vapor.cli.Action.Parameter.PType;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Something that can be done in the application, such as "add a game", "create an account".
 * todo: fill in javadocs
 */
public enum Action {
    EXAMPLE_ADD_GAME(
            AType.UPDATE,
            "add game", "ag",
            "[schemaName].[storedProcedureName](?,?)",
            new Parameter(PType.STRING, "game_name", "game name", Requirement.SimpleReq.NONEMPTY),
            new Parameter(PType.INT, "rating", "rating", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
            ),
    ;

    final AType type;
    final String fullName;
    final String shortName;

    /** The string with which to retrieve a StoredProcedure from a Connection */
    final String storedProcedureString;

    final List<Parameter> parameters;

    /** Correlates to SQL statements */
    enum AType {
        /** Insert, Update, Delete */
        UPDATE,
        /** Select */
        QUERY
    }

    Action(AType type, String fullName, String shortname, String storedProcedureString, List<Parameter> parameters) {
        this.type = type;
        this.fullName = fullName;
        this.shortName = shortname;
        this.storedProcedureString = String.format("{call %s}", storedProcedureString);
        this.parameters = parameters;
    }
    Action(AType type, String fullName, String shortname, String storedProcedureString, Parameter... parameters) {
        this(type, fullName, shortname, storedProcedureString, List.of(parameters));
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
    public void apply(CallableStatement cs, Map<Parameter, String> args) throws SQLException {
        for (var entry : args.entrySet()) applyParam(cs, entry.getKey(), entry.getValue());
    }

    private void applyParam(CallableStatement cs, Parameter parameter, String val) throws SQLException {
        switch (parameter.type) {
            case INT:
                cs.setInt(parameter.argName, Integer.parseInt(val));
                break;
            case STRING:
                cs.setString(parameter.argName, val);
                break;
            case DATE:
                cs.setDate(parameter.argName, Date.valueOf(val));
                break;
        }
    }

    @Override
    public String toString() {
        // todo: nice format, for display
        return super.toString();
    }

    /** An argument the user must provide to the Action. */
    static class Parameter {

        /** What SQL type this Parameter maps to */
        final PType type;

        /** The name used in the SQL stored Procedure */
        final String argName;

        /** The name shown to the user */
        final String displayName;

        /** Predicates to enforce on the user-given string */
        final List<Requirement> requirements;

        /** Maps to SQL data types */
        enum PType { INT, STRING, DATE }

        public Parameter(PType type, String argName, String displayName, List<Requirement> requirements) {
            this.type = type;
            this.argName = argName;
            this.displayName = displayName;
            this.requirements = List.copyOf(requirements);
        }

        public Parameter(PType type, String argName, String displayName, Requirement... requirements) {
            this(type, argName, displayName, List.of(requirements));
        }
    }

}
