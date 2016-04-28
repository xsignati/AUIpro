package CurveAnalyser; /**
 * Created by Flexscan2243 on 20.04.2016.
 */

import java.sql.Connection;
import java.sql.DriverManager;

public class CurveAnalyser {
    private String dbName;
    private String serverName;
    private String password;
    private String userName;
    private Connection conn;
    private DetermineCurves dc;
    private CalculateMetrics cm;
    private CalculateInputs cp;

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

            dc = new DetermineCurves(conn);
            dc.startDetermineCurves();
            cm = new CalculateMetrics(conn);
            cm.startCalculateMetrics();
            cp = new CalculateInputs(conn);
            cp.startCalculatePDFs();

            conn.close();
        }
        catch(Exception e){System.out.println("..");}

    }

}
