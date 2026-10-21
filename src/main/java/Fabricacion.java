import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Fabricacion {

    private final String usuario;
    private final String contraseña;
    private final String url;
    
    public Fabricacion(String usuario, String contraseña, String url) {
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.url = url;
    }
    
    public void cargarFabricacion(String usuario, String contraseña, String url) {
        try (Scanner sc = new Scanner(System.in)) {
            menuFabricacion();
            int opcion = Main.leerInt(sc, "> ");
            ejecutarFabricacion(opcion, sc);
        }
    }
    
    private void menuFabricacion() {
        System.out.println("\n====== Fabricación ======");
        System.out.println("1. Consultar fabricación");
        System.out.println("0. Volver");
    }
    
    private void ejecutarFabricacion(int opcion, Scanner sc) {
        switch (opcion) {
            case 1 -> consultarFabricacion();
            case 0 -> Main.mostarMenu(sc, usuario, contraseña, url);
            default -> System.out.println("Error: Opción incorrecta.");
        }
    }
    
    private void consultarFabricacion() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM ordenes_fabricacion");
            ResultSet rs = st.executeQuery();
            
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                System.err.println("\n=== Ordenes Fabricación ===\n");
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
