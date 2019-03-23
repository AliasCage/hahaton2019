package quest.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@NoArgsConstructor
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

}
