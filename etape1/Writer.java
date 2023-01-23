public class Writer extends Thread{

    private String msg;
    SharedObject sharedObject;

    int sleepTime = 5;
    public Writer (String msg) {
        this.msg = msg;
        Client.init();
        sharedObject = Client.lookup("IRC");
        if ( sharedObject == null ) {
            sharedObject = Client.create(new Sentence());
            Client.register("IRC", sharedObject);
        }
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public void run() {
        sharedObject.lock_write();
        try {
            Thread.sleep(1000 * sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Writer starts writing ...");

        ((Sentence)(sharedObject.obj)).write("Writer wrote " + msg);

        sharedObject.unlock();
    }
}
