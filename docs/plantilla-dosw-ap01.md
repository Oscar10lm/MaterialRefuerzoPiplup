# 📑 Especificación Formal de Requisito Funcional — Plantilla DOSW

---

## 📌 Identificación del Requisito

| Campo | Especificación |
|---|---|
| **Código** | `AP-01` |
| **Nombre** | `Registrar misión de transporte de muestra` |
| **Actor** | `Operador Hídrico` |
| **Precondiciones** | 1. El operador debe haber iniciado sesión activa en la estación de control de AquaPort.<br>2. Debe existir al menos un drone acuático registrado en el sistema con estado `disponible = true` y nivel de carga de batería $\ge 35\%$.<br>3. Los puntos de partida y llegada deben corresponder a zonas hídricas oficiales habilitadas en el campus (Embalse Norte, Canal Central, Laguna Sur, Punto Ribereño Este, Laboratorio Hídrico). |

---

## 📥 Datos de Entrada

> ⚠️ *Regla DOSW:* Prohibido usar términos de UI como "Seleccionador" o tipos genéricos como "Lista de objetos". Se especifican tipos de datos reales y atributos completos del modelo.

| Campo de Entrada | Tipo de Dato Real | Descripción / Restricción |
|---|---|---|
| `droneAsignado` | `DroneAcuatico(id: String, modelo: String, bateria: int, disponible: boolean, zona: String)` | Objeto completo del drone acuático seleccionado manualmente por el operador para cumplir la misión. |
| `puntoPartida` | `String` | Nombre de la zona hídrica inicial donde se recolecta la muestra (ej. `"Embalse Norte"`). No nulo ni vacío. |
| `puntoLlegada` | `String` | Nombre de la zona hídrica de destino donde se entregará la muestra (ej. `"Laboratorio Hídrico"`). Debe ser diferente al punto de partida. |
| `tipoCarga` | `Enum(MUESTRA_AGUA, SENSOR, PAQUETE_LIGERO)` | Tipo de elemento transportado. Para este caso de uso: `TipoCarga.MUESTRA_AGUA`. |

---

## 📤 Datos de Salida

| Campo de Salida | Tipo de Dato Real | Descripción |
|---|---|---|
| `codigoMision` | `String` | Identificador único alfanumérico generado para la misión registrada (ej. `"MIS-001"`). |
| `misionRegistrada` | `Mision(id: String, drone: DroneAcuatico, puntoPartida: String, puntoLlegada: String, tipoCarga: TipoCarga, estado: EstadoMision)` | Instancia inmutable de la misión creada y persistida con estado inicial `EstadoMision.PENDIENTE`. |

---

## 🔄 Flujo Básico de Eventos (5 Pasos)

1. **Paso 1 (Actor):** El `Operador Hídrico` solicita el registro de una nueva misión de transporte especificando el punto de partida (`"Embalse Norte"`), el punto de llegada (`"Laboratorio Hídrico"`), el tipo de carga (`TipoCarga.MUESTRA_AGUA`) y selecciona un drone acuático disponible (`DroneAcuatico` con `disponible = true`, batería $\ge 35\%$).
2. **Paso 2 (Sistema):** El sistema valida que los campos requeridos no sean nulos ni vacíos y comprueba que el punto de destino sea una zona hídrica válida y distinta del punto de origen (según **RN-02**).
3. **Paso 3 (Sistema):** El sistema valida que el drone acuático seleccionado se encuentre libre (`disponible = true`), que no posea otra misión activa simultánea y que su nivel de carga de batería sea $\ge 35\%$ (según **RN-01**).
4. **Paso 4 (Sistema):** El sistema genera un identificador único alfanumérico (`codigoMision`), construye la misión inmutable mediante `Mision.Builder` y la persiste en el repositorio (`RepositorioMisiones`) con estado `EstadoMision.PENDIENTE`.
5. **Paso 5 (Sistema):** El sistema notifica al `Operador Hídrico` la confirmación de la asignación, retornando en pantalla el `codigoMision` generado y el resumen del trayecto programado.

---

## 🔀 Flujos Alternos

### Flujo Alterno A: Drone sin batería suficiente ($\text{batería} < 35\%$)
* **Condición de activación:** En el **Paso 3**, el sistema detecta que el drone acuático seleccionado tiene un nivel de batería inferior al umbral mínimo operativo ($35\%$).
* **A1 (Sistema):** El sistema interrumpe el proceso de registro y lanza una excepción de validación (`IllegalStateException`).
* **A2 (Sistema):** El notificador informa al operador: *"Error: El drone '[ID]' cuenta con [X]% de batería, insuficiente para iniciar misión (mínimo requerido: 35%)."*
* **A3 (Sistema/Actor):** El sistema mantiene el estado previo sin persistir cambios y solicita al operador seleccionar un drone alternativo con carga apta.

### Flujo Alterno B: Zona de destino inválida o idéntica al punto de partida
* **Condición de activación:** En el **Paso 2**, el sistema detecta que el `puntoLlegada` no pertenece al catálogo oficial de zonas hídricas o es exactamente igual al `puntoPartida`.
* **B1 (Sistema):** El sistema interrumpe el flujo antes de comprometer la persistencia.
* **B2 (Sistema):** El notificador informa al operador: *"Error: La zona de destino '[puntoLlegada]' es inválida o coincide con el punto de partida."*
* **B3 (Sistema/Actor):** El sistema retorna el control al operador permitiendo corregir los puntos de navegación de la ruta.

---

## ⚖️ Reglas de Negocio (RN)

> ⚠️ *Regla DOSW:* Restricciones del dominio que aplican obligatoriamente **DURANTE** la ejecución de la lógica del sistema.

* **RN-01 (Autonomía mínima y exclusividad de flota):**  
  Un drone acuático únicamente puede ser asignado a una misión si su estado operativo actual es disponible (`disponible == true`), tiene una batería remanente mayor o igual al $35\%$ al momento de la asignación y no tiene ninguna otra misión en estado `PENDIENTE` o `EN_TRANSITO` (un drone no puede ejecutar más de una misión activa simultáneamente).

* **RN-02 (Coherencia topológica de ruta hídrica):**  
  El trayecto de transporte debe ser unidireccional y conectivo entre zonas diferenciadas: el `puntoPartida` y el `puntoLlegada` deben pertenecer obligatoriamente a la lista cerrada de zonas habilitadas de la ECI (`"Embalse Norte"`, `"Canal Central"`, `"Laguna Sur"`, `"Punto Ribereño Este"`, `"Laboratorio Hídrico"`) y no pueden ser idénticos entre sí (`puntoPartida != puntoLlegada`).
