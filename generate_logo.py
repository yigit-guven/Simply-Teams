import os
import io
import zipfile
import math
import random
from PIL import Image, ImageDraw

def create_mod_logo():
    jar_path = r'C:\Users\yigit\.gradle\caches\fabric-loom\1.20.1\minecraft-client.jar'
    if not os.path.exists(jar_path):
        raise FileNotFoundError(f"Minecraft client jar not found at {jar_path}")

    with zipfile.ZipFile(jar_path, 'r') as z:
        # Load font
        font_data = z.read('assets/minecraft/textures/font/ascii.png')
        font_img = Image.open(io.BytesIO(font_data)).convert('RGBA')

        # Load skins
        skins = {}
        for name in ['steve', 'alex', 'noor', 'sunny', 'ari', 'zuri', 'makena', 'kai', 'efe']:
            path = f'assets/minecraft/textures/entity/player/slim/{name}.png'
            skins[name] = Image.open(io.BytesIO(z.read(path))).convert('RGBA')

        # Load items
        items = {}
        item_names = ['diamond_sword', 'golden_apple', 'netherite_sword', 'emerald', 'bow', 'totem_of_undying', 'ender_pearl', 'diamond', 'nether_star']
        for item in item_names:
            path = f'assets/minecraft/textures/item/{item}.png'
            items[item] = Image.open(io.BytesIO(z.read(path))).convert('RGBA')

    # Color palette
    GOLD = (255, 170, 0, 255)
    AQUA = (85, 255, 255, 255)
    GREEN = (85, 255, 85, 255)
    RED = (255, 85, 85, 255)
    LIGHT_PURPLE = (255, 85, 255, 255)
    YELLOW = (255, 255, 85, 255)
    WHITE = (255, 255, 255, 255)
    DARK_AQUA = (0, 170, 170, 255)
    DARK_GREEN = (0, 170, 0, 255)
    BLUE = (85, 85, 255, 255)

    def get_glyph(ch):
        val = ord(ch) if ord(ch) < 256 else ord('?')
        col = val % 16
        row = val // 16
        glyph = font_img.crop((col * 8, row * 8, col * 8 + 8, row * 8 + 8))
        bbox = glyph.getbbox()
        width = 4 if bbox is None else max(bbox[2] + 1, 2)
        return glyph, width

    def render_mc_text(text, color, bold=False, shadow=True, scale=2):
        chars = []
        total_w = 0
        for ch in text:
            g, w = get_glyph(ch)
            char_w = w + (1 if bold else 0)
            chars.append((ch, g, w, char_w))
            total_w += char_w

        img_h = 10
        img = Image.new('RGBA', (total_w + (2 if shadow else 0), img_h), (0, 0, 0, 0))
        shadow_col = (color[0] // 4, color[1] // 4, color[2] // 4, 255)

        cur_x = 0
        for ch, g, w, char_w in chars:
            if ch != ' ':
                if shadow:
                    for x in range(w):
                        for y in range(8):
                            if g.getpixel((x, y))[3] > 0:
                                img.putpixel((cur_x + x + 1, y + 1), shadow_col)
                                if bold:
                                    img.putpixel((cur_x + x + 2, y + 1), shadow_col)
                for x in range(w):
                    for y in range(8):
                        if g.getpixel((x, y))[3] > 0:
                            img.putpixel((cur_x + x, y), color)
                            if bold:
                                img.putpixel((cur_x + x + 1, y), color)
            cur_x += char_w

        if scale > 1:
            img = img.resize((img.width * scale, img.height * scale), Image.Resampling.NEAREST)
        return img

    def extract_head(skin_img):
        face = skin_img.crop((8, 8, 16, 16))
        hat = skin_img.crop((40, 8, 48, 16))
        head = Image.new('RGBA', (8, 8), (0, 0, 0, 0))
        head.paste(face, (0, 0))
        head.paste(hat, (0, 0), hat)
        return head

    # Define characters with ASCII-safe suffixes and distinct cosmetics
    characters = [
        {
            "name": "steve",
            "skin": skins["steve"],
            "prefix": "[LEADER]",
            "prefix_col": GOLD,
            "nick": "Steve",
            "nick_col": AQUA,
            "icon": items["diamond_sword"],
            "suffix": "[#1]",
            "suffix_col": YELLOW
        },
        {
            "name": "alex",
            "skin": skins["alex"],
            "prefix": "[OFFICER]",
            "prefix_col": GREEN,
            "nick": "Alex",
            "nick_col": GOLD,
            "icon": items["golden_apple"],
            "suffix": "[VIP]",
            "suffix_col": LIGHT_PURPLE
        },
        {
            "name": "noor",
            "skin": skins["noor"],
            "prefix": "[CHAMPION]",
            "prefix_col": RED,
            "nick": "Noor",
            "nick_col": YELLOW,
            "icon": items["netherite_sword"],
            "suffix": "[MVP]",
            "suffix_col": AQUA
        },
        {
            "name": "sunny",
            "skin": skins["sunny"],
            "prefix": "[ELITE]",
            "prefix_col": LIGHT_PURPLE,
            "nick": "Sunny",
            "nick_col": GREEN,
            "icon": items["emerald"],
            "suffix": "[OG]",
            "suffix_col": GOLD
        },
        {
            "name": "ari",
            "skin": skins["ari"],
            "prefix": "[WARRIOR]",
            "prefix_col": DARK_AQUA,
            "nick": "Ari",
            "nick_col": RED,
            "icon": items["bow"],
            "suffix": "[TOP]",
            "suffix_col": YELLOW
        },
        {
            "name": "zuri",
            "skin": skins["zuri"],
            "prefix": "[CAPTAIN]",
            "prefix_col": YELLOW,
            "nick": "Zuri",
            "nick_col": AQUA,
            "icon": items["totem_of_undying"],
            "suffix": "[GOD]",
            "suffix_col": LIGHT_PURPLE
        },
        {
            "name": "makena",
            "skin": skins["makena"],
            "prefix": "[VETERAN]",
            "prefix_col": DARK_GREEN,
            "nick": "Makena",
            "nick_col": LIGHT_PURPLE,
            "icon": items["ender_pearl"],
            "suffix": "[WIN]",
            "suffix_col": RED
        },
        {
            "name": "kai",
            "skin": skins["kai"],
            "prefix": "[MASTER]",
            "prefix_col": GOLD,
            "nick": "Kai",
            "nick_col": RED,
            "icon": items["diamond"],
            "suffix": "[PRO]",
            "suffix_col": AQUA
        },
        {
            "name": "efe",
            "skin": skins["efe"],
            "prefix": "[LEGEND]",
            "prefix_col": BLUE,
            "nick": "Efe",
            "nick_col": GOLD,
            "icon": items["nether_star"],
            "suffix": "[MVP+]",
            "suffix_col": GREEN
        }
    ]

    W, H = 512, 512
    BG_COLOR = (0, 148, 245)       # Vibrant bright blue
    BG_BORDER = (0, 75, 165)       # Darker blue frame
    BG_HIGHLIGHT = (70, 195, 255)  # Cyan highlight line

    # Fixed seed particles for subtle drifting effect
    random.seed(1337)
    particle_data = []
    for _ in range(30):
        px = random.randint(24, W - 24)
        py = random.randint(30, H - 30)
        size = random.choice([2, 3, 4])
        speed = random.uniform(0.5, 1.2)
        alpha = random.randint(90, 180)
        particle_data.append([px, py, size, speed, alpha])

    # Pre-render Title
    title_img = render_mc_text("SIMPLY TEAMS", WHITE, bold=True, shadow=True, scale=4)
    subtitle_img = render_mc_text("SERVER-SIDED TEAM SYSTEM", (210, 240, 255, 255), bold=False, shadow=True, scale=2)
    footer_img = render_mc_text("NEOFORGE 1.21.1", (170, 220, 255, 200), bold=False, shadow=True, scale=2)

    frames = []

    # 8 frames per character: subtle Minecraft living bobbing
    bob_offsets = [0, -3, -6, -8, -5, -2, 1, 0]

    for char_idx, char in enumerate(characters):
        raw_head = extract_head(char["skin"])
        # 26x scale = 208x208 pixel-sharp head
        head_scaled = raw_head.resize((208, 208), Image.Resampling.NEAREST)

        # Build nametag badge directly on top of the head
        prefix_img = render_mc_text(char["prefix"], char["prefix_col"], bold=True, shadow=True, scale=2)
        nick_img = render_mc_text(char["nick"], char["nick_col"], bold=True, shadow=True, scale=2)
        icon_scaled = char["icon"].resize((24, 24), Image.Resampling.NEAREST)
        suffix_img = render_mc_text(char["suffix"], char["suffix_col"], bold=True, shadow=True, scale=2)

        gap = 8
        nametag_content_w = prefix_img.width + gap + nick_img.width + gap + icon_scaled.width + gap + suffix_img.width
        badge_pad_x = 18
        badge_pad_y = 9
        badge_w = nametag_content_w + badge_pad_x * 2
        badge_h = max(prefix_img.height, nick_img.height, icon_scaled.height, suffix_img.height) + badge_pad_y * 2

        badge = Image.new('RGBA', (badge_w, badge_h), (0, 0, 0, 0))
        draw_b = ImageDraw.Draw(badge)
        draw_b.rounded_rectangle([(0, 0), (badge_w - 1, badge_h - 1)], radius=6, fill=(10, 20, 40, 215), outline=(0, 60, 130, 240), width=2)

        bx = badge_pad_x
        # Prefix
        py = (badge_h - prefix_img.height) // 2
        badge.paste(prefix_img, (bx, py), prefix_img)
        bx += prefix_img.width + gap

        # Nick
        ny = (badge_h - nick_img.height) // 2
        badge.paste(nick_img, (bx, ny), nick_img)
        bx += nick_img.width + gap

        # Icon
        iy = (badge_h - icon_scaled.height) // 2
        badge.paste(icon_scaled, (bx, iy), icon_scaled)
        bx += icon_scaled.width + gap

        # Suffix
        sy = (badge_h - suffix_img.height) // 2
        badge.paste(suffix_img, (bx, sy), suffix_img)

        # Render 8 animation frames for this character
        for f_idx, bob_y in enumerate(bob_offsets):
            global_frame_idx = char_idx * len(bob_offsets) + f_idx

            frame = Image.new('RGBA', (W, H), BG_COLOR)
            f_draw = ImageDraw.Draw(frame)

            # Draw drifting particles
            for p in particle_data:
                # Update position based on global frame
                py_curr = (p[1] - global_frame_idx * p[3] * 3) % (H - 50) + 25
                px_curr = p[0] + math.sin(global_frame_idx * 0.15 + p[1]) * 3
                sz = p[2]
                f_draw.rectangle([(px_curr, py_curr), (px_curr + sz, py_curr + sz)], fill=(255, 255, 255, p[4]))

            # Decorative border
            border_w = 10
            f_draw.rectangle([(0, 0), (W - 1, H - 1)], outline=BG_BORDER, width=border_w)
            f_draw.rectangle([(border_w, border_w), (W - 1 - border_w, H - 1 - border_w)], outline=BG_HIGHLIGHT, width=2)

            # Corner accents
            acc_size = 20
            f_draw.rectangle([(0, 0), (acc_size, acc_size)], fill=BG_BORDER)
            f_draw.rectangle([(W - acc_size, 0), (W, acc_size)], fill=BG_BORDER)
            f_draw.rectangle([(0, H - acc_size), (acc_size, H)], fill=BG_BORDER)
            f_draw.rectangle([(W - acc_size, H - acc_size), (W, H)], fill=BG_BORDER)

            # Title
            title_x = (W - title_img.width) // 2
            title_y = 26
            frame.paste(title_img, (title_x, title_y), title_img)

            # Subtitle
            sub_x = (W - subtitle_img.width) // 2
            sub_y = title_y + title_img.height + 6
            frame.paste(subtitle_img, (sub_x, sub_y), subtitle_img)

            # Divider line
            div_y = sub_y + subtitle_img.height + 12
            f_draw.line([(60, div_y), (W - 60, div_y)], fill=(35, 175, 255), width=2)

            # Floating Nametag Badge directly on top of the head
            badge_x = (W - badge.width) // 2
            badge_y = div_y + 18 + bob_y
            # Shadow behind badge
            f_draw.rounded_rectangle([(badge_x + 3, badge_y + 4), (badge_x + badge.width + 3, badge_y + badge.height + 4)],
                                     radius=6, fill=(0, 45, 110, 110))
            frame.paste(badge, (badge_x, badge_y), badge)

            # Head position (below nametag)
            head_x = (W - head_scaled.width) // 2
            head_y = badge_y + badge.height + 14 + (bob_y // 2)

            # Ground drop shadow
            shadow_w = 180
            shadow_h = 24
            shadow_x = (W - shadow_w) // 2
            shadow_y = head_y + head_scaled.height - 6
            f_draw.ellipse([(shadow_x, shadow_y), (shadow_x + shadow_w, shadow_y + shadow_h)], fill=(0, 50, 120, 120))

            # Paste head
            frame.paste(head_scaled, (head_x, head_y), head_scaled)

            # Footer
            foot_x = (W - footer_img.width) // 2
            foot_y = H - 32
            frame.paste(footer_img, (foot_x, foot_y), footer_img)

            frames.append(frame.convert('RGB'))

    # Save animated GIF
    gif_path = 'logo.gif'
    frames[0].save(
        gif_path,
        save_all=True,
        append_images=frames[1:],
        duration=100,
        loop=0,
        optimize=True
    )
    print(f"Created {gif_path}: {len(frames)} frames.")

    # Save static PNG
    png_path = 'logo.png'
    frames[0].save(png_path)
    print(f"Created {png_path}.")

    # Save resource icon
    res_icon_path = 'src/main/resources/icon.png'
    os.makedirs('src/main/resources', exist_ok=True)
    frames[0].save(res_icon_path)
    print(f"Created {res_icon_path}.")

if __name__ == '__main__':
    create_mod_logo()
