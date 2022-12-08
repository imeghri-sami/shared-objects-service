import java.util.ArrayList;
import java.util.List;

public class ServerObject implements ServerObject_itf{

    private State lock;
    private Object obj;
    private Client_itf writer;
    private List<Client_itf> readers;

    public ServerObject (Object o){
        this.lock = State.NL;
        this.obj = o;
        this.readers = new ArrayList<>();
    }

    @Override
    public Object lock_read(int id, Client_itf client) {
        return null;
    }

    @Override
    public Object lock_write(int id, Client_itf client) {
        return null;
    }
}
