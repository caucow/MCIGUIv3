package com.caucraft.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Arrays;

/**
 *
 * @author caucow
 */
public class HttpPayload {
    
    public static HttpPayload getPayload(String urlString, String protocol, String applicationType, String payload)
            throws SocketTimeoutException, IOException {
        URL url = new URL(urlString);
        
        HttpURLConnection https = (HttpURLConnection)url.openConnection();
        
        https.setConnectTimeout(10000);
        https.setReadTimeout(10000);
        https.setRequestMethod(protocol);
        https.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (applicationType != null) {
            https.setRequestProperty("Content-Type", applicationType);
        }
        https.setDoInput(true);
        https.setDoOutput(payload != null);
        https.connect();
        
        if (payload != null) {
            try (DataOutputStream dos = new DataOutputStream(https.getOutputStream())) {
                dos.writeBytes(payload);
                dos.flush();
                dos.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        }
        
        int responseCode = -1;
        responseCode = https.getResponseCode();
        StringBuilder sb = new StringBuilder();
        
        if (responseCode >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(https.getErrorStream()))) {
                String line = null;
                while (true) {
                    try {
                        if ((line = br.readLine()) == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }

                br.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(https.getInputStream()))) {
                String line = null;
                while (true) {
                    try {
                        if ((line = br.readLine()) == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }

                br.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        }
        
        return new HttpPayload(responseCode, sb.toString());
    }
    
    public static HttpPayload getRawPayload(String urlString, String protocol, String applicationType, String payload)
            throws SocketTimeoutException, IOException {
        URL url = new URL(urlString);
        
        HttpURLConnection https = (HttpURLConnection)url.openConnection();
        
        https.setConnectTimeout(10000);
        https.setReadTimeout(10000);
        https.setRequestMethod(protocol);
        https.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (applicationType != null) {
            https.setRequestProperty("Content-Type", applicationType);
        }
        https.setDoInput(true);
        https.setDoOutput(payload != null);
        https.connect();
        
        if (payload != null) {
            try (DataOutputStream dos = new DataOutputStream(https.getOutputStream())) {
                dos.writeBytes(payload);
                dos.flush();
                dos.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        }
        
        int responseCode = -1;
        responseCode = https.getResponseCode();
        StringBuilder sb = new StringBuilder();
        
        if (responseCode >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(https.getErrorStream()))) {
                String line = null;
                while (true) {
                    try {
                        if ((line = br.readLine()) == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }

                br.close();
            } catch (IOException ioe) {
                throw ioe;
            }
            return new HttpPayload(responseCode, sb.toString());
        } else {
            int i = 0;
            int read = 0;
            byte[] bytes = new byte[33554432];
            try (InputStream input = https.getInputStream()) {
                do {
                    read = input.read(bytes, i, bytes.length - i);
                    i += Math.max(0, read);
                } while (read > 0);
                bytes = Arrays.copyOf(bytes, i);
                input.close();
            } catch (IOException ioe) {
                throw ioe;
            }
            return new HttpPayload(responseCode, bytes);
        }
    }
    
    public static InputStream getInputStream(String urlString, String protocol, String applicationType, String payload)
            throws SocketTimeoutException, IOException {
        URL url = new URL(urlString);
        
        HttpURLConnection https = (HttpURLConnection)url.openConnection();
        
        https.setConnectTimeout(10000);
        https.setReadTimeout(10000);
        https.setRequestMethod(protocol);
        https.setRequestProperty("User-Agent", "Mozilla/5.0");
        if (applicationType != null) {
            https.setRequestProperty("Content-Type", applicationType);
        }
        https.setDoInput(true);
        https.setDoOutput(payload != null);
        https.connect();
        
        if (payload != null) {
            try (DataOutputStream dos = new DataOutputStream(https.getOutputStream())) {
                dos.writeBytes(payload);
                dos.flush();
                dos.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        }
        
        int responseCode = -1;
        responseCode = https.getResponseCode();
        StringBuilder sb = new StringBuilder();
        
        if (responseCode >= 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(https.getErrorStream()))) {
                String line = null;
                while (true) {
                    try {
                        if ((line = br.readLine()) == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (IOException ioe) {
                        throw ioe;
                    }
                }

                br.close();
            } catch (IOException ioe) {
                throw ioe;
            }
            throw new IOException(String.format("Server returned error when reading from %s: %d %s", urlString, responseCode, sb.toString()));
        } else {
            int i = 0;
            int read = 0;
            byte[] bytes = new byte[33554432];
            return https.getInputStream();
        }
    }
    
    private final int responseCode;
    private final Object payload;
    
    private HttpPayload(int responseCode, String payload) {
        this.responseCode = responseCode;
        this.payload = payload;
    }
    
    private HttpPayload(int responseCode, byte[] payload) {
        this.responseCode = responseCode;
        this.payload = payload;
    }
    
    public int getResponseCode() {
        return responseCode;
    }
    
    public String getPayload() {
        return (String)payload;
    }
    
    public byte[] getRawPayload() {
        return (byte[])payload;
    }
    
    public boolean isRaw() {
        return payload instanceof byte[];
    }
}
