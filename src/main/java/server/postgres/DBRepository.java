package server.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class DBRepository {

    public boolean auth(String name, String password, Connection conn) throws SQLException {
        var resultSet = conn
                .createStatement()
                .executeQuery("select u.passhashcode, u.passsalt, u.username from oneuser u where u.username = '" + name + "';");
        while (resultSet.next()) {
            var passSalt = resultSet.getInt("passsalt");
            var passHashcode = resultSet.getInt("passhashcode");
            if (password.hashCode() == passHashcode && password.hashCode() - 1 == passSalt) {
                //System.out.println("USER ACCESS GRANTED");
                return true;
            }
        }
        resultSet.close();
        return false;
    }

    public ArrayList<String> returnLast20Messages(Connection connection) throws SQLException {
        var array = new ArrayList<String>();
        var result = connection
                .createStatement()
                .executeQuery("select usermsg from logchat limit 20;");
        while (result.next()) {
            array.add(result.getString("usermsg"));
        }
        result.close();
        return array;
    }

    public void saveMessage(String name, String msg, Connection connection) throws SQLException{
        var time = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
        var date = LocalDate.now();
        var results = connection
                .createStatement()
                .executeQuery("insert into logchat values ('" + msg + "', '" + date + "'," +
                        "(select userid from oneuser where oneuser.username='" + name + "'), '" + time + "');");

        results.close();
    }
}
