/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
import javax.swing.JComponent;
import logger.CctnsLogger;

/**
 *
 * @author Administrator
 */
public class DownloadData {

    String sourceORtarget;
    public Connection connectionSource;
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
    PrintWriter out;
    private static CctnsLogger logger = CctnsLogger.getInstance(DownloadData.class.getName());

    public DownloadData() {
            prefixTempDB = "CIPATemp_DB.";
            try{
            java.sql.DatabaseMetaData dmdEndTime = null;
            dmdEndTime = connectionSource.getMetaData();
            Statement stmtEndTime = connectionSource.createStatement();
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
            }catch(Exception e){
//            e.printStackTrace();
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
    public String[] executeDownloadBlock(String str, String selectStringSql[]) throws SQLException, FileNotFoundException, IOException {
        try {
            File fileSequel = new File(str);
            out = new PrintWriter(fileSequel, "UTF-8");
            fileSequel.createNewFile();
            out.println("-- DATA MIGRATION UTILITY FOR CIPA \n-- RUN DATE " + dtEndTime + "");
            out.println("-- For the Duration : " + selectedFromMM + "/" + selectedFromYYYY
                    + " -- to " + selectedToMM + "/" + selectedToYYYY + "");
            out.println("-- State          : " + selectedState);
            out.println("-- District       : " + selectedDistrict);
            out.println("-- Police Station : " + selectedPS);
            out.println("-- Records download are follows:");
            out.println("-- ");
            out.println("-- ");
        } catch (Exception ee) {
            logger.log(CctnsLogger.ERROR, ee);
//            ee.printStackTrace();
        }


        PreparedStatement pstmtMetaSelect, pstmtSelect;
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


//..............................................................................        
        //-number of records iterated by jk variable--------------------------------
        for (int jk = 0; jk < selectStringSql.length; jk++) {

//            System.out.println("--  (" + (jk + 1) + ")");
            StringBuffer buffterMeta = new StringBuffer();
            StringBuffer buffterValue = null;
            HashMap hm = new HashMap();

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
                    while (rsetSelect.next()) {
                        buffterValue = new StringBuffer();
                        for (int ij = 1; ij <= rsetMetaSelect.getColumnCount(); ij++) {
                                                        
                            if (insertCounter == 0) {
                                buffterMeta.append(rsetMetaSelect.getColumnName(ij) + ", ");
                                hm.put(rsetMetaSelect.getColumnName(ij), rsetMetaSelect.getColumnTypeName(ij));
                            }

//                                System.out.println(rsetMetaSelect.getColumnTypeName(ij) + ":" + rsetMetaSelect.getColumnName(ij) + "-->" + rsetSelect.getString(ij));
                                if ((rsetSelect.getString(rsetMetaSelect.getColumnName(ij)) == null)) {
                                    buffterValue.append("" + "NULL" + ",");
                                } else {
                                    if ((hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("char")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("image")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("nchar")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("ntext")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("nvarchar")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("text")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("varchar")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("bpchar")) {
                                        buffterValue.append(" N'" + (refineData(rsetSelect.getString(ij))) + "',");
                                    } else if ((hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("bool")) {
                                        if (rsetSelect.getString(ij).equalsIgnoreCase("f")) {
                                            buffterValue.append("0, ");
                                        } else {
                                            buffterValue.append("1, ");
                                        }
                                    } else if ((hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("bytea")) {
                                        buffterValue.append("" + "NULL" + ",");
                                    } else if ((hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("datetime")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("date")
                                            || (hm.get(rsetMetaSelect.getColumnName(ij))).toString().equalsIgnoreCase("timestamp")) {
                                        if (rsetSelect.getString(ij).equals("0000-00-00 00:00:00")) {
                                            buffterValue.append("" + "NULL" + ",");
                                        } else {
//                                            System.out.println(rsetSelect.getString(ij));
                                            buffterValue.append("'" + rsetSelect.getString(ij) + "',");
                                        }
                                    } else {
                                        buffterValue.append("" + rsetSelect.getString(ij) + ",");
                                    }
                                }
                        }

                        if (insertCounter == 0) {
                            buffterMeta.deleteCharAt(buffterMeta.lastIndexOf(","));
                        }


                        buffterValue.deleteCharAt(buffterValue.lastIndexOf(","));

                        switch (jk) {
                            case 0:
                                insertString = "INSERT INTO " + prefixTempDB + "t015_psstaffcurr (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t015_psstaffcurr";
                                break;
                            case 1:
                                insertString = "INSERT INTO " + prefixTempDB + "t014_policestationbeat (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t014_policestationbeat";
                                break;
                            case 2:
                                insertString = "INSERT INTO " + prefixTempDB + "t1_registration (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1_registration";
                                break;
                            case 3:
                                insertString = "INSERT INTO " + prefixTempDB + "t201_fir (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t201_fir";
                                break;
                            case 4:
                                insertString = "insert into  " + prefixTempDB + "t311_transfer (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t311_transfer";
                                break;
                            case 5:
                                insertString = "insert into  " + prefixTempDB + "t101_actsection  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t101_actsection";
                                break;
                            case 6:
                                insertString = "insert into " + prefixTempDB + "t301_crime  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t301_crime";
                                break;
                            case 7:
                                insertString = "insert into " + prefixTempDB + "t301b_crime  (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t301b_crime";
                                break;
                            case 8:
                                insertString = "insert into " + prefixTempDB + "t102_Person (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t102_Person";
                                break;
                            case 9:
                                insertString = "insert into " + prefixTempDB + "t1021_Personal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1021_Personal";
                                break;
                            case 10:
                                insertString = "insert into " + prefixTempDB + "t2013_victim (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t2013_victim";
                                break;
                            case 11:
                                insertString = "insert into " + prefixTempDB + "t305_witness (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t305_witness";
                                break;
                            case 12:
                                insertString = "insert into " + prefixTempDB + "t2011_accused (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t2011_accused";
                                break;
                            case 13:
                                insertString = "INSERT INTO " + prefixTempDB + "t303_arrest (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t303_arrest";
                                break;
                            case 14:
                                insertString = "insert into " + prefixTempDB + "t10221_physical (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t10221_physical";
                                break;
                            case 15:
                                insertString = "insert into " + prefixTempDB + "t10222_physical (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t10222_physical";
                                break;
                            case 16:
                                insertString = "insert into " + prefixTempDB + "t103_properties (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t103_properties";
                                break;
                            case 17:
                                insertString = "insert into " + prefixTempDB + "t1031_automobile (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1031_automobile";
                                break;
                            case 18:
                                insertString = "insert into " + prefixTempDB + "t304a_seizure (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t304a_seizure";
                                break;
                            case 19:
                                insertString = "insert into " + prefixTempDB + "t1034a_currency (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1034a_currency";
                                break;
                            case 20:
                                insertString = "insert into " + prefixTempDB + "t1034b_currency (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1034b_currency";
                                break;
                            case 21:
                                insertString = "insert into " + prefixTempDB + "t1032_cultural (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1032_cultural";
                                break;
                            case 22:
                                insertString = "insert into " + prefixTempDB + "t1035_narcotics (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1035_narcotics";
                                break;
                            case 23:
                                insertString = "insert into " + prefixTempDB + "t1033_numbered (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t1033_numbered";
                                break;
                            case 24:
                                insertString = "insert into " + prefixTempDB + "t312_finalreport (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t312_finalreport";
                                break;
                            case 25:
                                insertString = "insert into " + prefixTempDB + "t312b_fraccused (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t312b_fraccused";
                                break;
                            case 26:
                                insertString = "insert into " + prefixTempDB + "t312c_fractsec (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t312c_fractsec";
                                break;
                            case 27:
                                insertString = "insert into " + prefixTempDB + "t3034_remandcustody (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t3034_remandcustody";
                                break;
                            case 28:
                                insertString = "insert into " + prefixTempDB + "t3033_bail (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t3033_bail";
                                break;
                            case 29:
                                insertString = "insert into " + prefixTempDB + "t406a_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t406a_courtdisposal";
                                break;
                            case 30:
                                insertString = "insert into " + prefixTempDB + "t406b_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t406b_courtdisposal";
                                break;
                            case 31:
                                insertString = "insert into " + prefixTempDB + "t406c_courtdisposal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t406c_courtdisposal";
                                break;
                            case 32:
                                insertString = "insert into " + prefixTempDB + "t202_missing (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t202_missing";
                                break;
                            case 33:
                                insertString = "insert into " + prefixTempDB + "t205_unnatural (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t205_unnatural";
                                break;
                            case 34:
                                insertString = "insert into " + prefixTempDB + "t204_mlc (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t204_mlc";
                                break;
                            case 35:
                                insertString = "insert into " + prefixTempDB + "t207_others (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t207_others";
                                break;
                            case 36:
                                insertString = "insert into " + prefixTempDB + "t501a_criminal (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t501a_criminal";
                                break;
                            case 37:
                                insertString = "insert into " + prefixTempDB + "t5011_criaddress (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5011_criaddress";
                                break;
                            case 38:
                                insertString = "insert into " + prefixTempDB + "t5027_bank (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5027_bank";
                                break;
                            case 39:
                                insertString = "insert into " + prefixTempDB + "t5012_criknowns (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5012_criknowns";
                                break;
                            case 40:
                                insertString = "insert into " + prefixTempDB + "t5023_operationarea (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5023_operationarea";
                                break;
                            case 41:
                                insertString = "insert into " + prefixTempDB + "t5021_general (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5021_general";
                                break;
                            case 42:
                                insertString = "insert into " + prefixTempDB + "t5022_affiliation (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5022_affiliation";
                                break;
                            case 43:
                                insertString = "insert into " + prefixTempDB + "t5026_employment (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5026_employment";
                                break;
                            case 44:
                                insertString = "insert into " + prefixTempDB + "t5025_political (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5025_political";
                                break;
                            case 45:
                                insertString = "insert into " + prefixTempDB + "t5024_notices (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5024_notices";
                                break;
                            case 46:
                                insertString = "insert into " + prefixTempDB + "t503_gang (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t503_gang";
                                break;
                            case 47:
                                insertString = "insert into " + prefixTempDB + "t5037_frontalorg (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5037_frontalorg";
                                break;
                            case 48:
                                insertString = "insert into " + prefixTempDB + "t5032_support (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5032_support";
                                break;
                            case 49:
                                insertString = "insert into " + prefixTempDB + "t5034_transport (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5034_transport";
                                break;
                            case 50:
                                insertString = "insert into " + prefixTempDB + "t5033_holdings (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5033_holdings";
                                break;
                            case 51:
                                insertString = "insert into " + prefixTempDB + "t5035_training (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5035_training";
                                break;
                            case 52:
                                insertString = "insert into " + prefixTempDB + "t5036_hideouts (" + buffterMeta.toString() + ") values (" + buffterValue.toString() + ");";
                                insertingTable = "t5036_hideouts";
                                break;
                        }
//                        System.out.println(insertString);
                        out.println(insertString);
                        buffterMeta.delete(0, buffterMeta.length());
                        buffterValue.delete(0, buffterValue.length());

//                        System.out.println(insertCounter + " row(s) inserted...");
                        insertingTables[jk] = insertingTable + ":" + recordsFound[jk];//+ ":" + recordsInserted[jk];
                    }
                    recordsInserted[jk] = insertCounter;
                    insertingTables[jk] = insertingTables[jk].toString().trim() + ":" + recordsInserted[jk];
//                    //------------------------------------------------------------------
                }
            } catch (Exception ex) {
                logger.log(CctnsLogger.ERROR, ex);
//                ex.printStackTrace();
            }
        }
        out.close();
        return insertingTables;
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

        return sOut9;
    }
}
