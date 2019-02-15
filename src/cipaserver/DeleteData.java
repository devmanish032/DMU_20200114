/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cipaserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import logger.CctnsLogger;

/**
 *
 * @author sdanaresh
 */
public class DeleteData {

    String prefixStagging;
    String prefixTempDB;
    String sourceORtarget;
    public Connection connectionSource, connectionTarget;
    private static CctnsLogger logger = CctnsLogger.getInstance(DeleteData.class.getName());


    public DeleteData() {
        java.sql.DatabaseMetaData dmd = null;
        try {
            sourceORtarget = "target";
            SQLConnection sqlcon = new SQLConnection();
            connectionTarget = sqlcon.SQLCon(sourceORtarget);
            dmd = connectionTarget.getMetaData();
            if (!(dmd.getDatabaseProductName().toString()).contains("Microsoft".toString())) {
                //For MySql
                prefixStagging = "";
                prefixTempDB = "";
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

    public void executeDeleteBlock() {

        String strDelete[] = new String[53];
        strDelete[0] = "delete FROM " + prefixTempDB + "t015_psstaffcurr".toLowerCase();
        strDelete[1] = "delete from " + prefixTempDB + "t014_policestationbeat".toLowerCase();
        strDelete[2] = "delete from " + prefixTempDB + "t1_registration".toLowerCase();
        strDelete[3] = "delete from " + prefixTempDB + "t201_fir".toLowerCase();
        strDelete[4] = "delete from " + prefixTempDB + "t311_transfer".toLowerCase();
        strDelete[5] = "delete FROM " + prefixTempDB + "t101_actsection".toLowerCase();
        strDelete[6] = "delete FROM " + prefixTempDB + "t301_crime".toLowerCase();
        strDelete[7] = "delete FROM " + prefixTempDB + "t301b_crime".toLowerCase();
        strDelete[8] = "delete FROM " + prefixTempDB + "t102_Person".toLowerCase();
        strDelete[9] = "delete FROM " + prefixTempDB + "t1021_Personal".toLowerCase();
        strDelete[10] = "delete FROM " + prefixTempDB + "t2013_victim".toLowerCase();
        strDelete[11] = "delete FROM " + prefixTempDB + "t305_witness".toLowerCase();
        strDelete[12] = "delete FROM " + prefixTempDB + "t2011_accused".toLowerCase();
        strDelete[13] = "delete FROM " + prefixTempDB + "t303_arrest".toLowerCase();
        strDelete[14] = "delete FROM " + prefixTempDB + "t10221_physical".toLowerCase();
        strDelete[15] = "delete FROM " + prefixTempDB + "t10222_physical".toLowerCase();
        strDelete[16] = "delete FROM " + prefixTempDB + "t103_properties".toLowerCase();
        strDelete[17] = "delete FROM " + prefixTempDB + "t1031_automobile".toLowerCase();
        strDelete[18] = "delete FROM " + prefixTempDB + "t304a_seizure".toLowerCase();
        strDelete[19] = "delete FROM " + prefixTempDB + "t1034a_currency".toLowerCase();
        strDelete[20] = "delete FROM " + prefixTempDB + "t1034b_currency".toLowerCase();
        strDelete[21] = "delete FROM " + prefixTempDB + "t1032_cultural".toLowerCase();
        strDelete[22] = "delete FROM " + prefixTempDB + "t1035_narcotics".toLowerCase();
        strDelete[23] = "delete FROM " + prefixTempDB + "t1033_numbered".toLowerCase();
        strDelete[24] = "delete FROM " + prefixTempDB + "t312_finalreport".toLowerCase();
        strDelete[25] = "delete FROM " + prefixTempDB + "t312b_fraccused".toLowerCase();
        strDelete[26] = "delete FROM " + prefixTempDB + "t312c_fractsec".toLowerCase();
        strDelete[27] = "delete FROM " + prefixTempDB + "t3034_remandcustody".toLowerCase();
        strDelete[28] = "delete FROM " + prefixTempDB + "t3033_bail".toLowerCase();
        strDelete[29] = "delete FROM " + prefixTempDB + "t406a_courtdisposal".toLowerCase();
        strDelete[30] = "delete FROM " + prefixTempDB + "t406b_courtdisposal".toLowerCase();
        strDelete[31] = "delete FROM " + prefixTempDB + "t406c_courtdisposal".toLowerCase();
        strDelete[32] = "delete FROM " + prefixTempDB + "t202_missing".toLowerCase();
        strDelete[33] = "delete FROM " + prefixTempDB + "t205_unnatural".toLowerCase();
        strDelete[34] = "delete FROM " + prefixTempDB + "t204_mlc".toLowerCase();
        strDelete[35] = "delete FROM " + prefixTempDB + "t207_others".toLowerCase();
        strDelete[36] = "delete FROM " + prefixTempDB + "t501a_criminal".toLowerCase();
        strDelete[37] = "delete FROM " + prefixTempDB + "t5011_criaddress".toLowerCase();
        strDelete[38] = "delete FROM " + prefixTempDB + "t5027_bank".toLowerCase();
        strDelete[39] = "delete FROM " + prefixTempDB + "t5012_criknowns".toLowerCase();
        strDelete[40] = "delete FROM " + prefixTempDB + "t5023_operationarea".toLowerCase();
        strDelete[41] = "delete FROM " + prefixTempDB + "t5021_general".toLowerCase();
        strDelete[42] = "delete FROM " + prefixTempDB + "t5022_affiliation".toLowerCase();
        strDelete[43] = "delete FROM " + prefixTempDB + "t5026_employment".toLowerCase();
        strDelete[44] = "delete FROM " + prefixTempDB + "t5025_political".toLowerCase();
        strDelete[45] = "delete FROM " + prefixTempDB + "t5024_notices".toLowerCase();
        strDelete[46] = "delete FROM " + prefixTempDB + "t503_gang".toLowerCase();
        strDelete[47] = "delete FROM " + prefixTempDB + "t5037_frontalorg".toLowerCase();
        strDelete[48] = "delete FROM " + prefixTempDB + "t5032_support".toLowerCase();
        strDelete[49] = "delete FROM " + prefixTempDB + "t5034_transport".toLowerCase();
        strDelete[50] = "delete FROM " + prefixTempDB + "t5033_holdings".toLowerCase();
        strDelete[51] = "delete FROM " + prefixTempDB + "t5035_training".toLowerCase();
        strDelete[52] = "delete FROM " + prefixTempDB + "t5036_hideouts".toLowerCase();


        PreparedStatement pstmtDelete;
        sourceORtarget = "target";
        SQLConnection sqlcon = new SQLConnection();
        connectionTarget = sqlcon.SQLCon(sourceORtarget);

        for (int ij = 0; ij < strDelete.length; ij++) {
            try {
                pstmtDelete = connectionTarget.prepareStatement(strDelete[ij].toString());
                pstmtDelete.executeUpdate();
                pstmtDelete.close();
            } catch (Exception ex) {
                logger.log(CctnsLogger.ERROR, ex);
//                ex.printStackTrace();
            }
        }
    }
}
