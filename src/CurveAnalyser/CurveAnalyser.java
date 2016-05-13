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
    private ManualLoader ml;

    public static final String CALC_OK = "All blocks (feature vectors) calculated successfully";
    public static final String TRAIN_OK = "Training completed successfully";
    public static final String TRAIN_E_1 = "Not enough number of negative data samples (related with a selected sessionID";
    public static final String TRAIN_E_2 = "Not enough number of positive data samples (environment testing data)";
    public static final String MANUAL_OK= "All data loaded successfully";
    public static final String MANUAL_ERR = "Error loading data";

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

    public void startCurveAnalyser(Gui gui, Subsidiaries.CAmode mode, SVM svm){
        switch (mode) {
            case CALCULATE :
                calculateAllFeatures(gui);
                break;
            case TRAIN :
                trainSVM(gui, svm);
                break;
            case TEST :
                testSVM();
                break;
            default :
                break;
        }
    }

    public void calculateAllFeatures(Gui gui){
        try {
            System.out.println("aaa");
            dbConnect();

            conn.setAutoCommit(false); /**< much faster for inserts, safe for tables with timestamps */
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
            conn.setAutoCommit(true); /**< a safe option for table with no timestamp column */
            dbDisconnect();
        }
        catch(SQLException e){System.out.println("Cannot achieve db");}
    }
    public void trainSVM(Gui gui, SVM svm){
        dbConnect();
        tsvm = new TrainSVM(conn);
        int tsvmRes = tsvm.startTrainSVM(gui, svm);
        if(tsvmRes == 0){
            gui.updateTextArea(TRAIN_OK, Gui.S_GREEN, false);
        }
        else if(tsvmRes == 1){
            gui.updateTextArea(TRAIN_E_1, Gui.S_RED, false);
        }
        else if(tsvmRes == 2){
            gui.updateTextArea(TRAIN_E_2, Gui.S_RED, false);
        }
        dbDisconnect();
    }

    public boolean deleteDb(){
        try {
            String sqlKill = "delete FROM `mousetracks`";
            Statement stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            sqlKill = "delete FROM `curveparameters`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            sqlKill = "delete FROM `general`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            sqlKill = "delete FROM `mousecurves`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);
            sqlKill = "delete FROM `blocks`";
            stKill = conn.createStatement();
            stKill.executeQuery(sqlKill);

            return true;
        }
        catch(SQLException e){return false;}

    }
    public void manualLoad(String name, Gui gui){
        gui.updateBar(50, Gui.S_RED);
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

    }
    public void testSVM(){}

    public Connection getConn() {
        return conn;
    }
}
