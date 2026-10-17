# 🚀 AquaPort v2 (Prinplup) — Release Notes v2.0.0

## 📦 Identificación del Release
* **Versión Oficial:** `v2.0.0`
* **Codename:** `Prinplup — Flota Autónoma con Alertas`
* **Fecha de Estabilización:** Octubre 2026
* **Rama de Release:** `release/v2.0`
* **Rama de Destino:** `main`

---

## 🎯 Resumen Ejecutivo de la Versión v2.0.0

La versión **2.0.0 (Prinplup)** representa la evolución de AquaPort desde un prototipo MVP supervisado manualmente hacia una plataforma de gestión ambiental hidrológica completamente autónoma, diseñada para la Escuela Colombiana de Ingeniería Julio Garavito (ECI).

### 🌟 Capacidades Clave Entregadas
1. **Flota Acuática Ampliada a 15 Drones de 3 Tipos:**
   - 🚤 `DroneSuperficial`: Optimizado para aguas tranquilas y transferencias rápidas (hasta 500g).
   - 🚢 `DroneSemisumergido`: Navegación resiliente en aguas agitadas y oleaje moderado (hasta 1.5kg).
   - 🤿 `DroneBuceador`: Inspección y muestreo en profundidad acuática (hasta 300g).
2. **Motor de Selección Inteligente (Patrón Strategy):**
   - Selección dinámica basada en `MayorBateriaStrategy`, `PrioridadCriticaStrategy` y `ZonaCercanaStrategy` sin acoplamiento por `instanceof`.
3. **Distribución Reactiva de Alertas (Patrón Observer):**
   - Notificación instantánea a `CentroControlObserver` y `TecnicoMantenimientoObserver` ante detección de estados `FALLO` o contingencias críticas.
4. **Instanciación Polimórfica (Patrón Factory Method):**
   - `FabricaDrones` desacopla la creación de modelos garantizando el Principio Open/Closed (OCP).
5. **Quality Gate y Confiabilidad:**
   - JaCoCo: **99.4%** de cobertura en líneas y **97.1%** en ramas (superando ampliamente el Quality Gate de 80%).
   - SonarQube: **0 Bugs, 0 Vulnerabilidades, 0 Minutos de Deuda Técnica y 0.0% Duplicación** (Rating A en todos los indicadores).
   - Suite de Pruebas: **70 pruebas automatizadas** en JUnit 5 y Mockito pasando al 100%.

---

## 📋 Lista de Verificación de Release (Release Checklist)
- [x] Compilación limpia sin advertencias (`mvn clean compile`).
- [x] 70 pruebas unitarias y de integración superadas al 100% (`mvn test`).
- [x] Quality Gate de JaCoCo verificado en Maven (`jacoco:check` exitoso).
- [x] SonarQube: 4 indicadores en verde (Dashboard verificado).
- [x] Flujo de GitFlow completado: ramas de features integradas en `develop`, estabilizadas en `release/v2.0` y promovidas a `main` con tag `v2.0.0`.
- [x] Documentación oficial sincronizada en `Prinplup/README.md` y artefactos de arquitectura en `/docs`.
