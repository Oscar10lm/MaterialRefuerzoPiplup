# 💧 Piplup — AquaPort MVP: Reparto Básico en el Campus

Sistema de drones acuáticos autónomos de la **Escuela Colombiana de Ingeniería Julio Garavito (ECI)** para transporte de muestras y sensores entre zonas hídricas.

---

## 📌 Contexto del Dominio
- **Flota actual:** 4 drones acuáticos modelo `Aqua-Ranger 100`.
- **Carga:** Muestras de agua, sensores ligeros y paquetes de reactivos.
- **Zonas:** Embalse Norte, Canal Central, Laguna Sur, Punto Ribereño Este y Laboratorio Hídrico.

---

## 🗺️ Reto 05 — Diagrama de Contexto C4 (Nivel 1)

El diagrama de contexto representa al sistema **AquaPort MVP** como una caja negra central, modelando las personas (actores) y los flujos de información bidireccionales que entran y salen del sistema.

### 🖼️ Diagrama Generado
![Diagrama de Contexto C4 - AquaPort MVP](../docs/c4-contexto-piplup.png)

> Archivo fuente editable disponible en: [`docs/c4-contexto-piplup.puml`](../docs/c4-contexto-piplup.puml)

### 👥 Actores y Flujos de Información

| Actor / Entidad | Rol / Responsabilidad | Flujo de Entrada (Hacia AquaPort) | Flujo de Salida (Desde AquaPort) |
|---|---|---|---|
| **Solicitante** | Investigador / Laboratorista | Solicitud de transporte de muestra ambiental o sensor | Código de misión generado |
| **Operador Hídrico** | Personal técnico del campus | Solicitud de misión y asignación manual de drone | Confirmación de asignación y estado de misión en tiempo real |
| **Administrador ECI** | Responsable directivo de la flota | Gestión de inventario de flota y consultas | Métricas operativas, disponibilidad y reportes |
| **Sistemas Externos** | *Ninguno* | *Sin integraciones externas en el alcance del MVP* | *N/A* |

---

## 🚀 Reto 01 — Streams & Lambdas

Implementación del servicio `ConsultorFlota` que responde a las necesidades operativas de la estación mediante **Java Streams** puros, eliminando por completo bucles iterativos y estados mutables.

### 📐 Flota Base de Prueba

```java
List<DroneAcuatico> flota = List.of(
    new DroneAcuatico("AR-01", "Aqua-Ranger 100", 92, true,  "Embalse Norte"),
    new DroneAcuatico("AR-02", "Aqua-Ranger 100", 45, true,  "Canal Central"),
    new DroneAcuatico("AR-03", "Aqua-Ranger 100", 18, false, "Laguna Sur"),
    new DroneAcuatico("AR-04", "Aqua-Ranger 100", 73, true,  "Punto Ribereño Este")
);
```

### 🔍 Explicación Visual de los 5 Streams

#### 1. Drones disponibles con batería ≥ 35% ordenados descendentemente
**Firma:** `List<DroneAcuatico> obtenerDronesDisponiblesConBateriaSuficiente(List<DroneAcuatico> flota)`
```text
  flota (List<DroneAcuatico>)
    │
    ├──> stream()
    ├──> filter(DroneAcuatico::disponible)                [Descarta AR-03 (no disponible)]
    ├──> filter(d -> d.bateria() >= 35)                   [Mantiene AR-01, AR-04, AR-02]
    ├──> sorted(Comparator.comparingInt(...).reversed())  [Ordena: 92% -> 73% -> 45%]
    └──> toList()                                         [Retorna lista inmutable]
```

#### 2. Solo IDs de drones disponibles
**Firma:** `List<String> obtenerIdsDronesDisponibles(List<DroneAcuatico> flota)`
```text
  flota (List<DroneAcuatico>)
    │
    ├──> stream()
    ├──> filter(DroneAcuatico::disponible)                [Mantiene AR-01, AR-02, AR-04]
    ├──> map(DroneAcuatico::id)                           [Transforma objeto -> String "AR-XX"]
    └──> toList()                                         [Retorna: ["AR-01", "AR-02", "AR-04"]]
```

#### 3. Existencia de drone disponible con batería ≥ 35%
**Firma:** `boolean existeDroneDisponibleConBateriaSuficiente(List<DroneAcuatico> flota)`
```text
  flota (List<DroneAcuatico>)
    │
    ├──> stream()
    └──> anyMatch(d -> d.disponible() && d.bateria() >= 35) [Short-circuit: retorna true en AR-01]
```

#### 4. Conteo de drones disponibles
**Firma:** `long contarDronesDisponibles(List<DroneAcuatico> flota)`
```text
  flota (List<DroneAcuatico>)
    │
    ├──> stream()
    ├──> filter(DroneAcuatico::disponible)                [Filtra elementos activos]
    └──> count()                                          [Retorna: 3L]
```

#### 5. Drone con mayor nivel de batería
**Firma:** `Optional<DroneAcuatico> obtenerDroneConMayorBateria(List<DroneAcuatico> flota)`
```text
  flota (List<DroneAcuatico>)
    │
    ├──> stream()
    └──> max(Comparator.comparingInt(DroneAcuatico::bateria)) [Retorna Optional con AR-01 (92%)]
```

---

## 🛠 Reto 03 — Patrón Creacional Builder (`Mision.Builder`)

Resuelve el problema de constructores telescópicos con múltiples parámetros ambiguos, garantizando **inmutabilidad** y **validación estricta en el método `build()`**.

```java
Mision mision = new Mision.Builder()
    .id("M-001")
    .drone(droneDisponible)
    .puntoPartida("Embalse Norte")
    .puntoLlegada("Laboratorio Hídrico")
    .tipoCarga(TipoCarga.MUESTRA_AGUA)
    .estado(EstadoMision.PENDIENTE)
    .build(); // Lanza IllegalStateException si algún campo es inválido o el drone no está disponible
```

---

## 🏛 Reto Arquitectura — Principios SRP y DIP en el Núcleo

### 1. Principio de Responsabilidad Única (SRP)
En lugar de un `GestorMision` monolítico con múltiples motivos para cambiar, el sistema separa las responsabilidades en componentes especializados:

* **`RegistradorMisiones`**: Orquesta el registro y las consultas de misiones.
* **`ValidadorMision`**: Encargado exclusivamente de las políticas y reglas de negocio operativas.
* **`NotificadorOperador`**: Encargado exclusivamente de emitir mensajes, alertas y reportes al operador.

### 2. Principio de Inversión de Dependencias (DIP)
`RegistradorMisiones` no se acopla a ningún almacenamiento concreto. Depende de la interfaz abstracta [`RepositorioMisiones`](src/aquaport/repository/RepositorioMisiones.java), inyectada por constructor (**Constructor Injection**):

```java
RepositorioMisiones repo = new RepositorioMisionesEnMemoria();
ValidadorMision validador = new ValidadorMision();
NotificadorOperador notificador = new NotificadorOperador();

RegistradorMisiones registrador = new RegistradorMisiones(repo, validador, notificador);
```

### 3. Diagrama de Clases (UML)

```mermaid
classDiagram
    class RepositorioMisiones {
        <<interface>>
        +guardar(Mision mision) void
        +buscarPorId(String id) Optional~Mision~
        +obtenerTodas() List~Mision~
        +existePorId(String id) boolean
    }

    class RepositorioMisionesEnMemoria {
        -Map~String, Mision~ almacenamiento
        +guardar(Mision mision) void
        +buscarPorId(String id) Optional~Mision~
        +obtenerTodas() List~Mision~
        +existePorId(String id) boolean
    }

    class ValidadorMision {
        -int BATERIA_MINIMA_DESPEGUE
        +validar(Mision mision) void
    }

    class NotificadorOperador {
        +notificarMisionRegistrada(Mision mision) void
        +notificarAdvertencia(String mensaje) void
        +notificarFallo(String mensaje) void
    }

    class RegistradorMisiones {
        -RepositorioMisiones repositorio
        -ValidadorMision validador
        -NotificadorOperador notificador
        +RegistradorMisiones(RepositorioMisiones, ValidadorMision, NotificadorOperador)
        +registrarMision(Mision mision) void
        +consultarMisionPorId(String id) Optional~Mision~
        +consultarTodasLasMisiones() List~Mision~
    }

    RepositorioMisiones <|.. RepositorioMisionesEnMemoria : implementa
    RegistradorMisiones --> RepositorioMisiones : depende de abstracción (DIP)
    RegistradorMisiones --> ValidadorMision : delega reglas (SRP)
    RegistradorMisiones --> NotificadorOperador : delega avisos (SRP)
```

---

## 💻 Compilación y Ejecución Manual

Desde la raíz del repositorio:

```powershell
javac -d bin Piplup/src/aquaport/model/*.java Piplup/src/aquaport/repository/*.java Piplup/src/aquaport/service/*.java Piplup/src/aquaport/*.java
java -cp bin aquaport.Main
```
