# 🐧 Prinplup — AquaPort v2: Flota Autónoma con Alertas

> **Nivel Intermedio de Evolución del Sistema AquaPort (ECI)**  
> 15 drones especializados de 3 tipos, asignación autónoma basada en condiciones hídricas, alertas de anomalías y precedencia absoluta de misiones críticas.  
> *En esta fase el sistema decide solo: el operador humano supervisa, no asigna.*

---

## 🌊 Contexto Operativo v2

La Escuela Colombiana de Ingeniería Julio Garavito (ECI) expande su infraestructura de monitoreo ambiental acuático a **15 drones** agrupados en tres categorías especializadas:

| Tipo de Drone | Entorno Operativo | Capacidad Máxima | Propósito Principal |
|---|---|---|---|
| 🚤 **Superficial** (`DroneSuperficial`) | Zonas tranquilas de embalses y lagunas | Hasta **500 g** (0.50 kg) | Transporte rápido de muestras superficiales y reactivos ligeros. |
| 🚢 **Semisumergido** (`DroneSemisumergido`) | Aguas agitadas, canales y corrientes moderadas | Hasta **1.50 kg** (1.50 kg) | Transporte de sensores hidrológicos pesados y navegación resiliente. |
| 🤿 **Buceador** (`DroneBuceador`) | Inmersión subacuática y aguas profundas | Hasta **300 g** (0.30 kg) | Extracción de muestras de sedimentos y parámetros en el fondo hídrico. |

### 🔄 Flujo Autónomo de Asignación v2

$$\text{Solicitud de Misión} \longrightarrow \text{Consulta Condiciones del Agua} \longrightarrow \text{Filtrado por Zona y Tipo} \longrightarrow \text{Estrategia de Selección Óptima} \longrightarrow \text{Validación} \longrightarrow \text{Asignación Automática} \longrightarrow \text{Alerta al Centro de Control}$$

---

## 🧩 Modelo de Dominio Ampliado

* **`EstadoDrone` (Enum):**  
  `DISPONIBLE`, `EN_MISION`, `RECARGANDO`, `MANTENIMIENTO`, `SUMERGIDO`.  
  *Contiene métodos utilitarios como `isDisponible()` y `isActivo()`.*
* **`Prioridad` (Enum):**  
  `CRITICA` *(Precedencia absoluta por contingencia o derrame)*, `ALTA`, `NORMAL`, `BAJA`.
* **`DroneAcuatico` (Clase Abstracta):**  
  Base polimórfica inmutable con atributos `id`, `modelo`, `bateria`, `estado`, `zona`, `capacidadCargaKg` y método abstracto `getTipo()`.
* **Subclases Especializadas:**  
  * [`DroneSuperficial`](file:///Prinplup/src/aquaport/model/DroneSuperficial.java)
  * [`DroneSemisumergido`](file:///Prinplup/src/aquaport/model/DroneSemisumergido.java)
  * [`DroneBuceador`](file:///Prinplup/src/aquaport/model/DroneBuceador.java)
* **`Mision`:**  
  Asocia ruta (`puntoPartida`, `puntoLlegada`), `prioridad`, `drone`, `tipoCarga` y `estado`.

---

## 🎯 Reto 01: Streams & Lambdas Avanzados en Flota de 15 Drones

Implementado en el servicio [`ConsultorFlotaAvanzado`](file:///Prinplup/src/aquaport/service/ConsultorFlotaAvanzado.java) bajo el paradigma de pipelines funcionales puros (sin bucles mutables ni efectos colaterales).

### 1️⃣ Agrupar drones disponibles por tipo de drone (`groupingBy`)
```java
Map<TipoDrone, List<DroneAcuatico>> disponiblesPorTipo = flota.stream()
        .filter(DroneAcuatico::isDisponible)
        .collect(Collectors.groupingBy(DroneAcuatico::getTipo));
```
* **Resultado:**  
  * **Superficial (2 disponibles):** `DS-01` (Bat: 95%), `DS-03` (Bat: 40%).  
  * **Semisumergido (3 disponibles):** `DM-01` (Bat: 88%), `DM-03` (Bat: 90%), `DM-04` (Bat: 55%).  
  * **Buceador (3 disponibles):** `DB-01` (Bat: 92%), `DB-03` (Bat: 65%), `DB-04` (Bat: 30%).

---

### 2️⃣ Obtener el drone óptimo para una zona dada (mayor batería, tipo compatible)
```java
Optional<DroneAcuatico> optimo = flota.stream()
        .filter(DroneAcuatico::isDisponible)
        .filter(drone -> zona.equalsIgnoreCase(drone.getZona()))
        .filter(drone -> drone.getTipo() == tipoCompatible)
        .max(Comparator.comparingInt(DroneAcuatico::getBateria));
```
* **Caso aplicado:** En `"Embalse Norte"` con requerimiento `SEMISUMERGIDO` $\longrightarrow$ Selecciona **`DM-03`** (Batería: 90%).

---

### 3️⃣ Calcular el promedio de batería por tipo de drone (`averagingDouble`)
```java
Map<TipoDrone, Double> promedios = flota.stream()
        .collect(Collectors.groupingBy(
                DroneAcuatico::getTipo,
                Collectors.averagingDouble(DroneAcuatico::getBateria)
        ));
```
* **Resultados en la flota de 15 drones:**  
  * 🚢 **Semisumergido:** `65.6%`  
  * 🤿 **Buceador:** `55.4%`  
  * 🚤 **Superficial:** `46.4%`

---

### 4️⃣ Separar misiones CRÍTICAS de las demás (`partitioningBy`)
```java
Map<Boolean, List<Mision>> particionadas = misiones.stream()
        .collect(Collectors.partitioningBy(
                mision -> mision.getPrioridad() == Prioridad.CRITICA
        ));
```
* **`true` (Misiones Críticas):**  
  * `MIS-202` (Embalse Norte $\rightarrow$ Bahía Ciencia, Drone `DM-03`).  
  * `MIS-204` (Bahía Ciencia $\rightarrow$ Laguna Sur, Drone `DB-01`).
* **`false` (Misiones Regulares / Otras Prioridades):**  
  * `MIS-201` (NORMAL), `MIS-203` (ALTA), `MIS-205` (BAJA).

---

### 5️⃣ Listar todas las zonas únicas que cubre la flota activa (`distinct`, `sorted`)
```java
List<String> zonas = flota.stream()
        .filter(DroneAcuatico::isActivo)
        .map(DroneAcuatico::getZona)
        .distinct()
        .sorted()
        .toList();
```
* **Zonas cubiertas:** `Bahía Ciencia`, `Canal Central`, `Dársena Oeste`, `Embalse Norte`, `Laboratorio Hídrico`, `Laguna Sur`, `Punto Ribereño Este`.

---

### ➕ Operación Complementaria: Cola de Misiones por Precedencia y Batería
```java
List<Mision> ordenadas = misiones.stream()
        .sorted(Comparator.comparing((Mision m) -> m.getPrioridad().getNivel())
                .thenComparing(m -> m.getDrone().getBateria(), Comparator.reverseOrder()))
        .toList();
```
*Garantiza que las misiones de mayor urgencia se despachen primero, y ante igual nivel de prioridad se favorezca el drone con mayor reserva energética.*

---

## 🧪 Pruebas Unitarias (TDD / JUnit 5)

Las pruebas están ubicadas en [`Prinplup/test/aquaport/service/ConsultorFlotaAvanzadoTest.java`](file:///Prinplup/test/aquaport/service/ConsultorFlotaAvanzadoTest.java):

```powershell
mvn -f Prinplup/pom.xml test
```

* **Total pruebas:** 12 ejecutadas, 0 fallos, 0 errores.
* **Cobertura:** Verificada con JaCoCo (`target/site/jacoco/index.html`).

---

## 💻 Ejecución de la Demostración

```powershell
# Compilación y ejecución directa con Maven:
mvn -f Prinplup/pom.xml compile exec:java -Dexec.mainClass="aquaport.MainPrinplup"

# O mediante javac/java:
javac -d Prinplup/target/classes Prinplup/src/aquaport/model/*.java Prinplup/src/aquaport/service/*.java Prinplup/src/aquaport/*.java
java -cp Prinplup/target/classes aquaport.MainPrinplup
```
