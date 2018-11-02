package robots;

import static robocode.util.Utils.normalRelativeAngleDegrees;

import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * FirstRobot - a robot by (your name here)
 */
public class FirstRobot extends Robot {
	
	private double bearingGun = 0;
	private double prevBearingGun = 0;
	private double lowEnergy = 0;
	private double highEnergy = 0;
	private double closeDistance = 0;
	private double farDistance = 0;
	private double lowAggression = 0;
	private double moderateAggression = 0;
	private double highAggression = 0;

	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		colorfy();
		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			ahead(100);
			turnGunRight(90);
			back(100);
			turnGunRight(90);
		}
	}

	private void colorfy() {
		setBodyColor(Color.pink);
		setGunColor(Color.yellow);
		setRadarColor(Color.lightGray);
		setBulletColor(Color.cyan);
		setScanColor(Color.red);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// energy = getEnergy();
		// enemyEnergy = e.getEnergy();
		double absoluteBearing = getHeading() + e.getBearing();
		prevBearingGun = bearingGun;
		bearingGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

		turnRight(bearingGun);
		if (Math.abs(bearingGun) <= 3) {
			if (getGunHeat() == 0) {
				fuzzyControl(e);
			}
		}
		if (bearingGun == 0) {
			scan();
		}
		
	}
	
	private void fuzzyControl(ScannedRobotEvent e) {
		fuzzifyDistance(e.getDistance());
		fuzzifyEnergy(getEnergy());
		resetAgressivity();
		generateAggressivity();
		fire(5);
	}

	private void fuzzifyDistance(double dist) {
		if (dist <= 200) {
			closeDistance = 1;
			farDistance = 1;
		} else if (200 < dist && dist < 300) {
			closeDistance = (300 - dist) / 100;
			farDistance = (dist - 200) / 100;
		} else {
			closeDistance = 0;
			farDistance = 1;
		}
	}

	private void fuzzifyEnergy(double energy) {
		if (energy <= 30) {
			lowEnergy = 1;
			highEnergy = 0;
		} else if (30 < energy && energy < 50) {
			lowEnergy = (50 - energy) / 20;
			highEnergy = (energy - 30) / 20;
		} else {
			lowEnergy = 0;
			highEnergy = 1;
		}
	}

	private void resetAgressivity() {
		lowAggression = 0;
		moderateAggression = 0;
		highAggression = 0;
	}

	private void generateAggressivity() {
		if (highEnergy > 0) {
			if (closeDistance > 0) {
				highAggression += highEnergy * closeDistance;
			}

			if (farDistance > 0) {
				moderateAggression += highEnergy * farDistance;
			}
		}

		if (lowEnergy > 0) {
			if (closeDistance > 0) {
				moderateAggression += lowEnergy * closeDistance;
			}

			if (farDistance > 0) {
				lowAggression += lowEnergy * farDistance;
			}
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	
}
