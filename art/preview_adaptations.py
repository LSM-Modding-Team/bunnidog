"""The approved geometry in a dim scene: carried glow versus an expressed self-lit coat.

Software approximation of block lighting; never rewrites meshes, atlases or reference art.
"""
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw
from preview_bunnidog import world_faces, render, font, ROOT


def main():
    root = Path(ROOT)
    faces = world_faces()
    sheet = Image.new('RGB',(1080,650),(16,18,24)); d = ImageDraw.Draw(sheet)
    d.text((30,20),'INHERITED GLOW',font=font(24),fill=(215,226,213))
    for x,coat,brightness,label in [(0,'tan',.18,'Hidden glow: ordinary coat lighting'),
                                     (540,'glow',1.0,'Expressed glow: self-lit coat')]:
        texture = np.asarray(Image.open(root/f'src/main/resources/assets/bunnidogs/textures/entity/bunnidog_{coat}.png'),dtype=float)
        panel = render(faces,texture,-32,9,540,520,23,nearest=True,scene_light=brightness,background_rgb=(16,18,24))
        sheet.paste(panel,(x,65))
        d.text((x+22,580),label,font=font(17),fill=(215,226,213))
    d.text((30,620),'Software lighting preview; native in-game lighting remains gameplay review.',font=font(14),fill=(152,163,158))
    sheet.save(root/'art/adaptations_preview.png')


if __name__ == '__main__': main()
