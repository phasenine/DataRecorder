/*
 * tEngineData.java
 *
 * Created on February 3, 2014, 3:01 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import javax.swing.JOptionPane;
import java.util.Arrays;

import java.io.File;

/**
 *
 * @author FKY301079
 */
public class tEngineData {
    private final int NUM_BYTES_PER_DATA = 2;   // each data[i] item is 2 bytes
    Runtime runtime;
    Process process;
    tDcu5file baselineDcu5File = new tDcu5file();                
    tEngineDataBlock engineDataBlock0 = null;
    tEngineDataBlock engineDataBlock1 = null;
    tEngineDataBlock engineDataBlock9 = null;
    tEngineDataBlock engineDataBlock205 = null;
    tEdtReadbackBlockData edtReadBackBlock205 = null;
   
            
    /** Creates a new instance of tEngineData */
    public tEngineData() {
    }

    private String partNumber = new String();
    public List<tEngineDataBlock> engineDataBlockList = new ArrayList<tEngineDataBlock>();

    public String performedBy = new String();
    public String password= new String();
    
    public int setup(String filename)
    {
        int returnValue = 0;
        boolean blockEnd = true;
        tEngineDataBlock tempEngineDataBlock;
        byte[] tempBuffer = new byte[128];
        byte[] tempId = new byte[10];
        int[] tempData = new int[80];
        FileInputStream fData;
        BufferedReader br;
        String strLine;
        int searchIndex = 0;
        int spaceIndex = 0;
        int tabIndex = 0;
        String tmpString;
        String[] tmpStringArray;

        int i;        


        try{
            //fData = new FileInputStream(filename);            
            fData = new FileInputStream(filename);            
            br = new BufferedReader(new InputStreamReader(fData));
        }
        catch (FileNotFoundException ex){
            System.err.println(ex.getMessage());
            JOptionPane.showMessageDialog(null,"Error !!!  EDT File 62996.txt not found on the DVD disk.\nThe DVD disk may be damaged.\nPlease inform your supervisor or manager of the problem.\nThe Utility program will shutdown now. ","DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);

            Utilities.shutdownSystem();
            
            return 1;
        }


        // Get the first line of the file which includes part number.
    try{
            strLine = br.readLine();

            //----------------------------------------------------------------
            // The part number is sandwiched between space and tab characters
            //----------------------------------------------------------------
            // find first space in the Part Number line
            spaceIndex = strLine.indexOf(" ", 0);

            // find the first tab character.  We can search from the space character onwards
            tabIndex = strLine.indexOf('\t', spaceIndex);

            // Grab the Part number
            GlobalVars.PART_NUMBER = strLine.substring(spaceIndex + 1,tabIndex);

        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }


        // Check for password if we are dealing with the SEC Production department
        if (GlobalVars.gSecDepartment.equals("Production"))
        {
            // Check password =? partNumber
            //if ( GlobalVars.testLoginPwd.jTextField2.getText() != GlobalVars.PART_NUMBER )
            if ( !(GlobalVars.PART_NUMBER.equals(GlobalVars.testLoginPwd.jTextField2.getText())) )
            {
                JOptionPane.showMessageDialog(null,"Incorrect password","DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                returnValue = 1;
            }  
        }

        // Get all the blocks of data
        try{
            // Keep reading each Data Block
            while ( ((strLine = br.readLine()) != null) && (returnValue ==0) )
            {
                // Line read in the loop check

                // find the first tab character.  We can search from the space character onwards
                tabIndex = strLine.indexOf('\t', 0);

                // Find the keyword "Block"
                searchIndex = strLine.indexOf("Block", 0);

                // Block keyword found
                if (searchIndex != -1)
                {
                    // Create new EngineDataBlock to hold the data
                    tempEngineDataBlock = new tEngineDataBlock();

                    // Read the block data
                    tempEngineDataBlock.blockId = Integer.parseInt(strLine.substring(searchIndex+7,tabIndex));

                    // Data block start
                    blockEnd = false;

                    // Check Block ID
                    if ( tempEngineDataBlock.blockId > Constants.MAX_BLOCK_ID_NUMBER )
                    {
                        //invalid block ID
                        returnValue = 2;
                    }
                    // Get the data
                    else
                    {
                        // build data block
                        //while ( ((strLine = br.readLine()) != null) && (blockEnd != true) && (returnValue ==0) )
                        while ((blockEnd != true) && (returnValue ==0) )
                        {
                            // read in the next line from the file
                            if ((strLine = br.readLine()) == null)
                            {
                                // we have reached the end of of the file.  Get out
                                returnValue = 4;
                            }
                            else // not end of file
                            {
                                // find the first tab character.  We can search from the space character onwards
                                tabIndex = strLine.indexOf('\t', spaceIndex);


                                // Check the line is not a blank line
                                if (tabIndex != -1)
                                {
                                    // Check each line to determin the data line, Checksum or CRC
                                    // Check if the line is CRC
                                    if ( (strLine.indexOf("CRC", 0) == 0) ||
                                            (strLine.indexOf("crc", 0) == 0) ||
                                            (strLine.indexOf("Crc", 0) == 0))
                                    {
                                        // Split the string into tokens using '\t' as the separator
                                        tmpStringArray = strLine.split("\\t");

                                        // Grab the 5th token which is the value we want
                                        tmpString = tmpStringArray[tmpStringArray.length-1];

                                        tempEngineDataBlock.addData(((int) Long.parseLong(tmpString,16)));

                                        //Check CRC
                                        if ( tempEngineDataBlock.blockCRCorChecksum !=
                                            tempEngineDataBlock.calculateBlockCRC(tempEngineDataBlock.data, tempEngineDataBlock.dataIndex))
                                        {
                                            // Checksum error
                                            JOptionPane.showMessageDialog(null,"CRC error in data block " + Integer.toString(tempEngineDataBlock.blockId),"DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                                            returnValue = 3;
                                        }


                                        if ((tempEngineDataBlock.dataIndex == 64) ||
                                                (tempEngineDataBlock.dataIndex == 128) ||
                                                (tempEngineDataBlock.dataIndex == 256) ||
                                                (tempEngineDataBlock.dataIndex == 512))
                                        {
                                            //CRC is outside of data block
                                            tempEngineDataBlock.CsCrcOutsideFlag = true;
                                        }
                                        else if ((tempEngineDataBlock.dataIndex == 62) ||
                                                (tempEngineDataBlock.dataIndex == 126) ||
                                                (tempEngineDataBlock.dataIndex == 254) ||
                                                (tempEngineDataBlock.dataIndex == 510))
                                        {
                                            //Put CRC at the end of the data block as per procedure
                                            tempEngineDataBlock.addData((tempEngineDataBlock.blockCRCorChecksum>>16) & 0x0000FFFF);
                                            tempEngineDataBlock.addData((tempEngineDataBlock.blockCRCorChecksum & 0x0000FFFF));
                                        }
                                        else
                                        {
                                            // CRC alignment error
                                            JOptionPane.showMessageDialog(null,"CRC alignment error in data block " + Integer.toString(tempEngineDataBlock.blockId),"DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                                            returnValue = 3;
                                        }
                                        tempEngineDataBlock.CRCFlag = true;
                                        // block end
                                        blockEnd = true;                                    
                                    }
                                    // Check if the line is Checksum
                                    else if ( (strLine.indexOf("checksum", 0) == 0) ||
                                            (strLine.indexOf("CHECKSUM", 0) == 0) ||
                                            (strLine.indexOf("Checksum", 0) == 0))
                                    {
                                        // Split the string into tokens using '\t' as the separator
                                        tmpStringArray = strLine.split("\\t");

                                        // Grab the 5th token which is the value we want
                                        tmpString = tmpStringArray[tmpStringArray.length-1];

                                        tempEngineDataBlock.blockCRCorChecksum = (int) (Long.parseLong(tmpString,16) & 0xFFFF);

                                        //Check the checksum
                                        if ( tempEngineDataBlock.blockCRCorChecksum !=
                                            ((tempEngineDataBlock.calculateBlockChecksum(tempEngineDataBlock.data, tempEngineDataBlock.dataIndex) + 1) & 0x0000FFFFL))
                                        {
                                            long expectedChecksum = ((tempEngineDataBlock.calculateBlockChecksum(tempEngineDataBlock.data, tempEngineDataBlock.dataIndex) + 1) & 0x0000FFFFL);
                                            // Checksum error
                                            JOptionPane.showMessageDialog(null,"Checksum error in data block " + Integer.toString(tempEngineDataBlock.blockId) + ".\nExpected block checksum " + Integer.toHexString((int)expectedChecksum) + "\nBut block in EDT file is " + Integer.toHexString(tempEngineDataBlock.blockCRCorChecksum) + ".","DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                                            returnValue = 3;
                                        }


                                        if ((tempEngineDataBlock.dataIndex == 64) ||
                                            (tempEngineDataBlock.dataIndex == 128) ||
                                            (tempEngineDataBlock.dataIndex == 256) ||
                                            (tempEngineDataBlock.dataIndex == 512))
                                        {
                                            //Checksum is outside of data block
                                            tempEngineDataBlock.CsCrcOutsideFlag = true;
                                        }
                                        else if ((tempEngineDataBlock.dataIndex == 63) ||
                                            (tempEngineDataBlock.dataIndex == 127) ||
                                            (tempEngineDataBlock.dataIndex == 255) ||
                                            (tempEngineDataBlock.dataIndex == 511))
                                        {
                                            //Checksum is inside of datablock
                                            //Put Checksum at the end of the data block as per procedure
                                            tempEngineDataBlock.addData(tempEngineDataBlock.blockCRCorChecksum);
                                        }
                                        else
                                        {
                                            // Checksum alignment error
                                            JOptionPane.showMessageDialog(null,"Checksum error in data block " + Integer.toString(tempEngineDataBlock.blockId),"DCU V GSE" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                                            returnValue = 3;
                                        }

                                        tempEngineDataBlock.CRCFlag = false;
                                        // block end
                                        blockEnd = true;                                    
                                    } // else if ( (strLine.indexOf("checksum", 0) == 0) ||
                                    // Data line
                                    else
                                    {
                                        // Split the string into tokens using '\t' as the separator
                                        tmpStringArray = strLine.split("\\t");

                                        // Grab the 5th token which is the value we want
                                        tmpString = tmpStringArray[tmpStringArray.length-1];

                                        tempEngineDataBlock.addData(((int) Long.parseLong(tmpString,16)));
                                    } // End of if, Check each line to determine the data line, Checksum or CRC                                

                                    //-----------------------------------
                                    // Finish parsing this Data Block
                                    //-----------------------------------
                                    // a Data block was read, add the Data block to list
                                    if ( blockEnd == true && returnValue == 0 )
                                    {
//TBD_Add_Test_Blocks                                                                           }
//if ((tempEngineDataBlock.blockId == 0) || (tempEngineDataBlock.blockId == 1) || (tempEngineDataBlock.blockId == 9) || (tempEngineDataBlock.blockId == 205))
//{
//    int returnVal = JOptionPane.showConfirmDialog(null,"Do you want add the following Block ID for upload : " + tempEngineDataBlock.blockId + " ?","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
//    if (returnVal == JOptionPane.YES_OPTION)
//    {    
                                        // push the block data into list
                                        engineDataBlockList.add(tempEngineDataBlock);
//    }
//}
//else
//{    
//                                        // push the block data into list
//                                        engineDataBlockList.add(tempEngineDataBlock);                                        
//}                                        
    
                                        //Reset the temp data block structure
                                        ///    tempEngineDataBlock.reset();
                                    } // End of if, a Data block was read, add the Data block to list                                    
                                } // if (tabIndex != -1)                            
                            }                                
                        } // build data block
                    } // // Get the data
                } // // Block keyword found           
            } // Keep reading each Data Block
        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }
 
            
        return 0;
    }

    public boolean upload(){
        int blockProgrammingProgress = 1;
        short offset;
        TcmdData cmdData = new TcmdData();
        final int FAIL_COUNT_LIMIT = 2;
        int failureCounter = 0;
        boolean result = true;
        
        //===================================================================================================
        // Program Blocks 0,1,5 and 9 using Write Data Command in Normal Mode
        //===================================================================================================
        for (tEngineDataBlock engineDataBlock : engineDataBlockList)
        {            
            if ((engineDataBlock.blockId == 0) || (engineDataBlock.blockId == 1) || (engineDataBlock.blockId == 9) || (engineDataBlock.blockId == 205))
            {
                Utilities.ProgressFrameSetTxt("Programming engine data blocks: " + blockProgrammingProgress++ + "/"  + engineDataBlockList.size() + " (Block ID : " + engineDataBlock.blockId + ")", (double) 0.01);

                // init command buffer offset
                offset = 0;

                // Set the data in to command buffer
                for (int i=0; i < engineDataBlock.dataIndex; i++)
                {        
                    cmdData.dataBlock[offset++] = (byte) ((engineDataBlock.data[i] >>> 8) & 0xFF);
                    cmdData.dataBlock[offset++] = (byte) (engineDataBlock.data[i] & 0xFF);
                }

                // Delay for 1.0 sec
                // Give the DCU V time to settle down before sending next block for programming
                // We do all the leg work upfront prior to sleeping for this period
                try
                {
                    TimeUnit.MILLISECONDS.sleep(1000);  // delay for 1000 milliseconds
                }
                catch(InterruptedException ex){
                    System.err.println(ex.getMessage());         
                }


                Utilities.CommDataFrameAppendTxt("\nBlock #: " + Integer.toString(engineDataBlock.blockId));

                if ((result = GlobalVars.dcuCommand.writeData(engineDataBlock.blockId, cmdData, false)) == false)
                    failureCounter++;

                GlobalVars.commDataFrame.jEditorPane1.setCaretPosition(GlobalVars.commDataDoc.getLength());
                GlobalVars.commDataFrame.jEditorPane1.update(GlobalVars.commDataFrame.jEditorPane1.getGraphics());
                GlobalVars.commDataFrame.update(GlobalVars.commDataFrame.getGraphics());

                if (GlobalVars.tdsDataFrame != null)
                {
                    GlobalVars.tdsDataFrame.jEditorPane1.update(GlobalVars.tdsDataFrame.jEditorPane1.getGraphics());                        
                    GlobalVars.tdsDataFrame.update(GlobalVars.tdsDataFrame.getGraphics());            
                }

                if (failureCounter > FAIL_COUNT_LIMIT)
                    return result;            
                
                // Delay for 1.0 sec
                // Give the DCU V time to settle down
                //try{
                //    TimeUnit.MILLISECONDS.sleep(1000);  // delay for 1000 milliseconds
                //}
                //catch(InterruptedException ex){
                //    System.err.println(ex.getMessage());         
                //}                
            }
        }

        // Delay for 1.0 sec
        // Give the DCU V time to settle down
        try{
            TimeUnit.MILLISECONDS.sleep(1000);  // delay for 1000 milliseconds
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());         
        }
        //===================================================================================================

        
        //---------------------------------------------------------------------------------------------------
        // Enter Maintenance Mode to program Read-Only Data Blocks
        //---------------------------------------------------------------------------------------------------
        cmdData.clear();
        GlobalVars.maintCommand.maintCommand(cmdData);                    

        Utilities.ProgressFrameSetTxt("Entering Maintenance Mode...", (double) 0.01);

        // Need some delay after entering Maintenance mode. Need to wait for DCU device to settle down
        // Sleep for 3000 ms
        try
        {
            TimeUnit.MILLISECONDS.sleep(3000);  // 2000ms delay
        }
        catch(InterruptedException ex){}   
        //---------------------------------------------------------------------------------------------------


        //===================================================================================================
        // Program Blocks other than 0,1,5 and 9 using Write RO Data Command in Maintenance Mode
        //===================================================================================================
        for (tEngineDataBlock engineDataBlock : engineDataBlockList)
        {            
            if ((engineDataBlock.blockId != 0) && (engineDataBlock.blockId != 1) && (engineDataBlock.blockId != 5) && (engineDataBlock.blockId != 9) && (engineDataBlock.blockId != 205))
            {
                Utilities.ProgressFrameSetTxt("Programming engine data blocks: " + blockProgrammingProgress++ + "/"  + engineDataBlockList.size() + " (Block ID : " + engineDataBlock.blockId + ")", (double) 0.01);

                // init command buffer offset
                offset = 0;

                // Set the data in to command buffer
                for (int i=0; i < engineDataBlock.dataIndex; i++)
                {        
                    cmdData.dataBlock[offset++] = (byte) ((engineDataBlock.data[i] >>> 8) & 0xFF);
                    cmdData.dataBlock[offset++] = (byte) (engineDataBlock.data[i] & 0xFF);
                }

                // Delay for 1.0 sec
                // Give the DCU V time to settle down before sending next block for programming
                // We do all the leg work upfront prior to sleeping for this period
                try
                {
                    TimeUnit.MILLISECONDS.sleep(1000);  // delay for 1000 milliseconds
                }
                catch(InterruptedException ex){
                    System.err.println(ex.getMessage());         
                }
                //--------------------------------------------------------------------------------            

                //Send data block
                Utilities.CommDataFrameAppendTxt("\nBlock #: " + Integer.toString(engineDataBlock.blockId));

                cmdData.clear();
                if ((result = GlobalVars.maintCommand.writeROBlock(engineDataBlock.blockId,cmdData)) == false)
                    failureCounter++;

                GlobalVars.commDataFrame.jEditorPane1.setCaretPosition(GlobalVars.commDataDoc.getLength());
                GlobalVars.commDataFrame.jEditorPane1.update(GlobalVars.commDataFrame.jEditorPane1.getGraphics());
                GlobalVars.commDataFrame.update(GlobalVars.commDataFrame.getGraphics());

                if (GlobalVars.tdsDataFrame != null)
                {
                    GlobalVars.tdsDataFrame.jEditorPane1.update(GlobalVars.tdsDataFrame.jEditorPane1.getGraphics());                        
                    GlobalVars.tdsDataFrame.update(GlobalVars.tdsDataFrame.getGraphics());            
                }

                if (failureCounter > FAIL_COUNT_LIMIT)
                    return result;
                
                // Delay for 1.0 sec
                // Give the DCU V time to settle down
                //try{
                //    TimeUnit.MILLISECONDS.sleep(1000);  // delay for 1000 milliseconds
                //}
                //catch(InterruptedException ex){
                //    System.err.println(ex.getMessage());         
                //}                
            }
        }
        //===================================================================================================

        
        return result;
    }

    public void readBack(){
        int returnVal = JOptionPane.CANCEL_OPTION;
        short offset;
        TcmdData cmdData = new TcmdData();
        int blockCounter = 1;
        String lowerByte, upperByte, checksumStr, storedChecksumStr;
        
        for (tEngineDataBlock engineDataBlock : engineDataBlockList)
        {
            Utilities.ProgressFrameSetTxt("Read-back EDT Block : " + blockCounter++ + "/" + engineDataBlockList.size() + " (Block ID: " + engineDataBlock.blockId + ")", (double) 0.01);
            
            //Utilities.CommDataFrameAppendTxt("\nBlock #: " + Integer.toString(engineDataBlock.blockId));
            Utilities.CommDataFrameAppendTxt("\nBlock #: " + engineDataBlock.blockId);

            // Delay for 0.1 sec.  This is to compensate for the slow USB-RS232 converter
            try{
                TimeUnit.MILLISECONDS.sleep(100);  // delay for 100 milliseconds
            }
            catch(InterruptedException ex){
                System.err.println(ex.getMessage());         
            }
            
            //read the RO data block as pr block id.
            cmdData.clear();
            GlobalVars.dcuCommand.readData(engineDataBlock.blockId, cmdData);            
            /*
                int checksum = 0;
                if (cmdData.cmdResult == false)
                {
                    returnVal = JOptionPane.showConfirmDialog(null,"Eror during read-back for Data Block ID #" + engineDataBlock.blockId + ".\nData block not found in DCU device's NVM.\nDo you want to continue with the upgrade?\nSelect YES to continue.\nSelect NO to terminate the Utility Program.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
                    if (returnVal == JOptionPane.NO_OPTION)
                        System.exit(0);

                }
            */
                        
            // Set the read back data into class structure.
            offset = 0;
            for (int i=0; i < engineDataBlock.dataIndex; i++)
            {
                engineDataBlock.readBackData[i] = cmdData.dataBlock[offset++] & 0xFF;
                engineDataBlock.readBackData[i] = ((engineDataBlock.readBackData[i] & 0xFF) << 8) + (cmdData.dataBlock[offset++] & 0xFF);                                
            }
            
            /*
                //------------------------------------------------------------------------------
                // Check the CRC value here
                // This is the only place where we are dealing with the EDT in byte[] array
                //
                // Notify the operator if there was an error with the CRC check
                // If there was an error, notify the operator which Block # had the CRC error
                // The program will have to shutdown immediately if there is a CRC error
                //------------------------------------------------------------------------------
                Utilities.CommDataFrameAppendTxt("\nDownload data for Block #: " + engineDataBlock.blockId + "\n");
                offset = 0;
                for (int i=0; i < engineDataBlock.dataIndex; i++)
                {
                    upperByte = String.format("%02x", cmdData.dataBlock[offset++]);
                    lowerByte = String.format("%02x", cmdData.dataBlock[offset++]);
                    Utilities.CommDataFrameAppendTxt(upperByte +lowerByte + " ");
                    if (((i%16) == 0) && (i != 0))
                        Utilities.CommDataFrameAppendTxt("\n");
                }            
                Utilities.CommDataFrameAppendTxt("\n\n");

                //checksum = Utilities.calculateCS(cmdData.dataBlock, (short) ((engineDataBlock.dataIndex * 2) - 2));
                checksum = Utilities.calculateCRC(cmdData.dataBlock, (short) ((engineDataBlock.dataIndex * 2) - 2), -1);
                if (((checksum & 0xFF00) >>> 8) != cmdData.dataBlock[(engineDataBlock.dataIndex*2)-2])
                {
                    upperByte = String.format("%02x", cmdData.dataBlock[(engineDataBlock.dataIndex*2)-2]);
                    lowerByte = String.format("%02x", cmdData.dataBlock[(engineDataBlock.dataIndex*2)-1]);
                    checksumStr = String.format("%04x", checksum);                
                    // Notify the user that the checksum for a specific EDT data block is not correct
                    JOptionPane.showMessageDialog(null,"Downloaded checksum does not match stored checksum for Block ID # " + engineDataBlock.blockId + " (block size: " + engineDataBlock.dataIndex + ")" + ".\nCalculated checksum : " + checksumStr + "\nDownloaded checksum : " + upperByte + lowerByte + "\nThe Utility Program will shutdown now.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                }
            */
        }        
    }

    public int createDcu5BaselineFile()
    {
        int returnVal = JOptionPane.CANCEL_OPTION;
        byte[] tmpReceivedBuffer = new byte[Constants.BUF_SIZE];        
        byte dataToWrite[];
        FileOutputStream outFile = null;
        int dataSize = 0;
        int maxId = 0;
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
        TsysConfig tempSysConfig = new TsysConfig();
        TcmdData cmdData;
        String dcuAddressStr = new String();
        String baudRateStr = new String();
        String dcuTypeStr = new String();
        String blockSizeStr = new String();
        String testEnableStr = new String();
        String transmitModeStr = new String();
        String spareStr = new String();
        File baselineFile;                
        tEdtReadbackBlockData edtDataBlock = null;
        int numNormalBlocks = 0;
        int numReadOnlyBlocks = 0;
        int numCorruptedBlocks = 0;
        String baselineFilename = new String("SN_");        
        byte[] tmpBytes = new byte[42];
        int numHeaderMapBlks = 0;        
        int numReadOnlyMapBlks = 0;
        int numCorruptedMapBlks = 0;
        String tempBlksStr = new String();

        //--------------------------------------
        // Send "Read DCU ID" command
        //--------------------------------------
        cmdData = new TcmdData();
        GlobalVars.dcuCommand.readDcuID(cmdData,false);

        //--------------------------------------
        // Open File
        //--------------------------------------		           
        baselineFilename += GlobalVars.SERIAL_NUMBER;
        baselineFilename += "PN_";
        baselineFilename += GlobalVars.PART_NUMBER;
        baselineFilename += "_";
        baselineFilename += GlobalVars.sysConfig.getBaudRate();
        baselineFilename += "_";
        switch(GlobalVars.sysConfig.getCmdSet())
        {
            case (int) Constants.DCUII:
                baselineFilename += "1";
                break;
            case (int) Constants.DCUIV:
                baselineFilename += "2";
                break;
            case (int) Constants.DCUV:
                baselineFilename += "3";
                break;
            default:
                baselineFilename += "3";
                break;
        }
        baselineFilename += ".DCU5";
        
        try
        {
            if (GlobalVars.gOsName.equals("Windows"))
            {               
                baselineFile = new File(GlobalVars.gDataStorageDirectory + baselineFilename);
            }
            else
                baselineFile = new File(GlobalVars.gDataStorageDirectory + "/" + baselineFilename);
           
            if (baselineFile.exists() == true)            
            {
                // Give the operator a warning that the Baseline file exist
                // Ask the operator if he/she still wants to proceed with the programming.
                // If yes, we will skip the "read-back" of the DCU device's memory
                returnVal = JOptionPane.showConfirmDialog(null,"DCU5 baseline file exist.\nSkip read-back and proceed with EDT programming?","DCU V FPK" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
                
                if (returnVal == JOptionPane.YES_OPTION)
                {
                    // Do not safe baseline file (no read-back), just proceed to program the EDT file
                }
                else
                {
                    // Notify the operator that the Utility program will shut down 
                    JOptionPane.showMessageDialog(null,"Since you chose not to proceed with Upgrade,\nthe Utility Program will shutdown now.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.INFORMATION_MESSAGE);
                    Utilities.shutdownSystem();
                }
            }
            else
            {
                // Create the Baseline file
                baselineFile.createNewFile();                    
                outFile = new FileOutputStream(baselineFile);
            }                        
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null,"Unable to create DCU5 baseline file.\nPlease ensure that you have read and write access to the specified directory.\nThe Utility Program will shutdown now.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
            return 6;
        }

        
        //---------------------------
        // Send Maintenance Command
        //---------------------------
        cmdData.clear();
        GlobalVars.maintCommand.maintCommand(cmdData);        
        
        //--------------------------------------
        // Send Display Configuration command
        //--------------------------------------
        cmdData.clear();
        GlobalVars.maintCommand.displayConfigure(cmdData);

        //tmpReceivedBuffer = cmdData.getReceive();                
        tmpReceivedBuffer = java.util.Arrays.copyOf(cmdData.receiveBuf, 14);


        // DCU Address
        offset = 3;
        tempSysConfig.setDcuAddress((int) tmpReceivedBuffer[offset]&0xFF);

        // baud rate
        offset = 4;
        tempSysConfig.setBaudRate((int) tmpReceivedBuffer[offset]&0xFF);

        // Transmit Mode
        offset = 5;
        tempSysConfig.setTransmitMode((int) tmpReceivedBuffer[offset]&0xFF);

        // DCU Type
        offset = 6;
        tempSysConfig.setCmdSet((int) tmpReceivedBuffer[offset]&0xFF);

        // Block size
        offset = 7;
        tempSysConfig.setBlockSize((int) tmpReceivedBuffer[offset]&0xFF);

        // Test Enable
        offset = 8;
        tempSysConfig.setTestEnable((int) tmpReceivedBuffer[offset]&0xFF);

        // Spare
        offset = 9;
        tempSysConfig.setSpare((int) tmpReceivedBuffer[offset]&0xFF);

        //----------------------------------------------------
        // Send Reboot command to get back to Normal Mode
        //----------------------------------------------------	
        cmdData.clear();
        returnVal = GlobalVars.maintCommand.reboot(cmdData);
        cmdStatusWord = cmdData.receiveBuf[2];
        cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
        if ((cmdStatusWord != 0) || (returnVal != 0))
        {
            JOptionPane.showMessageDialog(null,"Unexpected DCU error.\nFail To Reboot DCU.  Upgrade process discontinued.\nThe Utility Program will shutdown now.\nPlease inform your manager or supervisor of the error.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            Utilities.shutdownSystem();
        }
        
        // Command the DCU II to 115,200 baud
        Utilities.ProgressFrameSetTxt("Commanding DCU II device to 115,200 baud...", (double) 0.01);
        cmdData.clear();
        GlobalVars.dcuCommand.receiveBaudRate((byte) 4);
        cmdData.clear();
        GlobalVars.dcuCommand.invokeBaudRate();

        //------------------------------------------------------------------------------------
        // Set the Utility Program to 115,200 baud for faster download of engine data block
        //------------------------------------------------------------------------------------
        Utilities.ProgressFrameSetTxt("Setting Utility Program to 115,200 baud", (double) 0.01);
        Utilities.setConnectionBaudrate(115200);        
        
        // Set display to show that we are at 115,200 baud rate
        GlobalVars.progressFrame.jLabel16.setText("115200");
        
        //----------------------------------------------------
        // Set Data Block ID max. value based on Block Size
        //----------------------------------------------------
        //Figure out Maximum data ID use origenal configuration
        switch (GlobalVars.sysConfig.getCmdSet())
        {
        case Constants.DCUV: // DCU V
            if (tempSysConfig.getBlockSize() == 4)
            {
                //maxId = 1923;
                maxId = 300;
                dataSize = 1024;
                GlobalVars.progressFrame.jLabel17.setText("1024");
            }
            else if (tempSysConfig.getBlockSize() == 3)
            {
                //maxId =  3846;
                maxId = 300;
                dataSize = 512;
                GlobalVars.progressFrame.jLabel17.setText("512");
            }
            else if (tempSysConfig.getBlockSize() == 2)
            {
                //maxId = 7692;
                maxId = 300;
                dataSize = 256;
                GlobalVars.progressFrame.jLabel17.setText("256");
            }
            else if (tempSysConfig.getBlockSize() == 1)
            {
                //maxId = 7692;
                maxId = 300;
                dataSize = 128;
                GlobalVars.progressFrame.jLabel17.setText("128");
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Error: Configuration Conflict with block size.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
                return 4;
            }
            break;
            
        case Constants.DCUIV: // DCU IV
            //maxId = 7692;
            maxId = 300;
            dataSize = 128;
            GlobalVars.progressFrame.jLabel17.setText("128");
            break;
        case Constants.DCUII: // DCU II
            //maxId = 1982;
            maxId = 300;
            dataSize = 128;
            GlobalVars.progressFrame.jLabel17.setText("128");
            break;
        default:
            JOptionPane.showMessageDialog(null,"Error: Configuration Conflict with CMD set.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            return 5;
        }

        Utilities.ProgressFrameSetTxt("Reading DCU ID at 115,200 baud...", (double) 0.1);        
        //--------------------------------------
        // Send "Read DCU ID" command
        //--------------------------------------
        cmdData.clear();
        GlobalVars.dcuCommand.readDcuID(cmdData,false);

        Utilities.ProgressFrameSetTxt("Downloading data blocks at 115,200 baud...", (double) 0.1);        
        //-----------------------------------------------------------------
        // Download all entire area that stores the Engine Data Table(s)
        //-----------------------------------------------------------------        
        try
        {
            //-----------------------------------------------------------
            // Send as many ReadData commands as maxId value set earlier
            // Write all the data into the DCU5 Baseline File
            //-----------------------------------------------------------
            for (int dataId = 0; dataId<=maxId; dataId++)
            {
               Utilities.ProgressFrameSetTxt("Downloading data block # " + dataId + " / " + maxId, (double) 0.15);
               cmdData.clear();
               GlobalVars.dcuCommand.readData(dataId, cmdData);

               switch (cmdData.cmdStatus)
               {
                    case (byte) 0x00:    // normal data
                    {
                        // Set the header's Block ID bit
                        //
                        baselineDcu5File.header  [(int)(dataId / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        //------------------------------------------------------------------------------------
                        // Store the data into the variable baselineDcu5File, we will write the data later
                        //------------------------------------------------------------------------------------
                        edtDataBlock = new tEdtReadbackBlockData();                   
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    
                        edtDataBlock.numEntries = dataSize;

                        baselineDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        numNormalBlocks++;
                    }
                        break;
                    case (byte) 0x10:    // cmd CRC error
                    {
                        // Set our data block storage location to null since there is a CRC error
                        edtDataBlock = null;                        

                        JOptionPane.showMessageDialog(null,"CMD CRC error.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 7;
                    }   
                    case (byte) 0x14:    // data not exist
                    {
                        // Do nothing.  No "bit" will be captured in the bit-map within the header as expected

                        // Set our data block storage location to null since the data does not exist
                        edtDataBlock = null;                        
                    }   
                        break;
                    case (byte) 0x30:    // Data Storage and Retrieval (DSR) Integrity error
                    {
                        // Set the corrupted's Block ID bit
                        //
                        baselineDcu5File.header  [(int)(dataId / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));
                        baselineDcu5File.corrupted  [(int)(dataId / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        //------------------------------------------------------------------------------------
                        // Store the data into the variable baselineDcu5File, we will write the data later
                        //------------------------------------------------------------------------------------
                        edtDataBlock = new tEdtReadbackBlockData();                   
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    
                        edtDataBlock.numEntries = dataSize;

                        baselineDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        
                        numCorruptedBlocks++;
                    }   
                        break;
                    case (byte) 0x80:    // Read Only Data
                    {
                        //-------------------------------------
                        // Set the Readonly Blocks map
                        //-------------------------------------
                        // Set the header's Block ID bit
                        //-------------------------------------                    
                        baselineDcu5File.header  [(int)(dataId / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));
                        baselineDcu5File.readonly[(int)(dataId / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        //------------------------------------------------------------------------------------
                        // Store the data into the variable baselineDcu5File, we will write the data later
                        //------------------------------------------------------------------------------------
                        edtDataBlock = new tEdtReadbackBlockData();

                        // Store the Block data
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    
                        edtDataBlock.numEntries = dataSize;

                        baselineDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        numReadOnlyBlocks++;                    
                    }   
                        break;
                    case (byte) 0xFF:    // No response
                    {
                        // Set our data block storage location to null since there was no response
                        edtDataBlock = null;                        

                        
                        JOptionPane.showMessageDialog(null,"No response from DCU for Block #" + dataId,"DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 8;
                    }
                    default:
                    {
                        // Set our data block storage location to null the data block was not Normal or Read-Only
                        edtDataBlock = null;                        

                        
                        JOptionPane.showMessageDialog(null,"Response error from DCU.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 9;
                    }   
                } // switch (cmdData.cmdStatus)                

                // We have to create all Engine Data Blocks for Normal Write Data (read/write Blocks)
                // It would be too difficulty to do it after all the Blocks are downloaded as all 
                // blocks are stored in a list.  The blocks are not stored in the list if the Block is
                // empty.  Even if its not empty, it will not be block number 5 in the list as there
                // may be empty block prior to Block5.
                //
                // Create Block 0,1,9 or 205 if the data returned from the connected DCU V is valid, 
                // not-exist or read-only
                //
                if (dataId == 0)
                {                   
                    createEngineDataBlock0(edtDataBlock);
                }               
                else if (dataId == 1)
                {                   
                   createEngineDataBlock1(edtDataBlock);
                }               
/*
                else if (dataId == 5)
                {                   
                    createEngineDataBlock205(edtDataBlock);
                }               
*/
                else if (dataId == 9)
                {                   
                    createEngineDataBlock9(edtDataBlock);
                }                              
                else if (dataId == 205)
                {                   
                    createEngineDataBlock205(edtDataBlock);
                }                              
            } // for (int dataId = 0; dataId<=maxId; dataId++)            
        }
        catch (IndexOutOfBoundsException e)
        {
           JOptionPane.showMessageDialog(null,"Internal error.  Array index out of bounds.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
           System.out.println(e.getMessage());
           return 10;
        }

        
        Utilities.ProgressFrameSetTxt("Saving baseline file: " + baselineFilename, (double) 0.01);
        try
        {
            //-----------------------------------------------------------------------------
            // Fill-in the details for the TAIL BLOCK
            //-----------------------------------------------------------------------------
            baselineDcu5File.version[0] = (byte) 'V';
            baselineDcu5File.version[1] = (byte) '0';
            baselineDcu5File.version[2] = (byte) '0';
            baselineDcu5File.version[3] = (byte) '5';
            baselineDcu5File.version[4] = (byte) 0;

            baselineDcu5File.model[0]   = (byte) 'P';
            baselineDcu5File.model[1]   = (byte) 'W';
            baselineDcu5File.model[2]   = (byte) '1';
            baselineDcu5File.model[3]   = (byte) '2';
            baselineDcu5File.model[4]   = (byte) '7';
            baselineDcu5File.model[5]   = (byte) 'N';
            baselineDcu5File.model[6]   = (byte) 0;
            
            tmpBytes = GlobalVars.PART_NUMBER.getBytes();            
            for (int i=0; i<GlobalVars.PART_NUMBER.length();i++)
            {
                baselineDcu5File.dcu_part[i] = tmpBytes[i];
            }
            
            // Set this to 4 for now as we only have a max 7692 blocks  {ASCII character String} 
            // That fits into a 256 x 8 x 4 = 8192 bits;
            numHeaderMapBlks =  (int) Math.ceil((double)(((double)(numNormalBlocks+numReadOnlyMapBlks+numCorruptedMapBlks)) / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.HEADER_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numHeaderMapBlks);
            Arrays.fill(baselineDcu5File.header_blks, (byte) 0);
            if ((numHeaderMapBlks+numReadOnlyMapBlks+numCorruptedMapBlks) > 9)
            {
                baselineDcu5File.header_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.header_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                baselineDcu5File.header_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.header_blks[1] = (byte) 0x0; // Null character
            }
            baselineDcu5File.header_blks[2] = (byte) 0; // null termination

            
            // Store the value of the number of Read-Only blocks {ASCII character String} 
            numReadOnlyMapBlks =  (int) Math.ceil((double)(((double)numReadOnlyBlocks) / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.READONLY_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numReadOnlyMapBlks);
            if (numReadOnlyMapBlks > 9)
            {
                baselineDcu5File.readonly_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.readonly_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                baselineDcu5File.readonly_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.readonly_blks[1] = (byte) 0x0; // Null character
            }
            baselineDcu5File.readonly_blks[2] = (byte) 0; // null termination

            
            // Store the value of the number of Corrupted blocks {ASCII character String} 
            numCorruptedMapBlks =  (int) Math.ceil((double)(((double)numCorruptedBlocks) / (baselineDcu5File.EIGHT_BITS_PER_BYTE*baselineDcu5File.CORRUPTED_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numCorruptedMapBlks);
            if (numCorruptedMapBlks > 9)
            {
                baselineDcu5File.corrupted_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.corrupted_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                baselineDcu5File.corrupted_blks[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.corrupted_blks[1] = (byte) 0x0; // Null character
            }
            baselineDcu5File.corrupted_blks[2] = (byte) 0; // null termination

            
            // Store the Block Size (Default is 128, max is 1024 for DCU V) {ASCII character String} 
            tempBlksStr = Integer.toString(dataSize);
            if (dataSize > 999)
            {
                baselineDcu5File.blkSize[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.blkSize[1] = (byte) tempBlksStr.getBytes()[1];
                baselineDcu5File.blkSize[2] = (byte) tempBlksStr.getBytes()[2];
                baselineDcu5File.blkSize[3] = (byte) tempBlksStr.getBytes()[3];
            }
            else
            {
                baselineDcu5File.blkSize[0] = (byte) tempBlksStr.getBytes()[0];
                baselineDcu5File.blkSize[1] = (byte) tempBlksStr.getBytes()[1];
                baselineDcu5File.blkSize[2] = (byte) tempBlksStr.getBytes()[2];        
                baselineDcu5File.blkSize[3] = (byte) 0x0; // Null character
            }
            baselineDcu5File.blkSize[4] = (byte) 0; // null termination
            
            // Store the index 0 stating that the data is from the DCU(0) {1 is for EPROM)
            baselineDcu5File.eprom[0] = 0;
            baselineDcu5File.eprom[1] = 0; // null termination
            //-----------------------------------------------------------------------------

            if ((numNormalBlocks > 0) || (numReadOnlyBlocks > 0) || (numCorruptedBlocks > 0))
            {
                // Save the header blocks [256 bytes per blk] which contains the bit-map of the data blocks
                outFile.write(baselineDcu5File.header[0],0,baselineDcu5File.HEADER_FIELD_SIZE * numHeaderMapBlks);
            }
            
            // Save the read-back engine data blocks
            for (tEdtReadbackBlockData edtBlock: baselineDcu5File.edtReadbackBlocks)
            {
                outFile.write(edtBlock.block_data,0,dataSize);
            }

            if (numReadOnlyBlocks > 0)
            {
                // Save the readonly blocks [256 bytes per blk] which contains the bit-map of the data blocks
                outFile.write(baselineDcu5File.readonly[0],0,baselineDcu5File.READONLY_FIELD_SIZE * numReadOnlyMapBlks);
            }
            
            if (numCorruptedBlocks > 0)
            {
                // Save the corrupted blocks [256 bytes per blk] which contains the bit-map of the data blocks
                outFile.write(baselineDcu5File.corrupted[0],0,baselineDcu5File.CORRUPTED_FIELD_SIZE * numCorruptedMapBlks);
            }            
            
            
            outFile.flush();
            
            //-----------------------------------------------------------------------------
            // Write the TAIL BLOCK to the DCU5 baseline file
            //-----------------------------------------------------------------------------
            outFile.write(baselineDcu5File.version       ,0,baselineDcu5File.VERSION_FIELD_SIZE);            //   5     0x3A00
            outFile.write(baselineDcu5File.model         ,0,baselineDcu5File.MODEL_FIELD_SIZE);              //  41     0x3A05
            outFile.write(baselineDcu5File.dcu_part      ,0,baselineDcu5File.DCU_PART_FIELD_SIZE);           //  41     0x3A2E
            outFile.write(baselineDcu5File.aircraft_sn   ,0,baselineDcu5File.AIRCRAFT_SN_FIELD_SIZE);        //  41     0x3A57
            outFile.write(baselineDcu5File.engine_pos    ,0,baselineDcu5File.ENGINE_POS_FIELD_SIZE);         //   3     0x3A80
            outFile.write(baselineDcu5File.engine_sn     ,0,baselineDcu5File.ENGINE_SN_FIELD_SIZE);          //  21     0x3A83
            outFile.write(baselineDcu5File.reserved      ,0,baselineDcu5File.RESERVED_FIELD_SIZE);           //   3     0x3A98
            outFile.write(baselineDcu5File.header_blks   ,0,baselineDcu5File.HEADER_BLK_CNT_FIELD_SIZE);     //   3     0x3A9B
            outFile.write(baselineDcu5File.corrupted_blks,0,baselineDcu5File.CORRUPTED_BLK_CNT_FIELD_SIZE);  //   3     0x3A9E            
            outFile.write(baselineDcu5File.byte_fill     ,0,baselineDcu5File.BYTE_FILL_FIELD_SIZE);          //   5     0x3AA1
            outFile.write(baselineDcu5File.readonly_blks ,0,baselineDcu5File.READONLY_BLK_CNT_FIELD_SIZE);   //   3     0x3AA6
            outFile.write(baselineDcu5File.blkSize       ,0,baselineDcu5File.BLKSIZE_FIELD_SIZE);            //   5     0x3AA9
            outFile.write(baselineDcu5File.eprom         ,0,baselineDcu5File.EEPROM_FIELD_SIZE);             //   2     0x3AAE
            outFile.write(baselineDcu5File.filler        ,0,baselineDcu5File.FILLER_FIELD_SIZE);             //  80     0x3AB0
            
            //--------------------------------------
            // Close the file
            //--------------------------------------		   
            outFile.flush();            
            outFile.close();            
        }
        catch(IOException e)
        {
           System.out.println(e.getMessage());
           
           return 11;            
        }
        
        
        // Command the DCU II to 9,600 baud
        Utilities.ProgressFrameSetTxt("Commanding DCU II device to 9,600 baud...", (double) 0.01);
        cmdData.clear();
        GlobalVars.dcuCommand.receiveBaudRate((byte) 1);
        cmdData.clear();
        GlobalVars.dcuCommand.invokeBaudRate();

        //------------------------------------------------------------------------------------
        // Set the Utility Program to 9,600 baud
        //------------------------------------------------------------------------------------
        Utilities.ProgressFrameSetTxt("Setting Utility Program to 9,600 baud", (double) 0.01);
        Utilities.setConnectionBaudrate(9600);        

        // Set display to show that we are back at 9,600 baud rate
        GlobalVars.progressFrame.jLabel16.setText("9600");
        
        return 0;
    }

    public int createDcu5FinalStateFile(){
        int returnVal = JOptionPane.CANCEL_OPTION;
        byte[] tmpReceivedBuffer = new byte[Constants.BUF_SIZE];        
        byte dataToWrite[];
        FileOutputStream outFile = null;
        int dataSize = 0;
        int maxId = 0;
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
        TsysConfig tempSysConfig = new TsysConfig();
        TcmdData cmdData;
        String dcuAddressStr = new String();
        String baudRateStr = new String();
        String dcuTypeStr = new String();
        String blockSizeStr = new String();
        String testEnableStr = new String();
        String transmitModeStr = new String();
        String spareStr = new String();
        File finalStateFile;                
        tDcu5file finalStateDcu5File = new tDcu5file();
        tEdtReadbackBlockData edtDataBlock;
        int numNormalBlocks = 0;        
        int numReadOnlyBlocks = 0;
        int numCorruptedBlocks = 0;
        String finalStateFilename = new String("SN_");
        byte[] tmpBytes = new byte[42];
        int numHeaderMapBlks = 0;
        int numReadOnlyMapBlks = 0;
        int numCorruptedMapBlks = 0;
        String tempBlksStr;
        
        //--------------------------------------
        // Send "Read DCU ID" command
        //--------------------------------------
        cmdData = new TcmdData();
        GlobalVars.dcuCommand.readDcuID(cmdData,false);

        //--------------------------------------
        // Open File
        //--------------------------------------		           
        finalStateFilename += GlobalVars.SERIAL_NUMBER;
        finalStateFilename += "PN_";
        finalStateFilename += GlobalVars.PART_NUMBER;
        finalStateFilename += "_";
        finalStateFilename += GlobalVars.sysConfig.getBaudRate();
        finalStateFilename += "_";
        switch(GlobalVars.sysConfig.getCmdSet())
        {
            case (int) Constants.DCUII:
                finalStateFilename += "1";
                break;
            case (int) Constants.DCUIV:
                finalStateFilename += "2";
                break;
            case (int) Constants.DCUV:
                finalStateFilename += "3";
                break;
            default:
                finalStateFilename += "3";
                break;
        }
        finalStateFilename += ".DCU5";
        
        try
        {
            finalStateFile = new File(GlobalVars.gDataStorageDirectory + "//" + finalStateFilename);
            
            if (finalStateFile.exists() == true)            
            {
                // Since the final-state file is found,
                // inform that the connected DCU V unit has already been programmed
                // Then inform the operator that the Utility program will be terminating
                JOptionPane.showMessageDialog(null,"Final-state file found at specified folder.\nThe DCU V device has been programmed previously.\nThe Utility Program will shutdown now.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                Utilities.shutdownSystem();
            }
            else
            {
                // Create the Final-State file
                finalStateFile.createNewFile();                    
                outFile = new FileOutputStream(finalStateFile);
            }                        
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            JOptionPane.showMessageDialog(null,"Unable to create DCU5 final-state file.  Please ensure that you have read and write access to the specified directory.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
            return 6;
        }
        
        //---------------------------
        // Send Maintenance Command
        //---------------------------
        cmdData.clear();
        GlobalVars.maintCommand.maintCommand(cmdData);        

        // Need some delay after entering Maintenance mode. Need to wait for DCU device to settle down
        // Sleep for 0.2 second.
        try{
            TimeUnit.MILLISECONDS.sleep(200);  // delay
        }
        catch(InterruptedException ex){}   
        
                
        //--------------------------------------
        // Send Display Configuration command
        //--------------------------------------
        cmdData.clear();
        GlobalVars.maintCommand.displayConfigure(cmdData);

        //tmpReceivedBuffer = cmdData.getReceive();                
        tmpReceivedBuffer = java.util.Arrays.copyOf(cmdData.receiveBuf, Constants.BUF_SIZE);


        // DCU Address
        offset = 3;
        tempSysConfig.setDcuAddress((int) tmpReceivedBuffer[offset]&0xFF);

        // baud rate
        offset = 4;
        tempSysConfig.setBaudRate((int) tmpReceivedBuffer[offset]&0xFF);

        // Transmit Mode
        offset = 5;
        tempSysConfig.setTransmitMode((int) tmpReceivedBuffer[offset]&0xFF);

        // DCU Type
        offset = 6;
        tempSysConfig.setCmdSet((int) tmpReceivedBuffer[offset]&0xFF);

        // Block size
        offset = 7;
        tempSysConfig.setBlockSize((int) tmpReceivedBuffer[offset]&0xFF);

        // Test Enable
        offset = 8;
        tempSysConfig.setTestEnable((int) tmpReceivedBuffer[offset]&0xFF);

        // Spare
        offset = 9;
        tempSysConfig.setSpare((int) tmpReceivedBuffer[offset]&0xFF);

        
        //----------------------------------------------------
        // Send Reboot command to get back to Normal Mode
        //----------------------------------------------------	
        cmdData.clear();
        returnVal = GlobalVars.maintCommand.reboot(cmdData);
        cmdStatusWord = cmdData.receiveBuf[2];
        cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
        if ((cmdStatusWord != 0) || (returnVal != 0))
        {
            JOptionPane.showMessageDialog(null,"Unexpected DCU error.\nFail To Reboot DCU.  Upgrade process discontinued.\nThe Utility Program will shutdown now.\nPlease inform your manager or supervisor of the error.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            Utilities.shutdownSystem();
        }
        
        // Command the DCU II to 115,200 baud
        Utilities.ProgressFrameSetTxt("Commanding DCU II device to 115,200 baud...", (double) 0.01);
        cmdData.clear();
        GlobalVars.dcuCommand.receiveBaudRate((byte) 4);
        cmdData.clear();
        GlobalVars.dcuCommand.invokeBaudRate();

        //------------------------------------------------------------------------------------
        // Set the Utility Program to 115,200 baud for faster download of engine data block
        //------------------------------------------------------------------------------------
        Utilities.ProgressFrameSetTxt("Setting Utility Program to 115,200 baud", (double) 0.01);
        Utilities.setConnectionBaudrate(115200);        
        
        // Set display to show that we are at 115,200 baud rate
        GlobalVars.progressFrame.jLabel16.setText("115200");
        
                        
        //----------------------------------------------------
        // Set Data Block ID max. value based on Block Size
        // As per telecon at 3:30pm on Wed., 13 May 2014
        // the max. Block ID will be set to 300
        //----------------------------------------------------        
        //Figure out Maximum data ID use origenal configuration
        switch (GlobalVars.sysConfig.getCmdSet())
        {
        case Constants.DCUV: // DCU V
            if (tempSysConfig.getBlockSize() == 4)
            {
                //maxId = 1923;
                maxId = 300;
                dataSize = 1024;
                GlobalVars.progressFrame.jLabel17.setText("1024");
            }
            else if (tempSysConfig.getBlockSize() == 3)
            {
                //maxId =  3846;
                maxId = 300;
                dataSize = 512;
                GlobalVars.progressFrame.jLabel17.setText("512");
            }
            else if (tempSysConfig.getBlockSize() == 2)
            {
                //maxId = 7692;
                maxId = 300;
                dataSize = 256;
                GlobalVars.progressFrame.jLabel17.setText("256");
            }
            else if (tempSysConfig.getBlockSize() == 1)
            {
                //maxId = 7692;
                maxId = 300;
                dataSize = 128;
                GlobalVars.progressFrame.jLabel17.setText("128");
            }
            else
            {
                JOptionPane.showMessageDialog(null,"Error: Configuration Conflict with block size.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
                return 4;
            }
            break;
            
        case Constants.DCUIV: // DCU IV
            //maxId = 7692;
            maxId = 300;
            dataSize = 128;
            GlobalVars.progressFrame.jLabel17.setText("128");
            break;
        case Constants.DCUII: // DCU II
            //maxId = 1982;
            maxId = 300;
            dataSize = 128;
            GlobalVars.progressFrame.jLabel17.setText("128");
            break;
        default:
            JOptionPane.showMessageDialog(null,"Error: Configuration Conflict with CMD set.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            return 5;
        }
        
        
        //--------------------------------------
        // Send "Read DCU ID" command
        //--------------------------------------
        cmdData.clear();
        GlobalVars.dcuCommand.readDcuID(cmdData,false);        

        
        //-----------------------------------------------------------------
        // Download all entire area that stores the Engine Data Table(s)
        //-----------------------------------------------------------------
        try
        {
            // initialize edtDataBlock
            edtDataBlock = null;
            
            //-----------------------------------------------------------
            // Send as many ReadData commands as maxId value set earlier
            // Write all the data into the DCU5 Final-State File
            //-----------------------------------------------------------
            for (int dataId = 0; dataId<=maxId; dataId++)
            {
               Utilities.ProgressFrameSetTxt("Downloading data block # " + dataId + " / " + maxId, (double) 0.15);
               GlobalVars.dcuCommand.readData(dataId, cmdData);
              
               switch (cmdData.cmdStatus)
               {
                    case (byte) 0x00:    // normal data
                    {
                        // Set the header's Block ID bit
                        //
                        //-------------------------------------
                        // Set the header's Block ID bit
                        //-------------------------------------                    
                        finalStateDcu5File.header  [(int)(dataId / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        //------------------------------------------------------------------------------------
                        // Store the data into the variable finalStateDcu5File, we will write the data later
                        //------------------------------------------------------------------------------------
                        edtDataBlock = new tEdtReadbackBlockData();                   
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    

                        finalStateDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        numNormalBlocks++;                        
                    }
                        break;
                    case (byte) 0x10:    // cmd CRC error
                    {
                        JOptionPane.showMessageDialog(null,"CMD CRC error.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 7;
                    }   
                    case (byte) 0x14:    // data not exist
                    {
                        // Do nothing.  No "bit" will be captured in the bit-map within the header as expected
                    }   
                        break;
                    case (byte) 0x30:    // Data Storage and Retrieval (DSR) Integrity error
                    {
                        // Set the corrupted's Block ID bit
                        //
                        finalStateDcu5File.header   [(int)(dataId / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));
                        finalStateDcu5File.corrupted[(int)(dataId / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        //------------------------------------------------------------------------------------
                        // Store the data into the variable baselineDcu5File, we will write the data later
                        //------------------------------------------------------------------------------------
                        edtDataBlock = new tEdtReadbackBlockData();                   
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    
                        edtDataBlock.numEntries = dataSize;

                        finalStateDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        
                        numCorruptedBlocks++;
                    }   
                        break;
                    case (byte) 0x80:    // Read Only Data
                    {
                        //-------------------------------------
                        // Set the Readonly Blocks map
                        //-------------------------------------
                        // Set the header's Block ID bit
                        //-------------------------------------                    
                        finalStateDcu5File.header  [(int)(dataId / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));
                        finalStateDcu5File.readonly[(int)(dataId / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE))][(((int)Math.floor(dataId/8)) % 256) % 2048 ] += (1 << (dataId - (((int)Math.floor(dataId/8)) * 8)));

                        // Store the data into the variable finalStateDcu5File, we will write the data later
                        edtDataBlock = new tEdtReadbackBlockData();

                        // Store the Block data
                        edtDataBlock.block_data = java.util.Arrays.copyOfRange(cmdData.dataBlock,0,0+dataSize);                    

                        finalStateDcu5File.edtReadbackBlocks.add(edtDataBlock);
                        numReadOnlyBlocks++;                    
                    }   
                        break;
                    case (byte) 0xFF:    // No response
                    {
                        JOptionPane.showMessageDialog(null,"No response from DCU.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 8;
                    }   
                    default:
                    {
                        JOptionPane.showMessageDialog(null,"Response error from DCU.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
                        return 9;
                    }   
                }               
            } // for (int dataId = 0; dataId<=maxId; dataId++)           
        }
        catch(IndexOutOfBoundsException e)
        {
           System.out.println(e.getMessage());
           return 10;            
        }

        Utilities.ProgressFrameSetTxt("Saving final-state file: " + finalStateFilename, (double) 0.01);
        try
        {
            //-----------------------------------------------------------------------------
            // Fill-in the details for the TAIL BLOCK
            //-----------------------------------------------------------------------------
            finalStateDcu5File.version[0] = (byte) 'V';
            finalStateDcu5File.version[1] = (byte) '0';
            finalStateDcu5File.version[2] = (byte) '0';
            finalStateDcu5File.version[3] = (byte) '5';
            finalStateDcu5File.version[4] = (byte) 0;

            finalStateDcu5File.model[0]   = (byte) 'P';
            finalStateDcu5File.model[1]   = (byte) 'W';
            finalStateDcu5File.model[2]   = (byte) '1';
            finalStateDcu5File.model[3]   = (byte) '2';
            finalStateDcu5File.model[4]   = (byte) '7';
            finalStateDcu5File.model[5]   = (byte) 'N';
            finalStateDcu5File.model[6]   = (byte) 0;
            
            tmpBytes = GlobalVars.PART_NUMBER.getBytes();            
            for (int i=0; i<GlobalVars.PART_NUMBER.length();i++)
            {
                finalStateDcu5File.dcu_part[i] = tmpBytes[i];
            }
            
            // Set this to 4 for now as we only have a max 7692 blocks
            // That fits into a 256 x 8 x 4 = 8192 bits;
            numHeaderMapBlks =  (int) Math.ceil((double)(((double)(numNormalBlocks+numReadOnlyMapBlks+numCorruptedMapBlks)) / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.HEADER_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numHeaderMapBlks);
            Arrays.fill(finalStateDcu5File.header_blks, (byte) 0);
            if ((numHeaderMapBlks+numReadOnlyMapBlks+numCorruptedMapBlks) > 9)
            {
                finalStateDcu5File.header_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.header_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                finalStateDcu5File.header_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.header_blks[1] = (byte) 0x0; // NULL character
            }
            finalStateDcu5File.header_blks[2] = (byte) 0; // null termination

            // Store the value of the number of Read-Only blocks
            numReadOnlyMapBlks =  (int) Math.ceil((double)(((double)numReadOnlyBlocks) / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.READONLY_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numReadOnlyMapBlks);
            Arrays.fill(finalStateDcu5File.readonly_blks, (byte) 0);
            if (numReadOnlyMapBlks > 9)
            {
                finalStateDcu5File.readonly_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.readonly_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                finalStateDcu5File.readonly_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.readonly_blks[1] = (byte) 0x0; // NULL character
            }
            finalStateDcu5File.readonly_blks[2] = (byte) 0; // null termination

            
            // Store the value of the number of Corrupted blocks
            numCorruptedMapBlks =  (int) Math.ceil((double)(((double)numCorruptedBlocks) / (finalStateDcu5File.EIGHT_BITS_PER_BYTE*finalStateDcu5File.CORRUPTED_FIELD_SIZE)));
            tempBlksStr = Integer.toString(numCorruptedMapBlks);
            Arrays.fill(finalStateDcu5File.corrupted_blks, (byte) 0);
            if (numCorruptedMapBlks > 9)
            {
                finalStateDcu5File.corrupted_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.corrupted_blks[1] = (byte) tempBlksStr.getBytes()[1];
            }
            else
            {
                finalStateDcu5File.corrupted_blks[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.corrupted_blks[1] = (byte) 0x0; // NULL character
            }
            finalStateDcu5File.corrupted_blks[2] = (byte) 0; // null termination

            
            // Store the Block Size (Default is 128, max is 1024 for DCU V) {ASCII character String} 
            tempBlksStr = Integer.toString(dataSize);
            if (dataSize > 999)
            {
                finalStateDcu5File.blkSize[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.blkSize[1] = (byte) tempBlksStr.getBytes()[1];
                finalStateDcu5File.blkSize[2] = (byte) tempBlksStr.getBytes()[2];
                finalStateDcu5File.blkSize[3] = (byte) tempBlksStr.getBytes()[3];
            }
            else
            {
                finalStateDcu5File.blkSize[0] = (byte) tempBlksStr.getBytes()[0];
                finalStateDcu5File.blkSize[1] = (byte) tempBlksStr.getBytes()[1];
                finalStateDcu5File.blkSize[2] = (byte) tempBlksStr.getBytes()[2];        
                finalStateDcu5File.blkSize[3] = (byte) 0x0; // NULL character
            }
            baselineDcu5File.blkSize[4] = (byte) 0; // null termination
            
            // Store the index 0 stating that the data is from the DCU(0) {1 is for EPROM)} 
            finalStateDcu5File.eprom[0] = 0;
            finalStateDcu5File.eprom[1] = 0; // null termination
            //-----------------------------------------------------------------------------

            
            // Save the header blocks [256 bytes per blk] which contains the bit-map of the data blocks
            if ((numNormalBlocks > 0) || (numReadOnlyBlocks > 0) || (numCorruptedBlocks > 0))
            {
                outFile.write(finalStateDcu5File.header[0],0,256 * numHeaderMapBlks);
            }
            
            // Save the read-back engine data blocks
            for (tEdtReadbackBlockData edtBlock: finalStateDcu5File.edtReadbackBlocks)
            {
                outFile.write(edtBlock.block_data,0,dataSize);
            }

            if (numReadOnlyBlocks > 0)
            {
                // Save the readonly blocks [256 bytes per blk] which contains the bit-map of the data blocks
                outFile.write(finalStateDcu5File.readonly[0],0,256 * numReadOnlyMapBlks);
            }
            
            if (numCorruptedBlocks > 0)
            {
                // Save the corrupted blocks [256 bytes per blk] which contains the bit-map of the data blocks
                outFile.write(finalStateDcu5File.corrupted[0],0,256 * numCorruptedMapBlks);
            }

            outFile.flush();
            
            //-----------------------------------------------------------------------------
            // Write the TAIL BLOCK to the DCU5 final-state file
            //-----------------------------------------------------------------------------
            outFile.write(finalStateDcu5File.version       ,0,finalStateDcu5File.VERSION_FIELD_SIZE);               //  5   0x3A00
            outFile.write(finalStateDcu5File.model         ,0,finalStateDcu5File.MODEL_FIELD_SIZE);                 // 41   0x3A05
            outFile.write(finalStateDcu5File.dcu_part      ,0,finalStateDcu5File.DCU_PART_FIELD_SIZE);              // 41   0x3A2E
            outFile.write(finalStateDcu5File.aircraft_sn   ,0,finalStateDcu5File.AIRCRAFT_SN_FIELD_SIZE);           // 41   0x3A57
            outFile.write(finalStateDcu5File.engine_pos    ,0,finalStateDcu5File.ENGINE_POS_FIELD_SIZE);            //  3   0x3A80
            outFile.write(finalStateDcu5File.engine_sn     ,0,finalStateDcu5File.ENGINE_SN_FIELD_SIZE);             // 21   0x3A83
            outFile.write(finalStateDcu5File.reserved      ,0,finalStateDcu5File.RESERVED_FIELD_SIZE);              //  3   0x3A98
            outFile.write(finalStateDcu5File.header_blks   ,0,finalStateDcu5File.HEADER_BLK_CNT_FIELD_SIZE);        //  3   0x3A9B
            outFile.write(finalStateDcu5File.corrupted_blks,0,finalStateDcu5File.CORRUPTED_BLK_CNT_FIELD_SIZE);     //  3   0x3A9E
            outFile.write(finalStateDcu5File.byte_fill     ,0,finalStateDcu5File.BYTE_FILL_FIELD_SIZE);             //  5   0x3AA1
            outFile.write(finalStateDcu5File.readonly_blks ,0,finalStateDcu5File.READONLY_BLK_CNT_FIELD_SIZE);      //  3   0x3AA6
            outFile.write(finalStateDcu5File.blkSize       ,0,finalStateDcu5File.BLKSIZE_FIELD_SIZE);               //  5   0x3AA9
            outFile.write(finalStateDcu5File.eprom         ,0,finalStateDcu5File.EEPROM_FIELD_SIZE);                //  2   0x3AAE
            outFile.write(finalStateDcu5File.filler        ,0,finalStateDcu5File.FILLER_FIELD_SIZE);                // 80   0x3A80
            
            //--------------------------------------
            // Close the file
            //--------------------------------------		   
            outFile.flush();            
            outFile.close();            
        }
        catch(IOException e)
        {
           System.out.println(e.getMessage());
           JOptionPane.showMessageDialog(null,"Error writing DCU5 final-state file.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
           JOptionPane.showMessageDialog(null,"Please verify that the USB stick is plugged into the laptop and you have write access to your specified folder.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.ERROR_MESSAGE);
           return 11;            
        }

        
        
        // Command the DCU II to 9,600 baud
        Utilities.ProgressFrameSetTxt("Commanding DCU II device to 9,600 baud...", (double) 0.01);
        cmdData.clear();
        GlobalVars.dcuCommand.receiveBaudRate((byte) 1);
        cmdData.clear();
        GlobalVars.dcuCommand.invokeBaudRate();

        //------------------------------------------------------------------------------------
        // Set the Utility Program to 9,600 baud
        //------------------------------------------------------------------------------------
        Utilities.ProgressFrameSetTxt("Setting Utility Program to 9,600 baud", (double) 0.01);
        Utilities.setConnectionBaudrate(9600);        

        // Set display to show that we are back at 9,600 baud rate
        GlobalVars.progressFrame.jLabel16.setText("9600");
        
        return 0;
    }
    

    
    public int dataVerification()
    {
        int returnValue1 = JOptionPane.CANCEL_OPTION;
        int returnValue2 = 0;
        tEngineDataBlock engineDataBlock;
        ///String tempString1 = new String();
        ///String tempString2 = new String();
                
        for(int j=0;j<engineDataBlockList.size() && (returnValue2 == 0);j++)
        {            
            engineDataBlock = engineDataBlockList.get(j);

            Utilities.ProgressFrameSetTxt("Verifying EDT blocks: " + (j+1) + "/"  + engineDataBlockList.size() + " (Block ID : " + engineDataBlock.blockId + ")", (double) 0.01);
            
            for (int i=0; i < engineDataBlock.dataIndex; i++)
            {
                if (engineDataBlock.readBackData[i] != engineDataBlock.data[i])
                {
                    //id = engineDataBlock.blockId;
                    returnValue2 = 1;   // 1 means error condition

                   returnValue1 = JOptionPane.showConfirmDialog(null,"Error verifying Block ID # " + engineDataBlock.blockId + ".  Expected value from DCU device is data[" + i + "] = 0x" + String.format("%04x",engineDataBlock.data[i]) + ".\nActual read-back from DCU device is data[" + i + "] = 0x" + String.format("%04x",engineDataBlock.readBackData[i]) + ".\nClick on YES to stop the Upgrade process and shutdown the Utility Program.\nClick on NO to continue with the Upgrade process.","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);                   
                    if (returnValue1 == JOptionPane.YES_OPTION)
                    {
                        //System.exit(0);
                        Utilities.shutdownSystem();
                    }
                    
                    break;
                }
            }
        }

        return returnValue2;
    }

    //public void outputToForm(TFormTDS[] pFormTDS, Boolean readOnlyDataLoad);
    public void outputToForm(boolean readOnlyDataLoad){
        String indentOutput = new String();
        int dataLength;
        
        /* Output format:
        0----------1--------2---------3-------- 4---------5---------6
        0123456789012345678901234567890123456789012345678901234567890
        8.3.11 P&WC Engin Data Table Upload

        Data for block # 0001 :
            FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF
            FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF
            FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF
            FFFF  FFFF  FFFF  FFFF  FFFF  FFFF  FFFF

        Data for block # 0002 :
            FFFF  FFFF  ......
        */

        Utilities.TdsFrameAppendTxt("  8.3.11 P&WC Engine Data Table Upload\n\n");

        for (tEngineDataBlock engineDataBlock : engineDataBlockList) 
        {    
            // For each data block
            // Put a space character in indentOutput for each block to clear it virtually
            indentOutput = " ";
            Utilities.TdsFrameAppendTxt("    Data for block # " + engineDataBlock.blockId +  " :");
            
            for (int i = 0; i < engineDataBlock.dataIndex; i++ )
            {
                // new line, indent, and puch back to output
                if ((i % 8) == 0)           // 8 columns
                {
                    indentOutput += "\n";
                    indentOutput += "    ";
                }

                // push back to output
                indentOutput += "  " + Utilities.intToASC(engineDataBlock.readBackData[i]);
            }
                        
            // output to form
            Utilities.TdsFrameAppendTxt(indentOutput + "\n");   
      
            // find data length
            if (engineDataBlock.CsCrcOutsideFlag == true)
            {
                dataLength = engineDataBlock.dataIndex;
            }                  
            else
            {
                if (engineDataBlock.CRCFlag == true)
                {
                    // CRC
                    dataLength = engineDataBlock.dataIndex - 2;
                }
                else
                {
                    // Checksum
                    dataLength = engineDataBlock.dataIndex - 1;
                }
            }
            
            
            // output Checksum
            if ( engineDataBlock.CRCFlag == false )
            {
                //Checksum calculation
                //sprintf(tembuff, "          Block#%04d Checksum final:%04X  __Fail __Pass\r\n",
                // engineDataBlock_ptr->blockId,
                // (engineDataBlock_ptr->calculateBlockChecksum(engineDataBlock_ptr->readBackData, dataLength) + 1) & 0x0000FFFFL);
                
                indentOutput = "          Block#";
                indentOutput += Integer.toString(engineDataBlock.blockId);
                indentOutput += " Checksum final:";
                indentOutput += Utilities.intToASC((int)((engineDataBlock.calculateBlockChecksum(engineDataBlock.readBackData, dataLength)+1) & 0x0000FFFFL));
                Utilities.TdsFrameAppendTxt(indentOutput + "  __Fail __Pass\n");                   
            }
            //Output CRC
            else
            {
                //CRC calculation, do not include last 4 bytes of CRC at the end of the data block
                indentOutput = "          Block#";
                indentOutput += Integer.toString(engineDataBlock.blockId);
                indentOutput += " CRC final:";
                indentOutput += Utilities.intToASC((int)(engineDataBlock.calculateBlockCRC(engineDataBlock.readBackData, dataLength)));
                Utilities.TdsFrameAppendTxt(indentOutput + "  __Fail __Pass\n");                   
            }                        

            Utilities.TdsFrameAppendTxt("\n");
        } // for (tEngineDataBlock engineDataBlock : engineDataBlockList) 
    }
    
    public void createEngineDataBlock0(tEdtReadbackBlockData edtReadBackBlock0)
    {
        int returnVal = JOptionPane.CANCEL_OPTION;
        
        // Check if edtReadBackBlock0 is null.  If yes, create a new EDT block
        if (edtReadBackBlock0 == null)
        {
            // Notify the operator that Engine Data Block 0 was empty
            returnVal = JOptionPane.showConfirmDialog(null,"Engine Data Block 0 on connected DCU V device is empty.\nThe connected DCU V device is not fault-free !!!\nClick on YES to stop the Upgrade process and shutdown the Utility Program.\nClick on NO to continue with the Upgrade process.","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                Utilities.shutdownSystem();
            }
            else
            {
                edtReadBackBlock0 = new tEdtReadbackBlockData();
            }
        }

        // Create a new EngineDataBlock #0 based on Block0
        engineDataBlock0 = new tEngineDataBlock();
        engineDataBlock0.blockId = 0;
        engineDataBlock0.dataIndex = edtReadBackBlock0.numEntries / NUM_BYTES_PER_DATA;
        
        
        for (int offset=0; offset<engineDataBlock0.dataIndex; offset++)
        {
            // Parse through the data & convert byte[] to int value
            engineDataBlock0.data[offset] = (int) ((edtReadBackBlock0.block_data[(offset*2)] << 8) & 0xFFFF);
            engineDataBlock0.data[offset] = (int) (engineDataBlock0.data[offset]+ ((edtReadBackBlock0.block_data[(offset*2)+1]) & 0xFF));
        }
        engineDataBlockList.add(engineDataBlock0);
    }

    public void createEngineDataBlock1(tEdtReadbackBlockData edtReadBackBlock1)
    {
        int returnVal = JOptionPane.CANCEL_OPTION;
        
        // Check if edtReadBackBlock1 is null.  If yes, create a new EDT block
        if (edtReadBackBlock1 == null)
        {
            // Notify the operator that Engine Data Block 1 was empty
            returnVal = JOptionPane.showConfirmDialog(null,"Engine Data Block 1 on connected DCU V device is empty.\nThe connected DCU V device is not fault-free !!!\nClick on YES to stop the Upgrade process and shutdown the Utility Program.\nClick on NO to continue with the Upgrade process.","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                Utilities.shutdownSystem();
            }
            else
            {
                edtReadBackBlock1 = new tEdtReadbackBlockData();
            }
        }

        // Create a new EngineDataBlock #1 based on Block1
        engineDataBlock1 = new tEngineDataBlock();
        engineDataBlock1.blockId = 1;
        engineDataBlock1.dataIndex = edtReadBackBlock1.numEntries / NUM_BYTES_PER_DATA;
        
        
        for (int offset=0; offset<engineDataBlock1.dataIndex; offset++)
        {
            // Parse through the data & convert byte[] to int value
            engineDataBlock1.data[offset] = (int) ((edtReadBackBlock1.block_data[(offset*2)] << 8) & 0xFFFF);
            engineDataBlock1.data[offset] = (int) (engineDataBlock1.data[offset]+ ((edtReadBackBlock1.block_data[(offset*2)+1]) & 0xFF));
        }
        engineDataBlockList.add(engineDataBlock1);
    }

    public void createEngineDataBlock9(tEdtReadbackBlockData edtReadBackBlock9)
    {
        int returnVal = JOptionPane.CANCEL_OPTION;
        
        // Check if edtReadBackBlock9 is null.  If yes, create a new EDT block
        if (edtReadBackBlock9 == null)
        {
            // Notify the operator that Engine Data Block 9 was empty
            returnVal = JOptionPane.showConfirmDialog(null,"Engine Data Block 9 on connected DCU V device is empty.\nThe connected DCU V device is not fault-free !!!\nClick on YES to stop the Upgrade process and shutdown the Utility Program.\nClick on NO to continue with the Upgrade process.","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                Utilities.shutdownSystem();
            }
            else
            {
                edtReadBackBlock9 = new tEdtReadbackBlockData();
            }
        }

        // Create a new EngineDataBlock #9 based on Block9
        engineDataBlock9 = new tEngineDataBlock();
        engineDataBlock9.blockId = 9;
        engineDataBlock9.dataIndex = edtReadBackBlock9.numEntries / NUM_BYTES_PER_DATA;        
        
        for (int offset=0; offset<engineDataBlock9.dataIndex; offset++)
        {
            // Parse through the data & convert byte[] to int value
//            engineDataBlock9.data[offset] = (int) ((edtReadBackBlock9.block_data[(offset*2)] << 8) & 0xFFFF);
//            engineDataBlock9.data[offset] = (int) (engineDataBlock9.data[offset]+ ((edtReadBackBlock9.block_data[(offset*2)+1]) & 0xFF));
            engineDataBlock9.data[offset] = (int) 0;
            engineDataBlock9.data[offset] = (int) 0;
        }
        engineDataBlockList.add(engineDataBlock9);
    }
    
    public void createEngineDataBlock205(tEdtReadbackBlockData edtReadBackBlock205)
    {        
        int returnVal = JOptionPane.CANCEL_OPTION;
        
        // Check if edtReadBackBlock205 is null.
        // This should not be the case.
        // Shutdown the program if it is null
        if (edtReadBackBlock205 == null)
        {
            // Notify the operator that Engine Data Block 5 was empty
            returnVal = JOptionPane.showConfirmDialog(null,"Engine Data Block 5 on connected DCU V device is empty.\nThe connected DCU V device is not fault-free !!!\nClick on YES to stop the Upgrade process and shutdown the Utility Program.\nClick on NO to continue with the Upgrade process.","DCU V GSE" + Constants.SW_VERSION,JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                Utilities.shutdownSystem();
            }
            else
            {
                edtReadBackBlock205 = new tEdtReadbackBlockData();
            }
        }

        // Create a new EngineDataBlock #205 based on Block5
        //edtBlock205 = baselineDcu5File.edtReadbackBlocks.get(205);
        engineDataBlock205 = new tEngineDataBlock();
        engineDataBlock205.blockId = 205;
        engineDataBlock205.dataIndex = edtReadBackBlock205.numEntries / NUM_BYTES_PER_DATA;
        
        
        for (int offset=0; offset<engineDataBlock205.dataIndex; offset++)
        {
            // Just copy the data directly from Block5 to Block205
            engineDataBlock205.data[offset] = (int) ((edtReadBackBlock205.block_data[(offset*2)] << 8) & 0xFFFF);
            engineDataBlock205.data[offset] = (int) (engineDataBlock205.data[offset] + ((edtReadBackBlock205.block_data[(offset*2)+1]) & 0xFF));                
        }
        engineDataBlockList.add(engineDataBlock205);        
    }    
}






