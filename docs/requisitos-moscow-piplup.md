# 📋 Especificación de Requisitos y Priorización MoSCoW — AquaPort MVP (Piplup)

Este documento define los Requisitos Funcionales (RF) y Requisitos No Funcionales (RNF) para el nivel **Piplup (MVP)** del sistema AquaPort de la Escuela Colombiana de Ingeniería Julio Garavito (ECI).

---

## 🎯 1. Requisitos Funcionales (RF)

Estructura obligatoria: **Actor + Acción + Resultado Observable**.

### RF-01: Consulta de Drones Disponibles para Misión
* **Actor:** Operador Hídrico.
* **Acción:** Solicita la consulta del estado operativo de la flota acuática a través del sistema.
* **Resultado Observable:** El sistema presenta una lista ordenada de mayor a menor batería con los drones cuyo estado sea disponible (`disponible = true`) y cuya carga sea igual o superior al umbral operativo ($\ge 35\%$).
* **Categoría MoSCoW:** **Must Have**  
  * *Justificación:* Sin conocer qué drones están libres y con energía suficiente, el operador no puede ejecutar el proceso central de asignación de misiones en el campus.

---

### RF-02: Registro y Asignación Manual de Misión con Validación
* **Actor:** Operador Hídrico.
* **Acción:** Ingresa los datos de una misión (identificador, drone disponible seleccionado, punto de partida, punto de destino y tipo de carga) y confirma el registro.
* **Resultado Observable:** El sistema valida la integridad de los datos, asigna la misión en estado `PENDIENTE` en el repositorio e imprime la confirmación al operador con el código y detalles de la ruta.
* **Categoría MoSCoW:** **Must Have**  
  * *Justificación:* Constituye la función nuclear del MVP para formalizar el inicio del transporte de muestras y sensores entre zonas hídricas.

---

### RF-03: Filtrado de Drones por Zona Hídrica
* **Actor:** Operador Hídrico.
* **Acción:** Aplica un filtro seleccionando una zona hídrica específica del campus (ej. "Embalse Norte" o "Canal Central").
* **Resultado Observable:** El sistema retorna exclusivamente el subconjunto de drones que se encuentran anclados o posicionados en dicha zona geográfica.
* **Categoría MoSCoW:** **Should Have**  
  * *Justificación:* Facilita una asignación más ágil reduciendo tiempos de traslado vacío, pero el operador aún podría revisar manualmente las zonas en el listado general si no estuviera disponible en el primer despliegue.

---

## ⚙️ 2. Requisitos No Funcionales (RNF)

Todos los RNF cuentan con **métrica objetiva, contexto de carga y método/instrumento de verificación**.

### RNF-01: Rendimiento en Consulta de Flota
* **Descripción:** La consulta de drones disponibles y ordenamiento de batería debe responder en menos de **200 ms** bajo una flota en memoria de hasta **10 drones**.
* **Métrica concreta:** Latencia máxima de ejecución $\le 200\text{ ms}$ (percentil 99).
* **Método de verificación:** Prueba de rendimiento unitaria con JUnit 5 utilizando `assertTimeout(Duration.ofMillis(200), ...)`.
* **Categoría MoSCoW:** **Must Have**  
  * *Justificación:* El operador requiere retroalimentación inmediata en la consola para no bloquear la logística de despacho de emergencias o muestras perecederas.

---

### RNF-02: Integridad y Consistencia de Dominio (Fail-Fast)
* **Descripción:** El constructor/builder de misiones debe validar el $100\%$ de los campos obligatorios y rechazar drones no disponibles o estados nulos lanzando `IllegalStateException` en un tiempo inferior a **5 ms**.
* **Métrica concreta:** Cero ($0$) misiones persistidas con campos requeridos nulos, vacíos o drones en `disponible = false` ($100\%$ de detección en tiempo de construcción).
* **Método de verificación:** Suite de pruebas unitarias automatizadas con aserciones negativas (`assertThrows(IllegalStateException.class, ...)`).
* **Categoría MoSCoW:** **Must Have**  
  * *Justificación:* Evita estados corruptos en memoria que provocarían fallos catastróficos o pérdida de sincronización de los drones durante la navegación física.

---

### RNF-03: Modularidad y Desacoplamiento Arquitectónico (DIP)
* **Descripción:** El servicio de registro (`RegistradorMisiones`) debe estar desacoplado de la tecnología de persistencia, manteniendo una cobertura de pruebas unitarias $\ge 85\%$ sobre la lógica de registro utilizando un mock o implementación en memoria de `RepositorioMisiones` sin requerir base de datos externa ni arranque de red.
* **Métrica concreta:** Tiempo de ejecución de la suite completa de pruebas unitarias del repositorio y registrador inferior a **1.5 segundos**, con acoplamiento aferente dependiente solo de la interfaz.
* **Método de verificación:** Ejecución automatizada de pruebas sin dependencias externas mediante `mvn test` o script de pruebas en tiempo de compilación.
* **Categoría MoSCoW:** **Should Have**  
  * *Justificación:* Permite evolucionar el sistema hacia bases de datos relacionales en niveles futuros (*Prinplup*) sin refactorizar la lógica central de negocio del MVP.

---

## 📊 3. Matriz Resumen de Priorización MoSCoW

| ID | Tipo | Descripción Sintética | Prioridad MoSCoW | Justificación en una sola oración |
|---|---|---|---|---|
| **RF-01** | Funcional | Consulta de drones disponibles con batería $\ge 35\%$ | **Must Have** | Es imprescindible para que el operador conozca qué unidades pueden despegar antes de cualquier misión. |
| **RF-02** | Funcional | Registro y asignación manual de misión validada | **Must Have** | Es la funcionalidad central que justifica la existencia del sistema de transporte en el campus. |
| **RF-03** | Funcional | Filtrado de drones por zona hídrica | **Should Have** | Optimiza la toma de decisiones al priorizar drones cercanos, aunque la operación básica puede subsistir sin él. |
| **RNF-01** | No Funcional | Tiempo de respuesta en consulta $\le 200\text{ ms}$ | **Must Have** | Garantiza la fluidez requerida para que el operador atienda solicitudes sin bloqueos perceptibles en consola. |
| **RNF-02** | No Funcional | $100\%$ de validación de integridad inmutable al construir | **Must Have** | Impide que se inicien misiones con datos incompletos o drones inactivos que pongan en riesgo la carga. |
| **RNF-03** | No Funcional | Desacoplamiento de persistencia con pruebas $\le 1.5\text{ s}$ | **Should Have** | Facilita la evolución y mantenibilidad hacia persistencia externa en niveles futuros sin frenar el MVP. |

---

## 🚫 Fuera del Alcance del MVP (*Won't Have*)
Para mantener la simplicidad y el foco del nivel **Piplup**, las siguientes características se aplazan explícitamente para los niveles **Prinplup** y **Empoleon**:
1. **Asignación automática de drones por algoritmo de optimización.** *(Nivel Prinplup)*
2. **Navegación con rutas multi-etapa y recarga dinámica en estaciones intermedias.** *(Nivel Prinplup / Empoleon)*
3. **Telemetría GPS en tiempo real transmitida por sockets.** *(Nivel Empoleon)*
