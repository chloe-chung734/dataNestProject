import java.sql.DriverManager;

public class DbTest {
    public static void main(String[] args) {
        String password = "ewha52@DB.zieyou";
        String[] hosts = {"localhost", "127.0.0.1"};
        for (String host : hosts) {
            String url = "jdbc:mysql://" + host + ":3306/ewha?useSSL=false&allowPublicKeyRetrieval=true";
            try {
                DriverManager.getConnection(url, "root", password).close();
                System.out.println("OK: " + host);
            } catch (Exception e) {
                System.out.println("FAIL " + host + ": " + e.getMessage());
            }
        }
    }
}
