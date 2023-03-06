import java.awt.*;

public class PrintIrcCallbackImpl implements Callback_itf{

    private TextArea textArea;

    private SharedObject sharedObject;

    public PrintIrcCallbackImpl(TextArea ta){
        this.textArea = ta;
    }

    @Override
    public void callback(Object o) {
        sharedObject.setObj(o);
        textArea.append(o. toString() + "\n");
    }

    public void setSharedObject(SharedObject sharedObject) {
        this.sharedObject = sharedObject;
    }
}
