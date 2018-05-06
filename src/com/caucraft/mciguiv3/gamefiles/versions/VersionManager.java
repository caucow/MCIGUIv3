package com.caucraft.mciguiv3.gamefiles.versions;

import com.caucraft.mciguiv3.gamefiles.versions.manifest.VersionManifest;
import com.caucraft.mciguiv3.launch.Launcher;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class VersionManager {
    
    private final File mcHome;
    private VersionManifest manifest;
    private Map<String, GameVersion> versionMap;
    
    public VersionManager(File mcHome) {
        this.mcHome = mcHome;
        versionMap = new HashMap<>();
    }
    
    public void load() {
        File[] vers = new File(mcHome, "versions").listFiles();
        versionMap.clear();
        if (vers != null) {
            for (File ver : vers) {
                try {
                    GameVersion gameVer = GameVersion.getGameVersion(
                            mcHome,
                            ver.getName());
                    versionMap.put(gameVer.getId(), gameVer);
                } catch (IOException e) {
                } catch (Exception e) {
                    Launcher.LOGGER.log(Level.WARNING, "Could not load version " + ver.getName(), e);
                }
            }
        }
    }
    
    public VersionManifest getVersionManifest() {
        if (manifest == null) {
            downloadManifest();
        }
        return manifest;
    }
    
    public boolean downloadManifest() {
        try {
            manifest = VersionManifest.getVersionManifest();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public Collection<GameVersion> getVersions() {
        return new TreeSet<>(versionMap.values()).descendingSet();
    }
    
    public GameVersion getVersion(String name) {
        return versionMap.get(name);
    }
    
}
