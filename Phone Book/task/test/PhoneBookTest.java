import org.hyperskill.hstest.v6.stage.BaseStageTest;
import org.hyperskill.hstest.v6.testcase.CheckResult;
import org.hyperskill.hstest.v6.testcase.TestCase;
import phonebook.MainKt;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneBookTest extends BaseStageTest {
    public PhoneBookTest() {
        super(MainKt.class);
    }
    
    
    private long timeOnTestStart;
    private String outputFirstTest = "";
    
    @Override
    public List<TestCase> generate() {
        timeOnTestStart = System.currentTimeMillis();
        return Arrays.asList(
                new TestCase<>().setTimeLimit(30 * 60 * 1000),
                new TestCase<>().setTimeLimit(30 * 60 * 1000).setCheckFunc((reply, attach) -> {
                            if (reply.equals(outputFirstTest)) {
                                return CheckResult.FALSE("Your program output is exactly the same during different runs. " +
                                        "Does your program just output a string?");
                            }
                            return CheckResult.TRUE;
                        }
                )
        );
    }
    
    private CheckResult checkPhrases(String reply, String... phrases) {
        reply = reply.toLowerCase();
        for (String phrase : phrases) {
            if (!reply.contains(phrase.toLowerCase())) {
                return CheckResult.FALSE("Not found the part `" + phrase + "` in your output.");
            }
        }
        return CheckResult.TRUE;
    }
    
    private Pattern timePattern = Pattern.compile(".*(\\d+)\\s*min.*?(\\d+)\\s*sec.*?(\\d+)\\s*ms.*", Pattern.DOTALL);
    
    // returns -1 if not matches.
    private long getUserEstimatedTime(String reply) {
        Matcher matcher = timePattern.matcher(reply);
        if (!matcher.matches()) {
            return -1;
        }
        int min = Integer.parseInt(matcher.group(1));
        int sec = Integer.parseInt(matcher.group(2));
        int ms = Integer.parseInt(matcher.group(3));
        
        return ms + sec * 1000 + min * 1000 * 60;
    }
    
    @Override
    public CheckResult check(String reply, Object clue) {
        long realTime = System.currentTimeMillis() - timeOnTestStart;
        timeOnTestStart = System.currentTimeMillis();
        outputFirstTest = reply;
//        System.out.println("Time delta: " + realTime);
        
        if (!reply.contains("500 / 500") && !reply.contains("500/500")) {
            return CheckResult.FALSE("Your output should contain `500 / 500` fragment.");
        }
        
        CheckResult res = checkPhrases(reply, "start searching", "found",
                "min.", "sec.", "ms.");
        if (!res.isCorrect()) {
            return res;
        }
        long estimatedTime = getUserEstimatedTime(reply);
        if (estimatedTime == -1) {
            return CheckResult.FALSE("Your output format doesn't contain numbers before min, sec, ms words.");
        }
        
        if (realTime < 1000) {
            return CheckResult.FALSE("Your program completes too fast. Faster than a second!");
        }
        
        double ratio = estimatedTime / (realTime + 0.0);
        if (ratio < 0.5 || ratio > 1.5) {
            return CheckResult.FALSE("Too large difference between the real working time and your output. " +
                    "Real program working time was " + realTime + " ms, and your output contained " + estimatedTime + "ms in total.");
        }
        
        return CheckResult.TRUE;
    }
}
