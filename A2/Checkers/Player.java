import java.util.*;

public class Player {
    /**
     * Performs a move
     *
     * @param pState the current state of the board
     * @param pDue   time before which we must have returned
     * @return the next state the board is in after our move
     */
    public final int d = 15;
    public HashMap<Integer, int[]> hm = new HashMap<Integer, int[]>();

    public Vector<GameState> EvaluateAndSort(Vector<GameState> lNextStates, int player) {
        HashMap<GameState, Integer> pQueue = new HashMap<>();
        for (int i = 0; i < lNextStates.size(); i++) {
            GameState g = lNextStates.get(i);
            pQueue.put(g, heuristic(g, player));
        }

        Vector<GameState> sortedByCount = sortByValue(pQueue);

        return sortedByCount;

    }

    public int returnHash(GameState g) {

        int arr[] = new int[32];
        for (int i = 0; i < 32; i++) {
            arr[i] = g.get(i);
        }

        return arr.hashCode();
    }

    public static Vector<GameState> sortByValue(HashMap<GameState, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<GameState, Integer>> list = new LinkedList<Map.Entry<GameState, Integer>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<GameState, Integer>>() {
            public int compare(Map.Entry<GameState, Integer> o1, Map.Entry<GameState, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        Vector<GameState> temp2 = new Vector<>();
        HashMap<GameState, Integer> temp = new LinkedHashMap<GameState, Integer>();
        for (Map.Entry<GameState, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
            temp2.add(aa.getKey());
        }

        return temp2;
    }

    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        int index = 0;
        int me = pState.getNextPlayer();
        System.err.println(me);
        int map[] = new int[lNextStates.size()];

        try {
            for (int j = 0; j <= d; j++)

            {

                lNextStates = EvaluateAndSort(lNextStates, me);

                for (int i = lNextStates.size() - 1; i >= 0; i--) {

                    map[i] = miniMax(lNextStates.get(i), me, j, Integer.MIN_VALUE, Integer.MAX_VALUE, pDue);

                }

                hm.clear();
            }
        } catch (Exception e) {
        }

        int temp = map[0];
        index = 0;
        for (int i = 1; i < lNextStates.size(); i++) {
            if (map[i] > temp) {
                temp = map[i];
                index = i;
            }
        }

        /*
         * Here you should write your algorithms to get the best next move, i.e. the
         * best next state. This skeleton returns a random move instead.
         */
        // System.err.print(hm.values());
        // hm.clear();
        return lNextStates.elementAt(index);
    }

    public int heuristic(GameState g, int Player) {
        int score = 0;
        int red_pieces = 0;
        int white_pieces = 0;

        for (int i = 0; i < 32; i++) {
            if (g.get(i) == 1) {
                red_pieces += 1;
            } else if (g.get(i) == 5) {
                red_pieces += 2;
            }
            if (g.get(i) == 2) {
                white_pieces += 1;
            } else if (g.get(i) == 6) {
                white_pieces += 2;
            }

        }
        if (Player == Constants.CELL_RED) {
            score = red_pieces - white_pieces;
            if (g.isRedWin())
                score = Integer.MAX_VALUE;
            else if (g.isWhiteWin())
                score = Integer.MIN_VALUE;

        }
        if (Player == Constants.CELL_WHITE) {
            score = white_pieces - red_pieces;
            if (g.isRedWin())
                score = Integer.MIN_VALUE;
            else if (g.isWhiteWin())
                score = Integer.MAX_VALUE;
            ;

        }
        return score;
    }

    int miniMax(GameState g, int player, int depth, int alpha, int beta, Deadline pDue) throws Exception {

        Vector<GameState> nextMoves = new Vector<GameState>();
        int score;
        if (pDue.timeUntil() < 100) {
            throw new Exception("time out");
        }

        if (hm.containsKey(returnHash(g))) {

            int x = hm.get(returnHash(g))[0];
            int scoretemp = hm.get(returnHash(g))[1];
            if (x <= depth) {
                return scoretemp;

            }
        }

        if (depth == 0 || g.isEOG()) {
            score = heuristic(g, player);

            int temp[] = new int[2];
            temp[0] = depth;
            temp[1] = score;
            if (hm.containsKey(returnHash(g)))
                hm.replace(returnHash(g), temp);
            else
                hm.put(returnHash(g), temp);
            return score;

        }
        g.findPossibleMoves(nextMoves);
        nextMoves = EvaluateAndSort(nextMoves, player);

        if (player == g.getNextPlayer()) {

            score = Integer.MIN_VALUE;
            for (int i = nextMoves.size() - 1; i >= 0; i--) {
                //System.err.println("inside max term. player is+"+player);
                score = Math.max(miniMax(nextMoves.get(i), g.getNextPlayer(), depth - 1, alpha, beta, pDue), score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break;
                }

            }

            int temp[] = new int[2];
            temp[0] = depth;
            temp[1] = score;
            if (hm.containsKey(returnHash(g)))
                hm.replace(returnHash(g), temp);
            else
                hm.put(returnHash(g), temp);
            return score;

        } else {
            score = Integer.MAX_VALUE;
            for (int i = 0; i < nextMoves.size(); i++) {
               // System.err.println("inside min term. player is+"+player);
                score = Math.min(miniMax(nextMoves.get(i), player, depth - 1, alpha, beta, pDue), score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break;
                }

            }

            int temp[] = new int[2];
            temp[0] = depth;
            temp[1] = score;
            if (hm.containsKey(returnHash(g)))
                hm.replace(returnHash(g), temp);
            else
                hm.put(returnHash(g), temp);
            return score;
        }
    }

}