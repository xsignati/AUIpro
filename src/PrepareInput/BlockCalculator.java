package PrepareInput;

import Database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Flexscan2243 on 24.04.2016.
 */
public class BlockCalculator {
    //Statistical metrics parameters
    private static final int BLOCK_SIZE = 25;
    private static final int AB_MIN = 0;
    private static final int AB_BIN_NUM = 36;
    private static final double AB_BIN_SIZE = 10;
    private static final int ABC_MIN = 0;
    private static final int ABC_BIN_NUM = 36;
    private static final double ABC_BIN_SIZE = 5;
    private static final double BAC_MIN = 0;
    private static final int BAC_BIN_NUM = 36;
    private static final double BAC_BIN_SIZE = 0.005;
    private static final double PDF_PRECISION = 1000.0;
    private int blockIterator;
    private double[] abInput;
    private double[] abcInput;
    private double[] bacInput;
    private int abSum;
    private int abcSum;
    private int bacSum;
    private Database db;

    public BlockCalculator(Database db) {
        this.db = db;
        //Block data initialization
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
        abSum = 0;
        abcSum = 0;
        bacSum = 0;
    }

    public void start() throws SQLException{
        //Get the list of users' sessionIDs
        String currSessionID;
        int currBlockID;
        String SQL_CP_SID = "SELECT DISTINCT sessionID FROM `curveparameters`";
        ResultSet rsCpsid = db.select(SQL_CP_SID);
        while (rsCpsid.next()) {
            currSessionID = rsCpsid.getString("sessionID");
            currBlockID = 0;
            //check if SessionID exists in CurveParameters, if it does get another SessionID
            String SQL_B_SID = "SELECT sessionID FROM `blocks` WHERE sessionID = '"+currSessionID+"' LIMIT 1";
            ResultSet rsBsid = db.select(SQL_B_SID);
            if (!rsBsid.isBeforeFirst()) {
                //Get a list of curves
                String SQL_CP_ALL = "SELECT angleAB, angleABC, ratioBAC, firstPointTime, curveID FROM `curveparameters` WHERE sessionID = '"+currSessionID+"' ORDER BY `curveparameters`.`firstPointTime` ASC";
                ResultSet rsCpcall = db.select(SQL_CP_ALL);
                String currCurveID;
                String prevCurveID = "";
                resetBlockData();
                while (rsCpcall.next()) {
                    currCurveID = rsCpcall.getString("curveID");
                    if(!currCurveID.equals(prevCurveID)) {
                        if (blockIterator == BLOCK_SIZE + 1) {
                            //Create CDFs from histograms
                            hist2cdf();
                            //save a block in database
                            sendBlock(currSessionID, currBlockID);
                            //clear all blocks' data
                            resetBlockData();
                            //iterate the current block's ID
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

    public void sendBlock(String currSessionID, int currBlockID) throws SQLException{
            String SQL_B_ALL;
            int featID = 0;
            for(int i = 0; i<= AB_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature, featID) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+abInput[i]+"', '"+featID+"')";
                db.insert(SQL_B_ALL);
                featID++;
            }

            featID = AB_BIN_NUM + 1;
            for(int i = 0; i<= ABC_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature, featID) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+abcInput[i]+"', '"+featID+"')";
                db.insert(SQL_B_ALL);
                featID++;
            }
            featID = AB_BIN_NUM + ABC_BIN_NUM + 2;

            for(int i = 0; i<= BAC_BIN_NUM; i++){
                SQL_B_ALL = "INSERT INTO blocks (sessionID, blockID, feature, featID) VALUES ( '"+currSessionID+"', '"+currBlockID+"', '"+bacInput[i]+"', '"+featID+"')";
                db.insert(SQL_B_ALL);
                featID++;
            }
    }

    public void resetBlockData(){
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
        abSum = 0;
        abcSum = 0;
        bacSum = 0;
        blockIterator = 1;
    }

    public void histograms(ResultSet rsCpcall) throws SQLException{
        int ABbin = (int)((rsCpcall.getInt("angleAB") - AB_MIN) / AB_BIN_SIZE);
        int ABCbin = (int)((rsCpcall.getInt("angleABC") - ABC_MIN) / ABC_BIN_SIZE);
        int BACbin = (int)((rsCpcall.getDouble("ratioBAC") - BAC_MIN) / BAC_BIN_SIZE);

        if(match2anyBin(ABbin, AB_BIN_NUM)
                && match2anyBin(ABCbin, ABC_BIN_NUM)
                && match2anyBin(BACbin, BAC_BIN_NUM)){
            abInput[ABbin]++;
            abcInput[ABCbin]++;
            bacInput[BACbin]++;
        }
    }

    public void hist2cdf(){
        //Sum data frequencies
        for(int i = 0; i<= AB_BIN_NUM; i++){
            abSum += abInput[i];
        }
        for(int i = 0; i<= ABC_BIN_NUM; i++){
            abcSum += abcInput[i];
        }
        for(int i = 0; i<= BAC_BIN_NUM; i++){
            bacSum += bacInput[i];
        }

        //Cumulative sum
        double tempSum = 0; // a temporary sum for every histogram*/

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
        return bin < 0 || bin > numBins;
    }
}


