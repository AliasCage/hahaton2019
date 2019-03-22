package quest;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static void main(String[] args) throws IOException, URISyntaxException {

        String text = init();
        System.out.println(text);

        createQuestionWithNumber(text);

    }

    private static void createQuestionWithNumber(String text) {
        //todo:
    }
}
