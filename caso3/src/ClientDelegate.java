public class ClientDelegate extends Thread {
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
