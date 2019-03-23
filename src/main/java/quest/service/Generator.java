package quest.service;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.springframework.stereotype.Component;
import quest.model.Padegi;
import quest.model.Response;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.util.*;

@Component
public class Generator {

    public List<Response> generate(String rawText) {
        try {
            List<Response> question = createQuestionWithNumber(rawText);
            question.addAll(createQuestionWithYears(rawText));
            question.addAll(createQuestionWithNames(rawText));
            question.addAll(createQuestionWithGeo(rawText));
            return question;
        } catch (MyStemApplicationException e) {
            throw new InternalException("Something bad =(");
        }
    }

    private List<Response> createQuestionWithNames(String text) throws MyStemApplicationException {
        List<Response> responses = new ArrayList<>();
        Set<String> names = new HashSet<>();
        {
            for (String sentense : text.split("\\.")) {
                String[] word = sentense.split("\\s");
                for (int i = 0; i < word.length; i++) {
                    if (isFIO(word[i])) {
                        int up = 1;
                        String name = normalize(word[i]);
                        if (i + 1 < word.length && isFIO(word[i + 1])) {
                            name = name + " " + normalize(word[i + 1]);
                            up++;
                        }
                        if (i + 2 < word.length && isFIO(word[i + 2])) {
                            name = name + " " + normalize(word[i + 2]);
                            up++;
                        }
                        i = i + up;
                        names.add(name);
                    }
                }
            }
        }

        for (String sentense : text.split("\\.")) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (isFIO(split[i])) {
                    String answer = normalize(split[i]);
                    split[i] = "";
                    if (i + 2 < length && isFIO(split[i + 1])) {
                        answer = answer + " " + normalize(split[i + 1]);
                        split[i + 1] = "";
                    }
                    if (i + 3 < length && isFIO(split[i + 2])) {
                        answer = answer + " " + normalize(split[i + 2]);
                        split[i + 2] = "";
                    }
                    StringBuilder question = new StringBuilder("Кто ");
                    for (int j = 0; j < length; j++) {
                        if (split[j].length() > 0) {
                            question.append(split[j]).append(" ");
                        }
                    }
                    responses.add(new Response(question.toString(), getRandName(answer, names), answer));
                    break;
                }
            }
        }
        return responses;
    }

    private boolean isFIO(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("имя") || info1.rawResponse().contains("отч") || info1.rawResponse().contains("фам")) {
                return true;
            }
        }
        return false;
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
                    split[i] = "";
                    if (split[i - 1].toLowerCase().equals("с")) {
                        question = new StringBuilder("С какого года ");
                    } else if (split[i - 1].toLowerCase().equals("в")) {
                        question = new StringBuilder("В каком году ");
                    } else {
                        System.out.println(sentense);
                        continue;
                    }
                    split[i - 1] = "";
                    if (split[i + 1].toLowerCase().startsWith("г")) {
                        split[i + 1] = "";
                    }

                    for (int j = 0; j < length; j++) {
                        if (split[j].length() > 0) {
                            question.append(" ").append(split[j]);
                        }
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
                    //todo:fix in future
                    String answer = split[i];
                    split[i] = "";
                    StringBuilder question = new StringBuilder(" сколько ");
                    if (checkPadeg(split[i + 1], Padegi.PRED, Padegi.DAT)) {
                        question = new StringBuilder(" скольки ");
                    }
                    if (isPred(split[i - 1])) {
                        question = new StringBuilder(split[i - 1]).append(" ");
//                        if (isMany(split[i + 1])) {
                        if (checkPadeg(split[i + 1], Padegi.ROD)) {
                            //pr dat  rod+ predlog
                            question.append(" скольки ");
                        }
//                        else {
//                            //vin rod tv
//                            question.append(" сколько ");
//                        }
                    }
                    question.append(split[i + 1]);
                    split[i + 1] = "";
                    for (int j = i; j > 0; j--) {
                        if (isVerb(split[j])) {
                            question.append(" ").append(split[j]);
                            split[j] = "";
                            i--;
                        }
                    }

                    for (int j = 0; j < i; j++) {
                        if (split[i].length() > 0 && !badWords.contains(split[i].toLowerCase())) {
                            question.append(" ").append(split[j].toLowerCase());
                        }
                    }
                    for (int j = i + 1; j < length; j++) {
                        if (split[i].length() > 0 && !badWords.contains(split[i].toLowerCase())) {
                            question.append(" ").append(split[j].toLowerCase());
                        }
                    }
                    responses.add(new Response(question.toString(), getRandAnswer(answer), answer));
                    break;
                }
            }
        }
        return responses;
    }

    private List<Response> createQuestionWithGeo(String text) throws MyStemApplicationException {
        List<Response> responses = new ArrayList<>();
        for (String sentense : text.split("\\.")) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (isGeo(split[i])) {
                    String answer = split[i];
                    split[i] = "";
                    StringBuilder question;
                    question = new StringBuilder("Где ");
                    for (int j = 0; j < length; j++) {
                      if (split[j].length() > 0) {
                        question.append(" ").append(split[j]);
                      }
                    }
                  responses.add(new Response(question.toString(), null, answer));
                  break;
                }
            }
        }
        return responses;
    }

    private boolean checkPadeg(String str, Padegi... padegis) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            for (Padegi padegi : padegis) {
                if (info1.rawResponse().contains(padegi.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMany(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("мн")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPred(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("PR")) {
                return true;
            }
        }
        return false;
    }

    private static List<String> badWords = Arrays.asList("около", "почти");
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

    public boolean isGeo(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("geo")) {
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

    private static String normalize(String data) throws MyStemApplicationException {
        Iterable<Info> stringInfo = getStringInfo(data);
        for (Info info : stringInfo) {
            return info.lex().get();
        }
        return data;
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

    private List<String> getRandName(String answer, Set<String> names) {
        if (names.size() < 4) {
            return new ArrayList<>(names);
        }

        List<String> list = new ArrayList<>(names);
        Set<String> answers = new HashSet<>(4);
        answers.add(answer);

        Random rn = new Random();
        while (answer.length() < 4) {
            int rand = rn.nextInt((list.size()));
            answers.add(list.get(rand));
            list.remove(list.get(rand));
        }
        return new ArrayList<>(answers);
    }


}
