package aquaport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArquitecturaEnterpriseTest {

    private static Path getSrcPath() {
        Path p = Paths.get("src");
        if (Files.exists(p)) return p;
        p = Paths.get("Empoleon", "src");
        if (Files.exists(p)) return p;
        return Paths.get("src");
    }

    @Test
    @DisplayName("Todas las clases de produccion en Empoleon tienen <= 150 lineas")
    void todasLasClases_tienenMenosDe150Lineas() throws IOException {
        Path src = getSrcPath();
        try (Stream<Path> paths = Files.walk(src)) {
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
        Path modelDir = getSrcPath().resolve("aquaport").resolve("model");
        try (Stream<Path> paths = Files.walk(modelDir)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path file : javaFiles) {
                List<String> imports = Files.lines(file).map(String::trim).filter(l -> l.startsWith("import ")).toList();
                for (String imp : imports) {
                    boolean permitida = imp.startsWith("import java.lang") ||
                            imp.startsWith("import java.util") ||
                            imp.startsWith("import aquaport.model");
                    assertTrue(permitida, "Import prohibido en capa de dominio en " + file.getFileName() + ": " + imp);
                }
            }
        }
    }

    @Test
    @DisplayName("La capa de aplicacion aquaport.service no depende de la infraestructura")
    void servicioAplicacion_sinDependenciasDeInfraestructura() throws IOException {
        Path serviceDir = getSrcPath().resolve("aquaport").resolve("service");
        try (Stream<Path> paths = Files.walk(serviceDir)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
            for (Path file : javaFiles) {
                List<String> imports = Files.lines(file).map(String::trim).filter(l -> l.startsWith("import ")).toList();
                for (String imp : imports) {
                    assertFalse(imp.contains("aquaport.infrastructure"),
                            "Violacion de direccion arquitectonica: service depende de infraestructura en "
                                    + file.getFileName() + ": " + imp);
                }
            }
        }
    }

    @Test
    @DisplayName("Cero acoplamiento: El dominio y servicios nunca instancian clases de infraestructura")
    void dominioYServicios_sinInstanciacionInfraestructura() throws IOException {
        Path domainAndService = getSrcPath().resolve("aquaport");
        try (Stream<Path> paths = Files.walk(domainAndService)) {
            List<Path> files = paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("infrastructure") && !p.toString().contains("MainEmpoleon"))
                    .toList();
            for (Path file : files) {
                List<String> codeLines = Files.lines(file).toList();
                for (String line : codeLines) {
                    assertFalse(line.contains("new Adaptador") || line.contains("new ServicioAPI"),
                            "Violacion DIP: Instanciacion directa de infraestructura en " + file.getFileName() + ": " + line);
                }
            }
        }
    }

    @Test
    @DisplayName("Auditoria completa y generacion de reporte de dependencias por capas")
    void auditoriaArquitectura_generaReporteDependencias() throws IOException {
        Path src = getSrcPath();
        Map<String, Set<String>> dependenciasPorCapa = new HashMap<>();
        dependenciasPorCapa.put("dominio", new TreeSet<>());
        dependenciasPorCapa.put("aplicacion", new TreeSet<>());
        dependenciasPorCapa.put("infraestructura", new TreeSet<>());

        List<String> violaciones = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(src)) {
            List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.getFileName().toString().equals("MainEmpoleon.java"))
                    .toList();

            for (Path file : javaFiles) {
                String capa = determinarCapa(file);
                List<String> imports = Files.lines(file).map(String::trim).filter(l -> l.startsWith("import ")).toList();
                for (String imp : imports) {
                    dependenciasPorCapa.get(capa).add(imp.replace("import ", "").replace(";", ""));

                    if ("dominio".equals(capa)) {
                        if (imp.contains("aquaport.service") || imp.contains("aquaport.infrastructure")
                                || imp.contains("jackson") || imp.contains("http") || imp.contains("mockito")) {
                            violaciones.add("Dominio " + file.getFileName() + " contiene dependencia prohibida: " + imp);
                        }
                    } else if ("aplicacion".equals(capa)) {
                        if (imp.contains("aquaport.infrastructure")) {
                            violaciones.add("Aplicacion " + file.getFileName() + " contiene dependencia prohibida: " + imp);
                        }
                    }
                }
            }
        }

        System.out.println("================================================================================");
        System.out.println("  REPORTE DE AUDITORIA DE ARQUITECTURA POR CAPAS - AQUAPORT ENTERPRISE (v3)");
        System.out.println("================================================================================");
        System.out.println("  1. Capa Dominio (aquaport.model): Solo tipos canonicos de Java y modelo puro.");
        System.out.println("     -> Dependencias detectadas: " + dependenciasPorCapa.get("dominio"));
        System.out.println("  2. Capa Aplicacion (aquaport.service): Casos de uso y puertos DIP.");
        System.out.println("     -> Dependencias detectadas: " + dependenciasPorCapa.get("aplicacion"));
        System.out.println("  3. Capa Infraestructura (aquaport.infrastructure): Adaptadores y DTOs externos.");
        System.out.println("     -> Dependencias detectadas: " + dependenciasPorCapa.get("infraestructura"));
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("  Total violaciones de direccion de dependencias: " + violaciones.size());
        System.out.println("  Estado de Auditoria: " + (violaciones.isEmpty() ? "APROBADO (PASSED)" : "FALLIDO"));
        System.out.println("================================================================================");

        assertTrue(violaciones.isEmpty(), "Se detectaron violaciones en la auditoria de arquitectura: " + violaciones);
    }

    private String determinarCapa(Path file) {
        String s = file.toString().replace("\\", "/");
        if (s.contains("/model/")) return "dominio";
        if (s.contains("/service/")) return "aplicacion";
        if (s.contains("/infrastructure/")) return "infraestructura";
        return "raiz";
    }
}
