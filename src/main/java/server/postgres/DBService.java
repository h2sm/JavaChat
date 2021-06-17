package server.postgres;

import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBService implements DBInterface {
    private final DataSource src;
    private final DBRepository repository;

    public DBService(DataSource ds, DBRepository dbr) {
        this.src=ds;
        this.repository=dbr;
    }

    @Override
    @SneakyThrows
    public boolean authenticate(String name, String password) {
        var auth = false;
        try {
            var conn = src.getConnection();
            auth = repository.auth(name, password, conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return auth;
    }

    @Override
    public void saveMessage(String name, String msg) {
        try {
            var conn = src.getConnection();
            repository.saveMessage(name,msg,conn);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<String> loadMessages() {
        ArrayList<String> list = null;
        try {
            var conn = src.getConnection();
            list = repository.returnLast20Messages(conn);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return list;
    }
}
