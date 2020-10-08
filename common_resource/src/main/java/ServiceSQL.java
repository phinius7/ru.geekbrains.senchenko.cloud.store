import java.sql.*;

public class ServiceSQL {
    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:loginDB.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) throws SQLException {
        String sql = String.format("SELECT nickname FROM main where " +
                "login = '%s' and password = '%s'", login, pass);
        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

    public static boolean isLogin(String nick) throws SQLException {
        String sql = String.format("SELECT id FROM main where nickname = '%s'", nick);
        ResultSet rs = stmt.executeQuery(sql);
        return rs.next();
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
