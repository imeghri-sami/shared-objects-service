public interface ServerObject_itf {
    Object lock_read(int id, Client_itf client);
    Object lock_write(int id, Client_itf client);
}
