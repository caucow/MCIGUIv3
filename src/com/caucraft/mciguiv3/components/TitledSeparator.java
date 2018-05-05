package com.caucraft.mciguiv3.components;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author caucow
 */
public class TitledSeparator extends JPanel {
    
    private String title;
    
    public TitledSeparator(String title) {
        this.title = title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.revalidate();
    }
    
    public String getTitle() {
        return title;
    }
    
    @Override
    public void validate() {
        super.validate();
        FontMetrics fm = this.getFontMetrics(this.getFont());
        this.setMinimumSize(fm.getStringBounds(this.title, this.getGraphics()).getBounds().getSize());
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (this.isOpaque()) {
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.fillRect(0, 0, 500, 500);
        }
        g.setColor(this.getForeground());
        Font f = this.getFont();
        FontMetrics fm = this.getFontMetrics(f);
        int fontHeight = fm.getAscent() + fm.getDescent();
        g.drawString(this.title, 0, this.getHeight() / 2 + fontHeight / 2 - fm.getDescent());
        int fontWidth = fm.stringWidth(this.title);
        if (fontWidth + 6 < this.getWidth()) {
            g.drawLine(fontWidth + 6, this.getHeight() / 2, this.getWidth(), this.getHeight() / 2);
        }
    }
    
}
