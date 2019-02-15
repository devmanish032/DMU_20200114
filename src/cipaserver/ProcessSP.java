/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.mysql.jdbc.CallableStatement;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import logger.CctnsLogger;

/**
 *
 * @author sdanaresh
 */
public class ProcessSP {

    String sourceORtarget;
    public Connection connectionSource, connectionTarget;
    SQLConnection sqlcon;
    String prefixStagging;
    String prefixTempDB;
    String selectedState, selectedDistrict, selectedPS,
            selectedFromMM, selectedFromYYYY, selectedToMM, selectedToYYYY;
    public String[] str = new String[10];
    String keyState, keyDistrict, keyPS;
    ArrayList rangeYYYYMM;
    int supressedRecords[];
    ArrayList sourceTableNameAL;
    Date dtBegTime, dtEndTime;
    String checkString[];
    String cString[];
    String checkSumFIRstring[] = null;
    String checkSumMLCstring[] = null;
    String checkSumNCRstring[] = null;
    String checkSumMPstring[] = null;
    String checkSumDBstring[] = null;
//    String checkSumCPstring[] = null;
    String insertingTablesCount[] = new String[52];
    private static CctnsLogger logger = CctnsLogger.getInstance(DeleteData.class.getName());
    
    private String checkSumEnabled="false";

    public ProcessSP() {
        java.sql.DatabaseMetaData dmd = null;
        try {
            sourceORtarget = "target";
            SQLConnection sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            dmd = connectionTarget.getMetaData();
            if (!(dmd.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
                //For MySql
                prefixStagging = "CIPA_STAGING.";
                prefixTempDB = "CIPATemp_DB.";
            } else {
                //For MSSQL
                prefixStagging = "CIPA_STAGING.dbo.";
                prefixTempDB = "CIPATemp_DB.dbo.";
            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }
    }

    public void setSelections(String[] selectedValues) {
        for (int i = 0; i < selectedValues.length; i++) {
            str[i] = selectedValues[i];
//            System.out.println(str[i]);
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
//==============================================================================

    public void registerTheRunning(String batchCD, ArrayList rangeYYYYMM, String location, String nameLocation[]) throws SQLException {
        PreparedStatement pstmtInsert;
        java.sql.DatabaseMetaData dmdBeginTime = null;
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
        rsetBeginTime.close();
        stmtBeginTime.close();
//.............................................................................. 
        String kState = location.substring(0, 2);
        String kDistrict = location.substring(2, 5);
        String kPS = location.substring(5, 7);

//        String sState = null, sDistrict = null, sPS = null;


//        System.out.println(batchCD);
//        System.out.println(kState + ":" + kDistrict + ":" + kPS);
//        System.out.println(nameLocation[0] + " " + nameLocation[1] + nameLocation[2]);
//        System.out.println(dtBegTime);
//        rangeYYYYMM = rangeOfSelectedYYYYMM();
        for (int i = 0; i < rangeYYYYMM.size(); i++) {
//            batchCD = "" + keyPS.toString().trim() + "_" + selectedFromYYYY + "_" + datestr + "";
            String batchInfo;
            batchInfo = "INSERT INTO " + prefixStagging + "s_batch_info "
                    + "(BATCH_CD, LOCATION, STATE_CD, DISTRICT_CD, PS_CD, YEAR_MON, STATE_NAME, DISTRICT_NAME, PS_NAME, START_TIME, END_TIME, BATCH_STATUS) "
                    + "VALUES ('" + batchCD + "','" + location + "','" + kState + "','" + kDistrict + "','" + kPS + "'," + rangeYYYYMM.get(i).toString() + ",'" + nameLocation[0] + "','" + nameLocation[1] + "','" + nameLocation[2] + "','" + dtBegTime + "',null,'R')";
//            System.out.println("Utility starts and registered...");
            pstmtInsert = connectionTarget.prepareStatement(batchInfo);
            pstmtInsert.executeUpdate();
            pstmtInsert.close();
        }
    }
//==============================================================================

    public boolean checkNoRecord() throws SQLException {
        sourceORtarget = "source";
        SQLConnection sqlcon = new SQLConnection();
        connectionSource = sqlcon.SQLCon(sourceORtarget);

        boolean foundRecord = false;
        String noRecordString = "SELECT count(*)  from t1_registration reg where "
                + "to_char(reg.reg_date, 'YYYYMM') >= '" + selectedFromYYYY + selectedFromMM + "' "
                + "and to_char(reg.reg_date, 'YYYYMM') <= '" + selectedToYYYY + selectedToMM + "' "
                + "and reg.location='" + keyState.trim() + keyDistrict.trim() + keyPS.trim() + "'";

//        System.out.println(noRecordString);

        PreparedStatement pstmtNoRecord = connectionSource.prepareStatement(noRecordString);

        ResultSet rsetNoRecord = pstmtNoRecord.executeQuery();
        rsetNoRecord.next();

        if (rsetNoRecord.getInt(1) > 0) {
            foundRecord = true;
        } else {
            foundRecord = false;
        }
        rsetNoRecord.close();
        pstmtNoRecord.close();
        return foundRecord;
    }

//==============================================================================
    public boolean checkDuplicateRun() {
        boolean runSkip = true;
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
//        System.out.println("reached at duplicate...........");

        try {
            rangeYYYYMM = rangeOfSelectedYYYYMM();
            for (int i = 0; i < rangeYYYYMM.size(); i++) {
//                System.out.println(rangeYYYYMM.get(i));
            }
            checkString = new String[rangeYYYYMM.size()];
            for (int i = 0; i < rangeYYYYMM.size(); i++) {
//                System.out.println("Inside checkDuplicateRun" + rangeYYYYMM.get(i).toString());

                checkString[i] = "select count(*) from " + prefixStagging + "s_batch_info where "
                        + "state_cd = '" + keyState.trim() + "' and "
                        + "district_cd ='" + keyDistrict.trim() + "' and "
                        + "ps_cd = '" + keyPS.trim() + "' and "
                        + "YEAR_MON = '" + rangeYYYYMM.get(i).toString() + "' and "
                        + "batch_status='S'";
//                System.out.println(checkString);
                try {
                    pstmtCheck = connectionTarget.prepareStatement(checkString[i]);
                    ResultSet rsetCheck = pstmtCheck.executeQuery();
                    rsetCheck.next();
                    int check = rsetCheck.getInt(1);
                    if (check > 0) {
                        runSkip = true;
//                        System.out.println("kindly change your selection" + rangeYYYYMM.get(i).toString());
                        break;
                    } else {
                        runSkip = false;
                    }
                    rsetCheck.close();
                    pstmtCheck.close();

                } catch (SQLException ex) {
                    logger.log(CctnsLogger.ERROR, ex);
//                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }

        return runSkip;
    }
//==============================================================================

    public boolean checkDuplicateUpload(ArrayList rangeYYYYMM) {
        boolean runSkip = true;
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
//        System.out.println("reached at duplicate...........");
        try {
            for (int index = 0; index < rangeYYYYMM.size(); index++) {
                String checkString = "select COUNT(*) from "
                        + prefixTempDB + "t011_state s, "
                        + prefixTempDB + "t012_district d, "
                        + prefixTempDB + "t013_policestation ps, "
                        + prefixStagging + "s_batch_info inf, "
                        + "(select distinct location from "
                        + prefixTempDB + "t1_registration) r  "
                        + "where s.state_code = RTRIM(substring(r.location, 1, 2)) "
                        + "and d.district_code = RTRIM(substring(r.location, 3, 3)) "
                        + "and d.state_code = RTRIM(substring(r.location, 1, 2)) "
                        + "and ps.district_code = RTRIM(substring(r.location, 3, 3)) "
                        + "and ps.ps_code = RTRIM(substring(r.location, 6, 2)) "
                        + "and inf.state_cd = s.state_code "
                        + "and inf.district_cd = d.district_code "
                        + "and inf.ps_cd = ps.ps_code "
                        + "and inf.YEAR_MON = '" + rangeYYYYMM.get(index) + "' "
                        + "and inf.batch_status = 'S'";

//                System.out.println(checkString);

                pstmtCheck = connectionTarget.prepareStatement(checkString);
                ResultSet rsetCheck = pstmtCheck.executeQuery();
                rsetCheck.next();
                int check = rsetCheck.getInt(1);
                if (check > 0) {
                    runSkip = true;
                    break;
                } else {
                    runSkip = false;
                }
                rsetCheck.close();
                pstmtCheck.close();
            }


        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }

        return runSkip;
    }
//==============================================================================

    public ArrayList checkStatusR() {
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
//        ArrayList rangeYYYYMM = rangeOfSelectedYYYYMM();
        ArrayList RbatchCD = new ArrayList();

        for (int i = 0; i < rangeYYYYMM.size(); i++) {
//            System.out.println(rangeYYYYMM.get(i).toString());

            String checkString = "select batch_cd from " + prefixStagging + "s_batch_info where "
                    + "state_cd = '" + keyState.trim() + "' and "
                    + "district_cd ='" + keyDistrict.trim() + "' and "
                    + "YEAR_MON = '" + rangeYYYYMM.get(i).toString().trim() + "' and "
                    + "ps_cd = '" + keyPS.trim() + "' and "
                    + "batch_status='R'";
            try {
                pstmtCheck = connectionTarget.prepareStatement(checkString);
                ResultSet rsetCheck = pstmtCheck.executeQuery();
                if (rsetCheck.next() == true) {
//                    if (rsetCheck.getString(1).equals("") || rsetCheck.getString(1).equals(null)) {
//                        RbatchCD.add("");
//                    } else {
                    RbatchCD.add(rsetCheck.getString(1));
//                    }
                }
            } catch (SQLException ex) {
                logger.log(CctnsLogger.ERROR, ex);
//                ex.printStackTrace();
            }
        }
        return RbatchCD;//check for distinct
    }
    //==============================================================================

    public ArrayList checkStatusR_Upload(ArrayList rangeYYMM, String location) {
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
        String kState = location.substring(0, 2);
        String kDistrict = location.substring(2, 5);
        String kPS = location.substring(5, 7);

//        ArrayList rangeYYYYMM = rangeOfSelectedYYYYMM();
        ArrayList RbatchCD = new ArrayList();

        for (int i = 0; i < rangeYYMM.size(); i++) {
//            System.out.println(rangeYYMM.get(i).toString());

            String checkString = "select batch_cd from " + prefixStagging + "s_batch_info where "
                    + "state_cd = '" + kState.trim() + "' and "
                    + "district_cd ='" + kDistrict.trim() + "' and "
                    + "YEAR_MON = '" + rangeYYMM.get(i).toString().trim() + "' and "
                    + "ps_cd = '" + kPS.trim() + "' and "
                    + "batch_status='R'";
            try {
                pstmtCheck = connectionTarget.prepareStatement(checkString);
                ResultSet rsetCheck = pstmtCheck.executeQuery();
                if (rsetCheck.next() == true) {
//                    if (rsetCheck.getString(1).equals("") || rsetCheck.getString(1).equals(null)) {
//                        RbatchCD.add("");
//                        System.out.println("adding blank");
//                    } else {
                    RbatchCD.add(rsetCheck.getString(1));
//                    }
                }
            } catch (SQLException ex) {
                logger.log(CctnsLogger.ERROR, ex);
//                ex.printStackTrace();
            }
        }
        return RbatchCD;//check for distinct
    }
//==============================================================================

    public void runForStatusR(ArrayList statusR) {
        try {
            sourceORtarget = "target";
            sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();

//            rangeYYYYMM = rangeOfSelectedYYYYMM();
//                System.out.println(rangeYYYYMM.get(i));
            for (int i = 0; i < statusR.size(); i++) {

                if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString())) {//System.out.println("MySql");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "Sp_Rollback_Data(?)}");
                    cs.setString(1, statusR.get(i).toString());
//            cs.setInt(1, 1);
                    cs.execute();
                } else {//System.out.println("SQL Server");
                    try {
                        SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "Sp_Rollback_Data(?)}");
                        scs.setString(1, statusR.get(i).toString());
                        boolean bl = scs.execute();
//                    System.out.println("Stored Procedure Run : " + bl);
                        scs.close();
                    } catch (Exception e) {
                        logger.log(CctnsLogger.ERROR, e);
//                        e.printStackTrace();    //marked
                    }
                }
            }
//=================================================================
            PreparedStatement pstmtSP;
            for (int i = 0; i < statusR.size(); i++) {
                String batchUpdate;
                batchUpdate = "update " + prefixStagging + "s_batch_info "
                        + "set batch_status='F' "
                        + "where batch_cd='" + statusR.get(i) + "'";

                pstmtSP = connectionTarget.prepareStatement(batchUpdate);
                pstmtSP.executeUpdate();
            }

        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }

    }

  //==============================================================================
    public void SP_from_CIPAtempDB_TO_Stagging(String batchCD, String insertingTables[]) {
        try {
//            System.out.println("calling SP_from_CIPAtempDB_TO_Stagging");
            sourceORtarget = "target";
            sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();
            if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString()))
           {
                System.out.println("Calling procedure 1/12 : " + prefixStagging + "SP_Load_CIPA_Data('"+batchCD+"')");
//                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_Load_CIPA_Data(?)}");
//                cs.setString(1, batchCD);
//                cs.execute();
           try  {
                    System.out.println("Calling procedure 2/12 : " + prefixStagging + "SP_CIPA_IIF_1('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_1(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_1: "+se.toString());
                }
            
            try { 
                    System.out.println("Calling procedure 3/12 : " + prefixStagging + "SP_CIPA_IIF_2('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_2(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_2: "+se.toString());
                }
   
            try { 
                    System.out.println("Calling procedure 4/12 : " + prefixStagging + "SP_CIPA_IIF_3('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_3(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_3: "+se.toString());
                }
   
            try { 
                    System.out.println("Calling procedure 5/12 : " + prefixStagging + "SP_CIPA_IIF_5('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_5(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_5: "+se.toString());
                }
            //comment by nitin
          try { 
                    System.out.println("Calling procedure 6/12 : " + prefixStagging + "SP_CIPA_IIF_6('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_6(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_6: "+se.toString());
                }
   
           try { 
                    System.out.println("Calling procedure 7/12 : " + prefixStagging + "SP_CIPA_IIF_8('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_8(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_8: "+se.toString());
                }
   
               try { 
                   System.out.println("Calling procedure 8/12 : " + prefixStagging + "SP_CIPA_IIF_9('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_9(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_IIF_9: "+se.toString());
                }
   
                try { 
                    System.out.println("Calling procedure 9/12 : " + prefixStagging + "SP_CIPA_MLC('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_MLC(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_MLC: "+se.toString());
                }
   

   
                try { 
                        System.out.println("Calling procedure 10/12 :" + prefixStagging + "SP_CIPA_NCR('"+batchCD+"')");
                        CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_NCR(?)}");
                        cs.setString(1, batchCD);
                        cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_NCR: "+se.toString());
                }
                
                try { 
                    System.out.println("Calling procedure 11/12 :" + prefixStagging + "SP_CIPA_CRIMINAL_PROFILE('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_CRIMINAL_PROFILE(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
                } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_CRIMINAL_PROFILE: "+se.toString());
                }
               try { 
                    System.out.println("Calling procedure 12/12 :" + prefixStagging + "SP_CIPA_GD_DETAILS('"+batchCD+"')");
                    CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_GD_DETAILS(?)}");
                    cs.setString(1, batchCD);
                    cs.execute();
               } catch (SQLException se) {
                    System.out.println("Error in SP_CIPA_GD_DETAILS: "+se.toString());
                }
            } else {//System.out.println("SQL Server");
                SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SP_Load_CIPA_Data(?)}");
                scs.setString(1, batchCD);
                scs.execute();
                scs.close();
            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e); 
            System.out.println("Error: "+e.toString());//            e.printStackTrace();        //marked
        }
        //=========================================

        //=========================================
        supressedRecords = new int[insertingTables.length];
        for (int i = 0; i < insertingTables.length; i++) {
//            System.out.println("----------->-" + insertingTables[i]);
            if (insertingTables[i] != null) {
                try {
                    String sqlSelect = "select count(*) from " + prefixTempDB + insertingTables[i].substring(0, insertingTables[i].indexOf(":")) + " "
                            + "where UsedByStagging is null";
//                    System.out.println(sqlSelect);
                    PreparedStatement pstmt = connectionTarget.prepareStatement(sqlSelect);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
//                        insertingTables[i] = insertingTables + ":" + rs.getInt(1);
                        supressedRecords[i] = rs.getInt(1);
//                        System.out.println("supressedRecords[i]" + supressedRecords[i]);
                    } else {
                        insertingTables[i] = insertingTables + ":0";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//            logger.log(CctnsLogger.ERROR, e);
                }
            }
        }
        //=========================================

    }

    public void callAllStoredProcedures(String batchCD) {
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtAllStoredProcedures;

        String callAllStoredProcedures = "select load_proc_name, "
                + "stagging_table,transaction_table,rejection_table "
                + "from " + prefixStagging + "proc_table_mapping "
                + "order by FORM_NUMBER, RUN_ORDER";
        try {
            pstmtAllStoredProcedures = connectionTarget.prepareStatement(callAllStoredProcedures);
            ResultSet rcount = pstmtAllStoredProcedures.executeQuery();
            int icount = 0;
            while (rcount.next()) {
                icount++;
            }

            int jcount = 0;
            ResultSet rsetAllStoredProcedures = pstmtAllStoredProcedures.executeQuery();
            String staggingSP[] = new String[icount];
            String staggingStagging[] = new String[icount];
            String staggingTransaction[] = new String[icount];
            String staggingRejection[] = new String[icount];

            while (rsetAllStoredProcedures.next()) {
                staggingSP[jcount] = rsetAllStoredProcedures.getString(1);
                staggingStagging[jcount] = rsetAllStoredProcedures.getString(2);
                staggingTransaction[jcount] = rsetAllStoredProcedures.getString(3);
                staggingRejection[jcount] = rsetAllStoredProcedures.getString(4);
//                System.out.println(staggingSP[jcount]);
                jcount++;
            }
            //------------------------------------------------------------------
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();
            int lenght = 0;
//            System.out.println("Length of staging SP Batch   : " + staggingSP.length);
            for (int exe = 0; exe < staggingSP.length; exe++) {
                if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString())) {//System.out.println("MySql");
                    CallableStatement cs = null;
                    try {
//                        System.out.println("Calling mysql :" + staggingSP[exe]);
                        cs = (CallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "" + staggingSP[exe].toString().toLowerCase() + "(?,?)}");
                        cs.setString(1, batchCD);
                        cs.setInt(2, 1);
                        cs.execute();
                    } catch (Exception e) {
                        logger.log(CctnsLogger.ERROR, "{call " + prefixStagging + "" + staggingSP[exe].toString().toLowerCase() + "("+batchCD+ "  ,1  )}  Error details: " +e);
//                        e.printStackTrace();
                    } finally {
//                        System.out.println(lenght++);
                        cs.clearParameters();
                        cs.close();
                    }
                } else {//System.out.println("SQL Server");
                    try {
//                        System.out.println("calling sql : " + staggingSP[exe]);
                        SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "" + staggingSP[exe].toString().trim() + "(?)}");

                        scs.setString(1, batchCD);
                        scs.execute();
                        scs.close();
                    } catch (Exception e) {
                        logger.log(CctnsLogger.ERROR, "{call " + prefixStagging + "" + staggingSP[exe].toString().trim() + "("+batchCD+ " )}  Error details: "+e);
                        e.printStackTrace();    //marked
                    }
                }
            }
            //------------------------------------------------------------------
        } catch (Exception ex) {
            logger.log(CctnsLogger.ERROR, ex);
//            ex.printStackTrace();
        }
    }

    public ArrayList rangeOfSelectedYYYYMM() {
        //--
        int fromYear = Integer.parseInt(selectedFromYYYY);
        int fromMonth = Integer.parseInt(selectedFromMM);

        int toYear = Integer.parseInt(selectedToYYYY);
        int toMonth = Integer.parseInt(selectedToMM);

        int tempM = fromMonth;
        ArrayList rangeYYYYMM = new ArrayList();

        for (int year = fromYear; year <= toYear; year++) {
            for (int month = tempM; month <= 12; month++) {
                if (month < 10) {
                    rangeYYYYMM.add(year + "0" + month);
                } else {
                    rangeYYYYMM.add(year + "" + month);
                }
                if (month == toMonth && year == toYear) {
                    break;
                }
            }
            tempM = 1;
        }
        return rangeYYYYMM;
    }

    public ArrayList rangeFromToYYYYMM(String from, String to) {
        //--
        int fromYear = Integer.parseInt(from.substring(0, 4));
        int fromMonth = Integer.parseInt(from.substring(4, 6));

        int toYear = Integer.parseInt(to.substring(0, 4));
        int toMonth = Integer.parseInt(to.substring(4, 6));

        int tempM = fromMonth;
        ArrayList rangeYYYYMM = new ArrayList();

        for (int year = fromYear; year <= toYear; year++) {
            for (int month = tempM; month <= 12; month++) {
                if (month < 10) {
                    rangeYYYYMM.add(year + "0" + month);
                } else {
                    rangeYYYYMM.add(year + "" + month);
                }
                if (month == toMonth && year == toYear) {
                    break;
                }
            }
            tempM = 1;
        }
        return rangeYYYYMM;
    }
//==============================================================================

    public void callCheckSum(String batchCD) {
        try {
            sourceORtarget = "target";
            sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();
            if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString())) {//System.out.println("MySql");
                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SP_CIPA_IIF_CheckSum(?)}");
                cs.setString(1, batchCD);
                cs.execute();

////                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_FIR_Landing_to_Target(?,?)}");
//                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_1(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_2(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_3(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_5(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_6(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_8(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_FIR_IIF_9(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_MLC(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();
//
//                cs = (CallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpLoadChecksum_NCR(?,?)}");
//                cs.setInt(1, 1);
//                cs.setString(2, batchCD);
//                cs.execute();

            } else {//System.out.println("SQL Server");

                SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SP_CIPA_IIF_CheckSum(?)}");
                scs.setString(1, batchCD);
                scs.execute();
                scs.close();

////                SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpChecksum_FIR_Landing_to_Target(?)}");
//                SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_1(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
////                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_MP_Landing_to_Target(?)}");
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_2(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
////                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_DB_Landing_to_Target(?)}");
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_3(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
////                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_MLC_Landing_to_Target(?)}");
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_5(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
////                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_NCR_Landing_to_Target(?)}");
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_6(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
////                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{ call " + prefixStagging + "SpChecksum_CP_Landing_to_Target(?)}");
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_8(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_FIR_IIF_9(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_MLC(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();
//
//                scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SpLoadChecksum_NCR(?)}");
//                scs.setString(1, batchCD);
//                scs.execute();
//                scs.close();

            }
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
            e.printStackTrace();        //marked
        }
    }

//==============================================================================
    public String[] Tables_Record_Count() {
        String tableNames[] = new String[53];

        tableNames[0] = "t015_psstaffcurr";
        tableNames[1] = "t014_policestationbeat";
        tableNames[2] = "t1_registration";
        tableNames[3] = "t201_fir";
        tableNames[4] = "t311_transfer";
        tableNames[5] = "t101_actsection";
        tableNames[6] = "t301_crime";
        tableNames[7] = "t301b_crime";
        tableNames[8] = "t102_Person";
        tableNames[9] = "t1021_Personal";
        tableNames[10] = "t2013_victim";
        tableNames[11] = "t305_witness";
        tableNames[12] = "t2011_accused";
        tableNames[13] = "t303_arrest";
        tableNames[14] = "t10221_physical";
        tableNames[15] = "t10222_physical";
        tableNames[16] = "t103_properties";
        tableNames[17] = "t1031_automobile";
        tableNames[18] = "t304a_seizure";
        tableNames[19] = "t1034a_currency";
        tableNames[20] = "t1034b_currency";
        tableNames[21] = "t1032_cultural";
        tableNames[22] = "t1035_narcotics";
        tableNames[23] = "t1033_numbered";
        tableNames[24] = "t312_finalreport";
        tableNames[25] = "t312b_fraccused";
        tableNames[26] = "t312c_fractsec";
        tableNames[27] = "t3034_remandcustody";
        tableNames[28] = "t3033_bail";
        tableNames[29] = "t406a_courtdisposal";
        tableNames[30] = "t406b_courtdisposal";
        tableNames[31] = "t406c_courtdisposal";
        tableNames[32] = "t202_missing";
        tableNames[33] = "t205_unnatural";
        tableNames[34] = "t204_mlc";
        tableNames[35] = "t207_others";
        tableNames[36] = "t501a_criminal";
        tableNames[37] = "t5011_criaddress";
        tableNames[38] = "t5027_bank";
        tableNames[39] = "t5012_criknowns";
        tableNames[40] = "t5023_operationarea";
        tableNames[41] = "t5021_general";
        tableNames[42] = "t5022_affiliation";
        tableNames[43] = "t5026_employment";
        tableNames[44] = "t5025_political";
        tableNames[45] = "t5024_notices";
        tableNames[46] = "t503_gang";
        tableNames[47] = "t5037_frontalorg";
        tableNames[48] = "t5032_support";
        tableNames[49] = "t5034_transport";
        tableNames[50] = "t5033_holdings";
        tableNames[51] = "t5035_training";
        tableNames[52] = "t5036_hideouts";

        String strSelect[] = new String[53];
        strSelect[0] = "select count(*) from " + prefixTempDB + "t015_psstaffcurr".toLowerCase();
        strSelect[1] = "select count(*) from " + prefixTempDB + "t014_policestationbeat".toLowerCase();
        strSelect[2] = "select count(*) from " + prefixTempDB + "t1_registration".toLowerCase();
        strSelect[3] = "select count(*) from " + prefixTempDB + "t201_fir".toLowerCase();
        strSelect[4] = "select count(*) from " + prefixTempDB + "t311_transfer".toLowerCase();
        strSelect[5] = "select count(*) from " + prefixTempDB + "t101_actsection".toLowerCase();
        strSelect[6] = "select count(*) from " + prefixTempDB + "t301_crime".toLowerCase();
        strSelect[7] = "select count(*) from " + prefixTempDB + "t301b_crime".toLowerCase();
        strSelect[8] = "select count(*) from " + prefixTempDB + "t102_Person".toLowerCase();
        strSelect[9] = "select count(*) from " + prefixTempDB + "t1021_Personal".toLowerCase();
        strSelect[10] = "select count(*) from " + prefixTempDB + "t2013_victim".toLowerCase();
        strSelect[11] = "select count(*) from " + prefixTempDB + "t305_witness".toLowerCase();
        strSelect[12] = "select count(*) from " + prefixTempDB + "t2011_accused".toLowerCase();
        strSelect[13] = "select count(*) from " + prefixTempDB + "t303_arrest".toLowerCase();
        strSelect[14] = "select count(*) from " + prefixTempDB + "t10221_physical".toLowerCase();
        strSelect[15] = "select count(*) from " + prefixTempDB + "t10222_physical".toLowerCase();
        strSelect[16] = "select count(*) from " + prefixTempDB + "t103_properties".toLowerCase();
        strSelect[17] = "select count(*) from " + prefixTempDB + "t1031_automobile".toLowerCase();
        strSelect[18] = "select count(*) from " + prefixTempDB + "t304a_seizure".toLowerCase();
        strSelect[19] = "select count(*) from " + prefixTempDB + "t1034a_currency".toLowerCase();
        strSelect[20] = "select count(*) from " + prefixTempDB + "t1034b_currency".toLowerCase();
        strSelect[21] = "select count(*) from " + prefixTempDB + "t1032_cultural".toLowerCase();
        strSelect[22] = "select count(*) from " + prefixTempDB + "t1035_narcotics".toLowerCase();
        strSelect[23] = "select count(*) from " + prefixTempDB + "t1033_numbered".toLowerCase();
        strSelect[24] = "select count(*) from " + prefixTempDB + "t312_finalreport".toLowerCase();
        strSelect[25] = "select count(*) from " + prefixTempDB + "t312b_fraccused".toLowerCase();
        strSelect[26] = "select count(*) from " + prefixTempDB + "t312c_fractsec".toLowerCase();
        strSelect[27] = "select count(*) from " + prefixTempDB + "t3034_remandcustody".toLowerCase();
        strSelect[28] = "select count(*) from " + prefixTempDB + "t3033_bail".toLowerCase();
        strSelect[29] = "select count(*) from " + prefixTempDB + "t406a_courtdisposal".toLowerCase();
        strSelect[30] = "select count(*) from " + prefixTempDB + "t406b_courtdisposal".toLowerCase();
        strSelect[31] = "select count(*) from " + prefixTempDB + "t406c_courtdisposal".toLowerCase();
        strSelect[32] = "select count(*) from " + prefixTempDB + "t202_missing".toLowerCase();
        strSelect[33] = "select count(*) from " + prefixTempDB + "t205_unnatural".toLowerCase();
        strSelect[34] = "select count(*) from " + prefixTempDB + "t204_mlc".toLowerCase();
        strSelect[35] = "select count(*) from " + prefixTempDB + "t207_others".toLowerCase();
        strSelect[36] = "select count(*) from " + prefixTempDB + "t501a_criminal".toLowerCase();
        strSelect[37] = "select count(*) from " + prefixTempDB + "t5011_criaddress".toLowerCase();
        strSelect[38] = "select count(*) from " + prefixTempDB + "t5027_bank".toLowerCase();
        strSelect[39] = "select count(*) from " + prefixTempDB + "t5012_criknowns".toLowerCase();
        strSelect[40] = "select count(*) from " + prefixTempDB + "t5023_operationarea".toLowerCase();
        strSelect[41] = "select count(*) from " + prefixTempDB + "t5021_general".toLowerCase();
        strSelect[42] = "select count(*) from " + prefixTempDB + "t5022_affiliation".toLowerCase();
        strSelect[43] = "select count(*) from " + prefixTempDB + "t5026_employment".toLowerCase();
        strSelect[44] = "select count(*) from " + prefixTempDB + "t5025_political".toLowerCase();
        strSelect[45] = "select count(*) from " + prefixTempDB + "t5024_notices".toLowerCase();
        strSelect[46] = "select count(*) from " + prefixTempDB + "t503_gang".toLowerCase();
        strSelect[47] = "select count(*) from " + prefixTempDB + "t5037_frontalorg".toLowerCase();
        strSelect[48] = "select count(*) from " + prefixTempDB + "t5032_support".toLowerCase();
        strSelect[49] = "select count(*) from " + prefixTempDB + "t5034_transport".toLowerCase();
        strSelect[50] = "select count(*) from " + prefixTempDB + "t5033_holdings".toLowerCase();
        strSelect[51] = "select count(*) from " + prefixTempDB + "t5035_training".toLowerCase();
        strSelect[52] = "select count(*) from " + prefixTempDB + "t5036_hideouts".toLowerCase();


        PreparedStatement pstmtSelect;
//        sourceORtarget = "target";
//        SQLConnection sqlcon = new SQLConnection();
//        connectionTarget = sqlcon.SQLCon(sourceORtarget);

        for (int ij = 0; ij < strSelect.length; ij++) {
            try {
//                System.out.println(strSelect[ij].toString());
                pstmtSelect = connectionTarget.prepareStatement(strSelect[ij].toString());
                ResultSet rset = pstmtSelect.executeQuery();
                rset.next();
                int intCount = rset.getInt(1);
                insertingTablesCount[ij] = tableNames[ij].toString().trim() + ":" + intCount + ":" + intCount;
                rset.close();
                pstmtSelect.close();
            } catch (Exception ex) {
                logger.log(CctnsLogger.ERROR, ex);
//                ex.printStackTrace();
            }

        }
        return insertingTablesCount;
    }
//==============================================================================

    public boolean checkProcessing() {
        boolean runSkip = true;
        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtCheck;
        String checkString = "select count(*) from " + prefixTempDB + "FIR";
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
        } catch (SQLException ex) {
            logger.log(CctnsLogger.ERROR, ex);
//            Logger.getLogger(MigrateData.class.getName()).log(Level.SEVERE, null, ex);
//            ex.printStackTrace();
        }
        return runSkip;
    }
//==============================================================================

    public void CALL_SP_Table_Record_Count(String filename, String batchCD, String location, String datestr, String str[]) {
        try {
//.//--End time--------------------------------------------------------------...        
            java.sql.DatabaseMetaData dmdEndTime = null;
            dmdEndTime = connectionTarget.getMetaData();
            Statement stmtEndTime = connectionTarget.createStatement();
            PreparedStatement pstmtSP;
            ResultSet rsetEndTime;
            if (!(dmdEndTime.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
                rsetEndTime = stmtEndTime.executeQuery("select now()");
                rsetEndTime.next();
                dtEndTime = rsetEndTime.getTimestamp(1);
            } else {
                rsetEndTime = stmtEndTime.executeQuery("select getdate()");
                rsetEndTime.next();
                dtEndTime = rsetEndTime.getTimestamp(1);
            }
            String kState = location.substring(0, 2);
            String kDistrict = location.substring(2, 5);
            String kPS = location.substring(5, 7);

            if (filename != null) {
                String parts[] = filename.split("_");
                for (int i = 0; i < parts.length; i++) {
                    if (i == 1) {
                        selectedFromYYYY = parts[i].substring(0, 4);
                        selectedFromMM = parts[i].substring(4, 6);
                        selectedToYYYY = parts[i].substring(6, 10);
                        selectedToMM = parts[i].substring(10, 12);
                    }
                }
                selectedState = kState;
                selectedDistrict = kDistrict;
                selectedPS = kPS;
            }
//..............................................................................
//            ArrayList rangeYYYYMM = rangeOfSelectedYYYYMM();
            String batchUpdate;
            batchUpdate = "update " + prefixStagging + "s_batch_info "
                    + "set END_TIME='" + dtEndTime + "', batch_status='S' where batch_cd='" + batchCD + "'";

            pstmtSP = connectionTarget.prepareStatement(batchUpdate);
            pstmtSP.executeUpdate();
        } catch (Exception e) {
            logger.log(CctnsLogger.ERROR, e);
//            e.printStackTrace();
        }
//..............................................................................

        String insertingTables[] = new String[str.length];
        int recordsFound[] = new int[str.length];
        int recordsInserted[] = new int[str.length];
        ArrayList<String> mString = new ArrayList<String>();
        ;

        String temp;
        for (int i = 0; i < str.length; i++) {
//            System.out.println("str[i]" + str[i]);
            if (str[i] != null) {
                //System.out.println("str[" + i + "]" + str[i]);
                int colon1 = str[i].trim().indexOf(':');
                int colon2 = str[i].trim().indexOf(':', colon1 + 1);
//                System.out.println("colon1--"+colon1);
//                System.out.println("colon2--"+colon2);
                insertingTables[i] = str[i].substring(0, str[i].indexOf(":")).toString().toLowerCase().trim();

                temp = str[i].substring(colon1 + 1, colon2);
                recordsFound[i] = Integer.parseInt(temp);
                temp = str[i].substring(colon2 + 1, str[i].length());
                recordsInserted[i] = Integer.parseInt(temp);
                //System.out.println(insertingTables[i] + "=" + recordsFound[i] + "=" + recordsInserted[i]);
            }
        }

        sourceORtarget = "target";
        sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);
        PreparedStatement pstmtSP_Table_Record_Count;
        //------------------------------------------------------------------
        try {
            java.sql.DatabaseMetaData dm = null;
            dm = connectionTarget.getMetaData();
//            ArrayList rangeYYYYMM = rangeOfSelectedYYYYMM();
            if (!(dm.getDatabaseProductName().toString()).contains("Microsoft".toString())) {//System.out.println("MySql");
                CallableStatement cs = (CallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "sp_table_record_count" + "(?)}");
                cs.setString(1, batchCD);
//                cs.setInt(2, 1);
                cs.execute();
                cs.close();
            } else {//System.out.println("SQL Server");
                try {
                    SQLServerCallableStatement scs = (SQLServerCallableStatement) connectionTarget.prepareCall("{call " + prefixStagging + "SP_Table_Record_Count" + "(?)}");
                    scs.setString(1, batchCD);
                    scs.execute();
                    scs.close();
                } catch (Exception e) {
                    logger.log(CctnsLogger.ERROR, e);
//                    e.printStackTrace();    //marked
                }
            }
        } catch (Exception ee) {
            logger.log(CctnsLogger.ERROR, ee);
//            ee.printStackTrace();    //marked                        
        }

        try {
            //count records from table src_target_record_count
            String sqlCountString = "select count(*)  from " + prefixStagging + "src_target_record_count where source_record_count != 0 and batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount = connectionTarget.prepareStatement(sqlCountString);
            ResultSet rsetCount = pstmtCount.executeQuery();
            rsetCount.next();
            int recCount = rsetCount.getInt(1);
            sourceTableNameAL = new ArrayList();
            if (recCount > 0) {

                String sqlString = "select SOURCE_TABLE_NAME, SOURCE_RECORD_COUNT, TARGET_TABLE_NAME, TARGET_RECORD_COUNT  from " + prefixStagging + "src_target_record_count where source_record_count != 0 and batch_cd='" + batchCD + "' order by FORM_NUMBER , SEQUENCE_NUMBER ";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset = pstmt.executeQuery();
                cString = new String[recCount];
                int next = 0;
                while (rset.next()) {
                    cString[next] = ""
                            + rset.getString(1).trim() + ":"
                            + rset.getString(2).trim() + ":"
                            + rset.getString(3).trim() + ":"
                            + rset.getString(4).trim();
//                    System.out.println(cString[next]);
                    sourceTableNameAL.add(rset.getString(1).toUpperCase().trim());
                    next++;
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

//=========//checksum details======================================================================================================================================================================================
        //fir-------------------------------------------------------------------
        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'FIR' and batch_cd='" + batchCD + "'";
            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_IIF_Checksum where flag LIKE '%FIR%' and batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
            ResultSet rsetCount1 = pstmtCount1.executeQuery();
            rsetCount1.next();
            int recCount1 = rsetCount1.getInt(1);
            rsetCount1.close();
            pstmtCount1.close();
            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'FIR' and  batch_cd='" + batchCD + "'";
                String sqlString = "select FIRNo,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_IIF_Checksum where flag LIKE '%FIR%' and  batch_cd='" + batchCD + "'";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset1 = pstmt.executeQuery();
                checkSumFIRstring = new String[recCount1];
                int next = 0;
                while (rset1.next()) {
                    checkSumFIRstring[next] = ""
                            + rset1.getString(1).trim() + ":"
                            + rset1.getString(2).trim() + ":"
                            + rset1.getString(3).trim() + ":"
                            + rset1.getString(4).trim();
                    next++;
                }
                rset1.close();
                pstmt.close();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

        //MP----------------------------------------------------------------------
        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'MP' and  batch_cd='" + batchCD + "'";
            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_MP_IIF_Checksum where flag LIKE '%MP%' and  batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
            ResultSet rsetCount1 = pstmtCount1.executeQuery();
            rsetCount1.next();
            int recCount1 = rsetCount1.getInt(1);
            rsetCount1.close();
            pstmtCount1.close();
            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'MP' and batch_cd='" + batchCD + "'";
                String sqlString = "select MPNo,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_MP_IIF_Checksum where flag LIKE '%MP%' and batch_cd='" + batchCD + "'";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset1 = pstmt.executeQuery();
                checkSumMPstring = new String[recCount1];
                int next = 0;
                while (rset1.next()) {
                    checkSumMPstring[next] = ""
                            + rset1.getString(1).trim() + ":"
                            + rset1.getString(2).trim() + ":"
                            + rset1.getString(3).trim() + ":"
                            + rset1.getString(4).trim();
                    next++;
                }
                rset1.close();
                pstmt.close();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

        //DB----------------------------------------------------------------------
        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'DB' and batch_cd='" + batchCD + "'";
            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_DB_IIF_Checksum where flag LIKE '%DBR%' and batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
            ResultSet rsetCount1 = pstmtCount1.executeQuery();
            rsetCount1.next();
            int recCount1 = rsetCount1.getInt(1);
            rsetCount1.close();
            pstmtCount1.close();
            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'DB' and batch_cd='" + batchCD + "'";
                String sqlString = "select DB_INQUEST_NUM,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_DB_IIF_Checksum where flag LIKE '%DBR%' and batch_cd='" + batchCD + "'";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset1 = pstmt.executeQuery();
                checkSumDBstring = new String[recCount1];
                int next = 0;
                while (rset1.next()) {
                    checkSumDBstring[next] = ""
                            + rset1.getString(1).trim() + ":"
                            + rset1.getString(2).trim() + ":"
                            + rset1.getString(3).trim() + ":"
                            + rset1.getString(4).trim();
                    next++;
                }
                rset1.close();
                pstmt.close();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

        //----------------------------------------------------------------------
        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'MLC' and batch_cd='" + batchCD + "'";
            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_MLC_IIF_Checksum where flag LIKE '%MLC%' and batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
            ResultSet rsetCount1 = pstmtCount1.executeQuery();
            rsetCount1.next();
            int recCount1 = rsetCount1.getInt(1);
            rsetCount1.close();
            pstmtCount1.close();
            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'MLC' and batch_cd='" + batchCD + "'";
                String sqlString = "select MLCNo,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_MLC_IIF_Checksum where flag LIKE '%MLC%' and batch_cd='" + batchCD + "'";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset1 = pstmt.executeQuery();
                checkSumMLCstring = new String[recCount1];
                int next = 0;
                while (rset1.next()) {
                    checkSumMLCstring[next] = ""
                            + rset1.getString(1).trim() + ":"
                            + rset1.getString(2).trim() + ":"
                            + rset1.getString(3).trim() + ":"
                            + rset1.getString(4).trim();
                    next++;
                }
                rset1.close();
                pstmt.close();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

        //NCR----------------------------------------------------------------------       
        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'NCR' and batch_cd='" + batchCD + "'";
            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_NCR_IIF_Checksum where flag LIKE '%NCR%' and batch_cd='" + batchCD + "'";
            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
            ResultSet rsetCount1 = pstmtCount1.executeQuery();
            rsetCount1.next();
            int recCount1 = rsetCount1.getInt(1);
            rsetCount1.close();
            pstmtCount1.close();
            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'NCR' and batch_cd='" + batchCD + "'";
                String sqlString = "select NCRNo,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_NCR_IIF_Checksum where flag LIKE '%NCR%' and batch_cd='" + batchCD + "'";
                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
                ResultSet rset1 = pstmt.executeQuery();
                checkSumNCRstring = new String[recCount1];
                int next = 0;
                while (rset1.next()) {
                    checkSumNCRstring[next] = ""
                            + rset1.getString(1).trim() + ":"
                            + rset1.getString(2).trim() + ":"
                            + rset1.getString(3).trim() + ":"
                            + rset1.getString(4).trim();
                    next++;
                }
                rset1.close();
                pstmt.close();
            }
        } catch (Exception e) {
//            e.printStackTrace();
            logger.log(CctnsLogger.ERROR, e);
        }

//        //----------------------------------------------------------------------        
//        try {
//            String sqlCountString1 = "select count(*)  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'CP' and  batch_cd='" + batchCD + "'";
//            PreparedStatement pstmtCount1 = connectionTarget.prepareStatement(sqlCountString1);
//            ResultSet rsetCount1 = pstmtCount1.executeQuery();
//            rsetCount1.next();
//            int recCount1 = rsetCount1.getInt(1);
//            rsetCount1.close();
//            pstmtCount1.close();
//            if (recCount1 > 0) {
//                String sqlString = "select FIRNo_Src,Landing_Checksum,Staging_Checksum,Target_Checksum  from " + prefixStagging + "S_FIR_Checksum where flag LIKE 'CP' and  batch_cd='" + batchCD + "'";
//                PreparedStatement pstmt = connectionTarget.prepareStatement(sqlString);
//                ResultSet rset1 = pstmt.executeQuery();
//                checkSumCPstring = new String[recCount1];
//                int next = 0;
//                while (rset1.next()) {
//                    checkSumCPstring[next] = ""
//                            + rset1.getString(1).trim() + ":"
//                            + rset1.getString(2).trim() + ":"
//                            + rset1.getString(3).trim() + ":"
//                            + rset1.getString(4).trim();
//                    next++;
//                }
//                rset1.close();
//                pstmt.close();
//            }
//        } catch (Exception e) {
////            e.printStackTrace();
//            logger.log(CctnsLogger.ERROR, e);
//        }
//================================================================================================================================================================================================

        try {
            String FinalUpdateString1 =
                    "select a.TABLE_NAME,a.RECORD_COUNT,a.TABLE_FLAG "
                    + "from " + prefixStagging + "s_batch_control a, "
                    + "" + prefixStagging + "proc_table_mapping b "
                    + "where a.TABLE_NAME = b.STAGGING_TABLE "
                    + "and a.BATCH_CD = '" + batchCD + "' "
                    //                    + "and TRANSACTION_TABLE not like 't_person%'"
                    + "and a.BATCH_STATUS = 'S' and a.TABLE_FLAG='S'";

            String FinalUpdateString2 =
                    "select a.TABLE_NAME,a.RECORD_COUNT,a.TABLE_FLAG "
                    + "from " + prefixStagging + "s_batch_control a, "
                    + "" + prefixStagging + "proc_table_mapping b "
                    + "where a.TABLE_NAME = b.TRANSACTION_TABLE "
                    //                    + "and TRANSACTION_TABLE not like 't_person%' "
                    + "and a.BATCH_CD = '" + batchCD + "' and "
                    + "a.BATCH_STATUS = 'S' and a.TABLE_FLAG='T' ";

            String FinalUpdateString3 =
                    "select a.TABLE_NAME,a.RECORD_COUNT,a.TABLE_FLAG "
                    + "from " + prefixStagging + "s_batch_control a, "
                    + "" + prefixStagging + "proc_table_mapping b "
                    + "where a.TABLE_NAME = b.REJECTION_TABLE "
                    //                    + "and  TRANSACTION_TABLE not like 't_person%' "
                    + "and a.BATCH_CD = '" + batchCD + "' "
                    + "and a.BATCH_STATUS = 'S' and a.TABLE_FLAG='R' ";

            String FinalUpdateString4 = "";

            String countFinalUpdateString4 =
                    "select count(a.TABLE_NAME) "
                    + "from " + prefixStagging + "s_batch_control a, "
                    + "" + prefixStagging + "proc_table_mapping b "
                    + "where a.TABLE_NAME=b.TRANSACTION_TABLE "
                    //                    + "and TRANSACTION_TABLE not like 't_person%' "
                    + "and a.BATCH_CD = '" + batchCD + "' "
                    + "and a.BATCH_STATUS = 'S' and a.TABLE_FLAG='E'";

            FinalUpdateString4 =
                    "select a.TABLE_NAME,a.RECORD_COUNT,a.TABLE_FLAG "
                    + "from " + prefixStagging + "s_batch_control a, "
                    + "" + prefixStagging + "proc_table_mapping b "
                    + "where a.TABLE_NAME=b.TRANSACTION_TABLE "
                    //                    + "and TRANSACTION_TABLE not like 't_person%' "
                    + "and a.BATCH_CD = '" + batchCD + "' "
                    + "and a.BATCH_STATUS = 'S' and a.TABLE_FLAG='E'";

            HashMap hashmapDimentions3 = new HashMap();
            PreparedStatement pstmtFinalUpdate;
            pstmtFinalUpdate = connectionTarget.prepareStatement(FinalUpdateString1);
            ResultSet rsetFinalUpdate = pstmtFinalUpdate.executeQuery();
            int count4 = 0;
            HashMap hashmapError = null;
            try {
                while (rsetFinalUpdate.next()) {

                    if (rsetFinalUpdate.getString(2) == null || rsetFinalUpdate.getString(2).equals("")) {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), "0");
                    } else {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), rsetFinalUpdate.getString(2));
                    }
                }

                pstmtFinalUpdate = connectionTarget.prepareStatement(FinalUpdateString2);
                rsetFinalUpdate = pstmtFinalUpdate.executeQuery();
                while (rsetFinalUpdate.next()) {
                    if (rsetFinalUpdate.getString(2) == null || rsetFinalUpdate.getString(2).equals("")) {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), "0");
//                    System.out.println("0 found.................");;
                    } else {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), rsetFinalUpdate.getString(2));
                    }
                }

                pstmtFinalUpdate = connectionTarget.prepareStatement(FinalUpdateString3);
                rsetFinalUpdate = pstmtFinalUpdate.executeQuery();
                while (rsetFinalUpdate.next()) {
                    if (rsetFinalUpdate.getString(2) == null || rsetFinalUpdate.getString(2).equals("")) {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), "0");
                    } else {
                        hashmapDimentions3.put(rsetFinalUpdate.getString(1).toUpperCase(), rsetFinalUpdate.getString(2));
                    }
                }

//-count for records exits or not in error--------------------------------------

                pstmtFinalUpdate = connectionTarget.prepareStatement(countFinalUpdateString4);
                ResultSet countrsetFinalUpdate = pstmtFinalUpdate.executeQuery();

                countrsetFinalUpdate.next();
                count4 = countrsetFinalUpdate.getInt(1);

                if (count4 > 0) {
                    pstmtFinalUpdate = connectionTarget.prepareStatement(FinalUpdateString4);

                    hashmapError = new HashMap();
                    rsetFinalUpdate = pstmtFinalUpdate.executeQuery();
                    while (rsetFinalUpdate.next()) {
                        hashmapError.put(rsetFinalUpdate.getString(1).toUpperCase(), rsetFinalUpdate.getString(2));
                    }
                    int hj = hashmapError.size();
                }
                //========================================
                String mappingString = "select stagging_table,transaction_table,rejection_table "
                        + "from " + prefixStagging + "proc_table_mapping "
                        + "order by FORM_NUMBER, RUN_ORDER";
                PreparedStatement pstmtMapping;
                pstmtMapping = connectionTarget.prepareStatement(mappingString);
                ResultSet rsetMapping = pstmtMapping.executeQuery();
                int k = 0;



                while (rsetMapping.next()) {
//                    if (!rsetMapping.getString(2).contains("t_person")) {
                   
                    mString.add(k ,rsetMapping.getString(1) + ":" + rsetMapping.getString(2) + ":" + rsetMapping.getString(3));
                    
                    k++;
//                    }
//                    System.out.println(mString[k] + ":" + k);
                }
            } catch (Exception e) {
                logger.log(CctnsLogger.ERROR, e);
                e.printStackTrace();
            }

            //..............................................................................selectedState, selectedDistrict, selectedPS
            PreparedStatement pstmt1;

            try {
                File tofolder = new File(System.getProperty("user.dir"));
//                System.out.println(tofolder);System.getProperty("user.dir")
                File logFolder = new File(tofolder + "/Logs");

                if (!logFolder.exists()) {
                    logFolder.mkdir();
                }

                File logFile = new File(tofolder + "/Logs/" + datestr + "LOG.rtf");
//                System.out.println(logFile);
                PrintWriter out = new PrintWriter(logFile);
                logFile.createNewFile();

                out.println("DATA MIGRATION UTILITY FOR CIPA \nRUN DATE " + dtEndTime + "");
                out.println("For the Duration : " + selectedFromMM + "/" + selectedFromYYYY
                        + " to " + selectedToMM + "/" + selectedToYYYY + "");
                out.println("State            : " + selectedState);
                out.println("District         : " + selectedDistrict);
                out.println("Police Station   : " + selectedPS);
                out.println("Batch Code       : " + batchCD);
                out.println();
                out.println("Records transfer details are follows:");
                out.println("=============================================================================================");
                out.println("Table Name             Records in Source            Records in Landing         Unused Records");
                out.println("---------------------------------------------------------------------------------------------");
                ArrayList cStringAL = new ArrayList();
                if(cString!=null){
                    
                
                for (int jj = 0; jj < cString.length; jj++) {
                    cStringAL.add(jj, cString[jj].substring(0, cString[jj].indexOf(":")).toString().toLowerCase().trim());
                    //System.out.println("cStringAL[jj]" + cStringAL.get(jj) + ":");
                }}
                if(insertingTables!=null)
                {
                    
                                for (int i = 1; i < insertingTables.length; i++) {
//                    System.out.println(insertingTables[i] + "------------------>>>>>>>");
//                    String temp1 = insertingTables[i].substring(0, insertingTables[i].indexOf(":"));
                    //System.out.println(insertingTables[i] + ":" + cStringAL.contains(insertingTables[i]));
                    if (insertingTables[i] != null && cStringAL.contains(insertingTables[i])) {
                        if (insertingTables[i].equalsIgnoreCase("t015_psstaffcurr") || insertingTables[i].equalsIgnoreCase("t014_policestationbeat")
                                || insertingTables[i].equalsIgnoreCase("t1_registration") || insertingTables[i].equalsIgnoreCase("t1021_Personal")) {
                            continue;
                        } else {
                            out.print(insertingTables[i]);
                            for (int j = 0; j < 40 - (insertingTables[i].toString().length()) - (String.valueOf(recordsFound[i]).trim().length()); j++) {
                                out.print(" ");
                            }
                            out.print(recordsFound[i]);
                            for (int j = 0; j < 30 - (String.valueOf(recordsInserted[i]).trim().length()); j++) {
                                out.print(" ");
                            }
                            out.print(recordsInserted[i]);
                            for (int j = 0; j < 23 - (String.valueOf(supressedRecords[i]).trim().length()); j++) {
                                out.print(" ");
                            }
                            out.println(supressedRecords[i]);
                        }
                    }
                }
            }
                out.println("=============================================================================================");
                out.println();
                out.println();
                //==============================================================
                out.println("Source - Landing Reject Record Count");
                out.println("=============================================================================================================");
                out.println("Sr#         Reg_No         Table Name               Error Details ");
                out.println("-------------------------------------------------------------------------------------------------------------");

                String reject_temp_Query = "select SR_NO, REG_NO, R_TABLE_NAME, ERROR_CODE from " + prefixTempDB + "Rejection_Temp where batch_cd='" + batchCD + "' order by SR_NO";
//                System.out.println(reject_temp_Query);

                PreparedStatement reject_temp_pstmt = connectionTarget.prepareStatement(reject_temp_Query);
                ResultSet reject_temp_rset = reject_temp_pstmt.executeQuery();

                int serialN = 1;
                while (reject_temp_rset.next()) {
                    out.print(serialN);
                    for (int k = 0; k < 12 - (String.valueOf(reject_temp_rset.getString(1)).trim().length()); k++) {
                        out.print(" ");
                    }
                    out.print(reject_temp_rset.getString(2));
                    for (int k = 0; k < 15 - (reject_temp_rset.getString(2).trim().length()); k++) {
                        out.print(" ");
                    }
                    out.print(reject_temp_rset.getString(3));
                    for (int k = 0; k < 25 - (reject_temp_rset.getString(3).trim().length()); k++) {
                        out.print(" ");
                    }
                    out.println(reject_temp_rset.getString(4));
                    serialN++;
                }
                reject_temp_pstmt.close();
                reject_temp_pstmt.close();

                reject_temp_Query = "select count(*) from " + prefixTempDB + "Rejection_Temp where batch_cd='" + batchCD + "'";
                reject_temp_pstmt = connectionTarget.prepareStatement(reject_temp_Query);
                reject_temp_rset = reject_temp_pstmt.executeQuery();

                out.println("-------------------------------------------------------------------------------------------------------------");
                if (reject_temp_rset.next()) {
                    out.println("Total count : " + reject_temp_rset.getInt(1));
                } else {
                    out.println("Total count : 0");
                }

                out.println("=============================================================================================================");
                out.println();
                out.println();

                //NEW START=====================================================
                out.println("Landing - Stagging Record Count");
                out.println("=============================================================================================================");
                out.println("Source Table Name         Source Record Count     Target Table Name                    Target Record Count");
                out.println("-------------------------------------------------------------------------------------------------------------");
                if(cString!=null){
                    
                
                for (int i = 0; i < cString.length; i++) {
//                    System.out.println(cString[i]);
                    String tempo[] = new String[4];
                    tempo = cString[i].split(":");
                    out.print(tempo[0].trim());
                    for (int k = 0; k < 43 - (String.valueOf(tempo[0]).trim().length()) - (String.valueOf(tempo[1]).trim().length()); k++) {
                        out.print(" ");
                    }
                    out.print(tempo[1].trim());
                    for (int k = 0; k < 7; k++) {
                        out.print(" ");
                    }
                    out.print(tempo[2].trim());
                    for (int k = 0; k < 55 - (String.valueOf(tempo[2]).trim().length()) - (String.valueOf(tempo[3]).trim().length()); k++) {
                        out.print(" ");
                    }
                    out.println(tempo[3].trim());
                }
            }
                out.println("=============================================================================================================");
                //NEW END=======================================================
                int j;
                out.println("\n\n");
                for (int i = 0; i < mString.size(); i++) {
                    String[] sTableName = mString.get(i).split(":");
                    out.println("FOR STAGGING TABLE - " + sTableName[0].toUpperCase());
                    out.println("===========================================================================================================");
                    String sErrorcount = "1000";
                    String eTable = "";
                    //------------------aaa
                    for (j = 0; j < sTableName.length; j++) {

                        out.print(sTableName[j].toUpperCase() + "\t");

                        int iTableName = sTableName[j].toUpperCase().toString().trim().length();
                        int iVal;


                        if (hashmapDimentions3.containsKey(sTableName[j].toUpperCase())) {
                            iVal = (Integer.parseInt(hashmapDimentions3.get(sTableName[j].toUpperCase()).toString()));

                            int val = sTableName[j].toString().trim().length();
                            for (int p = 0; p < 60 - iTableName - val; p++) {
                                out.print(" ");
                            }
                            out.println(hashmapDimentions3.get(sTableName[j].toUpperCase()));
                            if (j == 2) {
//                                System.out.println(sTableName[j].toString());

                                if (iVal > 0) {
                                    //------------------------------
                                    String rString = "select error_code from " + prefixStagging + "" + sTableName[j].toString().toLowerCase() + " where batch_cd='" + batchCD + "'";
//                                    System.out.println(rString);
                                    pstmt1 = connectionTarget.prepareStatement(rString);
                                    ResultSet rset1 = pstmt1.executeQuery();
//                                    System.out.println(rString);
                                    while (rset1.next()) {
                                        out.println(rset1.getString(1));
                                    }
                                    //------------------------------
                                }
                            }
                        } else {
                            out.println("0");
                        }

                        if (count4 > 0) {
                            if (hashmapError.containsKey(sTableName[j].toUpperCase())) {
                                sErrorcount = (String) hashmapError.get(sTableName[j].toUpperCase());
                                eTable = sTableName[j].toUpperCase();
                                out.print("SQL_ERROR_COUNT  ");
                                out.println("\t" + sErrorcount);

                                if (Integer.parseInt(sErrorcount) > 0) {
                                    //------------------------------
                                    String eString = "select sp_error_message from " + prefixStagging + "usp_error_log where err_load_tbl='" + eTable + "' "
                                            + " and batch_cd='" + batchCD + "'";
                                    pstmt1 = connectionTarget.prepareStatement(eString);
                                    ResultSet rset1 = pstmt1.executeQuery();
                                    while (rset1.next()) {
                                        out.println(rset1.getString(1));
//                                        System.out.println(rset1.getString(1));
                                    }
                                    //------------------------------                       
                                }
                            }
                        }
                    }

                    out.println("-----------------------------------------------------------------------------------------------------------");
                    out.println();
                }
                 CheckSumEnabled();
                 System.out.println("checkSumEnabled...>"+checkSumEnabled);
                 if(checkSumEnabled.equalsIgnoreCase("true"))
                 {
                //--checksum fir-------------------------------------------------
                if (checkSumFIRstring != null) {
                    out.println("FIR Checksum Details");
                    out.println("========================================================================================================================");
                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
                    out.println("------------------------------------------------------------------------------------------------------------------------");
                    for (int i = 0; i < checkSumFIRstring.length; i++) {
//                        System.out.println(checkSumFIRstring[i]);
                        String tempo[];
                        tempo = checkSumFIRstring[i].split(":");
                        out.print(tempo[0].trim());
                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[1].trim());
                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[2].trim());
                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.println(tempo[3].trim());
                    }
                    out.println("========================================================================================================================");
                }
                //--------------------------------------------------------------
                //--checksum fir-------------------------------------------------
                if (checkSumMPstring != null) {
                    out.println();
                    out.println();
                    out.println("Missing Person Checksum Details");
                    out.println("========================================================================================================================");
                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
                    out.println("------------------------------------------------------------------------------------------------------------------------");
                    for (int i = 0; i < checkSumMPstring.length; i++) {
//                        System.out.println(checkSumMPstring[i]);
                        String tempo[];
                        tempo = checkSumMPstring[i].split(":");
                        out.print(tempo[0].trim());
                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[1].trim());
                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[2].trim());
                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.println(tempo[3].trim());
                    }
                    out.println("========================================================================================================================");
                }
                //--------------------------------------------------------------
                //--checksum fir-------------------------------------------------
                if (checkSumDBstring != null) {
                    out.println();
                    out.println();
                    out.println("Dead Body Checksum Details");
                    out.println("========================================================================================================================");
                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
                    out.println("------------------------------------------------------------------------------------------------------------------------");
                    for (int i = 0; i < checkSumDBstring.length; i++) {
//                        System.out.println(checkSumDBstring[i]);
                        String tempo[];
                        tempo = checkSumDBstring[i].split(":");
                        out.print(tempo[0].trim());
                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[1].trim());
                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[2].trim());
                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.println(tempo[3].trim());
                    }
                    out.println("========================================================================================================================");
                }
                //--------------------------------------------------------------
                //--checksum fir-------------------------------------------------
                if (checkSumMLCstring != null) {
                    out.println();
                    out.println();
                    out.println("MLC Checksum Details");
                    out.println("========================================================================================================================");
                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
                    out.println("------------------------------------------------------------------------------------------------------------------------");
                    for (int i = 0; i < checkSumMLCstring.length; i++) {
//                        System.out.println(checkSumMLCstring[i]);
                        String tempo[];
                        tempo = checkSumMLCstring[i].split(":");
                        out.print(tempo[0].trim());
                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[1].trim());
                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[2].trim());
                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.println(tempo[3].trim());
                    }
                    out.println("========================================================================================================================");
                }
                //--------------------------------------------------------------
                //--checksum fir-------------------------------------------------
                if (checkSumNCRstring != null) {
                    out.println();
                    out.println();
                    out.println("NCR Checksum Details");
                    out.println("========================================================================================================================");
                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
                    out.println("------------------------------------------------------------------------------------------------------------------------");
                    for (int i = 0; i < checkSumNCRstring.length; i++) {
//                        System.out.println(checkSumNCRstring[i]);
                        String tempo[];
                        tempo = checkSumNCRstring[i].split(":");
                        out.print(tempo[0].trim());
                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[1].trim());
                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.print(tempo[2].trim());
                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
                            out.print(" ");
                        }
                        out.println(tempo[3].trim());
                    }
                    out.println("========================================================================================================================");
                }
                 }
                //--------------------------------------------------------------
                //--checksum fir-------------------------------------------------
//                if (checkSumCPstring != null) {
//                    out.println();
//                    out.println();
//                    out.println("Criminal Profiling Checksum Details");
//                    out.println("========================================================================================================================");
//                    out.println("Reg_No                          Landing_Checksum                 Staging_Checksum                        Target_Checksum");
//                    out.println("------------------------------------------------------------------------------------------------------------------------");
//                    for (int i = 0; i < checkSumCPstring.length; i++) {
////                        System.out.println(checkSumCPstring[i]);
//                        String tempo[];
//                        tempo = checkSumCPstring[i].split(":");
//                        out.print(tempo[0].trim());
//                        for (int k = 0; k < 40 - (String.valueOf(tempo[1]).trim().length()); k++) {
//                            out.print(" ");
//                        }
//                        out.print(tempo[1].trim());
//                        for (int k = 0; k < 33 - (String.valueOf(tempo[2]).trim().length()); k++) {
//                            out.print(" ");
//                        }
//                        out.print(tempo[2].trim());
//                        for (int k = 0; k < 39 - (String.valueOf(tempo[3]).trim().length()); k++) {
//                            out.print(" ");
//                        }
//                        out.println(tempo[3].trim());
//                    }
//                    out.println("========================================================================================================================");
//                }

                out.close();
            } catch (Exception ee) {
                logger.log(CctnsLogger.ERROR, ee);
                ee.printStackTrace();
            }
            //------------------------------------------------------------------------    
        } catch (Exception ee) {
            logger.log(CctnsLogger.ERROR, ee);
//            ee.printStackTrace();
        }
    }
    
    private void CheckSumEnabled()
    {
             Properties props = new Properties();
             File tofolder = new File(System.getProperty("user.dir"));
              System.out.println("Folder .func..>"+tofolder);
             File propFile = new File(tofolder + "/config.properties");
              FileInputStream fileIn;
              try {
                 fileIn = new FileInputStream(propFile);
                  props.load(fileIn);
                  checkSumEnabled= props.getProperty("isCheckSumEnable");
                  System.out.println("checkSumEnabled.func..>"+checkSumEnabled);
            }   catch (IOException ex) {
               logger.log(CctnsLogger.ERROR, ex);
            }          
    } 
    
}
