package quest.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class Response {
    private String question;
    private List<String> answers;
    private String rightAnswer;

    public Response(String question, List<String> answers, String rightAnswer) {
        question = question.trim().toLowerCase().replace("  ", " ").replace("   ", " ").replace("    ", " ");
        this.question = question.substring(0, 1).toUpperCase() + question.substring(1) + "?";
        this.answers = answers;
        this.rightAnswer = rightAnswer;
    }
}
