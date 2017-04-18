package Analyser;

import GUI.Gui;
import SVM.SVM;
import SVM.SVMresult;

/**
 * Created by Xsignati on 18.04.2017.
 */
public class RunParams{
    public Analyser ca;
    public Gui gui;
    public SVMresult.CAmode caMode;
    public SVM svm;
    public String trainOptSel;
    public String testOptSel;
    public String modelName;
    public String manualFileName;

    public RunParams(Analyser ca, Gui gui, SVMresult.CAmode caMode, SVM svm, String trainOptsel, String testOptsel, String modelName, String manualFileName){
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