package com.caucraft.mciguiv3.gamefiles.profiles;

import com.caucraft.mciguiv3.json.JsonConfig;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author caucow
 */
public class ProfileManager {
    
    private TreeMap<String, Profile> profiles;
    
    public ProfileManager() {
        profiles = new TreeMap<>();
    }
    
    public ProfileManager(JsonConfig json) {
        this();
        if (json != null) {
            json.getKeys("").forEach((s) -> {
                try {
                    Profile p = new Profile(json.getSubConfig("[\"" + s + "\"]"));
                    profiles.put(s, p);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    public Profile getProfile(String name) {
        return profiles.get(name);
    }
    
    public Collection<Profile> getProfiles() {
        return new TreeSet<>(profiles.values());
    }
    
    public void addProfile(Profile p) {
        profiles.put(p.getName(), p);
    }
    
    public void removeProfile(Profile p) {
        profiles.remove(p.getName());
    }
    
    public Profile removeProfile(String name) {
        return profiles.remove(name);
    }
    
    public JsonConfig save() {
        JsonConfig json = new JsonConfig();
        for (Map.Entry<String, Profile> e : profiles.entrySet()) {
            json.set("[\"" + e.getKey() + "\"]", e.getValue().save().getRootElement());
        }
        return json;
    }
    
    @Override
    public String toString() {
        return String.format("ProfileManager{%s}", profiles);
    }
}
