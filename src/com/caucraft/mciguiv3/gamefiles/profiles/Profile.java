package com.caucraft.mciguiv3.gamefiles.profiles;

import com.caucraft.mciguiv3.json.JsonConfig;
import com.google.gson.JsonObject;
import java.awt.Dimension;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author caucow
 */
public class Profile implements Comparable<Profile> {
    
    private JsonConfig json;
    private String name;
    private File gameDir;
    private String lastVersionId;
    private File javaDir;
    private String javaArgs;
    private Dimension resolution;
    private Set<String> allowedReleaseTypes;
    
    public Profile(String name) {
        this.json = new JsonConfig();
        this.name = name;
        this.allowedReleaseTypes = new HashSet<>(4);
    }
    
    public Profile(String name, JsonConfig json) {
        this.json = json;
        this.name = json.getString("name", null);
        String gameDir = json.getString("gameDir", null);
        if (gameDir != null) {
            this.gameDir = new File(gameDir);
        }
        this.lastVersionId = json.getString("lastVersionId", null);
        if (this.name == null) {
            if (this.lastVersionId == null) {
                this.name = name.substring(0, Math.min(8, name.length()));
            } else {
                this.name = this.lastVersionId + " " + name.substring(0, Math.min(8, name.length()));
            }
        }
        String javaDir = json.getString("javaDir", null);
        if (javaDir != null) {
            this.javaDir = new File(javaDir);
        }
        this.javaArgs = json.getString("javaArgs", null);
        int w = json.getInt("resolution.width", 0);
        int h = json.getInt("resolution.height", 0);
        if (w > 0 && h > 0) {
            this.resolution = new Dimension(w, h);
        }
        List<Object> allowedReleaseTypes = json.getAsList("allowedReleaseTypes");
        if (allowedReleaseTypes != null) {
            this.allowedReleaseTypes = new HashSet<>(4);
            for (Object o : allowedReleaseTypes) {
                this.allowedReleaseTypes.add(o.toString());
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public File getGameDir() {
        return gameDir;
    }
    
    public String getLastVersionId() {
        return lastVersionId;
    }
    
    public File getJavaDir() {
        return javaDir;
    }
    
    public String getJavaArgs() {
        return javaArgs;
    }
    
    public Dimension getResolution() {
        return resolution;
    }
    
    public Set<String> getAllowedReleaseTypes() {
        return allowedReleaseTypes;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setGameDir(File gameDir) {
        this.gameDir = gameDir;
    }
    
    public void setLastVersionId(String lastVersionId) {
        this.lastVersionId = lastVersionId;
    }
    
    public void setJavaDir(File javaDir) {
        this.javaDir = javaDir;
    }
    
    public void setJavaArgs(String javaArgs) {
        this.javaArgs = javaArgs;
    }
    
    public void setResolution(Dimension resolution) {
        this.resolution = resolution;
    }
    
    public void setAllowedReleaseTypes(Set<String> allowedReleaseTypes) {
        this.allowedReleaseTypes.clear();
        this.allowedReleaseTypes.addAll(allowedReleaseTypes);
    }
    
    @Override
    public String toString() {
        return String.format("Profile{%s, %s, %s, %s, %s, %s, %s}", name, gameDir, lastVersionId, javaDir, javaArgs, resolution, allowedReleaseTypes);
    }
    
    public JsonConfig save() {
        JsonObject root = json.getRootElement().getAsJsonObject();
        json.set("name", name);
        if (gameDir == null) {
            root.remove("gameDir");
        } else {
            json.set("gameDir", gameDir.getPath());
        }
        if (lastVersionId == null) {
            root.remove("lastVersionId");
        } else {
            json.set("lastVersionId", lastVersionId);
        }
        if (javaDir == null) {
            root.remove("javaDir");
        } else {
            json.set("javaDir", javaDir.getPath());
        }
        if (javaArgs == null) {
            root.remove("javaArgs");
        } else {
            json.set("javaArgs", javaArgs);
        }
        if (resolution == null) {
            root.remove("resolution");
        } else {
            json.set("resolution.width", resolution.width);
            json.set("resolution.height", resolution.height);
        }
        if (allowedReleaseTypes == null) {
            root.remove("allowedReleaseTypes");
        } else {
            json.set("allowedReleaseTypes", allowedReleaseTypes);
        }
        return json;
    }

    @Override
    public int compareTo(Profile o) {
        return this.name.compareTo(o.name);
    }
    
}
