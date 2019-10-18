import java.util.ArrayList;
class Player {

ArrayList<ArrayList<HMM>> model= new ArrayList<ArrayList<HMM>>();

    public Player() {
     for(int i=0;i<Constants.COUNT_SPECIES;i++)
     {
         model.add(i,new ArrayList<HMM>());  // initliazed seperate HMM Model for each species
     }
    }

    public Action shoot(GameState pState, Deadline pDue) {
       double threshold=0.60;
       if(pState.getBird(0).getSeqLength()<70) return cDontShoot;
        double maxPbestmodel=Double.NEGATIVE_INFINITY;
        int maxPindexmodel=-1;
        int birdtoHit=-1;
        for(int i=0;i<pState.getNumBirds();i++)
        {
           
            double maxP=Double.NEGATIVE_INFINITY;
            int maxPindex=-1;
           // System.err.println("outside loop");
            if(pState.getBird(i).isAlive() && getSpecies(pState.getBird(i), pState.getRound())!=Constants.SPECIES_BLACK_STORK)
            {
              //  System.err.println("inside if");
                int[] obsSeq = new int[pState.getBird(i).getSeqLength()];
                for (int k = 0; k < pState.getBird(i).getSeqLength();k++) {
                    obsSeq[k]=pState.getBird(i).getObservation(k);
                }
                HMM x;
                int sp=getSpecies(pState.getBird(i), pState.getRound());
               if(this.model.get(sp).size()>0){
                 x= this.model.get(sp).get(0);
                 
                }
                else{x= new HMM(5,9);
                    x.baumWelch(obsSeq);}
                
                double pOfnextMove[]=scale(x.pOfNextOb(scale(x.fwdAlgo_alphaAtT(obsSeq))));
                
                for(int z=0;z<pOfnextMove.length;z++)
                {
                    if(pOfnextMove[z]>maxP)
                    {
                        maxP=pOfnextMove[z];
                        maxPindex=z;
                    }
                }
                //System.err.println("maxP"+maxP);
            }
            if(maxP>maxPbestmodel)
            {
                maxPbestmodel= maxP;
                maxPindexmodel=maxPindex;
                birdtoHit=i;
            }
            //System.err.println(maxPbestmodel);
            }

        if(maxPbestmodel>threshold) return new Action(birdtoHit,maxPindexmodel);
        else return cDontShoot;

    }
   
    public int[] guess(GameState pState, Deadline pDue) {
   
        int[] lGuess = new int[pState.getNumBirds()];
        for (int i = 0; i < pState.getNumBirds(); i++)
        {
            int round=pState.getRound();
            Bird b= pState.getBird(i);
            lGuess[i] = getSpecies(b,round);
        
            }    return lGuess;
    }

    public void hit(GameState pState, int pBird, Deadline pDue) {
        System.err.println("HIT BIRD!!!");
    }

    public void reveal(GameState pState, int[] pSpecies, Deadline pDue) {
    for(int i=0;i<pSpecies.length;i++)
    {
        Bird b=pState.getBird(i);
        int species=pSpecies[i];
        
            HMM h=new HMM(5,9);
            int[] obsSeq = new int[b.getSeqLength()];
            for (int k = 0; k < b.getSeqLength(); k++) {
                if (b.wasDead(k)) {
                    break;
                }
                else { 
                    obsSeq[k]=b.getObservation(k);
                }
            }
            h.baumWelch(obsSeq);
            this.model.get(species).add(h); 
        
    }
    
    }

    public static int getRandomIntegerBetweenRange(double min, double max){
        int x = (int)((Math.random()*((max-min)+1))+min);
        return x;
    }
    
    public static final Action cDontShoot = new Action(-1, -1);

    public int getSpecies(Bird b, int round)
    {
        
    if(round==0)
    {
         // return getRandomIntegerBetweenRange(Constants.SPECIES_PIGEON,Constants.SPECIES_BLACK_STORK);
        return Constants.SPECIES_PIGEON; 
    }
    else{
        double maxPSpecies[]=new double[Constants.COUNT_SPECIES];
        for (int j = 0; j < Constants.COUNT_SPECIES; j++)
        {
            ArrayList<HMM> speciesModel = this.model.get(j);
        
            double tempP=0.0;
            double maxPBird=0.0;
        for(int k = 0; k < speciesModel.size(); k++)
        {
            int[] obsSeq = new int[b.getSeqLength()];
            for (int i = 0; i < b.getSeqLength(); i++) {
                if (b.wasDead(i)) {
                    break;
                }
                else { 
                    obsSeq[i]=b.getObservation(i);
                }
            }
            tempP=speciesModel.get(k).fwdAlgo(obsSeq);
            if(tempP>maxPBird) {maxPBird=tempP;}
        }
        maxPSpecies[j]=maxPBird;
    }
    double maxPOverall=0.0;
    int indexMaxP=0;
    for(int z=0;z<maxPSpecies.length;z++)
    {
        if(maxPSpecies[z]>maxPOverall)
        {
            maxPOverall=maxPSpecies[z];
            indexMaxP=z;
        }  
    }
    return indexMaxP;
    }
    }
    public static double[] scale(double[] arr) {

        double temp =0.0;
        double[] arr2 = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
          temp += arr[i];
        }
        //System.err.println("SUM "+ sum);
        for (int i = 0; i < arr.length; i++) {
            arr2[i] = arr[i]/ temp;
        }

        return arr2;
    }

}