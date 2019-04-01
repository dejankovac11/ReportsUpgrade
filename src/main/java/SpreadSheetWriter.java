import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.test.xsd.Izvod;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SpreadSheetWriter {
    static Sheets sheetsService;
    static String SPREADSHEET_ID;
    static String spreadSheetTitle;

    public static void setup() throws GeneralSecurityException, IOException {
        sheetsService = SheetsServiceUtil.getSheetsService();
    }

    public static void createSpreadsheet () throws IOException {
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(spreadSheetTitle));
        List<NamedRange> nameRanges=new ArrayList<>();
        Field[] fields=Izvod.class.getFields();
        int currentColumn=1;
        for (Field f:fields)
        {
            if(f.getName().compareTo("Prom")==0)
                continue;
            NamedRange range=new NamedRange();
            range.setName(f.getName());
            range.setRange(new GridRange().setStartColumnIndex(currentColumn++).setEndColumnIndex(Integer.MAX_VALUE));
            nameRanges.add(range);
        }
        spreadsheet.setNamedRanges(nameRanges);
        Spreadsheet result = sheetsService.spreadsheets().create(spreadsheet).execute();
        SPREADSHEET_ID=result.getSpreadsheetId();
    }
}
