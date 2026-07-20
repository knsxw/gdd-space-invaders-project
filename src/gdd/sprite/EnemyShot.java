package gdd.sprite;

public class EnemyShot extends Sprite {
    private double preciseX;
    private double preciseY;
    private final double velocityX;
    private final double velocityY;
    private final boolean heavy;

    public EnemyShot(int x, int y, double velocityX, double velocityY, boolean heavy) {
        this.x = x;
        this.y = y;
        preciseX = x;
        preciseY = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.heavy = heavy;
    }

    public void act() {
        preciseX += velocityX;
        preciseY += velocityY;
        x = (int) preciseX;
        y = (int) preciseY;
    }

    public boolean isHeavy() { return heavy; }
}
