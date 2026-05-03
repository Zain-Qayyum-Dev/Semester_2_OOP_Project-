package Assigment_3_OOP;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Enemy {

    public static final int BASE_HP = 4;
    public static final int BOSS_HP = BASE_HP * 30;

    // 3 seconds at 60fps = 180 ticks for small enemies
    private static final int SHOOT_DELAY_SMALL = 120;
    // 7 seconds at 60fps = 420 ticks for boss / all-direction fire
    private static final int SHOOT_DELAY_BOSS  = 180;

    public enum Type {
        CITY_1_PATROL, CITY_2_BRICK, CITY_3_BRICK, CITY_4_PATROL,
        DESERT_1_PATROL, DESERT_2_PATROL, DESERT_3_STONE, DESERT_4_FIRE, DESERT_5_PATROL,
        FOREST_1_PATROL, FOREST_2_WATER, FOREST_3_PATROL, FOREST_4_PATROL, FOREST_5_PLANT,
        VOLCANO_1_PATROL, VOLCANO_2_STONE, VOLCANO_3_PATROL, VOLCANO_4_FIRE,
        BOSS_CITY, BOSS_DESERT, BOSS_FOREST, BOSS_VOLCANO
    }

    public double x, y;
    public int    width, height;
    public double speed;
    public double leftBound, rightBound;
    public int    screenHeight;
    public boolean movingRight = true;
    public int    hp;
    public boolean alive = true;
    public int    hitFlashTimer = 0;

    private final Type type;
    private BufferedImage sprite;
    // Pre-processed sprite with checkerboard removed (transparent BG)
    private BufferedImage cleanSprite;

    private int shootTimer = 0;
    public  List<EnemyProjectile> projectiles = new ArrayList<>();

    // ── Bob / hover animation ────────────────────────────────────────────────
    private int   animTick  = 0;
    private float bobOffset = 0f;

    public Enemy(double x, double y,
                 double leftBound, double rightBound,
                 double speed, int screenHeight, Type type) {
        this.x = x;  this.y = y;
        this.leftBound   = leftBound;
        this.rightBound  = rightBound;
        this.speed       = speed;
        this.screenHeight = screenHeight;
        this.type        = type;

        boolean isBoss = isBossType(type);
        width  = isBoss ? 346  : 68;
        height = isBoss ? 346  : 68;
        hp     = isBoss ? BOSS_HP : BASE_HP;

        // Stagger shoot timers
        int delay = isBoss ? SHOOT_DELAY_BOSS : SHOOT_DELAY_SMALL;
        shootTimer = (int)(Math.random() * delay);

        loadSprite();
    }

    private static boolean isBossType(Type t) {
        return t == Type.BOSS_CITY || t == Type.BOSS_DESERT
            || t == Type.BOSS_FOREST || t == Type.BOSS_VOLCANO;
    }

    // ── Sprite loading + checkerboard removal ────────────────────────────────
    private void loadSprite() {
        String file = spriteFile(type);
        if (file == null) return;
        try {
            BufferedImage raw = ImageIO.read(getClass().getResourceAsStream(file));
            if (raw != null) {
                cleanSprite = removeCheckerboard(raw);
                sprite = cleanSprite;
            }
        } catch (Exception e) {
            sprite = null;
        }
    }

    /**
     * Removes the checkerboard / grey background from a sprite.
     * Any pixel that is either:
     *   – a near-grey alternating colour (the checker pattern), or
     *   – a near-white background
     * is made fully transparent.
     * The heuristic: if R ≈ G ≈ B and brightness > 180 → background.
     * Also handles the dark-grey checker squares (brightness 110-170, R≈G≈B).
     */
    private static BufferedImage removeCheckerboard(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        // Sample corners to guess background colour range
        // We treat any nearly-achromatic pixel (low saturation) with no
        // strong hue as background.
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int argb = src.getRGB(px, py);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb        & 0xFF;

                // Already transparent
                if (a < 30) { out.setRGB(px, py, 0); continue; }

                // Check if the pixel is a near-grey (checker / solid bg)
                int maxC = Math.max(r, Math.max(g, b));
                int minC = Math.min(r, Math.min(g, b));
                int sat  = maxC - minC; // colour saturation (0 = pure grey)
                int bright = (r + g + b) / 3;

                // Pure greys / whites / light-greys: typical checker colours
                // Light checker ~(204,204,204) and dark checker ~(153,153,153)
                // Also near-white bg like (240,240,240)
                boolean isLightChecker = (sat <= 18 && bright >= 140);
                // Very light whites
                boolean isWhiteBg      = (sat <= 10 && bright >= 230);

                if (isLightChecker || isWhiteBg) {
                    out.setRGB(px, py, 0);
                } else {
                    out.setRGB(px, py, argb);
                }
            }
        }
        return out;
    }

    private static String spriteFile(Type t) {
        switch (t) {
            case CITY_1_PATROL: return "Enemy_1_City.png";
            case CITY_2_BRICK:  return "Enemy_2_City.png";
            case CITY_3_BRICK:  return "Enemy_3_City.png";
            case CITY_4_PATROL: return "Enemy_4_City.png";
            case BOSS_CITY:     return "Boss_City.png";
            case DESERT_1_PATROL: return "Enemy_1_Dessert.png";
            case DESERT_2_PATROL: return "Enemy_2_Dessert.png";
            case DESERT_3_STONE:  return "Enemy_3_Dessert.png";
            case DESERT_4_FIRE:   return "Enemy_4_Dessert.png";
            case DESERT_5_PATROL: return "Enemy_5_Dessert.png";
            case BOSS_DESERT:     return "Boss_Dessert.png";
            case FOREST_1_PATROL: return "Enemy_1_Forest.png";
            case FOREST_2_WATER:  return "Enemy_2_Forest.png";
            case FOREST_3_PATROL: return "Enemy_3_Forest.png";
            case FOREST_4_PATROL: return "Enemy_4_Forest.png";
            case FOREST_5_PLANT:  return "Enemy_5_Forest.png";
            case BOSS_FOREST:     return "Boss_Forest.png";
            case VOLCANO_1_PATROL: return "Enemy_1_Volcano.png";
            case VOLCANO_2_STONE:  return "Enemy_2_Volcano.png";
            case VOLCANO_3_PATROL: return "Enemy_3_Volcano.png";
            case VOLCANO_4_FIRE:   return "Enemy_4_Volcano.png";
            case BOSS_VOLCANO:     return "Boss_Volcano.png";
            default: return null;
        }
    }

    // ── Update ───────────────────────────────────────────────────────────────
    public void update() {
        if (!alive) return;
        if (hitFlashTimer > 0) hitFlashTimer--;

        // ── Smooth patrol movement ────────────────────────────────────────
        x += movingRight ? speed : -speed;
        if (x + width >= rightBound) { x = rightBound - width; movingRight = false; }
        if (x <= leftBound)          { x = leftBound;          movingRight = true;  }

        // ── Bob / hover animation ──────────────────────────────────────────
        // Gentle sine-wave vertical bob — gives life to the sprite
        animTick++;
        bobOffset = (float)(Math.sin(animTick * 0.08) * 2.5);

        // ── Shooting ──────────────────────────────────────────────────────
        if (isThrower()) {
            int delay = isBossType(type) ? SHOOT_DELAY_BOSS : SHOOT_DELAY_SMALL;
            shootTimer++;
            if (shootTimer >= delay) {
                shootTimer = 0;
                if (isBossType(type)) {
                    fireBossProjectiles(); // boss fires in all 4 directions
                } else {
                    fireProjectile();
                }
            }
        }

        projectiles.removeIf(pr -> !pr.active);
        for (EnemyProjectile pr : projectiles) pr.update();
    }

    private boolean isThrower() {
        switch (type) {
            case CITY_2_BRICK:   case CITY_3_BRICK:
            case DESERT_3_STONE: case DESERT_4_FIRE:
            case FOREST_2_WATER: case FOREST_5_PLANT:
            case VOLCANO_2_STONE:case VOLCANO_4_FIRE:
            case BOSS_CITY: case BOSS_DESERT:
            case BOSS_FOREST: case BOSS_VOLCANO:
                return true;
            default: return false;
        }
    }

    // Normal enemy: fire one projectile horizontally in facing direction
    private void fireProjectile() {
        int projX = movingRight ? (int)(x + width) : (int)x;
        int projY = (int)(y + height / 2);
        double vx = movingRight ? 6 : -6;
        projectiles.add(new EnemyProjectile(projX, projY, vx, 0,
                                            screenHeight, projectileColor()));
    }

    // Boss: fire in 4 directions (left, right, up-left, up-right)
    private void fireBossProjectiles() {
        int cx = (int)(x + width / 2);
        int cy = (int)(y + height / 2);
        Color c = projectileColor();
        double spd = 5;
        // left, right, diagonal up-left, diagonal up-right
        double[][] dirs = { {-spd, 0}, {spd, 0}, {-spd*0.7, spd*0.7}, {spd*0.7, spd*0.7} };
        for (double[] d : dirs) {
            projectiles.add(new EnemyProjectile(cx, cy, d[0], d[1], screenHeight, c));
        }
    }

    private Color projectileColor() {
        switch (type) {
            case CITY_2_BRICK:  case CITY_3_BRICK:   return new Color(140, 80,  30);
            case DESERT_3_STONE:                      return new Color(200, 200, 160);
            case DESERT_4_FIRE: case VOLCANO_4_FIRE:  return new Color(255, 120, 20);
            case FOREST_2_WATER:                      return new Color( 60, 160, 255);
            case FOREST_5_PLANT:                      return new Color( 40, 200,  60);
            case VOLCANO_2_STONE:                     return new Color(180, 180, 180);
            case BOSS_CITY:   return new Color(180, 180, 255);
            case BOSS_DESERT: return new Color(255, 200,  50);
            case BOSS_FOREST: return new Color( 80, 220,  80);
            case BOSS_VOLCANO:return new Color(255,  80,  20);
            default:          return Color.WHITE;
        }
    }

    public void takeDamage(int amount) {
        hp -= amount;
        hitFlashTimer = 20;
        if (hp <= 0) alive = false;
    }

    // ── Draw ─────────────────────────────────────────────────────────────────
    public void draw(Graphics g) {
        if (!alive) return;

        for (EnemyProjectile pr : projectiles) pr.draw(g);

        // bobOffset makes the enemy float up/down slightly each frame
        int drawY = screenHeight - (int)y - height + (int)bobOffset;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);

        if (hitFlashTimer > 0 && (hitFlashTimer / 3) % 2 == 0)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

        if (sprite != null) {
            // Slight scale-breathe: enemy pulses very slightly in size
            double scale = 1.0 + Math.sin(animTick * 0.05) * 0.015;
            int dw = (int)(width  * scale);
            int dh = (int)(height * scale);
            int dx = (int)(x + (width  - dw) / 2.0);
            int dy = drawY - (dh - height) / 2;

            if (movingRight) {
                g2.drawImage(sprite, dx, dy, dw, dh, null);
            } else {
                g2.drawImage(sprite, dx + dw, dy, -dw, dh, null);
            }
        } else {
            drawPlaceholder(g2, drawY);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        drawHpBar(g2, drawY);
    }

    private void drawPlaceholder(Graphics2D g2, int drawY) {
        Color c = placeholderColor();
        g2.setColor(c);
        g2.fillRoundRect((int)x, drawY, width, height, 10, 10);
        g2.setColor(c.darker());
        g2.drawRoundRect((int)x, drawY, width, height, 10, 10);
        g2.setColor(Color.YELLOW);
        int ex = movingRight ? (int)x + width - 14 : (int)x + 6;
        g2.fillOval(ex, drawY + 8, 8, 8);
    }

    private Color placeholderColor() {
        switch (type) {
            case BOSS_CITY:
            case CITY_1_PATROL: case CITY_2_BRICK: case CITY_3_BRICK: case CITY_4_PATROL:
                return new Color(80, 80, 120);
            case BOSS_DESERT:
            case DESERT_1_PATROL: case DESERT_2_PATROL: case DESERT_3_STONE:
            case DESERT_4_FIRE:   case DESERT_5_PATROL:
                return new Color(180, 120, 40);
            case BOSS_FOREST:
            case FOREST_1_PATROL: case FOREST_2_WATER: case FOREST_3_PATROL:
            case FOREST_4_PATROL: case FOREST_5_PLANT:
                return new Color(40, 120, 40);
            default: return new Color(180, 60, 20);
        }
    }

    private void drawHpBar(Graphics2D g2, int drawY) {
        int maxHp = isBossType(type) ? BOSS_HP : BASE_HP;
        if (hp >= maxHp) return;
        int barW = width, barH = 5, barY = drawY - barH - 2;
        g2.setColor(new Color(60, 0, 0));
        g2.fillRect((int)x, barY, barW, barH);
        g2.setColor(new Color(220, 40, 40));
        g2.fillRect((int)x, barY, (int)(barW * (double)hp / maxHp), barH);
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, screenHeight - (int)y - height, width, height);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  EnemyProjectile inner class
    // ─────────────────────────────────────────────────────────────────────────
    public static class EnemyProjectile {
        double x, y;
        double vx, vy;
        int    screenHeight;
        boolean active = true;
        Color   color;
        static final int R = 7;

        public EnemyProjectile(int x, int y, double vx, double vy,
                               int screenHeight, Color color) {
            this.x = x;
            this.y = screenHeight - y;  // world → screen Y
            this.vx = vx;
            this.vy = -vy;              // flip: positive vy = upward on screen = negative screen-Y
            this.screenHeight = screenHeight;
            this.color = color;
        }

        public void update() {
            x += vx;
            y += vy;
            if (x < -R || x > 2000 + R || y < -R || y > screenHeight + R)
                active = false;
        }

        public void draw(Graphics g) {
            if (!active) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(color.getRed(), color.getGreen(),
                                  color.getBlue(), 80));
            g2.fillOval((int)x - R - 3, (int)y - R - 3,
                        (R + 3) * 2, (R + 3) * 2);
            g2.setColor(color);
            g2.fillOval((int)x - R, (int)y - R, R * 2, R * 2);
            g2.setColor(Color.WHITE);
            g2.fillOval((int)x - R + 2, (int)y - R + 2, 4, 4);
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x - R, (int)y - R, R * 2, R * 2);
        }
    }
}
