import com.google.api.services.sheets.v4.model.*;
import com.test.xsd.Izvod;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main
{
    private static List<List<Object> > createBodyValues(Izvod izvod, int startingIzvodIndex)
    {
        List<List<Object> >bodyValues=new ArrayList<List<Object>>();
        if(startingIzvodIndex==1)
            bodyValues.add(createLabels());
        Izvod.Prom prom =izvod.getProm();
        List<String> bodyValuesArgs=new ArrayList<String>();
        bodyValuesArgs.add(izvod.getRacun());
        bodyValuesArgs.add(Byte.toString(izvod.getBrojIzvoda()));
        bodyValuesArgs.add(izvod.getDatumIzvoda().toString());
        bodyValuesArgs.add(izvod.getNazivUcesnikaPP());
        bodyValuesArgs.add(Float.toString(izvod.getPrethodnoStanje()));
        bodyValuesArgs.add(Float.toString(izvod.getDugovniPromet()));
        bodyValuesArgs.add(Float.toString(izvod.getPotrazniPromet()));
        bodyValuesArgs.add(Float.toString(izvod.getNovoStanje()));
        bodyValuesArgs.add(Byte.toString(izvod.getBrojNaloga()));
        bodyValuesArgs.add(izvod.getDatumRada().toString());
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
        bodyValuesArgs.add(prom.getDatumValute().toString());
        bodyValues.add(new ArrayList<Object>(bodyValuesArgs));
        return bodyValues;
    }

    private static List<Object> createLabels()
    {

        Izvod.class.getFields().toString();
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
                "Naziv Naloga",
                "Mesto Naloga",
                "Opis1",
                "Opis2",
                "Racun Nalaloga",
                "Broj Naloga",
                "Broj Virmana",
                "Osnov",
                "Iznos Dug",
                "Iznos Pot",
                "Model",
                "Poziv",
                "Model Vezni",
                "Poziv Vezni",
                "Datum Knjizenja",
                "Datum Valute");
        List<List<Object>> headerValues=new ArrayList<>();
        return new ArrayList<Object>(labels);
    }

    private static String calculateStartingIndex(int startingIzvodIndex)
    {
        return "A"+((Integer)startingIzvodIndex).toString();
    }

    private static int updateStartingIndex(int startingCatalogIndex)
    {
        //additional rows for catalog name, one for labels, and one for blank row
        if(startingCatalogIndex==1)
            return 3;
        return startingCatalogIndex+1;
    }

    /*private static File handleCorrectXMLPath(File xmlPath, Scanner scanner)
    {
        while(!xmlPath.isDirectory())
        {
            System.out.print("Wrong path to directory. Enter again");
            String path=scanner.next();
            xmlPath=new File(path);
        }
        return xmlPath;
    } */
    public static void enterXMLDirectory(File xmlPath, Scanner scanner) {
        while (!xmlPath.isDirectory()) {
            System.out.print("Wrong path to directory. Enter again: ");
            String path = scanner.next();
            xmlPath = new File(path);
        }
    }


    /*public static void update(String directoryPath) throws IOException, GeneralSecurityException
    {
        File xmlPath=new File(directoryPath);
        Scanner scanner=new Scanner(System.in);
        if (!xmlPath.exists()|| !xmlPath.isDirectory())
        {
            enterXMLDirectory(xmlPath,scanner);
        }
        SpreadSheetWriter.setup(); //Establishes new connection and authorization
        System.out.print("Default title is XMLResults. Change file title [Y/n]: ");
        String changeTitleAnswer=scanner.next();
        while(changeTitleAnswer.compareTo("Y")!=0 && changeTitleAnswer.compareTo("n")!=0)
        {
            System.out.print("Wrong response. Enter new response: ");
            changeTitleAnswer=scanner.next();
        }
        if(changeTitleAnswer.compareTo("Y")==0)
        {
            System.out.print("Enter new title: ");
            Scanner scannerTitle=new Scanner(System.in);
            String title=scannerTitle.nextLine();
            SpreadSheetWriter.spreadSheetTitle=title.trim();
        }
        SpreadSheetWriter.createSpreadsheet();
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller(xmlPath);
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
            List<List<Object> >bodyValues=createBodyValues(izvod, startingIzvodIndex);
            valueRange.setValues(bodyValues);
            body.add(valueRange);
            startingIzvodIndex=updateStartingIndex(startingIzvodIndex);
        }
        //after everything is read in, we update the whole content all at once
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(body);
        BatchUpdateValuesResponse batchResult = SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
    } */



    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        File xmlPath;
        String path;
        Scanner scanner=new Scanner(System.in);
        if (args.length==0 || ! new File(args[0]).isDirectory())
        {
            System.out.print("Enter again: ");
            path=scanner.next();
            xmlPath=new File(path);
            enterXMLDirectory(xmlPath,scanner);
        }
        else
        {
            xmlPath=new File(args[0]);
            enterXMLDirectory(xmlPath,scanner);
        }
        SpreadSheetWriter.setup(); //Establishes new connection and authorization
        System.out.print("Default title is XMLResults. Change file title [Y/n]: ");
        String changeTitleAnswer=scanner.next();
        while(changeTitleAnswer.compareTo("Y")!=0 && changeTitleAnswer.compareTo("n")!=0)
        {
            System.out.print("Wrong response. Enter new response: ");
            changeTitleAnswer=scanner.next();
        }
        if(changeTitleAnswer.compareTo("Y")==0)
        {
            System.out.print("Enter new title: ");
            Scanner scannerTitle=new Scanner(System.in);
            String title=scannerTitle.nextLine();
            SpreadSheetWriter.spreadSheetTitle=title.trim();
        }
        SpreadSheetWriter.createSpreadsheet();
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller(xmlPath);
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
            List<List<Object> >bodyValues=createBodyValues(izvod, startingIzvodIndex);
            valueRange.setValues(bodyValues);
            body.add(valueRange);
            startingIzvodIndex=updateStartingIndex(startingIzvodIndex);
        }
        //after everything is read in, we update the whole content all at once
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(body);
        BatchUpdateValuesResponse batchResult = SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
    }
}