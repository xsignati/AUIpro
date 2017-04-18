package PrepareInput;

import Database.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by Flexscan2243 on 21.04.2016.
 */

public class CurveCalculator {
    private static final double MAX_MOVE_INTERVAL = 200 * 1e3; /**< millisecs */
    private static final double MAX_CLICK_INTERVAL = 200 * 1e3;//90000
    private static final int MIN_CURVE_POINTS = 50; //50
    private static final int SAMPLING_RATE = 0;
    private Database db;

    public CurveCalculator(Database db){
        this.db = db;
    }

    public void start() throws SQLException{
        //SessionIDs from Cursor tracks used to iterate through users and
        //to check if they exist in General to avoid duplicates
        String SQL_CT_SID = "SELECT DISTINCT sessionID FROM `mousetracks`";
        String currSessionID;
        ResultSet rsCtsid = db.select(SQL_CT_SID);

        while (rsCtsid.next()) {
            currSessionID = rsCtsid.getString("sessionID");
            String sqlGsid = "SELECT sessionID FROM mousecurves WHERE sessionID = '" + currSessionID + "' LIMIT 1";
            ResultSet rsGsid = db.select(sqlGsid);

            //If sessionID exists in General, skip.
            if (!rsGsid.isBeforeFirst()) {
                //Cursor movements queries used to determine curves
                LinkedList<CursorPoint> curveList = new LinkedList<>(); // list for potential curves
                String sqlCcVals = "SELECT time, action, sessionID, x, y FROM `mousetracks` WHERE sessionID = '" + currSessionID + "' ORDER BY `mousetracks`.`time` ASC";
                ResultSet rsCcVals = db.select(sqlCcVals);

            //Loop of functions that checks if mouse movements contain curves. If so, it sends them to the db
                while (rsCcVals.next()) {
                    checkCursorMovements(rsCcVals, curveList);
                }
                rsCcVals.close();
            }
            rsGsid.close();
        }
        rsCtsid.close();
    }

    public void checkCursorMovements(ResultSet rsCcVals, LinkedList<CursorPoint> curveList ) throws SQLException{
        if(curveList.isEmpty()){
            if(rsCcVals.getString("action").equals("Moved")){
                curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                    rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
            }
        }
        else{
            if(rsCcVals.getString("action").equals("Moved")){
                if((rsCcVals.getDouble("time") - curveList.getLast().getTime()) < MAX_MOVE_INTERVAL){
                    if((rsCcVals.getInt("x") == curveList.getLast().getX()) && (rsCcVals.getInt("y") == curveList.getLast().getY())){
                        curveList.removeLast();
                        curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                            rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                    }
                    else if (sampling(rsCcVals.getInt("x") - curveList.getLast().getX(), rsCcVals.getInt("y") - curveList.getLast().getY())){
                        curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                            rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                    }
                }
                else{
                    curveList.clear();
                    curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                        rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                }
            }
            else{
                if((curveList.size() > MIN_CURVE_POINTS) && ((rsCcVals.getLong("time") - curveList.getLast().getTime()) < MAX_CLICK_INTERVAL)){
                    if((rsCcVals.getInt("x") == curveList.getLast().getX()) && (rsCcVals.getInt("y") == curveList.getLast().getY())){
                        curveList.removeLast();
                        curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                            rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                    }
                    else
                        curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                            rsCcVals.getLong("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));

                    //Send data to db
                    String curveID = UUID.randomUUID().toString();
                    for (CursorPoint cp : curveList) {
                        String sqlCcAll = "INSERT INTO MouseCurves (sessionID, curveID, action, time, x, y) VALUES ( '"+cp.getSessionID()+"', '"+curveID+"', '"+cp.getAction()+"', '"+cp.getTime()+"', '"+cp.getX()+"', '"+cp.getY()+"')";
                        db.insert(sqlCcAll);
                    }
                    curveList.clear();
                }
                else
                    curveList.clear();
            }
        }
    }

    public boolean sampling(int vecX, int vecY){
        return Math.sqrt(Math.pow(vecX,2) + Math.pow(vecY,2)) > SAMPLING_RATE;
    }
}

