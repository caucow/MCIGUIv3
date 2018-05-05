package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.components.GoodComboBoxRenderer;
import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class PastRunsPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    private final Map<LaunchInfo, Document> pastRuns;
    private final JLabel sortByLabel;
    private final JComboBox<String> sortByComboBox;
    private final JLabel sortOrderLabel;
    private final JComboBox<String> sortOrderComboBox;
    private final JLabel chooseRunLabel;
    private final JComboBox<LaunchInfo> chooseRunComboBox;
    private final JButton popOutButton;
    private final JScrollPane logScrollPane;
    private final JTextArea logTextArea;
    
    public PastRunsPanel(Launcher launcher) {
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
        
        this.launcher = launcher;
        this.pastRuns = new LinkedHashMap<>();
        this.sortByLabel = new JLabel("Sort By:");
        this.sortByComboBox = new JComboBox<>(new String[] {
            "Start Time",
            "Close Time",
            "Name - Start Time",
            "Name - Close Time"
        });
        this.sortByComboBox.setSelectedItem("Close Time");
        this.sortByComboBox.setEditable(false);
        this.sortOrderLabel = new JLabel("Sort Order:");
        this.sortOrderComboBox = new JComboBox<>(new String[] {
            "Descending",
            "Ascending"
        });
        this.sortOrderComboBox.setSelectedItem("Descending");
        this.sortOrderComboBox.setEditable(false);
        this.chooseRunLabel = new JLabel("Show Log:");
        this.chooseRunComboBox = new JComboBox<>();
        this.chooseRunComboBox.setEditable(false);
        this.chooseRunComboBox.setEnabled(false);
        this.chooseRunComboBox.setMinimumSize(new Dimension(50, 20));
        this.chooseRunComboBox.setMaximumSize(new Dimension(300, 50));
        this.popOutButton = new JButton("Pop Out");
        this.popOutButton.setEnabled(false);
        this.logTextArea = new JTextArea();
        this.logTextArea.setEditable(false);
        this.logScrollPane = new JScrollPane(logTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        this.sortByLabel.setOpaque(false);
        this.sortByLabel.setForeground(Color.WHITE);
        this.sortByComboBox.setForeground(Color.WHITE);
        this.sortByComboBox.setBackground(Color.DARK_GRAY);
        GoodComboBoxRenderer goodRenderer = new GoodComboBoxRenderer(sortByComboBox, Color.WHITE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY);
        goodRenderer.setOpaque(false);
        this.sortByComboBox.setRenderer(goodRenderer);
        this.sortOrderLabel.setOpaque(false);
        this.sortOrderLabel.setForeground(Color.WHITE);
        this.sortOrderComboBox.setForeground(Color.WHITE);
        this.sortOrderComboBox.setBackground(Color.DARK_GRAY);
        goodRenderer = new GoodComboBoxRenderer(sortOrderComboBox, Color.WHITE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY);
        goodRenderer.setOpaque(false);
        this.sortOrderComboBox.setRenderer(goodRenderer);
        this.chooseRunLabel.setOpaque(false);
        this.chooseRunLabel.setForeground(Color.WHITE);
        this.chooseRunComboBox.setForeground(Color.WHITE);
        this.chooseRunComboBox.setBackground(Color.DARK_GRAY);
        goodRenderer = new GoodComboBoxRenderer(chooseRunComboBox, Color.WHITE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY);
        goodRenderer.setOpaque(false);
        this.chooseRunComboBox.setRenderer(goodRenderer);
        this.logScrollPane.setOpaque(false);
        this.logScrollPane.getViewport().setOpaque(false);
        this.logScrollPane.getHorizontalScrollBar().setOpaque(false);
        this.logScrollPane.getVerticalScrollBar().setOpaque(false);
        this.logTextArea.setOpaque(false);
        this.logTextArea.setForeground(Color.WHITE);
        
        JPanel shouldBeUnnecessary = new JPanel();
        shouldBeUnnecessary.setLayout(new MigLayout("nogrid, ins 0, fillx"));
        shouldBeUnnecessary.setOpaque(false);
        shouldBeUnnecessary.add(sortByLabel, "ax center");
        shouldBeUnnecessary.add(sortByComboBox);
        shouldBeUnnecessary.add(sortOrderLabel);
        shouldBeUnnecessary.add(sortOrderComboBox);
        shouldBeUnnecessary.add(chooseRunLabel);
        shouldBeUnnecessary.add(chooseRunComboBox, "growx");
        shouldBeUnnecessary.add(popOutButton);
        this.setLayout(new MigLayout("nogrid, flowy, fill"));
        this.add(shouldBeUnnecessary, "growx");
        this.add(logScrollPane, "grow");
//        this.add(sortByLabel, "ax center");
//        this.add(sortByComboBox);
//        this.add(sortOrderLabel);
//        this.add(sortOrderComboBox);
//        this.add(chooseRunLabel);
//        this.add(chooseRunComboBox, "growx");
//        this.add(popOutButton);
//        this.setLayout(new MigLayout("nogrid, flowy, fill"));
//        this.add(shouldBeUnnecessary, "growx");
//        this.add(logScrollPane, "grow");
        
        reloadRuns();
        
        registerListeners();
    }
    
    private void registerListeners() {
        sortByComboBox.addItemListener((ItemEvent e) -> {
            reloadRuns();
        });
        sortOrderComboBox.addItemListener((ItemEvent e) -> {
            reloadRuns();
        });
        chooseRunComboBox.addItemListener((ItemEvent e) -> {
            if (chooseRunComboBox.getSelectedItem() != null) {
                logTextArea.setDocument(pastRuns.get((LaunchInfo)chooseRunComboBox.getSelectedItem()));
            }
        });
        popOutButton.addActionListener((ActionEvent e) -> {
            PastRunViewer viewer = new PastRunViewer(launcher.getMainWindow(), pastRuns.get((LaunchInfo)chooseRunComboBox.getSelectedItem()));
            viewer.setVisible(true);
        });
    }
    
    public void addRun(LaunchInfo info, Document logs) {
        pastRuns.put(info, logs);
        reloadRuns();
        chooseRunComboBox.setEnabled(true);
        popOutButton.setEnabled(true);
        chooseRunComboBox.setSelectedItem(info);
        logTextArea.setDocument(logs);
        revalidate();
        repaint();
    }
    
    private void reloadRuns() {
        int multiplier = sortOrderComboBox.getSelectedItem().equals("Descending") ? -1 : 1;
        Comparator<LaunchInfo> comp = null;
        LaunchInfo selected = (LaunchInfo)chooseRunComboBox.getSelectedItem();
        switch (sortByComboBox.getSelectedItem().toString()) {
            case "Start Time":
                comp = (LaunchInfo o1, LaunchInfo o2) -> {
                    int val = o1.getStartTime().compareTo(o2.getStartTime());
                    if (val == 0) {
                        val = 1;
                    }
                    return val * multiplier;
                };
                break;
            case "Name - Start Time":
                comp = (LaunchInfo o1, LaunchInfo o2) -> {
                    int val = o1.getName().compareTo(o2.getName());
                    if (val == 0) {
                        val = o1.getStartTime().compareTo(o2.getStartTime());
                    }
                    if (val == 0) {
                        val = 1;
                    }
                    return val * multiplier;
                };
                break;
            case "Name - Close Time":
                comp = (LaunchInfo o1, LaunchInfo o2) -> {
                    int val = o1.getName().compareTo(o2.getName());
                    if (val == 0) {
                        val = o1.getCloseTime().compareTo(o2.getCloseTime());
                    }
                    if (val == 0) {
                        val = 1;
                    }
                    return val * multiplier;
                };
                break;
            default:
                comp = (LaunchInfo o1, LaunchInfo o2) -> {
                    int val = o1.getCloseTime().compareTo(o2.getCloseTime());
                    if (val == 0) {
                        val = 1;
                    }
                    return val * multiplier;
                };
        }
        TreeSet<LaunchInfo> sortset = new TreeSet<>(comp);
        sortset.addAll(pastRuns.keySet());
        chooseRunComboBox.setModel(new DefaultComboBoxModel<>(new Vector<>(sortset)));
        chooseRunComboBox.setSelectedItem(selected);
        revalidate();
        repaint();
    }
}
