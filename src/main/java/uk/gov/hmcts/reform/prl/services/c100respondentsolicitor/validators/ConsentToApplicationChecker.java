package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
public class ConsentToApplicationChecker implements RespondentEventChecker {

    @Override
    public boolean isStarted(CaseData caseData) {
        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();
        return anyNonEmpty(activeRespondent
                               .get()
                               .getValue()
                               .getResponse()
                               .getConsent()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        boolean mandatoryInfo = false;

        Optional<Element<PartyDetails>> activeRespondent = Optional.empty();
        activeRespondent = caseData.getRespondents()
            .stream()
            .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
            .findFirst();

        Optional<Consent> consent = activeRespondent.map(
            partyDetailsElement -> Optional.ofNullable(partyDetailsElement
                                                           .getValue()
                                                           .getResponse()
                                                           .getConsent()))
            .orElse(null);
        if (consent.isPresent() && null != consent) {
            if (checkConsentMandatoryCompleted(consent)) {
                mandatoryInfo = true;
            }
        }
        return mandatoryInfo;
    }

    private boolean checkConsentMandatoryCompleted(Optional<Consent> consent) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(consent.get().getConsentToTheApplication()));
        if (consent.get().getConsentToTheApplication().equals(YesOrNo.No)) {
            fields.add(ofNullable(consent.get().getNoConsentReason()));
        }
        fields.add(ofNullable(consent.get().getApplicationReceivedDate()));
        fields.add(ofNullable(consent.get().getPermissionFromCourt()));
        if (consent.get().getPermissionFromCourt().equals(YesOrNo.Yes)) {
            fields.add(ofNullable(consent.get().getCourtOrderDetails()));
        }
        boolean test = fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        log.info("Consent to application result:: {}", test);
        return test;
    }
}
