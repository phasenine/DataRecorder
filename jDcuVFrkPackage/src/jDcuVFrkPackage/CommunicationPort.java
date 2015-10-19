/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.awt.Component;
import java.awt.Cursor;
import javax.swing.JButton;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import gnu.io.SerialPortEventListener;
import gnu.io.SerialPortEvent;

import gnu.io.RXTXPort;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.TooManyListenersException;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.nio.charset.Charset;

/**
 *
 * @author root
 */
public class CommunicationPort {
    private final int MAX_BUFFER_SIZE = 400;
    private final int WAIT_TIME_OUT_CTR = 10;
    private final int EACH_SLEEP_PERIOD = 10;
    private int bufferHead = 0;
    private int bufferTail = 0;
    private int numBytesRecv = 0;
    private int bytesOccupied = 0;
    
    private byte[] readBuffer = new byte[MAX_BUFFER_SIZE];
    private byte[] cmdBuffer = new byte[MAX_BUFFER_SIZE];
    private static CommPortIdentifier portId;
    public static SerialPort serialPort;
    private static OutputStream outStream;
    private static InputStream inStream;
    private String tempString;
    public static String portArray[] = null;

    public static String[] listSerialPorts(){
        Enumeration ports;
        ArrayList<String> portList = new ArrayList();        
        
        try{
            ports = CommPortIdentifier.getPortIdentifiers();
            
            while (ports.hasMoreElements()){
                CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
                if (port.getPortType() == CommPortIdentifier.PORT_SERIAL){
                    portList.add(port.getName());                
                }
            }
            portArray = (String[]) portList.toArray(new String[0]);
            
        }
        catch (Exception ex){
            System.err.println(ex.getMessage());
        }
        
        return portArray;
    }
    
    private void setSerialEventHandler(){
        try{
            serialPort.addEventListener(new SerialEventHandler());
            serialPort.notifyOnDataAvailable(true);
            serialPort.enableReceiveThreshold(15);            
        } catch (TooManyListenersException ex) {
            System.err.println(ex.getMessage());
        } catch (UnsupportedCommOperationException ex) {
            System.err.println(ex.getMessage());
        }               
    }
    
    public void connect(String portName, int baudRate) throws IOException {
        try{
            portId = CommPortIdentifier.getPortIdentifier(portName);

            serialPort = (SerialPort) portId.open("DCU V GSE application", 5000);

            setSerialPortParameters(baudRate);                       
            
            outStream = serialPort.getOutputStream();
            inStream = serialPort.getInputStream();


                
            // setup the buffer's Head and Tail index
            bufferHead = 0;
            bufferTail = 0;
            
            // Setup Serial Event Handler for incoming data
            
            // <TBC> setSerialEventHandler();
        }
        catch(NoSuchPortException ex){
            throw new IOException(ex.getMessage());
        }
        catch(PortInUseException ex){
            throw new IOException(ex.getMessage());
        }
        catch(IOException ex){
            System.err.println(ex.getMessage());
        }
    }    
    
    public void disconnect() throws IOException {
        try
        {
            outStream.close();
            inStream.close();
        }
        catch(IOException ex)
        {
            System.err.println(ex.getMessage());
        }
        
        serialPort.close();
    }    
    
    public void send(byte[] data, int dataLen){
        try{
            ///outStream.write(data);  <TBC>
            outStream.write(data,0,dataLen);
            try{
                TimeUnit.MILLISECONDS.sleep(10);  // delay for 10 milliseconds
            }
            catch(InterruptedException ex){
                System.err.println(ex.getMessage());
            }            
        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }
    }

    public int receive(){
        int availableBytes = 0;
        try{
            availableBytes = inStream.available();
            if (availableBytes > 0){
                Utilities.CommDataFrameAppendTxt("Received : " + availableBytes + " bytes.\n");
                inStream.read(readBuffer,0,availableBytes);
            }            
        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }

        return availableBytes;                
    }
    
    public InputStream getSerialInputStream(){
        return inStream;
    }
    
    public OutputStream getSerialOutputStream(){
        return outStream;
    }
    
    public void setSerialPortParameters(int baudRate) throws IOException{
        try{            
            serialPort.setSerialPortParams(baudRate,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_ODD);
                    //SerialPort.PARITY_NONE);
            
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            
        }
        catch(UnsupportedCommOperationException ex){
            System.err.println(ex.getMessage());
            throw new IOException("Unsupported serial port parameter");
        }                
    }
    

    public short ReadFile(byte[] receiveBuf, int numReadBytes){
        int availableBytes = 0;
        short returnBytes = 0;
        int counter = 0;
        int waitPeriodMs = 0;
/*
        try{
            // Wait until all the expected bytes arrive from the DCU
            while ((inStream.available() < numReadBytes) && (counter++ < WAIT_TIME_OUT_CTR)) {
                try{
                    TimeUnit.MILLISECONDS.sleep(EACH_SLEEP_PERIOD);  // delay
                }
                catch(InterruptedException ex){}            
            }
            
            availableBytes = inStream.available();

            inStream.read(receiveBuf,0,availableBytes);                
            returnBytes = (short) availableBytes;            
        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }
*/
        
        try{
            counter = serialPort.getBaudRate();
            waitPeriodMs = (int) Math.ceil((((double) numReadBytes / (serialPort.getBaudRate()/10)) * 1000  * 1.05));
            // Sleep "waitPeriodMs" seconds
            try{
                TimeUnit.MILLISECONDS.sleep(waitPeriodMs);  // delay
            }
            catch(InterruptedException ex){}      
            
            availableBytes = inStream.available();
            
            if (availableBytes > 0)
            {
                if (availableBytes > numReadBytes)
                    inStream.read(receiveBuf,0,availableBytes);                
                else    
                    inStream.read(receiveBuf,0,numReadBytes);                
                //returnBytes = (short) availableBytes;            
            }
        }
        catch (IOException ex){
            System.err.println(ex.getMessage());
        }


        return ((short) availableBytes);
    }        
    
    
    public void WriteFile(byte[] cmdBuf, int numWriteBytes){
        GlobalVars.commPort.send(cmdBuf, numWriteBytes);
    }                
    
    

    class SerialEventHandler implements SerialPortEventListener{
        public void serialEvent(SerialPortEvent event){
            switch(event.getEventType()){
                case SerialPortEvent.DATA_AVAILABLE:
                    numBytesRecv = receive();
                    bytesOccupied = bytesOccupied + numBytesRecv;
                    
                    //-------------------------------------------------------------------------------------------------------
                    // The following code takes the received data from the Serial Port and put it into the buffer array
                    //-------------------------------------------------------------------------------------------------------
                    //
                    // If the buffer is full and we have not processed the data, start overwriting the data at the beginning
                    // of the buffer
                    //
                    // Remember to subtract 1 from MAX_BUFFER_SIZE as our array has 0 based index                    
                    //
                    if ((bufferTail+numBytesRecv) > (MAX_BUFFER_SIZE-1)){
                        System.arraycopy(readBuffer, 0, cmdBuffer, bufferTail, (MAX_BUFFER_SIZE-1) - bufferTail);                        
                        bufferTail = numBytesRecv - ((MAX_BUFFER_SIZE-1) - bufferTail);
                        System.arraycopy(readBuffer, 0, cmdBuffer, 0, bufferTail);
                        bufferTail = bufferTail -1;
                    }                        
                    else {
                        System.arraycopy(readBuffer, 0, cmdBuffer, bufferTail, numBytesRecv);                         
                        bufferTail = bufferTail + numBytesRecv;
                    }
                    
                    break;
            } // switch

            
            tempString = new String(cmdBuffer, 0, numBytesRecv, Charset.forName("UTF-8"));
            Utilities.CommDataFrameAppendTxt("Received data " + "(" + Integer.toString(tempString.length()) + " bytes) : ");
            Utilities.CommDataFrameAppendTxt(tempString + "\n");
            bufferTail = 0;            
        }
    }    
}


