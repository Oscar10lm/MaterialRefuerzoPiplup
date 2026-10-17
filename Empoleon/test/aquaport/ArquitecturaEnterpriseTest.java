package aquaport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArquitecturaEnterpriseTest {

    private static final Path SRC_PATH = Paths.get("src");

    @Test
    @DisplayName("Todas las clases de produccion en Empoleon tienen <= 150 lineas")
    void todasLasClases_tienenMenosDe150Lineas() throws IOException {
        try (Stream<Path> paths = Files.walk(SRC_PATH)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            assertFalse(javaFiles.isEmpty(), "Debe haber archivos Java en src/");
            for (Path file : javaFiles) {
                long lineCount = Files.lines(file).count();
                assertTrue(lineCount <= 150, "La clase " + file.getFileName() + " supera 150 lineas: " + lineCount);
            }
        }
    }

    @Test
    @DisplayName("El paquete de dominio aquaport.model no tiene dependencias externas")
    void dominioModel_sinDependenciasExternas() throws IOException {
        Path modelDir = SRC_PATH.resolve("aquaport").resolve("model");
        try (Stream<Path> paths = Files.walk(modelDir)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path file : javaFiles) {
                List<String> imports = Files.lines(file)
                        .map(String::trim)
                        .filter(l -> l.startsWith("import "))
                        .toList();
                for (String imp : imports) {
                    boolean permitida = imp.startsWith("import java.lang") ||
                            imp.startsWith("import java.util") ||
                            imp.startsWith("import aquaport.model");
                    assertTrue(permitida, "Import prohibido en capa de dominio en " + file.getFileName() + ": " + imp);
                }
            }
        }
    }
}
