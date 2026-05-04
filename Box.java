package Assigment_3_OOP;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

// ─────────────────────────────────────────────────────────────────────────────
//  Box.java  — Player character + ice projectile
//
//  Sprite rules (req #12):
//    • Two player PNGs: Player_1.png, Player_2.png (swapped with TAB)
//    • Walk animation: alternate between a "step" frame by flipping the sprite
//      vertically slightly — no extra PNGs needed, just a simple bob offset.
//    • Jump: sprite drawn 2px higher with a slight vertical squish (scale only,
//      no horizontal stretch — width stays fixed).
//    • Facing left: sprite mirrored horizontally (negative-width drawImage).
//    • Placeholder cartoon character drawn if PNGs missing.
//
//  Ice projectile (req #13):
//    • Fired horizontally in facing direction.
//    • Hits enemies → reduces their HP by 1 (handled in Main).
//    • Visual: glowing cyan orb.
// ─────────────────────────────────────────────────────────────────────────────
public class Box {

    // ── Dimensions ───────────────────────────────────────────────────────────
    int x = 100, y = 0;
    int width = 48, height = 64;

    // ── Physics ───────────────────────────────────────────────────────────────
    double vx = 0, vy = 0;
    double gravity   = -0.7;   // applied every tick (negative = downward in world-space)
    double jumpForce = 17;
    double friction  = 0.85;   // horizontal deceleration when no key held
    boolean canJump  = true;

    // ── Screen refs ───────────────────────────────────────────────────────────
    int screenHeight, screenWidth;
    List<Platform> platforms;

    // ── Spawn point (set once per level) ─────────────────────────────────────
    int spawnX, spawnY;

    // ── Visual state ──────────────────────────────────────────────────────────
    int     hitFlashTimer = 0;  // counts down; player flickers when > 0
    boolean facingRight   = true;

    // Walk bob: alternates every N ticks when the player is moving on the ground
    private int  walkTick    = 0;   // increments while moving
    private boolean stepUp   = false; // which "foot" is forward

    // Jump squish: slight vertical scale when airborne (looks more dynamic)
    private boolean isAirborne = true;

    // ── Sprites ───────────────────────────────────────────────────────────────
    // Player_1.png and Player_2.png — swapped with TAB key
    private BufferedImage[] sprites = new BufferedImage[2];
    int activeSprite = 0;

    // ── Projectiles ───────────────────────────────────────────────────────────
    List<Projectile> projectiles = new ArrayList<>();
    private int shootCooldown   = 0;
    private static final int SHOOT_DELAY = 15; // ticks between shots (~4/sec)

    // ─────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────
    public Box() { loadSprites(); }

    // ─────────────────────────────────────────────────────────────────────────
    //  SPRITE LOADING
    // ─────────────────────────────────────────────────────────────────────────
    private void loadSprites() {
        // Player_1.png and Player_2.png must be in the same package resource folder
        String[] names = { "Player_1.png", "Player_2.png" };
        for (int i = 0; i < names.length; i++) {
            try {
                sprites[i] = ImageIO.read(getClass().getResourceAsStream(names[i]));
            } catch (Exception e) {
                sprites[i] = null; // placeholder will be drawn instead
            }
        }
    }

    public void setActiveSprite(int idx) {
        if (idx >= 0 && idx < sprites.length) activeSprite = idx;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SETUP
    // ─────────────────────────────────────────────────────────────────────────
    public void setScreenDimensions(int w, int h) {
        screenWidth = w; screenHeight = h;
        if (platforms != null)
            for (Platform p : platforms) p.setScreenHeight(h);
    }

    public void setPlatforms(List<Platform> pl) {
        this.platforms = pl;
        if (pl != null && screenHeight > 0)
            for (Platform p : pl) p.setScreenHeight(screenHeight);
    }

    public void setSpawn(int x, int y) { spawnX = x; spawnY = y; }

    // ─────────────────────────────────────────────────────────────────────────
    //  RESPAWN  — called when the player falls off screen
    // ─────────────────────────────────────────────────────────────────────────
    public void respawn() {
        x = spawnX; y = spawnY;
        vx = 0; vy = 0;
        canJump = true;
        hitFlashTimer = 30;   // brief invincibility flash on respawn
        projectiles.clear();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INPUT METHODS  (called from Main game loop)
    // ─────────────────────────────────────────────────────────────────────────
    public void moveLeft()        { vx = -9; facingRight = false; }
    public void moveRight()       { vx =  9; facingRight = true;  }
    public void stopHorizontal()  { vx = 0; }

    public void jump() {
        if (canJump) { vy = jumpForce; canJump = false; }
    }

    // Fire one ice projectile in facing direction (respects cooldown)
    public void shoot() {
        if (shootCooldown > 0) return;
        // Projectile spawns at the leading edge of the player sprite
        int projX = facingRight ? x + width : x;
        int projY = y + height / 2;
        projectiles.add(new Projectile(projX, projY,
                         facingRight ? 14 : -14, screenHeight, screenWidth));
        shootCooldown = SHOOT_DELAY;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  UPDATE  — called every game tick
    // ─────────────────────────────────────────────────────────────────────────
    public void update() {
        // ── Timers ────────────────────────────────────────────────────────
        if (hitFlashTimer  > 0) hitFlashTimer--;
        if (shootCooldown  > 0) shootCooldown--;

        // ── Gravity ───────────────────────────────────────────────────────
        vy += gravity;

        // ── Horizontal friction (when no key pressed, vx decays to ~0) ───
        vx *= friction;

        // ── Walk animation tick ───────────────────────────────────────────
        boolean moving = Math.abs(vx) > 0.5;
        if (moving && canJump) {        // only bob when on the ground
            walkTick++;
            if (walkTick >= 10) {       // swap "foot" every 10 ticks
                walkTick = 0;
                stepUp = !stepUp;
            }
        } else {
            walkTick = 0;
            stepUp   = false;           // reset bob when standing still or airborne
        }

        // ── Airborne state ────────────────────────────────────────────────
        isAirborne = !canJump;

        // ═══════════════════════════════════════════════════════════════════
        //  HORIZONTAL MOVEMENT + WALL COLLISION
        // ═══════════════════════════════════════════════════════════════════
        int nextX = x + (int) vx;

        // Screen edges
        if (nextX < 0)                       { nextX = 0;                   vx = 0; }
        else if (nextX + width > screenWidth) { nextX = screenWidth - width; vx = 0; }

        // Platform side walls
        if (platforms != null) {
            for (Platform p : platforms) {
                // Only collide horizontally if player vertically overlaps the platform
                if (y + height > p.y && y < p.y + p.height) {
                    // Moving right → hitting platform's left face
                    if (vx > 0 && x + width <= p.x && nextX + width > p.x) {
                        nextX = p.x - width; vx = 0;
                    }
                    // Moving left → hitting platform's right face
                    else if (vx < 0 && x >= p.x + p.width && nextX < p.x + p.width) {
                        nextX = p.x + p.width; vx = 0;
                    }
                }
            }
        }
        x = nextX;

        // ═══════════════════════════════════════════════════════════════════
        //  VERTICAL MOVEMENT + FLOOR / CEILING COLLISION
        // ═══════════════════════════════════════════════════════════════════
        int nextY = y + (int) vy;
        boolean onFloor = false;

        if (platforms != null) {
            for (Platform p : platforms) {
                // Check horizontal overlap first
                if (x + width > p.x && x < p.x + p.width) {

                    // LAND ON TOP of platform (falling, vy <= 0)
                    if (vy <= 0 && y >= p.y + p.height && nextY < p.y + p.height) {
                        nextY   = p.y + p.height;
                        vy      = 0;
                        onFloor = true;
                    }
                    // HIT CEILING from below (only for tall platforms ≥ 60px)
                    else if (vy > 0 && y + height <= p.y && nextY + height > p.y) {
                        if (p.height >= 60) {
                            nextY = p.y - height;
                            vy    = 0;
                        }
                    }
                }
            }
        }

        // Ground floor (y = 0 in world-space = bottom of screen)
        if (nextY < 0) { nextY = 0; vy = 0; onFloor = true; }

        // Fell off bottom → respawn
        else if (nextY > screenHeight + 300) respawn();

        y       = nextY;
        canJump = onFloor;

        // ── Update projectiles ────────────────────────────────────────────
        projectiles.removeIf(pr -> !pr.active);
        for (Projectile pr : projectiles) pr.update(platforms);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  DRAW
    // ─────────────────────────────────────────────────────────────────────────
    public void draw(Graphics g) {
        // Draw player's ice projectiles
        for (Projectile pr : projectiles) pr.draw(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // ── Convert world-Y to screen-Y ───────────────────────────────────
        // Walk bob: shift the drawn position up by 3px on alternate steps
        int bobOffset = (stepUp && !isAirborne) ? -3 : 0;
        int drawY     = screenHeight - y - height + bobOffset;

        // ── Hit flash: make player semi-transparent ───────────────────────
        if (hitFlashTimer > 0 && (hitFlashTimer / 4) % 2 == 0)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));

        BufferedImage img = sprites[activeSprite];
        if (img != null) {
            // Jump squish: when airborne, draw sprite 4px taller and 4px narrower
            // to give a "stretched upward" feel — width stays within collision box.
            int drawW = isAirborne ? width  - 4 : width;
            int drawH = isAirborne ? height + 4 : height;
            int drawX = x + (width - drawW) / 2; // keep centred horizontally

            if (facingRight) {
                // Normal orientation
                g2.drawImage(img, drawX, drawY, drawW, drawH, null);
            } else {
                // Mirror horizontally: start at right edge, draw with negative width
                g2.drawImage(img, drawX + drawW, drawY, -drawW, drawH, null);
            }
        } else {
            // Fallback placeholder (cartoon character drawn with basic shapes)
            drawPlaceholder(g2, drawY);
        }

        // Restore full opacity
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // Simple cartoon placeholder used when Player PNG is missing
    private void drawPlaceholder(Graphics2D g, int drawY) {
        // Hat / hair
        g.setColor(activeSprite == 0 ? new Color(180, 30, 30) : new Color(30, 130, 30));
        g.fillOval(x + 8, drawY, 32, 18);
        // Face
        g.setColor(new Color(255, 220, 170));
        g.fillOval(x + 10, drawY + 10, 28, 26);
        // Eyes
        g.setColor(Color.BLACK);
        g.fillOval(x + 15, drawY + 17, 5, 5);
        g.fillOval(x + 28, drawY + 17, 5, 5);
        // Smile
        g.drawArc(x + 16, drawY + 25, 16, 8, 180, 180);
        // Body
        g.setColor(new Color(30, 100, 200));
        g.fillRect(x + 8, drawY + 34, 32, 22);
        // Arms
        g.setColor(new Color(255, 220, 170));
        g.fillOval(x,      drawY + 34, 12, 20);
        g.fillOval(x + 36, drawY + 34, 12, 20);
        // Legs
        g.setColor(new Color(100, 60, 20));
        g.fillRoundRect(x + 10, drawY + 54, 12, 10, 5, 5);
        g.fillRoundRect(x + 26, drawY + 54, 12, 10, 5, 5);
    }

    // ── Collision bounds (screen-space) ───────────────────────────────────────
    public Rectangle getBounds() {
        return new Rectangle(x, screenHeight - y - height, width, height);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  INNER CLASS: Projectile  (player's ice ball)
    //
    //  Travels horizontally.  Deactivates on platform collision or screen edge.
    //  Main.java checks this against enemies and calls enemy.takeDamage(1).
    // ═════════════════════════════════════════════════════════════════════════
    public static class Projectile {
        double  x, y, vx;
        double  startX;          // spawn X for range calculation
        int     screenHeight, screenWidth;
        boolean active = true;
        static final int R = 7; // radius

        public Projectile(int x, int y, double vx, int sh, int sw) {
            this.startX = x;
            this.x = x; this.y = y; this.vx = vx;
            this.screenHeight = sh; this.screenWidth = sw;
        }

        public void update(List<Platform> platforms) {
            x += vx;

            // Deactivate at screen edges
            if (x < -R || x > screenWidth + R) { active = false; return; }

            // Max range = sw / 3  — ice ball fades out after that distance
            if (Math.abs(x - startX) > screenWidth / 3.0) { active = false; return; }

            // Deactivate on platform collision (ice melts on solid surfaces)
            if (platforms != null) {
                int drawY = screenHeight - (int) y; // screen-space Y of projectile centre
                for (Platform p : platforms) {
                    int pTop = screenHeight - p.y - p.height;
                    int pBot = screenHeight - p.y;
                    if (x + R > p.x && x - R < p.x + p.width
                            && drawY > pTop && drawY < pBot) {
                        active = false; return;
                    }
                }
            }
        }

        public void draw(Graphics g) {
            if (!active) return;
            int dx = (int) x, dy = screenHeight - (int) y;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            // Outer glow ring (light blue, semi-transparent)
            g2.setColor(new Color(180, 230, 255, 100));
            g2.fillOval(dx - R - 3, dy - R - 3, (R+3)*2, (R+3)*2);
            // Core ice orb
            g2.setColor(new Color(200, 240, 255));
            g2.fillOval(dx - R, dy - R, R * 2, R * 2);
            // Rim
            g2.setColor(new Color(90, 170, 230));
            g2.drawOval(dx - R, dy - R, R * 2, R * 2);
            // Specular highlight
            g2.setColor(Color.WHITE);
            g2.fillOval(dx - R + 2, dy - R + 2, 4, 4);
        }

        public Rectangle getBounds() {
            return new Rectangle((int)x - R, screenHeight - (int)y - R, R*2, R*2);
        }
    }
}
