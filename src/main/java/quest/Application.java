package quest;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class Application {

//  public static void main(String[] args) {
//    SpringApplication.run(Application.class, args);
//  }


    static String init() throws IOException, URISyntaxException {
        Path path = Paths.get(Application.class.getClassLoader().getResource("text.txt").toURI());
        Stream<String> lines = Files.lines(path);
        String data = lines.collect(Collectors.joining("\n"));
        lines.close();
        return data;
    }

    private final static MyStem mystemAnalyzer = new Factory("-igd --eng-gr --format json --weight")
            .newMyStem("3.0", Option.empty()).get();

    public static void main(String[] args) throws IOException, URISyntaxException, MyStemApplicationException {


        final Iterable<Info> result =
                JavaConversions.asJavaIterable(
                        mystemAnalyzer
                                .analyze(Request.apply("Мэнникс просит у Уоррена письмо Линкольна и зачитывает его вслух."))
                                .info()
                                .toIterable());

        for (final Info info : result) {
            System.out.println(info.initial() + " -> " + info.lex() + " | " + info.rawResponse());
        }


//        String text = init();
//        System.out.println(text);
//
//        createQuestionWithNumber(text);

    }

    private static void createQuestionWithNumber(String text) {
        System.out.println();
        System.out.println();
        for (String sentense : text.split("\\.")) {
//            System.out.println(sentense);
//            if (sentense.contains(" в ")) {
            for (String words : sentense.split("\\s")) {
                if (isVerb(words)) {
                    System.out.print("Кто ");
                    System.out.print(words);
//                    System.out.print(" ");
                    String[] parts = sentense.split(words);
                    if (parts.length > 1) {
                        System.out.println(parts[1]);
                    }

                }
            }
//            }
        }
    }

    private static List<String> ends = Arrays.asList("ешь", "ет", "ем", "ете", "ут", "ют", "ишь", "ит", " ет", "им", "ите", "ат", "ят");

    private static boolean isVerb(String words) {
        if (words.length() < 5) {
            return false;

        }
        boolean isverb = false;
        for (String end : ends) {
            isverb = words.endsWith(end);
            if (isverb) {
                return true;
            }

        }
        return isverb;
    }
}
