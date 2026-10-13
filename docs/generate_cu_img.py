import os
from PIL import Image, ImageDraw, ImageFont

def generate_use_case_diagram():
    W, H = 1380, 880
    img = Image.new("RGB", (W, H), color="#FFFFFF")
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 18)
        font_actor = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 13)
        font_actor_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
        font_uc = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_uc_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
        font_rel = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
        font_note = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 10)
    except Exception:
        font_title = font_actor = font_actor_sub = font_uc = font_uc_sub = font_rel = font_note = ImageFont.load_default()

    # 1. System Boundary Rectangle
    rx, ry, rw, rh = 280, 50, 780, 790
    draw.rounded_rectangle([(rx, ry), (rx + rw, ry + rh)], radius=12, fill="#F8FAFC", outline="#64748B", width=2)
    draw.text((rx + 30, ry + 16), "Sistema AquaPort v2 (Prinplup) — Fleet Control Station", fill="#0F172A", font=font_title)

    # Helper to draw stick-figure actor
    def draw_actor(x, y, name, sub="", color="#084298"):
        # Head
        draw.ellipse([(x - 14, y - 40), (x + 14, y - 12)], outline=color, width=2, fill="#E0F2FE")
        # Body
        draw.line([(x, y - 12), (x, y + 20)], fill=color, width=2)
        # Arms
        draw.line([(x - 22, y + 2), (x + 22, y + 2)], fill=color, width=2)
        # Legs
        draw.line([(x, y + 20), (x - 16, y + 48)], fill=color, width=2)
        draw.line([(x, y + 20), (x + 16, y + 48)], fill=color, width=2)
        # Label
        lines = name.split("\n")
        ly = y + 55
        for l in lines:
            w_box = draw.textlength(l, font=font_actor)
            draw.text((x - w_box / 2, ly), l, fill=color, font=font_actor)
            ly += 16
        if sub:
            w_sub = draw.textlength(sub, font=font_actor_sub)
            draw.text((x - w_sub / 2, ly), sub, fill="#475569", font=font_actor_sub)

    # Helper to draw use case ellipse
    def draw_usecase(cx, cy, rx, ry, title_lines, is_core=False):
        fill_col = "#BAE6FD" if is_core else "#E0F2FE"
        border_col = "#0284C7" if not is_core else "#0369A1"
        border_w = 3 if is_core else 2
        draw.ellipse([(cx - rx, cy - ry), (cx + rx, cy + ry)], fill=fill_col, outline=border_col, width=border_w)
        
        total_h = len(title_lines) * 16
        start_y = cy - total_h / 2
        for idx, line in enumerate(title_lines):
            f = font_uc if idx == 0 else font_uc_sub
            c = "#0F172A"
            w_line = draw.textlength(line, font=f)
            draw.text((cx - w_line / 2, start_y + idx * 16), line, fill=c, font=f)

    # Helper to draw dashed arrow with label
    def draw_dashed_arrow(p1, p2, text, note=""):
        # Draw dashed line
        dx = p2[0] - p1[0]
        dy = p2[1] - p1[1]
        dist = (dx**2 + dy**2)**0.5
        steps = int(dist / 10)
        for i in range(0, steps, 2):
            t1 = i / steps
            t2 = min((i + 1) / steps, 1.0)
            x_a = p1[0] + dx * t1
            y_a = p1[1] + dy * t1
            x_b = p1[0] + dx * t2
            y_b = p1[1] + dy * t2
            draw.line([(x_a, y_a), (x_b, y_b)], fill="#334155", width=2)
        
        # Arrowhead at p2
        ux = dx / dist
        uy = dy / dist
        arrow_sz = 10
        # perpendicular
        draw.polygon([
            (p2[0], p2[1]),
            (p2[0] - ux * arrow_sz + uy * (arrow_sz / 2), p2[1] - uy * arrow_sz - ux * (arrow_sz / 2)),
            (p2[0] - ux * arrow_sz - uy * (arrow_sz / 2), p2[1] - uy * arrow_sz + ux * (arrow_sz / 2))
        ], fill="#334155")
        
        # Midpoint label
        mx = (p1[0] + p2[0]) / 2
        my = (p1[1] + p2[1]) / 2 - 14
        w_t = draw.textlength(text, font=font_rel)
        draw.rectangle([(mx - w_t / 2 - 4, my - 2), (mx + w_t / 2 + 4, my + 14)], fill="#FFFFFF", outline="#CBD5E1")
        draw.text((mx - w_t / 2, my), text, fill="#0284C7", font=font_rel)
        if note:
            w_n = draw.textlength(note, font=font_note)
            draw.text((mx - w_n / 2, my + 16), note, fill="#DC2626", font=font_note)

    # Helper to draw UML generalization arrow (solid line + open white triangle)
    def draw_generalization(p_child, p_parent):
        # Line from child to near parent
        dx = p_parent[0] - p_child[0]
        dy = p_parent[1] - p_child[1]
        dist = (dx**2 + dy**2)**0.5
        ux = dx / dist
        uy = dy / dist
        
        triangle_sz = 14
        stop_x = p_parent[0] - ux * triangle_sz
        stop_y = p_parent[1] - uy * triangle_sz
        
        draw.line([(p_child[0], p_child[1]), (stop_x, stop_y)], fill="#084298", width=2)
        
        # Hollow triangle pointing to parent
        p_tip = (p_parent[0], p_parent[1])
        p_left = (stop_x + uy * (triangle_sz / 2), stop_y - ux * (triangle_sz / 2))
        p_right = (stop_x - uy * (triangle_sz / 2), stop_y + ux * (triangle_sz / 2))
        draw.polygon([p_tip, p_left, p_right], outline="#084298", fill="#FFFFFF")

    # 2. Draw Actors
    # Left side:
    # Actor Base
    draw_actor(110, 120, "Usuario del Sistema", "<<Actor Base ECI>>")
    # Sub-actors with Generalization
    draw_actor(110, 360, "Operador Hídrico", "Supervisión / Control")
    draw_actor(110, 640, "Solicitante", "Investigador / Docente")

    # Generalization arrows pointing to Usuario Base
    draw_generalization((110, 290), (110, 190))
    draw_generalization((110, 570), (110, 200))

    # Right side:
    # Nuevo Actor: Centro de Control ECI
    draw_actor(1210, 230, "Centro de Control ECI", "<<Sistema / Autoridad>>", color="#0369A1")
    # Nuevo Actor: Técnico de Mantenimiento
    draw_actor(1210, 620, "Técnico de\nMantenimiento", "<<Nuevo Actor v2>>", color="#B45309")

    # 3. Draw Use Cases
    # UC Nucleares dentro del rectángulo (cx, cy, rx, ry)
    # CU Solicitar
    draw_usecase(430, 640, 120, 42, ["Solicitar Misión de", "Transporte o Muestreo"])
    # CU Monitoreo
    draw_usecase(430, 230, 120, 42, ["Monitorear Telemetría de", "15 Drones en Tiempo Real"])
    # CU Reportes
    draw_usecase(450, 125, 125, 36, ["Consultar Historial", "y Reportes Operativos"])

    # CU AP-07 Asignar Automáticamente (Central)
    draw_usecase(570, 410, 140, 52, ["AP-07: Asignar Misión", "Automáticamente", "(Strategy + Validaciones)"], is_core=True)

    # <<include>>: Validar Condiciones Hídricas
    draw_usecase(880, 260, 130, 46, ["Validar Condiciones Hídricas", "API Limnológica Externa", "(Calma / Agitada / Profunda)"])

    # <<include>>: Autorizar Corredor Acuático
    draw_usecase(880, 410, 130, 46, ["Autorizar Corredor Acuático", "y Plan de Vuelo ECI", "(Clearance Institucional)"])

    # <<extend>>: Notificar Fallo al Técnico
    draw_usecase(880, 580, 135, 50, ["Notificar Fallo al Técnico", "y Centro de Control", "(Observer Pattern)"])

    # CU Gestión Reparación
    draw_usecase(880, 730, 130, 46, ["Gestionar Reparación y", "Reintegrar Drone a Flota", "(Retorno a DISPONIBLE)"])

    # 4. Draw Relationships
    # Actors associations (Solid lines)
    # Usuario base -> Reportes
    draw.line([(180, 120), (325, 125)], fill="#334155", width=2)
    # Operador -> Monitoreo
    draw.line([(180, 340), (315, 240)], fill="#334155", width=2)
    # Operador -> Asignar Automáticamente
    draw.line([(180, 370), (430, 410)], fill="#334155", width=2)
    # Solicitante -> Solicitar Misión
    draw.line([(180, 640), (310, 640)], fill="#334155", width=2)

    # Trigger dashed: Solicitar ..> Asignar
    draw_dashed_arrow((450, 598), (530, 462), "<<trigger>>")

    # <<include>> obligatorio: Asignar ..> Validar Condiciones
    draw_dashed_arrow((675, 375), (780, 295), "<<include>>")

    # <<include>> obligatorio: Asignar ..> Autorizar Corredor
    draw_dashed_arrow((710, 410), (750, 410), "<<include>>")

    # <<extend>> condicional: Notificar Fallo ..> Asignar
    draw_dashed_arrow((775, 545), (665, 455), "<<extend>>", "[si drone en estado FALLO]")

    # Centro de Control -> Autorizar Corredor
    draw.line([(1010, 410), (1130, 250)], fill="#0369A1", width=2)
    # Centro de Control <- Notificar Fallo
    draw.line([(1015, 570), (1130, 270)], fill="#0369A1", width=2)

    # Técnico <- Notificar Fallo
    draw.line([(1015, 600), (1130, 620)], fill="#B45309", width=2)
    # Técnico -> Gestionar Reparación
    draw.line([(1010, 730), (1130, 650)], fill="#B45309", width=2)

    output_dir = "docs"
    os.makedirs(output_dir, exist_ok=True)
    out_path = os.path.join(output_dir, "diagrama-cu-prinplup.png")
    img.save(out_path)
    print(f"Use Case diagram image generated at {out_path}")

if __name__ == "__main__":
    generate_use_case_diagram()
