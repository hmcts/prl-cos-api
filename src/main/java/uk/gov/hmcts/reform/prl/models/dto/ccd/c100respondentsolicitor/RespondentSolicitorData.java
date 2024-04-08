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

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentSolicitorData {


    private String respondentNameForResponse;
    private Consent respondentConsentToApplication;

    private final String whatIsMiamPlaceHolder;
    private final String helpMiamCostsExemptionsPlaceHolder;
    //PRL-4588 - Miam new case fields
    private final YesOrNo hasRespondentAttendedMiam;
    private final YesOrNo respondentWillingToAttendMiam;
    private final String respondentReasonNotAttendingMiam;

    private KeepDetailsPrivate keepContactDetailsPrivate;
    @JsonIgnore
    private KeepDetailsPrivate keepContactDetailsPrivateOther;
    private String confidentialListDetails;

    private final AttendToCourt respondentAttendingTheCourt;

    /**
     * Respondent solicitor's international element.
     */
    private final CitizenInternationalElements internationalElementChild;


    @JsonUnwrapped
    private final RespondentAllegationsOfHarmData respondentAllegationsOfHarmData;

    @JsonUnwrapped
    private final ResponseToAllegationsOfHarm responseToAllegationsOfHarm;

    /** Confirm or Edit contact details. **/
    private final CitizenDetails resSolConfirmEditContactDetails;

    /**
     * Respondent solicitor's Draft PDF response.
     */
    @JsonIgnore
    private final String viewC7PdfLinkText;
    @JsonIgnore
    private final String isEngC7DocGen;
    private final Document draftC7ResponseDoc;
    private final Document finalC7ResponseDoc;
    private final Document finalC7WelshResponseDoc;
    private final Document draftC8ResponseDoc;
    private final Document finalC8ResponseDoc;

    private final List<SubmitConsentEnum> respondentAgreeStatement;

    private final Document draftC1ADoc;

    private final Document draftC1ADocWelsh;

    private final Document finalC1AResponseDoc;

    private final Document finalC1AResponseDocWelsh;

    /**
     * Respondent solicitor's Current or Past proceedings.
     */
    private final YesNoDontKnow currentOrPastProceedingsForChildren;
    private final List<Element<RespondentProceedingDetails>> respondentExistingProceedings;

    /**
     * Respondent solicitor's Ability to participate proceedings.
     */
    private final AbilityToParticipate abilityToParticipateInProceedings;
}
