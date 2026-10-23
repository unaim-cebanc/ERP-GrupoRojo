public class Worker {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: falta la operación");
            System.exit(2);
        }

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");

        Inventario inv = new Inventario(url, user, pass);
        Compra compra = new Compra(user, pass, url);

        switch (args[0]) {
            case "mostrar"     -> inv.mostrarInventario();
            case "recepciones" -> inv.listarRecepciones();
            case "entregas"    -> inv.listarEntregas();
            case "fabricacion" -> inv.listarFabricacion();
            case "compras"     -> compra.listarPresupuestos();
            default -> {
                System.err.println("Error: operación desconocida: " + args[0]);
                System.exit(2);
            }
        }
        System.out.flush();
    }
}
