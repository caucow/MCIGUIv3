package com.caucraft.mciguiv3.pmgr;

import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.pmgr.PasswordManager;
import com.caucraft.mciguiv3.util.ImageResources;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class PasswordManagerPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    private final JScrollPane passListScrollPane;
    private final JLabel passListInfoLabel;
    private final JPanel passListViewPanel;
    private final JButton editButton;
    private final JButton decryptButton;
    private final JButton refreshButton;
    private final JCheckBox showUsersCheckbox;
    private final JButton changeMasterButton;
    
    public PasswordManagerPanel(Launcher launcher) {
        super(new double[] {
            5,
            1
        }, new Image[]{
            ImageResources.BEDROCK,
            ImageResources.STONE
        });
        
        this.launcher = launcher;
        
        this.passListInfoLabel = new JLabel("[B]ottom Text");
        this.passListInfoLabel.setAlignmentX(0.5F);
        this.passListInfoLabel.setAlignmentY(0.5F);
        this.passListInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.passListScrollPane = new JScrollPane(this.passListInfoLabel);
        this.passListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.passListScrollPane.getVerticalScrollBar().setUnitIncrement(8);
        
        this.passListViewPanel = new JPanel(new MigLayout("wrap", "[grow 75, fill][grow 25, fill]"));
        
        this.editButton = new JButton("Edit");
        this.decryptButton = new JButton("Decrypt");
        this.refreshButton = new JButton("Forget Master/Refresh");
        this.showUsersCheckbox = new JCheckBox("Show Usernames");
        this.changeMasterButton = new JButton("Change Master Password");
        
        this.passListInfoLabel.setOpaque(false);
        this.passListInfoLabel.setForeground(Color.WHITE);
        this.passListScrollPane.setOpaque(false);
        this.passListScrollPane.getViewport().setOpaque(false);
        this.passListScrollPane.getVerticalScrollBar().setOpaque(false);
        this.passListViewPanel.setOpaque(false);
        this.showUsersCheckbox.setOpaque(false);
        this.showUsersCheckbox.setForeground(Color.WHITE);
        
        this.setLayout(new MigLayout("fill", "[][]unrel[][]push[]", "[grow,fill][]"));
        this.add(this.passListScrollPane, "grow,span,wrap");
        this.add(this.editButton);
        this.add(this.decryptButton);
        this.add(this.refreshButton);
        this.add(this.showUsersCheckbox);
        this.add(this.changeMasterButton);
        
        registerListeners();
    }
    
    private void registerListeners() {
        editButton.addActionListener((e) -> {
            PasswordManager passMgr = launcher.getPasswordManager();
            passMgr.editPasswords(launcher.getProfiles().getClientToken());
            reloadManagerView();
        });
        decryptButton.addActionListener((e) -> {
            PasswordManager passMgr = launcher.getPasswordManager();
            passMgr.decryptFile(launcher.getProfiles().getClientToken());
            reloadManagerView();
        });
        refreshButton.addActionListener((e) -> {
            launcher.reloadPassManager();
        });
        showUsersCheckbox.addActionListener((e) -> {
            reloadManagerView();
        });
        changeMasterButton.addActionListener((e) -> {
            PasswordManager passMgr = launcher.getPasswordManager();
            passMgr.changePassword(launcher.getProfiles().getClientToken());
            reloadManagerView();
        });
    }
    
    public void reloadManagerView() {
        passListInfoLabel.setText("Reloading password manager.");
        passListScrollPane.setViewportView(passListInfoLabel);
        passListViewPanel.removeAll();
        editButton.setEnabled(false);
        decryptButton.setEnabled(false);
        changeMasterButton.setEnabled(false);
        refreshButton.setEnabled(true);
        PasswordManager passMgr = launcher.getPasswordManager();
        if (!passMgr.isPasswordSet()) {
            try {
                passMgr.load();
            } catch (IOException e) {
                passListInfoLabel.setText("Can not load accounts file.");
                passListScrollPane.setViewportView(passListInfoLabel);
                return;
            }
        }
        editButton.setEnabled(true);
        if (!passMgr.isPasswordSet()) {
            passListInfoLabel.setText("No account file.");
            passListScrollPane.setViewportView(passListInfoLabel);
            return;
        }
        decryptButton.setEnabled(true);
        if (!passMgr.isDecrypted()) {
            passListInfoLabel.setText("Accounts file encrypted.");
            passListScrollPane.setViewportView(passListInfoLabel);
            return;
        }
        decryptButton.setEnabled(false);
        changeMasterButton.setEnabled(true);
        Set<String> names = passMgr.getUsernames();
        for (String userName : names) {
            if (!showUsersCheckbox.isSelected()) {
                userName = userName.matches(".+@.+\\..+")
                        ? userName.replaceAll("(.).+(.@.+)", "$1\u2022\u2022\u2022\u2022$2")
                        : userName.replaceAll("(.{1,2}).+(.{1,2})", "$1\u2022\u2022\u2022\u2022$2");
            }
            
            JTextField textField = new JTextField(userName);
            textField.setForeground(Color.WHITE);
            textField.setOpaque(false);
            textField.setEditable(false);
            passListViewPanel.add(textField);
            textField = new JTextField("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
            textField.setForeground(Color.WHITE);
            textField.setOpaque(false);
            textField.setEditable(false);
            textField.setEnabled(false);
            passListViewPanel.add(textField);
        }
        passListScrollPane.setViewportView(passListViewPanel);
    }
}
