package edu.cwru.csds341.vapor.common;

import edu.cwru.csds341.vapor.common.Action.Parameter.PType;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * DB-involving action that can be done in the application, such as "add a game", "create an account".
 * <li>Has a list of {@link Parameter}s.</li>
 * <li>Associated with a stored procedure</li>
 * Designed to facilitate automating the menu creation.
 */
public enum Action {

    //todo
    //make callable statements in database, correct storedProcedure strings for each action


    CREATE_ACCOUNT(
        AType.INSERT_ID,
        "create account", "c",
        "[user].[create_account](?)",
        new Parameter(PType.STRING, "username", "username", Requirement.SimpleReq.NONEMPTY)
    ),
    MAKE_COMMENT(
        AType.INSERT,
        "add new comment", "mc",
        "[user_profile_comment].[make_comment(?,?,?)]",
        new Parameter(PType.INT, "profile_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.STRING, "commenter_id", "users user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.STRING, "message", "message", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_FOLLOWERS(
            AType.QUERY,
            "view friends", "vf",
            "[followers].[viewFollowers](?,?)",
            new Parameter(PType.STRING, "user1", "user id", Requirement.SimpleReq.NONEMPTY)
            ),
    FOLLOW_USER(
        AType.INSERT,
        "follow a user", "f",
        "[follows].[follow_user](?,?)",
        new Parameter(PType.INT, "userA_id", "user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "userB_id", "users user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    UNFOLLOW_USER(
        AType.DELETE,
        "unfollow a user", "u",
        "[follows].[unfollow](?,?)",
        new Parameter(PType.INT, "userA_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "userB_id", "users user id", Requirement.SimpleReq.NONEMPTY)
    ),
    GRANT_GAME(
        AType.INSERT,
        "grant possesion of a game", "gg",
        "g[ame_ownership].[grant_game_ownsership](?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game's id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_PROFILE(
        AType.QUERY,
        "view user profile", "vf",
        "[user].[view_profile](?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_GAMES_OWNED(
        AType.QUERY,
        "view games owned", "vg",
        "[game_ownership].[view_games_owned](?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY) 
    ),
    VIEW_USER_INFO(
        AType.QUERY,
        "view username and join date", "vu",
        "[user].[view_info](?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY)
    ),
    //could be many more as said in query doc
    SEARCH_GAMES_ESRB_RATING(
        AType.QUERY,
        "search games by ESRB rating", "se",
        "[games].[search_esrb_rating](?)",
        new Parameter(PType.INT, "rating_id", "ESRB rating id", Requirement.SimpleReq.NONEMPTY)
    ),
    SEARCH_GAMES_HIGHEST_RATING(
        AType.QUERY,
        "search games by highest rating average", "sa",
        "[games].[search_highest_average]"
        //would not take in any parameters
    ),
    VIEW_GAME_DETAILS(
        AType.QUERY,
        "view game details", "vd",
        "[games].[view_game_details](?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_FOLLOWED_GAME_SIMILARITIES(
        AType.QUERY,
        "users followed that own game", "vfg",
        "[user].[followed_game_similarities](?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_BEST_SELLING(
        AType.QUERY,
        "view top selling", "vs",
        "[games].[best_selling]"
        //would not take any parameters
    )
    
    ;

    /** What type of SQL statement this Action performs */
    public final AType type;
    /** Short blurb about what this Action does */
    public final String description;
    /** The name displayed to the user on the CLI. A few letters. */
    public final String shortName;

    /** The string with which to retrieve a StoredProcedure from a Connection */
    final String storedProcedureString;

    /** The parameters that must be provided to this Action's {@link CallableStatement} */
    public final List<Parameter> parameters;

    /** Correlates to SQL statements */
    public enum AType {
        /** Update existing entry */
        UPDATE,
        /** Add new row, with identity column */
        INSERT_ID,
        /** Add new row, without identity column*/
        INSERT,
        /** Remove existing row */
        DELETE,
        /** Select */
        QUERY
    }

    Action(AType type, String description, String shortname, String storedProcedureString, List<Parameter> parameters) {
        this.type = type;
        this.description = description;
        this.shortName = shortname;
        this.storedProcedureString = String.format("{call %s}", storedProcedureString);
        this.parameters = parameters;
    }
    Action(AType type, String description, String shortname, String storedProcedureString, Parameter... parameters) {
        this(type, description, shortname, storedProcedureString, List.of(parameters));
    }

    /** Read-only list of all Actions. Prefer this to Enum::values because that creates a copy everytime */
    public static final List<Action> VALUES = List.of(Action.values());

    /** Use the given connection to prepare a CallableStatement of the storedProcedure associated with this Action */
    public CallableStatement getCallableStatement(Connection connection) throws SQLException {
        return connection.prepareCall(storedProcedureString);
    }

    /**
     * Set parameters from the map. Assumes map has all necessary fields and that they are all valid.
     * @param cs  the statement to set parameters on
     * @param args  Values are assumed to have been validated w.r.t. the Parameter,
     *             and Parameters are assumed to be valid for the given statement
     * @throws SQLException  if the statement doesn't accept one of the Parameters;
     *                      if a database access error occurs
     *                      or this method is passed a closed CallableStatement
     */
    public static void applyAll(CallableStatement cs, Map<Parameter, String> args) throws SQLException {
        for (var entry : args.entrySet()) entry.getKey().apply(cs, entry.getValue());
    }


    /**
     * An argument the user must provide to the Action.
     */
    public static class Parameter {

        /** What SQL type this Parameter maps to */
        public final PType type;

        /** The name used in the SQL stored Procedure */
        public final String argName;

        /** The name shown to the user */
        public final String displayName;

        /** Predicates to enforce on the user-given string */
        public final List<Requirement> requirements;

        /** Maps to SQL data types */
        public enum PType {
            INT {
                @Override
                void apply(CallableStatement statement, String argName, String val) throws SQLException {
                    statement.setInt(argName, Integer.parseInt(val));
                }
            },
            STRING {
                @Override
                void apply(CallableStatement statement, String argName, String val) throws SQLException {
                    statement.setString(argName, val);
                }
            },
            DATE {
                @Override
                void apply(CallableStatement statement, String argName, String val) throws SQLException {
                    statement.setDate(argName, Date.valueOf(val));
                }
            }
            ;

            /**
             * Set a parameter on a statement, interpreting the value according to this type.
             * External code should use {@link Parameter#apply(CallableStatement, String)}
             * @param statement  the statement to set a parameter for
             * @param argName  the name of the parameter to set
             * @param val  the value to enter. Assumed to have been validated
             * @throws SQLException  if parameterName does not correspond to a named parameter;
             *                      if a database access error occurs
             *                      or this method is called on a closed CallableStatement
             */
            abstract void apply(CallableStatement statement, String argName, String val) throws SQLException;
        }

        public Parameter(PType type, String argName, String displayName, List<Requirement> requirements) {
            this.type = type;
            this.argName = argName;
            this.displayName = displayName;
            this.requirements = List.copyOf(requirements);
        }

        public Parameter(PType type, String argName, String displayName, Requirement... requirements) {
            this(type, argName, displayName, List.of(requirements));
        }

        /**
         * Use the given string as the value for this parameter of the given statement
         * @param statement  the statement to set parameters for
         * @param val  the value to use. Assumed to be a valid value for this Parameter
         * @throws SQLException  if parameterName does not correspond to a named parameter;
         *                      if a database access error occurs
         *                      or this method is called on a closed CallableStatement
         */
        public void apply(CallableStatement statement, String val) throws SQLException {
            type.apply(statement, argName, val);
        }
    }

}
