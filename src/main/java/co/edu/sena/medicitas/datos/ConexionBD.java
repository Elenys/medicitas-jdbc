package co.edu.sena.medicitas.datos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC a la base de datos PostgreSQL de MediCitas.
 *
 * <p>Los datos de conexión no se escriben en el código: se leen del archivo
 * {@code config/db.properties} (que no se sube al repositorio). Cada variable de entorno
 * {@code MEDICITAS_DB_URL}, {@code MEDICITAS_DB_USUARIO} y {@code MEDICITAS_DB_CLAVE},
 * si existe, tiene prioridad sobre el archivo.</p>
 *
 * @author Carlas Elenys Montalvo Mendoza
 * @author Freyder López
 */
public final class ConexionBD {

    private static final Path ARCHIVO_CONFIGURACION = Path.of("config", "db.properties");

    private final String url;
    private final String usuario;
    private final String clave;

    private ConexionBD(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /**
     * Carga la configuración desde {@code config/db.properties} y las variables de entorno.
     *
     * @throws DatosException si falta algún dato de conexión
     */
    public static ConexionBD desdeConfiguracion() {
        Properties propiedades = new Properties();
        if (Files.exists(ARCHIVO_CONFIGURACION)) {
            try (InputStream entrada = Files.newInputStream(ARCHIVO_CONFIGURACION)) {
                propiedades.load(entrada);
            } catch (IOException error) {
                throw new DatosException("No se pudo leer " + ARCHIVO_CONFIGURACION, error);
            }
        }
        return new ConexionBD(
                leerValor(propiedades, "db.url", "MEDICITAS_DB_URL"),
                leerValor(propiedades, "db.usuario", "MEDICITAS_DB_USUARIO"),
                leerValor(propiedades, "db.clave", "MEDICITAS_DB_CLAVE"));
    }

    /** Abre una conexión nueva. Quien la recibe debe cerrarla (try-with-resources). */
    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** URL de conexión, útil para mostrar a qué base se conectó la aplicación. */
    public String getUrl() {
        return url;
    }

    private static String leerValor(Properties propiedades, String clavePropiedad, String variableEntorno) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = propiedades.getProperty(clavePropiedad);
        }
        if (valor == null || valor.isBlank()) {
            throw new DatosException("Falta el dato de conexión «" + clavePropiedad + "». Copie config/db.properties.ejemplo"
                    + " como config/db.properties y complételo.", null);
        }
        return valor.trim();
    }
}
