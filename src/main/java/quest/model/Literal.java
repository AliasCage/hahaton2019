package quest.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Collections;
import java.util.List;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class Literal {

    private String text;
    private List<Analysis> analysis;
    private boolean number = false;
    private boolean year = false;

    public static Literal ofNumber(Integer number) {
        return Literal.builder()
                .analysis(Collections.emptyList())
                .year(number < 2032 && number > 1009)
                .text(number.toString())
                .number(true)
                .build();
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
            if (analysis.getGr().contains("persn") ||
                    analysis.getGr().contains("patrn") ||
                    analysis.getGr().contains("famn")) {
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
        return text;
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
            if (analysis.getGr().contains("geo")) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean checkPadeg(Padegi... padegis) {
        for (Analysis a : analysis) {
            for (Padegi padegi : padegis) {
                if (a.getGr().contains(padegi.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

}
