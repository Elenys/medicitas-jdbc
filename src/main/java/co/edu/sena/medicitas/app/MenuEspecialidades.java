package co.edu.sena.medicitas.app;

import co.edu.sena.medicitas.datos.ConexionBD;
import co.edu.sena.medicitas.datos.DatosException;
import co.edu.sena.medicitas.datos.EspecialidadDao;
import co.edu.sena.medicitas.datos.EspecialidadDaoJdbc;
import co.edu.sena.medicitas.modelo.Especialidad;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Módulo de consola para administrar el catálogo de especialidades médicas de MediCitas:
 * crear, listar, buscar, actualizar y eliminar (CRUD) con JDBC.
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public class MenuEspecialidades {

    private final EspecialidadDao especialidadDao;
    private final Scanner teclado;

    public MenuEspecialidades(EspecialidadDao especialidadDao, Scanner teclado) {
        this.especialidadDao = especialidadDao;
        this.teclado = teclado;
    }

    public static void main(String[] argumentos) {
        ConexionBD conexionBD = ConexionBD.desdeConfiguracion();
        System.out.println("MediCitas · Módulo de especialidades (JDBC)");
        System.out.println("Base de datos: " + conexionBD.getUrl());
        new MenuEspecialidades(new EspecialidadDaoJdbc(conexionBD), new Scanner(System.in)).ejecutar();
    }

    /** Muestra el menú hasta que el usuario elija salir. */
    public void ejecutar() {
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            String opcion = teclado.nextLine().trim();
            try {
                switch (opcion) {
                    case "1" -> crear();
                    case "2" -> listar();
                    case "3" -> buscar();
                    case "4" -> actualizar();
                    case "5" -> eliminar();
                    case "0" -> continuar = false;
                    default -> System.out.println("Opción no válida.");
                }
            } catch (DatosException | IllegalArgumentException error) {
                System.out.println("⚠ " + error.getMessage());
            }
        }
        System.out.println("Hasta pronto.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("1. Crear especialidad");
        System.out.println("2. Listar especialidades");
        System.out.println("3. Buscar especialidad");
        System.out.println("4. Actualizar especialidad");
        System.out.println("5. Eliminar especialidad");
        System.out.println("0. Salir");
        System.out.print("Elija una opción: ");
    }

    private void crear() {
        String nombre = preguntar("Nombre: ");
        String descripcion = preguntar("Descripción (opcional): ");
        Especialidad creada = especialidadDao.insertar(new Especialidad(nombre, descripcion));
        System.out.println("✓ Especialidad creada con el id " + creada.getId() + ".");
    }

    private void listar() {
        imprimir(especialidadDao.listarTodas());
    }

    private void buscar() {
        String texto = preguntar("Nombre o parte del nombre: ");
        imprimir(especialidadDao.buscarPorNombre(texto));
    }

    private void actualizar() {
        Optional<Especialidad> encontrada = especialidadDao.buscarPorId(preguntarId());
        if (encontrada.isEmpty()) {
            System.out.println("No existe una especialidad con ese id.");
            return;
        }
        Especialidad especialidad = encontrada.get();
        String nombre = preguntar("Nuevo nombre [" + especialidad.getNombre() + "]: ");
        String descripcion = preguntar("Nueva descripción [" + valorOGuion(especialidad.getDescripcion()) + "]: ");
        if (!nombre.isBlank()) {
            especialidad.setNombre(nombre);
        }
        if (!descripcion.isBlank()) {
            especialidad.setDescripcion(descripcion);
        }
        especialidadDao.actualizar(especialidad);
        System.out.println("✓ Especialidad actualizada.");
    }

    private void eliminar() {
        int id = preguntarId();
        String confirmacion = preguntar("¿Eliminar la especialidad " + id + "? (s/n): ");
        if (!confirmacion.equalsIgnoreCase("s")) {
            System.out.println("Operación cancelada.");
            return;
        }
        System.out.println(especialidadDao.eliminar(id) ? "✓ Especialidad eliminada." : "No existe una especialidad con ese id.");
    }

    private String preguntar(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine();
    }

    private int preguntarId() {
        String valor = preguntar("Id de la especialidad: ").trim();
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("El id debe ser un número entero.");
        }
    }

    private static void imprimir(List<Especialidad> especialidades) {
        if (especialidades.isEmpty()) {
            System.out.println("No hay especialidades que coincidan.");
            return;
        }
        System.out.println(" id | nombre               | descripción");
        especialidades.forEach(System.out::println);
    }

    private static String valorOGuion(String valor) {
        return valor == null ? "—" : valor;
    }
}
