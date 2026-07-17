package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCuAccess;

@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllegationOfHarm {

    /**
     * Allegations of harm.
     */

    @CCD(
            label = "*Are there allegations of harm?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CourtnavCuAccess.class}
    )
    private final YesOrNo allegationsOfHarmYesNo;
    @CCD(
            label = "*Any form of domestic abuse",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final YesOrNo allegationsOfHarmDomesticAbuseYesNo;
    @CCD(
            label = "Physical abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "applicantOrChildren",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("physicalAbuseVictim")
    private final List<ApplicantOrChildren> physicalAbuseVictim;
    @CCD(
            label = "Emotional abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "applicantOrChildren",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("emotionalAbuseVictim")
    private final List<ApplicantOrChildren> emotionalAbuseVictim;
    @CCD(
            label = "Psychological abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "applicantOrChildren",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("psychologicalAbuseVictim")
    private final List<ApplicantOrChildren> psychologicalAbuseVictim;
    @CCD(
            label = "Sexual abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "applicantOrChildren",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("sexualAbuseVictim")
    private final List<ApplicantOrChildren> sexualAbuseVictim;
    @CCD(
            label = "Financial abuse",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "applicantOrChildren",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("financialAbuseVictim")
    private final List<ApplicantOrChildren> financialAbuseVictim;
    @CCD(
            label = "*Child abduction",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final YesOrNo allegationsOfHarmChildAbductionYesNo;
    @CCD(
            label = "*Why do you believe the child(ren) may be abducted?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String childAbductionReasons;
    @CCD(
            label = "*Have there been any previous threats, attempts to abduct or actual abduction of the child(ren)?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo previousAbductionThreats;
    @CCD(
            label = "*Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String previousAbductionThreatsDetails;
    @CCD(
            label = "*Where is/are the child(ren) now?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String childrenLocationNow;
    @CCD(
            label = "*Has the passport office been notified?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo abductionPassportOfficeNotified;
    @CCD(
            label = "*Do(es) the child(ren) have more than one passport?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo abductionChildHasPassport;
    @CCD(
            label = "*Who is in posession of the child(ren)'s passport(s)?",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final AbductionChildPassportPossessionEnum abductionChildPassportPosession;
    @CCD(
            label = "*Give Details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String abductionChildPassportPosessionOtherDetail;
    @CCD(
            label = "*Were the police or any other organisation/agency involved in any previous incident of attempted abduction or abduction?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo abductionPreviousPoliceInvolvement;
    @CCD(
            label = "*Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String abductionPreviousPoliceInvolvementDetails;
    @CCD(
            label = "*Child abuse",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final YesOrNo allegationsOfHarmChildAbuseYesNo;
    @CCD(
            label = "*Drugs, alcohol or substance abuse",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final YesOrNo allegationsOfHarmSubstanceAbuseYesNo;
    @CCD(
            label = "*Other safety or welfare concerns",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class}
    )
    private final YesOrNo allegationsOfHarmOtherConcernsYesNo;
    @CCD(
            label = "Behaviour",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class, CaseworkerWaTaskConfigurationCudAccess.class, CourtnavCuAccess.class}
    )
    @JsonProperty("behaviours")
    private final List<Element<Behaviours>> behaviours;
    @CCD(
            label = "*Non-molestation order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersNonMolestation;
    @CCD(
            label = "*Occupation order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CourtnavCruAccess.class}
    )
    private final YesOrNo ordersOccupation;
    @CCD(
            label = "*Forced marriage protection order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersForcedMarriageProtection;
    @CCD(
            label = "*Restraining order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersRestraining;
    @CCD(
            label = "*Other injunctive order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersOtherInjunctive;
    @CCD(
            label = "*Undertaking in place order",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersUndertakingInPlace;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersNonMolestationDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersNonMolestationEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersNonMolestationCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersNonMolestationCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersNonMolestationDocument;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOccupationDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOccupationEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersOccupationCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersOccupationCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersOccupationDocument;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersForcedMarriageProtectionDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersForcedMarriageProtectionEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersForcedMarriageProtectionCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersForcedMarriageProtectionCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersForcedMarriageProtectionDocument;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersRestrainingDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersRestrainingEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersRestrainingCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersRestrainingCourtName;
    @CCD(
            label = "Restraining order document",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersRestrainingDocument;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOtherInjunctiveDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersOtherInjunctiveEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersOtherInjunctiveCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersOtherInjunctiveCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersOtherInjunctiveDocument;
    @CCD(
            label = "Date issued",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersUndertakingInPlaceDateIssued;
    @CCD(
            label = "End date",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate ordersUndertakingInPlaceEndDate;
    @CCD(
            label = "*Is the order current?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo ordersUndertakingInPlaceCurrent;
    @CCD(
            label = "Name of court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String ordersUndertakingInPlaceCourtName;
    @CCD(
            label = "Upload relevant order(s)",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class}
    )
    private final Document ordersUndertakingInPlaceDocument;
    @CCD(
            label = "*Are there other concerns about the child(ren)'s safety and wellbeing?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo allegationsOfHarmOtherConcerns;
    @CCD(
            label = "*Give details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String allegationsOfHarmOtherConcernsDetails;
    @CCD(
            label = "*What steps or orders does the applicant want the court to take or make to protect the safety of the child(ren) and/or yourself?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String allegationsOfHarmOtherConcernsCourtActions;
    @CCD(
            label = "*Do you agree to the child(ren) spending unsupervised time with the other person(s) in receipt of this form?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo agreeChildUnsupervisedTime;
    @CCD(
            label = "*Do you agree to the child(ren) spending supervised time with the other person(s) in receipt of this form?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo agreeChildSupervisedTime;
    @CCD(
            label = "*Do you agree to the child having other forms of contact with the other person in receipt of this form? (by telephone, text, email, social media)",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo agreeChildOtherContact;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Types of harm", searchable = false)
  private String typesOfHarm;
  // ==== end synthesised definition-only fields ====
}
