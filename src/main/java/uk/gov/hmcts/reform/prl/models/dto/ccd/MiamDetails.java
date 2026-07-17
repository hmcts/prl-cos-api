package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamDetails {
    @CCD(
            label = "        ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CourtnavCuAccess.class}
    )
    private YesOrNo applicantAttendedMiam;
    @CCD(
            label = "        ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CourtnavCuAccess.class}
    )
    private YesOrNo claimingExemptionMiam;
    @CCD(
            label = "        ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private YesOrNo familyMediatorMiam;
    @CCD(
            label = "        ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess.class}
    )
    private YesOrNo otherProceedingsMiam;
    @CCD(
            label = "        ",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess.class}
    )
    private String applicantConsentMiam;
    @CCD(
            label = "       ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMExemptionsChecklistEnum",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private List<MiamExemptionsChecklistEnum> miamExemptionsChecklist;
    @CCD(
            label = "        ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMDomesticViolenceChecklistEnum",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private List<MiamDomesticViolenceChecklistEnum> miamDomesticViolenceChecklist;
    @CCD(
            label = "        ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMUrgencyReasonChecklistEnum",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private List<MiamUrgencyReasonChecklistEnum> miamUrgencyReasonChecklist;
    @CCD(
            label = "*The applicant confirms that a child who would be the subject of the\napplication or another child of the family who is living\nwith that child is currently",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private List<MiamChildProtectionConcernChecklistEnum> miamChildProtectionConcernList;
    @CCD(
            label = "*Select one",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private MiamPreviousAttendanceChecklistEnum miamPreviousAttendanceChecklist;
    @CCD(
            label = "*Select one which are applied",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMPreviousAttendanceChecklistEnum",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess.class}
    )
    private List<MiamPreviousAttendanceChecklistEnum> miamPreviousAttendanceChecklist1;
    @CCD(
            label = "*Select one",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private MiamOtherGroundsChecklistEnum miamOtherGroundsChecklist;
    @CCD(
            label = "*Select one which are applied",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMOtherGroundsChecklistEnum",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess.class}
    )
    private List<MiamOtherGroundsChecklistEnum> miamOtherGroundsChecklist1;
    @CCD(
            label = "*Mediator Registration Number (URN)",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private final String mediatorRegistrationNumber;
    @CCD(
            label = "*Family Mediation Service Name",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private final String familyMediatorServiceName;
    @CCD(
            label = "*Sole Trader Name",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private final String soleTraderName;
    //TODO: refactor to remove duplicated details screen
    @CCD(
            label = "*MIAM certificate",
            categoryID = "MIAMCertificate",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private Document miamCertificationDocumentUpload;
    @CCD(
            label = "*Mediator Registration Number (URN)",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final String mediatorRegistrationNumber1;
    @CCD(
            label = "*Family Mediation Service Name",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final String familyMediatorServiceName1;
    @CCD(
            label = "*Sole Trader Name",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final String soleTraderName1;
    @CCD(
            label = "*MIAM certificate",
            categoryID = "MIAMCertificate",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CourtnavCudAccess.class}
    )
    private final Document miamCertificationDocumentUpload1;
}
