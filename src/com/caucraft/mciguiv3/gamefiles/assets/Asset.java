package com.caucraft.mciguiv3.gamefiles.assets;

import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.mciguiv3.launch.Launcher;
import java.io.File;
import java.nio.file.Paths;

/**
 *
 * @author caucow
 */
public class Asset {
    
    private final String id;
    private final String hash;
    private final String hashPrefix;
    private final long size;
    private final File assetFile;
    
    public Asset(String id, String hash, long size, File mcHome) {
        this.id = id;
        this.hash = hash;
        this.hashPrefix = hash.substring(0, 2);
        this.size = size;
        this.assetFile = Paths.get(mcHome.getPath(), "assets", "objects", hashPrefix, hash).toFile();
    }
    
    public String getId() {
        return id;
    }
    
    public String getHash() {
        return hash;
    }
    
    public String getHashPrefix() {
        return hashPrefix;
    }
    
    public long getSize() {
        return size;
    }
    
    public File getAssetFile() {
        return assetFile;
    }
    
    public String getUrl() {
        return String.format("http://resources.download.minecraft.net/%s/%s", hashPrefix, hash);
    }
    
    public boolean validateFiles(ValidGameFileSet validFiles) {
        File file = getAssetFile();
        if (validFiles != null && validFiles.isValid(file)) {
            return true;
        }
        boolean valid = file.exists() && size == file.length() && hash != null && hash.equalsIgnoreCase(Launcher.getFileSha1(file));
        if (valid) {
            validFiles.setValid(file);
        }
        return valid;
    }
}
