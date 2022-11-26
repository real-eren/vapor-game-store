package edu.cwru.csds341.vapor.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ad-hoc testing
 */
public class DBPlaceholder {
    public static void main(String[] args) throws IOException {
    
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("example.credentials"));
             Statement statement = connection.createStatement();) {

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
