package com.caucraft.mciguiv3.gamefiles.assets;

import com.caucraft.mciguiv3.gamefiles.util.ValidGameFileSet;
import com.caucraft.util.HttpPayload;
import com.caucraft.util.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 * @author caucow
 */
public class AssetIndexInfo {
    
    private final String id;
    private final String sha_1;
    private final long size;
    private final String url;
    private final long totalSize;
    
    public AssetIndexInfo(String id, String sha_1, long size, String url, long totalSize) {
        this.id = Objects.requireNonNull(id);
        this.sha_1 = Objects.requireNonNull(sha_1);
        this.size = size;
        this.url = Objects.requireNonNull(url);
        this.totalSize = totalSize;
    }
    
    public String getId() {
        return id;
    }
    
    public String getSha_1() {
        return sha_1;
    }
    
    public long getSize() {
        return size;
    }
    
    public String getUrl() {
        return url;
    }
    
    public long getTotalSize() {
        return totalSize;
    }
    
    public File getAssetIndexFile(File mcHome) {
        return Paths.get(mcHome.getPath(), "assets", "indexes", id + ".json").toFile();
    }
    
    public AssetIndex loadAssetIndex(File mcHome) throws FileNotFoundException, IOException, JsonParseException {
        return AssetIndex.loadAssetIndex(mcHome, this);
    }
    
    public boolean validateFiles(ValidGameFileSet validFiles, File mcHome) {
        File indexFile = getAssetIndexFile(mcHome);
        if (validFiles != null && validFiles.isValid(indexFile)) {
            return true;
        }
        boolean valid = indexFile.exists() && size == indexFile.length() && sha_1 != null && sha_1.equalsIgnoreCase(Launcher.getFileSha1(indexFile));
        if (valid) {
            validFiles.setValid(indexFile);
        }
        return valid;
    }
    
    public void installAssetIndex(File mcHome) throws SocketTimeoutException, IOException {
        HttpPayload pl = HttpPayload.getPayload(url, "GET", null, null);
        if (pl.getResponseCode() == 200) {
            JsonConfig json = new JsonConfig(pl.getPayload());
            File jsonFile = getAssetIndexFile(mcHome);
            File indexDir = jsonFile.getParentFile();
            if (!indexDir.exists()) {
                indexDir.mkdirs();
            }
            jsonFile.createNewFile();
            json.save(jsonFile);
        } else {
            throw new IOException(String.format("Non-200 (%d) response code received with payload: %s", pl.getResponseCode(), pl.getPayload()));
        }
    }
}
