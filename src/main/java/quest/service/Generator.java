package quest.service;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.springframework.stereotype.Component;
import quest.model.Response;
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
            List<Response> question = createQuestionWithNumber(rawText);
            question.addAll(createQuestionWithYears(rawText));
            return question;
        } catch (MyStemApplicationException e) {
            throw new InternalException("Something bad =(");
        }
    }

    private List<Response> createQuestionWithYears(String text) throws MyStemApplicationException {
        List<Response> responses = new ArrayList<>();
        for (String sentense : text.split("\\.")) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (isYear(split[i]) && (split[i + 1].toLowerCase().startsWith("г") || split[i + 1].toLowerCase().equals("г"))) {
                    if (i == 0) {
                        continue;
                    }
                    StringBuilder question;
                    String answer = split[i];
                    if (split[i - 1].toLowerCase().equals("с")) {
                        question = new StringBuilder("С какого года ");
                    } else if (split[i - 1].toLowerCase().equals("в")) {
                        question = new StringBuilder("В каком ");
                    } else {
                        System.out.println(sentense);
                        continue;
                    }
                    int from = 0;
                    int to = i - 1;
                    if (i < length / 2) {
                        from = i + 1;
                        to = length;
                    }
                    for (int j = from; j < to; j++) {
                        question.append(" ").append(split[j]);
                    }
                    responses.add(new Response(question.toString(), getRandAnswer(answer), answer));

                }
//                if (isNumeric(split[i])) {
//                    if (length == i + 1) {
//                        continue;
//                    }
//                    if (isMonth(split[i + 1])) {
//                        continue;
//                    }
//                    String answer = split[i];
//                    StringBuilder question = new StringBuilder("Сколько ").append(split[i + 1]);
//                    if (isVerb(split[i - 1])) {
//                        question.append(split[i - 1]);
//                        i--;
//                    }
//                    for (int j = 0; j < i; j++) {
//                        question.append(" ").append(split[j].toLowerCase());
//                    }
//                    for (int j = i + 3; j < length; j++) {
//                        question.append(" ").append(split[j].toLowerCase());
//                    }
//                    responses.add(new Response(question.toString(), getRandAnswer(answer), answer));
//                    break;
//                }
            }
        }

        return responses;
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
                    StringBuilder question = new StringBuilder("Сколько ").append(split[i + 1]);
                    if (isVerb(split[i - 1])) {
                        question.append(split[i - 1]);
                        i--;
                    }
                    for (int j = 0; j < i; j++) {
                        question.append(" ").append(split[j].toLowerCase());
                    }
                    for (int j = i + 3; j < length; j++) {
                        question.append(" ").append(split[j].toLowerCase());
                    }
                    responses.add(new Response(question.toString(), getRandAnswer(answer), answer));
                    break;
                }
            }
        }
        return responses;
    }

    private static List<String> month = Arrays.asList("январ", "февра", "март", "апрел", "май", "мае", "мая", "июн", "июл", "август", "сентябр", "октябр", "ноябр", "декабр");

    private boolean isMonth(String s) {
        boolean contains = false;
        for (String s1 : month) {
            contains = s.toLowerCase().contains(s1);
            if (contains) {
                return true;
            }
        }
        return contains;
    }

    private boolean isYear(String s) {
        return isNumeric(s) && s.length() <= 4 && (Integer.parseInt(s) < 2032 && Integer.parseInt(s) > 1009);
    }

    public boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public boolean isVerb(String str) throws MyStemApplicationException {
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

    public List<String> getRandAnswer(String answer) {
        List<String> answers = new ArrayList<>(4);
        answers.add(answer);

        int val = Integer.parseInt(answer);
        float step = (float) (3f / (Math.pow(10, answer.length() - 1)));
        int max = (int) (val * (1 + step));
        int min = (int) (val * (1 - step));
        Random rn = new Random();
        for (int i = 0; i < 3; ) {
            Integer rand = rn.nextInt((max - min) + 1) + min;
            if (!answers.contains(String.valueOf(rand))) {
                answers.add(String.valueOf(rand));
                i++;
            }
        }
        return answers;
    }


}
