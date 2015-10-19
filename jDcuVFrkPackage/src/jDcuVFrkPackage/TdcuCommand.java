/*
 * TdcuCommand.java
 *
 * Created on February 3, 2014, 2:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import javax.swing.JOptionPane;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

/**
 *
 * @author FKY301079
 */
public class TdcuCommand {        
    private String parserCmdStatus(int pStatus){
        String tmpString;
        
        tmpString = "--CMD ";
        if (pStatus == 0)
        {
            tmpString = tmpString + "OK\r\n";
        }
        else
        {
            /* D0 */
            if ((pStatus & Constants.DCU_MEMORY_FULL) != 0)
            {
                tmpString = tmpString + "ERR, D0: Memory_Full\r\n";
            }
            /* D1 */
            if ((pStatus & Constants.TRAN_INTEGRITY_FAILURE) != 0)
            {
                tmpString = tmpString + "ERR, D1: UART_Transmission_Failure\r\n";
            }
            /* D2 */
            if ((pStatus & Constants.INVALID_BLOCK_ID) != 0)
            {
                tmpString = tmpString + "ERR, D2: Invalid Block _ID\r\n";
            }
            /* D3 */
            if ((pStatus & Constants.DCU_HW_FAUILRE) != 0)
            {
                tmpString = tmpString + "ERR, D3: DCU_HW_Failure\r\n";
            }
            /* D4 */
            if ((pStatus & Constants.LAST_CMD_FAULT) != 0)
            {
                tmpString = tmpString + "ERR, D4: Last_CMD_Fault\r\n";
            }
            /* D5 */
            if ((pStatus & Constants.DATA_INTEGRITY_FAILURE) != 0)
            {
                tmpString = tmpString + "ERR, D5: Data_CRC_Failure\r\n";
            }
            /* D6 */
            if ((pStatus & Constants.DCU_BUSY) != 0)
            {
                tmpString = tmpString + "ERR, D6: DCU_Busy\r\n";
            }
            /* D7 */
            if ((pStatus & Constants.READ_ONLY_BLOCK) != 0)
            {
                tmpString = tmpString + "INF, D7: Read_Only_Block\r\n";
            }
        }
        //memcpy(pStatusMsg_ptr, tmpString.c_str(), tmpString.Length());

        return tmpString;        
    }    
    
    /** Creates a new instance of TdcuCommand */
    public void TdcuCommand() {
    }

    public void invokeBaudRate()
    {
        int offset = 0;
        int crc = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
        int cmdLength = 0;
        TcmdData cmdData = new TcmdData();

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) Constants.INVOKEBAUDRATE;

        cmdLength = calculateCsCrc(cmdData.cmdBuf, offset);

        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, cmdLength);
            

        //------- display the sent message ---------
        //displayStr = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) cmdLength);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");

        // Sleep for 0.3 second.  Wait for the DCU to process the command
        try{
            TimeUnit.MILLISECONDS.sleep(300);  // 200ms delay
        }
        catch(InterruptedException ex){}    
                
        //-------------------- comm receive block ----------------------
        //dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, Constants.BUF_SIZE);
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 10);

        if(dwBytesRead != 0) // get some thing from serial port
        {
            boolean result = true;

            offset = 0;
            // Check DCU address
            if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
            {
                result = false;
            }
            
            // Check DCU cmd status
            if(cmdData.receiveBuf[offset++] != Constants.OK)
            {
                result = false;
            }

            // Check Checksum/CRC
            if(GlobalVars.sysConfig.getCmdSet() == Constants.DCUV)
            {
                if (dwBytesRead >= 6)
                {
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 4) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            else
            {
                if (dwBytesRead >= 4)
                {
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 2) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short) dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");    
                        
            statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
            Utilities.CommDataFrameAppendTxt(statusMsg);    

            cmdData.cmdResult = result;

            if (result != true)
            {
                JOptionPane.showMessageDialog(null,"ERROR: DCU response error!\nPlease write down this error.\nNotify your manager or supervisor of this error.\nThe Utility Program will shutdown now.\nThe power to the laptop will turned off.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);                            
                Utilities.shutdownSystem();
 
               // Wait for 30 seconds for shutdown
                try
                {
                    TimeUnit.MILLISECONDS.sleep(30000);  // 3000ms delay
                }
                catch(InterruptedException ex) {}
             }
        }
        else
        {
            JOptionPane.showMessageDialog(null,"ERROR: DCU no Response! Please make sure the DCU is connected and power on.\nThe Utility Program will shutdown now.\nThe power to the laptop will be turned off.\nPlease power up the laptop and start again after you have everything connected.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            

            Utilities.shutdownSystem();
        }
    }
    
    public void receiveBaudRate(byte baudRate)
    {
        int offset = 0;
        int crc = 0;
        String statusMsg = new String();
        short dwBytesRead = 0;
        int cmdLength = 0;
        TcmdData cmdData = new TcmdData();

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) Constants.RECEIVEBAUDRATE;
        cmdData.cmdBuf[offset++] = (byte) baudRate;

        cmdLength = calculateCsCrc(cmdData.cmdBuf, offset);

        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, cmdLength);
            
        //------- display the sent message ---------
        //displayStr = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) cmdLength);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");

        // Sleep for 0.2 second.  Wait for the DCU to process the command
        try{
            TimeUnit.MILLISECONDS.sleep(200);  // 200ms delay
        }
        catch(InterruptedException ex){}    
                
        //-------------------- comm receive block ----------------------
        //dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, Constants.BUF_SIZE);
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 10);

        if(dwBytesRead != 0) // get some thing from serial port
        {
            boolean result = true;

            offset = 0;
            // Check DCU address
            if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
            {
                result = false;
            }
            
            // Check DCU cmd status
            if(cmdData.receiveBuf[offset++] != Constants.OK)
            {
                result = false;
            }

            // Check Checksum/CRC
            if(GlobalVars.sysConfig.getCmdSet() == Constants.DCUV)
            {
                if (dwBytesRead >= 6)
                {                    
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 4) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            else
            {                
                if (dwBytesRead >= 4)
                {                    
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 2) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short) dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");    
                        
            statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
            Utilities.CommDataFrameAppendTxt(statusMsg);    

            cmdData.cmdResult = result;

            if (result != true)
            {                
                JOptionPane.showMessageDialog(null,"ERROR: DCU response error!\nPlease write down this error.\nNotify your manager or supervisor of this error.\nThe Utility Program will shutdown now.\nThe power to the laptop will turned off.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);                            
                
                Utilities.shutdownSystem();
             }
        }
        else
        {
            JOptionPane.showMessageDialog(null,"ERROR: DCU no Response! Please make sure the DCU is connected and power on.\nThe Utility Program will shutdown now.\nThe power to the laptop will be turned off.\nPlease power up the laptop and start again after you have everything connected.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            

            Utilities.shutdownSystem();
        }                
    }

    public void readStatus(TcmdData cmdData)
    {
       int offset = 0;
       int cmdLength = 0;

       //cmdData.cmdBuf[offset++] = sysConfig->dcuAddress;
       cmdData.cmdBuf[offset++] = 0; //READSTATUS;
       //calculateCsCrc(cmdData.cmdBuf, offset);
       cmdLength = offset;

       //PurgeComm(hComm, PURGE_RXABORT|PURGE_RXCLEAR|PURGE_TXABORT|PURGE_TXCLEAR);
       short dwBytesWrite = 0;
       //WriteFile(hComm, cmdData.cmdBuf, cmdLength, &dwBytesWrite, NULL);
       //short timer =  GetTickCount();
       //------- display the sent message ---------
       //charToASC(cmdData.cmdBuf, cmdData.strBuf, cmdLength);
       //FormCommData->Memo1->SetSelTextBuf("\r\nSent:\r\n");
       //FormCommData->Memo1->SetSelTextBuf(cmdData.strBuf);
       //------------ comm receive block ----------
       {
            //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
            short dwBytesRead = 0;

            //ReadFile(hComm, cmdData.receiveBuf, BUF_SIZE, &dwBytesRead, NULL);
            //timer =  GetTickCount() - timer;
            //charToASC(cmdData.receiveBuf, cmdData.strBuf, (unsigned short)dwBytesRead);
            if(dwBytesRead != 0) // get some thing from serial port
            {
                Boolean result = true;
                int dcuStatus = 0;
             /*
             offset = 0;
             if(cmdData.receiveBuf[offset++] != sysConfig->dcuAddress)
             {
                result = false;
             }
             if(cmdData.receiveBuf[offset++] != OK)
             {
                result = false;
             }

             //----- note that dcu status is transfered as little endian ---
             cmdData.dcuStatus = cmdData.receiveBuf[offset++];
             cmdData.dcuStatus += (cmdData.receiveBuf[offset++])<<8;

             if(verifyCsCrc(cmdData.receiveBuf, offset) != true)
             {
                result = false;
             }

             FormCommData->Memo1->SetSelTextBuf("\r\nReceived:\r\n");
             FormCommData->Memo1->SetSelTextBuf(cmdData.strBuf);
             cmdData.cmdResult = result;
             */
            }
            else {
                JOptionPane.showMessageDialog(null,"No Response.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            
            }
        }
    }
    
    public int readData(int inputID, TcmdData cmdData)
    {
        int offset = 0;
        String statusMsg;
        int dwBytesRead = 0;
        boolean result = true;
        int dataSize = 0;
        
        switch (GlobalVars.sysConfig.getCmdSet())
        {
        case Constants.DCUV: // DCU V
            if (GlobalVars.sysConfig.getBlockSize() == 4)
            {
                dataSize = 1024;
            }
            else if (GlobalVars.sysConfig.getBlockSize() == 3)
            {
                dataSize = 512;
            }
            else if (GlobalVars.sysConfig.getBlockSize() == 2)
            {
                dataSize = 256;
            }
            else if (GlobalVars.sysConfig.getBlockSize() == 1)
            {
                dataSize = 128;
            }
            else // default size is 128
            {
                dataSize = 128;
            }
            break;

        case Constants.DCUIV: // DCU IV
            dataSize = 128;
            break;
        case Constants.DCUII: // DCU II
            dataSize = 128;
            break;
        default:
            dataSize = 128;
            break;
        }

        
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) Constants.READDATA;

        cmdData.cmdBuf[offset++] = (byte) ((inputID>>8) & 0xFF);
        cmdData.cmdBuf[offset++] = (byte) (inputID & 0xFF);

        offset = calculateCsCrc(cmdData.cmdBuf, offset);
        


        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);      
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");                        


        // Delay for 0.1 sec for DCU device to processs Read Data command
        try{
            TimeUnit.MILLISECONDS.sleep(100);  // delay for 100 milliseconds
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }    

        //------------ comm receive block ----------
        // read data from DCU
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 2+dataSize+4);
        cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short) dwBytesRead);            


        
        
        
        
        if(dwBytesRead != 0) // get some thing from serial port
        {
            offset = 0;
            if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
            {
                result = false;
            }
            
            cmdData.cmdStatus = cmdData.receiveBuf[offset];                 //return cmd status
            if( (cmdData.receiveBuf[offset++] != Constants.OK) &&
                        (GlobalVars.sysConfig.getCmdSet() != Constants.DCUV) )   //ENH mode will not return OK
            {
                result = false;
            }
            else
            {
                cmdData.dataBlock = java.util.Arrays.copyOfRange(cmdData.receiveBuf,offset,offset+dataSize);
            }        
            
            offset += dataSize;
            if(verifyCsCrc(cmdData.receiveBuf, offset) != true)
            {
                result = false;
            }
            
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short)dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");  
            
            statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
            Utilities.CommDataFrameAppendTxt(statusMsg);  

            cmdData.cmdResult = result;                     
            return dwBytesRead;
        }
        else
        {
            JOptionPane.showMessageDialog(null,"No Response from DCU device for Read Data command.\nClick on Continue button to continue with upgrade.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
            cmdData.cmdStatus = (byte) 0xFF;   //return cmd status
            return -1;
        }
    }
    
    
    public boolean writeData(int inputID, TcmdData cmdData, Boolean random)
    {
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        int blockSize = 0;
        String statusMsg = new String();
        String displayStr = new String();
        int cmdStatus = 0;
        int dcuAddr = 0;    
        boolean result = true;
        Random generator = new Random(inputID);
        short dwBytesRead = 0;
        long timer = 0;

        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) Constants.WRITEDATA;

        cmdData.cmdBuf[offset++] = (byte) ((inputID>>8) & 0xFF);
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
        
        
        if (random)      /* generate data block randomly */
        {
            for(int i=0;i<blockSize;i++)
            {
                cmdData.dataBlock[i] = (byte) generator.nextInt();
            }
        }        
        
        System.arraycopy(cmdData.dataBlock,0,cmdData.cmdBuf, offset, blockSize);
        offset += blockSize;
        offset = calculateCsCrc(cmdData.cmdBuf, offset);
        

        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);        
        timer = System.currentTimeMillis();
        
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");                   

//TBD
        // Delay for 0.2 sec for DCU device to processs Write Data command
        // Delay for 3.0 sec for DCU device to processs Write Data command
        try{
            TimeUnit.MILLISECONDS.sleep(200);  // delay for 200 milliseconds
//            TimeUnit.MILLISECONDS.sleep(3000);  // delay for 1000 milliseconds
        }
        catch(InterruptedException ex){
            System.err.println(ex.getMessage());
        }    
        
        //--------------------------------------------------------------
        //-------------------- comm receive block ----------------------
        //--------------------------------------------------------------
        {
            dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, Constants.BUF_SIZE);
            timer =  System.currentTimeMillis() - timer;                    
            
            
            if(dwBytesRead != 0) // get some thing from serial port
            {
                offset = 0;
                if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
                {
                    result = false;
                }
                if(cmdData.receiveBuf[offset++] != Constants.OK)
                {
                    result = false;
                }

                if(verifyCsCrc(cmdData.receiveBuf, offset) != true)
                {
                    result = false;
                }

                cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
                Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");

                statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
                Utilities.CommDataFrameAppendTxt(statusMsg);
                
                cmdData.cmdResult = result;
            }
            else
            {
                result = false;
                cmdData.cmdResult = result;
                JOptionPane.showMessageDialog(null,"No Response for Write Data command. " + "Received: " + Integer.toString(dwBytesRead) + " bytes.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.INFORMATION_MESSAGE);                        
            }            
        }
        
        return result;
    }

    public int readDcuID(TcmdData cmdData, boolean msgOutput)
    {
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        String displayStr = new String();
        short dwBytesRead = 0;
        int cmdLength = 0;
        byte[] displayBuf = new byte[16];

        cmdData.cmdResult = false;     
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = (byte) Constants.READDCUID;

        cmdLength = calculateCsCrc(cmdData.cmdBuf, offset);

        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, cmdLength);
            

        //------- display the sent message ---------
        //displayStr = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) cmdLength);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");

        // Sleep for 0.3 second.  The Read DCU ID command is slow
        try{
            TimeUnit.MILLISECONDS.sleep(300);  // 300ms delay
        }
        catch(InterruptedException ex){}    
                
        //-------------------- comm receive block ----------------------
        //dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, Constants.BUF_SIZE);
        dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, 38);

        //charToASC(cmdData.receiveBuf, cmdData.strBuf, (unsigned short)dwBytesRead);
        if(dwBytesRead != 0) // get some thing from serial port
        {
            boolean result = true;

            offset = 0;
            if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
            {
                result = false;
            }
            if(cmdData.receiveBuf[offset++] != Constants.OK)
            {
                result = false;
            }

            //----- note that dcu status is transfered as little endian ---
            cmdData.dcuStatus = cmdData.receiveBuf[offset++];
            cmdData.dcuStatus += (cmdData.receiveBuf[offset++])<<8;

            if(GlobalVars.sysConfig.getCmdSet() == Constants.DCUV)
            {
                if (dwBytesRead >= 6)
                {
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 4) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            else
            {
                if (dwBytesRead >= 4)
                {
                    if(verifyCsCrc(cmdData.receiveBuf, dwBytesRead - 2) != true)
                    {
                        result = false;
                    }
                }
                else
                {
                    result = false;
                }
            }
            
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, (short) dwBytesRead);
            Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");    
                        
            statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
            Utilities.CommDataFrameAppendTxt(statusMsg);    

            cmdData.cmdResult = result;
            
            try
            {
                String msgBuf = new String("Part Number: ");
                String tmpString = new String(java.util.Arrays.copyOfRange(cmdData.receiveBuf,2,Constants.PART_SERIAL_NUM_LEN), "UTF-8");    
                msgBuf = msgBuf + tmpString;
                GlobalVars.PART_NUMBER = tmpString.trim();
                msgBuf = msgBuf + "\nSerial Number: ";

                tmpString = new String(java.util.Arrays.copyOfRange(cmdData.receiveBuf,18,18+Constants.PART_SERIAL_NUM_LEN), "UTF-8");    
                msgBuf = msgBuf + tmpString;
                GlobalVars.SERIAL_NUMBER = tmpString.trim();

                if (msgOutput == true)
                {
                    JOptionPane.showMessageDialog(null,msgBuf,"DCU V FPK" + Constants.SW_VERSION,JOptionPane.INFORMATION_MESSAGE);                        
                }                                                    
            } catch(java.io.UnsupportedEncodingException ex){}                    


            // succesful
            return 0;
        }
        else
        {
            JOptionPane.showMessageDialog(null,"ERROR: DCU no Response! Please make sure the DCU is connected and power on.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);            

            // failed
            return 1;
        }
    }
    
    public void readConfiguration(TcmdData cmdData)
    {
        int offset = 0;
        int crc = 0;
        int cmdStatusWord = 0;
        String statusMsg = new String();
        String displayStr = new String();
        int cmdLength = 0;
        
        cmdData.cmdBuf[offset++] = (byte) GlobalVars.sysConfig.getDcuAddress();
        cmdData.cmdBuf[offset++] = Constants.READCONFIG;
        crc = Utilities.calculateCRC(cmdData.cmdBuf, (short) offset, 0xFFFFFFFF);
        cmdData.cmdBuf[offset++] = (byte) (crc>>24);
        cmdData.cmdBuf[offset++] = (byte) (crc>>16);
        cmdData.cmdBuf[offset++] = (byte) (crc>>8);
        cmdData.cmdBuf[offset++] = (byte) (crc);
        cmdLength = (byte) offset;
        
        GlobalVars.commPort.WriteFile(cmdData.cmdBuf, offset);
        
        //------- display the sent message ---------
        cmdData.stringBuf = Utilities.charToASC(cmdData.cmdBuf, (short) offset);
        Utilities.CommDataFrameAppendTxt("\nSent: " + cmdData.stringBuf + "\n");

        //------------ comm receive block ----------
        {
            //--- every byte in inBuf needs 3 bytes in charBuf to display(include space)---
            short dwBytesRead = 0;
            dwBytesRead = GlobalVars.commPort.ReadFile(cmdData.receiveBuf, Constants.BUF_SIZE);            
            cmdData.numRecvBytes = dwBytesRead;
                       
            cmdData.stringBuf = Utilities.charToASC(cmdData.receiveBuf, dwBytesRead);
            if(dwBytesRead != 0) // get some thing from serial port
            {
                boolean result = true;
                //       t_u16 dcuStatus = 0;

                offset = 0;
                if(cmdData.receiveBuf[offset++] != GlobalVars.sysConfig.getDcuAddress())
                {
                    result = false;
                }
                if(cmdData.receiveBuf[offset++] != ((byte) Constants.OK))
                {
                    result = false;
                }

                crc = 0;
                offset = 9;
                crc = cmdData.receiveBuf[offset++] << 24;
                crc += cmdData.receiveBuf[offset++] << 16;
                crc += cmdData.receiveBuf[offset++] << 8;
                crc += (cmdData.receiveBuf[offset++] & 0xFF);
                if(crc != Utilities.calculateCRC(cmdData.receiveBuf, (short) (offset - 4), 0xFFFFFFFF))
                {
                    result = false;
                }                                

                Utilities.CommDataFrameAppendTxt("Received: " + cmdData.stringBuf + "\n");    
                statusMsg = parserCmdStatus(cmdData.receiveBuf[1]);
                Utilities.CommDataFrameAppendTxt(statusMsg);              
                                
                cmdData.cmdResult = result;                                
                
            } 
            else{
                JOptionPane.showMessageDialog(null,"No Response.","DCU V FPK" + Constants.SW_VERSION,JOptionPane.WARNING_MESSAGE);
            }
        }        
    }


    public int calculateCsCrc(byte[] inbuf, int length)
    {
        int crc = 0;
        short checksum = 0;
        int offset = length;

        if(GlobalVars.sysConfig.getCmdSet() == Constants.DCUV)
        {
            crc = Utilities.calculateCRC(inbuf, (short) offset, 0xFFFFFFFF);
            inbuf[offset++] = (byte)(crc>>24);
            inbuf[offset++] = (byte)(crc>>16);
            inbuf[offset++] = (byte)(crc>>8);
            inbuf[offset++] = (byte)(crc & 0xFF);
        }
        else
        {
            checksum = Utilities.calculateCS(inbuf, (short) offset);
            inbuf[offset++] = (byte)(checksum>>>8);
            inbuf[offset++] = (byte)(checksum & 0xFF);
        }
        
        return offset;
    }
    
    public boolean verifyCsCrc(byte[] inbuf, int index){
        int crc = 0;
        int checksum = 0;
        boolean result = false;
        int offset = index;


        
        if(GlobalVars.sysConfig.getCmdSet() == Constants.DCUV)
        {
            crc = (inbuf[offset++] << 24)   & 0xFF000000 ;
            crc += ((inbuf[offset++]) << 16)& 0x00FF0000;
            crc += (inbuf[offset++] << 8)   & 0x0000FF00;
            crc += (inbuf[offset++])        & 0x000000FF;
            if(crc == Utilities.calculateCRC(inbuf, (short) (offset - 4), 0xFFFFFFFF))
            {
                result = true;
            }
        }
        else
        {
            checksum = inbuf[offset++] << 8;
            checksum += (inbuf[offset++] & 0xFF);
            if(checksum == Utilities.calculateCS(inbuf, (short) (offset - 2)))
            {
                result = true; 
            }
        }        
        return result;
    }    
}