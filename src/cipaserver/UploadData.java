/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import logger.CctnsLogger;

/**
 *
 * @author sdanaresh
 */
public class UploadData {

    String selectedState, selectedDistrict, selectedPS,
            selectedFromMM, selectedFromYYYY, selectedToMM, selectedToYYYY;
    public String[] str = new String[10];
    String keyState, keyDistrict, keyPS;
    public Connection connectionSource, connectionTarget;
    String sourceORtarget;
    String prefixStagging;
    String prefixTempDB;
    String locationName[];
    private static CctnsLogger logger = CctnsLogger.getInstance(UploadData.class.getName());


    public UploadData() {
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
                prefixStagging = "";
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

    public String[] executeUploadBlock(String fileName) throws FileNotFoundException, IOException, SQLException {
        FileInputStream fstream = new FileInputStream(fileName);
        DataInputStream in = new DataInputStream( fstream);
        //BufferedReader br = new BufferedReader(new InputStreamReader(in));
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF8"));
        String strLine;

        java.sql.DatabaseMetaData dmd = null;
        dmd = connectionTarget.getMetaData();
        if (!(dmd.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
            //For MySql
            prefixStagging = "";
            prefixTempDB = "CIPATemp_DB.";
        } else {
            //For MSSQL
            prefixStagging = "";
            prefixTempDB = "CIPATemp_DB.dbo.";
        }

        //Read File Line By Line
        int i = 0;
        locationName = new String[3];
//        for (int ii = 0; i < 7; ii++) {
//            strLine = br.readLine();
//        }
        while ((strLine = br.readLine()) != null) {
            if (strLine.contains("-- State          : ")) {
                locationName[0] = (strLine.substring(strLine.indexOf(':') + 1, strLine.length())).trim();
            }
            if (strLine.contains("-- District       : ")) {
                locationName[1] = (strLine.substring(strLine.indexOf(':') + 1, strLine.length())).trim();

            }
            if (strLine.contains("-- Police Station : ")) {
                locationName[2] = (strLine.substring(strLine.indexOf(':') + 1, strLine.length())).trim();
            }

            try {
                PreparedStatement pstmtSP;
                String str = null;
                if ((dmd.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
                    str = strLine.replace("CIPATemp_DB.", "CIPATemp_DB.dbo.");
                    //System.out.println(str);
                    pstmtSP = connectionTarget.prepareStatement(str);
                    i = pstmtSP.executeUpdate();
//                    System.out.println(i + " row inserted");
                }
                else{
//                    System.out.println(strLine);
                    pstmtSP = connectionTarget.prepareStatement(strLine);
                    i = pstmtSP.executeUpdate();
//                    System.out.println(i + " row inserted");
                }
                pstmtSP.close();
            } catch (Exception e) {
//                e.printStackTrace();
                logger.log(CctnsLogger.ERROR, e);  
            }
            i++;
        }
        fstream.close();
        return locationName;
    }
}
