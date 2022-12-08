import java.io.*;

public class SharedObject implements Serializable, SharedObject_itf {
	private Integer id;
	public Object obj;
	private State lock;

	// invoked by the user program on the client node
	public void lock_read() {
	}

	// invoked by the user program on the client node
	public void lock_write() {
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {
		return null;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
	}

	public synchronized Object invalidate_writer() {
		return null;
	}

	public Integer getId(){
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public State getLock() {
		return lock;
	}

	public void setLock(State lock) {
		this.lock = lock;
	}
}
