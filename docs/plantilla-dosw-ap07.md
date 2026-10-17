# 📑 Especificación Formal de Requisito Funcional — Plantilla DOSW (v2)

---

## 📌 Identificación del Requisito

| Campo | Especificación |
|---|---|
| **Código** | `AP-07` |
| **Nombre** | `Asignar automáticamente drone a misión hídrica` |
| **Actor** | `Sistema AquaPort (Fleet Control Station)` / `Solicitante` / `Operador Hídrico` |
| **Precondiciones** | 1. El sistema AquaPort se encuentra en ejecución activa con la flota de 15 drones sincronizada en memoria/repositorio.<br>2. La *API de Condiciones Hídricas* se encuentra accesible y respondiendo telemetría ambiental para las zonas del campus.<br>3. Los observadores (`CentroControlObserver`, `TecnicoMantenimientoObserver`) se encuentran suscritos al bus de eventos.<br>4. Se ha configurado una estrategia de selección activa (`MayorBateriaStrategy`, `ZonaCercanaStrategy` o `PrioridadCriticaStrategy`). |

---

## 📥 Estructura de Datos de Entrada y Salida

| Nombre | Tipo de campo | Reglas | Oblig. |
|---|---|---|---|
| `carga` | Estructura / Objeto | Encapsula los metadatos de la carga científica transportada | **Sí** |
| `carga.peso` | Integer | Entre 1 y 1500 gramos (0.001 kg a 1.500 kg) | **Sí** |
| `carga.tipo` | `Enum(MUESTRA_AGUA, SENSOR, PAQUETE_LIGERO, EQUIPO_MEDICION)` | Catálogo de tipos de carga admitidos en el campus | **Sí** |
| `carga.prioridad` | `Enum(CRITICA, ALTA, NORMAL, BAJA)` | Determina la urgencia y activa políticas de resiliencia reforzada | **Sí** |
| `zonaDestino` | `Enum(EMBALSE_NORTE, CANAL_CENTRAL, LAGUNA_SUR, RIBERA_ESTE, LAB_HIDRICO)` | Zona hídrica del campus hacia donde se dirige el drone | **Sí** |
| `droneAsignado` | Objeto `DroneAcuatico` | Calculado por el sistema según la estrategia activa | **No (salida)** |
| `droneAsignado.id` | String | Formato `AR-XX` (o prefijo especializado `DS-XX`, `DM-XX`, `DB-XX`) | **No (salida)** |
| `droneAsignado.tipo` | String | `SUPERFICIAL` / `SEMISUMERGIDO` / `BUCEADOR` | **No (salida)** |

---

## 🔄 Flujo Básico de Eventos (7 Pasos)

1. **Paso 1 (Recibir Solicitud):**  
   El sistema recibe la solicitud de transporte/muestreo con los atributos obligatorios: `zonaDestino`, `carga.tipo`, `carga.peso` y `carga.prioridad`.
2. **Paso 2 (Consultar Condiciones Hídricas):**  
   El sistema realiza una petición síncrona a la *API de Condiciones Hídricas* para obtener los parámetros en tiempo real de la zona de operación: nivel de agitación (`CALMA`, `AGITADA`, `PROFUNDA`), profundidad en metros y temperatura.
3. **Paso 3 (Filtrar Drones Aptos para la Zona):**  
   El motor filtra el pool de 15 drones descartando unidades fuera de servicio (`disponible == false` o estado $\neq$ `DISPONIBLE`), unidades en zonas incompatibles y modelos cuyo casco no soporte las condiciones hidrológicas (según **RN-03**).
4. **Paso 4 (Aplicar Estrategia Activa):**  
   El asignador invoca la heurística de la estrategia configurada (`EstrategiaSeleccionDrone`):
   * Si la prioridad es `CRITICA`: aplica `PrioridadCriticaStrategy` (umbral batería $\ge 50\%$ y preferencia de máxima robustez estructural).
   * Si la prioridad es `NORMAL`/`BAJA`: aplica `MayorBateriaStrategy` (maximiza reserva energética) o `ZonaCercanaStrategy` (minimiza traslado en vacío).
5. **Paso 5 (Validar Invariantes de Seguridad):**  
   El sistema verifica formalmente las restricciones duras sobre el candidato óptimo preseleccionado:
   * Nivel de batería $\ge 35\%$ (según **RN-01**).
   * Capacidad de carga del tipo de drone suficiente para `carga.peso` (según **RN-02**).
   * Tipo de drone físicamente compatible con la agitación y profundidad (según **RN-03**).
6. **Paso 6 (Asignar y Construir Misión):**  
   El sistema reserva el drone cambiando su estado a `EN_MISION`, genera el identificador único de la misión y construye la instancia inmutable `Mision` en estado `PENDIENTE`.
7. **Paso 7 (Notificar Observadores y Confirmar):**  
   El sistema notifica al Centro de Control el despacho exitoso, genera el reporte telemétrico para el Operador Hídrico y retorna el resultado al Solicitante con los datos de salida (`droneAsignado.id`, `droneAsignado.tipo`, tiempo estimado y ruta autorizada).

---

## 🔀 Flujos Alternos

### Flujo Alterno A: Sin drones disponibles
* **Condición de activación:** En el **Paso 3**, tras consultar el estado de la flota, ningún drone en la zona cuenta con estado `DISPONIBLE` ni supera el umbral operativo del $35\%$ de batería.
* **A1 (Sistema):** El motor interrumpe la asignación y registra la solicitud en cola de espera.
* **A2 (Sistema):** Se emite una notificación de advertencia al operador y solicitante:  
  *`"Asignación suspendida: No hay drones disponibles en la zona con batería >= 35%. Misión encolada hasta liberación de flota."`*
* **A3 (Sistema):** El sistema mantiene la flota intacta sin alterar estados y retorna un `Optional.empty()` o código de espera.

---

### Flujo Alterno B: Condiciones hídricas adversas
* **Condición de activación:** En el **Paso 2**, la *API de Condiciones Hídricas* reporta alertas climáticas severas (ej. oleaje extremo superior al límite de navegación, tormenta eléctrica o nivel de agua anómalo en el canal).
* **B1 (Sistema):** El asignador determina que el nivel de riesgo excede la tolerancia estructural de todos los modelos de la flota (incluso semisumergidos).
* **B2 (Sistema):** Se aborta la asignación y se dispara una alerta de severidad `ADVERTENCIA` al `CentroControlObserver`.
* **B3 (Sistema):** El sistema notifica al solicitante:  
  *`"Asignación denegada: Condiciones hídricas adversas en '[zonaDestino]'. Navegación suspendida por protocolo de seguridad."`*

---

### Flujo Alterno C: Carga supera capacidad del tipo de drone
* **Condición de activación:** En el **Paso 5**, se requiere una toma de muestra profunda en `CondicionAgua.PROFUNDA`, pero el peso solicitado (`carga.peso`) es mayor a 300 gramos (ej. 450 g).
* **C1 (Sistema):** El sistema detecta que el único tipo con capacidad de inmersión profunda (`DroneBuceador`) tiene una capacidad máxima estricta de 300 g (**RN-02**).
* **C2 (Sistema):** El sistema rechaza la asignación lanzando `IllegalStateException`.
* **C3 (Sistema):** Se notifica al solicitante:  
  *`"Error de carga: El DroneBuceador solo admite hasta 300g. La carga solicitada de [X]g excede el límite estructural de inmersión."`*
* **C4 (Sistema/Actor):** Se sugiere fraccionar la carga o reconfigurar la instrumentación de los sensores.

---

## ⚖️ Reglas de Negocio (RN)

* **RN-01 (Umbral mínimo de batería operativa):**  
  Un drone solo puede ser considerado y asignado para una misión si su nivel de batería remanente es mayor o igual al **$35\%$** al momento del cálculo. Si la misión posee prioridad `CRITICA`, el umbral mínimo de seguridad asciende obligatoriamente al **$50\%$**.
* **RN-02 (Límites estructurales de carga por tipo de drone):**  
  Cada tipo de drone posee un techo inamovible de carga útil:
  * 🤿 `DroneBuceador`: Carga máxima **$300\text{ g}$** ($0.30\text{ kg}$). Bajo ninguna circunstancia se asigna para pesos $> 300\text{ g}$.
  * 🚤 `DroneSuperficial`: Carga máxima **$500\text{ g}$** ($0.50\text{ kg}$).
  * 🚢 `DroneSemisumergido`: Carga máxima **$1500\text{ g}$** ($1.50\text{ kg}$).
* **RN-03 (Compatibilidad estricta de casco y entorno hídrico):**  
  * Los drones `SUPERFICIAL` solo pueden operar en aguas de condición `CALMA`/`TRANQUILA`. Tienen prohibido el despacho en canales agitados o misiones subacuáticas.
  * Los drones `SEMISUMERGIDO` son los únicos autorizados para condición `AGITADA` (oleaje, corrientes y lluvia) y pueden apoyar misiones en aguas calmas.
  * Los drones `BUCEADOR` son de uso exclusivo para condición `PROFUNDA` y toma de sedimentos sumergidos.
