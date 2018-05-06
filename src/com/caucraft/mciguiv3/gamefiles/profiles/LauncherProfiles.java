package com.caucraft.mciguiv3.gamefiles.profiles;

import com.caucraft.mciguiv3.json.JsonConfig;
import com.caucraft.mciguiv3.launch.Launcher;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 * @author caucow
 */
public class LauncherProfiles {
    
    public static final String LAUNCHER_NAME = "MCIGUIv3";
    public static final int LAUNCHER_FORMAT = 21;
    public static final int PROFILES_FORMAT = 1;
    private JsonConfig json;
    private final File profilesFile;
    private String clientToken;
    private ProfileManager profiles;
    private String selectedProfile;
    private AuthenticationDatabase authdb;
    private AuthenticatedUser selectedUser;
    private boolean hasChanged;
    
    public LauncherProfiles(File profilesFile) {
        this.profilesFile = profilesFile;
        this.clientToken = UUID.randomUUID().toString();
        this.profiles = new ProfileManager();
        this.authdb = new AuthenticationDatabase();
    }
    
    @Override
    public String toString() {
        return String.format("LauncherProfiles:{%s, token=%s, profiles=%s, selected=%s, authdb=%s, user=%s}", profilesFile, clientToken, profiles, selectedProfile, authdb, selectedUser);
    }
    
    public void load() throws JsonParseException, FileNotFoundException, IOException {
        JsonConfig newJson = new JsonConfig();
        newJson.load(profilesFile);
        try {
            ProfileManager newProfiles = new ProfileManager(newJson.getSubConfig("profiles"));
            String newSelectedProfile = newJson.getString("selectedProfile", null);
            String newClientToken = Objects.requireNonNull(newJson.getString("clientToken", null));
            AuthenticationDatabase newAuthdb = new AuthenticationDatabase(newJson.getSubConfig("authenticationDatabase"));
            AuthenticatedUser newUser = newAuthdb.getUserByUUIDKey(newJson.getString("selectedUser", null));
            this.json = newJson;
            this.hasChanged = false;
            this.clientToken = newClientToken;
            this.profiles = newProfiles;
            this.selectedProfile = newSelectedProfile;
            this.authdb = newAuthdb;
            this.selectedUser = newUser;
            hasChanged = false;
        } catch (Exception e) {
            Launcher.LOGGER.log(Level.WARNING, "Unable to load launcher profiles", e);
        }
    }
    
    public void save(boolean force) {
        if (!hasChanged && !force) {
            return;
        }
        if (json == null) {
            json = new JsonConfig();
        }
        json.set("profiles", profiles.save().getRootElement());
        json.set("selectedProfile", selectedProfile);
        json.set("clientToken", clientToken);
        json.set("authenticationDatabase", authdb.save().getRootElement());
        if (selectedUser != null) {
            json.set("selectedUser", selectedUser.getId().toString().replace("-", ""));
        }
        json.set("launcherVersion.name", LAUNCHER_NAME);
        json.set("launcherVersion.format", LAUNCHER_FORMAT);
        json.set("launcherVersion.profilesFormat", PROFILES_FORMAT);
        try {
            json.save(profilesFile);
        } catch (IOException e) {
            Launcher.getLogger().log(Level.WARNING, "Unable to save launcher profiles.", e);
        }
    }
    
    public String getClientToken() {
        return clientToken;
    }
    
    public void setSelectedProfile(Profile profile) {
        if (profile == null) {
            return;
        }
        this.selectedProfile = profile.getName();
        profiles.addProfile(profile);
    }
    
    public Profile getSelectedProfile() {
        if (selectedProfile == null) {
            selectedProfile = "";
        }
        Profile p = profiles.getProfile(selectedProfile);
        if (p == null) {
            Collection<Profile> profCollection = profiles.getProfiles();
            if (profCollection.isEmpty()) {
                p = new Profile("Minecraft");
                p.getAllowedReleaseTypes().add("release");
                profiles.addProfile(p);
            } else {
                p = profCollection.iterator().next();
            }
            selectedProfile = p.getName();
        }
        return p;
    }
    
    public ProfileManager getProfileManager() {
        return profiles;
    }
    
    public AuthenticationDatabase getAuthDb() {
        return authdb;
    }
    
    public AuthenticatedUser getSelectedUser() {
        return selectedUser;
    }
    
    public void setSelectedUser(AuthenticatedUser user) {
        authdb.addUser(user);
        selectedUser = user;
    }
    
}
