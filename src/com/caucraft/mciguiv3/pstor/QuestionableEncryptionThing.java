package com.caucraft.mciguiv3.pstor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.PKCS12ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 *
 * @author caucow
 */
public class QuestionableEncryptionThing {
    
    private CipherParameters cPars;
    
    public QuestionableEncryptionThing(char[] password, byte[] salt, int iterations) {
        PKCS12ParametersGenerator pGen = new PKCS12ParametersGenerator(new SHA256Digest());
        final byte[] pkcs12PasswordBytes = PBEParametersGenerator.PKCS12PasswordToBytes(password);
        pGen.init(pkcs12PasswordBytes, salt == null ? CONST_SALT : salt, iterations);
        cPars = (ParametersWithIV) pGen.generateDerivedParameters(256, 128);
    }
    
    private PaddedBufferedBlockCipher getCipher(boolean encryptMode) {
        CBCBlockCipher aesCBC = new CBCBlockCipher(new AESEngine());
        aesCBC.init(true, cPars);
        PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(aesCBC, new PKCS7Padding());
        cipher.init(encryptMode, cPars);
        return cipher;
    }
    
    public byte[] encrypt(byte[] plain) throws DataLengthException, InvalidCipherTextException {
        PaddedBufferedBlockCipher cipher = getCipher(true);
        byte[] output = new byte[cipher.getOutputSize(plain.length)];
        int bytesWrittenOut = cipher.processBytes(
            plain, 0, plain.length, output, 0);
        cipher.doFinal(output, bytesWrittenOut);
        return output;
    }
    
    public byte[] decrypt(byte[] cipher) throws DataLengthException, InvalidCipherTextException {
        PaddedBufferedBlockCipher aesCipher = getCipher(false);
        byte[] plainTemp = new byte[aesCipher.getOutputSize(cipher.length)];
        int offset = aesCipher.processBytes(cipher, 0, cipher.length, plainTemp, 0);
        int last = aesCipher.doFinal(plainTemp, offset);
        final byte[] plain = new byte[offset + last];
        System.arraycopy(plainTemp, 0, plain, 0, plain.length);
        return plain;
    }
    
    public static final byte[] CONST_SALT = {
        (byte) 0x93, (byte) 0x7b, (byte) 0xbe, (byte) 0x6c,
        (byte) 0x61, (byte) 0xe8, (byte) 0x39, (byte) 0xaf,
        (byte) 0x4c, (byte) 0xd7, (byte) 0xb2, (byte) 0x6a,
        (byte) 0xd3, (byte) 0xbf, (byte) 0x3c, (byte) 0xd9,
        (byte) 0x13, (byte) 0x43, (byte) 0x0a, (byte) 0xda,
        (byte) 0xdb, (byte) 0xec, (byte) 0xf0, (byte) 0x45,
        (byte) 0x95, (byte) 0x2f, (byte) 0xe2, (byte) 0x3e,
        (byte) 0x39, (byte) 0x6b, (byte) 0x59, (byte) 0xa2,};
    
    public static void killString(String s) {
        try {
            Field valueField = String.class.getDeclaredField("value");
            valueField.setAccessible(true);
            Arrays.fill((char[])valueField.get(s), '\u0000');
            valueField.setAccessible(false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
        }
    }
}
