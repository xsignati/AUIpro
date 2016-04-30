package CurveAnalyser; /**
 * Created by Flexscan2243 on 20.04.2016.
 */

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class CurveAnalyser {
    private String dbName;
    private String serverName;
    private String password;
    private String userName;
    private Connection conn;
    private DetermineCurves dc;
    private CalculateMetrics cm;
    private CalculateBlocks cp;

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

    public void startCurveAnalyser(Gui gui, Subsidiaries.CAmode mode){
        switch (mode) {
            case CALCULATE :
                calculateFeatureVectors(gui);
                break;
            case TRAIN :
                trainSVM();
                break;
            case TEST :
                testSVM();
                break;
            default :
                break;
        }
    }

    public void calculateFeatureVectors(Gui gui){
        try {
            System.out.println("aaa");
            dbConnect();

            conn.setAutoCommit(false); /**< much faster for inserts, safe for tables with timestamps */
            gui.updateBar(25, new Color(255,135,135));
            dc = new DetermineCurves(conn);
            dc.startDetermineCurves();
            gui.updateBar(50, new Color(255,135,135));
            cm = new CalculateMetrics(conn);
            cm.startCalculateMetrics();
            gui.updateBar(75, new Color(255,135,135));
            conn.commit(); /**< execute all queries */
            conn.setAutoCommit(true); /**< a safe option for table with no timestamp column */
            cp = new CalculateBlocks(conn);
            cp.startCalculateBlocks();
            gui.updateBar(100, new Color(163,255,135));
            gui.updateTextArea("All blocks (feature vectors) calculated successfully");
            dbDisconnect();
        }
        catch(SQLException e){System.out.println("Cannot achieve db");}
    }
    public void trainSVM(){}
    public void testSVM(){}

    public Connection getConn() {
        return conn;
    }
}
