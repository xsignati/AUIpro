package CurveAnalyser; /**
 * Created by Flexscan2243 on 20.04.2016.
 */

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

            conn.setAutoCommit(false); /**< much faster for inserts, safe for tables with timestamps */
            dc = new DetermineCurves(conn);
            dc.startDetermineCurves();
            cm = new CalculateMetrics(conn);
            cm.startCalculateMetrics();
            conn.commit(); /**< execute all queries */
            conn.setAutoCommit(true); /**< a safe option for table with no timestamp column */
            cp = new CalculateBlocks(conn);
            cp.startCalculateBlocks();

            conn.close();
        }
        catch(SQLException e){System.out.println("..");}

    }

}
