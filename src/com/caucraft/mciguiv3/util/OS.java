package com.caucraft.mciguiv3.util;

/**
 *
 * @author caucow
 */
public enum OS {
    WINDOWS("windows"),
    OSX("osx"),
    LINUX("linux");
    
    public final String osName;
    
    private OS(String osName) {
        this.osName = osName;
    }
}
