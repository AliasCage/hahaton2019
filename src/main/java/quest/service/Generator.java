package quest.service;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.springframework.stereotype.Component;
import quest.Response;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
public class Generator {

    public List<Response> generate(String rawText) {
        try {
            return createQuestionWithNumber(rawText);
        } catch (MyStemApplicationException e) {
            throw new InternalException("Something bad =(");
        }
    }


    private List<Response> createQuestionWithNumber(String text) throws MyStemApplicationException {
        List<Response> responses = new ArrayList<>();
        for (String sentense : text.split("\\.")) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (isYear(split[i])) {
                    continue;
                }
                if (isNumeric(split[i])) {
                    if (length == i + 1) {
                        continue;
                    }
                    if (isMonth(split[i + 1])) {
                        continue;
                    }
                    String answer = split[i];
                    String question = "Сколько ";
                    question = question + split[i + 1];
                    if (isVerb(split[i - 1])) {
                        question = question + split[i - 1];
                        i--;
                    }
                    for (int j = 0; j < i; j++) {
                        question = question + " ";
                        question = question + split[j].toLowerCase();
                    }
                    for (int j = i + 3; j < length; j++) {
                        question = question + " ";
                        question = question + split[j].toLowerCase();
                    }
                    responses.add(new Response(question, getRandAnswer(answer), answer));
                    break;
                }
            }
        }
        return responses;
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

    private final static MyStem mystemAnalyzer = new Factory("-igd --format json --weight")
            .newMyStem("3.0", Option.empty()).get();

    private static Iterable<Info> getStringInfo(String data) throws MyStemApplicationException {
        return JavaConversions.asJavaIterable(
                mystemAnalyzer
                        .analyze(Request.apply(data))
                        .info()
                        .toIterable());
    }

    public static List<String> getRandAnswer(String answer) {
        List<String> answers = new ArrayList<>(4);
        answers.add(answer);

        Random rn = new Random();
        int parseInt = Integer.parseInt(answer);
        for (int i = 0; i < 3; ) {
            Integer rand = rn.nextInt((int) (((parseInt + (parseInt * 0.25)) - (parseInt - (parseInt * 0.25)) + 1) + (parseInt - (parseInt * 0.25))));
            if (!answers.contains(String.valueOf(rand))) {
                answers.add(String.valueOf(rand));
                i++;
            }
        }
        return answers;
    }


}
