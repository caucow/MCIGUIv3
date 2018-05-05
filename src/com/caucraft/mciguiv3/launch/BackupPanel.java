package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Image;
import javax.swing.JPanel;

/**
 *
 * @author caucow
 */
public class BackupPanel extends RandomTexturedPanel {
    
    public BackupPanel() {
        super(new double[] {1}, new Image[] {ImageResources.STONE});
    }
    
}
