package quest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Response {
    private String question;
    private List<String> answers;
    private String rightAnswer;

    public Response(String question, List<String> answers, String rightAnswer) {
        this.question = question;
        this.answers = answers;
        this.rightAnswer = rightAnswer;
    }
}
