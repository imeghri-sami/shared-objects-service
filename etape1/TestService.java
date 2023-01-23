/*Les tests ne fonctionnent pas*/
public class TestService {
    public static void main(String[] args) {

        if ( args.length <= 0 ){
            System.out.println("USAGE : java TestService <option> ");
            System.out.println("<option> :");
            System.out.println("-1rnw [number_of_writer] [msg] [sleep_time]");
            System.out.println("-1wnr [number_of_readers] [msg] [sleep_time]");
        }else {

            int nb = Integer.parseInt(args[1]);
            String msg = args[2];
            int sleepTime = Integer.parseInt(args[3]);

            String option = args[0];
            if (option.equals("-1rnw")) {
                testOneReaderMultiplesWriter(nb, msg);
            } else if (option.equals("-1wnr")) {
                testOneWriterMultiplesReader(nb, msg);
            }
        }

    }

    private static void testOneWriterMultiplesReader(int nbReaders, String msg) {

        Writer writer = new Writer(msg);
        writer.start();

        Reader[] readers = new Reader[nbReaders];

        for (int i=0; i < nbReaders; i++) {
            readers[i] = new Reader();
            readers[i].start();
        }

    }

    private static void testOneReaderMultiplesWriter(int nbWriter, String msg) {

        Reader reader = new Reader();
        reader.start();

        Writer[] writers = new Writer[nbWriter];

        for (int i=0; i < nbWriter; i++) {
            writers[i] = new Writer(msg + i);
            writers[i].start();
        }

    }
}
