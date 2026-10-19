
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Compra {
    private final String usuario;
    private final String contraseña;
    private final String url;
    
    public Compra(String usuario, String contraseña, String url) {
        this.usuario = usuario;
        this.contraseña = contraseña;
        this.url = url;
    }
    
    public void cargarCompra(String usuario, String contraseña, String url) {
        try (Scanner sc = new Scanner(System.in)){
            int opcion;
            do { 
                mostrarMenu();
                opcion = Main.leerInt(sc, "> ");
                ejecutarCompra(opcion, sc);
            } while (opcion != 0);
        }
    }
    
    private void mostrarMenu() {
        System.out.println("====== Compra ======");
        System.out.println("1. Listar presupuestos");
        System.out.println("2. Confirmar presupuesto");
        System.out.println("0. Volver");
    }
    
    private void ejecutarCompra(int opcion, Scanner sc) {
        switch (opcion) {
            case 1 -> listarPresupuestos();
            case 2 -> confirmarPresupuesto(sc);
            case 0 -> Main.mostarMenu(sc, usuario, contraseña, url);
            default -> System.out.println("Error: Opción introducida incorrecta");
        }
    }
    
    private void listarPresupuestos() {
        System.out.println("====== Mostrar Presupuestos ======\n");
        
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)) {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM compras");
            
            ResultSet rs = st.executeQuery();
            
            if (rs.isBeforeFirst()) {
                ResultSetMetaData md = rs.getMetaData();
                int columnas = md.getColumnCount();    
                String[] cabeceras = Main.recogerCabeceras(md, columnas);
                ArrayList<String[]> filas = Main.recogerDatos(rs, columnas);
                int[] anchos = Main.calcularAnchos(columnas, cabeceras, filas);
                
                Main.imprimirLinea(cabeceras, anchos);
                Main.imprimirSeparador(anchos);
                for (String[] fila : filas) {
                    Main.imprimirLinea(fila, anchos);
                }
            } else {
                System.out.println("Error: No hay ningún presupuesto registrado");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void confirmarPresupuesto(Scanner sc) {
        while (true) { 
            System.out.println("====== Confirmar Presupusto ======\n");
            int id = Main.leerInt(sc, "Introduce el id del presupuesto a confirmar: ");
            System.out.println("Se establecerá el estado del presupuesto " + id + " como 'recibida'");
            String conf = Main.leerString(sc, "Seguro que quieres proceder? (y/n): ");
            
            switch (conf) {
                case "y" -> {
                    try (Connection conn = DriverManager.getConnection(url, usuario, contraseña)){
                        PreparedStatement st = conn.prepareStatement("UPDATE compras SET estado = 'recibida', fecha_recepcion = NOW() WHERE id = ?");
                        st.setInt(1, id);
                        st.executeUpdate();
                        
                        PreparedStatement st2 = conn.prepareStatement("SELECT producto_id, cantidad FROM lineas_compra WHERE compra_id = ?");
                        st2.setInt(1, id);
                        ResultSet rs = st2.executeQuery();
                        if (rs.next()) {
                            int idProducto = rs.getInt("producto_id");
                            int cantidad = rs.getInt("cantidad");
                            
                            PreparedStatement st3 = conn.prepareStatement("UPDATE existencias SET cantidad = cantidad + ? WHERE producto_id = ?");
                            st3.setInt(1, cantidad);
                            st3.setInt(2, idProducto);
                            st3.executeUpdate();
                        } else {
                            System.out.println("Error: No se ha encontrado el producto asociado al presupuesto");
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
                    System.out.println("\nAccion cancelada\n");
                    return;
                }
                default -> System.out.println("Error: Opción introducida incorrecta");
            }
        }
    }
}