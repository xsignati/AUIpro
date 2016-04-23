package CurveAnalyser;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedList;

/**
 * Created by Flexscan2243 on 22.04.2016.
 */
public class CalculateMetrics {
    private static final int LIST_MIDDLE = 1;
    /**
     * Database variables
     */
    private Connection conn;

    public CalculateMetrics(Connection conn){
        this.conn = conn;
    }

    public void startCalculateMetrics(){
        try {
            //SQL
            String SQL_CC_SID = "SELECT DISTINCT sessionID FROM `mousecurves`";
            String currSessionID;
            Statement stmtCcsid = conn.createStatement();
            ResultSet rsCcsid = stmtCcsid.executeQuery(SQL_CC_SID);
            while (rsCcsid.next()) {
                currSessionID = rsCcsid.getString("sessionID");

                /**
                 *check if SessionID exists in CurveParameters, if it does get another SessionID
                 */
                String SQL_CP_SID = "SELECT sessionID FROM `curveparameters` WHERE sessionID = '"+currSessionID+"' LIMIT 1";
                Statement stmtCpsid = conn.createStatement();
                ResultSet rsCpsid = stmtCpsid.executeQuery(SQL_CP_SID);

                if (!rsCpsid.isBeforeFirst()) {
                    /**
                     * Get a single curve
                     */
                    String SQL_CC_CID = "SELECT DISTINCT curveID FROM `mousecurves` WHERE sessionID = '"+currSessionID+"'";
                    Statement stmtCccid = conn.createStatement();
                    ResultSet rsCccid = stmtCccid.executeQuery(SQL_CC_CID);

                    while (rsCccid.next()) {
                        /**
                        * Prepare a temporary container for angle based metrics
                         */
                        String currCurveID = rsCccid.getString("curveID");
                        LinkedList<CursorPoint> curveList = new LinkedList<CursorPoint>(); /**< list for potentional curves */
                        String SQL_CC_ALL = "SELECT x, y, sessionID, curveID, action, time FROM `mousecurves` WHERE curveID = '"+currCurveID+"' ORDER BY `mousecurves`.`time` ASC";
                        Statement stmtCcall= conn.createStatement();
                        ResultSet rsCccall = stmtCcall.executeQuery(SQL_CC_ALL);
                        while (rsCccall.next()) {

                            calculateFeatures(curveList, rsCccall);
                        }
                    }
                }
            }
        }
        catch(Exception e){}
    }
    public void calculateFeatures(LinkedList<CursorPoint> curveList, ResultSet rsCccall){
        try {
            if (curveList.size() == 2) {
                curveList.addLast(new CursorPoint(rsCccall.getString("sessionID"), rsCccall.getString("curveID"), rsCccall.getString("action"),
                        rsCccall.getFloat("time"), rsCccall.getInt("x"), rsCccall.getInt("y")));
                Point pointA = new Point(curveList.getFirst().getX(), curveList.getFirst().getY());
                Point pointB = new Point(curveList.get(LIST_MIDDLE).getX(), curveList.get(LIST_MIDDLE).getY());
                Point pointC = new Point(curveList.getLast().getX(), curveList.getLast().getY());
                double abAngle = abAngle(pointA, pointB);
                double abcAngle = abcAngle(pointA, pointB, pointC);
                double bacRatio = bACratio(pointA, pointB, pointC);

                /**
                 * Send angle based metrics to database
                 */
                String sqlCpAll = "INSERT INTO curveParameters (sessionID, curveID, angleAB, angleABC, ratioBAC, firstPointTime) VALUES ( '"+rsCccall.getString("sessionID")+"', '"+rsCccall.getString("curveID")+"', '"+abAngle+"', '"+abcAngle+"', '"+bacRatio+"', '"+rsCccall.getFloat("time")+"')";
                Statement stmtCpAll = conn.createStatement();
                ResultSet rsCpAll = stmtCpAll.executeQuery(sqlCpAll);

                /**
                 * Remove the first one point of all three. New point will be added to compute new metrics
                 */
                curveList.removeFirst();
            } else {
                curveList.addLast(new CursorPoint(rsCccall.getString("sessionID"), rsCccall.getString("curveID"), rsCccall.getString("action"),
                        rsCccall.getFloat("time"), rsCccall.getInt("x"), rsCccall.getInt("y")));
            }
        }
        catch(Exception e){}
    }

    public double abcAngle(Point pointA, Point pointB, Point pointC){
        /**
         * Set down AB and CB vectors
         */
        Point vectorAB = new Point(pointB.getX() - pointA.getX(),pointB.getY() - pointA.getY());
        Point vectorCB = new Point(pointB.getX()- pointC.getX(),pointB.getY() - pointC.getY());

        /**
         * Count dot product and pseudo cross product
         */
        double dot = (vectorAB.getX() * vectorCB.getX() + vectorAB.getY() * vectorCB.getY());
        double cross = (vectorAB.getX() * vectorCB.getY() - vectorAB.getY() * vectorCB.getX());

        /**
         * count angle
         */
        double angle = Math.atan2(cross, dot);
        angle = angle > 0 ? angle : angle * (-1);

        return Math.floor(angle * 180 / Math.PI + 0.5);
    }

    public double abAngle(Point pointA, Point pointB){
        /**
         *Set down AB vector
         */
        Point vectorAB = new Point(pointB.getX() - pointA.getX(),pointB.getY() - pointA.getY());

        /**
         *count angle AB-horizontal
         */
        double angle = Math.atan2(vectorAB.getY(), vectorAB.getX());

        /**
         *$angle > 0 ? $angle : $angle += pi();
         */
        return Math.floor(angle * 180 / Math.PI + 0.5);
    }

    public double bACratio(Point pointA, Point pointB, Point pointC){
        /**
         *Set down AB vector
         */
        Point vectorAB = new Point(pointB.getX() - pointA.getX(), pointB.getY() - pointA.getY());
        Point vectorAC = new Point(pointC.getX() - pointA.getX(),pointC.getY() - pointA.getY());

        /**
         * Count dot product and pseudo cross product
         */
        double cross = (vectorAB.getX() * vectorAC.getY() - vectorAB.getY() * vectorAC.getX());

        /**
         * count vAC module
         */
        double vectorACmod = Math.sqrt(Math.pow(vectorAC.getX(),2) + Math.pow(vectorAC.getY(),2));
        if(vectorACmod == 0){
            return (Math.sqrt(Math.pow(vectorAB.getX(),2) + Math.pow(vectorAB.getY(),2)));
        }
        else{
            double bACdist = Math.abs(cross)/vectorACmod;
            return bACdist/vectorACmod;
        }
    }
}
class Point{
    private int x;
    private int y;

    public Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}