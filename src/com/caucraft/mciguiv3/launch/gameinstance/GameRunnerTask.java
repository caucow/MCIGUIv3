package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.gamefiles.util.Library;
import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.launch.LogPanel;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JOptionPane;

/**
 *
 * @author caucow
 */
public class GameRunnerTask extends TaskList {
    
    private Launcher launcher;
    private GameVersion version;
    private List<Library.CodeLibrary> libs;
    private List<Library.NativeLibrary> nats;
    private Logger logger;
    private Formatter logFormatter;
    private LogPanel logPanel;
    private LaunchInfo launchInfo;
    
    public GameRunnerTask(
            ValidGameFileSet validFiles,
            Launcher launcher,
            File javaExe,
            File mcHome,
            String versionId,
            File nativeParentDir,
            File gameDir,
            boolean errorDialogs,
            Map<String, String> properties,
            boolean canDownloadFiles,
            String extraJvmArgs) {
        super("Starting game (" + versionId + ")");
        
        this.launcher = launcher;
        
        this.addTask(new Task("Preparing launcher") {
            @Override
            public float getProgress() {
                return -1.0F;
            }
            
            @Override
            public void run() throws Exception {
                launchInfo = new LaunchInfo(properties.get("auth_player_name"), versionId);
                logger = Logger.getAnonymousLogger();
                logFormatter = new SimpleFormatter() {
                    @Override
                    public String format(LogRecord record) {
                        return record.getMessage() + "\n";
                    }
                };
                logPanel = new LogPanel(logger, logFormatter);
                String date = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss.SSS").format(new Date(System.currentTimeMillis()));
                File logFile = Paths.get(Launcher.LAUNCHER_DIR.getPath(), "Logs", launchInfo.getName() + " " + date + ".log").toFile();
                try {
                    logFile.getParentFile().mkdirs();
                    FileHandler fh = new FileHandler(logFile.getPath());
                    fh.setFormatter(logFormatter);
                    logger.addHandler(fh);
                } catch (IOException e) {
                    Launcher.getLogger().log(Level.WARNING, "Unable to write to log file: " + logFile, e);
                }
                launcher.getSecondaryTaskMgr().addTask(new Task("Adding game log panel") {
                    @Override
                    public float getProgress() {
                        return -1;
                    }

                    @Override
                    public void run() throws Exception {
                        int tab = launcher.getLauncherPanel().addTab(launchInfo.getName(), logPanel);
                        launcher.getLauncherPanel().setSelectedTab(tab);
                    }
                });
            }
        });
        
        File nativeDir = new File(nativeParentDir, "" + System.currentTimeMillis());
        if (!nativeDir.mkdirs()) {
            doError("Could not create natives directory.", null, true);
        } else {
            TaskList extractTask = new TaskList("Extracting natives", true);
            if (canDownloadFiles) {
                this.addTask(new GameFileDownloaderTask(validFiles, mcHome, versionId, logger));
            }
            this.addTask(new Task("Loading version information") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        version = GameVersion.getGameVersion(mcHome, versionId);
                    } catch (Exception e) {
                        doError("Could not load version JSON for " + versionId, e, errorDialogs);
                        throw e;
                    }
                    libs = new ArrayList<>();
                    nats = new ArrayList<>();

                    addPassingLibraries(version.getLibraries(), properties);
                    addPassingNatives(version.getNatives(), properties);

                    GameVersion nextVer = version;
                    while (nextVer.getInheritsFrom() != null && (nextVer = launcher.getVersionManager().getVersion(nextVer.getInheritsFrom())) != null) {
                        addPassingLibraries(nextVer.getLibraries(), properties);
                        addPassingNatives(nextVer.getNatives(), properties);
                    }
                    for (Library.NativeLibrary nativeLib : nats) {
                        File jfile = nativeLib.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH);
                        if (!jfile.exists()) {
                            continue;
                        }
                        JarFile jar = new JarFile(jfile);
                        extractTask.addTask(Util.getJarExtractTask("Extracting " + nativeLib.getName(), nativeDir, jar, nativeLib.getExcluded()));
                    }
                }
            });
            this.addTask(extractTask);
            this.addTask(new Task("Compiling launh arguments and starting game") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }
                
                @Override
                public void run() throws Exception {
                    StringBuilder classPath = new StringBuilder();
                    for (Library l : libs) {
                        classPath.append(l.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH));
                        classPath.append(File.pathSeparatorChar);
                    }
                    classPath.append(version.getVersionJarFile(mcHome).getPath());
                    
                    properties.put("version_name", version.getId());
                    properties.put("version_type", version.getType());
                    properties.put("assets_index_name", version.getAssets().getId());
                    properties.put("natives_directory", nativeDir.getPath());
                    properties.put("classpath", classPath.toString());
                    
                    List<String> gameArgs = version.getArgumentParser().compile(javaExe.getPath(), extraJvmArgs == null ? null : Arrays.asList(extraJvmArgs.split(" ")), properties, null);
                    
                    GameMonitor gmon = new GameMonitor(logPanel, logger, launcher, gameArgs, gameDir, nativeDir, GameRunnerTask.this.launchInfo);
                    gmon.start();
                }
            });
        }
    }
    
    private void addPassingLibraries(List<Library.CodeLibrary> list, Map<String, String> props) {
        list.stream().filter((lib) -> (lib.passRules(props))).forEachOrdered((lib) -> {
            libs.add(lib);
        });
    }
    
    private void addPassingNatives(List<Library.NativeLibrary> list, Map<String, String> props) {
        list.stream().filter((lib) -> (lib.passRules(props))).forEachOrdered((lib) -> {
            nats.add(lib);
        });
    }
    
    private void doError(String message, Throwable t, boolean dialog) {
        if (dialog) {
            JOptionPane.showMessageDialog(null, message);
        }
        Launcher.getLogger().log(Level.WARNING, message, t);
    }
    
    public Task getCancelTask() {
        return new Task("Verifying game launch") {
            @Override
            public float getProgress() {
                return -1.0F;
            }
            
            @Override
            public void run() throws Exception {
                if (GameRunnerTask.this.getState() == State.FAIL) {
                    launcher.getSecondaryTaskMgr().addTask(new Task("Removing game log panel") {
                        @Override
                        public float getProgress() {
                            return -1.0F;
                        }

                        @Override
                        public void run() throws Exception {
                            try {
                                for (Handler h : logger.getHandlers()) {
                                    h.close();
                                }
                                launcher.getPastRunsPanel().addRun(launchInfo, logPanel.getLogTextArea().getDocument());
                            } catch (Exception e) {
                                Launcher.LOGGER.log(Level.WARNING, "Unable to remove cancelled game launch panel.", e);
                            }
                        }
                    });
                }
            }
        };
    }
}
