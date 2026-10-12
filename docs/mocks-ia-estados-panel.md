# 🖥️ Diseño de Pantallas y Mocks con IA — AquaPort MVP (Reto 11)

Este documento contiene la especificación de diseño, referencias reales de la industria hídrica, los **3 prompts estructurados y especializados** para generar los estados del panel con herramientas de IA (Midjourney, v0.dev, Claude Artifacts, Lovable, Figma AI) y la verificación de las heurísticas de usabilidad de Nielsen.

---

## 🌊 1. Referencias Reales de Sistemas de Monitoreo Hídrico

Para asegurar un diseño profesional y fundamentado en centros de control reales, se toman como base dos sistemas de la industria:
1. **Ocean Alpha Fleet Control Center (Unmanned Surface Vessels - USV):** Centro de mando para embarcaciones no tripuladas en puertos y lagos. Destaca por el uso de mapas oscuros, telemetría de batería en tiempo real en tarjetas compactas y visualización de rutas con colores de estado verde/azul/rojo.
2. **YSI Hydrodata EXO Water Quality Dashboard:** Plataforma de telemetría de boyas y sensores de agua de campo. Utiliza tarjetas modulares con IDs técnicos en fuentes monoespaciadas, umbrales de alerta crítica por batería y segregación de acciones según disponibilidad.

---

## 🎨 2. Tokens de Identidad Aplicados (del Reto 08)
* **Fondo Principal:** Deep Abyssal Dark (`#0B132B`)
* **Superficie / Cards:** Ocean Navy (`#1C2541`)
* **Acento Primario:** Bioluminescent Cyan (`#48CAE4`)
* **Bordes / Separadores:** Technical Slate (`#3A506B`)
* **Texto Primario:** Crisp White (`#F8F9FA`)
* **Colores de Estado:**
  * Disponible: `#10B981` (Verde Esmeralda)
  * En Misión: `#3B82F6` (Azul Océano)
  * Recargando: `#F59E0B` (Ámbar)
  * Mantenimiento: `#64748B` (Gris Pizarra)
  * Fallo Crítico / Batería Baja: `#EF4444` (Rojo Coral)
* **Tipografía:** `Inter` (interfaz) y `JetBrains Mono` (IDs `AR-XX`, batería y zonas).

---

## 🤖 3. Prompts Estructurados para Generación con IA (Los 3 Estados)

Copia y pega estos prompts en herramientas como **Midjourney**, **v0.dev**, **Claude**, **ChatGPT/DALL-E 3** o **Lovable**:

---

### 🟢 Estado 1: Vista Normal (Flota Operativa Mixta)
> **Contexto:** Vista habitual de trabajo del operador con los 4 drones del MVP distribuidos en distintos estados operativos.

```text
Actúa como diseñador UX/UI senior de sistemas de monitoreo ambiental.
SISTEMA: AquaPort — Panel de control de flota de drones acuáticos ECI (Escuela Colombiana de Ingeniería).
PANTALLA: Panel de monitoreo de flota (Vista Principal del Operador - ESTADO NORMAL).
ESTILO Y PALETA: Deep dark maritime dashboard. Fondo principal #0B132B, tarjetas de drones en superficie #1C2541 con bordes sutiles #3A506B, botones de acción y acentos en #48CAE4 (bioluminescent cyan). Tipografía limpia Inter para textos y JetBrains Mono para IDs y métricas técnicas.
ACTOR: Operador Hídrico — necesita supervisar la flota y tomar decisiones rápidas de asignación.
DATOS A MOSTRAR (Exactamente 4 tarjetas de drones en cuadrícula responsive 2x2):
- Card 1: ID "[AR-01]", Modelo "Aqua-Ranger 100", Batería 92% (barra de progreso verde #10B981), Estado "DISPONIBLE" (badge verde #10B981), Zona "Embalse Norte". Botón activo cian "Asignar a Misión".
- Card 2: ID "[AR-02]", Modelo "Aqua-Ranger 100", Batería 45% (barra verde #10B981), Estado "DISPONIBLE" (badge verde #10B981), Zona "Canal Central". Botón activo cian "Asignar a Misión".
- Card 3: ID "[AR-03]", Modelo "Aqua-Ranger 100", Batería 18% (barra ámbar #F59E0B), Estado "RECARGANDO" (badge ámbar #F59E0B), Zona "Laguna Sur". Botón deshabilitado con candado "Cargando en base".
- Card 4: ID "[AR-04]", Modelo "Aqua-Ranger 100", Batería 73% (barra azul #3B82F6), Estado "EN MISIÓN" (badge azul #3B82F6), Zona "Punto Ribereño Este". Botón secundario "Ver Detalle de Ruta".
DETALLES UX: Barra superior con título "AquaPort ECI · Monitoreo de Flota", reloj de campus y estado general "Operacional". Interfaz limpia, minimalista (Heurística #8), badges semánticos visibles al primer vistazo (Heurística #1). Render 16:9 4K dashboard, Dribbble trending UI.
```

---

### 🔴 Estado 2: Vista de Alerta (Drone en Estado FALLO Crítico)
> **Contexto:** Se produce una emergencia o batería crítica en navegación; el sistema destaca visualmente el incidente para intervención urgente.

```text
Actúa como diseñador UX/UI senior de sistemas de monitoreo ambiental.
SISTEMA: AquaPort — Panel de control de flota de drones acuáticos ECI.
PANTALLA: Panel de monitoreo de flota (ESTADO DE ALERTA CRÍTICA).
ESTILO Y PALETA: Dashboard técnico oscuro con alerta visual destacada. Fondo #0B132B, cards normales #1C2541, y card en fallo con borde y halo rojo pulsante #EF4444 (coral red).
ACTOR: Operador Hídrico — debe diagnosticar y responder inmediatamente a una anomalía en un canal.
ELEMENTOS VISUALES DEL ESTADO DE ALERTA:
1. Banner superior de advertencia global: Fondo rojo oscuro translúcido con icono de peligro: "ALERTA TÉCNICA: Drone AR-03 reporta fallo de propulsión y batería crítica (12%) en Laguna Sur. Protocolo de rescate activado."
2. Tarjeta destacada [AR-03] (ESTADO FALLO):
   - Borde rojo intenso #EF4444 con resplandor neón.
   - Badge parpadeante rojo: "FALLO / BATERÍA CRÍTICA (12%)".
   - Zona: "Laguna Sur — Estación Boya 2".
   - Acciones prioritarias rojas: Botón "Activar Retorno de Emergencia" y "Contactar Equipo de Rescate".
3. Otras tarjetas (AR-01, AR-02, AR-04): Permanecen atenuadas sutilmente en segundo plano para dirigir el 100% de la atención del operador a la anomalía.
DETALLES UX: Cumple Heurística #1 (visibilidad inmediata del incidente) y Heurística #9 (diagnóstico claro del problema y acciones de recuperación evidentes). Render HD 16:9 interfaz UI.
```

---

### 🟡 Estado 3: Vista Vacía / Saturada (Todos los Drones en Misión Simultánea)
> **Contexto:** Flota temporalmente agotada sin drones libres para asignación inmediata. Debe informar claramente la situación y cuándo se liberarán.

```text
Actúa como diseñador UX/UI senior de sistemas de monitoreo ambiental.
SISTEMA: AquaPort — Panel de control de flota de drones acuáticos ECI.
PANTALLA: Panel de monitoreo de flota (ESTADO VACÍO / FLOTA COMPLETA EN MISIÓN).
ESTILO Y PALETA: Deep dark UI #0B132B, cards en #1C2541, acentos informativos en azul marino #3B82F6 y gris pizarra #64748B.
ACTOR: Operador Hídrico — intenta despachar una nueva muestra pero no hay drones disponibles en este momento.
ELEMENTOS VISUALES DEL ESTADO VACÍO:
1. Panel central o banner informativo prominente: "0 DRONES DISPONIBLES EN PUERTO — Toda la flota se encuentra ejecutando misiones hídricas activas".
2. Indicador de tiempo estimado para próxima disponibilidad: "Próxima liberación estimada: AR-01 en 14 minutos (Laboratorio Hídrico)".
3. Las 4 tarjetas de los drones muestran badges azules "EN MISIÓN" (AR-01, AR-02, AR-03, AR-04) con porcentajes de progreso de ruta (ej. 85%, 60%, 40%, 15%).
4. Botón principal de asignación general "Nueva Misión" deshabilitado temporalmente con tooltip explicativo: "Espere el arribo de un drone o encole la solicitud en lista de espera".
DETALLES UX: Cumple Heurística #5 (prevención de errores bloqueando asignaciones imposibles) y Heurística #1 (retroalimentación transparente del estado del sistema). Render UI dashboard.
```

---

## 🖼️ 4. Espacios Reservados para los Mocks

A continuación se disponen los enlaces a las imágenes una vez generadas con la IA y guardadas en la carpeta `docs/images/`:

### Mock 1: Estado Normal (Flota Operativa)
![Mock Estado Normal](../docs/images/mock-estado-normal.png)
*Descripción: Cuadrícula de 4 drones con estados disponibles, en recarga y en misión.*

---

### Mock 2: Estado de Alerta (Fallo en Drone)
![Mock Estado Alerta](../docs/images/mock-estado-alerta.png)
*Descripción: Banner superior rojo y tarjeta de AR-03 destacada con opciones de contingencia.*

---

### Mock 3: Estado Vacío (Toda la Flota en Misión)
![Mock Estado Vacío](../docs/images/mock-estado-vacio.png)
*Descripción: 0 drones libres, progreso de trayectos activos y tiempo estimado para el próximo arribo.*

---

## 🧭 5. Verificación de Heurísticas de Nielsen en los Mocks

| Heurística | Aplicación Concreta en los 3 Estados del Mock |
|---|---|
| **#1 Visibilidad del estado del sistema** | El operador identifica en menos de 2 segundos el estado de cada drone mediante códigos de color unívocos (verde = listo, rojo = fallo, azul = en ruta). En el estado vacío, sabe exactamente cuándo se liberará el próximo drone. |
| **#5 Prevención de errores** | En el estado de alerta o de batería baja, los botones de "Asignar a Misión" se bloquean y atenúan (`disabled`), impidiendo físicamente que el operador envíe un drone sin capacidad energética. |
| **#8 Estética y diseño minimalista** | No hay gráficos superfluos ni métricas irrelevantes. Cada tarjeta se concentra estrictamente en los 4 datos operativos de decisión: ID, batería, estado y zona. |
| **#9 Ayudar a reconocer y recuperarse de errores** | En el estado de alerta crítica, el banner no dice *"Error 500"*, sino *"Fallo de propulsión en Laguna Sur (12% batería)"* y provee botones directos de acción correctiva (*"Activar Retorno de Emergencia"*). |
