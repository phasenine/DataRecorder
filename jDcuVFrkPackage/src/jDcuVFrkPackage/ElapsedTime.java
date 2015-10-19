/*
 * ElapsedTime.java
 *
 * Created on April 10, 2014, 8:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jDcuVFrkPackage;

import java.util.TimerTask;

/**
 *
 * @author FKY301079
 */
public class ElapsedTime extends TimerTask{
    String elapsedTimeStr = new String();
    int elapsedHours = 0;
    int elapsedMinutes = 0;
    int elapsedSeconds = 0;
    

    public void run(){
        GlobalVars.gElapsedTimeSeconds++;
        elapsedHours = ((int)(GlobalVars.gElapsedTimeSeconds/3600));
        elapsedMinutes = ((int)(GlobalVars.gElapsedTimeSeconds/60)) - (elapsedHours*60);
        elapsedSeconds = GlobalVars.gElapsedTimeSeconds - (elapsedHours*3600) - (elapsedMinutes*60);
        elapsedTimeStr = String.format("%02d:%02d:%02d",elapsedHours,elapsedMinutes,elapsedSeconds);
        GlobalVars.progressFrame.jLabel4.setText(elapsedTimeStr);
        GlobalVars.progressFrame.update(GlobalVars.progressFrame.jLabel4.getGraphics());
    }
        
    /** Creates a new instance of ElapsedTime */
    public ElapsedTime() {
    }
    
}
