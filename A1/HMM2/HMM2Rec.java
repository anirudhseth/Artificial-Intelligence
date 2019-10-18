package HMM2;

import java.util.Scanner;


public class HMM2Rec {
	
	public static double[][] transposeMatrix(double [][] m){
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }
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
    public static int maxP(double arr[])
    {
    	int temp=0;
  
    	for(int x=1;x<arr.length;x++)
    	{
    		if(arr[temp]<arr[x])
    		{
    
    			temp=x;
    		}
    	}
    	return temp;
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
		
		
		/* Display A */
//		 System.out.println("\nDisplay A");
//	       for (int x = 0; x < arrA.length; x++) {
//             for (int y = 0; y < arrA[0].length; y++) {
//
//                 System.out.print(arrA[x][y]);
//                 System.out.print("\t");
//             }System.out.println("");}
		
		/* Get B = Observation  Probability */
		rowB=in.nextInt();
		colB=in.nextInt(); 
		double arrB[][] = new double[rowB][colB]; 
		for (r = 0; r < rowB; r++) 
            for (c = 0; c < colB; c++) 
                arrB[r][c] = in.nextDouble();
		
		/* Display B */
//		 System.out.println("\nDisplay B");
//	       for (int x = 0; x < arrB.length; x++) {
//            for (int y = 0; y < arrB[0].length; y++) {
//
//                System.out.print(arrB[x][y]);
//                System.out.print("\t");
//            }System.out.println("");}
	       
	       
		
		/* Get PI = Initial State Probability */
		rowI=in.nextInt();
		colI=in.nextInt(); 
		double arrI[][] = new double[rowI][colI]; 
		for (r = 0; r < rowI; r++) 
            for (c = 0; c < colI; c++) 
                arrI[r][c] = in.nextDouble();
		
		
		/* Display PI */
//		 System.out.println("\nDisplay PI");
//	       for (int x = 0; x < arrI.length; x++) {
//           for (int y = 0; y < arrI[0].length; y++) {
//
//               System.out.print(arrI[x][y]);
//               System.out.print("\t");
//           }System.out.println("");}
//		
	       
		/* Observation  */
		rowS=in.nextInt(); 
		int arrS[] = new int[rowS]; 
            for (r = 0; r < rowS; r++) 
            { 
                arrS[r] = in.nextInt();
           }
            
       /* Display Observation */
            
//   		 System.out.println("\nDisplay Observation");
//   	      
//              for (int y = 0; y < arrS.length; y++) {
//
//                  System.out.print(arrS[y]);
//                  System.out.print("\t");
//              }
		
   	   double[][] viterbi = new double[rowA+1][rowS];
   	   int[][] backpointer = new int[rowA][rowS];
   	for(int i=0;i<rowA;i++)
   	{
   		viterbi[i][0]=arrI[0][i]*arrB[i][arrS[0]];
   		backpointer[i][0]=0;
   	}
   	for(int t=1;t<arrS.length;t++)
   	{
   		for(int s=0;s<arrA.length;s++)
   		{
   		
   			double temp= viterbi[0][t-1]*arrA[0][s]*arrB[s][arrS[t]];
   			int index=0;
   			for(int a=1;a<arrA.length;a++)
   			{
   				if(temp<viterbi[a][t-1]*arrA[a][s]*arrB[s][arrS[t]])
   				{
   					temp=viterbi[a][t-1]*arrA[a][s]*arrB[s][arrS[t]];
   					index=a;	}	
   		}
   			viterbi[s][t]=temp;
   			backpointer[s][t]=index;
   		}
   	}
   		
   	int arr[]=new int[arrS.length];
   	int best=0; 
   	
   	double max=viterbi[0][arrS.length-1];
   	for(int i=1;i<arrA.length;i++)
   	{
   		if(max<viterbi[i][arrS.length-1])
   		{
   			best=i;
   		}
   	}
   	arr[0]=best;
   	int t= arrS.length-1;
   	while(t>0)
   	{
   		int x= backpointer[best][t];
   		arr[arrS.length-t]=x;
   		best=x;
   		t--;
   		
   	}
   	
   	
   	/* Display backpointer */
//	 System.out.println("\nDisplay backpointer");
//      for (int x = 0; x < backpointer.length; x++) {
//       for (int y = 0; y < backpointer[0].length; y++) {
//
//           System.out.print(backpointer[x][y]);
//           System.out.print("\t");
//       }System.out.println("");}	
      
   	/* Display Viterbi */
//	 System.out.println("\nDisplay viterbi");
//      for (int x = 0; x < viterbi.length; x++) {
//       for (int y = 0; y < viterbi[0].length; y++) {
//
//           System.out.print(viterbi[x][y]);
//           System.out.print("\t");
//       }System.out.println("");}	  
      
      for(int i=arr.length-1;i>=0;i--)
      
      {
    	  System.out.print(arr[i]+" ");
      }
      
      
      
            in.close();
		}
	  
	

	
	
           } 
       
	

