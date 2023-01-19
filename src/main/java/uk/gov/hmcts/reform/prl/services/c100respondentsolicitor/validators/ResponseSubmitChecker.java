package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
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
        boolean mandatoryFinished = false;
        Optional<Consent> consent = null;

        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication()) && activeRespondentResponse.isPresent()) {
            consent = ofNullable(activeRespondentResponse.get().getValue().getResponse().getConsent());
            mandatoryFinished = checkConsentManadatoryCompleted(consent);
        }

        return mandatoryFinished;
    }

    private boolean checkConsentManadatoryCompleted(Optional<Consent> consent) {
        if (consent.isPresent()) {
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
        return false;
    }

    private boolean checkKeepDetailsPrivateManadatoryCompleted(Optional<KeepDetailsPrivate> keepDetailsPrivate) {
        if (keepDetailsPrivate.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(keepDetailsPrivate.get().getOtherPeopleKnowYourContactDetails()));
            fields.add(ofNullable(keepDetailsPrivate.get().getConfidentiality()));
            fields.add(ofNullable(keepDetailsPrivate.get().getConfidentiality().equals(YesOrNo.Yes)
                                      ? null != keepDetailsPrivate.get().getConfidentialityList() : null));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkContactDetailsManadatoryCompleted(Optional<CitizenDetails> citizenDetails) {
        if (citizenDetails.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(citizenDetails.get().getFirstName()));
            fields.add(ofNullable(citizenDetails.get().getLastName()));
            fields.add(ofNullable(citizenDetails.get().getDateOfBirth()));
            fields.add(ofNullable(citizenDetails.get().getAddress()));
            fields.add(ofNullable(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years()));
            fields.add(ofNullable(citizenDetails.get().getAddressHistory().getIsAtAddressLessThan5Years()
                                      .equals(YesOrNo.No)
                                      ? null != citizenDetails.get().getAddressHistory().getPreviousAddressHistory() : null));
            fields.add(ofNullable(citizenDetails.get().getContact().getPhoneNumber()));
            fields.add(ofNullable(citizenDetails.get().getContact().getEmail()));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkAttendToCourtManadatoryCompleted(Optional<AttendToCourt> attendToCourt) {
        if (attendToCourt.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeeds()));
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeeds().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentWelshNeedsList() : null));
            fields.add(ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter()));
            fields.add(ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentInterpreterNeeds() : null));
            fields.add(ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentInterpreterNeeds() : null));
            fields.add(ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter()));
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeeds()));


            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }
}
