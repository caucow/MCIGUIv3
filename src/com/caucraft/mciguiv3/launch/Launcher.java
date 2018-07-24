package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.pmgr.PasswordManagerPanel;
import com.caucraft.mciguiv3.gamefiles.auth.AuthPanel;
import com.caucraft.mciguiv3.gamefiles.auth.Authenticator;
import com.caucraft.mciguiv3.gamefiles.auth.ForbiddenOperationException;
import com.caucraft.mciguiv3.gamefiles.profiles.AuthenticatedUser;
import com.caucraft.mciguiv3.util.OS;
import com.caucraft.mciguiv3.gamefiles.versions.VersionManager;
import com.caucraft.mciguiv3.gamefiles.profiles.LauncherProfiles;
import com.caucraft.mciguiv3.gamefiles.profiles.Profile;
import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.ManifestGameVersion;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.ManifestViewerDialog;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.VersionManifest;
import com.caucraft.mciguiv3.launch.gameinstance.GameFileDownloaderTask;
import com.caucraft.mciguiv3.launch.gameinstance.GameMonitor;
import com.caucraft.mciguiv3.launch.gameinstance.GameRunnerTask;
import com.caucraft.mciguiv3.launch.gameinstance.PastRunsPanel;
import com.caucraft.mciguiv3.pmgr.PasswordDialog;
import com.caucraft.mciguiv3.pmgr.PasswordDialogPanel;
import com.caucraft.mciguiv3.pmgr.PasswordManager;
import com.caucraft.mciguiv3.update.AboutWindow;
import com.caucraft.mciguiv3.update.LauncherVersions;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.TaskManager;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author caucow
 */
public final class Launcher {
    
    public static final File LAUNCHER_JARFILE = getJarFile();
    public static final String LAUNCHER_VERSION = "3.1.3";
    public static final Logger LOGGER;
    public static final String UPDATE_URL = "https://github.com/caucow/MCIGUIv3/raw/master/version_manifest.JSON";
    public static final int MOJANG_LAUNCHER_VERSION = 21;
    public static final GridBagConstraints FILL_CONSTRAINTS;
    public static final File CD;
    public static final Path CD_PATH;
    public static final File LAUNCHER_DIR;
    public static final File NATIVES_DIR;
    public static final File ARCHIVE_DIR;
    public static final OS OS_NAME;
    public static final String OS_VER, OS_ARCH;
    
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] [%4$s] %5$s%6$s%n");
        LOGGER = Logger.getLogger("launcher");
        Thread.setDefaultUncaughtExceptionHandler(new LauncherExceptionLogger());
        System.setProperty("sun.awt.exception.handler", LauncherExceptionLogger.class.getName());
        FILL_CONSTRAINTS = new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        String osname = System.getProperty("os.name").toLowerCase();
        if (osname.contains("windows")) {
            OS_NAME = OS.WINDOWS;
        } else if (osname.contains("mac") || osname.contains("os x") || osname.contains("osx")) {
            OS_NAME = OS.OSX;
        } else if (osname.contains("linux")) {
            OS_NAME = OS.LINUX;
        } else {
            OS_NAME = null;
            LOGGER.log(Level.SEVERE, "[ERROR] Unknown or unaccounted for OS: {0}", osname);
        }
        OS_VER = System.getProperty("os.version");
        OS_ARCH = System.getProperty("sun.arch.data.model");
        File cd = new File(".");
        try {
            cd = cd.toPath().toRealPath().toFile();
        } catch (IOException e) {}
        CD = cd;
        CD_PATH = cd.toPath();
        LAUNCHER_DIR = new File(CD, "MCIGUI");
        NATIVES_DIR = new File(LAUNCHER_DIR, "Natives");
        ARCHIVE_DIR = new File(LAUNCHER_DIR, "Archive");
    }
    
    public static String getFileSha1(File f) {
        if (f == null || !f.exists()) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }
        try {
            InputStream fis = new FileInputStream(f);
            int n = 0;
            byte[] buffer = new byte[1048576];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            StringBuilder sb = new StringBuilder(40);
            for (byte b : digest.digest())
                sb.append(String.format("%02x", b & 255));
            return sb.toString();
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public static File getCurrentDirectory() {
        return CD;
    }
    
    public static File relativize(File other) {
        try {
            return CD_PATH.relativize(other.toPath()).toFile();
        } catch (IllegalArgumentException iae) {
            return other;
        }
    }
    
    public static File resolve(File other) {
        try {
            return CD_PATH.resolve(other.toPath()).toFile();
        } catch (IllegalArgumentException iae) {
            return other;
        }
    }
    
    private static File getJarFile() {
        try {
            String s = Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            if (s.endsWith(".jar")) {
                return new File(s);
            }
            return new File(s + ".jar");
        } catch (Exception e) {
            return null;
        }
    }
    
    private static void ensureClassesLoaded() {
        if (LAUNCHER_JARFILE == null || !LAUNCHER_JARFILE.exists()) {
            return;
        }
        try (JarFile jar = new JarFile(LAUNCHER_JARFILE)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (name.endsWith(".class")) {
                    Class.forName(name.substring(0, name.length() - 6).replace('/', '.'));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not extract required libraries", e);
        }
    }
    
    public static BufferedImage getImageResource(String resourceName) {
        try {
            return ImageIO.read(Launcher.class.getResource(resourceName));
        } catch (Exception e) {
            return null;
        }
    }
    
    private static void addSoftwareLibrary(File file) throws Exception {
        Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(ClassLoader.getSystemClassLoader(), new Object[]{file.toURI().toURL()});
    }

    public static void deleteFileTree(File deleteMe, boolean verboseOutput) {
        if (deleteMe.isDirectory()) {
            for (File deleteThisFirst : deleteMe.listFiles()) {
                deleteFileTree(deleteThisFirst, verboseOutput);
            }
        }
        if (verboseOutput) {
            if (deleteMe.delete()) {
                LOGGER.log(Level.INFO, "Successfully deleted {0}.", deleteMe.toString());
            } else {
                LOGGER.log(Level.INFO, "Could not delete {0}. Is it open in another program?", deleteMe.toString());
            }
        } else {
            deleteMe.delete();
        }
    }
    
    public static Logger getLogger() {
        return LOGGER;
    }
    
    public static void main(String[] args) {
        Launcher l = new Launcher();
        l.start(args);
    }
    
    private boolean checkedClassPath;
    private final JFrame mainWindow;
    private final LogPanel logPanel;
    private final TaskManager mainTaskMgr;
    private final TaskManager secondaryTaskMgr;
    private final TaskManager authTaskMgr;
    
    private LauncherConfig config;
    private File chooserCD;
    private LauncherProfiles profiles;
    private VersionManager versions;
    private ValidGameFileSet validFiles;
    private PasswordManager passMgr;
    private File passFile;
    
    private LauncherPanel launcherPanel;
    private MainPanel mainPanel;
    private BackupPanel backupPanel;
    private PasswordManagerPanel passMgrPanel;
    private PastRunsPanel pastRunsPanel;
    
    private boolean hasLoggedInOnce;
    private ConcurrentHashMap<UUID, GameMonitor> gameMonitorMap;
    private AboutWindow aboutWindow;
    
    public Launcher() {
        this.mainWindow = new JFrame("MCIGUI " + LAUNCHER_VERSION);
        this.mainWindow.setSize(800, 600);
        this.mainWindow.setMinimumSize(new Dimension(620, 460));
        this.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainWindow.getContentPane().setLayout(new GridBagLayout());
        this.logPanel = new LogPanel(LOGGER);
        this.setCurrentScreen(this.logPanel);
        this.mainTaskMgr = new TaskManager(null, false);
        this.secondaryTaskMgr = new TaskManager(null, true);
        this.authTaskMgr = new TaskManager(mainWindow, false);
        this.chooserCD = CD;
    }
    
    public void start(String[] args) {
        LOGGER.info("Showing launcher window.");
        mainWindow.setLocationRelativeTo(null);
        mainWindow.setVisible(true);
        LOGGER.info("Deleting old natives");
        cleanOldNatives();
        LOGGER.info("Loading launcher.");
        LOGGER.info("Starting main task manager.");
        mainTaskMgr.startTaskThread();
        LOGGER.info("Starting secondary task manager.");
        secondaryTaskMgr.startTaskThread();
        LOGGER.info("Starting authentication task manager.");
        authTaskMgr.startTaskThread();
        LOGGER.info("Ensuring classpath integrity.");
        ensureClasspath();
        LOGGER.info("Ensuring update safety.");
        ensureClassesLoaded();
        LOGGER.info("Loading configuration.");
        config = new LauncherConfig(this, new File(LAUNCHER_DIR, "config.json"));
        config.load();
        LOGGER.info("Loading MCHome.");
        File mcHome = config.getMcHome();
        if (mcHome == null) {
            mcHome = chooseDirectory("Select Minecraft Home (.minecraft)");
            if (mcHome == null) {
                JOptionPane.showMessageDialog(mainWindow, "Must select a folder to run Minecraft from.");
                System.exit(0);
                return;
            }
            config.setMcHome(mcHome);
            config.save();
        }
        config.setMcHome(mcHome);
        profiles = new LauncherProfiles(new File(mcHome, "launcher_profiles.json"));
        try {
            profiles.load();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load launcher profiles.", e);
        }
        versions = new VersionManager(mcHome);
        versions.load();
        passMgr = new PasswordManager(this, new File(mcHome, "ccraft/accounts.ecca"));
        validFiles = new ValidGameFileSet();
        LOGGER.info("Building GUI.");
        
        launcherPanel = new LauncherPanel(this);
        mainPanel = new MainPanel(this);
        backupPanel = new BackupPanel();
        passMgrPanel = new PasswordManagerPanel(this);
        pastRunsPanel = new PastRunsPanel(this);
        
        launcherPanel.addTab("Launcher Log", logPanel);
        int mainIndex = launcherPanel.addTab("Settings", mainPanel);
//        launcherPanel.addTab("Backup", backupPanel);
        launcherPanel.addTab("Password Manager", passMgrPanel);
        launcherPanel.addTab("Past Runs", pastRunsPanel);
        launcherPanel.setSelectedTab(mainIndex);
        
        gameMonitorMap = new ConcurrentHashMap<>();
        aboutWindow = new AboutWindow(mainWindow);
        aboutWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        setCurrentScreen(launcherPanel);
        mainPanel.loadProfiles();
        reloadAuthDisplay(true);
        reloadPassManager();
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (gameMonitorMap.isEmpty()) {
                    System.exit(0);
                } else {
                    int confirmed = JOptionPane.showConfirmDialog(mainWindow,
                            "There are still games open, are you sure you want to close the launcher?",
                            "Do you really want to close me? v,v",
                            JOptionPane.YES_NO_OPTION);
                    if (confirmed == JOptionPane.YES_OPTION) {
                        gameMonitorMap.entrySet().forEach((ent) -> {
                            ent.getValue().closeHandlers();
                        });
                        System.exit(0);
                    }
                }
            }
        });
        mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        startUpdateThread(0);
    }
    
    private void startUpdateThread(long sleepTime) {
        Thread updateThread = new Thread(() -> {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
            try {
                LOGGER.log(Level.INFO, "Checking for updates");
                LauncherVersions vers = LauncherVersions.getLauncherVersions(Launcher.this);
                aboutWindow.setUpdateSuccess(vers);
                String cVer = vers.getCurrentVersion();
                if (!cVer.equals(LAUNCHER_VERSION)) {
                    LOGGER.log(Level.INFO, "Update found: {0}", vers.getCurrentVersion());
                    launcherPanel.getControlPanel().setUpdateAvailable();
                } else {
                    LOGGER.log(Level.INFO, "MCIGUI is up to date.");
                }
            } catch (Exception e) {
                LOGGER.log(Level.INFO, "Unable to check for updates", e);
                aboutWindow.setUpdateFail(e);
            }
            startUpdateThread(86400000);
        },"MCIGUI Update Checker");
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
    private void cleanOldNatives() {
        deleteFileTree(NATIVES_DIR, true);
        try { Thread.sleep(1000); } catch (Exception e) { }
        NATIVES_DIR.mkdirs();
    }
    
    public void ensureClasspath() {
        if (checkedClassPath) {
            return;
        }
        String[] jars = new String[] {
            "bcprov-jdk15on-159.jar",
            "commons-compress-1.16.1.jar",
            "gson-2.8.2.jar",
            "miglayout-4.0.jar",
            "xz-1.8.jar"
        };
        File libDir = new File(LAUNCHER_DIR, "libs");
        if (!libDir.exists()) {
            libDir.mkdirs();
        }
        byte[] buffer = new byte[1048576];
        TaskList writeLibs = new TaskList("Extracting Launcher Libraries");
        for (String jarName : jars) {
            File libFile = new File(libDir, jarName);
            if (libFile.exists()) {
                try {
                    addSoftwareLibrary(libFile);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Could not load required library", e);
                }
                continue;
            }
            
            writeLibs.addTask(new Task("Writing " + jarName) {
                
                private long total = 1;
                private long read = 0;
                
                @Override
                public float getProgress() {
                    return (float)read / (float)total;
                }
                
                @Override
                public void run() throws Exception {
                    LOGGER.log(Level.INFO, "Writing {0}", jarName);
                    total = Launcher.class.getResource("/com/caucraft/mciguiv3/resources/" + jarName).openConnection().getContentLengthLong();
                    try (FileOutputStream libStream = new FileOutputStream(libFile); InputStream resourceStream = Launcher.class.getResourceAsStream("/com/caucraft/mciguiv3/resources/" + jarName)) {
                        byte[] buffer = new byte[1048576];
                        int cur;
                        while ((cur = resourceStream.read(buffer)) > 0) {
                            read += cur;
                            libStream.write(buffer, 0, cur);
                            updateProgress();
                        }
                        
                        addSoftwareLibrary(libFile);
                    }
                }
            });
        }
        if (writeLibs.getProgress() == 0.0F) {
            mainTaskMgr.addTask(writeLibs);
            mainTaskMgr.waitOnTasks();
        }
    }
    
    public synchronized void setCurrentScreen(JPanel panel) {
        mainWindow.getContentPane().removeAll();
        mainWindow.getContentPane().add(panel, FILL_CONSTRAINTS);
        mainWindow.revalidate();
        mainWindow.repaint();
    }
    
    public JFrame getMainWindow() {
        return mainWindow;
    }
    
    public LauncherPanel getLauncherPanel() {
        return launcherPanel;
    }
    
    public MainPanel getMainPanel() {
        return mainPanel;
    }
    
    public BackupPanel getWorldPanel() {
        return backupPanel;
    }
    
    public PasswordManagerPanel getPasswordManagerPanel() {
        return passMgrPanel;
    }
    
    public PastRunsPanel getPastRunsPanel() {
        return pastRunsPanel;
    }
    
    public TaskManager getMainTaskMgr() {
        return mainTaskMgr;
    }
    
    public TaskManager getSecondaryTaskMgr() {
        return secondaryTaskMgr;
    }
    
    public TaskManager getAuthTaskMgr() {
        return authTaskMgr;
    }
    
    public LauncherProfiles getProfiles() {
        return profiles;
    }
    
    public LauncherConfig getConfig() {
        return config;
    }
    
    public VersionManager getVersionManager() {
        return versions;
    }
    
    public PasswordManager getPasswordManager() {
        return passMgr;
    }
    
    public File getMcHome() {
        return config.getMcHome();
    }
    
    public File getJavaExe() {
        return config.getJavaLoc();
    }
    
    public void setMcHome(File f) {
        if (!f.exists()) {
            return;
        }
        config.setMcHome(f);
        reloadMcHome();
    }
    
    public void setJavaExe(File f) {
        if (!f.exists()) {
            return;
        }
        config.setJavaLoc(f);
    }
    
    public void reloadMcHome() {
        profiles = new LauncherProfiles(new File(getMcHome(), "launcher_profiles.json"));
        try {
            profiles.load();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load launcher profiles.", e);
        }
        versions = new VersionManager(config.getMcHome());
        versions.load();
        mainPanel.loadProfiles();
        validFiles = new ValidGameFileSet();
        passMgr = new PasswordManager(this, new File(getMcHome(), "ccraft/accounts.ecca"));
        reloadPassManager();
    }
    
    public void reloadProfiles() {
        try {
            profiles.load();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load launcher profiles.", e);
        }
        mainPanel.loadProfiles();
    }
    
    public void reloadVersions() {
        versions.load();
        mainPanel.loadVersions();
    }
    
    public void reloadAuthDisplay(boolean setUser) {
        launcherPanel.getControlPanel().reloadAuthDisplay(setUser);
    }
    
    public void reloadPassManager() {
        passMgr.forgetPassword();
        refreshPassManager();
    }
    
    public void refreshPassManager() {
        passMgrPanel.reloadManagerView();
    }
    
    public void selectAccount() {
        setCurrentScreen(new AuthPanel(this));
    }
    
    public AuthenticatedUser getCurrentUser() {
        AuthenticatedUser user;
        if (config.getOfflineMode()) {
            user = profiles.getAuthDb().getUserByName(config.getOfflineName());
            if (user == null) {
                user = new AuthenticatedUser(config.getOfflineName());
            }
        } else {
            user = profiles.getSelectedUser();
        }
        return user;
    }
    
    public void launchGame() {
        profiles.save(true);
        config.save();
        
        File mcHome = getMcHome();
        Profile p = profiles.getSelectedProfile();
        if (config.getOfflineMode()) {
            AuthenticatedUser user = getCurrentUser();
            Task prepTask = new Task("Preparing game launch") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() {
                    LOGGER.log(Level.INFO, "Launching version {0} as {1} (offline mode)", new Object[]{p.getLastVersionId(), user.getDisplayName()});
                    Dimension d = p.getResolution();
                    File gameDir = p.getGameDir();
                    if (gameDir == null) {
                        gameDir = mcHome;
                    }

                    Map<String, String> props = new HashMap<>();
                    props.put("launcher_name", "MCIGUI");
                    props.put("launcher_version", "3.0");

                    props.put("os.name", Launcher.OS_NAME.osName);
                    props.put("os.version", Launcher.OS_VER);
                    props.put("os.arch", Launcher.OS_ARCH);

                    props.put("features.is_demo_user", "false");
                    props.put("features.has_custom_resolution", String.valueOf(d != null));
                    if (d != null) {
                        props.put("resolution_width", Integer.toString(d.width));
                        props.put("resolution_height", Integer.toString(d.height));
                    } else {
                        props.put("resolution_width", "854");
                        props.put("resolution_height", "480");
                    }

                    props.put("auth_player_name", user.getDisplayName());
                    props.put("auth_uuid", user.getId().toString());
                    props.put("auth_access_token", user.getAccessToken());
                    props.put("user_type", user.getType());

                    props.put("game_directory", gameDir.getPath());
                    props.put("assets_root", new File(mcHome, "assets").getPath());

                    GameRunnerTask runner = new GameRunnerTask(
                            validFiles,
                            Launcher.this,
                            getJavaExe(),
                            mcHome,
                            profiles.getSelectedProfile().getLastVersionId(),
                            NATIVES_DIR,
                            gameDir,
                            true,
                            props,
                            getLoggedInOnce(),
                            p.getJavaArgs()
                    );
                    mainTaskMgr.addTask(runner);
                    mainTaskMgr.addTask(runner.getCancelTask());
                }
            };
            mainTaskMgr.addTask(prepTask);
        } else {
            UUID id = getCurrentUser().getId();
            AuthenticatedUser[] usera = {getCurrentUser()};
            TaskList authTaskList = new TaskList("Logging in as existing user");
            AtomicBoolean loggedIn = new AtomicBoolean(false);
            AtomicReference<char[]> storedPass = new AtomicReference<>();
            PasswordManager pmgr = getPasswordManager();
            PasswordDialog pd = PasswordDialog.getPasswordDialog(getMainWindow(), "Enter password for " + usera[0].getDisplayName(), pmgr.isPasswordSet() && !pmgr.isDecrypted());
            
            Task validateTask = new Task("Checking existing auth token") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        usera[0] = profiles.getAuthDb().getUserByUUID(id);
                        LOGGER.log(Level.INFO, "Validating {0}''s access token", usera[0].getDisplayName());
                        if (Authenticator.validateAccessToken(profiles.getClientToken(), usera[0])) {
                            loggedIn.set(true);
                            setLoggedInOnce();
                            LOGGER.log(Level.INFO, "{0}''s access token is valid and will be reused.", usera[0].getDisplayName());
                        } else {
                            LOGGER.log(Level.INFO, "{0}''s access token is invalid.", usera[0].getDisplayName());
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e ) {
                        LOGGER.log(Level.INFO, "Could not validate {0}''s access token: {1}", new Object[] {usera[0].getDisplayName(), e.getMessage()});
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
                    usera[0] = profiles.getAuthDb().getUserByUUID(id);
                    LOGGER.log(Level.INFO, "Refreshing {0}''s access token", usera[0].getDisplayName());
                    try {
                        usera[0] = Authenticator.refreshAccessToken(profiles.getClientToken(), usera[0]);
                        loggedIn.set(true);
                        setLoggedInOnce();
                        profiles.getAuthDb().addUser(usera[0]);
                        reloadAuthDisplay(true);
                        profiles.save(true);
                        LOGGER.log(Level.INFO, "Refreshed {0}''s access token", usera[0].getDisplayName());
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e) {
                        LOGGER.log(Level.INFO, "Could not refresh {0}''s access token: {1}", new Object[] {usera[0].getDisplayName(), e.getMessage()});
                    }
                }
            };
            Task passMgrTask = new Task("Logging in with password manager") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() throws Exception {
                    if (loggedIn.get()) {
                        return;
                    }
                    // no password file
                    if (!pmgr.isPasswordSet()) {
                        return;
                    }
                    // passwords not decrypted
                    if (!pmgr.isDecrypted()) {
                        pd.setVisible(true);
                        if (pd.getResult() == PasswordDialogPanel.Result.CANCEL) {
                            return;
                        }
                        if (pd.getResult() == PasswordDialogPanel.Result.ACCEPT) {
                            storedPass.set(pd.getPassword());
                        }
                        if (!pmgr.decryptFile(pd.getPassword(), getProfiles().getClientToken())) {
                            LOGGER.log(Level.INFO, "Could not decrypt accounts file in password manager.");
                            throw new Exception("Could not decrypt accounts file.");
                        }
                        refreshPassManager();
                    }
                    // passwords decrypted
                    if (!pmgr.hasPassword(usera[0].getUsername())) {
                        return;
                    }
                    storedPass.set(pmgr.getPassword(usera[0].getUsername(), true, false, getProfiles().getClientToken()).toCharArray());
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
                    usera[0] = profiles.getAuthDb().getUserByUUID(id);
                    LOGGER.log(Level.INFO, "Logging in as {0} using user/pass", usera[0].getDisplayName());
                    try {
                        char[] pass;
                        if (storedPass.get() == null) {
                            pd.setCanDecrypt(false);
                            pd.setVisible(true);
                            pass = pd.getPassword();
                            if (pass == null) {
                                return;
                            }
                        } else {
                            pass = storedPass.get();
                        }
                        usera[0] = Authenticator.loginWithPassword(getProfiles().getClientToken(), usera[0].getUsername(), new String(pass));
                        loggedIn.set(true);
                        setLoggedInOnce();
                        profiles.getAuthDb().addUser(usera[0]);
                        reloadAuthDisplay(true);
                        profiles.save(true);
                        LOGGER.log(Level.INFO, "Logged in as {0}", usera[0].getDisplayName());
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e) {
                        LOGGER.log(Level.INFO, "Could not log in as {0} with user/pass: {1}", new Object[]{usera[0].getDisplayName(), e.getMessage()});
                        JOptionPane.showMessageDialog(getMainPanel(), "Could not log in as " + usera[0].getDisplayName());
                    }
                }
            };
            Task prepTask = new Task("Preparing game launch") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() {
                    LOGGER.log(Level.INFO, "Launching version {0} as {1}", new Object[]{p.getLastVersionId(), usera[0].getDisplayName()});
                    Dimension d = p.getResolution();
                    File gameDir = p.getGameDir();
                    if (gameDir == null) {
                        gameDir = mcHome;
                    }

                    Map<String, String> props = new HashMap<>();
                    props.put("launcher_name", "MCIGUI");
                    props.put("launcher_version", "3.0");

                    props.put("os.name", Launcher.OS_NAME.osName);
                    props.put("os.version", Launcher.OS_VER);
                    props.put("os.arch", Launcher.OS_ARCH);

                    props.put("features.is_demo_user", "false");
                    props.put("features.has_custom_resolution", String.valueOf(d != null));
                    if (d != null) {
                        props.put("resolution_width", Integer.toString(d.width));
                        props.put("resolution_height", Integer.toString(d.height));
                    } else {
                        props.put("resolution_width", "854");
                        props.put("resolution_height", "480");
                    }

                    props.put("auth_player_name", usera[0].getDisplayName());
                    props.put("auth_uuid", usera[0].getId().toString());
                    props.put("auth_access_token", usera[0].getAccessToken());
                    props.put("user_type", usera[0].getType());

                    props.put("game_directory", gameDir.getPath());
                    props.put("assets_root", new File(mcHome, "assets").getPath());

                    GameRunnerTask runner = new GameRunnerTask(
                            validFiles,
                            Launcher.this,
                            getJavaExe(),
                            mcHome,
                            profiles.getSelectedProfile().getLastVersionId(),
                            NATIVES_DIR,
                            gameDir,
                            true,
                            props,
                            getLoggedInOnce(),
                            p.getJavaArgs()
                    );
                    mainTaskMgr.addTask(runner);
                    mainTaskMgr.addTask(runner.getCancelTask());
                }
            };
            Task startTask = new Task("Starting game") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() {
                    if (loggedIn.get()) {
                        mainTaskMgr.addTask(prepTask);
                    }
                }
            };
            authTaskList.addTask(validateTask);
            authTaskList.addTask(refreshTask);
            authTaskList.addTask(passMgrTask);
            authTaskList.addTask(loginTask);
            authTaskList.addTask(startTask);
            authTaskMgr.addTask(authTaskList);
        }
    }
    
    public void setLoggedInOnce() {
        hasLoggedInOnce = true;
    }
    
    public boolean getLoggedInOnce() {
        return hasLoggedInOnce;
    }
    
    public void installVersions() {
        Task mainTask = new Task("Preparing to install versions") {
            @Override
            public float getProgress() {
                return -1.0F;
            }

            @Override
            public void run() {
                if (!getLoggedInOnce()) {
                    JOptionPane.showMessageDialog(mainWindow, "You must be logged in to install new versions.");
                    return;
                }
                VersionManifest manifest;
                try {
                    manifest = VersionManifest.getVersionManifest();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(mainWindow, "Could not connect to Mojang's servers.");
                    return;
                }
                ManifestViewerDialog mfViewer = new ManifestViewerDialog(mainWindow, manifest);
                List<ManifestGameVersion> vers = mfViewer.getVersions();
                if (!vers.isEmpty()) {
                    installVersionJsons(getMcHome(), manifest, vers, new ArrayList<>());
                }
            }
        };
        if (!getLoggedInOnce()) {
            UUID id = getCurrentUser().getId();
            AuthenticatedUser[] usera = {getCurrentUser()};
            TaskList authTaskList = new TaskList("Logging in as existing user");
            AtomicBoolean loggedIn = new AtomicBoolean(false);
            AtomicReference<char[]> storedPass = new AtomicReference<>();
            PasswordManager pmgr = getPasswordManager();
            PasswordDialog pd = PasswordDialog.getPasswordDialog(getMainWindow(), "Enter password for " + usera[0].getDisplayName(), pmgr.isPasswordSet() && !pmgr.isDecrypted());
            
            Task validateTask = new Task("Checking existing auth token") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        usera[0] = profiles.getAuthDb().getUserByUUID(id);
                        LOGGER.log(Level.INFO, "Validating {0}''s access token", usera[0].getDisplayName());
                        if (Authenticator.validateAccessToken(profiles.getClientToken(), usera[0])) {
                            loggedIn.set(true);
                            setLoggedInOnce();
                            LOGGER.log(Level.INFO, "{0}''s access token is valid and will be reused.", usera[0].getDisplayName());
                        } else {
                            LOGGER.log(Level.INFO, "{0}''s access token is invalid.", usera[0].getDisplayName());
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e ) {
                        LOGGER.log(Level.INFO, "Could not validate {0}''s access token: {1}", new Object[] {usera[0].getDisplayName(), e.getMessage()});
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
                    usera[0] = profiles.getAuthDb().getUserByUUID(id);
                    LOGGER.log(Level.INFO, "Refreshing {0}''s access token", usera[0].getDisplayName());
                    try {
                        usera[0] = Authenticator.refreshAccessToken(profiles.getClientToken(), usera[0]);
                        loggedIn.set(true);
                        setLoggedInOnce();
                        profiles.getAuthDb().addUser(usera[0]);
                        reloadAuthDisplay(true);
                        profiles.save(true);
                        LOGGER.log(Level.INFO, "Refreshed {0}''s access token", usera[0].getDisplayName());
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e) {
                        LOGGER.log(Level.INFO, "Could not refresh {0}''s access token: {1}", new Object[] {usera[0].getDisplayName(), e.getMessage()});
                    }
                }
            };
            Task passMgrTask = new Task("Logging in with password manager") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() throws Exception {
                    if (loggedIn.get()) {
                        return;
                    }
                    // no password file
                    if (!pmgr.isPasswordSet()) {
                        return;
                    }
                    // passwords not decrypted
                    if (!pmgr.isDecrypted()) {
                        pd.setVisible(true);
                        if (pd.getResult() == PasswordDialogPanel.Result.CANCEL) {
                            return;
                        }
                        if (pd.getResult() == PasswordDialogPanel.Result.ACCEPT) {
                            storedPass.set(pd.getPassword());
                        }
                        if (!pmgr.decryptFile(pd.getPassword(), getProfiles().getClientToken())) {
                            LOGGER.log(Level.INFO, "Could not decrypt accounts file in password manager.");
                            throw new Exception("Could not decrypt accounts file.");
                        }
                        refreshPassManager();
                    }
                    // passwords decrypted
                    if (!pmgr.hasPassword(usera[0].getUsername())) {
                        return;
                    }
                    storedPass.set(pmgr.getPassword(usera[0].getUsername(), true, false, getProfiles().getClientToken()).toCharArray());
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
                    usera[0] = profiles.getAuthDb().getUserByUUID(id);
                    LOGGER.log(Level.INFO, "Logging in as {0} using user/pass", usera[0].getDisplayName());
                    try {
                        char[] pass;
                        if (storedPass.get() == null) {
                            pd.setCanDecrypt(false);
                            pd.setVisible(true);
                            pass = pd.getPassword();
                            if (pass == null) {
                                return;
                            }
                        } else {
                            pass = storedPass.get();
                        }
                        usera[0] = Authenticator.loginWithPassword(getProfiles().getClientToken(), usera[0].getUsername(), new String(pass));
                        loggedIn.set(true);
                        setLoggedInOnce();
                        profiles.getAuthDb().addUser(usera[0]);
                        reloadAuthDisplay(true);
                        profiles.save(true);
                        LOGGER.log(Level.INFO, "Logged in as {0}", usera[0].getDisplayName());
                    } catch (IOException e) {
                        LOGGER.log(Level.WARNING, "Could not connect to Mojang's servers: {0}: {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
                        throw e;
                    } catch (ForbiddenOperationException | IllegalArgumentException e) {
                        LOGGER.log(Level.INFO, "Could not log in as {0} with user/pass: {1}", new Object[]{usera[0].getDisplayName(), e.getMessage()});
                        JOptionPane.showMessageDialog(getMainPanel(), "Could not log in as " + usera[0].getDisplayName());
                    }
                }
            };
            Task startTask = new Task("Installing game version") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() {
                    if (loggedIn.get()) {
                        mainTaskMgr.addTask(mainTask);
                    }
                }
            };
            authTaskList.addTask(validateTask);
            authTaskList.addTask(refreshTask);
            authTaskList.addTask(passMgrTask);
            authTaskList.addTask(loginTask);
            authTaskList.addTask(startTask);
            authTaskMgr.addTask(authTaskList);
        } else {
            mainTaskMgr.addTask(mainTask);
        }
    }
    
    public void installVersionJsons(File mcHome, VersionManifest manifest, List<ManifestGameVersion> versions, List<GameVersion> downloadFiles) {
        mainTaskMgr.addTask(new Task("Downloading JSON version info") {
            @Override
            public float getProgress() {
                return -1.0F;
            }

            @Override
            public void run() throws Exception {
                List<ManifestGameVersion> newVers = new ArrayList<>();
                for (ManifestGameVersion mver : versions) {
                    mver.installVersionJson(mcHome);
                    try {
                        GameVersion ver = mver.getGameVersion(mcHome);
                        downloadFiles.add(ver);
                        String parent = ver.getInheritsFrom();
                        while (parent != null) {
                            try {
                                ver = GameVersion.getGameVersion(mcHome, parent);
                                downloadFiles.add(ver);
                                parent = ver.getInheritsFrom();
                            } catch (Exception e) {
                                ManifestGameVersion mgf = manifest.getVersion(parent);
                                if (mgf == null) {
                                    JOptionPane.showMessageDialog(null, "Could not find version \"" + parent + "\" in version manifest, you will need to install it manually.");
                                } else {
                                    newVers.add(mgf);
                                }
                            }
                        }
                    } catch (Exception e) {
//                        continue;
                    }
                }
                if (!newVers.isEmpty()) {
                    installVersionJsons(mcHome, manifest, newVers, downloadFiles);
                } else {
                    TaskList downloadTask = new TaskList("Downloading required game files");
                    downloadFiles.forEach((ver) -> {
                        downloadTask.addTask(new GameFileDownloaderTask(validFiles, mcHome, ver.getId(), LOGGER));
                    });
                    downloadTask.addTask(new Task("Reloading version list") {
                        @Override
                        public float getProgress() {
                            return -1.0F;
                        }
                        
                        @Override
                        public void run() throws Exception {
                            reloadVersions();
                        }
                    });
                    mainTaskMgr.addTask(downloadTask);
                }
            }
        });
    }
    
    public void addGameMonitor(UUID id, GameMonitor monitor) {
        this.gameMonitorMap.put(id, monitor);
    }
    
    public void removeGameMonitor(UUID id) {
        this.gameMonitorMap.remove(id);
    }
    
    public void showAboutWindow() {
        aboutWindow.setVisible(true);
    }
    
    public File chooseDirectory(String title) {
        JFileChooser chooser = new JFileChooser(chooserCD);
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        int result = chooser.showOpenDialog(mainWindow);
        chooserCD = chooser.getCurrentDirectory();
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    public File chooseFile(String title) {
        JFileChooser chooser = new JFileChooser(chooserCD);
        chooser.setFileHidingEnabled(false);
        chooser.setDialogTitle(title);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        int result = chooser.showOpenDialog(mainWindow);
        chooserCD = chooser.getCurrentDirectory();
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    private static class LauncherExceptionLogger implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            LOGGER.log(Level.WARNING, "Problem in thread " + t.getName(), e);
        }
        
    }
}
