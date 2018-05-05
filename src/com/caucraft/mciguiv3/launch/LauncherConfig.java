package com.caucraft.mciguiv3.launch;

import com.caucraft.mciguiv3.json.JsonConfig;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class LauncherConfig {
    
    private Launcher launcher;
    private File configFile;
    private String mcHome;
    private String javaWindows;
    private String javaMac;
    private String javaLinux;
    private boolean offlineMode;
    private String offlineName;
    
    public LauncherConfig(Launcher launcher, File configFile) {
        this.launcher = launcher;
        this.configFile = configFile;
    }
    
    public void setJavaLoc(File f) {
        if (!f.exists()) {
            return;
        }
        f = Launcher.relativize(f);
        switch (Launcher.OS_NAME) {
            case WINDOWS:
                javaWindows = f.getPath();
            case OSX:
                javaMac = f.getPath();
            case LINUX:
                javaLinux = f.getPath();
        }
    }
    
    public File getJavaLoc() {
        switch (Launcher.OS_NAME) {
            case WINDOWS:
                return getJavaLoc(javaWindows);
            case OSX:
                return getJavaLoc(javaMac);
            case LINUX:
                return getJavaLoc(javaLinux);
        }
        return null;
    }
    
    private File getJavaLoc(String saved) {
        if (saved == null) {
            return getDefaultJavaLoc();
        }
        saved = saved.replace('\\', '/');
        File savedFile = Launcher.resolve(new File(saved));
        if (!savedFile.exists()) {
            return getDefaultJavaLoc();
        }
        try {
            return savedFile.toPath().toRealPath().toFile();
        } catch (IOException ioe) {
            return getDefaultJavaLoc();
        }
    }
    
    public static File getDefaultJavaLoc() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toFile();
    }
    
    public File getMcHome() {
        if (mcHome == null) {
            return null;
        }
        mcHome = mcHome.replace('\\', '/');
        File mcHomeFile = Launcher.resolve(new File(mcHome));
        if (!mcHomeFile.exists()) {
            return null;
        }
        try {
            return mcHomeFile.toPath().toRealPath().toFile();
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public void setMcHome(File f) {
        if (!f.exists()) {
            return;
        }
        mcHome = f.getPath();
    }
    
    public void setOfflineMode(boolean offline) {
        this.offlineMode = offline;
    }
    
    public void setOfflineName(String name) {
        this.offlineName = name;
    }
    
    public boolean getOfflineMode() {
        return offlineMode;
    }
    
    public String getOfflineName() {
        if (offlineName == null) {
            return offlineName = "Player";
        }
        if (!offlineName.matches("[A-Za-z0-9)]{1,16}")) {
            if (!(offlineName = offlineName.replaceAll("[^A-Za-z0-9)]", mcHome)).matches("[A-Za-z0-9)]{1,16}")) {
                offlineName = "Player";
            }
        }
        return offlineName;
    }
    
    public void save() {
        JsonConfig json = new JsonConfig();
        json.set("version", Launcher.VERSION);
        if (javaWindows != null)
            json.set("javaLoc.windows", javaWindows);
        if (javaMac != null)
            json.set("javaLoc.mac", javaMac);
        if (javaLinux != null)
            json.set("javaLoc.linux", javaLinux);
        if (mcHome != null)
            json.set("mcHome", mcHome);
        json.set("offline.enabled", offlineMode);
        json.set("offline.name", offlineName);
        try {
            json.save(configFile);
        } catch (IOException e) {
            Launcher.getLogger().log(Level.WARNING, "Unable to save configuration.", e);
        }
    }
    
    public void load() {
        JsonConfig json = new JsonConfig();
        try {
            json.load(configFile);
        } catch (JsonParseException | IOException ex) {
            
        }
        javaWindows = json.getString("javaLoc.windows", null);
        javaMac = json.getString("javaLoc.mac", null);
        javaLinux = json.getString("javaLoc.linux", null);
        mcHome = json.getString("mcHome", null);
        offlineMode = json.getBool("offline.enabled", false);
        offlineName = json.getString("offline.name", "Player");
    }
}
