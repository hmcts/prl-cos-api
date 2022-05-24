package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Data;
import org.joda.time.LocalDate;

@Data
public class ServiceOfApplicationSolicitorEmail extends EmailTemplateVars {

    private final String caseName;
    private final LocalDate issueDate;
    private final String solicitorName;
    private final String caseLink;
    private final String privacyNoticeLink;


    @Builder
    public ServiceOfApplicationSolicitorEmail(String caseReference,
                                              String caseName,
                                              LocalDate issueDate,
                                              String solicitorName,
                                              String caseLink,
                                              String privacyNoticeLink) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
        this.caseLink = caseLink;
        this.privacyNoticeLink = privacyNoticeLink;
    }

}
