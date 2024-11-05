import java.io.*;
import java.net.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ServerData serverData;
    private PrivateKey privateKey;
    private PublicKey clientPublicKey;  // Definimos clientPublicKey como un atributo de la clase
    private SecretKey sessionKey;

    public ClientHandler(Socket socket, ServerData serverData, PrivateKey privateKey) {
        this.clientSocket = socket;
        this.serverData = serverData;
        this.privateKey = privateKey;
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error al iniciar los streams de entrada/salida: " + e.getMessage());
        }
    }

    public void run() {
        try {
            initializeSession();
            handleClientCommunication();
        } catch (Exception e) {
            System.out.println("Error al manejar al cliente: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error al cerrar las conexiones: " + e.getMessage());
            }
        }
    }

    private void initializeSession() throws Exception {
        // Recibir la clave pública del cliente y almacenarla
        System.out.println("Esperando la clave pública del cliente...");
        clientPublicKey = (PublicKey) in.readObject();  // Guardamos la clave pública del cliente
        System.out.println("Clave pública del cliente recibida.");

        // Generar la clave simétrica para la sesión
        SecretKey symmetricKey = CryptoUtil.generateAESKey();
        byte[] encryptedKey = CryptoUtil.encryptRSA(clientPublicKey, symmetricKey.getEncoded());

        // Enviar la clave simétrica cifrada al cliente
        System.out.println("Enviando la clave simétrica cifrada al cliente...");
        out.writeObject(encryptedKey);
        out.flush();

        // Guardar la clave de sesión para su uso posterior
        this.sessionKey = symmetricKey;
        System.out.println("Clave simétrica de sesión establecida.");
    }

    private void handleClientCommunication() throws Exception {
        System.out.println("Esperando consultas del cliente...");
    
        while (true) {
            // Leer la solicitud encriptada del cliente
            byte[] encryptedRequest = (byte[]) in.readObject();
            System.out.println("Solicitud cifrada recibida del cliente.");
    
            // Desencriptar la solicitud usando la clave de sesión
            byte[] decryptedData = CryptoUtil.decryptAES(sessionKey, encryptedRequest);
            String request = new String(decryptedData);
            System.out.println("Solicitud descifrada: " + request);
    
            // Procesar la solicitud y generar una respuesta
            String response = processRequest(request);
    
            // Medir el tiempo de cifrado simétrico (AES)
            long startSymmetric = System.nanoTime();
            byte[] encryptedResponseAES = CryptoUtil.encryptAES(sessionKey, response.getBytes());
            long endSymmetric = System.nanoTime();
            long symmetricTime = endSymmetric - startSymmetric;
            System.out.println("Tiempo de cifrado simétrico (AES): " + symmetricTime + " ns");
    
            // Medir el tiempo de cifrado asimétrico (RSA)
            long startAsymmetric = System.nanoTime();
            byte[] encryptedResponseRSA = CryptoUtil.encryptRSA(clientPublicKey, response.getBytes());
            long endAsymmetric = System.nanoTime();
            long asymmetricTime = endAsymmetric - startAsymmetric;
            System.out.println("Tiempo de cifrado asimétrico (RSA): " + asymmetricTime + " ns");
    
            // Crear un mensaje que incluya los tiempos de cifrado
            String fullResponse = response + "\n" +
                                  "Tiempo de cifrado simétrico (AES): " + symmetricTime + " ns\n" +
                                  "Tiempo de cifrado asimétrico (RSA): " + asymmetricTime + " ns";
    
            // Cifrar la respuesta completa usando AES para enviar al cliente
            byte[] finalEncryptedResponse = CryptoUtil.encryptAES(sessionKey, fullResponse.getBytes());
            
            // Enviar la respuesta cifrada al cliente
            out.writeObject(finalEncryptedResponse);
            out.flush();
            System.out.println("Respuesta completa con tiempos de cifrado enviada al cliente.");
        }
    }

    private String processRequest(String request) {
        System.out.println("Consultando estado para el paquete con ID: " + request);
        int status = serverData.getPackageStatus(request);
        switch (status) {
            case ServerData.EN_OFICINA:
                return "El paquete está en la oficina.";
            case ServerData.RECOGIDO:
                return "El paquete ha sido recogido.";
            case ServerData.EN_CLASIFICACION:
                return "El paquete está en clasificación.";
            case ServerData.DESPACHADO:
                return "El paquete ha sido despachado.";
            case ServerData.EN_ENTREGA:
                return "El paquete está en camino de entrega.";
            case ServerData.ENTREGADO:
                return "El paquete ha sido entregado.";
            default:
                return "Estado del paquete desconocido.";
        }
    }
}
