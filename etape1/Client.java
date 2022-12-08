import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.net.*;

public class Client extends UnicastRemoteObject implements Client_itf {
	private static final int RMI_REGISTRY_PORT = 50051;
	private static final String RMI_REGISTRY_HOSTNAME = "localhost";
	private static Server_itf server;

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
		} catch (NotBoundException | MalformedURLException | RemoteException e) {
			e.printStackTrace();
		}
	}
	// TODO : return a sharedObject instance
	// lookup in the name server
	public static SharedObject lookup(String name) {
		try {
			server.lookup(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		return null;
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
		sharedObject.obj = o;
		try {
			sharedObject.setId(server.create(o));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return sharedObject;
	}
	
/////////////////////////////////////////////////////////////
//    Interface to be used by the consistency protocol
////////////////////////////////////////////////////////////

	// request a read lock from the server
	public static Object lock_read(int id) {
		return null;
	}

	// request a write lock from the server
	public static Object lock_write (int id) {
		return null;
	}

	// receive a lock reduction request from the server
	public Object reduce_lock(int id) throws java.rmi.RemoteException {
		return null;
	}


	// receive a reader invalidation request from the server
	public void invalidate_reader(int id) throws java.rmi.RemoteException {

	}


	// receive a writer invalidation request from the server
	public Object invalidate_writer(int id) throws java.rmi.RemoteException {
		return null;
	}
}
