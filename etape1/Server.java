import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server extends UnicastRemoteObject implements Server_itf {

    static final int RMI_REGISTRY_PORT = 50051;
    static final String RMI_REGISTRY_HOSTNAME = "localhost";

    private Map<Integer, ServerObject> serverObjects;
    private Map<String, Integer> bindingMap;
    private AtomicInteger atomicInteger;

    private Map<Integer, List<Client_itf>> subs;


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
        subs = new HashMap<>();
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

    //Notify all the subs by calling their callback
    @Override
    public void publish(int id, Client_itf writer) throws RemoteException{


        if ( subs.isEmpty() ) {
            System.out.println("no subscriber found, the map is empty");
            return;
        }

        Optional.of(subs.get(id))
                .orElse(Collections.emptyList())
                .forEach(c -> {
            try {
                if (serverObjects.get(id).getObj() == null ) {
                    System.out.println("Null object !");
                }
                // retrieve the modified shared object from the Client
                // Execute the callback method of each client
                c.callback(writer.getUpdatedObject(id));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void subscribe(int id, Client_itf client) throws java.rmi.RemoteException{

        if( subs.containsKey(id) ) {
            List<Client_itf> subscribedClients = subs.get(id);

            if( subscribedClients.contains(client)) {
                System.out.println("Client is already subscribed !");
                return;
            }
            subscribedClients.add(client);
        }
        // add a new subscriber for a new shared object
        else {
            List<Client_itf> newSubs = new ArrayList<>();
            newSubs.add(client);
            subs.put(id, newSubs);
        }
        System.out.println("Subscribed !");
    }

    @Override
    public void unsubscribe(Integer id, Client_itf thisClient) throws RemoteException {
        if( subs.containsKey(id) ) subs.get(id).remove(thisClient);
        System.out.println("Unsubscribed !");
    }
}
