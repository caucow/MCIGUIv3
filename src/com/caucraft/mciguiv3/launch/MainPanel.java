package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.components.GoodComboBoxRenderer;
import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.components.TitledSeparator;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.gamefiles.profiles.LauncherProfiles;
import com.caucraft.mciguiv3.gamefiles.profiles.Profile;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class MainPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    
    private final TitledSeparator profilesSeparator;
    private final JPanel profilePanel;
    private final JScrollPane profileScrollPane;
    private ButtonGroup profileButtonGroup;
    
    private final TitledSeparator versionSeparator;
    private final JPanel versionContainerPanel;
    private final JPanel versionPanel;
    private final JScrollPane versionScrollPane;
    private final JButton versionInstallButton;
    private ButtonGroup versionButtonGroup;
    
    private final JPanel globalSettingsPanel;
    private final JLabel mcHomeLabel;
    private final JTextField mcHomeTextField;
    private final JButton mcHomeReloadButton;
    private final JButton mcHomeBrowseButton;
    private final JLabel javaExeLabel;
    private final JTextField javaExeTextField;
    private final JButton javaExeBrowseButton;
    private final JButton addProfileButton;
    private final JButton renameProfileButton;
    private final JButton saveProfilesButton;
    private final JButton deleteProfileButton;
    
    private final JPanel profileSettingsPanel;
    private final JCheckBox gameDirCheckbox;
    private final JTextField gameDirTextField;
    private final JCheckBox resolutionCheckbox;
    private final JTextField widthTextField;
    private final JLabel resXLabel;
    private final JTextField heightTextField;
    private final JComboBox<String> commonResComboBox;
    private final JCheckBox snapshotsCheckbox;
    private final JCheckBox betaCheckbox;
    private final JCheckBox alphaCheckbox;
    private final JCheckBox jvmArgsCheckbox;
    private final JTextField jvmArgsTextField;
    
    private ActionListener profileListener;
    private ActionListener versionListener;
    
    public MainPanel(Launcher launcher) {
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
        
        this.profilesSeparator = new TitledSeparator("Profiles");
        this.profilePanel = new JPanel();
        this.profileScrollPane = new JScrollPane();
        this.profileScrollPane.setViewportView(this.profilePanel);
        this.profileScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.profileScrollPane.setMinimumSize(new Dimension(100, 100));
        MigLayout profileLayout = new MigLayout("ins 0, gap 0!, flowy", "[fill]", "[min!]");
        this.profilePanel.setLayout(profileLayout);
        
        this.versionSeparator = new TitledSeparator("Version");
        this.versionContainerPanel = new JPanel();
        this.versionPanel = new JPanel();
        this.versionScrollPane = new JScrollPane();
        this.versionScrollPane.setViewportView(this.versionPanel);
        this.versionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.versionScrollPane.setMinimumSize(new Dimension(100, 100));
        this.versionInstallButton = new JButton("Install New");
        this.versionInstallButton.setMargin(new Insets(0, 0, 0, 0));
        MigLayout versionContainerLayout = new MigLayout("ins 0, gap 0!, flowy", "[fill]", "[][grow, fill][]");
        this.versionContainerPanel.setLayout(versionContainerLayout);
        this.versionContainerPanel.add(this.versionSeparator);
        this.versionContainerPanel.add(this.versionScrollPane, "gapy 3");
        this.versionContainerPanel.add(this.versionInstallButton);
        MigLayout versionLayout = new MigLayout("ins 0, gap 0!, flowy", "[fill]", "[min!]");
        this.versionPanel.setLayout(versionLayout);
        
        this.globalSettingsPanel = new RandomTexturedPanel(
                new double[] {
                    5,
                    5,
                    2,
                    1,
                }, new Image[] {
                    ImageResources.DIRT,
                    ImageResources.STONE,
                    ImageResources.COAL_ORE,
                    ImageResources.STONE
                }
        );
        this.mcHomeLabel = new JLabel("MCHome (.minecraft)");
        this.mcHomeTextField = new JTextField();
        this.mcHomeTextField.setEditable(false);
        this.mcHomeReloadButton = new JButton("Reload");
        this.mcHomeBrowseButton = new JButton("Browse");
        this.javaExeLabel = new JLabel("Java Location");
        this.javaExeTextField = new JTextField();
        this.javaExeTextField.setEditable(false);
        this.javaExeBrowseButton = new JButton("Browse");
        this.addProfileButton = new JButton("Add Profile");
        this.renameProfileButton = new JButton("Rename Profile");
        this.saveProfilesButton = new JButton("Save Profiles");
        this.deleteProfileButton = new JButton("Delete Profile");
        MigLayout globalSettingsLayout = new MigLayout("nogrid, fillx");
        this.globalSettingsPanel.setLayout(globalSettingsLayout);
        this.globalSettingsPanel.add(this.mcHomeLabel, "sg labels");
        this.globalSettingsPanel.add(this.mcHomeTextField, "growx");
        this.globalSettingsPanel.add(this.mcHomeReloadButton, "sg browsebtns");
        this.globalSettingsPanel.add(this.mcHomeBrowseButton, "wrap, sg browsebtns");
        this.globalSettingsPanel.add(this.javaExeLabel, "sg labels");
        this.globalSettingsPanel.add(this.javaExeTextField, "growx");
        this.globalSettingsPanel.add(this.javaExeBrowseButton, "wrap, sg browsebtns");
        this.globalSettingsPanel.add(this.profilesSeparator, "growx, gapy 15, wrap");
        this.globalSettingsPanel.add(this.saveProfilesButton);
        this.globalSettingsPanel.add(this.addProfileButton, "gap push");
        this.globalSettingsPanel.add(this.renameProfileButton);
        this.globalSettingsPanel.add(this.deleteProfileButton);
        
        this.profileSettingsPanel = new RandomTexturedPanel(
                new double[] {
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
                }
        );
        this.gameDirCheckbox = new JCheckBox("Game Directory");
        this.gameDirTextField = new JTextField();
        this.resolutionCheckbox = new JCheckBox("Resolution");
        this.widthTextField = new JTextField();
        this.widthTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField tf = (JTextField) input;
                if (tf.getText().matches("\\d{1,5}")) {
                    return true;
                }
                String[] dims = commonResComboBox.getSelectedItem().toString().split("\\D+");
                if (dims.length > 1) {
                    tf.setText(dims[0]);
                } else {
                    tf.setText("854");
                }
                return true;
            }
        });
        this.resXLabel = new JLabel("x");
        this.heightTextField = new JTextField();
        this.heightTextField.setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                JTextField tf = (JTextField) input;
                if (tf.getText().matches("\\d{1,5}")) {
                    return true;
                }
                String[] dims = commonResComboBox.getSelectedItem().toString().split("\\D+");
                if (dims.length > 1) {
                    tf.setText(dims[1]);
                } else {
                    tf.setText("480");
                }
                return true;
            }
        });
        this.commonResComboBox = new JComboBox<>(new String[] {
            "Custom",
            "1920 x 1080 (16:9)",
            "1440 x 1080 (4:3)",
            "1280 x 960 (4:3)",
            "1600 x 900 (16:9)",
            "1366 x 768 (16:9)",
            "1024 x 768 (4:3)",
            "1280 x 720 (16:9)",
            "960 x 720 (4:3)",
            "800 x 600 (4:3)",
            "1024 x 576 (16:9)",
            "854 x 480 (16:9 Def.)",
            "640 x 480 (4:3)",
            "512 x 384 (4:3)",
            "640 x 360 (16:9)",
            "480 x 360 (4:3)",
            "426 x 240 (16:9)",
            "320 x 240 (4:3)",
            "342 x 192 (16:9)",
            "256 x 192 (4:3)",
        });
        this.commonResComboBox.setEditable(false);
        this.commonResComboBox.setSelectedItem("854 x 480 (16:9 Def.)");
        this.snapshotsCheckbox = new JCheckBox("Allow Snapshots");
        this.betaCheckbox = new JCheckBox("Allow Old Beta");
        this.alphaCheckbox = new JCheckBox("Allow Old Alpha");
        this.jvmArgsCheckbox = new JCheckBox("JVM Arguments");
        this.jvmArgsTextField = new JTextField();
        MigLayout profileSettingsLayout = new MigLayout("nogrid, fillx");
        this.profileSettingsPanel.setLayout(profileSettingsLayout);
        this.profileSettingsPanel.add(this.gameDirCheckbox, "sg check");
        this.profileSettingsPanel.add(this.gameDirTextField, "growx, wrap");
        this.profileSettingsPanel.add(this.resolutionCheckbox, "sg check");
        this.profileSettingsPanel.add(this.widthTextField, "gp 30, growx, sg restext, wmax 75");
        this.profileSettingsPanel.add(this.resXLabel);
        this.profileSettingsPanel.add(this.heightTextField, "gp 30, growx, sg restext, wmax 75");
        this.profileSettingsPanel.add(this.commonResComboBox, "gp 20, growx, wrap");
        this.profileSettingsPanel.add(this.jvmArgsCheckbox, "sg check");
        this.profileSettingsPanel.add(this.jvmArgsTextField, "growx, wrap");
        this.profileSettingsPanel.add(this.snapshotsCheckbox, "sg checkver, align 50%, gapy 15");
        this.profileSettingsPanel.add(this.betaCheckbox, "sg checkver");
        this.profileSettingsPanel.add(this.alphaCheckbox, "sg checkver");
        
        profilesSeparator.setForeground(Color.WHITE);
        profilesSeparator.setOpaque(false);
        profilePanel.setOpaque(false);
        profileScrollPane.setOpaque(false);
        profileScrollPane.getViewport().setOpaque(false);
        profileScrollPane.getHorizontalScrollBar().setOpaque(false);
        profileScrollPane.getVerticalScrollBar().setOpaque(false);
        profileScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        versionSeparator.setForeground(Color.WHITE);
        versionSeparator.setOpaque(false);
        versionContainerPanel.setOpaque(false);
        versionPanel.setOpaque(false);
        versionScrollPane.setOpaque(false);
        versionScrollPane.getViewport().setOpaque(false);
        versionScrollPane.getHorizontalScrollBar().setOpaque(false);
        versionScrollPane.getVerticalScrollBar().setOpaque(false);
        versionScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        globalSettingsPanel.setOpaque(false);
        mcHomeLabel.setForeground(Color.WHITE);
        mcHomeTextField.setOpaque(false);
        mcHomeTextField.setForeground(Color.WHITE);
        mcHomeTextField.setCaretColor(Color.LIGHT_GRAY);
        javaExeLabel.setForeground(Color.WHITE);
        javaExeTextField.setOpaque(false);
        javaExeTextField.setForeground(Color.WHITE);
        javaExeTextField.setCaretColor(Color.LIGHT_GRAY);
        profileSettingsPanel.setOpaque(false);
        gameDirCheckbox.setForeground(Color.WHITE);
        gameDirCheckbox.setOpaque(false);
        gameDirTextField.setOpaque(false);
        gameDirTextField.setForeground(Color.WHITE);
        gameDirTextField.setCaretColor(Color.LIGHT_GRAY);
        resolutionCheckbox.setForeground(Color.WHITE);
        resolutionCheckbox.setOpaque(false);
        widthTextField.setOpaque(false);
        widthTextField.setForeground(Color.WHITE);
        widthTextField.setCaretColor(Color.LIGHT_GRAY);
        resXLabel.setForeground(Color.WHITE);
        heightTextField.setOpaque(false);
        heightTextField.setForeground(Color.WHITE);
        heightTextField.setCaretColor(Color.LIGHT_GRAY);
        commonResComboBox.setOpaque(false);
        commonResComboBox.setForeground(Color.WHITE);
        commonResComboBox.setBackground(Color.DARK_GRAY);
        GoodComboBoxRenderer goodRenderer = new GoodComboBoxRenderer(commonResComboBox, Color.WHITE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY);
        goodRenderer.setOpaque(false);
        commonResComboBox.setRenderer(goodRenderer);
        snapshotsCheckbox.setForeground(Color.WHITE);
        snapshotsCheckbox.setOpaque(false);
        betaCheckbox.setForeground(Color.WHITE);
        betaCheckbox.setOpaque(false);
        alphaCheckbox.setForeground(Color.WHITE);
        alphaCheckbox.setOpaque(false);
        jvmArgsCheckbox.setForeground(Color.WHITE);
        jvmArgsCheckbox.setOpaque(false);
        jvmArgsTextField.setOpaque(false);
        jvmArgsTextField.setForeground(Color.WHITE);
        jvmArgsTextField.setCaretColor(Color.LIGHT_GRAY);
        
        disableAll();
        
        MigLayout mainLayout = new MigLayout("fill, nogrid, nocache");
        this.setLayout(mainLayout);
        this.setMinimumSize(new Dimension(600, 300));
        this.add(this.globalSettingsPanel, "north");
        this.add(this.profileScrollPane, "west, growy, w pref!");
        this.add(this.versionContainerPanel, "east, growy, w pref!");
        this.add(this.profileSettingsPanel, "center, grow");
        
        this.registerListeners();
    }
    
    private void disableAll() {
        mcHomeReloadButton.setEnabled(false);
        mcHomeBrowseButton.setEnabled(false);
        javaExeBrowseButton.setEnabled(false);
        saveProfilesButton.setEnabled(false);
        addProfileButton.setEnabled(false);
        renameProfileButton.setEnabled(false);
        deleteProfileButton.setEnabled(false);
        
        gameDirCheckbox.setEnabled(false);
        gameDirTextField.setEnabled(false);
        resolutionCheckbox.setEnabled(false);
        widthTextField.setEnabled(false);
        heightTextField.setEnabled(false);
        commonResComboBox.setEnabled(false);
        jvmArgsCheckbox.setEnabled(false);
        jvmArgsTextField.setEnabled(false);
        snapshotsCheckbox.setEnabled(false);
        betaCheckbox.setEnabled(false);
        alphaCheckbox.setEnabled(false);
    }
    
    public void loadProfiles() {
        mcHomeReloadButton.setEnabled(false);
        mcHomeBrowseButton.setEnabled(false);
        javaExeBrowseButton.setEnabled(false);
        saveProfilesButton.setEnabled(false);
        addProfileButton.setEnabled(false);
        renameProfileButton.setEnabled(false);
        deleteProfileButton.setEnabled(false);
        
        // Global Config UI
        LauncherProfiles profiles = launcher.getProfiles();
        LauncherConfig config = launcher.getConfig();
        mcHomeTextField.setText(config.getMcHome().getPath());
        javaExeTextField.setText(config.getJavaLoc().getPath());
        
        // Profiles
        Profile selected = profiles.getSelectedProfile();
        Collection<Profile> profileSet = profiles.getProfileManager().getProfiles();
        profileButtonGroup = new ButtonGroup();
        profilePanel.removeAll();
        for (Profile prof : profileSet) {
            JCheckBox profRb = new JCheckBox(prof.getName());
            profRb.setForeground(Color.WHITE);
            profRb.setOpaque(false);
            profileButtonGroup.add(profRb);
            profilePanel.add(profRb);
            if (prof.getName().equals(selected.getName())) {
                profRb.setSelected(true);
            }
            profRb.addActionListener(getProfileListener());
        }
        
        loadProfile();
        
        this.revalidate();
        this.repaint();
        
        mcHomeTextField.setEnabled(true);
        mcHomeReloadButton.setEnabled(true);
        mcHomeBrowseButton.setEnabled(true);
        javaExeTextField.setEnabled(true);
        javaExeBrowseButton.setEnabled(true);
        
        saveProfilesButton.setEnabled(true);
        addProfileButton.setEnabled(true);
        renameProfileButton.setEnabled(true);
        deleteProfileButton.setEnabled(true);
    }
    
    private ActionListener getProfileListener() {
        if (profileListener != null) {
            return profileListener;
        }
        profileListener = (ActionEvent e) -> {
            launcher.getProfiles().setSelectedProfile(
                    launcher.getProfiles().getProfileManager().getProfile(
                            ((JCheckBox)e.getSource()).getText()
                    )
            );
            loadProfile();
        };
        return profileListener;
    }
    
    public void loadProfile() {
        gameDirCheckbox.setEnabled(false);
        gameDirCheckbox.setSelected(false);
        gameDirTextField.setEnabled(false);
        gameDirTextField.setText("");
        resolutionCheckbox.setEnabled(false);
        resolutionCheckbox.setSelected(false);
        widthTextField.setEnabled(false);
        widthTextField.setText("");
        heightTextField.setEnabled(false);
        heightTextField.setText("");
        commonResComboBox.setEnabled(false);
        commonResComboBox.setSelectedItem("Custom");
        jvmArgsCheckbox.setEnabled(false);
        jvmArgsCheckbox.setSelected(false);
        jvmArgsTextField.setEnabled(false);
        snapshotsCheckbox.setEnabled(false);
        snapshotsCheckbox.setSelected(false);
        betaCheckbox.setEnabled(false);
        betaCheckbox.setSelected(false);
        alphaCheckbox.setEnabled(false);
        alphaCheckbox.setSelected(false);
        
        Profile prof = launcher.getProfiles().getSelectedProfile();
        
        File gameDir = prof.getGameDir();
        if (gameDir != null) {
            gameDirCheckbox.setSelected(true);
            gameDirTextField.setText(gameDir.getPath());
        }
        Dimension res = prof.getResolution();
        if (res != null) {
            resolutionCheckbox.setSelected(true);
            widthTextField.setText(res.width + "");
            heightTextField.setText(res.height + "");
            for (int i = commonResComboBox.getItemCount() - 1; i >= 0; i--) {
                if (commonResComboBox.getItemAt(i).startsWith(res.width + " x " + res.height)) {
                    commonResComboBox.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            commonResComboBox.setSelectedItem("854 x 480 (16:9 Def.)");
        }
        String jvmArgs = prof.getJavaArgs();
        if (jvmArgs != null) {
            jvmArgsCheckbox.setSelected(true);
            jvmArgsTextField.setText(jvmArgs);
        }
        Set<String> allowedReleaseTypes = prof.getAllowedReleaseTypes();
        if (allowedReleaseTypes != null) {
            snapshotsCheckbox.setSelected(allowedReleaseTypes.contains("snapshot"));
            betaCheckbox.setSelected(allowedReleaseTypes.contains("old_beta"));
            alphaCheckbox.setSelected(allowedReleaseTypes.contains("old_alpha"));
        }
        
        loadVersions();
        
        gameDirCheckbox.setEnabled(true);
        gameDirTextField.setEnabled(gameDirCheckbox.isSelected());
        
        resolutionCheckbox.setEnabled(true);
        widthTextField.setEnabled(resolutionCheckbox.isSelected());
        heightTextField.setEnabled(resolutionCheckbox.isSelected());
        commonResComboBox.setEnabled(resolutionCheckbox.isSelected());
        
        jvmArgsCheckbox.setEnabled(true);
        jvmArgsTextField.setEnabled(jvmArgsCheckbox.isSelected());
        
        snapshotsCheckbox.setEnabled(true);
        betaCheckbox.setEnabled(true);
        alphaCheckbox.setEnabled(true);
        
        launcher.reloadAuthDisplay(false);
    }
    
    public void loadVersions() {
        // TODO add versions
        // make versionmanager in Launcher.java
        Profile p = launcher.getProfiles().getSelectedProfile();
        Set<String> allowedReleaseTypes = p.getAllowedReleaseTypes();
        String selectedVersion = p.getLastVersionId();
        if (selectedVersion == null) {
            selectedVersion = "";
        }
        Collection<GameVersion> vers = launcher.getVersionManager().getVersions();
        versionButtonGroup = new ButtonGroup();
        versionPanel.removeAll();
        for (GameVersion v : vers) {
            if (!"release".equals(v.getType()) && (allowedReleaseTypes == null || !allowedReleaseTypes.contains(v.getType()))) {
                continue;
            }
            JCheckBox verRb = new JCheckBox(v.getId());
            verRb.setForeground(Color.WHITE);
            verRb.setOpaque(false);
            versionButtonGroup.add(verRb);
            versionPanel.add(verRb);
            if (selectedVersion.equals(v.getId())) {
                verRb.setSelected(true);
            }
            verRb.addActionListener(getVersionListener());
        }
        
        this.revalidate();
        this.repaint();
    }
    
    private ActionListener getVersionListener() {
        if (versionListener != null) {
            return versionListener;
        }
        versionListener = (ActionEvent e) -> {
            launcher.getProfiles().getSelectedProfile().setLastVersionId(
                    ((JCheckBox)e.getSource()).getText()
            );
            launcher.reloadAuthDisplay(false);
        };
        return versionListener;
    }
    
    private void registerListeners() {
        mcHomeReloadButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            disableAll();
            launcher.reloadMcHome();
        }); // </editor-fold>
        mcHomeBrowseButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            disableAll();
            File f = launcher.chooseDirectory("Select Minecraft Home (.minecraft)");
            if (f != null) {
                launcher.setMcHome(f);
            }
            launcher.reloadMcHome();
        }); // </editor-fold>
        javaExeBrowseButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            File f = launcher.chooseFile("Select Java Executable");
            if (f != null) {
                launcher.setJavaExe(f);
            }
        }); // </editor-fold>
        saveProfilesButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            launcher.getProfiles().save(true);
        }); // </editor-fold>
        addProfileButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            String name1 = JOptionPane.showInputDialog(
                    MainPanel.this,
                    "Choose a new profile name.",
                    "New Profile");
            Profile p = launcher.getProfiles().getProfileManager().getProfile(name1);
            if (p != null) {
                JOptionPane.showMessageDialog(MainPanel.this, "A profile named \"" + name1 + "\" already exists.");
                return;
            }
            p = new Profile(name1);
            p.getAllowedReleaseTypes().add("release");
            launcher.getProfiles().setSelectedProfile(p);
            loadProfiles();
        }); // </editor-fold>
        renameProfileButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            Profile p = launcher.getProfiles().getSelectedProfile();
            launcher.getProfiles().getProfileManager().removeProfile(p);
            String name1 = JOptionPane.showInputDialog(
                    MainPanel.this,
                    "Choose a new profile name.",
                    "New Profile");
            Profile other = launcher.getProfiles().getProfileManager().getProfile(name1);
            if (other != null) {
                JOptionPane.showMessageDialog(MainPanel.this, "A profile named \"" + name1 + "\" already exists.");
                launcher.getProfiles().setSelectedProfile(p);
                return;
            }
            p.setName(name1);
            launcher.getProfiles().setSelectedProfile(p);
            loadProfiles();
        }); // </editor-fold>
        deleteProfileButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            launcher.getProfiles().getProfileManager().removeProfile(
                    launcher.getProfiles().getSelectedProfile()
            );
            loadProfiles();
        }); // </editor-fold>
        versionInstallButton.addActionListener((ActionEvent e) -> { // <editor-fold>
            launcher.installVersions();
        }); // </editor-fold>
        gameDirCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            gameDirTextField.setEnabled(gameDirCheckbox.isSelected());
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (gameDirCheckbox.isSelected()) {
                p.setGameDir(new File(gameDirTextField.getText()));
            } else {
                p.setGameDir(null);
            }
        }); // </editor-fold>
        gameDirTextField.getDocument().addUndoableEditListener((UndoableEditEvent e) -> { // <editor-fold>
            if (!gameDirTextField.isEnabled()) {
                return;
            }
            launcher.getProfiles().getSelectedProfile()
                    .setGameDir(new File(gameDirTextField.getText()));
        }); // </editor-fold>
        resolutionCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            widthTextField.setEnabled(false);
            heightTextField.setEnabled(false);
            commonResComboBox.setEnabled(false);
            
            String[] dims = commonResComboBox.getSelectedItem().toString().split("\\D+");
            if (dims.length > 1) {
                if (widthTextField.getText().isEmpty()) {
                    widthTextField.setText(dims[0]);
                }
                if (heightTextField.getText().isEmpty()) {
                    heightTextField.setText(dims[1]);
                }
            } else {
                if (widthTextField.getText().isEmpty()) {
                    widthTextField.setText("854");
                }
                if (heightTextField.getText().isEmpty()) {
                    heightTextField.setText("480");
                }
            }
            
            widthTextField.setEnabled(resolutionCheckbox.isSelected());
            heightTextField.setEnabled(resolutionCheckbox.isSelected());
            commonResComboBox.setEnabled(resolutionCheckbox.isSelected());
            
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (resolutionCheckbox.isSelected()) {
                p.setResolution(new Dimension(
                        Integer.parseInt(widthTextField.getText()),
                        Integer.parseInt(heightTextField.getText())));
            } else {
                p.setResolution(null);
            }
        }); // </editor-fold>
        widthTextField.getDocument().addUndoableEditListener((UndoableEditEvent e) -> { // <editor-fold>
            if (!widthTextField.isEnabled()) {
                return;
            }
            if (!widthTextField.getText().matches("\\d{0,5}")) {
                e.getEdit().undo();
            }
            for (int i = commonResComboBox.getItemCount() - 1; i >= 0; --i) {
                if (commonResComboBox.getItemAt(i).startsWith(
                        widthTextField.getText()
                                + " x "
                                + heightTextField.getText()
                                + " ")) {
                    commonResComboBox.setEnabled(false);
                    commonResComboBox.setSelectedIndex(i);
                    commonResComboBox.setEnabled(true);
                    break;
                }
            }
            if (!widthTextField.getText().isEmpty()) {
                launcher.getProfiles()
                        .getSelectedProfile()
                        .setResolution(new Dimension(
                                Integer.parseInt(widthTextField.getText()),
                                Integer.parseInt(heightTextField.getText())));
            }
        }); // </editor-fold>
        heightTextField.getDocument().addUndoableEditListener((UndoableEditEvent e) -> { // <editor-fold>
            if (!heightTextField.isEnabled()) {
                return;
            }
            if (!heightTextField.getText().matches("\\d{0,5}")) {
                e.getEdit().undo();
            }
            for (int i = commonResComboBox.getItemCount() - 1; i >= 0; --i) {
                if (commonResComboBox.getItemAt(i).startsWith(
                        widthTextField.getText()
                                + " x "
                                + heightTextField.getText()
                                + " ")) {
                    commonResComboBox.setEnabled(false);
                    commonResComboBox.setSelectedIndex(i);
                    commonResComboBox.setEnabled(true);
                    break;
                }
            }
            if (!heightTextField.getText().isEmpty()) {
                launcher.getProfiles()
                        .getSelectedProfile()
                        .setResolution(new Dimension(
                                Integer.parseInt(widthTextField.getText()),
                                Integer.parseInt(heightTextField.getText())));
            }
        }); // </editor-fold>
        commonResComboBox.addItemListener((ItemEvent e) -> { // <editor-fold>
            if (!commonResComboBox.isEnabled()) {
                return;
            }
            String[] dims = commonResComboBox.getSelectedItem().toString().split("\\D+");
            if (dims.length > 1) {
                widthTextField.setEnabled(false);
                heightTextField.setEnabled(false);
                
                widthTextField.setText(dims[0]);
                heightTextField.setText(dims[1]);
                
                widthTextField.setEnabled(true);
                heightTextField.setEnabled(true);
                launcher.getProfiles()
                        .getSelectedProfile()
                        .setResolution(new Dimension(
                                Integer.parseInt(widthTextField.getText()),
                                Integer.parseInt(heightTextField.getText())));
            }
        }); // </editor-fold>
        jvmArgsCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            jvmArgsTextField.setEnabled(jvmArgsCheckbox.isSelected());
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (jvmArgsCheckbox.isSelected()) {
                p.setJavaArgs(jvmArgsTextField.getText());
            } else {
                p.setJavaArgs(null);
            }
        }); // </editor-fold>
        jvmArgsTextField.getDocument().addUndoableEditListener((UndoableEditEvent e) -> { // <editor-fold>
            if (!jvmArgsTextField.isEnabled()) {
                return;
            }
            launcher.getProfiles().getSelectedProfile()
                    .setJavaArgs(jvmArgsTextField.getText());
        }); // </editor-fold>
        snapshotsCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (snapshotsCheckbox.isSelected()) {
                p.getAllowedReleaseTypes().add("snapshot");
            } else {
                p.getAllowedReleaseTypes().remove("snapshot");
            }
            loadVersions();
        }); // </editor-fold>
        betaCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (betaCheckbox.isSelected()) {
                p.getAllowedReleaseTypes().add("old_beta");
            } else {
                p.getAllowedReleaseTypes().remove("old_beta");
            }
            loadVersions();
        }); // </editor-fold>
        alphaCheckbox.addActionListener((ActionEvent e) -> { // <editor-fold>
            Profile p = launcher.getProfiles().getSelectedProfile();
            if (alphaCheckbox.isSelected()) {
                p.getAllowedReleaseTypes().add("old_alpha");
            } else {
                p.getAllowedReleaseTypes().remove("old_alpha");
            }
            loadVersions();
        }); // </editor-fold>
    }
    
}
