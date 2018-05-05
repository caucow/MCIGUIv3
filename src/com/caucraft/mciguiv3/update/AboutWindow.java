package com.caucraft.mciguiv3.update;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class AboutWindow extends JFrame {
    
    private final RandomTexturedPanel mainPanel;
    private final JTextPane about;
    private final JTextPane upcoming;
    private final JScrollPane updateScrollPane;
    private final JPanel updatePanel;
    
    public AboutWindow(JFrame parent) {
        super("MCIGUIv3");
        this.setSize(720, 540);
        this.setLocationRelativeTo(parent);
        
        about = new JTextPane();
        about.setMargin(new Insets(0, 0, 0, 0));
        about.setEditable(false);
        about.setContentType("text/html");
        about.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        about.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hle) {
                if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.browse(hle.getURL().toURI());
                    } catch (Exception ex) {
                        Launcher.LOGGER.log(Level.WARNING, "Couldn't follow text hyperlink.", ex);
                    }
                }
            }
        });
        about.setText(
                "<html><center><b>"
                        + "<h1>MCIGUI Version 3</h1>"
                        + "<body><p>"
                        + "A \"\"\"simple\"\"\" Minecraft launcher made by a nerd (caucow) that didn't like the official one.<BR>"
                        + "No I don't html. Don't expect so much from me.<BR>"
                        + "<a href=\"https://github.com/caucow/MCIGUIv3\">MCIGUIv3 on GitHub</a>"
                        + "</p></body>"
                + "</b></center></html>"
        );
        about.setOpaque(false);
        about.setForeground(Color.WHITE);
        upcoming = new JTextPane();
        upcoming.setEditable(false);
        upcoming.setContentType("text/html");
        upcoming.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        upcoming.setOpaque(false);
        upcoming.setForeground(Color.WHITE);
        upcoming.setVisible(false);
        updatePanel = new JPanel(new MigLayout("fill, ins 0, nogrid"));
        updatePanel.setOpaque(false);
        updateScrollPane = new JScrollPane(updatePanel);
        updateScrollPane.setOpaque(false);
        updateScrollPane.setForeground(Color.WHITE);
        updateScrollPane.getViewport().setOpaque(false);
        updateScrollPane.getHorizontalScrollBar().setOpaque(false);
        updateScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        updateScrollPane.getVerticalScrollBar().setOpaque(false);
        updateScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        updateScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        mainPanel = new RandomTexturedPanel(new double[] {
            6,
            2,
            2
        }, new Image[] {
            ImageResources.PLANKS_OAK,
            ImageResources.PLANKS_BIRCH,
            ImageResources.PLANKS_BIG_OAK
        });
        mainPanel.setOverlay(new Color(0, 0, 0, 128));
        mainPanel.setLayout(new MigLayout("fill"));
        mainPanel.add(about, "north, growx");
        mainPanel.add(upcoming, "north, growx");
        mainPanel.add(updateScrollPane, "grow");
        this.setContentPane(mainPanel);
    }
    
    public void setUpdateSuccess(LauncherVersions vers) {
        updatePanel.removeAll();
        TreeSet<LauncherVersion> versionSet = new TreeSet<>(vers.getVersions());
        boolean expand = true;
        for (LauncherVersion ver : versionSet.descendingSet()) {
            LauncherVersionPanel lvp = new LauncherVersionPanel(ver);
            lvp.setOpaque(false);
            updatePanel.add(lvp, "north, growx, wrap");
            if (expand) {
                lvp.toggleExpanded();
                expand = false;
            }
        }
        List<String> upcomingList = vers.getUpcoming();
        if (upcomingList != null) {
            upcoming.setVisible(true);
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            boolean newline = false;
            for (String s : upcomingList) {
                if (newline) {
                    html.append("<BR>");
                } else {
                    newline = true;
                }
                html.append(s);
            }
            html.append("</html>");
            upcoming.setText(html.toString());
        }
        revalidate();
        repaint();
    }
    
    public void setUpdateFail(Throwable t) {
        updatePanel.removeAll();
        JTextPane textPane = new JTextPane(new HTMLDocument());
        textPane.setForeground(Color.WHITE);
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setContentType("text/html");
        textPane.setText("<html><center>Failed to check for updates</center></html>");
        updatePanel.add(textPane, "grow");
        revalidate();
        repaint();
    }
}
