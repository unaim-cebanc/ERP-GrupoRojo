
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public class Crm {
    
    private final String usuario;
    private final String contraseña;
    private final String url;
    
    public Crm(String usuario, String contraseña, String url) {
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.url = url;
    }
    
    public void cargarCrm(String usuario, String contraseña, String url) {
        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do { 
                menuCrm();
                opcion = Main.leerInt(scanner, "> ");
                ejecutarCrm(opcion, scanner);
            } while (opcion != 0);
        }
    }
    
    private void menuCrm() {
        System.out.println("====== CRM ======");
        System.out.println("1. Añadir oportunidad");
        System.out.println("2. Ver oportunidad");
        System.out.println("3. Confirmar presupuesto");
        System.out.println("4. Eliminar oportunidad");
        System.out.println("0. Volver");
    }
    
    private void ejecutarCrm(int opcion, Scanner scanner) {
        switch (opcion) {
            case 1 -> añadirOportunidad(scanner);
            case 0 -> Main.mostarMenu(scanner, usuario, contraseña, url);
            default -> System.out.println("Opcion introducida incorrecta");
        }
    }
    
    private void añadirOportunidad(Scanner scanner) {
        while (true) { 
            System.out.println("====== Nueva Oportunidad ======");
            String cliente = Main.leerString(scanner, "Cliente: ");
            String titulo = Main.leerString(scanner, "Título: ");
            int importe = Main.leerInt(scanner, "Importe estimado: ");
            int probabilidad = Main.leerInt(scanner, "Probabilidad: ");
            LocalDate fechaCierrePrevista = LocalDate.parse(Main.leerString(scanner, "Fecha cierre prevista: "));
            LocalDate fechaCierre = LocalDate.parse(Main.leerString(scanner, "Fecha cierre: "));
            
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement stId = conn.prepareStatement("SELECT id FROM clientes WHERE nombre = ?");
                stId.setString(1, cliente);
                ResultSet rsId = stId.executeQuery();
                
                if (rsId.isBeforeFirst()) {
                    rsId.next();
                    int idCliente = rsId.getInt(1);
                    
                    PreparedStatement st = conn.prepareStatement("INSERT INTO oportunidades" +
                    "(cilente_id, titulo, importe_estimado, probabilidad, fecha_cierre_prevista, fecha_cierre)" + 
                    "VALUES (?, ?, ?, ?, ?, ?)");
                    
                    st.setInt(1, idCliente);
                    st.setString(2, titulo);
                    st.setInt(3, importe);
                    st.setInt(4, probabilidad);
                    st.setObject(5, fechaCierrePrevista);
                    st.setObject(6, fechaCierre);
                    
                    st.executeUpdate();
                } else { System.out.println("Error: Cliente introducido no existe"); }
            } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
            System.out.println("Se ha creado la oportunidad correctamente");
            break;
        }
    }
}
