
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Ventas {
    
    private final String usuario;
    private final String contraseña;
    private final String url;
    record Producto(int id, String nombre, int cantidad, int precio) {}
    record ProductoFabricar(int id, int idPedido, int idProveedor, boolean fabricable, int cantidad, int existencias, BigDecimal costo) {}
    
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
        System.out.println("====== Confirmar presupuesto ======");
        int id = Main.leerInt(scanner, "Introduce el id del presupuesto a aceptar: ");
        System.out.println("Se establecerá el estado del presupuesto nº " + id + " como 'aceptado'.");
        // Se repite hasta recibir y/n (antes, cualquier otra entrada provocaba un bucle infinito)
        String conf;
        do {
            conf = Main.leerString(scanner, "¿Seguro que quieres proceder? (y/n): ").trim().toLowerCase();
        } while (!conf.equals("y") && !conf.equals("n"));
        if (conf.equals("n")) {
            System.out.println("Acción cancelada");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            conn.setAutoCommit(false); // todo o nada
            try {
                procesarConfirmacion(conn, id);
                conn.commit();
                System.out.println("Presupuesto nº " + id + " aceptado correctamente");
            } catch (SQLException | IllegalStateException e) {
                conn.rollback(); // no queda nada a medias
                System.out.println("Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
    
    private void procesarConfirmacion(Connection conn, int idPresupuesto) throws SQLException {
        int idPedido;
        
        try (PreparedStatement st = conn.prepareStatement("SELECT estado, pedido_id FROM presupuestos WHERE id = ? FOR UPDATE")) {
            st.setInt(1, idPresupuesto);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("El presupuesto " + idPresupuesto + " no existe");
                }
                if ("aceptado".equals(rs.getString("estado"))) {
                    throw new IllegalStateException("El presupuesto ya estaba aceptado");
                }
                idPedido = rs.getInt("pedido_id");
                if (rs.wasNull()) {
                    throw new IllegalStateException("El presupuesto no tiene un pedido asociado");
                }
            }
        }
        
        try (PreparedStatement st = conn.prepareStatement("UPDATE presupuestos SET estado = 'aceptado' WHERE id = ?")) {
            st.setInt(1, idPresupuesto);
            st.executeUpdate();
        }
        
        List<ProductoFabricar> productos = new ArrayList<>();
        String sql = """
            SELECT lp.producto_id, pr.proveedor_id, pr.fabricable, pr.costo, lp.cantidad,
            COALESCE(e.cantidad, 0) AS existencias
            FROM lineas_presupuesto AS lp
            INNER JOIN productos AS pr ON pr.id = lp.producto_id
            LEFT JOIN existencias AS e ON e.producto_id = pr.id AND e.almacen_id = 1
            WHERE lp.presupuesto_id = ?
        """;
        
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, idPresupuesto);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    productos.add(new ProductoFabricar(
                        rs.getInt("producto_id"),
                        idPedido,
                        rs.getInt("proveedor_id"),
                        rs.getBoolean("fabricable"),
                        rs.getInt("cantidad"),
                        rs.getInt("existencias"),
                        rs.getBigDecimal("costo")));
                }
            }
        }
        
        if (productos.isEmpty()) {
            throw new IllegalStateException("El presupuesto no tiene líneas");
        }
        
        Map<Integer, List<ProductoFabricar>> comprasPorProveedor = new HashMap<>();
        String insertOrden = "INSERT INTO ordenes_fabricacion (producto_id, almacen_id, cantidad) VALUES (?, 1, ?)";
        String insertEntrega = "INSERT INTO entregas (pedido_id, producto_id, cantidad_entregada) VALUES (?, ?, ?)";
        
        try (PreparedStatement stOf = conn.prepareStatement(insertOrden);
            PreparedStatement stEntrega = conn.prepareStatement(insertEntrega)) {
            for (ProductoFabricar p : productos) {
                if (p.fabricable()) {
                    stOf.setInt(1, p.id());
                    stOf.setInt(2, p.cantidad());
                    stOf.addBatch();
                } else if (p.existencias() >= p.cantidad()) {
                    stEntrega.setInt(1, p.idPedido());
                    stEntrega.setInt(2, p.id());
                    stEntrega.setInt(3, p.cantidad());
                    stEntrega.addBatch();
                } else {
                    comprasPorProveedor
                        .computeIfAbsent(p.idProveedor(), k -> new ArrayList<>())
                        .add(p);
                }
            }
            stOf.executeBatch();
            stEntrega.executeBatch();
        }
        
        try (PreparedStatement stCompra = conn.prepareStatement(
            "INSERT INTO compras (proveedor_id, almacen_id) VALUES (?, 1)",
            Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stLinea = conn.prepareStatement(
            "INSERT INTO lineas_compra (compra_id, producto_id, cantidad, costo_unitario) VALUES (?, ?, ?, ?)")) {
            
                for (Map.Entry<Integer, List<ProductoFabricar>> entry : comprasPorProveedor.entrySet()) {
                stCompra.setInt(1, entry.getKey());
                stCompra.executeUpdate();
                
                int idCompra;
                try (ResultSet keys = stCompra.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se pudo obtener el id de la compra");
                    }
                    idCompra = keys.getInt(1);
                }
                
                for (ProductoFabricar p : entry.getValue()) {
                    stLinea.setInt(1, idCompra);
                    stLinea.setInt(2, p.id());
                    stLinea.setInt(3, p.cantidad() - p.existencias());
                    stLinea.setBigDecimal(4, p.costo);
                    stLinea.addBatch();
                }
                stLinea.executeBatch();
            }
        }
    }
}