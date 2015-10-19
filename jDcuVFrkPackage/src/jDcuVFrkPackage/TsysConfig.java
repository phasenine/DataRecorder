/*
 * TsysConfig.java
 *
 * Created on February 21, 2014, 12:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

/**
 *
 * @author FKY301079
 */
public class TsysConfig {
    private int dcuAddress = 0;
    private int baudRate = 0;
    private int transmitMode = 0;
    private int cmdSet = 0;
    private int blockSize = 0;
    private int testEnable = 0;
    private int spare = 0;

    public static int idSize;

    /** Creates a new instance of TsysConfig */
    public TsysConfig() {
        dcuAddress = 0;
        baudRate = 0;
        transmitMode = 0;
        cmdSet = 0;
        blockSize = 0;
        testEnable = 0;
        spare = 0;
    }
    
    public void setDcuAddress (int setVal){
        dcuAddress = setVal;
    }
    
    public int getDcuAddress (){
        return dcuAddress;
    }

    public void setBaudRate (int setVal){
        baudRate = setVal;
    }
    
    public int getBaudRate (){
        return baudRate;
    }
    
    public void setTransmitMode (int setVal){
        transmitMode = setVal;
    }
    
    public int getTransmitMode (){
        return transmitMode;
    }
    
    public void setCmdSet (int setVal){
        cmdSet = setVal;
    }
    
    public int getCmdSet (){
        return cmdSet;
    }
    
    public void setBlockSize (int setVal){
        blockSize = setVal;
    }
    
    public int getBlockSize (){
        return blockSize;
    }
    
    public void setTestEnable (int setVal){
        testEnable = setVal;
    }
    
    public int getTestEnable (){
        return testEnable;
    }
    
    public void setSpare (int setVal){
        spare = setVal;
    }
    
    public int getSpare (){
        return spare;
    }
    
    public void setIdSize (int setVal){
        idSize = setVal;
    }
    
    public int getIdSize (){
        return idSize;
    }
    
}
