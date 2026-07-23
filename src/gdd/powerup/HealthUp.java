package gdd.powerup;

import gdd.sprite.Player;

public class HealthUp extends PowerUp {
    public HealthUp(int x, int y) { super(x, y); }
    public void upgrade(Player player) { player.heal(); die(); }
    public String getLabel() { return "H"; }
}
