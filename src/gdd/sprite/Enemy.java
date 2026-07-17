package gdd.sprite;

public class Enemy extends Sprite {
    protected int health = 1;
    protected int age;
    protected int points = 100;

    public Enemy(int x, int y) { this.x = x; this.y = y; }
    public void act() { x -= 3; age++; }
    public void hit() { if (--health <= 0) die(); }
    public int getHealth() { return health; }
    public int getPoints() { return points; }
    public int getAge() { return age; }
}
