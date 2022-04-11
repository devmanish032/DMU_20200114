/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.mysql.jdbc.CallableStatement;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import logger.CctnsLogger;

/**
 *
 * @author Administrator
 */
public class MigrateData {

    String sourceORtarget;
    public Connection connectionSource, connectionTarget;
    String selectedValues[];
    String selectedState, selectedDistrict, selectedPS,
            selectedFromMM, selectedFromYYYY, selectedToMM, selectedToYYYY;
    String keyState, keyDistrict, keyPS;
    Date dtBegTime, dtEndTime;
    public String[] str = new String[10];
    String batchCD;
    public String insertingTables[];
    int recordsFound[];
    int recordsInserted[];
    String datestr;
    SQLConnection sqlcon;
    JComponent comp;
    String prefixStagging;
    String prefixTempDB;
    private static CctnsLogger logger = CctnsLogger.getInstance(MigrateData.class.getName());

    public MigrateData() {
        java.sql.DatabaseMetaData dmd = null;
        try {
            sourceORtarget = "target";
            SQLConnection sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            dmd = connectionTarget.getMetaData();
            if (!(dmd.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
                //For MySql
                prefixStagging = "cipa_staging";
                prefixTempDB = "CIPATemp_DB.";
            } else {
                //For MSSQL
                prefixStagging = "cipa_staging";
                prefixTempDB = "CIPATemp_DB.dbo.";
            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
            //e.printStackTrace();
        }
    }

    public void setSelections(String[] selectedValues) {
        for (int i = 0; i < selectedValues.length; i++) {
            str[i] = selectedValues[i];
        }
        keyState = str[0];
        keyDistrict = str[1];
        keyPS = str[2];
        selectedState = str[3];
        selectedDistrict = str[4];
        selectedPS = str[5];
        selectedFromMM = str[6];
        selectedFromYYYY = str[7];
        selectedToMM = str[8];
        selectedToYYYY = str[9];
    }

    /**
     * 
     * @throws SQLException
     */
//==============================================================================
    /**
     * 
     * @throws SQLException
     */
    public String[] executeInsertsBlock(String selectStringSql[], String batchCode) throws SQLException {
        PreparedStatement pstmtMetaSelect, pstmtSelect, pstmtInsert;
        ResultSet rsetSelect = null;
        ResultSet rsetCount = null;
        String insertString = "";
//------------------------------------------------------------------------------    
        String insertingTables[] = new String[selectStringSql.length];
        int recordsFound[] = new int[selectStringSql.length];
        int recordsInserted[] = new int[selectStringSql.length];
//..............................................................................
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateformatter = new SimpleDateFormat("yyyyMMddhhmmss");
        datestr = dateformatter.format(date.getTime());

//        batchCD = "" + keyPS.toString().trim() + "_" + selectedYear + "_" + datestr + "";
//..............................................................................        
        java.sql.DatabaseMetaData dmdBeginTime = null;
        sourceORtarget = "source";
        sqlcon = new SQLConnection();
        connectionSource = sqlcon.SQLCon(sourceORtarget);

        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);

        dmdBeginTime = connectionTarget.getMetaData();
        Statement stmtBeginTime = connectionTarget.createStatement();
        ResultSet rsetBeginTime;
        if (!(dmdBeginTime.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
            rsetBeginTime = stmtBeginTime.executeQuery("select now()");
            rsetBeginTime.next();
            dtBegTime = rsetBeginTime.getTimestamp(1);
        } else {
            rsetBeginTime = stmtBeginTime.executeQuery("select getdate()");
            rsetBeginTime.next();
            dtBegTime = rsetBeginTime.getTimestamp(1);
        }
//..............................................................................        

        //-number of records iterated by jk variable--------------------------------
        for (int jk = 0; jk < selectStringSql.length; jk++) {
//            System.out.println((jk + 1) + " / " + selectStringSql.length);
            StringBuffer buffterMeta = new StringBuffer();
            StringBuffer buffterValue = null;
            HashMap hm = new HashMap();
            HashMap getRejectHM = new HashMap();

lbl_Next_Statement:  
        try {
                pstmtMetaSelect = connectionSource.prepareStatement(selectStringSql[jk].toString());
                ResultSetMetaData rsetMetaSelect = pstmtMetaSelect.getMetaData();
//                System.out.println("Query" + selectStringSql[jk].toString());
                pstmtSelect = connectionSource.prepareStatement(selectStringSql[jk].toString());
                rsetCount = pstmtSelect.executeQuery();
                int selectCount = 0;
                while (rsetCount.next()) {
                    selectCount++;
                }
                rsetSelect = pstmtSelect.executeQuery();
//                System.out.println("Number of Rows Found=" + selectCount);
                recordsFound[jk] = selectCount;
                //-number of columns are fetched with name and type---------------------
                int insertCounter = 0;
                if (selectCount != 0) {
                    String insertingTable = null;
                    String respective_Reg_Field = null;
                    while (rsetSelect.next()) {
                        try {
                            buffterValue = new StringBuffer();
                            for (int ij = 1; ij <= rsetMetaSelect.getColumnCount(); ij++) {
                                if (insertCounter == 0) {
                                    buffterMeta.append(rsetMetaSelect.getColumnName(ij) + ", ");
                                    hm.put(rsetMetaSelect.getColumnName(ij), rsetMetaSelect.getColumnTypeName(ij));
                                }

//                            System.out.println(rsetMetaSelect.getColumnLabel(ij) + "===>" + hm.get(rsetMetaSelect.getColumnName(ij)));
                                if (hm.get(rsetMetaSelect.getColumnName(ij)).toString().equalsIgnoreCase("bytea")) {
                                    buffterValue.append("'',");
                                } else {
                                    if ((rsetSelect.getString(rsetMetaSelect.getColumnName(ij)) == null)) {
//            System.out.println("null found");
                                        buffterValue.append("" + "NULL" + ",");
                                    } else {
                                        if ((hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("DATETIME")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("DATE")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("BPCHAR")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("IMAGE")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("NCHAR")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("NTEXT")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("TIMESTAMP")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("bool")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("varchar")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("TEXT")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("NVARCHAR")
                                                || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("CHAR")) {
                                            buffterValue.append(" N'" + refineData(rsetSelect.getString(ij)) + "',");
                                        } else {
                                            buffterValue.append("" + rsetSelect.getString(ij) + ",");
                                        }
                                        getRejectHM.put(rsetMetaSelect.getColumnName(ij), rsetSelect.getString(ij));
//                                        System.out.println(rsetMetaSelect.getColumnName(ij)+";"+rsetSelect.getString(ij));
                                    }
                                }
                            }

                            if (insertCounter
                                    == 0) {
                                buffterMeta.deleteCharAt(buffterMeta.lastIndexOf(","));
                            }

                            buffterValue.deleteCharAt(buffterValue.lastIndexOf(","));



                            switch (jk) {
                                case 0:
                                    insertString = "INSERT INTO " + prefixTempDB + "t015_psstaffcurr (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t015_psstaffcurr";
                                    respective_Reg_Field = "pis_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;                                  
                                case 1:
                                    insertString = "INSERT INTO " + prefixTempDB + "t014_policestationbeat (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t014_policestationbeat";
                                    respective_Reg_Field = "beat_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 2:
                                    insertString = "INSERT INTO " + prefixTempDB + "t1_registration (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1_registration";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 3:
                                    insertString = "INSERT INTO " + prefixTempDB + "t201_fir (" + buffterMeta.toString() + ") values (" + chekUpdate(buffterValue.toString()) + ")";
                                    insertingTable = "t201_fir";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                    
                                case 4:
                                    insertString = "insert into  " + prefixTempDB + "t311_transfer (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t311_transfer";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 5:
                                    insertString = "insert into  " + prefixTempDB + "t101_actsection  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t101_actsection";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 6:
                                    insertString = "insert into " + prefixTempDB + "t301_crime  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t301_crime";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 7:
                                    insertString = "insert into " + prefixTempDB + "t301b_crime  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t301b_crime";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 8:
                                    insertString = "insert into " + prefixTempDB + "t102_Person (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t102_Person";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 9:
                                    insertString = "insert into " + prefixTempDB + "t102_Person (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t102_Person";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                
                                case 10:
                                    insertString = "insert into " + prefixTempDB + "t1021_Personal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1021_Personal";
                                    respective_Reg_Field = "person_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 11:
                                    insertString = "insert into " + prefixTempDB + "t1021_Personal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1021_Personal";
                                    respective_Reg_Field = "person_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 12:
                                    insertString = "insert into " + prefixTempDB + "t2013_victim (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t2013_victim";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 13:
                                    insertString = "insert into " + prefixTempDB + "t305_witness (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t305_witness";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 14:
                                    insertString = "insert into " + prefixTempDB + "t2011_accused (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t2011_accused";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 15:
                                    insertString = "INSERT INTO " + prefixTempDB + "t303_arrest (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t303_arrest";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 16:
                                    insertString = "insert into " + prefixTempDB + "t10221_physical (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t10221_physical";
                                    respective_Reg_Field = "person_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 17:
                                    insertString = "insert into " + prefixTempDB + "t10222_physical (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t10222_physical";
                                    respective_Reg_Field = "person_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 18:
                                    insertString = "insert into " + prefixTempDB + "t103_properties (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t103_properties";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 19:
                                    insertString = "insert into " + prefixTempDB + "t1031_automobile (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1031_automobile";
                                    respective_Reg_Field = "property_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 20:
                                    insertString = "insert into " + prefixTempDB + "t304a_seizure (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t304a_seizure";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 21:
                                    insertString = "insert into " + prefixTempDB + "t1034a_currency (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1034a_currency";
                                    respective_Reg_Field = "property_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 22:
                                    insertString = "insert into " + prefixTempDB + "t1034b_currency (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1034b_currency";
                                    respective_Reg_Field = "t1034b_currency";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 23:
                                    insertString = "insert into " + prefixTempDB + "t1032_cultural (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1032_cultural";
                                    respective_Reg_Field = "property_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 24:
                                    insertString = "insert into " + prefixTempDB + "t1035_narcotics (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1035_narcotics";
                                    respective_Reg_Field = "property_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 25:
                                    insertString = "insert into " + prefixTempDB + "t1033_numbered (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t1033_numbered";
                                    respective_Reg_Field = "property_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 26:
                                    insertString = "insert into " + prefixTempDB + "t312_finalreport (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t312_finalreport";
                                    respective_Reg_Field = "fr_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 27:
                                    insertString = "insert into " + prefixTempDB + "t312b_fraccused (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t312b_fraccused";
                                    respective_Reg_Field = "fr_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 28:
                                    insertString = "insert into " + prefixTempDB + "t312c_fractsec (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t312c_fractsec";
                                    respective_Reg_Field = "fr_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 29:
                                    insertString = "insert into " + prefixTempDB + "t3034_remandcustody (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t3034_remandcustody";
                                    respective_Reg_Field = "arrest_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 30:
                                    insertString = "insert into " + prefixTempDB + "t3033_bail (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t3033_bail";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 31:
                                    insertString = "insert into " + prefixTempDB + "t406a_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t406a_courtdisposal";
                                    respective_Reg_Field = "fr_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 32:
                                    insertString = "insert into " + prefixTempDB + "t406b_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t406b_courtdisposal";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 33:
                                    insertString = "insert into " + prefixTempDB + "t406c_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t406c_courtdisposal";
                                    respective_Reg_Field = "fr_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 34:
                                    insertString = "insert into " + prefixTempDB     + "t202_missing (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t202_missing";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 35:
                                    insertString = "insert into " + prefixTempDB + "t205_unnatural (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t205_unnatural";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 36:
                                    insertString = "insert into " + prefixTempDB + "t204_mlc (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t204_mlc";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 37:
                                    insertString = "insert into " + prefixTempDB + "t207_others (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t207_others";
                                    respective_Reg_Field = "regn_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 38:
                                    insertString = "insert into " + prefixTempDB + "t501a_criminal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t501a_criminal";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 39:
                                    insertString = "insert into " + prefixTempDB + "t5011_criaddress (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5011_criaddress";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 40:
                                    insertString = "insert into " + prefixTempDB + "t5027_bank (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5027_bank";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 41:
                                    insertString = "insert into " + prefixTempDB + "t5012_criknowns (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5012_criknowns";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 42:
                                    insertString = "insert into " + prefixTempDB + "t5023_operationarea (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5023_operationarea";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 43:
                                    insertString = "insert into " + prefixTempDB + "t5021_general (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5021_general";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 44:
                                    insertString = "insert into " + prefixTempDB + "t5022_affiliation (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5022_affiliation";
                                    respective_Reg_Field = "crim_srno";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 45:
                                    insertString = "insert into " + prefixTempDB + "t5026_employment (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5026_employment";
                                    respective_Reg_Field = "empl_sr";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 46:
                                    insertString = "insert into " + prefixTempDB + "t5025_political (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5025_political";
                                    respective_Reg_Field = "politic_sr";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 47:
                                    insertString = "insert into " + prefixTempDB + "t5024_notices (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5024_notices";
                                    respective_Reg_Field = "notice_sr";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 48:
                                    insertString = "insert into " + prefixTempDB + "t503_gang (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t503_gang";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 49:
                                    insertString = "insert into " + prefixTempDB + "t5037_frontalorg (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5037_frontalorg";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 50:
                                    insertString = "insert into " + prefixTempDB + "t5032_support (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5032_support";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 51:
                                    insertString = "insert into " + prefixTempDB + "t5034_transport (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5034_transport";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 52:
                                    insertString = "insert into " + prefixTempDB + "t5033_holdings (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5033_holdings";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 53:
                                    insertString = "insert into " + prefixTempDB + "t5035_training (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5035_training";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                case 54:
                                    insertString = "insert into " + prefixTempDB + "t5036_hideouts (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t5036_hideouts";
                                    respective_Reg_Field = "gang_code";
                                    //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                               
                                case 55:
                                    insertString = "insert into " + prefixTempDB + "t099_generaldiary (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t099_generaldiary";
                                    respective_Reg_Field = "gd_srno";
//                                    System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                
                                case 56:
                                    insertString = "insert into " + prefixTempDB + "t3_caseprogress (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t3_caseprogress";
                                    respective_Reg_Field = "regn_srno";
//                                   System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                    
                                  
                     
                                 case 57:
                                    insertString = "insert into " + prefixTempDB + "t304b_seizure (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                    insertingTable = "t304b_seizure";
                                    respective_Reg_Field = "regn_srno";
//                                  //System.out.println(insertString);
                                    pstmtInsert = connectionTarget.prepareStatement(insertString);
                                    insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                    break;
                                    
                                 case 58:
                                     insertString = "insert into " + prefixTempDB + "t2012_properties (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                     insertingTable = "t2012_properties";
                                     respective_Reg_Field = "regn_srno";
                                     //System.out.println(insertString);
                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                     break;
                                     
                                 case 59:
                                     insertString = "insert into " + prefixTempDB + "t015_psstaffold (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                     insertingTable = "t015_psstaffold";
                                     respective_Reg_Field = "pis_code";
                                     //System.out.println(insertString);
                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                     break;
                                     
                                 case 60:
                                     insertString = "insert into " + prefixTempDB + "t011_state (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                     insertingTable = "t011_state";
                                     respective_Reg_Field = "state_code";
                                     //System.out.println(insertString);
                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                     break;
                                     
                                 case 61:
                                     insertString = "insert into " + prefixTempDB + "t012_district (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                     insertingTable = "t012_district";
                                     respective_Reg_Field = "district_code";
                                     //System.out.println(insertString);
                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                     break;
                                     
                                 case 62:
                                     insertString = "insert into " + prefixTempDB + "t013_policestation (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
                                     insertingTable = "t013_policestation";
                                     respective_Reg_Field = "ps_code";
                                     //System.out.println(insertString);
                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
                                     break;
                                     
//                                 case 63:
//                                     insertString = "insert into " + prefixTempDB + "t5013_cristatus (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ")";
//                                     insertingTable = "t5013_cristatus";
//                                     respective_Reg_Field = "crim_srno";
//                                     //System.out.println(insertString);
//                                     pstmtInsert = connectionTarget.prepareStatement(insertString);
//                                     insertCounter = insertCounter + pstmtInsert.executeUpdate();
//                                     break;
                           }

                            insertingTables[jk] = insertingTable
                                    + ":" + recordsFound[jk];//+ ":" + recordsInserted[jk];

                        } catch (Exception ee) {
//                            ee.printStackTrace();
                            logger.log(CctnsLogger.ERROR, ee); System.out.println(ee.toString());
                            //--------------------------------------------------
                            try {
                                String eReason;
//                                if (ee.toString().length() > 200) {
//                                    eReason = ee.toString().substring(0, 199);
//                                } else {
                                eReason = "Error: " + ee.getMessage().replaceAll("'", "");
                                String respective_Reg_Field_Value = getRejectHM.get(respective_Reg_Field).toString();
//                                }
                                insertString = "insert into " + prefixTempDB + "Rejection_Temp (REG_NO, ERROR_CODE, R_TABLE_NAME,batch_cd ) values ('"
                                        + respective_Reg_Field_Value + "','" + eReason + "','" + insertingTable + "','" + batchCode + "')";
//                                System.out.println(insertString);
                                pstmtInsert = connectionTarget.prepareStatement(insertString);
                                pstmtInsert.executeUpdate();
                            } catch (Exception ex) {
//                                ex.printStackTrace();
                                logger.log(CctnsLogger.ERROR, ex);System.out.println(ex.toString());
                            }
                            //break lbl_Next_Statement;
                            //--------------------------------------------------
                        }
                    }
                    recordsInserted[jk] = insertCounter;
                    System.out.println("Processed table "+(jk+1)+" of 63 : "+insertingTables[jk]+": "+recordsInserted[jk]+" records inserted ..");
                    insertingTables[jk] = insertingTables[jk].toString().trim() + ":" + recordsInserted[jk];                    
                }
                else
                {
                    System.out.println("No data for table "+(jk+1)+" of 63");
                }
            } catch (Exception ex) {
                logger.log(CctnsLogger.ERROR, ex);System.out.println(ex.toString());
//                ex.printStackTrace();
            }
        }
        return insertingTables;
    }

    public String chekUpdate(String string) throws UnsupportedEncodingException {
        String sOut = string;
//        System.out.println("calling update...................");
        if (string.contains("½")) {
            sOut = sOut.replaceAll("½", ".5");
        }
        //char chara[] = string.toCharArray();
        StringBuilder sNewVal = new StringBuilder();
        for (int i = 0; i < sOut.length(); ++i) {
            char c = sOut.charAt(i);
            int j = (int) c;
//            System.out.println(c + " : " + j);

            if (j != 160) {
                sNewVal.append(c);
            }
        }


        sOut = sNewVal.toString();
        return sOut.toString();
    }
//==============================================================================

    public boolean checkDuplicateRun() {
        boolean runSkip = true;
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
        String checkString = "select count(*) from " + prefixStagging + "s_batch_info where "
                + "state_cd = '" + keyState + "' and "
                + "district_cd ='" + keyDistrict + "' and "
                + "ps_cd = '" + keyPS + "' and "
                + "batch_status='S'";
        try {
            pstmtCheck = connectionTarget.prepareStatement(checkString);
            ResultSet rsetCheck = pstmtCheck.executeQuery();
            rsetCheck.next();
            int check = rsetCheck.getInt(1);
            if (check > 0) {
                runSkip = true;
            } else {
                runSkip = false;
            }
            rsetCheck.close();
            pstmtCheck.close();
        } catch (SQLException ex) {
            logger.log(CctnsLogger.ERROR, ex);
//            ex.printStackTrace();
        }
        return runSkip;
    }
//==============================================================================

    public String checkStatusR() {
        String RbatchCD = "";
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
        String checkString = "select batch_cd from " + prefixStagging + "s_batch_info where "
                + "state_cd = '" + keyState + "' and "
                + "district_cd ='" + keyDistrict + "' and "
                + "ps_cd = '" + keyPS + "' and "
                + "batch_status='R'";
        try {
            pstmtCheck = connectionTarget.prepareStatement(checkString);
            ResultSet rsetCheck = pstmtCheck.executeQuery();
            if (rsetCheck.next() == true) {
                RbatchCD = rsetCheck.getString(1);
            }
            rsetCheck.close();
            pstmtCheck.close();
        } catch (SQLException ex) {
            logger.log(CctnsLogger.ERROR, ex);
//            ex.printStackTrace();
        }
        return RbatchCD;
    }
//==============================================================================

    public void runForStatusR(String statusR) {
        try {
            sourceORtarget = "target";
            sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();

            if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString())) {//System.out.println("MySql");
                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "Sp_Rollback_Data(?)}");
                cs.setString(1, batchCD);
//            cs.setInt(1, 1);
                cs.execute();
            } else {//System.out.println("SQL Server");
                try {
                    SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "Sp_Rollback_Data(?)}");
                    scs.setString(1, statusR);
                    boolean bl = scs.execute();
//                    System.out.println("Stored Procedure Run : " + bl);
                    scs.close();
                } catch (Exception e) {
                    logger.log(CctnsLogger.ERROR, e);
//                    e.printStackTrace();    //marked
                }
            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }
    }
//==============================================================================

    public String refineData(String string) {
        String sOut1 = string;
        String sOut2 = string;
        String sOut3 = string;
        String sOut4 = string;
        String sOut5 = string;
        String sOut6 = string;
        String sOut7 = string;
        String sOut8 = string;
        String sOut9 = string;
        String sOut10 = string;

        if (string.contains("'")) {
            sOut1 = string.replaceAll("'", "''");
        } else {
            sOut1 = string;
        }

        if (sOut1.contains("½")) {
            sOut2 = sOut1.replaceAll("½", "1/2");
        } else {
            sOut2 = sOut1;
        }

        if (sOut2.contains("“")) {
            sOut3 = sOut2.replaceAll("“", " ");
        } else {
            sOut3 = sOut2;
        }

        if (sOut3.contains("–")) {
            sOut4 = sOut3.replaceAll("–", "-");
        } else {
            sOut4 = sOut3;
        }

        if (sOut4.contains("”")) {
            sOut5 = sOut4.replaceAll("”", " ");
        } else {
            sOut5 = sOut4;
        }

        if (sOut5.contains("®")) {
            sOut6 = sOut5.replaceAll("®", "(R)");
        } else {
            sOut6 = sOut5;
        }

        if (sOut6.contains("©")) {
            sOut7 = sOut6.replaceAll("©", "(C)");
        } else {
            sOut7 = sOut6;
        }

        if (sOut7.contains("– ")) {
            sOut8 = sOut7.replaceAll("– ", "-");
        } else {
            sOut8 = sOut7;
        }

        if (sOut8.contains("—")) {
            sOut9 = sOut8.replaceAll("—", "-");
        } else {
            sOut9 = sOut8;
        }

        if (sOut9.contains("\\")) {
            sOut10 = sOut9.replaceAll("\\\\", "\\\\\\\\");
        } else {
            sOut10 = sOut9;
        }
        
        return sOut10;
    }
}
