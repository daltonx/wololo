import Http.Server;
import com.sun.star.uno.Exception;

import java.io.IOException;
import java.util.Objects;

public class Main {
    public static void main (String [] args) throws Exception, IOException {
        boolean authorize = Objects.equals(System.getenv("AUTHORIZE"), "true");
        String secretKey = Objects.requireNonNullElse(System.getenv("SECRET_KEY"), "SECRET_KEY");
        new Server(authorize, secretKey);
        //test();
    }

    /*public static void test () {
        for (int i = 1; i < 9; i++) {
            final int xx = i;
            final Instance office = new Instance();
            office.pipeName = "teste123";
            (new Thread(new Runnable() {
                int p = xx;
                Instance xoffice = office;
                public void run() {
                    String[] files = {"1.xslx", "2.doc", "3.DOCX", "4.xlsx", "5.docx", "6.pptx", "7.docx", "8.xlsx", "9.docx"};
                    long start = System.currentTimeMillis();

                    while (true) {
                        office.connect();
                        if (xoffice.state == State.READY)
                            break;

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            System.out.println("cant connect");
                        }
                    }

                    File inputFile = new File("/Users/dalton/Downloads/inputs/x/" + files[xx]);
                    File outputFile = new File("/Users/dalton/Downloads/inputs/x/" + files[xx] + ".pdf");

                    Document document = new Document(xoffice);
                    document.load(inputFile.getPath());
                    System.out.println(p + " Loaded");
                    document.convert(outputFile.toString());
                    System.out.println(p + " Converted");
                    long end = System.currentTimeMillis();
                    System.out.println(p + " Finished in "+ (end - start) / 1000);
                    document.dispose();

                };
            })).start();
        }
    }*/
}