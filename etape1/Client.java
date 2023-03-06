import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.net.*;
import java.util.List;
import java.util.Vector;


public class Client extends UnicastRemoteObject implements Client_itf {
	private static final int RMI_REGISTRY_PORT = 50051;
	private static final String RMI_REGISTRY_HOSTNAME = "localhost";
	private static Server_itf server;
	private static List<SharedObject> sharedObjects = new Vector<>();

	private static Client thisClient;

	private static Callback_itf callbackObject;

	private static Callback_itf trackCallback;

	private static Callback_itf notifyCallback;

	private boolean isTracked = false;

	@Override
	public void callback(Object o) throws RemoteException{
		if ( callbackObject == null ) return;
		callbackObject.callback(o);
	}

	public Client() throws RemoteException {
		super();
	}


///////////////////////////////////////////////////
//         Interface to be used by applications
///////////////////////////////////////////////////

	// initialization of the client layer
	public static void init() {
		try {
			server = (Server_itf) Naming.lookup("rmi://"
					+ RMI_REGISTRY_HOSTNAME + ":"
					+ RMI_REGISTRY_PORT
					+ "/server");
			if ( server == null) System.out.println("Server null");
			thisClient = new Client();
		} catch (NotBoundException | MalformedURLException | RemoteException e) {
			e.printStackTrace();
		}
	}

	public static void setCallbackObject(Callback_itf callbackObject){
		Client.callbackObject = callbackObject;
	}
	public static void initCallbacks(Callback_itf tcb, Callback_itf notifcb){
		trackCallback = tcb;
		notifyCallback = notifcb;
	}

	// lookup in the name server
	public static SharedObject lookup(String name) {
		try {
			final int objectReference = server.lookup(name);

			if( objectReference == -1 )
				return null;

			 SharedObject obj = sharedObjects
					.stream()
					.filter(e -> e.getId() == objectReference)
					.findFirst().orElse(null);
			 if (obj == null) {
				 // add the shared object to the list
				 obj = new SharedObject();
				 obj.setId(objectReference);
				 sharedObjects.add(obj);
			 }

			 return obj;
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	// binding in the name server
	public static void register(String name, SharedObject_itf so) {
		try {
			server.register(name, ((SharedObject)so).getId());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	// creation of a shared object
	public static SharedObject create(Object o) {
		SharedObject sharedObject = new SharedObject();
		try {
			sharedObject.setId(server.create(o));
			sharedObject.setObj(o);
			sharedObjects.add(sharedObject);
			return sharedObject;
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
		try {
			return server.lock_read(id, thisClient);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	// request a writing lock from the server
	public static Object lock_write (int id) {
		try {
			return server.lock_write(id, thisClient);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		return sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().reduce_lock();
	}

	@Override
	public Object getUpdatedObject(int id) throws java.rmi.RemoteException {
		return sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().getObject();
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id, boolean isSubscribed) throws java.rmi.RemoteException {
		sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().invalidate_reader(isSubscribed);
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id, boolean isSubscribed) throws java.rmi.RemoteException {
		return sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().invalidate_writer(isSubscribed);
	}

	public static void publish(int id) throws RemoteException {
		System.out.println("Object published ...");
		Object o = null;
		for ( SharedObject so : sharedObjects) {
			if( so.getId() == id ) o = so.getObj();
		}
		server.publish(id, o, thisClient);
	}

	public static void subscribe(int id) {
		try {
			server.subscribe(id, thisClient);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

	}

	public static void unsubscribe(Integer id) throws RemoteException {
		callbackObject = null;
		server.unsubscribe(id, thisClient);
	}

	public void track() {
		isTracked = true;
	}

	public void leave_track(){
		isTracked = false;
	}
}
