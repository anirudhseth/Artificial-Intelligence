import java.util.*;


public class Player {
    
    public static final int d=2;
    public static final int[][] winningpos = 
     {
        { 0, 1, 2, 3 },
        { 4, 5, 6, 7 },
        { 8, 9, 10, 11 },
        { 12, 13, 14, 15 },
        { 0, 4, 8, 12 },
        { 1, 5, 9, 13 },
        { 2, 6, 10, 14 },
        { 3, 7, 11, 15 },
        { 0, 5, 10, 15 },
        { 3, 6, 9, 12 }
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
        for(int i=0;i<10;i++)
        {    x_count=0;
             o_count=0;
            for (int j=0;j<4;j++)
            {
                int findCell= g.at(winningpos[i][j]);
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