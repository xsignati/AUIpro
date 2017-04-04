package CurveAnalyser; /**
 * Created by Flexscan2243 on 20.04.2016.
 */

import java.sql.*;

public class CurveAnalyser {
    private String dbName;
    private String serverName;
    private String password;
    private String userName;
    private Connection conn;
    private DetermineCurves dc;
    private CalculateMetrics cm;
    private CalculateBlocks cp;
    private TrainSVM tsvm;
    private TestSVM ttsvm;
    private ManualLoader ml;

    public static final String CALC_OK = "All blocks (feature vectors) calculated successfully";
    public static final String TRAIN_OK = "Training completed successfully";
    public static final String TRAIN_E_1 = "Not enough number of negative data samples (related with a selected sessionID";
    public static final String TRAIN_E_2 = "Not enough number of positive data samples (environment testing data)";
    public static final String MANUAL_OK= "All data loaded successfully";
    public static final String MANUAL_ST = "Loading cursor tracks";
    public static final String MANUAL_ERR = "Error loading data";
    public static final String TEST_OK = "Testing completed successfully";
    public static final String TEST_E_1 = "Data is too small. Create at least one block";

    public CurveAnalyser(){
        dbName = "databasec1";
        password = "";
        userName = "root";
        serverName = "localhost";

    }

    public void dbConnect(){
        try {
            conn = DriverManager.getConnection(
                "jdbc:mariadb://" + serverName + "/" + dbName,
                userName,
                password);
        }
        catch(SQLException e){System.out.println("Error connecting database");}
    }
    public void dbDisconnect(){
        try {
            conn.close();
        }
        catch (SQLException e){}
    }

    public void startCurveAnalyser(Subsidiaries.RunParams runParams){
        switch (runParams.caMode) {
            case CALCULATE :
                calculateAllFeatures(runParams.gui);
                break;
            case TRAIN :
                trainSVM(runParams.gui, runParams.svm, runParams.trainOptSel);
                break;
            case TEST :
                testSVM(runParams.gui, runParams.svm, runParams.testOptSel, runParams.modelName);
                break;
            case LOAD:
                manualLoad(runParams.manualFileName, runParams.gui);
                break;
            case DELETE:
                deleteDb(runParams.gui);
                break;
            default :
                break;
        }
    }

    public void calculateAllFeatures(Gui gui){
        try {
            dbConnect();
            conn.setAutoCommit(false); /**< faster inserts*/
            gui.updateBar(25, Gui.S_RED);
            dc = new DetermineCurves(conn);
            dc.startDetermineCurves();
            gui.updateBar(50, Gui.S_RED);
            cm = new CalculateMetrics(conn);
            cm.startCalculateMetrics();
            gui.updateBar(75, Gui.S_RED);
            cp = new CalculateBlocks(conn);
            cp.startCalculateBlocks();
            gui.updateBar(100, Gui.S_GREEN);
            gui.updateTextArea(CALC_OK, Gui.S_GREEN, false);
            conn.commit(); /**< execute all queries */
            conn.setAutoCommit(true);
            dbDisconnect();
        }
        catch(SQLException e){System.out.println("Cannot achieve DB connection");}
    }
    public void trainSVM(Gui gui, SVM svm, String selSessionID){
        gui.updateBar(0, Gui.S_RED);
        gui.updateTextArea("Training started. Selected SessionID: " + selSessionID, Gui.S_WHITE, false);
        dbConnect();
        tsvm = new TrainSVM(conn);
        int tsvmRes = tsvm.startTrainSVM(gui, svm, selSessionID);
        if(tsvmRes == 0){
            gui.updateTextArea(TRAIN_OK, Gui.S_WHITE, true);
            gui.updateBar(100, Gui.S_GREEN);
        }
        else if(tsvmRes == 1){
            gui.updateTextArea(TRAIN_E_1, Gui.S_RED, true);
        }
        else if(tsvmRes == 2){
            gui.updateTextArea(TRAIN_E_2, Gui.S_RED, true);
        }
        dbDisconnect();
    }

    public void testSVM(Gui gui, SVM svm, String selSessionID, String selModelName){
        gui.updateBar(0, Gui.S_RED);
        gui.updateTextArea("Test started. Selected SessionID: " + selSessionID + "\nSelected Model: " + selModelName, Gui.S_WHITE, true);
        dbConnect();
        ttsvm = new TestSVM(conn);
        int ttsvmRes = ttsvm.startTestSVM(gui, svm, selSessionID, selModelName);
        if(ttsvmRes == 0){
            gui.updateBar(100, Gui.S_GREEN);
        }
        else if(ttsvmRes == 1){
            gui.updateTextArea(TRAIN_E_1, Gui.S_RED, false);
        }
        else if(ttsvmRes == 2){
            gui.updateTextArea(TRAIN_E_2, Gui.S_RED, false);
        }
        dbDisconnect();
    }

    public boolean deleteDb(Gui gui){
        try {
            dbConnect();
            gui.updateTextArea("Erasing database", Gui.S_RED, false);
            gui.updateBar(0, Gui.S_RED);
            String sqlKill = "delete FROM `mousetracks`";
            Statement stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(20, Gui.S_RED);
            sqlKill = "delete FROM `curveparameters`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(40, Gui.S_RED);
            sqlKill = "delete FROM `general`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(60, Gui.S_RED);
            sqlKill = "delete FROM `mousecurves`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(80, Gui.S_RED);
            sqlKill = "delete FROM `blocks`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(100, Gui.S_GREEN);
            gui.updateTextArea("Database erased", Gui.S_GREEN, false);
            dbDisconnect();

            return true;
        }
        catch(SQLException e){
            gui.updateTextArea("Database erasing error", Gui.S_RED, false);
            return false;}

    }
    public void manualLoad(String name, Gui gui){
        gui.updateBar(50, Gui.S_RED);
        gui.updateTextArea(MANUAL_ST, Gui.S_GREEN, false);
        dbConnect();
        try {
            conn.setAutoCommit(false);
        }
        catch (SQLException e){}
        ml = new ManualLoader(conn);
        if(ml.startManualLoader(name)) {
            gui.updateBar(100, Gui.S_GREEN);
            gui.updateTextArea(MANUAL_OK, Gui.S_GREEN, false);
        }
        else{
            gui.updateTextArea(MANUAL_ERR, Gui.S_RED, false);
        }
        try {
            conn.setAutoCommit(true);
        }
        catch (SQLException e){}
        dbDisconnect();
        int a = 4;

    }
    public void testSVM(){}

    public Connection getConn() {
        return conn;
    }
}
