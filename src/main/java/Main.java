import com.google.api.services.sheets.v4.model.*;
import com.test.xsd.Izvod;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static boolean isGetter(Method method) {
        if (!method.getName().startsWith("get")) return false;
        if (method.getName().compareTo("getClass") == 0) return false;
        if (method.getName().compareTo("getProm") == 0) return false;
        return true;
    }

    private static List<Method> getIzvodGetters() {
        List<Method> getterMethods = new ArrayList<>();
        Method[] methods = Izvod.class.getMethods();
        for (Method m : methods) {
            if (isGetter(m))
                getterMethods.add(m);
        }
        return getterMethods;
    }

    private static List<Method> getPromGetters() {
        List<Method> getterMethods = new ArrayList<>();
        Method[] methods = Izvod.Prom.class.getMethods();
        for (Method m : methods) {
            if (isGetter(m))
                getterMethods.add(m);
        }
        return getterMethods;
    }

    private static List<List<Object>> createBodyValues(Izvod izvod, int startingIzvodIndex) throws InvocationTargetException, IllegalAccessException {
        List<Method> promMethods = getPromGetters();
        List<Method> izvodMethods = getIzvodGetters();
        List<List<Object>> bodyValues = new ArrayList<List<Object>>();
        List<String> izvodLabels = createIzvodLabels();
        List<String> promLabels = createPromLabels();
        if (startingIzvodIndex == 1)
            bodyValues.add(createLabels());
        Izvod.Prom prom = izvod.getProm();
        List<String> bodyValuesArgs = new ArrayList<String>();
        for (String label : izvodLabels) {
            for (Method method : izvodMethods) {
                String regex = "get" + label.toLowerCase();
                if (method.getName().toLowerCase().matches(regex)) {
                    String bodyArg = method.invoke(izvod, null) == null ? "" : method.invoke(izvod, null).toString();
                    bodyValuesArgs.add(bodyArg);
                    break;
                }
            }
        }
        for (String label : promLabels) {
            for (Method method : promMethods) {
                String regex = "get" + label.toLowerCase();
                if (method.getName().toLowerCase().matches(regex)) {
                    String bodyArg = method.invoke(prom, null) == null ? "" : method.invoke(prom, null).toString();
                    bodyValuesArgs.add(bodyArg);
                    break;
                }
            }

        }
        bodyValues.add(new ArrayList<Object>(bodyValuesArgs));
        return bodyValues;
    }


    private static List<Object> createLabels() {
        List<String> fieldNames = new ArrayList<>();
        Field[] izvodFields = Izvod.class.getDeclaredFields();
        for (Field f : izvodFields) {
            fieldNames.add(f.getName());
        }
        fieldNames.remove(fieldNames.indexOf("prom"));
        Field[] promFields = Izvod.Prom.class.getDeclaredFields();
        for (Field f : promFields) {
            fieldNames.add(f.getName());
        }
        return new ArrayList<Object>(fieldNames);
    }

    private static List<String> createIzvodLabels() {
        List<String> fieldNames = new ArrayList<>();
        Field[] izvodFields = Izvod.class.getDeclaredFields();
        for (Field f : izvodFields) {
            fieldNames.add(f.getName());
        }
        fieldNames.remove(fieldNames.indexOf("prom"));
        return fieldNames;
    }

    private static List<String> createPromLabels() {
        List<String> fieldNames = new ArrayList<>();
        Field[] promFields = Izvod.Prom.class.getDeclaredFields();
        for (Field f : promFields) {
            fieldNames.add(f.getName());
        }
        return fieldNames;
    }

    private static String calculateStartingIndex(int startingIzvodIndex) {
        return "A" + ((Integer) startingIzvodIndex).toString();
    }

    private static int updateStartingIndex(int startingCatalogIndex) {
        //additional rows for catalog name, one for labels, and one for blank row
        if (startingCatalogIndex == 1)
            return 3;
        return startingCatalogIndex + 1;
    }

    private static void sortSpreadSheet(String spreadSheetID, Integer SheetID) throws IOException {
        BatchUpdateSpreadsheetRequest busReq = new BatchUpdateSpreadsheetRequest();
        SortSpec sortSpec = new SortSpec();
        sortSpec.setSortOrder("DESCENDING");
        sortSpec.setDimensionIndex(2);
        SortRangeRequest sortRangeRequest = new SortRangeRequest();
        sortRangeRequest.setSortSpecs(Arrays.asList(sortSpec));
        sortRangeRequest.setRange(new GridRange().setStartColumnIndex(0).setEndColumnIndex(25).setStartRowIndex(1).setSheetId(SheetID));
        Request req = new Request();
        req.setSortRange(sortRangeRequest);
        busReq.setRequests(Arrays.asList(req));
        SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(spreadSheetID, busReq).execute();
    }

    private static void formatHeader(String spreadSheetID, Integer SheetID) throws IOException
    {
        List<Request> requests=new ArrayList<>();
        BatchUpdateSpreadsheetRequest batchRequest=new BatchUpdateSpreadsheetRequest();
        RepeatCellRequest cellRequest=new RepeatCellRequest().setFields("userEnteredFormat(backgroundColor,textFormat)").setRange(new GridRange().setSheetId(SheetID).setStartRowIndex(0)
                .setEndRowIndex(1).setStartColumnIndex(0)).setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setItalic(true).setBold(true)
                .setForegroundColor(new Color().setAlpha(new Float(0.5))))));
        UpdateBordersRequest bordersRequest=new UpdateBordersRequest().setRange(new GridRange().setSheetId(SheetID).setStartRowIndex(0)
                .setEndRowIndex(1).setStartColumnIndex(0)).setLeft(new Border().setStyle("SOLID")).setRight(new Border().setStyle("SOLID"))
                .setTop(new Border().setStyle("SOLID")).setBottom(new Border().setStyle("SOLID"));
        Request request1=new Request().setRepeatCell(cellRequest);
        requests.add(request1);
        Request request2=new Request().setUpdateBorders(bordersRequest);
        requests.add(request2);
        batchRequest.setRequests(requests);
        SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(spreadSheetID,batchRequest).execute();
    }

    public static void enterXMLDirectory(File xmlPath, Scanner scanner)
    {
        while (!xmlPath.isDirectory())
        {
            System.out.print("Wrong path to directory. Enter again: ");
            String path = scanner.next();
            xmlPath = new File(path);
        }
    }

    public static void createIzvodi(String url, String directoryPath) throws IOException, GeneralSecurityException, InvocationTargetException, IllegalAccessException {
        String spreadSheetID=extractSpreadsheetID(url);
        SpreadSheetWriter.SPREADSHEET_ID=spreadSheetID;
        Scanner scanner=new Scanner(System.in);
        File xmlPath=new File(directoryPath);
        if (!xmlPath.exists() || !xmlPath.isDirectory())
        {
            enterXMLDirectory(xmlPath,scanner);
        }
        SpreadSheetWriter.setup(); //Establishes new connection and authorization
        String title="NoviIzvodi";
        System.out.print("Default sheet title is noviIzvodi. Change file title [Y/n]: ");
        String changeTitleAnswer=scanner.next();
        while(changeTitleAnswer.compareTo("Y")!=0 && changeTitleAnswer.compareTo("n")!=0)
        {
            System.out.print("Wrong response. Enter new response: ");
            changeTitleAnswer=scanner.next();
        }
        if(changeTitleAnswer.compareTo("Y")==0)
        {
            System.out.print("Enter new title: ");
            Scanner newTitle=new Scanner(System.in);
            title=newTitle.nextLine();
        }
        Spreadsheet currentSpreadSheet=SpreadSheetWriter.sheetsService.spreadsheets().get(spreadSheetID).execute();
        for (Sheet sheet:currentSpreadSheet.getSheets())
        {
            if(title.compareTo(sheet.getProperties().getTitle())==0)
            {
                Scanner resetTitle=new Scanner(System.in);
                while(title.compareTo(sheet.getProperties().getTitle())==0)
                {
                    System.out.print("Sheet "+title+" already exists. Enter new title: ");
                    title=resetTitle.nextLine();
                }
            }
        }
        System.out.println("New title is: "+title);
        Request AddSheetRequest=new Request().setAddSheet(new AddSheetRequest().setProperties(new SheetProperties().setTitle(title)));
        BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest=new BatchUpdateSpreadsheetRequest().setRequests(Arrays.asList(AddSheetRequest));
        BatchUpdateSpreadsheetResponse response=SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchUpdateSpreadsheetRequest).execute();
        Integer SheetID=response.getReplies().get(0).getAddSheet().getProperties().getSheetId();
        String sheetName=response.getReplies().get(0).getAddSheet().getProperties().getTitle();
        IzvodUnmarshaller izvodUnmarshaller=new IzvodUnmarshaller(xmlPath);
        List<Izvod> izvodi=izvodUnmarshaller.readXML(); //kreiranje i ucitavanje XML koda u listu Izvod objekata
        List<ValueRange> body=new ArrayList<ValueRange>(); //Content of the new spreadsheet
        int startingIzvodIndex=1; //where to begin writing new ValueRange object
        for (Izvod izvod:izvodi)
        {
            ValueRange valueRange=new ValueRange(); //valueRange for the current Izvod
            String startingIzvodIndexString=calculateStartingIndex(startingIzvodIndex);
            valueRange.setRange(title+"!"+startingIzvodIndexString);//Initialize starting field for writing the contents of current Izvod
            List<List<Object> >bodyValues=createBodyValues(izvod, startingIzvodIndex);
            valueRange.setValues(bodyValues);
            body.add(valueRange);
            startingIzvodIndex=updateStartingIndex(startingIzvodIndex);
        }
        //after everything is read in, we update the whole content all at once
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(body);
        SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
        sortSpreadSheet(SpreadSheetWriter.SPREADSHEET_ID,SheetID);
        formatHeader(SpreadSheetWriter.SPREADSHEET_ID,SheetID);
        ValueRange pozivi = SpreadSheetWriter.sheetsService.spreadsheets().values().get(spreadSheetID, sheetName + "!V2:W").execute();
        formatSpreadSheet(SpreadSheetWriter.SPREADSHEET_ID,SheetID,pozivi);
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

    //https://docs.google.com/spreadsheets/d/1YsA8jDCvN2fEnT38JC1ca_UeSUtjK0tn5M2PUg-QE7c/edit#gid=374258422 --> 374258422
    public static String extractSheetID(String urlPath)
    {
        Pattern sheetIdPattern=Pattern.compile(".+?#gid=(.+?)");
        Matcher matcher=sheetIdPattern.matcher(urlPath);
        if(matcher.matches())
            return matcher.group(1);
        return "";
    }

    public static String extractStan(String entry)
    {
        Pattern extractStanPattern=Pattern.compile("(\\n)?Stan\\s+(\\d\\d|\\d\\/\\d).+");
        Matcher matcher=extractStanPattern.matcher(entry);
        if(matcher.matches())
            return matcher.group(2);
        return "";
    }

    public static String columnPosition(Integer position)
    {
        return Character.toString((char)(position+65));
    }

    public static void resetSpreadSheetFormat(String spreadSheetID,Integer sheetID, ValueRange pozivi) throws IOException
    {
        int numRows=pozivi.getValues().size();
        List<Request> requests = new ArrayList<>();
        for(int i=0; i<numRows;i++)
        {
            RepeatCellRequest cellRequest = new RepeatCellRequest().setFields("userEnteredFormat(backgroundColor,textFormat)").setRange(new GridRange().setSheetId(sheetID).setStartRowIndex(i + 1)
                    .setEndRowIndex(i + 2).setStartColumnIndex(0).setEndColumnIndex(26)).setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setBold(false)
                    .setForegroundColor(new Color().setRed((float) 0)))));
            Request request = new Request().setRepeatCell(cellRequest);
            requests.add(request);

        }
        if (requests.size() != 0) {
            BatchUpdateSpreadsheetRequest updateColored=new BatchUpdateSpreadsheetRequest().setRequests(requests);
            SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(spreadSheetID,updateColored).execute();
        }
    }
    public static void formatSpreadSheet(String spreadSheetID,Integer sheetID, ValueRange pozivi) throws IOException
    {
        List<Request> requests = new ArrayList<>();
        int numRows=pozivi.getValues().size();
        for(int i=0; i<numRows;i++)
        {
            String poziv=pozivi.getValues().get(i).size()==0? "":(String) pozivi.getValues().get(i).get(0);
            if (!poziv.matches("\\d\\d-\\d\\d-\\d\\d\\d\\d"))
            {
                RepeatCellRequest cellRequest = new RepeatCellRequest().setFields("userEnteredFormat(backgroundColor,textFormat)").setRange(new GridRange().setSheetId(sheetID).setStartRowIndex(i + 1)
                        .setEndRowIndex(i + 2).setStartColumnIndex(0).setEndColumnIndex(26)).setCell(new CellData().setUserEnteredFormat(new CellFormat().setTextFormat(new TextFormat().setBold(true)
                        .setForegroundColor(new Color().setRed((float) 0.7)))));
                Request request = new Request().setRepeatCell(cellRequest);
                requests.add(request);
            }
        }
        if (requests.size() != 0) {
            BatchUpdateSpreadsheetRequest updateColored=new BatchUpdateSpreadsheetRequest().setRequests(requests);
            SpreadSheetWriter.sheetsService.spreadsheets().batchUpdate(spreadSheetID,updateColored).execute();
        }

    }

    public static void updateIzvodi(String url, String directoryPath) throws GeneralSecurityException, IOException, InvocationTargetException, IllegalAccessException {
        File xmlPath = new File(directoryPath);
        if (!xmlPath.exists() || !xmlPath.isDirectory()) {
            System.out.print("Wrong path to XML directory. Enter again: ");
            Scanner scanner = new Scanner(System.in);
            enterXMLDirectory(xmlPath, scanner);
        }
        SpreadSheetWriter.setup();
        String spreadSheetID = extractSpreadsheetID(url);
        Integer sheetID = new Integer(extractSheetID(url));
        IzvodUnmarshaller izvodUnmarshaller = new IzvodUnmarshaller(xmlPath);
        List<Izvod> izvodi = izvodUnmarshaller.readXML();
        Spreadsheet currentSpreadSheet = SpreadSheetWriter.sheetsService.spreadsheets().get(spreadSheetID).execute();
        String sheetName = "";
        for (Sheet sheet : currentSpreadSheet.getSheets()) {
            if (sheetID.compareTo(sheet.getProperties().getSheetId()) == 0) {
                sheetName = sheet.getProperties().getTitle();
                break;
            }
        }
        ValueRange body = SpreadSheetWriter.sheetsService.spreadsheets().values().get(spreadSheetID, sheetName).execute();
        int numRows = body.getValues() != null ? body.getValues().size() : 0;
        if (numRows == 0) {
            List<List<Object>> labelValues = new ArrayList<>();
            labelValues.add(createLabels());
            ValueRange labels = new ValueRange().setValues(labelValues);
            UpdateValuesResponse result =
                    SpreadSheetWriter.sheetsService.spreadsheets().values().update(spreadSheetID, sheetName + "!A1", labels)
                            .setValueInputOption("RAW")
                            .execute();
        }
        int startingIzvodIndex = 2;
        for (Izvod izvod : izvodi) {
            ValueRange valueRange = new ValueRange(); //valueRange for the current catalog
            List<List<Object>> bodyValues = createBodyValues(izvod, startingIzvodIndex);
            valueRange.setValues(bodyValues);
            SpreadSheetWriter.sheetsService.spreadsheets().values().append(spreadSheetID, sheetName, valueRange).setValueInputOption("USER_ENTERED")
                    .setInsertDataOption("INSERT_ROWS").setValueInputOption("RAW").execute(); //potencijalno ce upisati na nulti deo
        }
        ValueRange pozivi = SpreadSheetWriter.sheetsService.spreadsheets().values().get(spreadSheetID, sheetName + "!V2:W").execute();
        resetSpreadSheetFormat(spreadSheetID,sheetID,pozivi);
        sortSpreadSheet(spreadSheetID, sheetID);
        pozivi=SpreadSheetWriter.sheetsService.spreadsheets().values().get(spreadSheetID, sheetName + "!V2:W").execute();
        formatSpreadSheet(spreadSheetID,sheetID,pozivi);
    }


    public static void updateStanovi(String url,String directoryPath) throws IOException, GeneralSecurityException {

        String spreadSheetID = extractSpreadsheetID(url);
        SpreadSheetWriter.SPREADSHEET_ID = spreadSheetID;
        System.out.println("Enter sample sheet to be updated: ");
        Scanner scanner = new Scanner(System.in);
        String sampleSheetName = scanner.next();
        String range = sampleSheetName + "!A1:A100";
        SpreadSheetWriter.setup();
        ValueRange result = SpreadSheetWriter.sheetsService.spreadsheets().values().get(SpreadSheetWriter.SPREADSHEET_ID, range).execute();
        int numRows = result.getValues() != null ? result.getValues().size() : 0;
        List<List<Object>> values = result.getValues();
        List<String> pozicija = new ArrayList<>();
        List<String> brojStana = new ArrayList<>();
        for (int i = 0; i < numRows; i++)
        {
            if (values.get(i).size() != 0)
            {
                pozicija.add(new Integer(i + 1).toString());
                String name = (String) values.get(i).get(0);
                brojStana.add(extractStan(name));
            }
        }
        File xmlPath = new File(directoryPath);
        if (!xmlPath.exists() || !xmlPath.isDirectory())
        {
            System.out.print("Wrong path to XML directory. Enter again. ");
            enterXMLDirectory(xmlPath, scanner);
        }
        IzvodUnmarshaller izvodUnmarshaller = new IzvodUnmarshaller(xmlPath);
        SpreadSheetWriter.setup();
        List<Izvod> izvodi = izvodUnmarshaller.readXML();
        List<ValueRange> listaUpdate = new ArrayList<>();
        for (Izvod izvod : izvodi)
        {
            String poziv=izvod.getProm().getPoziv();
            if(poziv.compareTo("")==0)
                continue;
            if (!poziv.matches("\\d\\d-\\d\\d-\\d\\d\\d\\d"))
                continue;
            String currentStan = poziv.substring(0, 2);
            Integer mesec = new Integer(poziv.substring(3, 5));
            String godina = poziv.substring(6);
            ValueRange valueRange = new ValueRange();
            List<String> potrazniPrometArgs = Arrays.asList(new Float(izvod.getPotrazniPromet()).toString());
            List<List<Object>> potrazniPromet = new ArrayList<>();
            potrazniPromet.add( new ArrayList<Object> (potrazniPrometArgs));
            valueRange.setValues(potrazniPromet);
            valueRange.setRange("Stanovi-" + godina + "!"  + columnPosition(mesec) + pozicija.get(brojStana.indexOf(currentStan)));
            listaUpdate.add(valueRange);
        }
        BatchUpdateValuesRequest batchBody=new BatchUpdateValuesRequest().setValueInputOption("RAW").setData(listaUpdate);
        SpreadSheetWriter.sheetsService.spreadsheets().values().batchUpdate(SpreadSheetWriter.SPREADSHEET_ID,batchBody).execute();
    }
    public static void main(String[] args) throws IOException, GeneralSecurityException, InvocationTargetException, IllegalAccessException
    {
        if(args.length<2)
        {
            System.out.println("Incorrect number of arguments.");
            System.out.println("Program runs with -create url dirpath or -updateI url dirpath or -updateS url dirpath.");
            System.exit(1);
        }
        if(args[0].compareTo("-create")!=0 && args[0].compareTo("-updateI")!=0 && args[0].compareTo("-updateS")!=0)
        {
            System.out.println("Incorrect option chosen.");
            System.exit(1);
        }
        if(args[0].compareTo("-create")==0)
        {
            createIzvodi(args[1],args[2]);
            System.out.println("Create finished.");
        }
        else if(args[0].compareTo("-updateI")==0 && args.length==3)
        {
            updateIzvodi(args[1],args[2]);
            System.out.println("Update Izvodi finished.");
        }
        else if(args[0].compareTo("-updateS")==0 && args.length==3)
        {
            updateStanovi(args[1],args[2]);
            System.out.println("Update Stanovi finished");
        }
        else
        {
            System.out.println("Error. Exiting now.");
            System.exit(1);
        }
    }
}