package com.caucraft.mciguiv3.gamefiles.versions;

import com.caucraft.mciguiv3.gamefiles.util.Library;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndexInfo;
import com.caucraft.mciguiv3.gamefiles.util.Rule;
import com.caucraft.mciguiv3.gamefiles.util.Download;
import com.caucraft.mciguiv3.gamefiles.util.Argument;
import com.caucraft.mciguiv3.gamefiles.util.ArgumentParser;
import com.caucraft.mciguiv3.json.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class GameVersion implements Comparable<GameVersion> {
    
    private AssetIndexInfo assets;
    private String inheritsFrom;
    private String id;
    private String mainClass;
    private ArgumentParser args;
    private Download clientDownload;
    private Download serverDownload;
    private int minimumLauncherVersion;
    private Date time;
    private Date releaseTime;
    private String type;
    private final List<Library.CodeLibrary> libraries;
    private final List<Library.NativeLibrary> natives;
    
    private GameVersion() {
        libraries = new ArrayList<>();
        natives = new ArrayList<>();
    }
    
    public AssetIndexInfo getAssets() {
        return assets;
    }
    
    public String getInheritsFrom() {
        return inheritsFrom;
    }
    
    public String getId() {
        return id;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public ArgumentParser getArgumentParser() {
        return args;
    }
    
    public Download getClientDownload() {
        return clientDownload;
    }
    
    public Download getServerDownload() {
        return serverDownload;
    }
    
    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }
    
    public Date getTime() {
        return time;
    }
    
    public Date getReleaseTime() {
        return releaseTime;
    }
    
    public String getType() {
        return type;
    }
    
    public List<Library.CodeLibrary> getLibraries() {
        return libraries;
    }
    
    public List<Library.NativeLibrary> getNatives() {
        return natives;
    }
    
    public File getVersionJsonFile(File mcHome) {
        return Paths.get(mcHome.getPath(), "versions", id, id + ".json").toFile();
    }
    
    public File getVersionJarFile(File mcHome) {
        return Paths.get(mcHome.getPath(), "versions", id, id + ".jar").toFile();
    }

    @Override
    public int compareTo(GameVersion o) {
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
    public boolean equals(Object o) {
        if (!(o instanceof GameVersion)) {
            return false;
        }
        GameVersion v = (GameVersion)o;
        return id.equals(v.id)
                && type.equals(v.type)
                && time.equals(v.time)
                && releaseTime.equals(v.releaseTime)
                && inheritsFrom.equals(v.inheritsFrom)
                && mainClass.equals(v.mainClass);
    }
    
    public static GameVersion getGameVersion(File mcHome, String versionId) throws FileNotFoundException, JsonParseException, IOException {
        File jsonFile = Paths.get(mcHome.getPath(), "versions", versionId, versionId + ".json").toFile();
        if (!jsonFile.exists()) {
            throw new FileNotFoundException("Game version file does not exist: " + jsonFile);
        }
        JsonConfig json = new JsonConfig();
        json.load(jsonFile);
        GameVersion ver = new GameVersion();
        ver.assets = new AssetIndexInfo(
                json.getString("assetIndex.id", null),
                json.getString("assetIndex.sha1", null),
                json.getLong("assetIndex.size", 0),
                json.getString("assetIndex.url", null),
                json.getLong("assetIndex.totalSize", 0));
        ver.id = Objects.requireNonNull(json.getString("id", null));
        ver.mainClass = Objects.requireNonNull(json.getString("mainClass", null));
        ver.minimumLauncherVersion = json.getInt("minimumLauncherVersion", 21);
        if (ver.minimumLauncherVersion > Launcher.VERSION) {
            Launcher.LOGGER.log(
                    Level.WARNING,
                    "MCIGUI currently only supports minimumLauncherVersion up to {0}, but {1}''s is {2}.",
                    new Object[] {
                        Launcher.VERSION,
                        ver.id,
                        ver.minimumLauncherVersion
                    }
            );
            Launcher.LOGGER.log(Level.WARNING, "Consider updating or nagging the developer if there is no update.");
        }
        ver.releaseTime = Util.getStandardDate(json.getString("releaseTime", null));
        if (ver.releaseTime == null) {
            ver.releaseTime = Date.from(Instant.ofEpochMilli(jsonFile.lastModified()));
        }
        ver.time = Util.getStandardDate(json.getString("releaseTime", null));
        if (ver.time == null) {
            ver.time = Date.from(Instant.ofEpochMilli(jsonFile.lastModified()));
        }
        ver.type = json.getString("type", null);
        JsonConfig sub = json.getSubConfig("downloads.client");
        if (sub != null) {
            ver.clientDownload = new Download(
                    sub.getLong("size", 0), sub.getString("sha1", null), sub.getString("url", null));
        }
        sub = json.getSubConfig("downloads.server");
        if (sub != null) {
            ver.serverDownload = new Download(
                    sub.getLong("size", 0), sub.getString("sha1", null), sub.getString("url", null));
        }
        sub = json.getSubConfig("libraries");
        if (sub != null) {
            JsonArray a = sub.getRootElement().getAsJsonArray();
            for (JsonElement e : a) {
                Library lib = Library.getLibrary(new JsonConfig(e));
                if (lib instanceof Library.CodeLibrary) {
                    ver.libraries.add((Library.CodeLibrary) lib);
                } else if (lib instanceof Library.NativeLibrary) {
                    ver.natives.add((Library.NativeLibrary) lib);
                }
            }
        }
        String args = json.getString("minecraftArguments", null);
        if (args != null) {
            ArgumentParser argParser = ver.args = new ArgumentParser(ver.mainClass);
            for (String s : args.split(" ")) {
                argParser.addGameArg(new Argument(s, null));
            }
            List<Rule> rules = new ArrayList<>();
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("features.is_demo_user").matches("true") ? true : passing);
            argParser.addGameArg(new Argument("--demo", rules));
            rules = new ArrayList<>();
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("features.has_custom_resolution").matches("true") ? true : passing);
            argParser.addGameArg(new Argument("--width", rules));
            argParser.addGameArg(new Argument("${resolution_width}", rules));
            argParser.addGameArg(new Argument("--height", rules));
            argParser.addGameArg(new Argument("${resolution_height}", rules));
            rules = new ArrayList<>();
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("os.name").matches("osx") ? true : passing);
            argParser.addJvmArg(new Argument("-XstartOnFirstThread", rules));
            rules = new ArrayList<>();
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("os.name").matches("windows") ? true : passing);
            argParser.addJvmArg(new Argument("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump", rules));
            rules = new ArrayList<>();
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("os.name").matches("windows") ? true : passing);
            rules.add((Rule) (Map<String, String> props, boolean passing) -> props.get("os.version").matches("^10\\.") ? true : passing);
            argParser.addJvmArg(new Argument("-Dos.name=Windows 10", rules));
            argParser.addJvmArg(new Argument("-Dos.version=10.0", rules));
            argParser.addJvmArg(new Argument("-Djava.library.path=${natives_directory}", null));
            argParser.addJvmArg(new Argument("-Dminecraft.launcher.brand=${launcher_name}", null));
            argParser.addJvmArg(new Argument("-Dminecraft.launcher.version=${launcher_version}", null));
            argParser.addJvmArg(new Argument("-cp", null));
            argParser.addJvmArg(new Argument("${classpath}", null));
        } else {
            ArgumentParser argParser = ver.args = new ArgumentParser(ver.mainClass);
            JsonArray a = json.getSubConfig("arguments.game").getRootElement().getAsJsonArray();
            for (JsonElement arg : a) {
                if (arg.isJsonPrimitive()) {
                    argParser.addGameArg(new Argument(arg.getAsString(), null));
                    continue;
                }
                sub = new JsonConfig(arg);
                JsonElement value = sub.get("value");
                if (value != null) {
                    List<Rule> rules = null;
                    JsonConfig jsonRules = sub.getSubConfig("rules");
                    if (jsonRules != null) {
                        rules = new ArrayList<>();
                        JsonArray rulesArray = jsonRules.getRootElement().getAsJsonArray();
                        for (JsonElement eRule : rulesArray) {
                            JsonConfig jsonRule = new JsonConfig(eRule);
                            final boolean allow = jsonRule.getString("action", "allow").equals("allow");
                            for (String ruleMain : jsonRule.getKeys("")) {
                                if (!ruleMain.equals("action")) {
                                    JsonConfig jsonSubRule = jsonRule.getSubConfig("[\"" + ruleMain + "\"]");
                                    for (String ruleSub : jsonSubRule.getKeys("")) {
                                        rules.add((Rule) (Map<String, String> props, boolean passing)
                                                -> props.get(ruleMain + '.' + ruleSub)
                                                        .matches(jsonSubRule.get("[\"" + ruleSub + "\"]").getAsString()) ? allow : passing);
                                    }
                                }
                            }
                        }
                    }
                    if (value.isJsonPrimitive()) {
                        argParser.addGameArg(new Argument(value.getAsString(), rules));
                    } else {
                        for (JsonElement val : value.getAsJsonArray()) {
                            argParser.addGameArg(new Argument(val.getAsString(), rules));
                        }
                    }
                } else {
                    List<Rule> rules = null;
                    JsonConfig jsonRules = sub.getSubConfig("compatibilityRules");
                    if (jsonRules != null) {
                        rules = new ArrayList<>();
                        JsonArray rulesArray = jsonRules.getRootElement().getAsJsonArray();
                        for (JsonElement eRule : rulesArray) {
                            JsonConfig jsonRule = new JsonConfig(eRule);
                            final boolean allow = jsonRule.getString("action", "allow").equals("allow");
                            for (String ruleMain : jsonRule.getKeys("")) {
                                if (!ruleMain.equals("action")) {
                                    JsonConfig jsonSubRule = jsonRule.getSubConfig("[\"" + ruleMain + "\"]");
                                    for (String ruleSub : jsonSubRule.getKeys("")) {
                                        rules.add((Rule) (Map<String, String> props, boolean passing)
                                                -> props.get(ruleMain + '.' + ruleSub)
                                                        .matches(jsonSubRule.get("[\"" + ruleSub + "\"]").getAsString()) ? allow : passing);
                                    }
                                }
                            }
                        }
                    }
                    value = sub.get("values");
                    for (JsonElement val : value.getAsJsonArray()) {
                        argParser.addGameArg(new Argument(val.getAsString(), rules));
                    }
                }
            }
            a = json.getSubConfig("arguments.jvm").getRootElement().getAsJsonArray();
            for (JsonElement arg : a) {
                if (arg.isJsonPrimitive()) {
                    argParser.addJvmArg(new Argument(arg.getAsString(), null));
                    continue;
                }
                sub = new JsonConfig(arg);
                JsonElement value = sub.get("value");
                if (value != null) {
                    List<Rule> rules = null;
                    JsonConfig jsonRules = sub.getSubConfig("rules");
                    if (jsonRules != null) {
                        rules = new ArrayList<>();
                        JsonArray rulesArray = jsonRules.getRootElement().getAsJsonArray();
                        for (JsonElement eRule : rulesArray) {
                            JsonConfig jsonRule = new JsonConfig(eRule);
                            final boolean allow = jsonRule.getString("action", "allow").equals("allow");
                            for (String ruleMain : jsonRule.getKeys("")) {
                                if (!ruleMain.equals("action")) {
                                    JsonConfig jsonSubRule = jsonRule.getSubConfig("[\"" + ruleMain + "\"]");
                                    for (String ruleSub : jsonSubRule.getKeys("")) {
                                        rules.add((Rule) (Map<String, String> props, boolean passing)
                                                -> props.get(ruleMain + '.' + ruleSub)
                                                        .matches(jsonSubRule.get("[\"" + ruleSub + "\"]").getAsString()) ? allow : passing);
                                    }
                                }
                            }
                        }
                    }
                    if (value.isJsonPrimitive()) {
                        argParser.addJvmArg(new Argument(value.getAsString(), rules));
                    } else {
                        for (JsonElement val : value.getAsJsonArray()) {
                            argParser.addJvmArg(new Argument(val.getAsString(), rules));
                        }
                    }
                } else {
                    List<Rule> rules = null;
                    JsonConfig jsonRules = sub.getSubConfig("compatibilityRules");
                    if (jsonRules != null) {
                        rules = new ArrayList<>();
                        JsonArray rulesArray = jsonRules.getRootElement().getAsJsonArray();
                        for (JsonElement eRule : rulesArray) {
                            JsonConfig jsonRule = new JsonConfig(eRule);
                            final boolean allow = jsonRule.getString("action", "allow").equals("allow");
                            for (String ruleMain : jsonRule.getKeys("")) {
                                if (!ruleMain.equals("action")) {
                                    JsonConfig jsonSubRule = jsonRule.getSubConfig("[\"" + ruleMain + "\"]");
                                    for (String ruleSub : jsonSubRule.getKeys("")) {
                                        rules.add((Rule) (Map<String, String> props, boolean passing)
                                                -> props.get(ruleMain + '.' + ruleSub)
                                                        .matches(jsonSubRule.get("[\"" + ruleSub + "\"]").getAsString()) ? allow : passing);
                                    }
                                }
                            }
                        }
                    }
                    value = sub.get("values");
                    for (JsonElement val : value.getAsJsonArray()) {
                        argParser.addJvmArg(new Argument(val.getAsString(), rules));
                    }
                }
            }
        }
        return ver;
    }
    
    
}
