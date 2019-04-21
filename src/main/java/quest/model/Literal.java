package quest.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private boolean number = false;

    public Literal(String text) {
        this.text = text;
        this.number = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Analysis {
        private String qual;
        private String gr;
        private int wt;
        private String lex;
    }

    @JsonIgnore
    public boolean isFIO() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("имя,") || analysis.getGr().contains("отч,") || analysis.getGr().contains("фам,")) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public String normalize() {
        for (Analysis analysis : analysis) {
            return analysis.getLex();
        }
        return "";
    }

    @JsonIgnore
    public boolean isVerb() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("V")) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean isPred() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("PR")) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean isGeo() {
        for (Analysis analysis : analysis) {
            if (analysis.getGr().contains("гео")) {
                return true;
            }
        }
        return false;
    }

}
