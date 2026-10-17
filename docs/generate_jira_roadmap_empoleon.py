import os
from PIL import Image, ImageDraw, ImageFont

def generate_jira_roadmap():
    W, H = 1440, 960
    img = Image.new("RGB", (W, H), color="#F4F5F7")
    draw = ImageDraw.Draw(img)

    # Fonts
    try:
        font_nav = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 14)
        font_nav_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 14)
        font_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 22)
        font_subtitle = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
        font_sprint_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 15)
        font_sprint_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 12)
        font_item_title = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
        font_item_id = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_badge = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
        font_timeline = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
    except Exception:
        font_nav = font_nav_bold = font_title = font_subtitle = font_sprint_title = ImageFont.load_default()
        font_sprint_sub = font_item_title = font_item_id = font_badge = font_timeline = ImageFont.load_default()

    # 1. Atlassian Jira Top Navigation Bar
    draw.rectangle([(0, 0), (W, 52)], fill="#0747A6")
    draw.text((24, 15), "::: Jira Software", fill="#FFFFFF", font=font_nav_bold)

    nav_links = ["Your work", "Projects", "Filters", "Dashboards", "People", "Plans", "Roadmaps"]
    nx = 180
    for l in nav_links:
        is_active = (l == "Roadmaps")
        draw.text((nx, 17), l, fill="#FFFFFF" if is_active else "#B3D4FF", font=font_nav)
        if is_active:
            draw.rectangle([(nx, 48), (nx + 68, 52)], fill="#FFFFFF")
        nx += 95

    # Create Button
    draw.rounded_rectangle([(nx + 20, 11), (nx + 95, 41)], radius=4, fill="#0052CC")
    draw.text((nx + 36, 17), "Create", fill="#FFFFFF", font=font_nav_bold)

    # Search & Profile
    draw.rounded_rectangle([(W - 250, 12), (W - 80, 40)], radius=4, fill="#093276")
    draw.text((W - 235, 17), "Search roadmap...", fill="#79B2FF", font=font_subtitle)
    draw.ellipse([(W - 55, 12), (W - 27, 40)], fill="#00875A")
    draw.text((W - 47, 18), "PO", fill="#FFFFFF", font=font_item_id)

    # 2. Project Subheader & Breadcrumbs
    draw.rectangle([(0, 52), (W, 142)], fill="#FFFFFF")
    draw.text((24, 62), "Projects  /  AquaPort Enterprise (ECI)  /  Roadmap & Releases", fill="#6B778C", font=font_subtitle)
    draw.text((24, 82), "Enterprise Roadmap · Planificación de 3 Sprints (Empoleon v3)", fill="#172B4D", font=font_title)
    
    # Roadmap Meta Banner
    draw.text((820, 88), "Capacidad: 4 Devs (320h/sprint)  |  Total: 72 Story Points  |  Release v3.0", fill="#0052CC", font=font_nav_bold)

    # 3. Timeline Scale Header (Weeks 1 to 6 - Oct/Nov 2026)
    ty = 155
    draw.rectangle([(0, ty), (W, ty + 36)], fill="#EBECF0")
    draw.text((24, ty + 10), "SPRINTS Y ÉPICAS ENTERPRISE", fill="#5E6C84", font=font_timeline)
    
    # Timeline weeks columns
    time_x = 640
    weeks = [
        ("Sem 1 (01-07 Oct)", 125), ("Sem 2 (08-14 Oct)", 125),
        ("Sem 3 (15-21 Oct)", 125), ("Sem 4 (22-28 Oct)", 125),
        ("Sem 5 (29 Oct-04 Nov)", 135), ("Sem 6 (05-11 Nov)", 135)
    ]
    cur_tx = time_x
    for wname, wwidth in weeks:
        draw.line([(cur_tx, ty), (cur_tx, H - 40)], fill="#DFE1E6", width=1)
        draw.text((cur_tx + 10, ty + 10), wname, fill="#42526E", font=font_timeline)
        cur_tx += wwidth

    # 4. SPRINT SECTIONS & GANTT BARS
    # -------------------------------------------------------------
    # SPRINT 1
    # -------------------------------------------------------------
    sy = 205
    # Sprint Header Box
    draw.rounded_rectangle([(24, sy), (620, sy + 62)], radius=6, fill="#FFFFFF", outline="#DFE1E6", width=1)
    draw.text((36, sy + 10), "Sprint 1: Arquitectura por Capas y Patrones Enterprise", fill="#172B4D", font=font_sprint_title)
    draw.text((36, sy + 32), "Goal: Desacoplar dominio (SOLID) e implementar Chain, Decorator y Adapter.", fill="#5E6C84", font=font_sprint_sub)
    draw.text((540, sy + 10), "24 SP", fill="#0052CC", font=font_sprint_title)
    draw.rounded_rectangle([(530, sy + 32), (610, sy + 52)], radius=3, fill="#E3FCEF")
    draw.text((542, sy + 36), "COMPLETED", fill="#006644", font=font_badge)

    # Sprint 1 Gantt Bar (Sem 1 & 2: from x=640 to x=890)
    draw.rounded_rectangle([(645, sy + 12), (885, sy + 48)], radius=6, fill="#36B37E")
    draw.text((660, sy + 22), "Sprint 1 Execution (100% DONE - 24/24 SP)", fill="#FFFFFF", font=font_sprint_sub)

    # Sprint 1 Issues
    s1_issues = [
        ("EMP-01", "Dominio puro sin dependencias externas (Java Puro)", "5 SP", "DONE"),
        ("EMP-02", "Chain of Responsibility: validación de misiones en cadena", "5 SP", "DONE"),
        ("EMP-03", "Decorator: telemetría enriquecida y sobre cifrado AES-256", "5 SP", "DONE"),
        ("EMP-04", "Adapter: AdaptadorAPIHidrica para aislar API meteorológica", "5 SP", "DONE"),
        ("EMP-05", "Auditoría automatizada de clases <= 150 y métodos <= 15 líneas", "4 SP", "DONE")
    ]
    iy = sy + 70
    for cid, cname, csp, cst in s1_issues:
        draw.rectangle([(24, iy), (620, iy + 30)], fill="#FFFFFF")
        draw.line([(24, iy + 30), (620, iy + 30)], fill="#F4F5F7", width=1)
        draw.text((36, iy + 7), cid, fill="#0052CC", font=font_item_id)
        draw.text((105, iy + 7), cname, fill="#172B4D", font=font_item_title)
        draw.text((545, iy + 7), csp, fill="#6B778C", font=font_item_id)
        draw.rounded_rectangle([(580, iy + 6), (615, iy + 24)], radius=3, fill="#E3FCEF")
        draw.text((585, iy + 9), "DONE", fill="#006644", font=font_badge)
        iy += 32

    # -------------------------------------------------------------
    # SPRINT 2
    # -------------------------------------------------------------
    sy2 = iy + 16
    draw.rounded_rectangle([(24, sy2), (620, sy2 + 62)], radius=6, fill="#FFFFFF", outline="#DFE1E6", width=1)
    draw.text((36, sy2 + 10), "Sprint 2: Rutas Multi-Etapa y Coordinación Zonal", fill="#172B4D", font=font_sprint_title)
    draw.text((36, sy2 + 32), "Goal: Orquestación con waypoints, reasignación <= 3s y 4 zonas.", fill="#5E6C84", font=font_sprint_sub)
    draw.text((540, sy2 + 10), "26 SP", fill="#0052CC", font=font_sprint_title)
    draw.rounded_rectangle([(525, sy2 + 32), (610, sy2 + 52)], radius=3, fill="#DEEBFF")
    draw.text((535, sy2 + 36), "IN PROGRESS", fill="#0747A6", font=font_badge)

    # Sprint 2 Gantt Bar (Sem 3 & 4: from x=890 to x=1140)
    draw.rounded_rectangle([(895, sy2 + 12), (1135, sy2 + 48)], radius=6, fill="#0052CC")
    draw.text((910, sy2 + 22), "Sprint 2 Execution (85% Progress - 22/26 SP)", fill="#FFFFFF", font=font_sprint_sub)

    # Sprint 2 Issues
    s2_issues = [
        ("EMP-06", "Planificador de rutas multi-etapa con traspaso en waypoints", "8 SP", "DONE"),
        ("EMP-07", "Reasignación automática en caliente ante fallo en boya (<=3s)", "5 SP", "DONE"),
        ("EMP-08", "Cadena de custodia inmutable de muestras con SHA-256", "5 SP", "DONE"),
        ("EMP-09", "Coordinación interzonal y gestión de esclusas en canales", "5 SP", "IN PROGRESS"),
        ("EMP-10", "Telemetría streaming MQTTS y mapa interactivo 60 drones", "3 SP", "IN PROGRESS")
    ]
    iy2 = sy2 + 70
    for cid, cname, csp, cst in s2_issues:
        draw.rectangle([(24, iy2), (620, iy2 + 30)], fill="#FFFFFF")
        draw.line([(24, iy2 + 30), (620, iy2 + 30)], fill="#F4F5F7", width=1)
        draw.text((36, iy2 + 7), cid, fill="#0052CC", font=font_item_id)
        draw.text((105, iy2 + 7), cname, fill="#172B4D", font=font_item_title)
        draw.text((545, iy2 + 7), csp, fill="#6B778C", font=font_item_id)
        badge_bg = "#E3FCEF" if cst == "DONE" else "#DEEBFF"
        badge_fg = "#006644" if cst == "DONE" else "#0747A6"
        draw.rounded_rectangle([(575, iy2 + 6), (615, iy2 + 24)], radius=3, fill=badge_bg)
        draw.text((580, iy2 + 9), "DONE" if cst == "DONE" else "PROG", fill=badge_fg, font=font_badge)
        iy2 += 32

    # -------------------------------------------------------------
    # SPRINT 3
    # -------------------------------------------------------------
    sy3 = iy2 + 16
    draw.rounded_rectangle([(24, sy3), (620, sy3 + 62)], radius=6, fill="#FFFFFF", outline="#DFE1E6", width=1)
    draw.text((36, sy3 + 10), "Sprint 3: Calidad Enterprise, JaCoCo y SonarQube", fill="#172B4D", font=font_sprint_title)
    draw.text((36, sy3 + 32), "Goal: JaCoCo >= 85% Lines / 75% Branches, SonarQube 0 deuda.", fill="#5E6C84", font=font_sprint_sub)
    draw.text((540, sy3 + 10), "22 SP", fill="#0052CC", font=font_sprint_title)
    draw.rounded_rectangle([(530, sy3 + 32), (610, sy3 + 52)], radius=3, fill="#EAE6FF")
    draw.text((542, sy3 + 36), "PLANNED", fill="#403294", font=font_badge)

    # Sprint 3 Gantt Bar (Sem 5 & 6: from x=1140 to x=1410)
    draw.rounded_rectangle([(1145, sy3 + 12), (1395, sy3 + 48)], radius=6, fill="#6554C0")
    draw.text((1160, sy3 + 22), "Sprint 3 Quality & Release (22 SP)", fill="#FFFFFF", font=font_sprint_sub)

    # Sprint 3 Issues
    s3_issues = [
        ("EMP-11", "Quality Gate JaCoCo: Cobertura >= 85% líneas / 75% ramas", "5 SP", "TO DO"),
        ("EMP-12", "Collector O(N): BateriaPromedioCollector en sola pasada", "5 SP", "TO DO"),
        ("EMP-13", "SonarQube: 0 bugs, 0 vulnerabilidades, deuda < 15 min", "5 SP", "TO DO"),
        ("EMP-14", "Stress Testing de concurrencia y validación de fallos Mockito", "4 SP", "TO DO"),
        ("EMP-15", "Release v3.0 Enterprise y certificación de DoD", "3 SP", "TO DO")
    ]
    iy3 = sy3 + 70
    for cid, cname, csp, cst in s3_issues:
        draw.rectangle([(24, iy3), (620, iy3 + 30)], fill="#FFFFFF")
        draw.line([(24, iy3 + 30), (620, iy3 + 30)], fill="#F4F5F7", width=1)
        draw.text((36, iy3 + 7), cid, fill="#0052CC", font=font_item_id)
        draw.text((105, iy3 + 7), cname, fill="#172B4D", font=font_item_title)
        draw.text((545, iy3 + 7), csp, fill="#6B778C", font=font_item_id)
        draw.rounded_rectangle([(575, iy3 + 6), (615, iy3 + 24)], radius=3, fill="#DFE1E6")
        draw.text((580, iy3 + 9), "TODO", fill="#42526E", font=font_badge)
        iy3 += 32

    # 5. Bottom Milestone Bar
    draw.rectangle([(0, H - 42), (W, H)], fill="#FFFFFF")
    draw.line([(0, H - 42), (W, H - 42)], fill="#DFE1E6", width=1)
    draw.text((24, H - 28), "AquaPort Enterprise v3.0 Milestone: Ready for Deployment (11 Nov 2026)", fill="#006644", font=font_nav_bold)
    draw.text((W - 380, H - 28), "Jira Software Cloud • Enterprise Release Track", fill="#6B778C", font=font_subtitle)

    output_path = os.path.join("docs", "jira-roadmap-empoleon.png")
    img.save(output_path, "PNG", dpi=(300, 300))
    print(f"Roadmap de Jira generado en: {output_path}")

if __name__ == "__main__":
    generate_jira_roadmap()
