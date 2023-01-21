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
        log.info("finding activeRespondent " + activeRespondent);

        Optional<Consent> consent = Optional.ofNullable(activeRespondent.get()
                                                            .getValue().getResponse().getConsent());

        if (!consent.isEmpty()) {
            if (checkConsentManadatoryCompleted(consent)) {
                mandatoryInfo = true;
            }
        }

        return mandatoryInfo;
    }

    private boolean checkConsentManadatoryCompleted(Optional<Consent> consent) {

        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(consent.get().getConsentToTheApplication()));
        fields.add(ofNullable(consent.get().getConsentToTheApplication().equals(YesOrNo.No)
                                  ? null != consent.get().getNoConsentReason() : null));
        fields.add(ofNullable(consent.get().getApplicationReceivedDate()));
        fields.add(ofNullable(consent.get().getPermissionFromCourt()));
        fields.add(ofNullable(consent.get().getPermissionFromCourt().equals(YesOrNo.Yes)
                                  ? null != consent.get().getCourtOrderDetails() : null));

        return fields.stream().noneMatch(Optional::isEmpty)
            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }
}
