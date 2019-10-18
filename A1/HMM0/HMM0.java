package HMM0;

import java.util.Scanner;


public class HMM0 {
	
	public static double[][] multiplyMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length; // m1 columns length
        int m2RowLength = m2.length;    // m2 rows length
        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
        int mRRowLength = m1.length;    // m result rows length
        int mRColLength = m2[0].length; // m result columns length
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }

	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in); 
		int rowA,rowB,colA,colB,rowI,colI,r,c;
	
		
		rowA=in.nextInt();
		colA=in.nextInt(); 
		double arrA[][] = new double[rowA][colA]; 
		for (r = 0; r < rowA; r++) 
            for (c = 0; c < colA; c++) 
                arrA[r][c] = in.nextDouble();
		
	
		
		
		rowB=in.nextInt();
		colB=in.nextInt(); 
		double arrB[][] = new double[rowB][colB]; 
		for (r = 0; r < rowB; r++) 
            for (c = 0; c < colB; c++) 
                arrB[r][c] = in.nextDouble();
		
		
		
		
		rowI=in.nextInt();
		colI=in.nextInt(); 
		double arrI[][] = new double[rowI][colI]; 
		for (r = 0; r < rowI; r++) 
            for (c = 0; c < colI; c++) 
                arrI[r][c] = in.nextDouble();
		
		
		
		 
		double arrP[][] = multiplyMatrix(arrI,arrA);
		
        System.out.print(rowI+" "+colB+" ");
        double arrO[][] = multiplyMatrix(arrP,arrB);
            for (r = 0; r < rowI; r++) { 
                for (c = 0; c < colB; c++) 
                    System.out.print(arrO[r][c] + "  "); 
                System.out.println(); 
                
        }
            in.close();
		}
	  
           } 
       
	

