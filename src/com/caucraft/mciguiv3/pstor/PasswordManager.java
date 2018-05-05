package com.caucraft.mciguiv3.pstor;

import com.caucraft.mciguiv3.util.Function;
import com.caucraft.mciguiv3.util.WaitProgressDialog;
import com.google.gson.internal.LinkedTreeMap;
import java.awt.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Set;
import javax.swing.JOptionPane;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 *
 * @author caucow
 */
public class PasswordManager {
    
    private static final byte[] passTestPlain = "Hello World!".getBytes(StandardCharsets.UTF_8);
    private static final int ITERATIONS = 250000;
    private final JFrame parentWindow;
    private final File passFile;
    private LinkedTreeMap<String, byte[]> loginMap;
    private byte[] passSalt;
    private byte[] ePassData;
    private byte[] idSalt;
    private byte[] passTestBytes;
    private boolean requirePassword = false;
    private QuestionableEncryptionThing passThing;
    private QuestionableEncryptionThing idThing;
    
    public PasswordManager(JFrame parentWindow, File passFile) {
        this.parentWindow = parentWindow;
        this.passFile = passFile;
    }
    
    private void resetState() {
        loginMap = null;
        ePassData = null;
        passSalt = null;
        idSalt = null;
        passTestBytes = null;
        passThing = null;
        idThing = null;
    }
    
    public boolean isPasswordSet() {
        return passSalt != null && ePassData != null;
    }
    
    public boolean isDecrypted() {
        return isPasswordSet() && loginMap != null;
    }
    
    public void load() throws IOException {
        if (!passFile.exists() || passFile.length() == 0) {
            resetState();
            return;
        }
        try {
            try (RandomAccessFile passRaf = new RandomAccessFile(passFile, "r")) {
                FileChannel pfChan = passRaf.getChannel();
                ByteBuffer b = ByteBuffer.allocate((int)passFile.length());
                pfChan.position(0);
                pfChan.read(b);
                b.position(0);
                passSalt = new byte[b.get() & 255];
                b.get(passSalt);
                ePassData = new byte[(int)(b.limit() - b.position())];
                b.get(ePassData);
            }
        } catch (FileNotFoundException | BufferUnderflowException ex) {
            resetState();
            throw ex;
        }
    }
    
    public boolean decryptFile(String clientToken) {
        return decryptFile(null, clientToken);
    }
    
    /**
     * @param pass password
     * @param clientToken client token
     * @return true if the decryption succeeded.
     */
    public boolean decryptFile(char[] pass, String clientToken) {
        byte[] data;
        try {
            data = decryptWithPassword(pass, ePassData, true);
            if (pass != null) {
                Arrays.fill(pass, '\u0000');
            }
        } catch (DataLengthException | InvalidCipherTextException e) {
            JOptionPane.showMessageDialog(parentWindow, "Wrong password!");
            if (pass != null) {
                Arrays.fill(pass, '\u0000');
            }
            return false;
        }
        idSalt = new byte[data[0] & 255];
        System.arraycopy(data, 1, idSalt, 0, idSalt.length);
        data = Arrays.copyOfRange(data, 1 + idSalt.length, data.length);
        try {
            data = decryptWithCID(data, clientToken);
        } catch (DataLengthException | InvalidCipherTextException e) {
            JOptionPane.showMessageDialog(parentWindow, "Client ID changed, cannot decrypt");
            return false;
        }
        ByteBuffer b = ByteBuffer.wrap(data);
        loginMap = new LinkedTreeMap<>();
        int entries = b.getShort() & 0xFFFF;
        for (int i = 0; i < entries; i++) {
            byte[] sbytes = new byte[b.get() & 0xFF];
            b.get(sbytes);
            byte[] pbytes = new byte[b.getShort() & 0xFFFF];
            b.get(pbytes);
            loginMap.put(
                    new String(sbytes, StandardCharsets.UTF_8),
                    pbytes);
        }
        return true;
    }
    
    public boolean encryptFile() {
        int len = 2;
        byte[][] sbytes = new byte[Math.min(0xFFFF, loginMap.size())][];
        Iterator<Map.Entry<String, byte[]>> uIt = loginMap.entrySet().iterator();
        for (int i = 0; i < sbytes.length; ++i) {
            Map.Entry<String, byte[]> e = uIt.next();
            sbytes[i] = e.getKey().getBytes(StandardCharsets.UTF_8);
            len += sbytes[i].length + e.getValue().length + 3;
        }
        if (len == 2) {
            resetState();
            ePassData = new byte[0];
            return true;
        }
        ByteBuffer b = ByteBuffer.allocate(len);
        b.putShort((short)Math.min(0xFFFF, loginMap.size()));
        uIt = loginMap.entrySet().iterator();
        for (int i = 0; i < sbytes.length; ++i) {
            b.put((byte)sbytes[i].length);
            b.put(sbytes[i]);
            byte[] p = uIt.next().getValue();
            b.putShort((short)p.length);
            b.put(p);
        }
        try {
            byte[] data = b.array();
            data = idThing.encrypt(data);
            data = Arrays.concatenate(new byte[] {(byte)idSalt.length}, idSalt, data);
            ePassData = passThing.encrypt(data);
//            ePassData = Arrays.concatenate(passSalt, data);
        } catch (DataLengthException | InvalidCipherTextException e) {
            JOptionPane.showMessageDialog(parentWindow, "Unable to re-encrypt file.");
            return false;
        }
        return true;
    }
    
    public void save() throws IOException {
        if ((passSalt == null || ePassData == null) && passFile.exists()) {
            passFile.delete();
            return;
        }
        if (!passFile.exists()) {
            if (!passFile.getParentFile().exists()) {
                passFile.getParentFile().mkdirs();
            }
            passFile.createNewFile();
        }
        try (RandomAccessFile passRaf = new RandomAccessFile(passFile, "rw")) {
            ByteBuffer b = ByteBuffer.allocate(1 + passSalt.length + ePassData.length);
            b.put((byte)passSalt.length);
            b.put(passSalt);
            b.put(ePassData);
            b.position(0);
            FileChannel pfChan = passRaf.getChannel();
            pfChan.position(0);
            pfChan.write(b);
            pfChan.truncate(pfChan.position());
        }
    }
    
    private byte[] decrypt(byte[] edata, boolean rememberPass, boolean requirePassword, String clientToken) {
        try {
            edata = decryptWithPassword(edata, rememberPass);
        } catch (DataLengthException | InvalidCipherTextException e) {
            JOptionPane.showMessageDialog(parentWindow, "Wrong password!");
            return null;
        }
        if (edata == null) {
            return null;
        }
        try {
            edata = decryptWithCID(edata, clientToken);
        } catch (DataLengthException | InvalidCipherTextException e) {
            JOptionPane.showMessageDialog(parentWindow, "Client ID changed, cannot decrypt");
            return null;
        }
        return edata;
    }
    
    private byte[] decryptWithPassword(final char[] pass, final byte[] edata, final boolean rememberPass) throws DataLengthException, InvalidCipherTextException {
        try {
            return WaitProgressDialog.<byte[]>waitForAction(parentWindow, "Decrypting...", (Object... args) -> {
                QuestionableEncryptionThing thing = passThing;
                char[] master = pass;
                if (thing == null || requirePassword) {
                    if (master == null) {
                        master = PasswordDialog.getPassword(parentWindow, "Enter Master Password");
                    }
                    if (master == null) {
                        return null;
                    }
                    thing = new QuestionableEncryptionThing(master, passSalt, ITERATIONS);
                }
                byte[] pdata;
                try {
                    pdata = thing.decrypt(edata);
                } catch (DataLengthException | InvalidCipherTextException e) {
                    if (master != null) {
                        Arrays.fill(master, '\u0000');
                    }
                    throw e;
                }
                if (passTestBytes == null) {
                    setMasterPassword(master);
                }
                if (master != null) {
                    Arrays.fill(master, '\u0000');
                }
                if (rememberPass) {
                    requirePassword = false;
                    passThing = thing;
                }
                return pdata;
            });
        } catch (Exception e) {
            if (e instanceof DataLengthException) {
                throw (DataLengthException)e;
            }
            if (e instanceof InvalidCipherTextException) {
                throw (InvalidCipherTextException)e;
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentWindow, "Problem decrypting with password: " + e.getClass().getSimpleName());
            return null;
        }
    }
    
    private byte[] decryptWithPassword(final byte[] edata, final boolean rememberPass) throws DataLengthException, InvalidCipherTextException {
        return decryptWithPassword(null, edata, rememberPass);
    }
    
    private byte[] decryptWithCID(final byte[] edata, String clientToken) throws DataLengthException, InvalidCipherTextException {
        try {
            return WaitProgressDialog.<byte[]>waitForAction(parentWindow, "Decrypting...", (Object... args) -> {
                setIdThing(clientToken);
                try {
                    return idThing.decrypt(edata);
                } catch (DataLengthException | InvalidCipherTextException e) {
                    throw e;
                }
            });
        } catch (Exception e) {
            if (e instanceof DataLengthException) {
                throw (DataLengthException)e;
            }
            if (e instanceof InvalidCipherTextException) {
                throw (InvalidCipherTextException)e;
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentWindow, "Problem decrypting with client ID: " + e.getClass().getSimpleName());
            return null;
        }
    }
    
    public QuestionableEncryptionThing validateMasterPassword(final char[] password) {
        try {
            return WaitProgressDialog.<QuestionableEncryptionThing>waitForAction(parentWindow, "Validating master password...", (Object... args) -> {
                if (passTestBytes == null) {
                    throw new IllegalStateException("Master password hasn't been entered yet!");
                }
                QuestionableEncryptionThing thing = new QuestionableEncryptionThing(password, passSalt, ITERATIONS);
                try {
                    byte[] test = thing.encrypt(passTestPlain);
                    if (Arrays.areEqual(test, passTestBytes)) {
                        return thing;
                    } else {
                        return null;
                    }
                } catch (DataLengthException | InvalidCipherTextException e) {
                    return null;
                }
            });
        } catch (Exception e) {
            return null;
        }
    }
    
    private QuestionableEncryptionThing setMasterPassword(final char[] password) {
        try {
            return WaitProgressDialog.<QuestionableEncryptionThing>waitForAction(parentWindow, "Generating validator...", (Object... args) -> {
                QuestionableEncryptionThing thing = new QuestionableEncryptionThing(password, passSalt, ITERATIONS);
                try {
                    passTestBytes = thing.encrypt(passTestPlain);
                    passThing = thing;
                    return thing;
                } catch (InvalidCipherTextException | DataLengthException e) {
                    JOptionPane.showMessageDialog(parentWindow, "Error setting master password, launcher may need to be restarted.");
                }
                return null;
            });
        } catch (Exception e) {}
        return null;
    }
    
    public boolean hasPassword(String uname) {
        return isDecrypted() && loginMap.containsKey(uname);
    }
    
    public String getPassword(String uname, boolean rememberMaster, boolean requireMaster, String clientToken) {
        if (hasPassword(uname)) {
            byte[] d = decrypt(loginMap.get(uname), rememberMaster, requireMaster, clientToken);
            return new String(d, StandardCharsets.UTF_8);
        }
        return null;
    }
    
    private static byte[] getSalt(int numBytes) {
        byte[] salt = new byte[numBytes];
        SecureRandom secRng = new SecureRandom();
        secRng.nextBytes(salt);
        return salt;
    }
    
    public void editPasswords(String clientToken) {
        boolean isPassSet = isPasswordSet();
        if (isPassSet && !isDecrypted() && !decryptFile(clientToken)) {
            JOptionPane.showMessageDialog(parentWindow, "Couldn't decrypt accounts file.");
            return;
        }
        QuestionableEncryptionThing thing;
        if (isPassSet) {
            char[] master = PasswordDialog.getPassword(parentWindow, "Enter Master Password");
            if (master == null) {
                return;
            }
            thing = validateMasterPassword(master);
            Arrays.fill(master, '\u0000');
        } else {
            char[] master = PasswordDialog.getPassword(parentWindow, "Set Master Password");
            if (master == null) {
                return;
            }
            char[] confirm = PasswordDialog.getPassword(parentWindow, "Confirm Master Password");
            if (confirm == null) {
                return;
            }
            if (!Arrays.areEqual(master, confirm)) {
                JOptionPane.showMessageDialog(parentWindow, "Passwords do not match!");
                Arrays.fill(master, '\u0000');
                Arrays.fill(confirm, '\u0000');
                return;
            }
            Arrays.fill(confirm, '\u0000');
            passSalt = getSalt(32);
            idSalt = getSalt(32);
            thing = setMasterPassword(master);
            Arrays.fill(master, '\u0000');
            loginMap = new LinkedTreeMap<>();
        }
        if (thing == null) {
            JOptionPane.showMessageDialog(parentWindow, "Wrong password.");
            return;
        }
        setIdThing(clientToken);
        String[] unames = new String[loginMap.size()];
        byte[][] passes = new byte[loginMap.size()][];
        int x = 0;
        for (Map.Entry<String, byte[]> e : loginMap.entrySet()) {
            unames[x] = e.getKey();
            passes[x] = e.getValue();
            ++x;
        }
        JDialog editDialog = new JDialog(parentWindow, Dialog.ModalityType.DOCUMENT_MODAL);
        editDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        EditPasswordsDialogPanel editPanel = new EditPasswordsDialogPanel(editDialog, unames, passes, idThing, thing);
        editDialog.setLocationRelativeTo(parentWindow);
        editDialog.setVisible(true);
        if (!editPanel.getSuccess()) {
            return;
        }
        loginMap.clear();
        unames = editPanel.getUsernames();
        passes = editPanel.getPasswords();
        for (int i = 0; i < unames.length; ++i) {
            loginMap.put(unames[i], passes[i]);
        }
        try {
            if (encryptFile()) {
                save();
            } else {
                JOptionPane.showMessageDialog(parentWindow, "Could not re-encrypt file.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentWindow, "Could not save re-encrypted file.");
        }
    }
    
    public Set<String> getUsernames() {
        if (loginMap == null) {
            return null;
        }
        return loginMap.keySet();
    }
    
    private void setIdThing(String clientToken) {
        try {
            WaitProgressDialog.<Void>waitForAction(parentWindow, "Creating secondary cipher...", (Object... args) -> {
                if (idThing == null) {
                    char[] idChars = clientToken.toCharArray();
                    idThing = new QuestionableEncryptionThing(idChars, idSalt, ITERATIONS);
                }
                return null;
            });
        } catch (Exception e) {}
    }
    
    public void forgetPassword() {
        requirePassword = true;
    }
    
    public void changePassword(String clientToken) {
        if (!isPasswordSet()) {
            return;
        }
        if (!isDecrypted() && !decryptFile(clientToken)) {
            JOptionPane.showMessageDialog(parentWindow, "Could not decrypt accounts file.");
            return;
        }
        char[] master = PasswordDialog.getPassword(parentWindow, "Enter Old Master Password");
        if (master == null) {
            return;
        }
        QuestionableEncryptionThing thing = validateMasterPassword(master);
        Arrays.fill(master, '\u0000');
        if (thing == null) {
            JOptionPane.showMessageDialog(parentWindow, "Wrong password!");
            return;
        }
        master = PasswordDialog.getPassword(parentWindow, "Set Master Password");
        if (master == null) {
            return;
        }
        char[] confirm = PasswordDialog.getPassword(parentWindow, "Confirm Master Password");
        if (confirm == null) {
            return;
        }
        if (!Arrays.areEqual(master, confirm)) {
            JOptionPane.showMessageDialog(parentWindow, "Passwords do not match!");
            Arrays.fill(master, '\u0000');
            Arrays.fill(confirm, '\u0000');
            return;
        }
        Arrays.fill(confirm, '\u0000');
        byte[] newSalt = getSalt(32);
        QuestionableEncryptionThing newThing;
        try {
            newThing = WaitProgressDialog.<QuestionableEncryptionThing>waitForAction(parentWindow, "Generating new cipher...", (Object... args) -> new QuestionableEncryptionThing((char[]) args[0], (byte[]) args[1], ITERATIONS), master, newSalt);
        } catch (Exception e) {
            return;
        }
        setIdThing(clientToken);
        for (String s : new LinkedList<>(loginMap.keySet())) {
            byte[] data = loginMap.get(s);
            try {
                data = thing.decrypt(data);
            } catch (DataLengthException | InvalidCipherTextException e) {
                JOptionPane.showMessageDialog(parentWindow, "Could not decrypt password for " + s + ". Removing it from the list.");
                loginMap.remove(s);
                continue;
            }
            try {
                data = newThing.encrypt(data);
            } catch (DataLengthException | InvalidCipherTextException e) {
                JOptionPane.showMessageDialog(parentWindow, "Could not re-encrypt password for " + s + ". Removing it from the list.");
                loginMap.remove(s);
                continue;
            }
            loginMap.put(s, data);
        }
        passThing = newThing;
        passSalt = newSalt;
        setMasterPassword(master);
        Arrays.fill(master, '\u0000');
        try {
            if (encryptFile()) {
                save();
            } else {
                JOptionPane.showMessageDialog(parentWindow, "Could not re-encrypt file.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentWindow, "Could not save re-encrypted file.");
        }
    }
}
