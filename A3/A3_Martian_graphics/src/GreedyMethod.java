import java.awt.Point;
import java.util.ArrayList;
import java.util.function.Predicate;

public class GreedyMethod extends Rover {
	private ArrayList<Point> visited;
	private double[][] Damon;
	private final Predicate<Point> hasBeenVisited = p -> (visited.contains(p));

	public GreedyMethod(int startX, int startY, double startingBattery, Environment estimatedEnv) {
		super(startX, startY, startingBattery, estimatedEnv);
		visited = new ArrayList<Point>();
		Damon = copyArray(estimatedEnv.pdfDamon);
		// TODO Auto-generated constructor stub
	}

	public double[][] copyArray(double arrp[][]) {
		double arr2[][] = new double[arrp[0].length][arrp[1].length];
		for (int i = 0; i < arrp[0].length; i++) {
			for (int j = 0; j < arrp[1].length; j++) {
				arr2[i][j] = arrp[i][j];
			}
		}
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

	@Override
	public ArrayList<Point> updatePlan() {
		// TODO Auto-generated method stub

		visited.add(this.currentLocation);
		ArrayList<Point> u = new ArrayList<>();
		System.err.println("prob");
		if (!damonCollected) {
			System.out.println("prob");
			ArrayList<Point> options = this.estimatedEnv.getNeighbors(this.currentLocation);
			//options.removeIf(hasBeenVisited);
			Point toMove=options.get(0);
			double moveScore=Double.MIN_VALUE;
			for(int i=0;i<options.size();i++)
			{
			 Point p=options.get(i);
			 int x = p.x;
			 int y = p.y;
			 double testScore=Damon[x][y];
			 if(testScore>moveScore)
			 {
				 moveScore=testScore;
				 toMove=p;
			 }
			}
			u.add(toMove);
//			int x = currentLocation.x;
//			int y = currentLocation.y;
//			int retx, rety;
//			double up = Damon[x + 1][y];
//			double down = Damon[x - 1][y];
//			double left = Damon[x + 1][y - 1];
//			double right = Damon[x + 1][y + 1];
//			if (up > down && up > left && up > right) {
//				retx = x + 1;
//				rety = y;
//			} else if (left > down && left > up && left > right) {
//				retx = x;
//				rety = y - 1;
//			} else if (down > up && down > left && down > right) {
//				retx = x - 1;
//				rety = y;
//			}
//
//			else {
//				retx = x;
//				rety = y + 1;
//			}
//			System.err.println(up + " " + down + " " + right + " " + left);
//
//			Point plan = new Point(retx, rety);
//			visited = new ArrayList<Point>();
//
//			u.add(plan);
		}

		return u;
	}

}
