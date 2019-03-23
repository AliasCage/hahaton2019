package quest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import scala.Option;

import java.util.Arrays;
import java.util.List;

@Configuration
public class BeanHeap {

    @Bean
    public MyStem createMystemAnalyzer() {
        return new Factory("-igd --format json --weight")
                .newMyStem("3.0", Option.empty()).get();
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper();
    }

    @Bean()
    public List<String> badWords() {
        return Arrays.asList("около", "почти");
    }

    @Bean()
    public List<String> month() {
        return Arrays.asList("январ", "февра", "март", "апрел", "май", "мае", "мая", "июн", "июл", "август", "сентябр", "октябр", "ноябр", "декабр");
    }

}
