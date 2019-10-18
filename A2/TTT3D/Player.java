import java.util.*;


public class Player {
    
    public static final int d=1;
    private static final int[][] winningpos =
    {
            {0, 1, 2, 3},
            {4, 5, 6, 7},
            {8, 9, 10,11},
            {12, 13, 14, 15},
            {16, 17, 18, 19},
            {20, 21, 22, 23},
            {24, 25, 26, 27},
            {28, 29, 30, 31},
            {32, 33, 34, 35},
            {36, 37, 38, 39},
            {40, 41, 42, 43},
            {44, 45, 46, 47},
            {48, 49, 50, 51},
            {52, 53, 54, 55},
            {56, 57, 58, 59},
            {60, 61, 62, 63},
            {0, 4, 8, 12},
            {1, 5, 9, 13},
            {2, 6, 10, 14},
            {3, 7, 11, 15},
            {16, 20, 24, 28},
            {17, 21, 25, 29},
            {18, 22, 26, 30},
            {19, 23, 27, 31},
            {32, 36, 40, 44},
            {33, 37, 41, 45},
            {34, 38, 42, 46},
            {35, 39, 43, 47},
            {48, 52, 56, 60},
            {49, 53, 57, 61},
            {50, 54, 58, 62},
            {51, 55, 59, 63},
            {3, 6, 9, 12},
            {19, 22, 25, 28},
            {35, 38, 41, 44},
            {51, 54, 57, 60},
            {0, 5, 10, 15},
            {16, 21, 26, 31},
            {32, 37, 42, 47},
            {48, 53, 58, 63},
            {0, 17, 34, 51},
            {4, 21, 38, 55},
            {8, 25, 42, 59},
            {12, 29, 46, 63},
            {3, 18, 33, 48},
            {7, 22, 37, 52},
            {11, 26, 41, 56},
            {15, 30, 45, 60},
            {0, 16, 32, 48},
            {1, 17, 33, 49},
            {2, 18, 34, 50},
            {3, 19, 35, 51},
            {4, 20, 36, 52},
            {5, 21, 37, 53},
            {6, 22, 38, 54},
            {7, 23, 39, 55},
            {8, 24, 40, 56},
            {9, 25, 41, 57},
            {10, 26, 42, 58},
            {11, 27, 43, 59},
            {12, 28, 44, 60},
            {13, 29, 45, 61},
            {14, 30, 46, 62},
            {15, 31, 47, 63},
            {0, 20, 40, 60},
            {1, 21, 41, 61},
            {2, 22, 42, 62},
            {3, 23, 43, 63},
            {12, 24, 36, 48},
            {13, 25, 37, 49},
            {14, 26, 38, 50},
            {15, 27, 39, 51},
            {51, 38, 25, 12},
            {48, 37, 26, 15},
            {3, 22, 41, 60},
            {0, 21, 42, 63},

    };


      public static final int[][] heuristic= {
        {      0,   -10,  -100, -1000, -10000 },
        {     10,     0,     0,     0, 0      },
        {    100,     0,     0,     0, 0      },
        {   1000,     0,     0,     0, 0      },
        {  10000,     0,     0,     0, 0      }
      };
      
    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }
      //  System.err.println(lNextStates.get(0).at(0,0));
        else if (lNextStates.size() == 1) {
            return lNextStates.get(0);
        }
        int max=Integer.MIN_VALUE;
        int index=0;
        for(int i=0;i<lNextStates.size();i++)
        {
            int temp= miniMax(lNextStates.get(i), Constants.CELL_X, d,Integer.MIN_VALUE,Integer.MAX_VALUE);
            if(temp>max)
            {
                max=temp;
                index=i;
            }
        } 
        return lNextStates.elementAt(index);

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        //Random random = new Random();
       // return lNextStates.elementAt(random.nextInt(lNextStates.size()));
    }
    public int h_value(GameState g){
        if(g.isEOG()){
        if(g.isXWin()) {return 10000;}
        else if(g.isOWin()) {return -10000;}
            else
        return 0;}
        int score=0;
        int x_count=0;
            int o_count=0;
        for(int i=0;i<76;i++)
        {    x_count=0;
             o_count=0;
            for (int j=0;j<4;j++)
            {
                int findCell= g.at(winningpos[i][j]);
                //System.err.println(findCell);
                if(findCell==Constants.CELL_X)
                {
                    x_count++;
                }
                else if(findCell==Constants.CELL_O)
                {
                   o_count++;
                }
            }
            score+= heuristic[x_count][o_count];
        }
       // System.err.println("the score for gamestate "+g+" is: "+score);
        return score;
    }
    int miniMax(GameState g, int player, int depth, int alpha, int beta)
    {
        Vector<GameState> nextMoves= new Vector<GameState>();
        
        
        
        if(depth==0 || g.isEOG())
        {
            return h_value(g);
        }
        
        if(player==Constants.CELL_X)
        {   g.findPossibleMoves(nextMoves);
            int score=Integer.MIN_VALUE;
            for(int i=0;i<nextMoves.size();i++){
                 score=Math.max(miniMax(nextMoves.get(i), Constants.CELL_O, depth-1,alpha,beta),score);
                alpha=Math.max(alpha, score);
                if (beta <= alpha)
                {
                    //System.err.println("pruned ");
                    break;}
           
            
            }
            return score;
            
        }
        else 
        {   g.findPossibleMoves(nextMoves);
            int score=Integer.MAX_VALUE;
            for(int i=0;i<nextMoves.size();i++){
                score=Math.min(miniMax(nextMoves.get(i), Constants.CELL_X, depth-1,alpha,beta),score);
                beta=Math.min(beta, score);
                if (beta <= alpha)
                {
                   // System.err.println("pruned ");
                    break;}
            
            }
            return score;
        }
                     
    }
}