import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import  java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class Inventario {
    private final String url;
    private final String usuario;
    private final String contraseña;
    
    public Inventario(String url, String usuario, String contraseña) {
        this.url = url;
        this.usuario = usuario;
        this.contraseña = contraseña;
    }
    
    public void cargarInventario(String url, String usuario, String contraseña) {
        try (Scanner sc = new Scanner(System.in)) {
            int opcion;
            do {
                menuInventario();
                opcion = Main.leerInt(sc, "> ");
                ejecutarInventario(sc, opcion);
            } while (opcion != 0);
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
            case 0 -> Main.mostarMenu(sc, usuario, contraseña, url);
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
                
                System.err.println("\n=== Inventario ===\n");
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
                    st.executeUpdate();
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
            case 0 -> cargarInventario(url, usuario, contraseña);
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
                
                System.err.println("\n=== Recepciones ===\n");
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
                        
                        if (rs.isBeforeFirst()) {
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
            case 0 -> cargarInventario(url, usuario, contraseña);
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
                
                System.err.println("\n=== Entregas ===\n");
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
                        
                        if (rs.isBeforeFirst()) {
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
            case 0 -> cargarInventario(url, usuario, contraseña);
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
                
                System.err.println("\n=== OF ===\n");
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
    
    private void confirmarFabricacion(Scanner sc) {
        while (true) { 
            System.out.println("\n====== Confirmar OF ======");
            int id = Main.leerInt(sc, "Introduce el id de la OF a confirmar: ");
            String conf = Main.leerString(sc, "Seguro que quieres confirmar la OF con id " + id + "? (y/n): ");
            switch (conf) {
                case "y" -> {
                    try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                        PreparedStatement st = conn.prepareStatement("UPDATE ordenes_fabricacion SET estado = 'terminada', fecha_fin = NOW() WHERE id = ?");
                        st.setInt(1, id);
                        st.executeUpdate();
                        
                        PreparedStatement st2 = conn.prepareStatement("SELECT producto_id, cantidad FROM ordenes_fabricacion WHERE id = ?");
                        st2.setInt(1, id);
                        ResultSet rs = st2.executeQuery();
                        
                        if (rs.isBeforeFirst()) {
                            int idProducto = rs.getInt("producto_id");
                            int cantidadOrden = rs.getInt("cantidad");
                            
                            PreparedStatement st3 = conn.prepareStatement("SELECT componenete_id, cantidad FROM lista_materiales WHERE producto_id = ?");
                            st3.setInt(1, idProducto);
                            ResultSet rs2 = st3.executeQuery();
                            
                            TreeMap<Integer, Integer> materialesNecesarios = new TreeMap<>();
                            while (rs2.next()) {
                                materialesNecesarios.put(rs2.getInt("componente_id"), rs2.getInt("cantidad") * cantidadOrden);
                            }
                            
                            for (Map.Entry<Integer, Integer> material : materialesNecesarios.entrySet()) {
                                int idComponente = material.getKey();
                                int cantidad = material.getValue();
                                
                                PreparedStatement st4 = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad - ? WHERE producto_id = ?");
                                st4.setInt(1, cantidad);
                                st4.setInt(2, idComponente);
                                st4.executeUpdate();
                            }
                            
                            PreparedStatement st5 = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad + ? WHERE producto_id = ?");
                            st5.setInt(1, cantidadOrden);
                            st5.setInt(2, idProducto);
                            st5.executeUpdate();
                            
                            PreparedStatement st6 = conn.prepareStatement("""
                                SELECT p.id FROM pedidos AS p
                                INNER JOIN lineas_pedido AS lp ON lp.pedido_id = p.id
                                INNER JOIN productos AS pr ON pr.id = lp.producto_id
                                INNER JOIN ordenes_fabricacion AS f ON f.producto_id = pr.id
                                WHERE f.producto_id = lp.producto_id;
                            """);
                            
                            ResultSet rs3 = st6.executeQuery();
                            
                            if (rs3.next()) {
                                int idPedido = rs3.getInt("id");
                                PreparedStatement st7 = conn.prepareStatement("""
                                    INSERT INTO entregas (pedido_id, producto_id, cantidad_entregada, estado)
                                    VALUES (?, ?, ?, ?, ?)
                                """);
                                st7.setInt(1, idPedido);
                                st7.setInt(2, idProducto);
                                st7.setInt(3, cantidadOrden);
                                st7.setString(4, "enviada");
                                
                                st7.executeUpdate();
                            }
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
}
