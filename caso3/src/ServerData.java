import java.util.concurrent.ConcurrentHashMap;

public class ServerData {
    public static final int EN_OFICINA = 0;
    public static final int RECOGIDO = 1;
    public static final int EN_CLASIFICACION = 2;
    public static final int DESPACHADO = 3;
    public static final int EN_ENTREGA = 4;
    public static final int ENTREGADO = 5;
    public static final int DESCONOCIDO = 6;

    private ConcurrentHashMap<String, Integer> packageStatus;

    public ServerData() {
        packageStatus = new ConcurrentHashMap<>();
        // Datos de prueba
        packageStatus.put("1234", EN_OFICINA);
        packageStatus.put("5678", RECOGIDO);
        // Puedes agregar más datos de prueba aquí si es necesario
    }

    public int getPackageStatus(String packageId) {
        return packageStatus.getOrDefault(packageId, DESCONOCIDO);
    }
}
