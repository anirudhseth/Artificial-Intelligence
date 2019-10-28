import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing_v0 extends Rover {
	private double[][] Damon, homePdf;;
	private double[][][] Object;
	
	private Point lastLocation;
	private Point home;
	private boolean goHome;
	private Random rand;
	private double temp_init, temp_final, learning_rate;
	private double threshold;
	private int step=0;

	public SimulatedAnnealing_v0(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		// TODO Auto-generated constructor stub
		goHome = false;
		
		lastLocation = this.currentLocation;
		Damon = copy2dArray(estimatedEnv.pdfDamon);
		Object = copy3dArray(estimatedEnv.pdfObjects);
		homePdf = new double[Damon.length][Damon.length];
		EnvironmentBuilder.setGaussianPdf(homePdf, this.currentLocation.x, this.currentLocation.y, 3.0);
		rand = new Random();
		temp_init = 1.0;
		temp_final = 0000.1;
		learning_rate = 0.05;
		threshold = (estimatedEnv.size / 2.0);
		home = this.currentLocation;
	}

	@Override
	public boolean shouldUpdate() {
		// TODO Auto-generated method stub
		if(damonCollected) {
			goHome=true;
			return true;
		}
		
		if(step==0)
		{
			return true;
		}
		step++;
		if(step%19!=0)
		{
			
			return false;
		}
		return true;
	}

	public double[][] copy2dArray(double arrp[][]) {
		// Allocate new double[][]
		double arr2[][] = new double[arrp.length][arrp[1].length];

		// Fill new double[][][] with provided values
		for (int i = 0; i < arrp.length; i++)
			for (int j = 0; j < arrp[1].length; j++)
				arr2[i][j] = arrp[i][j];
		return arr2;
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

	public ArrayList<Point> goingHome() {

		ArrayList<Point> u = new ArrayList<Point>();
		if (this.currentLocation.equals(home))
			return u;
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);

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
		lastLocation = this.currentLocation;
		u.add(toMove);
		return u;

	}

	@Override
	public ArrayList<Point> updatePlan() {
		if(goHome)
		{
			return goingHome();
		}
		ArrayList<Point> plan = new ArrayList<Point>();
		System.err.println(getBestPlan(this.currentLocation));
		plan.add(getBestPlan(this.currentLocation));
		for(int i=0;i<20;i++)
		{
			plan.add(getBestPlan(plan.get(i)));
		}
		lastLocation=this.currentLocation;
		
		ArrayList<Point> solCurrent = plan;
		ArrayList<Point> solBest = plan;
		double temp = temp_init;
		if(temp_init<temp_final)
		{
			ArrayList<Point> solNew = getPlan();
			if (Accept(solCurrent, solNew, temp) > Math.random()) {
				solCurrent = solNew;
			}
			if (heuristicValue(solNew) > heuristicValue(solBest)) {
				solBest = solNew;
			}
			//temp = temp * learning_rate;
		
		}
		temp_final=temp_final*learning_rate;
		return solBest;

	}

	public Point getBestPlan(Point z) {

		
		
		ArrayList<Point> options = this.estimatedEnv.getNeighbors(z);
		Point toMove = this.currentLocation;
		double moveScore = Double.MIN_VALUE;
		for (int i = 0; i < options.size(); i++) {
			if(!options.get(i).equals(lastLocation)) {
			Point p = options.get(i);
			int x = p.x;
			int y = p.y;
			
				double testScore = (Damon[x][y])/estimatedEnv.getCost(z, options.get(i));
				System.err.print(testScore + " ");
				if (testScore > moveScore) {
					moveScore = testScore;
					toMove = p;
				}
			
		}}
		
		return toMove;
	} 
	

	public ArrayList<Point> getPlan() {
		ArrayList<Point> ret = new ArrayList<Point>();

		ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
		ret.add(options.get(rand.nextInt(options.size())));

		for (int i = 0; i < 10; i++) {
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
		if (goHome == true) {
			for (int i = 0; i < moves.size(); i++) {
				score += homePdf[moves.get(i).x][moves.get(i).y];
			}
		}
		if (!damonCollected) {
			for (int i = 0; i < moves.size(); i++) {
				score += Damon[moves.get(i).x][moves.get(i).y];
			}
		} else {
			int count = 0;
			for (int i = 0; i < moves.size(); i++) {
				for (int k = 0; k < estimatedEnv.numObjects; k++) {
					if (objectCollected[k] == false) {

						score += Object[k][moves.get(i).x][moves.get(i).y];
						count++;
					}

				}
				score = score / count;

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