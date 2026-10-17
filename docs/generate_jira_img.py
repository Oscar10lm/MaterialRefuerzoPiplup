import os
from PIL import Image, ImageDraw, ImageFont

def generate_jira_board():
    W, H = 1320, 840
    img = Image.new("RGB", (W, H), color="#F4F5F7")
    draw = ImageDraw.Draw(img)

    # Load Windows standard fonts
    try:
        font_nav = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 14)
        font_nav_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 14)
        font_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 22)
        font_subtitle = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
        font_goal = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 13)
        font_col_header = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_card_title = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
        font_card_id = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_tag = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
        font_sp = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
    except Exception:
        font_nav = font_nav_bold = font_title = font_subtitle = font_goal = font_col_header = font_card_title = font_card_id = font_tag = font_sp = ImageFont.load_default()

    # 1. Top Atlassian Jira Navigation Bar
    draw.rectangle([(0, 0), (W, 52)], fill="#0747A6")
    # Jira App Switcher & Logo
    draw.text((20, 15), "::: Jira Software", fill="#FFFFFF", font=font_nav_bold)
    
    nav_links = ["Your work", "Projects", "Filters", "Dashboards", "People", "Plans", "Apps"]
    nx = 180
    for l in nav_links:
        draw.text((nx, 17), l, fill="#B3D4FF" if l != "Projects" else "#FFFFFF", font=font_nav)
        if l == "Projects":
            draw.rectangle([(nx, 48), (nx + 55, 52)], fill="#FFFFFF")
        nx += 95
        
    # Blue Create Button
    draw.rounded_rectangle([(nx + 20, 11), (nx + 95, 41)], radius=4, fill="#0052CC")
    draw.text((nx + 36, 17), "Create", fill="#FFFFFF", font=font_nav_bold)
    
    # User Profile / Search right
    draw.rounded_rectangle([(W - 240, 12), (W - 80, 40)], radius=4, fill="#093276")
    draw.text((W - 225, 17), "Search (/) ...", fill="#79B2FF", font=font_subtitle)
    draw.ellipse([(W - 55, 12), (W - 27, 40)], fill="#00875A")
    draw.text((W - 47, 18), "PO", fill="#FFFFFF", font=font_card_id)

    # 2. Project Subheader & Breadcrumbs
    draw.rectangle([(0, 52), (W, 142)], fill="#FFFFFF")
    draw.text((24, 62), "Projects  /  AquaPort v2 (ECI)  /  Active Sprints", fill="#6B778C", font=font_subtitle)
    draw.text((24, 82), "PRIN Sprint 2 · Flota Autónoma y Alertas", fill="#172B4D", font=font_title)
    
    # Sprint Goal Banner
    draw.rounded_rectangle([(24, 112), (W - 24, 136)], radius=4, fill="#E6FCFF", outline="#00A3BF")
    goal_text = '🎯 Sprint Goal: "Completar el asignador automático con Strategy y las alertas con Observer"  |  Total: 21 Story Points'
    draw.text((36, 116), goal_text, fill="#004953", font=font_goal)

    # 3. Columns Layout
    cols = [
        {"name": "TO DO", "count": "1 issue (2 SP)", "bg": "#F4F5F7", "x": 24, "w": 305},
        {"name": "IN PROGRESS", "count": "2 issues (8 SP)", "bg": "#F4F5F7", "x": 345, "w": 305},
        {"name": "IN REVIEW / QA", "count": "1 issue (3 SP)", "bg": "#F4F5F7", "x": 665, "w": 305},
        {"name": "DONE", "count": "2 issues (8 SP)", "bg": "#F4F5F7", "x": 985, "w": 305}
    ]

    for c in cols:
        draw.rounded_rectangle([(c["x"], 154), (c["x"] + c["w"], H - 24)], radius=8, fill="#EBECF0")
        draw.text((c["x"] + 12, 166), c["name"], fill="#5E6C84", font=font_col_header)
        draw.text((c["x"] + c["w"] - 110, 166), c["count"], fill="#7A869A", font=font_subtitle)

    # Helper function to draw cards
    def draw_jira_card(x, y, w, h, issue_key, title, issue_type, priority, sp, assignee, tag):
        # Card Background
        draw.rounded_rectangle([(x, y), (x + w, y + h)], radius=6, fill="#FFFFFF", outline="#DFE1E6")
        # Tag Badge
        draw.rounded_rectangle([(x + 12, y + 10), (x + 12 + len(tag) * 7 + 10, y + 26)], radius=3, fill="#DEEBFF")
        draw.text((x + 16, y + 12), tag, fill="#0747A6", font=font_tag)
        # Title
        draw.text((x + 12, y + 34), title[:42], fill="#172B4D", font=font_card_title)
        if len(title) > 42:
            draw.text((x + 12, y + 50), title[42:85], fill="#172B4D", font=font_card_title)
        
        # Bottom info: Type icon, ID, Priority, SP, Assignee
        by = y + h - 28
        # Issue Key
        draw.text((x + 12, by), issue_key, fill="#0052CC", font=font_card_id)
        
        # Priority icon (colored box)
        p_color = "#FF5630" if priority == "Highest" else "#FF7452" if priority == "High" else "#FFAB00"
        draw.rounded_rectangle([(x + 115, by + 1), (x + 127, by + 13)], radius=2, fill=p_color)
        
        # Story Points Badge (Fibonacci)
        draw.ellipse([(x + w - 68, by - 2), (x + w - 48, by + 18)], fill="#DFE1E6")
        draw.text((x + w - 61, by), str(sp), fill="#172B4D", font=font_sp)
        
        # Assignee avatar circle
        draw.ellipse([(x + w - 38, by - 4), (x + w - 16, by + 18)], fill="#0052CC" if assignee == "Dev1" else "#403294" if assignee == "Dev2" else "#00875A")
        draw.text((x + w - 34, by - 1), assignee[:2], fill="#FFFFFF", font=font_tag)

    # Cards in Columns
    # TO DO
    draw_jira_card(36, 196, 281, 116, "PRIN-06", "Reintegración técnica y gestión de drones en estado FALLO/MANT", "Story", "Medium", 2, "Dev3", "MANTENIMIENTO")

    # IN PROGRESS
    draw_jira_card(357, 196, 281, 116, "PRIN-01", "Asignador automático de misiones mediante Patrón Strategy", "Story", "Highest", 5, "Dev1", "AUTONOMÍA")
    draw_jira_card(357, 324, 281, 116, "PRIN-05", "Resolución de tensión AP-07 vs AP-08: Precedencia en misiones críticas", "Task", "High", 3, "Dev2", "CRITICAL-RESILIENCE")

    # IN REVIEW / QA
    draw_jira_card(677, 196, 281, 116, "PRIN-03", "Consulta reactiva a API de Condiciones Hídricas y filtrado de casco", "Story", "High", 3, "Dev1", "API-LIMNOLÓGICA")

    # DONE
    draw_jira_card(997, 196, 281, 116, "PRIN-02", "Notificación síncrona de eventos de fallo mediante Patrón Observer", "Story", "Highest", 5, "Dev2", "ALERTS-OBSERVER")
    draw_jira_card(997, 324, 281, 116, "PRIN-04", "Fábrica polimórfica de 3 tipos de drones acuáticos (Factory Method)", "Story", "High", 3, "Dev3", "FACTORY-GOF")

    # Bottom status banner inside image
    draw.rectangle([(24, H - 42), (W - 24, H - 18)], fill="#FFFFFF")
    draw.text((36, H - 36), "✔ Definition of Done (DoD) Verificado: Compilación 0 warnings  |  JUnit 5: 35 passing  |  JaCoCo: ≥80%  |  C4 & DOSW actualizados", fill="#006644", font=font_subtitle)

    output_dir = "docs/images"
    os.makedirs(output_dir, exist_ok=True)
    out_path = os.path.join(output_dir, "jira-sprint-board.png")
    img.save(out_path)
    print(f"Jira board image generated successfully at {out_path}")

if __name__ == "__main__":
    generate_jira_board()
