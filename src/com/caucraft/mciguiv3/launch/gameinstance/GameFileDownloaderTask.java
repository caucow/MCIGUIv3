package com.caucraft.mciguiv3.launch.gameinstance;

import com.caucraft.mciguiv3.gamefiles.assets.Asset;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndex;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndexInfo;
import com.caucraft.mciguiv3.gamefiles.util.Download;
import com.caucraft.mciguiv3.gamefiles.util.Library;
import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.Util;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author caucow
 */
public class GameFileDownloaderTask extends TaskList {
    
    public GameFileDownloaderTask(ValidGameFileSet validFiles, File mcHome, String versionId, boolean errorDialogs) {
        super("Checking files for " + versionId);
        
        Set<String> assetIds = new HashSet<>();
        List<Asset> assets = new ArrayList<>();
        List<Library> libs = new ArrayList<>();
        TaskList missingGame = new TaskList("Downloading version jar", true);
        TaskList missingAssets = new TaskList("Downloading assets", true);
        TaskList missingLibs = new TaskList("Downloading libraries", true);
        
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
                    if (!indexInfo.validateFiles(validFiles, mcHome)) {
                        try {
                            indexInfo.installAssetIndex(mcHome);
                        } catch (IOException e) {
                            doError("Could not download asset index for " + curVerName, e, errorDialogs);
                            throw e;
                        }
                    }
                    AssetIndex index;
                    try {
                        index = indexInfo.loadAssetIndex(mcHome);
                    } catch (Exception e) {
                        doError("Could not load asset index for " + curVerName, e, errorDialogs);
                        throw e;
                    }
                    assets.addAll(index.getAssets());
                    assetIds.add(indexInfo.getId());
                    libs.addAll(parent.getLibraries());
                    libs.addAll(parent.getNatives());
                    checkedVersions.add(parent.getId());
                    
                    while ((curVerName = parent.getInheritsFrom()) != null && !checkedVersions.contains(curVerName)) {
                        parent = GameVersion.getGameVersion(mcHome, curVerName);
                        indexInfo = version.getAssets();
                        if (!assetIds.contains(indexInfo.getId())) {
                            if (!indexInfo.validateFiles(validFiles, mcHome)) {
                                try {
                                    indexInfo.installAssetIndex(mcHome);
                                } catch (IOException e) {
                                    doError("Could not download asset index for " + curVerName, e, errorDialogs);
                                    throw e;
                                }
                            }
                            try {
                                index = indexInfo.loadAssetIndex(mcHome);
                            } catch (Exception e) {
                                doError("Could not load asset index for " + curVerName, e, errorDialogs);
                                throw e;
                            }
                            assets.addAll(index.getAssets());
                            assetIds.add(indexInfo.getId());
                        }
                        libs.addAll(parent.getLibraries());
                        libs.addAll(parent.getNatives());
                        checkedVersions.add(parent.getId());
                    }
                } catch (Exception e) {
                    doError("Could not open version JSON for " + curVerName + ". Try reinstalling it.", e, true);
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
                        missingLibs.addTask(Util.getFileDownloadTask(
                                "Downloading " + lib.getName(),
                                lib.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH),
                                dl.getUrl(), dl.getSize()));
                    }
                    cur++;
                    updateProgress();
                }
            }
        });
        this.addTask(missingGame);
        this.addTask(missingAssets);
        this.addTask(missingLibs);
    }
    
    private void doError(String message, Throwable t, boolean dialog) {
        if (dialog) {
            JOptionPane.showMessageDialog(null, message);
        }
        Launcher.getLogger().log(Level.WARNING, message, t);
    }
}
