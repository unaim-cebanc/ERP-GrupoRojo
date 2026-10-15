import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
public class Main {
    public void main(String[] args) {
        Properties p = new Properties();
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("Error al leer config.properties: " + e.getMessage());
        }
        try (Scanner sc = new Scanner(System.in)) {
            iniciarSesion(sc, p);
            System.exit(0);
        }
    }
    
    private void iniciarSesion(Scanner scanner, Properties p) {
        while (true) { 
            System.out.println("====== INCIAR SESIÓN ======");
            String usuario = leerString(scanner, "Usuario: ");
            String contraseña = leerString(scanner, "Contraseña: ");
            if (p.getProperty(usuario) == null) {
                System.out.println("Error: Usuario introducido no existe");
            } else if (!p.getProperty(usuario).equals(contraseña)) {
                System.out.println("Error: Contraseña incorrecta");
            } else {
                mostarMenu(scanner, usuario, contraseña, p.getProperty("db.url"));
                break;
            }
        }
        
    }
    
    public static void mostarMenu(Scanner scanner, String usuario, String contraseña, String url) {
        int opcion;
        do {
            System.out.println("====== MENU ====== ");
            System.out.println("1. CRM");
            System.out.println("0. Salir");
            opcion = leerInt(scanner, "> ");
            ejecutar(usuario, contraseña, url, opcion);
        } while (opcion != 0);
    }
    
    private static void ejecutar(String usuario, String contraseña, String url, int opcion) {
        switch (opcion) {
            case 1 -> {
                if (usuario.equals("deca_crm")) {
                    Crm crm = new Crm(usuario, contraseña, url);
                    crm.cargarCrm(usuario, contraseña, url);
                } else { System.out.println("Error: Usuario actual no tiene permisos para entrar"); }
            }
            case 0 -> System.exit(0);
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void probarConexion(Properties p, String usuario, String contraseña) {
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
    public static int leerInt(Scanner scanner, String mensaje) {
        while (true) { 
            System.out.print(mensaje);
            String entrada = scanner.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {System.out.println("Error:" + e.getMessage());}
        }
    }
}
// aplicacion = funciona;
// estetica = bonito;
// funciones = muchas;