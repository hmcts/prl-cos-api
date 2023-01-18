package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@SuppressWarnings("ALL")
@Service
public class ResponseSubmitChecker {

    @Autowired
    private ConsentToApplicationChecker consentToApplicationChecker;

    public boolean hasMandatoryCompleted(CaseData caseData, Optional<Element<PartyDetails>> activeRespondentResponse) {
        boolean mandatoryFinished;
        Optional<Consent> consent = null;

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication()) && activeRespondentResponse.isPresent()) {
            consent = ofNullable(activeRespondentResponse.get().getValue().getResponse().getConsent());
            mandatoryFinished = checkConsentManadatoryCompleted(consent);
        }

        return false;
    }

    private boolean checkConsentManadatoryCompleted(Optional<Consent> consent) {
        if (consent.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(consent.get().getConsentToTheApplication()));
            fields.add(ofNullable(consent.get().getConsentToTheApplication().equals(YesOrNo.No)
                                      ? consent.get().getNoConsentReason() : null));
            fields.add(ofNullable(consent.get().getApplicationReceivedDate()));
            fields.add(ofNullable(consent.get().getPermissionFromCourt()));
            fields.add(ofNullable(consent.get().getPermissionFromCourt().equals(YesOrNo.Yes)
                                      ? consent.get().getCourtOrderDetails() : null));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkKeepDetailsPrivateManadatoryCompleted(Optional<KeepDetailsPrivate> keepDetailsPrivate) {
        if (keepDetailsPrivate.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(keepDetailsPrivate.get().getOtherPeopleKnowYourContactDetails()));
            fields.add(ofNullable(keepDetailsPrivate.get().getConfidentiality()));
            fields.add(ofNullable(keepDetailsPrivate.get().getConfidentiality().equals(YesOrNo.Yes)
                                      ? keepDetailsPrivate.get().getConfidentialityList() : null));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }
}
