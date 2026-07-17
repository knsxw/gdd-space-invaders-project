package gdd.sprite;

public class Alien1 extends Enemy {
    private final int originY;
    public Alien1(int x, int y) { super(x, y); originY = y; }
    public void act() {
        x -= 3;
        age++;
        y = originY + (int) (Math.sin(age * .08) * 28);
    }
}
