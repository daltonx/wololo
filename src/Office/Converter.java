package Office;

import Http.Request;
import Http.Response;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Converter {
    private Request req;
    private Response res;
    private Instance office;

    public Converter (Request req, Response res, Instance office) {
        this.req = req;
        this.res = res;
        this.office = office;
    }

    public Converter(Request req, Response res) {
        this.req = req;
        this.res = res;
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

    public void legacyPrint () {
        try {
            Instance instance = new Instance();
            instance.singleTask(req, res, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void convert () {

    }

    public void legacyConvert () {
        try {
            Instance instance = new Instance();
            instance.singleTask(req, res, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
