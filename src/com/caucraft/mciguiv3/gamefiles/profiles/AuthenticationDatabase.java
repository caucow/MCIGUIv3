package com.caucraft.mciguiv3.gamefiles.profiles;

import com.caucraft.util.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class AuthenticationDatabase {
    
    private LauncherProfiles profiles;
    private Map<String, AuthenticatedUser> userMap;
    private Map<String, String> nameMap;
    
    public AuthenticationDatabase() {
        this.profiles = profiles;
        userMap = new HashMap<>();
        nameMap = new TreeMap<>();
    }
    
    public AuthenticationDatabase(JsonConfig json) {
        this();
        if (json != null) {
            json.getKeys("").forEach((s) -> {
                try {
                    AuthenticatedUser user = new AuthenticatedUser(json.getSubConfig("[\"" + s + "\"]"));
                    userMap.put(s, user);
                    nameMap.put(user.getDisplayName(), s);
                } catch (Exception e) {
                    Launcher.LOGGER.log(Level.WARNING, "Could not load user profile", e);
                }
            });
        }
    }
    
    public void addUser(AuthenticatedUser user) {
        String key = user.getId().toString().replace("-", "");
        String name = user.getDisplayName();
        String existingKey = nameMap.get(name);
        if (existingKey != null && !existingKey.equals(key)) {
            userMap.remove(existingKey);
        }
        nameMap.put(name, key);
        userMap.put(key, user);
    }
    
    public AuthenticatedUser getUserByUUIDKey(String key) {
        if (key == null) {
            return null;
        }
        return userMap.get(key);
    }
    
    public AuthenticatedUser getUserByUUID(UUID id) {
        if (id == null) {
            return null;
        }
        return getUserByUUIDKey(id.toString().replace("-", ""));
    }
    
    public AuthenticatedUser getUserByName(String name) {
        if (name == null) {
            return null;
        }
        String key = nameMap.get(name);
        if (key == null) {
            return null;
        }
        return getUserByUUIDKey(key);
    }
    
    public void removeUser(AuthenticatedUser user) {
        String key = user.getId().toString().replace("-", "");
        if (key.equals(nameMap.get(user.getDisplayName()))) {
            nameMap.remove(user.getDisplayName());
        }
        userMap.remove(key);
    }
    
    public JsonConfig save() {
        JsonConfig json = new JsonConfig();
        for (Map.Entry<String, AuthenticatedUser> e : userMap.entrySet()) {
            json.set("[\"" + e.getKey() + "\"]", e.getValue().save().getRootElement());
        }
        return json;
    }
    
    public Collection<AuthenticatedUser> getUsers() {
        return userMap.values();
    }
    
    @Override
    public String toString() {
        return userMap.toString();
    }
}
