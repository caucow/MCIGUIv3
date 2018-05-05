package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Component;
import java.awt.Image;
import javax.swing.JTabbedPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class LauncherPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    private final JTabbedPane mainTabs;
    private final ControlPanel controlPanel;
    
    public LauncherPanel(Launcher launcher) {
        super(
                new double[] {
                    1,
                    1
                }, new Image[] {
                    ImageResources.STONE,
                    ImageResources.DIRT
                }
        );
        this.launcher = launcher;
        
        mainTabs = new JTabbedPane();
        controlPanel = new ControlPanel(launcher);
        
        this.mainTabs.setOpaque(false);
        
        this.setLayout(new MigLayout("nocache, ins 0, gap 0!, fill"));
        this.add(controlPanel, "south, growx, h pref:pref:max");
        this.add(mainTabs, "grow");
    }
    
    /**
     * @return the tab index of the component added
     */
    public int addTab(String name, Component c) {
        mainTabs.addTab(name, c);
        revalidate();
        repaint();
        return mainTabs.getTabCount() - 1;
    }
    
    public void setSelectedTab(int index) {
        mainTabs.setSelectedIndex(index);
    }
    
    public void removeTab(Component c) {
        mainTabs.remove(c);
        revalidate();
        repaint();
    }
    
    public ControlPanel getControlPanel() {
        return controlPanel;
    }
}
