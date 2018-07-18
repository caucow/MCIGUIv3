package com.caucraft.mciguiv3.gamefiles.versions.manifest;

import com.caucraft.util.HttpPayload;
import com.caucraft.util.JsonConfig;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author caucow
 */
public class VersionManifest {
    public static final String MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private String latestRelease;
    private String latestSnap;
    private Map<String, ManifestGameVersion> versionMap;
    
    private VersionManifest() {
        versionMap = new HashMap<>();
    }
    
    public static VersionManifest getVersionManifest() throws SocketTimeoutException, IOException {
        VersionManifest manifest = new VersionManifest();
        manifest.download();
        return manifest;
    }
    
    private void download() throws SocketTimeoutException, IOException {
        HttpPayload pl = HttpPayload.getPayload(MANIFEST_URL, "GET", null, null);
        if (pl.getResponseCode() == 200) {
            JsonConfig json = new JsonConfig(pl.getPayload());
            latestRelease = json.getString("latest.release", null);
            latestSnap = json.getString("latest.snapshot", null);
            JsonArray a = json.getSubConfig("versions").getRootElement().getAsJsonArray();
            int size = a.size();
            for (int i = 0; i < size; ++i) {
                json = new JsonConfig(a.get(i));
                ManifestGameVersion vi = new ManifestGameVersion (
                        json.getString("id", null),
                        json.getString("type", null),
                        json.getString("time", null),
                        json.getString("releaseTime", null),
                        json.getString("url", null));
                versionMap.put(vi.getId(), vi);
            }
        } else {
            throw new IOException(String.format("Non-200 (%d) response code received with payload: %s", pl.getResponseCode(), pl.getPayload()));
        }
    }
    
    public ManifestGameVersion getVersion(String v) {
        return versionMap.get(v);
    }
    
    public String getLatestRelease() {
        return latestRelease;
    }
    
    public String getLatestSnapshot() {
        return latestSnap;
    }
    
    public Collection<ManifestGameVersion> getVersions() {
        return versionMap.values();
    }
}
