import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Catalog {
    @XmlTransient
    private String catalogName;
    @XmlElement(name="book")
    private List<Book> bookList;

    public Catalog()
    {
        this.catalogName="";
        this.bookList=null;
    }
    public Catalog(String catalogName, List<Book> bookList)
    {
        this.catalogName=catalogName;
        this.bookList=bookList;
    }
    public Catalog(String catalogName)
    {
        this.catalogName=catalogName;
        this.bookList=new ArrayList<Book>();
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setBookList(List<Book> bookList) {
        this.bookList = bookList;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append("Catalog name: "+catalogName+"\n");
        for (Book b:this.bookList)
        {
            sb.append(b.toString()+"\n");
        }
        return sb.toString();
    }
}
