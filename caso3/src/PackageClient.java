import java.io.*;
import java.net.*;
import java.security.PublicKey;
import java.security.PrivateKey;
import java.util.Scanner;
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
        byte[] encryptedData = CryptoUtil.encryptAES(symmetricKey, packageId.getBytes());
        out.writeObject(encryptedData);
        out.flush();
    }

    public String receiveDecryptedResponse() throws Exception {
        byte[] encryptedResponse = (byte[]) in.readObject();
        byte[] decryptedData = CryptoUtil.decryptAES(symmetricKey, encryptedResponse);
        return new String(decryptedData);
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Ingrese el ID del paquete a consultar:");
        String packageId = scanner.nextLine();

        System.out.println("Seleccione el escenario de prueba:");
        System.out.println("1. Escenario Iterativo (32 consultas consecutivas)");
        System.out.println("2. Escenario Concurrente (4, 8 o 32 delegados concurrentes)");
        System.out.println("3. Una única solicitud para ver los tiempos de cifrado");
        int option = scanner.nextInt();

        if (option == 1) {
            // Escenario Iterativo
            try {
                PackageClient client = new PackageClient("localhost", 1234);
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < 32; i++) {
                    System.out.println("Enviando consulta #" + (i + 1) + " para el paquete ID: " + packageId);
                    client.sendEncryptedQuery(packageId);
                    String response = client.receiveDecryptedResponse();
                    System.out.println("Respuesta del servidor: " + response);
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Tiempo total para 32 consultas: " + (endTime - startTime) + " ms");

                client.close();
            } catch (Exception e) {
                System.out.println("Error en el cliente: " + e.getMessage());
            }
        } else if (option == 2) {
            // Escenario Concurrente con Delegados
            System.out.println("Ingrese el número de delegados (4, 8 o 32):");
            int numDelegates = scanner.nextInt();

            if (numDelegates == 4 || numDelegates == 8 || numDelegates == 32) {
                ClientDelegate[] delegates = new ClientDelegate[numDelegates];
                long startTime = System.currentTimeMillis();

                for (int i = 0; i < numDelegates; i++) {
                    delegates[i] = new ClientDelegate("localhost", 1234, packageId);  // ID del paquete
                    delegates[i].start();
                }

                for (int i = 0; i < numDelegates; i++) {
                    try {
                        delegates[i].join();  // Esperar a que todos los delegados terminen
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                long endTime = System.currentTimeMillis();
                System.out.println("Tiempo total para " + numDelegates + " delegados: " + (endTime - startTime) + " ms");
            } else {
                System.out.println("Número de delegados no válido. Debe ser 4, 8 o 32.");
            }
        } else if (option == 3) {
            // Una única solicitud para ver los tiempos de cifrado
            try {
                PackageClient client = new PackageClient("localhost", 1234);
                System.out.println("Enviando una única consulta para el paquete ID: " + packageId);
                client.sendEncryptedQuery(packageId);
                String response = client.receiveDecryptedResponse();
                System.out.println("Respuesta del servidor:\n" + response);
                client.close();
            } catch (Exception e) {
                System.out.println("Error en el cliente: " + e.getMessage());
            }
        } else {
            System.out.println("Opción no válida.");
        }

        scanner.close();
    }
}

// Clase ClientDelegate para manejar cada consulta en paralelo
class ClientDelegate extends Thread {
    private String packageId;
    private String address;
    private int port;

    public ClientDelegate(String address, int port, String packageId) {
        this.address = address;
        this.port = port;
        this.packageId = packageId;
    }

    public void run() {
        try {
            PackageClient client = new PackageClient(address, port);
            client.sendEncryptedQuery(packageId);
            String response = client.receiveDecryptedResponse();
            System.out.println("Respuesta del servidor: " + response);
            client.close();
        } catch (Exception e) {
            System.out.println("Error en el delegado del cliente: " + e.getMessage());
        }
    }
}
