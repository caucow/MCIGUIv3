package com.caucraft.mciguiv3.gamefiles.profiles;

import com.caucraft.util.JsonConfig;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author caucow
 */
public class AuthenticatedUser {
    
    private final JsonConfig json;
    private final UUID uuid;
    private final String displayName;
    private final String accessToken;
    private final String userId;
    private final String username;
    private final String type;
    
    public AuthenticatedUser(String offlineName) {
        this.json = new JsonConfig();
        this.uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        this.displayName = offlineName;
        this.accessToken = "00000000-0000-0000-0000-000000000000";
        this.userId = offlineName;
        this.username = offlineName;
        this.type = "mojang";
    }
    
    public AuthenticatedUser(JsonConfig json) {
        this.json = json;
        String uuidStr = Objects.requireNonNull(json.getString("uuid", null));
        this.uuid = uuidStr.length() == 32 ?
                UUID.fromString(
                        uuidStr.substring(0, 8)
                                + '-' + uuidStr.substring(8, 12)
                                + '-' + uuidStr.substring(12, 16)
                                + '-' + uuidStr.substring(16, 20)
                                + '-' + uuidStr.substring(20, 32))
                : UUID.fromString(uuidStr);
        this.displayName = Objects.requireNonNull(json.getString("displayName", null));
        this.accessToken = Objects.requireNonNull(json.getString("accessToken", null));
        this.userId = Objects.requireNonNull(json.getString("userid", null));
        this.username = Objects.requireNonNull(json.getString("username", null));
        this.type = this.userId.matches("\\d{1,31}") ? "legacy" : "mojang";
    }
    
    public UUID getId() {
        return uuid;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getType() {
        return type;
    }
    
    public JsonConfig save() {
        json.set("displayName", displayName);
        json.set("accessToken", accessToken);
        json.set("userid", userId);
        json.set("uuid", uuid.toString());
        json.set("username", username);
        return json;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AuthenticatedUser)) {
            return false;
        }
        AuthenticatedUser ou = (AuthenticatedUser)o;
        return uuid.equals(ou.uuid)
                && (displayName == null && ou.displayName == null || displayName != null && displayName.equals(ou.displayName))
                && (accessToken == null && ou.accessToken == null || accessToken != null && accessToken.equals(ou.accessToken))
                && (userId == null && ou.userId == null || userId != null && userId.equals(ou.userId))
                && (username == null && ou.username == null || username != null && username.equals(ou.username))
                && (type == null && ou.type == null || type != null && type.equals(ou.type));
    }
    
    @Override
    public String toString() {
        return String.format("AuthenticatedUser{uuid=%s, name=%s, token=%s, id=%s, user=%s}", uuid, displayName, accessToken, userId, username);
    }
    
}
