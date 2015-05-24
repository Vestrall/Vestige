package com.lescomber.vestige.projectiles;

public class AIProjectileBehavior {
	public boolean isExtended;        // true = extend destination off the screen. false = destination at or near target
	public boolean targetsEnemies;    // true = target selection looks at opposite faction
	public int range;                // Limits destination by limiting target selection range

	public AIProjectileBehavior() {
		isExtended = true;
		targetsEnemies = true;
		range = Integer.MAX_VALUE;
	}

	public AIProjectileBehavior(AIProjectileBehavior copyMe) {
		isExtended = copyMe.isExtended;
		targetsEnemies = copyMe.targetsEnemies;
		range = copyMe.range;
	}
}