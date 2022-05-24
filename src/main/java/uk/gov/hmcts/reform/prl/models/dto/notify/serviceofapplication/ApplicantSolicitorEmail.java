package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

import java.time.LocalDate;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApplicantSolicitorEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final LocalDate issueDate;
    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("caseLink")
    private final String caseLink;
    @JsonProperty("privacyNoticeLink")
    private final Map<String,Object> privacyNoticeLink;


    @Builder
    public ApplicantSolicitorEmail(String caseReference,
                                              String caseName,
                                              LocalDate issueDate,
                                              String solicitorName,
                                              String caseLink,
                                              Map<String,Object> privacyNoticeLink) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
        this.caseLink = caseLink;
        this.privacyNoticeLink = privacyNoticeLink;
    }
}
