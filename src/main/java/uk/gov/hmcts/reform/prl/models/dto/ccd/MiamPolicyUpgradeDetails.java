package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamDomesticAbuseChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.TypeOfMiamAttendanceEvidenceEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseEvidenceDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiamPolicyUpgradeDetails {
    @CCD(
            label = "*Has any application been made for a care order, a supervision order, an emergency protection order or an order requiring someone to disclose where a child is or to deliver the child to another person and which: a) is still going on? or b) has finished but the order is still in place?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private YesOrNo mpuChildInvolvedInMiam;
    @CCD(
            label = "*Has the applicant attended a Mediation Information & Assessment Meeting (MIAM)?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private YesOrNo mpuApplicantAttendedMiam;
    @CCD(
            label = "*Is the applicant claiming exemption from the requirement to attend a MIAM ?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private YesOrNo mpuClaimingExemptionMiam;
    @CCD(
            label = "Why is the applicant not attending a MIAM?",
            hint = "*Select all reasons that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMPolicyUpgradeExemptionsChecklistEnum",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private List<MiamExemptionsChecklistEnum> mpuExemptionReasons;
    @CCD(
            label = "*What evidence of domestic abuse does the applicant have?",
            hint = "*Select all evidence that applies",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MIAMDomesticAbuseChecklistEnum",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private List<MiamDomesticAbuseChecklistEnum> mpuDomesticAbuseEvidences;
    @CCD(
            label = "*Can you provide evidence?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private YesOrNo mpuIsDomesticAbuseEvidenceProvided;
    @CCD(
            label = "Evidence",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private List<Element<DomesticAbuseEvidenceDocument>> mpuDomesticAbuseEvidenceDocument;
    @CCD(
            label = "Tell us why you cannot provide evidence",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private String mpuNoDomesticAbuseEvidenceReason;
    @CCD(
            label = "Why must the application be made urgently?",
            hint = "*Select one",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private MiamUrgencyReasonChecklistEnum mpuUrgencyReason;
    @CCD(
            label = "Has, there been previous attendance of a MIAM or non-court dispute resolution?",
            hint = "*Select one",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private MiamPreviousAttendanceChecklistEnum mpuPreviousMiamAttendanceReason;
    @CCD(
            label = "* Upload the MIAM certificate or evidence of participating in non-court dispute resolution",
            hint = "These documents must be signed by the mediator or provider",
            categoryID = "MIAMCertificate",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document mpuDocFromDisputeResolutionProvider;
    @CCD(
            label = "*What evidence of MIAM attendance are you submitting?",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private TypeOfMiamAttendanceEvidenceEnum mpuTypeOfPreviousMiamAttendanceEvidence;
    @CCD(
            label = "*Upload the MIAM certificate signed by the mediator.",
            categoryID = "MIAMCertificate",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document mpuCertificateByMediator;
    @CCD(
            label = "*Provide the mediator details",
            hint = "Provide the date of the MIAM, as well as the name and contact details of the MIAM provider",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private String mpuMediatorDetails;
    @CCD(
            label = "What other grounds of exemption apply?",
            hint = "*Select one",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private MiamOtherGroundsChecklistEnum mpuOtherExemptionReasons;
    @CCD(
            label = "*Explain why the applicant is unable to attend MIAM",
            hint = "Include the names, addresses, telephone numbers or e-mail addresses for the authorised family mediators and when they were contacted.",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private String mpuApplicantUnableToAttendMiamReason1;
    @CCD(
            label = "*Explain why the applicant is unable to attend MIAM",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class}
    )
    private String mpuApplicantUnableToAttendMiamReason2;
    private final String mediatorRegistrationNumber;
    private final String familyMediatorServiceName;
    private final String soleTraderName;
    private final Document miamCertificationDocumentUpload;
    @CCD(
            label = "*The applicant confirms that a child who would be the subject of the application or another child of the family who is living with that child is currently",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private MiamPolicyUpgradeChildProtectionConcernEnum mpuChildProtectionConcernReason;
}
