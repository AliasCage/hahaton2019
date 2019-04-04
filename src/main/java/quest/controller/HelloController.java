package quest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import quest.model.Literal;
import quest.model.Request;
import quest.model.Response;
import quest.service.Analize;
import quest.service.Generator;

import java.util.List;

@RestController
public class HelloController {

    @Autowired
    private Generator generator;
    @Autowired
    private Analize analize;

    @RequestMapping(path = "/generate", method = RequestMethod.POST)
    public List<Response> generate(@RequestBody Request request) {
        return generator.generate(request.getText());
    }

    @RequestMapping(path = "/analise", method = RequestMethod.POST)
    public List<Literal> analise(@RequestBody String request) {
        return analize.analyze(request);
    }

}
