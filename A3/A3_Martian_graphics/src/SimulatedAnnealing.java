import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing extends Rover {
	private double[][] Damon;

	private double fullBattery;

	private Point home;
	private boolean goHome;
	private Random rand;
	private double temp_init, temp_final, learning_rate;
	private int updatePlan, planSize;

	public ArrayList<Point> goingHome() {
		Point curr = new Point(this.currentLocation.x, this.currentLocation.y);
		ArrayList<Point> u = new ArrayList<>();
		System.err.println("inside going home");
		if (curr.x - home.x != 0) {

			int a;
			if (curr.x > home.x) {
				a = curr.x - 1;
			} else {
				a = curr.x + 1;
			}
			Point p = new Point(a, curr.y);
			

			u.add(p);

		} else if (curr.y - home.y != 0) {
			
			int a;
			if (curr.y > home.y) {
				a = curr.y - 1;
			} else {
				a = curr.y + 1;
			}
			Point p = new Point(curr.x, a);
		
			u.add(p);
			System.err.println("arraylist frm home " + u);

		}
		return u;
	}

	public SimulatedAnnealing(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		// TODO Auto-generated constructor stub

		fullBattery = this.remainingBattery;
		goHome = false;
		Damon = copy2dArray(estimatedEnv.pdfDamon);
		rand = new Random();
		temp_init = 1.0;
		temp_final = 0000.1;
		learning_rate = 0.1;
		planSize = 16;
		updatePlan = 0;
		home = this.currentLocation;
	}

	@Override
	public boolean shouldUpdate() {
		updatePlan++;
		// TODO Auto-generated method stub
		System.err.println(updatePlan);
		if (damonCollected == true) {
			
			return true;

		}
		if ((updatePlan - 1) % planSize == 0) {
			return true;
		}

		else {

			return false;
		}
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

	@Override
	public ArrayList<Point> updatePlan() {
		ArrayList<Point> plan = new ArrayList<Point>();

		if (this.remainingBattery < .2 * fullBattery) {
			goHome = true;
			return goingHome();
		}

		else if (goHome == true) {

			plan = goingHome();
			return plan;

		}

		else if(!damonCollected) {

			ArrayList<Point> ret = new ArrayList<Point>();
			ret = getPlan();
			ret=sim_ann(ret, temp_init, temp_final, learning_rate);
			if(ret.equals(null))
			{
				return goingHome();
			}
			else return ret;
		}
		else {
			ArrayList<Point> ret = new ArrayList<Point>();
			ret = getPlan();
			
			ret=sim_ann(ret, temp_init, temp_final, learning_rate);
			if(ret.equals(null))
			{
				return goingHome();
			}
			else return ret;
		}
		
		
		

	}

	public ArrayList<Point> getPlan() {
		ArrayList<Point> ret = new ArrayList<Point>();

		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		ret.add(options.get(rand.nextInt(options.size())));

		for (int i = 0; i < planSize; i++) {
			Point p = ret.get(i);

			options = this.estimatedEnv.getNeighbors(p);
			ret.add(options.get(rand.nextInt(options.size())));
		}

		return ret;
	}

	public ArrayList<Point> sim_ann(ArrayList<Point> solBad, double tStart, double tFinal, double alpha) {

		ArrayList<Point> solCurrent = solBad;
		ArrayList<Point> solBest = solBad;
		double temp = tStart;
		while (temp > tFinal) {
			ArrayList<Point> solNew = getPlan();
			if (Accept(solCurrent, solNew, temp) > Math.random()) {
				solCurrent = solNew;
			}
			if (heuristicValue(solNew) > heuristicValue(solBest)) {
				solBest = solNew;
			}
			temp = temp * alpha;
		}
		return solBest;
	}

	public double heuristicValue(ArrayList<Point> moves) {
		double score = 0;
		for (int i = 0; i < moves.size(); i++) {
			if (!damonCollected) {
				score += estimatedEnv.pdfDamon[moves.get(i).x][moves.get(i).y];
			}
			for (int k = 0; k < estimatedEnv.numObjects; k++) {
				if (!objectCollected[k]) {
					score += estimatedEnv.pdfObjects[k][moves.get(i).x][moves.get(i).y];
				}
			}
		}
		score = score / moves.size();
		return score;
	}

	public double Accept(ArrayList<Point> pOld, ArrayList<Point> pNew, Double Temp) {
		double x;
		if (heuristicValue(pNew) >= heuristicValue(pOld)) {
			return 1;
		} else {
			x = (heuristicValue(pNew) - heuristicValue(pOld)) / Temp;
			return Math.pow(Math.E, x);
		}

	}
}