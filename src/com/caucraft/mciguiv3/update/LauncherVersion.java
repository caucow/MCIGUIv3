package com.caucraft.mciguiv3.update;

import com.caucraft.mciguiv3.http.HttpPayload;
import com.caucraft.mciguiv3.json.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import com.caucraft.mciguiv3.util.Task;
import com.caucraft.mciguiv3.util.TaskList;
import com.caucraft.mciguiv3.util.Util;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author caucow
 */
public class LauncherVersion implements Comparable<LauncherVersion> {
    
    private final Launcher launcher;
    private final String id;
    private final Date release;
    private List<String> added;
    private List<String> changed;
    private List<String> removed;
    private List<String> installNotes;
    private final String url;
    private final long size;
    
    public LauncherVersion(Launcher launcher, String id, Date release, String url, long size) {
        this.launcher = launcher;
        this.id = id;
        this.release = release;
        this.url = url;
        this.size = size;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Date getRelease() {
        return release;
    }
    
    public List<String> getAdded() {
        return added == null || added.isEmpty() ? null : added;
    }
    
    public List<String> getChanged() {
        return changed == null || changed.isEmpty() ? null : changed;
    }
    
    public List<String> getRemoved() {
        return removed == null || removed.isEmpty() ? null : removed;
    }
    
    public List<String> getInstallNotes() {
        if (installNotes == null || installNotes.isEmpty()) {
            return null;
        }
        return installNotes;
    }
    
    @Override
    public int compareTo(LauncherVersion o) {
        return release.compareTo(o.release);
    }
    
    public void download() {
        TaskList downloadList = new TaskList("Installing MCIGUI " + id);
        Task downloadTask = Util.getFileDownloadTask("Downloading update: " + id, Launcher.LAUNCHER_JARFILE, url, size);
        downloadList.addTask(new Task("Downloading update: " + id) {
            @Override
            public float getProgress() {
                return downloadTask.getProgress();
            }
            
            @Override
            public void run() throws Exception {
                try {
                    downloadTask.addUpdateListener(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                    downloadTask.run();
                } catch (Exception e) {
                    Launcher.LOGGER.log(Level.WARNING, "Could not download MCIGUI " + id, e);
                    JOptionPane.showMessageDialog(
                            launcher.getMainWindow(),
                            "<html>The new version could not be downloaded:<br>"
                                    + e.getClass().getSimpleName()
                                    + ":<br>"
                                    + e.getMessage()
                                    + "</html>"
                    );
                    throw e;
                }
            }
        });
        downloadList.addTask(new Task("Finished installing update.") {
            @Override
            public float getProgress() {
                return -1.0F;
            }
            
            @Override
            public void run() throws Exception {
                JOptionPane.showMessageDialog(launcher.getMainWindow(), "The new version has been installed. Restart MCIGUI.");
            }
        });
        launcher.getMainTaskMgr().addTask(downloadList);
    }
    
    public static LauncherVersion getVersion(Launcher launcher, String id, JsonConfig json) {
        LauncherVersion ver = new LauncherVersion(
                launcher,
                id,
                Util.getStandardDate(json.getString("date", null)),
                Objects.requireNonNull(json.getString("url", null)),
                json.getLong("size", 0)
        );
        List<Object> list = json.getAsList("changelog.added");
        if (list != null) {
            ver.added = new ArrayList<>();
            for (Object o : list) {
                ver.added.add(o.toString());
            }
        }
        list = json.getAsList("changelog.changed");
        if (list != null) {
            ver.changed = new ArrayList<>();
            for (Object o : list) {
                ver.changed.add(o.toString());
            }
        }
        list = json.getAsList("changelog.removed");
        if (list != null) {
            ver.removed = new ArrayList<>();
            for (Object o : list) {
                ver.removed.add(o.toString());
            }
        }
        list = json.getAsList("installNotes");
        if (list != null) {
            ver.installNotes = new ArrayList<>();
            for (Object o : list) {
                ver.installNotes.add(o.toString());
            }
        }
        return ver;
    }
}
