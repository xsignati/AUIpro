package CurveAnalyser;

/**
 * Created by Flexscan2243 on 30.04.2016.
 */
public class Subsidiaries {
    public enum CAmode{
        CALCULATE, TRAIN, TEST
    }

    public class GuiRun implements Runnable {
        private CurveAnalyser ca;
        private Gui gui;
        private CAmode caMode;
        private SVM svm;
        String selSessionID;
        String selModelName;

        public GuiRun(CurveAnalyser ca, Gui gui, CAmode caMode, SVM svm, String selSessionID, String selModelName) {
            this.ca = ca;
            this.gui = gui;
            this.caMode = caMode;
            this.svm = svm;
            this.selSessionID = selSessionID;
            this.selModelName = selModelName;
        }
        @Override
        public void run() {
            ca.startCurveAnalyser(gui, caMode, svm, selSessionID, selModelName);
        }
    }

    public class hyperParameters{
        double c;
        double gamma;

        public hyperParameters(double c, double gamma){
            this.c = c;
            this.gamma = gamma;
        }

        public double getC() {
            return c;
        }

        public void setC(double c) {
            this.c = c;
        }

        public double getGamma() {
            return gamma;
        }

        public void setGamma(double gamma) {
            this.gamma = gamma;
        }
    }

}
