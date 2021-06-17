package server.postgres;

public class DBFactory {
    private static DBService service = null;
    private static String str = "docker";
    public static synchronized DBInterface getInstance() {
        if (service == null) {
            service = new DBService(new PGDataSource(str,str),new DBRepository());
        }
        return service;
    }
}
