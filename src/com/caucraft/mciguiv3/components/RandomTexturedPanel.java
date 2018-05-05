package com.caucraft.mciguiv3.components;

import com.caucraft.mciguiv3.util.WeightedRandom;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author caucow
 */
public class RandomTexturedPanel extends JPanel {
    
    private WeightedRandom<Image> textureRandomizer;
    private List<List<Image>> textureMatrix;
    private Color overlay;
    
    public RandomTexturedPanel() {}
    
    public RandomTexturedPanel(double[] weights, Image[] textures) {
        if (weights.length != textures.length) {
            throw new IllegalArgumentException("Length of weight array != length of texture array.");
        }
        textureRandomizer = new WeightedRandom<>();
        for (int i = 0; i < weights.length; ++i) {
            if (weights[i] <= 0.0 || textures[i] == null);
            textureRandomizer.put(weights[i], textures[i]);
        }
        textureMatrix = new ArrayList<>();
    }
    
    public void setOverlay(Color c) {
        this.overlay = c;
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (textureRandomizer == null || textureRandomizer.getSize() == 0) {
            super.paintComponent(g);
            return;
        }
        if (textureRandomizer.getSize() == 1) {
            Image tex = getTexture(0, 0);
            int w = this.getWidth();
            int h = this.getHeight();
            for (int y = 0; y < h; y += 64) {
                for (int x = 0; x < w; x += 64) {
                    g.drawImage(tex, x, y, 64, 64, this);
                }
            }
        } else {
            int w = this.getWidth();
            int h = this.getHeight();
            for (int y = 0; y < h; y += 64) {
                for (int x = 0; x < w; x += 64) {
                    g.drawImage(getTexture(x / 64, y / 64), x, y, 64, 64, this);
                }
            }
        }
        if (overlay != null) {
            g.setColor(overlay);
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
    }
    
    private Image getTexture(int x, int y) {
        while (textureMatrix.size() <= y) {
            textureMatrix.add(new ArrayList<>());
        }
        List<Image> row = textureMatrix.get(y);
        while (row.size() <= x) {
            row.add(textureRandomizer.get());
        }
        return row.get(x);
    }
    
}
