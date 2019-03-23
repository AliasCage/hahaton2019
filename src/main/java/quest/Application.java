package quest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

//  public static void main(String[] args) {
//    SpringApplication.run(Application.class, args);
//  }

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final static MyStem mystemAnalyzer = new Factory("-igd --format json --weight")
            .newMyStem("3.0", Option.empty()).get();


    static String init() throws IOException, URISyntaxException {
        Path path = Paths.get(Application.class.getClassLoader().getResource("text.txt").toURI());
        Stream<String> lines = Files.lines(path);
        String data = lines.collect(Collectors.joining("\n"));
        lines.close();
        return data;
    }

    public static void main(String[] args) throws IOException, URISyntaxException, MyStemApplicationException {

        String text = init();
        System.out.println(text);

        createQuestionWithNumber(text);
        final Iterable<Info> result = getStringInfo("К около первоначальным членам ООН относятся 50 государств, подписавших Устав ООН на конференции в Сан-Франциско 26 июня 1945 года, а также Польша");

        for (final Info info : result) {
            Literal literal = objectMapper.readValue(info.rawResponse(), Literal.class);
            System.out.println(info.initial() + " -> " + info.lex() + " | " + info.rawResponse());
        }

    }

    private static Iterable<Info> getStringInfo(String data) throws MyStemApplicationException {
        return JavaConversions.asJavaIterable(
                mystemAnalyzer
                        .analyze(Request.apply(data))
                        .info()
                        .toIterable());
    }

    private static void createQuestionWithNumber(String text) throws MyStemApplicationException {
        System.out.println();
        System.out.println();
        for (String sentense : text.split("\\.")) {
            String[] split = sentense.split("\\s");
            for (int i = 0; i < split.length; i++) {
                if (isYear(split[i])) {
                    continue;
                }
                if (isNumeric(split[i])) {
                    if (isMonth(split[i + 1])) {
                        continue;
                    }
                    String answer = split[i];
                    System.out.print("Сколько ");
                    System.out.print(split[i + 1]);
                    if (isVerb(split[i - 1])) {
                        System.out.print(split[i - 1]);
                        i--;
                    }
                    for (int j = 0; j < i; j++) {
                        System.out.print(" ");
                        System.out.print(split[j].toLowerCase());
                    }
                    for (int j = i + 3; j < split.length; j++) {
                        System.out.print(" ");
                        System.out.print(split[j].toLowerCase());
                    }
                    System.out.println();
                    System.out.println(getRandAnswer(answer));
                    break;
                }
            }
        }
    }


    private static List<String> month = Arrays.asList("январ", "февра", "март", "апрел", "май", "мае", "мая", "июн", "июл", "август", "сентябр", "октябр", "ноябр", "декабр");

    private static boolean isMonth(String s) {
        boolean contains = false;
        for (String s1 : month) {
            contains = s.toLowerCase().contains(s1);
            if (contains) {
                return true;
            }
        }
        return contains;
    }

    private static boolean isYear(String s) {
        return isNumeric(s) && s.length() <= 4 && (Integer.parseInt(s) < 2032 && Integer.parseInt(s) > 1009);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isVerb(String str) throws MyStemApplicationException {
        if (str.length() < 4) {
            return false;
        }
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("V")) {
                return true;
            }
        }
        return false;
    }

    public static String getRandAnswer(String answer) {
        StringBuilder answers = new StringBuilder(answer + " | ");
        Random rn = new Random();
        int parseInt = Integer.parseInt(answer);
        for (int i = 0; i < 3; ) {
            Integer rand = rn.nextInt((int) (((parseInt + (parseInt * 0.25)) - (parseInt - (parseInt * 0.25)) + 1) + (parseInt - (parseInt * 0.25))));
            if (!answers.toString().contains(String.valueOf(rand))) {
                answers.append(rand).append(" | ");
                i++;
            }
        }
        return answers.toString();
    }


}
