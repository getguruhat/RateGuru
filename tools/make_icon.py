"""Draws the RateGuru icon: python3 tools/make_icon.py <out-dir> (needs Pillow, macOS SF Rounded font)."""
import sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ORANGE_TOP=(255,154,61); ORANGE_BOT=(240,92,10); WHITE=(255,255,255)
BRAND=(255,118,37)
FONT='/System/Library/Fonts/SFNSRounded.ttf'

def gradient(n):
    im=Image.new('RGB',(n,n)); px=im.load()
    for y in range(n):
        for x in range(n):
            t=(x+y)/(2*(n-1))
            px[x,y]=tuple(round(a+(b-a)*t) for a,b in zip(ORANGE_TOP,ORANGE_BOT))
    return im

def font(size):
    f=ImageFont.truetype(FONT,size)
    try: f.set_variation_by_name('Bold')
    except Exception as e: print('no bold',e)
    return f

def hat(d, cx, cy, s, fill, hole):
    # GuruHat top hat, from the 108-unit themed-icon path, centred at (cx,cy), s px per unit.
    X=lambda u: cx+(u-54)*s; Y=lambda v: cy+(v-50)*s
    d.ellipse([X(26),Y(61),X(82),Y(76)],fill=fill)              # brim
    d.polygon([(X(36.5),Y(27)),(X(39),Y(66)),(X(54),Y(69)),(X(69),Y(66)),(X(71.5),Y(27))],fill=fill)  # crown
    d.ellipse([X(36.5),Y(22.8),X(71.5),Y(31.2)],fill=fill)      # top
    for u in (46,54,62):
        d.ellipse([X(u-3.4),Y(42-3.4),X(u+3.4),Y(42+3.4)],fill=hole)

def foreground(n, span):
    """Transparent layer: four symbols, divider cross, centre badge. span = fraction of n used."""
    S=4; N=n*S
    im=Image.new('RGBA',(N,N),(0,0,0,0)); d=ImageDraw.Draw(im)
    c=N/2; half=N*span/2
    # divider cross, broken around the badge
    w=N*0.012; badge=N*span*0.17; gap=badge*1.35
    a=(255,255,255,90)
    d.rounded_rectangle([c-w/2,c-half,c+w/2,c-gap],radius=w/2,fill=a)
    d.rounded_rectangle([c-w/2,c+gap,c+w/2,c+half],radius=w/2,fill=a)
    d.rounded_rectangle([c-half,c-w/2,c-gap,c+w/2],radius=w/2,fill=a)
    d.rounded_rectangle([c+gap,c-w/2,c+half,c+w/2],radius=w/2,fill=a)
    f=font(int(N*span*0.33))
    q=half*0.52
    for sym,(sx,sy) in zip("$¥€£",[(-1,-1),(1,-1),(-1,1),(1,1)]):
        d.text((c+sx*q,c+sy*q),sym,font=f,fill=WHITE,anchor='mm')
    # centre badge: white disc, orange hat
    d.ellipse([c-badge,c-badge,c+badge,c+badge],fill=WHITE)
    hat(d,c,c+badge*0.03,badge/45,BRAND,WHITE)
    return im.resize((n,n),Image.LANCZOS)

out=sys.argv[1]
# Android adaptive layers at xxxhdpi (108dp * 4). Safe zone is the middle 66dp.
fg=foreground(432,0.60); fg.save(f'{out}/ic_launcher_fg.png')
gradient(432).save(f'{out}/ic_launcher_bg.png')
# Master icon for README/website: rounded square.
M=1024
m=gradient(M).convert('RGBA'); m.alpha_composite(foreground(M,0.80))
mask=Image.new('L',(M,M),0); ImageDraw.Draw(mask).rounded_rectangle([0,0,M-1,M-1],radius=230,fill=255)
master=Image.new('RGBA',(M,M),(0,0,0,0)); master.paste(m,(0,0),mask); master.save(f'{out}/icon.png')
# Launcher preview: circle mask over the visible 72dp.
pv=gradient(432).convert('RGBA'); pv.alpha_composite(fg); pv=pv.crop((72,72,360,360))
cm=Image.new('L',pv.size,0); ImageDraw.Draw(cm).ellipse([0,0,287,287],fill=255)
prev=Image.new('RGB',(288*2+30,288),(240,240,240)); prev.paste(pv,(0,0),cm)
sm=pv.resize((96,96),Image.LANCZOS); cm2=cm.resize((96,96)); prev.paste(sm,(318,96),cm2)
prev.save(f'{out}/preview.png')
