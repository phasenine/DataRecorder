/*
 * Constants.java
 *
 * Created on February 3, 2014, 10:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

/**
 *
 * @author FKY301079
 */
public class Constants {
    
    // Software Version
    public static final String CUSTOMER = "P&WC";
    //public static final String CUSTOMER = "SE-C";

    public static final String MAN_OVERRIDE_PWD = "dcuv";
    
    public static final String SW_VERSION = " v0.0.5"; // numbers 1.0.0 or subsequent version must only be 5 chars long
    public static final String DCU5FILE_SW_VERSION = SW_VERSION.substring(SW_VERSION.length()-5);
    
    public static final int MAX_BLOCK_ID_NUMBER = 7692;
    
    // DCU V Command Status
    public static final int DCU_MEMORY_FULL = 1;
    public static final int TRAN_INTEGRITY_FAILURE = 2;
    public static final int INVALID_BLOCK_ID = 4;
    public static final int DCU_HW_FAUILRE = 8;
    public static final int LAST_CMD_FAULT = 16;
    public static final int DATA_INTEGRITY_FAILURE = 32;
    public static final int DCU_BUSY = 64;
    public static final int READ_ONLY_BLOCK = 128;


    //DCU V Maint. Command Status
    public static final int INVALID_CMD_CODE           = 0x0001;  //D0
    public static final int UART_COMM_ERROR            = 0x0002;  //D1
    public static final int ILLEGAL_DATA_ID            = 0x0004;  //D2
    public static final int DCU_SYSTEM_FAILURE         = 0x0008;  //D3
    public static final int FAULT_LAST_CMD             = 0x0010;  //D4
    public static final int TRANSMISSION_CRC_FAILURE   = 0x0020;  //D5
    public static final int RESERVED_1                 = 0x0040;  //D6
    public static final int OVERWRITE_READ_ONLY        = 0x0080;  //D7
    public static final int COMFIG_DATA_OUT_RANGE      = 0x0100;  //D8
    public static final int CONFIG_DATA_CONFLICT       = 0x0200;  //D9
    public static final int RESERVED_2                 = 0x0400;  //D10
    public static final int TEST_READ_BLOCK_NOT_EXIST  = 0x0800;  //D11    
    
    //---------- command result ----------
    public static final int OK = 0;

    //---------- command code -------------
    public static final int INVOKEBAUDRATE = 1;
    public static final int RECEIVEBAUDRATE = 2;
    public static final int READSTATUS = 3;
    public static final int READDATA = 4;
    public static final int WRITEDATA = 5;
    public static final int READDCUID = 6;
    public static final int READCONFIG = 7;
    public static final int SETBLOCKSIZE = 9;
    public static final int REBOOT = 0xA;
    public static final int SETUART = 0xB;

    //----------- maintenance command code --------
    public static final int MAINT_INIT_CMD = 0xAA;
    public static final int MAINT_CONFIG_CMD = 1;
    public static final int MAINT_REQ_VER_CMD = 2;
    public static final int MAINT_DIS_CONFIG_CMD = 3;
    public static final int MAINT_REBOOT_CMD = 4;
    public static final int MAINT_DATA_ERASE_CMD = 5;
    public static final int MAINT_INVALIDATE_DATA_CMD = 0xB;
    public static final int MAINT_TEST_WRITE_CMD = 6;
    public static final int MAINT_TEST_READ_CMD = 7;
    public static final int MAINT_WRITE_RO_BLOCK_CMD = 8;
    public static final int MAINT_DATA_DOWNLOAD_CMD = 9;
    public static final int MAINT_READ_STATUS_CMD = 0xA;
    public static final int MAINT_CONFIG_UNIT_ID = 0xE0;
    public static final int MAINT_CALCULATE_CRC = 0xE1;
    public static final int MAINT_UPLOAD_SOFTWARE = 0xCC;
    public static final int MAINT_GET_CONFIG_COUNTER = 0xE3;
    public static final int MAINT_GET_TEMP_RECORD = 0xE4;
    public static final int MAINT_CHANGE_BAUDRATE = 0xEE;
    public static final int MAINT_ACTIVE_BAUDRATE = 0xEF;


    //----- sysConfig->cmdSet --------
    public static final int DCUII = 0xAA;
    public static final int DCUIV = 0xCC;
    public static final int DCUV = 0x55;

    //-------- baud rate config ----------
    //public static final int  BAUD_9600 = 1;
    //public static final int  BAUD_19200 = 2;
    //public static final int  BAUD_38400 = 3;
    //public static final int   BAUD_57600 = 4;
    //public static final int  BAUD_115200 = 4;
    //-------- command buffer size ----------
    public static final int BUF_SIZE = 1100;                        
    
    public static final int PART_SERIAL_NUM_LEN = 16;
    
    public static final String edtFileName = new String("62996-01.txt");
    
    /** Creates a new instance of Constants */
    private Constants() {
    }
    
}
