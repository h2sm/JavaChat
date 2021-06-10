package server.postgres;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public final class PostgresHandler {
    private final String username;
    private final String password;
    private static PostgresHandler handler;
    private static Connection connection;

    private PostgresHandler(String user, String pswrd) {
        this.username = user;
        this.password = pswrd;
        initialize();
    }

    private void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:postgresql:chatlog", username, password);
            //System.out.println("Database is connected");
        } catch (SQLException e) {
            System.out.println(e.getSQLState() + e.getMessage());
            System.out.println("SQL Exception at initializing");
        }
    }

    public void saveMessage(String name, String message) {
        try {
            var time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
            var date = LocalDate.now();
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            var msg = new String(bytes, StandardCharsets.UTF_8);
            var results = connection
                    .createStatement()
                    .executeQuery("insert into logchat values ('" + msg + "', '" + date + "'," +
                            "(select userid from oneuser where oneuser.username='" + name + "'), '" + time + "');");

            results.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ArrayList<String> returnLast20Messages() {
        var array = new ArrayList<String>();
        try {
            var result = connection
                    .createStatement()
                    .executeQuery("select usermsg from logchat limit 20;");
            while (result.next()){
                array.add(result.getString("usermsg"));
            }
            result.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return array;
    }

    public boolean authenticate(String uname, String pswrd) {//пароль для EGORIK-а - EGORIK. ну тот же самый то есть
        //Б - безопасность. веракрипт не спасет если сам дурак :D
        try {
            var resultSet = connection
                    .createStatement()
                    .executeQuery("select u.passhashcode, u.passsalt, u.username from oneuser u where u.username = '" + uname + "';");
            while (resultSet.next()) {
                var passSalt = resultSet.getInt("passsalt");
                var passHashcode = resultSet.getInt("passhashcode");
                if (pswrd.hashCode() == passHashcode && pswrd.hashCode() - 1 == passSalt) {
                    //System.out.println("USER ACCESS GRANTED");
                    return true;
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;//не
    }


    public static PostgresHandler getInstance(String user, String pswrd) {
        if (handler == null) {
            handler = new PostgresHandler(user, pswrd);
        }
        return handler;
    }
}
