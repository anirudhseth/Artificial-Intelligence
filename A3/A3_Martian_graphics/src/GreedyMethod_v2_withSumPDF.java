import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Predicate;

public class GreedyMethod_v2_withSumPDF extends Rover {
	private ArrayList<Point> visited;
	private double[][] Damon, homePdf;
	private double[][][] Object;
	private final Predicate<Point> hasBeenVisited = p -> (visited.contains(p));
	private double threshold;
	private Point lastLocation;

	public GreedyMethod_v2_withSumPDF(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		visited = new ArrayList<Point>();
		Damon = copy2dArray(estimatedEnv.pdfDamon);
		homePdf = new double[Damon.length][Damon.length];
		EnvironmentBuilder.setGaussianPdf(homePdf, this.currentLocation.x, this.currentLocation.y, 3.0);
		Object = copy3dArray(estimatedEnv.pdfObjects);
		threshold = (estimatedEnv.size / 2.0);
		lastLocation = this.currentLocation;
	}

	// Creates a deep copy of a double[][]
	public double[][] copy2dArray(double arrp[][]) {
		// Allocate new double[][]
		double arr2[][] = new double[arrp.length][arrp[1].length];

		// Fill new double[][][] with provided values
		for (int i = 0; i < arrp.length; i++)
			for (int j = 0; j < arrp[1].length; j++)
				arr2[i][j] = arrp[i][j];
		return arr2;
	}

	public boolean shouldUpdate() {
		return true;

	}

	// Creates a deep copy of a double[][][]
	public double[][][] copy3dArray(double arrp[][][]) {
		// Allocate new double[][][]
		double arr2[][][] = new double[arrp.length][arrp[0].length][arrp[0][0].length];

		// Fill new double[][][] with the provided values
		for (int i = 0; i < arrp.length; i++)
			for (int j = 0; j < arrp[0].length; j++)
				for (int k = 0; k < arrp[0][0].length; k++)
					arr2[i][j][k] = arrp[i][j][k];

		return arr2;
	}

	// This algorithm will always update.
	public ArrayList<Point> collectObject() {
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		ArrayList<Point> u = this.estimatedEnv.getNeighbors(this.currentLocation);
		options.removeIf(hasBeenVisited);
		Point toMove = this.currentLocation;
		double moveScore = Double.MIN_VALUE;
		for (int i = 0; i < options.size(); i++) {
			Point p = options.get(i);
			int x = p.x;
			int y = p.y;
			double testScore=0;
			for (int k = 0; k < estimatedEnv.numObjects; k++) {
				if (!objectCollected[k]) {
				
						 testScore += Object[k][x][y] / estimatedEnv.getCost(this.currentLocation, options.get(i));
						
						
					}
				}
			if (testScore > moveScore) {
				moveScore = testScore;
				toMove = p;
			}
		}

		
			u.add(toMove);
		return u;
	}

	// Prints the values of an array
	public void printArray(double arrp[][]) {
		for (int j = 0; j < arrp.length; j++) {
			// Construct one line of output from a row of the matrix
			String line = "";
			for (int i = 0; i < arrp[0].length; i++)
				line += arrp[i][j] + " ";

			// Log that line
			Debug.Log(line);
		}
	}
	// Returns the planned path home
	public ArrayList<Point> goingHome() {
		visited.clear();
		ArrayList<Point> u = new ArrayList<Point>();
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		options.removeIf(hasBeenVisited);
		Point toMove = this.currentLocation;
		double moveScore = Double.MIN_VALUE;
		for (int i = 0; i < options.size(); i++) {
			if (!options.get(i).equals(lastLocation)) {
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;

				double testScore = homePdf[x][y] / estimatedEnv.getCost(this.currentLocation, options.get(i));
				System.err.print(testScore + " ");
				if (testScore > moveScore) {
					moveScore = testScore;
					toMove = p;
				}
			}

		}
		u.add(toMove);
		return u;

	}

	
	public int collectedObjectCount()
	{
		int x=0;
		for (int k = 0; k < estimatedEnv.numObjects; k++) {
			if(objectCollected[k]==true)
			{
				x++;
			}
		}
		return x;
	}

	public ArrayList<Point> updatePlan() {


		if (this.remainingBattery < threshold) {
			return goingHome();

		}
		if (collectedObjectCount() == (estimatedEnv.numObjects)-1) {
			return goingHome();
		}

		visited.add(this.currentLocation);
		
		ArrayList<Point> u = new ArrayList<>();

		if (damonCollected == true & this.currentLocation.equals(startingLocation)) {
			ArrayList<Point> empty = new ArrayList<Point>();
			return empty;
		}

		if (!damonCollected) {
			ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
			options.removeIf(hasBeenVisited);
			Point toMove = this.currentLocation;
			double moveScore = Double.MIN_VALUE;
			for (int i = 0; i < options.size(); i++) {
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;
				if (estimatedEnv.pdfDamon[x][y] == 1.0) {
					toMove = p;
					break;
				} else {
					double testScore = Damon[x][y] / estimatedEnv.getCost(this.currentLocation, options.get(i));
					System.err.print(testScore + " ");
					if (testScore > moveScore) {
						moveScore = testScore;
						toMove = p;
					}
				}
			}
			if (estimatedEnv.getCost(this.currentLocation, toMove) > 999999) {
				u = goingHome();
			} else {
				u.add(toMove);
			}
		}
		else if(collectedObjectCount()<estimatedEnv.numObjects) {
			return collectObject();

		} 
		else {
			u = goingHome();
		}
		lastLocation = this.currentLocation;
		return u;

	}

}