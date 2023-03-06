import java.awt.*;

public class NotifyIrcCallbackImpl implements Callback_itf{
    protected TextArea textArea;

    public NotifyIrcCallbackImpl(TextArea ta){
        this.textArea = ta;
    }

    @Override
    public void callback(Object o) {
        textArea.append("An object has been modified ...\n");
    }
}
