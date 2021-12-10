package com.caucraft.mciguiv3.gamefiles.util;

/**
 *
 * @author caucow
 */
public class LoggingData {
    
    private String argument;
    private String fileId;
    private Download configDownload;
    private String type; // TODO what is this used for? "type": "log4j2-xml"
    
    public LoggingData(String jvmArg, String fileName, Download download, String type) {
        this.argument = jvmArg;
        this.fileId = fileName;
        this.configDownload = download;
        this.type = type;
    }
    
    public String getJvmArg() {
        return argument;
    }
    
    public String getFileName() {
        return fileId;
    }
    
    public Download getDownload() {
        return configDownload;
    }
    
    public String getType() {
        return type;
    }
    
}
