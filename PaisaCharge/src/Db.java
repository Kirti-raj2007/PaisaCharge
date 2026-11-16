import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Unit 4: JDBC connection. Change USER and PASS to match your MySQL setup. */
public class Db {
    static final String URL = "jdbc:mysql://localhost:3306/paisacharge?useSSL=false&serverTimezone=Asia/Kolkata";
    static final String USER = "root";
    static final String PASS = "your_password";

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
