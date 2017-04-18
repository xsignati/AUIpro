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
    private static final int HALF_TRAINING_DATA_SIZE = 48;
    private static final int HALF_CV_DATA_SIZE = 1;
    private static final int posUserNum = 3;
    private static final int BLOCK_IT_LIMIT = 9999999;
    private Connection conn;
    private int positiveBlocks;
    private int negativeBlocks;
    private String cvInputName;
    private String trainInputName;
    private LinkedList<String> cvBlockList;
    private LinkedList<String> trainBlockList;
    private Subsidiaries.hyperParameters hyperParams;




    public TrainSVM(Connection conn){
        //sql
        this.conn = conn;

        //name
        cvInputName = "";
        trainInputName = "";
        // lists
        cvBlockList = new LinkedList<String>();
        trainBlockList = new LinkedList<String>();

        //hyperParams
        hyperParams = new Subsidiaries().new hyperParameters(0,0,0);

    }


    public int startTrainSVM(Gui gui, SVM svm, String selSessionID){
        try {
            /**
             * Get sessionIDs selected in GUI (temporary solution)
             */
            String selectedNegative = selSessionID;

            /**
             * Check if there is a appropriate number of blocks if so - continue, if not - abort
             * Count positive ones
             */
            String SQL_B_SUM = "SELECT SUM(c) as s FROM(SELECT COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID != '"+selectedNegative+"' GROUP BY sessionID) AS x";
            Statement stmtBsum = conn.createStatement();
            ResultSet rsBsum = stmtBsum.executeQuery(SQL_B_SUM);
            rsBsum.next();
            positiveBlocks = rsBsum.getInt("s");

            /**
             * Count negative ones
             */
            String SQL_B_SUM2 = "SELECT COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID = '"+selectedNegative+"'";
            Statement stmtBsum2 = conn.createStatement();
            ResultSet rsBsum2 = stmtBsum2.executeQuery(SQL_B_SUM2);
            rsBsum2.next();
            negativeBlocks = rsBsum2.getInt("c");

            /**
             * Return if the data is too small
             */
            if(negativeBlocks < (HALF_TRAINING_DATA_SIZE + HALF_CV_DATA_SIZE)){return 1;}
            else if(positiveBlocks < (HALF_TRAINING_DATA_SIZE + HALF_CV_DATA_SIZE) ){return 2;}

            /**
             * Create SVM input data
             */
            gui.updateBar(25, Gui.S_RED);
            createSvmInput(selectedNegative);
            gui.updateBar(50, Gui.S_RED);
            /**
             * Grid search cross validation
             */
            String[] argv = {"-v","10", cvInputName};

            try {
                svm.run(argv, hyperParams);
                gui.updateBar(75, Gui.S_RED);
                gui.updateTextArea("Grid search cross validation completed. \nSelected parameters: \nC: " + hyperParams.getC() + "\nGamma: " + hyperParams.getGamma() + "\nAccuracy: " + hyperParams.getAcc(), Gui.S_WHITE, true);
            }
            catch (IOException e){
                gui.updateTextArea("Grid search cross validation error", Gui.S_RED, true);}
            /**
             * Train
             */
            String[] argv2 = {trainInputName};

            try {
                gui.updateBar(99, Gui.S_RED);
                svm.run(argv2, hyperParams);
                gui.updateTextArea("SVM model trained and saved to a file successfully", Gui.S_WHITE, true);
            }
            catch (IOException e){gui.updateTextArea("Error training SVM model", Gui.S_RED, true);}

        }
        catch (SQLException e){System.out.println("ERROR");}


        return 0;
    }

    public String writeToFile(String selectedNegative, LinkedList<String> fullBlockList){
        File f = new File(selectedNegative + ".txt");
        int fIt = 1;
        String inputName = "";
        if(f.exists() && !f.isDirectory()) {
            while(f.exists() && !f.isDirectory()) {
                f = new File(selectedNegative + " (" + fIt + ")" + ".txt");
                inputName = selectedNegative + " (" + fIt + ")" + ".txt";
                fIt++;
            }
        }
        else{inputName = selectedNegative + ".txt";}

        FileWriter fr = null;
        BufferedWriter br = null;
        String newLine = System.getProperty("line.separator");

        try {
            fr = new FileWriter(f);
            br  = new BufferedWriter(fr);

            for (int i = 0 ; i < fullBlockList.size() ; i++){
                br.write(fullBlockList.get(i) + newLine);
            }

        } catch (IOException e){
        } finally{
            try {
                br.close();
                fr.close();
            }
            catch (IOException e){}
        }
        return inputName;
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

    public int[] createRandomizationVector(int halfDataSize, int size){
        int[] randomVec = new int[halfDataSize];
        int[] tmpVec = new int[size];
        for(int i = 0 ; i < tmpVec.length ; i++){
            tmpVec[i] = i;
        }
        shuffleArray(tmpVec);

        for(int i = 0 ; i < randomVec.length ; i++){
            randomVec[i] = tmpVec[i];
        }
        Arrays.sort(randomVec);

        for (int i = 0 ; i < randomVec.length ; i++){
            System.out.print("," + randomVec[i]);
        }
        System.out.print("\n");
        return randomVec;
    }

    public void shuffleBlockLists(LinkedList<String> fullBlockList, LinkedList<String> negBlockList, LinkedList<String> posBlockList, int halfDataSize){
//        for (int i = 0 ; i < halfDataSize ; i++){
//            fullBlockList.add(negBlockList.get(i));
//            fullBlockList.add(posBlockList.get(i));
//        }
        fullBlockList.addAll(negBlockList);
        fullBlockList.addAll(posBlockList);
        Collections.shuffle(fullBlockList);
    }

    public void createSvmInput(String selectedNegative){
        /**
         * Negative/Positive samples list queries
         */
        String SQL_B_SID_CNT_N = "SELECT `blocks`.sessionID, blockID, sums.mark_sum " +
            "FROM `blocks` " +
            "INNER JOIN (select sessionID, count(DISTINCT blockID) AS mark_sum " +
            "FROM `blocks` " +
            "GROUP BY sessionID) AS sums ON sums.sessionID = `blocks`.sessionID " +
            "WHERE `blocks`.sessionID = '"+selectedNegative+"' " +
            "GROUP BY sessionID, blockID " +
            "ORDER BY sums.mark_sum ASC";

        //String magda = "magdawe";
        String SQL_B_SID_CNT_P = "SELECT `blocks`.sessionID, blockID, sums.mark_sum " +
                "FROM `blocks` " +
                "INNER JOIN (select sessionID, count(DISTINCT blockID) AS mark_sum " +
                "FROM `blocks` " +
                "GROUP BY sessionID) AS sums ON sums.sessionID = `blocks`.sessionID " +
                "WHERE `blocks`.sessionID != '"+selectedNegative+"' " +
                "GROUP BY sessionID, blockID " +
                "ORDER BY sums.mark_sum ASC";
//        String SQL_B_SID_CNT_P = "SELECT `blocks`.sessionID, blockID, sums.mark_sum " +
//            "FROM `blocks` " +
//            "INNER JOIN (select sessionID, count(DISTINCT blockID) AS mark_sum " +
//            "FROM `blocks` " +
//            "GROUP BY sessionID) AS sums ON sums.sessionID = `blocks`.sessionID " +
//            "WHERE `blocks`.sessionID != '"+selectedNegative+"' " +
//            "GROUP BY sessionID, blockID " +
//            "ORDER BY sums.mark_sum ASC";

        try {
            /**
             * Get a list of positive and negative samples grouped by blockID
             */
            Statement stmtBsidCntN = conn.createStatement();
            ResultSet rsBsidN = stmtBsidCntN.executeQuery(SQL_B_SID_CNT_N);
            Statement stmtBsidCntP = conn.createStatement();
            ResultSet rsBsidP = stmtBsidCntP.executeQuery(SQL_B_SID_CNT_P);

            /**
             * Temporary lists
             */
            LinkedList<String> trainNegativeBlockList = new LinkedList<String>();
            LinkedList<String> trainPositiveBlockList = new LinkedList<String>();
            LinkedList<String> cvNegativeBlockList = new LinkedList<String>();
            LinkedList<String> cvPositiveBlockList = new LinkedList<String>();

            int[] negRandomVec = createRandomizationVector(HALF_CV_DATA_SIZE + HALF_TRAINING_DATA_SIZE, negativeBlocks);
            makeBlockList(rsBsidN, "-1 ", cvNegativeBlockList, trainNegativeBlockList, negRandomVec, HALF_CV_DATA_SIZE);

            int[] posRandomVec = createRandomizationVector((HALF_CV_DATA_SIZE + HALF_TRAINING_DATA_SIZE) * posUserNum , positiveBlocks);
            makeBlockList(rsBsidP, "+1 ", cvPositiveBlockList, trainPositiveBlockList, posRandomVec, HALF_CV_DATA_SIZE * posUserNum);

            shuffleBlockLists(cvBlockList, cvNegativeBlockList, cvPositiveBlockList, HALF_CV_DATA_SIZE);
            shuffleBlockLists(trainBlockList, trainNegativeBlockList, trainPositiveBlockList, HALF_TRAINING_DATA_SIZE);

            //****************************************************************
            //no seperate cv data hack (too few inputs...)
            cvInputName = writeToFile("4cv_" + selectedNegative, trainBlockList);
            //*******************************
            trainInputName = writeToFile("4train_" + selectedNegative, trainBlockList);


        }
        catch (SQLException e){System.out.println("input sql error");}
    }

    public void makeBlockList(ResultSet rsBsid, String label, LinkedList<String> halfCvBlockList, LinkedList<String> halfTrainBlockList, int[] randomVec, int halfDataSize) {
        int blockIt = 0;
        try {
            while (rsBsid.next()) {
                /**
                 * Reset data
                 */
                if (blockIt > BLOCK_IT_LIMIT){
                    break;
                }

                if (Arrays.binarySearch(randomVec, blockIt) >= 0) {
                    String currSessionID = rsBsid.getString("sessionID");
                    String currBlockID = rsBsid.getString("BlockID");

                    String SQL_B_ALL = "SELECT feature FROM `blocks` WHERE SessionID = '" + currSessionID + "' AND BlockID = '" + currBlockID + "' ORDER BY featID ASC";
                    Statement stmtBall = conn.createStatement();
                    ResultSet rsBall = stmtBall.executeQuery(SQL_B_ALL);
                    int rowIt = 0;
                    String blockRow = label;
                    while (rsBall.next()) {
                        if (rsBall.getDouble("feature") > 0) {
                            blockRow = blockRow + rowIt + ":" + rsBall.getDouble("feature") + " ";
                        }
                        else{
                            blockRow = blockRow + rowIt + ":" + rsBall.getDouble("feature") + " ";
                        }
                        rowIt++;
                    }

                    if(halfCvBlockList.size() < halfDataSize) {
                        halfCvBlockList.add(blockRow);
                    }
                    else{
                        halfTrainBlockList.add(blockRow);
                    }
                }
                blockIt++;
            }
        }
        catch(SQLException e){}
    }
}
