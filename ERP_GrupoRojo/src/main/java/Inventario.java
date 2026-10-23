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
    
    public void mostrarInventario() {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            PreparedStatement st = conn.prepareStatement("""
            WITH RECURSIVE
 
-- 1) Explosión recursiva de materiales: producto -> componentes -> sub-componentes...
bom AS (
    SELECT lm.producto_id AS raiz,
           lm.componente_id,
           lm.cantidad * 1.0 AS cantidad_total,
           1 AS nivel
    FROM lista_materiales lm
    UNION ALL
    SELECT b.raiz,
           lm.componente_id,
           b.cantidad_total * lm.cantidad,
           b.nivel + 1
    FROM bom b
    JOIN lista_materiales lm ON lm.producto_id = b.componente_id
    WHERE b.nivel < 12
),
 
-- 2) Coste de materiales por producto raíz usando la explosión anterior
coste_bom AS (
    SELECT b.raiz AS producto_id,
           COUNT(*)                         AS num_componentes,
           MAX(b.nivel)                     AS profundidad,
           SUM(b.cantidad_total * p.precio) AS coste_materiales
    FROM bom b
    JOIN productos p ON p.id = b.componente_id
    GROUP BY b.raiz
),
 
-- 3) Matriz CARTESIANA clientes x productos con ventas calculadas por subconsulta correlacionada
matriz_cliente_producto AS (
    SELECT c.id AS cliente_id,
           c.nombre AS cliente,
           c.pais,
           p.id AS producto_id,
           p.nombre AS producto,
           p.categoria,
           (SELECT COALESCE(SUM(lp.cantidad * lp.precio_unitario), 0)
              FROM lineas_pedido lp
              JOIN pedidos pe ON pe.id = lp.pedido_id
             WHERE pe.cliente_id = c.id
               AND lp.producto_id = p.id)                         AS importe_pedidos,
           (SELECT COALESCE(SUM(lpr.cantidad * lpr.precio_unitario
                                * (1 - lpr.descuento_pct / 100)), 0)
              FROM lineas_presupuesto lpr
              JOIN presupuestos pr ON pr.id = lpr.presupuesto_id
             WHERE pr.cliente_id = c.id
               AND lpr.producto_id = p.id)                        AS importe_presupuestado,
           (SELECT COALESCE(SUM(en.cantidad_entregada), 0)
              FROM entregas en
              JOIN pedidos pe2 ON pe2.id = en.pedido_id
             WHERE pe2.cliente_id = c.id
               AND en.producto_id = p.id)                         AS unidades_entregadas
    FROM clientes c
    CROSS JOIN productos p
),
 
-- 4) Ranking con funciones ventana sobre la matriz
ranking AS (
    SELECT m.*,
           RANK()  OVER (PARTITION BY m.cliente_id ORDER BY m.importe_pedidos DESC)       AS rk_en_cliente,
           RANK()  OVER (PARTITION BY m.producto_id ORDER BY m.importe_pedidos DESC)      AS rk_en_producto,
           SUM(m.importe_pedidos) OVER (PARTITION BY m.pais, m.categoria
                                        ORDER BY m.importe_pedidos DESC
                                        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS acumulado_pais_cat,
           NTILE(100) OVER (ORDER BY m.importe_pedidos DESC)                              AS percentil
    FROM matriz_cliente_producto m
),
 
-- 5) Self-join de pedidos por proximidad temporal (non-equi join: ±30 días entre clientes distintos)
pedidos_cercanos AS (
    SELECT p1.id AS pedido_a,
           p1.cliente_id AS cliente_a,
           p2.id AS pedido_b,
           p2.cliente_id AS cliente_b,
           DATEDIFF(p2.fecha, p1.fecha) AS dias_diferencia,
           (SELECT SUM(l1.cantidad * l1.precio_unitario) FROM lineas_pedido l1 WHERE l1.pedido_id = p1.id) AS total_a,
           (SELECT SUM(l2.cantidad * l2.precio_unitario) FROM lineas_pedido l2 WHERE l2.pedido_id = p2.id) AS total_b
    FROM pedidos p1
    JOIN pedidos p2
      ON p2.fecha BETWEEN p1.fecha AND DATE_ADD(p1.fecha, INTERVAL 30 DAY)
     AND p2.cliente_id <> p1.cliente_id
),
 
-- 6) Evolución de facturación por cliente con LAG y medias móviles
facturacion_cliente AS (
    SELECT pe.cliente_id,
           DATE_FORMAT(f.fecha, '%Y-%m') AS mes,
           SUM(f.total) AS total_mes,
           LAG(SUM(f.total), 1) OVER (PARTITION BY pe.cliente_id ORDER BY DATE_FORMAT(f.fecha, '%Y-%m')) AS total_mes_anterior,
           AVG(SUM(f.total)) OVER (PARTITION BY pe.cliente_id ORDER BY DATE_FORMAT(f.fecha, '%Y-%m')
                                   ROWS BETWEEN 5 PRECEDING AND CURRENT ROW) AS media_movil_6m
    FROM facturas f
    JOIN pedidos pe ON pe.id = f.pedido_id
    GROUP BY pe.cliente_id, DATE_FORMAT(f.fecha, '%Y-%m')
),
 
-- 7) CARTESIANO almacenes x productos con stock, compras pendientes y fabricación en curso
estado_almacen_producto AS (
    SELECT a.id AS almacen_id,
           a.nombre AS almacen,
           p.id AS producto_id,
           p.sku,
           COALESCE(ex.cantidad, 0)      AS stock,
           COALESCE(ex.stock_minimo, 0)  AS stock_minimo,
           (SELECT COALESCE(SUM(lc.cantidad), 0)
              FROM lineas_compra lc
              JOIN compras co ON co.id = lc.compra_id
             WHERE co.almacen_id = a.id
               AND lc.producto_id = p.id
               AND co.estado <> 'recibida')                           AS unidades_en_compra,
           (SELECT COALESCE(SUM(re.cantidad_recibida), 0)
              FROM recepciones re
              JOIN compras co2 ON co2.id = re.compra_id
             WHERE co2.almacen_id = a.id
               AND re.producto_id = p.id)                             AS unidades_recibidas,
           (SELECT COALESCE(SUM(ofa.cantidad), 0)
              FROM ordenes_fabricacion ofa
             WHERE ofa.almacen_id = a.id
               AND ofa.producto_id = p.id
               AND ofa.fecha_fin IS NULL)                             AS unidades_en_fabricacion
    FROM almacenes a
    CROSS JOIN productos p
    LEFT JOIN existencias ex
           ON ex.almacen_id = a.id AND ex.producto_id = p.id
),
 
-- 8) Embudo comercial: oportunidades + interacciones + presupuestos, con JOIN por LIKE sobre texto
embudo AS (
    SELECT o.id AS oportunidad_id,
           o.titulo,
           o.etapa,
           o.importe_estimado,
           o.probabilidad,
           o.importe_estimado * o.probabilidad / 100 AS importe_ponderado,
           COUNT(DISTINCT i.id)  AS num_interacciones,
           COUNT(DISTINCT pr.id) AS num_presupuestos,
           COUNT(DISTINCT pm.id) AS productos_mencionados,
           GROUP_CONCAT(DISTINCT pm.sku ORDER BY pm.sku SEPARATOR ',') AS skus_mencionados
    FROM oportunidades o
    LEFT JOIN interacciones i  ON i.oportunidad_id = o.id
    LEFT JOIN presupuestos pr  ON pr.oportunidad_id = o.id
    LEFT JOIN productos pm     ON o.titulo LIKE CONCAT('%', pm.nombre, '%')
                               OR i.nota   LIKE CONCAT('%', pm.sku,    '%')
    GROUP BY o.id, o.titulo, o.etapa, o.importe_estimado, o.probabilidad
)

SELECT DISTINCT
       r.cliente_id,
       r.cliente,
       r.pais,
       r.producto_id,
       r.producto,
       r.categoria,
       r.importe_pedidos,
       r.importe_presupuestado,
       r.unidades_entregadas,
       r.rk_en_cliente,
       r.rk_en_producto,
       r.acumulado_pais_cat,
       r.percentil,
       cb.num_componentes,
       cb.profundidad,
       cb.coste_materiales,
       (r.importe_pedidos - COALESCE(cb.coste_materiales, 0)) AS margen_estimado,
       eap.almacen,
       eap.stock,
       eap.stock_minimo,
       eap.unidades_en_compra,
       eap.unidades_recibidas,
       eap.unidades_en_fabricacion,
       fc.mes,
       fc.total_mes,
       fc.total_mes_anterior,
       fc.media_movil_6m,
       (SELECT COUNT(*)
          FROM pedidos_cercanos pc
         WHERE pc.cliente_a = r.cliente_id)                   AS pedidos_cercanos_de_otros,
       (SELECT AVG(pc2.dias_diferencia)
          FROM pedidos_cercanos pc2
         WHERE pc2.cliente_a = r.cliente_id
           AND pc2.total_b > pc2.total_a)                     AS media_dias_pedidos_mayores,
       (SELECT SUM(em.importe_ponderado)
          FROM embudo em
          JOIN oportunidades oo ON oo.id = em.oportunidad_id
         WHERE oo.cliente_id = r.cliente_id)                  AS pipeline_ponderado,
       (SELECT GROUP_CONCAT(DISTINCT em2.skus_mencionados SEPARATOR '|')
          FROM embudo em2
          JOIN oportunidades oo2 ON oo2.id = em2.oportunidad_id
         WHERE oo2.cliente_id = r.cliente_id)                 AS skus_en_oportunidades
FROM ranking r
LEFT JOIN coste_bom cb                 ON cb.producto_id = r.producto_id
LEFT JOIN estado_almacen_producto eap  ON eap.producto_id = r.producto_id
LEFT JOIN facturacion_cliente fc       ON fc.cliente_id   = r.cliente_id
WHERE r.rk_en_cliente <= 50
   OR r.percentil <= 90
   OR eap.stock < eap.stock_minimo
ORDER BY RAND();
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
            case 0 -> cargarInventario(url, usuario, contraseña);
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    public void listarRecepciones() {
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
            case 0 -> cargarInventario(url, usuario, contraseña);
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    public void listarEntregas() {
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
            case 0 -> cargarInventario(url, usuario, contraseña);
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    public void listarFabricacion() {
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
