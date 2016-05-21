package CurveAnalyser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by Flexscan2243 on 17.05.2016.
 */
public class TestSVM {
    private Connection conn;
    private String inputName;
    private LinkedList<String> neutralBlockList;


    public TestSVM(Connection conn) {
        //sql
        this.conn = conn;
    }

    public int startTestSVM(Gui gui, SVM svm, String selSessionID, String selModelName){
        try {
            /**
             * Get sessionIDs selected in GUI (temporary solution)
             */
            String selectedNeutral = selSessionID;
            String modelName = selModelName;

            /**
             * Count negative ones
             */
            String SQL_B_SUM2 = "SELECT COUNT(DISTINCT blockID) AS c FROM `blocks` WHERE sessionID = '"+selectedNeutral+"'";
            Statement stmtBsum2 = conn.createStatement();
            ResultSet rsBsum2 = stmtBsum2.executeQuery(SQL_B_SUM2);
            rsBsum2.next();

            /**
             * Return if the data is too small
             */
            int negativeBlocks = rsBsum2.getInt("c");
            if(negativeBlocks < 1){return 1;}

            /**
             * Training
             */
            createSvmInput(selectedNeutral);

            /**
             * test SVM
             */
            writeToFile(selectedNeutral);

            try {
                double[] prob_estimates = new double[2];
                String output = "modelName_" + "result.txt";
                String[] argv = {inputName, modelName, output};
                svm.runPredict(argv);
            }
            catch (IOException e){System.out.println("SVM START ERROR");}


        }
        catch (SQLException e){System.out.println("ERROR");}


        return 0;
    }

    public void createSvmInput(String selectedNeutral){
        try {
            /**
             * Fetch all blockIDs specific to the current sessionID
             */
            String SQL_B_BID = "SELECT DISTINCT blockID FROM `blocks` WHERE sessionID = '" + selectedNeutral + "'";
            Statement stmtBbid = conn.createStatement();
            ResultSet rsBbid = stmtBbid.executeQuery(SQL_B_BID);
            String currBlockID;
            neutralBlockList = new LinkedList<String>();

            int blockIt = 0;

            while (rsBbid.next()) {
                /**
                 * reset data
                 */
                if(blockIt > 99999999){break;}
                String blockRow = "-1 ";
                int rowIt = 0;
                /**
                 * Get all features needed to create a one feature vector (one SVM sample)
                 */
                currBlockID = rsBbid.getString("BlockID");

                String SQL_B_ALL = "SELECT feature FROM `blocks` WHERE SessionID = '" + selectedNeutral + "' AND BlockID = '" + currBlockID + "'";
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
                neutralBlockList.add(blockRow);

                }
                blockIt++;
        }
        catch (SQLException e){}
    }

    public void writeToFile(String selectedNeutral){
        File f = new File("test_" + selectedNeutral + ".txt");
        int fIt = 1;
        if(f.exists() && !f.isDirectory()) {
            while(f.exists() && !f.isDirectory()) {
                f = new File("test_" + selectedNeutral + " (" + fIt + ")" + ".txt");
                inputName = "test_" + selectedNeutral + " (" + fIt + ")" + ".txt";
                fIt++;
            }
        }
        else{inputName = "test_" + selectedNeutral + ".txt";}

        FileWriter fr;
        String newLine = System.getProperty("line.separator");
        try {
            fr = new FileWriter(f);
            BufferedWriter br  = new BufferedWriter(fr);

            for (int i = 0 ; i < neutralBlockList.size() ; i++){
                br.write(neutralBlockList.get(i) + newLine);
            }
            fr.close();
        }
        catch (IOException e){}
    }

}