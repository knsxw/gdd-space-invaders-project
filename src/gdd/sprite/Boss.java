package gdd.sprite;

public class Boss extends Enemy {
    private int vertical = 2;
    public Boss(int x, int y) { super(x, y); health = 90; points = 5000; }
    public void act() {
        if (x > 520) x -= 1;
        if (health <= 30) vertical = vertical < 0 ? -3 : 3;
        else if (health <= 60) vertical = vertical < 0 ? -3 : 3;
        y += vertical;
        if (y < 105 || y > 490) vertical = -vertical;
        age++;
    }

    public int getPhase() {
        if (health <= 30) return 3;
        if (health <= 60) return 2;
        return 1;
    }
}
