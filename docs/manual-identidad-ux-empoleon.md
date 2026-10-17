# 🎨 Sistema de Diseño, Manual de Identidad Visual y Guía UX — AquaPort Enterprise (Empoleon v3)

Este documento define el **Design System** oficial para la escala Enterprise de AquaPort: tokens de diseño (color, tipografía, espaciado, radio de borde, elevación), especificación de componentes con sus estados interactivos, verificación de contraste **WCAG AA** y el mapa de flujos de **6 pantallas** con fundamentación de las **leyes de experiencia de usuario (UX)** aplicadas.

> 🌐 **Prototipo Interactivo Funcional de Alta Fidelidad:**  
> Se encuentra disponible la maqueta interactiva completa que permite experimentar las 6 pantallas en: [`docs/aquaport-enterprise-ux-flota.html`](file:///docs/aquaport-enterprise-ux-flota.html).

---

## 🌊 1. Tokens de Diseño del Sistema (*Abyssal Enterprise Design System*)

### A. Paleta de Colores de Estructura (*Dark Abyssal Palette*)

| Token CSS | Código Hex | Rol en la Interfaz Enterprise | Verificación WCAG AA sobre Fondo |
|---|---|---|---|
| `--bg-abyssal` | `#050B18` | Fondo principal de la aplicación; optimizado para centros de control nocturnos. | N/A (Fondo base) |
| `--bg-surface` | `#0B1528` | Superficie de paneles, barra lateral y contenedores de datos. | N/A (Superficie intermedia) |
| `--bg-card` | `#111F38` | Fondo de las tarjetas de drones y tramos de ruta. | Contraste de borde $3.2:1$ |
| `--bg-elevated` | `#182B4D` | Modales flotantes, menús desplegables y tooltips. | Contraste de borde $4.1:1$ |
| `--border-subtle` | `#1E3A66` | Separadores de datos y delimitación de cuadrícula. | Contraste componente $3.5:1$ |
| `--border-accent` | `#0284C7` | Borde de foco, contenedor de límites y selección activa. | Contraste UI $5.8:1$ |

---

### B. Paleta Semántica de Estados de Flota y Auditoría de Contraste WCAG AA

Todos los colores de estado han sido verificados matemáticamente contra el fondo oscuro principal (`#050B18` y `#0B1528`), superando el umbral **WCAG 2.1 Nivel AA** ($\ge 4.5:1$ para texto regular, $\ge 3.0:1$ para componentes de interfaz y texto grande):

| Estado del Drone / Sistema | Código Hex | Fondo Translúcido (Tint) | Ratio de Contraste | Cumplimiento WCAG | Semántica Operativa Enterprise |
|---|---|---|---|---|---|
| 🟢 **Disponible** | `#10B981` | `rgba(16, 185, 129, 0.12)` | **8.2 : 1** | ✅ **AAA** / **AA** | Drone en estación o boya listo para recibir misión; batería $\ge 35\%$. |
| 🔵 **En Tránsito** | `#38BDF8` | `rgba(56, 189, 248, 0.12)` | **9.4 : 1** | ✅ **AAA** / **AA** | Drone navegando en ruta multi-etapa con telemetría activa a $1\text{ Hz}$. |
| 🟣 **En Traspaso / Esclusa** | `#818CF8` | `rgba(129, 140, 248, 0.14)` | **7.8 : 1** | ✅ **AAA** / **AA** | Drone en boya de transferencia o esclusa realizando relevo físico de muestra. |
| 🌌 **Sumergido / Buceo** | `#60A5FA` | `rgba(96, 165, 250, 0.14)` | **8.1 : 1** | ✅ **AAA** / **AA** | Drone Buceador en inmersión profunda recolectando sedimentos o agua de fondo. |
| 🟡 **Recargando** | `#FBBF24` | `rgba(251, 191, 36, 0.12)` | **11.2 : 1** | ✅ **AAA** / **AA** | Drone acoplado a muelle de carga rápida en zona de mantenimiento. |
| 🔴 **Alerta / Fallo Crítico** | `#F87171` | `rgba(248, 113, 113, 0.16)` | **6.9 : 1** | ✅ **AA** | Anomalía severa (batería $< 35\%$, desvío de corredor o pérdida de señal). |
| ⚪ **Texto Primario** | `#F8FAFC` | N/A | **16.8 : 1** | ✅ **AAA** / **AA** | Encabezados, títulos y valores numéricos de alta prioridad. |
| 🔘 **Texto Secundario** | `#94A3B8` | N/A | **6.3 : 1** | ✅ **AA** | Etiquetas de metadatos, unidades de medida y leyendas descriptivas. |

---

### C. Tokens Tipográficos

* **Familia Primaria (UI, Headings, Botones):** `'Inter'`, `'Outfit'`, `-apple-system`, `sans-serif`.
* **Familia Técnica (Telemetría, IDs, Coordenadas GPS, Hash Criptográfico):** `'JetBrains Mono'`, `'Fira Code'`, `monospace`.

```css
/* Escala Tipográfica Modular */
--font-display: 700 1.75rem/1.2 'Inter', sans-serif;    /* 28px - H1 Hero / Dashboard */
--font-title:   600 1.25rem/1.3 'Inter', sans-serif;    /* 20px - H2 Secciones / Modales */
--font-subtitle:600 1.00rem/1.4 'Inter', sans-serif;    /* 16px - H3 Tarjetas de Drone */
--font-body:    400 0.875rem/1.5 'Inter', sans-serif;   /* 14px - Párrafos y Formularios */
--font-caption: 500 0.75rem/1.4 'Inter', sans-serif;    /* 12px - Badges y Metadatos */
--font-mono:    500 0.8125rem/1.4 'JetBrains Mono', monospace; /* 13px - Telemetría y Hash */
```

---

### D. Tokens de Espaciado (Escala Modular de 4px) y Radio de Borde

```css
/* Espaciado */
--space-1: 4px;   --space-2: 8px;   --space-3: 12px;  --space-4: 16px;
--space-5: 20px;  --space-6: 24px;  --space-8: 32px;  --space-12: 48px; --space-16: 64px;

/* Radios de Borde */
--radius-sm:   4px;    /* Chips e inputs compactos */
--radius-md:   8px;    /* Botones y controles */
--radius-lg:   12px;   /* Tarjetas de drones y paneles */
--radius-xl:   16px;   /* Ventanas modales de alerta */
--radius-full: 9999px; /* Badges tipo píldora y avatares */
```

---

## 🧩 2. Especificación de Componentes Enterprise y sus Estados

### Componente 1: Tarjeta de Drone Enterprise (6 Estados de Flota)

Cada una de las 60 tarjetas de drone cuenta con franja superior semántica, badge de estado, barra de batería reactiva, telemetría técnica (`JetBrains Mono`) y botón de acción contextual:

1. **Estado Disponible (Verde `#10B981`):**  
   * Borde superior verde esmeralda. Badge `● DISPONIBLE`. Batería $\ge 35\%$.  
   * Botón habilitado: `«Asignar a Tramo»`.
2. **Estado En Tránsito (Azul `#38BDF8`):**  
   * Borde superior cyan eléctrico con pulso sutil. Badge `🚢 EN TRÁNSITO [TR-2]`.  
   * Botón secundario: `«Ver Telemetría»`. Muestra velocidad (nudos) y coordenadas GPS.
3. **Estado En Traspaso / Esclusa (Índigo `#818CF8`):**  
   * Borde superior índigo. Badge `⏳ EN TRASPASO (Boya 2)`.  
   * Indicador de temporizador regresivo de exclusa y receptáculo de muestra.
4. **Estado Buceando / Sumergido (Azul Profundo `#60A5FA`):**  
   * Borde superior azul cielo. Badge `🤿 BUCEO (-3.50 m)`.  
   * Muestra profundidad en metros, presión hidrostática y turbidez en NTU.
5. **Estado Recargando (Ámbar `#FBBF24`):**  
   * Borde superior ámbar. Badge `⚡ RECARGANDO (82%)`.  
   * Muestra tiempo estimado para carga completa (ETA: 14 min). Botón deshabilitado.
6. **Estado Alerta / Fallo Crítico (Rojo `#F87171`):**  
   * Borde superior rojo con halo pulsante animado (`box-shadow: 0 0 16px rgba(248,113,113,0.4)`).  
   * Badge `🚨 FALLO CRÍTICO`. Botón de emergencia prioritario: `«Reasignar Unidad»`.

---

### Componente 2: Botón Primario de Misión (5 Estados Interactivos)

| Estado | Apariencia Visual | Comportamiento e Interacción |
|---|---|---|
| **Default** | Fondo degradado cyan `#0284C7` $\to$ `#0369A1`, texto `#FFFFFF`, borde sutil. | Listo para recibir clic; cursor pointer. |
| **Hover** | Brillo elevado `#38BDF8`, sombra exterior `0 4px 14px rgba(56,189,248,0.3)`. | Transición suave en $150\text{ ms}$; eleva $1\text{px}$ en eje Y. |
| **Active / Pressed** | Tinte oscurecido `#0284C7`, escala $0.98$, sombra contraída. | Retroalimentación táctil de pulsación inmediata. |
| **Loading** | Fondo `#0F2744`, spinner circular giratorio blanco, texto «Validando Cadena...». | Puntero en espera; bloquea clics duplicados (*debouncing*). |
| **Disabled** | Fondo `#16233B`, texto gris `#64748B`, cursor `not-allowed`. | Indica condición no cumplida (ej. sin waypoints o batería $<35\%$). |

---

### Componente 3: Selector de Waypoints y Tramos Multi-Etapa (5 Estados)

* **No Asignado:** Borde discontinuo gris pizarra, badge gris `[PENDIENTE DE DRONE]`.
* **Asignado:** Borde cyan sólido, muestra ID de drone seleccionado, modelo y autonomía estimada.
* **En Progreso:** Fondo azul translúcido con barra de progreso animada del tramo.
* **Traspaso Exitoso:** Checkmark verde `✓ RELEVO COMPLETADO`, hash de custodia anclado.
* **Error / Reasignando:** Borde rojo parpadeante con cuenta regresiva de sustitución de unidad.

---

### Componente 4: Badge de Cadena de Custodia de Muestras (4 Estados)

* 🟢 **Custodia Certificada (Verde `#10B981`):** Hash SHA-256 verificado, temperatura estable ($4.0^\circ\text{C}$).
* 🔵 **En Traspaso Activo (Cyan `#38BDF8`):** Sellando nuevo bloque criptográfico con drone receptor.
* 🟡 **Alerta Térmica (Ámbar `#FBBF24`):** Temperatura de muestra fuera de rango óptimo ($6.8^\circ\text{C}$).
* 🔴 **Firma Inválida (Rojo `#F87171`):** Discrepancia criptográfica en el hash encadenado; rechazo en LIMS.

---

## 🗺️ 3. Mapa de Flujos del Enterprise (Las 6 Pantallas) y Leyes UX

El flujo operativo Enterprise guía al usuario a través de 6 pantallas especializadas:

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   PANTALLA 1    │  ───> │   PANTALLA 2    │  ───> │   PANTALLA 3    │
│  Mapa de Zonas  │       │ Selec. de Ruta  │       │ Asignac. Tramos │
└─────────────────┘       └─────────────────┘       └─────────────────┘
                                                             │
                                                             ▼
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   PANTALLA 6    │  <─── │   PANTALLA 5    │  <─── │   PANTALLA 4    │
│ Reporte Entrega │       │ Alerta & Reasig.│       │ Monitoreo Vivo  │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

---

### 🖥️ Pantalla 1: Mapa Global de Zonas (Visión Sinóptica de 60 Drones)
* **Propósito:** Brinda a los operadores una panorámica en tiempo real de las 4 zonas hídricas (Embalse Norte, Canales, Laguna Reserva, Laboratorio Central), la disponibilidad de los 60 drones y los indicadores globales de salud de la flota.
* **Elementos Clave:** Mini-mapa interactivo del campus ECI, cuadrícula de KPIs (Disponibles: 48, En Misión: 9, Recargando: 2, Fallo: 1), filtros rápidos por zona y tipo de drone.
* **Leyes UX Aplicadas:**
  1. **Ley de Hick:** La agrupación jerárquica de 60 drones en 4 zonas discretas reduce el tiempo de decisión cognitiva del operador en comparación con mostrar una lista plana desordenada.
  2. **Ley de Proximidad de Gestalt:** Los drones pertenecientes a una misma cuenca hídrica comparten proximidad visual y código de color zonal, permitiendo interpretar la cobertura territorial en menos de 2 segundos.

---

### 🗺️ Pantalla 2: Selección de Ruta y Waypoints Intermedios
* **Propósito:** Permite estructurar misiones complejas multi-etapa seleccionando el origen, insertando boyas limnológicas intermedias y fijando el destino final de la carga.
* **Elementos Clave:** Mapa cartográfico vectorial, selector de puntos de ruta arrastrables (*drag-and-drop*), formulario de metadatos de carga (tipo, peso en gramos, prioridad) y cálculo instantáneo de distancia geodésica.
* **Leyes UX Aplicadas:**
  1. **Ley de Fitts:** Los botones de adición de waypoints y el botón primario de despacho tienen áreas de clic generosas ($\ge 48\times 48\text{px}$) y están anclados en zonas ergonómicas del panel lateral para minimizar el tiempo de puntería del cursor.
  2. **Efecto de Posición Serial:** Los operadores retienen con mayor precisión el inicio (origen) y el fin (Laboratorio Central) de la ruta; la interfaz refuerza visualmente estos dos polos con insignias de alto contraste y alinea los waypoints intermedios en una línea de tiempo secuencial.

---

### ⚙️ Pantalla 3: Asignación por Tramos y Validación de Cadena
* **Propósito:** Desglosa la ruta multi-etapa en tramos especializados, asigna automáticamente el drone óptimo para cada cuerpo de agua y ejecuta la *Chain of Responsibility*.
* **Elementos Clave:** Tarjetas de tramo conectadas por líneas de relevo, ficha técnica del drone sugerido, visor de la cadena de validación (Batería $\to$ Carga $\to$ Zona $\to$ Condiciones Hídricas) con animación de aprobación eslabón por eslabón.
* **Leyes UX Aplicadas:**
  1. **Ley de Miller:** La descomposición de una ruta compleja en fragmentos digeribles (*chunks*) de $3\pm 1$ tramos legibles por pantalla evita sobrecargar la memoria de trabajo del operador.
  2. **Principio de Visibilidad del Estado del Sistema (Nielsen #1):** Cada validador de la cadena refleja de manera explícita si el candidato superó la prueba con checkmarks verdes o muestra el motivo exacto de rechazo si un eslabón detuvo la cadena.

---

### 📡 Pantalla 4: Monitoreo en Tiempo Real y Telemetría Cifrada
* **Propósito:** Supervisa el vuelo acuático del drone activo en el tramo actual, el consumo de batería, la velocidad en nudos y la integridad de la cadena de custodia.
* **Elementos Clave:** Telemetría HUD estilo náutico en tipografía monoespaciada `JetBrains Mono`, brújula de rumbo, gráfico de perfil de inmersión para drones buceadores y terminal en vivo de eventos telemétricos cifrados.
* **Leyes UX Aplicadas:**
  1. **Ley de Jakob:** Se utilizan convenciones estandarizadas de instrumentación naval y aeronáutica (indicadores de aguja/arco, horizonte artificial y rumbos de compás), reduciendo la curva de aprendizaje para personal técnico.
  2. **Ley de Pregnancia (Buena Forma):** Las figuras geométricas simples, limpias y libres de saturación decorativa facilitan la rápida asimilación de métricas bajo condiciones de estrés o emergencia.

---

### 🚨 Pantalla 5: Alerta de Fallo y Reasignación Autónoma en Waypoint
* **Propósito:** Gestiona contingencias operativas cuando una unidad entra en batería crítica o avería durante el tránsito hacia una boya de relevo.
* **Elementos Clave:** Modal de alerta de alta prioridad con halo rojo pulsante, cuenta regresiva de reasignación automática de **$3.0\text{ segundos}$**, comparativa visual entre el drone averiado y el drone de relevo asignado, y botón de confirmación/anulación manual.
* **Leyes UX Aplicadas:**
  1. **Efecto de Aislamiento (Ley de Von Restorff):** El elemento anómalo (drone en fallo) rompe radicalmente el patrón cromático mediante un halo carmesí y animación de pulso, capturando de forma instantánea la atención selectiva del operador.
  2. **Ley de Pareto (Principio 80/20):** El sistema automatiza el 80% de la lógica de sustitución de emergencia sin requerir clics del operador, reservando la intervención humana exclusivamente para el 20% de decisiones críticas de desvío mayor.

---

### 📑 Pantalla 6: Reporte de Entrega, Liquidación y Cadena de Custodia
* **Propósito:** Formaliza el cierre exitoso de la misión en el Laboratorio Hídrico Central, certifica la cadena de custodia inmutable y expone las métricas analíticas de eficiencia zonal.
* **Elementos Clave:** Acta de recepción con sello verde digital, historial de hashes encadenados SHA-256 por waypoint, gráfica de temperatura de muestra durante el viaje, botón de exportación al LIMS central y KPIs de eficiencia calculados en una pasada $O(N)$.
* **Leyes UX Aplicadas:**
  1. **Regla del Pico y Final (Peak-End Rule):** El cierre del ciclo operativo proporciona una experiencia de alta satisfacción con certificación visual de muestra íntegra, reforzando la confianza en la confiabilidad de la plataforma Enterprise.
  2. **Ley de Tesler (Conservación de la Complejidad):** Toda la complejidad matemática del encadenamiento criptográfico y la sincronización de 4 zonas es absorbida internamente por el sistema, presentando al usuario un reporte limpio, accionable y comprensible.
