package gdd.scene;

import gdd.Game;
import static gdd.Global.*;
import gdd.powerup.MultiShot;
import gdd.powerup.HealthUp;
import gdd.powerup.PowerUp;
import gdd.powerup.SpeedUp;
import gdd.sprite.Alien1;
import gdd.sprite.Alien2;
import gdd.sprite.Boss;
import gdd.sprite.Enemy;
import gdd.sprite.EnemyShot;
import gdd.sprite.Player;
import gdd.sprite.Shot;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;

public class Scene1 extends JPanel {
    protected static final int FLIGHT_FRAMES = 60 * 60 * 5;
    private static final long STEP_NANOS = 1_000_000_000L / 60;
    private static final long MAX_ELAPSED_NANOS = STEP_NANOS * 5;
    private static final int[][] SHOT_SPREADS = { { 0 }, { -2, 2 }, { -3, 0, 3 }, { -5, -2, 2, 5 } };
    private static final Font POWER_FONT = new Font("Dialog", Font.BOLD, 18);
    private static final Font DASHBOARD_FONT = new Font("Monospaced", Font.BOLD, 14);
    private static final Font END_FONT = new Font("Dialog", Font.BOLD, 24);
    private static final Font STAGE_FONT = new Font("Dialog", Font.BOLD, 52);
    private static final Font STAGE_NAME_FONT = new Font("Dialog", Font.BOLD, 24);
    private static final Font OBJECTIVE_FONT = new Font("Dialog", Font.PLAIN, 17);
    private static final Font WARNING_FONT = new Font("Dialog", Font.BOLD, 30);
    private static final Color IMPACT_COLOR = new Color(255, 190, 65);
    private static final Color STAGE_ONE_SKY = new Color(3, 9, 30);
    private static final Color STAGE_TWO_SKY = new Color(24, 4, 35);
    private static final Color DASHBOARD_BACKGROUND = new Color(0, 5, 18, 220);
    private static final Color HUD_CYAN = new Color(50, 220, 255);
    protected final Game game;
    protected final int stageNumber;
    private final Random random = new Random(1717);
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Shot> shots = new ArrayList<>();
    private final List<EnemyShot> enemyShots = new ArrayList<>();
    private final List<PowerUp> powerups = new ArrayList<>();
    private final List<Spark> sparks = new ArrayList<>();
    private Player player;
    private Timer timer;
    private int frame;
    private int score;
    private int spawnClock;
    private int invulnerable;
    private boolean playing;
    private boolean bossSpawned;
    private int bossBeamWarning;
    private int bossBeamFrames;
    private int bossBeamY;
    private int stageIntroFrames;
    private int bossIntroFrames;
    private int bossFightFrames;
    private String endMessage = "";
    private long lastTickNanos;
    private long accumulatedNanos;

    public Scene1(Game game) {
        this(game, 1);
    }

    protected Scene1(Game game, int stageNumber) {
        this.game = game;
        this.stageNumber = stageNumber;
        setBackground(new Color(3, 7, 22));
        setFocusable(true);
        addKeyListener(new Controls());
    }

    public void start() {
        startWithProgress(null, 0);
    }

    public void startWithProgress(Player previousPlayer, int carriedScore) {
        if (timer != null)
            timer.stop();
        player = new Player(previousPlayer);
        enemies.clear();
        shots.clear();
        enemyShots.clear();
        powerups.clear();
        sparks.clear();
        frame = spawnClock = 0;
        score = carriedScore;
        playing = true;
        bossSpawned = false;
        bossBeamWarning = bossBeamFrames = 0;
        stageIntroFrames = 150;
        bossIntroFrames = 0;
        bossFightFrames = 0;
        SwingUtilities.invokeLater(this::requestFocusInWindow);
        lastTickNanos = System.nanoTime();
        accumulatedNanos = 0;
        timer = new Timer(15, e -> cycle());
        timer.setCoalesce(true);
        timer.start();
    }

    public Player getPlayer() {
        return player;
    }

    public int getScore() {
        return score;
    }

    public void stop() {
        if (timer != null)
            timer.stop();
    }

    private void cycle() {
        if (!playing)
            return;
        long now = System.nanoTime();
        long elapsed = Math.min(now - lastTickNanos, MAX_ELAPSED_NANOS);
        lastTickNanos = now;
        accumulatedNanos += elapsed;
        while (accumulatedNanos >= STEP_NANOS && playing) {
            updateFrame();
            accumulatedNanos -= STEP_NANOS;
        }
        repaint();
    }

    private void updateFrame() {
        if (stageIntroFrames > 0) {
            stageIntroFrames--;
            return;
        }
        frame++;
        spawnClock++;
        player.act();
        if (invulnerable > 0)
            invulnerable--;

        if (frame < FLIGHT_FRAMES) {
            int interval = stageNumber == 1 ? 150 : 110;
            if (spawnClock >= interval) {
                spawnClock = 0;
                spawnWave(frame / interval);
            }
            if (frame % 600 == 180)
                powerups.add(new SpeedUp(BOARD_WIDTH + 20, 130 + random.nextInt(450)));
            if (frame % 480 == 300)
                powerups.add(new MultiShot(BOARD_WIDTH + 20, 130 + random.nextInt(450)));
            if (frame % 900 == 540)
                powerups.add(new HealthUp(BOARD_WIDTH + 20, 130 + random.nextInt(450)));
        } else if (stageNumber == 1) {
            finishStageOne();
            return;
        } else if (!bossSpawned) {
            enemies.clear();
            enemyShots.clear();
            shots.clear();
            bossSpawned = true;
            bossIntroFrames = 150;
        }
        if (bossIntroFrames > 0) {
            bossIntroFrames--;
            if (bossIntroFrames == 0)
                enemies.add(new Boss(BOARD_WIDTH + 120, 300));
            return;
        }
        if (stageNumber == 2 && bossSpawned) {
            bossFightFrames++;
            if (bossFightFrames == 75 || bossFightFrames % 360 == 0) {
                spawnBossPowerUp();
            }
        }
        updateObjects();
    }

    private void spawnBossPowerUp() {
        int y = 130 + random.nextInt(450);
        boolean needsSpeed = player.getSpeedLevel() < 2;
        boolean needsShots = player.getShotLevel() < 4;
        if (player.getHealth() <= 3) {
            powerups.add(new HealthUp(BOARD_WIDTH + 20, y));
        } else if (needsShots && (!needsSpeed || (bossFightFrames / 360) % 2 == 0)) {
            powerups.add(new MultiShot(BOARD_WIDTH + 20, y));
        } else {
            powerups.add(new SpeedUp(BOARD_WIDTH + 20, y));
        }
    }

    private void spawnWave(int wave) {
        int centerY = 150 + random.nextInt(390);
        if (stageNumber == 1) {
            for (int i = 0; i < 2; i++)
                enemies.add(new Alien1(BOARD_WIDTH + 35 + i * 58, centerY + (i * 2 - 1) * 42));
            if (wave % 4 == 0)
                enemies.add(new Alien2(BOARD_WIDTH + 180, centerY));
        } else if (wave % 2 == 0) {
            enemies.add(new Alien2(BOARD_WIDTH + 35, 105));
            enemies.add(new Alien2(BOARD_WIDTH + 90, 570));
            enemies.add(new Alien1(BOARD_WIDTH + 150, centerY));
        } else {
            for (int i = 0; i < 3; i++)
                enemies.add(new Alien1(BOARD_WIDTH + 35 + i * 52, 150 + i * 170));
        }
    }

    private void finishStageOne() {
        playing = false;
        timer.stop();
        game.loadScene2();
    }

    private void updateObjects() {
        for (PowerUp power : powerups) {
            power.act();
            if (power.isVisible() && hit(power.getX(), power.getY(), 34, 34,
                    player.getX(), player.getY(), 48, 28))
                power.upgrade(player);
            if (power.getX() < -50)
                power.die();
        }
        for (Shot shot : shots) {
            shot.act();
            if (shot.getX() > BOARD_WIDTH + 20 || shot.getY() < 55 || shot.getY() > BOARD_HEIGHT)
                shot.die();
        }
        updateEnemyShots();
        for (Enemy enemy : enemies) {
            if (!enemy.isVisible())
                continue;
            enemy.act();
            fireEnemyWeapons(enemy);
            int ew = enemy instanceof Boss ? 135 : 42;
            int eh = enemy instanceof Boss ? 115 : 34;
            for (Shot shot : shots) {
                if (shot.isVisible() && hit(shot.getX(), shot.getY(), 16, 5,
                        enemy.getX(), enemy.getY(), ew, eh)) {
                    shot.die();
                    enemy.hit();
                    burst(shot.getX(), shot.getY(), IMPACT_COLOR, 5);
                    if (!enemy.isVisible()) {
                        score += enemy.getPoints();
                        burst(enemy.getX() + ew / 2, enemy.getY() + eh / 2, Color.CYAN,
                                enemy instanceof Boss ? 60 : 14);
                    }
                }
            }
            if (enemy.isVisible() && invulnerable == 0
                    && hit(player.getX(), player.getY(), 48, 28, enemy.getX(), enemy.getY(), ew, eh)) {
                damagePlayer();
            }
            if (enemy.getX() < -160)
                enemy.die();
        }
        for (Spark spark : sparks)
            spark.update();
        shots.removeIf(s -> !s.isVisible());
        enemyShots.removeIf(s -> !s.isVisible());
        enemies.removeIf(e -> !e.isVisible());
        powerups.removeIf(p -> !p.isVisible());
        sparks.removeIf(s -> s.life <= 0);

        if (player.getHealth() <= 0)
            end("MISSION FAILED — Press ENTER");
        if (stageNumber == 2 && bossSpawned && enemies.isEmpty())
            end("GALAXY SAVED! Score " + score + " — Press ENTER");
    }

    private void fireEnemyWeapons(Enemy enemy) {
        if (enemy.getX() > BOARD_WIDTH || enemy.getX() < 120)
            return;
        if (enemy instanceof Boss) {
            Boss boss = (Boss) enemy;
            int phase = boss.getPhase();
            int rate = phase == 1 ? 90 : phase == 2 ? 65 : 45;
            if (boss.getAge() % rate == 0) {
                int count = phase * 2 + 1;
                for (int i = 0; i < count; i++) {
                    double velocityY = (i - (count - 1) / 2.0) * .85;
                    enemyShots.add(new EnemyShot(boss.getX(), boss.getY() + 55,
                            -4.8, velocityY, phase == 3));
                }
            }
            if (boss.getAge() % 360 == 250 && bossBeamWarning == 0 && bossBeamFrames == 0) {
                bossBeamY = player.getY() + 14;
                bossBeamWarning = 75;
            }
            return;
        }

        int rate = stageNumber == 1 ? 160 : 110;
        if (enemy.getAge() % rate == 45) {
            double targetX = player.getX() - enemy.getX();
            double targetY = player.getY() - enemy.getY();
            double length = Math.max(1, Math.sqrt(targetX * targetX + targetY * targetY));
            double speed = stageNumber == 1 ? 3.4 : 4.2;
            enemyShots.add(new EnemyShot(enemy.getX(), enemy.getY() + 16,
                    targetX / length * speed, targetY / length * speed, enemy instanceof Alien2));
        }
    }

    private void updateEnemyShots() {
        for (EnemyShot shot : enemyShots) {
            shot.act();
            if (shot.getX() < -30 || shot.getX() > BOARD_WIDTH + 30
                    || shot.getY() < 55 || shot.getY() > BOARD_HEIGHT + 20)
                shot.die();
            if (shot.isVisible() && invulnerable == 0
                    && hit(shot.getX(), shot.getY(), shot.isHeavy() ? 16 : 10,
                            shot.isHeavy() ? 16 : 10, player.getX(), player.getY(), 48, 28)) {
                shot.die();
                damagePlayer();
            }
        }
        if (bossBeamWarning > 0) {
            bossBeamWarning--;
            if (bossBeamWarning == 0)
                bossBeamFrames = 22;
        } else if (bossBeamFrames > 0) {
            bossBeamFrames--;
            if (invulnerable == 0 && player.getY() + 28 > bossBeamY - 13
                    && player.getY() < bossBeamY + 13)
                damagePlayer();
        }
    }

    private void damagePlayer() {
        player.damage();
        invulnerable = 105;
        burst(player.getX(), player.getY(), Color.RED, 18);
    }

    private void end(String text) {
        playing = false;
        endMessage = text;
        timer.stop();
    }

    private boolean hit(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private void burst(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++)
            sparks.add(new Spark(x, y, random.nextInt(9) - 4, random.nextInt(9) - 4, color));
    }

    private void fire() {
        if (!playing || shots.size() > 22)
            return;
        for (int velocity : SHOT_SPREADS[player.getShotLevel() - 1])
            shots.add(new Shot(player.getX() + 42, player.getY() + 12, velocity));
    }

    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g);
        for (PowerUp p : powerups)
            drawPower(g, p);
        for (Enemy e : enemies)
            drawEnemy(g, e);
        for (Shot s : shots)
            drawShot(g, s);
        for (EnemyShot s : enemyShots)
            drawEnemyShot(g, s);
        drawBossBeam(g);
        for (Spark s : sparks)
            s.draw(g);
        if (player != null && (invulnerable / 5) % 2 == 0)
            drawPlayer(g);
        drawDashboard(g);
        if (stageIntroFrames > 0)
            drawStageIntro(g);
        if (bossIntroFrames > 0)
            drawBossIntro(g);
        if (!playing && !endMessage.isEmpty())
            drawEnd(g);
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setPaint(new GradientPaint(0, 60,
                stageNumber == 1 ? new Color(2, 20, 52) : new Color(34, 2, 44),
                BOARD_WIDTH, BOARD_HEIGHT,
                stageNumber == 1 ? new Color(1, 4, 17) : new Color(5, 0, 17)));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        if (stageNumber == 1)
            drawOuterReach(g);
        else
            drawVoidCitadel(g);

        // Three star layers provide continuous horizontal parallax.
        for (int layer = 1; layer <= 3; layer++) {
            g.setColor(stageNumber == 1
                    ? new Color(100 + layer * 35, 155 + layer * 25, 255, 155)
                    : new Color(180 + layer * 20, 80 + layer * 25, 230 + layer * 8, 145));
            int speed = layer * 2;
            for (int i = 0; i < 34; i++) {
                int x = Math.floorMod(i * 149 - frame * speed, BOARD_WIDTH + 80) - 40;
                int y = 65 + Math.floorMod(i * 83 + layer * 47, BOARD_HEIGHT - 85);
                g.fillOval(x, y, layer + 1, layer);
            }
        }
    }

    private void drawOuterReach(Graphics2D g) {
        int planetX = Math.floorMod(980 - frame / 5, 1800) - 330;
        int planetY = 105;
        g.setColor(new Color(30, 95, 145, 65));
        g.fillOval(planetX - 45, planetY + 90, 330, 58);
        g.setColor(new Color(35, 98, 145));
        g.fillOval(planetX, planetY, 240, 240);
        g.setColor(new Color(80, 180, 205, 90));
        g.fillArc(planetX + 18, planetY + 13, 204, 210, 70, 170);
        g.setColor(new Color(195, 245, 255, 75));
        g.fillOval(planetX + 42, planetY + 36, 72, 35);
        g.setStroke(new BasicStroke(6f));
        g.setColor(new Color(105, 210, 235, 105));
        g.drawOval(planetX - 52, planetY + 87, 345, 65);
        g.setStroke(new BasicStroke(1f));

        // Nearby tumbling asteroid silhouettes form the fastest background layer.
        for (int i = 0; i < 9; i++) {
            int x = Math.floorMod(i * 263 - frame * 4, BOARD_WIDTH + 260) - 130;
            int y = 105 + Math.floorMod(i * 127, BOARD_HEIGHT - 170);
            int size = 12 + Math.floorMod(i * 11, 24);
            g.setColor(new Color(42, 58, 77));
            g.fillPolygon(new int[] { x, x + size / 2, x + size, x + size * 3 / 4, x + 4 },
                    new int[] { y + size / 3, y, y + size / 4, y + size, y + size * 4 / 5 }, 5);
            g.setColor(new Color(105, 130, 145, 80));
            g.drawLine(x + 5, y + 5, x + size / 2, y + size / 3);
        }
    }

    private void drawVoidCitadel(Graphics2D g) {
        // Slow nebula clouds give Stage 2 its stormy red-purple identity.
        for (int i = 0; i < 5; i++) {
            int x = Math.floorMod(i * 310 - frame / 3, BOARD_WIDTH + 500) - 250;
            int y = 100 + i * 115;
            g.setColor(new Color(175, 20 + i * 8, 145, 24));
            g.fillOval(x, y, 390, 160);
            g.setColor(new Color(65, 25, 135, 28));
            g.fillOval(x + 120, y - 35, 310, 150);
        }

        // Distant alien towers scroll like a fortress on the horizon.
        int baseY = BOARD_HEIGHT - 95;
        g.setColor(new Color(18, 5, 35));
        g.fillRect(0, baseY, BOARD_WIDTH, 95);
        for (int i = 0; i < 7; i++) {
            int x = Math.floorMod(i * 185 - frame, BOARD_WIDTH + 210) - 105;
            int height = 65 + Math.floorMod(i * 37, 125);
            g.fillPolygon(new int[] { x, x + 35, x + 52, x + 69, x + 104 },
                    new int[] { baseY, baseY - height + 25, baseY - height,
                            baseY - height + 25, baseY },
                    5);
            g.setColor(new Color(255, 35, 150, 105));
            g.fillRect(x + 49, baseY - height + 19, 6, height - 32);
            g.setColor(new Color(18, 5, 35));
        }

        int riftX = Math.floorMod(1300 - frame / 4, 1900) - 300;
        g.setColor(new Color(255, 35, 175, 35));
        g.fillOval(riftX, 110, 240, 360);
        g.setColor(new Color(255, 90, 220, 105));
        g.drawArc(riftX + 55, 130, 130, 320, 75, 220);
    }

    private void drawPlayer(Graphics2D g) {
        int x = player.getX(), y = player.getY();
        int pulse = (frame / 4) % 3;
        g.setColor(new Color(50, 220, 255, 130));
        g.fillPolygon(new int[] { x, x - 18 - pulse * 4, x }, new int[] { y + 8, y + 14, y + 21 }, 3);
        g.setColor(new Color(225, 238, 255));
        g.fillPolygon(new int[] { x, x + 48, x + 17, x + 5 }, new int[] { y, y + 14, y + 28, y + 22 }, 4);
        g.setColor(new Color(255, 72, 150));
        g.fillOval(x + 15, y + 7, 17, 10);
        g.setColor(Color.CYAN);
        g.drawLine(x + 5, y + 2 + pulse, x - 7, y - 5);
    }

    private void drawEnemy(Graphics2D g, Enemy e) {
        if (e instanceof Boss) {
            drawBoss(g, (Boss) e);
            return;
        }
        int x = e.getX(), y = e.getY(), wing = 5 + (frame / 6) % 6;
        if (e instanceof Alien2) {
            g.setColor(new Color(255, 105, 45));
            g.fillRoundRect(x, y, 42, 34, 12, 12);
            g.setColor(Color.YELLOW);
            g.fillOval(x + 8, y + 10, 7, 7);
            g.fillOval(x + 27, y + 10, 7, 7);
            g.setColor(Color.RED);
            g.drawArc(x - wing, y - wing, 42 + wing * 2, 34 + wing * 2, 190, 160);
        } else {
            g.setColor(new Color(128, 255, 115));
            g.fillOval(x + 5, y + 5, 32, 24);
            g.fillPolygon(new int[] { x, x + 12, x + 9 }, new int[] { y + 17, y + 12 - wing, y + 27 }, 3);
            g.fillPolygon(new int[] { x + 42, x + 30, x + 33 }, new int[] { y + 17, y + 12 - wing, y + 27 }, 3);
            g.setColor(new Color(30, 15, 55));
            g.fillOval(x + 13, y + 13, 6, 7);
            g.fillOval(x + 25, y + 13, 6, 7);
        }
    }

    private void drawBoss(Graphics2D g, Boss b) {
        int x = b.getX(), y = b.getY();
        int phase = b.getPhase();
        int wingMotion = (int) (Math.sin(frame * .13) * 6);
        int corePulse = 4 + (int) (Math.sin(frame * .22) * 3);
        Color armor = phase == 1 ? new Color(65, 35, 110)
                : phase == 2 ? new Color(105, 25, 105) : new Color(135, 20, 62);
        Color energy = phase == 1 ? new Color(80, 225, 255)
                : phase == 2 ? new Color(255, 80, 220) : new Color(255, 55, 70);

        // Animated rear engines make the flagship read clearly as a left-facing ship.
        int flame = 10 + (frame / 3) % 9;
        g.setColor(new Color(80, 210, 255, 75));
        g.fillPolygon(new int[] { x + 116, x + 135 + flame, x + 116 },
                new int[] { y + 28, y + 36, y + 44 }, 3);
        g.fillPolygon(new int[] { x + 116, x + 135 + flame, x + 116 },
                new int[] { y + 71, y + 79, y + 87 }, 3);
        g.setColor(Color.WHITE);
        g.fillRect(x + 113, y + 32, 12, 8);
        g.fillRect(x + 113, y + 75, 12, 8);

        // Four armored wings open farther as the boss becomes more aggressive.
        int spread = wingMotion + phase * 3;
        g.setColor(new Color(34, 16, 64));
        g.fillPolygon(new int[] { x + 42, x + 82, x + 126, x + 96, x + 55 },
                new int[] { y + 52, y - 10 - spread, y + 7, y + 43, y + 58 }, 5);
        g.fillPolygon(new int[] { x + 42, x + 82, x + 126, x + 96, x + 55 },
                new int[] { y + 63, y + 125 + spread, y + 108, y + 72, y + 57 }, 5);
        g.setColor(armor);
        g.fillPolygon(new int[] { x + 28, x + 63, x + 111, x + 127, x + 93, x + 43, x },
                new int[] { y + 27, y + 15, y + 31, y + 57, y + 87, y + 91, y + 58 }, 7);

        // Plated hull and forward weapon mandibles.
        g.setColor(new Color(155, 105, 190));
        g.drawLine(x + 55, y + 24, x + 34, y + 84);
        g.drawLine(x + 91, y + 31, x + 93, y + 82);
        g.setColor(armor.brighter());
        g.fillPolygon(new int[] { x, x + 34, x + 29, x - 13 },
                new int[] { y + 58, y + 39, y + 52, y + 46 }, 4);
        g.fillPolygon(new int[] { x, x + 34, x + 29, x - 13 },
                new int[] { y + 58, y + 76, y + 64, y + 70 }, 4);

        // Rotating reactor rings telegraph the boss phase and beam weapon.
        g.setStroke(new BasicStroke(4f));
        g.setColor(new Color(energy.getRed(), energy.getGreen(), energy.getBlue(), 75));
        g.fillOval(x + 42 - corePulse, y + 35 - corePulse,
                46 + corePulse * 2, 46 + corePulse * 2);
        g.setColor(energy);
        g.drawArc(x + 43, y + 36, 44, 44, frame * 5, 225);
        g.drawArc(x + 48, y + 41, 34, 34, -frame * 7, 210);
        g.fillOval(x + 57, y + 50, 16, 16);
        g.setColor(Color.WHITE);
        g.fillOval(x + 62, y + 55, 6, 6);
        g.setStroke(new BasicStroke(1f));

        // Weapon ports flash in time with the projectile volley.
        boolean portsHot = b.getAge() % (phase == 1 ? 90 : phase == 2 ? 65 : 45) < 12;
        g.setColor(portsHot ? Color.WHITE : energy);
        g.fillOval(x + 4, y + 37, 9, 9);
        g.fillOval(x + 4, y + 69, 9, 9);

        g.setFont(DASHBOARD_FONT);
        g.setColor(Color.WHITE);
        g.drawString("PHASE " + phase, x + 43, y + 108);
        g.setColor(Color.RED);
        g.fillRect(x + 12, y - 13, 111, 7);
        g.setColor(Color.GREEN);
        g.fillRect(x + 12, y - 13, (int) (111 * b.getHealth() / 90.0), 7);
    }

    private void drawShot(Graphics2D g, Shot s) {
        int pulse = 3 + (frame / 2) % 4;
        g.setColor(new Color(80, 220, 255, 100));
        g.fillOval(s.getX() - pulse, s.getY() - pulse / 2, 19 + pulse, 6 + pulse);
        g.setColor(Color.WHITE);
        g.fillRoundRect(s.getX(), s.getY(), 15, 4, 4, 4);
    }

    private void drawEnemyShot(Graphics2D g, EnemyShot s) {
        int size = s.isHeavy() ? 14 : 9;
        g.setColor(s.isHeavy() ? new Color(255, 45, 95, 100) : new Color(255, 145, 40, 100));
        g.fillOval(s.getX() - 5, s.getY() - 5, size + 10, size + 10);
        g.setColor(s.isHeavy() ? Color.PINK : Color.YELLOW);
        g.fillOval(s.getX(), s.getY(), size, size);
    }

    private void drawBossBeam(Graphics2D g) {
        if (bossBeamWarning > 0) {
            int alpha = 45 + (75 - bossBeamWarning) * 2;
            g.setColor(new Color(255, 40, 90, Math.min(210, alpha)));
            g.drawLine(0, bossBeamY, BOARD_WIDTH, bossBeamY);
            g.drawLine(0, bossBeamY - 13, BOARD_WIDTH, bossBeamY - 13);
            g.drawLine(0, bossBeamY + 13, BOARD_WIDTH, bossBeamY + 13);
        } else if (bossBeamFrames > 0) {
            g.setColor(new Color(255, 20, 80, 125));
            g.fillRect(0, bossBeamY - 20, BOARD_WIDTH, 40);
            g.setColor(Color.WHITE);
            g.fillRect(0, bossBeamY - 5, BOARD_WIDTH, 10);
        }
    }

    private void drawPower(Graphics2D g, PowerUp p) {
        int r = 15 + (int) (Math.sin(p.getAge() * .15) * 4);
        Color powerColor = p instanceof HealthUp ? new Color(75, 255, 105)
                : p instanceof SpeedUp ? Color.CYAN : Color.MAGENTA;
        g.setColor(new Color(powerColor.getRed(), powerColor.getGreen(), powerColor.getBlue(), 90));
        g.fillOval(p.getX() + 17 - r, p.getY() + 17 - r, r * 2, r * 2);
        g.setColor(powerColor);
        g.drawOval(p.getX(), p.getY(), 34, 34);
        g.setFont(POWER_FONT);
        g.drawString(p.getLabel(), p.getX() + 10, p.getY() + 24);
    }

    private void drawDashboard(Graphics2D g) {
        g.setColor(DASHBOARD_BACKGROUND);
        g.fillRoundRect(10, 10, BOARD_WIDTH - 35, 48, 14, 14);
        g.setColor(HUD_CYAN);
        g.drawRoundRect(10, 10, BOARD_WIDTH - 35, 48, 14, 14);
        g.setFont(DASHBOARD_FONT);
        g.setColor(Color.WHITE);
        int remain = Math.max(0, FLIGHT_FRAMES - frame) / 60;
        int seconds = remain % 60;
        String objective = frame < FLIGHT_FRAMES
                ? remain / 60 + ":" + (seconds < 10 ? "0" : "") + seconds
                : "BOSS";
        g.drawString("STAGE " + stageNumber + "   SCORE " + score + "   HP " + player.getHealth()
                + "   SPEED " + player.getSpeedLevel() + "/2   SHOTS " + player.getShotLevel() + "/4   " + objective,
                24, 40);
    }

    private void drawStageIntro(Graphics2D g) {
        int fade = Math.min(255, Math.min((150 - stageIntroFrames) * 12, stageIntroFrames * 8));
        g.setColor(new Color(0, 0, 18, Math.max(0, fade - 25)));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        String stageText = "STAGE " + stageNumber;
        String stageName = stageNumber == 1 ? "THE OUTER REACH" : "THE VOID CITADEL";
        String objective = stageNumber == 1
                ? "Break through the alien formations"
                : "Survive the assault and destroy the flagship";

        g.setFont(STAGE_FONT);
        int stageWidth = g.getFontMetrics().stringWidth(stageText);
        g.setColor(new Color(70, 225, 255, fade));
        g.drawString(stageText, (BOARD_WIDTH - stageWidth) / 2, 285);

        g.setFont(STAGE_NAME_FONT);
        int nameWidth = g.getFontMetrics().stringWidth(stageName);
        g.setColor(new Color(255, 90, 190, fade));
        g.drawString(stageName, (BOARD_WIDTH - nameWidth) / 2, 330);

        g.setFont(OBJECTIVE_FONT);
        int objectiveWidth = g.getFontMetrics().stringWidth(objective);
        g.setColor(new Color(235, 240, 255, fade));
        g.drawString(objective, (BOARD_WIDTH - objectiveWidth) / 2, 375);

        int lineWidth = 250 + (150 - stageIntroFrames);
        g.setColor(new Color(70, 225, 255, fade));
        g.drawLine((BOARD_WIDTH - lineWidth) / 2, 398, (BOARD_WIDTH + lineWidth) / 2, 398);
    }

    private void drawBossIntro(Graphics2D g) {
        int fade = Math.min(255, Math.min((150 - bossIntroFrames) * 12, bossIntroFrames * 8));
        int pulse = 160 + (int) (Math.sin(bossIntroFrames * .22) * 70);
        g.setColor(new Color(20, 0, 12, Math.max(0, fade - 15)));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setColor(new Color(255, 25, 75, Math.min(fade, pulse)));
        g.fillRect(0, 190, BOARD_WIDTH, 5);
        g.fillRect(0, 430, BOARD_WIDTH, 5);

        String warning = "BOSS APPROACHING";
        g.setFont(WARNING_FONT);
        int warningWidth = g.getFontMetrics().stringWidth(warning);
        g.setColor(new Color(255, 55, 90, fade));
        g.drawString(warning, (BOARD_WIDTH - warningWidth) / 2, 270);

        String bossName = "THE VOID SOVEREIGN";
        g.setFont(STAGE_NAME_FONT);
        int nameWidth = g.getFontMetrics().stringWidth(bossName);
        g.setColor(new Color(255, 115, 220, fade));
        g.drawString(bossName, (BOARD_WIDTH - nameWidth) / 2, 325);

        String objective = "Destroy the core • Evade the targeting beam";
        g.setFont(OBJECTIVE_FONT);
        int objectiveWidth = g.getFontMetrics().stringWidth(objective);
        g.setColor(new Color(245, 235, 255, fade));
        g.drawString(objective, (BOARD_WIDTH - objectiveWidth) / 2, 370);
    }

    private void drawEnd(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(END_FONT);
        int width = g.getFontMetrics().stringWidth(endMessage);
        g.drawString(endMessage, (BOARD_WIDTH - width) / 2, BOARD_HEIGHT / 2);
    }

    private class Controls extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE)
                fire();
            else if (e.getKeyCode() == KeyEvent.VK_ENTER && !playing)
                game.loadTitle();
            else
                player.keyPressed(e);
        }

        public void keyReleased(KeyEvent e) {
            player.keyReleased(e);
        }
    }

    private static class Spark {
        int x, y, dx, dy, life = 25;
        final Color color;

        Spark(int x, int y, int dx, int dy, Color color) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.color = color;
        }

        void update() {
            x += dx;
            y += dy;
            life--;
        }

        void draw(Graphics2D g) {
            g.setColor(color);
            g.fillRect(x, y, Math.max(1, life / 6), Math.max(1, life / 6));
        }
    }
}
