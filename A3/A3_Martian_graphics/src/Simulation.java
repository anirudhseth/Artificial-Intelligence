import java.awt.Point;
import java.util.ArrayList;

public class Simulation {

	public Point damonLocation;
	public Point[] objectLocations;
	public Environment env;
	public Rover rover;
	public boolean isDone;
	int count = 0;
	
	public Simulation(
			int size,
			int numObjects,
			double minGaussianVariance,
			double maxGaussianVariance,
			double startingBattery,
			double minTravelCost,
			double minTerrainNoise,
			double maxTerrainNoise,
			double estimationNoise) {
		isDone = false;
		
		// Building the true environment
		env = new Environment(size, numObjects, minTravelCost);
		EnvironmentBuilder.addNoise(env.terrain, minTerrainNoise, maxTerrainNoise);
		EnvironmentBuilder.setRandomGaussianPdf(env.pdfDamon, minGaussianVariance, maxGaussianVariance);
		damonLocation = EnvironmentBuilder.sampleFromPdf(env.pdfDamon);
		
		objectLocations = new Point[numObjects];
		for (int i = 0; i < numObjects; i++) {
			EnvironmentBuilder.setRandomGaussianPdf(env.pdfObjects[i], minGaussianVariance, maxGaussianVariance);
			objectLocations[i] = EnvironmentBuilder.sampleFromPdf(env.pdfObjects[i]);
		}
		
		// Build the rover
		rover = new GreedyMethod_v2_withCost2(size/2, size/2, startingBattery, 
				EnvironmentBuilder.copyWithNoisyTerrain(env, estimationNoise));
	}
	
	
	// Executes one step of the simulation
	// 1. Queries the rover for its next move
	// 2. If the rover has no next move, the simulation is done.
	// 3. If the rover provides a next move, execute that move and its side effects.
	//       - Move the rover
	//       - Consume battery
	//       - Update which items the rover has recovered (and their pdfs)
	// 4. Update the rover's estimate of the terrain
	public boolean stepSimulation() {
		if (Debug.ON) Debug.LogEnter("Simulation::stepSimulation");
		
		if (isDone) {
			if (Debug.ON) {
				Debug.Log("Simulation is Done.");
				Debug.LogExit("Simulation::stepSimulation");
			}
			return isDone;
		}
			
		if (Debug.ON) Debug.Log("Running step " + count);
		count++;
		
		// 1. Query the rover for its next move
		Point nextLocation = rover.getNextMove();
		
		// 2. If the rover has no next move, the simulation is done.
		if (nextLocation == null) {
			isDone = true;
			if (Debug.ON) {
				Debug.Log("Rover has no next move.");
				Debug.LogExit("Simulation::stepSimulation");
			}
			return isDone;
		}
		
		// 3. If the rover provides a next move, execute that move and its side effects (if possible).
		if (env.edgeFilter.test(nextLocation)) {
			isDone = true;
			if (Debug.ON) {
				Debug.Log("Rover attemped an illegal move: (" + nextLocation.x + ", " + nextLocation.y +")");
				Debug.LogExit("Simulation::stepSimulation");
			}
			return isDone;
		}
		
		double cost = env.getCost(rover.currentLocation, nextLocation);
		if (Debug.ON) Debug.Log("Cost: " + cost);
		if (cost > rover.remainingBattery) {
			isDone = true;
			if (Debug.ON) {
				Debug.Log("Rover does not have enough battery to move to: (" + nextLocation.x + ", " + nextLocation.y +")");
				Debug.LogExit("Simulation::stepSimulation");
			}
			return isDone;
		}
		rover.remainingBattery -= cost;
		rover.currentLocation = nextLocation;
		
		// If we havent collected damon yet, check if we just did and update our estimates
		if (!rover.damonCollected) {
			
			if (rover.currentLocation.equals(damonLocation)) {
				// This means we just collected Damon
				if (Debug.ON) Debug.Log("Rover got Damon!");
				rover.damonCollected = true;
				rover.estimatedEnv.pdfDamon[damonLocation.x][damonLocation.y] = 0.0;
			} else if (rover.estimatedEnv.pdfDamon[damonLocation.x][damonLocation.y] != 1.0){
				// This means we have not found Damon yet
				if (env.areNeighbors(rover.currentLocation, damonLocation)) {
					// This means we have found Damon for the first time
					// Reveal the true Damon PDF (1.0 at his location)
					if (Debug.ON) Debug.Log("Rover found Damon!");
					EnvironmentBuilder.clear(rover.estimatedEnv.pdfDamon);
					rover.estimatedEnv.pdfDamon[damonLocation.x][damonLocation.y] = 1.0;
				} else {
					// This means we *still* have not found Damon
					// Set our neighbors to zero and renormalize the PDF
					ArrayList<Point> neighbors = env.getNeighbors(rover.currentLocation);
					double total = 1.0;
					for (Point neighbor : neighbors) {
						total -= rover.estimatedEnv.pdfDamon[neighbor.x][neighbor.y];
						rover.estimatedEnv.pdfDamon[neighbor.x][neighbor.y] = 0.0;
					}
					EnvironmentBuilder.renormalize(rover.estimatedEnv.pdfDamon, total);
				}
			}
		}
		
		// For each object, if we haven't collected it yet, check if we just did and update our estimates
		for (int i = 0; i < env.numObjects; i++) {
			if (rover.objectCollected[i])
				continue;
			
			if (rover.currentLocation.equals(objectLocations[i])) {
				// This means we just collected the object
				if (Debug.ON) Debug.Log("Rover got Object " + i +"!");
				rover.objectCollected[i] = true;
				rover.estimatedEnv.pdfObjects[i][objectLocations[i].x][objectLocations[i].y] = 0.0;
			} else if (rover.estimatedEnv.pdfObjects[i][objectLocations[i].x][objectLocations[i].y] != 1.0) {
				// This means we have not found the object yet
				if (env.areNeighbors(rover.currentLocation, objectLocations[i])) {
					// This means we have found the object for the first time
					// Reveal the true object PDF (1.0 at its location)
					if (Debug.ON) Debug.Log("Rover found Object " + i +"!");
					EnvironmentBuilder.clear(rover.estimatedEnv.pdfObjects[i]);
					rover.estimatedEnv.pdfObjects[i][objectLocations[i].x][objectLocations[i].y] = 1.0;
				} else {
					// This means we *still* have not found the object
					// Set our neighbors to zero and renormalize the PDF
					ArrayList<Point> neighbors = env.getNeighbors(rover.currentLocation);
					double total = 1.0;
					for (Point neighbor : neighbors) {
						total -= rover.estimatedEnv.pdfObjects[i][neighbor.x][neighbor.y];
						rover.estimatedEnv.pdfObjects[i][neighbor.x][neighbor.y] = 0.0;
					}
					EnvironmentBuilder.renormalize(rover.estimatedEnv.pdfObjects[i], total);
				}
			}
		}
		
		// 4. Update the rover's estimate of the terrain
		ArrayList<Point> neighbors = env.getNeighbors(rover.currentLocation);
		for (Point neighbor : neighbors) {
			rover.estimatedEnv.terrain[neighbor.x][neighbor.y] = env.terrain[neighbor.x][neighbor.y];
		}
		
		if (Debug.ON) Debug.LogExit("Simulation::stepSimulation");
		return isDone;
	}
	
	
	// Executes the simulation until termination then returns the results
	public int runSimulation() {
		while(!isDone) {
			stepSimulation();
		}
		return getResults();
	}
	
	
	// Gets the results of the simulation if it has terminated.
	public int getResults() {
		// Algorithm Name
		// Environment number
		// Board size
		// Num objects
		// Starting battery
		
		// Collected Damon
		// Returned home
		// Num objects collected
		// Battery Used (Start - End)
		// Steps taken
		// 
		// TODO: Implement getting the results of the simulation
		// TODO: Create custom data structure to hold results of simulation (execution time, objects collected, success/failure, etc.)
		return 0;
	}
}
