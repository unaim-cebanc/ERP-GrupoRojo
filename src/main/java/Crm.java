
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    
    public void cargarCrm(String usuario, String contraseña, String url, Scanner sc) {
        try {
            int opcion;
            do { 
                menuCrm();
                opcion = Main.leerInt(sc, "> ");
                ejecutarCrm(opcion, sc);
            } while (opcion != 0);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
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
    
    private void ejecutarCrm(int opcion, Scanner sc) {
        switch (opcion) {
            case 1 -> añadirOportunidad(sc);
            case 2 -> añadirPresupuesto(sc);
            case 3 -> mostrarOportunidad(sc);
            case 4 -> confirmarPresupuesto(sc);
            case 5 -> eliminarOportunidad(sc);
            case 0 -> {}
            default -> System.out.println("Opcion introducida incorrecta");
        }
    }
    
    private void añadirOportunidad(Scanner sc) {
        while (true) { 
            System.out.println("====== Nueva Oportunidad ======");
            String cliente = Main.leerString(sc, "Cliente: ");
            String titulo = Main.leerString(sc, "Título: ");
            int importe = Main.leerInt(sc, "Importe estimado: ");
            int probabilidad = Main.leerInt(sc, "Probabilidad: ");
            LocalDate fechaCierrePrevista = LocalDate.parse(Main.leerString(sc, "Fecha cierre prevista (yyyy-MM-dd): "));
            
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
    
    private void añadirPresupuesto(Scanner sc) {
        while (true) { 
            try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                System.out.println("====== Nuevo Presupuesto ======");
                String cliente = Main.leerString(sc, "Cliente: ");
                int idOportunidad = Main.leerInt(sc, "Id oportunidad asociada: ");
                LocalDate fechaValidez = LocalDate.parse(Main.leerString(sc, "Fecha validez (yyyy-MM-dd): "));
                String observaciones = Main.leerString(sc, "Observaciones: ");
                
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
            } catch (SQLException | DateTimeParseException e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println("Se ha creado el presupuesto correctamente");
            break;
        }
    }
    
    private void mostrarOportunidad(Scanner sc) {
        System.out.println("====== Mostrar Oportunidad ======");
        System.out.println("Deseas mostrar todos los registros? (y/n)");
        
        String opcion = Main.leerString(sc, "> ").toLowerCase();
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
                FROM oportunidades AS o
                LEFT JOIN presupuestos AS p WHERE o.id = p.oportunidad_id""");
                
                ResultSet rs = st.executeQuery();
                
                if (rs.isBeforeFirst()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnas = md.getColumnCount();    
                    String[] cabeceras = Main.recogerCabeceras(md, columnas);
                    ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                    int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                    
                    System.out.println("\n=== OPORTUNIDADES ===\n");
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
                int id = Main.leerInt(sc, "> ");
                PreparedStatement st = conn.prepareStatement("""
                SELECT
                    o.id AS 'id',
                    o.cliente_id AS 'cliente',
                    o.titulo AS 'titulo',
                    o.importe_estimado AS 'importe estimado',
                    o.probabilidad AS 'prob.',
                    o.etapa AS 'etapa',
                    o.fecha_creacion AS 'fecha creación',
                    o.fecha_cierre_prevista AS 'cierre previsto',
                    o.fecha_cierre AS 'fecha cierre',
                    p.id AS 'presupuesto',
                    p.estado AS 'estado presupuesto'
                FROM oportunidades AS o 
                INNER JOIN presupuestos AS p ON o.id = p.oportunidad_id
                WHERE o.id = ?""");
                st.setInt(1, id);
                
                ResultSet rs = st.executeQuery();
                
                if (rs.isBeforeFirst()) {
                    ResultSetMetaData md = rs.getMetaData();
                    int columnas = md.getColumnCount();    
                    String[] cabeceras = Main.recogerCabeceras(md, columnas);
                    ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                    int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                    
                    System.out.println("\n=== OPORTUNIDAD ===\n");
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
    
    private void confirmarPresupuesto(Scanner sc) {
        while (true) {
            System.out.println("====== Confirmar presupuesto ======");
            int id = Main.leerInt(sc, "Introduce el id del presupuesto a aceptar: ");
            System.out.println("Se establecera el estado del presupuesto nº " + id + " como 'aceptado'.");
            String conf = Main.leerString(sc, "Seguro que quieres proceder? (y/n): ").toLowerCase();
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
    
    private void eliminarOportunidad(Scanner sc) {
        int id;
        String conf;
        
        while (true) { 
            System.out.println("====== Eliminar Oportunidad ======");
            System.out.println("Introduce el id de la oportunidad que deseas eliminar");
            
            id = Main.leerInt(sc, "> ");
            
            System.out.println("Seguro que quieres eliminar la oportunidad nº " + id + "? (y/n)");
            
            conf = Main.leerString(sc, "> ").toLowerCase();
            
            switch (conf) {
                case "y" -> {
                    if (Main.verificarId(id, "oportunidades")) {
                        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
                            PreparedStatement st = conn.prepareStatement("DELETE FROM oportunidades WHERE id = ?");
                            st.setInt(1, id);
                            st.executeUpdate();
                            
                            System.out.println("Se ha eliminado la oportunidad correctamente");
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
}
