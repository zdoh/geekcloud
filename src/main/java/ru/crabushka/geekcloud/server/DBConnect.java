package ru.crabushka.geekcloud.server;

import java.sql.*;

public class DBConnect {
    private static String url = "jdbc:sqlite:db.sqlite";

    private static Connection connection;
    private static Statement statement;

    static public void connect() throws SQLException {
        connection = DriverManager.getConnection(url);
    }

    static public String getNickname(String username, String password) {
        String nickname = null;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement("select login from users where login = ? and passwd = ?;");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                nickname = resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nickname;
    }

}
