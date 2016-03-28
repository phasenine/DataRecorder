/*
 * Globals.java
 *
 * Created on February 3, 2014, 4:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import java.util.Timer;

/**
 *
 * @author FKY301079
 */
public class GlobalVars {
    public static String PART_NUMBER = new String("ToBeDetermine   ");
    public static String NEW_PART_NUMBER = new String();
    public static String OLD_PART_NUMBER = new String();
    public static String SERIAL_NUMBER = new String("Unknown       ");
    public static String PWC_EDT_PART_NUMBER = new String();
    public static String SOFTWARE_VERSION = new String("S/W VERSION: DCUV300");
    public static String SOFTWARE_UPLOAD_VERSION = new String();
    public static String SOFTWARE_UPLOAD_CRC = new String();
    public static String SOFTWARE_CRC = new String();
    public static String DCU_ADDRESS = new String(" 24 ");
    public static String BAUD_RATE = new String("115211  ");
    public static String TRANSMIT_MODE = new String("Continuous");
    public static String DCU_TYPE = new String("Enhanced");
    public static String BLOCK_SIZE = new String("  90000 ");
    public static String TEST_ENABLE = new String("Disable");
    public static String SPARE = new String("none");
    public static String ENGINE_DATA_TYPE = new String("none");

    public static boolean gAutoBaudrate = false;
    public static boolean gAutoBaudrateQuit = false;
    public static boolean gEngineDataLoad = false;

    public static String gDataStorageDirectory = new String();
    public static String gEdtFilePath = new String();
    
    public static String gOperationMode;
    
    public static String gSecDepartment = new String("Engineering");

    public static double gProgressPercentage = 0.0;

    public static int gElapsedTimeSeconds = 0;

    public static Timer gElapsedTimer;
    
    public static String gCommPortName = new String();
    
    public static String gOsName = new String();
    
    /** Creates a new instance of Globals */
    private void GlobalVars() {
    }

    
    public static TsysConfig sysConfig = null;
    public static TdcuCommand dcuCommand = null;
    public static TMaintCommand maintCommand = null;
    public static CommunicationPort commPort = null;
    public static ConfigureUnitId configUnitId = null;
    public static DcuConfigure dcuConfig = null;
    public static TestLoginPwd testLoginPwd = null;

    public static CommunicationData commDataFrame = new CommunicationData(); 
    public static DcuVFrkMain dcuvFrkMain = null;
        
    public static TDSframe tdsDataFrame = null;
    
    public static Document commDataDoc = null;
    public static Document tdsDataDoc = null;
    
    public static boolean autoEdtProgramFlag;
    
    public static StartProgramming startProgrammingFrame = null;
    public static ManualOverridePwd manOverridePwd = null;
    public static ProgressFrame progressFrame = null;
   
    public static boolean sdb1IsSystemReserved = false;
}
