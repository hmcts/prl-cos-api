package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum RespondentSolicitorEvents {
    START_RESPONSE("c100ResSolStartingResponse", "c100ResSolStartingResponse"),
    CONSENT("c100ResSolConsentingToApplication", "respondentConsentToApplication"),
    KEEP_DETAILS_PRIVATE("c100ResSolKeepDetailsPrivate", "keepContactDetailsPrivate"),
    CONFIRM_EDIT_CONTACT_DETAILS("c100ResSolConfirmOrEditContactDetails", "resSolConfirmEditContactDetails");

    private final String eventId;
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
