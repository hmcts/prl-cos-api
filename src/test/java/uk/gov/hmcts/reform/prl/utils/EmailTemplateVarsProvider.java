package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

@NoArgsConstructor
public class EmailTemplateVarsProvider {

    public static EmailTemplateVars empty() {
        return new EmailTemplateVars();
    }

    public static EmailTemplateVars of(String caseId) {
        return new EmailTemplateVars(caseId);
    }
}
