package quest.model;


import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Literal {

    private String text;
    private List<Analysis> analysis;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Analysis {
        private String qual;
        private String gr;
        private int wt;
        private String lex;
    }

    public boolean isFIO() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("имя,") || analysis.getGr().contains("отч,") || analysis.getGr().contains("фам,")) {
                return true;
            }
        }
        return false;
    }

    public String normalize() {
        for (Analysis analysis : analysis) {
            return analysis.getLex();
        }
        return "";
    }

    public boolean isVerb() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("V")) {
                return true;
            }
        }
        return false;
    }

    public boolean isPred() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("PR")) {
                return true;
            }
        }
        return false;
    }

    public boolean isGeo() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("гео")) {
                return true;
            }
        }
        return false;
    }

}
