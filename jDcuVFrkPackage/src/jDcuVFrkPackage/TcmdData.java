/*
 * TcmdData.java
 *
 * Created on February 3, 2014, 10:10 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.util.Arrays;


/**
 *
 * @author FKY301079
 */
public class TcmdData {
       
    public byte[] cmdBuf = new byte[Constants.BUF_SIZE];
    //@SuppressWarnings("restriction")
    //sun.misc.Unsafe unsafe = Utilities.getUnsafe();
    //long cmdBufAddr = unsafe.allocateMemory(Constants.BUF_SIZE);
    public byte[] receiveBuf = new byte[Constants.BUF_SIZE];

    public byte[] dataBlock = new byte[Constants.BUF_SIZE];
    public byte[] strBuf = new byte[Constants.BUF_SIZE*3];
    public String stringBuf = new String();

    public Boolean cmdResult;
    public byte cmdStatus;
    public int dcuStatus;

    public int numRecvBytes = 0;

    /** Creates a new instance of TcmdData */
    public void TcmdData() 
    {
        cmdStatus = 0;
        dcuStatus = 0;
        cmdResult = false;
    }
   
    public byte[] getReceive()
    {
        //inputBuffer = java.util.Arrays.copyOf(receiveBuf, Constants.BUF_SIZE);
        return receiveBuf;
    }
   
   
    public void clear()
    {
        cmdStatus = 0;
        dcuStatus = 0;
        cmdResult = false;
        Arrays.fill(cmdBuf,(byte)0);
        Arrays.fill(receiveBuf,(byte)0);            
    }

}


