import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Vector;


public class Client extends UnicastRemoteObject implements Client_itf {
	private static final int RMI_REGISTRY_PORT = 50051;
	private static final String RMI_REGISTRY_HOSTNAME = "localhost";
	private static Server_itf server;
	private static List<SharedObject> sharedObjects = new Vector<>();

	private static Client thisClient;

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
				 Object o =  server.lock_read(objectReference, thisClient);
				 StubFilesGen.generate(o);
				 String className = o.getClass().getSimpleName() + "_stub";

				 try {
					 obj = (SharedObject) Class.forName(className).newInstance();
				 } catch (InstantiationException e) {
					 throw new RuntimeException(e);
				 } catch (IllegalAccessException e) {
					 throw new RuntimeException(e);
				 } catch (ClassNotFoundException e) {
					 throw new RuntimeException(e);
				 }

				 obj.setObj(o);
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
		try {
			int id = server.create(o);

			StubFilesGen.generate(o);

			String className = o.getClass().getSimpleName() + "_stub";

			SharedObject obj = (SharedObject) Class.forName(className).newInstance();

			obj.setObj(o);
			obj.setId(id);

			sharedObjects.add(obj);

			return obj;
		} catch (RemoteException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
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

	// request a write lock from the server
	public static Object lock_write (int id) {
		try {
			return server.lock_write(id, thisClient);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws RemoteException {
		return sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().reduce_lock();
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws RemoteException {
		sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().invalidate_reader();
	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws RemoteException {
		return sharedObjects
				.stream()
				.filter(e -> e.getId() == id)
				.findFirst().get().invalidate_writer();
	}
}
