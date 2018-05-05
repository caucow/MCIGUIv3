package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class PastRunViewer extends JFrame {
    
    private final RandomTexturedPanel panel;
    private final JScrollPane runScrollPane;
    private final JTextArea runTextArea;
    
    public PastRunViewer(JFrame parentFrame, Document doc) {
        this.panel = new RandomTexturedPanel(
                new double[] {
                    1,
                    1
                }, new Image[] {
                    ImageResources.STONE,
                    ImageResources.DIRT
                }
        );
        this.runTextArea = new JTextArea(doc);
        this.runScrollPane = new JScrollPane(runTextArea);
        
        this.runTextArea.setOpaque(false);
        this.runTextArea.setForeground(Color.WHITE);
        this.runScrollPane.setOpaque(false);
        this.runScrollPane.getViewport().setOpaque(false);
        this.runScrollPane.getHorizontalScrollBar().setOpaque(false);
        this.runScrollPane.getVerticalScrollBar().setOpaque(false);
        
        this.panel.setLayout(new MigLayout("fill"));
        this.panel.add(this.runScrollPane, "grow");
        
        this.setSize(400, 300);
        this.setLocationRelativeTo(parentFrame);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.getContentPane().add(this.panel);
    }
}
