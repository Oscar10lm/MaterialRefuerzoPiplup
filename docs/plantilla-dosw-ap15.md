# 📑 Especificación Formal de Requisito Funcional — Plantilla DOSW (Nivel Enterprise)

---

## 📌 1. Identificación del Requisito

| Campo | Especificación |
|---|---|
| **Código** | `AP-15` |
| **Nombre** | `Ejecutar misión multi-etapa con waypoints y coordinación interzonal` |
| **Actor Principal** | `Operador de Misión` / `Sistema AquaPort Core Service` |
| **Actores Secundarios** | `Drone Acuático Custodio (IoT)` / `Sistema LIMS Central` / `Módulo de Telemetría` |
| **Precondiciones** | 1. La flota Enterprise de 60 drones se encuentra sincronizada y catalogada en las 4 zonas hídricas.<br>2. El `AdaptadorAPIHidrica` se encuentra activo y proveyendo condiciones ambientales en tiempo real.<br>3. El `Módulo de Telemetría` está operando con ingesta continua y desencriptación sobre AES-256.<br>4. La infraestructura GIS ECI tiene sincronizadas las boyas fijas de muestreo y las esclusas de transferencia.<br>5. La cadena de validación (*Chain of Responsibility*) se encuentra inicializada y enlazada. |

---

## 📥 2. Estructura de Datos de Entrada y Salida

| Nombre de Campo | Tipo de Campo | Reglas y Restricciones | Obligatorio |
|---|---|---|---|
| `mision.id` | String | Identificador alfanumérico único. Formato `MIS-XXX` (ej. `MIS-301`). | **Sí** (Salida) |
| `origen` | `Enum / String` | Debe pertenecer a una de las 4 zonas canónicas: `EMBALSE_INVESTIGACION`, `RED_CANALES`, `LAGUNA_RESERVA`, `LABORATORIO_CENTRAL`. | **Sí** |
| `waypoints` | `List<Waypoint>` | Lista ordenada de $1$ a $10$ puntos de control. Cada waypoint contiene: `id`, `nombre`, `zona`, `latitud`, `longitud`, `orden`. | **Sí** |
| `destino` | `Enum / String` | Zona de entrega final (habitualmente `LABORATORIO_CENTRAL` para análisis limnológico). | **Sí** |
| `carga.tipo` | `Enum` | `MUESTRA_AGUA`, `SENSOR`, `PAQUETE_LIGERO`, `EQUIPO_MEDICION`. | **Sí** |
| `carga.pesoKg` | Double | Rango: $0.001\text{ kg}$ a $1.500\text{ kg}$ según el tipo de drone asignado. | **Sí** |
| `prioridad` | `Enum` | `CRITICA`, `ALTA`, `NORMAL`, `BAJA`. Activa umbrales reforzados de batería ($\ge 50\%$). | **Sí** |
| `tramosRuta` | `List<TramoRuta>` | Lista de tramos calculados. Cada tramo especifica origen, destino, distancia en km, drone asignado y estado. | **No** (Salida) |
| `actaCustodia` | Objeto `ActaCustodia` | Registro inmutable de custodia: hash SHA-256 encadenado, firmas de drones custodios, temperaturas y marcas temporales. | **No** (Salida) |

---

## 🔄 3. Flujo Básico de Eventos (9 Pasos)

1. **Paso 1 (Recepción y Parametrización de la Solicitud):**  
   El `Operador de Misión` define a través del panel de control web la travesía requerida ingresando: zona de origen, la secuencia ordenada de waypoints intermedios (boyas de muestreo y esclusas), el destino final, el tipo y peso de carga y el nivel de prioridad.
2. **Paso 2 (Descomposición de Travesía en Tramos Consecutivos):**  
   El `AquaPort Core Service` descompone la ruta en una lista finita de $N-1$ tramos contiguos ($P_0 \to P_1, P_1 \to P_2, \dots, P_{N-1} \to P_N$), identificando la zona hídrica predominante y la distancia geodésica de cada segmento.
3. **Paso 3 (Consulta Climática e Hidrológica Zonal):**  
   Para cada tramo de la ruta, el Core invoca al `AdaptadorAPIHidrica` (`consultarCondiciones(zona)`), el cual traduce de forma transparente los datos meteorológicos externos en inglés (`waterLevel`, `turbidity`, `depth`, `temperature`) al modelo canónico del dominio (`CondicionHidrica`).
4. **Paso 4 (Selección de Drones Especializados por Tramo):**  
   El asignador filtra la flota de 60 drones por zona y tipo de casco:
   * Tramos en Embalse Norte con requerimiento de profundidad $\to$ `DroneBuceador`.
   * Tramos en Canales del Campus con aguas calmas $\to$ `DroneSuperficial`.
   * Tramos en Laguna de Reserva con fuerte oleaje $\to$ `DroneSemisumergido`.
   Se preselecciona la mejor unidad disponible aplicando la estrategia heurística activa.
5. **Paso 5 (Validación por Cadena de Responsabilidad):**  
   Cada asignación propuesta es evaluada secuencialmente por la cadena de validación:  
   $$\text{ValidadorBateria} \longrightarrow \text{ValidadorCapacidadCarga} \longrightarrow \text{ValidadorZonaActiva} \longrightarrow \text{ValidadorCondicionesHidricas}$$  
   Si todos los eslabones retornan `ResultadoValidacion.ok()`, el tramo se aprueba formalmente; de lo contrario, se descarta el candidato y se evalúa el siguiente.
6. **Paso 6 (Generación del Acta Criptográfica Inicial de Custodia):**  
   Al confirmarse los tramos y drones asignados, el sistema sella el acta digital de cadena de custodia inicial: genera el hash criptográfico SHA-256 base, vincula el identificador del contenedor isotérmico de muestra y reserva las unidades en estado `EN_TRANSITO`.
7. **Paso 7 (Despacho y Monitoreo Telemétrico en Tiempo Real):**  
   El primer drone inicia el recorrido del Tramo 1. El `Módulo de Telemetría` recibe a alta frecuencia las transmisiones cifradas (AES-256) mediante el decorador `DroneConCifrado(new DroneConMonitoreo(drone))`, auditando la posición satelital y telemetría de navegación en el mapa del centro de control.
8. **Paso 8 (Coordinación de Traspaso en Waypoint y Relevo Físico):**  
   Al arribar el drone al waypoint de transferencia (boya limnológica o esclusa):
   * El drone saliente deposita el contenedor de muestras en el receptáculo automatizado.
   * El drone del tramo entrante verifica el código criptográfico y toma posesión de la carga.
   * El sistema calcula el nuevo hash encadenado de custodia (*hash chaining*) con la temperatura registrada y timestamp UTC, habilitando la navegación del siguiente tramo.
9. **Paso 9 (Arribo al Laboratorio Central y Liquidación en LIMS):**  
   El último drone atraca en el muelle del Laboratorio Hídrico Central. El sistema valida el expediente íntegro de custodia, verifica que el hash final concuerde con todos los tramos previos, transiciona la misión a `COMPLETADA`, libera los drones a `DISPONIBLE` y transfiere el acta digital al sistema LIMS externo vía HTTPS REST / AMQP.

---

## 🔀 4. Flujos Alternos

### Flujo Alterno A: Fallo de Drone en Waypoint Intermedio (Reasignación Autónoma en Caliente)
* **Condición de Activación:** En el **Paso 7** u **8**, el drone asignado a un tramo sufre una avería crítica (pérdida de propulsión, batería $< 35\%$, o ausencia de telemetría $> 5\text{ segundos}$).
* **A1 (Detección y Aseguramiento):** El `Módulo de Telemetría` detecta la anomalía, marca al drone en estado `FALLO`, emite una alerta síncrona al Core Service y ordena a la unidad ejecutar protocolo de anclaje de emergencia en la boya más próxima.
* **A2 (Evaluación de Relevo):** El Core Service activa el algoritmo de reasignación automática en caliente en la misma zona hídrica. Filtra unidades en estado `DISPONIBLE` con batería $\ge 50\%$ y capacidad de carga adecuada.
* **A3 (Reasignación y Aprobación):** El sistema asigna el drone de relevo al tramo en menos de **$3.0\text{ segundos}$** (según **RNF-02**), recalcula la ventana de aproximación al waypoint y notifica a la estación de control con sonido de advertencia y confirmación visual.
* **A4 (Sellado de Custodia del Incidente):** Se anexa una entrada de contingencia en el acta de custodia inmutable con el motivo de relevo y el nuevo custodio asignado.
* **A5 (Continuación):** El drone de relevo recoge el contenedor en el waypoint y la misión retoma el **Paso 7** con normalidad.

---

### Flujo Alterno B: Condiciones Hídricas Adversas en Tramo Específico (Desvío Dinámico de Ruta)
* **Condición de Activación:** En el **Paso 3** o durante la navegación en el **Paso 7**, el `AdaptadorAPIHidrica` detecta un deterioro meteorológico repentino en un canal intermedio (oleaje $> 2.0\text{ m}$, turbidez anómala o corriente $> 1.5\text{ m/s}$ no navegable para el modelo asignado).
* **B1 (Detección de Incompatibilidad):** El eslabón `ValidadorCondicionesHidricas` rechaza la navegación del tramo por riesgo inminente de volcamiento o hundimiento.
* **B2 (Cálculo de Desvío Alternativo):** El planificador consulta al Sistema GIS ECI e identifica un canal de interconexión secundario que evade la zona de turbulencia agregando un waypoint de desvío temporal ($WP_{alt}$).
* **B3 (Verificación de Autonomía):** El sistema recalcula la distancia total del nuevo recorrido y valida que el drone mantenga batería suficiente para absorber el incremento de distancia ($\le 1.2\text{ km}$ adicionales).
* **B4 (Aprobación y Reenrutamiento):** Si la verificación es exitosa, se actualiza el plan de navegación en caliente y se continúa con el **Paso 7**; si la batería no alcanza, se conmuta al **Flujo Alterno A** para intercalar una unidad de relevo fresca.

---

### Flujo Alterno C: Zona de Destino Temporalmente Inactiva o Saturada
* **Condición de Activación:** Al aproximarse al último tramo (**Paso 8**), el Laboratorio Hídrico Central entra en estado no operativo (saturación de muelles de descarga, alerta biológica o mantenimiento correctivo de esclusa).
* **C1 (Notificación de Espera Segura):** El sistema de control zonal informa que el muelle central no puede recibir muestras durante una ventana estimada de $\Delta t$.
* **C2 (Maniobra de Espera en Boya Climatizada):** El sistema ordena al drone custodio del último tramo dirigirse a la *Boya de Transferencia Sur* (`WP-03`), activando el protocolo de espera pasiva en boya (refrigeración activa de contenedor de muestra y consumo energético mínimo).
* **C3 (Alerta al Operador):** El panel de control muestra la misión en estado `EN_ESPERA_DESTINO` con temporizador regresivo.
* **C4 (Reanudación o Cuarentena):** Si el muelle se habilita dentro de los 30 minutos, el drone retoma el **Paso 9**; si expira el tiempo máximo, se deriva la muestra a la estación secundaria de conservación en la Laguna de Reserva para asegurar la cadena de frío.

---

## 📜 5. Reglas de Negocio (RN)

### RN-01: Invariantes Energéticas de Despacho y Operación
* Ningún drone puede ser asignado a un tramo multi-etapa si su nivel de batería es inferior al **$35\%$** en condiciones normales, o inferior al **$50\%$** si la misión es de prioridad `CRITICA` o involucra zonas profundas de buceo.
* Si durante la travesía el nivel de batería desciende a $\le 20\%$, el drone entra automáticamente en estado `FALLO` disparando el protocolo de reasignación síncrona.

### RN-02: Compatibilidad Estructural por Cuerpo de Agua
* **Red de Canales:** Exclusiva para `DroneSuperficial` (carga útil $\le 500\text{ g}$). Prohibido en aguas con agitación moderada o alta.
* **Laguna de Reserva:** Requiere `DroneSemisumergido` (carga útil $\le 1500\text{ g}$). Diseñado para oleaje medio y corrientes de superficie.
* **Embalse de Investigación:** Requiere `DroneBuceador` (carga útil $\le 300\text{ g}$) para recolección de muestras subacuáticas a profundidad ($> 1\text{ m}$).

### RN-03: Cadena de Custodia Criptográfica Inmutable de Muestras
* Toda muestra biológica o química transportada debe poseer un expediente digital con estructura encadenada (*hash chain*):
  $$H_k = \text{SHA-256}(H_{k-1} \;\|\; \text{DroneID}_k \;\|\; \text{WaypointID}_k \;\|\; \text{Temp}^\circ\text{C} \;\|\; \text{TimestampUTC})$$
* La temperatura de la muestra debe mantenerse en el rango de $4^\circ\text{C} \pm 2^\circ\text{C}$. Una lectura fuera de rango marca el acta con bandera `ADVERTENCIA_TEMPERATURA` pero no detiene el transporte físico.
* Los registros de custodia se almacenan en tablas de sólo adición (*append-only*) en PostgreSQL con replicación en TimescaleDB.

### RN-04: Umbrales Decisionales de Reasignación Automática
* Se dispara reasignación autónoma inmediata ante cualquiera de las siguientes condiciones:
  1. Ausencia de paquete de telemetría durante más de **$5.0\text{ segundos}$**.
  2. Tasa de descarga anómala de batería ($> 2.5\times$ la tasa nominal).
  3. Desvío radial superior a **$15\text{ metros}$** respecto al corredor del tramo.
* El drone sustituto debe encontrarse en la misma zona hídrica del incidente, en estado `DISPONIBLE` y con batería $\ge 50\%$.
* El tiempo total de cómputo y notificación de la reasignación no debe superar los **$3.0\text{ segundos}$**.
