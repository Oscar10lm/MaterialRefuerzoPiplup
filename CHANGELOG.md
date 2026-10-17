# 📜 CHANGELOG — AquaPort Enterprise (ECI)

> Historial de cambios y arquitectura trazable generado automáticamente mediante Conventional Commits.

## [3.0.0] - Empoleon (AquaPort Enterprise: Red Multi-Embalse)

* **Escala:** 60 Drones en 4 Zonas Hídricas con Rutas Multi-Etapa y Waypoints.

* **Calidad:** JaCoCo $\ge 85\%$ Lines, $\ge 75\%$ Branches, SonarQube Rating A (0 Bugs, 0 Vuln, Deuda < 15m).

### 🚀 Nuevas Funcionalidades (Features)

- [`34529cb`] **feat(empoleon-cli): integrar patrones Chain, Decorator y Adapter en MainEmpoleon** *(por Oscar10lm el 2026-10-17)*
- [`11fc940`] **feat(empoleon-adapter): implementar AdaptadorAPIHidrica para aislar API externa** *(por Oscar10lm el 2026-10-17)*
- [`5113a40`] **feat(empoleon-decorator): implementar Decorator para telemetria y cifrado** *(por Oscar10lm el 2026-10-17)*
- [`7cf89bb`] **feat(empoleon-chain): implementar Chain of Responsibility en validaciones** *(por Oscar10lm el 2026-10-17)*
- [`208bcae`] **feat(empoleon-cli): implementar MainEmpoleon y configurar JaCoCo Quality Gate** *(por Oscar10lm el 2026-10-17)*
- [`7a4cd39`] **feat(empoleon-service): calcular carga de trabajo y eficiencia zonal** *(por Oscar10lm el 2026-10-17)*
- [`cb73f48`] **feat(empoleon-collector): implementar BateriaPromedioCollector de complejidad O(N) en una sola pasada** *(por Oscar10lm el 2026-10-17)*
- [`1cd9049`] **feat(empoleon-flota): implementar Flota60Factory con distribucion zonal balanceada de 60 drones** *(por Oscar10lm el 2026-10-17)*
- [`51ddb6c`] **feat(empoleon-model): modelar waypoints georreferenciados y tramos de ruta con traspasos** *(por Oscar10lm el 2026-10-17)*
- [`d97e6a9`] **feat(empoleon-model): estructurar topologia de 4 zonas hidricas institucionales de la ECI** *(por Oscar10lm el 2026-10-17)*
- [`b23b2f2`] **feat(empoleon-model): incorporar enums de dominio EstadoDrone, Prioridad, TipoCarga y EstadoMision** *(por Oscar10lm el 2026-10-17)*
- [`c12ebbd`] **feat(empoleon-model): definir entidades base DroneAcuatico y familias de drones especializados** *(por Oscar10lm el 2026-10-17)*
- [`57c5876`] **feat(prinplup-tdd): implementar Reto 12 TDD con Mockito en AsignadorAutomaticoMisiones** *(por Oscar10lm el 2026-10-17)*
- [`f347a48`] **feat(docs): actualizar diagrama de casos de uso v2 con actores nuevos, include y extend (Reto 10)** *(por Oscar10lm el 2026-10-13)*
- [`f9a9a34`] **feat(ux): disenar componentes 6 estados tarjeta, 5 estados boton, flujo 3 pantallas y leyes Fitts/Hick (Reto 08)** *(por Oscar10lm el 2026-10-12)*

### 🐛 Correcciones de Errores (Fixes)

- [`90cb923`] **fix(hotfix): corregir validacion preventiva de bateria critica en v1.0** *(por Oscar10lm el 2026-10-12)*
- [`d38d9d0`] **fix: prevenir posible NullPointerException al validar rutas en ValidadorMision (Sonar java:S2259)** *(por Oscar10lm el 2026-10-12)*

### ♻️ Refactorización de Arquitectura

- [`5088573`] **refactor(sonarqube): resolver bugs, smells y Quality Gate v2 (Reto 14)** *(por Oscar10lm el 2026-10-17)*
- [`e3bad06`] **refactor: centralizar zonas hidricas en clase de constantes ZonasCampus (Sonar java:S1192 y java:S1118)** *(por Oscar10lm el 2026-10-12)*
- [`2954081`] **refactor: reemplazar System.out por Logger en NotificadorOperador (Sonar java:S106)** *(por Oscar10lm el 2026-10-12)*
- [`b15d136`] **refactor: eliminar import no utilizado java.util.Objects en Mision (Sonar java:S1128)** *(por Oscar10lm el 2026-10-12)*
- [`b3e113e`] **refactor: extraer helpers en ValidadorMision y reutilizar metodos probados** *(por Oscar10lm el 2026-10-12)*

### 🧪 Pruebas Unitarias y de Integración

- [`348d980`] **test(empoleon-arch): auditar arquitectura por capas y reporte de dependencias** *(por Oscar10lm el 2026-10-17)*
- [`e183948`] **test(empoleon-solid): auditar aislamiento de dominio e inyeccion por constructor** *(por Oscar10lm el 2026-10-17)*
- [`7b7fdab`] **test(empoleon-arch): auditar limites de lineas y dominio desacoplado** *(por Oscar10lm el 2026-10-17)*
- [`fae2f8b`] **test(empoleon-streams): validar streams y colector en una sola pasada** *(por Oscar10lm el 2026-10-17)*
- [`ff9682f`] **test(prinplup): agregar pruebas unitarias completas para ConsultorFlotaAvanzado (Reto 01)** *(por Oscar10lm el 2026-10-12)*
- [`243db95`] **test: ampliar pruebas unitarias de ValidadorMision para alcanzar 90.9% de line coverage** *(por Oscar10lm el 2026-10-12)*
- [`d4531e0`] **test: escribir pruebas Red de ValidadorMision (bateria, disponibilidad, punto de llegada y zona)** *(por Oscar10lm el 2026-10-12)*

### 📚 Documentación y Diagramas

- [`b34d2f9`] **docs(changelog): actualizar historial tras fusion de retos 13 y 14** *(por Oscar10lm el 2026-10-17)*
- [`2e4ca42`] **docs(empoleon): integrar retos 13 y 14 en README y actualizar CHANGELOG** *(por Oscar10lm el 2026-10-17)*
- [`a85372b`] **docs(empoleon-qa): configurar quality gate y certificar 6 indicadores sonarqube** *(por Oscar10lm el 2026-10-17)*
- [`83f29ae`] **docs: actualizar CHANGELOG tras merge de Retos 09 y 10 Empoleon** *(por Oscar10lm el 2026-10-17)*
- [`ebe306d`] **docs(empoleon): integrar retos 09 y 10 en README y actualizar CHANGELOG** *(por Oscar10lm el 2026-10-17)*
- [`794697e`] **docs(empoleon-cu): modelar casos de uso con 5 flujos alternos extendidos** *(por Oscar10lm el 2026-10-17)*
- [`c3cec6d`] **docs(empoleon-jira): planificar roadmap 3 sprints y retrospectiva en Jira** *(por Oscar10lm el 2026-10-17)*
- [`104d53c`] **docs: actualizar CHANGELOG tras merge de Retos 07 y 08 Empoleon** *(por Oscar10lm el 2026-10-17)*
- [`e12ab89`] **docs(empoleon): integrar retos 07 y 08 en README y actualizar CHANGELOG** *(por Oscar10lm el 2026-10-17)*
- [`967ee80`] **docs(empoleon-ux): construir design system tokens 6 pantallas y prototipo HTML** *(por Oscar10lm el 2026-10-17)*
- [`3dd4fd9`] **docs(empoleon-dosw): completar plantilla DOSW AP-15 mision multi-etapa** *(por Oscar10lm el 2026-10-17)*
- [`d95a4a7`] **docs: actualizar CHANGELOG tras merge de Retos 05 y 06 Empoleon** *(por Oscar10lm el 2026-10-17)*
- [`dfbb62a`] **docs(empoleon): integrar retos 05 y 06 en README y actualizar CHANGELOG** *(por Oscar10lm el 2026-10-17)*
- [`a26d8e3`] **docs(empoleon-rf): documentar 5 RF, 5 RNF, MoSCoW y analisis de tensiones** *(por Oscar10lm el 2026-10-17)*
- [`db67ab6`] **docs(empoleon-c4): disenar diagrama C4 nivel 2 de contenedores** *(por Oscar10lm el 2026-10-17)*

### ⚙️ Integración Continua y Calidad

- [`3218a49`] **ci(empoleon-gitflow): configurar hooks de Conventional Commits y pre-push** *(por Oscar10lm el 2026-10-17)*

### 🔧 Tareas de Mantenimiento y Releases

- [`92fa881`] **chore(merge): fusionar feature/empoleon-qa-architecture en develop** *(por Oscar10lm el 2026-10-17)*
- [`3bc913a`] **chore(release): consolidar checklist y notas oficiales de estabilizacion para release v2.0.0** *(por Oscar10lm el 2026-10-17)*
- [`f5affc7`] **chore(release): preparar version v2.0.0 y estabilizacion de release** *(por Oscar10lm el 2026-10-12)*
- [`c58716b`] **chore: configurar JaCoCo 0.8.11 en el pom.xml de Piplup** *(por Oscar10lm el 2026-10-12)*
- [`1ed68aa`] **chore: configurar Maven con JUnit 5 para pruebas del modulo Piplup** *(por Oscar10lm el 2026-10-12)*
- [`627ea7b`] **chore: configurar .gitignore para proyectos Java e IDEs** *(por Oscar10lm el 2026-10-12)*

### 📦 Otros Commits de Consolidación

- [`434c4a1`] build: configurar sonar-maven-plugin y propiedades de analisis SonarQube en pom.xml *(2026-10-12)*
- [`eb32436`] Initial commit *(2026-10-12)*
