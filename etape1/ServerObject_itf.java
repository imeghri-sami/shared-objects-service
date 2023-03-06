import java.util.List;
import java.util.Map;

public interface ServerObject_itf {
    Object lock_read(int id, Client_itf client);
    Object lock_write(int id, Client_itf client);

    void addSubscriber(Client_itf sub);
    void removeSubscriber(Client_itf sub);
}
