package edu.cwru.csds341.vapor.common;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ad-hoc testing
 */
public class DBPlaceholder {
    public static void main(String[] args) throws IOException {
        var params = Connections.loadFromFile(Path.of("./example.ini"));
    
        try (Connection connection = Connections.makConnection(params);
             Statement statement = connection.createStatement();) {

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
