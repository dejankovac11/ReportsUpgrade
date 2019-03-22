import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
/*
    Ucitava (unmarshall-uje) json u listu kataloga

 */


public class BookUnmarshaller {
    private File directory;
    private JAXBContext jaxbContext;
    private List<Catalog>catalogList;

    public BookUnmarshaller(File directory)
    {
        this.directory=directory;
        try {
            this.jaxbContext = JAXBContext.newInstance(Catalog.class);
        }
        catch (JAXBException e)
        {
            System.err.println("Unable to create an instance of JAXB object");
            e.printStackTrace();
        }
        catalogList=new ArrayList<Catalog>();
    }
    public BookUnmarshaller()
    {
        this.directory=new File("/home/korisnik/Desktop/bookList");
        try {
            this.jaxbContext = JAXBContext.newInstance(Catalog.class);
        }
        catch (JAXBException e)
        {
            System.err.println("Unable to create an instance of JAXB object");
            e.printStackTrace();
        }
        catalogList=new ArrayList<Catalog>();
    }
    public void setDirectory(File directory) {
        this.directory = directory;
    }
    public List<Catalog> readXML()
    {
        String catalogName;
        for (File file: directory.listFiles())
        {
            catalogName=file.getName();
            Catalog catalog=new Catalog(catalogName);
            try {
                Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
                catalog = (Catalog) unmarshaller.unmarshal(file);
            }
            catch(JAXBException e)
            {
                System.err.println("Error while parsing XML file "+catalogName);
                e.printStackTrace();
            }
            this.catalogList.add(catalog);
        }
        return this.catalogList;
    }


    /*public static void main(String[] args) throws Exception {
        File directory = new File("/home/korisnik/Desktop/bookList");
        JAXBContext jaxbContext = JAXBContext.newInstance(Catalog.class);
        List <Catalog> catalogList=new ArrayList<Catalog>();

        for(Catalog c:catalogList)
        {
            System.out.print(c.toString());
        }
    } */
}
