/*
 * TMaintCommand.java
 *
 * Created on February 3, 2014, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.lang.*;
import java.util.concurrent.TimeUnit;

import javax.swing.text.BadLocationException;

/**
 *
 * @author FKY301079
 */
public class TMaintCommand {
    
    /** Creates a new instance of TMaintCommand */
    public void TMaintCommand() {
    }
    
    public void TMaintCommand(TcmdData cmdData) {
    }
    
    public String  parserMaintCmdStatus(int pStatus){
        String tmpString;

        tmpString = "--Maint. CMD ";
        if (pStatus == 0)
        {
            tmpString = tmpString + "OK\r\n";
        }
        /* D0 */
        if ((pStatus & Constants.INVALID_CMD_CODE) != 0)
        {
            tmpString = tmpString + "ERR, Invalid CMD Code.\r\n";
        }
        /* D1 */
        if ((pStatus & Constants.UART_COMM_ERROR) != 0)
        {
            tmpString = tmpString + "ERR, UART Comm. Error.\r\n";
        }
        /* D2 */
        if ((pStatus & Constants.ILLEGAL_DATA_ID) != 0)
        {
            tmpString = tmpString + "ERR, Illegal Data ID.\r\n";
        }
        /* D3 */
        if ((pStatus & Constants.DCU_SYSTEM_FAILURE) != 0)
        {
            tmpString = tmpString + "ERR, DCU System Failure.\r\n";
        }
        /* D4 */
        if ((pStatus & Constants.FAULT_LAST_CMD) != 0)
        {
            tmpString = tmpString + "ERR, Fault During Last CMD.\r\n";
        }
        /* D5 */
        if ((pStatus & Constants.TRANSMISSION_CRC_FAILURE) != 0)
        {
            tmpString = tmpString + "ERR, Transmission CRC FAILURE.\r\n";
        }
        /* D7 */
        if ((pStatus & Constants.OVERWRITE_READ_ONLY) != 0)
        {
            tmpString = tmpString + "ERR, Overwrite Read Only Block.\r\n";
        }
        /* D8 */
        if ((pStatus & Constants.COMFIG_DATA_OUT_RANGE) != 0)
        {
            tmpString = tmpString + "ERR, Config. Data Out Of Range.\r\n";
        }
        /* D9 */
        if ((pStatus & Constants.CONFIG_DATA_CONFLICT) != 0)
        {
            tmpString = tmpString + "ERR, Config. Data Conflict.\r\n";
        }
        /* D11 */
        if ((pStatus & Constants.TEST_READ_BLOCK_NOT_EXIST) != 0)
        {
            tmpString = tmpString + "ERR, Test Read Block Not Exist.\r\n";
        }

        return tmpString;
    }

    //----- command list ------------
    public int maintCommand(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_INIT_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            

        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n"); 

        // Sleep for 0.1 second.  The Init Maintenance command is slow
        try{
            TimeUnit.MILLISECONDS.sleep(100);  // delay
        }
        catch(InterruptedException ex){}    
        
        
        //-------------------- comm receive block ----------------------
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 7);

        //charToASC(cmdData.receiveBuf, cmdData.strBuf, (unsigned short)dwBytesRead);
        if(dwBytesRead != 0) // get some thing from serial port
        {
            String dispStr;

            crc = (cmdData.receiveBuf[3]<<24) +
                    (cmdData.receiveBuf[4]<<16) +
                    (cmdData.receiveBuf[5]<<8) +
                    (cmdData.receiveBuf[6] & 0xFF);           
            
            if (crc == calculateMaintCRC(cmdData.receiveBuf, (short) 3, 0xFFFFFFFF))
            {
                cmdData.cmdResult = true;
            }

            //dispStr = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            ///GlobalVars.commDataFrame.jTextArea1.append("Received: " + cmdData.stringBuf + "\n");
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");
            
            cmdStatusWord = cmdData.receiveBuf[2];
            cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
            statusMsg = parserMaintCmdStatus(cmdStatusWord);
            ///GlobalVars.commDataFrame.jTextArea1.append(statusMsg);
            Utilities.CommDataFrameAppendTxt(statusMsg);                        
            
            return 0;
        }
        else
        {
            //JOptionPane.showMessageDialog(null,"ERROR: DCU no Response! Please make sure the DCU is connected and power on.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            return 1;
        }        
    }
    
    public void configure(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_CONFIG_CMD;

        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getBaudRate();
        cmdData.cmdBuf[offset++] = (byte) 0;          // communication mode, leave as 0 for now
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getCmdSet();
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getBlockSize();
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getTestEnable();
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getSpare();
        
        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
        
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        // Sleep for 1 second.  The Configure command is very slow to get a response
        try{
            TimeUnit.MILLISECONDS.sleep(1000);  // delay
        }
        catch(InterruptedException ex){}    
        
        //--------------------------------------------------------------
        //-------------------- comm receive block ----------------------
        //--------------------------------------------------------------
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 7);        
        if(dwBytesRead != 0) // get some thing from serial port
        {
            String dispStr;

            crc = (cmdData.receiveBuf[3]<<24) +
                    (cmdData.receiveBuf[4]<<16) +
                    (cmdData.receiveBuf[5]<<8) +
                    (cmdData.receiveBuf[6] & 0xFF);           
            
            if (crc == calculateMaintCRC(cmdData.receiveBuf, (short) 3, 0xFFFFFFFF))
            {
                cmdData.cmdResult = true;
            }

            //dispStr = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");
            
            cmdStatusWord = cmdData.receiveBuf[2];
            cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
            statusMsg = parserMaintCmdStatus(cmdStatusWord);
            Utilities.CommDataFrameAppendTxt(statusMsg);                        
        }
        else
        {
            JOptionPane.showMessageDialog(null,"No Response","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
        }                            
    }

    public int reqVersion(TcmdData cmdData, Boolean msgFlag){
       return 1;
    }

    public void displayConfigure(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
        
        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_DIS_CONFIG_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            

        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        //wait for 0.1 second as the Display DCU Configuration command is slow to respond
        try{
            TimeUnit.MILLISECONDS.sleep(100);  // 1000ms delay
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }
        
        //--------------------------------------------------------------
        //-------------------- comm receive block ----------------------
        //--------------------------------------------------------------
        //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 20);

        //charToASC(cmdData.receiveBuf, cmdData.strBuf, (unsigned short)dwBytesRead);
        if(dwBytesRead != 0) // get some thing from serial port
        {
            String dispStr;

            crc = (cmdData.receiveBuf[3]<<24) +
                    (cmdData.receiveBuf[4]<<16) +
                    (cmdData.receiveBuf[5]<<8) +
                    (cmdData.receiveBuf[6] & 0xFF);            
            
            if (crc == calculateMaintCRC(cmdData.receiveBuf, (short) 3, 0xFFFFFFFF))
            {
                cmdData.cmdResult = true;
            }

            //dispStr = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");
            
            cmdStatusWord = cmdData.receiveBuf[2];
            cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
            statusMsg = parserMaintCmdStatus(cmdStatusWord);
            Utilities.CommDataFrameAppendTxt(statusMsg);            
            
        }
        else
        {
            JOptionPane.showMessageDialog(null,"No Response.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
        }        
    }
    
    public int reboot(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_REBOOT_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            

        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        ///GlobalVars.commDataFrame.jTextArea1.append("\nSent: " + cmdData.stringBuf + "\n");
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        //wait for 1 second after the DCU device is instructed to reboot
        try{
            TimeUnit.MILLISECONDS.sleep(1000);  // 1000ms delay
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }
                
        //--------------------------------------------------------------
        //-------------------- comm receive block ----------------------
        //--------------------------------------------------------------
        //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
        short dwBytesRead = 0;
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 7);

        //charToASC(cmdData.receiveBuf, cmdData.strBuf, (unsigned short)dwBytesRead);
        if(dwBytesRead != 0) // get some thing from serial port
        {
            String dispStr;
            
            crc = (cmdData.receiveBuf[3]<<24) +
                    (cmdData.receiveBuf[4]<<16) +
                    (cmdData.receiveBuf[5]<<8) +
                    (cmdData.receiveBuf[6] & 0xFF);            
            
            if (crc == calculateMaintCRC(cmdData.receiveBuf, (short) 3, 0xFFFFFFFF))
            {
                cmdData.cmdResult = true;
            }

            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);         
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");
            
            cmdStatusWord = cmdData.receiveBuf[2];
            cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
            statusMsg = parserMaintCmdStatus(cmdStatusWord);   
            Utilities.CommDataFrameAppendTxt(statusMsg);  
            
            return 0;
        }
        else
        {
            JOptionPane.showMessageDialog(null,"No Response.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
            return 1;
        }                
    }

    public void eraseData(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int oriTimeOut = 0;
        int cmdStatusWord = 0;
        String statusMsg;
        short dwBytesRead;

        cmdData.cmdBuf[offset++] = 0;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_DATA_ERASE_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        // Wait for 1.0 seconds after the DCU device is instructed to erase all data
        // It takes time for the eraseData command to complete and respond back
        try{
            TimeUnit.MILLISECONDS.sleep(1000);  // 1000ms delay
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }
             
        
        //------------ comm receive block ----------
        {
            //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
            dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 7);

            if(dwBytesRead != 0) // get some thing from serial port
            {
                statusMsg = parserMaintCmdStatus(cmdStatusWord);                                  
                Utilities.CommDataFrameAppendTxt(statusMsg);
                
                cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);             
                Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");  
            }
            else
            {
                JOptionPane.showMessageDialog(null,"No Response","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void eraseConfigure(TcmdData cmdData){

    }

    public boolean writeROBlock(int inputID, TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        int blockSize = 0;
        String statusMsg = new String();
        int cmdStatus = 0;
        int dcuAddr = 0;    
        boolean result = true;
        short dwBytesRead = 0;
  
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_WRITE_RO_BLOCK_CMD;
        cmdData.cmdBuf[offset++] = (byte) ((inputID>>>8) & 0xFF);
        cmdData.cmdBuf[offset++] = (byte) (inputID & 0xFF);

        switch(GlobalVars.sysConfig.getBlockSize())
        {
            case 1:
                blockSize = 128;
                break;

            case 2:
                blockSize = 256;
                break;

            case 3:
                blockSize = 512;
                break;

            case 4:
                blockSize = 1024;
                break;

            default:
                cmdData.cmdResult = false;
                JOptionPane.showMessageDialog(null,"Please Configure Block Size!","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                return result;
        }        
        
        System.arraycopy(cmdData.dataBlock,0,cmdData.cmdBuf,offset,blockSize);
        offset += blockSize;                
        
        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");

        // Sleep for 0.2 second.  The Write RO Block is slow
        try{
            TimeUnit.MILLISECONDS.sleep(200);  // delay
        }
        catch(InterruptedException ex){}    
        
        //-------------------- comm receive block ----------------------
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 7);

        if(dwBytesRead != 0) // get some thing from serial port
        {
            String dispStr;

            offset = 0;
            dcuAddr = cmdData.receiveBuf[offset++];
            cmdStatus = cmdData.receiveBuf[offset++];
            cmdStatus = cmdStatus + (cmdData.receiveBuf[offset++] << 8);
            if(dcuAddr != GlobalVars.sysConfig.getDcuAddress())
            {
                JOptionPane.showMessageDialog(null,"Error with writeROBlock command.  Block ID: " + inputID + ".  Received a response of only " + dwBytesRead + " bytes.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
                result = false;
            }
            if(cmdStatus != Constants.OK)
            {
                result = false;
            }
                    
            crc = cmdData.receiveBuf[offset++];
            crc = (crc <<8) + (cmdData.receiveBuf[offset++] & 0xFF);
            crc = (crc <<8) + (cmdData.receiveBuf[offset++] & 0xFF);
            crc = (crc <<8) + (cmdData.receiveBuf[offset++] & 0xFF);
            
            if (crc != calculateMaintCRC(cmdData.receiveBuf, (short) 3, 0xFFFFFFFF))
            {
                cmdData.cmdResult = false;
            }
            
            //dispStr = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            ///GlobalVars.commDataFrame.jTextArea1.append("Received: " + cmdData.stringBuf + "\n");
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");
            
            statusMsg = parserMaintCmdStatus(cmdStatusWord);
            ///GlobalVars.commDataFrame.jTextArea1.append(statusMsg);
            Utilities.CommDataFrameAppendTxt(statusMsg);
        }
        else
        {
            result = false;
            cmdData.cmdResult = result;            
            JOptionPane.showMessageDialog(null,"No Response.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
        }
        
        return result;
    }

    public void downloadData(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int oriTimeOut = 0;
        int cmdStatusWord = 0;
        String statusMsg;
        short dwBytesRead = Constants.BUF_SIZE;
        int blockSize = 0;      
        int i = 0;
        int totalNumber = 0;
        int totalBytes = 0;
        

        cmdData.cmdBuf[offset++] = 0;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_DATA_DOWNLOAD_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        //------------ comm receive block ----------
        {
            switch(GlobalVars.sysConfig.getBaudRate())
            {
                case 1:
                    blockSize = 128;
                    break;

                case 2:
                    blockSize = 256;
                    break;

                case 3:
                    blockSize = 512;
                    break;

                case 4:
                    blockSize = 1024;
                    break;

                default:
                    cmdData.cmdResult = false;
                    JOptionPane.showMessageDialog(null,"Please Configure Block Size!","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
                    return;
            }        

            
            Utilities.CommDataFrameAppendTxt("Received:\n");                     
            while(dwBytesRead != 0)
            {
                if(i == 0)
                {
                    i = 3;
                }
                else
                {
                    i = blockSize + 7;//block size + id size + crc size + 1 byte status
                }
                dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, i);
                totalBytes += dwBytesRead;
                                
                if(dwBytesRead != 0) // get some thing from serial port
                {
                    totalNumber++;
                    cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
                    Utilities.CommDataFrameAppendTxt(cmdData.stringBuf + "\n");                     
                }
            }            
        }
        //sprintf(tempBuf, "the total download blockNumber is %d\n totally %d bytes", totalNumber-2, totalBytes);
        //ShowMessage((char*)tempBuf);        
    }

    public void readStatus(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
  
        cmdData.cmdBuf[offset++] = 0x00;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_READ_STATUS_CMD;

        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF);
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
            
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        {
            //--------------------------------------------------------------
            //-------------------- comm receive block ----------------------
            //--------------------------------------------------------------
            //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
            dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, offset);

            if(dwBytesRead != 0) // get some thing from serial port
            {
                cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
                Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");

                cmdStatusWord = cmdData.receiveBuf[2];
                cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
                statusMsg = parserMaintCmdStatus(cmdStatusWord);
                Utilities.CommDataFrameAppendTxt(statusMsg);                        
            }
            else
            {
                JOptionPane.showMessageDialog(null,"ERROR: DCU no Response!","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            }        
        }
    }

    public void invalidateData(int inputID, TcmdData cmdData){

    }

    public void testWriteData(int inputID, Boolean ip, TcmdData cmdData){

    }

    public void testReadData(int inputID, TcmdData cmdData){

    }

    public void configureUnitID(TcmdData cmdData){
        int offset = 0;
        int crc = 0;
        int oriTimeOut = 0;
        int cmdStatusWord = 0;
        String statusMsg;
        int dwBytesWrite;
        int dwBytesRead;

        cmdData.cmdBuf[offset++] = 0;
        cmdData.cmdBuf[offset++] = (byte) 0xFF;
        cmdData.cmdBuf[offset++] = (byte) Constants.MAINT_CONFIG_UNIT_ID;
        
        /*
        while((cmdData.dataBlock[i] != 0) && (i<32)){
            cmdData.cmdBuf[offset++] = cmdData.dataBlock[i++];
        }
        */
        // Code change.  Since the user always enters a Part No. & Serial No. combination of 32 bytes, 
        // we don't need to check for (cmdData.dataBlock[i] != 0)
        //System.arraycopy(cmdData.dataBlock,0,cmdData.cmdBuf,offset);
        for (int i=0; i<32; i++) {            
            cmdData.cmdBuf[offset++] = cmdData.dataBlock[i];
        }

        
        crc = calculateMaintCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);

        cmdData.cmdBuf[offset++] = (byte) ((crc>>>24)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>>16)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) ((crc>>>8)&0xFF);
        cmdData.cmdBuf[offset++] = (byte) (crc&0xFF); 
       
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);

        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");
        
        // Sleep for 0.2 second.  Give time for the DCU device to process the command
        try{
            TimeUnit.MILLISECONDS.sleep(200);  // 100ms delay
        }
        catch(InterruptedException ex){}    
        
        //------------ comm receive block ----------
        {
            //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
            dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 300);

            if(dwBytesRead != 0) // get some thing from serial port
            {
                cmdStatusWord = cmdData.receiveBuf[2];
                cmdStatusWord = ((cmdStatusWord << 8) & 0xFF00) + cmdData.receiveBuf[1];
                statusMsg = parserMaintCmdStatus(cmdStatusWord);
                Utilities.CommDataFrameAppendTxt(statusMsg);
                cmdData.cmdResult = true;

                cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short) dwBytesRead);
                Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");

            }
            else{
                JOptionPane.showMessageDialog(null,"No Response","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            }
        }
    }

    public void calculateDCUCRC(TcmdData cmdData){

    }

    public void uploadSoftware(TcmdData cmdData){

    }

    public void getTempRecord(TcmdData cmdData){

    }

    public void getConfigCounter(TcmdData cmdData){

    }

    public int calculateMaintCRC(byte [] inbuf,
                              short length,
                              int startCRC){
        return ((Utilities.calculateCRC(inbuf, length, startCRC))^0xFFFFFFFF);
    }
    
    /*
    public int calculateMaintCRC2(long addr,
                              short length,
                              int startCRC){
        return ((Utilities.calculateCRC2(addr, length, startCRC))^0xFFFFFFFF);
    }
    */
}
