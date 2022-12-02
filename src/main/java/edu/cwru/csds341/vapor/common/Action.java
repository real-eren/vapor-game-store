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

    CREATE_ACCOUNT(
        AType.INSERT_ID,
        "create account", "c",
        "InsertUser(?,?,?)",
        new Parameter(PType.STRING, "username", "username", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.DATE, "date", "date", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY)
    ),
    
    UPDATE_USERNAME(
        AType.UPDATE,
        "update username of user", "uu",
        "UpdateUsername(?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.STRING, "new_name", "new username", Requirement.SimpleReq.NONEMPTY)
    ),
    DELETE_ACCOUNT(
        AType.DELETE,
        "delete user from database", "d",
        "DeleteUser(?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    VIEW_USER_INFO(
        AType.QUERY,
        "view username and join date", "vu",
        "GetUserInfo(?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY)
    ),
    MAKE_COMMENT(
        AType.INSERT,
        "add new comment", "mc",
        "InsertComment(?,?,?,?)",
        new Parameter(PType.INT, "commenter_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.STRING, "profile_id", "users user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.DATE, "date", "date", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.STRING, "message", "message", Requirement.SimpleReq.NONEMPTY)
    ),
    
    VIEW_FOLLOWERS(
            AType.QUERY,
            "view followers", "vf",
            "GetFollowersList(?)",
            new Parameter(PType.STRING, "user_id", "user id", Requirement.SimpleReq.NONEMPTY)
    ),
    FOLLOW_USER(
        AType.INSERT,
        "follow a user", "f",
        "InsertFollow(?,?,?)",
        new Parameter(PType.INT, "followed_id", "user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "follower_id", "users user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.DATE, "date", "date", Requirement.SimpleReq.NONEMPTY)
    ),
    UNFOLLOW_USER(
        AType.DELETE,
        "unfollow a user", "u",
        "DeleteFollow(?,?)",
        new Parameter(PType.INT, "followed_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "follower_id", "users user id", Requirement.SimpleReq.NONEMPTY)
    ),
    INSERT_NEW_GAME(
        AType.INSERT,
        "insert a new game to database", "ig",
        "InsertGame(?,?,?,?,?,?)",
        new Parameter(PType.STRING, "game_name", "game name", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "review_avg", "game's review average", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "ESRB_rating_id", "ESRB rating id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.DATE, "release_date", "date game was released", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "price", "price", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "id", "id output", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    UPDATE_GAME_REVIEW_AVERAGE(
        AType.UPDATE,
        "update a games review average", "ua",
        "UpdateGameReviewAverage(?,?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "review_avg", "update review average", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    UPDATE_GAME_PRICE(
        AType.UPDATE,
        "update the price of existing game", "up",
        "UpdateGamePrice(?,?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "price", "price", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    DELETE_GAME(
        AType.DELETE,
        "delete game from database", "dg",
        "DeleteGame(?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    GRANT_GAME(
        AType.INSERT,
        "grant possesion of a game to user", "gg",
        "InsertGameOwnership(?,?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game's id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.DATE, "date", "date", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_PROFILE_COMMENTS(
        AType.QUERY,
        "view comments on user profile", "vc",
        "GetProfileComments(?)",
        new Parameter(PType.STRING, "user_id", "user id", Requirement.SimpleReq.NONEMPTY) 
    ),
    VIEW_GAMES_OWNED(
        AType.QUERY,
        "view games owned", "vg",
        "GetOwnedGamesForUser(?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY) 
    ),
    
    SEARCH_GAMES_ESRB_RATING(
        AType.QUERY,
        "search games by ESRB rating", "se",
        "GetGamesWithESRB(?)",
        new Parameter(PType.INT, "ESRB_id", "ESRB rating id", Requirement.SimpleReq.NONEMPTY)
    ),
    SEARCH_GAMES_HIGHEST_RATING(
        AType.QUERY,
        "view games by highest rating average", "sa",
        "GamesOrderedByReview"
    ),
    VIEW_GAME_DETAILS(
        AType.QUERY,
        "view game details", "vd",
        "GetGameDetails(?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_FOLLOWED_GAME_SIMILARITIES(
        AType.QUERY,
        "view users followed that own game", "vfg",
        "GetFollowedUsersThatOwnGame(?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    VIEW_BEST_SELLING(
        AType.QUERY,
        "view top selling", "vs",
        "GetBestSellingPastSevenDays(?)",
        new Parameter(PType.STRING, "limit", "Amount to view", Requirement.SimpleReq.NONEMPTY)
    );

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
