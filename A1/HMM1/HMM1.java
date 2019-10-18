


import java.util.Scanner;


public class HMM1 {
	
	public static double alphaPass(double[][] arrA, double[][] arrB, double[] arrI,int[] arrS)
	{
		double P=0.0;
		double[][] alpha = new double[arrS.length][arrA.length];    
          for (int i = 0; i < arrA.length; i++) {
              alpha[0][i]=arrI[i]*arrB[i][arrS[0]];
            }
            
            for (int t = 1; t < arrS.length; t++) {
              for (int i = 0; i < arrA.length; i++) {
                for (int j = 0; j < arrA.length; j++) {
                  alpha[t][i] += alpha[(t-1)][j]*arrA[j][i]*arrB[i][arrS[t]];
                }
              }
            }
           
            for (int i = 0; i < arrA.length; i++) {
              P += alpha[(arrS.length-1)][i];
            }
            return P;

     
   
	}
	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in); 
		int rowA,rowB,colA,colB,rowI,colI,r,c,rowS;
	
		 /* Get A = State Transition Probability */
		rowA=in.nextInt();
		colA=in.nextInt(); 
		double arrA[][] = new double[rowA][colA]; 
		for (r = 0; r < rowA; r++) 
            for (c = 0; c < colA; c++) 
                arrA[r][c] = in.nextDouble();
		
		
		/* Get B = Observation  Probability */
		rowB=in.nextInt();
		colB=in.nextInt(); 
		double arrB[][] = new double[rowB][colB]; 
		for (r = 0; r < rowB; r++) 
            for (c = 0; c < colB; c++) 
                arrB[r][c] = in.nextDouble();
	       
		
		/* Get PI = Initial State Probability */
		rowI=in.nextInt();
		colI=in.nextInt(); 
		double arrI[] = new double[colI]; 
            for (c = 0; c < colI; c++) 
                arrI[c] = in.nextDouble();
		
		
		
	       
		/* Get Observation  */
		rowS=in.nextInt(); 
		
		int arrS[] = new int[rowS]; 
            for (r = 0; r < rowS; r++) 
            { 
                arrS[r] = in.nextInt();
           }
          
            in.close();
            System.out.println(alphaPass(arrA,arrB,arrI,arrS)) ;
		}
	  
           } 
       
	

