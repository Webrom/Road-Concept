package fr.enssat.lanniontech.api.repositories;

import fr.enssat.lanniontech.api.repositories.connectors.SQLDatabaseConnector;
import fr.enssat.lanniontech.api.utilities.Constants;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLTests {

    public static void main(String[] args) throws ClassNotFoundException {

        SQLDatabaseConnector.configure(Constants.ACTIVE_SGBD);

        URL location = SQLTests.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());

        try (Connection connection = SQLDatabaseConnector.getConnection()) {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

             statement.executeUpdate("drop table if exists person");
             statement.executeUpdate("create table person (id integer, name VARCHAR )");
             statement.executeUpdate("insert into person values(1, 'leo')");
             statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
