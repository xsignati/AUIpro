package CurveAnalyser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Flexscan2243 on 10.05.2016.
 */
public class ManualLoader {
    Connection conn;
    private static final int SKIP_LINES_NUM = 5;

    public ManualLoader(Connection conn){
        this.conn = conn;

    }

    public void startManualLoader(String name){
        try {
            String manSessionID = "None";
            if (name != null && name.length() > 0) {
                manSessionID = name.substring(0, name.length()-4);
            }

            String sqlCsid = "SELECT * FROM `mousetracks` WHERE sessionID LIKE '%" +manSessionID+ "%' LIMIT 1";
            Statement stmtCsid = conn.createStatement();
            ResultSet rsCsid= stmtCsid.executeQuery(sqlCsid);

            if(!rsCsid.isBeforeFirst()){
                String sqlGsid = "INSERT INTO `general` VALUES ('"+manSessionID+"')";
                Statement stmtGsid = conn.createStatement();
                stmtCsid.executeQuery(sqlGsid);

                try {
                    BufferedReader in = new BufferedReader(new FileReader(name));

                    int skipLineIt = 0;
                    double timeCorrect = 0;
                    String line;
                    while ((line = in.readLine()) != null) {
                        if(skipLineIt > SKIP_LINES_NUM) {
                            String[] parts = line.split(" ");

                            if(parts[1].equals("Pressed Left"))
                                parts[1] = "Clicked";
                            parts[0] = parts[0].replace(",", ".");
                            double time = Double.parseDouble(parts[0]) * 1000;

                            if(time <= timeCorrect){
                                time = timeCorrect + 1;
                            }
                            timeCorrect = time;

                            String sqlCCp = "INSERT INTO MouseTracks (sessionID, action, time, x, y) VALUES ( '"+manSessionID+"', '"+parts[1]+"', '"+time+"', '"+parts[2]+"', '"+parts[3]+"')";
                            Statement stmtSqlCcp = conn.createStatement();
                            stmtSqlCcp.executeQuery(sqlCCp);


                        }
                        else{
                            skipLineIt++;
                        }
                    }
                    in.close();
                }
                catch (IOException e){}

            }
        }
        catch (SQLException e){}


    }
}
