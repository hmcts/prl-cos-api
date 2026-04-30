package uk.gov.hmcts.reform.prl.services;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private final Handlebars handlebars;

    public EmailTemplateService() {
        this.handlebars = new Handlebars()
            .with(new ClassPathTemplateLoader("/email-templates", ".hbs"));
    }

    public String render(String templateName, Map<String, Object> data) throws IOException {
        Template template = handlebars.compile(templateName);
        return template.apply(data);
    }
}
