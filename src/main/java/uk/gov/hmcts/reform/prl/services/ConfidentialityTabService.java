package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.Fl401ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityTabService {

    public Map<String, Object> updateConfidentialityDetails(CaseData caseData) {

        List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails = new ArrayList<>();
        List<Element<ApplicantConfidentialityDetails>> respondentsConfidentialDetails = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                List<PartyDetails> applicants = caseData.getApplicants().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                applicantsConfidentialDetails = getConfidentialApplicantDetails(
                    applicants);
            }
            Optional<List<Element<Child>>> chiildList = ofNullable(caseData.getChildren());
            if (chiildList.isPresent()) {
                List<Child> children = caseData.getChildren().stream()
                    .map(Element::getValue)
                    .collect(Collectors.toList());
                childrenConfidentialDetails = getChildrenConfidentialDetails(children);
            }

            respondentsConfidentialDetails = caseData.getRespondentConfidentialDetails();

            return Map.of(
                "applicantsConfidentialDetails",
                applicantsConfidentialDetails,
                "childrenConfidentialDetails",
                childrenConfidentialDetails,
                "respondentsConfidentialDetails",
                respondentsConfidentialDetails
            );

        } else {
            if (null != caseData.getApplicantsFL401()) {
                List<PartyDetails> fl401Applicant = List.of(caseData.getApplicantsFL401());
                applicantsConfidentialDetails = getConfidentialApplicantDetails(
                    fl401Applicant);
            }

            List<Element<Fl401ChildConfidentialityDetails>> childrenConfidentialDetails = getFl401ChildrenConfidentialDetails(caseData);

            respondentsConfidentialDetails = caseData.getRespondentConfidentialDetails();

            return Map.of(
                "applicantsConfidentialDetails",
                applicantsConfidentialDetails,
                "fl401ChildrenConfidentialDetails",
                childrenConfidentialDetails,
                "respondentsConfidentialDetails",
                respondentsConfidentialDetails
            );

        }

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

    public List<Element<Fl401ChildConfidentialityDetails>> getFl401ChildrenConfidentialDetails(CaseData caseData) {
        List<Element<Fl401ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
        Optional<TypeOfApplicationOrders> typeOfApplicationOrders = ofNullable(caseData.getTypeOfApplicationOrders());
        if (typeOfApplicationOrders.isPresent() && ofNullable(typeOfApplicationOrders.get().getOrderType()).isPresent()
            && !typeOfApplicationOrders.get().getOrderType().isEmpty()
            && typeOfApplicationOrders.get().getOrderType().contains(occupationOrder)
            && ofNullable(caseData.getHome()).isPresent() && ofNullable(caseData.getHome().getChildren()).isPresent()) {
            List<ChildrenLiveAtAddress> children = unwrapElements(caseData.getHome().getChildren());
            for (ChildrenLiveAtAddress child : children) {
                if (child.getKeepChildrenInfoConfidential().equals(YesOrNo.Yes)) {
                    Element<Fl401ChildConfidentialityDetails> childElement = Element
                        .<Fl401ChildConfidentialityDetails>builder()
                        .value(Fl401ChildConfidentialityDetails.builder()
                                   .fullName(child.getChildFullName()).build()).build();
                    childrenConfidentialDetails.add(childElement);
                }
            }
        }

        return childrenConfidentialDetails;
    }

}

