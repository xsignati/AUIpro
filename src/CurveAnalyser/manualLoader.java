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

    public boolean startManualLoader(String name){
        try {
            String manSessionID = "None";
            if (name != null && name.length() > 0) {
                manSessionID = name.substring(0, name.length()-4);
            }

            String sqlCsid = "SELECT sessionID FROM `mousetracks` WHERE sessionID = '" +manSessionID+ "' LIMIT 1";
            Statement stmtCsid = conn.createStatement();
            ResultSet rsCsid= stmtCsid.executeQuery(sqlCsid);

            if(!rsCsid.isBeforeFirst()){
                String sqlGsid = "INSERT INTO `general` (sessionID) VALUES ('"+manSessionID+"')";
                Statement stmtGsid = conn.createStatement();
                stmtGsid.executeQuery(sqlGsid);

                try {
                    BufferedReader in = new BufferedReader(new FileReader(name));

                    int skipLineIt = 0;
                    double timeCorrect = 0;
                    String line;
                    while ((line = in.readLine()) != null) {
                        if(skipLineIt > SKIP_LINES_NUM) {
                            String[] parts = line.split("\t");

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
                catch (IOException e){System.out.println("IOexception"); return false;}

            }
            return true;
        }
        catch (SQLException e){ System.out.println("SQLexception"); return false;}


    }
}
