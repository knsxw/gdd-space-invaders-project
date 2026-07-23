package gdd.powerup;

import gdd.sprite.Player;

public class SpeedUp extends PowerUp {
    public SpeedUp(int x, int y) { super(x, y); }
    public void upgrade(Player player) { player.upgradeSpeed(); die(); }
    public String getLabel() { return "S"; }
}
