import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PublicKey;

public class KeyFileGenerator {

    public static void generateKeyFile(String publicKeyName) {
        try {
            // Generar el par de claves
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();
            
            // Obtener la clave pública
            PublicKey publicKey = pair.getPublic();
            
            // Guardar la clave pública en un archivo
            try (ObjectOutputStream publicKeyOS = new ObjectOutputStream(new FileOutputStream(publicKeyName))) {
                publicKeyOS.writeObject(publicKey);
            }

            System.out.println("Clave pública almacenada en: " + publicKeyName);
        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Error al generar o guardar la clave pública: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Cambia "publicKey.ser" por la ruta y nombre del archivo donde deseas guardar la clave pública.
        generateKeyFile("publicKey.ser");
    }
}
