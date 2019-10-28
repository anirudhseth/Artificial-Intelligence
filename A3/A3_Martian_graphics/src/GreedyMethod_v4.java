import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Predicate;

public class GreedyMethod_v4 extends Rover {
	private ArrayList<Point> visited;
	private double[][] Damon;
	private int objectCount;
	private Point home;
	private int currentObj;
	private double fullBattery;
	private double[][][] Object;
	private final Predicate<Point> hasBeenVisited = p -> (visited.contains(p));
	private boolean goHome;
	private double threshold;
	private double avgCost;
	private int steps;

	public GreedyMethod_v4(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		visited = new ArrayList<Point>();
		Damon = copy2dArray(estimatedEnv.pdfDamon);
		objectCount = estimatedEnv.numObjects;
		goHome = false;
		Object = copy3dArray(estimatedEnv.pdfObjects);
		currentObj = 0;
		home = this.currentLocation;
		fullBattery = this.remainingBattery;
		threshold = (estimatedEnv.size / 2.0);
		steps = 0;
		avgCost = 0;

		// TODO Auto-generated constructor stub
	}

	public double[][] copy2dArray(double arrp[][]) {
		double arr2[][] = new double[arrp[0].length][arrp[1].length];
		for (int i = 0; i < arrp[0].length; i++) {
			for (int j = 0; j < arrp[1].length; j++) {
				arr2[i][j] = arrp[i][j];
			}
		}
		return arr2;
	}

	public double[][][] copy3dArray(double arrp[][][]) {
		double arr2[][][] = new double[objectCount][Damon.length][Damon.length];

		for (int i = 0; i < objectCount; i++)
			for (int j = 0; j < Damon.length; j++)
				for (int k = 0; k < Damon.length; k++) // These 2 lines could be replaced by
					arr2[i][j][k] = arrp[i][j][k];
		return arr2;
	}

	@Override
	public boolean shouldUpdate() {
		// TODO Auto-generated method stub
		return true;
	}

	public void printArray(double arrp[][]) {
		for (int i = 0; i < arrp[0].length; i++) {
			for (int j = 0; j < arrp[1].length; j++) {
				System.err.print(arrp[i][j] + " ");
			}
			System.err.println();
		}

	}
	public ArrayList<Point> goingHome()
	{	Point curr = new Point(this.currentLocation.x, this.currentLocation.y);
		ArrayList<Point> u = new ArrayList<>();
		visited.clear();
		if (curr.x - home.x != 0) {

			int a;
			if (curr.x > home.x) {
				a = curr.x - 1;
			} else {
				a = curr.x + 1;
			}
			Point p = new Point(a, curr.y);
			System.err.println("the problem  decreasing x: " + p);

			u.add(p);

		} else if (curr.y - home.y != 0) {
			System.err.println("inside first if");
			int a;
			if (curr.y > home.y) {
				a = curr.y - 1;
			} else {
				a = curr.y + 1;
			}
			Point p = new Point(curr.x, a);
			System.err.println("the problem decreasing y: " + p);

			u.add(p);
			System.err.println("arraylist frm home " + u);

		}
		return u;
	}
	@Override

	public ArrayList<Point> updatePlan() {

		if (currentObj < objectCount && objectCollected[currentObj] == true) {

			currentObj++;
			visited.clear();

		}

		if (this.remainingBattery < threshold) {
			goHome = true;
		
		}
		if (currentObj == (objectCount)) {
			goHome = true;
		}

	
		// TODO Auto-generated method stub

		// System.err.println("Going home");
		visited.add(this.currentLocation);
		ArrayList<Point> u = new ArrayList<>();

		if (damonCollected == true & this.currentLocation.equals(home)) {
			ArrayList<Point> empty = new ArrayList<Point>();
			return empty;
		}
		

		if (goHome) {
			u=goingHome();
		}

		else if (!damonCollected) {
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
					double testScore = Damon[x][y];
					System.err.print(testScore + " ");
					if (testScore > moveScore) {
						moveScore = testScore;
						toMove = p;
					}
				}
			}
			if(estimatedEnv.getCost(this.currentLocation, toMove)>999999)
			{
				u=goingHome();
			}
			else {
			u.add(toMove);
			}
		}

		else if (objectCollected[currentObj] == false && currentObj < objectCount)

		{
			ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
			options.removeIf(hasBeenVisited);
			Point toMove = this.currentLocation;
			double moveScore = Double.MIN_VALUE;
			for (int i = 0; i < options.size(); i++) {
				Point p = options.get(i);
				int x = p.x;
				int y = p.y;
				if (estimatedEnv.pdfObjects[currentObj][x][y] == 1.0) {

					toMove = p;
					break;
				} else {
					double testScore = Object[currentObj][x][y];
					if (testScore > moveScore) {
						moveScore = testScore;
						toMove = p;
					}
				}
			}
			if(estimatedEnv.getCost(this.currentLocation, toMove)>999999)
			{
				u=goingHome();
			}
			else {
			u.add(toMove);
			}

		}
		
		else {
			u=goingHome();
			}

		

		avgCost = estimatedEnv.getCost(this.currentLocation, u.get(0));
		return u;

	}

}