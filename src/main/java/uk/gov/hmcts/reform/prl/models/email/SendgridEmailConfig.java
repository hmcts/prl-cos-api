package uk.gov.hmcts.reform.prl.models.email;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SendgridEmailConfig {

    private Map<String, Object> dynamicTemplateData;

    private String toEmailAddress;

    private List<Document> listOfAttachments;

    private LanguagePreference languagePreference;
}
