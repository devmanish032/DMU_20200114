/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import logger.CctnsLogger;

/**
 *
 * @author Administrator
 */
public class CIPAServerMain extends JFrame {

    private JPanel mainPanel, topSelectionPanel;
    private JPanel WindowUpperPanel, WindowLowerPanel;
    private JPanel upperLeftPanel, upperRightPanel;
    final String[] labelContents = new String[]{"", "User Name :- ", "Password :- ", "Hostname/IP address :-", "Database Name :-", "Database Type :-", "Operating System :-"};
    final String[] DatabaseNamesSource = new String[]{"", "PostgreSQL"};
    final String[] DatabaseNamesTarget = new String[]{"", "MySql", "SQL Server",};
    final String[] OSystemNamesSource = new String[]{"", "Linux"};
    final String[] OSystemNamesTarget = new String[]{"", "Windows", "Linux"};
    JTextField sourcePortTextField = new JTextField();
    JTextField targetPortTextField = new JTextField();
    /**
     * 
     */
    public Connection connectionSource, connectionTarget;
    JButton sourceConButton, targetConButton, sourcePreConButton, targetPreConButton, sourceBackButton, targetBackButton;
    JLabel labelSource[], labelTarget[], labelSourceField[], labelTargetField[];
    JTextField sourceTextField[] = new JTextField[7];
    JTextField targetTextField[] = new JTextField[7];
    String batchCD;
    String datestr;
    JProgressBar pb;
    JLabel statusLabel, content;
    Timer timer;
    int i;
    /**
     * 
     */
    public String condetails[];
    JComponent comp;
    String sourceORtarget;
    Date dtBegTime, dtEndTime;
    String s[] = new String[10];
    final static int interval = 500;
    //..Lower panel component.....................................
    final String[] formTypeName = new String[]{"", "IIF1_FIR_Registration_Final",
        "IIF2_Crime_Detail_Final", "IIF3_Arrest_Memo_Final", "IIF4_Seizure_Memo_Final",
        "IIF5_Final_Report_Final", "IIF6 & 7 _FIR_Prosecution_Final", "IIF8_Missing_Person_Final",
        "IIF9 & 10_ Dead_Body_Registration_Final", "IIF11_Unidentified_Person_Final",
        "Criminal_Profiling1", "Criminal_Profiling2_Final", "Bail_Details_Final",
        "Interrogated_Person_Final", "Lost_Property_Final", "MLC_Details_Final",
        "NCR_Final", "Remand_Form_Final"};
    HashMap hashMapState = new HashMap();
    HashMap hashMapDistrict = new HashMap();
    HashMap hashMapPS = new HashMap();
    /**
     * 
     */
    private static CctnsLogger logger = CctnsLogger.getInstance(CIPAServerMain.class.getName());
    /**
     * 
     */
    File fileSequel;
    public boolean leftConOk, rightConOk;
    JLabel labelState, labelDistrict, labelPS, labelFromDate, labelToDate, labelSaveAt, labeluploadFrom;
    JComboBox comboState, comboDistrict, comboPS;    //adding checking
    String selectedState, selectedDistrict, selectedPS;
    Object keyState, keyDistrict, keyPS;
    JRadioButton sourceOnlyRadio, targetOnlyRadio, bothRadio;
    JLabel sourceOnlyLabel, targetOnlyLabel, bothLabel;
    JButton downloadButton, uploadButton, migrateButton;
    private JButton fromCalander, toCalander, browseSaveAtButton, browseSelectFromButton;
    JTextField textFieldFromDate, textFieldToDate, textFieldSaveAt, textFieldUploadFrom;
    float ffLeft = (float) 55.2;
    float ffRight = (float) 56.1;
    File tofolder = new File(System.getProperty("user.dir"));
    //====Read Property File====================

    CIPAServerMain() throws IOException, SQLException {
        super("CIPA DMU");
        final SQLConnection sqlcon;
//        final JFrame window = new JFrame();
        Container window = this.getContentPane();
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //     setLayout(new FlowLayout());
//        Calendar date = Calendar.getInstance();
//        SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddhhmmss");
//        datestr = dateformatter.format(date.getTime());
        statusLabel = new JLabel();
        pb = new JProgressBar(0, 20);
        pb.setValue(0);
        pb.setStringPainted(true);
//        pb.setVisible(false);
        pb.setBounds(10, 120, 480, 20);
        pb.setVisible(false);

//--MAIN + 2PARTS PANEL---------------------------------------------------------
        mainPanel = new JPanel(null);
        topSelectionPanel = new JPanel(null);
//        mainPanel.setLayout(new GridLayout(2, 1));

        mainPanel.setBounds(0, 40, 720, 400);
        topSelectionPanel.setBounds(0, 0, 720, 40);
        topSelectionPanel.setBackground(Color.lightGray);


        window.add(mainPanel);
        window.add(topSelectionPanel);

        WindowUpperPanel = new JPanel();
        WindowUpperPanel.setBounds(0, 0, 720, 200);
        mainPanel.add(WindowUpperPanel);

        WindowLowerPanel = new JPanel();
        WindowLowerPanel.setBounds(0, 200, 720, 200);
        mainPanel.add(WindowLowerPanel);

        add(mainPanel, BorderLayout.CENTER);
//--SOURCE AND TARGET PANELS----------------------------------------------------
        upperLeftPanel = new JPanel();
        upperLeftPanel.setBackground(Color.getHSBColor(ffRight, ffRight, ffRight));
        upperLeftPanel.setLayout(null);

        upperRightPanel = new JPanel();
        upperRightPanel.setBackground(Color.getHSBColor(ffLeft, ffLeft, ffLeft));
        upperRightPanel.setLayout(null);

        upperLeftPanel.setVisible(false);
        upperRightPanel.setVisible(false);

        WindowUpperPanel.setLayout(new GridLayout(1, 2));

        JLabel sourceLabel = new JLabel("Source Server");
        sourceLabel.setForeground(Color.BLUE);
        sourceLabel.setBounds(30, 2, 120, 50);
        upperLeftPanel.add(sourceLabel);

        JLabel targetLabel = new JLabel("Target Server");
        targetLabel.setForeground(Color.BLUE);
        targetLabel.setBounds(30, 2, 120, 50);
        upperRightPanel.add(targetLabel);

//.....UPPER LEFT PANEL CONTENTS..............................................
        JLabel sourceConnectStatus = new JLabel("Server Not Connected !!!");

        final JButton sourceNewConnectButton = new JButton();
        final JButton sourceOldConnectButton = new JButton();

        sourceNewConnectButton.setText("Connect with New Connection");
        sourceOldConnectButton.setText("Connect with Previous Connection");

        sourceConnectStatus.setBounds(30, 60, 300, 20);
        sourceNewConnectButton.setBounds(30, 100, 300, 20);
        sourceOldConnectButton.setBounds(30, 150, 300, 20);

        upperLeftPanel.add(sourceConnectStatus);
        upperLeftPanel.add(sourceNewConnectButton);
        upperLeftPanel.add(sourceOldConnectButton);

        sourceConButton = new JButton();
        targetConButton = new JButton();
        sourcePreConButton = new JButton();
        targetPreConButton = new JButton();
        sourceBackButton = new JButton();
        targetBackButton = new JButton();
//.....UPPER RIGHT PANEL CONTENTS..............................................
        JLabel targetConnectStatus = new JLabel("Server Not Connected !!!");

        final JButton targetNewConnectButton = new JButton();
        final JButton targetOldConnectButton = new JButton();

        targetNewConnectButton.setText("Connect with New Connection");
        targetOldConnectButton.setText("Connect with Previous Connection");

        targetConnectStatus.setBounds(30, 60, 300, 20);
        targetNewConnectButton.setBounds(30, 100, 300, 20);
        targetOldConnectButton.setBounds(30, 150, 300, 20);

        upperRightPanel.add(targetConnectStatus);
        upperRightPanel.add(targetNewConnectButton);
        upperRightPanel.add(targetOldConnectButton);

//--LOWER PANEL CONTENTS--------------------------------------------------------
        WindowLowerPanel.setLayout(null);

        labelState = new JLabel("State                 :");
        comboState = new JComboBox();

        labelDistrict = new JLabel("District             :");
        comboDistrict = new JComboBox();

        labelPS = new JLabel("Police Station :");
        comboPS = new JComboBox();

        labelFromDate = new JLabel("From (MM-YYYY)");
        textFieldFromDate = new JTextField();

        labelToDate = new JLabel("To      (MM-YYYY)");
        textFieldToDate = new JTextField();

        labelSaveAt = new JLabel("Save At  :");
        textFieldSaveAt = new JTextField();

        labeluploadFrom = new JLabel("Select File     :");
        textFieldUploadFrom = new JTextField();

        WindowLowerPanel.add(labelFromDate);
        WindowLowerPanel.add(labelToDate);
        WindowLowerPanel.add(labelSaveAt);
        WindowLowerPanel.add(labeluploadFrom);

        sourceOnlyRadio = new JRadioButton();
        targetOnlyRadio = new JRadioButton();
        bothRadio = new JRadioButton();
        sourceOnlyLabel = new JLabel("Source Connection Only ");
        targetOnlyLabel = new JLabel("Target Connection Only ");
        bothLabel = new JLabel("Both Connections ");

        downloadButton = new JButton("Download Data");
        uploadButton = new JButton("Upload Data");
        migrateButton = new JButton("Migrate Data");

        fromCalander = new JButton(new ImageIcon("src/i_cal.gif"));
        toCalander = new JButton(new ImageIcon("src/i_cal.gif"));
        browseSaveAtButton = new JButton("Browse");
        browseSelectFromButton = new JButton("Browse");

        WindowLowerPanel.add(labelState);
        WindowLowerPanel.add(labelDistrict);
        WindowLowerPanel.add(labelPS);
        WindowLowerPanel.add(comboDistrict);
        WindowLowerPanel.add(comboPS);


        topSelectionPanel.add(sourceOnlyRadio);
        topSelectionPanel.add(targetOnlyRadio);
        topSelectionPanel.add(bothRadio);

        topSelectionPanel.add(sourceOnlyLabel);
        topSelectionPanel.add(targetOnlyLabel);
        topSelectionPanel.add(bothLabel);

        WindowLowerPanel.add(comboState);

        WindowLowerPanel.add(downloadButton);
        WindowLowerPanel.add(uploadButton);
        WindowLowerPanel.add(migrateButton);
        WindowLowerPanel.add(fromCalander);
        WindowLowerPanel.add(toCalander);
        WindowLowerPanel.add(textFieldFromDate);
        WindowLowerPanel.add(textFieldToDate);
        WindowLowerPanel.add(textFieldSaveAt);
        WindowLowerPanel.add(browseSaveAtButton);
        WindowLowerPanel.add(textFieldUploadFrom);
        WindowLowerPanel.add(browseSelectFromButton);
        WindowLowerPanel.add(pb);
        WindowLowerPanel.add(statusLabel);

        labelFromDate.setVisible(false);
        labelSaveAt.setVisible(false);
        labeluploadFrom.setVisible(false);
        labelToDate.setVisible(false);
        labelState.setVisible(false);
        labelDistrict.setVisible(false);
        labelPS.setVisible(false);
        comboState.setVisible(false);
        downloadButton.setVisible(false);
        uploadButton.setVisible(false);
        migrateButton.setVisible(false);
        fromCalander.setVisible(false);
        toCalander.setVisible(false);
        textFieldFromDate.setVisible(false);
        textFieldToDate.setVisible(false);
        textFieldSaveAt.setVisible(false);
        browseSaveAtButton.setVisible(false);
        textFieldUploadFrom.setVisible(false);
        browseSelectFromButton.setVisible(false);
        statusLabel.setVisible(false);

        labelState.setBounds(10, 32, 100, 20);
        labelDistrict.setBounds(10, 54, 100, 20);
        labelPS.setBounds(10, 76, 100, 20);

        comboDistrict.setBounds(110, 54, 200, 20);
        comboPS.setBounds(110, 76, 200, 20);

        comboState.setBounds(110, 32, 200, 20);

        sourceOnlyRadio.setBounds(20, 10, 20, 20);
        targetOnlyRadio.setBounds(260, 10, 20, 20);
        bothRadio.setBounds(510, 10, 20, 20);

        sourceOnlyLabel.setBounds(50, 10, 200, 20);
        targetOnlyLabel.setBounds(300, 10, 200, 20);
        bothLabel.setBounds(550, 10, 200, 20);

        sourceOnlyLabel.setForeground(Color.black);
        targetOnlyLabel.setForeground(Color.black);
        bothLabel.setForeground(Color.black);

        labelFromDate.setBounds(350, 32, 100, 20);
        labelToDate.setBounds(350, 54, 100, 20);
        labelSaveAt.setBounds(350, 76, 100, 20);
        labeluploadFrom.setBounds(10, 32, 100, 20);

        textFieldFromDate.setBounds(450, 32, 180, 20);
        textFieldToDate.setBounds(450, 54, 180, 20);
        textFieldSaveAt.setBounds(450, 76, 117, 20);
        browseSaveAtButton.setBounds(570, 76, 80, 20);
        browseSelectFromButton.setBounds(292, 32, 80, 20);
        textFieldUploadFrom.setBounds(90, 32, 200, 20);

        fromCalander.setBounds(633, 32, 16, 16);
        toCalander.setBounds(633, 54, 16, 16);

        statusLabel.setBounds(10, 100, 480, 20);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(sourceOnlyRadio);
        buttonGroup.add(targetOnlyRadio);
        buttonGroup.add(bothRadio);

        //---default--------------------------------
        sourceOnlyRadio.setSelected(true);
        WindowUpperPanel.add(upperLeftPanel);
        upperLeftPanel.setVisible(true);
        WindowUpperPanel.setBackground(Color.getHSBColor(ffRight, ffRight, ffRight));
        displayLowerPanelComponentsForSource();
        //------------------------------------------
        downloadButton.setBounds(500, 110, 150, 30);
        uploadButton.setBounds(500, 110, 150, 30);
        migrateButton.setBounds(500, 110, 150, 30);

        sourceOnlyRadio.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == sourceOnlyRadio) {
                    WindowUpperPanel.add(upperLeftPanel);
                    WindowUpperPanel.add(upperRightPanel);

                    upperLeftPanel.setVisible(true);
                    upperRightPanel.setVisible(false);
                    WindowUpperPanel.setBackground(Color.getHSBColor(ffRight, ffRight, ffRight));
                    displayLowerPanelComponentsForSource();
                }
            }
        });
        targetOnlyRadio.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetOnlyRadio) {
                    WindowUpperPanel.add(upperRightPanel);
                    WindowUpperPanel.add(upperLeftPanel);

                    upperRightPanel.setVisible(true);
                    upperLeftPanel.setVisible(false);
                    WindowUpperPanel.setBackground(Color.getHSBColor(ffLeft, ffLeft, ffLeft));
                    displayLowerPanelComponentsForTarget();
                }
            }
        });
        bothRadio.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == bothRadio) {
                    WindowUpperPanel.add(upperLeftPanel);
                    WindowUpperPanel.add(upperRightPanel);

                    upperRightPanel.setVisible(true);
                    upperLeftPanel.setVisible(true);
                    displayLowerPanelComponentsForBoth();
                }
            }
        });
//================================            
        fromCalander.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {

                String str = new DatePickerCalendar(300, 300).getPickedDate();
                if (!str.isEmpty()) {
                    textFieldFromDate.setText(str.substring(3, 10));
                    int fromMM = 0, fromYYYY = 0, toMM = 0, toYYYY = 0;

                    if (textFieldFromDate.getText() != null && !textFieldFromDate.getText().isEmpty()) {
                        String from = textFieldFromDate.getText();
                        fromMM = Integer.parseInt(from.substring(0, 2));
                        fromYYYY = Integer.parseInt(from.substring(3, 7));
                    }

                    if (textFieldToDate.getText() != null && !textFieldToDate.getText().isEmpty()) {
                        String to = textFieldToDate.getText();
                        toMM = Integer.parseInt(to.substring(0, 2));
                        toYYYY = Integer.parseInt(to.substring(3, 7));
                    }

                    if (!textFieldFromDate.getText().isEmpty() && !textFieldToDate.getText().isEmpty()) {
                        if ((fromYYYY > toYYYY) || (fromYYYY == toYYYY && fromMM > toMM)) {
                            JOptionPane.showMessageDialog(comp, "'From (MM/YYYY)' should be Lower \n than 'To (MM/YYYY)'");
                            textFieldFromDate.setText("");
                            textFieldToDate.setText("");
                        }
                    }
                }
            }
        });

        toCalander.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {

                String str = new DatePickerCalendar(300, 300).getPickedDate();
                if (!str.isEmpty()) {
                    textFieldToDate.setText(str.substring(3, 10));
                    int fromMM = 0, fromYYYY = 0, toMM = 0, toYYYY = 0;

                    if (textFieldFromDate.getText() != null && !textFieldFromDate.getText().isEmpty()) {
                        String from = textFieldFromDate.getText();
                        fromMM = Integer.parseInt(from.substring(0, 2));
                        fromYYYY = Integer.parseInt(from.substring(3, 7));
                    }

                    if (textFieldToDate.getText() != null && !textFieldToDate.getText().isEmpty()) {
                        String to = textFieldToDate.getText();
                        toMM = Integer.parseInt(to.substring(0, 2));
                        toYYYY = Integer.parseInt(to.substring(3, 7));
                    }

                    if (!textFieldFromDate.getText().isEmpty() && !textFieldToDate.getText().isEmpty()) {
                        if ((fromYYYY > toYYYY) || (fromYYYY == toYYYY && fromMM > toMM)) {
                            JOptionPane.showMessageDialog(comp, "'To (MM/YYYY)' should be Higher \n than 'From (MM/YYYY)'");
                            textFieldFromDate.setText("");
                            textFieldToDate.setText("");
                        }
                    }
                }
            }
        });


////=SOURCE NEW CONNECTION ACTION COMMAND=======================================
        sourceNewConnectButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == sourceNewConnectButton) {

                    upperLeftPanel.removeAll();
                    upperLeftPanel.revalidate();
                    validate();
                    repaint();
                    //.......DRAW COMPONENT ON LEFT PANEL.................................
                    JLabel newConDetails = new JLabel("Enter New Source Server Connection Details :");
                    newConDetails.setForeground(Color.blue);
                    newConDetails.setBounds(15, 5, 300, 20);
                    upperLeftPanel.add(newConDetails);
                    labelSource = new JLabel[labelContents.length];
                    labelSourceField = new JLabel[labelContents.length];

                    final JComboBox OSComboBoxN = new JComboBox();
                    final JComboBox DBComboBoxN = new JComboBox();

                    for (int i = 1; i < OSystemNamesSource.length; i++) {
                        OSComboBoxN.addItem(OSystemNamesSource[i]);
                    }

                    for (int j = 1; j < DatabaseNamesSource.length; j++) {
                        DBComboBoxN.addItem(DatabaseNamesSource[j]);
                    }


                    for (int i = 1; i < labelContents.length; i++) {
                        labelSource[i] = new JLabel(labelContents[i]);
                        if (i == 2) {
                            sourceTextField[i] = new JPasswordField();
                        } else {
                            sourceTextField[i] = new JTextField();
                        }

                        labelSource[i].setBounds(15, 22 * i, 140, 20);

                        if (i == 3) {
                            sourceTextField[i].setBounds(150, 22 * i, 100, 20);

                            JLabel sourcePortLabel = new JLabel("Port:");
                            sourcePortTextField = new JTextField();
                            sourcePortTextField.setBounds(290, 22 * i, 50, 20);
                            sourcePortLabel.setBounds(260, 22 * i, 100, 20);
                            upperLeftPanel.add(sourcePortLabel);
                            upperLeftPanel.add(sourcePortTextField);
                        } else {
                            sourceTextField[i].setBounds(150, 22 * i, 190, 20);
                        }

//                        sourceTextField[i].setBounds(150, 22 * i, 190, 20);
                        upperLeftPanel.add(labelSource[i]);

                        if (i == 5) {
                            DBComboBoxN.setBounds(150, 22 * i, 190, 20);
                            sourceTextField[i].hide();
                            upperLeftPanel.add(DBComboBoxN);
                        }

                        if (i == 6) {
                            OSComboBoxN.setBounds(150, 22 * i, 190, 20);
                            sourceTextField[i].hide();
                            upperLeftPanel.add(OSComboBoxN);
                        }

                        if (i == 2) {
                            sourceTextField[i].setBounds(150, 22 * i, 190, 20);
                        }
                        upperLeftPanel.add(sourceTextField[i]);
                    }

                    sourceTextField[5].setText(DatabaseNamesSource[1]); //Default Database Type
                    sourceTextField[6].setText(OSystemNamesSource[1]); //Default Operating System


                    DBComboBoxN.addActionListener(new ActionListener()                                                                                                                                                                                                    {

                        public void actionPerformed(ActionEvent e) {
                            if (e.getSource() == DBComboBoxN) {
                                Object obg2 = DBComboBoxN.getSelectedItem();
                                sourceTextField[5].setText(obg2.toString());
                                if (obg2.toString().trim().equals("PostgreSQL")) {
                                    sourcePortTextField.setText("5432");
                                }
                            }
                        }
                    });


                    OSComboBoxN.addActionListener(new ActionListener()                                                                                                                                                                                                    {

                        public void actionPerformed(ActionEvent e) {
                            if (e.getSource() == OSComboBoxN) {
                                Object obg1 = OSComboBoxN.getSelectedItem();
                                sourceTextField[6].setText(obg1.toString());
                            }
                        }
                    });

                    sourceBackButton.setBounds(15, 160, 160, 20);
                    sourceBackButton.setText("<< Go Back");
                    upperLeftPanel.add(sourceBackButton);

                    sourceConButton.setBounds(180, 160, 160, 20);
                    sourceConButton.setText("Connect");
                    upperLeftPanel.add(sourceConButton);

                    upperLeftPanel.add(OSComboBoxN);
                    upperLeftPanel.add(DBComboBoxN);

                }
            }
        });
////=TARGET NEW CONNECTION ACTION COMMAND=======================================
        targetNewConnectButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetNewConnectButton) {

                    upperRightPanel.removeAll();
                    upperRightPanel.revalidate();
                    validate();
                    repaint();
                    //.......DRAW COMPONENT ON RIGHT PANEL.................................
                    JLabel newConDetails = new JLabel("Enter New Target Server Connection Details :");
                    newConDetails.setForeground(Color.blue);
                    newConDetails.setBounds(15, 5, 300, 20);
                    upperRightPanel.add(newConDetails);
                    labelTarget = new JLabel[labelContents.length];
                    labelTargetField = new JLabel[labelContents.length];

                    final JComboBox OSComboBoxT = new JComboBox();
                    final JComboBox DBComboBoxT = new JComboBox();

                    for (int i = 1; i < OSystemNamesTarget.length; i++) {
                        OSComboBoxT.addItem(OSystemNamesTarget[i]);
                    }

                    for (int j = 1; j < DatabaseNamesTarget.length; j++) {
                        DBComboBoxT.addItem(DatabaseNamesTarget[j]);
                    }


                    for (int i = 1; i < labelContents.length; i++) {
                        labelTarget[i] = new JLabel(labelContents[i]);
                        if (i == 2) {
                            targetTextField[i] = new JPasswordField();
                        } else {
                            targetTextField[i] = new JTextField();
                        }

                        labelTarget[i].setBounds(15, 22 * i, 140, 20);

                        if (i == 3) {
                            targetTextField[i].setBounds(150, 22 * i, 100, 20);

                            JLabel targetPortLabel = new JLabel("Port:");
                            targetPortTextField = new JTextField();
                            targetPortTextField.setBounds(290, 22 * i, 50, 20);
                            targetPortLabel.setBounds(260, 22 * i, 100, 20);
                            upperRightPanel.add(targetPortLabel);
                            upperRightPanel.add(targetPortTextField);
                        } else {
                            targetTextField[i].setBounds(150, 22 * i, 190, 20);
                        }

                        upperRightPanel.add(labelTarget[i]);

                        if (i == 5) {
                            DBComboBoxT.setBounds(150, 22 * i, 190, 20);
                            targetTextField[i].hide();
                            upperRightPanel.add(DBComboBoxT);
                        }

                        if (i == 6) {
                            OSComboBoxT.setBounds(150, 22 * i, 190, 20);
                            targetTextField[i].hide();
                            upperRightPanel.add(OSComboBoxT);
                        }

                        if (i == 2) {
                            targetTextField[i].setBounds(150, 22 * i, 190, 20);
                        }
                        upperRightPanel.add(targetTextField[i]);
                    }

                    targetTextField[5].setText(DatabaseNamesTarget[1]); //Default Database Type
                    targetTextField[6].setText(OSystemNamesTarget[1]); //Default Operating System

                    DBComboBoxT.addActionListener(new ActionListener()                                                                                                                                                                                                    {

                        public void actionPerformed(ActionEvent e) {
                            if (e.getSource() == DBComboBoxT) {
                                Object obg2 = DBComboBoxT.getSelectedItem();
                                targetTextField[5].setText(obg2.toString());
                                if (obg2.toString().trim().equals("SQL Server")) {
                                    targetPortTextField.setText("1433");
                                } else {
                                    targetPortTextField.setText("3306");
                                }

                            }
                        }
                    });


                    OSComboBoxT.addActionListener(new ActionListener()                                                                                                                                                                                                    {

                        public void actionPerformed(ActionEvent e) {
                            if (e.getSource() == OSComboBoxT) {
                                Object obg1 = OSComboBoxT.getSelectedItem();
                                targetTextField[6].setText(obg1.toString());
                            }
                        }
                    });

                    targetBackButton.setBounds(15, 160, 160, 20);
                    targetBackButton.setText("<< Go Back");
                    upperRightPanel.add(targetBackButton);

                    targetConButton.setBounds(180, 160, 160, 20);
                    targetConButton.setText("Connect");
                    upperRightPanel.add(targetConButton);

                    upperRightPanel.add(OSComboBoxT);
                    upperRightPanel.add(DBComboBoxT);

                }
            }
        });

// Source new connection action ================================================
        sourceConButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == sourceConButton) {

                    if (sourceTextField[1].getText().equals("") || sourceTextField[2].getText().equals("")
                            || sourceTextField[3].getText().equals("") || sourceTextField[4].getText().equals("") || sourcePortTextField.getText().equals("")) {
                        JOptionPane.showMessageDialog(comp, "All fields are mandatory");
                    } else {
                        try {
                            condetails = new String[7];
                            condetails[0] = sourceTextField[1].getText();
                            condetails[1] = sourceTextField[2].getText();
                            condetails[2] = sourceTextField[3].getText();
                            condetails[3] = sourceTextField[4].getText();
                            condetails[4] = sourceTextField[5].getText();
                            condetails[5] = sourceTextField[6].getText();
                            condetails[6] = sourcePortTextField.getText();

                            String[] values = setHostDetails();

                            SQLConnection sqlcon = new SQLConnection();

                            sqlcon.callRetriveMethod(values);
                            sourceORtarget = "source";
                            Connection conn = sqlcon.getConnectionDetails(sourceORtarget);
                            if (conn != null) {
                                JOptionPane.showMessageDialog(comp, "Source Database Connection Established");
                                connectionSource = conn;
                                upperLeftPanel.removeAll();
                                upperLeftPanel.revalidate();
                                validate();
                                repaint();
                                populateCombos();
                                displayLowerPanelComponentsForSource();
                            }
                        } catch (Exception er) {
                            JOptionPane.showMessageDialog(comp, "Source Database Connection NOT Established\n\n" + er);
                            logger.log(CctnsLogger.ERROR, er);
//                            er.printStackTrace();
                        }

                        //.......LEFT PANEL for confirmation of source..................
                        JLabel newConDetails = new JLabel("Source Server Connection Established as :");
                        newConDetails.setForeground(Color.blue);
                        newConDetails.setBounds(15, 5, 300, 20);
                        upperLeftPanel.add(newConDetails);
                        //-READ FROM PROPERTY FILE TO DISPLAY CON DETAILS---------------
                        Properties props = new Properties();
                        String conExtbd[] = new String[7];
                        try {
                            File tofolder = new File(System.getProperty("user.dir"));
                            File propFile = new File(tofolder + "/ServerConnection.properties");
                            FileInputStream fileIn = new FileInputStream(propFile);
                            props.load(fileIn);
                            //========================
                            if (sourceORtarget.equals("source")) {
                                conExtbd[0] = props.getProperty("S_userName");
                                conExtbd[1] = props.getProperty("S_password");
                                conExtbd[2] = props.getProperty("S_ipAddress");
                                conExtbd[3] = props.getProperty("S_databaseName");
                                conExtbd[4] = props.getProperty("S_databaseType");
                                conExtbd[5] = props.getProperty("S_operatingType");
                                conExtbd[6] = props.getProperty("S_port");
                                fileIn.close();
                            }
                        } catch (Exception ee) {
                            logger.log(CctnsLogger.ERROR, ee);
//                            ee.printStackTrace();
                        }

                        for (int i = 3; i < labelContents.length; i++) {
                            labelSource[i] = new JLabel(labelContents[i]);
                            labelSource[i].setBounds(15, 20 * (i - 2), 140, 20);
                            upperLeftPanel.add(labelSource[i]);

                            labelSourceField[i - 1] = new JLabel(conExtbd[i - 1]);
                            labelSourceField[i - 1].setBounds(150, 20 * (i - 2), 190, 20);
                            upperLeftPanel.add(labelSourceField[i - 1]);
                        }
                        //--------------------------------------------------------------
                    }
                }
            }
        });

// Target new connection action ================================================
        targetConButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetConButton) {

                    if (targetTextField[1].getText().equals("") || targetTextField[2].getText().equals("")
                            || targetTextField[3].getText().equals("") || targetTextField[4].getText().equals("") || targetPortTextField.getText().equals("")) {
                        JOptionPane.showMessageDialog(comp, "All fields are mandatory");
                    } else {

                        try {
                            condetails = new String[7];
                            condetails[0] = targetTextField[1].getText();
                            condetails[1] = targetTextField[2].getText();
                            condetails[2] = targetTextField[3].getText();
                            condetails[3] = targetTextField[4].getText();
                            condetails[4] = targetTextField[5].getText();
                            condetails[5] = targetTextField[6].getText();
                            condetails[6] = targetPortTextField.getText();

                            String[] values = setHostDetails();
                            SQLConnection sqlcon = new SQLConnection();
                            sqlcon.callRetriveMethod(values);
                            sourceORtarget = "target";
                            Connection conn = sqlcon.getConnectionDetails(sourceORtarget);
                            if (conn != null) {
                                JOptionPane.showMessageDialog(comp, "Target Database Connection Established");
                                connectionTarget = conn;
                                upperRightPanel.removeAll();
                                upperRightPanel.revalidate();
                                validate();
                                repaint();
                            }
                        } catch (Exception er) {
                            JOptionPane.showMessageDialog(comp, "Target Database Connection NOT Established\n\n" + er);
                            logger.log(CctnsLogger.ERROR, er);
//                            er.printStackTrace();
                        }

                        //.......LEFT PANEL for confirmation of source..................
                        JLabel newConDetails = new JLabel("Target Server Connection Established as :");
                        newConDetails.setForeground(Color.blue);
                        newConDetails.setBounds(15, 5, 300, 20);
                        upperRightPanel.add(newConDetails);
                        //-READ FROM PROPERTY FILE TO DISPLAY CON DETAILS---------------
                        Properties props = new Properties();
                        condetails = new String[7];
                        try {
                            File tofolder = new File(System.getProperty("user.dir"));
                            File propFile = new File(tofolder + "/ServerConnection.properties");
                            FileInputStream fileIn = new FileInputStream(propFile);
                            props.load(fileIn);
                            //========================
                            if (sourceORtarget.equals("target")) {
                                condetails[0] = props.getProperty("T_userName");
                                condetails[1] = props.getProperty("T_password");
                                condetails[2] = props.getProperty("T_ipAddress");
                                condetails[3] = props.getProperty("T_databaseName");
                                condetails[4] = props.getProperty("T_databaseType");
                                condetails[5] = props.getProperty("T_operatingType");
                                condetails[6] = props.getProperty("T_port");
                                fileIn.close();
                            }
                        } catch (Exception ee) {
                            logger.log(CctnsLogger.ERROR, ee);
//                            ee.printStackTrace();
                        }

                        for (int i = 3; i < labelContents.length; i++) {
                            labelTarget[i] = new JLabel(labelContents[i]);
                            labelTarget[i].setBounds(15, 20 * (i - 2), 140, 20);
                            upperRightPanel.add(labelTarget[i]);

                            labelTargetField[i - 1] = new JLabel(condetails[i - 1]);
                            labelTargetField[i - 1].setBounds(150, 20 * (i - 2), 190, 20);
                            upperRightPanel.add(labelTargetField[i - 1]);
                        }

                        //--------------------------------------------------------------
                    }
                }
            }
        });

// Source old connection action ================================================
        sourceOldConnectButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == sourceOldConnectButton) {
                    condetails = new String[7];
                    Properties props = new Properties();
                    try {
                        File tofolder = new File(System.getProperty("user.dir"));
                        File propFile = new File(tofolder + "/ServerConnection.properties");
                        FileInputStream fileIn = new FileInputStream(propFile);
                        props.load(fileIn);
                        //========================
                        condetails[0] = props.getProperty("S_userName");
                        condetails[1] = props.getProperty("S_password");
                        condetails[2] = props.getProperty("S_ipAddress");
                        condetails[3] = props.getProperty("S_databaseName");
                        condetails[4] = props.getProperty("S_databaseType");
                        condetails[5] = props.getProperty("S_operatingType");
                        condetails[6] = props.getProperty("S_port");
                        fileIn.close();
                    } catch (Exception ee) {
                        logger.log(CctnsLogger.ERROR, ee);
//                        ee.printStackTrace();
                    }
                    if (!props.containsKey("S_userName")) {
                        JOptionPane.showMessageDialog(comp, "No Previous Connection !!! \n\n Please Enter New Connection Details");
                    } else {
                        upperLeftPanel.removeAll();
                        upperLeftPanel.revalidate();
                        validate();
                        repaint();
                        //.......DRAW COMPONENT ON LEFT PANEL.................................
                        JLabel newConDetails = new JLabel("Enter Password for Previous Source Connection");
                        newConDetails.setForeground(Color.blue);
                        newConDetails.setBounds(15, 5, 300, 20);
                        upperLeftPanel.add(newConDetails);
                        labelSource = new JLabel[labelContents.length];
                        labelSourceField = new JLabel[labelContents.length];


                        for (int i = 1; i < labelContents.length; i++) {
                            if (i != 2) {
                                labelSource[i] = new JLabel(labelContents[i]);
                                labelSource[i].setBounds(15, 22 * i, 190, 20);
                                upperLeftPanel.add(labelSource[i]);

                                labelSourceField[i - 1] = new JLabel(condetails[i - 1]);
                                labelSourceField[i - 1].setBounds(150, 22 * i, 190, 20);
                                upperLeftPanel.add(labelSourceField[i - 1]);

                            } else {
                                labelSource[i] = new JLabel(labelContents[i]);
                                labelSource[i].setBounds(15, 22 * i, 190, 20);
                                upperLeftPanel.add(labelSource[i]);

                                sourceTextField[i] = new JPasswordField();
                                sourceTextField[i].setBounds(150, 22 * i, 190, 20);
                                upperLeftPanel.add(sourceTextField[i]);
                            }
                        }

                        sourceBackButton.setBounds(15, 160, 160, 20);
                        sourceBackButton.setText("<< Go Back");
                        upperLeftPanel.add(sourceBackButton);

                        sourcePreConButton.setBounds(180, 160, 160, 20);
                        sourcePreConButton.setText("Connect");
                        upperLeftPanel.add(sourcePreConButton);
                    }
                }

            }
        });

//--Source previous details shown after connection------------------------------    
        sourcePreConButton.addActionListener(
                new ActionListener()                                                                                                                                                                                                    {

                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource() == sourcePreConButton) {

                            if (sourceTextField[2].getText().equals("")) {
                                JOptionPane.showMessageDialog(comp, "Please Enter Source Server Password");
                            } else {

                                Properties props = new Properties();
                                try {
                                    File tofolder = new File(System.getProperty("user.dir"));
                                    File propFile = new File(tofolder + "/ServerConnection.properties");
                                    FileInputStream fileIn = new FileInputStream(propFile);
                                    props.load(fileIn);
                                } catch (Exception ee) {
                                    logger.log(CctnsLogger.ERROR, ee);
//                                    ee.printStackTrace();
                                }
                                //========================
                                condetails = new String[7];
                                condetails[0] = props.getProperty("S_userName").toString();
                                condetails[1] = props.getProperty("S_password");
                                condetails[2] = props.getProperty("S_ipAddress");
                                condetails[3] = props.getProperty("S_databaseName");
                                condetails[4] = props.getProperty("S_databaseType");
                                condetails[5] = props.getProperty("S_operatingType");
                                condetails[6] = props.getProperty("S_port");
//                     System.out.println(props.getProperty(condetails[0]));

                                if (sourceTextField[2].getText().equals(condetails[1].toString())) {
                                    try {
                                        String[] values = setHostDetails();
                                        SQLConnection sqlcon = new SQLConnection();
                                        sqlcon.callRetriveMethod(condetails);
                                        sourceORtarget = "source";
                                        Connection conn = sqlcon.getConnectionDetails(sourceORtarget);
                                        if (conn != null) {
                                            JOptionPane.showMessageDialog(comp, "Source Database Connection Established");
                                            connectionSource = conn;
                                            upperLeftPanel.removeAll();
                                            upperLeftPanel.revalidate();
                                            validate();
                                            repaint();
                                            populateCombos();
                                        }
                                    } catch (Exception er) {
                                        JOptionPane.showMessageDialog(comp, "Source Database Connection NOT Established\n\n" + er);
                                        logger.log(CctnsLogger.ERROR, er);
//                                        er.printStackTrace();
                                    }
                                } //.......LEFT PANEL for confirmation of source..................
                                else {
                                    JOptionPane.showMessageDialog(comp, "Password Mismatched !!!");
                                }
                                JLabel newConDetails = new JLabel("Source Server Connection Established as :");

                                newConDetails.setForeground(Color.blue);

                                newConDetails.setBounds(
                                        15, 5, 300, 20);
                                upperLeftPanel.add(newConDetails);
                                //-READ FROM PROPERTY FILE TO DISPLAY CON DETAILS---------------
                                props = new Properties();



                                try {
                                    File tofolder = new File(System.getProperty("user.dir"));
                                    File propFile = new File(tofolder + "/ServerConnection.properties");
                                    FileInputStream fileIn = new FileInputStream(propFile);
                                    props.load(fileIn);
                                    sourceORtarget = "source";
                                    //========================
                                    if (sourceORtarget.equals("source")) {
                                        condetails[0] = props.getProperty("S_userName");
                                        condetails[1] = props.getProperty("S_password");
                                        condetails[2] = props.getProperty("S_ipAddress");
                                        condetails[3] = props.getProperty("S_databaseName");
                                        condetails[4] = props.getProperty("S_databaseType");
                                        condetails[5] = props.getProperty("S_operatingType");
                                        condetails[6] = props.getProperty("S_port");
                                        fileIn.close();
                                    }
                                } catch (Exception ee) {
                                    logger.log(CctnsLogger.ERROR, ee);
//                                    ee.printStackTrace();
                                }

                                for (int i = 3;
                                        i < labelContents.length;
                                        i++) {
                                    labelSource[i] = new JLabel(labelContents[i]);
                                    labelSource[i].setBounds(15, 20 * (i - 2), 140, 20);
                                    upperLeftPanel.add(labelSource[i]);

                                    labelSourceField[i - 1] = new JLabel(condetails[i - 1]);
                                    labelSourceField[i - 1].setBounds(150, 20 * (i - 2), 190, 20);
                                    upperLeftPanel.add(labelSourceField[i - 1]);
                                }
                                //--------------------------------------------------------------
                            }
                        }
                    }
                });

////=SOURCE BACK BUTTON ACTION COMMAND==========================================
        sourceBackButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == sourceBackButton) {
                    upperLeftPanel.removeAll();
                    upperLeftPanel.revalidate();
                    validate();
                    repaint();
                    //--------------------------------------------------------------------
                    JLabel sourceLabel = new JLabel("Source Server");
                    sourceLabel.setForeground(Color.BLUE);
                    sourceLabel.setBounds(30, 2, 120, 50);
                    upperLeftPanel.add(sourceLabel);

                    JLabel sourceConnectStatus = new JLabel("Server Not Connected !!!");

                    sourceNewConnectButton.setText("Connect with New Connection");
                    sourceOldConnectButton.setText("Connect with Previous Connection");

                    sourceConnectStatus.setBounds(30, 60, 300, 20);
                    sourceNewConnectButton.setBounds(30, 100, 300, 20);
                    sourceOldConnectButton.setBounds(30, 150, 300, 20);

                    upperLeftPanel.add(sourceConnectStatus);
                    upperLeftPanel.add(sourceNewConnectButton);
                    upperLeftPanel.add(sourceOldConnectButton);

                }
            }
        });
////=TARGET BACK BUTTON ACTION COMMAND==========================================
        targetBackButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetBackButton) {
                    upperRightPanel.removeAll();
                    upperRightPanel.revalidate();
                    validate();
                    repaint();
                    //--------------------------------------------------------------------
                    JLabel targetLabel = new JLabel("Target Server");
                    targetLabel.setForeground(Color.BLUE);
                    targetLabel.setBounds(30, 2, 120, 50);
                    upperRightPanel.add(targetLabel);

                    JLabel targetConnectStatus = new JLabel("Server Not Connected !!!");
                    targetConnectStatus.setBounds(30, 60, 300, 20);

                    targetNewConnectButton.setText("Connect with New Connection");
                    targetOldConnectButton.setText("Connect with Previous Connection");


                    targetNewConnectButton.setBounds(30, 100, 300, 20);
                    targetOldConnectButton.setBounds(30, 150, 300, 20);

                    upperRightPanel.add(targetConnectStatus);
                    upperRightPanel.add(targetNewConnectButton);
                    upperRightPanel.add(targetOldConnectButton);

                }
            }
        });

// Target old connection action ================================================
        targetOldConnectButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetOldConnectButton) {
                    condetails = new String[7];
                    Properties props = new Properties();
                    try {
                        File tofolder = new File(System.getProperty("user.dir"));
                        File propFile = new File(tofolder + "/ServerConnection.properties");
                        FileInputStream fileIn = new FileInputStream(propFile);
                        props.load(fileIn);
                        //========================
                        condetails[0] = props.getProperty("T_userName");
                        condetails[1] = props.getProperty("T_password");
                        condetails[2] = props.getProperty("T_ipAddress");
                        condetails[3] = props.getProperty("T_databaseName");
                        condetails[4] = props.getProperty("T_databaseType");
                        condetails[5] = props.getProperty("T_operatingType");
                        condetails[6] = props.getProperty("T_port");
                        fileIn.close();
                    } catch (Exception ee) {
                        logger.log(CctnsLogger.ERROR, ee);
//                        ee.printStackTrace();
                    }

                    if (!props.containsKey("T_userName")) {
                        JOptionPane.showMessageDialog(comp, "No Previous Connection !!! \n\n Please Enter New Connection Details");
                    } else {
                        upperRightPanel.removeAll();
                        upperRightPanel.revalidate();
                        validate();
                        repaint();
                        //.......DRAW COMPONENT ON LEFT PANEL.................................
                        JLabel newConDetails = new JLabel("Enter Password for Previous Target Connection");
                        newConDetails.setForeground(Color.blue);
                        newConDetails.setBounds(15, 5, 300, 20);
                        upperRightPanel.add(newConDetails);
                        labelTarget = new JLabel[labelContents.length];
                        labelTargetField = new JLabel[labelContents.length];


                        for (int i = 1; i < labelContents.length; i++) {
                            if (i != 2) {
                                labelTarget[i] = new JLabel(labelContents[i]);
                                labelTarget[i].setBounds(15, 22 * i, 190, 20);
                                upperRightPanel.add(labelTarget[i]);

                                labelTargetField[i - 1] = new JLabel(condetails[i - 1]);
                                labelTargetField[i - 1].setBounds(150, 22 * i, 190, 20);
                                upperRightPanel.add(labelTargetField[i - 1]);

                            } else {
                                labelTarget[i] = new JLabel(labelContents[i]);
                                labelTarget[i].setBounds(15, 22 * i, 190, 20);
                                upperRightPanel.add(labelTarget[i]);

                                targetTextField[i] = new JPasswordField();
                                targetTextField[i].setBounds(150, 22 * i, 190, 20);
                                upperRightPanel.add(targetTextField[i]);
                            }
                        }

                        targetBackButton.setBounds(15, 160, 160, 20);
                        targetBackButton.setText("<< Go Back");
                        upperRightPanel.add(targetBackButton);

                        targetPreConButton.setBounds(180, 160, 160, 20);
                        targetPreConButton.setText("Connect");
                        upperRightPanel.add(targetPreConButton);
                    }
                }
            }
        });

//--Target previous details shown after connection------------------------------    
        targetPreConButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == targetPreConButton) {

                    if (targetTextField[2].getText().equals("")) {
                        JOptionPane.showMessageDialog(comp, "Please Enter Target Server Password");
                    } else {

                        Properties props = new Properties();
                        try {
                            File tofolder = new File(System.getProperty("user.dir"));
                            File propFile = new File(tofolder + "/ServerConnection.properties");
                            FileInputStream fileIn = new FileInputStream(propFile);
                            props.load(fileIn);
                        } catch (Exception ee) {
                            logger.log(CctnsLogger.ERROR, ee);
//                            ee.printStackTrace();
                        }
                        //========================
                        condetails = new String[7];
                        condetails[0] = props.getProperty("T_userName").toString();
                        condetails[1] = props.getProperty("T_password");
                        condetails[2] = props.getProperty("T_ipAddress");
                        condetails[3] = props.getProperty("T_databaseName");
                        condetails[4] = props.getProperty("T_databaseType");
                        condetails[5] = props.getProperty("T_operatingType");
                        condetails[6] = props.getProperty("T_port");
//            System.out.println(props.getProperty(condetails[0]));

                        if (targetTextField[2].getText().equals(condetails[1].toString())) {
                            try {
                                String[] values = setHostDetails();
                                SQLConnection sqlcon = new SQLConnection();
                                sqlcon.callRetriveMethod(condetails);
                                sourceORtarget = "target";
                                Connection conn = sqlcon.getConnectionDetails(sourceORtarget);
                                if (conn != null) {
                                    JOptionPane.showMessageDialog(comp, "Target Database Connection Established");
                                    connectionTarget = conn;
                                    upperRightPanel.removeAll();
                                    upperRightPanel.revalidate();
                                    validate();
                                    repaint();
                                }
                            } catch (Exception er) {
                                JOptionPane.showMessageDialog(comp, "Target Database Connection NOT Established\n\n" + er);
                                logger.log(CctnsLogger.ERROR, er);
                            }
                        } else {
                            JOptionPane.showMessageDialog(comp, "Password Mismatched !!!");
                        }


                        //.......LEFT PANEL for confirmation of source..................
                        JLabel newConDetails = new JLabel("Target Server Connection Established as :");
                        newConDetails.setForeground(Color.blue);
                        newConDetails.setBounds(15, 5, 300, 20);
                        upperRightPanel.add(newConDetails);
                        //-READ FROM PROPERTY FILE TO DISPLAY CON DETAILS---------------
                        props = new Properties();
                        //String conExtbd[] = new String[6];
                        try {
                            File tofolder = new File(System.getProperty("user.dir"));
                            File propFile = new File(tofolder + "/ServerConnection.properties");
                            FileInputStream fileIn = new FileInputStream(propFile);
                            props.load(fileIn);
                            sourceORtarget = "target";
                            //========================
                            if (sourceORtarget.equals("target")) {
                                condetails[0] = props.getProperty("T_userName");
                                condetails[1] = props.getProperty("T_password");
                                condetails[2] = props.getProperty("T_ipAddress");
                                condetails[3] = props.getProperty("T_databaseName");
                                condetails[4] = props.getProperty("T_databaseType");
                                condetails[5] = props.getProperty("T_operatingType");
                                condetails[6] = props.getProperty("T_port");
                                fileIn.close();
                            }
                        } catch (Exception ee) {
                            logger.log(CctnsLogger.ERROR, ee);
//                            ee.printStackTrace();
                        }

                        for (int i = 3; i < labelContents.length; i++) {
                            labelTarget[i] = new JLabel(labelContents[i]);
                            labelTarget[i].setBounds(15, 20 * (i - 2), 140, 20);
                            upperRightPanel.add(labelTarget[i]);

                            labelTargetField[i - 1] = new JLabel(condetails[i - 1]);
                            labelTargetField[i - 1].setBounds(150, 20 * (i - 2), 190, 20);
                            upperRightPanel.add(labelTargetField[i - 1]);
                        }

                        //--------------------------------------------------------------
                    }
                }
            }
        });

        //=====
        timer = new Timer(interval, new ActionListener()                                                                                                {

            public void actionPerformed(ActionEvent evt) {
                if (i == 20) {
                    Toolkit.getDefaultToolkit().beep();
                    timer.stop();
                    String str = "Insertion completed.";
                    statusLabel.setText(str);
                    pb.setVisible(false);
                    statusLabel.setVisible(false);
                }
                i = i + 1;
                pb.setValue(i);
            }
        });

////==LOWER PANEL ACTION COMMANDS======================================================
        migrateButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == migrateButton) {
                    if (!isbothConnectionEstablished()) {
                        JOptionPane.showMessageDialog(comp, "Both Connection Required !!! \n\nPlease connect with both servers first");
                    } else if (comboState.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect State");
                    } else if (comboDistrict.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect District");
                    } else if (comboPS.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect Police Station");
                    } else if (textFieldFromDate.getText().isEmpty() || textFieldToDate.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect From (MM/YYYY) & To (MM/YYYY)");
                    } else {
                        try {
                            ProcessSP processSP = new ProcessSP();
                            String[] selections = setSelected();
                            processSP.setSelections(selections);

                            boolean noRecord = false;
                            try {
                                noRecord = processSP.checkNoRecord(); //Checking data if exists...
                            } catch (SQLException ex) {
                                logger.log(CctnsLogger.ERROR, e);
//                                ex.printStackTrace();
                            }
                            if (noRecord == false) {//No Data Available.
                                JOptionPane.showMessageDialog(comp, "No Record Found!!!\n\nNo record exists for this \nselection in CIPA database");
                            } else {
                                boolean alreadyRun = processSP.checkDuplicateRun();//Checking previous run for same...
                                if (alreadyRun == true) {
                                    JOptionPane.showMessageDialog(comp, "Can't Go Ahead!!!\n\nFor the same selection\nthe data has already been\ntransfered successfully.");
                                } else {
                                    pb.setVisible(true);
                                    Calendar date = Calendar.getInstance();
                                    SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddhhmmss");
                                    datestr = dateformatter.format(date.getTime());

                                    batchCD = "CIPA_" + keyState.toString().trim() + keyDistrict.toString().trim() + keyPS.toString().trim() + "_" + textFieldFromDate.getText().trim() + "_" + datestr.trim();
//                                    System.out.println(batchCD);

                                    ArrayList statusR = processSP.checkStatusR();//Checking failed data...
                                    if (statusR.size() != 0) {
                                        processSP.runForStatusR(statusR);//Removing failed data...

                                    }

                                    String location;
                                    location = keyState.toString().trim() + keyDistrict.toString().trim() + keyPS.toString().trim();
//                                    from = textFieldFromDate.getText().trim();
//                                    to = textFieldToDate.getText().trim();

                                    String from = textFieldFromDate.getText().trim().substring(3, 7) + textFieldFromDate.getText().trim().substring(0, 2);//textFieldFromDate.getText().trim();
                                    String to = textFieldToDate.getText().trim().substring(3, 7) + textFieldToDate.getText().trim().substring(0, 2);

//                                    System.out.println("from :" + from);
//                                    System.out.println("to : " + to);

                                    ArrayList YYMMarrayList = processSP.rangeFromToYYYYMM(from, to);

                                    String nameLocation[] = new String[3];
                                    nameLocation[0] = selectedState;
                                    nameLocation[1] = selectedDistrict;
                                    nameLocation[2] = selectedPS;
                                    try {
                                        processSP.registerTheRunning(batchCD, YYMMarrayList, location, nameLocation);
                                    } catch (SQLException ex) {
//                                        ex.printStackTrace();
                                        logger.log(CctnsLogger.ERROR, e);

                                    }
                                    //========
                                    String str = "Insertion is in process.......";
                                    statusLabel.setText(str);
                                    statusLabel.setVisible(true);
                                    timer.start();

                                    pb.setVisible(true);
                                    statusLabel.setVisible(false);
                                    //========

                                    //-Deletions----------------------------------------
                                    DeleteData deleteData = new DeleteData();
                                    deleteData.executeDeleteBlock();
                                    //-Selects------------------------------------------                        
                                    SelectData selectData = new SelectData();
                                    selectData.setSelections(selections);
                                    String selectQueries[] = selectData.select();
                                    //-Migrations---------------------------------------
                                    MigrateData migrate = new MigrateData();
                                    migrate.setSelections(selections);
                                    String string[] = null;
                                    try {
                                        System.out.println("selectQueries"+selectQueries);
                                        string = migrate.executeInsertsBlock(selectQueries, batchCD);
                                    } catch (Exception ex) {
//                                        ex.printStackTrace();
                                        logger.log(CctnsLogger.ERROR, e);

                                    }
                                    //calling Stored Procedures-------------------------
                                    processSP.SP_from_CIPAtempDB_TO_Stagging(batchCD, string);//SP_Load_CIPA_Data

                                    processSP.callAllStoredProcedures(batchCD);
                                    processSP.callCheckSum(batchCD);
                                    // call SpChecksum_Landing_to_Target with batchcd
                                    //---------------------------------------------------
                                    try {
                                        String filename = null;
                                        processSP.CALL_SP_Table_Record_Count(filename, batchCD, location, datestr, string);//Finalizing process...
                                        JOptionPane.showMessageDialog(content, "Data Migrated Successfully!!!" + "\n" + " Please see the generated LOG File " + "\n" + " for complete Status");
                                        pb.setVisible(false);
                                    } catch (Exception ee) {
                                        logger.log(CctnsLogger.ERROR, ee);
//                                        ee.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            logger.log(CctnsLogger.ERROR, ex);
//                            ex.printStackTrace();
                        }
                    }
                }
            }
        });


        downloadButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == downloadButton) {
                    if (isSourceConnectionEstablished() == false) {
                        JOptionPane.showMessageDialog(comp, "Source Connection Required !!! \n\nPlease connect with source server first");
                    } else if (comboState.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect State");
                    } else if (comboDistrict.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect District");
                    } else if (comboPS.getSelectedIndex() == 0) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect Police Station");
                    } else if (textFieldFromDate.getText().isEmpty() || textFieldToDate.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(comp, "No Selection !!!\n\nSelect From (MM/YYYY) & To (MM/YYYY)");
                    } else if (textFieldSaveAt.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(comp, "No Path Selection !!! ");
                    } else {
                        String[] selections = setSelected();
                        //-Selects---------------------------------------------                        
                        SelectData selectData = new SelectData();
                        selectData.setSelections(selections);
                        String selectQueries[] = selectData.select();
                        //-Downloads---------------------------------------------
                        DownloadData download = new DownloadData();
                        download.setSelections(selections);
                        String toFile = fileSequel.toString();
                        try {
                            download.executeDownloadBlock(toFile, selectQueries);
                            JOptionPane.showMessageDialog(comp, "Data Downloaded Successfully!!!\n\nA .sql file has generated at selected folder.");

                        } catch (Exception ee) {
                            logger.log(CctnsLogger.ERROR, ee);
//                            ee.printStackTrace();
                        }
                    }
                }
            }
        });

        uploadButton.addActionListener(new ActionListener()                                                                                                                                                                                                    {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == uploadButton) {
                    if (isTargetConnectionEstablished() == false) {
                        JOptionPane.showMessageDialog(comp, "Target Connection Required !!! \n\nPlease connect with Target servers first");
                    } else if (textFieldUploadFrom.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(comp, "No File Selection !!! \n\nPlease select SQL data file to upload.");
                    } else {
                        //--
                        try {
                            //-Deletions----------------------------------------
                            DeleteData deleteData = new DeleteData();
                            deleteData.executeDeleteBlock();
                            //-Uploading to temp--------------------------------
                            UploadData uploadData = new UploadData();
                            String withPathfileName = textFieldUploadFrom.getText();

                            String nameLocation[] = uploadData.executeUploadBlock(withPathfileName);

                            int beginIndex = withPathfileName.lastIndexOf(System.getProperty("file.separator")) + 1; //changed to accomodate file system path for Linux
                            int endIndex = withPathfileName.indexOf('.');
                            String fileName = withPathfileName.substring(beginIndex, endIndex);

                            String location = null, from = null, to = null;
                            String parts[] = fileName.split("_");
                            for (int i = 0; i < parts.length; i++) {
                                if (i == 0) {
                                    location = parts[i];
                                }
                                if (i == 1) {
                                    from = parts[i].substring(0, 6);
                                    to = parts[i].substring(6, 12);
                                }
                            }

                            ProcessSP processSP = new ProcessSP();
                            ArrayList YYMMarrayList = processSP.rangeFromToYYYYMM(from, to);

                            //added by Vikas
                            //JOptionPane.showMessageDialog(comp, "Data moved to tempd db,!!!\n\nProgram will exit now.");
                            //System.exit(0);
                            //added by Vikas
                            boolean alreadyRun = processSP.checkDuplicateUpload(YYMMarrayList);//Checking previous run for same...
                            if (alreadyRun == true) {
                                JOptionPane.showMessageDialog(comp, "Can't Go Ahead!!!\n\nFor the same selection\nthe data has already been\ntransfered successfully.");
                            } else {
//                                //=====checking 
                                Calendar date = Calendar.getInstance();
                                SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddhhmmss");
                                datestr = dateformatter.format(date.getTime());
                                pb.setVisible(true);
                                batchCD = "CIPA_" + location + "_" + from.substring(4, 6) + "-" + from.substring(0, 4) + "_" + datestr.trim();
//                                System.out.println(batchCD);

                                ArrayList statusR = processSP.checkStatusR_Upload(YYMMarrayList, location);//Checking failed data...
                                if (statusR.size() != 0) {
                                    processSP.runForStatusR(statusR);//Removing failed data...
                                }

                                processSP.registerTheRunning(batchCD, YYMMarrayList, location, nameLocation);

                                //calling Stored Procedures-------------------------
                                String string[] = processSP.Tables_Record_Count();
                                processSP.SP_from_CIPAtempDB_TO_Stagging(batchCD, string);//SP_Load_CIPA_Data

                                processSP.callAllStoredProcedures(batchCD);
                                processSP.callCheckSum(batchCD);
                                // call SpChecksum_Landing_to_Target with batchcd
                                //---------------------------------------------------
//                                String string[] = processSP.Tables_Record_Count();
                                processSP.CALL_SP_Table_Record_Count(fileName, batchCD, location, datestr, string);//Finalizing process...

                                JOptionPane.showMessageDialog(comp, "Data Uploaded Successfully!!!\n\nA log file has generated for details");
                                pb.setVisible(false);
//                                    //=====checking
                            }
                        } catch (Exception ex) {
                            logger.log(CctnsLogger.ERROR, ex);
//                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        browseSaveAtButton.addActionListener(
                new ActionListener()                                                                                                                                                                                                   {

                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource() == browseSaveAtButton) {
                            JFileChooser chooser;
                            String path;
                            chooser = new JFileChooser();

                            Calendar date = Calendar.getInstance();
                            SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddhhmmss");
                            datestr = dateformatter.format(date.getTime());
 
                            chooser.setCurrentDirectory(new java.io.File("."));
                            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                            chooser.setAcceptAllFileFilterUsed(false);
                            //    
                            if (chooser.showOpenDialog(browseSaveAtButton) == JFileChooser.APPROVE_OPTION) {
                                path = chooser.getSelectedFile().toString();
                            } else {
                                path = tofolder.toString();
                                JOptionPane.showMessageDialog(comp, "No Path Selection !!! \n\nSql File will be Saved at \n" + path + "");
                            }
                            String from = textFieldFromDate.getText().trim().substring(3, 7) + textFieldFromDate.getText().trim().substring(0, 2);//textFieldFromDate.getText().trim();
                            String to = textFieldToDate.getText().trim().substring(3, 7) + textFieldToDate.getText().trim().substring(0, 2);

                            fileSequel = new File(path + "/" + keyState + keyDistrict + keyPS + "_" + from + to + "_" + datestr + ".sql");
                            textFieldSaveAt.setText(fileSequel.toString());//0816756_201011201011_null.sql
//                            System.out.println(fileSequel);
                        }
                    }
                });


        browseSelectFromButton.addActionListener(
                new ActionListener()                                                                                                                                                                                                   {

                    public void actionPerformed(ActionEvent e) {
                        if (e.getSource() == browseSelectFromButton) {
                            JFileChooser chooser;
                            String path;
                            chooser = new JFileChooser();

                            chooser.setCurrentDirectory(new java.io.File("."));
                            //    
                            if (chooser.showOpenDialog(browseSaveAtButton) == JFileChooser.APPROVE_OPTION) {
                                path = chooser.getSelectedFile().toString();
                                if (path.substring(path.length() - 3, path.length()).equalsIgnoreCase("sql")) {
//                                    System.out.println("Ok");
                                    textFieldUploadFrom.setText(path);
                                } else {
                                    path = null;
                                    JOptionPane.showMessageDialog(comp, "Not a SQL File !!! \n\nPlease select a SQL Data file");
                                }
                            }
                        }
                    }
                });
    }

    /**
     * 
     */
    public void displayLowerPanelComponentsForSource() {
        validate();
        repaint();

        labelFromDate.setVisible(true);
        labelToDate.setVisible(true);
        labelState.setVisible(true);
        labelDistrict.setVisible(true);
        labelPS.setVisible(true);
        comboState.setVisible(true);
        downloadButton.setVisible(true);
        fromCalander.setVisible(true);
        toCalander.setVisible(true);
        textFieldFromDate.setVisible(true);
        textFieldToDate.setVisible(true);
        uploadButton.setVisible(false);
        labelSaveAt.setVisible(true);
        textFieldSaveAt.setVisible(true);
        browseSaveAtButton.setVisible(true);
        labeluploadFrom.setVisible(false);
        textFieldUploadFrom.setVisible(false);
        browseSelectFromButton.setVisible(false);
        comboDistrict.setVisible(true);
        comboState.setVisible(true);
        comboPS.setVisible(true);
    }

    public void displayLowerPanelComponentsForTarget() {
        validate();
        repaint();

        labelFromDate.setVisible(false);
        labelToDate.setVisible(false);
        labelState.setVisible(false);
        labelDistrict.setVisible(false);
        labelPS.setVisible(false);

        comboState.setVisible(false);
        downloadButton.setVisible(false);
        migrateButton.setVisible(false);
        fromCalander.setVisible(false);
        toCalander.setVisible(false);
        textFieldFromDate.setVisible(false);
        textFieldToDate.setVisible(false);
        uploadButton.setVisible(true);
        labelSaveAt.setVisible(false);
        textFieldSaveAt.setVisible(false);
        browseSaveAtButton.setVisible(false);
        labeluploadFrom.setVisible(true);
        textFieldUploadFrom.setVisible(true);
        browseSelectFromButton.setVisible(true);
        comboDistrict.setVisible(false);
        comboState.setVisible(false);
        comboPS.setVisible(false);
    }

    public void displayLowerPanelComponentsForBoth() {
        validate();
        repaint();

        labelFromDate.setVisible(true);
        labelToDate.setVisible(true);
        labelState.setVisible(true);
        labelDistrict.setVisible(true);
        labelPS.setVisible(true);
        comboState.setVisible(true);
        downloadButton.setVisible(false);
        uploadButton.setVisible(false);
        migrateButton.setVisible(true);
        fromCalander.setVisible(true);
        toCalander.setVisible(true);
        textFieldFromDate.setVisible(true);
        textFieldToDate.setVisible(true);
        labelSaveAt.setVisible(false);
        textFieldSaveAt.setVisible(false);
        browseSaveAtButton.setVisible(false);
        labeluploadFrom.setVisible(false);
        textFieldUploadFrom.setVisible(false);
        browseSelectFromButton.setVisible(false);
        comboDistrict.setVisible(true);
        comboState.setVisible(true);
        comboPS.setVisible(true);
    }

    //============================================================================
    /**
     * 
     * @return
     */
    public String[] setHostDetails() {
        for (int i = 0; i < condetails.length; i++) {
//            System.out.println("str[sending] " + condetails[i]);    //marked
        }
        return condetails;
    }

//==============================================================================
    /**
     * 
     * @param args
     * @throws IOException
     * @throws SQLException
     */
    public static void main(String[] args) throws IOException, SQLException {
        //==========
        long heapSize = Runtime.getRuntime().totalMemory();
        System.getProperties().put("<heap variable>", heapSize);
        //==========        
        CIPAServerMain select = new CIPAServerMain();
        select.setResizable(false);
        select.setBounds(140, 100, 720, 430);

        select.setVisible(true);
        select.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public String[] setSelected() {
        s[0] = keyState.toString();
        s[1] = keyDistrict.toString();
        s[2] = keyPS.toString();
        s[3] = selectedState;
        s[4] = selectedDistrict;
        s[5] = selectedPS;
        s[6] = textFieldFromDate.getText().trim().substring(0, 2); //from month 
        s[7] = textFieldFromDate.getText().trim().substring(3, 7); //from year
        s[8] = textFieldToDate.getText().trim().substring(0, 2);   //to month
        s[9] = textFieldToDate.getText().trim().substring(3, 7);   //to year 

//        for (int i = 0; i < s.length; i++) {
//            System.out.println("sending" + s[i]);
//        }
        return s;
    }

    public boolean isbothConnectionEstablished() {
        boolean isBothConnected = false;
        if (connectionSource != null && connectionTarget != null) {
            isBothConnected = true;
        }
        return isBothConnected;
    }

    public boolean isSourceConnectionEstablished() {
        boolean isSourceConnected = false;
        if (connectionSource != null) {
            isSourceConnected = true;
        }
        return isSourceConnected;
    }

    public boolean isTargetConnectionEstablished() {
        boolean isTargetConnected = false;
        if (connectionTarget != null) {
            isTargetConnected = true;
        }
        return isTargetConnected;
    }

    public void populateCombos() {
        try {
            //...populate data in combobox of lower panel.......................................................................
            String sqlState = "select s.state_code, s.state_name "
                    + "from t011_state s,(select distinct location from t1_registration) r "
                    + "where s.state_code = RTRIM(substring(r.location, 1, 2))";

            SQLConnection sqlcon = new SQLConnection();
            sourceORtarget = "source";
            Connection conn = sqlcon.SQLCon(sourceORtarget);

            connectionSource = conn;
            PreparedStatement pstmtState = connectionSource.prepareStatement(sqlState);
            ResultSet rsetState = pstmtState.executeQuery();

            hashMapState.clear();
            hashMapState.put("0001", "--Select--");
            while (rsetState.next()) {
                hashMapState.put(rsetState.getString(1), rsetState.getString(2));
            }
            rsetState.close();
            Map sortedMap = new TreeMap(hashMapState);

            Set setState = sortedMap.entrySet();
            Iterator itrState = setState.iterator();
            while (itrState.hasNext()) {
                Map.Entry meState = (Map.Entry) itrState.next();
                comboState.addItem(meState.getValue());
            }

            //...for district after selection on state...........
            comboState.addActionListener(new ActionListener()                                                                                                                                                                  {

                public void actionPerformed(ActionEvent e) {
                    if (e.getSource() == comboState && connectionSource != null) {
                        comboDistrict.removeAllItems();
                        Object obgState = comboState.getSelectedItem();
                        selectedState = obgState.toString();

                        if (comboState.getSelectedItem() != null) {
                            Object obgDistrict = comboState.getSelectedItem();
                            selectedDistrict = obgDistrict.toString();

                            for (Object o : hashMapState.keySet()) {
                                if (hashMapState.get(o).equals(selectedState)) {
                                    keyState = o;
                                }
                            }
                            String sqlDistrict = " select d.district_code, d.district_name from t012_district d,"
                                    + "(select distinct location from t1_registration) r "
                                    + "where d.district_code = RTRIM(substring(r.location, 3, 3)) and "
                                    + "d.state_code = RTRIM(substring(r.location, 1, 2))";


                            PreparedStatement pstmtDistrict;
                            try {
                                pstmtDistrict = connectionSource.prepareStatement(sqlDistrict);
                                ResultSet rsetDistrict = pstmtDistrict.executeQuery();

                                hashMapDistrict.clear();
                                hashMapDistrict.put("0001", "--Select--");
                                while (rsetDistrict.next()) {
                                    hashMapDistrict.put(rsetDistrict.getString(1), rsetDistrict.getString(2));
                                }
                                rsetDistrict.close();

                                Map sortedMap = new TreeMap(hashMapDistrict);

                                Set setDistrict = sortedMap.entrySet();
                                Iterator itrDistrict = setDistrict.iterator();
                                while (itrDistrict.hasNext()) {
                                    Map.Entry me = (Map.Entry) itrDistrict.next();
                                    comboDistrict.addItem(me.getValue());
                                }
                            } catch (SQLException ex) {
                                logger.log(CctnsLogger.ERROR, ex);
//                                                                Logger.getLogger(SynchServer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                        //...for ps after selection on district......
                        comboDistrict.addActionListener(new ActionListener()                                                                                                                                                                  {

                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == comboDistrict) {
                                    comboPS.removeAllItems();
                                    String sqlPS = "";

                                    if (comboDistrict.getSelectedItem() != null) {
                                        Object obgDistrict = comboDistrict.getSelectedItem();
                                        selectedDistrict = obgDistrict.toString();

                                        for (Object o : hashMapDistrict.keySet()) {
                                            if (hashMapDistrict.get(o).equals(selectedDistrict)) {
//                                             System.out.println(o);
                                                keyDistrict = o;
                                                sqlPS = "select ps.ps_code, ps.ps_name from t013_policestation ps,"
                                                        + "(select distinct location from t1_registration) r "
                                                        + "where ps.district_code = RTRIM(substring(r.location, 3, 3)) and "
                                                        + "ps.ps_code = RTRIM(substring(r.location, 6, 2)) ";
                                                PreparedStatement pstmtPS;
                                                try {
                                                    pstmtPS = connectionSource.prepareStatement(sqlPS);
                                                    ResultSet rsetPS = pstmtPS.executeQuery();

                                                    hashMapPS.clear();
                                                    hashMapPS.put("0001", "--Select--");
                                                    while (rsetPS.next()) {
                                                        hashMapPS.put(rsetPS.getString(1), rsetPS.getString(2));
                                                    }
                                                    rsetPS.close();
                                                    Map sortedMap = new TreeMap(hashMapPS);

                                                    Set setPS = sortedMap.entrySet();
                                                    Iterator itrPS = setPS.iterator();
                                                    while (itrPS.hasNext()) {
                                                        Map.Entry mePS = (Map.Entry) itrPS.next();
                                                        comboPS.addItem(mePS.getValue());
                                                    }

                                                } catch (SQLException ex) {
                                                    logger.log(CctnsLogger.ERROR, ex);
//                                                                                    Logger.getLogger(SynchServer.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        });

                        //...for ps after selection on district......
                        comboPS.addActionListener(new ActionListener()                                                                                                                                                                  {

                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == comboPS) {
                                    if (comboPS.getSelectedItem() != null) {
                                        Object obgPS = comboPS.getSelectedItem();
                                        selectedPS = obgPS.toString();

                                        for (Object o : hashMapPS.keySet()) {
                                            if (hashMapPS.get(o).equals(selectedPS)) {
                                                keyPS = o;
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        //.............................................
                    } else {
//                        System.out.println("hoho");
                    }
                }
            });
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }


    }
}
//    {
//        try {
//            System.out.println("***********" + Thread.currentThread().getContextClassLoader().loadClass("synchserver/FetchInsert"));
//
//
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(SynchServer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
