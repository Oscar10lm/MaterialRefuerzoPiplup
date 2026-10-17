import os
import math
from PIL import Image, ImageDraw, ImageFont

def create_c4_diagram():
    W, H = 2200, 1220
    img = Image.new("RGB", (W, H), color="#F8FAFC")
    draw = ImageDraw.Draw(img)

    # Fonts
    try:
        font_header_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 26)
        font_header_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 15)
        font_box_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 16)
        font_box_type = ImageFont.truetype("C:/Windows/Fonts/segoeuii.ttf", 12)
        font_box_desc = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 12)
        font_edge = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 11)
        font_edge_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
        font_legend = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
        font_legend_sub = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 11)
    except Exception:
        font_header_title = font_header_sub = font_box_title = font_box_type = font_box_desc = ImageFont.load_default()
        font_edge = font_edge_sub = font_legend = font_legend_sub = ImageFont.load_default()

    # 1. Header Bar
    draw.rectangle([(0, 0), (W, 85)], fill="#0F172A")
    draw.rectangle([(0, 80), (W, 85)], fill="#0284C7")
    draw.text((40, 16), "AquaPort Enterprise — Diagrama de Contenedores C4 (Nivel 2)", fill="#FFFFFF", font=font_header_title)
    draw.text((40, 50), "Arquitectura de Contenedores y Protocolos de Comunicación de la Red Multi-Embalse (60 Drones, 4 Zonas Hídricas, Rutas Multi-Etapa)", fill="#94A3B8", font=font_header_sub)
    draw.text((W - 370, 32), "Nivel 2: Containers | v3.0 Enterprise", fill="#38BDF8", font=font_header_sub)

    # 2. System Boundary (AquaPort Enterprise)
    bx, by, bw, bh = 370, 115, 1340, 920
    draw.rounded_rectangle([(bx, by), (bx + bw, by + bh)], radius=16, fill="#F0F9FF", outline="#0284C7", width=2)
    # Header tag for system boundary
    draw.rounded_rectangle([(bx + 20, by - 14), (bx + 490, by + 18)], radius=6, fill="#0284C7")
    draw.text((bx + 30, by - 10), "Sistema AquaPort Enterprise  [Software System Boundary]", fill="#FFFFFF", font=font_box_title)

    # Helper function to draw C4 box
    def draw_c4_box(x, y, w, h, title, type_tag, desc_lines, bg_color, border_color="#1E293B", text_color="#FFFFFF", is_person=False, is_db=False):
        # Drop shadow
        draw.rounded_rectangle([(x + 3, y + 3), (x + w + 3, y + h + 3)], radius=10, fill="#CBD5E1")
        # Main box
        draw.rounded_rectangle([(x, y), (x + w, y + h)], radius=10, fill=bg_color, outline=border_color, width=2)
        
        # Person indicator
        if is_person:
            cx = x + w // 2
            draw.ellipse([(cx - 18, y - 26), (cx + 18, y + 8)], fill=bg_color, outline=border_color, width=2)

        # Title
        draw.text((x + 14, y + 14), title, fill=text_color, font=font_box_title)
        draw.text((x + 14, y + 36), type_tag, fill="#BAE6FD" if text_color == "#FFFFFF" else "#0369A1", font=font_box_type)
        
        # Divider line
        draw.line([(x + 14, y + 54), (x + w - 14, y + 54)], fill=border_color, width=1)
        
        # Description
        curr_y = y + 62
        for line in desc_lines:
            draw.text((x + 14, curr_y), line, fill=text_color, font=font_box_desc)
            curr_y += 18
            
        # DB Cylinder icon indicator if DB
        if is_db:
            draw.rounded_rectangle([(x + w - 42, y + 12), (x + w - 14, y + 38)], radius=4, fill="#1D4ED8", outline="#FFFFFF", width=1)
            draw.text((x + w - 34, y + 16), "DB", fill="#FFFFFF", font=font_box_type)

    # Helper to draw arrow head
    def draw_arrow_head(x, y, angle_rad, length=12, color="#0F172A"):
        p1 = (x, y)
        p2 = (x - length * math.cos(angle_rad - math.pi / 6), y - length * math.sin(angle_rad - math.pi / 6))
        p3 = (x - length * math.cos(angle_rad + math.pi / 6), y - length * math.sin(angle_rad + math.pi / 6))
        draw.polygon([p1, p2, p3], fill=color)

    # Helper to draw connector lines with label badge
    def draw_edge(p1, p2, label_title, protocol_label, badge_pos=None, color="#0F172A", arrow_to=True, arrow_from=False):
        draw.line([p1, p2], fill=color, width=2)
        angle = math.atan2(p2[1] - p1[1], p2[0] - p1[0])
        if arrow_to:
            draw_arrow_head(p2[0], p2[1], angle, color=color)
        if arrow_from:
            draw_arrow_head(p1[0], p1[1], angle + math.pi, color=color)

        if badge_pos is None:
            mx = (p1[0] + p2[0]) // 2
            my = (p1[1] + p2[1]) // 2
        else:
            mx, my = badge_pos
        
        tw1 = len(label_title) * 7 + 16
        tw2 = len(protocol_label) * 7 + 16
        bw = max(tw1, tw2, 110)
        bh = 38
        bx_badge = mx - bw // 2
        by_badge = my - bh // 2
        
        draw.rounded_rectangle([(bx_badge, by_badge), (bx_badge + bw, by_badge + bh)], radius=6, fill="#FFFFFF", outline="#94A3B8", width=1)
        draw.text((bx_badge + 8, by_badge + 4), label_title, fill="#0F172A", font=font_edge)
        draw.text((bx_badge + 8, by_badge + 20), protocol_label, fill="#0284C7", font=font_edge_sub)

    # -------------------------------------------------------------
    # 3. EXTERNAL SYSTEMS & ACTORS (LEFT & RIGHT)
    # -------------------------------------------------------------
    # Person: Operador / Científico (Top Left)
    draw_c4_box(30, 160, 240, 170, "Operador / Científico", "[Persona]",
                ["Personal técnico y científico", "que planifica misiones multi-etapa,", "supervisa alertas y analiza", "muestras recolectadas."],
                bg_color="#08427B", border_color="#06325D", is_person=True)

    # External System 2: Red de Drones IoT (Bottom Left)
    draw_c4_box(30, 640, 240, 210, "Flota 60 Drones IoT", "[Sistema Externo: IoT Embedded]",
                ["60 drones acuáticos en 4 zonas:", "• Superficial (20 drones)", "• Semisumergido (20 drones)", "• Buceador (20 drones)", "Emiten telemetría con sobre", "cifrado y ejecutan rutas."],
                bg_color="#475569", border_color="#334155")

    # External System 3: Sistema GIS Campus ECI (Top Right)
    draw_c4_box(1780, 150, 370, 170, "Sistema GIS ECI", "[Sistema Externo: Cartografía]",
                ["Infraestructura geoespacial de la ECI.", "Provee cartografía vectorial de canales,", "boyas fijas de muestreo y", "polígonos de cuencas hídricas."],
                bg_color="#475569", border_color="#334155")

    # External System 4: Sistema LIMS Central (Middle Right)
    draw_c4_box(1780, 380, 370, 170, "Sistema LIMS Central", "[Sistema Externo: Lab. Limnológico]",
                ["Gestión de laboratorio químico-biológico.", "Registra órdenes de análisis de agua y", "valida actas digitales de trazabilidad", "de la cadena de custodia."],
                bg_color="#475569", border_color="#334155")

    # External System 1: API Meteorológica e Hídrica Externa (Bottom Right)
    draw_c4_box(1780, 650, 370, 190, "API Meteorológica Hídrica", "[Sistema Externo: API Externa]",
                ["Red de estaciones limnológicas y sensores.", "Expone condiciones en inglés:", "• waterLevel (m)", "• turbidity (NTU)", "• depth (m) y temperature (°C)"],
                bg_color="#475569", border_color="#334155")

    # -------------------------------------------------------------
    # 4. INTERNAL CONTAINERS (INSIDE BOUNDARY)
    # -------------------------------------------------------------
    # Container 1: Panel de Control Web SPA
    draw_c4_box(410, 160, 270, 210,
                "Panel de Control Web", "[Container: SPA / HTML5 / JS / CSS]",
                ["Dashboard operativo interactivo:", "• Monitoreo geoespacial en vivo.", "• Formulario de rutas multi-etapa.", "• Visualización de 60 drones.", "• Notificación de alertas críticas."],
                bg_color="#0284C7", border_color="#0369A1")

    # Container 2: Aplicación Principal (Core Service)
    draw_c4_box(910, 140, 360, 270,
                "AquaPort Core Service", "[Container: Java 21 / Spring Boot]",
                ["Orquestación central y dominio puro:", "• Planificación de rutas multi-etapa.", "• Chain of Responsibility de validación.", "• Reasignación autónoma ante fallo en WP.", "• Trazabilidad de cadena de custodia.", "• Cálculo O(N) de eficiencia y batería.", "• Coordinación entre las 4 zonas."],
                bg_color="#1168BD", border_color="#0A4C8B")

    # Container 3: Módulo de Telemetría y Monitoreo
    draw_c4_box(410, 640, 280, 230,
                "Módulo de Telemetría", "[Container: Java 21 Worker / MQTT Engine]",
                ["Ingesta concurrente de alta frecuencia:", "• Desencriptación sobre AES-256.", "• Patrón Decorator (DroneConMonitoreo).", "• Bitácora inmutable en memoria.", "• Detección síncrona de fallo/emergencia.", "• Streaming WebSocket a operadores."],
                bg_color="#1168BD", border_color="#0A4C8B")

    # Container 4: Base de Datos de Misiones
    draw_c4_box(910, 640, 360, 230,
                "BD Misiones y Telemetría", "[Container: PostgreSQL 16 + TimescaleDB]",
                ["Persistencia relacional y series temporales:", "• Catálogo maestro de 60 drones y 4 zonas.", "• Misiones multi-etapa, tramos y waypoints.", "• Actas inmutables de cadena de custodia.", "• Histórico de telemetría y diagnósticos."],
                bg_color="#1E40AF", border_color="#172554", is_db=True)

    # Container 5: Adaptador de API Hídrica
    draw_c4_box(1380, 640, 280, 230,
                "Adaptador API Hídrica", "[Container: Java Ports & Adapters]",
                ["Aislamiento de infraestructura externa:", "• Implementa ProveedorCondicionesHidricas.", "• Traduce JSON en inglés al modelo puro.", "• Normaliza NivelAgua y NivelTurbidez.", "• Caché en memoria de condiciones."],
                bg_color="#1168BD", border_color="#0A4C8B")

    # -------------------------------------------------------------
    # 5. CONNECTORS & FLOWS (WITH PROTOCOL & PAYLOAD)
    # -------------------------------------------------------------
    # 1. Operador -> SPA
    draw_edge((270, 240), (410, 240), "Usa dashboard y despacha", "HTTPS / GUI", badge_pos=(340, 240))

    # 2. SPA -> Core Service
    draw_edge((680, 240), (910, 240), "Solicitud misiones y órdenes", "HTTPS / REST JSON", badge_pos=(795, 240))

    # 3. SPA <-> Telemetry (Vertical)
    draw_edge((540, 370), (540, 640), "Streaming telemetría y alertas", "WSS / WebSockets", badge_pos=(540, 505), arrow_from=True)

    # 4. Flota 60 Drones -> Telemetry
    draw_edge((270, 750), (410, 750), "Telemetría cifrada y waypoints", "MQTTS / TLS [Sobre AES-256]", badge_pos=(340, 750), arrow_from=True)

    # 5. Telemetry -> Core (Diagonal)
    draw_edge((690, 660), (910, 390), "Eventos de fallo y desvíos", "In-Process / Síncrono", badge_pos=(790, 500))

    # 6. Telemetry -> DB (Horizontal)
    draw_edge((690, 770), (910, 770), "Batch insert series temporales", "TCP / JDBC [Port 5432]", badge_pos=(800, 770))

    # 7. Core -> DB (Vertical)
    draw_edge((1090, 410), (1090, 640), "CRUD Misiones, Drones, Custodia", "TCP / JDBC [Port 5432]", badge_pos=(1090, 525), arrow_from=True)

    # 8. Core -> Adapter (Diagonal)
    draw_edge((1240, 410), (1390, 640), "consultarCondiciones(zona)", "Java Interface [Puerto DIP]", badge_pos=(1330, 515), arrow_from=True)

    # 9. Adapter -> External API (Horizontal)
    draw_edge((1660, 750), (1780, 750), "waterLevel, turbidity, depth", "HTTPS / REST JSON", badge_pos=(1720, 750), arrow_from=True)

    # 10. Core -> GIS ECI (Horizontal/Diagonal)
    draw_edge((1270, 220), (1780, 220), "Cartografía, boyas, geocercas", "HTTPS / WFS GeoJSON", badge_pos=(1525, 220), arrow_from=True)

    # 11. Core -> LIMS Central (Horizontal/Diagonal)
    draw_edge((1270, 350), (1780, 420), "Actas custodia y muestras", "HTTPS REST / AMQP", badge_pos=(1525, 385), arrow_from=True)

    # -------------------------------------------------------------
    # 6. LEGEND BAR (BOTTOM)
    # -------------------------------------------------------------
    ly = 1070
    draw.rounded_rectangle([(30, ly), (W - 30, ly + 115)], radius=8, fill="#FFFFFF", outline="#CBD5E1", width=1)
    draw.text((50, ly + 12), "Convenciones y Leyenda C4 (Nivel 2 - Diagrama de Contenedores):", fill="#0F172A", font=font_legend)

    # Legend Item 1: Person
    draw.rounded_rectangle([(50, ly + 42), (180, ly + 90)], radius=6, fill="#08427B")
    draw.text((62, ly + 58), "Persona / Actor", fill="#FFFFFF", font=font_legend_sub)

    # Legend Item 2: Internal Container
    draw.rounded_rectangle([(200, ly + 42), (380, ly + 90)], radius=6, fill="#1168BD")
    draw.text((212, ly + 58), "Contenedor (Java / Core / Worker)", fill="#FFFFFF", font=font_legend_sub)

    # Legend Item 3: Database Container
    draw.rounded_rectangle([(400, ly + 42), (580, ly + 90)], radius=6, fill="#1E40AF")
    draw.text((412, ly + 58), "Contenedor Base de Datos (SQL)", fill="#FFFFFF", font=font_legend_sub)

    # Legend Item 4: External System
    draw.rounded_rectangle([(600, ly + 42), (780, ly + 90)], radius=6, fill="#475569")
    draw.text((612, ly + 58), "Sistema Externo Integrado", fill="#FFFFFF", font=font_legend_sub)

    # Legend Item 5: System Boundary
    draw.rounded_rectangle([(800, ly + 42), (1030, ly + 90)], radius=6, fill="#F0F9FF", outline="#0284C7", width=2)
    draw.text((812, ly + 58), "Límite del Sistema AquaPort", fill="#0369A1", font=font_legend_sub)

    # Legend Item 6: Protocol lines
    draw.line([(1060, ly + 65), (1140, ly + 65)], fill="#0F172A", width=2)
    draw_arrow_head(1140, ly + 65, 0)
    draw.text((1155, ly + 58), "Canal de Comunicación (Protocolo técnico + Datos transferidos explícitos)", fill="#475569", font=font_legend_sub)

    # Output path
    output_path = os.path.join("docs", "c4-contenedores-empoleon.png")
    img.save(output_path, "PNG", dpi=(300, 300))
    print(f"Diagrama C4 de contenedores generado exitosamente en: {output_path}")

if __name__ == "__main__":
    create_c4_diagram()
