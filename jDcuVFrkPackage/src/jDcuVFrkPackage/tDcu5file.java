/*
 * tDcu5file.java
 *
 * Created on March 18, 2014, 4:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream; 
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.nio.charset.Charset;


/**
 *
 * @author FKY301079
 */
public class tDcu5file {

    final int EIGHT_BITS_PER_BYTE           = 8;    
    final int LAST_BLOCK_NUMBER             = 236;    
    final int NUM_HEADER_FIELDS             = 4;
    final int HEADER_FIELD_SIZE             = 256;
    final int HEADER_BLK_CNT_FIELD_SIZE     = 3;
    final int VERSION_FIELD_SIZE            = 5;
    final int MODEL_FIELD_SIZE              = 41;
    final int DCU_PART_FIELD_SIZE           = 41;
    final int AIRCRAFT_SN_FIELD_SIZE        = 41;
    final int ENGINE_POS_FIELD_SIZE         = 3;
    final int ENGINE_SN_FIELD_SIZE          = 21;
    final int RESERVED_FIELD_SIZE           = 3;
    final int CORRUPTED_FIELD_SIZE          = 256;
    final int CORRUPTED_BLK_CNT_FIELD_SIZE  = 3;
    final int NUM_CORRUPTED_FIELDS          = 4;
    final int BYTE_FILL_FIELD_SIZE          = 5;
    final int NUM_READONLY_FIELDS           = 4;
    final int READONLY_FIELD_SIZE           = 256;
    final int READONLY_BLK_CNT_FIELD_SIZE   = 3;
    final int BLKSIZE_FIELD_SIZE            = 5;
    final int EEPROM_FIELD_SIZE             = 2;
    final int FILLER_FIELD_SIZE             = 256-5-41-41-41-3-21-3-3-3-5-3-5-2;        // 80

    public byte[][] header      = new byte[NUM_HEADER_FIELDS][HEADER_FIELD_SIZE];       // 8192
    public byte[][] readonly    = new byte[NUM_READONLY_FIELDS][READONLY_FIELD_SIZE];   // 8192
    public byte[][] corrupted   = new byte[NUM_CORRUPTED_FIELDS][CORRUPTED_FIELD_SIZE];   // 8192

    // ------------------- Tail block ---------------------
    public byte[] version       = new byte[VERSION_FIELD_SIZE];                         //    5
    public byte[] model         = new byte[MODEL_FIELD_SIZE];                           //   41
    public byte[] dcu_part      = new byte[DCU_PART_FIELD_SIZE];                        //   41
    public byte[] aircraft_sn   = new byte[AIRCRAFT_SN_FIELD_SIZE];                     //   41
    public byte[] engine_pos    = new byte[ENGINE_POS_FIELD_SIZE];                      //    3
    public byte[] engine_sn     = new byte[ENGINE_SN_FIELD_SIZE];                       //   21
    public byte[] reserved      = new byte[RESERVED_FIELD_SIZE];                        //    3
    public byte[] header_blks   = new byte[HEADER_BLK_CNT_FIELD_SIZE];                  //    3
    public byte[] corrupted_blks= new byte[CORRUPTED_FIELD_SIZE];                       //    3
    public byte[] byte_fill     = new byte[BYTE_FILL_FIELD_SIZE];                       //    5
    public byte[] readonly_blks = new byte[READONLY_BLK_CNT_FIELD_SIZE];                //  256
    public byte[] blkSize       = new byte[BLKSIZE_FIELD_SIZE];                         //    5
    public byte[] eprom         = new byte[EEPROM_FIELD_SIZE];                          //    2
    public byte[] filler        = new byte[FILLER_FIELD_SIZE];                          //   80
    // ----------------------------------------------------

    Runtime runtime;
    Process process;

    
    private List<tEngineDataBlock> engineDataReadbackList = new ArrayList<tEngineDataBlock>();    

    public List<tEdtReadbackBlockData> edtReadbackBlocks = new ArrayList<tEdtReadbackBlockData>();    
        
    /** Creates a new instance of tDcu5file */
    public tDcu5file()
    {                        
    }

    
}
