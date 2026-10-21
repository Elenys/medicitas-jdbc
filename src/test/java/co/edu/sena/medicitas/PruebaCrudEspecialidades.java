package co.edu.sena.medicitas;

import co.edu.sena.medicitas.datos.ConexionBD;
import co.edu.sena.medicitas.datos.DatosException;
import co.edu.sena.medicitas.datos.EspecialidadDao;
import co.edu.sena.medicitas.datos.EspecialidadDaoJdbc;
import co.edu.sena.medicitas.modelo.Especialidad;

/**
 * Prueba del CRUD completo contra la base de datos real: inserta, consulta, actualiza y elimina
 * una especialidad de prueba, y verifica los errores de nombre repetido y de llave foránea.
 * Al terminar, la base queda como estaba.
 *
 * <p>Se ejecuta con {@code ./ejecutar.sh prueba} y termina con código 1 si algo falla.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public final class PruebaCrudEspecialidades {

    private static int aprobadas;

    private PruebaCrudEspecialidades() {
    }

    public static void main(String[] argumentos) {
        EspecialidadDao dao = new EspecialidadDaoJdbc(ConexionBD.desdeConfiguracion());
        int totalInicial = dao.listarTodas().size();
        System.out.println("Especialidades al iniciar: " + totalInicial);

        // INSERTAR
        Especialidad prueba = dao.insertar(new Especialidad("Neurología (prueba JDBC)", "Sistema nervioso"));
        verificar("insertar asigna un id", prueba.getId() != null && prueba.getId() > 0);
        System.out.println("  insertada → " + prueba);

        // CONSULTAR
        verificar("buscar por id la encuentra", dao.buscarPorId(prueba.getId()).isPresent());
        verificar("buscar por nombre la encuentra", dao.buscarPorNombre("neuro").size() == 1);
        verificar("listar incluye la nueva", dao.listarTodas().size() == totalInicial + 1);

        // ACTUALIZAR
        prueba.setDescripcion("Cerebro, médula espinal y nervios");
        verificar("actualizar devuelve true", dao.actualizar(prueba));
        String descripcionGuardada = dao.buscarPorId(prueba.getId()).orElseThrow().getDescripcion();
        verificar("la descripción quedó actualizada", "Cerebro, médula espinal y nervios".equals(descripcionGuardada));
        System.out.println("  actualizada → " + dao.buscarPorId(prueba.getId()).orElseThrow());

        // RESTRICCIONES
        verificar("nombre repetido se rechaza", lanzaError(() -> dao.insertar(new Especialidad("Neurología (prueba JDBC)", null)),
                "Ya existe una especialidad"));
        int idConMedicos = dao.buscarPorNombre("Medicina General").get(0).getId();
        verificar("no se elimina una especialidad con médicos", lanzaError(() -> dao.eliminar(idConMedicos),
                "hay médicos asignados"));

        // ELIMINAR
        verificar("eliminar devuelve true", dao.eliminar(prueba.getId()));
        verificar("ya no se encuentra", dao.buscarPorId(prueba.getId()).isEmpty());
        verificar("el total volvió al inicial", dao.listarTodas().size() == totalInicial);

        System.out.println("Resultado: " + aprobadas + " verificaciones aprobadas.");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (!condicion) {
            System.out.println("✗ FALLÓ: " + descripcion);
            System.exit(1);
        }
        aprobadas++;
        System.out.println("✓ " + descripcion);
    }

    private static boolean lanzaError(Runnable accion, String textoEsperado) {
        try {
            accion.run();
            return false;
        } catch (DatosException error) {
            System.out.println("  mensaje → " + error.getMessage());
            return error.getMessage().contains(textoEsperado);
        }
    }
}
