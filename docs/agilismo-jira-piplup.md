# 📋 Gestión Ágil y Backlog en Jira — AquaPort MVP (Piplup)

Este documento especifica la estructura jerárquica ágil del proyecto **AquaPort** (Nivel Piplup) para su configuración y registro en **Jira Software**, asegurando la alineación entre la estrategia del producto, las funcionalidades y la ejecución técnica.

---

## 🏛️ 1. Jerarquía Ágil Completa

```text
[ ÉPICA ]
   └── Digitalizar el monitoreo y reparto hídrico de la ECI mediante una flota de drones acuáticos supervisada
        │
        └── [ FEATURE ]
             └── Gestión de la flota de drones acuáticos del campus
                  │
                  ├── [ HISTORIA DE USUARIO 1 ]
                  │    └── HU-01: Visualización y consulta de estado de la flota
                  │
                  ├── [ HISTORIA DE USUARIO 2 (Focal) ]
                  │    └── HU-02: Asignación manual de drone a misión de transporte
                  │         ├── [Subtarea 1] Implementar validación de disponibilidad y batería en ValidadorMision
                  │         ├── [Subtarea 2] Construir asignación inmutable mediante Mision.Builder
                  │         └── [Subtarea 3] Persistir misión en RepositorioMisiones y notificar al operador
                  │
                  └── [ HISTORIA DE USUARIO 3 ]
                       └── HU-03: Cancelación de misión en estado PENDIENTE
```

---

## 🏔️ 2. Épica del MVP

* **Tipo de Issue en Jira:** `Epic`
* **Nombre de la Épica / Resumen:**  
  `Digitalizar el monitoreo y reparto hídrico de la ECI mediante una flota de drones acuáticos supervisada`
* **Descripción:**  
  Implementar el núcleo funcional (MVP) de la plataforma AquaPort para permitir al personal de la Escuela Colombiana de Ingeniería Julio Garavito coordinar, monitorear y asignar de forma manual y segura el transporte de muestras ambientales, sensores y paquetes entre los 5 cuerpos hídricos del campus.

---

## 🌟 3. Feature

* **Tipo de Issue en Jira:** `Feature` *(o Epic de segundo nivel / Capability según configuración del workspace de Jira)*
* **Resumen:**  
  `Gestión de la flota de drones acuáticos del campus`
* **Pertenencia:** Vinculada a la Épica del MVP.
* **Descripción:**  
  Módulo central encargado del inventario operativo de los 4 drones acuáticos modelo Aqua-Ranger 100, control de autonomía energética, asignación de misiones y cancelación preventiva de operaciones.

---

## 📖 4. Las 3 Historias de Usuario (Formato Estándar Ágil)

### HU-01: Visualización y consulta de estado de la flota
* **Resumen en Jira:** `HU-01: Visualizar estado operativo y autonomía de la flota acuática`
* **Estructura Ágil:**  
  > **Como** Operador Hídrico,  
  > **quiero** consultar la lista de drones acuáticos filtrando los disponibles con batería $\ge 35\%$ y ordenados de mayor a menor carga,  
  > **para** identificar de manera inmediata y sin errores qué unidades están listas para navegar antes de programar una misión.
* **Puntos de Historia (Estimación):** 3 SP
* **Prioridad:** High / Must Have

---

### HU-02 (Focal): Asignación manual de drone a misión de transporte
* **Resumen en Jira:** `HU-02: Registrar y asignar manualmente un drone a una misión de transporte`
* **Estructura Ágil:**  
  > **Como** Operador Hídrico,  
  > **quiero** registrar una misión de transporte indicando origen, destino, tipo de carga y asignando manualmente un drone disponible,  
  > **para** autorizar el despacho seguro de muestras ambientales entre las zonas hídricas del campus.
* **Puntos de Historia (Estimación):** 5 SP
* **Prioridad:** Highest / Must Have

#### 🎯 Criterios de Aceptación (Formato Given-When-Then / Gherkin)

1. **Criterio de Aceptación 1 (Asignación Exitosa con Drone Apto):**  
   * **Dado que** el operador selecciona un drone con estado `disponible = true` y nivel de batería $\ge 35\%$, un punto de partida y un destino hídrico válido diferente al origen,  
   * **Cuando** confirma el registro de la misión a través del sistema,  
   * **Entonces** el sistema genera un código de misión único, cambia/registra la misión en estado `PENDIENTE` en el repositorio y emite una confirmación en pantalla con el resumen del trayecto.

2. **Criterio de Aceptación 2 (Bloqueo Preventivo por Batería Insuficiente o No Disponibilidad):**  
   * **Dado que** el operador intenta asignar un drone con batería $< 35\%$ o con disponibilidad en `false`,  
   * **Cuando** solicita la construcción o registro de la misión,  
   * **Entonces** el sistema rechaza la operación lanzando una excepción controlada (`IllegalStateException`), no persiste ningún cambio en el repositorio y notifica al operador el motivo específico del rechazo indicando el porcentaje de carga actual y el mínimo requerido.

#### 🛠️ Subtareas Técnicas Concretas (Asignables a 1 Desarrollador)

* **Subtarea 1 (Backend / Dominio):**  
  `[HU02-ST1] Implementar validación de disponibilidad y batería mínima (35%) en ValidadorMision`  
  * *Descripción técnica:* Codificar método de validación en `ValidadorMision.java` que verifique `drone.disponible()` y `drone.bateria() >= 35`, lanzando `IllegalStateException` con mensaje en español.

* **Subtarea 2 (Backend / Patrones):**  
  `[HU02-ST2] Implementar método build() seguro y validado en Mision.Builder`  
  * *Descripción técnica:* Asegurar que `Mision.Builder` valide que los campos obligatorios (id, drone, partida, llegada, tipoCarga) no sean nulos ni vacíos antes de instanciar la clase inmutable `Mision`.

* **Subtarea 3 (Backend / Persistencia y Notificación):**  
  `[HU02-ST3] Integrar persistencia en RepositorioMisiones y disparo de evento en NotificadorOperador`  
  * *Descripción técnica:* Conectar `RegistradorMisiones` para persistir la misión validada en la abstracción `RepositorioMisiones` y formatear la salida hacia `NotificadorOperador`.

---

### HU-03: Cancelación de misión en estado PENDIENTE
* **Resumen en Jira:** `HU-03: Cancelar misión programada en estado PENDIENTE`
* **Estructura Ágil:**  
  > **Como** Operador Hídrico,  
  > **quiero** cancelar una misión que se encuentre en estado `PENDIENTE`,  
  > **para** corregir asignaciones accidentales o reprogramar rutas sin comprometer la disponibilidad del drone ni despachar operaciones erróneas.
* **Puntos de Historia (Estimación):** 2 SP
* **Prioridad:** Medium / Should Have
