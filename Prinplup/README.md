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

## 🌿 GitHub y GitFlow en AquaPort v2 (Reto 02)

Estrategia formal de ramas bajo el modelo **GitFlow** implementada para el ciclo de vida de la versión 2.0:

```mermaid
gitGraph
   commit id: "MVP Piplup Aprobado" tag: "v1.0.0"
   branch develop
   checkout develop
   commit id: "feat: Reto 01 Streams Flota 15"
   branch feature/factory-drones
   checkout feature/factory-drones
   commit id: "feat: FabricaDrones"
   checkout develop
   merge feature/factory-drones id: "PR: FabricaDrones"
   branch feature/asignacion-automatica
   checkout feature/asignacion-automatica
   commit id: "feat: AsignadorAutomatico"
   checkout develop
   merge feature/asignacion-automatica id: "PR: AsignacionAuto"
   branch feature/alertas-centro-control
   checkout feature/alertas-centro-control
   commit id: "feat: CentroControlAlertas"
   checkout develop
   merge feature/alertas-centro-control id: "PR: AlertasControl"
   checkout main
   branch hotfix/fix-bateria-critica
   checkout hotfix/fix-bateria-critica
   commit id: "fix: aborto preventivo bateria <20%"
   checkout main
   merge hotfix/fix-bateria-critica id: "Merge Hotfix" tag: "v1.0.1"
   checkout develop
   merge hotfix/fix-bateria-critica id: "Sync Hotfix"
   branch release/v2.0
   checkout release/v2.0
   commit id: "chore: bump version 2.0.0"
   checkout main
   merge release/v2.0 id: "Merge Release v2.0" tag: "v2.0.0"
   checkout develop
   merge release/v2.0 id: "Sync Release v2.0"
```

### 📋 Mapeo de Ramas y Responsabilidades en el Repositorio

| Rama | Tipo GitFlow | Origen | Destino | Propósito / Alcance | Enlace / PR en GitHub |
|---|---|---|---|---|---|
| **`main`** | Producción | — | — | Código estable aprobado en producción (`v1.0.0`, `v1.0.1`, `v2.0.0`). | [Rama `main`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/tree/main) |
| **`develop`** | Integración | `main` | `main` (vía release) | Integración continua de features completadas de la versión 2. | [Rama `develop`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/tree/develop) |
| **`feature/factory-drones`** | Feature | `develop` | `develop` | Implementación del patrón Factory Method ([`FabricaDrones`](file:///Prinplup/src/aquaport/service/FabricaDrones.java)) para creación de drones polimórficos. | [PR `feature/factory-drones`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/pull/new/feature/factory-drones) |
| **`feature/asignacion-automatica`** | Feature | `develop` | `develop` | Asignación inteligente y autónoma según agitación hídrica y carga ([`AsignadorAutomaticoMisiones`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java)). | [PR `feature/asignacion-automatica`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/pull/new/feature/asignacion-automatica) |
| **`feature/alertas-centro-control`** | Feature | `develop` | `develop` | Despacho y monitoreo automático de alertas ante batería crítica y fallos ([`CentroControlAlertas`](file:///Prinplup/src/aquaport/service/CentroControlAlertas.java)). | [PR `feature/alertas-centro-control`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/pull/new/feature/alertas-centro-control) |
| **`hotfix/fix-bateria-critica`** | Hotfix | `main` | `main` & `develop` | Parche urgente en caliente sobre producción: evaluación de batería crítica $< 20\%$ con aborto preventivo. | [PR `hotfix/fix-bateria-critica`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/pull/new/hotfix/fix-bateria-critica) |
| **`release/v2.0`** | Release | `develop` | `main` & `develop` | Estabilización, incremento de versión a `2.0.0` y verificación pre-despliegue. | [PR `release/v2.0`](https://github.com/Oscar10lm/MaterialRefuerzoPiplup/pull/new/release/v2.0) |

### 🏷️ Tags de Versión Oficiales

* **`v1.0.0`**: MVP AquaPort (Piplup) con 14 retos aprobados.
* **`v1.0.1`**: Hotfix urgente sobre producción para validación preventiva de batería crítica $< 20\%$.
* **`v2.0.0`**: AquaPort v2 (Prinplup) — Flota autónoma multimodal de 15 drones con asignación inteligente y alertas.

---

## 🏗️ Patrones de Diseño de la v2 (Reto 03)

La arquitectura de AquaPort v2 integra tres patrones fundamentales del GoF:

### 1. 🏭 Factory Method — ¿Cómo crear cada tipo de drone?
* **Componente:** [`FabricaDrones`](file:///Prinplup/src/aquaport/service/FabricaDrones.java).
* **Rol:** Encapsula la decisión de qué subclase polimórfica instanciar (`DroneSuperficial`, `DroneSemisumergido`, `DroneBuceador`) según el `TipoDrone` solicitado.
* **Ventaja:** Incorporar nuevos tipos de drones no requiere tocar la lógica del asignador ni del consultor de flota (principio OCP).

### 2. 🧭 Strategy — ¿Qué drone elegir?
* **Contrato:** [`EstrategiaSeleccionDrone`](file:///Prinplup/src/aquaport/service/strategy/EstrategiaSeleccionDrone.java).
* **Estrategias concretas:**
  * [`MayorBateriaStrategy`](file:///Prinplup/src/aquaport/service/strategy/MayorBateriaStrategy.java): Maximiza la reserva energética.
  * [`ZonaCercanaStrategy`](file:///Prinplup/src/aquaport/service/strategy/ZonaCercanaStrategy.java): Prioriza proximidad física para reducir traslados en vacío.
  * [`PrioridadCriticaStrategy`](file:///Prinplup/src/aquaport/service/strategy/PrioridadCriticaStrategy.java): Exige batería $\ge 50\%$ y favorece drones de máxima robustez.
* **Cero violaciones:** El [`AsignadorAutomaticoMisiones`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java) no contiene sentencias `instanceof`, delegando de forma 100% polimórfica en la estrategia activa.

### 3. 📢 Observer — ¿Quién se entera del fallo?
* **Contrato:** [`ObservadorFalloDrone`](file:///Prinplup/src/aquaport/service/observer/ObservadorFalloDrone.java).
* **Observadores concretos:**
  * [`CentroControlObserver`](file:///Prinplup/src/aquaport/service/observer/CentroControlObserver.java): Despacha alarmas de monitoreo y telemetría.
  * [`TecnicoMantenimientoObserver`](file:///Prinplup/src/aquaport/service/observer/TecnicoMantenimientoObserver.java): Genera órdenes de trabajo inmediatas para el equipo técnico.
* **Desacoplamiento:** Los observadores se suscriben o desuscriben dinámicamente (`agregarObservador`, `removerObservador`) sin tocar las clases de dominio.

### 🧪 Verificación con Mockito (Notificación Exactamente 1 Vez)
Prueba automatizada en [`PatronesDisenoIntegracionTest.java`](file:///Prinplup/test/aquaport/service/PatronesDisenoIntegracionTest.java):
```java
// Al registrar una misión con drone en estado FALLO:
assertThrows(IllegalStateException.class, () -> asignador.registrarMision(misionConFallo));

// Mockito verifica que cada observador registrado fue notificado exactamente 1 vez:
verify(mockCentroControl, times(1)).notificarFallo(eq(droneEnFallo), anyString());
verify(mockTecnicoMantenimiento, times(1)).notificarFallo(eq(droneEnFallo), anyString());
verifyNoMoreInteractions(mockCentroControl);
verifyNoMoreInteractions(mockTecnicoMantenimiento);
```

---

## 📐 Reto 04: Principios SOLID en AquaPort v2

En la arquitectura de **AquaPort v2 (Prinplup)**, cada componente y abstracción fue diseñado siguiendo rigurosamente los 5 principios SOLID para garantizar extensibilidad sin modificación, sustituibilidad polimórfica y bajo acoplamiento en un entorno crítico de monitoreo ambiental autónomo.

### 📋 Auditoría de Clases e Interfaces de la v2

| Clase / Interfaz | Paquete | Principio(s) Aplicado(s) | Cómo se Aplica en AquaPort v2 |
|---|---|---|---|
| [`DroneAcuatico`](file:///Prinplup/src/aquaport/model/DroneAcuatico.java) | `model` | **OCP, LSP** | Abstracción base inmutable que encapsula atributos universales de la flota y define el método template polimórfico `getTipo()`, extensible a nuevos tipos sin tocar el núcleo. |
| [`DroneSuperficial`](file:///Prinplup/src/aquaport/model/DroneSuperficial.java) | `model` | **SRP, LSP, OCP** | Especialización para aguas calmas con capacidad fija de 500 g. Sustituye limpiamente a `DroneAcuatico` sin romper contratos de validación ni lanzar excepciones imprevistas. |
| [`DroneSemisumergido`](file:///Prinplup/src/aquaport/model/DroneSemisumergido.java) | `model` | **SRP, LSP, OCP** | Especialización para aguas agitadas y corrientes con capacidad de 1.50 kg. Cumple todos los invariantes de batería y disponibilidad de la superclase. |
| [`DroneBuceador`](file:///Prinplup/src/aquaport/model/DroneBuceador.java) | `model` | **SRP, LSP, OCP** | Especialización subacuática con capacidad de 300 g para toma de muestras en profundidad. Sustituible en cualquier pipeline que consuma `DroneAcuatico`. |
| [`TipoDrone`](file:///Prinplup/src/aquaport/model/TipoDrone.java) | `model` | **SRP** | Encapsula exclusivamente la taxonomía categórica de drones admitidos por la plataforma. |
| [`CondicionAgua`](file:///Prinplup/src/aquaport/model/CondicionAgua.java) | `model` | **SRP** | Representa los estados físicos del cuerpo hídrico (`CALMA`, `AGITADA`, `PROFUNDA`) para evaluación desacoplada de aptitud. |
| [`Prioridad`](file:///Prinplup/src/aquaport/model/Prioridad.java) | `model` | **SRP** | Modela los niveles de urgencia (`CRITICA`, `ALTA`, `NORMAL`, `BAJA`) para guiar las políticas de asignación. |
| [`EstadoMision`](file:///Prinplup/src/aquaport/model/EstadoMision.java) | `model` | **SRP** | Modela el ciclo de vida de una orden de trabajo (`PLANIFICADA`, `EN_CURSO`, `COMPLETADA`, `CANCELADA`, `FALLIDA`). |
| [`Mision`](file:///Prinplup/src/aquaport/model/Mision.java) | `model` | **SRP** | Agrega los metadatos de la misión (ruta, prioridad, drone asignado, carga). Única razón de cambio: cambios en la especificación de una misión. |
| [`Alerta`](file:///Prinplup/src/aquaport/model/Alerta.java) | `model` | **SRP** | Encapsula el evento de notificación emitido por anomalías operativas o telemétricas. |
| [`NivelAlerta`](file:///Prinplup/src/aquaport/model/NivelAlerta.java) | `model` | **SRP** | Clasificación de severidad de eventos (`INFO`, `ADVERTENCIA`, `CRITICA`). |
| [`FabricaDrones`](file:///Prinplup/src/aquaport/service/FabricaDrones.java) | `service` | **SRP, OCP** | Centraliza la instanciación de drones según su `TipoDrone`. Aísla el código cliente de conocer los constructores concretos. |
| [`EstrategiaSeleccionDrone`](file:///Prinplup/src/aquaport/service/strategy/EstrategiaSeleccionDrone.java) | `service.strategy` | **OCP, ISP, DIP** | Interfaz delgada enfocada en un único propósito: seleccionar el drone óptimo entre un conjunto de candidatos. |
| [`MayorBateriaStrategy`](file:///Prinplup/src/aquaport/service/strategy/MayorBateriaStrategy.java) | `service.strategy` | **SRP, LSP, DIP** | Implementación concreta de selección por reserva energética máxima. |
| [`ZonaCercanaStrategy`](file:///Prinplup/src/aquaport/service/strategy/ZonaCercanaStrategy.java) | `service.strategy` | **SRP, LSP, DIP** | Implementación concreta de selección por proximidad geográfica para minimizar tránsito en vacío. |
| [`PrioridadCriticaStrategy`](file:///Prinplup/src/aquaport/service/strategy/PrioridadCriticaStrategy.java) | `service.strategy` | **SRP, LSP, DIP** | Implementación concreta de selección para contingencias con umbral de batería $\ge 50\%$. |
| [`ObservadorFalloDrone`](file:///Prinplup/src/aquaport/service/observer/ObservadorFalloDrone.java) | `service.observer` | **ISP, DIP, OCP** | Interfaz funcional segregada (`@FunctionalInterface`) con un único método (`notificarFallo`), evitando obligar a los clientes a implementar métodos irrelevantes. |
| [`CentroControlObserver`](file:///Prinplup/src/aquaport/service/observer/CentroControlObserver.java) | `service.observer` | **SRP, LSP, DIP** | Receptor de alarmas para la estación de control telemétrico. |
| [`TecnicoMantenimientoObserver`](file:///Prinplup/src/aquaport/service/observer/TecnicoMantenimientoObserver.java) | `service.observer` | **SRP, LSP, DIP** | Receptor de incidentes para emisión de órdenes de reparación y triaje. |
| [`AsignadorAutomaticoMisiones`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java) | `service` | **SRP, OCP, DIP** | Coordina la asignación inteligente y el despacho de fallos dependiendo exclusivamente de abstracciones (`EstrategiaSeleccionDrone`, `ObservadorFalloDrone`). |
| [`CentroControlAlertas`](file:///Prinplup/src/aquaport/service/CentroControlAlertas.java) | `service` | **SRP** | Gestiona el registro histórico y despacho de alertas del campus. |
| [`ConsultorFlotaAvanzado`](file:///Prinplup/src/aquaport/service/ConsultorFlotaAvanzado.java) | `service` | **SRP** | Servicio analítico que ejecuta consultas declarativas sobre la flota mediante Streams & Lambdas. |

---

### 🔍 Análisis Detallado de los 5 Principios SOLID

#### 1. S — Single Responsibility Principle (Principio de Responsabilidad Única)
* **Dónde se respeta:**
  * En [`FabricaDrones.java`](file:///Prinplup/src/aquaport/service/FabricaDrones.java): Su única responsabilidad es instanciar la subclase adecuada de `DroneAcuatico` según las especificaciones de fábrica. No realiza validaciones de misiones ni gestiona inventarios.
  * En [`AsignadorAutomaticoMisiones.java`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java): Su único rol es orquestar la asignación y coordinar el despacho de fallos. Delega la lógica de decisión a las estrategias y la reacción a los observadores.
  * En [`CentroControlAlertas.java`](file:///Prinplup/src/aquaport/service/CentroControlAlertas.java): Únicamente almacena y despacha alertas a los suscriptores telemétricos.
* **Por qué importa en AquaPort:**
  En una estación de monitoreo fluvial y lacustre con 15 drones autónomos, separar la toma de decisiones (asignación), la creación de unidades (fábrica), el reporte de incidentes (alertas) y las consultas analíticas evita que un cambio en la normativa de alarmas afecte los algoritmos de vuelo o el motor de despacho.

#### 2. O — Open/Closed Principle (Principio Abierto/Cerrado)
* **Dónde se respeta:**
  * **Jerarquía de Drones abierta a extensión:** Al incorporar un 4to tipo de drone (por ejemplo, `DroneSumergibleProfundo`), **no se modifica en lo absoluto ni una sola línea de [`AsignadorAutomaticoMisiones.java`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java)** ni del modelo base. El asignador interactúa puramente con la abstracción `DroneAcuatico` y sus métodos polimórficos (`getCapacidadCargaKg()`, `getTipo()`, etc.).
  * **Estrategias de Selección abiertas a extensión:** Al incorporar nuevas políticas operativas (como `ZonaCercanaStrategy`, `PrioridadCriticaStrategy` o una estrategia ad-hoc `PrimerDisponibleStrategy`), el asignador las consume a través del contrato [`EstrategiaSeleccionDrone`](file:///Prinplup/src/aquaport/service/strategy/EstrategiaSeleccionDrone.java) sin tocar su implementación ni recurrir a condicionales `if-else` o `switch`.
  * **Sistema de Alertas abierto a extensión:** Se pueden registrar nuevos escuchas (ej. guardacostas, servicios web externos) implementando [`ObservadorFalloDrone`](file:///Prinplup/src/aquaport/service/observer/ObservadorFalloDrone.java) sin alterar la lógica de despacho.
* **Por qué importa en AquaPort:**
  Permite desplegar nuevos prototipos de hardware acuático (drones de profundidad abisal o lanchas de rescate) y nuevas políticas de optimización de combustible/batería en caliente sin recompilar ni arriesgar regresiones en el núcleo de navegación autónoma.

#### 3. L — Liskov Substitution Principle (Principio de Sustitución de Liskov)
* **Dónde se respeta:**
  * En todas las subclases de [`DroneAcuatico`](file:///Prinplup/src/aquaport/model/DroneAcuatico.java): [`DroneSuperficial`](file:///Prinplup/src/aquaport/model/DroneSuperficial.java), [`DroneSemisumergido`](file:///Prinplup/src/aquaport/model/DroneSemisumergido.java), [`DroneBuceador`](file:///Prinplup/src/aquaport/model/DroneBuceador.java) y el nuevo `DroneSumergibleProfundo`.
  * **Cumplimiento estricto de invariantes y contratos:**
    1. Ninguna subclase lanza excepciones imprevistas en métodos de consulta (`getBateria()`, `getCapacidadCargaKg()`, `isDisponible()`, `toString()`).
    2. Ninguna subclase retorna `null` donde el contrato del padre espera un valor consistente o un `Optional`.
    3. Todas las subclases respetan las precondiciones (batería acotada entre $0$ y $100$, capacidades estrictamente positivas) y postcondiciones del padre.
    4. En [`SolidPrincipiosTest.java:L103-L125`](file:///Prinplup/test/aquaport/service/SolidPrincipiosTest.java#L103-L125), se demuestra cómo cualquier colección de `DroneAcuatico` heterogénea se procesa uniformemente sin requerir verificaciones de tipo (`instanceof`) ni casting explícito.
* **Por qué importa en AquaPort:**
  Si un `DroneBuceador` retornara `null` o lanzara `UnsupportedOperationException` al consultar su capacidad de carga o nivel de batería en un algoritmo de asignación masiva, causaría una falla catastrófica en tiempo de ejecución, cancelando operaciones de contención de vertimientos en el campus.

#### 4. I — Interface Segregation Principle (Principio de Segregación de Interfaces)
* **Dónde se respeta:**
  * En [`ObservadorFalloDrone.java`](file:///Prinplup/src/aquaport/service/observer/ObservadorFalloDrone.java): Define un único método enfocado:
    ```java
    @FunctionalInterface
    public interface ObservadorFalloDrone {
        void notificarFallo(DroneAcuatico drone, String motivo);
    }
    ```
    No obliga a [`CentroControlObserver`](file:///Prinplup/src/aquaport/service/observer/CentroControlObserver.java) ni a [`TecnicoMantenimientoObserver`](file:///Prinplup/src/aquaport/service/observer/TecnicoMantenimientoObserver.java) a implementar métodos innecesarios (como monitoreo de GPS en vivo, telemetría de turbinas o control de carga eléctrica).
  * En [`EstrategiaSeleccionDrone.java`](file:///Prinplup/src/aquaport/service/strategy/EstrategiaSeleccionDrone.java): Segregada exclusivamente a la selección (`seleccionar`) y nomenclatura (`getNombre`), sin forzar métodos de serialización o persistencia.
* **Por qué importa en AquaPort:**
  Los observadores técnicos de hardware no tienen por qué acoplarse a interfaces "gordas" que manejen reportes administrativos o facturación, permitiendo componentes ligeros y de bajo consumo de memoria.

#### 5. D — Dependency Inversion Principle (Principio de Inversión de Dependencias)
* **Dónde se respeta:**
  * En [`AsignadorAutomaticoMisiones.java`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java):
    * El asignador (módulo de alto nivel) **no depende de ninguna clase concreta** como `MayorBateriaStrategy` ni `CentroControlObserver`.
    * Depende exclusivamente de **abstracciones**: [`EstrategiaSeleccionDrone`](file:///Prinplup/src/aquaport/service/strategy/EstrategiaSeleccionDrone.java) y [`ObservadorFalloDrone`](file:///Prinplup/src/aquaport/service/observer/ObservadorFalloDrone.java).
    * La estrategia activa y los observadores se inyectan a través del constructor o mediante métodos mutadores (`setEstrategia`, `agregarObservador`):
      ```java
      public AsignadorAutomaticoMisiones(EstrategiaSeleccionDrone estrategia) {
          this.estrategia = Objects.requireNonNull(estrategia, "La estrategia no puede ser nula");
      }
      ```
* **Por qué importa en AquaPort:**
  Permite alternar dinámicamente entre políticas de selección (p. ej., conmutar a `PrioridadCriticaStrategy` cuando se activa un protocolo de emergencia ambiental) e inyectar *test doubles* (mocks de Mockito) en pruebas unitarias sin levantar infraestructura real de radiocomunicación.

---

### 🧪 Pruebas Automatizadas de Principios SOLID

El cumplimiento formal de los principios SOLID se verifica mediante la suite [`SolidPrincipiosTest.java`](file:///Prinplup/test/aquaport/service/SolidPrincipiosTest.java):

```java
@Test
@DisplayName("OCP: Se agrega el 4to tipo de drone (DroneSumergibleProfundo) sin modificar AsignadorAutomaticoMisiones")
void ocp_cuartoTipoDeDrone_funcionaSinModificarAsignador() {
    AsignadorAutomaticoMisiones asignador = new AsignadorAutomaticoMisiones(new MayorBateriaStrategy());
    DroneAcuatico nuevoDrone = new DroneSumergibleProfundo("DSP-01", "Abyssal Explorer 3000", 99, EstadoDrone.DISPONIBLE, ZonasCampus.BAHIA_CIENCIA);

    Optional<DroneAcuatico> seleccionado = asignador.seleccionarDroneOptimo(
            List.of(nuevoDrone), ZonasCampus.BAHIA_CIENCIA, CondicionAgua.PROFUNDA, 0.250
    );

    assertTrue(seleccionado.isPresent());
    assertEquals("DSP-01", seleccionado.get().getId());
}
```

---

## 🗺️ Reto 05: Diagrama de Contexto C4 (MVP v1 vs. v2 Prinplup)

El **Diagrama de Contexto (Nivel 1 del Modelo C4)** establece las fronteras de software de AquaPort, identificando las personas (actores) que interactúan con él y los sistemas externos con los que se comunica en la infraestructura del campus de la Escuela Colombiana de Ingeniería Julio Garavito (ECI).

> [!IMPORTANT]
> **Regla clave del diagrama:** Toda flecha entre el sistema, los actores y los sistemas externos está rigurosamente etiquetada indicando **QUÉ datos específicos fluyen** y en **QUÉ dirección**.

---

### 1. Diagrama de Contexto — AquaPort MVP (v1 - Piplup)

En la versión inicial (MVP), AquaPort operaba como un sistema autónomo cerrado y manual, enfocado en una flota de 4 drones sin integración con servicios externos.

```mermaid
flowchart TD
    subgraph Actores_v1["👥 Actores del Sistema (v1)"]
        SOL["🧑‍🔬 Solicitante<br/>(Investigador / Docente)"]
        OPH["🧑‍✈️ Operador Hídrico<br/>(Personal de Operaciones)"]
        ADM["👨‍💼 Administrador ECI<br/>(Gestión de Infraestructura)"]
    end

    SYS_V1["📦 Sistema AquaPort MVP (v1)<br/><i>Gestión manual de flota y registro en memoria</i>"]

    SOL -->|"1. Envía solicitud de misión (origen, destino, prioridad, peso y tipo de carga)"| SYS_V1
    SYS_V1 -->|"2. Retorna ID de misión, confirmación y estado de despacho"| SOL

    OPH -->|"3. Consulta drones disponibles y asigna manualmente drone a misión"| SYS_V1
    SYS_V1 -->|"4. Retorna listado de flota, nivel de batería y confirmación de asignación"| OPH

    ADM -->|"5. Registra nuevos drones y actualiza inventario"| SYS_V1
    SYS_V1 -->|"6. Retorna métricas básicas de uso y reportes de inventario"| ADM

    style SYS_V1 fill:#1e3a8a,stroke:#3b82f6,stroke-width:3px,color:#ffffff
    style Actores_v1 fill:#f8fafc,stroke:#94a3b8,stroke-width:2px
```

---

### 2. Diagrama de Contexto — AquaPort v2 (Prinplup)

En la **v2 (Prinplup)**, el sistema evoluciona hacia una **estación de control autónoma** que interactúa con un nuevo actor especializado (**Técnico de Mantenimiento**) y **3 sistemas externos** en red para la toma de decisiones automatizada en tiempo real.

```mermaid
flowchart TD
    subgraph Actores_v2["👥 Actores del Sistema (v2)"]
        SOL2["🧑‍🔬 Solicitante<br/><i>(Investigador / Laboratorista)</i>"]
        OPH2["🧑‍✈️ Operador Hídrico<br/><i>(Supervisión y Contingencias)</i>"]
        ADM2["👨‍💼 Administrador ECI<br/><i>(Gobierno y Políticas de Flota)</i>"]
        TEC2["🧑‍🔧 Técnico de Mantenimiento<br/><i>(Diagnóstico y Reparación de Hardware)</i>"]
    end

    SYS_V2["🐧 Sistema AquaPort v2 (Prinplup)<br/><b>Fleet Control Station</b><br/><i>Asignación autónoma, selección óptima y gestión de incidentes</i>"]

    subgraph Sistemas_Externos_v2["🌐 Sistemas Externos Integrados (v2)"]
        API_HID["🌊 API Condiciones Hídricas<br/><i>Sensores limnológicos del campus</i>"]
        CTR_ECI["🏢 Centro de Control ECI<br/><i>Torre de tráfico y logística del campus</i>"]
        SIS_ALT["🚨 Sistema de Alertas<br/><i>Plataforma de alarmas y contingencias</i>"]
    end

    %% Flujos con Actores
    SOL2 -->|"1. Solicitud de misión (origen, destino, prioridad, peso y tipo de carga)"| SYS_V2
    SYS_V2 -->|"2. Confirmación de misión aceptada, drone asignado y telemetría de ruta"| SOL2

    OPH2 -->|"3. Comandos de supervisión pasiva y anulación manual en contingencia"| SYS_V2
    SYS_V2 -->|"4. Telemetría de 15 drones en tiempo real, mapa de flota y estado de misiones"| OPH2

    ADM2 -->|"5. Actualización de políticas operativas, umbrales de batería y zonificación"| SYS_V2
    SYS_V2 -->|"6. Reportes consolidados de utilización, KPIs y salud de la flota"| ADM2

    SYS_V2 -->|"7. Órdenes de trabajo urgentes y código de diagnóstico ante estado FALLO"| TEC2
    TEC2 -->|"8. Registro de mantenimiento completado y reincorporación a DISPONIBLE"| SYS_V2

    %% Flujos con Sistemas Externos
    SYS_V2 -->|"9. Consulta de condiciones hidrológicas por zona (ID zona, coordenadas)"| API_HID
    API_HID -->|"10. Telemetría ambiental en tiempo real (nivel de agitación, profundidad, temperatura)"| SYS_V2

    SYS_V2 -->|"11. Registro de plan de vuelo y solicitud de clearance de ruta acuática"| CTR_ECI
    CTR_ECI -->|"12. Autorización de tránsito hídrico, ventana horaria y clearance de corredor"| SYS_V2

    SYS_V2 -->|"13. Emisión de eventos de anomalía crítica (drone en FALLO, batería crítica < 20%)"| SIS_ALT
    SIS_ALT -->|"14. Despacho multicanal de alarmas (SMS a brigadistas, sirenas de campus, push)"| CTR_ECI

    style SYS_V2 fill:#0284c7,stroke:#0369a1,stroke-width:4px,color:#ffffff
    style Actores_v2 fill:#f8fafc,stroke:#64748b,stroke-width:2px
    style Sistemas_Externos_v2 fill:#f0fdf4,stroke:#22c55e,stroke-width:2px
    style TEC2 fill:#fef3c7,stroke:#f59e0b,stroke-width:2px
```

---

### 3. Matriz de Flujos de Datos Bidireccionales v2

| Origen | Destino | Datos que Fluyen | Propósito Operacional en AquaPort |
|---|---|---|---|
| **Solicitante** | **AquaPort v2** | `PuntoPartida`, `PuntoLlegada`, `Prioridad`, `TipoCarga`, `PesoKg` | Solicitar un transporte o muestreo limnológico. |
| **AquaPort v2** | **Solicitante** | `IdMision`, `EstadoMision`, `IdDroneAsignado`, `TiempoEstimado` | Confirmar y reportar el ciclo de vida de la misión. |
| **Operador Hídrico** | **AquaPort v2** | Comandos de control, pausas de emergencia, override manual | Mantener supervisión humana de alto nivel sin asignación rutinaria. |
| **AquaPort v2** | **Operador Hídrico** | Telemetría en vivo de 15 drones (batería, zona, estado, carga) | Monitoreo situacional del estado del campus en tiempo real. |
| **Administrador ECI** | **AquaPort v2** | Políticas de prioridad, umbrales de corte, zonas permitidas | Gobierno y parametrización de la plataforma. |
| **AquaPort v2** | **Administrador ECI** | Reportes de utilización, horas de operación, tasa de incidentes | Auditoría, planeación y sustentación de recursos. |
| **AquaPort v2** | **Técnico Mantenimiento** | `IdDrone`, `Zona`, `MotivoFallo`, `DiagnosticoPreliminar` | Despachar orden de servicio inmediata cuando un drone entra en `FALLO`. |
| **Técnico Mantenimiento** | **AquaPort v2** | Reporte de reparación, checklist técnico, cambio a `DISPONIBLE` | Devolver drones reparados al pool activo de asignación. |
| **AquaPort v2** | **API Condiciones Hídricas** | `IdZona`, `CoordenadasGPS`, `Timestamp` | Consultar la viabilidad hidrológica del cuerpo de agua antes de asignar. |
| **API Condiciones Hídricas** | **AquaPort v2** | `NivelAgitacion` (Calma/Agitada), `ProfundidadMetros`, `TemperaturaC` | Determinar si se requiere un `DroneSuperficial`, `Semisumergido` o `Buceador`. |
| **AquaPort v2** | **Centro de Control ECI** | `PlanDeVuelo`, `CorredorNavegacion`, `PrioridadMision` | Registrar la misión institucional y solicitar derecho de paso acuático. |
| **Centro de Control ECI** | **AquaPort v2** | `CodigoClearance`, `AprobacionRuta`, `VentanaHoraria` | Garantizar que no colisione con otras actividades náuticas o de mantenimiento. |
| **AquaPort v2** | **Sistema de Alertas** | `IdAlerta`, `NivelAlerta` (CRITICA), `DroneAfectado`, `ZonaIncidente` | Notificar incidentes severos en caliente (batería crítica, fallo de motor). |

---

### 4. Cuadro Comparativo: ¿Qué Creció? vs. ¿Qué se Mantuvo?

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                        EVOLUCIÓN ARQUITECTÓNICA: MVP (v1) → v2                         │
├───────────────────────────────────────────┬────────────────────────────────────────────┤
│               🌱 QUÉ CRECIÓ               │              ⚓ QUÉ SE MANTUVO              │
├───────────────────────────────────────────┼────────────────────────────────────────────┤
│ • Nuevo Actor Especializado:              │ • Propósito Central del Negocio:           │
│   Incorporación del Técnico de Mantenimiento│   Monitoreo ambiental, recolección de      │
│   para el ciclo de vida de fallas físicas.│   muestras y transporte hídrico en campus. │
│                                           │                                            │
│ • 3 Nuevos Sistemas Externos:             │ • Actores Primarios Fundamentales:         │
│   - API Condiciones Hídricas (IoT en vivo)│   Solicitante, Operador Hídrico y          │
│   - Centro de Control ECI (Clearance institucional)│   Administrador ECI continúan activos.     │
│   - Sistema de Alertas (Notificación multicanal)│                                            │
│                                           │ • Reglas de Seguridad Inviolables:         │
│ • Cambio de Paradigma Operativo:          │   Reserva mínima de batería de despegue    │
│   De asignación manual por operador a     │   (≥ 35%), control estricto de carga útil   │
│   asignación autónoma algorítmica.        │   y validación previa de disponibilidad.   │
│                                           │                                            │
│ • Escala y Especialización de la Flota:   │ • Modelo de Dominio Nuclear:               │
│   De 4 drones genéricos a 15 drones       │   Conceptos de Misión, Ruta, Prioridad,    │
│   divididos en 3 subclases hidrológicas.  │   Zonas del campus y Estado del Drone.     │
└───────────────────────────────────────────┴────────────────────────────────────────────┘
```

#### 📊 Detalle Analítico de la Transición

1. **¿Qué creció?**
   * **El ecosistema de integración:** El sistema dejó de ser una aplicación aislada para convertirse en un nodo central de un ecosistema interconectado con sensores ambientales (API de condiciones hídricas), autoridad de tráfico (Centro de Control) y plataformas de respuesta a emergencias (Sistema de Alertas).
   * **El flujo de entrada de datos:** Ahora el sistema consume variables hidrológicas dinámicas (profundidad, agitación, corrientes) que condicionan la selección del hardware antes de despachar una misión.
   * **El canal de soporte técnico:** La incorporación del *Técnico de Mantenimiento* cierra la brecha entre el software y el hardware físico: las fallas no quedan como simples registros mudos, sino que generan órdenes de servicio operativas.

2. **¿Qué se mantuvo?**
   * **La esencia funcional:** Garantizar el transporte confiable de cargas científicas y toma de muestras a lo largo del sistema lagunar de la Escuela.
   * **El gobierno institucional:** El *Administrador ECI* mantiene el control sobre la flota y el *Operador Hídrico* conserva la potestad de veto e intervención humana en caso de emergencia.
   * **Los invariantes de seguridad:** La integridad física de los drones y la preservación del cuerpo de agua permanecen como la máxima prioridad inmutable del diseño.

---

## 📋 Reto 06: Especificación de Requisitos (RF/RNF), Priorización MoSCoW y Resolución de Tensión AP-07 vs AP-08

> 📄 *Documentación formal completa disponible en: [`docs/requisitos-moscow-prinplup.md`](file:///docs/requisitos-moscow-prinplup.md)*

### 1. Cuatro Nuevos Requisitos Funcionales (RF) — v2
* **RF-01 (Asignación Automática por Strategy):**  
  *Actor:* Sistema AquaPort / Solicitante.  
  *Acción:* Evalúa la flota de 15 drones y selecciona automáticamente la unidad óptima según la política activa ([`MayorBateriaStrategy`](file:///Prinplup/src/aquaport/service/strategy/MayorBateriaStrategy.java), [`ZonaCercanaStrategy`](file:///Prinplup/src/aquaport/service/strategy/ZonaCercanaStrategy.java) o [`PrioridadCriticaStrategy`](file:///Prinplup/src/aquaport/service/strategy/PrioridadCriticaStrategy.java)).  
  *Resultado Observable:* Drone reservado, misión en estado `PENDIENTE` y confirmación de plan de ruta.
* **RF-02 (Alertas de Fallo en Tiempo Real - Observer):**  
  *Actor:* Sistema AquaPort / Centro de Control / Técnico de Mantenimiento.  
  *Acción:* Al detectarse un drone en estado `FALLO` o batería crítica $< 20\%$, emite el evento síncrono a todos los observadores registrados.  
  *Resultado Observable:* Alerta severa en [`CentroControlObserver`](file:///Prinplup/src/aquaport/service/observer/CentroControlObserver.java) y orden técnica prioritaria con diagnóstico en [`TecnicoMantenimientoObserver`](file:///Prinplup/src/aquaport/service/observer/TecnicoMantenimientoObserver.java).
* **RF-03 (Consulta y Filtrado por Condiciones Hídricas):**  
  *Actor:* Sistema AquaPort (consumiendo la *API Condiciones Hídricas*).  
  *Acción:* Consulta las condiciones limnológicas (`CALMA`, `AGITADA`, `PROFUNDA`) antes de autorizar la misión.  
  *Resultado Observable:* Filtra y descarta drones incompatibles de casco/inmersión (solo `Superficial` en aguas calmas $\le 500\text{g}$, `Semisumergido` en aguas agitadas $\le 1.5\text{kg}$, y `Buceador` en profundidad $\le 300\text{g}$).
* **RF-04 (Gestión de Órdenes y Reintegración por Técnico):**  
  *Actor:* Técnico de Mantenimiento.  
  *Acción:* Consulta los diagnósticos de fallo, efectúa las reparaciones mecánicas/electrónicas y reporta la finalización en el sistema.  
  *Resultado Observable:* El drone transiciona a `DISPONIBLE`, restableciendo su elegibilidad para nuevas asignaciones autónomas.

---

### 2. Cuatro Requisitos No Funcionales (RNF) con Métrica Objetiva Concreta

> [!TIP]
> **Sin ambigüedades:** Todo RNF declara métrica cuantitativa, condición de carga y método/instrumento de verificación reproducible.

| ID | Requisito No Funcional | Métrica Objetiva Concreta | Método de Verificación |
|---|---|---|---|
| **RNF-01** | **Rendimiento de Asignación Automática** | Selección y asignación del drone óptimo en **$< 400\text{ ms}$** para una flota ampliada de hasta **30 drones** en memoria. | Test unitario con JUnit 5: `assertTimeout(Duration.ofMillis(400), ...)` en [`AsignadorAutomaticoMisionesTest`](file:///Prinplup/test/aquaport/service/AsignadorAutomaticoMisionesTest.java). |
| **RNF-02** | **Latencia de Despacho de Eventos (Observer)** | Tiempo total de notificación a observadores **$\le 50\text{ ms}$** con tasa de entrega del **$100\%$** (0 eventos perdidos). | Test de integración con Mockito: `verify(..., times(1)).notificarFallo(...)` y cronometrado en [`PatronesDisenoIntegracionTest`](file:///Prinplup/test/aquaport/service/PatronesDisenoIntegracionTest.java). |
| **RNF-03** | **Robustez e Invariantes de Seguridad (*Fail-Fast*)** | **$0$** asignaciones con batería $< 35\%$ ($< 50\%$ en críticas) o sobrepeso, con rechazo tipado en **$< 10\text{ ms}$**. | Aserciones negativas de JUnit 5: `assertThrows(IllegalStateException.class, ...)` en validadores y asignador. |
| **RNF-04** | **Extensibilidad Arquitectónica (OCP)** | **$0$ líneas modificadas** en [`AsignadorAutomaticoMisiones.java`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java) al agregar un 4to tipo de drone o nueva estrategia. | Prueba automatizada en [`SolidPrincipiosTest`](file:///Prinplup/test/aquaport/service/SolidPrincipiosTest.java) y verificación con `git diff`. |

---

### 3. Matriz de Priorización MoSCoW

| ID | Tipo | Prioridad | Justificación MoSCoW |
|---|---|---|---|
| **RF-01** | Funcional | **Must Have** | Capacidad nuclear que sustituye la asignación manual humana por decisiones algorítmicas autónomas. |
| **RF-02** | Funcional | **Must Have** | Crítico para evitar el extravío o hundimiento de drones ante fallas en los embalses. |
| **RF-03** | Funcional | **Must Have** | Previene volcamientos de drones ligeros en zonas de corriente y oleaje moderado. |
| **RF-04** | Funcional | **Should Have** | Necesario para la autosostenibilidad de la flota en el mediano plazo tras reparaciones. |
| **RNF-01** | No Funcional | **Must Have** | Respuestas sub-segundo garantizan que no haya bloqueos en despachos de emergencia. |
| **RNF-02** | No Funcional | **Must Have** | La cuadrilla técnica requiere enterarse de forma instantánea ante un fallo físico. |
| **RNF-03** | No Funcional | **Must Have** | La seguridad física de equipos y preservación del agua es la restricción máxima ineludible. |
| **RNF-04** | No Funcional | **Should Have** | Mantiene bajo costo de mantenimiento al expandir la flota institucional con nuevos modelos. |

---

### ⚡ 4. Detección y Resolución de la Tensión: AP-07 vs. AP-08

#### 🔍 La Tensión Detectada:
* **AP-07:** *El sistema asigna automáticamente el drone de mayor batería disponible para cualquier misión hídrica.*
* **AP-08:** *Las misiones CRÍTICAS tienen prioridad absoluta: deben recibir el drone técnicamente más apto para la zona, independientemente de la batería.*

**¿Por qué no son contradictorios?**  
Porque aplican bajo objetivos operacionales distintos: AP-07 rige misiones ordinarias para balancear el ciclo de vida de las celdas eléctricas; AP-08 rige contingencias ambientales donde lo que prima es la supervivencia de la misión y la adecuación estructural del casco.

**¿Dónde radica la tensión real?**  
Si coexisten en el mismo cuerpo de agua:
1. Un `DroneSuperficial` con **$95\%$ de batería** (máxima batería, pero frágil ante turbulencia).
2. Un `DroneSemisumergido` con **$88\%$ de batería** (especializado para corrientes y agitación).

Si se aplica ciegamente **AP-07**, se elegiría el Superficial de 95%, resultando en volcamiento. Si se aplica **AP-08** sin umbrales ("independientemente de la batería"), se podría despachar un Semisumergido con solo 5% de batería, apagándose en mitad del embalse.

#### 🛠️ Resolución Arquitectónica en AquaPort v2:
1. **Fase 1 — Invariante Físico de Aptitud (Hard Filter):** Ningún drone participa en la selección si no es apto para la zona y condición hídrica (`condicionAgua.esCompatibleCon(d.getTipo())`) y si su carga útil no soporta el peso (`capacidadCargaKg >= peso`).
2. **Fase 2 — Umbral Diferenciado de Seguridad de Batería:**
   * Misión Estándar: Batería $\ge 35\%$.
   * Misión Crítica: Batería $\ge 50\%$ ([`PrioridadCriticaStrategy.BATERIA_MINIMA_CRITICA`](file:///Prinplup/src/aquaport/service/strategy/PrioridadCriticaStrategy.java)).
3. **Fase 3 — Conmutación Heurística Polimórfica (Strategy Pattern):**
   * Ante prioridad `CRITICA`, el asignador emplea [`PrioridadCriticaStrategy`](file:///Prinplup/src/aquaport/service/strategy/PrioridadCriticaStrategy.java), la cual favorece el tipo de máxima robustez estructural ([`TipoDrone.SEMISUMERGIDO`](file:///Prinplup/src/aquaport/model/TipoDrone.java)) asegurando simultáneamente aptitud técnica (**AP-08**) y suficiente reserva energética (**AP-07**).
   * Verificado formalmente en el test [`resolucionTension_ap07_vs_ap08_misionCriticaPriorizaAptitudTecnica`](file:///Prinplup/test/aquaport/service/AsignadorAutomaticoMisionesTest.java).

---

## 📑 Reto 07: Plantilla DOSW (RF AP-07: Asignación Automática de Drones)

> 📄 *Documento formal estandarizado disponible en: [`docs/plantilla-dosw-ap07.md`](file:///docs/plantilla-dosw-ap07.md)*

### 1. Estructura de Datos de Entrada y Salida (AP-07)

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| `carga` | — | Objeto / Contenedor de la carga científica | **Sí** |
| `carga.peso` | Integer | Entre **1 y 1500 gramos** ($0.001\text{ kg}$ a $1.500\text{ kg}$) | **Sí** |
| `carga.tipo` | Enum(`MUESTRA_AGUA`, `SENSOR`, `PAQUETE_LIGERO`, `EQUIPO_MEDICION`) | Tipología de material admitido en los lagos del campus | **Sí** |
| `carga.prioridad` | Enum(`CRITICA`, `ALTA`, `NORMAL`, `BAJA`) | Define urgencia e impacta la estrategia de asignación | **Sí** |
| `zonaDestino` | Enum(`EMBALSE_NORTE`, `CANAL_CENTRAL`, `LAGUNA_SUR`, `RIBERA_ESTE`, `LAB_HIDRICO`) | Zonas hídricas oficiales del campus universitario | **Sí** |
| `droneAsignado` | — | Calculado por el sistema según la estrategia activa | **No (salida)** |
| `droneAsignado.id` | String | Formato `AR-XX` (o id de serie `DS-XX`, `DM-XX`, `DB-XX`) | **No (salida)** |
| `droneAsignado.tipo` | String | `SUPERFICIAL` / `SEMISUMERGIDO` / `BUCEADOR` | **No (salida)** |

---

### 2. Flujo Básico de Eventos (7 Pasos)

1. **Recibir Solicitud:** El sistema recibe los parámetros obligatorios de misión (`zonaDestino`, `carga.peso`, `carga.tipo`, `carga.prioridad`).
2. **Consultar Condiciones Hídricas:** El motor consulta síncronamente la API externa para obtener el estado del agua (`CALMA`, `AGITADA`, `PROFUNDA`), profundidad y temperatura.
3. **Filtrar Drones Aptos para la Zona:** Se aíslan los drones en estado `DISPONIBLE`, en la zona adecuada y con casco compatible con las condiciones de navegación.
4. **Aplicar Estrategia Activa:** Se invoca la heurística polimórfica inyectada (`MayorBateriaStrategy`, `ZonaCercanaStrategy` o `PrioridadCriticaStrategy` si la prioridad es `CRITICA`).
5. **Validar Invariantes de Seguridad:** Verificación *fail-fast* de precondiciones: batería $\ge 35\%$ ($\ge 50\%$ en críticas), peso $\le$ capacidad del modelo, y compatibilidad física de casco.
6. **Asignar y Construir Misión:** Se reserva el drone en estado `EN_MISION` y se instancia la `Mision` inmutable con estado `PENDIENTE`.
7. **Notificar Observadores y Confirmar:** Se notifica el despacho al Centro de Control y Técnicos (`ObservadorFalloDrone`), retornando los datos de salida al solicitante.

---

### 3. Flujos Alternos

* **Flujo Alterno A (Sin drones disponibles):**
  * *Condición:* Ningún drone en la zona está disponible con batería $\ge 35\%$.
  * *Resultado:* El sistema encola la solicitud, emite advertencia al operador y no altera el estado de la flota remanente.
* **Flujo Alterno B (Condiciones hídricas adversas):**
  * *Condición:* La API reporta ráfagas severas o agitación por encima del umbral estructural de los drones.
  * *Resultado:* Se aborta el despacho, emite alerta de severidad `ADVERTENCIA` al Centro de Control y notifica al solicitante la suspensión preventiva por clima.
* **Flujo Alterno C (Carga supera capacidad del tipo):**
  * *Condición:* Se solicita una inmersión profunda (`DroneBuceador`) pero el peso de la muestra es $> 300\text{ g}$ (ej. 450 g).
  * *Resultado:* El sistema bloquea la asignación lanzando `IllegalStateException`, notificando que el `DroneBuceador` tiene límite de 300g y sugiriendo fraccionar la carga.

---

### 4. Reglas de Negocio Clave (RN)

* **RN-01 (Autonomía mínima de despegue):** Mínimo **$35\%$ de batería** para misiones estándar; mínimo **$50\%$ de batería** para misiones de prioridad `CRITICA`.
* **RN-02 (Límites de carga por modelo):**
  * 🤿 `DroneBuceador`: Hasta **$300\text{ g}$** ($0.30\text{ kg}$). Prohibido para cargas superiores.
  * 🚤 `DroneSuperficial`: Hasta **$500\text{ g}$** ($0.50\text{ kg}$).
  * 🚢 `DroneSemisumergido`: Hasta **$1500\text{ g}$** ($1.50\text{ kg}$).
* **RN-03 (Compatibilidad de casco con hidrodinámica):** Prohibido asignar `DroneSuperficial` en aguas agitadas o misiones subacuáticas.

---

## 🎨 Reto 08: Identidad Visual, Sistema de Diseño y UX (Prinplup v2)

> 🌐 **Prototipo HTML Interactivo:** [`docs/aquaport-v2-ux-flota-misiones.html`](file:///docs/aquaport-v2-ux-flota-misiones.html)  
> 📄 **Manual Completo de Diseño:** [`docs/manual-identidad-ux-prinplup.md`](file:///docs/manual-identidad-ux-prinplup.md)

### 1. Componentes y Estados Diseñados

#### A. Tarjeta de Drone Acuático (Los 6 Estados)
| Estado | Color Primario | Tratamiento Visual | Semántica Operativa |
|---|---|---|---|
| 🟢 **Disponible** | `#10B981` (Verde) | Borde superior verde esmeralda, badge `● DISPONIBLE`. | Unidad lista para recibir asignaciones automáticas ($\ge 35\%$ batería). |
| 🔵 **En Misión** | `#3B82F6` (Azul) | Borde azul océano, código de misión en curso (`MIS-042`). | Navegando activamente en ruta hidrológica. |
| 🟡 **Recargando** | `#F59E0B` (Amarillo) | Borde ámbar, tiempo restante de recarga eléctrica. | Anclado en estación base; botón de asignación bloqueado. |
| ⚪ **Mantenimiento** | `#64748B` (Gris) | Borde gris pizarra, orden de trabajo técnica en taller. | En calibración física de sensores o motores. |
| 🔴 **Fallo Crítico** | `#EF4444` (Rojo) | Borde rojo pulsante, alarma visual y diagnóstico de incidente. | Incidente de navegación (batería $< 20\%$, avería); cuadrilla alertada. |
| 🌌 **Sumergido** | `#1E3A8A` (Azul Oscuro) | Borde azul abisal, profundidad negativa (ej. `-3.40 m`). | `DroneBuceador` en inmersión activa tomando muestras en el fondo. |

#### B. Botón "Asignar Misión" (Los 5 Estados)
* **Default:** Fondo degradado cian (`#0284C7` $\rightarrow$ `#38BDF8`), texto contrastante, sombra sutil.
* **Hover:** Elevación con resplandor (*glow*) (`box-shadow`), borde brillante, transformación `translateY(-2px)`.
* **Procesando:** Fondo translúcido, cursor `wait`, spinner giratorio 360° y texto: *"Calculando ruta óptima..."*.
* **Éxito:** Fondo verde esmeralda con checkmark animado: *"✔ ¡Misión Asignada!"*.
* **Deshabilitado:** Opacidad al 40%, cursor `not-allowed`, candado: *"🔒 Bloqueado (< 35%)"*.

#### C. Indicador de Batería (Los 3 Umbrales)
* 🟩 **$\ge 60\%$ (Verde `#10B981`):** Nivel óptimo. Apto para misiones largas o contingencias críticas.
* 🟨 **$35\% - 59\%$ (Amarillo `#F59E0B`):** Nivel medio. Apto para misiones cortas dentro de la misma zona.
* 🟥 **$< 35\%$ (Rojo `#EF4444`):** Nivel crítico. Barra pulsante con advertencia: *"⚠ Batería crítica < 35% — Despegue bloqueado"*.

---

### 2. Flujo Completo de Asignación Automática (3 Pantallas)

```mermaid
flowchart LR
    P1["1️⃣ Panel de Flota<br/><i>(15 Drones agrupados por tipo)</i>"] 
    -->|"Clic 'Iniciar Asignación'"| P2["2️⃣ Detalle de Misión<br/><i>(Formulario + Telemetría API)</i>"]
    -->|"Asignar Automáticamente"| P3["3️⃣ Confirmación & Despacho<br/><i>(Clearance Centro Control + Ruta)</i>"]
```

1. **Pantalla 1 (Panel de Flota):** Visualización global de los 15 drones distribuidos en el campus. Incluye selector por tipo de drone, filtros de estado, telemetría general y botón de emergencia prominente.
2. **Pantalla 2 (Detalle y Configuración de Misión):** Formulario parametrizado (origen, destino, tipo de carga, peso $1$ a $1500$g, prioridad). Panel en vivo de la **API Condiciones Hídricas** con variables ambientales (calma/agitación/profundidad) y preselección algorítmica.
3. **Pantalla 3 (Confirmación y Despacho):** Tarjeta destacada del drone óptimo asignado, código de clearance otorgado por el Centro de Control ECI, visualizador de corredor náutico y confirmación de lanzamiento.

---

### 3. Fundamentación Ergonómica: Leyes de Fitts y Hick

* **🛑 Ley de Fitts ($T = a + b \log_2(2D/W)$) en el Botón de Emergencia:**  
  Ante un escape de reactivos o pérdida de telemetría de un drone en el agua, el operador humano necesita accionar la parada en milisegundos. El **Botón de Emergencia** se fijó en la esquina superior derecha del panel (borde infinito de pantalla donde la distancia $D$ es mínima y fija), con dimensiones sobredimensionadas ($W = 185\text{px}$, $H = 44\text{px}$) y color rojo contrastante pulsante, minimizando drásticamente el tiempo de movimiento $T$ a una respuesta refleja sub-segundo ($< 300\text{ ms}$).
* **🧠 Ley de Hick ($T = b \log_2(n + 1)$) en la Agrupación de los 15 Drones:**  
  Presentar una cuadrícula caótica con 15 drones desordenados ($n = 15$) eleva exponencialmente el tiempo de búsqueda cognitiva del operador. Se aplicó la Ley de Hick estructurando la flota en **3 grupos jerárquicos por especialidad técnica** (Superficial, Semisumergido, Buceador) con sub-filtros de zona, reduciendo las opciones inmediatas a $n = 3$, lo que agiliza la toma de decisiones críticas en más de un $60\%$.

---

## 🏃‍♂️ Reto 09: Agilismo y Planificación del Sprint en Jira

> 📄 *Documentación completa del sprint disponible en: [`docs/agilismo-jira-sprint-prinplup.md`](file:///docs/agilismo-jira-sprint-prinplup.md)*

### 1. Sprint Goal de la v2

> **Sprint Goal:**  
> *«Completar el asignador automático con Strategy y las alertas con Observer»*

* **Compromiso del Sprint:** **21 Story Points (SP)** (Secuencia de Fibonacci: 1, 2, 3, 5, 8).
* **Duración:** 2 semanas (10 días hábiles).

---

### 2. Product Backlog del Sprint (Historias de Usuario con Fibonacci)

| ID | Tipo | Historia de Usuario | Story Points | Prioridad | Estado |
|---|---|---|---|---|---|
| **PRIN-01** | Story | Asignador automático de misiones mediante Patrón Strategy | **5 SP** | Highest | `IN PROGRESS` |
| **PRIN-02** | Story | Notificación síncrona de eventos de fallo mediante Patrón Observer | **5 SP** | Highest | `DONE` |
| **PRIN-03** | Story | Consulta reactiva a API de Condiciones Hídricas y filtrado de casco | **3 SP** | High | `IN REVIEW / QA` |
| **PRIN-04** | Story | Fábrica polimórfica de 3 tipos de drones acuáticos (Factory Method) | **3 SP** | High | `DONE` |
| **PRIN-05** | Task | Resolución de tensión AP-07 vs AP-08: Precedencia en misiones críticas | **3 SP** | High | `IN PROGRESS` |
| **PRIN-06** | Story | Reintegración técnica y gestión de drones en estado FALLO/MANT | **2 SP** | Medium | `TO DO` |

---

### 3. Definition of Done (DoD) de la v2

Para dar por concluida cualquier Historia de Usuario en este sprint, debe satisfacerse el $100\%$ del checklist:
1. [✔] **Cero Warnings:** El código compila limpiamente sin advertencias del compilador.
2. [✔] **Pruebas Unitarias:** La suite completa pasa al 100% en JUnit 5 y Mockito.
3. [✔] **Cobertura JaCoCo:** Reporta **$\ge 80\%$ de line coverage** para las clases de la feature.
4. [✔] **Pull Request Aprobado:** Revisión técnica formal (*Code Review*) aprobada por al menos un par.
5. [✔] **Documentación Actualizada:** El diagrama de contexto C4 y la plantilla DOSW están sincronizados con los cambios.

---

### 4. Captura del Tablero Activo del Sprint en Jira

![Jira Sprint Board](../docs/images/jira-sprint-board.png)

---

## 📐 Reto 10: Diagrama de Casos de Uso (UML v2)

> 📊 **Artefacto PlantUML:** [`docs/diagrama-cu-prinplup.puml`](file:///docs/diagrama-cu-prinplup.puml)  
> 🖼️ **Diagrama Renderizado:** [`docs/diagrama-cu-prinplup.png`](file:///docs/diagrama-cu-prinplup.png)

### 1. Actores del Sistema en la v2
* **`Usuario del Sistema` (Actor Base ECI):** Abstracción general de usuario institucional autenticado.
* **`Operador Hídrico` (Especialización):** Hereda del *Usuario del Sistema*; supervisa la flota de 15 drones y gestiona la asignación autónoma.
* **`Solicitante` (Especialización):** Hereda del *Usuario del Sistema*; radica solicitudes de transporte de reactivos y muestreo ambiental.
* **`Técnico de Mantenimiento` (Nuevo v2):** Receptor especializado de órdenes de trabajo cuando un drone entra en `FALLO`; repara y reintegra unidades a estado `DISPONIBLE`.
* **`Centro de Control ECI` (Nuevo v2 / Autoridad):** Sistema/autoridad institucional que aprueba ventanas horarias y otorga el *clearance* de navegación en los cuerpos de agua del campus.

---

### 2. Casos de Uso Nucleares y Relaciones `<<include>>` / `<<extend>>`

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│               RELACIONES CLAVE DEL CU CENTRAL (AP-07: ASIGNACIÓN AUTOMÁTICA)           │
├────────────────────────────────────────────────────────────────────────────────────────┤
│ • <<include>> (Obligatorio):                                                          │
│   "AP-07 Asignar Misión Automáticamente" ──────>> "Validar Condiciones Hídricas"      │
│   (El sistema consulta SIEMPRE la API de telemetría limnológica antes de asignar).   │
│                                                                                        │
│ • <<include>> (Clearance):                                                             │
│   "AP-07 Asignar Misión Automáticamente" ──────>> "Autorizar Corredor Acuático"       │
│   (Requiere clearance institucional emitido por el Centro de Control ECI).             │
│                                                                                        │
│ • <<extend>> (Condicional):                                                            │
│   "Notificar Fallo al Técnico" ──────>> "AP-07 Asignar Misión Automáticamente"         │
│   [Punto de extensión / Condición: Se activa ÚNICAMENTE si un drone entra en estado    │
│    FALLO durante el proceso de asignación o validación].                               │
│                                                                                        │
│ • Generalización de Actores:                                                           │
│   Operador Hídrico ──▷ Usuario del Sistema  |  Solicitante ──▷ Usuario del Sistema     │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

### 3. Diagrama de Casos de Uso UML (v2 Prinplup)

![Diagrama de Casos de Uso Prinplup](../docs/diagrama-cu-prinplup.png)

---

## 🎨 Reto 11: Mocks y Diseño de Pantallas con IA (v2 Prinplup)

Para materializar el principio fundamental de la v2 (*"el sistema decide solo; el operador supervisa, no asigna"*), se diseñó el flujo completo de asignación autónoma asistido por IA, compuesto por **3 pantallas operativas principales** y **1 pantalla de contingencia crítica**:

1. **Pantalla 1 — Panel de Flota (15 Drones):** Vista global agrupada en 3 tipos de casco con insignias cromáticas de estado y batería.
2. **Pantalla 2 — Detalle de Misión & Recomendación IA:** Parámetros de la solicitud, condiciones del cuerpo de agua (API externa) y justificación explicable de la selección algorítmica.
3. **Pantalla 3 — Confirmación de Despacho & Flota Actualizada:** Trazado de ruta en mapa batimétrico, telemetría y contadores reactivos de la flota.
4. **Pantalla 4 — Alerta Crítica (Misión sin Drones Aptos):** Protocolo de excepción visual ante falta de unidades para misiones con prioridad `CRITICA`.

### 1. Galería de Pantallas del Flujo Autónomo

#### Pantalla 1: Panel de Flota de 15 Drones Agrupados por Tipo
![Panel de Flota de 15 Drones](../docs/images/mock-panel-flota-prinplup.jpg)
> **Prompt IA utilizado:**  
> `Modern dark-themed UI dashboard for AquaPort autonomous water drone fleet management. High-tech dashboard showing 15 aquatic drones grouped into three sections: Superficial Drones, Semi-submerged Drones, and Submarine Diver Drones. Each drone has a sleek card showing battery level (green, yellow, red), operational status badge (DISPONIBLE in green, EN_MISION in blue, RECARGANDO in yellow, MANTENIMIENTO in gray, FALLO in red, SUMERGIDO in dark blue), payload capacity, and current water campus zone. Beautiful glassmorphic UI, glowing blue and cyan neon accents, clean typography, professional naval tech interface.`

#### Pantalla 2: Detalle de Misión con Drone Recomendado y Justificación Algorítmica
![Detalle de Misión y Recomendación](../docs/images/mock-detalle-mision-prinplup.jpg)
> **Prompt IA utilizado:**  
> `Futuristic dark-themed UI screen for AquaPort autonomous drone mission detail and AI recommendation. Left panel displays mission parameters: Mission ID M-AUTO-2026, Origin: Embalse Norte, Destination: Laguna Sur, Priority badge CRITICA in glowing orange-red, Payload: Water Sample 250g, Hydrological conditions: Agitated Water Level 3, Depth 8m. Center panel highlights 'RECOMMENDED DRONE: DM-03 Wave-Ranger M2 (Semisumergido)' with 90 percent battery in green, showing 3D wireframe render of the drone. Below it, an AI reasoning breakdown box: 'Strategy: MayorBateriaStrategy + ZonaCercana. Drone selected because Semisumergido hull is required for agitated water conditions, and has optimal 90% battery margin.' Large prominent glowing cyan CTA button: 'AUTORIZAR Y ASIGNAR MISION'.`

#### Pantalla 3: Confirmación de Despacho con Estado Actualizado de la Flota
![Confirmación de Despacho](../docs/images/mock-confirmacion-flota-prinplup.jpg)
> **Prompt IA utilizado:**  
> `Sleek dark-themed UI confirmation screen for AquaPort autonomous dispatch. Success banner with green neon checkmark reading 'MISION ASIGNADA Y DESPACHADA EXITOSAMENTE - CODIGO M-AUTO-2026'. Telemetry tracking map showing route from Embalse Norte to Laguna Sur with animated glowing path. Bottom dashboard shows the updated fleet status overview: Total Active Drones 15, Available: 7, On Mission: 3, Charging: 2, Maintenance: 2, Fault: 1. A real-time notification feed on the right side: 'Centro de Control notificado (ACK 200)', 'Técnico de Mantenimiento sincronizado'. Modern clean sci-fi operations center UI.`

#### Pantalla 4: Alerta de Contingencia — Misión CRÍTICA sin Drones Disponibles
![Alerta Crítica Sin Drones](../docs/images/mock-alerta-mision-critica-prinplup.png)
> **Prompt IA utilizado:**  
> `Dark-themed emergency alert UI screen for AquaPort autonomous operations center. Prominent red and crimson pulsing alarm banner across the top reading 'ALERTA CRITICA: MISION DE EMERGENCIA SIN DRONES APTOS DISPONIBLES'. Center modal dialog displaying mission details: Mission ID M-CRIT-99, Origin Bahía Ciencia, Destination Laguna Sur, Agitated Water Level 5, Required Payload 1.2kg. Diagnostic readout showing '0 Drones Available Meeting Technical Requirements (Semisumergido hull, Battery >50%)'. Emergency actions section: Large red emergency button 'SOLICITAR RELEVO DE FLOTA EXTERNA' and secondary button 'ACTIVAR MANTENIMIENTO PRIORITARIO'. Real-time push alert log showing 'CentroControlObserver: Notificado (Nivel Crítico)' and 'TecnicoMantenimientoObserver: Alerta enviada'. Futuristic cybernetic security operations console.`

### 2. Fundamentos de Ergonomía Cognitiva y UX
* **Ley de Hick ($T = b \cdot \log_2(n + 1)$):** La agrupación categórica de 15 drones en 3 familias de casco (`Superficial`, `Semisumergido`, `Buceador`) acorta el tiempo de scanning del operador frente a una lista homogénea de 15 filas desordenadas.
* **Ley de Fitts ($ID = \log_2(2D/W)$):** Tanto el botón principal de autorización como los botones de contingencia ante alertas críticas poseen amplias áreas de impacto (anchura extendida y bordes luminosos), asegurando tiempos de reacción mínimos y reduciendo la tasa de error por clic.
* Documento detallado de diseño y prompts: [`docs/mocks-ia-asignacion-automatica-prinplup.md`](../docs/mocks-ia-asignacion-automatica-prinplup.md).

---

## 🧪 Reto 12: Desarrollo Guiado por Pruebas (TDD) con Mockito y Casos de Contingencia

La lógica del [`AsignadorAutomaticoMisiones`](file:///Prinplup/src/aquaport/service/AsignadorAutomaticoMisiones.java) fue implementada y blindada siguiendo el ciclo riguroso de **TDD (Test-Driven Development)** en [`AsignadorAutomaticoTDDTest.java`](file:///Prinplup/test/aquaport/service/AsignadorAutomaticoTDDTest.java), garantizando resiliencia ante contingencias operativas mediante dobles de prueba (Mocks).

### 1. Las 5 Pruebas Obligatorias del Reto 12

| # | Prueba Obligatoria | Escenario Evaluado | Comportamiento del Asignador / Aserción |
|---|---|---|---|
| **1** | **Happy Path (Asignación Exitosa)** | Flota con drones aptos y estrategia activa operativa. | Asigna el drone óptimo, retorna la `Mision` en estado `PENDIENTE` y valida que ningún observador sea alertado por error (`never().notificarFallo(...)`). |
| **2** | **Misión CRÍTICA sin Drones Aptos** | Solicitud con `Prioridad.CRITICA` cuando no hay drones disponibles que cumplan los requisitos ambientales. | Notifica inmediatamente a todos los observadores registrados (`times(1).notificarFallo(isNull(), contains("ALERTA CRÍTICA"))`) y lanza `IllegalStateException`. |
| **3** | **Drone en FALLO durante Asignación** | La estrategia devuelve inicialmente un drone cuya telemetría reporta `EstadoDrone.FALLO`. | Descarta el drone averiado, notifica a los observadores (`notificarFallo(eq(droneEnFallo), ...)`), lo remueve de la lista de candidatos y reintenta seleccionando automáticamente un drone alternativo funcional. |
| **4** | **Lista de Drones Vacía** | Se suministra una flota vacía (`List.of()`) al servicio. | Lanza `IllegalStateException` defensivamente informando la imposibilidad de operar sin unidades. |
| **5** | **Umbral Mínimo de Batería (Edge Case)** | Frontera exacta de batería: 35% operativo y 50% para misiones críticas. | Valida rigurosamente que $35\%$ sea aceptado pero $34\%$ descartado en misiones regulares; y que $50\%$ sea admitido pero $49\%$ descartado en misiones críticas. |

### 2. Demostración TDD con Mockito (`AsignadorAutomaticoTDDTest.java`)

```java
@ExtendWith(MockitoExtension.class)
class AsignadorAutomaticoTDDTest {

    @Mock private EstrategiaSeleccionDrone estrategiaMock;
    @Mock private ObservadorFalloDrone observadorMock;
    @Mock private ObservadorFalloDrone observadorSecundarioMock;
    @InjectMocks private AsignadorAutomaticoMisiones asignador;

    @Test
    @DisplayName("Misión CRÍTICA sin drones disponibles notifica a todos los observadores y lanza excepción")
    void misionCritica_sinDronesDisponibles_notificaObservadoresYLanzaExcepcion() {
        // ARRANGE
        Mision misionCritica = crearMisionSolicitud("M-CRIT-99", ZonasCampus.EMBALSE_NORTE, ZonasCampus.LAGUNA_SUR, Prioridad.CRITICA, TipoCarga.EQUIPO_MEDICION);
        when(estrategiaMock.seleccionar(any(), anyString(), any(), anyDouble(), any())).thenReturn(Optional.empty());
        when(estrategiaMock.getNombre()).thenReturn("EstrategiaMock");

        asignador.registrarObservador(observadorMock);
        asignador.registrarObservador(observadorSecundarioMock);

        // ACT & ASSERT
        assertThrows(IllegalStateException.class, () -> asignador.asignar(misionCritica, List.of(), CondicionAgua.AGITADA));

        // Todos los observadores notificados exactamente una vez (Reto 03 & Reto 12)
        verify(observadorMock, times(1)).notificarFallo(isNull(), argThat(m -> m.contains("ALERTA CRÍTICA")));
        verify(observadorSecundarioMock, times(1)).notificarFallo(isNull(), argThat(m -> m.contains("ALERTA CRÍTICA")));
    }
}
```

### 3. Suite Completa de Pruebas Unitarias de Prinplup (70 Pruebas Passing)

El proyecto cuenta con **70 pruebas automatizadas** con **0 fallos, 0 errores y 100% de éxito**:
* [`AsignadorAutomaticoTDDTest.java`](file:///Prinplup/test/aquaport/service/AsignadorAutomaticoTDDTest.java) (7 pruebas TDD con Mockito para contingencias y límites)
* [`AsignadorAutomaticoMisionesTest.java`](file:///Prinplup/test/aquaport/service/AsignadorAutomaticoMisionesTest.java) (14 pruebas: estrategia, rendimiento RNF-01 <400ms, ramas de lógica)
* [`ModelosDominioTest.java`](file:///Prinplup/test/aquaport/model/ModelosDominioTest.java) (11 pruebas exhaustivas para las clases y enums de dominio)
* [`ConsultorFlotaAvanzadoTest.java`](file:///Prinplup/test/aquaport/service/ConsultorFlotaAvanzadoTest.java) (12 pruebas de Streams y Collectors)
* [`PatronesDisenoIntegracionTest.java`](file:///Prinplup/test/aquaport/service/PatronesDisenoIntegracionTest.java) (5 pruebas de integración Strategy-Observer-Factory)
* [`SolidPrincipiosTest.java`](file:///Prinplup/test/aquaport/service/SolidPrincipiosTest.java) (4 pruebas de validación de principios SOLID)
* [`FabricaDronesTest.java`](file:///Prinplup/test/aquaport/service/FabricaDronesTest.java) (4 pruebas de Factory Method)
* [`CentroControlAlertasTest.java`](file:///Prinplup/test/aquaport/service/CentroControlAlertasTest.java) (6 pruebas de alertas y logging)
* [`PrinplupServiciosYObservadoresTest.java`](file:///Prinplup/test/aquaport/service/PrinplupServiciosYObservadoresTest.java) (4 pruebas de robustez y CLI)
* [`EstrategiasSeleccionTest.java`](file:///Prinplup/test/aquaport/service/strategy/EstrategiasSeleccionTest.java) (3 pruebas de estrategias polimórficas)

```powershell
mvn -f Prinplup/pom.xml test
```

---

## 📊 Reto 13: Cobertura de Código con JaCoCo y Quality Gate en Maven (v2 Prinplup)

En la v2 de AquaPort (Prinplup), el asignador automático y los servicios de flota incorporan mayor complejidad de lógica condicional: prioridades múltiples (`CRITICA`, `ALTA`, `NORMAL`, `BAJA`), tipos de drone especializados (`SUPERFICIAL`, `SEMISUMERGIDO`, `BUCEADOR`), condiciones hídricas dinámicas (`TRANQUILA`, `AGITADA`, `PROFUNDA`) y umbrales de seguridad energética.

### 1. Configuración del Quality Gate en Maven (`pom.xml`)

Se configuró el plugin `jacoco-maven-plugin` (0.8.11) con la fase de verificación `check` enlazada al ciclo de vida `test`. Si la cobertura de líneas o ramas cae por debajo del **$80\%$ ($\ge 0.80$)**, el build de Maven **falla inmediatamente**:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>test</phase>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                    <rule>
                        <element>CLASS</element>
                        <includes>
                            <include>aquaport.model.*</include>
                        </includes>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

---

### 2. Reporte de Cobertura Obtenido vs. Meta Exigida ($\ge 80\%$)

| Paquete | Clase evaluada | Line Coverage | Branch Coverage | Instruction Coverage | Estado Quality Gate |
|---|---|---|---|---|---|
| `aquaport.model` | **`DroneAcuatico`** | **100.0%** (22/22) | **100.0%** (4/4) | **100.0%** (136/136) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`DroneSuperficial`** | **100.0%** (5/5) | — | **100.0%** (22/22) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`DroneSemisumergido`** | **100.0%** (5/5) | — | **100.0%** (22/22) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`DroneBuceador`** | **100.0%** (5/5) | — | **100.0%** (22/22) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`Mision`** | **100.0%** (19/19) | **100.0%** (2/2) | **100.0%** (125/125) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`EstadoDrone`** | **100.0%** (14/14) | **100.0%** (8/8) | **100.0%** (105/105) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`CondicionAgua`** | **100.0%** (11/11) | — | **100.0%** (66/66) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`Prioridad`** | **100.0%** (12/12) | **100.0%** (2/2) | **100.0%** (76/76) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`TipoDrone`** | **100.0%** (12/12) | — | **100.0%** (66/66) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`TipoCarga`** | **100.0%** (11/11) | — | **100.0%** (69/69) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`ZonasCampus`** | **100.0%** (11/11) | — | **100.0%** (17/17) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`AlertaFlota`** | **100.0%** (6/6) | **100.0%** (2/2) | **100.0%** (63/63) | ✅ Superado ($\ge 80\%$) |
| `aquaport.model` | **`EstadoMision`** | **100.0%** (9/9) | — | **100.0%** (59/59) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service` | **`AsignadorAutomaticoMisiones`** | **100.0%** (44/44) | **100.0%** (18/18) | **100.0%** (218/218) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service.strategy` | **`MayorBateriaStrategy`** | **100.0%** (10/10) | **100.0%** (12/12) | **100.0%** (57/57) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service.strategy` | **`PrioridadCriticaStrategy`** | **100.0%** (14/14) | **100.0%** (16/16) | **100.0%** (82/82) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service.strategy` | **`ZonaCercanaStrategy`** | **100.0%** (12/12) | **100.0%** (12/12) | **100.0%** (62/62) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service.observer` | **`CentroControlObserver`** | **100.0%** (11/11) | **100.0%** (4/4) | **100.0%** (54/54) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service.observer` | **`TecnicoMantenimientoObserver`** | **100.0%** (11/11) | **100.0%** (4/4) | **100.0%** (54/54) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service` | **`ConsultorFlotaAvanzado`** | **100.0%** (44/44) | **100.0%** (24/24) | **100.0%** (149/149) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service` | **`FabricaDrones`** | **88.9%** (8/9) | **100.0%** (3/3) | **86.6%** (58/67) | ✅ Superado ($\ge 80\%$) |
| `aquaport.service` | **`CentroControlAlertas`** | **94.6%** (35/37) | **88.9%** (8/9) | **95.9%** (141/147) | ✅ Superado ($\ge 80\%$) |
| `aquaport` | **`MainPrinplup`** | **100.0%** (78/78) | — | **99.2%** (487/491) | ✅ Superado ($\ge 80\%$) |
| **GLOBAL (BUNDLE)** | **AquaPort v2 (Prinplup)** | **99.4%** (347/349) | **99.2%** (123/124) | **99.1%** (1995/2012) | ✅ **QUALITY GATE APROBADO** |

---

### 3. Matriz de Ramas Amarillas Resueltas y Justificación de Prueba

Las ramas amarillas (parcialmente cubiertas) en JaCoCo ocurren cuando una sentencia condicional (`if`, operador ternario o predicado de filtro) solo es evaluada para uno de sus valores booleanos (`true` o `false`). A continuación se documenta cada caso identificado, la prueba escrita y su relevancia operacional:

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                 RESOLUCIÓN DE RAMAS AMARILLAS                                    │
├──────────────────────────────────────────────────────────────────────────────────────────────────┤
│ 1. Parámetros Nulos en Seleccionar Drone (`flota == null || zona == null || condicion == null`) │
│    • Prueba: seleccionarDroneOptimo_parametrosNulos_retornaVacio()                               │
│    • Importancia: En telemetría hídrica en vivo, una pérdida temporal de enlace con sensores     │
│      o zonas no mapeadas podría inducir parámetros nulos. El sistema debe retornar un Optional   │
│      vacío defensivamente en lugar de lanzar NullPointerException no capturado en producción.    │
│                                                                                                  │
│ 2. Umbral de Batería Mínima Operativa (`d.getBateria() >= BATERIA_MINIMA_OPERATIVA [35%]`)       │
│    • Prueba: seleccionarDroneOptimo_bateriaMenorA35_descartada()                                 │
│    • Importancia: Un drone con <35% de batería no tiene autonomía suficiente para completar un   │
│      circuito de ida y vuelta con carga. Probar la rama falsa asegura que ningún algoritmo       │
│      asigne unidades en reserva crítica, previniendo varamiento y pérdida de activos náuticos.   │
│                                                                                                  │
│ 3. Idempotencia y Nulidad de Observadores (`observador != null && !observadores.contains(obs)`)  │
│    • Prueba: gestionObservadores_ramasDefensivas()                                               │
│    • Importancia: Previene suscripción duplicada de observadores (que dispararía órdenes de      │
│      trabajo redundantes a los técnicos) y garantiza tolerancia ante referencias nulas sin caídas│
│      en bucles de notificación.                                                                  │
│                                                                                                  │
│ 4. Registro de Misión con Drone Saludable (`drone != null && drone.getEstado() == FALLO`)       │
│    • Prueba: registrarMision_droneSaludable_retornaMisionSinNotificarObservadores()              │
│    • Importancia: Antes solo se probaba la excepción ante drone en FALLO. Cubrir la rama donde   │
│      el drone está DISPONIBLE garantiza que las operaciones regulares transcurran normalmente sin│
│      disparar falsas alarmas ni bloquear el flujo de trabajo.                                    │
│                                                                                                  │
│ 5. Compatibilidad Dinámica de Condiciones Hídricas (`condicion.esCompatibleCon(d.getTipo())`)   │
│    • Prueba: PrioridadCriticaStrategy y ZonaCercanaStrategy con drones incompatibles             │
│    • Importancia: Un Drone Superficial volcaría o se averiaría en aguas AGITADAS o PROFUNDAS.   │
│      Verificar la rama donde la condición es incompatible asegura que la flotilla frágil sea     │
│      rigurosamente filtrada, delegando únicamente a unidades estructuralmente aptas (Semisumerg).│
│                                                                                                  │
│ 6. Desempate y Fallbacks en Estrategias de Selección (`candidatos == null`, `zona == null`)     │
│    • Prueba: EstrategiasSeleccionTest (cobertura total de 3 estrategias)                         │
│    • Importancia: Si no se especifica una zona o se evalúa una flota vacía, las estrategias     │
│      deben aplicar heurísticas globales transparentes o retornar Optional.empty limpiamente.     │
│                                                                                                  │
│ 7. Manejo de Identidad de Drone Nulo en Notificaciones de Falla (Ternarios `DESCONOCIDO`)        │
│    • Prueba: observadores_droneNulo_manejaTernariosDefensivos()                                  │
│    • Importancia: Alertas de contingencia emitidas por fallos de comunicación antes de enlazar   │
│      un drone concreto deben formatear bitácoras consistentes ("DESCONOCIDO") sin NullPointer.   │
└──────────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 🛡️ Reto 14: Análisis Estático con SonarQube Community (v2 Prinplup)

Auditoría estática de calidad de código y deuda técnica ejecutada con **SonarQube Community**. Todos los Bugs y Vulnerabilidades han sido resueltos en su causa raíz, la deuda técnica final es **0 minutos (0.0% < 30 min)** y la duplicación de código es **0.0% (< 5.0%)** para el release v2 oficial de AquaPort (Prinplup).

### 1. 🚦 Prioridad de Issues en SonarQube

| Tipo | Descripción | Prioridad | Acción tomada en AquaPort v2 |
|---|---|---|---|
| 🔴 **Bug** | Código que claramente produce un comportamiento incorrecto | **Resolver siempre, primero** | Se corrigió `java:S2259` en `AsignadorAutomaticoMisiones` (prevención de NPE). |
| 🟠 **Vulnerability** | Código que puede ser explotado por un atacante | **Resolver siempre** | 0 vulnerabilidades identificadas en 24 clases. |
| 🟡 **Code Smell** | Código difícil de mantener o entender | **Resolver, pero es el último** | Se eliminaron todos los smells (`S106`, `S2384`, `S1192`, `S1118`, `S1128`). |

> [!CAUTION]
> **⚠ Regla Fundamental: Prohibido `@SuppressWarnings`:**  
> Suprimir un issue es ignorarlo, no resolverlo. La deuda técnica sigue existiendo en el sistema; solo la estás escondiendo del analizador. En AquaPort v2 no se utiliza ninguna anotación `@SuppressWarnings` para evadir análisis de calidad.

---

### 2. 📸 Captura del Dashboard de SonarQube (4 Indicadores en Verde)

![SonarQube Dashboard Prinplup](../docs/images/sonarqube-dashboard-prinplup.png)

---

### 3. 🎯 Resumen de Indicadores del Quality Gate v2

| Indicador SonarQube | Métrica / Umbral | **Resultado AquaPort v2** | Estado |
|---|---|---|---|
| 🟢 **Reliability (Confiabilidad)** | 0 Bugs | **0 Bugs (Rating A)** | ✅ 0 deuda |
| 🟢 **Security (Seguridad)** | 0 Vulnerabilidades | **0 Vulnerabilities (Rating A)** | ✅ 0 deuda |
| 🟢 **Security Review** | 100% Hotspots revisados | **0 Hotspots (100% revisados, Rating A)** | ✅ Seguro |
| 🟢 **Maintainability (Mantenibilidad)** | 0 Code Smells / Deuda $< 30$ min | **0 Code Smells (0 min, Rating A)** | ✅ Deuda Cero |
| 🧪 **Coverage (Cobertura)** | Cobertura de código $\ge 80.0\%$ | **99.4%** (347 / 349 líneas) | ✅ Superado |
| 📄 **Duplications (Duplicación)** | Bloques duplicados $< 5.0\%$ | **0.0%** (0 líneas duplicadas) | ✅ 0 duplicación |

---

### 4. 📋 Matriz de Issues Resueltos: Causa Raíz, Corrección y Justificación

| Tipo / Regla | Componente | Causa Raíz (Qué era el problema) | Corrección Aplicada | Por qué la corrección es adecuada |
|---|---|---|---|---|
| 🔴 **Bug** (`java:S2259`) | `AsignadorAutomaticoMisiones` | Desreferenciación potencial de drone nulo en `registrarMision` y listas nulas en stream del asignador | Guardas defensivas `drone != null`, `Objects.requireNonNull` y retornos tempranos de `Optional.empty()` | Previene caídas en caliente por telemetría nula o mensajes malformados sin colapsar el hilo de asignación |
| 🟡 **Code Smell** (`java:S106`) | `CentroControlObserver`, `TecnicoMantenimientoObserver`, `CentroControlAlertas` | Mensajes emitidos mediante `System.out` sin marcas de tiempo ni granularidad de severidad | Sustitución por `java.util.logging.Logger` con niveles `SEVERE`, `WARNING` e `INFO` | Habilita observabilidad institucional estructurada y auditoría forense sin añadir dependencias pesadas |
| 🟡 **Code Smell** (`java:S2384`) | `AsignadorAutomaticoMisiones`, `CentroControlAlertas`, Observadores | Retorno directo de listas internas mutables exponiendo la representación interna | Encapsulamiento con `Collections.unmodifiableList(...)` o streams inmutables `.toList()` | Previene que clientes externos muten el estado interno (como borrar observadores o alterar alertas) |
| 🟡 **Code Smell** (`java:S1192` / `java:S1118`) | `ZonasCampus`, `MainPrinplup` | Duplicación de literales de zonas campus $\ge 3$ veces y clases utilitarias con constructores públicos | Centralización en constantes públicas de `ZonasCampus` y constructores privados con `UnsupportedOperationException` | Single Source of Truth (SSOT) para la topología física y prevención de instanciaciones innecesarias |
| 🟡 **Code Smell** (`java:S1128`) | Módulo `aquaport.model` y `aquaport.service` | Sentencias de importación huérfanas no utilizadas que introducen ruido visual | Limpieza completa de imports no referenciados a lo largo del código v2 | Optimiza la legibilidad del código fuente y agiliza el tiempo de análisis estático |

> Documento completo de auditoría y commits: [`docs/sonarqube-analisis-prinplup.md`](../docs/sonarqube-analisis-prinplup.md)

## 💻 Ejecución de la Demostración

```powershell
# Compilación y ejecución directa con Maven:
mvn -f Prinplup/pom.xml compile exec:java -Dexec.mainClass="aquaport.MainPrinplup"

# O mediante javac/java:
javac -d Prinplup/target/classes Prinplup/src/aquaport/model/*.java Prinplup/src/aquaport/service/*.java Prinplup/src/aquaport/*.java
java -cp Prinplup/target/classes aquaport.MainPrinplup
```


