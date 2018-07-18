package com.caucraft.mciguiv3.gamefiles.versions.manifest;

import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.util.HttpPayload;
import com.caucraft.util.JsonConfig;
import com.caucraft.mciguiv3.util.Util;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.Date;

/**
 *
 * @author caucow
 */
public class ManifestGameVersion implements Comparable<ManifestGameVersion> {
    private final String id;
    private final String type;
    private final Date time;
    private final Date releaseTime;
    private final String url;

    public ManifestGameVersion(String id, String type, String time, String releaseTime, String url) {
        this.id = id;
        this.type = type;
        this.time = Util.getStandardDate(time);
        this.releaseTime = Util.getStandardDate(releaseTime);
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Date getTime() {
        return time;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int compareTo(ManifestGameVersion o) {
        int i = releaseTime.compareTo(o.releaseTime);
        if (i != 0)
            return i;
        i = time.compareTo(o.time);
        if (i != 0)
            return i;
        i = id.compareTo(o.id);
        return i;
    }

    @Override
    public String toString() {
        return String.format("%s:{%s %s %s %s}", id, type, time, releaseTime, url);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ManifestGameVersion)) {
            return false;
        }
        ManifestGameVersion v = (ManifestGameVersion)o;
        return id.equals(v.id)
                && type.equals(v.type)
                && time.equals(v.time)
                && releaseTime.equals(v.releaseTime)
                && url.equals(v.url);
    }
    
    public File getVersionJson(File mcHome) {
        return Paths.get(mcHome.getPath(), "versions", id, id + ".json").toFile();
    }

    public void installVersionJson(File mcHome) throws SocketTimeoutException, IOException {
        HttpPayload pl = HttpPayload.getPayload(url, "GET", null, null);
        if (pl.getResponseCode() == 200) {
            JsonConfig json = new JsonConfig(pl.getPayload());
            File jsonFile = getVersionJson(mcHome);
            File versionDir = jsonFile.getParentFile();
            if (!versionDir.exists()) {
                versionDir.mkdirs();
            }
            jsonFile.createNewFile();
            json.save(jsonFile);
        } else {
            throw new IOException(String.format("Non-200 (%d) response code received with payload: %s", pl.getResponseCode(), pl.getPayload()));
        }
    }

    public GameVersion getGameVersion(File mcHome) throws FileNotFoundException, JsonParseException, IOException {
        return GameVersion.getGameVersion(mcHome, id);
    }
}
