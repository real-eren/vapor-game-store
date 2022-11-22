package edu.cwru.csds341.vapor.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Collection of utilities for creating DB connections
 */
public final class Connections {
    /** Instances of this class are useless */
    private Connections() {}

    public static final class Params {
        public final String address, db, username, password, timeout;

        public Params(String address, String db, String username, String password, String timeout) {
            this.address = address;
            this.db = db;
            this.username = username;
            this.password = password;
            this.timeout = timeout;
        }
    }

    public static Connection makConnection(Params params) throws SQLException {
        String connectionUrl = String.format(
            "jdbc:sqlserver://%s;"
                + "database=%s;"
                + "user=%s;"
                + "password=%s;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=%s;",
                params.address,
                params.db,
                params.username,
                params.password,
                params.timeout
        );

        return DriverManager.getConnection(connectionUrl);
    }

    private static final Set<String> LEGAL_CONFIG_FIELDS = Set.of("address", "db", "username", "password", "timeout");
    private static final Set<String> REQUIRED_CONFIG_FIELDS = Set.of("address", "db", "username", "password");

    /** 
     * Returns a Params instance based on the contents of the given file.
     * The file should consist solely of lines of the form 'key=value'.
     * @throws IOException  if the file cannot be opened or read from.
     * @throws IllegalArgumentException  if the config file contains an illegal entry
     */
    public static Params loadFromFile(Path file) throws IOException {
        var lines = Files.readAllLines(file);
        Map<String, String> args = new HashMap<>(5);
        for (var line : lines) {
            if (line.isBlank()) continue;
            if (line.startsWith("#")) continue;
            System.out.println(line);
            var tokens = line.split("=", 2);
            var key = tokens[0];
            var val = tokens[1];
            if (args.containsKey(key)) throw new IllegalArgumentException("DB credentials file contains duplicate entries for: " + key);
            if (! LEGAL_CONFIG_FIELDS.contains(key)) throw new IllegalArgumentException("DB credentials file contains unrecognized entry for: " + key);
            args.put(key, val);
        }
        for (var key : REQUIRED_CONFIG_FIELDS)
            if (! args.containsKey(key)) throw new IllegalArgumentException("DB credentials file is missing an entry for: " + key);

        // all required fields present by this point
        return new Params(
            args.get("address"),
            args.get("db"),
            args.get("username"),
            args.get("password"),
            args.getOrDefault("timeout", "30")
            );
    }

}
