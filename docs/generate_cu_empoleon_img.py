import os
import math
from PIL import Image, ImageDraw, ImageFont

def generate_empoleon_use_cases():
    W, H = 1860, 1260
    img = Image.new("RGB", (W, H), color="#FFFFFF")
    draw = ImageDraw.Draw(img)

    try:
        font_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 22)
        font_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
        font_actor = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 13)
        font_actor_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
        font_uc_core = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 13)
        font_uc = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_uc_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
        font_rel = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
        font_cond_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 10)
        font_cond_desc = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 10)
        font_legend = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
    except Exception:
        font_title = font_sub = font_actor = font_actor_sub = font_uc_core = font_uc = font_uc_sub = ImageFont.load_default()
        font_rel = font_cond_title = font_cond_desc = font_legend = ImageFont.load_default()

    # 1. Header Bar
    draw.rectangle([(0, 0), (W, 75)], fill="#0F172A")
    draw.rectangle([(0, 70), (W, 75)], fill="#0284C7")
    draw.text((36, 14), "Diagrama de Casos de Uso del Nivel Enterprise — AquaPort v3 (Empoleon)", fill="#FFFFFF", font=font_title)
    draw.text((36, 44), "Caso de Uso Nuclear (AP-15), 4 Inclusiones <<include>> y los 5 Flujos Alternos como Extensiones <<extend>> con Condiciones Explícitas", fill="#94A3B8", font=font_sub)
    draw.text((W - 320, 28), "UML 2.5 • Nivel Enterprise", fill="#38BDF8", font=font_sub)

    # 2. System Boundary Rectangle
    rx, ry, rw, rh = 280, 105, 1260, 1010
    draw.rounded_rectangle([(rx, ry), (rx + rw, ry + rh)], radius=16, fill="#F8FAFC", outline="#0284C7", width=2)
    # Header tag for system boundary
    draw.rounded_rectangle([(rx + 24, ry - 14), (rx + 480, ry + 18)], radius=6, fill="#0284C7")
    draw.text((rx + 34, ry - 10), "Sistema AquaPort Enterprise (Empoleon v3)", fill="#FFFFFF", font=font_actor)

    # Helper to draw stick-figure actor
    def draw_actor(x, y, name, sub="", color="#084298", is_system=False):
        if not is_system:
            draw.ellipse([(x - 15, y - 44), (x + 15, y - 14)], outline=color, width=2, fill="#E0F2FE")
            draw.line([(x, y - 14), (x, y + 22)], fill=color, width=2)
            draw.line([(x - 24, y + 2), (x + 24, y + 2)], fill=color, width=2)
            draw.line([(x, y + 22), (x - 18, y + 54)], fill=color, width=2)
            draw.line([(x, y + 22), (x + 18, y + 54)], fill=color, width=2)
            ly = y + 62
        else:
            draw.rounded_rectangle([(x - 30, y - 30), (x + 30, y + 30)], radius=6, fill="#E0F2FE", outline=color, width=2)
            draw.rectangle([(x - 20, y - 20), (x + 20, y - 14)], fill=color)
            draw.text((x - 12, y - 4), "SYS", fill=color, font=font_actor)
            ly = y + 42

        lines = name.split("\n")
        for l in lines:
            w_box = draw.textlength(l, font=font_actor)
            draw.text((x - w_box / 2, ly), l, fill=color, font=font_actor)
            ly += 16
        if sub:
            w_sub = draw.textlength(sub, font=font_actor_sub)
            draw.text((x - w_sub / 2, ly), sub, fill="#475569", font=font_actor_sub)

    # Helper to draw use case ellipse
    def draw_usecase(cx, cy, rx_val, ry_val, title_lines, is_core=False, is_ext=False):
        if is_core:
            fill_col = "#BAE6FD"
            border_col = "#0284C7"
            border_w = 3
        elif is_ext:
            fill_col = "#FEE2E2"
            border_col = "#EF4444"
            border_w = 2
        else:
            fill_col = "#E0F2FE"
            border_col = "#0284C7"
            border_w = 2

        draw.ellipse([(cx - rx_val, cy - ry_val), (cx + rx_val, cy + ry_val)], fill=fill_col, outline=border_col, width=border_w)
        
        total_h = len(title_lines) * 16
        start_y = cy - total_h / 2
        for idx, line in enumerate(title_lines):
            f = font_uc_core if is_core else (font_uc if idx == 0 else font_uc_sub)
            c = "#0F172A"
            w_line = draw.textlength(line, font=f)
            draw.text((cx - w_line / 2, start_y + idx * 16), line, fill=c, font=f)

    # Helper to draw dashed arrow with badge and condition box
    def draw_dashed_arrow(p1, p2, tag="<<include>>", cond_title=None, cond_desc=None, color="#0284C7", badge_pos=None):
        dx = p2[0] - p1[0]
        dy = p2[1] - p1[1]
        dist = math.hypot(dx, dy)
        if dist == 0:
            return
        
        dash_len = 8
        gap_len = 5
        total_len = dash_len + gap_len
        steps = int(dist // total_len)
        ux = dx / dist
        uy = dy / dist

        for i in range(steps):
            s = i * total_len
            e = min(s + dash_len, dist)
            draw.line([(p1[0] + s * ux, p1[1] + s * uy), (p1[0] + e * ux, p1[1] + e * uy)], fill=color, width=2)
        
        angle = math.atan2(dy, dx)
        arr_len = 10
        ah1 = (p2[0] - arr_len * math.cos(angle - math.pi / 6), p2[1] - arr_len * math.sin(angle - math.pi / 6))
        ah2 = (p2[0] - arr_len * math.cos(angle + math.pi / 6), p2[1] - arr_len * math.sin(angle + math.pi / 6))
        draw.polygon([p2, ah1, ah2], fill=color)

        if badge_pos is None:
            mx = (p1[0] + p2[0]) // 2
            my = (p1[1] + p2[1]) // 2
        else:
            mx, my = badge_pos

        tw = draw.textlength(tag, font=font_rel) + 14
        draw.rounded_rectangle([(mx - tw // 2, my - 10), (mx + tw // 2, my + 10)], radius=4, fill="#FFFFFF", outline="#94A3B8", width=1)
        draw.text((mx - tw // 2 + 7, my - 7), tag, fill=color, font=font_rel)

        if cond_title and cond_desc:
            nw = max(draw.textlength(cond_title, font=font_cond_title), draw.textlength(cond_desc, font=font_cond_desc)) + 16
            nh = 34
            nx = mx - nw // 2
            ny = my + 14
            draw.rectangle([(nx, ny), (nx + nw, ny + nh)], fill="#FEF3C7", outline="#D97706", width=1)
            draw.text((nx + 8, ny + 3), cond_title, fill="#92400E", font=font_cond_title)
            draw.text((nx + 8, ny + 17), cond_desc, fill="#78350F", font=font_cond_desc)

    # 3. DRAW ACTORS (LEFT & RIGHT)
    # Operador de Misión (Top Left)
    draw_actor(130, 220, "Operador de Misión", "Actor Primario (Humano)")
    # Módulo de Telemetría (Mid Left)
    draw_actor(130, 560, "Módulo Telemetría", "Sistema IoT (MQTT)", is_system=True)
    # Técnico de Rescate Acuático (Bottom Left)
    draw_actor(130, 880, "Técnico de Rescate", "Personal de Campo (Rescate)")

    # Sistema AquaPort Core (Top Right)
    draw_actor(1680, 220, "AquaPort Core", "Sistema Orquestador", is_system=True)
    # Sistema LIMS Central (Bottom Right)
    draw_actor(1680, 720, "Sistema LIMS Central", "Sistema Lab. Limnológico", is_system=True)

    # 4. DRAW USE CASES INSIDE BOUNDARY
    # Central Nuclear Use Case: CU-01
    c_cu01 = (910, 210)
    draw_usecase(c_cu01[0], c_cu01[1], 210, 55, ["CU-01: Ejecutar Misión", "Multi-Etapa con Waypoints"], is_core=True)

    # 4 INCLUDED USE CASES (<<include>>) - Placed in horizontal band at y=370
    # CU-02: Validar Misión
    c_cu02 = (470, 370)
    draw_usecase(c_cu02[0], c_cu02[1], 150, 42, ["CU-02: Validar Misión con", "Cadena de Responsabilidad"])

    # CU-03: Consultar Condiciones Hídricas
    c_cu03 = (760, 370)
    draw_usecase(c_cu03[0], c_cu03[1], 140, 42, ["CU-03: Consultar Condiciones", "Hídricas (Adapter)"])

    # CU-04: Coordinar Traspaso Físico
    c_cu04 = (1050, 370)
    draw_usecase(c_cu04[0], c_cu04[1], 140, 42, ["CU-04: Coordinar Traspaso", "en Waypoint / Esclusa"])

    # CU-05: Sellar Acta Digital de Custodia
    c_cu05 = (1340, 370)
    draw_usecase(c_cu05[0], c_cu05[1], 150, 42, ["CU-05: Sellar Acta Digital de", "Cadena de Custodia (SHA-256)"])

    # 5 EXTENSION USE CASES (<<extend>>)
    # EXT-01: Reasignar Drone en Waypoint (Left-Center)
    c_ext01 = (470, 600)
    draw_usecase(c_ext01[0], c_ext01[1], 155, 45, ["CU-EXT-01: Reasignar Drone", "Automáticamente en Waypoint"], is_ext=True)

    # EXT-02: Desviar Ruta por Condiciones Adversas (Bottom-Left)
    c_ext02 = (470, 890)
    draw_usecase(c_ext02[0], c_ext02[1], 155, 45, ["CU-EXT-02: Desviar Ruta por", "Condiciones Hídricas Adversas"], is_ext=True)

    # EXT-03: Activar Espera Pasiva en Boya (Mid-Center)
    c_ext03 = (910, 600)
    draw_usecase(c_ext03[0], c_ext03[1], 155, 45, ["CU-EXT-03: Activar Espera Pasiva", "en Boya Climatizada"], is_ext=True)

    # EXT-05: Ejecutar Anclaje por Batería Crítica (Bottom-Center)
    c_ext05 = (910, 890)
    draw_usecase(c_ext05[0], c_ext05[1], 155, 45, ["CU-EXT-05: Ejecutar Anclaje de", "Emergencia por Batería Crítica"], is_ext=True)

    # EXT-04: Aislar Muestra e Iniciar Cuarentena (Right, extends CU-05!)
    c_ext04 = (1340, 650)
    draw_usecase(c_ext04[0], c_ext04[1], 155, 45, ["CU-EXT-04: Aislar Muestra y", "Reintentar Certificación Custodia"], is_ext=True)

    # 5. SOLID ACTOR-TO-USE-CASE ASSOCIATIONS (WITHOUT OVERLAPPING)
    # Operador -> CU-01
    draw.line([(180, 220), (700, 210)], fill="#0F172A", width=2)
    # Core Service -> CU-01
    draw.line([(1630, 220), (1120, 210)], fill="#0F172A", width=2)
    # Core Service -> CU-04
    draw.line([(1630, 240), (1190, 370)], fill="#0F172A", width=2)
    # Módulo Telemetría -> EXT-01 (Clean straight line on left)
    draw.line([(180, 560), (315, 600)], fill="#0F172A", width=2)
    # LIMS Central -> CU-05
    draw.line([(1630, 720), (1490, 370)], fill="#0F172A", width=2)
    # LIMS Central -> EXT-04
    draw.line([(1630, 730), (1495, 650)], fill="#0F172A", width=2)
    # Técnico de Rescate -> EXT-05
    draw.line([(180, 880), (755, 890)], fill="#0F172A", width=2)
    # Técnico de Rescate -> EXT-01
    draw.line([(180, 870), (410, 645)], fill="#0F172A", width=1)

    # 6. INCLUSION RELATIONSHIPS (<<include>>) - FROM CU-01 TO CU-02..05
    # CU-01 -> CU-02
    draw_dashed_arrow((750, 245), (540, 330), tag="<<include>>", color="#0284C7", badge_pos=(640, 285))
    # CU-01 -> CU-03
    draw_dashed_arrow((840, 260), (790, 328), tag="<<include>>", color="#0284C7", badge_pos=(815, 290))
    # CU-01 -> CU-04
    draw_dashed_arrow((980, 260), (1030, 328), tag="<<include>>", color="#0284C7", badge_pos=(1005, 290))
    # CU-01 -> CU-05
    draw_dashed_arrow((1070, 245), (1270, 330), tag="<<include>>", color="#0284C7", badge_pos=(1170, 285))

    # 7. EXTENSION RELATIONSHIPS (<<extend>>) - FROM EXT-XX TO BASE UC WITH EXPLICIT CONDITIONS
    # EXT-01 -> CU-01 (Dashed arrow up-right to CU-01)
    draw_dashed_arrow((540, 560), (780, 260), tag="<<extend>>",
                      cond_title="Condición de Extensión:",
                      cond_desc="Drone en FALLO o pérdida de señal > 5.0s",
                      color="#EF4444", badge_pos=(660, 430))

    # EXT-02 -> CU-01 (Dashed arrow up-right to CU-01)
    draw_dashed_arrow((560, 850), (820, 265), tag="<<extend>>",
                      cond_title="Condición de Extensión:",
                      cond_desc="Oleaje > 2.0m o corriente incompatible en canal",
                      color="#EF4444", badge_pos=(690, 520))

    # EXT-03 -> CU-01 (Straight up to CU-01)
    draw_dashed_arrow((910, 555), (910, 265), tag="<<extend>>",
                      cond_title="Condición de Extensión:",
                      cond_desc="Muelle de Lab Central saturado o inactivo",
                      color="#EF4444", badge_pos=(910, 480))

    # EXT-05 -> CU-01 (Up to CU-01)
    draw_dashed_arrow((950, 845), (960, 265), tag="<<extend>>",
                      cond_title="Condición de Extensión:",
                      cond_desc="Batería residual <= 20% en mitad de tramo",
                      color="#EF4444", badge_pos=(960, 720))

    # EXT-04 -> CU-05 (Straight up to CU-05!)
    draw_dashed_arrow((1340, 605), (1340, 412), tag="<<extend>>",
                      cond_title="Condición de Extensión:",
                      cond_desc="Discrepancia en hash SHA-256 o temp > 6°C",
                      color="#EF4444", badge_pos=(1340, 520))

    # 8. LEGEND BOX (BOTTOM)
    ly = 1135
    draw.rounded_rectangle([(36, ly), (W - 36, ly + 95)], radius=8, fill="#FFFFFF", outline="#CBD5E1", width=1)
    draw.text((50, ly + 12), "Convenciones UML de Casos de Uso Enterprise:", fill="#0F172A", font=font_legend)

    # Legend 1: Core UC
    draw.ellipse([(50, ly + 40), (170, ly + 80)], fill="#BAE6FD", outline="#0284C7", width=2)
    draw.text((64, ly + 54), "CU Nuclear (AP-15)", fill="#0F172A", font=font_legend)

    # Legend 2: Included UC
    draw.ellipse([(190, ly + 40), (310, ly + 80)], fill="#E0F2FE", outline="#0284C7", width=1)
    draw.text((204, ly + 54), "CU <<include>>", fill="#0F172A", font=font_legend)

    # Legend 3: Extended UC
    draw.ellipse([(330, ly + 40), (460, ly + 80)], fill="#FEE2E2", outline="#EF4444", width=1)
    draw.text((344, ly + 54), "CU <<extend>> (Fallo)", fill="#991B1B", font=font_legend)

    # Legend 4: Condition note
    draw.rectangle([(480, ly + 40), (660, ly + 80)], fill="#FEF3C7", outline="#D97706", width=1)
    draw.text((490, ly + 46), "Nota de Condición", fill="#92400E", font=font_cond_title)
    draw.text((490, ly + 60), "[Condición explícita]", fill="#78350F", font=font_cond_desc)

    # Legend 5: Associations
    draw.line([(690, ly + 60), (760, ly + 60)], fill="#0F172A", width=2)
    draw.text((770, ly + 54), "Asociación Actor-CU", fill="#475569", font=font_legend)

    # Legend 6: Include line
    draw.line([(930, ly + 60), (990, ly + 60)], fill="#0284C7", width=2)
    draw.text((1000, ly + 54), "<<include>> (Obligatorio)", fill="#0284C7", font=font_legend)

    # Legend 7: Extend line
    draw.line([(1170, ly + 60), (1230, ly + 60)], fill="#EF4444", width=2)
    draw.text((1240, ly + 54), "<<extend>> (Bajo condición)", fill="#EF4444", font=font_legend)

    output_path = os.path.join("docs", "diagrama-cu-empoleon.png")
    img.save(output_path, "PNG", dpi=(300, 300))
    print(f"Diagrama de Casos de Uso generado en: {output_path}")

if __name__ == "__main__":
    generate_empoleon_use_cases()
