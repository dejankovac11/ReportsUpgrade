import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.test.xsd.Izvod;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//svaki Izvod je jedan valueRange koji se sastoji iz imena (koji treba biti header
//i liste knjiga
//listu knjiga treba otpakovati tako da pokazuje svoje vrednosti


public class Main
{
    private static List<List<Object> > createBodyValues(Izvod izvod)
    {
        List<List<Object> >bodyValues=new ArrayList<List<Object>>();
        bodyValues.add(createLabels()); //first we create labels (one per each Izvod)
        Izvod.Prom prom =izvod.getProm();
        List<String> bodyValuesArgs=new ArrayList<String>();
        bodyValuesArgs.add(Byte.toString(izvod.getBrojIzvoda()));
        bodyValuesArgs.add(izvod.getDatumIzvoda().toString());
        bodyValuesArgs.add(izvod.getNazivUcesnikaPP());
        bodyValuesArgs.add(Float.toString(izvod.getPrethodnoStanje()));
        bodyValuesArgs.add(Float.toString(izvod.getDugovniPromet()));
        bodyValuesArgs.add(Float.toString(izvod.getPotrazniPromet()));
        bodyValuesArgs.add(Float.toString(izvod.getNovoStanje()));
        bodyValuesArgs.add(Byte.toString(izvod.getBrojNaloga()));
        bodyValuesArgs.add(izvod.getDatumRada().toString());
        bodyValuesArgs.add(prom.getBrojVirmana());
        bodyValuesArgs.add(prom.getNazivNal());
        bodyValuesArgs.add(prom.getMestoNal());
        bodyValuesArgs.add(prom.getOpis1());
        bodyValuesArgs.add(prom.getOpis2());
        bodyValuesArgs.add(prom.getRacunNal());
        bodyValuesArgs.add(Long.toString(prom.getBrojNaloga()));
        bodyValuesArgs.add(prom.getBrojVirmana());
        bodyValuesArgs.add(Short.toString(prom.getOsnov()));
        bodyValuesArgs.add(Float.toString(prom.getIznosDug()));
        bodyValuesArgs.add(Float.toString(prom.getIznosPot()));
        bodyValuesArgs.add(Byte.toString(prom.getModel()));
        bodyValuesArgs.add(prom.getPoziv());
        bodyValuesArgs.add(Byte.toString(prom.getModelVezni()));
        bodyValuesArgs.add(String.valueOf(prom.getPozivVezni()));
        bodyValuesArgs.add(prom.getDatumKnjizenja().toString());
        bodyValuesArgs.add(prom.getDatumKnjizenja().toString());
        bodyValues.add(new ArrayList<Object>(bodyValuesArgs));
        return bodyValues;
    }

    private static List<Object> createLabels()
    {

        List<String> labels= Arrays.asList("Racun",
                "broj Izvoda",
                "Datum Izvoda",
                "Naziv UcesnikaPP",
                "Prethodno Stanje",
                "Dugovni Promet",
                "Potrazni Promet",
                "Novo Stanje",
                "Broj Naloga",
                "Datum Rada",
                "prom","Naziv Naloga",
                "Mesto Naloga",
                "opis1",
                "opis2",
                "racun Nalaloga",
                "brojNaloga",
                "brojVirmana",
                "osnov",
                "Iznos Dug",
                "Iznos Pot",
                "model",
                "poziv",
                "model Vezni",
                "poziv Vezni",
                "Datum Knjizenja",
                "Datum Valute");
        return new ArrayList<Object>(labels);
    }

    private static String calculateStartingIndex(int startingIzvodIndex)
    {
        return "A"+((Integer)startingIzvodIndex).toString();
    }

    private static int updateStartingIndex(int startingCatalogIndex)
    {
        //additional rows for catalog name, one for labels, and one for blank row
        return startingCatalogIndex+3;
    }


    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        SpreadSheetWriter.setup(); //Establishes new connection and authorization
        SpreadSheetWriter.createSpreedsheet(); //Creates new empty spreadsheet
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller();
        List<Izvod> izvodi=izvodUnmarshaller.readXML(); //kreiranje i ucitavanje XML koda u listu Izvod objekata
        List<ValueRange> body=new ArrayList<ValueRange>(); //Content of the new spreadsheet
        int startingIzvodIndex=1; //where to begin writing new ValueRange object
        for (Izvod izvod:izvodi)
        {
            ValueRange valueRange=new ValueRange(); //valueRange for the current catalog
            String startingCatalogIndexString=calculateStartingIndex(startingIzvodIndex);//each new catalog begins at A1,A3,A5...
            valueRange.setRange(startingCatalogIndexString); //Initialize starting field for writing the contents of current catalogue

            //construct the argument of the setValues method called on current valueRange object
            //bodyValues represents the content of a single izvod
            List<List<Object> >bodyValues=createBodyValues(izvod);
            valueRange.setValues(bodyValues);
            body.add(valueRange);
            startingIzvodIndex=updateStartingIndex(startingIzvodIndex);
        }
        //after everything is read in, we update the whole content all at once
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(body);
        BatchUpdateValuesResponse batchResult = SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
    }
}