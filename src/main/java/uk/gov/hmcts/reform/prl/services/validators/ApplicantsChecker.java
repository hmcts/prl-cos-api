package uk.gov.hmcts.reform.prl.services.validators;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.APPLICANTS_DETAILS_ERROR;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Service
public class ApplicantsChecker implements EventChecker {

    @Autowired
    TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
            && caseData.getApplicantsFL401() != null) {
            Element<PartyDetails> wrappedPartyDetails = Element.<PartyDetails>builder().value(caseData.getApplicantsFL401()).build();
            applicantsWrapped = ofNullable(Collections.singletonList(wrappedPartyDetails));
        }

        if (applicantsWrapped.isEmpty()) {
            return false;
        }
        List<PartyDetails> applicants = applicantsWrapped.get()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : applicants) {
            Optional<String> dxNumber = ofNullable(applicant.getDxNumber());
            boolean mandatoryCompleted = mandatoryApplicantFieldsAreCompleted(
                applicant,
                caseData.getCaseTypeOfApplication()
            );
            boolean dxCompleted = (dxNumber.isPresent() && !(dxNumber.get().isBlank()));

            if (!(mandatoryCompleted && dxCompleted)) {
                if (mandatoryCompleted) {
                    taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
                } else {
                    taskErrorService.addEventError(
                        APPLICANT_DETAILS,
                        APPLICANTS_DETAILS_ERROR,
                        APPLICANTS_DETAILS_ERROR.getError()
                    );
                }
                return false;
            }
        }
        taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
        return true;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        return (caseData.getCaseTypeOfApplication().equals(FL401_CASE_TYPE)
            ? caseData.getApplicantsFL401() != null
            : caseData.getApplicants() != null);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> applicantsWrapped = Optional.empty();
        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            if (caseData.getApplicantsFL401() != null) {
                Element<PartyDetails> wrappedPartyDetails = Element.<PartyDetails>builder().value(caseData.getApplicantsFL401()).build();
                applicantsWrapped = ofNullable(Collections.singletonList(wrappedPartyDetails));
            }
        } else {
            applicantsWrapped = ofNullable(caseData.getApplicants());
        }

        boolean mandatoryCompleted = false;

        if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (PartyDetails applicant : applicants) {
                mandatoryCompleted = mandatoryApplicantFieldsAreCompleted(
                    applicant,
                    caseData.getCaseTypeOfApplication()
                );
                if (!mandatoryCompleted) {
                    break;
                }
            }
        }
        if (mandatoryCompleted) {
            taskErrorService.removeError(APPLICANTS_DETAILS_ERROR);
            return true;
        }
        taskErrorService.addEventError(
            APPLICANT_DETAILS,
            APPLICANTS_DETAILS_ERROR,
            APPLICANTS_DETAILS_ERROR.getError()
        );
        return false;
    }

    private boolean mandatoryApplicantFieldsAreCompleted(PartyDetails applicant, String caseTypeOfApplication) {
        List<Optional<?>> fields = new ArrayList<>();
        fields.add(ofNullable(applicant.getFirstName()));
        fields.add(ofNullable(applicant.getLastName()));
        fields.add(ofNullable(applicant.getDateOfBirth()));
        Optional<Gender> gender = ofNullable(applicant.getGender());
        fields.add(gender);
        if (gender.isPresent() && gender.get().equals(other)) {
            fields.add(ofNullable(applicant.getOtherGender()));
        }
        if (C100_CASE_TYPE.equals(caseTypeOfApplication)) {
            fields.add(ofNullable(applicant.getPlaceOfBirth()));
        }
        Optional<Address> address = ofNullable(applicant.getAddress());
        fields.add(address);
        if (address.isPresent() && !verifyAddressCompleted(address.get())) {
            return false;
        }
        fields.add(ofNullable(applicant.getIsAddressConfidential()));

        if (C100_CASE_TYPE.equals(caseTypeOfApplication)) {
            Optional<YesOrNo> isAtAddressLessThan5Years = ofNullable(applicant.getIsAtAddressLessThan5Years());
            fields.add(isAtAddressLessThan5Years);
            if (isAtAddressLessThan5Years.isPresent() && isAtAddressLessThan5Years.get().equals(Yes)) {
                fields.add(ofNullable(applicant.getAddressLivedLessThan5YearsDetails()));
            }
        }
        Optional<YesOrNo> canYouProvideEmailAddress = ofNullable(applicant.getCanYouProvideEmailAddress());
        fields.add(canYouProvideEmailAddress);
        if (canYouProvideEmailAddress.isPresent() && canYouProvideEmailAddress.get().equals(Yes)) {
            fields.add(ofNullable(applicant.getEmail()));
            fields.add(ofNullable(applicant.getIsEmailAddressConfidential()));
        }
        fields.add(ofNullable(applicant.getPhoneNumber()));
        fields.add(ofNullable(applicant.getIsPhoneNumberConfidential()));
        fields.add(ofNullable(applicant.getRepresentativeFirstName()));
        fields.add(ofNullable(applicant.getRepresentativeLastName()));
        fields.add(ofNullable(applicant.getSolicitorEmail()));
        if (addSolicitorAddressFields(applicant, fields)) {
            return false;
        }

        return fields.stream().noneMatch(Optional::isEmpty)

            && fields.stream().filter(Optional::isPresent).map(Optional::get).noneMatch(field -> field.equals(""));
    }

    private boolean addSolicitorAddressFields(PartyDetails applicant, List<Optional<?>> fields) {
        Optional<Organisation> solicitorOrg = ofNullable(applicant.getSolicitorOrg());
        if (solicitorOrg.isPresent() && (solicitorOrg.get().getOrganisationID() != null)) {
            fields.add(solicitorOrg);
        } else {
            Optional<Address> solicitorAddress = ofNullable(applicant.getSolicitorAddress());
            if (solicitorAddress.isPresent()
                && (ofNullable(solicitorAddress.get().getAddressLine1()).isEmpty()
                && ofNullable(solicitorAddress.get().getPostCode()).isEmpty())) {
                return true;
            }
            fields.add(solicitorAddress);
        }
        return false;
    }

    public boolean verifyAddressCompleted(Address address) {
        return ofNullable(address.getAddressLine1()).isPresent()
            && ofNullable(address.getPostCode()).isPresent();
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }

}
