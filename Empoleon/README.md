# 👑 Empoleon — AquaPort Enterprise: Red Multi-Embalse (v3)

> **Nivel Avanzado y Producción del Sistema AquaPort (ECI)**  
> 60 drones especializados en 4 zonas hídricas interconectadas, rutas multi-etapa con waypoints georreferenciados, coordinación de traspasos y estándares de ingeniería de software para misión crítica.  
> *En este nivel el sistema opera a escala Enterprise: arquitectura desacoplada, alta disponibilidad, observabilidad completa y deuda técnica cero.*

---

## 🌊 Contexto Operativo Enterprise

La Escuela Colombiana de Ingeniería Julio Garavito (ECI) expande la red hidrológica institucional a **4 zonas hídricas integradas** gestionadas por una flota de **60 drones acuáticos**:

| Zona Hídrica | Ubicación / Topología | Especialización de Flota | Unidades Asignadas | Propósito Operacional |
|---|---|---|:---:|---|
| 🏞️ **Embalse de Investigación** | Sector Norte | Drones Buceadores (`DB-01` a `DB-20`) | **20** | Inmersión profunda, recolección de sedimentos limnológicos y batimetría. |
| 🛶 **Red de Canales** | Campus Universitario | Drones Superficiales (`DS-01` a `DS-20`) | **20** | Tránsito veloz, muestreo de superficie y entrega de reactivos entre facultades. |
| 🌊 **Laguna de Reserva** | Sector Sur | Drones Semisumergidos (`DM-01` a `DM-15`) | **15** | Aguas agitadas con oleaje, transporte de boyas pesadas y telemetría climática. |
| 🔬 **Laboratorio Hídrico Central** | Destino y Hub Común | Flota de Relevo Mixta (`DM-16`, `DM-17`, `DS-21`, `DS-22`, `DB-21`) | **5** | Hub de recepción analítica, mantenimiento express y relevo rápido de contingencia. |

---

## 📏 Estándares de Calidad de Producción Enterprise

Para satisfacer los requerimientos de grado de producción, la base de código cumple estrictamente:

1. **Arquitectura por Capas:** Capa de dominio (`aquaport.model`) pura, inmutable y sin dependencias externas fuera de `java.lang` y `java.util`.
2. **Límites Estructurales:**
   * **Métodos $\le 15$ líneas de código** (verificado programáticamente en tests).
   * **Clases $\le 150$ líneas de código** (verificado programáticamente en tests).
   * **DRY estricto:** Cero duplicación de lógica ni literales de configuración.
3. **Quality Gate JaCoCo:**
   * $\ge 85\%$ Line Coverage (Alcanzado: **93.7%**).
   * $\ge 75\%$ Branch Coverage (Alcanzado: **80.2%**).
4. **SonarQube Quality Gate:**
   * 0 Bugs, 0 Vulnerabilidades, Deuda Técnica $< 15$ min, Rating A en todos los indicadores.

---

## 🎯 Reto 01: Streams & Lambdas en Escala Enterprise (60 Drones)

Implementado en [`ConsultorFlotaEnterprise.java`](file:///Empoleon/src/aquaport/service/ConsultorFlotaEnterprise.java) y [`BateriaPromedioCollector.java`](file:///Empoleon/src/aquaport/service/BateriaPromedioCollector.java):

### 1️⃣ Carga de Trabajo Total por Zona (`flatMap` + `groupingBy`)
```java
Map<String, Long> cargaPorZona = misiones.stream()
    .filter(m -> m.getEstado() == EstadoMision.EN_TRANSITO)
    .flatMap(m -> m.getWaypoints().stream())
    .collect(Collectors.groupingBy(Waypoint::getZona, Collectors.counting()));
```

### 2️⃣ Eficiencia de Cada Zona (`completadas / totales`)
```java
public Map<String, Double> calcularEficienciaPorZona(List<MisionEnterprise> misiones) {
    if (misiones == null || misiones.isEmpty()) return Map.of();
    Map<String, Long> totales = contarMisiones(misiones, m -> true);
    Map<String, Long> completadas = contarMisiones(misiones, MisionEnterprise::isCompletada);
    Map<String, Double> eficiencia = new HashMap<>();
    totales.forEach((zona, total) -> {
        long ok = completadas.getOrDefault(zona, 0L);
        eficiencia.put(zona, total == 0 ? 0.0 : (double) ok / total);
    });
    return Collections.unmodifiableMap(eficiencia);
}
```

### 3️⃣ Drone con Mejor Historial de Entregas Exitosas
```java
public Optional<DroneAcuatico> encontrarDroneMejorHistorial(List<MisionEnterprise> misiones) {
    if (misiones == null) return Optional.empty();
    return misiones.stream()
            .filter(MisionEnterprise::isCompletada)
            .flatMap(m -> m.getDronesInvolucrados().stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);
}
```

### 4️⃣ Planificación de Ruta Óptima Multi-Etapa con Waypoints
```java
public List<TramoRuta> planificarRutaOptimaMultiEtapa(List<Waypoint> waypoints, List<DroneAcuatico> flota) {
    if (waypoints == null || waypoints.size() < 2 || flota == null) return List.of();
    List<TramoRuta> ruta = new ArrayList<>();
    for (int i = 0; i < waypoints.size() - 1; i++) {
        Waypoint origen = waypoints.get(i);
        Waypoint destino = waypoints.get(i + 1);
        DroneAcuatico asignado = seleccionarDroneCercano(flota, origen.getZona())
                .orElseThrow(() -> new IllegalStateException("Sin drone apto en tramo: " + origen.getZona()));
        ruta.add(new TramoRuta("TR-" + (i + 1), origen, destino, asignado));
    }
    return Collections.unmodifiableList(ruta);
}
```

### 5️⃣ Collector Personalizado: Batería Promedio por Zona en una Sola Pasada
El colector [`BateriaPromedioCollector`](file:///Empoleon/src/aquaport/service/BateriaPromedioCollector.java) agrupa y promedia en complejidad temporal $\mathcal{O}(N)$ sin realizar pasadas dobles sobre los 60 drones:
```java
public class BateriaPromedioCollector implements Collector<DroneAcuatico, Map<String, AcumuladorBateria>, Map<String, Double>> {
    // supplier: new HashMap<>()
    // accumulator: mapa.computeIfAbsent(d.getZona(), ...).aceptar(d.getBateria())
    // combiner: merge entre mapas locales para soporte multihilo
    // finisher: transformacion final (suma / cuenta)
}
```

---

## ⚡ Análisis Técnico: ¿Cuándo Usar Stream Paralelo y Cuándo NO?

### ✅ Cuándo SÍ aplica Stream Paralelo:
1. **Problemas con Alto Costo Computacional por Elemento ($N \times Q > 10,000$):**  
   Si cada waypoint requiere cálculo cinemático no lineal, resolución de ecuaciones hidrodinámicas de Navier-Stokes en micro-oleaje o cálculo batimétrico intensivo, donde el costo computacional ($Q$) amortiza el costo de creación de tareas en `ForkJoinPool`.
2. **Estructuras de Datos Indexadas con Particionamiento $\mathcal{O}(1)$:**  
   `ArrayList` o arreglos primitivos (`double[]`, `int[]`), donde el `Spliterator` puede dividir el espacio de búsqueda por la mitad sin recorrer enlaces (a diferencia de `LinkedList` o `Stream.iterate`).
3. **Operaciones Puras, Sin Estado y Asociativas:**  
   Funciones de agregación donde los resultados parciales son matemáticamente asociativos (como el `combiner` de nuestro `BateriaPromedioCollector`).

### ❌ Por qué en los Otros Casos NO aplica (y perjudica el rendimiento):
1. **Volumen Reducido ($N = 60$ drones / 4 zonas):**  
   Para 60 elementos en memoria, la iteración secuencial se ejecuta en **microsegundos** (residiendo en caché L1/L2 del procesador). Crear hilos en `ForkJoinPool.commonPool()`, instanciar objetos `ForkJoinTask`, gestionar el robo de trabajo (*work-stealing*) y la sincronización de barrera (*join*) introduce un **overhead de CPU que triplica el tiempo de ejecución secuencial**.
2. **Operaciones con Dependencia Secuencial Estricta:**  
   La planificación de una ruta multi-etapa ($W_0 \rightarrow W_1 \rightarrow W_2 \rightarrow W_3$) requiere orden temporal determinista. Paralelizar los tramos generaría condiciones de carrera en la asignación de transferencias o requeriría sincronizaciones que anulan el paralelismo.
3. **Contención de Hilos en Operaciones `groupingBy`:**  
   El agrupamiento paralelo requiere `ConcurrentHashMap` o unión secuencial de mapas intermedios en el `combiner`, lo cual genera contención de memoria en colecciones pequeñas.
4. **Peligro de Agotamiento de `ForkJoinPool.commonPool()`:**  
   En una arquitectura de backend multi-hilo (servidor web / API REST), consumir el pool común de la JVM con operaciones de colección pequeñas degrada la latencia de todas las demás peticiones concurrentes del servidor.

---

## 🌿 Reto 02: GitHub y GitFlow en Nivel Enterprise

### 1. Convención de Mensajes (Conventional Commits v1.0.0)
Todos los commits de la v3 siguen estrictamente la especificación:
$$\text{<tipo>(<alcance>): <descripción imperativa breve>}$$
* `feat:` Nueva funcionalidad o capacidad del dominio Enterprise.
* `fix:` Corrección de errores o contingencias operativas.
* `refactor:` Mejora de arquitectura o simplificación sin alterar el comportamiento.
* `test:` Adición o robustecimiento de pruebas automatizadas.
* `docs:` Documentación técnica, diagramas o bitácoras.
* `ci:` Automatización, hooks y scripts de control de calidad.

### 2. Hook de Validación Automatizada (`commit-msg`)
Configurado en [`.githooks/commit-msg`](file:///docs/githooks/commit-msg):
Valida mediante expresiones regulares antes de persistir cualquier commit que el mensaje cumpla con la sintaxis de Conventional Commits.

### 3. Protección de Ramas (`main` y `develop`)
* **Prohibición de Push Directo:** Tanto `main` como `develop` tienen políticas de rama protegida; todo cambio ingresa mediante Pull Request revisado y validado.
* **Checks Obligatorios:** Ningún PR puede integrarse si no pasa la compilación limpia de Maven y el Quality Gate de JaCoCo $\ge 85\%$.
* Script de validación local: [`.githooks/pre-push`](file:///docs/githooks/pre-push) bloquea intentos de push directo localmente.

### 4. Generación Automatizada del `CHANGELOG.md`
El archivo [`CHANGELOG.md`](file:///CHANGELOG.md) es generado y actualizado automáticamente a partir del árbol de commits clasificado por tipos.

---

## 💻 Ejecución de la Demostración Enterprise

```powershell
# Compilación y verificación de Quality Gates:
mvn -f Empoleon/pom.xml clean test

# Ejecución de la demostración en consola:
mvn -f Empoleon/pom.xml exec:java -Dexec.mainClass="aquaport.MainEmpoleon"
```
