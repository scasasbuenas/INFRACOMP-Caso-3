import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class PackageServer {
    private ServerSocket serverSocket;
    private ServerData serverData;
    private PrivateKey privateKey;
    private boolean keysInitialized = false;

    public PackageServer(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        serverData = new ServerData();
        System.out.println("Servidor iniciado en el puerto " + port);
    }

    private void initializeSecurity() throws Exception {
        // Genera un nuevo par de claves
        KeyPair keyPair = CryptoUtil.generateRSAKeyPair();
        privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // Guardar la clave pública y privada en archivos
        CryptoUtil.savePublicKey(publicKey, "publicKey.ser");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("privateKey.ser"))) {
            out.writeObject(privateKey);
        }

        System.out.println("Claves generadas y almacenadas correctamente.");
        keysInitialized = true;
    }

    private void loadPrivateKey() throws Exception {
        privateKey = CryptoUtil.loadPrivateKey("privateKey.ser");
    }

    public void start() throws Exception {
        if (!keysInitialized) {
            loadPrivateKey();
        }
        System.out.println("Clave privada cargada, esperando clientes...");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Conexión aceptada de: " + clientSocket.getInetAddress().getHostAddress());
            new ClientHandler(clientSocket, serverData, privateKey).start();
        }
    }

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Ingrese el puerto del servidor:");
            int port = scanner.nextInt();
            PackageServer server = new PackageServer(port);

            while (true) {
                System.out.println("Seleccione una opción:");
                System.out.println("1. Generar y almacenar claves.");
                System.out.println("2. Iniciar servidor.");
                int option = scanner.nextInt();

                if (option == 1) {
                    server.initializeSecurity();
                } else if (option == 2) {
                    server.start();
                } else {
                    System.out.println("Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }
}
