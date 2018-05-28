package com.caucraft.mciguiv3.gamefiles.util;

import com.caucraft.mciguiv3.json.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public abstract class Library {
    
    protected String name;
    protected String path;
    protected String filename;
    protected List<Rule> rules;
    protected Download download;
    protected Map<String, Download> classifiers;
    
    private Library(String name, Download download, Map<String, Download> classifiers) {
        this.name = name;
        String[] spl = name.split(":");
        this.path = String.format("%s/%s/%s", spl[0].replace('.', '/'), spl[1], spl[2]);
        this.filename = spl[1] + '-' + spl[2];
        this.download = download;
        this.classifiers = classifiers;
    }
    
    public void addRule(Rule r) {
        if (rules == null) {
            rules = new ArrayList<>();
        }
        rules.add(r);
    }
    
    public boolean validateFiles(ValidGameFileSet validFiles, File mcHome, String os, String osArch) {
        File libFile = getLibraryFile(mcHome, os, osArch);
        Download dl = getDownload(os);
        if (dl == null) {
            return true;
        }
        return dl.validateFile(validFiles, libFile);
    }
    
    public boolean passRules(Map<String, String> properties) {
        if (rules == null || rules.isEmpty()) {
            return true;
        }
        boolean pass = false;
        for (Rule r : rules) {
            pass = r.test(properties, pass);
        }
        return pass;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Rule> getRules() {
        return rules;
    }
    
    public Download getDownload(String os) {
        return download;
    }
    
    public File getLibraryFile(File mcHome, String os, String osArch) {
        return Paths.get(mcHome.getPath(), "libraries", path, filename + ".jar").toFile();
    }
    
    public static Library getLibrary(JsonConfig json) {
        String name = json.getString("name", null);
        JsonConfig sub =  json.getSubConfig("downloads.artifact");
        Download download = null;
        if (sub != null) {
            download = new Download(
                    sub.getLong("size", 0),
                    sub.getString("sha1", null),
                    sub.getString("url", null));
        }
        sub = json.getSubConfig("downloads.classifiers");
        Map<String, Download> classifiers = null;
        if (sub != null) {
            classifiers = new HashMap<>();
            for (String s : sub.getKeys("")) {
                JsonConfig cJson = sub.getSubConfig("[\"" + s + "\"]");
                classifiers.put(s, new Download(
                        cJson.getLong("size", 0),
                        cJson.getString("sha1", null),
                        cJson.getString("url", null)));
            }
        }
        if (download == null && classifiers == null) {
            String tmpurl = json.getString("url", null);
            String[] path = name.split(":");
            if (path.length != 3) {
                Launcher.LOGGER.log(Level.WARNING, "Non-standard library name: {0}", name);
            } else {
                String subpath = path[0].replace('.', '/') + '/' + path[1] + '/' + path[2] + '/' + path[1] + '-' + path[2] + ".jar";
                if (tmpurl == null) {
                    download = new Download(0, null, "https://libraries.minecraft.net/" + subpath);
                } else {
                    download = new Download(0, null, tmpurl + subpath);
                }
            }
        }
        Library lib;
        sub = json.getSubConfig("natives");
        if (sub == null) {
            lib = new CodeLibrary(name, download, classifiers);
        } else {
            final Map<String, String> natives = new HashMap<>();
            for (Map.Entry<String, Object> e : sub.getAsMap("").entrySet()) {
                natives.put(e.getKey(), e.getValue().toString());
            }
            Set<String> exclude = null;
            sub = json.getSubConfig("extract.exclude");
            if (sub != null) {
                exclude = new LinkedHashSet<>();
                JsonArray a = sub.getRootElement().getAsJsonArray();
                for (JsonElement e : a) {
                    exclude.add(e.getAsString());
                }
            }
            lib = new NativeLibrary(name, download, classifiers, natives, exclude);
        }
        sub = json.getSubConfig("rules");
        if (sub != null) {
            JsonArray a = sub.getRootElement().getAsJsonArray();
            for (JsonElement e : a) {
                sub = new JsonConfig(e);
                final boolean finalValue = sub.getString("action", "allow").equals("allow");
                final String finalOsName = sub.getString("os.name", null);
                final String finalOsVer = sub.getString("os.version", null);
                if (finalOsName == null && finalOsVer == null) {
                    lib.addRule((Map<String, String> props, boolean passing) -> finalValue);
                }
                if (finalOsName != null) {
                    lib.addRule((Map<String, String> props, boolean passing)
                            -> props.get("os.name").matches(finalOsName) ? finalValue : passing);
                }
                if (finalOsVer != null) {
                    lib.addRule((Map<String, String> props, boolean passing)
                            -> props.get("os.version").matches(finalOsVer) ? finalValue : passing);
                }
            }
        }
        return lib;
    }
    
    @Override
    public String toString() {
        return String.format("\"%s\"{%d Rules, %s %s}", name, rules.size(), download, classifiers);
    }
    
    public static class CodeLibrary extends Library {
        
        private CodeLibrary(String name, Download download, Map<String, Download> classifiers) {
            super(name, download, classifiers);
        }
    }
    
    public static class NativeLibrary extends Library {
        
        private final Map<String, String> natives;
        private final Iterable<String> exclude;
        
        private NativeLibrary(String name, Download download, Map<String, Download> classifiers, Map<String, String> natives, Iterable<String> exclude) {
            super(name, download, classifiers);
            this.natives = natives;
            this.exclude = exclude;
        }
        
        @Override
        public boolean validateFiles(ValidGameFileSet validFiles, File mcHome, String os, String osArch) {
            String osNatives = natives.get(os);
            if (osNatives == null || classifiers == null) {
                return super.validateFiles(validFiles, mcHome, os, osArch);
            }
            Download dl = getDownload(os);
            File libFile = getLibraryFile(mcHome, os, osArch);
            return (dl != null && dl.validateFile(validFiles, libFile)) || (dl == null && libFile.exists());
        }
        
        @Override
        public File getLibraryFile(File mcHome, String os, String osArch) {
            String osNatives = natives.get(os);
            if (osNatives == null) {
                return super.getLibraryFile(mcHome, os, osArch);
            }
            return Paths.get(mcHome.getPath(), "libraries", path, filename + "-" + osNatives.replace("${arch}", osArch) + ".jar").toFile();
        }
        
        @Override
        public Download getDownload(String os) {
            String osNatives = natives.get(os);
            if (osNatives == null || classifiers == null) {
                return super.getDownload(os);
            }
            return classifiers.get(osNatives);
        }
        
        public Iterable<String> getExcluded() {
            return exclude;
        }
        
        public boolean isFileExcluded(String file) {
            if (exclude == null) {
                return false;
            }
            for (String s : exclude) {
                if (file.startsWith(s)) {
                    return true;
                }
            }
            return false;
        }
    }
}
