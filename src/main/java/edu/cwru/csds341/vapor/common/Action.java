package edu.cwru.csds341.vapor.common;

import edu.cwru.csds341.vapor.common.Action.Parameter.PType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * DB-involving action that can be done in the application, such as "add a game", "create an account".
 * <li>Has a list of {@link Parameter}s.</li>
 * <li>Associated with a stored procedure</li>
 * Designed to facilitate automating the menu creation.
 * Prefix of 'VIEW' indicates a single value will be returned,
 * Prefix of 'LIST' indicates many values may be returned.
 * See stored_proc.sql for order of returned fields
 */
public enum Action {

    CREATE_ACCOUNT(
        AType.INSERT_ID,
        "create account", "ca",
        "[InsertUser](?,?,?)", // 3rd is output param for created ID
        new Parameter(PType.STRING, "username", "username", Requirement.SimpleReq.NONEMPTY), //25
        new Parameter(PType.DATE, "date", "join date", Requirement.SimpleReq.NONEMPTY)
    ),
    UPDATE_USERNAME(
        AType.UPDATE,
        "change username", "uu",
        "[UpdateUsername](?,?)",
        new Parameter(PType.INT, "user_id", "user id"),
        new Parameter(PType.STRING, "new_name", "new username") //25
    ),
    DELETE_USER(
        AType.DELETE,
        "delete account", "du",
        "[DeleteUser](?)",
        new Parameter(PType.INT, "user_id", "user id")
    ),
    VIEW_USER_INFO(
        AType.QUERY,
        "get account information for user", "gu",
        "[GetUserInfo](?)",
        new Parameter(PType.INT, "user_id", "user id")
    ),
    MAKE_COMMENT(
        AType.INSERT,
        "add new comment", "mc",
        "[InsertComment(?,?,?,?)]",
        new Parameter(PType.INT, "commenter_id", "commenter's user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "profile_id", "profile user's id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.DATETIME, "datetime", "datetime"),
        new Parameter(PType.STRING, "message", "message", Requirement.SimpleReq.NONEMPTY) //100
    ),
    LIST_FOLLOWERS(
        AType.QUERY,
        "list users who follow this user", "lfr",
        "[GetFollowerList](?)",
        new Parameter(PType.INT, "user_id", "followed-user id", Requirement.SimpleReq.NONEMPTY)
    ),
    LIST_FOLLOWED(
        AType.QUERY,
        "list users who follow this user", "lfd",
        "[GetFollowedList](?)",
        new Parameter(PType.INT, "user_id", "followed-user id", Requirement.SimpleReq.NONEMPTY)
    ),
    FOLLOW_USER(
        AType.INSERT,
        "have userA follow userB", "fu",
        "[InsertFollow](?,?,?)",
        new Parameter(PType.INT, "follower_id", "userA (follower) id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "followed_id", "userB (followed) id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.DATETIME, "date", "datetime")
    ),
    UNFOLLOW_USER(
        AType.DELETE,
        "have userA unfollow userB", "ufu",
        "[DeleteFollow](?,?)",
        new Parameter(PType.INT, "follower_id", "userA id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "followed_id", "userB id", Requirement.SimpleReq.NONEMPTY)
    ),
    ADD_GAME(
        AType.INSERT_ID,
        "add a new game", "ag",
        "[InsertGame](?,?,?,?,?,?)",
        new Parameter(PType.STRING, "game_name", "game name"), //35
        new Parameter(PType.INT, "review_avg", "review average"),
        new Parameter(PType.INT, "ESRB_rating_id", "ESRB rating ID"),
        new Parameter(PType.DATE, "release_date", "release date"),
        new Parameter(PType.MONEY, "price", "price")
    ),
    UPDATE_GAME_REVIEW_AVG(
        AType.UPDATE,
        "update the review average for a game", "ugr",
        "[UpdateGameReviewAvg](?,?)",
        new Parameter(PType.INT, "game_id", "game id"),
        new Parameter(PType.INT, "review_avg", "new review average")
    ),
    UPDATE_GAME_PRICE(
        AType.UPDATE,
        "update the price of a game", "ugp",
        "[UpdateGamePrice](?,?)",
        new Parameter(PType.INT, "game_id", "game id"),
        new Parameter(PType.MONEY, "price", "new price")
    ),
    DELETE_GAME(
        AType.DELETE,
        "remove a game from the store", "dg",
        "[DeleteGame](?)",
        new Parameter(PType.INT, "game_id", "game id")
    ),
    GRANT_GAME(
        AType.INSERT,
        "grant possession of a game", "gg",
        "[InsertGameOwnership](?,?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.DATE, "date", "date acquired", Requirement.SimpleReq.NONEMPTY)
    ),
    LIST_PROFILE_COMMENTS(
        AType.QUERY,
        "list comments on a user's profile, newest to oldest", "gpc",
        "[GetProfileComments](?)",
        new Parameter(PType.INT, "user_id", "user ID")
    ),
    LIST_GAMES_OWNED(
        AType.QUERY,
        "view games owned by a user", "vgu",
        "[GetOwnedGamesForUser](?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY) 
    ),
    //could be many more as said in query doc
    LIST_GAMES_WITH_ESRB_RATING(
        AType.QUERY,
        "list games with a specific ESRB rating", "lge",
        "[GetGamesWithESRB](?)",
        new Parameter(PType.INT, "ESRB_id", "ESRB rating id", Requirement.SimpleReq.NONEMPTY)
    ),
    LIST_GAMES_HIGHEST_RATING(
        AType.QUERY,
        "list games from high to low review average", "lgr",
        "[GamesOrderedByReview]()"
        //would not take in any parameters
    ),
    VIEW_GAME_DETAILS(
        AType.QUERY,
        "view game details", "vgd",
        "[GetGameDetails](?)",
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    LIST_FOLLOWED_THAT_OWN_GAME(
        AType.QUERY,
        "list users followed by a user and that own a game", "vfuog",
        "[GetFollowedUsersThatOwnGame](?,?)",
        new Parameter(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY)
    ),
    LIST_N_BEST_SELLING(
        AType.QUERY,
        "view N top selling games", "vtsg",
        "[GetBestSellingPastSevenDays](?)",
        new Parameter(PType.INT, "limit", "N (as in top N)")
        //would not take any parameters
    ),
    LIST_ESRB_RATINGS(
        AType.QUERY,
        "List all the ESRB ratings", "lesrb",
        "[GetAllESRBRatingDetails]()"
        // no params
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
        this.storedProcedureString = String.format("{call [dbo].%s}", storedProcedureString);
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
            MONEY {
                @Override
                void apply(CallableStatement statement, String argName, String val) throws SQLException {
                    statement.setBigDecimal(argName, new BigDecimal(val));
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
            },
            DATETIME {
                @Override
                void apply(CallableStatement statement, String argName, String val) throws SQLException {
                    statement.setTimestamp(argName, Timestamp.valueOf(val));
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
