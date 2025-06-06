package uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum RespondentSolicitorEvents {
    CONSENT("c100ResSolConsentingToApplication", "Do you give your consent? / A ydych yn rhoi eich caniatâd?", "respondentConsentToApplication"),
    KEEP_DETAILS_PRIVATE("c100ResSolKeepDetailsPrivate", "Keep details private / Cadwch y manylion yn breifat", "keepContactDetailsPrivate"),
    CONFIRM_EDIT_CONTACT_DETAILS(
            "c100ResSolConfirmOrEditContactDetails",
            "Edit contact details / Golygu manylion cyswllt",
            "resSolConfirmEditContactDetails"
    ),
    ATTENDING_THE_COURT("c100ResSolAttendingTheCourt", "Attending the court / Mynychu'r llys", "respondentAttendingTheCourt"),
    MIAM(
            "c100ResSolMiam",
            "MIAM / MIAM",
        "hasRespondentAttendedMiam,respondentWillingToAttendMiam,respondentReasonNotAttendingMiam,"
                    + "whatIsMiamPlaceHolder,helpMiamCostsExemptionsPlaceHolder"
    ),
    OTHER_PROCEEDINGS("c100ResSolCurrentOrPreviousProceedings", "Other proceedings / Achosion eraill",
            "currentOrPastProceedingsForChildren,respondentExistingProceedings"
    ),
    ALLEGATION_OF_HARM("c100ResSolAllegationsOfHarm", "Make allegations of harm / Gwneud honiadau o niwed", "respondentAohYesNo,"
            + "respondentAllegationsOfHarm,respondentDomesticAbuseBehaviour,respondentChildAbuseBehaviour,"
            + "respondentChildAbduction,respondentOtherConcerns"),
    INTERNATIONAL_ELEMENT("c100ResSolInternationalElement", "International element / Elfen ryngwladol", "internationalElementChild"),
    RESPOND_ALLEGATION_OF_HARM("c100ResSolResponseToAllegationsOfHarm","Respond to allegations of harm / Ymateb i honiadau o niwed",
            "responseToAllegationsOfHarmYesOrNoResponse,responseToAllegationsOfHarmDocument"),
    ABILITY_TO_PARTICIPATE(
            "c100ResSolLitigationCapacity",
            "Litigation capacity / Capasiti cyfreitha",
            "abilityToParticipateInProceedings"
    ),
    VIEW_DRAFT_RESPONSE("c100ResSolViewResponseDraftDocument", "View a draft of your response / Gweld drafft o'ch ymateb", ""),
    SUBMIT("c100ResSolSubmit", "Submit / Cyflwyno", "");

    private final String eventId;
    private final String eventName;
    private final String caseFieldName;

    public String getEventId() {
        return eventId;
    }

    public static Optional<RespondentSolicitorEvents> getCaseFieldName(String eventId) {
        return Arrays.stream(RespondentSolicitorEvents.values())
                .filter(event -> event.eventId.equals(eventId))
                .findFirst();
    }

    public static List<RespondentSolicitorEvents> getEventOrder(CaseData caseData) {
        if (null != caseData.getC1ADocument()) {
            return List.of(
                    CONSENT,
                    KEEP_DETAILS_PRIVATE,
                    CONFIRM_EDIT_CONTACT_DETAILS,
                    ATTENDING_THE_COURT,
                    MIAM,
                    OTHER_PROCEEDINGS,
                    ALLEGATION_OF_HARM,
                    RESPOND_ALLEGATION_OF_HARM,
                    INTERNATIONAL_ELEMENT,
                    ABILITY_TO_PARTICIPATE
            );
        }
        return List.of(
                CONSENT,
                KEEP_DETAILS_PRIVATE,
                CONFIRM_EDIT_CONTACT_DETAILS,
                ATTENDING_THE_COURT,
                MIAM,
            OTHER_PROCEEDINGS,
                ALLEGATION_OF_HARM,
                INTERNATIONAL_ELEMENT,
                ABILITY_TO_PARTICIPATE
        );
    }

}