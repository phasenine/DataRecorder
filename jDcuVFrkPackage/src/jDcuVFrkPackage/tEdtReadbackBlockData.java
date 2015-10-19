/*
 * tEdtReadbackBlockData.java
 *
 * Created on March 31, 2014, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;


/**
 *
 * @author FKY301079
 */
public class tEdtReadbackBlockData {
    public final int BLK_DATA_FIELD_SIZE = 1024;
    public byte[] block_data = new byte[BLK_DATA_FIELD_SIZE];
    public int numEntries = 0;
        
    /** Creates a new instance of tEdtReadbackBlockData */
    public tEdtReadbackBlockData() {
    }
    
}
