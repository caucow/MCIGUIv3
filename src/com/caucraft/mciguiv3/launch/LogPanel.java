package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author caucow
 */
public class LogPanel extends RandomTexturedPanel {
    
    @SuppressWarnings("NonConstantLogger")
    private Logger logger;
    private Handler logHandler;
    private final JScrollPane logScrollPane;
    private JTextArea logTextArea;
    
    public LogPanel(Logger logger) {
        this(logger, new SimpleFormatter());
    }
    
    public LogPanel(Logger logger, Formatter cons_formatter) {
        super(new double[] {
            200,
            20,
            10,
            5,
            3,
            2,
            1,
            0.5
        }, new Image[] {
            ImageResources.STONE,
            ImageResources.COAL_ORE,
            ImageResources.IRON_ORE,
            ImageResources.REDSTONE_ORE,
            ImageResources.GOLD_ORE,
            ImageResources.LAPIS_ORE,
            ImageResources.DIAMOND_ORE,
            ImageResources.EMERALD_ORE
        });
        
        this.logger = logger;
        this.setLayout(new GridBagLayout());
        this.logTextArea = new JTextArea();
        this.logTextArea.setEditable(false);
        this.logScrollPane = new JScrollPane(this.logTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        this.logScrollPane.setOpaque(false);
        this.logScrollPane.getViewport().setOpaque(false);
        this.logScrollPane.getHorizontalScrollBar().setOpaque(false);
        this.logScrollPane.getVerticalScrollBar().setOpaque(false);
        this.logTextArea.setOpaque(false);
        this.logTextArea.setForeground(Color.WHITE);
        this.logTextArea.setCaretColor(Color.LIGHT_GRAY);
        
        this.add(this.logScrollPane, Launcher.FILL_CONSTRAINTS);
        if (this.logger != null) {
            this.logger.addHandler(this.logHandler = new Handler() {
                
                private final Formatter formatter = cons_formatter;
                
                @Override
                public void publish(LogRecord record) {
                    boolean autoscroll = logTextArea.getCaretPosition() == logTextArea.getDocument().getLength();
                    LogPanel.this.logTextArea.append(formatter.format(record));
                    if (autoscroll)
                        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
                }

                @Override
                public void flush() {}

                @Override
                public void close() throws SecurityException {
                    LogPanel.this.logger.removeHandler(LogPanel.this.logHandler);
                    LogPanel.this.logger = null;
                    LogPanel.this.logHandler = null;
                };
            });
        }
    }
    
    public JTextArea getLogTextArea() {
        return logTextArea;
    }
}
