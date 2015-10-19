/*
 * tBootLoader.java
 *
 * Created on February 3, 2014, 4:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

/**
 *
 * @author FKY301079
 */
public class tBootLoader {
    private char[] BLString = new char[80];
    private void buildBLString(char[] pBuffer, int length){        
    }

    private tBootLoader() {};
    public char[] partID = new char[12];
    public char[] bootLoaderVersion = new char[8];
    public int hardwareInfo(){          
        return 1;
    }
}
