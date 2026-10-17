import os
from PIL import Image, ImageDraw, ImageFont

def create_emergency_mock():
    width = 1920
    height = 1080
    img = Image.new('RGB', (width, height), color=(10, 14, 23))
    draw = ImageDraw.Draw(img)

    # Fonts
    try:
        font_title = ImageFont.truetype("arial.ttf", 36)
        font_header = ImageFont.truetype("arialbd.ttf", 46)
        font_sub = ImageFont.truetype("arial.ttf", 26)
        font_bold = ImageFont.truetype("arialbd.ttf", 28)
        font_card_title = ImageFont.truetype("arialbd.ttf", 32)
        font_regular = ImageFont.truetype("arial.ttf", 22)
        font_small = ImageFont.truetype("arial.ttf", 18)
    except Exception:
        font_title = font_header = font_sub = font_bold = font_card_title = font_regular = font_small = ImageFont.load_default()

    # Background subtle grid pattern
    for x in range(0, width, 60):
        draw.line([(x, 0), (x, height)], fill=(16, 24, 38), width=1)
    for y in range(0, height, 60):
        draw.line([(0, y), (width, y)], fill=(16, 24, 38), width=1)

    # Top Navbar
    draw.rectangle([(0, 0), (width, 80)], fill=(13, 20, 32))
    draw.line([(0, 80), (width, 80)], fill=(220, 38, 38), width=3) # Crimson line
    draw.text((40, 22), "AQUAPORT // EMERGENCY CONTINGENCY CENTER", fill=(248, 113, 113), font=font_title)
    draw.rectangle([(1600, 20), (1880, 60)], fill=(69, 10, 10), outline=(239, 68, 68), width=2)
    draw.text((1630, 28), "ESTADO: ALERTA CRITICA", fill=(254, 202, 202), font=font_bold)

    # Big Pulsing Alert Banner
    draw.rectangle([(60, 110), (width - 60, 210)], fill=(45, 10, 10), outline=(220, 38, 38), width=3)
    draw.rectangle([(80, 130), (130, 190)], fill=(220, 38, 38))
    draw.text((95, 132), "!", fill=(255, 255, 255), font=font_header)
    draw.text((150, 130), "ALERTA CRITICA: MISION DE EMERGENCIA SIN DRONES APTOS DISPONIBLES", fill=(254, 226, 226), font=font_header)
    draw.text((150, 180), "Protocolo de Excepcion Activado (AsignadorAutomaticoMisiones -> Notificacion automatica a Observadores)", fill=(252, 165, 165), font=font_sub)

    # Content Area - 2 Columns
    # Left Column: Detalle de Misión y Diagnóstico de Rechazo
    draw.rounded_rectangle([(60, 240), (1100, 940)], radius=16, fill=(17, 24, 39), outline=(55, 65, 81), width=2)
    draw.text((90, 265), "DETALLE DE LA MISION RECHAZADA (SOLICITUD CRITICA)", fill=(243, 244, 246), font=font_card_title)
    draw.line([(90, 310), (1070, 310)], fill=(55, 65, 81), width=1)

    details = [
        ("Identificador Mision:", "M-CRIT-99 (Despacho de Emergencia Ambiental)"),
        ("Nivel de Prioridad:", "CRITICA [Precedencia Absoluta sobre la Flota]"),
        ("Punto de Partida:", "Bahia Ciencia (Sector de Monitoreo Norte)"),
        ("Punto de Llegada:", "Laguna Sur (Laboratorio Limnologico)"),
        ("Tipo de Carga:", "EQUIPO_MEDICION (Kit de Deteccion Rapida - 1.10 kg)"),
        ("Condicion Hidrica:", "AGITADA [Nivel 4 - Oleaje Intenso y Viento 35 km/h]"),
        ("Requerimiento Casco:", "DroneSemisumergido (Solo casco M soporta agua agitada)"),
        ("Bateria Minima Requerida:", ">= 50% (Umbral de contingencia para mision critica)")
    ]

    curr_y = 330
    for label, val in details:
        draw.text((90, curr_y), label, fill=(156, 163, 175), font=font_regular)
        val_color = (239, 68, 68) if "CRITICA" in val or "AGITADA" in val else (229, 231, 235)
        draw.text((380, curr_y), val, fill=val_color, font=font_bold)
        curr_y += 42

    # Diagnostic Box inside left column
    draw.rounded_rectangle([(90, 680), (1070, 910)], radius=12, fill=(24, 18, 22), outline=(185, 28, 28), width=2)
    draw.text((115, 700), "DIAGNOSTICO DEL MOTOR DE ASIGNACION (STRATEGY & FACTORY)", fill=(252, 165, 165), font=font_bold)
    
    diag_lines = [
        "- Analisis de Flota: De 15 drones totales, solo 5 son Semisumergidos (DM-01 a DM-05).",
        "- En Bahia Ciencia: 0 drones Semisumergidos estacionados (todos son Buceadores DB-01, DB-05).",
        "- Drones Buceadores: Incompatibles por oleaje agitado y limite maximo de carga (300g < 1100g).",
        "- Drones Superficiales: Incompatibles por oleaje agitado (solo toleran aguas tranquilas).",
        "- Resultado Heuristica: candidatos.isEmpty() -> Asignacion Abortada (Reto 12 TDD: IllegalStateException)."
    ]
    diag_y = 740
    for line in diag_lines:
        draw.text((115, diag_y), line, fill=(229, 231, 235), font=font_small)
        diag_y += 32

    # Right Column: Registro de Observadores Notificados y Acciones
    draw.rounded_rectangle([(1140, 240), (width - 60, 940)], radius=16, fill=(17, 24, 39), outline=(55, 65, 81), width=2)
    draw.text((1170, 265), "OBSERVER PATTERN // NOTIFICACIONES REAL-TIME", fill=(243, 244, 246), font=font_card_title)
    draw.line([(1170, 310), (width - 90, 310)], fill=(55, 65, 81), width=1)

    # Observer 1: Centro de Control
    draw.rounded_rectangle([(1170, 330), (width - 90, 470)], radius=10, fill=(31, 41, 55), outline=(220, 38, 38), width=2)
    draw.rectangle([(1190, 345), (1330, 375)], fill=(153, 27, 27))
    draw.text((1200, 350), "CENTRO CONTROL", fill=(255, 255, 255), font=font_small)
    draw.text((1350, 350), "ALERTA EN TIEMPO REAL RECIBIDA (14:14:02)", fill=(156, 163, 175), font=font_small)
    draw.text((1190, 390), "Evento: notificarFallo(null, 'ALERTA CRITICA')", fill=(254, 202, 202), font=font_bold)
    draw.text((1190, 425), "Accion: Registro de incidente en bitacora oficial ECI y sirena en torre.", fill=(209, 213, 219), font=font_regular)

    # Observer 2: Técnico de Mantenimiento
    draw.rounded_rectangle([(1170, 490), (width - 90, 630)], radius=10, fill=(31, 41, 55), outline=(234, 88, 12), width=2)
    draw.rectangle([(1190, 505), (1330, 535)], fill=(194, 65, 12))
    draw.text((1200, 510), "TECNICO GUARDIA", fill=(255, 255, 255), font=font_small)
    draw.text((1350, 510), "TELEMETRIA PUSH ENVIADA (14:14:02)", fill=(156, 163, 175), font=font_small)
    draw.text((1190, 550), "Evento: notificarFallo(...) -> Despacho urgente de cuadrilla", fill=(254, 215, 170), font=font_bold)
    draw.text((1190, 585), "Accion: Orden de acelerar recarga de DM-05 en Laboratorio Hidrico.", fill=(209, 213, 219), font=font_regular)

    # Emergency Actions Section (Fitts's Law)
    draw.text((1170, 660), "ACCIONES DE CONTINGENCIA INMEDIATA:", fill=(243, 244, 246), font=font_bold)

    # Button 1: Relevo Flota Externa
    draw.rounded_rectangle([(1170, 700), (width - 90, 770)], radius=10, fill=(185, 28, 28), outline=(239, 68, 68), width=2)
    draw.text((1260, 720), "SOLICITAR RELEVO DE FLOTA EXTERNA (CARGA PESADA)", fill=(255, 255, 255), font=font_bold)

    # Button 2: Recarga Rápida Prioritaria
    draw.rounded_rectangle([(1170, 790), (width - 90, 850)], radius=10, fill=(30, 58, 138), outline=(59, 130, 246), width=2)
    draw.text((1270, 808), "ACTIVAR RECARGA FAST-CHARGE EN DM-05 (20m)", fill=(255, 255, 255), font=font_bold)

    # Button 3: Reintentar con Parámetros Flexibles
    draw.rounded_rectangle([(1170, 870), (width - 90, 920)], radius=8, fill=(55, 65, 81))
    draw.text((1310, 885), "REINTENTAR CON TRASBORDO INTERMEDIO", fill=(209, 213, 219), font=font_regular)

    # Bottom Footer
    draw.text((60, 1020), "AquaPort v2.0.0 (Prinplup) | Sistema Autonomo de Gestion Hidrica ECI | Reto 11 - Mock de Contingencia", fill=(107, 114, 128), font=font_small)

    out_path = r"c:\Users\ASUS\OneDrive\Escritorio\DOSW\MaterialRefuerzoPiplup\docs\images\mock-alerta-mision-critica-prinplup.png"
    img.save(out_path, format="PNG")
    print("Generated emergency mock image:", out_path)

if __name__ == "__main__":
    create_emergency_mock()
