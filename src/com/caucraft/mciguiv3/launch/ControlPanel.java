package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.gamefiles.profiles.AuthenticatedUser;
import com.caucraft.mciguiv3.gamefiles.profiles.LauncherProfiles;
import com.caucraft.mciguiv3.gamefiles.profiles.Profile;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.gamefiles.versions.VersionManager;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.UndoableEditEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class ControlPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    
    private final JPanel accountsPanel;
    private final JButton accountsButton;
    private final JCheckBox playOfflineCheckbox;
    private final JLabel usernameLabel;
    private final JTextField usernameTextField;
    
    private final JPanel logoPanel;
    private BufferedImage logo;
    private final JButton launchButton;
    private final JLabel infoLabel;
    
    public ControlPanel(Launcher launcher) {
        super(
                new double[] {2, 1},
                new Image[] {ImageResources.STONE, ImageResources.BEDROCK}
        );
        
        this.launcher = launcher;
        
        logo = ImageResources.MCIGUI;
        
        this.accountsPanel = new JPanel();
        this.accountsButton = new JButton("Select Account");
        this.playOfflineCheckbox = new JCheckBox("Play Offline");
        this.usernameLabel = new JLabel("Username");
        this.usernameTextField = new JTextField("Player");
        this.accountsPanel.setLayout(new MigLayout("ins 0, gap 0!, nogrid, fillx"));
        this.accountsPanel.setMinimumSize(new Dimension(140, 60));
        this.accountsPanel.add(this.accountsButton, "growx, wrap");
        this.accountsPanel.add(this.playOfflineCheckbox, "growx, wrap");
        this.accountsPanel.add(this.usernameLabel);
        this.accountsPanel.add(this.usernameTextField, "growx");
        
        this.logoPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                double scale = Math.min(
                        (double)(this.getWidth() - 20) / (double)logo.getWidth(),
                        (double)(this.getHeight() - 6) / (double)logo.getHeight()
                );
                g.drawImage(logo,
                        (int)(getWidth() * 0.5 - logo.getWidth() * 0.5 * scale),
                        (int)(getHeight() * 0.5 - logo.getHeight() * 0.5 * scale),
                        (int)(logo.getWidth() * scale),
                        (int)(logo.getHeight() * scale),
                        this);
            }
        };
        this.logoPanel.setMinimumSize(new Dimension(100, 50));
        
        this.launchButton = new JButton("Play");
        this.launchButton.setFont(this.launchButton.getFont().deriveFont(24.0F));
        this.launchButton.setMinimumSize(new Dimension(100, 50));
        
        this.infoLabel = new JLabel("");
        this.setInfoText("[B]ottom Text");
        this.infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.infoLabel.setVerticalAlignment(SwingConstants.CENTER);
        this.infoLabel.setMinimumSize(new Dimension(240, 50));
        
        this.accountsPanel.setOpaque(false);
        this.playOfflineCheckbox.setOpaque(false);
        this.playOfflineCheckbox.setForeground(Color.WHITE);
        this.usernameLabel.setForeground(Color.WHITE);
        this.usernameTextField.setOpaque(false);
        this.usernameTextField.setForeground(Color.WHITE);
        this.usernameTextField.setCaretColor(Color.LIGHT_GRAY);
        this.logoPanel.setOpaque(false);
        this.infoLabel.setForeground(Color.WHITE);
        
        registerListeners();
        
        this.setLayout(new MigLayout("nocache, nogrid, fillx"));
        this.add(this.accountsPanel, "growx, gp 110, w min:pref:200");
        this.add(this.logoPanel, "grow, gp 100");
        this.add(this.launchButton, "growy");
        this.add(this.infoLabel, "growy");
    }
    
    public void setUpdateAvailable() {
        this.logo = ImageResources.MCIGUI_UPDATE;
        this.revalidate();
        this.repaint();
    }
    
    public void reloadAuthDisplay(boolean setUser) {
        LauncherConfig config = launcher.getConfig();
        LauncherProfiles profiles = launcher.getProfiles();
        VersionManager versions = launcher.getVersionManager();
        
        launchButton.setEnabled(false);
        playOfflineCheckbox.setSelected(config.getOfflineMode());
        if (setUser) {
            usernameTextField.setEnabled(false);
            usernameTextField.setText(config.getOfflineName());
        }
        usernameTextField.setEnabled(config.getOfflineMode());
        
        Profile p = profiles.getSelectedProfile();
        GameVersion v = versions.getVersion(p.getLastVersionId());
        if (v == null) {
            setInfoText("Select or install a\nMinecraft version.");
            return;
        }
        AuthenticatedUser user = launcher.getCurrentUser();
        if (user == null) {
            setInfoText("Please log in to play.");
            return;
        }
        setInfoText(String.format("Welcome %s,%nReady to play Minecraft%n%s", user.getDisplayName(), v.getId()));
        launchButton.setEnabled(true);
    }
    
    private void registerListeners() {
        this.accountsButton.addActionListener((ActionEvent e) -> {
            launcher.selectAccount();
        });
        this.playOfflineCheckbox.addActionListener((ActionEvent e) -> {
            launcher.getConfig().setOfflineMode(playOfflineCheckbox.isSelected());
            launcher.reloadAuthDisplay(false);
        });
        this.usernameTextField.getDocument().addUndoableEditListener((UndoableEditEvent e) -> {
            if (!usernameTextField.isEnabled()) {
                return;
            }
            launcher.getConfig().setOfflineName(usernameTextField.getText());
            launcher.reloadAuthDisplay(false);
        });
        this.launchButton.addActionListener((ActionEvent e) -> {
            launcher.launchGame();
        });
        this.logoPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                launcher.showAboutWindow();
            }
        });
    }
    
    private void setInfoText(String text) {
        infoLabel.setText("<html><center>" + text.replace("\n", "<br>") + "");
    }
    
}
