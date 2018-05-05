package com.caucraft.mciguiv3.gamefiles.assets;

import com.caucraft.mciguiv3.json.JsonConfig;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author caucow
 */
public class AssetIndex {
    
    private AssetIndexInfo assetIndex;
    List<Asset> assets;
    
    private AssetIndex() {
        assets = new ArrayList<>();
    }
    
    public List<Asset> getAssets() {
        return assets;
    }
    
    public static AssetIndex loadAssetIndex(File mcHome, AssetIndexInfo assetIndex) throws FileNotFoundException, IOException, JsonParseException {
        File indexFile = assetIndex.getAssetIndexFile(mcHome);
        if (!indexFile.exists()) {
            throw new FileNotFoundException("Asset index file does not exist: " + indexFile);
        }
        JsonConfig json = new JsonConfig();
        json.load(indexFile);
        
        AssetIndex assets = new AssetIndex();
        json = json.getSubConfig("objects");
        for (String s : json.getKeys("")) {
            assets.assets.add(new Asset(s, json.getString("[\"" + s + "\"].hash", null), json.getLong("[\"" + s + "\"].size", 0), mcHome));
        }
        return assets;
    }
}
