package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCourtnavCruAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdersHearingPageFieldShowParams {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isCafcassCymru")
    private final YesOrNo isCafcassCymru;

    //FL401
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isFL401ApplicantPresent")
    private final YesOrNo isFL401ApplicantPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isFL401ApplicantSolicitorPresent")
    private final YesOrNo isFL401ApplicantSolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isFL401RespondentPresent")
    private final YesOrNo isFL401RespondentPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isFL401RespondentSolicitorPresent")
    private final YesOrNo isFL401RespondentSolicitorPresent;

    //C100
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant1Present")
    private final YesOrNo isApplicant1Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant2Present")
    private final YesOrNo isApplicant2Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant3Present")
    private final YesOrNo isApplicant3Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant4Present")
    private final YesOrNo isApplicant4Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant5Present")
    private final YesOrNo isApplicant5Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant1SolicitorPresent")
    private final YesOrNo isApplicant1SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant2SolicitorPresent")
    private final YesOrNo isApplicant2SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant3SolicitorPresent")
    private final YesOrNo isApplicant3SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant4SolicitorPresent")
    private final YesOrNo isApplicant4SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isApplicant5SolicitorPresent")
    private final YesOrNo isApplicant5SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent1Present")
    private final YesOrNo isRespondent1Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent2Present")
    private final YesOrNo isRespondent2Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent3Present")
    private final YesOrNo isRespondent3Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent4Present")
    private final YesOrNo isRespondent4Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent5Present")
    private final YesOrNo isRespondent5Present;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent1SolicitorPresent")
    private final YesOrNo isRespondent1SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent2SolicitorPresent")
    private final YesOrNo isRespondent2SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent3SolicitorPresent")
    private final YesOrNo isRespondent3SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent4SolicitorPresent")
    private final YesOrNo isRespondent4SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isRespondent5SolicitorPresent")
    private final YesOrNo isRespondent5SolicitorPresent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    @JsonProperty("isAutomatedHearingPresent")
    private final YesOrNo isAutomatedHearingPresent;
}
