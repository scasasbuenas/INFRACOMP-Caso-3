import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.security.PrivateKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PackageClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey symmetricKey;

    public PackageClient(String address, int port) throws Exception {
        socket = new Socket(address, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        loadKeys();
        sendPublicKeyToServer();
        receiveAndDecryptSymmetricKey();
    }

    private void loadKeys() throws Exception {
        System.out.println("Cargando las claves pública y privada del cliente...");
        publicKey = CryptoUtil.loadPublicKey("publicKey.ser");
        privateKey = CryptoUtil.loadPrivateKey("privateKey.ser");
        System.out.println("Claves cargadas correctamente.");
    }

    private void sendPublicKeyToServer() throws Exception {
        System.out.println("Enviando clave pública al servidor...");
        out.writeObject(publicKey);
        out.flush();
        System.out.println("Clave pública enviada al servidor.");
    }

    private void receiveAndDecryptSymmetricKey() throws Exception {
        System.out.println("Esperando la clave simétrica cifrada del servidor...");
        byte[] encryptedKey = (byte[]) in.readObject();
        byte[] keyBytes = CryptoUtil.decryptRSA(privateKey, encryptedKey);
        symmetricKey = new SecretKeySpec(keyBytes, "AES");
        System.out.println("Clave simétrica de sesión establecida correctamente.");
    }

    public void sendEncryptedQuery(String packageId) throws Exception {
        System.out.println("Enviando consulta cifrada para el paquete ID: " + packageId);
        byte[] encryptedData = CryptoUtil.encryptAES(symmetricKey, packageId.getBytes());
        out.writeObject(encryptedData);
        out.flush();
        System.out.println("Consulta enviada al servidor.");
    }

    public String receiveDecryptedResponse() throws Exception {
        System.out.println("Esperando respuesta cifrada del servidor...");
        byte[] encryptedResponse = (byte[]) in.readObject();
        byte[] decryptedData = CryptoUtil.decryptAES(symmetricKey, encryptedResponse);
        String response = new String(decryptedData);
        System.out.println("Respuesta recibida y descifrada: " + response);
        return response;
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
        System.out.println("Conexión cerrada.");
    }

    public static void main(String[] args) {
        try {
            PackageClient client = new PackageClient("localhost", 1234);
            client.sendEncryptedQuery("1234");  // Envía una consulta para el paquete con ID "1234"
            System.out.println("Respuesta del servidor: " + client.receiveDecryptedResponse());
            client.close();
        } catch (Exception e) {
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        }
    }
}
