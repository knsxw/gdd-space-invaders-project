package gdd.sprite;

public class Shot extends Sprite {
    private final int velocityY;
    public Shot(int x, int y, int velocityY) {
        this.x = x;
        this.y = y;
        this.velocityY = velocityY;
    }
    public void act() { x += 11; y += velocityY; }
}
