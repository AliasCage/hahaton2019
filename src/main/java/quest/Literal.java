package quest;


import java.util.List;

public class Literal {

    private String text;
    private List<Analysis> analysis;

    public Literal() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    public static class Analysis {
        private String gr;
        private int wt;
        private String lex;

        public String getGr() {
            return gr;
        }

        public void setGr(String gr) {
            this.gr = gr;
        }

        public int getWt() {
            return wt;
        }

        public void setWt(int wt) {
            this.wt = wt;
        }

        public String getLex() {
            return lex;
        }

        public void setLex(String lex) {
            this.lex = lex;
        }
    }

}
