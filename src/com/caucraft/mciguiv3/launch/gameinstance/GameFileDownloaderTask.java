package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.gamefiles.assets.Asset;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndex;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndexInfo;
import com.caucraft.mciguiv3.gamefiles.util.Download;
import com.caucraft.mciguiv3.gamefiles.util.Library;
import com.caucraft.mciguiv3.gamefiles.util.LoggingData;
import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.Util;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author caucow
 */
public class GameFileDownloaderTask extends TaskList {
    
    public GameFileDownloaderTask(ValidGameFileSet validFiles, File mcHome, String versionId, Logger errorLogger) {
        super("Checking files for " + versionId);
        Logger logger = errorLogger == null ? Launcher.LOGGER : errorLogger;
        
        Set<String> assetIds = new HashSet<>();
        List<Asset> assets = new ArrayList<>();
        List<Library> libs = new ArrayList<>();
        LoggingData[] logConfigHolder = new LoggingData[1];
        TaskList missingGame = new TaskList("Downloading version jar", true);
        TaskList missingAssets = new TaskList("Downloading assets", true);
        TaskList missingLibs = new TaskList("Downloading libraries", true);
        TaskList missingLogging = new TaskList("Downloading logging config", true);
        
        this.addTask(new Task("Loading version JSON(s)") {
            @Override
            public float getProgress() {
                return -1;
            }
            
            @Override
            public void run() throws Exception {
                String curVerName = versionId;
                try {
                    Set<String> checkedVersions = new HashSet<>();
                    GameVersion version = GameVersion.getGameVersion(mcHome, versionId);
                    Download client = version.getClientDownload();
                    File versionJar = version.getVersionJarFile(mcHome);
                    if (client != null && !client.validateFile(validFiles, versionJar)) {
                        missingGame.addTask(Util.getFileDownloadTask("Downloading " + version.getId() + ".jar", version.getVersionJarFile(mcHome), client.getUrl(), client.getSize()));
                    }
                    GameVersion parent = version;
                    
                    AssetIndexInfo indexInfo = version.getAssets();
                    AssetIndex index;
                    if (indexInfo != null) {
                        if (!indexInfo.validateFiles(validFiles, mcHome)) {
                            try {
                                indexInfo.installAssetIndex(mcHome);
                            } catch (IOException e) {
                                logger.log(Level.WARNING, "Could not download asset index for " + curVerName, e);
                                throw e;
                            }
                        }
                        try {
                            index = indexInfo.loadAssetIndex(mcHome);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Could not load asset index for " + curVerName, e);
                            throw e;
                        }
                        assets.addAll(index.getAssets());
                        assetIds.add(indexInfo.getId());
                    }
                    libs.addAll(parent.getLibraries());
                    libs.addAll(parent.getNatives());
                    checkedVersions.add(parent.getId());
                    
                    while ((curVerName = parent.getInheritsFrom()) != null && !checkedVersions.contains(curVerName)) {
                        parent = GameVersion.getGameVersion(mcHome, curVerName);
                        indexInfo = version.getAssets();
                        if (indexInfo != null && !assetIds.contains(indexInfo.getId())) {
                            if (!indexInfo.validateFiles(validFiles, mcHome)) {
                                try {
                                    indexInfo.installAssetIndex(mcHome);
                                } catch (IOException e) {
                                    logger.log(Level.WARNING, "Could not download asset index for " + curVerName, e);
                                    throw e;
                                }
                            }
                            try {
                                index = indexInfo.loadAssetIndex(mcHome);
                            } catch (Exception e) {
                                logger.log(Level.WARNING, "Could not load asset index for " + curVerName, e);
                                throw e;
                            }
                            assets.addAll(index.getAssets());
                            assetIds.add(indexInfo.getId());
                        }
                        libs.addAll(parent.getLibraries());
                        libs.addAll(parent.getNatives());
                        checkedVersions.add(parent.getId());
                    }
                    logConfigHolder[0] = version.getLoggingData();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not open version JSON for " + curVerName + ". Try reinstalling it.", e);
                    throw e;
                }
            }
        });
        this.addTask(new Task("Verifying assets") {
            
            int cur = 0;
            int total = 1;
            
            @Override
            public float getProgress() {
                return (float)cur / (float)total;
            }
            
            @Override
            public void run() throws Exception {
                total = assets.size();
                for (Asset a : assets) {
                    if (!a.validateFiles(validFiles)) {
                        missingAssets.addTask(Util.getFileDownloadTask("Downloading " + a.getId(), a.getAssetFile(), a.getUrl(), a.getSize()));
                    }
                    ++cur;
                    updateProgress();
                }
            }
        });
        this.addTask(new Task("Verifying libraries"){
            
            int cur = 0;
            int total = 1;
            
            @Override
            public float getProgress() {
                return (float)total / (float)cur;
            }
            
            @Override
            public void run() throws Exception {
                total = libs.size();
                for (Library lib : libs) {
                    if (!lib.validateFiles(validFiles, mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH)) {
                        Download dl = lib.getDownload(Launcher.OS_NAME.osName);
                        if (dl == null) {
                            logger.log(Level.WARNING, "Library {0} is missing and can not be downloaded.", lib.getName());
                        } else {
                            missingLibs.addTask(Util.getFileDownloadTask(
                                    "Downloading " + lib.getName(),
                                    lib.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH),
                                    dl.getUrl(), dl.getSize()));
                        }
                    }
                    cur++;
                    updateProgress();
                }
            }
        });
        this.addTask(new Task("Verifying logging config") {
            
            @Override
            public float getProgress() {
                return -1;
            }
            
            @Override
            public void run() throws Exception {
                LoggingData logConfig = logConfigHolder[0];
                if (logConfig == null) {
                    return;
                }
                File logCfgFile = Paths.get(mcHome.getPath(), logConfig.getFileName()).toFile();
                Download logDl = logConfig.getDownload();
                if (logDl == null) {
                    return;
                }
                if (!logDl.validateFile(validFiles, logCfgFile)) {
                    missingLogging.addTask(Util.getFileDownloadTask(
                            "Downloading log config " + logConfig.getFileName(),
                            logCfgFile, logDl.getUrl(), logDl.getSize()));
                }
            }
        });
        this.addTask(missingGame);
        this.addTask(missingAssets);
        this.addTask(missingLibs);
        this.addTask(missingLogging);
    }
}
