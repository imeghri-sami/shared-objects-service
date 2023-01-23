public class SentenceEx {
    String 		data;
    public SentenceEx() {
        data = new String("");
    }

    public void write(String text) {
        data = text;
    }
    public String read() {
        return data;
    }

    public String toString() {return data;}
}
