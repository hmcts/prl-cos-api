package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.CONSENT_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConsentToApplicationChecker implements RespondentEventChecker {
    private final RespondentTaskErrorService respondentTaskErrorService;

    @Override
    public boolean isStarted(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);

        if (response.isPresent()) {
            return ofNullable(response.get().getConsent())
                .filter(consent -> anyNonEmpty(
                    consent.getConsentToTheApplication(),
                    consent.getNoConsentReason(),
                    consent.getApplicationReceivedDate(),
                    consent.getPermissionFromCourt(),
                    consent.getCourtOrderDetails()
                )).isPresent();
        }
        return false;
    }

    @Override
    public boolean isFinished(PartyDetails respondingParty) {
        Optional<Response> response = findResponse(respondingParty);
        boolean isFinished;
        if (response.isPresent()) {
            Optional<Consent> consent = Optional.ofNullable(response.get().getConsent());
            if (!consent.isEmpty() && checkConsentMandatoryCompleted(consent)) {
                respondentTaskErrorService.removeError(CONSENT_ERROR);
                isFinished = true;
            } else {
                isFinished = addErrorAndReturn();
            }
        } else {
            isFinished = addErrorAndReturn();
        }
        return isFinished;
    }

    private boolean addErrorAndReturn() {
        respondentTaskErrorService.addEventError(CONSENT, CONSENT_ERROR, CONSENT_ERROR.getError());
        return false;
    }

    private boolean checkConsentMandatoryCompleted(Optional<Consent> consent) {

        List<Optional<?>> fields = new ArrayList<>();
        if (consent.isPresent()) {
            Optional<YesOrNo> getConsentToApplication = ofNullable(consent.get().getConsentToTheApplication());
            fields.add(getConsentToApplication);
            if (getConsentToApplication.isPresent() && YesOrNo.No.equals(getConsentToApplication.get())) {
                fields.add(ofNullable(consent.get().getNoConsentReason()));
            }
            fields.add(ofNullable(consent.get().getApplicationReceivedDate()));
            Optional<YesOrNo> getPermission = ofNullable(consent.get().getPermissionFromCourt());
            fields.add(getPermission);
            if (getPermission.isPresent() && YesOrNo.Yes.equals(getPermission.get())) {
                fields.add(ofNullable(consent.get().getCourtOrderDetails()));
            }
        }
        boolean test = fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        log.info("Consent to application result:: {}", test);
        return test;
    }
}
