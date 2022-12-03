package edu.cwru.csds341.vapor.common;

import edu.cwru.csds341.vapor.common.Action.Parameter.PType;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static edu.cwru.csds341.vapor.common.Action.Parameter.*;

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
        CA_USERNAME,
        CA_JOIN_DATE
    ),
    UPDATE_USERNAME(
        AType.UPDATE,
        "change username", "uu",
        "[UpdateUsername](?,?)",
        UU_USER_ID,
        UU_NEW_NAME
    ),
    DELETE_USER(
        AType.DELETE,
        "delete account", "du",
        "[DeleteUser](?)",
        DU_USER_ID
    ),
    VIEW_USER_INFO(
        AType.QUERY,
        "get account information for user", "gu",
        "[GetUserInfo](?)",
        VU_USER_ID
    ),
    MAKE_COMMENT(
        AType.INSERT,
        "add new comment", "mc",
        "[InsertComment(?,?,?,?)]",
        MC_COMMENTER_ID,
        MC_PROFILE_ID,
        MC_DATETIME,
        MC_MESSAGE
    ),
    LIST_FOLLOWERS(
        AType.QUERY,
        "list users who follow this user", "lfr",
        "[GetFollowerList](?)",
        LFR_USER_ID
    ),
    LIST_FOLLOWED(
        AType.QUERY,
        "list users who follow this user", "lfd",
        "[GetFollowedList](?)",
        LFD_USER_ID
    ),
    FOLLOW_USER(
        AType.INSERT,
        "have userA follow userB", "fu",
        "[InsertFollow](?,?,?)",
        FU_FOLLOWER_ID,
        FU_FOLLOWED_ID,
        FU_DATE
    ),
    UNFOLLOW_USER(
        AType.DELETE,
        "have userA unfollow userB", "ufu",
        "[DeleteFollow](?,?)",
        UFU_FOLLOWER_ID,
        UFU_FOLLOWED_ID
    ),
    ADD_GAME(
        AType.INSERT_ID,
        "add a new game", "ag",
        "[InsertGame](?,?,?,?,?,?)",
        AG_GAME_NAME,
        AG_REVIEW_AVG,
        AG_ESRB_RATING_ID,
        AG_RELEASE_DATE,
        AG_PRICE
    ),
    UPDATE_GAME_REVIEW_AVG(
        AType.UPDATE,
        "update the review average for a game", "ugr",
        "[UpdateGameReviewAvg](?,?)",
        UGR_GAME_ID,
        UGR_NEW_AVERAGE
    ),
    UPDATE_GAME_PRICE(
        AType.UPDATE,
        "update the price of a game", "ugp",
        "[UpdateGamePrice](?,?)",
        UGP_GAMEID,
        UGP_PRICE
    ),
    DELETE_GAME(
        AType.DELETE,
        "remove a game from the store", "dg",
        "[DeleteGame](?)",
        DG_GAMEID
    ),
    GRANT_GAME(
        AType.INSERT,
        "grant possession of a game", "gg",
        "[InsertGameOwnership](?,?,?)",
        GG_USERID,
        GG_GAMEID,
        GG_DATE
    ),
    LIST_PROFILE_COMMENTS(
        AType.QUERY,
        "list comments on a user's profile, newest to oldest", "gpc",
        "[GetProfileComments](?)",
        GPC_USERID
    ),
    LIST_GAMES_OWNED(
        AType.QUERY,
        "view games owned by a user", "vgu",
        "[GetOwnedGamesForUser](?)",
        VGU_USERID
    ),
    LIST_GAMES_WITH_ESRB_RATING(
        AType.QUERY,
        "list games with a specific ESRB rating", "lge",
        "[GetGamesWithESRB](?)",
        LGE_RATINGID
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
        VGD_GAMEID
    ),
    LIST_FOLLOWED_THAT_OWN_GAME(
        AType.QUERY,
        "list users followed by a user and that own a game", "vfuog",
        "[GetFollowedUsersThatOwnGame](?,?)",
        VFUOG_USERID,
        VFUOG_GAMEID
    ),
    LIST_N_BEST_SELLING(
        AType.QUERY,
        "view N top selling games", "vtsg",
        "[GetBestSellingPastSevenDays](?)",
        VTSG_LIMIT
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
     * Placed in an enum so that the GUI can directly refer to specific Parameters
     */
    public enum Parameter {
        CA_USERNAME(PType.STRING, "username", "username", Requirement.SimpleReq.NONEMPTY), //25
        CA_JOIN_DATE(PType.DATE, "date", "join date", Requirement.SimpleReq.NONEMPTY),
        UU_USER_ID(PType.INT, "user_id", "user id"),
        UU_NEW_NAME(PType.STRING, "new_name", "new username"), //25
        DU_USER_ID(PType.INT, "user_id", "user id"),
        VU_USER_ID(PType.INT, "user_id", "user id"),
        MC_COMMENTER_ID(PType.INT, "commenter_id", "commenter's user id", Requirement.SimpleReq.NONEMPTY),
        MC_PROFILE_ID(PType.INT, "profile_id", "profile user's id", Requirement.SimpleReq.NONEMPTY),
        MC_DATETIME(PType.DATETIME, "datetime", "datetime"),
        MC_MESSAGE(PType.STRING, "message", "message", Requirement.SimpleReq.NONEMPTY), //100
        LFR_USER_ID(PType.INT, "user_id", "followed-user id", Requirement.SimpleReq.NONEMPTY),
        LFD_USER_ID(PType.INT, "user_id", "followed-user id", Requirement.SimpleReq.NONEMPTY),
        FU_FOLLOWER_ID(PType.INT, "follower_id", "userA (follower) id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        FU_FOLLOWED_ID(PType.INT, "followed_id", "userB (followed) id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        FU_DATE(PType.DATETIME, "date", "datetime"),
        UFU_FOLLOWER_ID(PType.INT, "follower_id", "userA id", Requirement.SimpleReq.NONEMPTY),
        UFU_FOLLOWED_ID(PType.INT, "followed_id", "userB id", Requirement.SimpleReq.NONEMPTY),
        AG_GAME_NAME(PType.STRING, "game_name", "game name"), //35
        AG_REVIEW_AVG(PType.INT, "review_avg", "review average"),
        AG_ESRB_RATING_ID(PType.INT, "ESRB_rating_id", "ESRB rating ID"),
        AG_RELEASE_DATE(PType.DATE, "release_date", "release date"),
        AG_PRICE(PType.MONEY, "price", "price"),
        UGR_GAME_ID(PType.INT, "game_id", "game id"),
        UGR_NEW_AVERAGE(PType.INT, "review_avg", "new review average"),
        UGP_GAMEID(PType.INT, "game_id", "game id"),
        UGP_PRICE(PType.MONEY, "price", "new price"),
        DG_GAMEID(PType.INT, "game_id", "game id"),
        GG_USERID(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        GG_GAMEID(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY),
        GG_DATE(PType.DATE, "date", "date aquired", Requirement.SimpleReq.NONEMPTY),
        GPC_USERID(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        VGU_USERID(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        LGE_RATINGID(PType.INT, "ESRB_id", "ESRB rating id", Requirement.SimpleReq.NONEMPTY),
        VGD_GAMEID(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY),
        VFUOG_USERID(PType.INT, "user_id", "user id", Requirement.SimpleReq.NONEMPTY),
        VFUOG_GAMEID(PType.INT, "game_id", "game id", Requirement.SimpleReq.NONEMPTY),
        VTSG_LIMIT(PType.INT, "limit", "limit");
        ;
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
