package gdd.sprite;

public class Alien2 extends Enemy {
    private int vertical = 3;
    public Alien2(int x, int y) { super(x, y); health = 2; points = 175; }
    public void act() {
        x -= 2;
        y += vertical;
        if (y < 85 || y > 610) vertical = -vertical;
        age++;
    }
}
