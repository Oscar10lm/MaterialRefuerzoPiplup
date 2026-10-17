# 📋 Especificación de Requisitos, MoSCoW y Análisis de Tensiones — AquaPort Enterprise (Empoleon v3)

Este documento define la especificación formal de los **Requisitos Funcionales (RF)**, **Requisitos No Funcionales (RNF)** con métricas cuantitativas de producción, la priorización bajo metodología **MoSCoW**, y el análisis y resolución técnica de las **tensiones arquitecturales** para la escala Enterprise de **AquaPort v3 (Empoleon)** (60 drones, 4 zonas hídricas y rutas multi-etapa con waypoints).

---

## 🎯 1. Requisitos Funcionales (RF) — Nivel Enterprise

Formato estricto: **Actor + Acción + Resultado Observable + Criterios de Aceptación**.

### RF-01: Planificación y Ejecución de Rutas Multi-Etapa con Waypoints y Traspasos
* **Actor:** Operador de Misión / Sistema AquaPort Core Service.
* **Acción:** El operador ingresa una solicitud de misión especificando el punto de origen, una secuencia ordenada de *waypoints* intermedios (boyas de monitoreo limnológico, esclusas de transferencia) y el destino final (Laboratorio Hídrico Central).
* **Resultado Observable:** El sistema descompone la travesía en tramos consecutivos, consulta la flota activa de 60 drones, aplica la compatibilidad física por zona y asigna un drone especializado por cada tramo (Superficial para Canales, Buceador para Embalse Norte, Semisumergido para Laguna Sur), coordinando la ventana de traspaso en cada waypoint.
* **Criterios de Aceptación:**
  1. La ruta multi-etapa debe contemplar como mínimo 1 y hasta 10 waypoints intermedios.
  2. Cada tramo debe quedar asignado a un drone físicamente apto para el cuerpo de agua de dicho segmento.
  3. Los drones seleccionados deben superar satisfactoriamente la cadena de validación previa al inicio del tramo.
* **Categoría MoSCoW:** **Must Have**
  * *Justificación:* Es la capacidad nuclear que habilita la navegación inter-embalse en el campus de la ECI.

---

### RF-02: Reasignación Automática Inmediata ante Fallo de Drone en Waypoint
* **Actor:** Módulo de Telemetría / AquaPort Core Service.
* **Acción:** Detecta en tiempo real que un drone en tránsito hacia un waypoint o durante la maniobra de traspaso entra en estado `FALLO`, pierde propulsión o desciende su batería por debajo del umbral crítico ($< 35\%$).
* **Resultado Observable:** El sistema cancela la asignación del drone averiado en dicho tramo, localiza automáticamente la unidad de relevo disponible más cercana en la misma zona hídrica, actualiza el plan de vuelo y emite una alerta crítica a la estación de control sin abortar la misión global.
* **Criterios de Aceptación:**
  1. La reasignación debe seleccionar únicamente unidades en estado `DISPONIBLE` con batería $\ge 50\%$.
  2. Si no hay drones disponibles en la zona inmediata, el sistema debe ordenar al drone del tramo anterior mantener la carga en el waypoint y alertar a la cuadrilla técnica.
  3. El historial del incidente debe quedar registrado en la bitácora inmutable de la misión.
* **Categoría MoSCoW:** **Must Have**
  * *Justificación:* Evita la pérdida de muestras irreemplazables y garantiza la continuidad operativa de la red multi-embalse.

---

### RF-03: Trazabilidad Digital Inmutable de la Cadena de Custodia de Muestras
* **Actor:** Drone Acuático (vía sobre cifrado) / Sistema AquaPort / Sistema LIMS Central.
* **Acción:** En cada toma de muestra y en cada evento de traspaso físico de carga entre drones en un waypoint, el sistema genera un acta digital de custodia que incluye identificador de contenedor, ID de drone custodio, coordenadas GPS exactas, timestamp UTC, temperatura de la muestra y firma criptográfica SHA-256.
* **Resultado Observable:** Al arribar la muestra al Laboratorio Hídrico Central, el sistema exporta el expediente completo de custodia al sistema LIMS externo, acreditando que el espécimen no sufrió adulteración ni pérdida de trazabilidad térmica durante el transporte inter-embalse.
* **Criterios de Aceptación:**
  1. Cualquier intento de alteración en el hash de custodia invalida la recepción en el LIMS.
  2. El registro de custodia debe generarse y enlazarse en tiempo de ejecución sin intervención humana.
* **Categoría MoSCoW:** **Must Have**
  * *Justificación:* Exigencia regulatoria y científica internacional para la validez de los estudios limnológicos de la ECI.

---

### RF-04: Coordinación Interzonal y Gestión de Ventanas de Arribo en Esclusas
* **Actor:** AquaPort Core Service / Estaciones Zonales ECI.
* **Acción:** Coordina el tráfico acuático sincronizando las 4 zonas hídricas (Embalse de Investigación, Red de Canales, Laguna de Reserva y Laboratorio Central), calculando las velocidades de crucero y las ventanas de arribo (*ETA*) en las esclusas de transferencia.
* **Resultado Observable:** El sistema previene colisiones en pasos angostos de canales, autoriza el ingreso secuencial de un único drone a la esclusa a la vez y reduce los tiempos muertos de espera en boyas de intercambio.
* **Criterios de Aceptación:**
  1. Ningún drone puede ingresar a una esclusa si la esclusa se encuentra reservada por otra unidad en sentido contrario.
  2. Los desvíos temporales superiores a $\pm 90\text{ segundos}$ respecto a la ventana de arribo recalibran automáticamente la velocidad del tramo subsiguiente.
* **Categoría MoSCoW:** **Should Have**
  * *Justificación:* Maximiza la fluidez y seguridad en puntos de estrangulamiento hidráulico del campus.

---

### RF-05: Reportes Analíticos de Carga de Trabajo y Eficiencia Zonal en Tiempo Real
* **Actor:** Científico Limnólogo / Coordinador de Operaciones.
* **Acción:** Solicita la consolidación analítica del estado de la red multi-embalse en cualquier momento del ciclo operativo.
* **Resultado Observable:** El sistema genera en consola y en el dashboard web un reporte computado en una sola pasada $O(N)$ mediante `BateriaPromedioCollector`, detallando:
  * Número de misiones activas (`EN_TRANSITO`) por zona hídrica.
  * Tasa de eficiencia zonal (misiones completadas exitosamente vs fallidas/canceladas).
  * Promedio exacto de batería remanente de los 60 drones clasificado por zona.
  * Drone con mejor desempeño histórico en misiones de alta prioridad.
* **Criterios de Aceptación:**
  1. El cómputo analítico debe completarse en memoria sin bloquear las operaciones de despacho de nuevas misiones.
  2. Las métricas deben actualizarse de manera reactiva ante cada cambio de estado de misión o drone.
* **Categoría MoSCoW:** **Should Have**
  * *Justificación:* Permite la toma de decisiones basada en datos para mantenimiento preventivo y redistribución de flota.

---

## ⚙️ 2. Requisitos No Funcionales (RNF) con Métricas de Producción

> [!NOTE]
> Todos los RNF definen **métrica cuantitativa**, **condiciones de carga de producción**, y **mecanismo de verificación auditable**.

### RNF-01: SLA de Disponibilidad del Sistema (Alta Disponibilidad en Producción)
* **Descripción:** La plataforma AquaPort Enterprise debe mantenerse operativa de forma continua durante la ventana de muestreo ambiental ($24/7$).
* **Métrica Concreta:** Disponibilidad $\ge 99.9\%$ (SLA de tres nueves), equivalente a un tiempo de indisponibilidad no planificada acumulada menor a **43.8 minutos por mes**.
* **Condición de Carga:** Operación continua de 60 drones y hasta 50 misiones multi-etapa simultáneas.
* **Método de Verificación:** Monitorización sintética continua mediante Prometheus y Blackbox Exporter con alertas síncronas de caída de servicio.
* **Categoría MoSCoW:** **Must Have**

---

### RNF-02: Tiempo Máximo de Reasignación Automática ante Fallo
* **Descripción:** La latencia del motor de reasignación autónoma ante la pérdida de una unidad en un tramo o waypoint debe ser inmediata para evitar la deriva de la carga.
* **Métrica Concreta:** Tiempo total $\le 3.0\text{ segundos}$ (percentil 99, P99) medido desde el momento en que el `Módulo de Telemetría` registra el estado de fallo hasta la reserva confirmada y despacho del drone de relevo.
* **Condición de Carga:** Flota completa con 60 drones en memoria y concurrencia de eventos en 4 zonas simultáneas.
* **Método de Verificación:** Suite automatizada de pruebas de integración en JUnit 5 / JMeter inyectando fallos síncronos y cronometrando con `System.nanoTime()`.
* **Categoría MoSCoW:** **Must Have**

---

### RNF-03: Throughput y Concurrencia de Misiones Simultáneas
* **Descripción:** El núcleo de orquestación debe admitir la planificación, seguimiento y control concurrente de múltiples misiones multi-etapa sin degradación de rendimiento.
* **Métrica Concreta:** Capacidad de sostener un Throughput de al menos **$\ge 50$ misiones multi-etapa concurrentes** (con hasta 60 drones transmitiendo telemetría a $1\text{ Hz}$), manteniendo la latencia de respuesta de consultas en la API REST por debajo de **$500\text{ ms}$ (P95)**.
* **Condición de Carga:** 50 rutas multi-etapa activas, 60 conexiones persistentes MQTT y 10 operadores concurrentes en el dashboard web.
* **Método de Verificación:** Prueba de carga de rendimiento con Gatling / Apache JMeter simulando 50 misiones simultáneas durante 30 minutos de tráfico sostenido.
* **Categoría MoSCoW:** **Must Have**

---

### RNF-04: Seguridad Criptográfica y Arquitectura Cero Confianza (Zero Trust)
* **Descripción:** Toda la información que transite por redes inalámbricas y radio-enlaces de los drones debe ser inaccesible e inmodificable por terceros.
* **Métrica Concreta:** El $100\%$ de los paquetes de telemetría y órdenes de navegación deben viajar cifrados con **AES-256-GCM** sobre canales **TLS 1.3** (`MQTTS`), con un sobrecosto (*overhead*) de procesamiento de cifrado/descifrado $\le 5\text{ ms}$ por paquete.
* **Condición de Carga:** 60 transmisiones de telemetría por segundo procesadas por el `Módulo de Telemetría`.
* **Método de Verificación:** Inspección profunda de paquetes con Wireshark verificando que no existan datos en texto plano y pruebas de rendimiento del decorador `DroneConCifradoTest`.
* **Categoría MoSCoW:** **Should Have**

---

### RNF-05: Eficiencia Algorítmica en el Cálculo Analítico Zonal ($O(N)$ en Sola Pasada)
* **Descripción:** Los agregadores de telemetría, batería y eficiencia zonal no deben iterar múltiples veces sobre colecciones masivas de drones o histórico de misiones.
* **Métrica Concreta:** Complejidad temporal estrictamente **$O(N)$** mediante colector personalizado Java (`BateriaPromedioCollector`), ejecutándose en un tiempo $\le 15\text{ ms}$ para flotas de hasta **500 drones simulados** en memoria.
* **Condición de Carga:** Consulta reactiva ejecutada mientras el sistema procesa misiones activas concurrentemente.
* **Método de Verificación:** Microbenchmarks automatizados con JMH (Java Microbenchmark Harness) y suite unitaria en `BateriaPromedioCollectorTest.java`.
* **Categoría MoSCoW:** **Should Have**

---

## 📊 3. Matriz Resumen de Priorización MoSCoW

| ID | Tipo | Descripción Sintética | Prioridad MoSCoW | Justificación en una sola oración |
|---|---|---|---|---|
| **RF-01** | Funcional | Rutas multi-etapa con waypoints y traspasos especializados | **Must Have** | Habilita el desplazamiento inter-embalse según el tipo de drone y características del cuerpo de agua. |
| **RF-02** | Funcional | Reasignación automática inmediata ante fallo en waypoint | **Must Have** | Garantiza la continuidad de la misión y salvamento de muestras sin requerir intervención humana. |
| **RF-03** | Funcional | Trazabilidad inmutable de cadena de custodia de muestras | **Must Have** | Requisito indispensable para la validez legal y científica de los análisis limnológicos en el LIMS. |
| **RF-04** | Funcional | Coordinación interzonal y gestión de esclusas | **Should Have** | Optimiza el flujo y previene colisiones en puntos de paso restringido entre embalses. |
| **RF-05** | Funcional | Reportes analíticos de carga y eficiencia zonal en tiempo real | **Should Have** | Brinda visibilidad operativa para el mantenimiento preventivo y balanceo de carga de la flota. |
| **RNF-01** | No Funcional | SLA de disponibilidad $\ge 99.9\%$ ($< 43.8\text{ min/mes}$) | **Must Have** | La operación de monitoreo limnológico continuo no tolera caídas del sistema de control. |
| **RNF-02** | No Funcional | Tiempo de reasignación ante fallo $\le 3.0\text{ s}$ (P99) | **Must Have** | Un retraso mayor causaría la deriva física de la muestra en corrientes de agua abiertas. |
| **RNF-03** | No Funcional | Throughput $\ge 50$ misiones simultáneas y latencia $< 500\text{ ms}$ | **Must Have** | Soporta la escala de producción de 60 drones operando a capacidad máxima en la ECI. |
| **RNF-04** | No Funcional | Encriptación AES-256 / TLS 1.3 con latencia $\le 5\text{ ms}$ | **Should Have** | Protege los datos científicos e integridad de comandos contra sabotaje o interceptación. |
| **RNF-05** | No Funcional | Cálculo analítico $O(N)$ en una sola pasada en $\le 15\text{ ms}$ | **Should Have** | Evita la degradación del hilo principal del servidor ante consultas analíticas frecuentes. |

---

## ⚡ 4. Análisis y Resolución de Tensiones entre Requisitos (Trade-offs Arquitecturales)

En un sistema distribuido y autónomo de nivel Enterprise, los requisitos funcionales y no funcionales entran en conflicto directo debido a limitaciones de concurrencia, latencia, consistencia y recursos físicos. A continuación se documentan las cuatro principales tensiones identificadas y su resolución técnica.

```
                  ┌─────────────────────────────────────────────────────────┐
                  │                 MATRIZ DE TENSIONES                     │
                  └─────────────────────────────────────────────────────────┘
                                               │
       ┌───────────────────────┬───────────────┴───────────────┬───────────────────────┐
       ▼                       ▼                               ▼                       ▼
 ┌───────────┐           ┌───────────┐                   ┌───────────┐           ┌───────────┐
 │ TENSIÓN 1 │           │ TENSIÓN 2 │                   │ TENSIÓN 3 │           │ TENSIÓN 4 │
 │ Rutas vs  │           │ Custodia  │                   │ Coordinac.│           │ Métricas  │
 │ Reasig.   │           │ vs Tiempo │                   │ vs Resil. │           │ vs Rendim.│
 └───────────┘           └───────────┘                   └───────────┘           └───────────┘
```

---

### Tensión 1: Planificación de Rutas Multi-Etapa (RF-01) vs. Reasignación Automática en Tiempo Real (RF-02 / RNF-02)

* **Naturaleza del Conflicto:**  
  La planificación de rutas multi-etapa (**RF-01**) requiere pre-reservar drones para tramos que se ejecutarán en el futuro (ej. el tramo 3 iniciará 20 minutos después del tramo 1). Si un drone falla repentinamente en el tramo 1 (**RF-02**), el tiempo de arribo al waypoint 2 se retrasa, lo cual invalida la reserva del drone del tramo subsiguiente o genera condiciones de carrera (*race conditions*) si dicho drone es reclamado por otra misión emergente en menos de **3 segundos** (**RNF-02**).
* **Impacto en el Sistema:**  
  Riesgo de bloqueo mutuo (*deadlock*) en la reserva de flota o sobreasignación (*overbooking*) de drones en zonas con alta demanda como los Canales Centrales.
* **Resolución Arquitectural (Two-Phase Dynamic Handover Lock):**  
  Se implementa un modelo de reserva en dos fases:
  1. *Reserva Blanda (Soft Reservation):* Al planificar la ruta multi-etapa, los drones de tramos futuros se marcan en estado `RESERVADO_FUTURO`, lo que permite recalcular su asignación si la ventana temporal de arribo cambia en más de 2 minutos.
  2. *Reserva Dura (Hard Lock):* La reserva se vuelve inmutable únicamente cuando el drone del tramo precedente ingresa al último tercio de su ruta hacia el waypoint de traspaso. Ante un fallo en waypoint, el algoritmo de reasignación prioriza unidades en estado `DISPONIBLE` y, solo si no existen, reubica unidades de `RESERVADO_FUTURO` de misiones de menor prioridad aplicando el patrón *Strategy*.

---

### Tensión 2: Trazabilidad Inmutable de Cadena de Custodia (RF-03 / RNF-04) vs. Latencia de Reasignación y Throughput (RNF-02 / RNF-03)

* **Naturaleza del Conflicto:**  
  Garantizar la trazabilidad criptográfica inmutable (**RF-03**) con cifrado **AES-256** y firma digital **SHA-256** (**RNF-04**) en cada punto de relevo requiere escrituras transaccionales síncronas con garantías *ACID* en base de datos. Sin embargo, ante un fallo imprevisto, la reasignación debe resolverse en menos de **$3.0\text{ segundos}$** (**RNF-02**) mientras el sistema soporta **$\ge 50$ misiones concurrentes** (**RNF-03**), lo que genera contención en los bloqueos de base de datos relacional.
* **Impacto en el Sistema:**  
  Picos de latencia en la base de datos PostgreSQL y posible degradación del tiempo de respuesta del orquestador durante contingencias simultáneas.
* **Resolución Arquitectural (Write-Ahead In-Memory Audit + Batch Persistence):**  
  Se adopta el patrón *Decorator* apilable en memoria (`DroneConCifrado(new DroneConMonitoreo(drone))`) junto con una estrategia de persistencia en dos niveles:
  1. El sellado criptográfico del traspaso y la bitácora de telemetría se generan síncronamente en memoria (< 2 ms) utilizando buffers circulares protegidos contra concurrencia.
  2. La persistencia física en PostgreSQL/TimescaleDB se realiza de forma asíncrona mediante un *Write-Ahead Log (WAL)* en lotes (*batch insert*) cada 500 ms, liberando inmediatamente el hilo de reasignación del Core para cumplir el umbral de $\le 3.0\text{ s}$.

---

### Tensión 3: Coordinación Interzonal Centralizada (RF-04) vs. SLA de Disponibilidad y Resiliencia Local (RNF-01)

* **Naturaleza del Conflicto:**  
  La coordinación centralizada de esclusas y ventanas de tráfico (**RF-04**) asume un conocimiento global del estado de las 4 zonas en el `AquaPort Core Service`. No obstante, en un entorno de campo acuático abierto, los enlaces de radiofrecuencia o redes celulares en el Embalse Norte pueden experimentar caídas temporales de conectividad. Si la esclusa requiere aprobación obligatoria del servidor central para operar, un fallo de red violaría el **SLA de disponibilidad del 99.9%** (**RNF-01**), paralizando los drones en el agua.
* **Impacto en el Sistema:**  
  Un punto único de fallo (*SPOF*) en la capa de coordinación que puede detener la logística física en caso de intermitencia de red.
* **Resolución Arquitectural (Autonomous Local Zone Gateway Fallback):**  
  Se adopta un modelo federado:
  1. Durante operación normal, el Core Service central orquesta las esclusas mediante WebSockets/REST.
  2. Si un drone detecta pérdida de comunicación con el Core superior a 5 segundos al aproximarse a una esclusa, conmuta autónomamente a un protocolo descentralizado *Peer-to-Peer* (P2P) de corto alcance con la boya inteligente de la esclusa vía radioenlace local (LoRa/Zigbee), aplicando la regla de paso por orden de llegada con sensores ópticos de proximidad y reportando el evento retroactivamente al recuperar la conexión.

---

### Tensión 4: Reportes Analíticos en Tiempo Real (RF-05) vs. Throughput de Misiones Activas (RNF-03 / RNF-05)

* **Naturaleza del Conflicto:**  
  Calcular métricas analíticas de eficiencia zonal, conteo de estados de misiones y promedios de batería (**RF-05**) sobre una flota de 60 drones y 50 misiones simultáneas (**RNF-03**) puede saturar la CPU si se emplean operaciones tradicionales de filtrado múltiple en colecciones (`stream().filter().count()` repetido por cada zona).
* **Impacto en el Sistema:**  
  Degradación de latencia en la API de despacho de misiones si las consultas analíticas de los operadores compiten por los mismos ciclos de procesamiento.
* **Resolución Arquitectural (Custom O(N) Collector Pattern):**  
  Se diseñó e implementó [`BateriaPromedioCollector`](file:///Empoleon/src/aquaport/service/BateriaPromedioCollector.java), un colector personalizado de Java Streams que calcula la suma, conteo y promedio de batería por zona **en una única pasada lineal $O(N)$** sin estructuras intermedias ni pasadas redundantes.
  * Adicionalmente, el cálculo de eficiencia zonal en [`ConsultorFlotaEnterprise`](file:///Empoleon/src/aquaport/service/ConsultorFlotaEnterprise.java) utiliza un mapa clasificado en un solo `groupingBy`, garantizando tiempos de respuesta $< 15\text{ ms}$ y protegiendo el Throughput de misiones concurrentes.
