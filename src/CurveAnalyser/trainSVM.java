package CurveAnalyser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by Flexscan2243 on 30.04.2016.
 */
public class TrainSVM {
    private static final int HALF_TRAINING_DATA_SIZE = 5;
    private Connection conn;
    private SVM svm;
    private LinkedList<String> negBlockList;
    private LinkedList<String> posBlockList;
    private LinkedList<String> fullBlockList;
    private String inputName;
    private String blockRow;
    private ArrayList<Integer> bidSumVector;
    private int[] blocksSumVec;
    private RandomInputType rit;



    public TrainSVM(Connection conn){
        //sql
        this.conn = conn;

        //name
        inputName = "";
        // lists
        negBlockList = new LinkedList<String>();
        posBlockList = new LinkedList<String>();
        fullBlockList = new LinkedList<String>();

        //random
        bidSumVector = new ArrayList<Integer>();

    }



    public int startTrainSVM(Gui gui, SVM svm){
        /** Warning: getting the select list indirectly from Gui might be unsafe. A copy of list should be used. TO DO soon */
        try {
            /**
             * Get sessionIDs selected in GUI (temporary solution)
             */
            String selectedNegative = gui.getTrainOptSel();

            /**
             * Check if there is a appropriate number of blocks if so - continue, if not - abort
             * Count positive ones
             */
            String SQL_B_SUM = "SELECT SUM(c) as s FROM(SELECT COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID != '"+selectedNegative+"' GROUP BY sessionID) AS x";
            Statement stmtBsum = conn.createStatement();
            ResultSet rsBsum = stmtBsum.executeQuery(SQL_B_SUM);
            rsBsum.next();
            int positiveBlocks = rsBsum.getInt("s");

            /**
             * Count negative ones
             */
            String SQL_B_SUM2 = "SELECT COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID = '"+selectedNegative+"'";
            Statement stmtBsum2 = conn.createStatement();
            ResultSet rsBsum2 = stmtBsum2.executeQuery(SQL_B_SUM2);
            rsBsum2.next();

            /**
             * Return if the data is too small
             */
            int negativeBlocks = rsBsum2.getInt("c");
            if(negativeBlocks < HALF_TRAINING_DATA_SIZE){return 1;}
            else if(positiveBlocks < HALF_TRAINING_DATA_SIZE){return 2;}

            /**
             * Training
             */
            createSvmInput(selectedNegative, rit.NEGATIVE, 0);
            createSvmInput(selectedNegative, rit.POSITIVE, 0);
            shuffleBlockLists();
            for(String s : negBlockList){
                System.out.println(s);
            }
            for(String s : posBlockList){
                System.out.println(s);
            }
            System.out.println("all: ");
            for(String s : fullBlockList){
                System.out.println(s);
            }
            writeToFile(selectedNegative);

            /**
             * Get the best hyperparameters C and Gamma
             */
            String[] argv = {"-v","2", inputName};
            try {
                svm.run(argv);
            }
            catch (IOException e){System.out.println("SVM START ERROR");}


        }
        catch (SQLException e){System.out.println("ERROR");}


        return 0;
    }

    public void writeToFile(String selectedNegative){
        File f = new File(selectedNegative + ".txt");
        int fIt = 1;
        if(f.exists() && !f.isDirectory()) {
            while(f.exists() && !f.isDirectory()) {
                f = new File(selectedNegative + " (" + fIt + ")" + ".txt");
                inputName = selectedNegative + " (" + fIt + ")" + ".txt";
                fIt++;
            }
        }
        else{inputName = selectedNegative + ".txt";}

        FileWriter fr;
        String newLine = System.getProperty("line.separator");
        try {
            fr = new FileWriter(f);
            BufferedWriter br  = new BufferedWriter(fr);

            for (int i = 0 ; i < fullBlockList.size() ; i++){
                br.write(fullBlockList.get(i) + newLine);
            }
            fr.close();
        }
        catch (IOException e){}


    }
    private void createBlocksSumVec(ResultSet rs, int usedData ){
        try {
            bidSumVector = new ArrayList<Integer>();
            while (rs.next()) {
                if(rs.getInt("c") - usedData > 0) {
                    bidSumVector.add(rs.getInt("c"));
                }
                else{
                    bidSumVector.add(0);
                }
            }
            while (rs.previous()) {} /**< reset the pointer */

            int segArrSize = bidSumVector.size();
            blocksSumVec = new int[segArrSize];

            int randSegSize;
            int rest;
            int currHTDS = HALF_TRAINING_DATA_SIZE;
            boolean rearrangeArray = true;

            for (int i = 0 ; i < segArrSize ; i++){
                if(rearrangeArray) {
                    randSegSize = currHTDS / (segArrSize - i);
                    rest = currHTDS - randSegSize * (segArrSize - i);
                    for (int j = i; j < segArrSize; j++) {
                        if (rest > 0) {
                            blocksSumVec[j] = randSegSize + 1;
                            rest--;
                        } else {
                            blocksSumVec[j] = randSegSize;
                        }
                    }
                }

                if(bidSumVector.get(i) - usedData < blocksSumVec[i]){
                    blocksSumVec[i] = bidSumVector.get(i) - usedData;
                    rearrangeArray = true;
                }
                else {rearrangeArray = false;}

                currHTDS -= blocksSumVec[i];

            }
//            for(int i = 0 ; i < randomVector.length ; i++) {
//                System.out.println(randomVector[i]);
//            }
        }
        catch (SQLException e) {
        }

    }

    private void shuffleArray(int[] array)
    {
        int index, temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public int[] createRandomizationVector(int sessionIt){
        int[] randomVec = new int[blocksSumVec[sessionIt]];
        int[] tmpVec = new int[bidSumVector.get(sessionIt)];
        for(int i = 0 ; i < bidSumVector.get(sessionIt) ; i++){
            tmpVec[i] = i;
        }

        shuffleArray(tmpVec);
        for(int i = 0 ; i < blocksSumVec[sessionIt] ; i++){
            randomVec[i] = tmpVec[i];
        }
        Arrays.sort(randomVec);

        return randomVec;
    }

    public void createSvmInput(String selectedNegative, RandomInputType r, int usedData){
        /**
         * Negative/Positive input settings.
         * Get all necessary sessionIDs
         */
        String SQL_B_SID_CNT;
        String blockRowType;
        LinkedList<String> blockList;

        if(r == rit.NEGATIVE){
            SQL_B_SID_CNT = "SELECT SessionID, COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID = '" + selectedNegative + "' GROUP BY sessionID ORDER BY c ASC";
            blockRowType = "-1 ";
            negBlockList = new LinkedList<String>();
            fullBlockList = new LinkedList<String>();
            blockList = negBlockList;
        }
        else{
            SQL_B_SID_CNT = "SELECT SessionID, COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID != '" + selectedNegative + "' GROUP BY sessionID ORDER BY c ASC";
            blockRowType = "1 ";
            posBlockList = new LinkedList<String>();
            fullBlockList = new LinkedList<String>();
            blockList = posBlockList;
        }
        try {

            Statement stmtBsidCnt = conn.createStatement();
            ResultSet rsBsid = stmtBsidCnt.executeQuery(SQL_B_SID_CNT);
            String currSessionID;

            /**
             * Create a randomization vector. Init session iterator for it
             */
            createBlocksSumVec(rsBsid, usedData);
            //blockList = new LinkedList<String>();

            int sessionIt = 0;
            while (rsBsid.next()) {
                /**
                 * Go to next iteration if there is no data
                 */
                if(bidSumVector.get(sessionIt) == 0){
                    continue;
                }

                /**
                 * Reset data
                 */
                if(sessionIt > 99999999){break;}


                /**
                 * Fetch all blockIDs specific to the current sessionID
                 */
                currSessionID = rsBsid.getString("SessionID");
                String SQL_B_BID = "SELECT DISTINCT blockID FROM `blocks` WHERE sessionID = '" + currSessionID + "'";
                Statement stmtBbid = conn.createStatement();
                ResultSet rsBbid = stmtBbid.executeQuery(SQL_B_BID);
                String currBlockID;

                int[] randomSelectedBlocks = createRandomizationVector(sessionIt);
                int blockIt = 0;

                /**
                 * determine start block
                 */
                int usedDataIt = usedData;
                while (usedDataIt > 0){
                    rsBbid.next();
                    usedDataIt--;
                }

                while (rsBbid.next()) {
                    if (Arrays.binarySearch(randomSelectedBlocks, blockIt) >= 0) {

                        /**
                         * reset data
                         */
                        if(blockIt > 99999999){break;}
                        blockRow = blockRowType;
                        int rowIt = 0;
                        /**
                         * Get all features needed to create a one feature vector (one SVM sample)
                         */
                        currBlockID = rsBbid.getString("BlockID");

                        String SQL_B_ALL = "SELECT feature FROM `blocks` WHERE SessionID = '" + currSessionID + "' AND BlockID = '" + currBlockID + "'";
                        Statement stmtBall = conn.createStatement();
                        ResultSet rsBall = stmtBall.executeQuery(SQL_B_ALL);
                        while (rsBall.next()) {
                            if (rsBall.getDouble("feature") > 0) {
                                blockRow = blockRow + rowIt + ":" + rsBall.getDouble("feature") + " ";
                            }
                            rowIt++;
                        }
                        /**
                         * Save the row to the list
                         */
                        blockList.add(blockRow);


                        /**
                         * prepare input
                         */
                        prepareInput();
                    }
                    blockIt++;
                }
                /**
                 * iterator ++
                 */
                sessionIt++;
            }

        }
        catch (SQLException e){}
    }

    public void shuffleBlockLists(){
        fullBlockList = new LinkedList<String>();
        for (int i = 0 ; i < HALF_TRAINING_DATA_SIZE ; i++){
            fullBlockList.add(negBlockList.get(i));
            fullBlockList.add(posBlockList.get(i));
        }
    }

    public void prepareInput(){}


}

enum RandomInputType{
    NEGATIVE,POSITIVE
}
