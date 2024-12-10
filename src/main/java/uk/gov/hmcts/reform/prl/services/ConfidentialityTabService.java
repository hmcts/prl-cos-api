package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityTabService {

    private final ObjectMapper objectMapper;

    public Map<String, Object> updateConfidentialityDetails(CaseData caseData) {

        List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails = new ArrayList<>();
        List<Element<ApplicantConfidentialityDetails>> respondentsConfidentialDetails = new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            if (applicantList.isPresent()) {
                List<PartyDetails> applicants = caseData.getApplicants().stream()
                    .map(Element::getValue)
                    .toList();
                applicantsConfidentialDetails = getConfidentialApplicantDetails(
                    applicants);
            }

            List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = getChildrenConfidentialDetails(caseData);

            Optional<List<Element<PartyDetails>>> respondentList = ofNullable(caseData.getRespondents());
            if (respondentList.isPresent()) {
                List<PartyDetails> respondents = caseData.getRespondents().stream()
                    .map(Element::getValue)
                    .toList();
                respondentsConfidentialDetails = getConfidentialApplicantDetails(
                    respondents);
            }

            return Map.of(
                "applicantsConfidentialDetails",
                applicantsConfidentialDetails,
                "childrenConfidentialDetails",
                childrenConfidentialDetails,
                "respondentConfidentialDetails",
                respondentsConfidentialDetails
            );

        } else {
            if (null != caseData.getApplicantsFL401()) {
                List<PartyDetails> fl401Applicant = List.of(caseData.getApplicantsFL401());
                applicantsConfidentialDetails = getConfidentialApplicantDetails(
                    fl401Applicant);
            }

            if (null != caseData.getRespondentsFL401()) {
                List<PartyDetails> fl401Respondent = List.of(caseData.getRespondentsFL401());
                respondentsConfidentialDetails = getConfidentialApplicantDetails(
                    fl401Respondent);
            }

            List<Element<Fl401ChildConfidentialityDetails>> childrenConfidentialDetails = getFl401ChildrenConfidentialDetails(caseData);

            return Map.of(
                "applicantsConfidentialDetails",
                applicantsConfidentialDetails,
                "fl401ChildrenConfidentialDetails",
                childrenConfidentialDetails,
                "respondentConfidentialDetails",
                respondentsConfidentialDetails
            );

        }

    }

    public List<Element<ChildConfidentialityDetails>> getChildrenConfidentialDetails(CaseData caseData) {
        List<Element<ChildConfidentialityDetails>> elementList = new ArrayList<>();
        if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
            Optional<List<Element<ChildDetailsRevised>>> chiildList = ofNullable(caseData.getNewChildDetails());
            if (chiildList.isPresent()) {
                elementList = getChildrenConfidentialDetailsV2(caseData);
            }
        } else {
            Optional<List<Element<Child>>> chiildList = ofNullable(caseData.getChildren());
            if (chiildList.isPresent()) {
                List<Child> children = caseData.getChildren().stream()
                    .map(Element::getValue)
                    .toList();
                elementList =  getChildrenConfidentialDetails(children);
            }
        }
        return elementList;
    }


    public List<Element<ChildConfidentialityDetails>> getChildrenConfidentialDetails(List<Child> children) {
        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
        for (Child child : children) {
            List<Element<OtherPersonConfidentialityDetails>> tempOtherPersonConfidentialDetails = new ArrayList<>();
            List<OtherPersonWhoLivesWithChild> otherPerson =
                child.getPersonWhoLivesWithChild()
                    .stream()
                    .map(Element::getValue)
                    .toList()
                    .stream().filter(other -> other.getIsPersonIdentityConfidential().equals(YesOrNo.Yes))
                    .toList();
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

    public List<Element<ChildConfidentialityDetails>> getChildrenConfidentialDetailsV2(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> otherPersons = ofNullable(caseData.getOtherPartyInTheCaseRevised());
        Map<Object, PartyDetails> objectPartyDetailsMap = new HashMap<>();
        if (otherPersons.isPresent()) {
            List<PartyDetails> partyDetailsList =
                otherPersons.get()
                    .stream()
                    .map(Element::getValue)
                    .toList();
            objectPartyDetailsMap = partyDetailsList.stream()
                .collect(Collectors.toMap(x -> x.getFirstName() + " " + x.getLastName(), Function.identity()));
        }
        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> childrenAndOtherPeopleRelations =
            ofNullable(caseData.getRelations().getChildAndOtherPeopleRelations());
        if (childrenAndOtherPeopleRelations.isPresent()) {
            List<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationList =
                childrenAndOtherPeopleRelations.get()
                    .stream()
                    .map(Element::getValue)
                    .toList()
                    .stream().filter(other -> ofNullable(other.getIsChildLivesWithPersonConfidential()).isPresent()
                        && other.getIsChildLivesWithPersonConfidential().equals(YesOrNo.Yes))
                    .toList();
            Optional<List<Element<ChildDetailsRevised>>> children = ofNullable(caseData.getNewChildDetails());
            List<Element<ChildDetailsRevised>> childDetailsReviseds = new ArrayList<>();
            if (children.isPresent()) {
                List<String> childIds = childrenAndOtherPeopleRelationList.stream()
                    .map(ChildrenAndOtherPeopleRelation::getChildId)
                    .distinct()
                    .toList();
                children.get().stream()
                    .filter(child -> childIds.contains(String.valueOf(child.getId())))
                    .forEach(childDetailsReviseds::add);
            }
            for (Element<ChildDetailsRevised> childDetailsRevisedElement : childDetailsReviseds) {
                //get the matched full name related result from childDetailsRevisedElement full name relation object
                //fix any exceptions for below method
                Optional<ChildrenAndOtherPeopleRelation> optionalChildrenAndOtherPeopleRelation = childrenAndOtherPeopleRelationList
                    .stream()
                    .filter(other -> other.getChildId().equals(String.valueOf(childDetailsRevisedElement.getId())))
                    .findFirst();
                if (optionalChildrenAndOtherPeopleRelation.isPresent()) {
                    ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = optionalChildrenAndOtherPeopleRelation.get();
                    Element<OtherPersonConfidentialityDetails> tempOtherPersonConfidentialDetails =
                        getOtherPersonConfidentialDetails(childrenAndOtherPeopleRelation,objectPartyDetailsMap);
                    ChildDetailsRevised childDetailsRevised = childDetailsRevisedElement.getValue();
                    Element<ChildConfidentialityDetails> childElement = Element
                        .<ChildConfidentialityDetails>builder()
                        .value(ChildConfidentialityDetails.builder()
                                   .firstName(childDetailsRevised.getFirstName())
                                   .lastName(childDetailsRevised.getLastName())
                                   .otherPerson(List.of(tempOtherPersonConfidentialDetails)).build()).build();
                    childrenConfidentialDetails.add(childElement);
                }
            }
        }
        return childrenConfidentialDetails;
    }

    private List<ChildrenAndOtherPeopleRelation> getConfidentialRelationForOtherPeople(
        List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelations) {
        return childrenAndOtherPeopleRelations
            .stream()
            .map(Element::getValue)
            .toList()
            .stream().filter(other -> ofNullable(other.getIsChildLivesWithPersonConfidential()).isPresent()
                && other.getIsChildLivesWithPersonConfidential().equals(YesOrNo.Yes))
            .toList();
    }

    public Element<OtherPersonConfidentialityDetails> getOtherPersonConfidentialDetails(
        ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation, Map<Object, PartyDetails> objectPartyDetailsMap) {
        Optional<PartyDetails> partyDetails = ofNullable(objectPartyDetailsMap.get(
            childrenAndOtherPeopleRelation.getOtherPeopleFullName()));
        if (partyDetails.isPresent()) {
            Element<OtherPersonConfidentialityDetails> otherElement = Element
                .<OtherPersonConfidentialityDetails>builder()
                .value(OtherPersonConfidentialityDetails.builder()
                           .firstName(partyDetails.get().getFirstName())
                           .lastName(partyDetails.get().getLastName())
                           .email(YesOrNo.Yes.equals(childrenAndOtherPeopleRelation.getIsChildLivesWithPersonConfidential())
                                      ? "" : partyDetails.get().getEmail())
                           .phoneNumber(YesOrNo.Yes.equals(childrenAndOtherPeopleRelation.getIsChildLivesWithPersonConfidential())
                                            ? "" : partyDetails.get().getPhoneNumber())
                           .relationshipToChildDetails(childrenAndOtherPeopleRelation
                                                           .getChildAndOtherPeopleRelation().getDisplayedValue())
                           .address(partyDetails.get().getAddress()).build()).build();

            return otherElement;
        }
        return null;
    }

    public List<Element<ApplicantConfidentialityDetails>> getConfidentialApplicantDetails(List<PartyDetails> currentApplicants) {
        List<Element<ApplicantConfidentialityDetails>> tempConfidentialApplicants = new ArrayList<>();
        for (PartyDetails applicant : currentApplicants) {
            boolean addressSet = false;
            boolean emailSet = false;
            boolean phoneSet = false;
            if ((YesOrNo.Yes).equals(applicant.getIsAddressConfidential()) && isNotEmpty(applicant.getAddress())) {
                addressSet = true;
            }
            if ((YesOrNo.Yes).equals(applicant.getIsEmailAddressConfidential()) && isNotEmpty(applicant.getEmail())) {
                emailSet = true;
            }
            if ((YesOrNo.Yes).equals(applicant.getIsPhoneNumberConfidential()) && isNotEmpty(applicant.getPhoneNumber())) {
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

    public List<Element<PartyDetails>> updateOtherPeopleConfidentiality(List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelations,
                                                                        List<Element<PartyDetails>> otherPartyInTheCaseRevised) {
        return ofNullable(otherPartyInTheCaseRevised)
            .map(otherPeople -> {
                List<String> otherPersonIds = ofNullable(childrenAndOtherPeopleRelations)
                    .map(this::getConfidentialRelationForOtherPeople)
                    .orElseGet(ArrayList::new)
                    .stream()
                    .map(ChildrenAndOtherPeopleRelation::getOtherPeopleId)
                    .distinct()
                    .toList();
                List<Element<PartyDetails>> otherPeopleList = new ArrayList<>();
                for (int i = 0; i < otherPeople.size(); i++) {
                    Element<PartyDetails> partyDetails = otherPeople.get(i);
                    if (otherPersonIds.contains(String.valueOf(partyDetails.getId()))) {
                        otherPeopleList.add(Element.<PartyDetails>builder()
                                                .value(partyDetails.getValue().toBuilder()
                                                           .isAddressConfidential(YesOrNo.Yes)
                                                           .isPhoneNumberConfidential(YesOrNo.Yes)
                                                           .isEmailAddressConfidential(YesOrNo.Yes)
                                                           .build())
                                                .id(partyDetails.getId())
                                                .build());
                    } else {
                        otherPeopleList.add(Element.<PartyDetails>builder()
                                                .value(partyDetails.getValue().toBuilder()
                                                           .isAddressConfidential(YesOrNo.No)
                                                           .isPhoneNumberConfidential(YesOrNo.No)
                                                           .isEmailAddressConfidential(YesOrNo.No)
                                                           .build())
                                                .id(partyDetails.getId())
                                                .build());
                    }
                }
                return otherPeopleList;
            })
            .orElseGet(() -> null);
    }
}

