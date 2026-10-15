import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Properties p = new Properties();
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("Error al leer config.properties: " + e.getMessage());
            return;
        }
        try (Scanner sc = new Scanner(System.in)) {
            iniciarSesion(sc, p);
            System.exit(0);
        }
    }
    
    public static void iniciarSesion(Scanner scanner, Properties p) {
        while (true) { 
            System.out.println("====== INCIAR SESIÓN ======");
            String usuario = leerString(scanner, "Usuario: ");
            String contraseña = leerString(scanner, "Contraseña: ");
            if (p.getProperty(usuario) == null) {
                System.out.println("Error: Usuario introducido no existe");
            } else if (!p.getProperty(usuario).equals(contraseña)) {
                System.out.println("Error: Usuario o contraseña incorrectos");
            } else {
                probarConexion(p, usuario, contraseña);
                break;
            }
        }
        
    }
    
    public static void probarConexion(Properties p, String usuario, String contraseña) {
        try (Connection conn = DriverManager.getConnection(
            p.getProperty("db.url"), usuario, contraseña )) {
            System.out.println("Conexión exitosa");
        } catch (SQLException e) {System.out.println("Error: " + e.getMessage());}
    }
    
    public static String leerString(Scanner scanner, String mensaje) {
        System.out.print(mensaje);
        String entrada = scanner.nextLine().trim();
        return entrada;
    }
}
// aplicacion = funciona;
// estetica = bonito;
// funciones = muchas;