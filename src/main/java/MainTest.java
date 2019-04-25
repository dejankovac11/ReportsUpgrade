import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainTest {
    public static void main(String[] args) {
      String name="Stan 11 - Trivic Danijela";
        Pattern extractStanPattern=Pattern.compile("Stan\\s+(\\d\\d|\\d\\/\\d).+");
        Matcher matcher=extractStanPattern.matcher(name);
        System.out.println(matcher.matches());
        if(matcher.matches())
            System.out.println(matcher.group(1));

    }
}
