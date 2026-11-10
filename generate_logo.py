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

    def render_mc_text(text, color, bold=False, shadow=True, scale=3):
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

    def build_badge(prefix, p_col, nick, n_col, icon, suffix, s_col, scale=3):
        p_img = render_mc_text(prefix, p_col, bold=True, scale=scale)
        n_img = render_mc_text(nick, n_col, bold=True, scale=scale)
        i_scaled = icon.resize((int(11 * scale), int(11 * scale)), Image.Resampling.NEAREST)
        s_img = render_mc_text(suffix, s_col, bold=True, scale=scale)

        gap = int(2.6 * scale)
        pad_x = int(4 * scale)
        pad_y = int(2.4 * scale)
        cw = p_img.width + gap + n_img.width + gap + i_scaled.width + gap + s_img.width
        bw = cw + pad_x * 2
        bh = max(p_img.height, n_img.height, i_scaled.height, s_img.height) + pad_y * 2

        badge = Image.new('RGBA', (bw, bh), (0, 0, 0, 0))
        draw_b = ImageDraw.Draw(badge)
        draw_b.rounded_rectangle([(0, 0), (bw - 1, bh - 1)], radius=6, fill=(10, 20, 40, 225), outline=(0, 60, 130, 240), width=2)

        bx = pad_x
        badge.paste(p_img, (bx, (bh - p_img.height) // 2), p_img)
        bx += p_img.width + gap
        badge.paste(n_img, (bx, (bh - n_img.height) // 2), n_img)
        bx += n_img.width + gap
        badge.paste(i_scaled, (bx, (bh - i_scaled.height) // 2), i_scaled)
        bx += i_scaled.width + gap
        badge.paste(s_img, (bx, (bh - s_img.height) // 2), s_img)
        return badge

    characters = [
        {'name': 'steve', 'skin': skins['steve'], 'prefix': '[LEADER]', 'prefix_col': GOLD, 'nick': 'Steve', 'nick_col': AQUA, 'icon': items['diamond_sword'], 'suffix': '[#1]', 'suffix_col': YELLOW},
        {'name': 'alex', 'skin': skins['alex'], 'prefix': '[OFFICER]', 'prefix_col': GREEN, 'nick': 'Alex', 'nick_col': GOLD, 'icon': items['golden_apple'], 'suffix': '[VIP]', 'suffix_col': LIGHT_PURPLE},
        {'name': 'noor', 'skin': skins['noor'], 'prefix': '[CHAMPION]', 'prefix_col': RED, 'nick': 'Noor', 'nick_col': YELLOW, 'icon': items['netherite_sword'], 'suffix': '[MVP]', 'suffix_col': AQUA},
        {'name': 'sunny', 'skin': skins['sunny'], 'prefix': '[ELITE]', 'prefix_col': LIGHT_PURPLE, 'nick': 'Sunny', 'nick_col': GREEN, 'icon': items['emerald'], 'suffix': '[OG]', 'suffix_col': GOLD},
        {'name': 'ari', 'skin': skins['ari'], 'prefix': '[WARRIOR]', 'prefix_col': DARK_AQUA, 'nick': 'Ari', 'nick_col': RED, 'icon': items['bow'], 'suffix': '[TOP]', 'suffix_col': YELLOW},
        {'name': 'zuri', 'skin': skins['zuri'], 'prefix': '[CAPTAIN]', 'prefix_col': YELLOW, 'nick': 'Zuri', 'nick_col': AQUA, 'icon': items['totem_of_undying'], 'suffix': '[GOD]', 'suffix_col': LIGHT_PURPLE},
        {'name': 'makena', 'skin': skins['makena'], 'prefix': '[VETERAN]', 'prefix_col': DARK_GREEN, 'nick': 'Makena', 'nick_col': LIGHT_PURPLE, 'icon': items['ender_pearl'], 'suffix': '[WIN]', 'suffix_col': RED},
        {'name': 'kai', 'skin': skins['kai'], 'prefix': '[MASTER]', 'prefix_col': GOLD, 'nick': 'Kai', 'nick_col': RED, 'icon': items['diamond'], 'suffix': '[PRO]', 'suffix_col': AQUA},
        {'name': 'efe', 'skin': skins['efe'], 'prefix': '[LEGEND]', 'prefix_col': BLUE, 'nick': 'Efe', 'nick_col': GOLD, 'icon': items['nether_star'], 'suffix': '[MVP+]', 'suffix_col': GREEN}
    ]

    W, H = 512, 512
    BG_COLOR = (0, 148, 245)
    BG_BORDER = (0, 75, 165)
    BG_HIGHLIGHT = (70, 195, 255)

    # Drifting background particles for square animation
    random.seed(1337)
    particle_data = []
    for _ in range(30):
        px = random.randint(24, W - 24)
        py = random.randint(30, H - 30)
        size = random.choice([2, 3, 4])
        speed = random.uniform(0.5, 1.2)
        alpha = random.randint(90, 180)
        particle_data.append([px, py, size, speed, alpha])

    title_img = render_mc_text('SIMPLY TEAMS', WHITE, bold=True, shadow=True, scale=5)
    title_x = (W - title_img.width) // 2
    title_y = 425

    frames = []
    bob_offsets = [0, -2, -4, -5, -3, -1, 1, 0]

    for char_idx, char in enumerate(characters):
        raw_head = extract_head(char['skin'])
        head_scaled = raw_head.resize((280, 280), Image.Resampling.NEAREST)
        badge = build_badge(char['prefix'], char['prefix_col'], char['nick'], char['nick_col'], char['icon'], char['suffix'], char['suffix_col'], scale=3)

        for f_idx, bob_y in enumerate(bob_offsets):
            global_frame_idx = char_idx * len(bob_offsets) + f_idx

            frame = Image.new('RGBA', (W, H), BG_COLOR)
            f_draw = ImageDraw.Draw(frame)

            # Floating particles
            for p in particle_data:
                py_curr = (p[1] - global_frame_idx * p[3] * 3) % (H - 50) + 25
                px_curr = p[0] + math.sin(global_frame_idx * 0.15 + p[1]) * 3
                sz = p[2]
                f_draw.rectangle([(px_curr, py_curr), (px_curr + sz, py_curr + sz)], fill=(255, 255, 255, p[4]))

            # Frame borders
            border_w = 10
            f_draw.rectangle([(0, 0), (W - 1, H - 1)], outline=BG_BORDER, width=border_w)
            f_draw.rectangle([(border_w, border_w), (W - 1 - border_w, H - 1 - border_w)], outline=BG_HIGHLIGHT, width=2)

            # Corner accents
            acc_size = 20
            f_draw.rectangle([(0, 0), (acc_size, acc_size)], fill=BG_BORDER)
            f_draw.rectangle([(W - acc_size, 0), (W, acc_size)], fill=BG_BORDER)
            f_draw.rectangle([(0, H - acc_size), (acc_size, H)], fill=BG_BORDER)
            f_draw.rectangle([(W - acc_size, H - acc_size), (W, H)], fill=BG_BORDER)

            # Floating title badge
            badge_x = (W - badge.width) // 2
            badge_y = 20 + bob_y
            f_draw.rounded_rectangle([(badge_x + 3, badge_y + 4), (badge_x + badge.width + 3, badge_y + badge.height + 4)],
                                     radius=6, fill=(0, 45, 110, 110))
            frame.paste(badge, (badge_x, badge_y), badge)

            # Head
            head_x = (W - head_scaled.width) // 2
            head_y = 80 + (bob_y // 2)

            # Drop shadow
            shadow_w = 250
            shadow_h = 24
            shadow_x = (W - shadow_w) // 2
            shadow_y = head_y + head_scaled.height - 6
            f_draw.ellipse([(shadow_x, shadow_y), (shadow_x + shadow_w, shadow_y + shadow_h)], fill=(0, 50, 120, 120))

            frame.paste(head_scaled, (head_x, head_y), head_scaled)

            # Bottom Title: SIMPLY TEAMS (no other text)
            frame.paste(title_img, (title_x, title_y), title_img)

            frames.append(frame.convert('RGB'))

    # Save animated GIF (512x512)
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

    # Save square icon
    icon_path = 'src/main/resources/icon.png'
    os.makedirs('src/main/resources', exist_ok=True)
    frames[0].save(icon_path)
    print(f"Created {icon_path}.")

    # Generate in-game banner (800x280)
    BW, BH = 800, 280
    banner = Image.new('RGBA', (BW, BH), BG_COLOR)
    draw_banner = ImageDraw.Draw(banner)

    # Banner particles
    random.seed(42)
    for _ in range(40):
        px = random.randint(16, BW - 16)
        py = random.randint(16, BH - 16)
        sz = random.choice([2, 3, 4])
        alpha = random.randint(70, 160)
        draw_banner.rectangle([(px, py), (px + sz, py + sz)], fill=(255, 255, 255, alpha))

    # Banner borders
    b_border_w = 8
    draw_banner.rectangle([(0, 0), (BW - 1, BH - 1)], outline=BG_BORDER, width=b_border_w)
    draw_banner.rectangle([(b_border_w, b_border_w), (BW - 1 - b_border_w, BH - 1 - b_border_w)], outline=BG_HIGHLIGHT, width=2)
    b_acc_size = 18
    draw_banner.rectangle([(0, 0), (b_acc_size, b_acc_size)], fill=BG_BORDER)
    draw_banner.rectangle([(BW - b_acc_size, 0), (BW, b_acc_size)], fill=BG_BORDER)
    draw_banner.rectangle([(0, BH - b_acc_size), (b_acc_size, BH)], fill=BG_BORDER)
    draw_banner.rectangle([(BW - b_acc_size, BH - b_acc_size), (BW, BH)], fill=BG_BORDER)

    # Banner Steve Head & Badge
    steve_head = extract_head(skins['steve']).resize((184, 184), Image.Resampling.NEAREST)
    steve_badge = build_badge('[LEADER]', GOLD, 'Steve', AQUA, items['diamond_sword'], '[#1]', YELLOW, scale=2)

    col_center_x = 175
    s_badge_x = col_center_x - steve_badge.width // 2
    s_badge_y = 20
    banner.paste(steve_badge, (s_badge_x, s_badge_y), steve_badge)

    s_head_x = col_center_x - steve_head.width // 2
    s_head_y = 70
    draw_banner.ellipse([(s_head_x + 10, s_head_y + steve_head.height - 8),
                         (s_head_x + steve_head.width - 10, s_head_y + steve_head.height + 12)],
                        fill=(0, 50, 120, 120))
    banner.paste(steve_head, (s_head_x, s_head_y), steve_head)

    # Banner big SIMPLY TEAMS (scale 5)
    banner_title = render_mc_text('SIMPLY TEAMS', WHITE, bold=True, shadow=True, scale=5)
    banner_title_x = 345
    banner_title_y = (BH - banner_title.height) // 2
    banner.paste(banner_title, (banner_title_x, banner_title_y), banner_title)

    # Save in-game logo.png banner to root and src/main/resources/logo.png
    banner_rgb = banner.convert('RGB')
    banner_rgb.save('logo.png')
    banner_rgb.save('src/main/resources/logo.png')
    print("Created banner logo.png and src/main/resources/logo.png.")

if __name__ == '__main__':
    create_mod_logo()
