package Analyser; /**
 * Created by Flexscan2243 on 20.04.2016.
 */

import Database.Database;
import GUI.Gui;
import PrepareInput.MetricCalculator;
import PrepareInput.BlockCalculator;
import PrepareInput.CurveCalculator;
import PrepareInput.ManualLoader;
import SVM.SVM;
import SVM.SVMtester;
import SVM.SVMtrainer;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class Analyser {
    private Database db = new Database();

    public static final String CALC_OK = "All blocks (feature vectors) calculated successfully";
    public static final String TRAIN_OK = "Training completed successfully";
    public static final String TRAIN_E_1 = "Not enough number of negative data samples (related with a selected sessionID";
    public static final String TRAIN_E_2 = "Not enough number of positive data samples (environment testing data)";
    public static final String MANUAL_OK= "All data loaded successfully";
    public static final String MANUAL_ST = "Loading cursor tracks";
    public static final String MANUAL_ERR = "Error loading data";

    public void start(RunParams runParams){
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
            db.connect();
            db.getConn().setAutoCommit(false);
            gui.updateBar(25, Gui.S_RED);
            CurveCalculator cc = new CurveCalculator(db);
            cc.start();
            gui.updateBar(50, Gui.S_RED);
            MetricCalculator mc = new MetricCalculator(db);
            mc.start();
            gui.updateBar(75, Gui.S_RED);
            BlockCalculator cp = new BlockCalculator(db);
            cp.start();
            gui.updateBar(100, Gui.S_GREEN);
            gui.updateTextArea(CALC_OK, Gui.S_GREEN, false);
            db.getConn().commit();
            db.getConn().setAutoCommit(true);
            db.disconnect();
        }
        catch(SQLException e){e.printStackTrace();}
    }
    public void trainSVM(Gui gui, SVM svm, String selSessionID){
        gui.updateBar(0, Gui.S_RED);
        gui.updateTextArea("Training started. Selected SessionID: " + selSessionID, Gui.S_WHITE, false);
        db.connect();
        SVMtrainer st = new SVMtrainer(db);
        try {
            int tsvmRes = st.start(gui, svm, selSessionID);
            if (tsvmRes == 0) {
                gui.updateTextArea(TRAIN_OK, Gui.S_WHITE, true);
                gui.updateBar(100, Gui.S_GREEN);
            } else if (tsvmRes == 1) {
                gui.updateTextArea(TRAIN_E_1, Gui.S_RED, true);
            } else if (tsvmRes == 2) {
                gui.updateTextArea(TRAIN_E_2, Gui.S_RED, true);
            }
        }
        catch(SQLException | IOException e){
            e.printStackTrace();
        }
        db.disconnect();
    }

    public void testSVM(Gui gui, SVM svm, String selSessionID, String selModelName){
        gui.updateBar(0, Gui.S_RED);
        gui.updateTextArea("Test started. Selected SessionID: " + selSessionID + "\nSelected Model: " + selModelName, Gui.S_WHITE, true);
        db.connect();
        SVMtester ste = new SVMtester(db);

        try {
            int ttsvmRes = ste.start(gui, svm, selSessionID, selModelName);

            if(ttsvmRes == 0){
                gui.updateBar(100, Gui.S_GREEN);
            }
            else if(ttsvmRes == 1){
                gui.updateTextArea(TRAIN_E_1, Gui.S_RED, false);
            }
            else if(ttsvmRes == 2){
                gui.updateTextArea(TRAIN_E_2, Gui.S_RED, false);
            }
        }
        catch(SQLException | IOException e){
            e.printStackTrace();
        }
        db.disconnect();
    }

    public boolean deleteDb(Gui gui){
        try {
            db.connect();
            gui.updateTextArea("Erasing database", Gui.S_RED, false);
            gui.updateBar(0, Gui.S_RED);
            String sqlKill = "delete FROM `mousetracks`";
            Statement stKill = db.getConn().createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(20, Gui.S_RED);
            sqlKill = "delete FROM `curveparameters`";
            stKill = db.getConn().createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(40, Gui.S_RED);
            sqlKill = "delete FROM `general`";
            stKill = db.getConn().createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(60, Gui.S_RED);
            sqlKill = "delete FROM `mousecurves`";
            stKill = db.getConn().createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(80, Gui.S_RED);
            sqlKill = "delete FROM `blocks`";
            stKill = db.getConn().createStatement();
            stKill.executeQuery(sqlKill);
            gui.updateBar(100, Gui.S_GREEN);
            gui.updateTextArea("Database erased", Gui.S_GREEN, false);
            db.disconnect();

            return true;
        }
        catch(SQLException e){
            gui.updateTextArea("Database erasing error", Gui.S_RED, false);
            return false;}

    }
    public void manualLoad(String name, Gui gui) {
        gui.updateBar(50, Gui.S_RED);
        gui.updateTextArea(MANUAL_ST, Gui.S_GREEN, false);
        db.connect();
        try {
            db.getConn().setAutoCommit(false);

            ManualLoader ml = new ManualLoader(db);
            if (ml.start(name)) {
                gui.updateBar(100, Gui.S_GREEN);
                gui.updateTextArea(MANUAL_OK, Gui.S_GREEN, false);
            } else {
                gui.updateTextArea(MANUAL_ERR, Gui.S_RED, false);
            }
            db.getConn().setAutoCommit(true);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        db.disconnect();
    }

    public Database getDb() {
        return db;
    }
}
