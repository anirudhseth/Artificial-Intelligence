import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneticRover extends Rover{

	public PlanGA currentGAPlan;
	int distanceHome;
	boolean doRetreat = false;
	PlanGA retreatPlan;
	double avgMovementCost;
	double estRetreatCost;

	// The hyperparameters below can be tweaked.

	//	planLength: The length (number of steps) of the produced plans.
	//	populationSize: How large should the simulated population be
	//	selectionProp: What proportion of the simulated population should be chosen for breeding

	public int planLength = 15;
	public int populationSize = 100;
	public double selectionProp = 0.25;
	public int generations = 40;
	public double eliteProp = 0.1;
	public double mutateProb = 0.15;
	double mutationProp = 0.20;
	double marginProp = 1.5;
	ArrayList<Point> traversedPath = new ArrayList<Point>();

	public GeneticRover(
			int startX, 
			int startY, 
			double startingBattery, 
			Environment estimatedEnv,
			int planLength,
			int populationSize,
			double selectionProp,
			int generations,
			double eliteProp,
			double mutateProb,
			double mutationProp,
			double marginProp) {
		super(startX, startY, startingBattery, estimatedEnv);
		this.planLength = planLength;
		this.populationSize = populationSize;
		this.selectionProp = selectionProp;
		this.generations = generations;
		this.eliteProp = eliteProp;
		this.mutateProb = mutateProb;
		this.mutationProp = mutationProp;
		this.marginProp = marginProp;
	}
	
	
	public HashMap<String, String> getHyperparameters(){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("planLength", "" + planLength);
		params.put("populationSize", "" + populationSize);
		params.put("selectionProp", "" + selectionProp);
		params.put("generations", "" + generations);
		params.put("eliteProp", "" + eliteProp);
		params.put("mutateProb", "" + mutateProb);
		params.put("mutationProp", "" + mutationProp);
		params.put("marginProp", "" + marginProp);
		return params;
	}
	

	public void avgTravelCost() {
		double avgCost;
		double steps = traversedPath.size()-1;

		if(traversedPath.size() == 1) {
			avgCost = 0;
		} else {
			avgCost = ((this.avgMovementCost)*(steps-1) +
					estimatedEnv.getCost(traversedPath.get(traversedPath.size()-2), currentLocation))/steps;
		}

		avgMovementCost = avgCost;
	}

	public boolean shouldUpdate() {
		try {
			traversedPath.add(currentLocation);
			avgTravelCost();


			distanceHome = Math.abs(startingLocation.x - currentLocation.x) + Math.abs(startingLocation.y - currentLocation.y);

			estRetreatCost = distanceHome*avgMovementCost;

			if (currentPlan.size() == 0) {
				return true;
			}

			ArrayList<Point> neighbors = estimatedEnv.getNeighbors(currentLocation);

			if(doRetreat && (retreatPlan.plannedCoords.get(retreatPlan.plannedCoords.size()-1).equals(startingLocation)) && (estRetreatCost < remainingBattery)) {
				return false;
			}

			doRetreat = shouldRetreat();

			if (doRetreat) {
				return true;
			}

			if(!damonCollected) {
				for(Point coord : neighbors) {
					if(estimatedEnv.pdfDamon[coord.x][coord.y] == 1.0) {
						return true;
					}
				}
			}

			for(int i=0; i<estimatedEnv.pdfObjects.length; i++) {
				for(Point coord : neighbors) {
					if(estimatedEnv.pdfObjects[i][coord.x][coord.y] == 1.0) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			Debug.Log("Line 109 : " + e);
		}
		return false;
	}

	public ArrayList<Point> updatePlan() {

		PlanGA newPlan;
		ArrayList<Point> neighbors = estimatedEnv.getNeighbors(currentLocation);
		ArrayList<Point> getObject = new ArrayList<Point>();
		ArrayList<Point> getDamon = new ArrayList<Point>();

		Debug.Log("Entering updatePlan()");

		for(int i=0; i<estimatedEnv.pdfObjects.length; i++) {
			for(Point coord : neighbors) {
				if(estimatedEnv.pdfObjects[i][coord.x][coord.y] == 1.0) {
					currentPlan = getObject;
					getObject.add(coord);
					break;
				}
			}
		}
		Debug.Log("Plan empty and going home?");

		if(distanceHome == 0 && doRetreat) {
			return new ArrayList<Point>();
		} else if(!damonCollected) {
			for(Point coord : neighbors) {
				if(estimatedEnv.pdfDamon[coord.x][coord.y] == 1.0) {
					currentPlan = getDamon;
					getDamon.add(coord);
					break;
				}
			}
		}

		if(getDamon.size() > 0) {
			Debug.Log("Damon is here");
			return getDamon;
		} else if (getObject.size() > 0 && !doRetreat) {
			Debug.Log("Object is here and we have enough batteries");
			return getObject;
		} else if(doRetreat && (distanceHome > 1)) {
			newPlan = geneticAlgorithm(
					distanceHome,
					populationSize,
					selectionProp,
					generations,
					eliteProp,
					mutateProb,
					mutationProp,
					true);
			try {
				if(retreatPlan != null){
					retreatPlan = (newPlan.heuristicValue > retreatPlan.heuristicValue) ? newPlan : retreatPlan;
					Debug.Log("Found better retreat plan : " + retreatPlan);
				} else {
					Debug.Log("Retreat plan is null, new retreat plan : ", "" + newPlan);
					retreatPlan = newPlan;
				}
			} catch (Exception e) {
				Debug.Log(""+e, "new plan :" + newPlan, "retreat plan : " + retreatPlan);
			}

			Debug.Log("Returning retreatplan: " + retreatPlan);

			return retreatPlan.plannedCoords;
		} else {
			newPlan = geneticAlgorithm(
					planLength,
					populationSize,
					selectionProp,
					generations,
					eliteProp,
					mutateProb,
					mutationProp,
					doRetreat);		

			return newPlan.plannedCoords;
		}
	}

	public boolean shouldRetreat() {

		boolean haveAllObj = true;

		for(boolean bool : objectCollected) {
			if(!bool) {
				haveAllObj = bool;
				break;
			}
		}

		if ((estRetreatCost*marginProp >= remainingBattery) || (damonCollected && haveAllObj)){	
			Debug.Log("Returned [Damon = " + damonCollected + " , Objects = " + haveAllObj + "]");
			return true;
		} else {
			return false;
		}
	}

	public PlanGA geneticAlgorithm(
			int planLength,
			int populationSize,
			double selectionProp,
			int generations,
			double eliteProp,
			double mutateProb,
			double mutationProp,
			boolean doRetreat){

		ArrayList<PlanGA> randomPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> selectedPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> newPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> mutatedPopulation = new ArrayList<PlanGA>();
		ArrayList<PlanGA> newSelection = new ArrayList<PlanGA>();

		randomPopulation = createRandomPopulation(planLength, populationSize, doRetreat);

		selectedPopulation = evaluateAndSelect(randomPopulation, selectionProp, eliteProp, doRetreat);

		try {
			newPopulation = breed(selectedPopulation, eliteProp, populationSize);
		} catch (Exception e) {
			Debug.Log("Error in breed during doRetreat : " + e);
		}

		mutatedPopulation = mutatePopulation(newPopulation, mutateProb, mutationProp);

		for(int i=1; i<generations; i++) {
			selectedPopulation = evaluateAndSelect(mutatedPopulation, selectionProp, eliteProp, doRetreat);
			newPopulation = breed(selectedPopulation, eliteProp, populationSize);
			mutatedPopulation = mutatePopulation(newPopulation, mutateProb, mutationProp);
		}

		newSelection = evaluateAndSelect(mutatedPopulation, selectionProp, eliteProp, doRetreat);

		currentGAPlan = newSelection.get(newSelection.size()-1);

		return currentGAPlan;
	}

	public ArrayList<PlanGA> createRandomPopulation(int planLength, int populationSize, boolean doRetreat){
		ArrayList<PlanGA> populationList = new ArrayList<PlanGA>();

		for(int i=0; i<populationSize; i++) {
			PlanGA newRandomPlan = null;
			try {
				newRandomPlan = new PlanGA(
						planLength, 
						startingLocation, 
						currentLocation, 
						estimatedEnv,
						remainingBattery,
						damonCollected,
						objectCollected,
						1,
						doRetreat);
			} catch (Exception e) {
				Debug.Log("Error in createRandom Population : " + e);
			}
			populationList.add(newRandomPlan);
		}

		return populationList;
	}

	
	// Sorts the population based on fitness and selects a subset
	public ArrayList<PlanGA> evaluateAndSelect(
			ArrayList<PlanGA> population, 
			double selectionProp, 
			double eliteProp,
			boolean doRetreat){
		double tmpValue;
		HashMap<PlanGA,Double> valueMap = new HashMap<PlanGA,Double>();
		ArrayList<PlanGA> sortedPlans = new ArrayList<PlanGA>();
		ArrayList<PlanGA> selectedPlans = new ArrayList<PlanGA>();

		for(PlanGA plan : population) {
			plan.evaluatePlan();
			tmpValue = plan.heuristicValue;
			valueMap.put(plan,tmpValue);
		}

		sortedPlans = sortByValue(valueMap);

		int eliteSize = (int) (sortedPlans.size()*eliteProp);

		for(int i=0; i<eliteSize; i++) {
			sortedPlans.get(i).setElite(true);
			selectedPlans.add(sortedPlans.get(i));
		}
		
		for(int i=eliteSize; i<((int) (sortedPlans.size()*selectionProp)); i++) { 
			selectedPlans.add(sortedPlans.get(i));
		}

		return selectedPlans;
	}

	
	public ArrayList<PlanGA> breed(ArrayList<PlanGA> population, double eliteProp, int populationSize){
		ArrayList<PlanGA> bredPopulation = new ArrayList<PlanGA>();
		int indexB;
		int indexA;
		int eliteSize = (int) (eliteProp*population.size());

		for(int i=0; i < (populationSize - eliteSize) ; i++) {
			indexA = new Random().nextInt(population.size());
			indexB = new Random().nextInt(population.size());

			while(indexA == indexB) {
				indexB = new Random().nextInt(population.size());
			}
			PlanGA child = null;
			try {
				PlanGA planA = population.get(indexA);
				PlanGA planACopy = new PlanGA(
						planA.plannedMovement, 
						planA.startLocation,
						planA.currentLocation,
						planA.estEnv,
						planA.remainingBattery,
						planA.damonCollected,
						planA.objectsCollected,
						planA.generationNumber,
						planA.parents,
						planA.isRetreatPlan);
				PlanGA planB = population.get(indexB);
				child = planACopy.breedPlan(planB);
			} catch (Exception e) {
				Debug.Log("Error in retreatPlan -> breedPlan : " + e);
			}
			bredPopulation.add(child);
		}

		for(int i=0; i<eliteSize; i++) {
			population.get(i).setElite(false);
			bredPopulation.add(population.get(i));
		}

		return bredPopulation;
	}

	public ArrayList<PlanGA> mutatePopulation(ArrayList<PlanGA> population, double mutateProb, double mutationProp){
		ArrayList<PlanGA> mutatedPopulation = new ArrayList<PlanGA>();

		try {
			for(PlanGA plan : population) {
				boolean doMutate = (new Random().nextDouble() < mutateProb);

				if(doMutate) {
					plan.mutatePlan(mutationProp);
					mutatedPopulation.add(plan);
				} else {
					mutatedPopulation.add(plan);
				}
			}

		}
		catch (Exception e) {
			Debug.Log("Error in mutatePopulation : " + e);
		}

		return mutatedPopulation;
	}


	public static ArrayList<PlanGA> sortByValue(HashMap<PlanGA, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<PlanGA, Double>> list = new LinkedList<Map.Entry<PlanGA, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<PlanGA, Double>>() {
			public int compare(Map.Entry<PlanGA, Double> o1, Map.Entry<PlanGA, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to arrayList
		ArrayList<PlanGA> temp2 = new ArrayList<PlanGA>();
		//        HashMap<PlanGA, Double> temp = new LinkedHashMap<PlanGA, Double>();
		for (Map.Entry<PlanGA, Double> aa : list) {
			//            temp.put(aa.getKey(), aa.getValue());
			temp2.add(aa.getKey());
		}

		return temp2;
	}
}