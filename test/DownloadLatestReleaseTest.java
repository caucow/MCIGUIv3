
import com.caucraft.mciguiv3.gamefiles.assets.Asset;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndexInfo;
import com.caucraft.mciguiv3.gamefiles.versions.GameVersion;
import com.caucraft.mciguiv3.gamefiles.util.Library;
import com.caucraft.mciguiv3.gamefiles.assets.AssetIndex;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.ManifestGameVersion;
import com.caucraft.mciguiv3.gamefiles.versions.manifest.VersionManifest;
import com.caucraft.mciguiv3.gamefiles.util.Download;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.OS;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.TaskManager;
import com.caucraft.mciguiv3.util.Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

/**
 *
 * @author caucow
 */
public class DownloadLatestReleaseTest {
    
    public static void main(String[] args) throws Exception {
        TaskManager taskmgr = new TaskManager(null, false);
        taskmgr.startTaskThread();
        File mcHome = new File("D:\\Minecraft Stuff\\mcHome_test");
        if (!mcHome.exists()) {
            mcHome.mkdirs();
        }
        
        
        Map<String, String> props = new HashMap<>();
        props.put("launcher_name", "MCIGUI");
        props.put("launcher_version", "3.0");
        props.put("features.is_demo_user", "false");
        props.put("features.has_custom_resolution", "false");
        props.put("resolution_width", "1280");
        props.put("resolution_height", "720");
        props.put("os.name", Launcher.OS_NAME.osName);
        props.put("os.version", Launcher.OS_VER);
        props.put("os.arch", Launcher.OS_ARCH);
        
        props.put("auth_player_name", "ponycau");
        props.put("auth_uuid", "00000000-0000-0000-0000-000000000000");
        props.put("auth_access_token", "null");
        props.put("user_type", "mojang");
        
        
        System.out.println("Loading version manifest.");
        VersionManifest man = VersionManifest.getVersionManifest();
//        String release = man.getLatestRelease();
        String release = man.getLatestSnapshot();
        ManifestGameVersion info = man.getVersion(release);
        System.out.println("Downloading latest release version.json.");
        info.installVersionJson(mcHome);
        System.out.println("Loading game versions.");
        List<GameVersion> inheritanceList = new ArrayList<>();
        GameVersion ver;
        inheritanceList.add(info.getGameVersion(mcHome));
        String inheritsFrom;
        List<Library.CodeLibrary> libs = new ArrayList<>();
        List<Library.NativeLibrary> nats = new ArrayList<>();
        while ((inheritsFrom = (ver = inheritanceList.get(inheritanceList.size() - 1)).getInheritsFrom()) != null) {
            ManifestGameVersion parentInfo = man.getVersion(inheritsFrom);
            parentInfo.installVersionJson(mcHome);
            inheritanceList.add(parentInfo.getGameVersion(mcHome));
        }
        for (GameVersion gv : inheritanceList) {
            for (Library.CodeLibrary lib : ver.getLibraries()) {
                if (lib.passRules(props)) {
                    libs.add(lib);
                }
            }
            for (Library.NativeLibrary lib : ver.getNatives()) {
                if (lib.passRules(props)) {
                    nats.add(lib);
                }
            } // /summon phantom ~ ~ ~ {Size:5,Passengers:[{id:"pig",Saddle:1}]}
        }
        ver = inheritanceList.get(0);
        File gameJar = ver.getVersionJarFile(mcHome);
        Download clientDownload = ver.getClientDownload();
        if (clientDownload != null && (!gameJar.exists() || clientDownload.getSize() != gameJar.length())) {
            System.out.println("Downloading client jar");
            Task t = Util.getFileDownloadTask("Download client (" + ver.getId() + ")", gameJar, clientDownload.getUrl(), clientDownload.getSize());
            taskmgr.addTask(t);
            taskmgr.waitOnTasks();
        }
        System.out.println("Checking asset index.");
        AssetIndexInfo assets = ver.getAssets();
        File assetsFile = assets.getAssetIndexFile(mcHome);
        if (!assetsFile.exists() || assets.getSize() != assetsFile.length()) {
            System.out.println("Downloading asset index.");
            assets.installAssetIndex(mcHome);
        }
        System.out.println("Loading asset index.");
        AssetIndex loadedAssets = assets.loadAssetIndex(mcHome);


        System.out.println("Verifying assets.");
        List<Asset> missingAssets = new ArrayList<>();
        loadedAssets.getAssets().forEach((asset) -> {
            File assetFile = asset.getAssetFile();
            if (!assetFile.exists() || asset.getSize() != assetFile.length()) {
                missingAssets.add(asset);
            }
        });
        System.out.println("Downloading missing assets: " + missingAssets.size() + " (" + loadedAssets.getAssets().size() + " total)");
        TaskList multidownloader = new TaskList("Downloading assets");
        for (Asset a : missingAssets) {
            multidownloader.addTask(Util.getFileDownloadTask("Downloading asset " + a.getId(), a.getAssetFile(), a.getUrl(), a.getSize()));
        }
        taskmgr.addTask(multidownloader);
        taskmgr.waitOnTasks();


        System.out.println("Verifying libraries.");
        List<Library> missingLibs = new ArrayList<>();
        libs.forEach((l) -> {
            File libFile = l.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH);
            if (!libFile.exists() || l.getDownload(Launcher.OS_NAME.osName).getSize() != libFile.length()) {
                missingLibs.add(l);
            }
        });
        nats.forEach((l) -> {
            File libFile = l.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH);
            if (!libFile.exists() || l.getDownload(Launcher.OS_NAME.osName).getSize() != libFile.length()) {
                missingLibs.add(l);
            }
        });
        System.out.println("Downloading missing libraries: " + missingLibs.size() + " (" + (libs.size() + nats.size()) + " total)");
        multidownloader = new TaskList("Downloading libraries");
        for (Library l : missingLibs) {
            multidownloader.addTask(Util.getFileDownloadTask("Downloading library " + l.getName(), l.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH), l.getDownload(Launcher.OS_NAME.osName).getUrl(), l.getDownload(Launcher.OS_NAME.osName).getSize()));
        }
        taskmgr.addTask(multidownloader);
        taskmgr.waitOnTasks();
        System.out.println("Files should all be downloaded.");
        
        System.out.println("Extracting natives.");
        multidownloader = new TaskList("Extracting natives");
        File nativeDir = new File(mcHome, "natives");
        nativeDir.mkdirs();
        
        for (Library.NativeLibrary nativeLib : nats) {
            JarFile jar = new JarFile(nativeLib.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH));
            multidownloader.addTask(Util.getJarExtractTask("Extracting " + nativeLib.getName(), nativeDir, jar, nativeLib.getExcluded()));
        }
        taskmgr.addTask(multidownloader);
        taskmgr.waitOnTasks();
        System.out.println("Natives should have been extracted.");
        
        StringBuilder classPath = new StringBuilder();
        for (Library l : libs) {
            classPath.append(l.getLibraryFile(mcHome, Launcher.OS_NAME.osName, Launcher.OS_ARCH));
            classPath.append(File.pathSeparatorChar);
        }
        classPath.append(gameJar.getPath());
        
        
        props.put("version_name", ver.getId());
        props.put("version_type", ver.getType());
        props.put("game_directory", mcHome.getPath());
        props.put("assets_root", new File(mcHome, "assets").getPath());
        props.put("assets_index_name", assets.getId());
        props.put("natives_directory", nativeDir.getPath());
        props.put("classpath", classPath.toString());
        
        
        
        List<String> gameArgs = ver.getArgumentParser().compile(getDefaultJavaExecutable(), null, props, null);
        System.out.println();
        System.out.println();
        System.out.println("Game arguments:");
        for (String s : gameArgs) {
            System.out.println(s);
        }
        
        
        Process p = new ProcessBuilder(gameArgs).directory(mcHome).redirectErrorStream(true).start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
    
    private static String getDefaultJavaExecutable() {
        return System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java" + (Launcher.OS_NAME == OS.WINDOWS ? ".exe" : "");
    }
}
