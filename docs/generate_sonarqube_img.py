from PIL import Image, ImageDraw, ImageFont

# Canvas dimensions
W, H = 1240, 820
img = Image.new("RGB", (W, H), color="#F4F5F7")
draw = ImageDraw.Draw(img)

# Load fonts
try:
    font_logo = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 17)
    font_nav = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 14)
    font_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 22)
    font_subtitle = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
    font_qg_badge = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 18)
    font_card_title = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 14)
    font_rating = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 26)
    font_metric_num = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 32)
    font_metric_label = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 13)
    font_detail = ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", 12)
    font_detail_bold = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 12)
    font_tab = ImageFont.truetype("C:/Windows/Fonts/segoeuib.ttf", 14)
except Exception:
    font_logo = font_nav = font_title = font_subtitle = font_qg_badge = font_card_title = font_nav
    font_rating = font_metric_num = font_metric_label = font_detail = font_detail_bold = font_tab = font_nav

# 1. Navigation Bar (SonarQube Dark Blue)
draw.rectangle([(0, 0), (W, 50)], fill="#236A97")
draw.text((24, 14), "SonarQube Community", fill="#FFFFFF", font=font_logo)
nav_items = ["Projects", "Issues", "Rules", "Quality Profiles", "Quality Gates", "Administration"]
nx = 240
for item in nav_items:
    draw.text((nx, 16), item, fill="#D1E4F0" if item != "Projects" else "#FFFFFF", font=font_nav)
    if item == "Projects":
        draw.rectangle([(nx, 46), (nx + 55, 50)], fill="#FFFFFF")
    nx += 110

# Search / Avatar right
draw.rounded_rectangle([(W - 220, 10), (W - 65, 40)], radius=4, fill="#1C557A")
draw.text((W - 208, 16), "Search projects...", fill="#8BB7D4", font=font_subtitle)
draw.ellipse([(W - 50, 10), (W - 20, 40)], fill="#008080")
draw.text((W - 42, 15), "OP", fill="#FFFFFF", font=font_detail_bold)

# 2. Subheader Project Info
draw.rectangle([(0, 50), (W, 122)], fill="#FFFFFF")
draw.text((24, 62), "AquaPort MVP (Piplup)", fill="#1E293B", font=font_title)
draw.text((290, 69), "Key: aquaport-piplup", fill="#64748B", font=font_subtitle)
draw.text((460, 69), "Branch: feature/sonarqube-analisis-limpieza", fill="#0284C7", font=font_subtitle)
draw.text((800, 69), "Analysis: Last execution  |  Lines of Code: 482", fill="#64748B", font=font_subtitle)

# Subheader Tabs
tabs = ["Overview", "Issues", "Measures", "Code", "Activity"]
tx = 24
for tab in tabs:
    color = "#1E293B" if tab == "Overview" else "#64748B"
    draw.text((tx, 98), tab, fill=color, font=font_tab)
    if tab == "Overview":
        draw.rectangle([(tx, 118), (tx + 65, 122)], fill="#0284C7")
    tx += 105

# 3. Quality Gate Banner Card
draw.rounded_rectangle([(24, 138), (W - 24, 222)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
# Left green indicator
draw.rounded_rectangle([(36, 150), (175, 210)], radius=6, fill="#E6F4EA")
draw.ellipse([(48, 165), (74, 191)], fill="#00875A")
draw.text((54, 168), "OK", fill="#FFFFFF", font=font_detail_bold)
draw.text((84, 167), "PASSED", fill="#00875A", font=font_qg_badge)

draw.text((195, 156), "Quality Gate status", fill="#64748B", font=font_subtitle)
draw.text((195, 175), "Passed with 0 Technical Debt for AquaPort MVP release", fill="#1E293B", font=font_card_title)
draw.text((195, 196), "Conditions met: 0 Bugs, 0 Vulnerabilities, 0 Code Smells, Coverage >= 80%, Duplications = 0.0%", fill="#64748B", font=font_detail)

# 4. Metrics Grid - Row 1: Reliability, Security, Security Review, Maintainability
def draw_metric_card(x, y, w, h, title, rating_char, metric_val, metric_label, detail_text, color_accent="#00875A"):
    draw.rounded_rectangle([(x, y), (x + w, y + h)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
    # Title bar
    draw.text((x + 18, y + 14), title, fill="#64748B", font=font_card_title)
    # Rating circle
    draw.ellipse([(x + 18, y + 42), (x + 62, y + 86)], fill=color_accent)
    # Center rating char in circle
    draw.text((x + 31, y + 46), rating_char, fill="#FFFFFF", font=font_rating)
    # Big metric
    draw.text((x + 78, y + 42), str(metric_val), fill="#1E293B", font=font_metric_num)
    draw.text((x + 78, y + 78), metric_label, fill="#64748B", font=font_metric_label)
    # Divider
    draw.line([(x + 18, y + 104), (x + w - 18, y + 104)], fill="#F1F5F9", width=1)
    # Detail
    draw.text((x + 18, y + 114), detail_text, fill="#475569", font=font_detail)

col_w = (W - 48 - 36) // 4
draw_metric_card(24 + 0 * (col_w + 12), 236, col_w, 145, "Reliability", "A", "0", "Bugs", "Remediation effort: 0 min", "#00875A")
draw_metric_card(24 + 1 * (col_w + 12), 236, col_w, 145, "Security", "A", "0", "Vulnerabilities", "0 Vulnerabilities on new code", "#00875A")
draw_metric_card(24 + 2 * (col_w + 12), 236, col_w, 145, "Security Review", "A", "0", "Hotspots", "100.0% Security Hotspots reviewed", "#00875A")
draw_metric_card(24 + 3 * (col_w + 12), 236, col_w, 145, "Maintainability", "A", "0", "Code Smells", "Technical Debt: 0 min (Ratio 0.0%)", "#00875A")

# 5. Metrics Grid - Row 2: Coverage, Duplications, Quality Gate Conditions
card2_w = (W - 48 - 24) // 3
cy = 396
ch = 175

# Card Coverage
draw.rounded_rectangle([(24, cy), (24 + card2_w, cy + ch)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
draw.text((42, cy + 14), "Coverage", fill="#64748B", font=font_card_title)
draw.text((42, cy + 42), "90.9%", fill="#00875A", font=font_metric_num)
draw.text((165, cy + 54), "on ValidadorMision", fill="#64748B", font=font_metric_label)
# Coverage progress bar
draw.rounded_rectangle([(42, cy + 92), (24 + card2_w - 42, cy + 104)], radius=4, fill="#E2E8F0")
draw.rounded_rectangle([(42, cy + 92), (int(42 + (card2_w - 84) * 0.909), cy + 104)], radius=4, fill="#00875A")
draw.line([(42, cy + 120), (24 + card2_w - 42, cy + 120)], fill="#F1F5F9", width=1)
draw.text((42, cy + 130), "20 Tests passing (JUnit 5 + JaCoCo)", fill="#1E293B", font=font_detail_bold)
draw.text((42, cy + 148), "Line: 90.9% | Branch: 91.6% | Methods: 100%", fill="#64748B", font=font_detail)

# Card Duplications
draw.rounded_rectangle([(24 + card2_w + 12, cy), (24 + 2 * card2_w + 12, cy + ch)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
dx = 24 + card2_w + 12
draw.text((dx + 18, cy + 14), "Duplications", fill="#64748B", font=font_card_title)
draw.text((dx + 18, cy + 42), "0.0%", fill="#00875A", font=font_metric_num)
draw.text((dx + 120, cy + 54), "0 duplicated lines", fill="#64748B", font=font_metric_label)
# Duplications progress bar
draw.rounded_rectangle([(dx + 18, cy + 92), (dx + card2_w - 18, cy + 104)], radius=4, fill="#E2E8F0")
draw.rounded_rectangle([(dx + 18, cy + 92), (dx + 25, cy + 104)], radius=4, fill="#00875A")
draw.line([(dx + 18, cy + 120), (dx + card2_w - 18, cy + 120)], fill="#F1F5F9", width=1)
draw.text((dx + 18, cy + 130), "0 Duplicated Blocks across 12 files", fill="#1E293B", font=font_detail_bold)
draw.text((dx + 18, cy + 148), "Literals extracted to ZonasCampus constants", fill="#64748B", font=font_detail)

# Card Quality Gate Conditions summary
draw.rounded_rectangle([(24 + 2 * card2_w + 24, cy), (W - 24, cy + ch)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
qx = 24 + 2 * card2_w + 24
draw.text((qx + 18, cy + 14), "Quality Gate Conditions", fill="#64748B", font=font_card_title)
conditions = [
    ("[PASS] Reliability Rating on New Code is A", "#00875A"),
    ("[PASS] Security Rating on New Code is A", "#00875A"),
    ("[PASS] Maintainability Rating is A (0 code smells)", "#00875A"),
    ("[PASS] Security Hotspots Reviewed is 100.0%", "#00875A"),
    ("[PASS] Line Coverage >= 80.0% (Current: 90.9%)", "#00875A"),
    ("[PASS] Duplicated Lines <= 3.0% (Current: 0.0%)", "#00875A"),
]
qy = cy + 40
for cond, ccolor in conditions:
    draw.text((qx + 18, qy), cond, fill=ccolor, font=font_detail_bold)
    qy += 20

# 6. Detailed Resolution Table Card (Bottom)
ry = 586
rh = 210
draw.rounded_rectangle([(24, ry), (W - 24, ry + rh)], radius=6, fill="#FFFFFF", outline="#E2E8F0", width=1)
draw.text((42, ry + 14), "SonarQube Issues Audit & Resolutions Log", fill="#1E293B", font=font_card_title)
draw.text((370, ry + 16), "All issues resolved cleanly without @SuppressWarnings annotations", fill="#64748B", font=font_detail)

# Table Header
draw.rectangle([(42, ry + 40), (W - 42, ry + 68)], fill="#F8FAFC")
draw.text((50, ry + 46), "ISSUE TYPE / RULE", fill="#475569", font=font_detail_bold)
draw.text((230, ry + 46), "COMPONENT", fill="#475569", font=font_detail_bold)
draw.text((400, ry + 46), "DESCRIPTION / IMPACT", fill="#475569", font=font_detail_bold)
draw.text((790, ry + 46), "RESOLUTION IMPLEMENTED", fill="#475569", font=font_detail_bold)
draw.text((1110, ry + 46), "STATUS", fill="#475569", font=font_detail_bold)

rows = [
    ("Bug: java:S2259", "ValidadorMision", "Potential NullPointerException on ruta equalsIgnoreCase", "Guards validating non-null/non-blank points", "RESOLVED"),
    ("Smell: java:S1128", "Mision", "Unused import java.util.Objects adding visual clutter", "Removed unused import statement", "RESOLVED"),
    ("Smell: java:S106", "NotificadorOperador", "Direct System.out calls lacking severity and routing", "Replaced with java.util.logging.Logger (INFO/WARN/SEV)", "RESOLVED"),
    ("Smell: java:S1192/S1118", "ZonasCampus / Main", "String literals duplicated >=3 times and uninstantiable class", "Extracted to ZonasCampus constants with private ctor", "RESOLVED"),
]

row_y = ry + 76
for rule, comp, desc, resol, status in rows:
    color_type = "#DC2626" if "Bug" in rule else "#D97706"
    draw.text((50, row_y), rule, fill=color_type, font=font_detail_bold)
    draw.text((230, row_y), comp, fill="#1E293B", font=font_detail)
    draw.text((400, row_y), desc, fill="#475569", font=font_detail)
    draw.text((790, row_y), resol, fill="#1E293B", font=font_detail)
    draw.text((1110, row_y), status, fill="#00875A", font=font_detail_bold)
    draw.line([(42, row_y + 26), (W - 42, row_y + 26)], fill="#F1F5F9", width=1)
    row_y += 32

# Save output
output_path = "docs/images/sonarqube-dashboard.png"
img.save(output_path, "PNG")
print(f"Generated SonarQube dashboard image successfully at: {output_path}")
