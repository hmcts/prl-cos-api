package uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SubmitConsentEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus9RolesSpmfxdAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus19RolesFvwefjAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentSolicitorData {


    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String respondentNameForResponse;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
    )
    private Consent respondentConsentToApplication;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final String whatIsMiamPlaceHolder;
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final String helpMiamCostsExemptionsPlaceHolder;
    //PRL-4588 - Miam new case fields
    @CCD(
            label = "Has the respondent attended a Mediation Information and Assessment Meeting (MIAM)?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CitizenRAccess.class}
    )
    private final YesOrNo hasRespondentAttendedMiam;
    @CCD(
            label = "Would they be willing to attend a MIAM?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CitizenRAccess.class}
    )
    private final YesOrNo respondentWillingToAttendMiam;
    @CCD(
            label = "Explain why",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CitizenRAccess.class}
    )
    private final String respondentReasonNotAttendingMiam;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private KeepDetailsPrivate keepContactDetailsPrivate;
    @JsonIgnore
    private KeepDetailsPrivate keepContactDetailsPrivateOther;
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String confidentialListDetails;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final AttendToCourt respondentAttendingTheCourt;

    /**
     * Respondent solicitor's international element.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final CitizenInternationalElements internationalElementChild;


    @JsonUnwrapped
    private final RespondentAllegationsOfHarmData respondentAllegationsOfHarmData;

    @JsonUnwrapped
    private final ResponseToAllegationsOfHarm responseToAllegationsOfHarm;

    /** Confirm or Edit contact details. **/
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private final CitizenDetails resSolConfirmEditContactDetails;

    /**
     * Respondent solicitor's Draft PDF response.
     */
    @JsonIgnore
    private final String viewC7PdfLinkText;
    @JsonIgnore
    private final String isEngC7DocGen;
    @CCD(
            label = " ",
            categoryID = "respondentApplication",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
    )
    private final Document draftC7ResponseDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
    )
    private final Document finalC7ResponseDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
    )
    private final Document finalC7WelshResponseDoc;
    @CCD(
            label = " ",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    private final Document draftC8ResponseDoc;
    @CCD(
            label = " ",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    private final Document finalC8ResponseDoc;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
    )
    private final List<SubmitConsentEnum> respondentAgreeStatement;

    @CCD(
            label = " ",
            categoryID = "respondentC1AApplication",
            searchable = false,
            access = {APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    private final Document draftC1ADoc;

    @CCD(
            label = " ",
            categoryID = "respondentC1AApplication",
            searchable = false,
            access = {APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    private final Document draftC1ADocWelsh;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
    )
    private final Document finalC1AResponseDoc;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
    )
    private final Document finalC1AResponseDocWelsh;

    /**
     * Respondent solicitor's Current or Past proceedings.
     */
    @CCD(
            label = "*Are there previous or ongoing proceedings for the child(ren)?",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final YesNoDontKnow currentOrPastProceedingsForChildren;
    @CCD(label = "Other proceedings", searchable = false, access = {APPLICANTSOLICITORRPlus19RolesFvwefjAccess.class})
    private final List<Element<RespondentProceedingDetails>> respondentExistingProceedings;

    /**
     * Respondent solicitor's Ability to participate proceedings.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
    )
    private final AbilityToParticipate abilityToParticipateInProceedings;
}
