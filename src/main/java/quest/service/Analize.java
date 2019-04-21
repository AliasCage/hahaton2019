package quest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import quest.model.Literal;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class Analize {

    @Autowired
    private MyStem myStem;
    @Autowired
    private ObjectMapper mapper;

    public List<Literal> analyze(String row) {
        try {
            List<Literal> response = new ArrayList<>();

            int i = 0;
            String[] sentence = row.split(" ");
            for (Info info : getStringInfo(row)) {
                Literal literal = mapper.readValue(info.rawResponse(), Literal.class);
                while (!sentence[i].contains(literal.getText())) {
                    response.add(new Literal(sentence[i++]));
                }
                response.add(literal);
                i++;
            }
            return response;
        } catch (IOException | MyStemApplicationException e) {
            throw new RuntimeException("Something bad =(", e);
        }
    }

    private Iterable<Info> getStringInfo(String data) throws MyStemApplicationException {
        return JavaConversions.asJavaIterable(myStem
                .analyze(Request.apply(data))
                .info()
                .toIterable());
    }
}
