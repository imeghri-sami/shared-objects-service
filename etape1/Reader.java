public class Reader extends Thread{

    SharedObject sharedObject;

    int sleepTime = 5;
    public Reader () {
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
        sharedObject.lock_read();
        try {
            Thread.sleep(1000 * sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Reader starts reading ...");

        String s = ((Sentence)(sharedObject.obj)).read();

        System.out.println("I read " + s);

        sharedObject.unlock();
    }
}
