import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Inventario {
    private final String url;
    private final String usuario;
    private final String contraseña;
    
    public Inventario(String url, String usuario, String contraseña) {
        this.url = url;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }
    
    public void cargarInventario(String url, String usuario, String contraseña, Scanner sc) {
        try {
            int opcion;
            do {
                menuInventario();
                opcion = Main.leerInt(sc, "> ");
                ejecutarInventario(sc, opcion);
            } while (opcion != 0);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void menuInventario() {
        System.out.println("\n====== Inventario ======");
        System.out.println("1. Mostrar inventario");
        System.out.println("2. Modificar inventario");
        System.out.println("3. Recepciones");
        System.out.println("4. Entregas");
        System.out.println("5. Fabricaciones");
        System.out.println("0. Volver");
    }
    
    private void ejecutarInventario(Scanner sc, int opcion) {
        switch (opcion) {
            case 1 -> mostrarInventario();
            case 2 -> modificarInventario(sc);
            case 3 -> {
                do { 
                    menuRecepcion();
                    opcion = Main.leerInt(sc, "> ");
                    ejecutarRecepcion(sc, opcion);
                } while (opcion != 0);
            }
            case 4 -> {
                do { 
                    menuEntrega();
                    opcion = Main.leerInt(sc, "> ");
                    ejecutarEntrega(sc, opcion);
                } while (opcion != 0);
            }
            case 5 -> {
                do {
                menuFabricacion();
                opcion = Main.leerInt(sc, "> ");
                ejecutarFabricacion(sc, opcion);
                } while (opcion != 0);
            }
            case 0 -> {}
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void mostrarInventario() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            PreparedStatement st = conn.prepareStatement("""
            SELECT
                p.id AS 'ID',
                p.sku AS 'SKU',
                p.nombre AS 'NOMBRE',
                p.categoria AS 'CATEGORIA',
                p.precio AS 'PRECIO',
                e.almacen_id AS 'ALMACEN',
                e.cantidad AS 'STOCK ACTUAL'
            FROM productos AS p 
            INNER JOIN existencias AS e WHERE p.id = e.producto_id 
            """);
            
            ResultSet rs = st.executeQuery();
            
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                System.out.println("\n=== Inventario ===\n");
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            } else {
                System.out.println("No se han encontrado resultados");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void modificarInventario(Scanner sc) {
        while (true) { 
            System.out.println("\n====== Modificar Inventario ======");
            String nombre = Main.leerString(sc, "Introduce el nombre del producto a modificar: ");
            int cantidad = Main.leerInt(sc, "Cantidad nueva: ");
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement st = conn.prepareStatement("SELECT id FROM productos WHERE nombre = ?");
                st.setString(1, nombre);
                ResultSet rs = st.executeQuery();
                
                if (rs.next()) {
                    int id = rs.getInt("id");
                    PreparedStatement st2 = conn.prepareStatement("UPDATE existencias SET cantidad = ? WHERE producto_id = ?");
                    st2.setInt(1, cantidad);
                    st2.setInt(2, id);
                    st2.executeUpdate();
                    return;
                } else {
                    System.out.println("Error: No se ha encontrado el producto " + nombre);
                    return;
                }
                
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
                return;
            }
        }
    }
    
    // --- RECEPCIONES ---
    private void menuRecepcion() {
        System.out.println("\n====== Recepciones ======");
        System.out.println("1. Listar Recepciones");
        System.out.println("2. Confirmar Recepción");
        System.out.println("0. Volver");
    }
    
        private void ejecutarRecepcion(Scanner sc, int opcion) {
        switch (opcion) {
            case 1 -> listarRecepciones();
            case 2 -> confirmarRecepcion(sc);
            case 0 -> {}
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void listarRecepciones() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            System.out.println("====== Listar Recepciones ======");
            PreparedStatement st = conn.prepareStatement("SELECT * FROM recepciones");
            ResultSet rs = st.executeQuery();
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                System.out.println("\n=== Recepciones ===\n");
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            } else {
                System.out.println("No se encontraron datos");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void confirmarRecepcion(Scanner sc) {
        while (true) { 
            System.out.println("\n====== Confirmar Recepción ======");
            int id = Main.leerInt(sc, "Introduce el id de la recepción a confirmar: ");
            String conf = Main.leerString(sc, "Seguro que quieres confirmar la recepción con id " + id + "? (y/n): ");
            switch (conf) {
                case "y" -> {
                    try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                        PreparedStatement st = conn.prepareStatement("UPDATE recepciones SET estado = 'recibida', fecha_recepcion = NOW() WHERE id = ?");
                        st.setInt(1, id);
                        st.executeUpdate();
                        
                        PreparedStatement st2 = conn.prepareStatement("SELECT producto_id, cantidad_recibida FROM recepciones WHERE id = ?");
                        st2.setInt(1, id);
                        ResultSet rs = st2.executeQuery();
                        
                        if (rs.next()) {
                            int idProducto = rs.getInt("producto_id");
                            int cantidad = rs.getInt("cantidad_recibida");
                            
                            PreparedStatement st3 = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad + ? WHERE producto_id = ?");
                            st3.setInt(1, cantidad);
                            st3.setInt(2, idProducto);
                            st3.executeUpdate();
                            
                        } else {
                            System.out.println("Error: No se han encontrado los datos relacionados con la recepcion " + id);
                            return;
                        }
                        System.out.println("Operación realizada con éxito");
                        return;
                    } catch (SQLException e) {
                        System.out.println("Error: " + e.getMessage());
                        return;
                    }
                }
                case "n" -> {
                    System.out.println("Operación cancelada");
                    return;
                }
                default -> System.out.println("Error: Opción introducida incorrecta");
            }
        }
    }
    
    // --- ENTREGAS ---
    private void menuEntrega() {
        System.out.println("\n====== Entregas ======");
        System.out.println("1. Listar Entregas");
        System.out.println("2. Confirmar Entregas");
        System.out.println("0. Volver");
    }
    
        private void ejecutarEntrega(Scanner sc, int opcion) {
        switch (opcion) {
            case 1 -> listarEntregas();
            case 2 -> confirmarEntrega(sc);
            case 0 -> {}
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void listarEntregas() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            System.out.println("====== Listar Entregas ======");
            PreparedStatement st = conn.prepareStatement("SELECT * FROM entregas");
            ResultSet rs = st.executeQuery();
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                System.out.println("\n=== Entregas ===\n");
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            } else {
                System.out.println("No se encontraron datos");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void confirmarEntrega(Scanner sc) {
        while (true) { 
            System.out.println("\n====== Confirmar Entrega ======");
            int id = Main.leerInt(sc, "Introduce el id de la entrega a confirmar: ");
            String conf = Main.leerString(sc, "Seguro que quieres confirmar la entrega con id " + id + "? (y/n): ");
            switch (conf) {
                case "y" -> {
                    try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                        PreparedStatement st = conn.prepareStatement("UPDATE entregas SET estado = 'entregada', fecha_entrega = NOW() WHERE id = ?");
                        st.setInt(1, id);
                        st.executeUpdate();
                        
                        PreparedStatement st2 = conn.prepareStatement("SELECT producto_id, cantidad_entregada FROM entregas WHERE id = ?");
                        st2.setInt(1, id);
                        ResultSet rs = st2.executeQuery();
                        
                        if (rs.next()) {
                            int idProducto = rs.getInt("producto_id");
                            int cantidad = rs.getInt("cantidad_entregada");
                            
                            PreparedStatement st3 = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad - ? WHERE producto_id = ?");
                            st3.setInt(1, cantidad);
                            st3.setInt(2, idProducto);
                            st3.executeUpdate();
                            
                        } else {
                            System.out.println("Error: No se han encontrado los datos relacionados con la recepcion " + id);
                            return;
                        }
                        System.out.println("Operación realizada con éxito");
                        return;
                    } catch (SQLException e) {
                        System.out.println("Error: " + e.getMessage());
                        return;
                    }
                }
                case "n" -> {
                    System.out.println("Operación cancelada");
                    return;
                }
                default -> System.out.println("Error: Opción introducida incorrecta");
            }
        }
    }
    
    // --- FABRICACION ---
    private void menuFabricacion() {
        System.out.println("\n====== Fabricación ======");
        System.out.println("1. Listar OF");
        System.out.println("2. Confirmar OF");
        System.out.println("0. Volver");
    }
    
        private void ejecutarFabricacion(Scanner sc, int opcion) {
        switch (opcion) {
            case 1 -> listarFabricacion();
            case 2 -> confirmarFabricacion(sc);
            case 0 -> {}
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void listarFabricacion() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            System.out.println("====== Listar OF ======");
            PreparedStatement st = conn.prepareStatement("SELECT * FROM ordenes_fabricacion");
            ResultSet rs = st.executeQuery();
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                System.out.println("\n=== OF ===\n");
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            } else {
                System.out.println("No se encontraron datos");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    record Componente(int id, int idProveedor, BigDecimal costo, int necesaria, int disponible) {
        int faltante() { return necesaria - disponible; }
    }
    
    private void confirmarFabricacion(Scanner sc) {
        System.out.println("\n====== Confirmar OF ======");
        int id = Main.leerInt(sc, "Introduce el id de la OF a confirmar: ");
        
        String conf;
        do {
            conf = Main.leerString(sc, "Seguro que quieres confirmar la OF con id " + id + "? (y/n): ").trim().toLowerCase();
            if (!conf.equals("y") && !conf.equals("n")) {
                System.out.println("Error: Opción introducida incorrecta");
            }
        } while (!conf.equals("y") && !conf.equals("n"));
        
        if (conf.equals("n")) {
            System.out.println("Operación cancelada");
            return;
        }
        
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            conn.setAutoCommit(false);
            try {
                procesarOF(conn, id);
                conn.commit();
            } catch (SQLException | IllegalStateException e) {
                conn.rollback();
                System.out.println("Error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        }
    }
    
    private void procesarOF(Connection conn, int idOF) throws SQLException {
        int idProducto;
        int idAlmacen;
        int cantidadOrden;
        try (PreparedStatement st = conn.prepareStatement(
                "SELECT producto_id, almacen_id, cantidad, estado FROM ordenes_fabricacion WHERE id = ? FOR UPDATE")) {
            st.setInt(1, idOF);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("La OF " + idOF + " no existe");
                }
                if ("terminada".equals(rs.getString("estado"))) {
                    throw new IllegalStateException("La OF ya estaba terminada");
                }
                idProducto = rs.getInt("producto_id");
                idAlmacen = rs.getInt("almacen_id");
                cantidadOrden = rs.getInt("cantidad");
            }
        }
        
        List<Componente> componentes = new ArrayList<>();
        String sql = """
            SELECT lm.componente_id, pr.proveedor_id, pr.costo, lm.cantidad * ? AS necesaria,
            COALESCE(e.cantidad, 0) AS disponible
            FROM lista_materiales AS lm
            INNER JOIN productos AS pr ON pr.id = lm.componente_id
            LEFT JOIN existencias AS e ON e.producto_id = lm.componente_id AND e.almacen_id = ?
            WHERE lm.producto_id = ?
            FOR UPDATE OF e
        """;
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, cantidadOrden);
            st.setInt(2, idAlmacen);
            st.setInt(3, idProducto);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    componentes.add(new Componente(
                        rs.getInt("componente_id"),
                        rs.getInt("proveedor_id"),
                        rs.getBigDecimal("costo"),
                        rs.getInt("necesaria"),
                        rs.getInt("disponible")));
                }
            }
        }
        
        if (componentes.isEmpty()) {
            throw new IllegalStateException("El producto " + idProducto + " no tiene lista de materiales");
        }
        
        List<Componente> faltantes = new ArrayList<>();
        for (Componente c : componentes) {
            if (c.faltante() > 0) {
                faltantes.add(c);
            }
        }
        
        if (faltantes.isEmpty()) {
            terminarOF(conn, idOF, idProducto, idAlmacen, cantidadOrden, componentes);
        } else {
            crearCompraPorFaltantes(conn, idOF, idAlmacen, faltantes);
        }
    }
    
    private void terminarOF(Connection conn, int idOF, int idProducto, int idAlmacen,
        int cantidadOrden, List<Componente> componentes) throws SQLException {
        
            try (PreparedStatement st = conn.prepareStatement(
                "UPDATE existencias SET cantidad = cantidad - ? WHERE almacen_id = ? AND producto_id = ?")) {
            for (Componente c : componentes) {
                st.setInt(1, c.necesaria());
                st.setInt(2, idAlmacen);
                st.setInt(3, c.id());
                st.addBatch();
            }
            st.executeBatch();
        }
        
        try (PreparedStatement st = conn.prepareStatement(
                "UPDATE existencias SET cantidad = cantidad + ? WHERE almacen_id = ? AND producto_id = ?")) {
            st.setInt(1, cantidadOrden);
            st.setInt(2, idAlmacen);
            st.setInt(3, idProducto);
            if (st.executeUpdate() == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO existencias (almacen_id, producto_id, cantidad) VALUES (?, ?, ?)")) {
                    ins.setInt(1, idAlmacen);
                    ins.setInt(2, idProducto);
                    ins.setInt(3, cantidadOrden);
                    ins.executeUpdate();
                }
            }
        }
        
        try (PreparedStatement st = conn.prepareStatement(
                "UPDATE ordenes_fabricacion SET estado = 'terminada', fecha_fin = NOW() WHERE id = ?")) {
            st.setInt(1, idOF);
            st.executeUpdate();
        }
        
        try (PreparedStatement st = conn.prepareStatement("""
                SELECT p.id FROM pedidos AS p
                INNER JOIN lineas_pedido AS lp ON lp.pedido_id = p.id
                WHERE lp.producto_id = ?
                ORDER BY p.fecha ASC
                LIMIT 1
                """)) {
            st.setInt(1, idProducto);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO entregas (pedido_id, producto_id, cantidad_entregada, estado) VALUES (?, ?, ?, ?)")) {
                        ins.setInt(1, rs.getInt("id"));
                        ins.setInt(2, idProducto);
                        ins.setInt(3, cantidadOrden);
                        ins.setString(4, "enviada");
                        ins.executeUpdate();
                    }
                }
            }
        }
        System.out.println("Operación realizada con éxito: OF " + idOF + " terminada");
    }
    
    private void crearCompraPorFaltantes(Connection conn, int idOF, int idAlmacen,
        List<Componente> faltantes) throws SQLException {
        Map<Integer, List<Componente>> porProveedor = new HashMap<>();
        for (Componente c : faltantes) {
            porProveedor.computeIfAbsent(c.idProveedor(), k -> new ArrayList<>()).add(c);
        }
        
        List<Integer> idsCompra = new ArrayList<>();
        try (PreparedStatement stCompra = conn.prepareStatement(
            "INSERT INTO compras (proveedor_id, almacen_id, observaciones) VALUES (?, ?, ?)",
            Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stLinea = conn.prepareStatement(
            "INSERT INTO lineas_compra (compra_id, producto_id, cantidad, costo_unitario) VALUES (?, ?, ?, ?)")) {
                for (Map.Entry<Integer, List<Componente>> entry : porProveedor.entrySet()) {
                stCompra.setInt(1, entry.getKey());
                stCompra.setInt(2, idAlmacen);
                stCompra.setString(3, "Generada por falta de stock en la OF " + idOF);
                stCompra.executeUpdate();
                
                int idCompra;
                try (ResultSet keys = stCompra.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No se pudo obtener el id de la compra");
                    }
                    idCompra = keys.getInt(1);
                }
                idsCompra.add(idCompra);
                
                for (Componente c : entry.getValue()) {
                    stLinea.setInt(1, idCompra);
                    stLinea.setInt(2, c.id());
                    stLinea.setInt(3, c.faltante());   // solo lo que falta
                    stLinea.setBigDecimal(4, c.costo());
                    stLinea.addBatch();
                }
                stLinea.executeBatch();
            }
        }
        System.out.println("\nNo hay stock suficiente. La OF " + idOF + " NO se ha confirmado.");
        System.out.println("Componentes que faltan:");
        for (Componente c : faltantes) {
            System.out.println("  - Producto " + c.id() + ": necesarios " + c.necesaria()
                + ", disponibles " + c.disponible() + ", faltan " + c.faltante());
        }
        System.out.println("Se han creado presupuestos de compra con id: " + idsCompra);
    }
}