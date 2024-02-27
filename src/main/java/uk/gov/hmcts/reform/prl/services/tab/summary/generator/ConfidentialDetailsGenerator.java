package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ConfidentialDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Component
public class ConfidentialDetailsGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().confidentialDetails(ConfidentialDetails
                                                             .builder()
                                                             .isConfidentialDetailsAvailable(
                                                                 isConfidentialDetailsAvailable(caseData))
                                                             .build()).build();
    }

    private String isConfidentialDetailsAvailable(CaseData caseData) {

        // Checking the Child details. It is only for C100 applications
        if (C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
            && (validateChildrensDetails(caseData) || validateApplicantForCA(caseData) || validateRespondentConfidentialDetailsCA(caseData))) {
            return YesOrNo.Yes.getDisplayedValue();
        }

        if (FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
            && (validateApplicantForDA(caseData) || validateHomeConfidentialDetails(caseData)
            || validateRespondentConfidentialDetailsDA(caseData))) {
            return YesOrNo.Yes.getDisplayedValue();
        }

        return YesOrNo.No.getDisplayedValue();
    }

    private boolean validateHomeConfidentialDetails(CaseData caseData) {
        Optional<Home> homeOptional = ofNullable(caseData.getHome());
        if (homeOptional.isPresent()) {
            Optional<List<Element<ChildrenLiveAtAddress>>> childrenAddress = ofNullable(homeOptional.get().getChildren());
            if (childrenAddress.isPresent()) {
                List<ChildrenLiveAtAddress> childrenLiveAtAddressList = childrenAddress.get().stream().map(Element::getValue)
                    .toList();
                if (childrenLiveAtAddressList.stream().anyMatch(childAddress -> YesOrNo.Yes.equals(childAddress.getKeepChildrenInfoConfidential()))) {
                    return true;
                }
            }

        }
        return false;

    }

    private boolean validateChildrensDetails(CaseData caseData) {
        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        if (childrenWrapped.isPresent() && !childrenWrapped.get().isEmpty()) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (Child c : children) {
                if (YesOrNo.Yes.equals(c.getIsChildAddressConfidential())) {
                    return true;
                }
                Optional<List<Element<OtherPersonWhoLivesWithChild>>> otherPersonWrapped = ofNullable(c.getPersonWhoLivesWithChild());
                if (otherPersonWrapped.isPresent() && !otherPersonWrapped.get().isEmpty()) {
                    List<OtherPersonWhoLivesWithChild> otherPersonList = otherPersonWrapped.get()
                        .stream()
                        .map(Element::getValue)
                        .toList();
                    boolean isConfidentialDetailsAvaialble = otherPersonList.stream()
                        .anyMatch(eachPerson -> YesOrNo.Yes.equals(eachPerson.getIsPersonIdentityConfidential()));
                    if (isConfidentialDetailsAvaialble) {
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private boolean validateApplicantForDA(CaseData caseData) {
        Optional<PartyDetails> flApplicants = ofNullable(caseData.getApplicantsFL401());
        if (flApplicants.isPresent()) {
            PartyDetails partyDetails = flApplicants.get();
            return partyDetails.hasConfidentialInfo() || isEmailAddressAddressConfidentialForDA(partyDetails);
        }
        return false;
    }

    private boolean validateApplicantForCA(CaseData caseData) {
        // Checking the Applicant Details..
        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails applicant : applicants) {
                if (YesOrNo.Yes.equals(applicant.getIsAddressConfidential())
                    || YesOrNo.Yes.equals(applicant.getIsPhoneNumberConfidential())
                    || YesOrNo.Yes.equals(applicant.getIsEmailAddressConfidential())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmailAddressAddressConfidentialForDA(PartyDetails partyDetails) {
        if (YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
            return YesOrNo.Yes.equals(partyDetails.getIsEmailAddressConfidential());
        }
        return false;
    }

    private boolean validateRespondentConfidentialDetailsCA(CaseData caseData) {
        // Checking the Respondent Details..
        Optional<List<Element<PartyDetails>>> respondentsWrapped = ofNullable(caseData.getRespondents());

        if (!respondentsWrapped.isEmpty() && !respondentsWrapped.get().isEmpty()) {
            List<PartyDetails> respondents = respondentsWrapped.get()
                .stream()
                .map(Element::getValue)
                .toList();

            for (PartyDetails respondent : respondents) {
                if (YesOrNo.Yes.equals(respondent.getIsAddressConfidential())
                    || YesOrNo.Yes.equals(respondent.getIsPhoneNumberConfidential())
                    || YesOrNo.Yes.equals(respondent.getIsEmailAddressConfidential())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateRespondentConfidentialDetailsDA(CaseData caseData) {
        // Checking the Respondent Details..
        Optional<PartyDetails> flRespondents = ofNullable(caseData.getRespondentsFL401());
        if (flRespondents.isPresent()) {
            PartyDetails partyDetails = flRespondents.get();
            if (YesOrNo.Yes.equals(partyDetails.getIsAddressConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsPhoneNumberConfidential())
                || YesOrNo.Yes.equals(partyDetails.getIsEmailAddressConfidential())) {
                return true;
            }

        }
        return false;
    }

}
