package CurveAnalyser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;

/**
 * Created by Flexscan2243 on 28.04.2016.
 */
public class Gui{
    private CurveAnalyser ca;
    private SVM svm;
    private JPanel panel1;
    private JButton calculateBlocksButton;
    private JButton trainSVMButton;
    private JList list1;
    private DefaultListModel model1;
    private String trainOptSel;
    private DefaultListModel model2;
    private String testOptSel;
    private String modelName;
    private JButton testSVMButton;
    private JProgressBar progressBar1;
    private JTextArea textArea1;
    private JList list2;
    private JButton button1;
    private JButton refreshButton;
    private JButton manualCMLoaderButton;
    private JButton clearDatabaseButton;
    private JButton loadModelButton;
    private JTextArea modelTitle;
    private JLabel title;
    private JScrollPane modelPane;
    private JScrollPane testPane;
    private JScrollPane trainPane;
    private JScrollPane logPane;
    private Gui gui;
    private Subsidiaries.CAmode caMode;

    private Runnable[] guiRunners;
    private Thread[] guiThreads;

    public static final Color S_RED = new Color(255,135,135);
    public static final Color S_WHITE = new Color(235,235,235);
    public static final Color S_GREEN = new Color(163,255,135);
    public static final String INFO = "AUI 2016 internet users analyser." + "\n" + "\n"
            + "Calculate blocks: Determine curves within mouse movements," + "\n"
            + "angle based metrics and SVM feature vectors (blocks) " + "\n" + "\n"
            + "Train SVM: train the Support Vector Machine with a" + "\n"
            + "chosen user's metric and all the other users signatures " + "\n" + "\n"
            + "Test SVM: test if a chosen sample belongs to the user";
    public static final String TRAIN_STR = "Training started...";
    public static final String SELECT_SID = "Select one SessionID!";
    public static final String ANOTHER_PR = "Another process is working. Wait until it finishes!";
    public static final String CALC = "Calculating...";
    public static final String TEST_STR = "Testing started...";
    public static final String SEL_SID_AND_NAME = "Select SessionID and load SVM model!";



    public String getTrainOptSel() {
        return trainOptSel;
    }

    public Gui(CurveAnalyser ca, SVM svm) {
        this.ca = ca;
        this.gui = this;
        this.svm = svm;

        /**
         * Threads
         */
        guiRunners = new Runnable[3];
        guiThreads = new Thread[3];

        Subsidiaries.CAmode [] tabMod = {caMode.CALCULATE, caMode.TRAIN, caMode.TEST};
        for (int i = 0 ; i < 3 ; i++) {
            guiRunners[i] = new Subsidiaries().new GuiRun(ca, gui, tabMod[i], svm, "", "");
            guiThreads[i] = new Thread(guiRunners[i]);
        }

        /**
         * SVM data manipulation buttons
         */
        calculateBlocksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(processWorking()){
                    updateTextArea(ANOTHER_PR, S_RED, false);
                }
                else{
                    updateTextArea(CALC, S_GREEN, false);
                    guiThreads[0] = new Thread(guiRunners[0]);
                    guiThreads[0].start();

                }
            }
        });

        trainSVMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if(processWorking()){
                    updateTextArea(ANOTHER_PR, S_RED, false);
                }
                else if(trainOptSel.isEmpty()){
                    updateTextArea(SELECT_SID, S_RED, false);

                }
                else{
                    updateTextArea(TRAIN_STR, S_GREEN, false);
                    guiRunners[1] =  new Subsidiaries().new GuiRun(ca, gui, tabMod[1], svm, trainOptSel, "");
                    guiThreads[1] = new Thread(guiRunners[1]);
                    guiThreads[1].start();

                }

            }
        });

        testSVMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(processWorking()){
                    updateTextArea(ANOTHER_PR, S_RED, false);
                }
                else if(testOptSel.isEmpty() || modelName.isEmpty()){
                    updateTextArea(SEL_SID_AND_NAME, S_RED, false);

                }
                else{
                    updateTextArea(TEST_STR, S_GREEN, false);
                    guiRunners[2] =  new Subsidiaries().new GuiRun(ca, gui, tabMod[2], svm, testOptSel, modelName);
                    guiThreads[2] = new Thread(guiRunners[2]);
                    guiThreads[2].start();

                }
            }
        });

        /**
         * Right menu buttons. SVM buttons lists for selected options
         */
        model1 = new DefaultListModel();
        list1.setModel(model1);
        trainOptSel = "";
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selectedIx = list1.getSelectedIndices();
                trainOptSel = "";
                if(selectedIx.length == 1) {
                    if (list1.isSelectedIndex(selectedIx[0]) && !e.getValueIsAdjusting()) {
                        trainOptSel = String.valueOf(list1.getModel().getElementAt(selectedIx[0]));
                    }
                }
                else{
                    trainOptSel = "";
                }

            }
        });

        model2 = new DefaultListModel();
        list2.setModel(model2);
        testOptSel = "";
        list2.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selectedIx = list2.getSelectedIndices();
                testOptSel = "";
                if(selectedIx.length == 1) {
                    if (list2.isSelectedIndex(selectedIx[0]) && !e.getValueIsAdjusting()) {
                        testOptSel = String.valueOf(list2.getModel().getElementAt(selectedIx[0]));
                    }
                }
                else{
                    testOptSel = "";
                }

            }
        });

        modelTitle.setHighlighter(null);
        DefaultCaret caret = (DefaultCaret)modelTitle.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        /**
         *  left menu buttons. Calculate features, log screen, manual loader, refresh etc.
         */
        modelName = "";

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTextArea(INFO, S_WHITE, false);
            }

        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ca.dbConnect();
                loadSessionIDs();
                ca.dbDisconnect();
            }
        });
        manualCMLoaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File workingDirectory = new File(System.getProperty("user.dir"));
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(workingDirectory);
                int returnVal = chooser.showOpenDialog(panel1);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    String name = chooser.getSelectedFile().getName();
                    ca.manualLoad(name, gui);
                }
            }
        });
        clearDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all database data?", "Database eradication", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    int reply2 = JOptionPane.showConfirmDialog(null, "Really?", "Database eradication", JOptionPane.YES_NO_OPTION);
                    if (reply2 == JOptionPane.YES_OPTION) {
                        ca.dbConnect();
                        if(ca.deleteDb()){
                            JOptionPane.showMessageDialog(null, "All data deleted!");
                        }
                        else{
                            JOptionPane.showMessageDialog(null, "Error deleting database!");
                        }


                    }
                }
            }
        });
        loadModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File workingDirectory = new File(System.getProperty("user.dir"));
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(workingDirectory);
                int returnVal = chooser.showOpenDialog(panel1);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    modelName = chooser.getSelectedFile().getName();
                    modelTitle.setText("SVM model: " + "\n" + modelName);
                    modelTitle.setForeground(S_GREEN);
                }
            }
        });


    }

    public void guiInit(){
        updateTextArea(INFO, S_WHITE, false);
        ca.dbConnect();
        loadSessionIDs();
        ca.dbDisconnect();

    }

    public void loadSessionIDs(){
        try {
            String SQL_B_SID = "SELECT DISTINCT sessionID FROM `blocks`";
            Statement stmtBsid = ca.getConn().createStatement();
            ResultSet rsBsid = stmtBsid.executeQuery(SQL_B_SID);
            model1.clear();
            model2.clear();

            while (rsBsid.next()) {
                model1.addElement(rsBsid.getString("SessionID"));
                model2.addElement(rsBsid.getString("SessionID"));
            }

        }
        catch(SQLException e){}
    }

    public boolean processWorking(){
        if (guiThreads[0].isAlive() || guiThreads[1].isAlive() || guiThreads[2].isAlive()){
            return true;
        }
        else {return false;}
    }



    public void updateBar(int newValue, Color color) {
        progressBar1.setValue(newValue);
        progressBar1.setForeground(color);
    }

    public void updateTextArea(String text, Color color, boolean cont){
        if(!cont)
        textArea1.setText("");
        textArea1.setForeground(color);
        textArea1.append(text);
    }

    public JPanel getPanel1() {
        return panel1;
    }




}
