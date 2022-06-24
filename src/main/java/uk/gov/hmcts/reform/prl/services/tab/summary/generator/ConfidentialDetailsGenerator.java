package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ConfidentialDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

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
        // Checking the Child details..
        if (validateChildrensDetails(caseData)) {
            return YesOrNo.Yes.getDisplayedValue();
        }
        if (validateApplicantDetails(caseData)) {
            return YesOrNo.Yes.getDisplayedValue();
        }

        return YesOrNo.No.getDisplayedValue();
    }

    private boolean validateChildrensDetails(CaseData caseData) {
        Optional<List<Element<Child>>> childrenWrapped = ofNullable(caseData.getChildren());

        if (childrenWrapped.isPresent() && !childrenWrapped.get().isEmpty()) {
            List<Child> children = childrenWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

            for (Child c : children) {
                if (YesOrNo.Yes.equals(c.getIsChildAddressConfidential())) {
                    return true;
                }
                Optional<List<Element<OtherPersonWhoLivesWithChild>>> otherPersonWrapped = ofNullable(c.getPersonWhoLivesWithChild());
                if (otherPersonWrapped.isPresent() && !otherPersonWrapped.get().isEmpty()) {
                    List<OtherPersonWhoLivesWithChild> otherPersonList = otherPersonWrapped.get()
                        .stream()
                        .map(Element::getValue)
                        .collect(Collectors.toList());
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

    private boolean validateApplicantDetails(CaseData caseData) {
        // Checking the Applicant Details..
        Optional<List<Element<PartyDetails>>> applicantsWrapped = ofNullable(caseData.getApplicants());
        if (!applicantsWrapped.isEmpty() && !applicantsWrapped.get().isEmpty()) {
            List<PartyDetails> applicants = applicantsWrapped.get()
                .stream()
                .map(Element::getValue)
                .collect(Collectors.toList());

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
}
