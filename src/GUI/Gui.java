package GUI;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import Analyser.Analyser;
import Analyser.RunParams;
import SVM.SVM;
import SVM.SVMresult;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Flexscan2243 on 28.04.2016.
 */
public class Gui {
    private Analyser ca;
    private JPanel panel1;
    private JButton calculateBlocksButton;
    private JButton trainSVMButton;
    private JList<String> list1;
    private DefaultListModel model1;
    private String trainOptSel;
    private DefaultListModel model2;
    private String testOptSel;
    private String modelName;
    private JButton testSVMButton;
    private JProgressBar progressBar1;
    private JTextArea textArea1;
    private JList<String> list2;
    private JButton button1;
    private JButton refreshButton;
    private JButton manualCMLoaderButton;
    private JButton clearDatabaseButton;
    private JButton loadModelButton;
    private JTextArea modelTitle;
    private Gui gui;
    private Runnable[] guiRunners;
    private Thread[] guiThreads;
    private JLabel title;
    private JScrollPane modelPane;
    private JScrollPane testPane;
    private JScrollPane trainPane;
    private JScrollPane logPane;

    public static final Color S_RED = new Color(255, 135, 135);
    public static final Color S_WHITE = new Color(235, 235, 235);
    public static final Color S_GREEN = new Color(163, 255, 135);
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

    public Gui(Analyser ca, SVM svm) {
        this.ca = ca;
        this.gui = this;
        SVMresult.CAmode[] tabMod = {SVMresult.CAmode.CALCULATE, SVMresult.CAmode.TRAIN, SVMresult.CAmode.TEST, SVMresult.CAmode.LOAD, SVMresult.CAmode.DELETE};

        //Threads
        guiRunners = new Runnable[5];
        guiThreads = new Thread[5];

        for (int i = 0; i < 5; i++) {
            guiRunners[i] = new GuiRun(new RunParams(ca, gui, tabMod[i], svm, "", "", "", ""));
            guiThreads[i] = new Thread(guiRunners[i]);
        }

        //SVM data manipulation buttons
        calculateBlocksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processWorking()) {
                    updateTextArea(ANOTHER_PR, S_RED, false);
                } else {
                    updateTextArea(CALC, S_GREEN, false);
                    guiRunners[0] = new GuiRun(new RunParams(ca, gui, tabMod[0], svm, "", "", "", ""));
                    guiThreads[0] = new Thread(guiRunners[0]);
                    guiThreads[0].start();

                }
            }
        });

        trainSVMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (processWorking()) {
                    updateTextArea(ANOTHER_PR, S_RED, false);
                } else if (trainOptSel.isEmpty()) {
                    updateTextArea(SELECT_SID, S_RED, false);

                } else {
                    guiRunners[1] = new GuiRun(new RunParams(ca, gui, tabMod[1], svm, trainOptSel, "", "", ""));
                    guiThreads[1] = new Thread(guiRunners[1]);
                    guiThreads[1].start();

                }

            }
        });

        testSVMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processWorking()) {
                    updateTextArea(ANOTHER_PR, S_RED, false);
                } else if (testOptSel.isEmpty() || modelName.isEmpty()) {
                    updateTextArea(SEL_SID_AND_NAME, S_RED, false);

                } else {
                    updateTextArea(TEST_STR, S_GREEN, false);
                    guiRunners[2] = new GuiRun(new RunParams(ca, gui, tabMod[2], svm, "", testOptSel, modelName, ""));
                    guiThreads[2] = new Thread(guiRunners[2]);
                    guiThreads[2].start();
                }
            }
        });

        manualCMLoaderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processWorking()) {
                    updateTextArea(ANOTHER_PR, S_RED, false);
                } else {
                    File workingDirectory = new File(System.getProperty("user.dir"));
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(workingDirectory);
                    int returnVal = chooser.showOpenDialog(panel1);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        String name = chooser.getSelectedFile().getName();
                        guiRunners[3] = new GuiRun(new RunParams(ca, gui, tabMod[3], svm, "", "", "", name));
                        guiThreads[3] = new Thread(guiRunners[3]);
                        guiThreads[3].start();
                    }
                }
            }
        });

        clearDatabaseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (processWorking()) {
                    updateTextArea(ANOTHER_PR, S_RED, false);
                } else {
                    int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all database data?", "Database eradication", JOptionPane.YES_NO_OPTION);
                    if (reply == JOptionPane.YES_OPTION) {
                        int reply2 = JOptionPane.showConfirmDialog(null, "Really?", "Database eradication", JOptionPane.YES_NO_OPTION);
                        if (reply2 == JOptionPane.YES_OPTION) {
                            guiRunners[4] = new GuiRun(new RunParams(ca, gui, tabMod[4], svm, "", "", "", ""));
                            guiThreads[4] = new Thread(guiRunners[4]);
                            guiThreads[4].start();
                        }
                    }
                }
            }
        });

        //Right menu buttons. SVM buttons lists for selected options
        model1 = new DefaultListModel();
        list1.setModel(model1);
        trainOptSel = "";
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int[] selectedIx = list1.getSelectedIndices();
                trainOptSel = "";
                if (selectedIx.length == 1) {
                    if (list1.isSelectedIndex(selectedIx[0]) && !e.getValueIsAdjusting()) {
                        trainOptSel = String.valueOf(list1.getModel().getElementAt(selectedIx[0]));
                    }
                } else {
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
                if (selectedIx.length == 1) {
                    if (list2.isSelectedIndex(selectedIx[0]) && !e.getValueIsAdjusting()) {
                        testOptSel = String.valueOf(list2.getModel().getElementAt(selectedIx[0]));
                    }
                } else {
                    testOptSel = "";
                }

            }
        });

        modelTitle.setHighlighter(null);
        DefaultCaret caret = (DefaultCaret) modelTitle.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        //left menu buttons. Calculate features, log screen, manual loader, refresh etc.
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
                ca.getDb().connect();
                loadSessionIDs();
                ca.getDb().disconnect();
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

    public void guiInit() {
        updateTextArea(INFO, S_WHITE, false);
        ca.getDb().connect();
        loadSessionIDs();
        ca.getDb().disconnect();

    }

    public void loadSessionIDs() {
        try {
            String SQL_B_SID = "SELECT DISTINCT sessionID FROM `blocks`";
            ResultSet rsBsid = ca.getDb().select(SQL_B_SID);
            model1.clear();
            model2.clear();

            while (rsBsid.next()) {
                model1.addElement(rsBsid.getString("SessionID"));
                model2.addElement(rsBsid.getString("SessionID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean processWorking() {
        return guiThreads[0].isAlive() || guiThreads[1].isAlive() || guiThreads[2].isAlive();
    }


    public void updateBar(int newValue, Color color) {
        progressBar1.setValue(newValue);
        progressBar1.setForeground(color);
    }

    public void updateTextArea(String text, Color color, boolean cont) {
        if (!cont)
            textArea1.setText("");
        textArea1.setForeground(color);
        textArea1.append(text + "\n" + "\n");
    }

    public JPanel getPanel1() {
        return panel1;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(23, 19, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBackground(new Color(-12828863));
        panel1.setForeground(new Color(-2235157));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22), null));
        title = new JLabel();
        title.setBackground(new Color(-2235157));
        title.setFont(new Font(title.getFont().getName(), title.getFont().getStyle(), 12));
        title.setForeground(new Color(-2235157));
        title.setText("AUI internet users analyser");
        panel1.add(title, new GridConstraints(0, 0, 1, 15, GridConstraints.ANCHOR_NORTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        logPane = new JScrollPane();
        logPane.setFont(new Font(logPane.getFont().getName(), logPane.getFont().getStyle(), 22));
        logPane.setVerticalScrollBarPolicy(20);
        panel1.add(logPane, new GridConstraints(9, 1, 13, 13, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        logPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        textArea1 = new JTextArea();
        textArea1.setBackground(new Color(-14803426));
        textArea1.setEditable(false);
        textArea1.setFont(new Font(textArea1.getFont().getName(), textArea1.getFont().getStyle(), 14));
        textArea1.setForeground(new Color(-2235157));
        logPane.setViewportView(textArea1);
        trainPane = new JScrollPane();
        panel1.add(trainPane, new GridConstraints(1, 15, 8, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        trainPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null));
        list1 = new JList();
        list1.setBackground(new Color(-14803426));
        list1.setForeground(new Color(-2235157));
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        list1.setModel(defaultListModel1);
        list1.setSelectionForeground(new Color(-16777216));
        trainPane.setViewportView(list1);
        testPane = new JScrollPane();
        panel1.add(testPane, new GridConstraints(10, 15, 12, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, new Color(-4473925)));
        list2 = new JList();
        list2.setBackground(new Color(-14803426));
        list2.setForeground(new Color(-2235157));
        list2.setSelectionForeground(new Color(-16777216));
        testPane.setViewportView(list2);
        testSVMButton = new JButton();
        testSVMButton.setBackground(new Color(-9999763));
        testSVMButton.setBorderPainted(true);
        testSVMButton.setContentAreaFilled(true);
        testSVMButton.setFont(new Font(testSVMButton.getFont().getName(), testSVMButton.getFont().getStyle(), 18));
        testSVMButton.setForeground(new Color(-2235157));
        testSVMButton.setText("Test SVM");
        panel1.add(testSVMButton, new GridConstraints(22, 15, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        loadModelButton = new JButton();
        loadModelButton.setBackground(new Color(-9999763));
        loadModelButton.setFont(new Font(loadModelButton.getFont().getName(), loadModelButton.getFont().getStyle(), 18));
        loadModelButton.setForeground(new Color(-2235157));
        loadModelButton.setText("Load Model");
        panel1.add(loadModelButton, new GridConstraints(22, 18, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        button1 = new JButton();
        button1.setBackground(new Color(-9999763));
        button1.setFont(new Font(button1.getFont().getName(), button1.getFont().getStyle(), 18));
        button1.setForeground(new Color(-2235157));
        button1.setText("Instructions");
        panel1.add(button1, new GridConstraints(22, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        modelPane = new JScrollPane();
        modelPane.setAutoscrolls(false);
        modelPane.setHorizontalScrollBarPolicy(31);
        modelPane.setRequestFocusEnabled(true);
        modelPane.setVerticalScrollBarPolicy(21);
        modelPane.setWheelScrollingEnabled(false);
        panel1.add(modelPane, new GridConstraints(22, 8, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(150, 40), new Dimension(150, 40), null, 0, false));
        modelPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.LEFT, TitledBorder.DEFAULT_POSITION, new Font(modelPane.getFont().getName(), modelPane.getFont().getStyle(), modelPane.getFont().getSize()), new Color(-6052957)));
        modelTitle = new JTextArea();
        modelTitle.setAutoscrolls(true);
        modelTitle.setBackground(new Color(-16777216));
        modelTitle.setCaretColor(new Color(-16777216));
        modelTitle.setDisabledTextColor(new Color(-16777216));
        modelTitle.setEditable(false);
        modelTitle.setEnabled(true);
        modelTitle.setForeground(new Color(-30841));
        modelTitle.setLineWrap(false);
        modelTitle.setSelectionColor(new Color(-12894656));
        modelTitle.setSelectionEnd(21);
        modelTitle.setSelectionStart(21);
        modelTitle.setTabSize(8);
        modelTitle.setText("No SVM model selected");
        modelPane.setViewportView(modelTitle);
        refreshButton = new JButton();
        refreshButton.setBackground(new Color(-9999763));
        refreshButton.setFont(new Font(refreshButton.getFont().getName(), refreshButton.getFont().getStyle(), 18));
        refreshButton.setForeground(new Color(-2235157));
        refreshButton.setText("Refresh");
        panel1.add(refreshButton, new GridConstraints(22, 4, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        manualCMLoaderButton = new JButton();
        manualCMLoaderButton.setBackground(new Color(-9999763));
        manualCMLoaderButton.setFont(new Font(manualCMLoaderButton.getFont().getName(), manualCMLoaderButton.getFont().getStyle(), 18));
        manualCMLoaderButton.setForeground(new Color(-2235157));
        manualCMLoaderButton.setText("Load Cursor Movements");
        panel1.add(manualCMLoaderButton, new GridConstraints(1, 1, 1, 13, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        calculateBlocksButton = new JButton();
        calculateBlocksButton.setBackground(new Color(-9999763));
        calculateBlocksButton.setFocusCycleRoot(false);
        calculateBlocksButton.setFont(new Font(calculateBlocksButton.getFont().getName(), calculateBlocksButton.getFont().getStyle(), 18));
        calculateBlocksButton.setForeground(new Color(-2235157));
        calculateBlocksButton.setHorizontalAlignment(0);
        calculateBlocksButton.setRolloverEnabled(true);
        calculateBlocksButton.setText("Calculate Blocks");
        calculateBlocksButton.setToolTipText("Connect to database. Determine curves. Calculate angle based metrics. Prepare feature vectors for SVM (blocks)");
        panel1.add(calculateBlocksButton, new GridConstraints(2, 1, 1, 13, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        trainSVMButton = new JButton();
        trainSVMButton.setBackground(new Color(-9999763));
        trainSVMButton.setFont(new Font(trainSVMButton.getFont().getName(), trainSVMButton.getFont().getStyle(), 18));
        trainSVMButton.setForeground(new Color(-1315861));
        trainSVMButton.setOpaque(true);
        trainSVMButton.setText("Train SVM");
        panel1.add(trainSVMButton, new GridConstraints(9, 15, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        clearDatabaseButton = new JButton();
        clearDatabaseButton.setBackground(new Color(-12313831));
        clearDatabaseButton.setFont(new Font(clearDatabaseButton.getFont().getName(), clearDatabaseButton.getFont().getStyle(), 18));
        clearDatabaseButton.setForeground(new Color(-6272680));
        clearDatabaseButton.setText("Clear Database");
        panel1.add(clearDatabaseButton, new GridConstraints(3, 1, 1, 13, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        progressBar1 = new JProgressBar();
        progressBar1.setBackground(new Color(-6052957));
        progressBar1.setForeground(new Color(-2235157));
        progressBar1.setStringPainted(true);
        panel1.add(progressBar1, new GridConstraints(8, 1, 1, 13, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, 1, 1, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

    private class GuiRun implements Runnable {
        private RunParams runParams;

        public GuiRun(RunParams runParams) {
            this.runParams = runParams;
        }

        @Override
        public void run() {
            runParams.ca.start(runParams);
        }
    }
}
