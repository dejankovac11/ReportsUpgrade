import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.google.common.collect.Lists;
import com.test.xsd.Izvod;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void sortSpreadSheet(String spreadSheetID) throws IOException
    {
        BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
        SortSpec ss = new SortSpec();
// ordering ASCENDING or DESCENDING
        ss.setSortOrder("DESCENDING");
// the column number starting from 0
        ss.setDimensionIndex(2);
        SortRangeRequest srr = new SortRangeRequest();
        srr.setSortSpecs(Arrays.asList(ss));
        srr.setRange(new GridRange().setStartColumnIndex(0).setEndColumnIndex(26).setStartRowIndex(1).setEndColumnIndex(Integer.MAX_VALUE));
        Request req = new Request();
        req.setSortRange(srr);
        busReq.setRequests(Arrays.asList(req));
// mService is a instance of com.google.api.services.sheets.v4.Sheets
        SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(spreadSheetID, busReq).execute();
    }

    public static void enterXMLDirectory(File xmlPath, Scanner scanner) {
        while (!xmlPath.isDirectory()) {
            System.out.print("Wrong path to directory. Enter again: ");
            String path = scanner.next();
            xmlPath = new File(path);
        }
    }

    public static void create(String url, String directoryPath) throws IOException, GeneralSecurityException
    {
        String spreadSheetID=extractSpreadsheetID(url);
        Scanner scanner=new Scanner(System.in);
        File xmlPath=new File(directoryPath);
        if (!xmlPath.exists() || !xmlPath.isDirectory())
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
        //SpreadSheetWriter.sheetsService.spreadsheets().get(spreadSheetID).values();
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller(xmlPath);
        List<Izvod> izvodi=izvodUnmarshaller.readXML(); //kreiranje i ucitavanje XML koda u listu Izvod objekata
        List<ValueRange> body=new ArrayList<ValueRange>(); //Content of the new spreadsheet
        int startingIzvodIndex=1; //where to begin writing new ValueRange object
        for (Izvod izvod:izvodi)
        {
            ValueRange valueRange=new ValueRange(); //valueRange for the current catalog
            String startingIzvodIndexString=calculateStartingIndex(startingIzvodIndex);//each new catalog begins at A1,A3,A5...
            valueRange.setRange(startingIzvodIndexString); //Initialize starting field for writing the contents of current catalogue

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
        sortSpreadSheet(SpreadSheetWriter.SPREADSHEET_ID);
    }

    //https://docs.google.com/spreadsheets/d/1sILuxZUnyl_7-MlNThjt765oWshN3Xs-PPLfqYe4DhI/edit#gid=0 --> 1sILuxZUnyl_7-MlNThjt765oWshN3Xs-PPLfqYe4DhI
    public static String extractSpreadsheetID(String urlPath)
    {
        Pattern spreadSheetIdPattern=Pattern.compile(".+?/d/(.+?)/.*");
        Matcher matcher=spreadSheetIdPattern.matcher( urlPath);
        if(matcher.matches())
            return matcher.group(1);
        return "";
    }

    public static void updateSpreadSheet(String url, String directoryPath) throws GeneralSecurityException, IOException
    {
        File xmlPath=new File(directoryPath);
        if (!xmlPath.exists() || !xmlPath.isDirectory())
        {
            Scanner scanner=new Scanner(System.in);
            enterXMLDirectory(xmlPath,scanner);
        }
        SpreadSheetWriter.setup();
        String spreadSheetID=extractSpreadsheetID(url);
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller(xmlPath);
        List<Izvod> izvodi=izvodUnmarshaller.readXML();
        //List<ValueRange> bodyAppend=new ArrayList<ValueRange>(); //Content of the new spreadsheet
        int startingIzvodIndex=2; //where to begin writing new ValueRange object
        for (Izvod izvod:izvodi)
        {
            ValueRange valueRange=new ValueRange(); //valueRange for the current catalog
            String startingIzvodIndexString=calculateStartingIndex(startingIzvodIndex);//each new catalog begins at A1,A3,A5...
            valueRange.setRange(startingIzvodIndexString); //Initialize starting field for writing the contents of current catalogue
            List<List<Object> >bodyValues=createBodyValues(izvod, startingIzvodIndex);
            valueRange.setValues(bodyValues);
            //bodyAppend.add(valueRange);
            startingIzvodIndex=updateStartingIndex(startingIzvodIndex);
            AppendValuesResponse appendValuesResponse=SpreadSheetWriter.sheetsService.spreadsheets().values().append(spreadSheetID,startingIzvodIndexString,valueRange).setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS").setValueInputOption("RAW").execute();
        }
        sortSpreadSheet(spreadSheetID);
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException
    {
        if(args.length<2)
        {
            System.out.println("Incorrect arguments.");
            System.out.println("Program runs with create path or update url path.");
            System.exit(1);
        }
        if(args[0].compareTo("-create")!=0 && args[0].compareTo("-update")!=0)
        {
            System.out.println("Incorrect options.");
            System.exit(1);


        }
        if(args[0].compareTo("-create")==0)
        {
            create(args[1],args[2]);
        }
        else if(args[0].compareTo("-update")==0 && args.length==3)
        {
            updateSpreadSheet(args[1],args[2]);
        }
        else
        {
            System.out.println("Insufficient number of arguments.");
            System.exit(1);
        }
    }
}