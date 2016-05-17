package CurveAnalyser;

import sun.reflect.annotation.ExceptionProxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Flexscan2243 on 24.04.2016.
 */
public class CalculateBlocks {
    /**
     * Statistical metrics parameters
     */
    private static final int BLOCK_SIZE = 25;
    private static final int AB_MIN = 0;
    private static final int AB_MAX= 360;
    private static final int ABC_MIN = 0;
    private static final int ABC_MAX = 180;
    private static final double BAC_MIN = 0;
    private static final double BAC_MAX = 1;
    private static final int AB_BIN_NUM = 360;
    private static final int ABC_BIN_NUM = 180;
    private static final int BAC_BIN_NUM = 100;
    private static final double AB_BIN_SIZE = 1;
    private static final double ABC_BIN_SIZE = 1;
    private static final double BAC_BIN_SIZE = 0.01;
    private static final int TOTAL_BIN_NUM = AB_BIN_NUM + ABC_BIN_NUM + BAC_BIN_NUM;
    private static final double PDF_PRECISION = 10000.0;

    private int blockIterator;
    private int disposedBinsSum;
    private double[] abInput;
    private double[] abcInput;
    private double[] bacInput;
    private int abSum;
    private int abcSum;
    private int bacSum;
    /**
     * Database variables
     */
    private Connection conn;
    private int p = 0;

    public CalculateBlocks(Connection conn) {
        this.conn = conn;

        /**
         * Block data initialization
         */
        abInput = new double[AB_BIN_NUM + 1];
        abcInput = new double[ABC_BIN_NUM + 1];
        bacInput = new double[BAC_BIN_NUM + 1];
        for(int i = 0; i<= AB_BIN_NUM; i++){
            abInput[i] = 0;
        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            abcInput[i] = 0;
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            bacInput[i] = 0;
        }
        blockIterator = 1;
        disposedBinsSum = 0;
        abSum = 0;
        abcSum = 0;
        bacSum = 0;
    }

    public void startCalculateBlocks2() {
        try {
            /**
             * Get a list of users' sessionIDs
             */
            String currSessionID;
            int currBlockID;
            String SQL_CP_SID = "SELECT DISTINCT sessionID FROM `curveparameters`";
            Statement stmtCpsid = conn.createStatement();
            ResultSet rsCpsid = stmtCpsid.executeQuery(SQL_CP_SID);
            while (rsCpsid.next()) {
                currSessionID = rsCpsid.getString("sessionID");
                currBlockID = 0;

                /**
                 *check if SessionID exists in CurveParameters, if it does get another SessionID
                 */
                String SQL_B_SID = "SELECT sessionID FROM `blocks` WHERE sessionID = '"+currSessionID+"' LIMIT 1";
                Statement stmtBsid = conn.createStatement();
                ResultSet rsBsid = stmtBsid.executeQuery(SQL_B_SID);
                if (!rsBsid.isBeforeFirst()) {
                    /**
                     * Get a list of curves
                     */
                    String SQL_CP_CID = "SELECT DISTINCT curveID FROM `curveparameters` WHERE sessionID = '"+currSessionID+"'";
                    Statement stmtCpcid = conn.createStatement();
                    ResultSet rsCpcid = stmtCpcid.executeQuery(SQL_CP_CID);
                    resetBlockData();
                    while (rsCpcid.next()) {
                        /**
                         *
                         */
                    System.out.println("ax");
                        histograms(rsCpcid);
                        if (blockIterator == BLOCK_SIZE) {
                            //normHist();
                            hist2cdf();
                            /**
                             * save a block in database
                             */
                            createSVMsample();
                            sendBlock(currSessionID, currBlockID);
                            /**
                             * clear all blocks' data
                             */
                            resetBlockData();

                            /**
                             * iterate the current block's ID
                             */
                            currBlockID++;
                        } else {
                            blockIterator++;
                        }
                    }
                }

            }
        }
        catch (SQLException e) {System.out.println("Error1");}

    }

    //test
    public void startCalculateBlocks() {
        try {
            /**
             * Get a list of users' sessionIDs
             */
            String currSessionID;
            int currBlockID;
            String SQL_CP_SID = "SELECT DISTINCT sessionID FROM `curveparameters`";
            Statement stmtCpsid = conn.createStatement();
            ResultSet rsCpsid = stmtCpsid.executeQuery(SQL_CP_SID);
            while (rsCpsid.next()) {
                currSessionID = rsCpsid.getString("sessionID");
                currBlockID = 0;

                /**
                 *check if SessionID exists in CurveParameters, if it does get another SessionID
                 */
                String SQL_B_SID = "SELECT sessionID FROM `blocks` WHERE sessionID = '"+currSessionID+"' LIMIT 1";
                Statement stmtBsid = conn.createStatement();
                ResultSet rsBsid = stmtBsid.executeQuery(SQL_B_SID);
                if (!rsBsid.isBeforeFirst()) {
                    /**
                     * Get a list of curves
                     */

                    String SQL_CP_ALL = "SELECT angleAB, angleABC, ratioBAC, firstPointTime, curveID FROM `curveparameters` WHERE sessionID = '"+currSessionID+"' ORDER BY `curveparameters`.`firstPointTime` ASC";
                    Statement stmtCpall = conn.createStatement();
                    ResultSet rsCpcall = stmtCpall.executeQuery(SQL_CP_ALL);

                    String currCurveID;
                    String prevCurveID = "";
                    resetBlockData();
                    while (rsCpcall.next()) {
                        /**
                         *
                         */
                        currCurveID = rsCpcall.getString("curveID");
                        if(!currCurveID.equals(prevCurveID)) {
                            if (blockIterator == BLOCK_SIZE + 1) {

                                hist2cdf();
                                /**
                                 * save a block in database
                                 */
                                createSVMsample();
                                sendBlock(currSessionID, currBlockID);
                                /**
                                 * clear all blocks' data
                                 */
                                resetBlockData();

                                /**
                                 * iterate the current block's ID
                                 */
                                currBlockID++;
                            }
                            blockIterator++;
                        }
                        histograms(rsCpcall);
                        prevCurveID = currCurveID;
                    }
                }
            }
        }
        catch (SQLException e) {System.out.println("Error111");}

    }
    //test


    public void sendBlock(String currSessionID, int currBlockID){
        try {
            String SQL_B_ALL;
            Statement stmtBall;
            for(int i = 0; i<= AB_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+abInput[i]+"')";
                stmtBall = conn.createStatement();
                stmtBall.executeQuery(SQL_B_ALL);
            }

            for(int i = 0; i<= ABC_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+abcInput[i]+"')";
                stmtBall = conn.createStatement();
                stmtBall.executeQuery(SQL_B_ALL);
            }

            for(int i = 0; i<= BAC_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+bacInput[i]+"')";
                stmtBall = conn.createStatement();
                stmtBall.executeQuery(SQL_B_ALL);
            }
            System.out.println("end");
        }
        catch(Exception e){System.out.println("error");}
    }

    public void createSVMsample(){

        for(int i = 0; i<= AB_BIN_NUM; i++){
            System.out.println("PDF AB: " + "angle: " + i + ": " + abInput[i] + "\n");
        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            System.out.println("PDF ABC: " + "angle: " + i + ": " + abcInput[i] + "\n");
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            System.out.println("PDF BAC: " + "ratio: " + i + ": " + bacInput[i] + "\n");
        }
    }

    public void resetBlockData(){
        abInput = new double[AB_BIN_NUM + 1];
        abcInput = new double[ABC_BIN_NUM + 1];
        bacInput = new double[BAC_BIN_NUM + 1];
        abSum = 0;
        abcSum = 0;
        bacSum = 0;
        blockIterator = 1;
        disposedBinsSum = 0;
    }
    public void histograms(ResultSet rsCpcall){

        try{
            int ABbin = (int) ((rsCpcall.getInt("angleAB") - AB_MIN) / AB_BIN_SIZE);
            int ABCbin = (int) ((rsCpcall.getInt("angleABC") - ABC_MIN) / ABC_BIN_SIZE);
            int BACbin = (int) ((rsCpcall.getDouble("ratioBAC") - BAC_MIN) / BAC_BIN_SIZE);

            if(match2anyBin(ABbin, AB_BIN_NUM)
                    && match2anyBin(ABCbin, ABC_BIN_NUM)
                    && match2anyBin(BACbin, BAC_BIN_NUM)){
                abInput[ABbin]++;
                abcInput[ABCbin]++;
                bacInput[BACbin]++;
            }
            else{
                disposedBinsSum+=3;
            }
        }
        catch(Exception e){System.out.println("Error");}
    }

    public void histograms2(ResultSet rsCpcid){

        try{
            String currCurveID = rsCpcid.getString("curveID");
            String SQL_CP_ALL = "SELECT angleAB, angleABC, ratioBAC, firstPointTime FROM `curveparameters` WHERE curveID = '"+currCurveID+"' ORDER BY `curveparameters`.`firstPointTime` ASC";
            Statement stmtCpall = conn.createStatement();
            ResultSet rsCpcall = stmtCpall.executeQuery(SQL_CP_ALL);

            while(rsCpcall.next()){
                int ABbin = (int) ((rsCpcall.getInt("angleAB") - AB_MIN) / AB_BIN_SIZE);
                int ABCbin = (int) ((rsCpcall.getInt("angleABC") - ABC_MIN) / ABC_BIN_SIZE);
                int BACbin = (int) ((rsCpcall.getDouble("ratioBAC") - BAC_MIN) / BAC_BIN_SIZE);

                if(match2anyBin(ABbin, AB_BIN_NUM)
                        && match2anyBin(ABCbin, ABC_BIN_NUM)
                        && match2anyBin(BACbin, BAC_BIN_NUM)){
                    abInput[ABbin]++;
                    abcInput[ABCbin]++;
                    bacInput[BACbin]++;
                }
                else{
                    disposedBinsSum+=3;
                }
            }
        }
        catch(Exception e){System.out.println("Error");}

    }

    public void normHist(){
        /**
         * Sum data frequencies
         */
        for(int i = 0; i<= AB_BIN_NUM; i++){
            abSum += abInput[i];
        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            abcSum += abcInput[i];
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            bacSum += bacInput[i];
        }

        /**
         * Normalize histograms
         */
        for(int i = 0; i<= AB_BIN_NUM; i++){
            abInput[i] = Math.round((abInput[i]/(abSum)) * PDF_PRECISION)/PDF_PRECISION;

        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            abcInput[i] = Math.round((abcInput[i]/(abcSum)) * PDF_PRECISION)/PDF_PRECISION;
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            bacInput[i] = Math.round((bacInput[i]/((double)bacSum)) * PDF_PRECISION)/PDF_PRECISION;
        }
    }

    public void hist2cdf(){
        /**
         * Sum data frequencies
         */
        for(int i = 0; i<= AB_BIN_NUM; i++){
            abSum += abInput[i];
        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            abcSum += abcInput[i];
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            bacSum += bacInput[i];
        }

        /**
         * Cumulative sum
         */
        double tempSum = 0; /**< a temporary sum for every histogram*/


        for(int i = 0; i<= AB_BIN_NUM; i++){
             tempSum = tempSum + abInput[i];
             abInput[i] = (Math.round((tempSum / abSum) * PDF_PRECISION))/PDF_PRECISION;

         }
        tempSum = 0;

        for(int i = 0; i<= ABC_BIN_NUM; i++){
            tempSum = tempSum + abcInput[i];
            abcInput[i] = (Math.round((tempSum / abcSum) * PDF_PRECISION))/PDF_PRECISION;
        }

        tempSum = 0;

        for(int i = 0; i<= BAC_BIN_NUM; i++){
            tempSum = tempSum + bacInput[i];
            bacInput[i] = (Math.round((tempSum / bacSum) * PDF_PRECISION))/PDF_PRECISION;
        }

    }

    public boolean match2anyBin(int bin, int numBins){
        if (bin < 0 || bin >= numBins) {return false;} /**< data is out of scope, dispose */
        else{return true;}
    }
}


