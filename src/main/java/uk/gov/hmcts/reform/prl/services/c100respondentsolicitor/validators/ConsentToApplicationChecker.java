package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
public class ConsentToApplicationChecker implements RespondentEventChecker {

    @Override
    public boolean isStarted(CaseData caseData, String respondent) {
        Optional<Response> response = findResponse(caseData, respondent);

        return response
            .filter(res -> anyNonEmpty(
                res.getConsent().getApplicationReceivedDate(),
                res.getConsent().getConsentToTheApplication(),
                res.getConsent().getCourtOrderDetails(),
                res.getConsent().getNoConsentReason(),
                res.getConsent().getPermissionFromCourt()
            ))
            .isPresent();
    }

    @Override
    public boolean isFinished(CaseData caseData, String respondent) {
        boolean mandatoryInfo = false;
        Optional<Response> response = findResponse(caseData, respondent);

        log.info("caseData.getRespondents() =" + caseData.getRespondents());

        if (response.isPresent()) {
            Optional<Consent> consent = Optional.ofNullable(response.get().getConsent());
            if (!consent.isEmpty() && checkConsentMandatoryCompleted(consent)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkConsentMandatoryCompleted(Optional<Consent> consent) {

        List<Optional<?>> fields = new ArrayList<>();
        if (consent.isPresent()) {
            Optional<YesOrNo> getConsentToApplication = ofNullable(consent.get().getConsentToTheApplication());
            fields.add(getConsentToApplication);
            if (getConsentToApplication.isPresent() && getConsentToApplication.equals(Optional.of((YesOrNo.No)))) {
                fields.add(ofNullable(consent.get().getNoConsentReason()));
            }
            fields.add(ofNullable(consent.get().getApplicationReceivedDate()));
            Optional<YesOrNo> getPermission = ofNullable(consent.get().getPermissionFromCourt());
            fields.add(getPermission);
            if (getPermission.isPresent() && getPermission.equals(Optional.of((YesOrNo.Yes)))) {
                fields.add(ofNullable(consent.get().getCourtOrderDetails()));
            }
        }
        boolean test = fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        log.info("Consent to application result:: {}", test);
        return test;
    }
}
