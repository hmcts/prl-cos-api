package uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SubmitConsentEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespondentSolicitorData {


    private String respondentNameForResponse;
    private Consent respondentConsentToApplication;

    private final Miam respondentSolicitorHaveYouAttendedMiam;
    private final Miam respondentSolicitorWillingnessToAttendMiam;
    private final String whatIsMiamPlaceHolder;
    private final String helpMiamCostsExemptionsPlaceHolder;

    private KeepDetailsPrivate keepContactDetailsPrivate;
    private KeepDetailsPrivate keepContactDetailsPrivateOther;
    private String confidentialListDetails;

    private final AttendToCourt respondentAttendingTheCourt;

    /**
     * Respondent solicitor's international element.
     */
    private final CitizenInternationalElements internationalElementChild;

    /**
     * Respondent solicitor's allegations of harm.
     */
    private final YesOrNo respondentAohYesNo;
    private final RespondentAllegationsOfHarm respondentAllegationsOfHarm;
    private final List<Element<Behaviours>> respondentDomesticAbuseBehaviour;
    private final List<Element<Behaviours>> respondentChildAbuseBehaviour;
    private final RespondentChildAbduction respondentChildAbduction;
    private final RespondentOtherConcerns respondentOtherConcerns;

    /** Confirm or Edit your contact details. **/
    private final CitizenDetails resSolConfirmEditContactDetails;

    /**
     * Respondent solicitor's Draft PDF response.
     */
    private final String viewC7PdflinkText;
    private final String isEngC7DocGen;
    private final Document draftC7ResponseDoc;
    private final Document finalC7ResponseDoc;

    private final List<SubmitConsentEnum> respondentAgreeStatement;

    private final Document draftC1ADoc;
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
