package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.APPLICANTS_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.OTHER;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.allNonEmpty;

@Service
public class ApplicantsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());


        if (applicantsWrapped.isEmpty()) {
            return false;
        }

        boolean allFinished = true;

        List<PartyDetails> applicants = applicantsWrapped.get()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : applicants) {
            Optional<String> dxNumber = ofNullable(applicant.getDxNumber());

            boolean mandatoryCompleted = mandatoryApplicantFieldsAreCompleted(applicant);
            boolean dxCompleted = (dxNumber.isPresent() && !(dxNumber.get().isBlank()));

            if (!(mandatoryCompleted && dxCompleted)) {
                if (mandatoryCompleted) {
                    taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
                }
                else {
                    taskErrorService.addEventError(APPLICANT_DETAILS,
                                                   APPLICANTS_DETAILS_ERROR,
                                                   APPLICANTS_DETAILS_ERROR.getError());
                }
                return false;
            }
        }
        taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getApplicants() != null;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        boolean mandatoryCompleted = false;

        if (applicantsWrapped.isPresent() && applicantsWrapped.get().size() != 0) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails applicant : applicants) {
                mandatoryCompleted = mandatoryApplicantFieldsAreCompleted(applicant);
                if (!mandatoryCompleted) {
                    break;
                }
            }
        }
        if (mandatoryCompleted) {
            taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
            return true;
        }
        taskErrorService.addEventError(APPLICANT_DETAILS,
                                       APPLICANTS_DETAILS_ERROR,
                                       APPLICANTS_DETAILS_ERROR.getError());
        return false;
    }

    private boolean mandatoryApplicantFieldsAreCompleted(PartyDetails applicant) {

        Optional<String> firstName = ofNullable(applicant.getFirstName());
        Optional<String> lastName = ofNullable(applicant.getLastName());
        Optional<LocalDate> dateOfBirth = ofNullable(applicant.getDateOfBirth());
        Optional<Gender> gender = ofNullable(applicant.getGender());
        Optional<String> placeOfBirth = ofNullable(applicant.getPlaceOfBirth());
        Optional<Address> address = ofNullable(applicant.getAddress());
        Optional<YesOrNo> isAddressConfidential = ofNullable(applicant.getIsAddressConfidential());
        Optional<YesOrNo> isAtAddressLessThan5Years = ofNullable(applicant.getIsAtAddressLessThan5Years());
        Optional<String> addressLivedLessThan5YearsDetails = ofNullable(applicant.getAddressLivedLessThan5YearsDetails());
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(applicant.getCanYouProvideEmailAddress());
        Optional<String> email = ofNullable(applicant.getEmail());
        Optional<YesOrNo> isEmailAddressConfidential = ofNullable(applicant.getIsAddressConfidential());
        Optional<String> phoneNumber = ofNullable(applicant.getPhoneNumber());
        Optional<YesOrNo> isPhoneNumberConfidential = ofNullable(applicant.getIsPhoneNumberConfidential());
        Optional<String> representativeFirstName = ofNullable(applicant.getRepresentativeFirstName());
        Optional<String> representativeLastName = ofNullable(applicant.getRepresentativeLastName());
        Optional<String> solicitorEmail = ofNullable(applicant.getSolicitorEmail());
        Optional<Organisation> solicitorOrg = ofNullable(applicant.getSolicitorOrg());
        Optional<Address> solicitorAddress = ofNullable(applicant.getSolicitorAddress());

        List<Optional> fields = new ArrayList<>();
        fields.add(firstName);
        fields.add(lastName);
        fields.add(dateOfBirth);
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(OTHER)) {
            fields.add(ofNullable(applicant.getOtherGender()));
        }
        fields.add(placeOfBirth);
        fields.add(address);
        if (address.isPresent() && !verifyAddressCompleted(address.get())) {
            return false;
        }

        fields.add(isAddressConfidential);
        fields.add(isAtAddressLessThan5Years);
        if (isAtAddressLessThan5Years.isPresent() && isAtAddressLessThan5Years.get().equals(YES)) {
            fields.add(addressLivedLessThan5YearsDetails);
        }
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(YES)) {
            fields.add(email);
            fields.add(isEmailAddressConfidential);
        }
        fields.add(phoneNumber);
        fields.add(isPhoneNumberConfidential);
        fields.add(representativeFirstName);
        fields.add(representativeLastName);
        fields.add(solicitorEmail);
        if (solicitorOrg.isPresent() && (solicitorOrg.get().getOrganisationID() != null)) {
            fields.add(solicitorOrg);
        }
        else {
            if (solicitorAddress.isPresent() && ofNullable(solicitorAddress.get().getAddressLine1()).isEmpty()) {
                return false;
            }
            fields.add(solicitorAddress);
        }

        return fields.stream().noneMatch(Optional::isEmpty);

    }

    private boolean verifyAddressCompleted(Address address) {
        return allNonEmpty(
            address.getAddressLine1(),
            address.getPostTown(),
            address.getCountry(),
            address.getCounty(),
            address.getPostCode()
        );
    }


}


