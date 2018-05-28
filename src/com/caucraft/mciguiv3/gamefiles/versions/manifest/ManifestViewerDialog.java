package com.caucraft.mciguiv3.gamefiles.versions.manifest;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class ManifestViewerDialog extends JDialog {
    
    private final VersionManifest manifest;
    private final RandomTexturedPanel mainPanel;
    private final JScrollPane mainScrollPane;
    private final RandomTexturedPanel versionPanel;
    private final JCheckBox snapsCheckbox;
    private final JCheckBox betaCheckbox;
    private final JCheckBox alphaCheckbox;
    private final JButton installButton;
    private List<ManifestGameVersion> versions;
    
    public ManifestViewerDialog(JFrame parentFrame, VersionManifest manifest) {
        super(parentFrame, "Select versions to (re-)install");
        this.manifest = manifest;
        
        this.mainPanel = new RandomTexturedPanel( // <editor-fold>
                new double[] {
                    1,
                    1
                }, new Image[] {
                    ImageResources.STONE,
                    ImageResources.BEDROCK
                }); // </editor-fold>
        this.versionPanel = new RandomTexturedPanel( // <editor-fold>
                new double[] {
                    4,
                    2,
                    1
                }, new Image[] {
                    ImageResources.STONE,
                    ImageResources.GOLD_ORE,
                    ImageResources.DIAMOND_ORE
                }); // </editor-fold>
        this.versionPanel.setLayout(new MigLayout("fill, flowy, ins 0, gap 0!"));
        this.mainScrollPane = new JScrollPane(versionPanel);
        this.snapsCheckbox = new JCheckBox("Show Snapshots");
        this.betaCheckbox = new JCheckBox("Show Beta Versions");
        this.alphaCheckbox = new JCheckBox("Show Alpha Versions");
        this.installButton = new JButton("Install");
        
        this.mainScrollPane.setOpaque(false);
        this.mainScrollPane.getViewport().setOpaque(false);
        this.mainScrollPane.getHorizontalScrollBar().setOpaque(false);
        this.mainScrollPane.getVerticalScrollBar().setOpaque(false);
        this.mainScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        this.snapsCheckbox.setOpaque(false);
        this.snapsCheckbox.setForeground(Color.WHITE);
        this.betaCheckbox.setOpaque(false);
        this.betaCheckbox.setForeground(Color.WHITE);
        this.alphaCheckbox.setOpaque(false);
        this.alphaCheckbox.setForeground(Color.WHITE);
        
        registerListeners(); 
        
        this.mainPanel.setLayout(new MigLayout("nogrid, flowy, fill"));
        this.mainPanel.add(this.mainScrollPane, "growx");
        this.mainPanel.add(snapsCheckbox, "growx");
        this.mainPanel.add(betaCheckbox, "growx");
        this.mainPanel.add(alphaCheckbox, "growx");
        this.mainPanel.add(installButton, "center");
        
        this.getContentPane().add(mainPanel);
        
        reloadVersions();
        
        this.setSize(300, 400);
        this.setLocationRelativeTo(parentFrame);
        this.setModal(true);
        this.setModalityType(ModalityType.APPLICATION_MODAL);
    }
    
    private void registerListeners() {
        snapsCheckbox.addActionListener((ActionEvent e) -> {
            reloadVersions();
        });
        betaCheckbox.addActionListener((ActionEvent e) -> {
            reloadVersions();
        });
        alphaCheckbox.addActionListener((ActionEvent e) -> {
            reloadVersions();
        });
        installButton.addActionListener((ActionEvent e) -> {
            installButton.setEnabled(false);
            for (Component c : versionPanel.getComponents()) {
                JCheckBox verbox = (JCheckBox)c;
                if (verbox.isSelected()) {
                    versions.add(manifest.getVersion(verbox.getText()));
                }
            }
            this.dispose();
        });
    }
    
    private void reloadVersions() {
        versions = new ArrayList<>();
        versionPanel.removeAll();
        for (ManifestGameVersion mver : new TreeSet<>(manifest.getVersions()).descendingSet()) {
            Color foreground;
            switch (mver.getType()) {
                case "release":
                    foreground = Color.GREEN;
                    break;
                case "snapshot":
                    if (!snapsCheckbox.isSelected()) {
                        continue;
                    }
                    foreground = Color.YELLOW;
                    break;
                case "old_beta":
                    if (!betaCheckbox.isSelected()) {
                        continue;
                    }
                    foreground = Color.ORANGE;
                    break;
                case "old_alpha":
                    if (!alphaCheckbox.isSelected()) {
                        continue;
                    }
                    foreground = Color.RED;
                    break;
                default:
                    Launcher.getLogger().log(Level.WARNING, "Unknown version type: {0}", mver.getType());
                    continue;
            }
            JCheckBox verbox = new JCheckBox(mver.getId());
            verbox.setOpaque(false);
            verbox.setForeground(foreground);
            versionPanel.add(verbox);
        }
        this.revalidate();
        this.repaint();
    }
    
    public List<ManifestGameVersion> getVersions() {
        installButton.setEnabled(true);
        reloadVersions();
        this.setVisible(true);
        return versions;
    }
    
}
