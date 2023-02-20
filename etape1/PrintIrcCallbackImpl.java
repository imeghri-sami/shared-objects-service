import java.awt.*;

public class PrintIrcCallbackImpl implements Callback_itf{

    private TextArea textArea;

    public PrintIrcCallbackImpl(TextArea ta){
        this.textArea = ta;
    }

    @Override
    public void callback(Object o) {
        textArea.append(o. toString() + "\n");
    }
}
