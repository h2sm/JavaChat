package server.postgres;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PostgresHandlerTest {

    @Test
    void saveMessage() throws Exception {//используют имитацию БД - h2
        var loginAndPass = "docker";
        var msg = "TestUnit123";
        var name = "MASHA";
        var conn = PostgresHandler.getInstance(loginAndPass, loginAndPass);
        conn.saveMessage(name, msg);//ассерты - достать из БД сохраненное сообщение - проверить, то ли сохранилось

    }

    @Test
    void returnLast20Messages() {
    }

    @Test
    void authenticate() {
    }

    @Test
    void getInstance() {
    }
}