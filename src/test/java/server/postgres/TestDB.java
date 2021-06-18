package server.postgres;

import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

import org.junit.*;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

public class TestDB {
    private TestDataSource dataSource = new TestDataSource();
    private DBService service = new DBService(dataSource, new DBRepository());
    private Connection mainConn;

    @Test
    void setup() {
        try {
            mainConn = dataSource.getConnection();
            testAdd(mainConn);
            compareAllData();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAdd(Connection connection) throws IOException, SQLException {
        var bytes = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("sqlcode.sql")).readAllBytes();
        var sql = new String(bytes);
        for (var stmt : sql.split(";")) {
            connection.createStatement().execute(stmt);
        }
    }

    @Test
    void compareAllData() throws SQLException {
        var result = service.loadMessages();
        var resultSet = mainConn.createStatement().executeQuery("SELECT usermsg FROM logchat;");
        var arr = new ArrayList<String>();
        while (resultSet.next()) {
            arr.add(resultSet.getString("usermsg"));
        }
        assertThat(arr).isEqualTo(result);
    }
}

