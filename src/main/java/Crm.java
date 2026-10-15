
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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
        System.out.println("1. Crear oportunidad");
        System.out.println("2. Crear presupuesto");
        System.out.println("3. Ver oportunidades");
        System.out.println("4. Confirmar presupuesto");
        System.out.println("5. Eliminar oportunidad");
        System.out.println("0. Volver");
    }
    
    private void ejecutarCrm(int opcion, Scanner scanner) {
        switch (opcion) {
            case 1 -> añadirOportunidad(scanner);
            case 2 -> añadirPresupuesto(scanner);
            case 3 -> mostrarOportunidad(scanner);
            case 4 -> confirmarPresupuesto(scanner);
            case 5 -> eliminarOportunidad(scanner);
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
            LocalDate fechaCierrePrevista = LocalDate.parse(Main.leerString(scanner, "Fecha cierre prevista (yyyy-MM-dd): "));
            
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement stId = conn.prepareStatement("SELECT id FROM clientes WHERE nombre = ?");
                stId.setString(1, cliente);
                ResultSet rsId = stId.executeQuery();
                
                if (rsId.isBeforeFirst()) {
                    rsId.next();
                    int idCliente = rsId.getInt(1);
                    
                    PreparedStatement st = conn.prepareStatement("INSERT INTO oportunidades" +
                    "(cliente_id, titulo, importe_estimado, probabilidad, fecha_cierre_prevista)" + 
                    "VALUES (?, ?, ?, ?, ?)");
                    
                    st.setInt(1, idCliente);
                    st.setString(2, titulo);
                    st.setInt(3, importe);
                    st.setInt(4, probabilidad);
                    st.setObject(5, fechaCierrePrevista);
                    
                    st.executeUpdate();
                } else { System.out.println("Error: Cliente introducido no existe"); }
            } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); break;}
            System.out.println("Se ha creado la oportunidad correctamente");
            break;
        }
    }
    
    private void añadirPresupuesto(Scanner scanner) {
        while (true) { 
            System.out.println("====== Nuevo Presupuesto ======");
            String cliente = Main.leerString(scanner, "Cliente: ");
            int idOportunidad = Main.leerInt(scanner, "Id oportunidad asociada: ");
            LocalDate fechaValidez = LocalDate.parse(Main.leerString(scanner, "Fecha validez (yyyy-MM-dd): "));
            String observaciones = Main.leerString(scanner, "Observaciones: ");
            
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                PreparedStatement stId = conn.prepareStatement("SELECT id FROM clientes WHERE nombre = ?");
                stId.setString(1, cliente);
                ResultSet rsId = stId.executeQuery();
                
                if (rsId.isBeforeFirst()) {
                    rsId.next();
                    int idCliente = rsId.getInt(1);
                    
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
            System.out.println("Se ha creado el presupuesto correctamente");
            break;
        }
    }
    
    private void mostrarOportunidad(Scanner scanner) {
        System.out.println("====== Mostrar Oportunidad ======");
        System.out.println("Deseas mostrar todos los registros? (y/n)");
        
        String opcion = Main.leerString(scanner, "> ").toLowerCase();
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            if (opcion.equals("y")) {
                PreparedStatement st = conn.prepareStatement("""
                SELECT
                    o.id AS 'ID',
                    o.cliente_id AS 'ID CLIENTE',
                    o.titulo AS 'TITULO',
                    o.importe_estimado AS 'IMPORTE',
                    o.probabilidad AS 'PROBABLIDAD',
                    o.etapa AS 'ETAPA',
                    o.fecha_creacion AS 'FECHA CREACIÓN',
                    o.fecha_cierre_prevista AS 'CIERRE PREVISTO',
                    o.fecha_cierre AS 'FECHA CIERRE',
                    p.id AS 'ID PRESUPUESTO',
                    p.estado AS 'ESTADO PRESUPUESTO'
                FROM oportunidades AS o INNER JOIN presupuestos AS p WHERE o.id = p.oportunidad_id""");
                
                ResultSet rs = st.executeQuery();
                
                if (rs.isBeforeFirst()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnas = md.getColumnCount();    
                    String[] cabeceras = Main.recogerCabeceras(md, columnas);
                    ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                    int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                    
                    System.err.println("\n=== OPORTUNIDADES ===\n");
                    Main.imprimirLinea(cabeceras, anchos);
                    Main.imprimirSeparador(anchos);
                    for (String[] fila : filas) {
                        Main.imprimirLinea(fila, anchos);
                    }
                } else {
                    System.out.println("No se han encontrado resultados");
                }
            } else if (opcion.equals("n")) {
                System.out.println("Introduce el id de la oportunidad que deseas revisar");
                int id = Main.leerInt(scanner, "> ");
                PreparedStatement st = conn.prepareStatement("""
                SELECT
                    o.id AS 'ID',
                    o.cliente_id AS 'ID CLIENTE',
                    o.titulo AS 'TITULO',
                    o.importe_estimado AS 'IMPORTE ESTIMADO',
                    o.probabilidad AS 'PROBABLIDAD',
                    o.etapa AS 'ETAPA',
                    o.fecha_creacion AS 'FECHA CREACIÓN',
                    o.fecha_cierre_prevista AS 'CIERRE PREVISTO',
                    o.fecha_cierre AS 'FECHA CIERRE',
                    p.id AS 'ID PRESUPUESTO',
                    p.estado AS 'ESTADO PRESUPUESTO'
                FROM oportunidades AS o INNER JOIN presupuestos AS p WHERE o.id = p.oportunidad_id AND o.id = ?""");
                st.setInt(1, id);
                
                ResultSet rs = st.executeQuery();
                
                if (rs.isBeforeFirst()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnas = md.getColumnCount();    
                    String[] cabeceras = Main.recogerCabeceras(md, columnas);
                    ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                    int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                    
                    System.err.println("\n=== OPORTUNIDAD ===\n");
                    Main.imprimirLinea(cabeceras, anchos);
                    Main.imprimirSeparador(anchos);
                    for (String[] fila : filas) {
                        Main.imprimirLinea(fila, anchos);
                    }
                } else {
                    System.out.println("No se han encontrado resultados");
                }
            }
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }
    
    private void confirmarPresupuesto(Scanner scanner) {
        while (true) {
            System.out.println("====== Confirmar presupuesto ======");
            int id = Main.leerInt(scanner, "Introduce el id del presupuesto a aceptar: ");
            System.out.println("Se establecera el estado del presupuesto nº " + id + " como 'aceptado'.");
            String conf = Main.leerString(scanner, "Seguro que quieres proceder? (y/n): ").toLowerCase();
            switch (conf) {
                case "y" -> {
                    if (verificarId(id, "presupuestos")) {
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
    
    private void eliminarOportunidad(Scanner scanner) {
        int id;
        String conf;
        
        while (true) { 
            System.out.println("====== Eliminar Oportunidad ======");
            System.out.println("Introduce el id de la oportunidad que deseas eliminar");
            
            id = Main.leerInt(scanner, "> ");
            
            System.out.println("Seguro que quieres eliminar la oportunidad nº " + id + "? (y/n)");
            
            conf = Main.leerString(scanner, "> ").toLowerCase();
            
            switch (conf) {
                case "y" -> {
                    if (verificarId(id, "oportunidades")) {
                        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                            PreparedStatement st = conn.prepareStatement("DELETE FROM oportunidades WHERE id = ?");
                            st.setInt(1, id);
                            st.executeUpdate();
                            return;
                        } catch (SQLException e) {
                            System.out.println("Error: " + e.getMessage());
                            return;
                        }
                    }
                }
                case "n" -> {
                    System.out.println("Operación cancelada");
                    return;
                }
                default -> System.out.println("Error: Valor introducido no esperado");
            }
        }
    }
    
    private Boolean verificarId(int id, String tabla) {
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tabla + " WHERE id = ?");
            st.setInt(1, id);
            
            ResultSet rs = st.executeQuery();
            return rs.isBeforeFirst();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }
}
