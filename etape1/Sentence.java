public class Sentence implements java.io.Serializable {
	String 		data;
	public Sentence() {
		data = new String("");
	}
	
	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;	
	}

	@Override
	public String toString() {
		return "hello : " + data == null ? "null" : data;
	}
}