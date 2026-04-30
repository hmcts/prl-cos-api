package uk.gov.hmcts.reform.prl.services;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateServiceTest {

    @Mock
    private Handlebars handlebars;
    @InjectMocks
    private EmailTemplateService emailTemplateService;

    @Test
    void testRender() throws IOException {
        Map<String, Object> data = new HashMap<>();
        Template mockTemplate = mock(Template.class);
        when(mockTemplate.apply(data)).thenReturn("Rendered Content");
        when(handlebars.compile("test-template")).thenReturn(mockTemplate);

        String result = emailTemplateService.render("test-template", data);

        assertThat(result).isEqualTo("Rendered Content");
    }
}
