package CurveAnalyser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Created by Flexscan2243 on 28.04.2016.
 */
public class Gui{
    private CurveAnalyser ca;
    private JPanel panel1;
    private JButton calculateBlocksButton;
    private JButton trainSVMButton;
    private JList list1;
    private DefaultListModel model1;
    private ArrayList<String> list1IDs;
    private DefaultListModel model2;
    private JButton testSVMButton;
    private JProgressBar progressBar1;
    private JTextArea textArea1;
    private JList list2;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private Gui gui;
    private Subsidiaries.CAmode caMode;




    public Gui(CurveAnalyser ca) {
        this.ca = ca;
        this.gui = this;
        calculateBlocksButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Runnable runner = new Runnable()
                {
                    public void run() {
                        ca.startCurveAnalyser(gui, caMode.CALCULATE);
                    }
                };
                Thread t = new Thread(runner, "CALCULATE exectuer");
                t.start();
            }
        });

        /**
         * Lists
         */
        model1 = new DefaultListModel();
        list1.setModel(model1);
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                list1IDs = new ArrayList<String>();
                if (list1.isSelectionEmpty()) {
                    System.out.println("empty");
                } else {
                    int[] selectedIx = list1.getSelectedIndices();
                    for (int i = 0 ; i < selectedIx.length; i++) {
                        if (list1.isSelectedIndex(selectedIx[i]) && !e.getValueIsAdjusting()) {
                            list1IDs.add(i, String.valueOf(list1.getModel().getElementAt(selectedIx[i])));
                        }
                    }
                }
            }
        });

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTextArea("AUI 2016 internet users analyser." + "\n" + "\n"
                        + "Calculate blocks: Determine curves within mouse movements," + "\n"
                        + "angle based metrics and SVM feature vectors (blocks) " + "\n" + "\n"
                        + "Train SVM: train the Support Vector Machine with a" + "\n"
                        + "chosen user's metric and all the other users signatures " + "\n" + "\n"
                        + "Test SVM: test if a chosen sample belongs to the user");
            }
        });
        trainSVMButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (String a : getList1IDs()){
                    System.out.println(a);
                }
            }
        });

    }

    public void guiInit(){
        updateTextArea("AUI 2016 internet users analyser." + "\n" + "\n"
                        + "Calculate blocks: Determine curves within mouse movements," + "\n"
                        + "angle based metrics and SVM feature vectors (blocks) " + "\n" + "\n"
                        + "Train SVM: train the Support Vector Machine with a" + "\n"
                        + "chosen user's metric and all the other users signatures " + "\n" + "\n"
                        + "Test SVM: test if a chosen sample belongs to the user");
        ca.dbConnect();
        loadSessionIDs();
        ca.dbDisconnect();

    }

    public void loadSessionIDs(){
        try {
            String SQL_B_SID = "SELECT DISTINCT sessionID FROM `blocks`";
            Statement stmtBsid = ca.getConn().createStatement();
            ResultSet rsBsid = stmtBsid.executeQuery(SQL_B_SID);

            while (rsBsid.next()) {
                model1.addElement(rsBsid.getString("SessionID"));
            }

        }
        catch(SQLException e){}
    }



    public void updateBar(int newValue, Color color) {
        progressBar1.setValue(newValue);
        progressBar1.setForeground(color);
    }

    public void updateTextArea(String text){
        textArea1.setText("");
        textArea1.append(text);
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public ArrayList<String> getList1IDs() {
        return list1IDs;
    }



}
