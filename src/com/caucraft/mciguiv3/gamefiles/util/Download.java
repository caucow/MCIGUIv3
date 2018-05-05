package com.caucraft.mciguiv3.gamefiles.util;

import com.caucraft.mciguiv3.http.HttpPayload;
import com.caucraft.mciguiv3.launch.Launcher;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author caucow
 */
public class Download {
    private final long size;
    private final String hash;
    private final String url;

    public Download(long size, String hash, String url) {
        this.size = size;
        this.hash = hash;
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public String getHash() {
        return hash;
    }

    public String getUrl() {
        return url;
    }
    
    public boolean validateFile(ValidGameFileSet validFiles, File file) {
        if (validFiles != null && validFiles.isValid(file)) {
            return true;
        }
        boolean valid = file.exists() && file.length() == size && hash != null && hash.equalsIgnoreCase(Launcher.getFileSha1(file));
        if (valid) {
            validFiles.setValid(file);
        }
        return valid;
    }

    public void download(File file) throws SocketTimeoutException, IOException {
        HttpPayload payload = HttpPayload.getRawPayload(url, "GET", null, null);
        if (payload.isRaw()) {
            Files.write(file.toPath(), payload.getRawPayload(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DSYNC);
        } else {
            throw new IOException(String.format("Could not download library: %s (%d %s)", url, payload.getResponseCode(), payload.getPayload()));
        }
    }

    @Override
    public String toString() {
        return String.format("Download{%s %s %d", url, hash, size);
    }
}
