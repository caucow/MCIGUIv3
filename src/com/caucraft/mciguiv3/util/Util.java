package com.caucraft.mciguiv3.util;

import com.caucraft.mciguiv3.http.HttpPayload;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author caucow
 */
public final class Util {
    private Util() {}
    
    public static final Date getStandardDate(String dateString) {
        try {
            return new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(dateString).getTime());
        } catch (Exception e) {
            try {
                return new Date(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString.replaceAll("(\\d+-\\d+-\\dT\\d+:\\d+:\\d+-\\d{2,2})(.+)", "$1:$2")).getTime());
            } catch (Exception e2) {
                return null;
            }
        }
    }
    
    public static Task getFileDownloadTask(String desc, File file, String url, long size) {
        return new Task(desc) {
            private long read = 0;
            
            @Override
            public float getProgress() {
                return (float)read / (float)size;
            }
            
            @Override
            public void run() throws Exception {
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
                try (InputStream stream = HttpPayload.getInputStream(url, "GET", null, null); FileOutputStream fileOut = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1048576];
                    int cur;
                    while ((cur = stream.read(buffer)) > 0) {
                        read += cur;
                        fileOut.write(buffer, 0, cur);
                        updateProgress();
                    }
                    stream.close();
                    fileOut.close();
                }
            }
        };
    }
    
    public static Task getJarExtractTask(String desc, File nativeDir, JarFile jar, Iterable<String> exclude) {
        return new Task(desc) {
            private int totalSize = 0;
            private int read = 0;
            
            @Override
            public float getProgress() {
                return totalSize == 0 ? 0 : (float)read / (float)totalSize;
            }
            
            @Override
            public void run() throws Exception {
                Enumeration<JarEntry> enumEntries;
                enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    long size = enumEntries.nextElement().getSize();
                    if (size != -1) {
                        totalSize += size;
                    }
                    totalSize += 1;
                }
                enumEntries = jar.entries();
                entries: while (enumEntries.hasMoreElements()) {
                    JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
                    File f = new java.io.File(nativeDir + "/" + file.getName());
                    if (exclude != null) {
                        for (String s : exclude) {
                            if (file.getName().startsWith(s)) {
                                long size = file.getSize();
                                if (size != -1) {
                                    read += size;
                                }
                                read += 1;
                                continue entries;
                            }
                        }
                    }
                    if (file.isDirectory()) { // if its a directory, create it
                        f.mkdir();
                        long size = file.getSize();
                        if (size != -1) {
                            read += size;
                        }
                        read += 1;
                        continue;
                    }
                    try (InputStream is = jar.getInputStream(file) // get the input stream
                    ; FileOutputStream fos = new FileOutputStream(f)) {
                        byte[] buffer = new byte[1048576];
                        int cur;
                        while ((cur = is.read(buffer)) > 0) {
                            read += cur;
                            fos.write(buffer, 0, cur);
                            updateProgress();
                        }
                        read += 1;
                        is.close();
                        fos.close();
                    } catch (IOException e) {
                        throw e;
                    }
                }
            }
        };
    }
}
