import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SpreadSheetWriter {
    static Sheets sheetsService;
    static String SPREADSHEET_ID;

    public static void setup() throws GeneralSecurityException, IOException {
        sheetsService = SheetsServiceUtil.getSheetsService();
    }

    public static void createSpreedsheet () throws GeneralSecurityException, IOException {
        Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle("Result of XML parsing"));
        Spreadsheet result = sheetsService.spreadsheets().create(spreadsheet).execute();
        SPREADSHEET_ID=result.getSpreadsheetId();
    }
}
