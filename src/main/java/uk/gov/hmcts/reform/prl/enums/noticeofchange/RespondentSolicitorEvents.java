package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum RespondentSolicitorEvents {
    START_RESPONSE("c100ResSolStartingResponse", "Respond to the application", "c100ResSolStartingResponse"),
    CONSENT("c100ResSolConsentingToApplication", "Do you give your consent?", "respondentConsentToApplication"),
    KEEP_DETAILS_PRIVATE("c100ResSolKeepDetailsPrivate", "Keep your details private", "keepContactDetailsPrivate"),
    CONFIRM_EDIT_CONTACT_DETAILS(
        "c100ResSolConfirmOrEditContactDetails",
        "Edit your contact details",
        "resSolConfirmEditContactDetails"
    ),
    ATTENDING_THE_COURT("c100ResSolAttendingTheCourt", "Attending the court", "respondentAttendingToCourt"),
    MIAM(
        "c100ResSolSolicitorMiam",
        "MIAM",
        "respondentSolicitorWillingnessToAttendMiam,whatIsMiamPlaceHolder,helpMiamCostsExemptionsPlaceHolder"
    ),
    CURRENT_OR_PREVIOUS_PROCEEDINGS("c100ResSolCurrentOrPreviousProceedings", "Current or past proceedings",
                                    "currentOrPastProceedingsForChildren,respondentExistingProceedings"
    ),
    ALLEGATION_OF_HARM("c100ResSolAllegationsOfHarm", "Allegations of harm", ""),
    INTERNATIONAL_ELEMENT("c100ResSolInternationalElement", "International element", ""),
    ABILITY_TO_PARTICIPATE("c100ResSolAbilityToParticipate", "Ability to participate", ""),
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
}
