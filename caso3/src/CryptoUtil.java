import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

public class CryptoUtil {
    private static final String RSA = "RSA";
    private static final String AES = "AES";
    private static final String HMAC_SHA384 = "HmacSHA384";

    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
        keyGenerator.init(256); // Usar 256 bits para AES
        return keyGenerator.generateKey();
    }

    public static byte[] encryptRSA(PublicKey publicKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    public static byte[] decryptRSA(PrivateKey privateKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    public static byte[] encryptAES(SecretKey secretKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(new byte[16])); // IV de ejemplo
        return cipher.doFinal(data);
    }

    public static byte[] decryptAES(SecretKey secretKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(new byte[16])); // IV de ejemplo
        return cipher.doFinal(data);
    }

    public static byte[] hmacSHA384(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA384);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, HMAC_SHA384);
        mac.init(secretKeySpec);
        return mac.doFinal(data);
    }

    public static PublicKey loadPublicKey(String fileName) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            byte[] keyBytes = (byte[]) ois.readObject();
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            return keyFactory.generatePublic(spec);
        }
    }

    public static PrivateKey loadPrivateKey(String fileName) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (PrivateKey) ois.readObject();
        }
    }

    public static void savePublicKey(PublicKey publicKey, String fileName) throws Exception {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(publicKey.getEncoded());
        }
    }
}
