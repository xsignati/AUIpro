package CurveAnalyser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Flexscan2243 on 21.04.2016.
 */

public class DetermineCurves {
    /**
    * CurveAnalyser.DetermineCurves parameters
     */
    private static final int MAX_MOVE_INTERVAL = 500; /**< milisec */
    private static final int MAX_CLICK_INTERVAL = 2500;
    private static final int MIN_CURVE_POINTS = 10;
    private int curveIDit;
    private int curveIDlimit;

    /**
    * Database variables
     */
    private Connection conn;

    public DetermineCurves(Connection conn){
        this.conn = conn;
    }

    public void startDetermineCurves(){
        /**
        * SessionIDs from Cursor tracks used to iterate through users and
        * to check if they exist in General to avoid duplicates
         */
        try {
            String SQL_CT_SID = "SELECT DISTINCT sessionID FROM `mousetracks`";
            String currSessionID;
            Statement stmtCtsid = conn.createStatement();
            ResultSet rsCtsid = stmtCtsid.executeQuery(SQL_CT_SID);

            /**
             * Main loop.
             */
            while (rsCtsid.next()) {
                currSessionID = rsCtsid.getString("sessionID");
                String sqlGsid = "SELECT sessionID FROM mousecurves WHERE sessionID = '" + currSessionID + "' LIMIT 1";
                Statement stmtGsid = conn.createStatement();
                ResultSet rsGsid = stmtGsid.executeQuery(sqlGsid);

                /**
                 * If sessionID exists in General, skip.
                 */
                if (!rsGsid.isBeforeFirst()) {
                    /**
                     *  Cursor movements queries used to determine curves
                     */
                    LinkedList<CursorPoint> curveList = new LinkedList<CursorPoint>(); /**< list for potentional curves */
                    String sqlCcVals = "SELECT time, action, sessionID, x, y FROM `mousetracks` WHERE sessionID = '" + currSessionID + "' ORDER BY `mousetracks`.`time` ASC";
                    Statement stmtCcVals = conn.createStatement();
                    ResultSet rsCcVals = stmtCcVals.executeQuery(sqlCcVals);
                    stmtCcVals.close();

                    /**
                     * Loop of functions that check if mouse movements contain curves and if so send them to db
                     */
                    while (rsCcVals.next()) {
                            checkCursorMovements(rsCcVals, curveList);
                    }
                    rsCcVals.close();
                }
                stmtGsid.close();
                rsGsid.close();
            }
            stmtCtsid.close();
            rsCtsid.close();
        }
        catch (Exception e){}
    }

    public void checkCursorMovements(ResultSet rsCcVals, LinkedList<CursorPoint> curveList ){
        try{
            if(curveList.isEmpty()){
                if(rsCcVals.getString("action").equals("Moved")){
                    curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                        rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                }
            }
            else{
                if(rsCcVals.getString("action").equals("Moved")){
                    if((rsCcVals.getFloat("time") - curveList.getLast().getTime()) < MAX_MOVE_INTERVAL){
                        if((rsCcVals.getInt("x") == curveList.getLast().getX()) && (rsCcVals.getInt("y") == curveList.getLast().getY())){
                            curveList.removeLast();
                            curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                                rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                        }
                        else{
                            curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                                rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                        }
                    }
                    else{
                        curveList.clear();
                        curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                            rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                    }
                }
                else{
                    if((curveList.size() > MIN_CURVE_POINTS) && ((rsCcVals.getFloat("time") - curveList.getLast().getTime()) < MAX_CLICK_INTERVAL)){
                        if((rsCcVals.getInt("x") == curveList.getLast().getX()) && (rsCcVals.getInt("y") == curveList.getLast().getY())){
                            curveList.removeLast();
                            curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                                rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                        }
                        else{
                            curveList.addLast(new CursorPoint(rsCcVals.getString("sessionID"), "none", rsCcVals.getString("action"),
                                                rsCcVals.getFloat("time"), rsCcVals.getInt("x"), rsCcVals.getInt("y")));
                        }
                        /**
                         * send curves to database
                         */
                        String curveID = UUID.randomUUID().toString();
                        Statement stmtCcAll;
                        ResultSet rsCcAll;
                        for (CursorPoint cp : curveList) {
                            String sqlCcAll = "INSERT INTO MouseCurves (sessionID, curveID, action, time, x, y) VALUES ( '"+cp.getSessionID()+"', '"+curveID+"', '"+cp.getAction()+"', '"+cp.getTime()+"', '"+cp.getX()+"', '"+cp.getY()+"')";
                            stmtCcAll = conn.createStatement();
                            rsCcAll = stmtCcAll.executeQuery(sqlCcAll);
                        }
                        curveList.clear();
                    }
                    else{
                        curveList.clear();
                    }
                }
            }
        }
        catch(Exception e){}
    }

}

