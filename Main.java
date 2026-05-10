package Assigment_3_OOP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

// ─────────────────────────────────────────────────────────────────────────────
//  Main.java  — Game entry point, loop, HUD, menus, animations
//
//  Key improvements over Main(6):
//    1. Enemy spawning uses Enemy.Type per level (correct sprites + behaviour)
//    2. Two-enemy platforms: patrol bounds split at platform midpoint
//    3. Background particle animations (snow/rain/sand/volcano)
//    4. Player HP bar + enemy hit by ice reduces HP (not instant kill)
//    5. Death dialog: "Try Again" or "Main Menu"
//    6. Boss HP bar shown at top when in a boss level
//    7. Enemy projectiles drawn and checked vs player
//    8. Key input unchanged from Main(6) — already fixed with HashSet
// ─────────────────────────────────────────────────────────────────────────────
public class Main extends JPanel implements ActionListener {

    // ── Screen states ─────────────────────────────────────────────────────────
    private enum Screen { SPLASH, WORLD_SELECT, LEVEL_SELECT, PLAYING, DEAD }
    private Screen screen = Screen.SPLASH;

    // ── World metadata ────────────────────────────────────────────────────────
    private static final String[] WORLD_NAMES    = { "CITY", "DESERT", "FOREST", "VOLCANO" };
    private static final String[] WORLD_BG_FILES = {
        "Background_1.jpg",   // world 0 – city   (snow)
        "Background_2.png",   // world 1 – desert (sand)
        "Background_3.png",   // world 2 – forest (rain)
        "Background_4.png"    // world 3 – volcano (embers)
    };
    private static final Color[] WORLD_TINT = {
        new Color(10,  20,  60,  160),
        new Color(80,  40,   0,  160),
        new Color(0,   50,  10,  160),
        new Color(80,  10,   0,  160)
    };
    private static final Color[] WORLD_ACCENT = {
        new Color(80,  130, 255),
        new Color(255, 180,  50),
        new Color(60,  200,  80),
        new Color(255,  80,  20)
    };

    // level numbers per world slot [world][slot], slot 4 = boss
    private static final int[][] WORLD_LEVELS = {
        { 1,  2,  3,  4,  17 },   // City
        { 5,  6,  7,  8,  18 },   // Desert
        { 9, 10, 11, 12,  19 },   // Forest
        {13, 14, 15, 16,  20 }    // Volcano
    };
    private static final int LEVELS_PER_WORLD = 5;

    // Boss level numbers (levels where a boss enemy appears)
    private static final int[] BOSS_LEVELS = { 17, 18, 19, 20 };

    // ── Resources ─────────────────────────────────────────────────────────────
    private Image[] bgImages = new Image[4];
    private Image   activeBg;

    // ── Game objects ──────────────────────────────────────────────────────────
    Box            box       = new Box();
    Box            box2      = new Box();
    List<Platform> platforms = new ArrayList<>();
    List<Enemy>    enemies   = new ArrayList<>();

    // ── Player HP ─────────────────────────────────────────────────────────────
    // Player starts with 5 HP; each enemy touch costs 1 HP; ice heals nothing.
    private static final int PLAYER_MAX_HP = 5;
    private double playerHp  = PLAYER_MAX_HP;
    private double player2Hp = PLAYER_MAX_HP;

    // ── Boss state ────────────────────────────────────────────────────────────
    // Boss jumps between platforms after BOSS_JUMP_INTERVAL ticks.
    // Desert/Forest boss alternates between lowest-middle and top platform.
    // City/Volcano boss jumps to an isolated corner platform.
    private int  bossJumpTimer     = 0;
    private static final int BOSS_JUMP_INTERVAL = 300; // ~5 sec at 60fps

    // Boss mini-enemy spawning: one mini every 10 sec during 20-sec delay phase
    private int  bossMiniSpawnTimer = 0;
    private static final int BOSS_MINI_INTERVAL = 600; // 10 sec
    private int  bossShootTimer = 0;
    private static final int CITY_BOSS_SHOOT_INTERVAL = 240; // 4 sec
    private static final int VOLCANO_BOSS_SHOOT_INTERVAL = 208; // ~2.5 sec at 12ms tick
    private static final int CITY_BOSS_MINI_INTERVAL  = 300; // 5 sec
    private static final int CITY_BOSS_JUMP_INTERVAL  = 600; // 10 sec
    private static final int VOLCANO_BOSS_JUMP_INTERVAL = 833; // 10 sec at ~12ms tick
    private boolean cityBossAtUpper = false;
    private boolean volcanoBossAtUpper = false;

    // ── Level complete ────────────────────────────────────────────────────────
    private int levelCompleteTimer = 0; // counts down after all enemies dead

    // ── Key input (HashSet = zero lag, no OS repeat delay) ────────────────────
    private final Set<Integer> pressedKeys = new HashSet<>();

    // ── Game state ────────────────────────────────────────────────────────────
    private int  currentLevel  = 1;
    private int  selectedWorld = 0;
    private int  score         = 0;
    private String typedLevel  = "";
    private long   lastTypeTime = 0;

    // ── Mouse ─────────────────────────────────────────────────────────────────
    private Point mouse = new Point();

    // ── Background particle animation ─────────────────────────────────────────
    // Each particle is [x, y, speed, size] – world-independent floats
    private static final int PARTICLE_COUNT = 80;
    private float[][] particles = new float[PARTICLE_COUNT][4];
    private final Random rng = new Random();

    // ─────────────────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────
    public Main() {
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Global key dispatcher: catches keys even if the panel loses focus
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> { handleGlobalKey(e); return false; });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { mouse = e.getPoint(); repaint(); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                handleClick(e.getX(), e.getY());
            }
        });

        loadResources();
        initParticles(800, 600); // initial placeholder size; reset on first play
        Timer gameTimer = new Timer(12, this); // higher update rate for snappier input
        gameTimer.setCoalesce(false);          // avoid collapsing ticks under load
        gameTimer.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GLOBAL KEY HANDLER  (same as Main(6), unchanged)
    // ─────────────────────────────────────────────────────────────────────────
    private void handleGlobalKey(KeyEvent e) {
        int key  = e.getKeyCode();
        int type = e.getID();

        if (type == KeyEvent.KEY_PRESSED)  pressedKeys.add(key);
        if (type == KeyEvent.KEY_RELEASED) pressedKeys.remove(key);

        // Immediate movement response on press/release to reduce perceived input lag.
        if (screen == Screen.PLAYING) {
            if (type == KeyEvent.KEY_PRESSED) {
                if (playerHp > 0) {
                    if (key == KeyEvent.VK_LEFT)  box.moveLeft();
                    if (key == KeyEvent.VK_RIGHT) box.moveRight();
                }
                if (player2Hp > 0) {
                    if (key == KeyEvent.VK_A) box2.moveLeft();
                    if (key == KeyEvent.VK_D) box2.moveRight();
                }
            } else if (type == KeyEvent.KEY_RELEASED) {
                if (playerHp > 0 && (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT)) {
                    if      (isP1Left()  && !isP1Right()) box.moveLeft();
                    else if (isP1Right() && !isP1Left())  box.moveRight();
                    else                                   box.stopHorizontal();
                }
                if (player2Hp > 0 && (key == KeyEvent.VK_A || key == KeyEvent.VK_D)) {
                    if      (isP2Left()  && !isP2Right()) box2.moveLeft();
                    else if (isP2Right() && !isP2Left())  box2.moveRight();
                    else                                   box2.stopHorizontal();
                }
            }
        }

        if (type != KeyEvent.KEY_PRESSED)  return;

        // ESC: back to menu
        if (key == KeyEvent.VK_ESCAPE) {
            if (screen == Screen.PLAYING || screen == Screen.DEAD) {
                screen = Screen.WORLD_SELECT; repaint(); return;
            }
            if (screen == Screen.LEVEL_SELECT) {
                screen = Screen.WORLD_SELECT; repaint(); return;
            }
            typedLevel = "";
            return;
        }

        if (screen == Screen.DEAD) {
            // R = try again,  M = main menu
            if (key == KeyEvent.VK_R) { launchLevel(currentLevel); return; }
            if (key == KeyEvent.VK_M) { screen = Screen.WORLD_SELECT; repaint(); return; }
            return;
        }

        if (screen != Screen.PLAYING) return;

        // Jump fires immediately on keypress (no OS repeat lag)
        if ((key == KeyEvent.VK_SPACE || key == KeyEvent.VK_UP) && playerHp > 0) box.jump(); // P1
        if (key == KeyEvent.VK_W && player2Hp > 0) box2.jump();                                // P2

        // Shoot ice
        if (key == KeyEvent.VK_Z && playerHp > 0) box.shoot();   // P1
        if (key == KeyEvent.VK_F && player2Hp > 0) box2.shoot(); // P2

        // Character swap
        if (key == KeyEvent.VK_TAB)
            box.setActiveSprite((box.activeSprite + 1) % 2);

        // Numeric level jump: type digits then ENTER
        if (key >= KeyEvent.VK_0 && key <= KeyEvent.VK_9) {
            typedLevel  += (char)('0' + (key - KeyEvent.VK_0));
            lastTypeTime = System.currentTimeMillis();
            try {
                int lvl = Integer.parseInt(typedLevel);
                if (lvl >= 1 && lvl <= 99) { launchLevel(lvl); typedLevel = ""; }
            } catch (NumberFormatException ignored) {}
        }
        if (key == KeyEvent.VK_ENTER && typedLevel.length() > 0) {
            try { int lvl = Integer.parseInt(typedLevel); if (lvl >= 1) launchLevel(lvl); }
            catch (NumberFormatException ignored) {}
            typedLevel = "";
        }
    }

    private boolean isP1Left()  { return pressedKeys.contains(KeyEvent.VK_LEFT); }
    private boolean isP1Right() { return pressedKeys.contains(KeyEvent.VK_RIGHT); }
    private boolean isP2Left()  { return pressedKeys.contains(KeyEvent.VK_A); }
    private boolean isP2Right() { return pressedKeys.contains(KeyEvent.VK_D); }

    // ─────────────────────────────────────────────────────────────────────────
    //  RESOURCES
    // ─────────────────────────────────────────────────────────────────────────
    private void loadResources() {
        for (int i = 0; i < WORLD_BG_FILES.length; i++) {
            try { bgImages[i] = new ImageIcon(getClass().getResource(WORLD_BG_FILES[i])).getImage(); }
            catch (Exception ex) { bgImages[i] = null; }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BACKGROUND PARTICLE SYSTEM
    //  Particles are re-used and wrap around the screen edges.
    //  World 0 (City)   = snowflakes falling downward
    //  World 1 (Desert) = sand grains drifting right
    //  World 2 (Forest) = raindrops falling fast at an angle
    //  World 3 (Volcano)= ember sparks rising upward
    // ─────────────────────────────────────────────────────────────────────────
    private void initParticles(int W, int H) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i][0] = rng.nextFloat() * W;           // x
            particles[i][1] = rng.nextFloat() * H;           // y
            particles[i][2] = 1 + rng.nextFloat() * 3;       // speed
            particles[i][3] = 2 + rng.nextFloat() * 4;       // size
        }
    }

    private void updateParticles(int W, int H, int world) {
        for (float[] p : particles) {
            switch (world) {
                case 0: // City – snow falls downward, drifts slightly sideways
                    p[1] += p[2];
                    p[0] += (float)Math.sin(p[1] * 0.05) * 0.5f;
                    if (p[1] > H) { p[1] = -p[3]; p[0] = rng.nextFloat() * W; }
                    break;
                case 1: // Desert – sand blows from left to right
                    p[0] += p[2] * 2;
                    p[1] += (rng.nextFloat() - 0.5f) * 0.5f;
                    if (p[0] > W) { p[0] = -p[3]; p[1] = rng.nextFloat() * H; }
                    break;
                case 2: // Forest – rain falls fast at an angle
                    p[0] += p[2] * 0.4f;
                    p[1] += p[2] * 4;
                    if (p[1] > H) { p[1] = -10; p[0] = rng.nextFloat() * W; }
                    break;
                case 3: // Volcano – embers rise and fade out at top
                    p[1] -= p[2] * 1.5f;
                    p[0] += (rng.nextFloat() - 0.5f) * 1.5f;
                    if (p[1] < 0) { p[1] = H + p[3]; p[0] = rng.nextFloat() * W; }
                    break;
            }
        }
    }

    private void drawParticles(Graphics2D g2, int W, int H, int world) {
        for (float[] p : particles) {
            int sz = (int) p[3];
            switch (world) {
                case 0: // snowflake – white circle
                    g2.setColor(new Color(220, 235, 255, 180));
                    g2.fillOval((int)p[0], (int)p[1], sz, sz);
                    break;
                case 1: // sand grain – sandy yellow dash
                    g2.setColor(new Color(210, 175, 90, 140));
                    g2.fillRect((int)p[0], (int)p[1], sz * 2, 2);
                    break;
                case 2: // raindrop – thin blue line
                    g2.setColor(new Color(100, 160, 255, 130));
                    g2.drawLine((int)p[0], (int)p[1], (int)p[0] + 2, (int)p[1] + 10);
                    break;
                case 3: // ember – small orange dot, semi-transparent
                    int alpha = 80 + (int)(p[1] / H * 120);
                    g2.setColor(new Color(255, 100 + rng.nextInt(80), 20, Math.min(alpha, 220)));
                    g2.fillOval((int)p[0], (int)p[1], sz, sz);
                    break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  WORLD / LEVEL HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private int worldOfLevel(int lvl) {
        for (int w = 0; w < WORLD_LEVELS.length; w++)
            for (int s = 0; s < WORLD_LEVELS[w].length; s++)
                if (WORLD_LEVELS[w][s] == lvl) return w;
        return 0;
    }

    private boolean isBossLevel(int lvl) {
        for (int b : BOSS_LEVELS) if (b == lvl) return true;
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ENEMY SPAWN DATA
    //  Each level has a fixed list of [spawnX, spawnY, typeOrdinal] triples.
    //  typeOrdinal maps to Enemy.Type.values()[ordinal].
    //  When two enemies share a platform, their patrol bounds are split at the
    //  platform midpoint so they each own one half.
    // ─────────────────────────────────────────────────────────────────────────

    // Returns the Enemy.Type for each spawn slot in a given level.
    // Array length matches LevelLayouts.getEnemySpawns() count for that level.
    private Enemy.Type[] typesForLevel(int lvl) {
        switch (lvl) {
            // ── City normal levels ───────────────────────────────────────────
            case 1:  return new Enemy.Type[]{
                Enemy.Type.CITY_1_PATROL,  // Ground Left (E1)
                Enemy.Type.CITY_4_PATROL,  // Ground Right (E4)
                Enemy.Type.CITY_1_PATROL,  // Floor 1 (E1)
                Enemy.Type.CITY_4_PATROL,  // Floor 3 Left (E4)
                Enemy.Type.CITY_4_PATROL   // Floor 3 Right (E4)
            };
            // Level 2: E4,E4 ground; E3,E3 mid; E1 top
            case 2:  return new Enemy.Type[]{
                Enemy.Type.CITY_1_PATROL,  // mid floor moving
                Enemy.Type.CITY_4_PATROL,  // left floor
                Enemy.Type.CITY_4_PATROL,  // right floor
                Enemy.Type.CITY_3_BRICK,   // left middle
                Enemy.Type.CITY_3_BRICK    // right middle
            };
            // Level 3: Ground (E4,E4), Floor 1 (E1,E3), Floor 2 (E2), Floor 4 (E3,E1)
            case 3:  return new Enemy.Type[]{
                Enemy.Type.CITY_4_PATROL,  // Ground Left (E4)
                Enemy.Type.CITY_4_PATROL,  // Ground Right (E4)
                Enemy.Type.CITY_1_PATROL,  // Floor 1 Left (E1)
                Enemy.Type.CITY_3_BRICK,   // Floor 1 Right (E3)
                Enemy.Type.CITY_2_BRICK,   // Floor 2 Mid (E2)
                Enemy.Type.CITY_3_BRICK,   // Floor 4 Left (E3)
                Enemy.Type.CITY_1_PATROL   // Floor 4 Right (E1)
            };
            // Level 4: Ground (E1,E4,E1), Floor 2 (E2), Floor 3 (E3,E3), Floor 4 (E2)
            case 4:  return new Enemy.Type[]{
                Enemy.Type.CITY_1_PATROL,  // Ground Left (E1)
                Enemy.Type.CITY_4_PATROL,  // Ground Mid (E4)
                Enemy.Type.CITY_1_PATROL,  // Ground Right (E1)
                Enemy.Type.CITY_2_BRICK,   // Floor 2 Mid (E2)
                Enemy.Type.CITY_3_BRICK,   // Floor 3 Left (E3)
                Enemy.Type.CITY_3_BRICK,   // Floor 3 Right (E3)
                Enemy.Type.CITY_2_BRICK    // Floor 4 Top (E2)
            };
            // ── Desert normal levels ─────────────────────────────────────────
            // Level 5: Ground (E1,E1), Floor 2 (E2), Floor 3 (E2), Floor 4 (E5,E5), Floor 5 (E4)
            case 5:  return new Enemy.Type[]{
                Enemy.Type.DESERT_1_PATROL, // Ground Left (E1)
                Enemy.Type.DESERT_1_PATROL, // Ground Right (E1)
                Enemy.Type.DESERT_2_PATROL, // Floor 2 Mid (E2)
                Enemy.Type.DESERT_2_PATROL, // Floor 3 Mid (E2)
                Enemy.Type.DESERT_5_PATROL, // Floor 4 Left (E5)
                Enemy.Type.DESERT_5_PATROL, // Floor 4 Right (E5)
                Enemy.Type.DESERT_4_FIRE    // Floor 5 Left (E4)
            };
            // Level 6: Ground (E5,E5), Floor 2 (E2,E2), Floor 4 (E5), Floor 5 (E1,E1)
            case 6:  return new Enemy.Type[]{
                Enemy.Type.DESERT_5_PATROL, // Ground Left (E5)
                Enemy.Type.DESERT_5_PATROL, // Ground Right (E5)
                Enemy.Type.DESERT_2_PATROL, // Floor 2 Left (E2)
                Enemy.Type.DESERT_2_PATROL, // Floor 2 Right (E2)
                Enemy.Type.DESERT_5_PATROL, // Floor 4 Mid (E5)
                Enemy.Type.DESERT_1_PATROL, // Floor 5 Left (E1)
                Enemy.Type.DESERT_1_PATROL  // Floor 5 Right (E1)
            };
            // Level 7: Ground (E2,E2), Floor 2 (E3,E3), Floor 3 (E5), Floor 4 (E4,E4)
            case 7:  return new Enemy.Type[]{
                Enemy.Type.DESERT_2_PATROL, // Ground Left (E2)
                Enemy.Type.DESERT_2_PATROL, // Ground Right (E2)
                Enemy.Type.DESERT_3_STONE,  // Floor 2 Left (E3)
                Enemy.Type.DESERT_3_STONE,  // Floor 2 Right (E3)
                Enemy.Type.DESERT_5_PATROL, // Floor 3 Center (E5)
                Enemy.Type.DESERT_4_FIRE,   // Floor 4 Left (E4)
                Enemy.Type.DESERT_4_FIRE    // Floor 4 Right (E4)
            };
            // Level 8: Ground (E1,E1), Floor 2 (E2,E2), Floor 3 (E3,E3), Floor 4 (E4)
            case 8:  return new Enemy.Type[]{
                Enemy.Type.DESERT_1_PATROL, // Ground Left (E1)
                Enemy.Type.DESERT_1_PATROL, // Ground Right (E1)
                Enemy.Type.DESERT_2_PATROL, // Floor 2 Left (E2)
                Enemy.Type.DESERT_2_PATROL, // Floor 2 Right (E2)
                Enemy.Type.DESERT_3_STONE,  // Floor 3 Left (E3)
                Enemy.Type.DESERT_3_STONE,  // Floor 3 Right (E3)
                Enemy.Type.DESERT_4_FIRE    // Floor 4 Mid (E4)
            };
            // ── Forest normal levels ─────────────────────────────────────────
            // Level 9: Ground (E3,E3), Floor 2 (E1,E1), Floor 4 (E4,E4)
            case 9:  return new Enemy.Type[]{
                Enemy.Type.FOREST_3_PATROL, // Ground Left (E3)
                Enemy.Type.FOREST_3_PATROL, // Ground Right (E3)
                Enemy.Type.FOREST_1_PATROL, // Floor 2 Left (E1)
                Enemy.Type.FOREST_1_PATROL, // Floor 2 Right (E1)
                Enemy.Type.FOREST_4_PATROL, // Floor 4 Left (E4)
                Enemy.Type.FOREST_4_PATROL  // Floor 4 Right (E4)
            };
            // Level 10: Ground (E1,E1,E1), Lower Wings (E4,E4), Lower Center (E1), Upper Wings (E3,E3), Top Center (E2)
            case 10: return new Enemy.Type[]{
                Enemy.Type.FOREST_1_PATROL, // Ground Left (E1)
                Enemy.Type.FOREST_1_PATROL, // Ground Mid (E1)
                Enemy.Type.FOREST_1_PATROL, // Ground Right (E1)
                Enemy.Type.FOREST_4_PATROL, // Lower Left Wing (E4)
                Enemy.Type.FOREST_4_PATROL, // Lower Right Wing (E4)
                Enemy.Type.FOREST_1_PATROL, // Lower Center (E1)
                Enemy.Type.FOREST_3_PATROL, // Upper Left Wing (E3)
                Enemy.Type.FOREST_3_PATROL, // Upper Right Wing (E3)
                Enemy.Type.FOREST_2_WATER   // Top Center (E2)
            };
            // Level 11: Ground (E4,E4), Floor 2 (E2,E2), Floor 3 (E1,E1), Floor 4 (E5)
            case 11: return new Enemy.Type[]{
                Enemy.Type.FOREST_4_PATROL, // Ground Left (E4)
                Enemy.Type.FOREST_4_PATROL, // Ground Right (E4)
                Enemy.Type.FOREST_2_WATER,  // Floor 2 Mid Left (E2)
                Enemy.Type.FOREST_2_WATER,  // Floor 2 Mid Right (E2)
                Enemy.Type.FOREST_1_PATROL, // Floor 3 Left Upper (E1)
                Enemy.Type.FOREST_1_PATROL, // Floor 3 Right Upper (E1)
                Enemy.Type.FOREST_5_PLANT   // Floor 4 High Moving (E5)
            };
            // Level 12: Ground (E2,E2), Floor 1 (E5), Floor 2 (E3), Floor 3 (E5), Floor 4 (E4)
            case 12: return new Enemy.Type[]{
                Enemy.Type.FOREST_2_WATER,  // Ground Left (E2)
                Enemy.Type.FOREST_2_WATER,  // Ground Right (E2)
                Enemy.Type.FOREST_5_PLANT,  // Floor 1 Main (E5)
                Enemy.Type.FOREST_3_PATROL, // Floor 2 Main (E3)
                Enemy.Type.FOREST_5_PLANT,  // Floor 3 Main (E5)
                Enemy.Type.FOREST_4_PATROL  // Floor 4 Main (E4)
            };
            // ── Volcano normal levels ────────────────────────────────────────
            // Level 13: Ground (E1,E1), Floor 1 (E3), Floor 4 (E1,E1), Floor 5 (E3,E3)
            case 13: return new Enemy.Type[]{
                Enemy.Type.VOLCANO_1_PATROL, // Ground Left (E1)
                Enemy.Type.VOLCANO_1_PATROL, // Ground Right (E1)
                Enemy.Type.VOLCANO_3_PATROL, // Floor 1 Center (E3)
                Enemy.Type.VOLCANO_1_PATROL, // Floor 4 Left (E1)
                Enemy.Type.VOLCANO_1_PATROL, // Floor 4 Right (E1)
                Enemy.Type.VOLCANO_3_PATROL, // Floor 5 Left (E3)
                Enemy.Type.VOLCANO_3_PATROL  // Floor 5 Right (E3)
            };
            // Level 14: Ground (E3,E3), Floor 1 (E2), Floor 2 (E1,E1), Floor 3 (E2)
            case 14: return new Enemy.Type[]{
                Enemy.Type.VOLCANO_3_PATROL, // Ground Left (E3)
                Enemy.Type.VOLCANO_3_PATROL, // Ground Right (E3)
                Enemy.Type.VOLCANO_2_STONE,  // Floor 1 Mid (E2)
                Enemy.Type.VOLCANO_1_PATROL, // Floor 2 Left (E1)
                Enemy.Type.VOLCANO_1_PATROL, // Floor 2 Right (E1)
                Enemy.Type.VOLCANO_2_STONE   // Floor 3 Bridge (E2)
            };
            case 15: return new Enemy.Type[]{
                Enemy.Type.VOLCANO_1_PATROL, // spawn
                Enemy.Type.VOLCANO_1_PATROL, // spawn2
                Enemy.Type.VOLCANO_3_PATROL, // mid-left main
                Enemy.Type.VOLCANO_3_PATROL, // mid-right main
                Enemy.Type.VOLCANO_4_FIRE,   // center floating
                Enemy.Type.VOLCANO_2_STONE,  // bridge left
                Enemy.Type.VOLCANO_2_STONE   // bridge right
            };
            case 16: return new Enemy.Type[]{
                Enemy.Type.VOLCANO_2_STONE,  // Ground Left (E2)
                Enemy.Type.VOLCANO_2_STONE,  // Ground Right (E2)
                Enemy.Type.VOLCANO_3_PATROL, // Floor 2 Left step (E3)
                Enemy.Type.VOLCANO_3_PATROL, // Floor 2 Right step (E3)
                Enemy.Type.VOLCANO_4_FIRE,   // Floor 3 Bridge Left (E4)
                Enemy.Type.VOLCANO_4_FIRE,   // Floor 3 Bridge Right (E4)
                Enemy.Type.VOLCANO_1_PATROL, // Floor 4 Left step (E1)
                Enemy.Type.VOLCANO_1_PATROL  // Floor 4 Right step (E1)
            };
            // ── Boss levels ──────────────────────────────────────────────────
            case 17: return new Enemy.Type[]{ Enemy.Type.BOSS_CITY };
            case 18: return new Enemy.Type[]{ Enemy.Type.BOSS_DESERT };
            case 19: return new Enemy.Type[]{ Enemy.Type.BOSS_FOREST };
            case 20: return new Enemy.Type[]{ Enemy.Type.BOSS_VOLCANO };
            default: return new Enemy.Type[]{ Enemy.Type.CITY_1_PATROL };
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  ENEMY SPAWNING
    //  Uses LevelLayouts.getEnemySpawns() for positions and typesForLevel()
    //  for the correct Enemy.Type.  Two enemies on the same platform split the
    //  width: the left one patrols the left half, the right one the right half.
    // ─────────────────────────────────────────────────────────────────────────
    private void spawnEnemies() {
        enemies.clear();
        int sh = getHeight(), sw = getWidth();
        double speed = 1.5 + (currentLevel - 1) * 0.1; // reduced speed scaling (halved)
        // Volcano enemies were feeling too fast; slightly reduce patrol speed there.
        if (worldOfLevel(currentLevel) == 3) speed *= 0.88;

        double[][] spawns = LevelLayouts.getEnemySpawns(currentLevel, sw, sh);
        Enemy.Type[] types = typesForLevel(currentLevel);

        int count = Math.min(spawns.length, types.length);
        final double enemyYOffset = 5.0;

        for (int i = 0; i < count; i++) {
            double ex = spawns[i][0] - 34; // Center enemy (width 68) on spawn X
            double ey = spawns[i][1] - enemyYOffset;
            Enemy.Type etype = types[i];

            // ── Determine Bounds ──────────────────────────────────────────
            double lb, rb;
            boolean explicitBounds = (spawns[i].length >= 4);

            if (explicitBounds) {
                lb = spawns[i][2];
                rb = spawns[i][3];
            } else {
                // Default fallback patrol bounds
                lb = ex - 60;
                rb = ex + 60;
            }

            Platform myPlatform = null;
            // Search for best matching platform
            double bestDist = Double.MAX_VALUE;
            for (Platform p : platforms) {
                if (ex >= p.x && ex <= p.x + p.width) {
                    double platformTop = p.y + p.height;
                    double dist = Math.abs(ey - platformTop);
                    if (dist < bestDist) {
                        bestDist = dist;
                        myPlatform = p;
                    }
                }
            }

            // Snap enemy Y and set auto-bounds if not explicit
            if (myPlatform != null) {
                ey = myPlatform.y + myPlatform.height - enemyYOffset;
                if (!explicitBounds) {
                    lb = myPlatform.x + 5;
                    rb = myPlatform.x + myPlatform.width - 5;
                }
            }

            // ── Two-enemy split (only if not using explicit bounds) ──────────
            if (myPlatform != null && !explicitBounds) {
                double mid = myPlatform.x + myPlatform.width / 2.0;
                int sharedCount = 0;
                for (int j = 0; j < count; j++) {
                    double ox = spawns[j][0];
                    if (j != i && ox >= myPlatform.x && ox <= myPlatform.x + myPlatform.width) {
                        sharedCount++;
                    }
                }
                if (sharedCount > 0) {
                    if (ex < mid) rb = mid - 5;
                    else          lb = mid + 5;
                }
            }

            // Boss moves very slowly (0.4 px/tick); normal enemies use level speed
            double eSpeed = (etype == Enemy.Type.BOSS_CITY || etype == Enemy.Type.BOSS_DESERT
                          || etype == Enemy.Type.BOSS_FOREST || etype == Enemy.Type.BOSS_VOLCANO)
                          ? 0.4 : speed;

            Enemy enemy = new Enemy(ex, ey, lb, rb, eSpeed, sh, etype);
            boolean isBoss = (etype == Enemy.Type.BOSS_CITY || etype == Enemy.Type.BOSS_DESERT
                           || etype == Enemy.Type.BOSS_FOREST || etype == Enemy.Type.BOSS_VOLCANO);
            if (!isBoss) {
                // Left-side platforms start moving right first; right-side platforms move left first.
                double platformCenter = (myPlatform != null) ? (myPlatform.x + myPlatform.width / 2.0)
                                                             : ((lb + rb) / 2.0);
                if (platformCenter < sw / 2.0) enemy.movingRight = true;
                else if (platformCenter > sw / 2.0) enemy.movingRight = false;
                else enemy.movingRight = (ex < sw / 2.0);
            }
            enemies.add(enemy);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  BOSS HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    // Returns the mini-enemy type the boss spawns during its delay phase.
    private Enemy.Type bossMiniType(int lvl) {
        switch (lvl) {
            case 17: return Enemy.Type.CITY_1_PATROL;
            case 18: return Enemy.Type.DESERT_1_PATROL;
            case 19: return Enemy.Type.FOREST_1_PATROL;
            case 20: return Enemy.Type.VOLCANO_1_PATROL;
            default: return Enemy.Type.CITY_1_PATROL;
        }
    }

    // Teleport the boss to a new platform.
    // City/Volcano: jump between main floor and isolated corner thick platform.
    // Desert/Forest: alternate between ground-center and top-center platform.
    private void doBossJump(Enemy boss) {
        int sw = getWidth(), sh = getHeight();
        double newX, newY;
        switch (currentLevel) {
            case 17: // City – isolated thick platform right (sx-350,325,350,70)
                if (boss.x > sw / 2.0) {
                    newX = 20;  newY = 250 + boss.height + 5;  // FLOOR 2 left
                } else {
                    newX = sw - 350 + 40;  newY = 325 + 70 + 5; // isolated right
                }
                break;
            case 20: // Volcano – isolated thick platform left (0,325,350,70)
                if (boss.x < sw / 2.0) {
                    newX = sw - 700;  newY = 420 + boss.height + 5; // FLOOR 4 right
                } else {
                    newX = 20;  newY = 325 + 70 + 5; // isolated left
                }
                break;
            case 18: // Desert – base platform ↔ top platform
                if (boss.y < sh * 0.55) {
                    // currently high → drop to wide base (y=0 platform top=40)
                    newX = sw / 2.0 - boss.width / 2.0;
                    newY = 40 + boss.height + 5;
                } else {
                    // jump to top platform (sw/4, y=550, h=30)
                    newX = sw / 2.0 - boss.width / 2.0;
                    newY = 550 + 30 + boss.height + 5;
                }
                break;
            case 19: // Forest – ground base (y=0,h=90) ↔ top center (y=600,h=30)
                if (boss.y < sh * 0.55) {
                    newX = sw / 2.0 - boss.width / 2.0;
                    newY = 90 + boss.height + 5;
                } else {
                    newX = sw / 2.0 - boss.width / 2.0;
                    newY = 600 + 30 + boss.height + 5;
                }
                break;
            default:
                newX = sw / 2.0;  newY = 200 + boss.height;
        }
        boss.x = newX;
        boss.y = newY;
    }

    private void placeCityBossBottom(Enemy boss, int sw) {
        boss.x = sw - boss.width - 20;
        boss.y = -5;
        boss.speed = 0;
        boss.leftBound = boss.x;
        boss.rightBound = boss.x + boss.width;
    }

    private void placeCityBossUpper(Enemy boss, int sw) {
        boss.x = (sw - 350) + (350 - boss.width) / 2.0;
        boss.y = 375 + 30;
        boss.speed = 0;
        boss.leftBound = boss.x;
        boss.rightBound = boss.x + boss.width;
    }

    private void placeVolcanoBossBottomLeft(Enemy boss) {
        boss.x = 0;
        boss.y = -5;
        boss.speed = 0;
        boss.leftBound = boss.x;
        boss.rightBound = boss.x + boss.width;
    }

    private void placeVolcanoBossUpper(Enemy boss) {
        // Isolated left thick platform in level 20: (0,325,350,70)
        boss.x = (350 - boss.width) / 2.0;
        boss.y = 325 + 30;
        boss.speed = 0;
        boss.leftBound = boss.x;
        boss.rightBound = boss.x + boss.width;
    }

    private void fireCityBossRadial(Enemy boss, int projectileCount) {
        double cx = boss.x + boss.width / 2.0;
        double cy = boss.y + boss.height / 2.0;
        double shotSpeed = 5.5;
        for (int i = 0; i < projectileCount; i++) {
            double angle = (Math.PI * 2 * i) / projectileCount;
            double vx = Math.cos(angle) * shotSpeed;
            double vy = Math.sin(angle) * shotSpeed;
            boss.projectiles.add(new Enemy.EnemyProjectile((int)cx, (int)cy, vx, vy, getHeight(), new Color(255, 120, 40)));
        }
    }

    private void fireVolcanoBossMouthRadial(Enemy boss, int projectileCount) {
        Point mouth = boss.getVolcanoBossMouthOrigin();
        double shotSpeed = 5.8;
        Color red = new Color(255, 35, 35);
        boss.triggerVolcanoBossFireAnim();
        for (int i = 0; i < projectileCount; i++) {
            double angle = (Math.PI * 2 * i) / projectileCount;
            double vx = Math.cos(angle) * shotSpeed;
            double vy = Math.sin(angle) * shotSpeed;
            boss.projectiles.add(new Enemy.EnemyProjectile(mouth.x, mouth.y, vx, vy, getHeight(), red));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  LAUNCH LEVEL
    // ─────────────────────────────────────────────────────────────────────────
    private void launchLevel(int lvl) {
        currentLevel          = lvl;
        int worldIdx          = worldOfLevel(lvl);
        Platform.currentWorld = worldIdx;
        activeBg              = bgImages[worldIdx];
        screen                = Screen.PLAYING;
        playerHp              = PLAYER_MAX_HP;   // reset player HP on new level
        player2Hp             = PLAYER_MAX_HP;
        bossJumpTimer         = 0;               // reset boss jump clock
        bossMiniSpawnTimer    = (lvl == 17) ? -CITY_BOSS_MINI_INTERVAL : 0; // delay first mini for city boss
        bossShootTimer        = 0;               // reset boss radial-fire clock
        cityBossAtUpper       = false;
        volcanoBossAtUpper    = false;
        levelCompleteTimer    = 0;
        pressedKeys.clear();
        requestFocusInWindow();
        initGame();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME INIT
    // ─────────────────────────────────────────────────────────────────────────
    public void initGame() {
        int sw = getWidth(), sh = getHeight();
        if (sw <= 0 || sh <= 0) return;
        platforms = LevelLayouts.getLevel(currentLevel, sw, sh);
        box.setScreenDimensions(sw, sh);
        box2.setScreenDimensions(sw, sh);
        box.setPlatforms(platforms);
        box2.setPlatforms(platforms);
        box.x = sw / 2 - 80;
        box2.x = sw / 2 + 80;
        box.y = sh / 2;
        box2.y = sh / 2;
        box.setSpawn(sw / 2 - 80, sh / 2);
        box2.setSpawn(sw / 2 + 80, sh / 2);
        box.vy = 0;
        box2.vy = 0;
        box.canJump = false;
        box2.canJump = false;
        box.setActiveSprite(0);
        box2.setActiveSprite(1);
        initParticles(sw, sh);   // reset particle positions for new screen size
        spawnEnemies();
        if (currentLevel == 17 && !enemies.isEmpty()) {
            placeCityBossBottom(enemies.get(0), sw);
            cityBossAtUpper = false;
        } else if (currentLevel == 20 && !enemies.isEmpty()) {
            placeVolcanoBossBottomLeft(enemies.get(0));
            volcanoBossAtUpper = false;
        }
        repaint();
    }

    @Override public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (w > 0 && h > 0 && screen == Screen.PLAYING) initGame();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GAME LOOP  (~62 fps via Swing Timer)
    // ─────────────────────────────────────────────────────────────────────────
    @Override public void actionPerformed(ActionEvent e) {
        if (screen == Screen.DEAD) { repaint(); return; }
        if (screen != Screen.PLAYING) { repaint(); return; }

        int W = getWidth(), H = getHeight();
        int worldIdx = worldOfLevel(currentLevel);

        // ── Level complete: all enemies defeated → wait 90 ticks then advance ─
        if (levelCompleteTimer > 0) {
            levelCompleteTimer--;
            if (levelCompleteTimer == 0) {
                // Advance to the next level in this world, or back to world select
                int nextLvl = currentLevel + 1;
                boolean found = false;
                for (int[] row : WORLD_LEVELS)
                    for (int lv : row)
                        if (lv == nextLvl) { found = true; break; }
                if (found) launchLevel(nextLvl);
                else { screen = Screen.WORLD_SELECT; repaint(); }
            }
            repaint();
            return;
        }

        // Clear stale typed-level string after 2 s of no input
        if (typedLevel.length() > 0 && System.currentTimeMillis() - lastTypeTime > 2000)
            typedLevel = "";

        // ── Apply movement from held keys ─────────────────────────────────
        if (playerHp > 0) {
            if      (isP1Left()  && !isP1Right()) box.moveLeft();
            else if (isP1Right() && !isP1Left())  box.moveRight();
            else                                  box.stopHorizontal();
            box.update();
        } else {
            box.stopHorizontal();
        }

        if (player2Hp > 0) {
            if      (isP2Left()  && !isP2Right()) box2.moveLeft();
            else if (isP2Right() && !isP2Left())  box2.moveRight();
            else                                  box2.stopHorizontal();
            box2.update();
        } else {
            box2.stopHorizontal();
        }

        // ── Update background particles ───────────────────────────────────
        updateParticles(W, H, worldIdx);

        // ── Boss AI: jump + mini-enemy spawning ───────────────────────────
        if (isBossLevel(currentLevel) && !enemies.isEmpty()) {
            Enemy boss = enemies.get(0); // boss is always first in list
            if (boss.alive) {
                if (currentLevel == 17) {
                    // City boss: 4s radial fire (12 dirs), 5s mini spawn, 10s bottom↔upper jump cycle.
                    bossShootTimer++;
                    if (bossShootTimer >= CITY_BOSS_SHOOT_INTERVAL) {
                        bossShootTimer = 0;
                        fireCityBossRadial(boss, 12);
                    }

                    bossMiniSpawnTimer++;
                    if (bossMiniSpawnTimer >= CITY_BOSS_MINI_INTERVAL) {
                        bossMiniSpawnTimer = 0;
                        Enemy.Type miniType = bossMiniType(currentLevel);
                        Platform spawnPlatform = null;
                        if (!platforms.isEmpty()) {
                            java.util.List<Platform> candidates = new java.util.ArrayList<>();
                            for (Platform p : platforms) if (p.width >= 80) candidates.add(p);
                            if (!candidates.isEmpty())
                                spawnPlatform = candidates.get((int)(Math.random() * candidates.size()));
                        }
                        double mX, mY, lb, rb;
                        if (spawnPlatform != null) {
                            mX = spawnPlatform.x + spawnPlatform.width / 2.0;
                            mY = spawnPlatform.y + spawnPlatform.height - 10;
                            lb = spawnPlatform.x + 5;
                            rb = spawnPlatform.x + spawnPlatform.width - 5;
                        } else {
                            mX  = boss.x + boss.width / 2.0;
                            mY  = boss.y + boss.height - 5;
                            lb  = Math.max(0, mX - 100);
                            rb  = Math.min(W, mX + 100);
                        }
                        enemies.add(new Enemy(mX, mY, lb, rb, 1.8, H, miniType));
                    }

                    bossJumpTimer++;
                    if (bossJumpTimer >= CITY_BOSS_JUMP_INTERVAL) {
                        bossJumpTimer = 0;
                        cityBossAtUpper = !cityBossAtUpper;
                        if (cityBossAtUpper) placeCityBossUpper(boss, W);
                        else                 placeCityBossBottom(boss, W);
                    }
                } else if (currentLevel == 20) {
                    // Volcano jump cycle: bottom-left -> upper isolated platform every 10 seconds.
                    bossJumpTimer++;
                    if (bossJumpTimer >= VOLCANO_BOSS_JUMP_INTERVAL) {
                        bossJumpTimer = 0;
                        volcanoBossAtUpper = !volcanoBossAtUpper;
                        if (volcanoBossAtUpper) placeVolcanoBossUpper(boss);
                        else                    placeVolcanoBossBottomLeft(boss);
                    }

                    // Volcano boss: radial fire from mouth every 2.5s, red balls, 15 directions.
                    // Fire after potential jump so origin always matches current mouth position.
                    bossShootTimer++;
                    if (bossShootTimer >= VOLCANO_BOSS_SHOOT_INTERVAL) {
                        bossShootTimer = 0;
                        fireVolcanoBossMouthRadial(boss, 15);
                    }

                    bossMiniSpawnTimer++;
                    if (bossMiniSpawnTimer >= BOSS_MINI_INTERVAL) {
                        bossMiniSpawnTimer = 0;
                        Enemy.Type miniType = bossMiniType(currentLevel);
                        Platform spawnPlatform = null;
                        if (!platforms.isEmpty()) {
                            java.util.List<Platform> candidates = new java.util.ArrayList<>();
                            for (Platform p : platforms) if (p.width >= 80) candidates.add(p);
                            if (!candidates.isEmpty())
                                spawnPlatform = candidates.get((int)(Math.random() * candidates.size()));
                        }
                        double mX, mY, lb, rb;
                        if (spawnPlatform != null) {
                            mX = spawnPlatform.x + spawnPlatform.width / 2.0;
                            mY = spawnPlatform.y + spawnPlatform.height - 10;
                            lb = spawnPlatform.x + 5;
                            rb = spawnPlatform.x + spawnPlatform.width - 5;
                        } else {
                            mX  = boss.x + boss.width / 2.0;
                            mY  = boss.y + boss.height - 5;
                            lb  = Math.max(0, mX - 100);
                            rb  = Math.min(W, mX + 100);
                        }
                        enemies.add(new Enemy(mX, mY, lb, rb, 1.6, H, miniType));
                    }
                } else {
                    // Other bosses jump behavior
                    if (currentLevel != 18 && currentLevel != 19) {
                        bossJumpTimer++;
                        if (bossJumpTimer >= BOSS_JUMP_INTERVAL) {
                            bossJumpTimer = 0;
                            doBossJump(boss);
                        }
                    }

                    bossMiniSpawnTimer++;
                    if (bossMiniSpawnTimer >= BOSS_MINI_INTERVAL) {
                        bossMiniSpawnTimer = 0;
                        Enemy.Type miniType = bossMiniType(currentLevel);
                        Platform spawnPlatform = null;
                        if (!platforms.isEmpty()) {
                            java.util.List<Platform> candidates = new java.util.ArrayList<>();
                            for (Platform p : platforms) if (p.width >= 80) candidates.add(p);
                            if (!candidates.isEmpty())
                                spawnPlatform = candidates.get((int)(Math.random() * candidates.size()));
                        }
                        double mX, mY, lb, rb;
                        if (spawnPlatform != null) {
                            mX = spawnPlatform.x + spawnPlatform.width / 2.0;
                            mY = spawnPlatform.y + spawnPlatform.height - 10;
                            lb = spawnPlatform.x + 5;
                            rb = spawnPlatform.x + spawnPlatform.width - 5;
                        } else {
                            mX  = boss.x + boss.width / 2.0;
                            mY  = boss.y + boss.height - 5;
                            lb  = Math.max(0, mX - 100);
                            rb  = Math.min(W, mX + 100);
                        }
                        double miniSpeed = (currentLevel == 20) ? 1.6 : 1.8;
                        enemies.add(new Enemy(mX, mY, lb, rb, miniSpeed, H, miniType));
                    }
                }
            }
        }

        // ── Enemy AI + collision with player ─────────────────────────────
        // Use index loop so we can safely add mini-enemies above without ConcurrentModificationException
        for (int i = 0; i < enemies.size(); i++) {
            Enemy en = enemies.get(i);
            en.screenHeight = H;
            en.update();

            // Enemy body touches players → player loses 1 HP (invincibility window prevents spam)
            if (en.alive && playerHp > 0 && box.hitFlashTimer == 0
                    && box.getBounds().intersects(en.getBounds())) {
                playerHp = Math.max(0.0, playerHp - 1.0);
                box.hitFlashTimer = 60;
            }
            if (en.alive && player2Hp > 0 && box2.hitFlashTimer == 0
                    && box2.getBounds().intersects(en.getBounds())) {
                player2Hp = Math.max(0.0, player2Hp - 1.0);
                box2.hitFlashTimer = 60;
            }

            // Enemy projectile hits players → 1 HP damage
            for (Enemy.EnemyProjectile pr : en.projectiles) {
                double projectileDamage = en.isBoss() ? (PLAYER_MAX_HP * 0.10) : 1.0;
                if (pr.active && playerHp > 0 && box.hitFlashTimer == 0
                        && box.getBounds().intersects(pr.getBounds())) {
                    pr.active = false;
                    playerHp = Math.max(0.0, playerHp - projectileDamage);
                    box.hitFlashTimer = 60;
                }
                if (pr.active && player2Hp > 0 && box2.hitFlashTimer == 0
                        && box2.getBounds().intersects(pr.getBounds())) {
                    pr.active = false;
                    player2Hp = Math.max(0.0, player2Hp - projectileDamage);
                    box2.hitFlashTimer = 60;
                }
            }
        }

        if (playerHp <= 0 && player2Hp <= 0) {
            screen = Screen.DEAD; repaint(); return;
        }

        // ── Player ice-ball hits enemy → reduce enemy HP ──────────────────
        for (Enemy en : enemies) {
            if (!en.alive) continue;
            for (Box.Projectile pr : box.projectiles) {
                if (pr.active && pr.getBounds().intersects(en.getBounds())) {
                    pr.active = false;      // ice-ball disappears on hit
                    en.takeDamage(2);       // reduce enemy HP (2x power)
                    if (!en.alive) score += isBossLevel(currentLevel) ? 10 : 1;
                    break;
                }
            }
            if (!en.alive) continue;
            for (Box.Projectile pr : box2.projectiles) {
                if (pr.active && pr.getBounds().intersects(en.getBounds())) {
                    pr.active = false;
                    en.takeDamage(2);       // reduce enemy HP (2x power)
                    if (!en.alive) score += isBossLevel(currentLevel) ? 10 : 1;
                    break;
                }
            }
        }

        // ── Remove dead enemies ───────────────────────────────────────────
        enemies.removeIf(en -> !en.alive);

        // Boss levels: as soon as boss is dead, level is won even if minis remain.
        if (isBossLevel(currentLevel)) {
            boolean bossAlive = enemies.stream().anyMatch(Enemy::isBoss);
            if (!bossAlive) {
                enemies.clear();
            }
        }

        // ── Check level complete: all enemies defeated ────────────────────
        if (enemies.isEmpty() && levelCompleteTimer == 0) {
            levelCompleteTimer = 90; // ~1.5 sec delay before advancing
        }

        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  CLICK HANDLING
    // ─────────────────────────────────────────────────────────────────────────
    private void handleClick(int mx, int my) {
        int W = getWidth(), H = getHeight();
        switch (screen) {
            case SPLASH:
                if (startButtonRect(W, H).contains(mx, my)) {
                    screen = Screen.WORLD_SELECT; repaint();
                }
                break;
            case WORLD_SELECT:
                for (int i = 0; i < 4; i++) {
                    if (worldCardRect(i, W, H).contains(mx, my)) {
                        selectedWorld = i;
                        screen = Screen.LEVEL_SELECT; repaint(); return;
                    }
                }
                break;
            case LEVEL_SELECT:
                if (backRect(W, H).contains(mx, my)) {
                    screen = Screen.WORLD_SELECT; repaint(); return;
                }
                for (int slot = 0; slot < LEVELS_PER_WORLD; slot++) {
                    if (levelCardRect(slot, W, H).contains(mx, my)) {
                        launchLevel(WORLD_LEVELS[selectedWorld][slot]); return;
                    }
                }
                break;
            case DEAD:
                // Try Again button
                if (deadTryAgainRect(W, H).contains(mx, my)) { launchLevel(currentLevel); return; }
                // Main Menu button
                if (deadMenuRect(W, H).contains(mx, my)) { screen = Screen.WORLD_SELECT; repaint(); }
                break;
            case PLAYING:
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  GEOMETRY HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private Rectangle startButtonRect(int W, int H)    { return new Rectangle(W-250, H-80, 200, 60); }
    private Rectangle backRect(int W, int H)           { return new Rectangle(30, 25, 120, 46); }
    private Rectangle deadTryAgainRect(int W, int H)   { return new Rectangle(W/2-220, H/2+60, 180, 55); }
    private Rectangle deadMenuRect(int W, int H)       { return new Rectangle(W/2+40,  H/2+60, 180, 55); }

    private Rectangle worldCardRect(int idx, int W, int H) {
        int padX=30, padY=110, gap=18, total=W-padX*2-gap*3, cw=total/4, ch=H-padY-60;
        return new Rectangle(padX + idx*(cw+gap), padY, cw, ch);
    }
    private Rectangle levelCardRect(int slot, int W, int H) {
        int count=LEVELS_PER_WORLD, padX=60, gap=22, total=W-padX*2-gap*(count-1);
        int cw=total/count, ch=(int)(cw*1.35), cy=(H-ch)/2+30;
        return new Rectangle(padX + slot*(cw+gap), cy, cw, ch);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  PAINT DISPATCH
    // ─────────────────────────────────────────────────────────────────────────
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (screen) {
            case SPLASH:       paintSplash(g);       break;
            case WORLD_SELECT: paintWorldSelect(g);  break;
            case LEVEL_SELECT: paintLevelSelect(g);  break;
            case PLAYING:      paintGame(g);         break;
            case DEAD:         paintGame(g); paintDeadOverlay(g); break;
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SPLASH SCREEN  (unchanged from Main(6))
    // ═════════════════════════════════════════════════════════════════════════
    private void paintSplash(Graphics g) {
        int W = getWidth(), H = getHeight();
        Graphics2D g2 = aa(g);
        if (bgImages[0] != null) g2.drawImage(bgImages[0], 0, 0, W, H, this);
        else { g2.setColor(new Color(5,8,25)); g2.fillRect(0,0,W,H); }
        GradientPaint vig = new GradientPaint(W/2f,0,new Color(0,0,0,40),W/2f,H,new Color(0,0,0,200));
        g2.setPaint(vig); g2.fillRect(0,0,W,H);

        String line1="PLATFORM", line2="QUEST";
        g2.setFont(new Font("Courier New", Font.BOLD, W/10));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(0,0,0,200));
        g2.drawString(line1,(W-fm.stringWidth(line1))/2+5,H/2-60+5);
        g2.drawString(line2,(W-fm.stringWidth(line2))/2+5,H/2+fm.getHeight()-50+5);
        g2.setPaint(new GradientPaint(0,H/2f-120,new Color(255,220,80),0,H/2f+80,new Color(255,100,30)));
        g2.drawString(line1,(W-fm.stringWidth(line1))/2,H/2-60);
        g2.drawString(line2,(W-fm.stringWidth(line2))/2,H/2+fm.getHeight()-50);

        g2.setFont(new Font("Courier New", Font.PLAIN, 22));
        g2.setColor(new Color(220,220,255,210));
        String sub="4 Worlds  ·  16 Levels  ·  Epic Bosses";
        FontMetrics fsm=g2.getFontMetrics();
        g2.drawString(sub,(W-fsm.stringWidth(sub))/2,H/2+fm.getHeight()+10);

        g2.setFont(new Font("Courier New", Font.PLAIN, 16));
        g2.setColor(new Color(180,180,220,180));
        String hint="ARROWS/WASD: Move   SPACE/W: Jump   Z/F: Shoot";
        FontMetrics fh=g2.getFontMetrics();
        g2.drawString(hint,(W-fh.stringWidth(hint))/2,H/2+fm.getHeight()+40);

        Rectangle btn=startButtonRect(W,H);
        paintGlowButton(g2,btn,"▶  START",btn.contains(mouse),new Color(255,180,30),new Color(255,220,80));
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  WORLD SELECT  (unchanged from Main(6))
    // ═════════════════════════════════════════════════════════════════════════
    private void paintWorldSelect(Graphics g) {
        int W=getWidth(), H=getHeight();
        Graphics2D g2=aa(g);
        g2.setColor(new Color(8,8,18)); g2.fillRect(0,0,W,H);
        g2.setFont(new Font("Courier New",Font.BOLD,42)); g2.setColor(Color.WHITE);
        String t="SELECT  WORLD"; FontMetrics fm=g2.getFontMetrics();
        g2.drawString(t,(W-fm.stringWidth(t))/2,75);
        g2.setColor(new Color(255,180,30,180)); g2.setStroke(new BasicStroke(2f));
        int lx=(W-400)/2; g2.drawLine(lx,88,lx+400,88);
        for (int i=0;i<4;i++) {
            Rectangle r=worldCardRect(i,W,H); paintWorldCard(g2,r,i,r.contains(mouse));
        }
    }

    private void paintWorldCard(Graphics2D g2, Rectangle r, int idx, boolean hov) {
        Image bg=bgImages[idx];
        RoundRectangle2D rrect=new RoundRectangle2D.Float(r.x,r.y,r.width,r.height,22,22);
        Shape oldClip=g2.getClip(); g2.setClip(rrect);
        if (bg!=null) g2.drawImage(bg,r.x,r.y,r.width,r.height,null);
        else { g2.setColor(WORLD_TINT[idx]); g2.fillRect(r.x,r.y,r.width,r.height); }
        Color wt=WORLD_TINT[idx];
        g2.setPaint(new GradientPaint(r.x,r.y,new Color(wt.getRed(),wt.getGreen(),wt.getBlue(),hov?80:130),
            r.x,r.y+r.height,new Color(0,0,0,hov?180:220)));
        g2.fillRect(r.x,r.y,r.width,r.height); g2.setClip(oldClip);
        g2.setStroke(new BasicStroke(hov?3f:1.5f));
        Color ac=WORLD_ACCENT[idx];
        g2.setColor(hov?ac:new Color(ac.getRed(),ac.getGreen(),ac.getBlue(),120)); g2.draw(rrect);
        int bR=22; g2.setColor(ac); g2.fillOval(r.x+14,r.y+14,bR*2,bR*2);
        g2.setColor(Color.BLACK); g2.setFont(new Font("Courier New",Font.BOLD,20));
        String num=String.valueOf(idx+1); FontMetrics fn=g2.getFontMetrics();
        g2.drawString(num,r.x+14+bR-fn.stringWidth(num)/2,r.y+14+bR+fn.getAscent()/2-2);
        int fs=Math.max(14,r.width/6); g2.setFont(new Font("Courier New",Font.BOLD,fs));
        FontMetrics fw=g2.getFontMetrics(); String name=WORLD_NAMES[idx];
        int nx=r.x+(r.width-fw.stringWidth(name))/2, ny=r.y+r.height-30;
        g2.setColor(new Color(0,0,0,180)); g2.drawString(name,nx+2,ny+2);
        g2.setColor(hov?ac:Color.WHITE); g2.drawString(name,nx,ny);
        g2.setFont(new Font("Courier New",Font.PLAIN,13)); g2.setColor(new Color(200,200,200,200));
        String hint="4 Levels + Boss"; FontMetrics fh=g2.getFontMetrics();
        g2.drawString(hint,r.x+(r.width-fh.stringWidth(hint))/2,r.y+r.height-12);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LEVEL SELECT  (unchanged from Main(6))
    // ═════════════════════════════════════════════════════════════════════════
    private void paintLevelSelect(Graphics g) {
        int W=getWidth(), H=getHeight();
        Graphics2D g2=aa(g);
        Image bg=bgImages[selectedWorld];
        if (bg!=null) g2.drawImage(bg,0,0,W,H,this);
        else { g2.setColor(new Color(10,12,30)); g2.fillRect(0,0,W,H); }
        g2.setColor(new Color(0,0,0,170)); g2.fillRect(0,0,W,H);
        Rectangle back=backRect(W,H);
        paintGlowButton(g2,back,"◀ BACK",back.contains(mouse),new Color(50,50,70),new Color(90,90,120));
        Color accent=WORLD_ACCENT[selectedWorld];
        g2.setFont(new Font("Courier New",Font.BOLD,36)); g2.setColor(Color.WHITE);
        String title=WORLD_NAMES[selectedWorld]+"  —  CHOOSE LEVEL"; FontMetrics fm=g2.getFontMetrics();
        g2.drawString(title,(W-fm.stringWidth(title))/2,80);
        g2.setColor(accent); g2.setStroke(new BasicStroke(2f));
        int lw=fm.stringWidth(title); g2.drawLine((W-lw)/2,90,(W+lw)/2,90);
        for (int slot=0;slot<LEVELS_PER_WORLD;slot++) {
            Rectangle lr=levelCardRect(slot,W,H);
            paintLevelCard(g2,lr,slot+1,WORLD_LEVELS[selectedWorld][slot],
                slot==LEVELS_PER_WORLD-1,lr.contains(mouse),accent);
        }
    }

    private void paintLevelCard(Graphics2D g2, Rectangle r, int display, int lvl,
                                 boolean boss, boolean hov, Color accent) {
        Color base=boss?new Color(80,10,5):new Color(20,22,45);
        Color bord=boss?new Color(255,60,20):accent;
        RoundRectangle2D rr=new RoundRectangle2D.Float(r.x,r.y,r.width,r.height,18,18);
        g2.setPaint(new GradientPaint(r.x,r.y,hov?base.brighter():base,r.x,r.y+r.height,base.darker().darker()));
        g2.fill(rr); g2.setColor(hov?bord.brighter():bord); g2.setStroke(new BasicStroke(hov?3f:1.8f)); g2.draw(rr);
        if (boss) {
            g2.setFont(new Font("Segoe UI Emoji",Font.PLAIN,r.width/3+4)); FontMetrics fs=g2.getFontMetrics();
            String sk="💀"; g2.drawString(sk,r.x+(r.width-fs.stringWidth(sk))/2,r.y+r.height/2+10);
            g2.setFont(new Font("Courier New",Font.BOLD,Math.max(14,r.width/6)));
            g2.setColor(new Color(255,120,40)); FontMetrics fb=g2.getFontMetrics();
            String bs="BOSS"; g2.drawString(bs,r.x+(r.width-fb.stringWidth(bs))/2,r.y+r.height-20);
        } else {
            g2.setFont(new Font("Courier New",Font.BOLD,r.width/2));
            g2.setColor(hov?accent.brighter():Color.WHITE); FontMetrics fn=g2.getFontMetrics();
            String ns=String.valueOf(display);
            g2.drawString(ns,r.x+(r.width-fn.stringWidth(ns))/2,r.y+r.height/2+fn.getAscent()/3);
            g2.setFont(new Font("Courier New",Font.PLAIN,13)); g2.setColor(new Color(180,180,200,200));
            String sub="LVL "+lvl; FontMetrics fss=g2.getFontMetrics();
            g2.drawString(sub,r.x+(r.width-fss.stringWidth(sub))/2,r.y+r.height-15);
        }
        if (hov) {
            g2.setColor(new Color(bord.getRed(),bord.getGreen(),bord.getBlue(),35));
            g2.fillRoundRect(r.x-6,r.y-6,r.width+12,r.height+12,24,24);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  GAME SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    private void paintGame(Graphics g) {
        int W=getWidth(), H=getHeight();
        Graphics2D g2=aa(g);

        // Background image
        if (activeBg!=null) g2.drawImage(activeBg,0,0,W,H,this);
        else { g2.setColor(new Color(20,20,40)); g2.fillRect(0,0,W,H); }

        // Background particle animation (snow / sand / rain / embers)
        int worldIdx=worldOfLevel(currentLevel);
        drawParticles(g2, W, H, worldIdx);

        // Platforms, enemies, player
        for (Platform p : platforms) p.draw(g);
        for (Enemy en : enemies) en.draw(g);
        if (playerHp > 0) box.draw(g);
        if (player2Hp > 0) box2.draw(g);

        // ── HUD ──────────────────────────────────────────────────────────
        drawHUD(g2, W, H, worldIdx);

        // ── Level complete banner ─────────────────────────────────────────
        if (levelCompleteTimer > 0) {
            g2.setColor(new Color(0,0,0,160));
            g2.fillRoundRect(W/2-220, H/2-50, 440, 80, 18, 18);
            g2.setFont(new Font("Courier New", Font.BOLD, 40));
            g2.setColor(new Color(80,255,120));
            String cm = "LEVEL COMPLETE!";
            FontMetrics fcm = g2.getFontMetrics();
            g2.drawString(cm, W/2 - fcm.stringWidth(cm)/2, H/2+10);
        }

        // ── Player power label (top right under world tag) ────────────────
        // Shows which weapon/character is active
        String power = "P1:" + (box.activeSprite == 0 ? "ICE BLAST" : "ICE STORM")
                     + "   P2:" + (box2.activeSprite == 0 ? "ICE BLAST" : "ICE STORM");
        g2.setFont(new Font("Courier New", Font.BOLD, 13));
        g2.setColor(new Color(180,230,255,220));
        g2.drawString("POWER: " + power, W - g2.getFontMetrics().stringWidth("POWER: " + power) - 20, 60);

        // Typed-level overlay
        if (typedLevel.length()>0) {
            g2.setColor(new Color(0,0,0,190)); g2.fillRoundRect(W/2-120,H/2-50,240,72,14,14);
            g2.setColor(Color.YELLOW); g2.setFont(new Font("Courier New",Font.BOLD,34));
            String tl="Go "+typedLevel+"?"; FontMetrics tlfm=g2.getFontMetrics();
            g2.drawString(tl,W/2-tlfm.stringWidth(tl)/2,H/2+10);
        }
    }

    // ── HUD: level, score, player HP bar, boss HP bar, controls hint ──────────
    private void drawHUD(Graphics2D g2, int W, int H, int worldIdx) {
        // Panel background
        g2.setColor(new Color(0,0,0,150));
        g2.fillRoundRect(10, 10, 360, 100, 12, 12);

        // Level + Score
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Courier New", Font.BOLD, 22));
        g2.drawString("LVL " + currentLevel + "   SCORE: " + score, 20, 36);

        // ── Player HP bar ─────────────────────────────────────────────────
        g2.setColor(new Color(180, 180, 180));
        g2.setFont(new Font("Courier New", Font.BOLD, 14));
        g2.drawString("P1", 20, 56);
        int barX=50, barY=44, barW=220, barH=12;
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(barX, barY, barW, barH, 6, 6);
        // Colour shifts: green → yellow → red as HP drops
        float ratio = (float) playerHp / PLAYER_MAX_HP;
        Color hpColor = ratio > 0.6f ? new Color(50, 200, 80)
                      : ratio > 0.3f ? new Color(230, 190, 30)
                      : new Color(220, 40, 40);
        g2.setColor(hpColor);
        g2.fillRoundRect(barX, barY, (int)(barW * ratio), barH, 6, 6);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(barX, barY, barW, barH, 6, 6);

        g2.setColor(new Color(180, 180, 180));
        g2.drawString("P2", 20, 74);
        int p2BarY = 62;
        g2.setColor(new Color(60, 0, 0));
        g2.fillRoundRect(barX, p2BarY, barW, barH, 6, 6);
        float ratio2 = (float) player2Hp / PLAYER_MAX_HP;
        Color hpColor2 = ratio2 > 0.6f ? new Color(50, 180, 220)
                       : ratio2 > 0.3f ? new Color(230, 190, 30)
                       : new Color(220, 40, 40);
        g2.setColor(hpColor2);
        g2.fillRoundRect(barX, p2BarY, (int)(barW * ratio2), barH, 6, 6);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(barX, p2BarY, barW, barH, 6, 6);

        // Controls hint (small, at bottom of HUD panel)
        g2.setFont(new Font("Courier New", Font.PLAIN, 11));
        g2.setColor(new Color(200, 200, 220, 180));
        g2.drawString("P1: ARROWS+SPACE/Z   P2: A/D+W/F   ESC:Menu", 14, 94);

        // World tag top-right
        g2.setColor(WORLD_ACCENT[worldIdx]);
        g2.setFont(new Font("Courier New", Font.BOLD, 18));
        String wt=WORLD_NAMES[worldIdx]+" W"+(worldIdx+1);
        FontMetrics fwt=g2.getFontMetrics();
        g2.drawString(wt, W-fwt.stringWidth(wt)-20, 36);

        // ── Boss HP bar (top-centre, only on boss levels) ─────────────────
        if (isBossLevel(currentLevel) && !enemies.isEmpty()) {
            Enemy boss = enemies.get(0); // boss is always first enemy spawned
            int bBarW = 400, bBarH = 22, bBarX = (W - bBarW) / 2, bBarY = 14;
            // Dark background
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(bBarX-10, bBarY-6, bBarW+20, bBarH+18, 10, 10);
            // Label
            g2.setFont(new Font("Courier New", Font.BOLD, 13));
            g2.setColor(new Color(255, 80, 20));
            g2.drawString("BOSS", bBarX, bBarY + bBarH - 2);
            // Background of bar
            g2.setColor(new Color(80, 0, 0));
            g2.fillRoundRect(bBarX+48, bBarY, bBarW-48, bBarH, 6, 6);
            // Filled portion
            float bRatio = (float) boss.hp / Enemy.BOSS_HP;
            g2.setColor(new Color(220, 60, 20));
            g2.fillRoundRect(bBarX+48, bBarY, (int)((bBarW-48)*bRatio), bBarH, 6, 6);
            g2.setColor(new Color(255,255,255,60));
            g2.drawRoundRect(bBarX+48, bBarY, bBarW-48, bBarH, 6, 6);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DEAD SCREEN OVERLAY
    //  Drawn on top of the frozen game view (paintGame called first).
    // ═════════════════════════════════════════════════════════════════════════
    private void paintDeadOverlay(Graphics g) {
        int W=getWidth(), H=getHeight();
        Graphics2D g2=aa(g);

        // Semi-transparent dark film over the game
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, W, H);

        // Dialog box
        int dW=460, dH=220, dX=(W-dW)/2, dY=(H-dH)/2;
        g2.setColor(new Color(30, 10, 10, 230));
        g2.fillRoundRect(dX, dY, dW, dH, 20, 20);
        g2.setColor(new Color(200, 40, 40));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawRoundRect(dX, dY, dW, dH, 20, 20);

        // "YOU DIED" title
        g2.setFont(new Font("Courier New", Font.BOLD, 46));
        g2.setColor(new Color(220, 40, 40));
        String msg="YOU DIED"; FontMetrics fm=g2.getFontMetrics();
        g2.drawString(msg, dX+(dW-fm.stringWidth(msg))/2, dY+66);

        // Score line
        g2.setFont(new Font("Courier New", Font.PLAIN, 20));
        g2.setColor(new Color(200, 200, 200));
        String sc="Score: "+score; FontMetrics fsc=g2.getFontMetrics();
        g2.drawString(sc, dX+(dW-fsc.stringWidth(sc))/2, dY+104);

        // ── Buttons: Try Again  |  Main Menu ─────────────────────────────
        Rectangle tryR  = deadTryAgainRect(W, H);
        Rectangle menuR = deadMenuRect(W, H);
        paintGlowButton(g2, tryR,  "↩ TRY AGAIN", tryR.contains(mouse),
                        new Color(30,80,30),   new Color(60,160,60));
        paintGlowButton(g2, menuR, "⌂ MAIN MENU", menuR.contains(mouse),
                        new Color(30,30,80),   new Color(60,60,160));

        // Keyboard hint
        g2.setFont(new Font("Courier New", Font.PLAIN, 13));
        g2.setColor(new Color(160, 160, 160));
        String hint2="R = Try Again    M = Main Menu    ESC = Menu";
        FontMetrics fh=g2.getFontMetrics();
        g2.drawString(hint2, dX+(dW-fh.stringWidth(hint2))/2, dY+dH-14);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ═════════════════════════════════════════════════════════════════════════
    private Graphics2D aa(Graphics g) {
        Graphics2D g2=(Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g2;
    }

    private void paintGlowButton(Graphics2D g2, Rectangle r, String label,
                                  boolean hov, Color normal, Color hover) {
        Color c=hov?hover:normal;
        g2.setPaint(new GradientPaint(r.x,r.y,c.brighter(),r.x,r.y+r.height,c.darker()));
        g2.fillRoundRect(r.x,r.y,r.width,r.height,14,14);
        g2.setColor(hov?Color.WHITE:new Color(220,220,255)); g2.setStroke(new BasicStroke(hov?2.5f:1.5f));
        g2.drawRoundRect(r.x,r.y,r.width,r.height,14,14);
        g2.setFont(new Font("Courier New",Font.BOLD,18)); g2.setColor(Color.WHITE);
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(label,r.x+(r.width-fm.stringWidth(label))/2,r.y+(r.height+fm.getAscent()-fm.getDescent())/2);
        if (hov) {
            g2.setColor(new Color(hover.getRed(),hover.getGreen(),hover.getBlue(),40));
            g2.fillRoundRect(r.x-5,r.y-5,r.width+10,r.height+10,18,18);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame=new JFrame("Platform Quest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Main panel=new Main();
            frame.add(panel);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            frame.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
