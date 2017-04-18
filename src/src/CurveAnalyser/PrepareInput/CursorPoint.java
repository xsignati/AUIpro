package src.CurveAnalyser.PrepareInput;

/**
 * Created by Flexscan2243 on 21.04.2016.
 */
public class CursorPoint{
    private String sessionID;
    private String curveID;
    private String action;
    private long time;
    private int x;
    private int y;

    public CursorPoint(String sessionID, String curveID, String action, long time, int x, int y) {
        this.sessionID = sessionID;
        this.curveID = curveID;
        this.action = action;
        this.time = time;
        this.x = x;
        this.y = y;
    }

    public String getCurveID() {
        return curveID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getAction() {
        return action;
    }

    public long getTime() {
        return time;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}