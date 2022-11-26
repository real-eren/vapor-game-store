package edu.cwru.csds341.vapor.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Collection of utilities for creating DB {@link Connection}s
 */
public final class Connections {
    /** Instances of this class are useless */
    private Connections() {}

    private static final String
            ADDRESS = "address",
            DB = "db",
            USERNAME = "username",
            PASSWORD = "password",
            /** Default "10" */
            TIMEOUT = "timeout",
            /** see {@link #DEBUG_OFF}, {@link #DEBUG_IGNORE}. Default {@link #DEBUG_OFF} */
            DEBUG = "debug";

    /**  */
    private static final String
            DEBUG_OFF = "0",
            DEBUG_IGNORE = "1";
    private static final Set<String> LEGAL_CONFIG_FIELDS = Set.of(ADDRESS, DB, USERNAME, PASSWORD, TIMEOUT, DEBUG);
    private static final Set<String> REQUIRED_CONFIG_FIELDS = Set.of(ADDRESS, DB, USERNAME, PASSWORD);

    /** 
     * Returns a Connection based on the contents of the given file.
     * The file should consist solely of lines of the form 'key=value'.
     * @throws IOException  if the file cannot be opened or read from.
     * @throws IllegalArgumentException  if the config file contains an illegal entry
     * @throws SQLException  if a database access error occurs
     * @throws java.sql.SQLTimeoutException  if the time limit for connecting expires
     */
    public static Connection fromFile(Path file) throws IOException, SQLException {
        var lines = Files.readAllLines(file);
        Map<String, String> args = new HashMap<>(REQUIRED_CONFIG_FIELDS.size());
        for (var line : lines) {
            if (line.isBlank() || line.startsWith("#")) continue;
            var tokens = line.split("=", 2);
            if (tokens.length < 2) throw new DBIllegalConfigException(file, "has line with improper formatting: " + line);
            var key = tokens[0].toLowerCase();
            var val = tokens[1];
            if (args.containsKey(key)) throw new DBIllegalConfigException(file, "contains duplicate entries for:" + key);
            if (! LEGAL_CONFIG_FIELDS.contains(key)) throw new DBIllegalConfigException(file, "contains unrecognized entry for: " + key);
            if (key.equals(DEBUG) && val.equals(DEBUG_IGNORE)) return null;
            args.put(key, val);
        }
        for (var key : REQUIRED_CONFIG_FIELDS)
            if (! args.containsKey(key)) throw new DBIllegalConfigException(file, "is missing an entry for: " + key);

        // all required fields present by this point
        String connectionUrl = String.format(
            "jdbc:sqlserver://%s;"
                + "database=%s;"
                + "user=%s;"
                + "password=%s;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=%s;",
                args.get(ADDRESS),
                args.get(DB),
                args.get(USERNAME),
                args.get(PASSWORD),
                args.getOrDefault(TIMEOUT, "10")
        );

        return DriverManager.getConnection(connectionUrl);
    }

    /** Issue with the DB credentials file */
    public static class DBIllegalConfigException extends RuntimeException {
        public DBIllegalConfigException(Path file, String problem) {
            super(String.format("DB credentials file '%s': %s", file, problem));
        }
    }
}
