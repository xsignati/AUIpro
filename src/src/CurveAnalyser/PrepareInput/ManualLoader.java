package src.CurveAnalyser.PrepareInput;

import src.CurveAnalyser.Database.Database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Flexscan2243 on 10.05.2016.
 */
public class ManualLoader {
    private static final int SKIP_LINES_NUM = 5;
    Database db;

    public ManualLoader(Database db){
        this.db = db;
    }

    public boolean start(String name) throws SQLException, IOException{
        String manSessionID = "None";
        if (name != null && name.length() > 0)
            manSessionID = name.substring(0, name.length()-4);

        String sqlCsid = "SELECT sessionID FROM `mousetracks` WHERE sessionID = '" +manSessionID+ "' LIMIT 1";
        ResultSet rsCsid = db.select(sqlCsid);

        if(!rsCsid.isBeforeFirst()){
            //Insert overall info about user
            String sqlGsid = "INSERT INTO `general` (sessionID) VALUES ('"+manSessionID+"')";
            db.insert(sqlGsid);

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
                        double time = Double.parseDouble(parts[0]) * 1e6;

                        if(time <= timeCorrect)
                            time = timeCorrect + 1000;

                        timeCorrect = time;

                        String sqlCCp = "INSERT INTO MouseTracks (sessionID, action, time, x, y) VALUES ( '"+manSessionID+"', '"+parts[1]+"', '"+time+"', '"+parts[2]+"', '"+parts[3]+"')";
                        db.insert(sqlCCp);
                    }
                    else
                        skipLineIt++;
                }
                in.close();
        }
        return true;
    }
}

