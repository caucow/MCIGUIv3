package com.caucraft.mciguiv3.gamefiles.auth;

import com.caucraft.mciguiv3.components.GoodComboBoxRenderer;
import com.caucraft.mciguiv3.components.RandomTexturedPanel;
import com.caucraft.mciguiv3.gamefiles.profiles.AuthenticatedUser;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.pstor.PasswordDialog;
import com.caucraft.mciguiv3.util.ImageResources;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author caucow
 */
public class AuthPanel extends RandomTexturedPanel {
    
    private final Launcher launcher;
    private final RandomTexturedPanel mainPanel;
    private final JLabel playAsLabel;
    private final JComboBox<String> playAsComboBox;
    private final JButton playButton;
    private final JButton logOutButton;
    private final JLabel logInLabel;
    private final JLabel userLabel;
    private final JTextField userTextField;
    private final JLabel passLabel;
    private final JPasswordField passTextField;
    private final JButton logInButton;
    private final JButton playOfflineButton;
    private final JButton cancelButton;
    private final JLabel statusLabel;
    
    public AuthPanel(Launcher launcher) {
        super(
                new double[] {
                    1
                }, new Image[] {
                    ImageResources.BEDROCK
                }
        );
        this.launcher = launcher;
        this.mainPanel = new RandomTexturedPanel(
                new double[] {
                    1
                }, new Image[] {
                    ImageResources.STONE
                }
        );
        this.playAsLabel = new JLabel("Play as an existing user");
        this.playAsComboBox = new JComboBox<>();
        this.playAsComboBox.setEditable(false);
        
        this.playButton = new JButton("Play");
        this.logOutButton = new JButton("Log Out");
        this.logInLabel = new JLabel("Log in as a new user");
        this.userLabel = new JLabel("Username");
        this.userTextField = new JTextField();
        this.passLabel = new JLabel("Password");
        this.passTextField = new JPasswordField();
        this.logInButton = new JButton("Log In");
        this.playOfflineButton = new JButton("Play Offline");
        this.cancelButton = new JButton("Cancel");
        this.statusLabel = new JLabel("Leave password blank to log in with accounts file.");
        
        this.playAsLabel.setForeground(Color.WHITE);
        this.playAsComboBox.setOpaque(false);
        this.playAsComboBox.setForeground(Color.WHITE);
        this.playAsComboBox.setBackground(Color.DARK_GRAY);
        GoodComboBoxRenderer goodRenderer = new GoodComboBoxRenderer(playAsComboBox, Color.WHITE, Color.DARK_GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.GRAY, Color.DARK_GRAY);
        goodRenderer.setOpaque(false);
        this.playAsComboBox.setRenderer(goodRenderer);
        this.logInLabel.setForeground(Color.WHITE);
        this.userLabel.setForeground(Color.WHITE);
        this.userTextField.setForeground(Color.WHITE);
        this.userTextField.setOpaque(false);
        this.userTextField.setCaretColor(Color.LIGHT_GRAY);
        this.passLabel.setForeground(Color.WHITE);
        this.passTextField.setForeground(Color.WHITE);
        this.passTextField.setOpaque(false);
        this.passTextField.setCaretColor(Color.LIGHT_GRAY);
        this.statusLabel.setForeground(Color.WHITE);
        
        this.mainPanel.setBorder(new LineBorder(Color.WHITE));
        this.mainPanel.setLayout(new MigLayout("ins 10, nogrid"));
        this.mainPanel.add(this.playAsLabel, "growx, wrap");
        this.mainPanel.add(this.playAsComboBox, "growx, wrap");
        this.mainPanel.add(this.playButton);
        this.mainPanel.add(this.logOutButton, "gap push, wrap");
        this.mainPanel.add(this.logInLabel, "gapy 10, growx, wrap");
        this.mainPanel.add(this.userLabel, "sg cred_l");
        this.mainPanel.add(this.userTextField, "gap 20, growx, sg cred_tf, wrap");
        this.mainPanel.add(this.passLabel, "sg cred_l");
        this.mainPanel.add(this.passTextField, "gap 20, growx, sg cred_tf, wrap");
        this.mainPanel.add(this.logInButton);
        this.mainPanel.add(this.playOfflineButton);
        this.mainPanel.add(this.cancelButton, "gap push, wrap");
        this.mainPanel.add(this.statusLabel, "gapy 10, grow");
        
        refreshButtons();
        
        this.setLayout(new MigLayout("nogrid, align center center"));
        this.add(this.mainPanel);
        
        registerListeners();
    }
    
    private void registerListeners() {
        this.playButton.addActionListener((e) -> {
            disableAll();
            AuthenticatedUser user = launcher.getProfiles().getAuthDb().getUserByName((String)playAsComboBox.getSelectedItem());
            TaskList authTaskList = new TaskList("Logging in as existing user");
            AtomicBoolean loggedIn = new AtomicBoolean(false);
            
            Task validateTask = new Task("Checking existing auth token") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        if (Authenticator.validateAccessToken(launcher.getProfiles().getClientToken(), user)) {
                            loggedIn.set(true);
                            launcher.setLoggedInOnce();
                            launcher.getProfiles().setSelectedUser(user);
                            launcher.getConfig().setOfflineMode(false);
                            launcher.getConfig().setOfflineName(user.getDisplayName());
                            launcher.reloadAuthDisplay(true);
                            launcher.getConfig().save();
                            launcher.getProfiles().save(true);
                        } else {
                            statusLabel.setText("<html><center>Invalid access token.</center></html>");
                        }
                    } catch (IOException e) {
                        statusLabel.setText(String.format("<html><center>Unable to connect to Mojang's servers.<BR>%s: %s</center></html>", e.getClass().getSimpleName(), e.getMessage()));
                        throw e;
                    }
                }
            };
            Task refreshTask = new Task("Refreshing auth token") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    if (loggedIn.get()) {
                        return;
                    }
                    try {
                        AuthenticatedUser newUser = Authenticator.refreshAccessToken(launcher.getProfiles().getClientToken(), user);
                        loggedIn.set(true);
                        launcher.setLoggedInOnce();
                        launcher.getProfiles().setSelectedUser(newUser);
                        launcher.getConfig().setOfflineMode(false);
                        launcher.getConfig().setOfflineName(newUser.getDisplayName());
                        launcher.reloadAuthDisplay(true);
                        launcher.getConfig().save();
                        launcher.getProfiles().save(true);
                    } catch (IOException e) {
                        statusLabel.setText(String.format("<html><center>Unable to connect to Mojang's servers.<BR>%s: %s</center></html>", e.getClass().getSimpleName(), e.getMessage()));
                        throw e;
                    } catch (ForbiddenOperationException e) {
                        statusLabel.setText("<html><center>Could not refresh access token.</center></html>");
                    }
                }
            };
            Task loginTask = new Task("Logging in with credentials") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    if (loggedIn.get()) {
                        return;
                    }
                    try {
                        char[] pass = PasswordDialog.getPassword(launcher.getMainWindow(), "Enter password for " + user.getDisplayName());
                        if (pass == null) {
                            return;
                        }
                        AuthenticatedUser newUser = Authenticator.loginWithPassword(launcher.getProfiles().getClientToken(), user.getUsername(), new String(pass));
                        loggedIn.set(true);
                        launcher.setLoggedInOnce();
                        launcher.getProfiles().setSelectedUser(newUser);
                        launcher.getConfig().setOfflineMode(false);
                        launcher.getConfig().setOfflineName(newUser.getDisplayName());
                        launcher.reloadAuthDisplay(true);
                        launcher.getConfig().save();
                        launcher.getProfiles().save(true);
                    } catch (IOException e) {
                        statusLabel.setText(String.format("<html><center>Unable to connect to Mojang's servers.<BR>%s: %s</center></html>", e.getClass().getSimpleName(), e.getMessage()));
                        throw e;
                    } catch (ForbiddenOperationException e) {
                        statusLabel.setText(String.format("<html><center>%s</center></html>", e.getMessage()));
                    }
                }
            };
            authTaskList.addTask(validateTask);
            authTaskList.addTask(refreshTask);
            authTaskList.addTask(loginTask);
            launcher.getAuthTaskMgr().addTask(authTaskList);
            new Thread(() -> {
                launcher.getAuthTaskMgr().waitOnTasks();
                refreshButtons();
                if (loggedIn.get()) {
                    launcher.setCurrentScreen(launcher.getLauncherPanel());
                }
            }, "Task Wait Thread (AuthPanel Play)").start();
        });
        this.logOutButton.addActionListener((e) -> {
            disableAll();
            AuthenticatedUser user = launcher.getProfiles().getAuthDb().getUserByName(
                                        (String)playAsComboBox.getSelectedItem());
            launcher.getAuthTaskMgr().addTask(new Task("Logging out " + (String)playAsComboBox.getSelectedItem()) {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() throws Exception {
                    Exception ex = null;
                    try  {
                        Authenticator.invalidateToken(
                                launcher.getProfiles().getClientToken(),
                                user.getAccessToken()
                        );
                        launcher.getProfiles().getAuthDb().removeUser(user);
                        launcher.getProfiles().getSelectedUser().equals(user);
                    } catch (Exception e) {
                        ex = e;
                    }
                    refreshButtons();
                    if (ex != null) {
                        throw ex;
                    }
                }
            });
        });
        this.logInButton.addActionListener((e) -> {
            disableAll();
            TaskList authTaskList = new TaskList("Logging in as new user");
            AtomicBoolean loggedIn = new AtomicBoolean(false);
            
            Task loginTask = new Task("Logging in with credentials") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        String username = userTextField.getText();
                        char[] password = passTextField.getPassword();
                        
                        if (username.isEmpty()) {
                            statusLabel.setText(String.format("<html><center>%s</center></html>", "Enter a user to log in as.<BR>Leave pass blank to use pass manager."));
                            return;
                        }
                        if (password.length == 0) {
                            statusLabel.setText("Password manager currently unsupported.");
                            return;
                        }
                        
                        AuthenticatedUser newUser = Authenticator.loginWithPassword(launcher.getProfiles().getClientToken(), username, new String(password));
                        loggedIn.set(true);
                        launcher.setLoggedInOnce();
                        launcher.getProfiles().setSelectedUser(newUser);
                        launcher.getConfig().setOfflineMode(false);
                        launcher.getConfig().setOfflineName(newUser.getDisplayName());
                        launcher.reloadAuthDisplay(true);
                        launcher.getConfig().save();
                        launcher.getProfiles().save(true);
                    } catch (IOException e) {
                        statusLabel.setText(String.format("<html><center>Unable to connect to Mojang's servers.<BR>%s: %s</center></html>", e.getClass().getSimpleName(), e.getMessage()));
                        throw e;
                    } catch (ForbiddenOperationException e) {
                        statusLabel.setText(String.format("<html><center>%s</center></html>", e.getMessage()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            
            authTaskList.addTask(loginTask);
            launcher.getAuthTaskMgr().addTask(authTaskList);
            new Thread(() -> {
                launcher.getAuthTaskMgr().waitOnTasks();
                refreshButtons();
                if (loggedIn.get()) {
                    launcher.setCurrentScreen(launcher.getLauncherPanel());
                }
            }, "Task Wait Thread (AuthPanel Login)").start();
        });
        this.playOfflineButton.addActionListener((e) -> {
            launcher.getConfig().setOfflineMode(true);
            launcher.getConfig().setOfflineName(userTextField.getText());
            launcher.setCurrentScreen(launcher.getLauncherPanel());
            launcher.reloadAuthDisplay(true);
        });
        this.cancelButton.addActionListener((e) -> {
            launcher.setCurrentScreen(launcher.getLauncherPanel());
        });
    }
    
    private void refreshButtons() {
        Collection<AuthenticatedUser> users = launcher.getProfiles().getAuthDb().getUsers();
        TreeSet<String> sortedNames = new TreeSet<>();
        for (AuthenticatedUser user : users) {
            sortedNames.add(user.getDisplayName());
        }
        playAsComboBox.setModel(new DefaultComboBoxModel<>(new Vector(sortedNames)));
        AuthenticatedUser curUser = launcher.getProfiles().getSelectedUser();
        if (curUser != null) {
            this.playAsComboBox.setSelectedItem(curUser.getDisplayName());
        }
        
        boolean canPlayAs = playAsComboBox.getItemCount() > 0;
        playAsComboBox.setEnabled(canPlayAs);
        playButton.setEnabled(canPlayAs);
        logOutButton.setEnabled(canPlayAs);
        
        userTextField.setEnabled(true);
        passTextField.setEnabled(true);
        logInButton.setEnabled(true);
        playOfflineButton.setEnabled(true);
        cancelButton.setEnabled(true);
    }
    
    private void disableAll() {
        playAsComboBox.setEnabled(false);
        playButton.setEnabled(false);
        logOutButton.setEnabled(false);
        userTextField.setEnabled(false);
        passTextField.setEnabled(false);
        logInButton.setEnabled(false);
        playOfflineButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }
}
