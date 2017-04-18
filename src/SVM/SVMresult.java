package SVM;

/**
 * Created by Flexscan2243 on 30.04.2016.
 */
public class SVMresult{
    public enum CAmode{
        CALCULATE, TRAIN, TEST, LOAD, DELETE
    }

    double acc;
    int verified;
    int total;

    public SVMresult(double acc, int verified, int total) {
        this.acc = acc;
        this.verified = verified;
        this.total = total;
    }

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }

    public double getVerified() {
        return verified;
    }

    public void setVerified(int verified) {
        this.verified = verified;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}


