package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadAdditionalApplicationData {

    @CCD(
            label = "What are you applying for?",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCourtadminCourtnavRAccess.class}
    )
    private final AdditionalApplicationTypeEnum additionalApplicationsApplyingFor;
    @CCD(
            label = "What type of C2 application?",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class}
    )
    private final C2ApplicationTypeEnum typeOfC2Application;
    @CCD(
            label = "Select party(s)",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCourtadminCourtnavRAccess.class}
    )
    private final DynamicMultiSelectList additionalApplicantsList;
    @CCD(
            label = "C2 application",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final C2DocumentBundle temporaryC2Document;
    @CCD(
            label = "Other applications",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCourtadminCourtnavRAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final OtherApplicationsBundle temporaryOtherApplicationsBundle;
    @CCD(
            label = "Application fee to pay",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final String additionalApplicationFeesToPay;
    @CCD(
            label = "Has the applicant applied for Help with Fees?",
            hint = "You must select 'No' to continue with your application.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final YesOrNo additionalApplicationsHelpWithFees;
    @CCD(
            label = "Enter the Help with Fees reference number",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final String additionalApplicationsHelpWithFeesNumber;
    @CCD(
            label = "Solicitor Representing Party Type",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final String representedPartyType;
}
