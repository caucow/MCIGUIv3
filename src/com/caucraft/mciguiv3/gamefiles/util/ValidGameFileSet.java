package com.caucraft.mciguiv3.gamefiles.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author caucow
 */
public class ValidGameFileSet {
    
    private final Map<String, Long> validFiles;
    
    public ValidGameFileSet() {
        validFiles = new HashMap<>();
    }
    
    public boolean isValid(File f) {
        Long l = validFiles.get(f.getPath());
        return (l != null && f.lastModified() == l);
    }
    
    public void setValid(File f) {
        validFiles.put(f.getPath(), f.lastModified());
    }
    
}
