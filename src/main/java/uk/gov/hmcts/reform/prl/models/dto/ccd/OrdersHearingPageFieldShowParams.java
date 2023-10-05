package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdersHearingPageFieldShowParams {

    @JsonProperty("isCafcassCymru")
    private final YesOrNo isCafcassCymru;

    //FL401
    @JsonProperty("isFL401ApplicantPresent")
    private final YesOrNo isFL401ApplicantPresent;
    @JsonProperty("isFL401ApplicantSolicitorPresent")
    private final YesOrNo isFL401ApplicantSolicitorPresent;
    @JsonProperty("isFL401RespondentPresent")
    private final YesOrNo isFL401RespondentPresent;
    @JsonProperty("isFL401RespondentSolicitorPresent")
    private final YesOrNo isFL401RespondentSolicitorPresent;

    //C100
    @JsonProperty("isApplicant1Present")
    private final YesOrNo isApplicant1Present;
    @JsonProperty("isApplicant2Present")
    private final YesOrNo isApplicant2Present;
    @JsonProperty("isApplicant3Present")
    private final YesOrNo isApplicant3Present;
    @JsonProperty("isApplicant4Present")
    private final YesOrNo isApplicant4Present;
    @JsonProperty("isApplicant5Present")
    private final YesOrNo isApplicant5Present;
    @JsonProperty("isApplicant1SolicitorPresent")
    private final YesOrNo isApplicant1SolicitorPresent;
    @JsonProperty("isApplicant2SolicitorPresent")
    private final YesOrNo isApplicant2SolicitorPresent;
    @JsonProperty("isApplicant3SolicitorPresent")
    private final YesOrNo isApplicant3SolicitorPresent;
    @JsonProperty("isApplicant4SolicitorPresent")
    private final YesOrNo isApplicant4SolicitorPresent;
    @JsonProperty("isApplicant5SolicitorPresent")
    private final YesOrNo isApplicant5SolicitorPresent;
    @JsonProperty("isRespondent1Present")
    private final YesOrNo isRespondent1Present;
    @JsonProperty("isRespondent2Present")
    private final YesOrNo isRespondent2Present;
    @JsonProperty("isRespondent3Present")
    private final YesOrNo isRespondent3Present;
    @JsonProperty("isRespondent4Present")
    private final YesOrNo isRespondent4Present;
    @JsonProperty("isRespondent5Present")
    private final YesOrNo isRespondent5Present;
    @JsonProperty("isRespondent1SolicitorPresent")
    private final YesOrNo isRespondent1SolicitorPresent;
    @JsonProperty("isRespondent2SolicitorPresent")
    private final YesOrNo isRespondent2SolicitorPresent;
    @JsonProperty("isRespondent3SolicitorPresent")
    private final YesOrNo isRespondent3SolicitorPresent;
    @JsonProperty("isRespondent4SolicitorPresent")
    private final YesOrNo isRespondent4SolicitorPresent;
    @JsonProperty("isRespondent5SolicitorPresent")
    private final YesOrNo isRespondent5SolicitorPresent;
}
