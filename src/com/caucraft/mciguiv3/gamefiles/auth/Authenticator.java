package com.caucraft.mciguiv3.gamefiles.auth;

import com.caucraft.mciguiv3.gamefiles.profiles.AuthenticatedUser;
import com.caucraft.util.HttpPayload;
import com.caucraft.util.JsonConfig;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

/**
 *
 * @author caucow
 */
public class Authenticator {
    
    private static String expandUUID(String noHyphens) {
        if (!noHyphens.matches("[\\da-fA-F]{32}")) {
            throw new IllegalArgumentException("Argument be a valid UUID without hyphens.");
        }
        return String.format(
                "%s-%s-%s-%s-%s",
                noHyphens.substring(0, 8),
                noHyphens.substring(8, 12),
                noHyphens.substring(12, 16),
                noHyphens.substring(16, 20),
                noHyphens.substring(20, 32));
    }
    
    public static AuthenticatedUser loginWithPassword(String clientToken, String username, String password) throws SocketTimeoutException, IOException, ForbiddenOperationException {
        JsonConfig json = new JsonConfig();
        json.set("agent.name", "Minecraft");
        json.set("agent.version", 1);
        json.set("clientToken", JsonConfig.escape(clientToken));
        json.set("username", JsonConfig.escape(username));
        json.set("password", JsonConfig.escape(password));
        json.set("requestUser", true);
        
        HttpPayload response = HttpPayload.getPayload("https://authserver.mojang.com/authenticate", "POST", "application/json", json.toString());
        
        if (response.getResponseCode() >= 400) {
            getError(response);
            return null;
        } else {
            JsonConfig responseJson = new JsonConfig(new JsonParser().parse(response.getPayload()));
            JsonConfig profileJson = new JsonConfig();
            profileJson.set("uuid", expandUUID(Objects.requireNonNull(responseJson.getString("selectedProfile.id", null))));
            profileJson.set("displayName", Objects.requireNonNull(responseJson.getString("selectedProfile.name", null)));
            profileJson.set("accessToken", Objects.requireNonNull(responseJson.getString("accessToken", null)));
            profileJson.set("userid", Objects.requireNonNull(responseJson.getString("user.id", null)));
            profileJson.set("username", Objects.requireNonNull(username));
            JsonConfig properties = responseJson.getSubConfig("user.properties");
            if (properties != null) {
                profileJson.set("userProfiles", properties.getRootElement().getAsJsonArray());
            }
            return new AuthenticatedUser(profileJson);
        }
    }
    
    public static AuthenticatedUser refreshAccessToken(String clientToken, AuthenticatedUser user) throws SocketTimeoutException, IOException, ForbiddenOperationException {
        JsonConfig json = new JsonConfig();
        json.set("clientToken", JsonConfig.escape(clientToken));
        json.set("accessToken", JsonConfig.escape(user.getAccessToken()));
        json.set("requestUser", true);
        
        HttpPayload response = HttpPayload.getPayload("https://authserver.mojang.com/refresh", "POST", "application/json", json.toString());
        
        if (response.getResponseCode() >= 400) {
            getError(response);
            return null;
        } else {
            JsonConfig responseJson = new JsonConfig(new JsonParser().parse(response.getPayload()));
            JsonConfig profileJson = new JsonConfig();
            profileJson.set("uuid", expandUUID(Objects.requireNonNull(responseJson.getString("selectedProfile.id", null))));
            profileJson.set("displayName", Objects.requireNonNull(responseJson.getString("selectedProfile.name", null)));
            profileJson.set("accessToken", Objects.requireNonNull(responseJson.getString("accessToken", null)));
            profileJson.set("userid", Objects.requireNonNull(responseJson.getString("user.id", null)));
            profileJson.set("username", Objects.requireNonNull(user.getUsername()));
            JsonConfig properties = responseJson.getSubConfig("user.properties");
            if (properties != null) {
                profileJson.set("userProfiles", properties.getRootElement().getAsJsonArray());
            }
            return new AuthenticatedUser(profileJson);
        }
    }
    
    public static boolean validateAccessToken(String clientToken, AuthenticatedUser user) throws SocketTimeoutException, IOException {
        JsonConfig json = new JsonConfig();
        json.set("clientToken", JsonConfig.escape(clientToken));
        json.set("accessToken", JsonConfig.escape(user.getAccessToken()));
        
        HttpPayload response = HttpPayload.getPayload("https://authserver.mojang.com/validate", "POST", "application/json", json.toString());
        
        if (response.getResponseCode() == 204) {
            return true;
        } else if (response.getResponseCode() == 403) {
            return false;
        } else {
            getError(response);
            return false;
        }
    }
    
    public static boolean invalidateToken(String clientToken, String authToken) throws SocketTimeoutException, IOException {
        JsonConfig json = new JsonConfig();
        json.set("accessToken", JsonConfig.escape(authToken));
        json.set("clientToken", JsonConfig.escape(clientToken));
        
        HttpPayload response = HttpPayload.getPayload("https://authserver.mojang.com/invalidate", "POST", "application/json", json.toString());
        
        if (response.getResponseCode() == 204) {
            return true;
        } else if (response.getResponseCode() == 403) {
            return false;
        } else {
            getError(response);
            return false;
        }
    }
    
    private static void getError(HttpPayload response) throws IOException {
        try {
            JsonConfig error = new JsonConfig(new JsonParser().parse(response.getPayload()));
            if ("ForbiddenOperationException".equals(error.getString("error", null))) {
                throw new ForbiddenOperationException(error.getString("errorMessage", null));
            } else if ("IllegalArgumentException".equals(error.getString("error", null))) {
                throw new IllegalArgumentException(error.getString("errorMessage", null));
            } else {
                throw new IOException(response.getResponseCode() + " " + response.getPayload());
            }
        } catch (Exception e) {
            if (e instanceof ForbiddenOperationException
                    || e instanceof IllegalArgumentException
                    || e instanceof IOException) {
                throw e;
            }
            throw new IOException(response.getPayload(), e);
        }
    }
}
