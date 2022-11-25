package edu.cwru.csds341.vapor.common;

import edu.cwru.csds341.vapor.common.Action.Parameter.PType;

import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * Something that can be done in the application, such as "add a game", "create an account".
 * todo: fill in javadocs
 */
public enum Action {

    //todo
    //make callable statements in database, correct storedProcedure strings for each action


    CREATE_ACCOUNT(
        AType.UPDATE,
        "create account", "c",
        "[user].[create_account](?)",
        new Parameter(PType.STRING, "username", "username", Requirement.SimpleReq.NONEMPTY)
    ),
    MAKE_COMMENT(
        AType.UPDATE,
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
        AType.UPDATE,
        "follow a user", "f",
        "[follows].[follow_user](?,?)",
        new Parameter(PType.INT, "userA_id", "user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER),
        new Parameter(PType.INT, "userB_id", "users user id", Requirement.SimpleReq.NONEMPTY, Requirement.SimpleReq.POSITIVE_INTEGER)
    ),
    UNFOLLOW_USER(
        AType.UPDATE,
        "unfollow a user", "u",
        "[follows].[unfollow](?,?)",
        new Parameter(PType.INT, "userA_id", "user id", Requirement.SimpleReq.NONEMPTY),
        new Parameter(PType.INT, "userB_id", "users user id", Requirement.SimpleReq.NONEMPTY)
    ),
    GRANT_GAME(
        AType.UPDATE,
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

    public final AType type;
    public final String description;
    public final String shortName;

    /** The string with which to retrieve a StoredProcedure from a Connection */
    final String storedProcedureString;

    public final List<Parameter> parameters;

    /** Correlates to SQL statements */
    public enum AType {
        /** Insert, Update, Delete */
        UPDATE,
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


    public static final List<Action> VALUES = List.of(Action.values());


    public CallableStatement getCallableStatement(Connection connection) throws SQLException {
        return connection.prepareCall(storedProcedureString);
    }

    /**
     * Set parameters. Assumes map has all necessary fields and that they are all valid.
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


    /** An argument the user must provide to the Action. */
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
        public enum PType { INT, STRING, DATE }

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