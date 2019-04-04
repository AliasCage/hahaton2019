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
            Iterable<Info> info = getStringInfo(row);
            for (Info i : info) {
                Literal literal = mapper.readValue(i.rawResponse(), Literal.class);
                response.add(literal);
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
