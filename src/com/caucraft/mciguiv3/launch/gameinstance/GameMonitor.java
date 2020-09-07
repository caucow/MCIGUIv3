package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.launch.LogPanel;
import com.caucraft.mciguiv3.util.Task;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author caucow
 */
public class GameMonitor extends Thread {
    
    private final Launcher launcher;
    private final List<String> args;
    private final File cd;
    private final File nativeDir;
    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final LogPanel logPanel;
    private final LaunchInfo launchInfo;
    private Process gameProc;
    private UUID id;
    private final boolean debug;
    
    public GameMonitor(LogPanel logPanel, Logger logger, Launcher launcher, List<String> args, File cd, File nativeDir, LaunchInfo launchInfo, boolean debug) {
        this.logPanel = logPanel;
        this.logger = logger;
        this.launcher = launcher;
        this.args = args;
        this.cd = cd;
        this.nativeDir = nativeDir;
        this.launchInfo = launchInfo;
        this.setDaemon(true);
        this.debug = debug;
    }
    
    public void killProcess() {
        if (gameProc != null) {
            gameProc.destroy();
        }
        this.interrupt();
    }
    
    @Override
    public void run() {
        try {
            launcher.addGameMonitor(this.id = UUID.randomUUID(), this);
            if (debug) {
                this.logger.log(Level.INFO, "Started game in {0} with launch arguments [full]:", cd.toString());
                this.logger.info(args.toString());
            }
            boolean cleanExit = false;
            gameProc = new ProcessBuilder(args).directory(cd).redirectErrorStream(true).start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(gameProc.getInputStream()));
            launchInfo.setStartTime();
            
            String line;
            while ((line = stdout.readLine()) != null) {
                this.logger.info(line);
            }
            try {
                gameProc.waitFor();
                cleanExit = true;
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            launchInfo.setCloseTime();
            
            if (cleanExit) {
                int exitCode;
                try {
                    exitCode = gameProc.exitValue();
                } catch (Exception e2) {
                    exitCode = 0;
                }
                if (exitCode == 0) {
                    logger.info("Game ended normally (exit code 0).");
                } else {
                    logger.log(Level.INFO, "Game ended unexpectedly with exit code {0}.", exitCode);
                }

                logger.info("Deleting natives...");

                Launcher.deleteFileTree(nativeDir, false);
                if (nativeDir.exists()) {
                    logger.info("Unable to delete natives.");
                    logger.info("Remaining natives will be deleted when the launcher restarts.");
                } else {
                    logger.info("Successfully deleted natives.");
                }
            }
            for (Handler h : logger.getHandlers()) {
                h.close();
            }
            launcher.getSecondaryTaskMgr().addTask(new Task("Removing game log panel") {
                @Override
                public float getProgress() {
                    return -1.0F;
                }

                @Override
                public void run() throws Exception {
                    try {
                        launcher.getPastRunsPanel().addRun(launchInfo, logPanel.getLogTextArea().getDocument());
                    } catch (Exception e) {
                        Launcher.LOGGER.log(Level.WARNING, "Unable to remove stopped game launch panel.", e);
                    }
                }
            });
        } catch (IOException e) {
            Launcher.getLogger().log(Level.WARNING, "Unable to start game " + launchInfo.getName(), e);
        } finally {
            launcher.removeGameMonitor(this.id);
        }
        launcher.getSecondaryTaskMgr().addTask(new Task("Removing game log panel") {
            @Override
            public float getProgress() {
                return -1.0F;
            }
            
            @Override
            public void run() throws Exception {
                launcher.getLauncherPanel().removeTab(logPanel);
            }
        });
    }
    
    public void closeHandlers() {
        for (Handler h : logger.getHandlers()) {
            h.close();
        }
    }
}
