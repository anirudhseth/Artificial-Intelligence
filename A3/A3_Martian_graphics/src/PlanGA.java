import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


// This is a class for the Plan determining how the rover should move. It uses a Genetic Algorithm-approach.
// The 

public class PlanGA {
	
	public ArrayList<Point> plannedMovement = new ArrayList<Point>();
	public ArrayList<Point> plannedCoords = new ArrayList<Point>();
	public Point currentLocation;
	public Point startLocation;
	public double heuristicValue;
	public Environment estEnv;
	public double remainingBattery;
	public double estPowerUse;
	public boolean damonCollected;
	public boolean[] objectsCollected;
	public int damonIndicator;
	public boolean isRetreatPlan;
	public PlanGA[] parents;
	public int generationNumber;
	boolean isElite = false;
	boolean planMutated = false;
	int planLength;
	
	
	// These are hyperparameters which can be tuned after preference
	double w1=100 , w2beforeDamon = 0 , w2afterDamon=100 , w3=-1 , w4=-1;

	public PlanGA() {
	}
	
	public PlanGA(
			int planLength,
			Point startLocation, 
			Point currentLocation, 
			Environment estEnv,
			double remainingBattery,
			boolean damonCollected,
			boolean[] objectsCollected,
			int generationNumber,
			boolean isRetreatPlan) {
		
		this.startLocation = startLocation;
		this.currentLocation = currentLocation;
		this.estEnv = estEnv;	
		this.remainingBattery = remainingBattery; 
		this.damonCollected = damonCollected;
		this.objectsCollected = objectsCollected;
		this.generationNumber = generationNumber;
		this.parents = new PlanGA[2];
		this.isRetreatPlan = isRetreatPlan;
		this.planLength = planLength;
		
		randomPlan();
	}
	
	public PlanGA(
			ArrayList<Point> givenMoves,
			Point startLocation, 
			Point currentLocation, 
			Environment estEnv,
			double remainingBattery,
			boolean damonCollected,
			boolean[] objectsCollected,
			int generationNumber,
			PlanGA[] parents,
			boolean isRetreatPlan) {
		
		this.currentLocation = currentLocation;
		this.startLocation = startLocation;
		this.estEnv = estEnv;
		this.remainingBattery = remainingBattery; 
		this.damonCollected = damonCollected;
		this.objectsCollected = objectsCollected;
		this.plannedMovement = givenMoves;
		this.generationNumber = generationNumber;
		this.parents = parents;
		this.isRetreatPlan = isRetreatPlan;
		this.planLength = givenMoves.size();
	
		computeNewCoords();
		evaluatePlan();
	}
	
	public int size() {
		return plannedMovement.size();
	}
	
	public Point getNextMove() {
		return plannedMovement.get(0);
	}

	public Point computeNewCoord(Point currentCoord, Point nextMove) {
		// computeNewCoord calculates the rovers new coordinates after performing nextMove from position currentCoord
		
		return new Point(
				currentCoord.x + nextMove.x,
				currentCoord.y + nextMove.y);
	}
	
	public void randomPlan() {
		// randomPlan creates a random plan of length maxSteps consisting of valid steps
		
		ArrayList<Point> newMovements = new ArrayList<Point>();
		ArrayList<Point> newCoords = new ArrayList<Point>();
		Point movement , prevMove , coord , prevCoord;
		
		newMovements.add(getValidMove(currentLocation, new Point(0,0)));
		newCoords.add(computeNewCoord(currentLocation, newMovements.get(0)));
		
		for(int i=1; i<planLength; i++) {
			prevCoord = newCoords.get(i-1);
			prevMove = newMovements.get(i-1);
			movement = getValidMove(prevCoord, prevMove);
			newMovements.add(movement);
			coord = computeNewCoord(newCoords.get(i-1), movement);
			newCoords.add(coord);
		}
		plannedMovement = newMovements;
		plannedCoords = newCoords;
		evaluatePlan();
	}
	
	public Point getValidMove(Point location, Point lastMove) {
		// Returns a random valid move based on the current location
		// A valid move is one that does not result in the rover running off the map
		// or is not the reverse of the previous move

		ArrayList<Point> neighbors;
		ArrayList<Point> validMoves = new ArrayList<Point>();
		
		neighbors = estEnv.getNeighbors(location);
		
		for(Point neighbor : neighbors) {
			validMoves.add(new Point(neighbor.x - location.x, neighbor.y - location.y));
		}
		
		for(int i=0; i<validMoves.size(); i++) {
			if(validMoves.get(i).equals(reverseMove(lastMove))) {
				validMoves.remove(i);
			}
		}
		
		int chosenIndex = 1;
		try {
		chosenIndex = new Random().nextInt(validMoves.size());
		} catch (Exception e) {
			System.err.println("Error in chosenIndex : " + e);
			System.err.println("validMoves.size() = " + validMoves.size());
		}
		
		return validMoves.get(chosenIndex);
	}
	
	public boolean validCoords(Point start, ArrayList<Point> moves) {
		
		int[] mapLims = {0,estEnv.size-1};
		
		ArrayList<Point> coords = new ArrayList<Point>();
		
		coords.add(new Point(start.x + moves.get(0).x,start.y + moves.get(0).y));
		
		for(int i=1; i < moves.size(); i++) {
				coords.add(new Point(
						coords.get(i-1).x + moves.get(i).x,
						coords.get(i-1).y + moves.get(i).y));
		}
		
		for(Point coord : coords) {
			if(coord.x > mapLims[1] || coord.y > mapLims[1] || coord.x < mapLims[0] || coord.y < mapLims[0]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isValidPlan() {
		
		computeNewCoords();
		
		int[] mapLims = {0,estEnv.size-1};
		
		for(Point coord : plannedCoords) {
			if(coord.x > mapLims[1] || coord.y > mapLims[1] || coord.x < mapLims[0] || coord.y < mapLims[0]) {
				return false;
			}
		}
		
		return true;
	}
	
	public void evaluatePlan() {
//		This function calculates our "utility function" of the current plan.
//		The value is based on the acquisition of Matt Damon, the number of objects found,
//		the amount of used power and the distance to home from the current position.
//		w1, ..., w4 are weights for the different objectives. 
//		We want to maximize the heuristic.

		Point thisCoord = null, prevCoord = null;
		double[][] pdfDamonCopy = array2DCopy(estEnv.pdfDamon);
		double[][][] pdfObjectsCopy = new double[estEnv.pdfObjects.length][estEnv.pdfObjects[0].length][estEnv.pdfObjects[0][0].length];
		double gainedDamonProb = 0;
		double totObjProb = 0;
		int noObj = pdfObjectsCopy.length;
		double[] gainedObjProb = new double[noObj];
		
		int remainingObjects = 0;
		
		try {
		for(int i=0; i<objectsCollected.length; i++) {
			pdfObjectsCopy[i] = array2DCopy(estEnv.pdfObjects[i]);
		}
		
		for(int i=0; i<objectsCollected.length; i++) {
			if (!objectsCollected[i]) {remainingObjects++;}
		}
		} catch (Exception e) {
			Debug.Log("Error in lines 235-242 : " + e);
		}
		
		this.estPowerUse = estEnv.getCost(currentLocation, plannedCoords.get(0));
		
		for(int i=1; i < plannedCoords.size(); i++) {	
			thisCoord = plannedCoords.get(i);
			prevCoord = plannedCoords.get(i-1);
			this.estPowerUse += estEnv.getCost(prevCoord, thisCoord);
		}
		
		if(isRetreatPlan) {
			if(plannedCoords.get(plannedCoords.size()-1).equals(this.startLocation)) {
				heuristicValue = 1e7 - estPowerUse;
			} else {
				heuristicValue = -100*startLocation.distance(plannedCoords.get(plannedCoords.size()-1)) - estPowerUse;
			}
		} else if(!damonCollected) {			
			for(int i=1; i<plannedCoords.size(); i++) {
				
				thisCoord = plannedCoords.get(i);
				
				gainedDamonProb += pdfDamonCopy[thisCoord.x][thisCoord.y];
				pdfDamonCopy[thisCoord.x][thisCoord.y] = 0; 
				
				for(int j=0; j<noObj; j++) {
					gainedObjProb[j] += pdfObjectsCopy[j][thisCoord.x][thisCoord.y];
					pdfObjectsCopy[j][thisCoord.x][thisCoord.y] = 0;
				}
				
			}

			for(int k=0; k<noObj; k++) {
				totObjProb += gainedObjProb[k];
			}
			
			if(estPowerUse > remainingBattery) {
				heuristicValue = Double.NEGATIVE_INFINITY;
			} else {
				heuristicValue = w1*gainedDamonProb/estPowerUse;
			}
		} else if(remainingObjects > 0) {
//			ONCE WE'VE FOUND DAMON USE THIS HEURISTIC
			
			int nextObjectIndex = -1;
			
			for(int i=0; i<objectsCollected.length; i++) {
				if(!objectsCollected[i]) {
					nextObjectIndex = i;
					break;
				}
			}
			
			for(int i=1; i<plannedCoords.size(); i++) {
				thisCoord = plannedCoords.get(i);
				prevCoord = plannedCoords.get(i-1);
				
				gainedObjProb[nextObjectIndex] += pdfObjectsCopy[nextObjectIndex][thisCoord.x][thisCoord.y];
				pdfObjectsCopy[nextObjectIndex][thisCoord.x][thisCoord.y] = 0;

				for(int j=0; j<noObj; j++) {
					gainedObjProb[j] += pdfObjectsCopy[j][thisCoord.x][thisCoord.y];
					pdfObjectsCopy[j][thisCoord.x][thisCoord.y] = 0;
				}
				
//				estPowerUse += estEnv.terrain[prevCoord.x][prevCoord.y] - 
//						estEnv.terrain[thisCoord.x][thisCoord.y];
			}
			
			for(int i=0; i<noObj; i++) {
				totObjProb += gainedObjProb[i];
			}
			
			if((estPowerUse > remainingBattery) && (plannedCoords.get(plannedCoords.size() - 1)).equals(startLocation)) {
				heuristicValue = Double.NEGATIVE_INFINITY;
			} else {
				heuristicValue = w2afterDamon*totObjProb;
			}
		} else {
			double distanceHome =  plannedCoords.get(plannedCoords.size()-1).distance(startLocation);
			
			heuristicValue = 1/distanceHome;
		}
	}

	public Point reverseMove(Point move) {
		return new Point(move.x*(-1),move.y*(-1));
	}
	
	public PlanGA breedPlan(PlanGA planA) {
//		This function creates a new plan from this.plan and planA
//		This can be done in different ways but here we randomly select
//		a part of this.plan to be replaced with the corresponding part of planA.
		boolean validPlan = false;
		ArrayList<Point> combinedMoves = new ArrayList<Point>();
		int cutSize = 0;
		PlanGA newPlan = new PlanGA();
		while(!validPlan) {
			combinedMoves.clear();
			
			cutSize = new Random().nextInt(planLength/2);
			
			for(int i=0; i < cutSize; i++) {
				try {
				combinedMoves.add(this.plannedMovement.get(i));
				} catch (Exception e) {
					Debug.Log("Error in line 354 : " + e);
				}
			}
			for(int i=cutSize; i < this.size(); i++) {
				try {
					combinedMoves.add(planA.plannedMovement.get(i));
				} catch (Exception e) {
					Debug.Log("Error in line 361 : " + e);
				}
			}
			try {
			if(validCoords(this.currentLocation,combinedMoves)) {
				newPlan = new PlanGA(combinedMoves, 
						this.startLocation, 
						this.currentLocation,
						this.estEnv,
						this.remainingBattery,
						this.damonCollected,
						this.objectsCollected,
						Math.max(planA.generationNumber, this.generationNumber) + 1,
						new PlanGA[] {this, planA},
						this.isRetreatPlan);
				validPlan = newPlan.isValidPlan();

			} else {
				validPlan = false;
			}
			} catch (Exception e) {
				Debug.Log("Error in line 367 : " + e);
				Debug.Log("Moves: " + combinedMoves);
				Debug.Log("Valid coords? : " + validCoords(this.currentLocation,combinedMoves));
				Debug.Log("Is valid plan? : " + validPlan);
			}
		}
		newPlan.evaluatePlan();
		
		return newPlan;
	}
	
	public void mutatePlan(double mutationProp) {
		
		this.planMutated = true;
		int mutationSize = (int) (this.plannedMovement.size()*mutationProp);
			
		ArrayList<Point> copy = arrayListDeepCopy(this.plannedMovement);

		int multConstant = 0;
		int counter = 0;
		int newRand = 0;
		
		while(counter < mutationSize) {
			newRand = new Random().nextInt(copy.size());
			
			multConstant = ((newRand < (copy.size()-1)) ? 1 : -1);

			Collections.swap(copy, newRand, newRand + multConstant);			
			
			if(validCoords(this.currentLocation,copy)) { 
				counter++;
			} else {
				Collections.swap(copy, newRand, newRand + multConstant);
			}
		}
		this.plannedMovement = copy;
		this.plannedCoords = computeNewCoords(copy);
	}
	

	
	public void computeNewCoords() {
		// computeNewCoord calculates the new coordinates after performing nextMove from position currentCoord
		plannedCoords.clear();
		
		plannedCoords.add(new Point(this.currentLocation.x + plannedMovement.get(0).x,this.currentLocation.y + plannedMovement.get(0).y));
		
		for(int i=1; i < plannedMovement.size(); i++) {
				plannedCoords.add(new Point(
						plannedCoords.get(i-1).x + plannedMovement.get(i).x,
						plannedCoords.get(i-1).y + plannedMovement.get(i).y));
		}
	}
	
	public ArrayList<Point> computeNewCoords(ArrayList<Point> moves) {
		// computeNewCoord calculates and returns the new coordinates after performing moves from position currentLocation
		
		ArrayList<Point> coords = new ArrayList<Point>();
		
		coords.add(new Point(this.currentLocation.x + moves.get(0).x,this.currentLocation.y + moves.get(0).y));
		
		for(int i=1; i < moves.size(); i++) {
				coords.add(new Point(
						coords.get(i-1).x + moves.get(i).x,
						coords.get(i-1).y + moves.get(i).y));
		}
		
		return coords;
	}
	
	public String toString() {
		int gen = this.generationNumber;
		
		String s1 = "Estimated power usage : " + this.estPowerUse + " , Value = " + this.heuristicValue + ", Generation: " + gen;
		String s2 = "Coords: " + this.plannedCoords;
		
		return s1 + s2;
	}
	
	public void setElite(boolean bool) {
		this.isElite = bool;
	}
	
	public boolean isElite() {
		return isElite;
	}
	
	private ArrayList<Point> arrayListDeepCopy(ArrayList<Point> arrayList){
		ArrayList<Point> copyArrayList = new ArrayList<Point>();
		
		for(Point point : arrayList) {
			copyArrayList.add(point);
		}
		return copyArrayList;
	}
	
	private double[][] array2DCopy(double[][] array){
		double[][] copy = new double[array.length][array[0].length];
		
		for(int i=0; i<array.length; i++) {
			for(int j=0; j<array[0].length; j++) {
				copy[i][j] = array[i][j];
			}
		}	
		return copy;
	}
}