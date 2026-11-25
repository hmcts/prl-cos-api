package uk.gov.hmcts.reform.prl.services.document.docmosis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.services.document.docmosis.DocmosisTemplatesConfig.Template;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DocmosisTemplatesConfigTest {

    private DocmosisTemplatesConfig config;

    @BeforeEach
    void setUp() {
        config = new DocmosisTemplatesConfig();
    }

    @Test
    void shouldReturnFilenameWhenTemplateExists() {
        Template template1 = new Template();
        template1.setTemplateName("templateA");
        template1.setFilename("fileA.docx");

        Template template2 = new Template();
        template2.setTemplateName("templateB");
        template2.setFilename("fileB.docx");

        config.setTemplates(Arrays.asList(template1, template2));

        Optional<String> result = config.getFilenameByTemplateName("templateB");

        assertThat(result).isPresent();
        assertThat(result.get()).contains("fileB.docx");
    }

    @Test
    void shouldReturnEmptyWhenTemplateDoesNotExist() {
        Template template = new Template();
        template.setTemplateName("templateA");
        template.setFilename("fileA.docx");

        config.setTemplates(Collections.singletonList(template));

        Optional<String> result = config.getFilenameByTemplateName("nonexistent");

        assertThat(result).isNotPresent();
    }

    @Test
    void shouldReturnEmptyWhenTemplatesListIsEmpty() {
        config.setTemplates(Collections.emptyList());

        Optional<String> result = config.getFilenameByTemplateName("any");

        assertThat(result).isNotPresent();
    }
}
