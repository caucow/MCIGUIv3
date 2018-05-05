package com.caucraft.mciguiv3.update;

import com.caucraft.mciguiv3.components.TitledSeparator;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.Task;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class LauncherVersionPanel extends JPanel {
    
    private final JButton expandButton;
    private boolean expanded = false;
    private final TitledSeparator versionTitle;
    private final JButton installButton;
    private final JPanel mainPanel;
    
    public LauncherVersionPanel(LauncherVersion ver) {
        this.setLayout(new MigLayout("hidemode 3, nogrid, ins 0, fillx"));
        this.expandButton = new JButton("+");
        this.expandButton.setMargin(new Insets(0, 0, 0, 0));
        this.expandButton.setFocusable(false);
        this.expandButton.setOpaque(false);
        this.expandButton.setForeground(Color.WHITE);
        this.expandButton.setContentAreaFilled(false);
        this.expandButton.setPreferredSize(new Dimension(10, 10));
        this.expandButton.setSize(this.expandButton.getPreferredSize());
        this.expandButton.addActionListener((ActionEvent e) -> {
            toggleExpanded();
        });
        this.versionTitle = new TitledSeparator(ver.getId());
        this.versionTitle.setOpaque(false);
        this.versionTitle.setForeground(Color.WHITE);
        this.versionTitle.setFont(this.versionTitle.getFont().deriveFont(Font.BOLD, 16.0F));
        this.installButton = new JButton("Install");
        this.installButton.setMargin(new Insets(2, 2, 2, 2));
        this.installButton.setOpaque(false);
        this.installButton.setForeground(Color.WHITE);
        this.installButton.setContentAreaFilled(false);
        this.installButton.addActionListener((ActionEvent e) -> {
            installVersion(ver);
        });
        this.mainPanel = new JPanel(new MigLayout("hidemode 3, fillx, ins 0, nogrid"));
        this.mainPanel.setVisible(false);
        this.mainPanel.setOpaque(false);
        
        List<String> subList = ver.getAdded();
        if (subList != null) {
            JButton addExpand = new JButton("-");
            addExpand.setMargin(new Insets(0, 0, 0, 0));
            addExpand.setOpaque(false);
            addExpand.setForeground(Color.WHITE);
            addExpand.setContentAreaFilled(false);
            addExpand.setFocusable(false);
            
            TitledSeparator addTitle = new TitledSeparator("Added");
            addTitle.setOpaque(false);
            addTitle.setForeground(Color.WHITE);
            
            JTextPane addTextPane = new JTextPane();
            addTextPane.setContentType("text/html");
            addTextPane.setEditable(false);
            addTextPane.setMargin(new Insets(0, 0, 0, 0));
            addTextPane.setOpaque(false);
            addTextPane.setForeground(Color.WHITE);
            addTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            boolean newline = false;
            for (String s : subList) {
                if (newline) {
                    html.append("<BR>");
                } else {
                    newline = true;
                }
                html.append("<b>+</b> ");
                html.append(s);
            }
            html.append("</html>");
            addTextPane.setText(html.toString());
            
            AtomicBoolean subExpanded = new AtomicBoolean(true);
            addExpand.addActionListener((ActionEvent e) -> {
                boolean subexp = !subExpanded.get();
                subExpanded.set(subexp);
                addTextPane.setVisible(subexp);
                addExpand.setText(subexp ? "-" : "+");
            });
            
            this.mainPanel.add(addExpand, "gap 16, w 15!, h 15!");
            this.mainPanel.add(addTitle, "growx, wrap");
            this.mainPanel.add(addTextPane, "gap 32, growx, wrap");
        }
        subList = ver.getChanged();
        if (subList != null) {
            JButton changedExpand = new JButton("-");
            changedExpand.setMargin(new Insets(0, 0, 0, 0));
            changedExpand.setOpaque(false);
            changedExpand.setForeground(Color.WHITE);
            changedExpand.setContentAreaFilled(false);
            changedExpand.setFocusable(false);
            
            TitledSeparator changedTitle = new TitledSeparator("Changed");
            changedTitle.setOpaque(false);
            changedTitle.setForeground(Color.WHITE);
            
            JTextPane changedTextPane = new JTextPane();
            changedTextPane.setContentType("text/html");
            changedTextPane.setEditable(false);
            changedTextPane.setMargin(new Insets(0, 0, 0, 0));
            changedTextPane.setOpaque(false);
            changedTextPane.setForeground(Color.WHITE);
            changedTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            boolean newline = false;
            for (String s : subList) {
                if (newline) {
                    html.append("<BR>");
                } else {
                    newline = true;
                }
                html.append("<b>*</b> ");
                html.append(s);
            }
            html.append("</html>");
            changedTextPane.setText(html.toString());
            
            AtomicBoolean subExpanded = new AtomicBoolean(true);
            changedExpand.addActionListener((ActionEvent e) -> {
                boolean subexp = !subExpanded.get();
                subExpanded.set(subexp);
                changedTextPane.setVisible(subexp);
                changedExpand.setText(subexp ? "-" : "+");
            });
            
            this.mainPanel.add(changedExpand, "gap 16, w 15!, h 15!");
            this.mainPanel.add(changedTitle, "growx, wrap");
            this.mainPanel.add(changedTextPane, "gap 32, growx, wrap");
        }
        subList = ver.getRemoved();
        if (subList != null) {
            JButton removedExpand = new JButton("-");
            removedExpand.setMargin(new Insets(0, 0, 0, 0));
            removedExpand.setOpaque(false);
            removedExpand.setForeground(Color.WHITE);
            removedExpand.setContentAreaFilled(false);
            removedExpand.setFocusable(false);
            
            TitledSeparator removedTitle = new TitledSeparator("Removed");
            removedTitle.setOpaque(false);
            removedTitle.setForeground(Color.WHITE);
            
            JTextPane removedTextPane = new JTextPane();
            removedTextPane.setContentType("text/html");
            removedTextPane.setEditable(false);
            removedTextPane.setMargin(new Insets(0, 0, 0, 0));
            removedTextPane.setOpaque(false);
            removedTextPane.setForeground(Color.WHITE);
            removedTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
            
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            boolean newline = false;
            for (String s : subList) {
                if (newline) {
                    html.append("<BR>");
                } else {
                    newline = true;
                }
                html.append("<b>-</b> ");
                html.append(s);
            }
            html.append("</html>");
            removedTextPane.setText(html.toString());
            
            AtomicBoolean subExpanded = new AtomicBoolean(true);
            removedExpand.addActionListener((ActionEvent e) -> {
                boolean subexp = !subExpanded.get();
                subExpanded.set(subexp);
                removedTextPane.setVisible(subexp);
                removedExpand.setText(subexp ? "-" : "+");
            });
            
            this.mainPanel.add(removedExpand, "gap 16, w 15!, h 15!");
            this.mainPanel.add(removedTitle, "growx, wrap");
            this.mainPanel.add(removedTextPane, "gap 32, growx, wrap");
        }
        
        
        this.add(this.expandButton, "w 15!, h 15!");
        this.add(this.versionTitle, "growx");
        this.add(this.installButton, "wrap");
        this.add(this.mainPanel, "growx");
    }
    
    public void toggleExpanded() {
        expanded = !expanded;
        mainPanel.setVisible(expanded);
        expandButton.setText(expanded ? "-" : "+");
    }
    
    private void installVersion(LauncherVersion ver) {
        List<String> notes = ver.getInstallNotes();
        if (notes != null) {
            StringBuilder html = new StringBuilder();
            html.append("<html>");
            boolean newline = false;
            for (String s : notes) {
                if (newline) {
                    html.append("<BR>");
                } else {
                    newline = true;
                }
                html.append(s);
            }
            html.append("</html>");
            JOptionPane.showMessageDialog(this, html.toString(), "Note Before Installing", JOptionPane.PLAIN_MESSAGE);
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to install MCIGUI " + ver.getId() + "?", "", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ver.download();
        }
    }
}
