package uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum RespondentSolicitorEvents {
    CONSENT("c100ResSolConsentingToApplication", "Do you give your consent?", "respondentConsentToApplication"),
    KEEP_DETAILS_PRIVATE("c100ResSolKeepDetailsPrivate", "Keep your details private", "keepContactDetailsPrivate"),
    CONFIRM_EDIT_CONTACT_DETAILS(
        "c100ResSolConfirmOrEditContactDetails",
        "Edit your contact details",
        "resSolConfirmEditContactDetails"
    ),
    ATTENDING_THE_COURT("c100ResSolAttendingTheCourt", "Attending the court", "respondentAttendingTheCourt"),
    MIAM(
        "c100ResSolMiam",
        "MIAM",
        "respondentSolicitorHaveYouAttendedMiam,"
            + "whatIsMiamPlaceHolder,helpMiamCostsExemptionsPlaceHolder"
    ),
    CURRENT_OR_PREVIOUS_PROCEEDINGS("c100ResSolCurrentOrPreviousProceedings", "Current or past proceedings",
                                    "currentOrPastProceedingsForChildren,respondentExistingProceedings"
    ),
    ALLEGATION_OF_HARM("c100ResSolAllegationsOfHarm", "Allegations of harm", "respondentAohYesNo,"
        + "respondentAllegationsOfHarm,respondentDomesticAbuseBehaviour,respondentChildAbuseBehaviour,"
        + "respondentChildAbduction,respondentOtherConcerns"),
    INTERNATIONAL_ELEMENT("c100ResSolInternationalElement", "International element", "internationalElementChild"),
    ABILITY_TO_PARTICIPATE(
        "c100ResSolAbilityToParticipate",
        "Ability to participate",
        "abilityToParticipateInProceedings"
    ),
    VIEW_DRAFT_RESPONSE("c100ResSolViewResponseDraftDocument", "View a draft of your response", ""),
    SUBMIT("c100ResSolSubmit", "Submit", "");

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

    public static List<RespondentSolicitorEvents> getEventOrder() {
        return List.of(
            CONSENT,
            KEEP_DETAILS_PRIVATE,
            CONFIRM_EDIT_CONTACT_DETAILS,
            ATTENDING_THE_COURT,
            MIAM,
            CURRENT_OR_PREVIOUS_PROCEEDINGS,
            ALLEGATION_OF_HARM,
            INTERNATIONAL_ELEMENT,
            ABILITY_TO_PARTICIPATE
        );
    }

}
