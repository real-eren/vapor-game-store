package edu.cwru.csds341.vapor.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBPlaceholder {
    // Connect to your database.
    // Replace server name, username, and password with your credentials
    // TODO: pull connection credentials into config file (excluded from VCS)
    public static void main(String[] args) {
        String connectionUrl = 
        "jdbc:sqlserver://localhost;"
                + "database=__;"
                + "user=__;"
                + "password=__;"
                + "encrypt=true;"
                + "trustServerCertificate=true;"
                + "loginTimeout=__;";

        try (Connection connection = DriverManager.getConnection(connectionUrl);
                Statement statement = connection.createStatement();) {

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
