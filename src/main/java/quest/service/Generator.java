package quest.service;

import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quest.model.Literal;
import quest.model.Padegi;
import quest.model.Response;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.collection.JavaConversions;

import java.util.*;

@Component
public class Generator {

    @Autowired
    private Analize analize;
    @Autowired
    private MyStem mystemAnalyzer;
    @Autowired
    private List<String> badWords;
    @Autowired
    private List<String> month;

    public List<Response> generate(String rawText) {
        try {
            String[] sentenses = rawText.split("[.—:]");

            List<Response> questions = new ArrayList<>();
            createQuestionWithNumber(sentenses, questions);
            createQuestionWithYears(sentenses, questions);
            createQuestionWithNames(sentenses, questions);
//            createQuestionWithNames2(sentenses, questions);
            createQuestionWithGeo(sentenses, questions);
            return questions;
        } catch (MyStemApplicationException e) {
            throw new InternalException("Something bad =(");
        }
    }

    private void createQuestionWithNames(String[] sentenses, List<Response> questions) throws MyStemApplicationException {
        Set<String> names = new HashSet<>();
        {
            for (String sentense : sentenses) {
                List<Literal> analyze = analize.analyze(sentense);
                int size = analyze.size();
                for (int i = 0; i < size; i++) {
                    if (analyze.get(i).isFIO()) {
                        int up = 1;
                        String name = analyze.get(i).normalize();
                        if (i + 1 < size && analyze.get(i + 1).isFIO()) {
                            name = name + " " + analyze.get(i + 1).normalize();
                            up++;
                            if (i + 2 < size && analyze.get(i + 2).isFIO()) {
                                name = name + " " + analyze.get(i + 2).normalize();
                                up++;
                            }
                        }
                        i = i + up;
                        names.add(name);
                    }
                }
            }
        }

        for (String sentense : sentenses) {
            List<Literal> analyze = analize.analyze(sentense);
            int size = analyze.size();
            for (int i = 0; i < size; i++) {
                if (analyze.get(i).isFIO()) {

                    String answer = analyze.get(i).getText();
                    String padegQuestion = getPadegQuestion(analyze.get(i).getText());
                    analyze.get(i).setText(null);
                    if (i + 1 < size && analyze.get(i + 1).isFIO()) {
                        answer = answer + " " + analyze.get(i + 1).getText();
                        analyze.get(i + 1).setText(null);

                        if (i + 2 < size && analyze.get(i + 2).isFIO()) {
                            answer = answer + " " + analyze.get(i + 2).getText();
                            analyze.get(i + 2).setText(null);
                        }
                    }
                    StringBuilder question = new StringBuilder(padegQuestion);
                    String verb = null;
                    if (i > 0 && analyze.get(i - 1).isVerb()) {
                        verb = analyze.get(i - 1).getText();
                        analyze.get(i - 1).setText(null);
                    } else if (i + 1 < size && analyze.get(i + 1).isVerb()) {
                        verb = analyze.get(i + 1).getText();
                        analyze.get(i + 1).setText(null);
                    } else if (i + 2 < size && analyze.get(i + 2).isVerb()) {
                        verb = analyze.get(i + 2).getText();
                        analyze.get(i + 2).setText(null);
                    } else if (i + 3 < size && analyze.get(i + 3).isVerb()) {
                        verb = analyze.get(i + 3).getText();
                        analyze.get(i + 3).setText(null);
                    }
                    String pred = null;
                    if (i > 0 && analyze.get(i - 1).isPred() && analyze.get(i - 1).getText() != null) {
                        pred = analyze.get(i - 1).getText();
                        question = new StringBuilder(analyze.get(i - 1).getText());
                        question.append(" ").append(padegQuestion).append(" ");
                        analyze.get(i - 1).setText(null);
                    }
                    StringBuilder row = new StringBuilder();
                    for (String s : sentense.split("\\s")) {
                        row.append(s).append(" ");
                    }
                    String string = row.toString();

                    StringBuilder finalQuestion = new StringBuilder();
                    if (pred != null) {
                        string = string.replace(pred, "");
                        finalQuestion.append(pred).append(" ");
                    }
                    finalQuestion.append(padegQuestion).append(" ");
                    string = string.replace(answer, "");
                    if (verb != null) {
                        string = string.replace(verb, "");
                        finalQuestion.append(verb).append(" ");
                    }
                    finalQuestion.append(" ").append(string);
//                    analyze.stream().filter(s -> s.getText() != null).forEach(s -> finalQuestion.append(s.getText()).append(" "));

                    String question1 = finalQuestion.toString();
                    if (question1.split("\\s").length > 4) {
                        questions.add(new Response(question1, getRandName(answer, names), answer));
                    }
                    break;
                }
            }
        }
    }


//    private void createQuestionWithNames2(String[] sentenses, List<Response> questions) throws MyStemApplicationException {
//        Set<String> names = new HashSet<>();
//        {
//            for (String sentense : sentenses) {
//                List<Literal> analyze = analize.analyze(sentense);
//                int size = analyze.size();
//                for (int i = 0; i < size; i++) {
//                    if (analyze.get(i).isFIO()) {
//                        int up = 1;
//                        String name = analyze.get(i).normalize();
//                        if (i + 1 < size && analyze.get(i + 1).isFIO()) {
//                            name = name + " " + analyze.get(i + 1).normalize();
//                            up++;
//                            if (i + 2 < size && analyze.get(i + 2).isFIO()) {
//                                name = name + " " + analyze.get(i + 2).normalize();
//                                up++;
//                            }
//                        }
//                        i = i + up;
//                        names.add(name);
//                    }
//                }
//            }
//        }
//
//        for (String sentense : sentenses) {
//            List<Literal> analyze = analize.analyze(sentense);
//            int size = analyze.size();
//            for (int i = 0; i < size; i++) {
//                if (analyze.get(i).isFIO()) {
//
//                    String answer = analyze.get(i).normalize();
//                    String padegQuestion = getPadegQuestion(analyze.get(i).getText());
//                    analyze.get(i).setText(null);
//                    if (i + 1 < size && analyze.get(i + 1).isFIO()) {
//                        answer = answer + " " + analyze.get(i + 1).normalize();
//                        analyze.get(i + 1).setText(null);
//
//                        if (i + 2 < size && analyze.get(i + 2).isFIO()) {
//                            answer = answer + " " + analyze.get(i + 2).normalize();
//                            analyze.get(i + 2).setText(null);
//                        }
//                    }
//                    StringBuilder question = new StringBuilder(padegQuestion);
//                    String verb = null;
//                    if (i > 0 && analyze.get(i - 1).isVerb()) {
//                        verb = analyze.get(i - 1).getText();
//                        analyze.get(i - 1).setText(null);
//                    } else if (i + 1 < size && analyze.get(i + 1).isVerb()) {
//                        verb = analyze.get(i + 1).getText();
//                        analyze.get(i + 1).setText(null);
//                    } else if (i + 2 < size && analyze.get(i + 2).isVerb()) {
//                        verb = analyze.get(i + 2).getText();
//                        analyze.get(i + 2).setText(null);
//                    } else if (i + 3 < size && analyze.get(i + 3).isVerb()) {
//                        verb = analyze.get(i + 3).getText();
//                        analyze.get(i + 3).setText(null);
//                    }
//                    if (i > 0 && analyze.get(i - 1).isPred() && analyze.get(i - 1).getText() != null) {
//                        question = new StringBuilder(analyze.get(i - 1).getText());
//                        question.append(" ").append(padegQuestion).append(" ");
//                        analyze.get(i - 1).setText(null);
//                    }
//                    if (verb != null) {
//                        question.append(verb).append(" ");
//                    }
//                    StringBuilder finalQuestion = question;
//                    analyze.stream().filter(s -> s.getText() != null).forEach(s -> finalQuestion.append(s.getText()).append(" "));
//
//                    questions.add(new Response(question.toString(), getRandName(answer, names), answer));
//                    break;
//                }
//            }
//        }
//        for (String sentense : sentenses) {
//            String[] split = sentense.split("\\s");
//            int length = split.length;
//            for (int i = 0; i < length; i++) {
//                if (isFIO(split[i])) {
//                    String answer = normalize(split[i]);
//                    String padegQuestion = getPadegQuestion(split[i]);
//                    split[i] = "";
//                    if (i + 1 < length && isFIO(split[i + 1])) {
//                        answer = answer + " " + normalize(split[i + 1]);
//                        split[i + 1] = "";
//
//                        if (i + 2 < length && isFIO(split[i + 2])) {
//                            answer = answer + " " + normalize(split[i + 2]);
//                            split[i + 2] = "";
//                        }
//                    }
//                    if (i + 1 < length && isVerb(split[i] + " " + split[i + 1])) {
//                        StringBuilder question = new StringBuilder(padegQuestion);
//                        question.append(split[i + 1]).append(" ");
//                        split[i + 1] = "";
//
//                        for (int j = i + 1; j < length; j++) {
//                            question.append(split[j]).append(" ");
//                        }
//                        questions.add(new Response(question.toString(), getRandName(answer, names), answer));
//                        break;
//                    }
//                }
//            }
//        }
//    }

    private String getPadegQuestion(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            switch (Padegi.getByType(info1.rawResponse())) {
                case IMEN:
                    return "Кто ";
                case ROD:
                case VIN:
                    return "Кого ";
                case DAT:
                    return "Кому ";
                case TVOR:
                    return "Кем ";
                case PRED:
                    return "О ком ";
            }
        }
        throw new IllegalArgumentException();
    }

    private boolean isFIO(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("имя,") || info1.rawResponse().contains("отч,") || info1.rawResponse().contains("фам,")) {
                return true;
            }
        }
        return false;
    }

    private void createQuestionWithYears(String[] sentenses, List<Response> questions) throws MyStemApplicationException {
        for (String sentense : sentenses) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (i + 1 < length && isYear(split[i]) && (split[i + 1].toLowerCase().startsWith("г") || split[i + 1].toLowerCase().equals("г"))) {
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

                    for (String s : split) {
                        if (s.length() > 0) {
                            question.append(" ").append(s);
                        }
                    }
                    String question1 = question.toString();
                    if (question1.split("\\s").length > 4) {
                        questions.add(new Response(question.toString(), getRandAnswer(answer), answer));
                    }
                }
                if (isNumeric(split[i])) {
                    if (length == i + 1) {
                        continue;
                    }
                    if (i + 1 < length && isMonth(split[i + 1])) {
                        String[] answers = new String[3];
                        answers[0] = split[i];
                        answers[1] = split[i + 1];
                        split[i] = "";
                        split[i + 1] = "";
                        if (i + 2 < length && isYear(split[i + 2])) {
                            answers[2] = split[i + 2];
                            split[i + 2] = "";
                        }

                        if (i + 3 < length && split[i + 3].toLowerCase().startsWith("г")) {
                            split[i + 3] = "";
                        }

                        StringBuilder question = new StringBuilder("Когда ");
                        for (String s : split) {
                            if (s.length() > 0) {
                                question.append(s).append(" ");
                            }
                        }
                        String answer = answers[0] + " " + answers[1];
                        List<String> days = getRandAnswer(answers[0]);
                        List<String> resultAnswers = new ArrayList<>(4);
                        if (answers[2] != null) {
                            answer = answer + " " + answers[2];
                            List<String> years = getRandAnswer(answers[2]);
                            resultAnswers.add(days.get(0) + " " + answers[1] + " " + years.get(0));
                            resultAnswers.add(days.get(1) + " " + answers[1] + " " + years.get(1));
                            resultAnswers.add(days.get(2) + " " + answers[1] + " " + years.get(2));
                            resultAnswers.add(days.get(2) + " " + answers[1] + " " + years.get(2));
                        } else {
                            resultAnswers.add(days.get(0) + " " + answers[1]);
                            resultAnswers.add(days.get(1) + " " + answers[1]);
                            resultAnswers.add(days.get(2) + " " + answers[1]);
                            resultAnswers.add(days.get(3) + " " + answers[1]);
                        }

                        String question1 = question.toString();
                        if (question1.split("\\s").length > 4) {
                            questions.add(new Response(question.toString(), resultAnswers, answer));
                        }
                        break;
                    }
                }
            }
        }
    }

    private void createQuestionWithNumber(String[] sentenses, List<Response> questions) throws MyStemApplicationException {
        for (String sentense : sentenses) {
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
                    String q = " сколько ";
                    StringBuilder question = new StringBuilder();
                    if (i > 0 && isPred(split[i - 1])) {
                        question = new StringBuilder(split[i - 1]).append(" ");
                        split[i - 1] = "";
                    }
                    if (split[i + 1].toLowerCase().equals("лет")) {
                        question.append(" сколько ");
                        question.append(split[i + 1]).append(" ");
                        split[i + 1] = "";
                    } else if (checkPadeg(split[i + 1], Padegi.PRED, Padegi.DAT)) {
                        question.append(" скольки ");
                        question.append(split[i + 1]).append(" ");
                        split[i + 1] = "";
                    } else if (checkPadeg(split[i + 1], Padegi.ROD)) {
                        //pr dat  rod+ predlog
                        question.append(" скольки ");
                        question.append(split[i + 1]).append(" ");
                        split[i + 1] = "";
                    } else {
                        question.append(q);
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

                    for (String s : split) {
                        question.append(" ").append(s.toLowerCase());
                    }
                    String question1 = question.toString();
                    for (String badWord : badWords) {
                        question1 = question1.replace(badWord, "");
                    }
                    String question2 = question1;
                    if (question1.split("\\s").length > 4) {
                        questions.add(new Response(question.toString(), getRandAnswer(answer), answer));
                    }
                    break;
                }
            }
        }
    }

    private void createQuestionWithGeo(String[] sentenses, List<Response> questions) throws MyStemApplicationException {
        for (String sentense : sentenses) {
            String[] split = sentense.split("\\s");
            int length = split.length;
            for (int i = 0; i < length; i++) {
                if (isGeo(split[i])) {
                    String answer = split[i];
                    split[i] = "";

                    if (i - 1 > 0 && split[i - 1].equals("в")) {
                        split[i - 1] = "";
                    }
                    StringBuilder question;
                    question = new StringBuilder("Где ");
                    for (String s : split) {
                        if (s.length() > 0) {
                            question.append(" ").append(s);
                        }
                    }

                    String question1 = question.toString();
                    if (question1.split("\\s").length > 4) {
                        questions.add(new Response(question.toString(), null, normalize(answer)));
                    }
                    break;
                }
            }
        }
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

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private boolean isVerb(String str) throws MyStemApplicationException {
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

    private boolean isGeo(String str) throws MyStemApplicationException {
        Iterable<Info> info = getStringInfo(str);
        for (Info info1 : info) {
            if (info1.rawResponse().contains("гео")) {
                return true;
            }
        }
        return false;
    }

    private Iterable<Info> getStringInfo(String data) throws MyStemApplicationException {
        return JavaConversions.asJavaIterable(mystemAnalyzer
                .analyze(Request.apply(data))
                .info()
                .toIterable());
    }

    private String normalize(String data) throws MyStemApplicationException {
        Iterable<Info> stringInfo = getStringInfo(data);
        for (Info info : stringInfo) {
            return info.lex().get();
        }
        return data;
    }

    private List<String> getRandAnswer(String answer) {
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
        while (answers.size() < 4) {
            int rand = rn.nextInt((list.size()));
            answers.add(list.get(rand));
            list.remove(list.get(rand));
        }
        return new ArrayList<>(answers);
    }

}
