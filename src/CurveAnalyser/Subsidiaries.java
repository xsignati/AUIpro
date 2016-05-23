package CurveAnalyser;

/**
 * Created by Flexscan2243 on 30.04.2016.
 */
public class Subsidiaries {
    public enum CAmode{
        CALCULATE, TRAIN, TEST, LOAD, DELETE
    }

    public class GuiRun implements Runnable {
        private RunParams runParams;

        public GuiRun(RunParams runParams) {
            this.runParams = runParams;
        }
        @Override
        public void run() {
            runParams.ca.startCurveAnalyser(runParams);
        }
    }

//    public class GuiRun implements Runnable {
//        private CurveAnalyser ca;
//        private Gui gui;
//        private CAmode caMode;
//        private SVM svm;
//        String selSessionID;
//        String selModelName;
//
//        public GuiRun(CurveAnalyser ca, Gui gui, CAmode caMode, SVM svm, String selSessionID, String selModelName) {
//            this.ca = ca;
//            this.gui = gui;
//            this.caMode = caMode;
//            this.svm = svm;
//            this.selSessionID = selSessionID;
//            this.selModelName = selModelName;
//        }
//        @Override
//        public void run() {
//            ca.startCurveAnalyser(gui, caMode, svm, selSessionID, selModelName);
//        }
//    }

    public class RunParams{
        public CurveAnalyser ca;
        public Gui gui;
        public Subsidiaries.CAmode caMode;
        public SVM svm;
        public String trainOptSel;
        public String testOptSel;
        public String modelName;
        public String manualFileName;

        public RunParams(CurveAnalyser ca, Gui gui, Subsidiaries.CAmode caMode, SVM svm, String trainOptsel, String testOptsel, String modelName, String manualFileName){
            this.ca = ca;
            this.gui = gui;
            this.caMode= caMode;
            this.svm = svm;
            this.trainOptSel = trainOptsel;
            this.testOptSel = testOptsel;
            this.modelName = modelName;
            this.manualFileName = manualFileName;
        }

    }

    public class hyperParameters{
        double c;
        double gamma;
        double acc;

        public hyperParameters(double c, double gamma, double acc){
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

    public class SVMresult{
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

}
