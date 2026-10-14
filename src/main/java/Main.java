import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
public class Main {
    public static void main(String[] args) {
        Properties p = new Properties();
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("Error al leer config.properties: " + e.getMessage());
            return;
        }
        probarConexion(p);
    }
    
    public static void  mostrarMenu() {
        System.out.println("|||=== === === MENÚ === === ===|||");
        System.out.println("1. Hacer cosas");
    }
    public static void probarConexion(Properties p) {
        try (Connection conn = DriverManager.getConnection(
            p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.pass"))) {
            System.out.println("Conexión exitosa");
            PreparedStatement st = conn.prepareStatement("SELECT * FROM clientes");
            ResultSet rs = st.executeQuery();
            System.out.println(rs);
        } catch (SQLException e) {System.out.println("Error: " + e.getMessage());}
    }
}
// aplicacion = funciona;
// estetica = bonito;
// funciones = muchas;