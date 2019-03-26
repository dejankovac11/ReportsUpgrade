import com.test.xsd.Izvod;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/*
    Ucitava (unmarshall-uje) json u listu kataloga
 */


public class IzvodUnmarshaller {
    private File directory;
    private JAXBContext jaxbContext;
    private List<Izvod>izvodList;

    public IzvodUnmarshaller(File directory)
    {
        this.directory=directory;
        try {
            this.jaxbContext = JAXBContext.newInstance(Izvod.class);
        }
        catch (JAXBException e)
        {
            System.err.println("Unable to create an instance of JAXB object");
            e.printStackTrace();
        }
        izvodList=new ArrayList<Izvod>();
    }
    public IzvodUnmarshaller()
    {
        this.directory=new File("/home/korisnik/Desktop/XML");
        try {
            this.jaxbContext = JAXBContext.newInstance(Izvod.class);
        }
        catch (JAXBException e)
        {
            System.err.println("Unable to create an instance of JAXB object");
            e.printStackTrace();
        }
        izvodList=new ArrayList<Izvod>();
    }
    public void setDirectory(File directory) {
        this.directory = directory;
    }
    public List<Izvod> readXML()
    {
        Izvod izvod=new Izvod();
        for (File file: directory.listFiles())
        {
            try {
                Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
                izvod = (Izvod) unmarshaller.unmarshal(file);
            }
            catch(JAXBException e)
            {
                System.err.println("Error while parsing XML file");
                e.printStackTrace();
            }
            this.izvodList.add(izvod);
        }
        return this.izvodList;
    }

}
