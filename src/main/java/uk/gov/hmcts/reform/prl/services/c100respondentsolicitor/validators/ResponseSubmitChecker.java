package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            mandatoryFinished = checkConsentManadatoryCompleted(activeRespondentResponse);
        }

        return mandatoryFinished;
    }

    private boolean checkConsentManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<Consent> consent = ofNullable(activeRespondentResponse.get().getValue().getResponse().getConsent());

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

    private boolean checkKeepDetailsPrivateManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<KeepDetailsPrivate> keepDetailsPrivate = ofNullable(activeRespondentResponse.get().getValue().getResponse().getKeepDetailsPrivate());

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

    private boolean checkContactDetailsManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<CitizenDetails> citizenDetails = ofNullable(activeRespondentResponse.get().getValue().getResponse().getCitizenDetails());
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

    private boolean checkAttendToCourtManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<AttendToCourt> attendToCourt = ofNullable(activeRespondentResponse.get().getValue().getResponse().getAttendToCourt());
        if (attendToCourt.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeeds()));
            fields.add(ofNullable(attendToCourt.get().getRespondentWelshNeeds().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentWelshNeedsList() : null));
            fields.add(ofNullable(attendToCourt.get().getIsRespondentNeededInterpreter()));

            Optional<List<Element<RespondentInterpreterNeeds>>> respondentInterpreterNeeds = ofNullable(attendToCourt
                                                                                                            .get()
                                                                                                            .getRespondentInterpreterNeeds());

            List<RespondentInterpreterNeeds> interpreterNeedsList = respondentInterpreterNeeds.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (RespondentInterpreterNeeds interpreterNeed : interpreterNeedsList) {
                fields.add(ofNullable(interpreterNeed.getRelationName()));
                fields.add(ofNullable(interpreterNeed.getParty()));
            }
            fields.add(ofNullable(attendToCourt.get().getRespondentSpecialArrangements()));
            fields.add(ofNullable(attendToCourt.get().getRespondentSpecialArrangements().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentSpecialArrangementDetails() : null));
            fields.add(ofNullable(attendToCourt.get().getHaveAnyDisability()));
            fields.add(ofNullable(attendToCourt.get().getHaveAnyDisability().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getDisabilityNeeds() : null));
            fields.add(ofNullable(attendToCourt.get().getRespondentIntermediaryNeeds()));
            fields.add(ofNullable(attendToCourt.get().getRespondentIntermediaryNeeds().equals(YesOrNo.Yes)
                                      ? null != attendToCourt.get().getRespondentIntermediaryNeedDetails() : null));



            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkMiamManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<Miam> miam = ofNullable(activeRespondentResponse.get().getValue().getResponse().getMiam());
        if (miam.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(miam.get().getAttendedMiam()));
            fields.add(ofNullable(miam.get().getAttendedMiam().equals(YesOrNo.No)
                                      ? null != miam.get().getWillingToAttendMiam() : null));
            fields.add(ofNullable(miam.get().getWillingToAttendMiam().equals(YesOrNo.No)
                                      ? null != miam.get().getWillingToAttendMiam() : null));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkCurrentOrPastProceedingsCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<YesNoDontKnow> currentOrPastProceedings = ofNullable(activeRespondentResponse
                                                                          .get()
                                                                          .getValue()
                                                                          .getResponse()
                                                                          .getCurrentOrPastProceedingsForChildren());
        if (currentOrPastProceedings.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(currentOrPastProceedings.equals(YesNoDontKnow.yes)
                                      ? ofNullable(activeRespondentResponse.get().getValue()
                                                       .getResponse().getRespondentExistingProceedings()) : null));

            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

    private boolean checkAllegationsOfHarmManadatoryCompleted(Optional<Element<PartyDetails>> activeRespondentResponse) {
        Optional<RespondentAllegationsOfHarmData> respondentAllegationsOfHarmData = ofNullable(activeRespondentResponse
                                                                                                   .get()
                                                                                                   .getValue()
                                                                                                   .getResponse()
                                                                                                   .getRespondentAllegationsOfHarmData());
        if (respondentAllegationsOfHarmData.isPresent()) {
            List<Optional<?>> fields = new ArrayList<>();
            fields.add(ofNullable(respondentAllegationsOfHarmData.get().getAllegationsOfHarmYesNo()));
            if (respondentAllegationsOfHarmData.get().getAllegationsOfHarmYesNo().equals(YesOrNo.Yes)) {
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentDrugOrAlcoholAbuse()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm()
                                          .getRespondentDrugOrAlcoholAbuse().equals(YesOrNo.Yes)
                                          ? null != respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm()
                    .getRespondentDrugOrAlcoholAbuseDetails() : null));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentOtherSafetyConcerns()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm()
                                          .getRespondentOtherSafetyConcerns().equals(YesOrNo.Yes)
                                          ? null != respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm()
                    .getRespondentOtherSafetyConcernsDetails() : null));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentNonMolestationOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentOccupationOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentForcedMarriageOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentOtherInjunctiveOrder()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentRestrainingOrder()));
                if (respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentDomesticAbuse().equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentDomesticAbuseBehaviour()));
                }
                if (respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentChildAbuse().equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbuseBehaviour()));
                }
                if (respondentAllegationsOfHarmData.get().getRespondentAllegationsOfHarm().getRespondentChildAbduction().equals(YesOrNo.Yes)) {
                    fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentChildAbduction()));
                }

                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcerns().getChildHavingOtherFormOfContact()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcerns().getChildSpendingSupervisedTime()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcerns().getOrdersRespondentWantFromCourt()));
                fields.add(ofNullable(respondentAllegationsOfHarmData.get().getRespondentOtherConcerns().getChildSpendingUnsupervisedTime()));
            }


            return fields.stream().noneMatch(Optional::isEmpty)
                && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
        }
        return false;
    }

}
