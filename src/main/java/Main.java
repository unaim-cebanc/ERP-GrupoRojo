import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Main {
    public static void main(String[] args) {
        probarConexion();
    }
    
    public static void  mostrarMenu() {
        System.out.println("|||=== === === MENÚ === === ===|||");
        System.out.println("1. Hacer cosas");
    }
    public static void probarConexion() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/my_sql")) {
            System.out.println("Conexión exitosa");
        } catch (SQLException e) {System.out.println("Error: " + e.getMessage());}
    }
}
// aplicacion = funciona;
// estetica = bonito;
// funciones = muchas;