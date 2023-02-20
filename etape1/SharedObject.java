import java.io.*;
import java.rmi.RemoteException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SharedObject implements Serializable, SharedObject_itf {
	private Integer id;

	public Object obj;
	private State lock;

	private ReentrantLock monitor;

	private Condition accessReadWrite;

	private boolean thereIsWriter;

	private boolean isAccessWrite;

	public SharedObject() {
		this.monitor = new ReentrantLock();
		this.accessReadWrite = monitor.newCondition();

		this.lock = State.NL;
		this.isAccessWrite = true;
		this.thereIsWriter = false;
	}

	// invoked by the user program on the client node
	public void lock_read() {
		monitor.lock();

		while ( thereIsWriter ) {
			try {
				accessReadWrite.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		switch (lock) {
			case NL  :
				lock = State.RLT;
				obj = Client.lock_read(id);
				break;
			case RLC :
				lock = State.RLT;
				break;
			case WLC :
				lock = State.RLT_WLC;
				break;
		}
		monitor.unlock();
	}

	// invoked by the user program on the client node
	public void lock_write() {
		monitor.lock();

		while (!isAccessWrite) {
			try {
				accessReadWrite.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		switch (lock) {
			case NL  :
			case RLC :
				lock = State.WLT;
				obj = Client.lock_write(id);
				break;
			case WLC :
				lock = State.WLT;
				break;
		}
		monitor.unlock();
	}

	// invoked by the user program on the client node
	public synchronized void unlock() {
		// call the server method to notify subs
		if ( lock == State.WLT){
			try {
				Client.publish(id);
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}

		if ( lock == State.RLT) lock = State.RLC;
		else if ( lock == State.WLT || lock == State.RLT_WLC) lock = State.WLC;

		notify();
	}


	// callback invoked remotely by the server
	public synchronized Object reduce_lock() {

		isAccessWrite = false;

		while ( lock == State.WLT || lock == State.RLT_WLC) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if( lock == State.WLC ) lock = State.RLC;
		else if ( lock == State.RLT_WLC ) lock = State.RLT;

		isAccessWrite = true;
		notify();
		return obj;
	}

	@Override
	public Object getObject() {
		return obj;
	}

	// callback invoked remotely by the server
	public synchronized void invalidate_reader() {
		thereIsWriter = true; // suspend reader
		isAccessWrite = false; // suspend writer

		while (lock == State.RLT) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		if( lock == State.RLC ) lock = State.NL;
		thereIsWriter = false;
		isAccessWrite = true;
		notify();
	}

	public synchronized Object invalidate_writer() {

		isAccessWrite = false;


		while (lock == State.WLT || lock == State.RLT_WLC) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}


		if( lock == State.WLC ) lock = State.NL;
		isAccessWrite = true;
		notify();
		return obj;
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

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}
}
