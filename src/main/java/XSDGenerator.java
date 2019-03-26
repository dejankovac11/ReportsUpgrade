import org.wiztools.xsdgen.ParseException;
import org.wiztools.xsdgen.XsdGen;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XSDGenerator {
    File file;
    String result;

    public XSDGenerator(File file) {
        this.file = file;
    }

    public XSDGenerator() {
        this.file = null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getResult() throws IOException, ParseException {
        this.result = new XsdGen().parse(this.file).toString();
        return this.result;
    }

    public String getFormattedResult() {
        try {
            Source xmlInput = new StreamSource(new StringReader(this.getResult()));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void writeXSD() throws IOException
    {
        String result=this.getFormattedResult();
        File out=new File("/reportsXML/src/main/resources");
        FileWriter writer=new FileWriter(out);
        writer.append(result);
        writer.close();
    }
}

