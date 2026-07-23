package gdd.powerup;

import gdd.sprite.Player;

public class MultiShot extends PowerUp {
    public MultiShot(int x, int y) { super(x, y); }
    public void upgrade(Player player) { player.upgradeShots(); die(); }
    public String getLabel() { return "M"; }
}
