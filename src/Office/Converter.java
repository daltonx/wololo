package Office;

import Http.Request;
import Http.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Converter {
    private Request req;
    private Response res;
    private Instance office;

    public Converter (Request req, Response res, Instance office) {
        this.req = req;
        this.res = res;
        this.office = office;
    }

    public void print () {
        try {
            File inputFile = File.createTempFile("input_", ".xx");
            Files.write(inputFile.toPath(), req.body);

            File outputFile = File.createTempFile("output_", ".pdf");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();

            Document document = new Document(office);
            document.load(inputFile.getPath());
            document.print(outputFile.getPath());
            res.file(outputFile.getPath(), "application/pdf");
            res.ready = true;
            document.dispose();
            inputFile.delete();
            outputFile.delete();
        } catch (IOException e) {
            //throw new RuntimeException(e);
        }
    }
}
