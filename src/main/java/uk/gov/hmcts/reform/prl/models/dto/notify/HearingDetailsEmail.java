package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class HearingDetailsEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("partyName")
    private final String partyName;

    @JsonProperty("partySolicitorName")
    private final String partySolicitorName;

    @JsonProperty("hearingDetailsPageLink")
    private final String hearingDetailsPageLink;

    @JsonProperty("hearingDateAndTime")
    private final String hearingDateAndTime;

    @JsonProperty("hearingVenue")
    private final String hearingVenue;

    @JsonProperty("typeOfHearing")
    private final String typeOfHearing;

    @JsonProperty("issueDate")
    private final String issueDate;

    @Builder
    public HearingDetailsEmail(String caseReference, String caseName,
                               String partyName, String partySolicitorName,
                               String hearingDetailsPageLink, String hearingDateAndTime,
                               String hearingVenue, String typeOfHearing, String issueDate) {

        super(caseReference);
        this.caseName = caseName;
        this.partyName = partyName;
        this.partySolicitorName = partySolicitorName;
        this.hearingDetailsPageLink = hearingDetailsPageLink;
        this.hearingDateAndTime = hearingDateAndTime;
        this.hearingVenue = hearingVenue;
        this.typeOfHearing = typeOfHearing;
        this.issueDate = issueDate;
    }
}
