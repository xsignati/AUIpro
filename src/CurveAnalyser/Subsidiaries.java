package CurveAnalyser;

/**
 * Created by Flexscan2243 on 30.04.2016.
 */
public class Subsidiaries {
    public enum CAmode{
        CALCULATE, TRAIN, TEST;
    }

    public class GuiRun implements Runnable {
        private CurveAnalyser ca;
        private Gui gui;
        private CAmode caMode;

        public GuiRun(CurveAnalyser ca, Gui gui, CAmode caMode) {
            this.ca = ca;
            this.gui = gui;
            this.caMode = caMode;
        }
        @Override
        public void run() {
            ca.startCurveAnalyser(gui, caMode);
        }
    }

}
