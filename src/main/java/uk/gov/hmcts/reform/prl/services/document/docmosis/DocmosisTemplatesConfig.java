package uk.gov.hmcts.reform.prl.services.document.docmosis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@ConfigurationProperties(prefix = "docmosis")
public class DocmosisTemplatesConfig {

    private List<Template> templates = new ArrayList<>();

    @Getter
    @Setter
    public static class Template {
        private String templateName;
        private String filename;
    }

    public Optional<String> getFilenameByTemplateName(String templateName) {
        return templates.stream()
            .filter(t -> t.getTemplateName().equalsIgnoreCase(templateName))
            .map(Template::getFilename)
            .findFirst();
    }
}
