package gdd.powerup;

import gdd.sprite.Player;
import gdd.sprite.Sprite;

public abstract class PowerUp extends Sprite {
    protected int age;
    public PowerUp(int x, int y) { this.x = x; this.y = y; }
    public void act() { x -= 3; age++; }
    public abstract void upgrade(Player player);
    public abstract String getLabel();
    public int getAge() { return age; }
}
