import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends UnicastRemoteObject implements Server_itf {

    static final int RMI_REGISTRY_PORT = 50051;
    static final String RMI_REGISTRY_HOSTNAME = "localhost";

    private Map<Integer, ServerObject> serverObjects;
    private Map<String, Integer> bindingMap;
    private AtomicInteger atomicInteger;


    public static void main(String[] args) {

        try {
            Server server = new Server();
            LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
            Naming.rebind("rmi://" + RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + "/server", server);
            System.out.println("RMI registry started");
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected Server() throws RemoteException {
        atomicInteger = new AtomicInteger();
        serverObjects = new HashMap<>();
        bindingMap = new HashMap<>();
    }

    @Override
    public int lookup(String name) throws RemoteException {
        Integer id = bindingMap.get(name);
        return id == null ? -1 : id;
    }

    @Override
    public void register(String name, int id) throws RemoteException {
        if ( !serverObjects.containsKey(id) )
            throw new IllegalArgumentException(
                    String.format("No server object found with the id : %d ", id)
            );
        if( bindingMap.containsKey(name)){
            System.out.println("WARRING : an object is already bounded to this name, its value will be replaced");
        }
        // If an object is already exist with the same name, its value will be replaced
        bindingMap.put(name, id);
    }

    @Override
    public int create(Object o) throws RemoteException {
        int id = atomicInteger.incrementAndGet();
        ServerObject serverObject = new ServerObject(o, id);
        serverObjects.put(id, serverObject);
        return id;
    }

    @Override
    public Object lock_read(int id, Client_itf client) throws RemoteException {
        ServerObject serverObject = serverObjects.get(id);
        serverObject.lock_read(id, client);
        return serverObject.getObj();
    }

    @Override
    public Object lock_write(int id, Client_itf client) throws RemoteException {
        ServerObject serverObject = serverObjects.get(id);
        serverObject.lock_write(id, client);
        return serverObject.getObj();
    }
}
