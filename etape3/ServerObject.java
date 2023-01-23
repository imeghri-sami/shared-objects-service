import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ServerObject implements ServerObject_itf{
    enum State {READ, WRITE, FREE}
    private State lock;
    private Client_itf writer;
    private List<Client_itf> readers;
    private Object obj;

    private Integer id;

    public ServerObject (Object o, int id){
        this.lock = State.FREE;
        this.obj = o;
        this.readers = new ArrayList<>();
        this.id = id;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object lock_read(int id, Client_itf client) {
        try {
            // if there is a writer
            if ( writer != null && lock == State.WRITE ){
                obj = writer.reduce_lock(id);
                readers.add(writer);
            }
            else if (writer != null) writer.invalidate_writer(id);

            readers.add(client);
            lock = State.READ;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Object lock_write(int id, Client_itf client) {
        if ( lock == State.READ) {
            readers.remove(client);
            for ( Client_itf reader : readers) {
                try {
                    reader.invalidate_reader(id);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (lock == State.WRITE) {
            try {
                obj = writer.invalidate_writer(id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


        writer = client;
        lock = State.WRITE;

        return null;
    }
}
