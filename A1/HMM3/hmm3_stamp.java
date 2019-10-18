package HMM3;

import java.util.Scanner;

public class hmm3_stamp {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int rowA, rowB, colA, colB, rowI, colI, r, c, rowS;

		/* Get A = State Transition Probability */
		rowA = in.nextInt();
		colA = in.nextInt();
		double arrA[][] = new double[rowA][colA];
		for (r = 0; r < rowA; r++)
			for (c = 0; c < colA; c++)
				arrA[r][c] = in.nextDouble();

		/* Get B = Observation Probability */
		rowB = in.nextInt();
		colB = in.nextInt();
		double arrB[][] = new double[rowB][colB];
		for (r = 0; r < rowB; r++)
			for (c = 0; c < colB; c++)
				arrB[r][c] = in.nextDouble();


		/* Get PI = Initial State Probability */
		rowI = in.nextInt();
		colI = in.nextInt();
		double arrI[][] = new double[rowI][colI];
		for (r = 0; r < rowI; r++)
			for (c = 0; c < colI; c++)
				arrI[r][c] = in.nextDouble();


		/* Observation */
		rowS = in.nextInt();
		int arrS[] = new int[rowS];
		for (r = 0; r < rowS; r++) {
			arrS[r] = in.nextInt();
		}
		int maxiter=250;
		int iter=0;
		double oldLogProb=Double.NEGATIVE_INFINITY;
		double logprob;
		boolean flag=true;
		while(iter<maxiter && flag)
		{
			
			/* Alpha estimation */
			double scale[]=new double[rowS];
			double alpha[][]=new double[rowS][rowA];
			scale[0]=0;
			for(int i=0;i<rowA;i++)
			{
				alpha[0][i]=arrI[0][i]*arrB[i][arrS[0]];
				scale[0]=scale[0]+alpha[0][i];
			}
			scale[0]=1.0/scale[0];
			for(int i=0;i<rowA;i++)
			{
				alpha[0][i]=alpha[0][i]*scale[0];
			}
			for(int t=1;t<rowS;t++)
			{
				scale[t]=0;
				for(int i=0;i<rowA;i++)
				{
					for(int j=0;j<rowA;j++)
					{
						alpha[t][i]=alpha[t][i]+alpha[t-1][j]*arrA[j][i];
					}
					alpha[t][i]=alpha[t][i]*arrB[i][arrS[t]];
					scale[t]=alpha[t][i]+scale[t];
				}
				scale[t]=1.0/scale[t];
				for(int i=0;i<rowA;i++)
					{
					alpha[t][i]=alpha[t][i]*scale[t];
					}
			}
			
			/* Beta estimation */
			double beta[][]=new double[rowS][rowA];
			for(int i=0;i<rowA;i++)
			{
				beta[rowS-1][i]=scale[rowS-1];
			}
			for(int t=rowS-2;t>=0;t--)
			{
				for(int i=0;i<rowA;i++)
				{
					beta[t][i]=0;
					for(int j=0;j<rowA;j++)
					{
						beta[t][i]=beta[t][i]+beta[t+1][j]*arrA[i][j]*arrB[j][arrS[t+1]];
					}
					beta[t][i]=beta[t][i]*scale[t];
				}
				
			}
			/* gamma and digamma estimation */
			double gamma[][]=new double[rowS][rowA];
			double digamma[][][]=new double[rowS][rowA][rowA];
			for(int t=0;t<rowS-1;t++)
			{
				for(int i=0;i<rowA;i++)
				{
					gamma[t][i]=0;
					for(int j=0;j<rowA;j++)
					{
						digamma[t][i][j]=alpha[t][i]*arrA[i][j]*arrB[j][arrS[t+1]]*beta[t+1][j];
						gamma[t][i]=gamma[t][i]+digamma[t][i][j];
					}
				}
			}
			for(int i=0;i<rowA;i++)
			{
				gamma[rowS-1][i]=alpha[rowS-1][i];
			}
			
			/* pi estimation */
			double [][]newI=new double[rowI][colI];
			for(int i=0;i<rowA;i++)
			{
				newI[rowI-1][i]=gamma[0][i];
			}
			
			
			/* A estimation */
			double newA[][]=new double[rowA][colA];
			double num,den;
			for(int i=0;i<rowA;i++)
			{
				den=0.0;
				for(int t=0;t<rowS-1;t++)
				{
					den=den+gamma[t][i];
				}
				for(int j=0;j<rowA;j++)
				{
					num=0.0;
					for(int t=0;t<rowS-1;t++)
					{
						num=num+digamma[t][i][j];
					}
					newA[i][j]=num/den;
				}
			}
			
			/* B estimation */
			double[][] newB=new double[rowB][colB];
			for(int i=0;i<rowA;i++)
			{
				den=0.0;
				for(int t=0;t<rowS;t++)
				{
					den=den+gamma[t][i];
				}
				for(int j=0;j<colB;j++)
				{
					num=0;
					for(int t=0;t<rowS;t++)
					{
						if(arrS[t]==j)
						{
						 num=num+gamma[t][i];	
						}
					}
					newB[i][j]=num/den;
				}
			}
			
			/* log prob estimation */
			logprob=0;
			for(int t=0;t<rowS;t++)
			{
				logprob=logprob+Math.log(scale[t]);
			}
			logprob=-logprob;
			
			
			iter++;
			if(Math.abs(logprob-oldLogProb)>1E-10)
			{
				oldLogProb=logprob;
			}
			else {
				flag=false;
			}
			arrA=newA;
			arrB=newB;
			arrI=newI;
		}
		//System.out.println(iter);
		System.out.print(arrA.length+" "+arrA[0].length+" ");
		for (int x = 0; x < arrA.length; x++) {
			for (int y = 0; y < arrA[0].length; y++) {

				System.out.print(arrA[x][y]+" ");
				
			}
			
		}
		System.out.println();
		System.out.print(arrB.length+" "+arrB[0].length+" ");
		for ( int x = 0; x < arrB.length; x++) {
			for ( int y = 0; y < arrB[0].length; y++) {

				System.out.print(arrB[x][y]+" ");
				
			}	}
		in.close();}
}
