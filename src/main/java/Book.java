import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

@XmlRootElement(name="book")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"id", "author","title","genre","price","publishDate", "description"})
public class Book {
    @XmlAttribute
    private  String id;
    @XmlElement
    private String author;
    @XmlElement
    private String title;
    @XmlElement
    private String genre;
    @XmlElement
    private double price;
    @XmlElement(name="publish_date")
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date publishDate;
    @XmlElement
    private String description;

    public Book()
    {
        this.id="";
        this.author="";
        this.title="";
        this.genre="";
        this.price=0.0;
        this.publishDate=null;
        this.description="";
    }
    public Book(String id, String author, String genre, double price, Date publishDate, String description)
    {
        this.id=id;
        this.author=author;
        this.genre=genre;
        this.price=price;
        this.publishDate=publishDate;
        this.description=description;
    }

    public String getDescription() {
        return description;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public String getTitle() {
        return title;
    }
    public double getPrice() {
        return price;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public String toString() {
        return "Book id: "+id+"\n"+ "Description: "+description+"\n"+"Genre: "+genre+"\n"+ "Price: "+price+"\n"+ "Publish date:"+publishDate.toString()+"\n";
    }
}
