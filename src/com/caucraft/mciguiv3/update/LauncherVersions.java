package com.caucraft.mciguiv3.update;

import com.caucraft.util.HttpPayload;
import com.caucraft.util.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class LauncherVersions {
    
    private String currentVersion;
    private List<String> upcoming;
    private final HashMap<String, LauncherVersion> versions;
    
    private LauncherVersions() {
        versions = new HashMap<>();
    }
    
    public LauncherVersion getVersion(String id) {
        return versions.get(id);
    }
    
    public Collection<LauncherVersion> getVersions() {
        return versions.values();
    }
    
    public String getCurrentVersion() {
        return currentVersion;
    }
    
    public List<String> getUpcoming() {
        return upcoming == null || upcoming.isEmpty() ? null : upcoming;
    }
    
    public static LauncherVersions getLauncherVersions(Launcher launcher) throws IOException {
        LauncherVersions vers = new LauncherVersions();
        HttpPayload pl = HttpPayload.getPayload(Launcher.UPDATE_URL, "GET", null, null);
        if (pl.getResponseCode() == 200) {
            JsonConfig json = new JsonConfig(pl.getPayload());
            vers.currentVersion = Objects.requireNonNull(json.getString("currentVersion", null));
            for (String s : json.getKeys("versions")) {
                try {
                    JsonConfig jver = json.getSubConfig("versions[\"" + s + "\"]");
                    LauncherVersion ver = LauncherVersion.getVersion(launcher, s, jver);
                    vers.versions.put(s, ver);
                } catch (Exception e) {
                    Launcher.LOGGER.log(Level.INFO, "Problem loading launcher version", e);
                }
            }
            List<Object> upcoming = json.getAsList("upcoming");
            if (upcoming != null) {
                vers.upcoming = new ArrayList<>(upcoming.size());
                for (Object o : upcoming) {
                    vers.upcoming.add(o.toString());
                }
            }
        }
        return vers;
    }
}
