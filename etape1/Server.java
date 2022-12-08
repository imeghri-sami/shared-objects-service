import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server extends UnicastRemoteObject implements Server_itf {

    static final int RMI_REGISTRY_PORT = 50051;
    static final String RMI_REGISTRY_HOSTNAME = "localhost";

    private Map<Integer, ServerObject> serverObjects;
    private Map<String, Integer> bindingMap;

    private int autoIncrementID;

    public static void main(String[] args) {

        try {
            Server server = new Server();
            LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
            Naming.rebind("rmi://" + RMI_REGISTRY_HOSTNAME + ":" + RMI_REGISTRY_PORT + "/server", server);
        } catch (RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    protected Server() throws RemoteException {
        serverObjects = new HashMap<>();
        bindingMap = new HashMap<>();
        autoIncrementID = 0;
    }

    @Override
    public int lookup(String name) throws RemoteException {
        return Optional.of(bindingMap.get(name)).orElse(-1);
    }

    @Override
    public void register(String name, int id) throws RemoteException {
        if ( !serverObjects.containsKey(id) )
            throw new IllegalArgumentException(
                    String.format("No server object found with the id : %d ", id)
            );

        // If an object is already exist with the same name, its value will be replaced
        bindingMap.put(name, id);
    }

    @Override
    public int create(Object o) throws RemoteException {
        ServerObject serverObject = new ServerObject(o);
        serverObjects.put(autoIncrementID, serverObject);
        return autoIncrementID++;
    }

    @Override
    public Object lock_read(int id, Client_itf client) throws RemoteException {
        return null;
    }

    @Override
    public Object lock_write(int id, Client_itf client) throws RemoteException {
        return null;
    }
}
