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
            File inputFile = Util.writeTempFile("input_", ".tmp", req.body);
            inputFile.deleteOnExit();

            Path outputFile = Util.newTempPath("output_", ".pdf");

            Document document = new Document(office);
            document.load(inputFile.getPath());
            document.print(outputFile.toString());
            res.file(outputFile.toString(), "application/pdf");
            res.ready = true;
            document.dispose();
            inputFile.delete();
            Files.delete(outputFile);
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    public void convert () {
        try {
            File inputFile = Util.writeTempFile("input_", ".tmp", req.body);
            inputFile.deleteOnExit();

            Path outputFile = Util.newTempPath("output_", ".pdf");

            Document document = new Document(office);
            document.load(inputFile.getPath());
            document.convert(outputFile.toString());
            res.file(outputFile.toString(), "application/pdf");
            res.ready = true;
            document.dispose();
            inputFile.delete();
            Files.delete(outputFile);
        } catch (IOException e) {
            System.out.print(e);
        }
    }

    public void legacyPrint () {
        try {
            Instance instance = new Instance();
            instance.singleTask(req, res, false);
        } catch (Exception e) {
            System.out.print(e);
        }
    }

    public void legacyConvert () {
        try {
            Instance instance = new Instance();
            instance.singleTask(req, res, true);
        } catch (Exception e) {
            System.out.print(e);
        }
    }
}
