package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityTabService {

    private final CoreCaseDataService coreCaseDataService;


    public boolean updateConfidentialityDetails(Long id, CaseData caseData) {
        log.info("Updating ConfidentialityDetails");

        List<PartyDetails> applicants = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails = getConfidentialApplicantDetails(
            applicants);
        List<Child> children = caseData.getChildren().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = getChildrenConfidentialDetails(children);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            id,
            "internal-update-application-tab",
            Map.of(
                "applicantsConfidentialDetails",
                applicantsConfidentialDetails,
                "childrenConfidentialDetails",
                childrenConfidentialDetails
            )
        );

        return true;

    }

    public List<Element<ChildConfidentialityDetails>> getChildrenConfidentialDetails(List<Child> children) {
        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
        for (Child child : children) {
            List<Element<OtherPersonConfidentialityDetails>> tempOtherPersonConfidentialDetails = new ArrayList<>();
            List<OtherPersonWhoLivesWithChild> otherPerson =
                child.getPersonWhoLivesWithChild()
                    .stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList())
                    .stream().filter(other -> other.getIsPersonIdentityConfidential().equals(YesOrNo.Yes))
                    .collect(Collectors.toList());
            for (OtherPersonWhoLivesWithChild otherPersonWhoLivesWithChild : otherPerson) {
                Element<OtherPersonConfidentialityDetails> otherElement = Element
                    .<OtherPersonConfidentialityDetails>builder()
                    .value(OtherPersonConfidentialityDetails.builder()
                               .firstName(otherPersonWhoLivesWithChild.getFirstName())
                               .lastName(otherPersonWhoLivesWithChild.getLastName())
                               .relationshipToChildDetails(otherPersonWhoLivesWithChild.getRelationshipToChildDetails())
                               .address(otherPersonWhoLivesWithChild.getAddress()).build()).build();
                tempOtherPersonConfidentialDetails.add(otherElement);
            }
            if (!tempOtherPersonConfidentialDetails.isEmpty()) {
                Element<ChildConfidentialityDetails> childElement = Element
                    .<ChildConfidentialityDetails>builder()
                    .value(ChildConfidentialityDetails.builder()
                               .firstName(child.getFirstName())
                               .lastName(child.getLastName())
                               .otherPerson(tempOtherPersonConfidentialDetails).build()).build();
                childrenConfidentialDetails.add(childElement);
            }

        }
        return childrenConfidentialDetails;
    }

    public List<Element<ApplicantConfidentialityDetails>> getConfidentialApplicantDetails(List<PartyDetails> currentApplicants) {
        List<Element<ApplicantConfidentialityDetails>> tempConfidentialApplicants = new ArrayList<>();
        for (PartyDetails applicant : currentApplicants) {
            boolean addressSet = false;
            boolean emailSet = false;
            boolean phoneSet = false;
            if ((YesOrNo.Yes).equals(applicant.getIsAddressConfidential())) {
                addressSet = true;
            }
            if ((YesOrNo.Yes).equals(applicant.getIsEmailAddressConfidential())) {
                emailSet = true;
            }
            if ((YesOrNo.Yes).equals(applicant.getIsPhoneNumberConfidential())) {
                phoneSet = true;
            }
            if (addressSet || emailSet || phoneSet) {
                tempConfidentialApplicants
                    .add(getApplicantConfidentialityElement(addressSet, emailSet, phoneSet, applicant));
            }
        }

        return tempConfidentialApplicants;
    }

    private Element<ApplicantConfidentialityDetails> getApplicantConfidentialityElement(boolean addressSet,
              boolean emailSet, boolean phoneSet, PartyDetails applicant) {

        return Element
            .<ApplicantConfidentialityDetails>builder()
            .value(ApplicantConfidentialityDetails.builder()
                       .firstName(applicant.getFirstName())
                       .lastName(applicant.getLastName())
                       .address(addressSet ? applicant.getAddress() : null)
                       .phoneNumber(phoneSet ? applicant.getPhoneNumber() : null)
                       .email(emailSet ? applicant.getEmail() : null)
                       .build()).build();
    }

}
