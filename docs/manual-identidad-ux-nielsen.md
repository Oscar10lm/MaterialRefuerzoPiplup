# 🎨 Manual de Identidad Visual y Guía UX/UI — AquaPort MVP (Piplup)

Este documento establece el sistema de diseño, tokens de color, tipografía, tono de comunicación, prompt de generación de interfaz y la evaluación formal de las heurísticas de usabilidad de Jakob Nielsen para la estación de control y monitoreo de **AquaPort** (ECI).

---

## 🌊 1. Manual de Identidad Visual

### A. Justificación Conceptual
AquaPort opera en la gestión de recursos hídricos y monitoreo ambiental del campus universitario. La identidad visual debe transmitir **calma reflexiva**, **precisión técnica e ingeniería autónoma**, y **respeto por los ecosistemas acuáticos**. Se adopta un modo oscuro técnico (*deep dark UI*) con contrastes elevados para facilitar la lectura continua del operador en salas de control sin fatiga visual.

---

### B. Paleta de Colores (Tokens Oficiales con Códigos Hex)

#### 1. Colores Primarios y Neutros de Estructura
| Rol / Nombre | Código Hex | Muestra | Justificación y Aplicación |
|---|---|---|---|
| **Abyssal Dark (Fondo)** | `#0B132B` | 🟦 | Fondo principal de la aplicación. Representa la profundidad del agua y reduce la fatiga visual. |
| **Ocean Navy (Superficie / Cards)** | `#1C2541` | 🟦 | Fondo de paneles, tarjetas de drones y barras de herramientas. Contraste suave sobre el fondo principal. |
| **Bioluminescent Cyan (Acento Primario)** | `#48CAE4` | 🩵 | Cian hídrico vibrante. Se utiliza en botones de acción principal (CTA), bordes de selección activa y métricas clave. |
| **Technical Slate (Bordes)** | `#3A506B` | 🩶 | Líneas divisorias sutiles, bordes de tablas y separadores estructurales. |
| **Crisp White (Texto Primario)** | `#F8F9FA` | ⚪ | Tipografía principal, títulos y valores numéricos de alta relevancia. Máxima legibilidad. |
| **Muted Watermark (Texto Secundario)** | `#94A3B8` | 🔘 | Etiquetas de metadatos, descripciones secundarias y marcas de agua. |

---

#### 2. Colores Semánticos de Estado (Críticos para el Operador)
Cada color de estado cuenta con un token de primer plano (borde/icono) y un tinte de fondo translúcido (*tint* al 15%):

| Estado del Drone | Código Hex | Token | Significado Operativo en AquaPort |
|---|---|---|---|
| **Disponible / Listo** | `#10B981` | `Emerald Green` | Drone libre para tomar misiones con batería óptima ($\ge 35\%$). |
| **En Misión / Navegando** | `#3B82F6` | `Ocean Blue` | Drone en trayecto de entrega o recolección de muestra activa. |
| **Recargando / En Base** | `#F59E0B` | `Amber Warning` | Drone anclado en estación de carga hídrica recuperando energía. |
| **Mantenimiento / Deshabilitado** | `#64748B` | `Slate Neutral` | Drone fuera de servicio temporal o inspección técnica. |
| **Fallo Crítico / Batería Baja** | `#EF4444` | `Coral Red` | Batería crítica ($< 35\%$), avería de hélice o pérdida de telemetría. |

---

### C. Tipografía Oficial

| Uso | Familia Tipográfica | Pesos | Justificación |
|---|---|---|---|
| **Interfaz General y Títulos** | `Inter` / `Outfit` | Regular (400), SemiBold (600), Bold (700) | Sans-serif geométrica optimizada para legibilidad en pantallas digitales a cualquier densidad de píxeles. |
| **Identificadores y Telemetría** | `JetBrains Mono` / `Fira Code` | Medium (500), Bold (700) | Tipografía monoespaciada para IDs de drones (`AR-01`), códigos de misión (`MIS-001`), coordenadas y porcentajes (`92%`), evitando saltos visuales al actualizarse. |

---

### D. Tono de Voz y Comunicación
* **Tono:** Técnico, conciso, preventivo y contextualizado ambientalmente.
* **Directrices:**
  * Sin ambigüedades: Evitar frases genéricas como *"Ocurrió un error"*.
  * Orientado a la acción: Indicar siempre la causa exacta y el siguiente paso operativo (ej. *"Drone AR-03 con 18% de carga. Conecte a estación de carga en Laguna Sur antes de asignar"*).

---

## 🤖 2. Prompt Especializado para Generación del Mockup con IA

Copia y pega el siguiente prompt en herramientas como **Midjourney**, **DALL-E 3**, **v0.dev**, **Claude Artifacts** o **Lovable**:

```text
Professional UI/UX dashboard mockup of "AquaPort", an autonomous aquatic drone fleet monitoring station at a university engineering campus. 

Design System & Aesthetic:
- High-tech, dark mode control room dashboard interface with a refined maritime/hydrological theme.
- Color Palette: Deep abyssal navy background (#0B132B), card surfaces (#1C2541), glowing bioluminescent cyan accents (#48CAE4), clean border dividers (#3A506B), crisp white typography (#F8F9FA).
- Modern clean UI typography (Inter font for labels, JetBrains Mono font for drone IDs and telemetry data).
- Top navigation bar: "AquaPort ECI — Fleet Control Station" with campus water status badge "Status: All Channels Active" and operator profile "Operador Hídrico".

Main Dashboard View — "Monitoreo de Flota Acuática":
Display a clean responsive grid containing exactly 4 detailed drone fleet cards representing the MVP fleet:
1. Card 1: ID "[AR-01]", Model "Aqua-Ranger 100", Battery Level "92%" with a glowing green progress bar, Status Badge "DISPONIBLE" (vibrant emerald green #10B981), Current Zone "Embalse Norte", Action Button: Active cyan "Asignar Misión".
2. Card 2: ID "[AR-02]", Model "Aqua-Ranger 100", Battery Level "45%" with a green progress bar, Status Badge "DISPONIBLE" (emerald green), Current Zone "Canal Central", Action Button: Active cyan "Asignar Misión".
3. Card 3: ID "[AR-03]", Model "Aqua-Ranger 100", Battery Level "18%" with an urgent red warning progress bar, Status Badge "BATERÍA CRÍTICA / NO DISPONIBLE" (coral red #EF4444), Current Zone "Laguna Sur", Action Button: Visually disabled/grayed out with lock icon and helper tooltip "Batería < 35% requerida".
4. Card 4: ID "[AR-04]", Model "Aqua-Ranger 100", Battery Level "73%" with a green progress bar, Status Badge "DISPONIBLE" (emerald green), Current Zone "Punto Ribereño Este", Action Button: Active cyan "Asignar Misión".

UX Details:
- High contrast, premium glassmorphism accents, subtle rounded corners (12px radius), precise data alignment, zero clutter, professional industrial software aesthetic for environmental researchers. Desktop 16:9 aspect ratio, 4K crisp render, Dribbble trending UI.
```

---

## 🧭 3. Verificación de las Heurísticas de Usabilidad de Nielsen

El diseño planteado para el panel de monitoreo de AquaPort cumple rigurosamente con **5 heurísticas fundamentales de Jakob Nielsen**:

### 1. Heurística #1: Visibilidad del Estado del Sistema
* **Cumplimiento en AquaPort:** El operador puede conocer el estado operativo exacto de los 4 drones de un solo vistazo sin necesidad de interactuar o hacer clic. Cada tarjeta exhibe una insignia (*badge*) con color semántico inequívoco (verde para libre, rojo para crítico), barra porcentual de batería y la zona física actual en el campus.

### 2. Heurística #3: Control y Libertad del Usuario
* **Cumplimiento en AquaPort:** En caso de que el operador asigne un drone por error, el panel provee un mecanismo visible de cancelación inmediata de la misión mientras esta permanezca en estado `PENDIENTE`, retornando el drone a disponibilidad sin penalización ni bloqueos de flujo irreversibles.

### 3. Heurística #5: Prevención de Errores
* **Cumplimiento en AquaPort:** El sistema no espera a que el operador intente registrar una misión para informarle que el drone no tiene suficiente energía. La tarjeta del drone `AR-03` (18% de batería) deshabilita visualmente el botón de asignación (`disabled`), muestra un candado y un aviso contextual explicativo, haciendo imposible cometer el error de asignación desde la interfaz.

### 4. Heurística #8: Estética y Diseño Minimalista
* **Cumplimiento en AquaPort:** Cada tarjeta y panel presenta únicamente los 4 datos estrictamente necesarios para la toma de decisiones del operador MVP: **Identificador único**, **Modelo**, **Nivel de batería** y **Zona geográfica**. Se elimina el ruido visual, decoraciones innecesarias o animaciones que distraigan la labor de monitoreo hídrico.

### 5. Heurística #9: Ayudar a los Usuarios a Reconocer, Diagnosticar y Recuperarse de Errores
* **Cumplimiento en AquaPort:** Los mensajes del sistema abandonan tecnicismos crípticos o mensajes genéricos. Si una validación es interceptada, la interfaz expresa con claridad:
  > *"El drone AR-03 cuenta con 18% de batería. Se requiere un mínimo del 35% para iniciar navegación. Por favor asígnelo a un punto de recarga en Laguna Sur."*
  Indicando la causa raíz, la métrica incumplida y la sugerencia de recuperación inmediata.
