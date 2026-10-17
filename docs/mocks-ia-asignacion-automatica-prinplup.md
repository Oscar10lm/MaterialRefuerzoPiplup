# 🌊 AquaPort v2 (Prinplup) — Reto 11: Mocks y Flujo de Asignación Autónoma con IA

## 1. Visión General del Flujo de Usuario v2

En **Prinplup (AquaPort v2)**, la interacción del operador evoluciona radicalmente: **el sistema decide solo y el operador supervisa**. La interfaz de usuario refleja directamente esta transición mediante un flujo reactivo en 3 pantallas principales más una pantalla de contingencia crítica en tiempo real:

1. **Pantalla 1 — Panel de Flota (15 Drones):** Visión global agrupada por tipo con estado cromático de cada unidad.
2. **Pantalla 2 — Detalle de Misión & Recomendación IA:** Visualización de condiciones hídricas ambientales y justificación técnica algorítmica de la selección.
3. **Pantalla 3 — Confirmación de Despacho & Flota Actualizada:** Telemetría en vivo, ruta náutica trazada y sincronización de observadores.
4. **Pantalla 4 — Estado de Alerta (Misión CRÍTICA sin Drones):** Protocolo de emergencia cuando la flota no cumple los requisitos para una prioridad crítica.

---

## 2. Pantalla 1: Panel de Flota de 15 Drones Agrupados por Tipo

### 2.1. Imagen del Mockup Generado
![Panel de Flota de 15 Drones](/docs/images/mock-panel-flota-prinplup.jpg)

### 2.2. Prompt de Generación IA Utilizado
```text
PROMPT 1 — PANEL DE FLOTA (15 DRONES POR TIPO):
Modern dark-themed UI dashboard for AquaPort autonomous water drone fleet management. High-tech dashboard showing 15 aquatic drones grouped into three sections: Superficial Drones, Semi-submerged Drones, and Submarine Diver Drones. Each drone has a sleek card showing battery level (green, yellow, red), operational status badge (DISPONIBLE in green, EN_MISION in blue, RECARGANDO in yellow, MANTENIMIENTO in gray, FALLO in red, SUMERGIDO in dark blue), payload capacity, and current water campus zone. Beautiful glassmorphic UI, glowing blue and cyan neon accents, clean typography, professional naval tech interface.
```

### 2.3. Principios UX Aplicados
* **Ley de Hick ($T = b \cdot \log_2(n + 1)$):** Monitorear 15 drones individuales en una sola lista desordenada incrementa la carga cognitiva del operador. Agruparlos en 3 categorías visuales (`Superficial`, `Semisumergido`, `Buceador`) reduce drásticamente el tiempo de decisión al permitir filtrado selectivo según la zona.
* **Codificación Cromática Normalizada:**
  * `DISPONIBLE`: Verde brillante (#10B981) — Apto para asignación inmediata.
  * `EN_MISION`: Azul cian (#06B6D4) — Operando en ruta aprobada.
  * `RECARGANDO`: Amarillo ámbar (#F59E0B) — En base de carga rápida.
  * `MANTENIMIENTO`: Gris neutro (#6B7280) — En bahía técnica.
  * `FALLO`: Rojo carmesí (#EF4444) — Contingencia crítica notificada a observadores.
  * `SUMERGIDO`: Azul noche profundo (#1E3A8A) — Operando en profundidad acuática.

---

## 3. Pantalla 2: Detalle de Misión con Drone Recomendado y Justificación Técnica

### 3.1. Imagen del Mockup Generado
![Detalle de Misión y Recomendación](/docs/images/mock-detalle-mision-prinplup.jpg)

### 3.2. Prompt de Generación IA Utilizado
```text
PROMPT 2 — DETALLE DE MISION Y RECOMENDACION DE LA IA:
Futuristic dark-themed UI screen for AquaPort autonomous drone mission detail and AI recommendation. Left panel displays mission parameters: Mission ID M-AUTO-2026, Origin: Embalse Norte, Destination: Laguna Sur, Priority badge CRITICA in glowing orange-red, Payload: Water Sample 250g, Hydrological conditions: Agitated Water Level 3, Depth 8m. Center panel highlights 'RECOMMENDED DRONE: DM-03 Wave-Ranger M2 (Semisumergido)' with 90 percent battery in green, showing 3D wireframe render of the drone. Below it, an AI reasoning breakdown box: 'Strategy: MayorBateriaStrategy + ZonaCercana. Drone selected because Semisumergido hull is required for agitated water conditions, and has optimal 90% battery margin.' Large prominent glowing cyan CTA button: 'AUTORIZAR Y ASIGNAR MISION'.
```

### 3.3. Explicabilidad Algorítmica (XAI)
* **Justificación Transparente:** La interfaz no solo presenta el resultado (`DM-03`), sino que desglosa el razonamiento del `AsignadorAutomaticoMisiones`:
  1. *Condición hídrica reportada:* Nivel de agitación 3 requiere casco `Semisumergido` (los `Superficiales` son descartados por oleaje).
  2. *Capacidad de carga:* 250g es soportado plenamente (máximo 1.5kg).
  3. *Estrategia activa:* `MayorBateriaStrategy` identifica que `DM-03` posee 90% de batería en la zona `Embalse Norte`, superando ampliamente a los demás candidatos.
* **Ley de Fitts:** El botón principal `AUTORIZAR Y ASIGNAR MISIÓN` tiene un área amplia con contraste cian brillante, minimizando el tiempo de desplazamiento del cursor del operador.

---

## 4. Pantalla 3: Confirmación de Despacho con Estado Actualizado de Flota

### 4.1. Imagen del Mockup Generado
![Confirmación de Despacho y Flota](/docs/images/mock-confirmacion-flota-prinplup.jpg)

### 4.2. Prompt de Generación IA Utilizado
```text
PROMPT 3 — CONFIRMACION DE DESPACHO Y ESTADO DE FLOTA:
Sleek dark-themed UI confirmation screen for AquaPort autonomous dispatch. Success banner with green neon checkmark reading 'MISION ASIGNADA Y DESPACHADA EXITOSAMENTE - CODIGO M-AUTO-2026'. Telemetry tracking map showing route from Embalse Norte to Laguna Sur with animated glowing path. Bottom dashboard shows the updated fleet status overview: Total Active Drones 15, Available: 7, On Mission: 3, Charging: 2, Maintenance: 2, Fault: 1. A real-time notification feed on the right side: 'Centro de Control notificado (ACK 200)', 'Técnico de Mantenimiento sincronizado'. Modern clean sci-fi operations center UI.
```

### 4.3. Feedback Inmediato del Sistema
* **Trazabilidad Náutica:** Mapa de telemetría 3D con ruta hidrológica activa entre Embalse Norte y Laguna Sur.
* **Sincronización en Vivo con Observadores:** Registro del evento distribuido a `CentroControlObserver` y `TecnicoMantenimientoObserver`.
* **Consistencia de Inventario:** Contador global actualizado (15 drones en total: 7 disponibles, 3 en misión, 2 recargando, 2 en mantenimiento, 1 en fallo).

---

## 5. Pantalla 4: Alerta de Contingencia — Misión CRÍTICA sin Drones Disponibles

### 5.1. Imagen del Mockup Generado
![Alerta Crítica Sin Drones Disponibles](/docs/images/mock-alerta-mision-critica-prinplup.png)

### 5.2. Prompt de Generación IA Utilizado
```text
PROMPT 4 — ALERTA CRITICA SIN DRONES APTOS DISPONIBLES:
Dark-themed emergency alert UI screen for AquaPort autonomous operations center. Prominent red and crimson pulsing alarm banner across the top reading 'ALERTA CRITICA: MISION DE EMERGENCIA SIN DRONES APTOS DISPONIBLES'. Center modal dialog displaying mission details: Mission ID M-CRIT-99, Origin Bahía Ciencia, Destination Laguna Sur, Agitated Water Level 5, Required Payload 1.2kg. Diagnostic readout showing '0 Drones Available Meeting Technical Requirements (Semisumergido hull, Battery >50%)'. Emergency actions section: Large red emergency button 'SOLICITAR RELEVO DE FLOTA EXTERNA' and secondary button 'ACTIVAR MANTENIMIENTO PRIORITARIO'. Real-time push alert log showing 'CentroControlObserver: Notificado (Nivel Crítico)' and 'TecnicoMantenimientoObserver: Alerta enviada'. Futuristic cybernetic security operations console.
```

### 5.3. Manejo de Excepciones y Resiliencia
* **Disparador en Backend:** Corresponde al caso TDD obligatorio de Reto 12 (`misionCritica_sinDronesDisponibles_notificaObservadoresYLanzaExcepcion`).
* **Acciones Inmediatas de Mitigación (Ley de Fitts):**
  1. *Solicitar Relevo de Flota Externa:* Despacho asistido de drones de carga pesada.
  2. *Carga Rápida Prioritaria (Fast-Charge):* Acelerar el retorno operativo de drones en recarga.
  3. *Reintento con Trasbordo Intermedio:* Fraccionar el trayecto a través de una boya intermedia.
