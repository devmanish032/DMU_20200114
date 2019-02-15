/*
 * To change this template, choose Tools | Templates
 * and open the template in the ediftor.
 */
package cipaserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import logger.CctnsLogger;
//import xlstodatabase.XLStoDB;
public class SQLConnection {

    public Connection connection;
    
    public String[] str = new String[7];
    private static CctnsLogger logger = CctnsLogger.getInstance(SQLConnection.class.getName());

    public void callRetriveMethod(String[] strValue) {
        for (int i = 0; i < strValue.length; i++) {
            str[i] = strValue[i];
//      System.out.println("str[retriving] " + str[i]);
        }
    }

    public Connection getConnectionDetails(String sourceORtarget) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {

//    for (int i = 0; i < str.length; i++) {
//      System.out.println("str[getting] " + str[i]);
//    }

        Statement stat = null;
        if (str[5].toString().equals("Linux") && str[4].toString().equals("MySql")) {
            Class.forName("com.mysql.jdbc.Driver");
            String urlData1 = "jdbc:mysql://" + (str[2]).toString().trim() + ":" + str[6] + "/" + (str[3]).toString().trim() + "?useUnicode=true&characterEncoding=utf8";//+"?characterEncoding=utf8&useUnicode=true&useCursorFetch=true&defaultFetchSize=1000";//?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8";
            //getConnection(  "jdbc:mysql://localhost:3306/cctns_kochi?useUnicode=true&characterEncoding=utf8&user=root&password=abcd");?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8
            System.out.println(">>>"+urlData1);
            connection = DriverManager.getConnection(urlData1, str[0].toString(), str[1].toString());

        } else if (str[5].contains("Windows") && str[4].toString().equals("SQL Server")) {
            // System.out.println(str[0]);
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //String urlData2 = "jdbc:sqlserver://" + (str[2]).toString().trim() + ";databaseName=" + (str[3]).toString().trim();
            String urlData2 = "jdbc:sqlserver://" + (str[2]).toString().trim() + ";databaseName=" + (str[3]).toString().trim() + ";CharacterSet=UTF-8";
            //String urlData2 = "jdbc:sqlserver://" + (str[2]).toString().trim() + ";databaseName=" + (str[3]).toString().trim() + ";useUnicode=true;characterEncoding=utf8";
            
           
//                System.out.println(urlData2);
//                System.out.println("static: "+"jdbc:sqlserver://dvstdbwn01\\mssqlservercctns;databaseName=stagging");
//                System.out.println(str[2].toString()+""+ str[3].toString());

            connection = DriverManager.getConnection(urlData2, str[0].toString(), str[1].toString());
        } else {
            // System.out.println(str[0]);
            Class.forName("org.postgresql.Driver");
//                            "jdbc:postgresql://localhost/cipa","postgres", "administrator"
            String urlData2 = "jdbc:postgresql://" + (str[2]).toString().trim() + ":" + str[6] + "/" + (str[3]).toString().trim();
                System.out.println("url database is "+urlData2);
//                System.out.println("static: "+"jdbc:sqlserver://dvstdbwn01\\mssqlservercctns;databaseName=stagging");
//                System.out.println(str[2].toString()+""+ str[3].toString());

            connection = DriverManager.getConnection(urlData2, str[0].toString(), str[1].toString());

        }
        //========================    
        if (connection != null) {
            Properties props = new Properties();
            try {
                File tofolder = new File(System.getProperty("user.dir"));
                File propFile = new File(tofolder + "/ServerConnection.properties");
                System.out.println(propFile);
                if (!propFile.exists()) {
                    propFile.createNewFile();
                }
                //========================
                if (sourceORtarget.equals("source")) {
                    FileInputStream fileIn = new FileInputStream(propFile);
                    props.load(fileIn);
                    if (props.getProperty("T_userName") != null) {
                        props.setProperty("T_userName", props.getProperty("T_userName"));
                        props.setProperty("T_password", props.getProperty("T_password"));
                        props.setProperty("T_ipAddress", props.getProperty("T_ipAddress"));
                        props.setProperty("T_databaseName", props.getProperty("T_databaseName"));
                        props.setProperty("T_databaseType", props.getProperty("T_databaseType"));
                        props.setProperty("T_operatingType", props.getProperty("T_operatingType"));
                        props.setProperty("T_port", props.getProperty("T_port"));
                    }
                    props.setProperty("S_userName", str[0]);
                    props.setProperty("S_password", str[1]);
                    props.setProperty("S_ipAddress", str[2]);
                    props.setProperty("S_databaseName", str[3]);
                    props.setProperty("S_databaseType", str[4]);
                    props.setProperty("S_operatingType", str[5]);
                    props.setProperty("S_port", str[6]);

                    FileOutputStream fileOut = new FileOutputStream(propFile);
                    props.store(fileOut, "Source Server Connection Details");
                    fileOut.close();
                } else {
                    FileInputStream fileIn = new FileInputStream(propFile);
                    props.load(fileIn);

                    if (props.getProperty("S_userName") != null) {
                        props.setProperty("S_userName", props.getProperty("S_userName"));
                        props.setProperty("S_password", props.getProperty("S_password"));
                        props.setProperty("S_ipAddress", props.getProperty("S_ipAddress"));
                        props.setProperty("S_databaseName", props.getProperty("S_databaseName"));
                        props.setProperty("S_databaseType", props.getProperty("S_databaseType"));
                        props.setProperty("S_operatingType", props.getProperty("S_operatingType"));
                        props.setProperty("S_port", props.getProperty("S_port"));
                    }

                    props.setProperty("T_userName", str[0]);
                    props.setProperty("T_password", str[1]);
                    props.setProperty("T_ipAddress", str[2]);
                    props.setProperty("T_databaseName", str[3]);
                    props.setProperty("T_databaseType", str[4]);
                    props.setProperty("T_operatingType", str[5]);
                    props.setProperty("T_port", str[6]);

                    FileOutputStream fileOut = new FileOutputStream(propFile);
                    props.store(fileOut, "Target Server Connection Details");
                    fileOut.close();
                }
                return connection;
            } catch (Exception ee) {
                logger.log(CctnsLogger.ERROR, ee);
//                ee.printStackTrace();
            }
        }
        //================================================================
        return connection;
    }

    public Connection SQLCon(String sourceORtarget) {
        Properties props = new Properties();
        try {
            File tofolder = new File(System.getProperty("user.dir"));
            File propFile = new File(tofolder + "/ServerConnection.properties");
            FileInputStream in = new FileInputStream(propFile);
            props.load(in);
            String username = null,
                    password = null,
                    ipaddress = null,
                    databasename = null,
                    databasetype = null,
                    opertingsystem = null,
                    port = null;
            //========================
            if (sourceORtarget.equals("source")) {
                username = props.getProperty("S_userName");
                password = props.getProperty("S_password");
                ipaddress = props.getProperty("S_ipAddress");
                databasename = props.getProperty("S_databaseName");
                databasetype = props.getProperty("S_databaseType");
                opertingsystem = props.getProperty("S_operatingType");
                port = props.getProperty("S_port");
                in.close();
            } else {
                username = props.getProperty("T_userName");
                password = props.getProperty("T_password");
                ipaddress = props.getProperty("T_ipAddress");
                databasename = props.getProperty("T_databaseName");
                databasetype = props.getProperty("T_databaseType");
                opertingsystem = props.getProperty("T_operatingType");
                port = props.getProperty("T_port");
                in.close();
//        FileOutputStream fileOut = new FileOutputStream(propFile);
//        fileOut.close();
            }
//------------------------------------------------------------------------------
            if (opertingsystem.equals("Linux") && databasetype.equals("MySql")) {
                Class.forName("com.mysql.jdbc.Driver");
                String urlData1 = "jdbc:mysql://" + ipaddress + ":" + port + "/" + databasename;
                connection = DriverManager.getConnection(urlData1, username, password);
            } else if (opertingsystem.equals("Windows") && databasetype.equals("SQL Server")) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String urlData2 = "jdbc:sqlserver://" + ipaddress + ";databaseName=" + databasename;
                connection = DriverManager.getConnection(urlData2, username, password);
            } else {
                Class.forName("org.postgresql.Driver");
                //"jdbc:postgresql://localhost/cipa","postgres", "administrator"
                String urlData2 = "jdbc:postgresql://" + ipaddress + ":" + port + "/" + databasename;
                connection = DriverManager.getConnection(urlData2, username, password);
            }
        } catch (Exception ee) {
            logger.log(CctnsLogger.ERROR, ee);
//            ee.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() throws ClassNotFoundException, SQLException {
        connection.close();
    }
}
