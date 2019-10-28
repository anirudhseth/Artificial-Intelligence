import java.awt.Point;
import java.util.ArrayList;

public abstract class Rover {

	public Environment estimatedEnv;
	public double remainingBattery;
	public boolean damonCollected;
	public boolean[] objectCollected;
	public Point currentLocation, startingLocation;
	public ArrayList<Point> currentPlan;
	
	
	// All rover classes must provide these pieces
	public Rover(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		this.startingLocation = new Point(startX, startY);
		this.currentLocation = this.startingLocation;
		this.remainingBattery = startingBattery;
		this.estimatedEnv = estimatedEnv;
		this.currentPlan = new ArrayList<Point>();
		this.objectCollected = new boolean[estimatedEnv.numObjects];
		for (int i = 0; i < estimatedEnv.numObjects; i++)
			this.objectCollected[i] = false;
		this.damonCollected = false;
	}
	
	// Here the rover decides if it needs to update its plan
	public abstract boolean shouldUpdate();
	
	// Here the rover updates its current plan.
	public abstract ArrayList<Point> updatePlan();
	
	
	public Point getNextMove() {
		if (Debug.ON) Debug.LogEnter("Rover::getNextMove");
		
		if(shouldUpdate())
			this.currentPlan = updatePlan();
			
		if (currentPlan.size() == 0) {
			if (Debug.ON) {
				Debug.Log("Returning NULL");
				Debug.LogExit("Rover::getNextMove");
			}
			return null;
		}
		
		if (Debug.ON) Debug.LogExit("Rover::getNextMove");
		return currentPlan.remove(0);
	}
}
