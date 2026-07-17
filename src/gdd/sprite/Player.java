package gdd.sprite;

import static gdd.Global.*;
import java.awt.event.KeyEvent;

public class Player extends Sprite {
    private int dy;
    private int speedLevel;
    private int shotLevel = 1;
    private int health = 5;

    public Player() {
        x = 90;
        y = BOARD_HEIGHT / 2;
    }

    public Player(Player previous) {
        this();
        if (previous != null) {
            speedLevel = previous.speedLevel;
            shotLevel = previous.shotLevel;
            health = previous.health;
        }
    }

    public void act() {
        x = Math.max(15, Math.min(BOARD_WIDTH - 70, x + dx));
        y = Math.max(70, Math.min(BOARD_HEIGHT - 70, y + dy));
    }

    public int getSpeedLevel() { return speedLevel; }
    public int getShotLevel() { return shotLevel; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return 5; }
    public int getSpeed() { return 4 + speedLevel * 2; }
    public void upgradeSpeed() { speedLevel = Math.min(2, speedLevel + 1); }
    public void upgradeShots() { shotLevel = Math.min(4, shotLevel + 1); }
    public void damage() { health--; }
    public void heal() { health = Math.min(getMaxHealth(), health + 1); }

    public void keyPressed(KeyEvent e) {
        int speed = getSpeed();
        if (e.getKeyCode() == KeyEvent.VK_LEFT) dx = -speed;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = speed;
        if (e.getKeyCode() == KeyEvent.VK_UP) dy = -speed;
        if (e.getKeyCode() == KeyEvent.VK_DOWN) dy = speed;
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT && dx < 0) dx = 0;
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && dx > 0) dx = 0;
        if (e.getKeyCode() == KeyEvent.VK_UP && dy < 0) dy = 0;
        if (e.getKeyCode() == KeyEvent.VK_DOWN && dy > 0) dy = 0;
    }
}
