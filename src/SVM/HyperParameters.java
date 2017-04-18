package SVM;

/**
 * Created by Xsignati on 18.04.2017.
 */
public class HyperParameters {
    double c;
    double gamma;
    double acc;

    public HyperParameters(double c, double gamma, double acc){
        this.c = c;
        this.gamma = gamma;
        this.acc = acc;
    }

    public double getC() {return c;}

    public void setC(double c) {
        this.c = c;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }
}