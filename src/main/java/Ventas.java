import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Ventas {
    
    private final String usuario;
    private final String contraseña;
    private final String url;
    record Producto(int id, String nombre, int cantidad, int precio) {}
    
    public Ventas(String usuario, String contraseña, String url) {
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.url = url;
    }
    
    public void cargarVentas(String usuario, String contraseña, String url) {
        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do { 
                menuVentas();
                opcion = Main.leerInt(scanner, "> ");
                ejecutarVentas(opcion, scanner);
            } while (opcion != 0);
        }
    }
    
    private void menuVentas() {
        System.out.println("====== Ventas ======");
        System.out.println("1. Crear presupuesto");
        System.out.println("2. Confirmar presupuesto");
    }
    
    private void ejecutarVentas(int opcion, Scanner scanner) {
        while (true) { 
            switch (opcion) {
                case 1 -> añadirPresupuesto(scanner);
                case 2 -> confirmarPresupuesto(scanner);
                default -> System.out.println("Error: Opción introducida incorrecta");
            }
        }
    }
    
    private void añadirPresupuesto(Scanner scanner) {
        List<Producto> productos = new ArrayList<>();
        String seguir;
        while (true) { 
            
            System.out.println("====== Nuevo Presupuesto ======");
            String cliente = Main.leerString(scanner, "Cliente: ");
            int idOportunidad = Main.leerInt(scanner, "Id oportunidad asociada: ");
            
            do {
                String producto = Main.leerString(scanner, "Producto: ");
                int cantidad = Main.leerInt(scanner, "Cantidad: ");
                
                try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                    PreparedStatement st = conn.prepareStatement("SELECT id, precio FROM productos WHERE nombre = ?");
                    st.setString(1, producto);
                    ResultSet rs = st.executeQuery();
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        int precio = rs.getInt("precio");
                        productos.add(new Producto(id, producto, cantidad, precio));
                    } else {
                        System.out.println("Error: El producto " + producto + " no existe");
                        return;
                    }
                } catch (SQLException e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }
                System.out.print("¿Quieres añadir otro producto? (s/n): ");
                seguir = scanner.nextLine().trim();
            } while (seguir.equalsIgnoreCase("s"));
            
            LocalDate fechaValidez = LocalDate.parse(Main.leerString(scanner, "Fecha validez (yyyy-MM-dd): "));
            String observaciones = Main.leerString(scanner, "Observaciones: ");
            
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement stId = conn.prepareStatement("SELECT id FROM clientes WHERE nombre = ?");
                stId.setString(1, cliente);
                ResultSet rsId = stId.executeQuery();
                
                if (rsId.next()) {
                    int idCliente = rsId.getInt("id");
                    
                    PreparedStatement st = conn.prepareStatement("INSERT INTO presupuestos" +
                    "(cliente_id, oportunidad_id, fecha_validez, observaciones)" + 
                    "VALUES (?, ?, ?, ?)");
                    
                    st.setInt(1, idCliente);
                    st.setInt(2, idOportunidad);
                    st.setObject(3, fechaValidez);
                    st.setString(4, observaciones);
                    
                    st.executeUpdate();
                } else { System.out.println("Error: Cliente introducido no existe"); }
            } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
            
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement psPresupuesto = conn.prepareStatement("SELECT id FROM presupuestos ORDER BY id DESC LIMIT 1;");
                ResultSet rs = psPresupuesto.executeQuery();
                
                if (rs.next()) {
                    int idPresupuesto = rs.getInt("id");
                    PreparedStatement ps = conn.prepareStatement("INSERT INTO lineas_presupuesto (presupuesto_id, producto_id, cantidad, precio_unitario)");
                    for (Producto p : productos) {
                        ps.setInt(1, idPresupuesto);
                        ps.setInt(2, p.id());
                        ps.setInt(3, p.cantidad());
                        ps.setInt(4, p.precio());
                        ps.addBatch();
                    }
                    
                    ps.executeBatch();
                }
            } catch (SQLException e) {
                System.err.println("Error al insertar: " + e.getMessage());
                // al cerrarse la conexión sin commit, se descarta la transacción
            }
            System.out.println("Se ha creado el presupuesto correctamente");
            break;
        }
    }
    
    private void confirmarPresupuesto(Scanner scanner) {
        while (true) {
            System.out.println("====== Confirmar presupuesto ======");
            int id = Main.leerInt(scanner, "Introduce el id del presupuesto a aceptar: ");
            System.out.println("Se establecera el estado del presupuesto nº " + id + " como 'aceptado'.");
            String conf = Main.leerString(scanner, "Seguro que quieres proceder? (y/n): ").toLowerCase();
            switch (conf) {
                case "y" -> {
                    if (Main.verificarId(id, "presupuestos")) {
                        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                            PreparedStatement st = conn.prepareStatement("UPDATE presupuestos SET estado = 'aceptado' WHERE id = ?");
                            
                            st.setInt(1, id);
                            st.executeUpdate();
                            return;
                        } catch (SQLException e) {
                            System.out.println("Error: " + e.getMessage());
                            return;
                        }
                    } else {
                        System.out.println("Presupuesto con id " + id + " no existe");
                        return;
                    }
                }
                case "n" -> {
                    System.out.println("Acción cancelada");
                    return;
                }
            }
        }
    }
}