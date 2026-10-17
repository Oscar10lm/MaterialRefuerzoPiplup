import subprocess
import re
import os

def generate_changelog():
    cmd = ["git", "log", "--pretty=format:%h|%s|%an|%ad", "--date=short"]
    result = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8")
    lines = result.stdout.strip().split("\n")

    categories = {
        "feat": ("🚀 Nuevas Funcionalidades (Features)", []),
        "fix": ("🐛 Correcciones de Errores (Fixes)", []),
        "refactor": ("♻️ Refactorización de Arquitectura", []),
        "test": ("🧪 Pruebas Unitarias y de Integración", []),
        "docs": ("📚 Documentación y Diagramas", []),
        "ci": ("⚙️ Integración Continua y Calidad", []),
        "chore": ("🔧 Tareas de Mantenimiento y Releases", [])
    }
    other_commits = []

    for line in lines:
        if not line or "|" not in line:
            continue
        parts = line.split("|", 3)
        commit_hash, subject, author, date = parts[0], parts[1], parts[2], parts[3]
        
        matched = False
        for prefix in categories.keys():
            if re.match(rf"^{prefix}(\([^\)]+\))?:", subject, re.IGNORECASE):
                categories[prefix][1].append((commit_hash, subject, author, date))
                matched = True
                break
        if not matched and not subject.startswith("Merge"):
            other_commits.append((commit_hash, subject, author, date))

    output = []
    output.append("# 📜 CHANGELOG — AquaPort Enterprise (ECI)\n")
    output.append("> Historial de cambios y arquitectura trazable generado automáticamente mediante Conventional Commits.\n")
    output.append("## [3.0.0] - Empoleon (AquaPort Enterprise: Red Multi-Embalse)\n")
    output.append("* **Escala:** 60 Drones en 4 Zonas Hídricas con Rutas Multi-Etapa y Waypoints.\n")
    output.append("* **Calidad:** JaCoCo $\ge 85\%$ Lines, $\ge 75\%$ Branches, SonarQube Rating A (0 Bugs, 0 Vuln, Deuda < 15m).\n")

    for key, (title, items) in categories.items():
        if items:
            output.append(f"### {title}\n")
            for chash, subj, auth, dt in items[:15]:
                output.append(f"- [`{chash}`] **{subj}** *(por {auth} el {dt})*")
            output.append("")

    if other_commits:
        output.append("### 📦 Otros Commits de Consolidación\n")
        for chash, subj, auth, dt in other_commits[:10]:
            output.append(f"- [`{chash}`] {subj} *({dt})*")
        output.append("")

    changelog_content = "\n".join(output)
    with open("CHANGELOG.md", "w", encoding="utf-8") as f:
        f.write(changelog_content)
    print("CHANGELOG.md generado exitosamente.")

if __name__ == "__main__":
    generate_changelog()
