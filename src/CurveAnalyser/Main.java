package CurveAnalyser;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Flexscan2243 on 20.04.2016.
 */
public class Main {
    public static void main(String[] args){
        CurveAnalyser ca = new CurveAnalyser();
        SVM svm = new SVM();
        JFrame frame = new JFrame("Gui");
        Gui gui = new Gui(ca, svm);
        gui.guiInit();
        frame.setContentPane(gui.getPanel1());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(830, 520));
        frame.setPreferredSize(new Dimension(830, 520));
        frame.setTitle("AUI");
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}