import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

//svaki katalog je jedan valueRange koji se sastoji iz imena (koji treba biti header
//i liste knjiga
//listu knjiga treba otpakovati tako da pokazuje svoje vrednosti


public class Main
{
    private static List<List<Object> > createBodyValues(Catalog catalog)
    {
        List<List<Object> >bodyValues=new ArrayList<List<Object>>();
        bodyValues.add(createBookStoreTitle(catalog.getCatalogName())); //catalog's name corresponds to the bookstore's
        bodyValues.add(createlabels()); //first we create labels (one per each catalog)
        List<Book> books=catalog.getBookList();
        for(Book book:books)
        {
            List<String> bodyValuesArgs=new ArrayList<String>(); //contents of a single book
            bodyValuesArgs.add(book.getTitle());
            bodyValuesArgs.add(book.getId());
            bodyValuesArgs.add(book.getAuthor());
            bodyValuesArgs.add(book.getPublishDate().toString());
            bodyValuesArgs.add(book.getDescription());
            bodyValuesArgs.add(((Double) book.getPrice()).toString());
            List<Object>bodyValuesObjectArgs=new ArrayList<Object>(bodyValuesArgs);
            bodyValues.add(bodyValuesObjectArgs);
        }
        return bodyValues;
    }

    private static List<Object> createlabels()
    {
        List<String> labels=new ArrayList<String>();
        labels.add("Book Title");
        labels.add("Book ID");
        labels.add("Author");
        labels.add("Publish Date");
        labels.add("Book description");
        labels.add("Book Price");
        return new ArrayList<Object>(labels);
    }
    private static List<Object>createBookStoreTitle(String catalogName)
    {
        List<String> catalogNameList=new ArrayList<String>();
        catalogNameList.add(catalogName);
        return new ArrayList<Object>(catalogNameList);
    }


    private static String calculateStartingIndex(int startingCatalogIndex)
    {
        return "A"+((Integer)startingCatalogIndex).toString();
    }

    private static int updateStartingIndex(int startingCatalogIndex, int valueRangeLength)
    {
        //additional rows for catalog name, one for labels, and one for blank row
        return startingCatalogIndex+valueRangeLength+3;
    }


    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        SpreadSheetWriter.setup(); //Establishes new connection and authorization
        SpreadSheetWriter.createSpreedsheet(); //Creates new empty spreadsheet
        BookUnmarshaller bookUnmarshaller=new BookUnmarshaller();
        List<Catalog> catalogs=bookUnmarshaller.readXML(); //kreiranje i ucitavanje XML koda u listu Catalog objekata
        List<ValueRange> body=new ArrayList<ValueRange>(); //Content of the new spreadsheet
        int startingCatalogIndex=1; //where to begin writing new ValueRange object
        int valueRangeLength;
        for (Catalog catalog:catalogs)
        {
            valueRangeLength=catalog.getBookList().size();
            ValueRange valueRange=new ValueRange(); //valueRange for the current catalog
            String startingCatalogIndexString=calculateStartingIndex(startingCatalogIndex);//each new catalog begins at A1,A3,A5...
            valueRange.setRange(startingCatalogIndexString); //Initialize starting field for writing the contents of current catalogue

            //construct the argument of the setValues method called on current valueRange object
            //bodyValues represents the content of a single catalog
            List<List<Object> >bodyValues=createBodyValues(catalog);
            valueRange.setValues(bodyValues);
            body.add(valueRange);
            startingCatalogIndex=updateStartingIndex(startingCatalogIndex,valueRangeLength);
        }
        //after everything is read in, we update the whole content all at once
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(body);
        BatchUpdateValuesResponse batchResult = SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
    }
}