package uk.gov.hmcts.reform.prl.services;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS_CONFIDENTIAL_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CHILDREN_CONFIDENTIAL_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CHILDREN_CONFIDENTIAL_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_CONFIDENTIAL_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_CONFIDENTIAL_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityTabService {

    private final ConfidentialityC8RefugeService confidentialityC8RefugeService;

    record ConfidentialFields(boolean address, boolean emailAddress, boolean phoneNumber) {
        boolean isConfidentialFieldSet() {
            return address || emailAddress || phoneNumber;
        }
    }

    public Map<String, Object> updateConfidentialityDetails(CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return getC100ConfidentialityDetails(caseData);
        } else {
            return getFL401ConfidentialityDetails(caseData);
        }
    }

    public List<Element<ChildConfidentialityDetails>> getChildrenConfidentialDetails(CaseData caseData) {
        List<Element<ChildConfidentialityDetails>> elementList = new ArrayList<>();
        if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
            Optional<List<Element<ChildDetailsRevised>>> childList = ofNullable(caseData.getNewChildDetails());
            if (childList.isPresent()) {
                elementList = getChildrenConfidentialDetailsV2(caseData);
            }
        } else {
            Optional<List<Element<Child>>> childList = ofNullable(caseData.getChildren());
            if (childList.isPresent()) {
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
            List<Element<ChildDetailsRevised>> childDetailsRevisedList = new ArrayList<>();
            if (children.isPresent()) {
                List<String> childIds = childrenAndOtherPeopleRelationList.stream()
                    .map(ChildrenAndOtherPeopleRelation::getChildId)
                    .distinct()
                    .toList();
                children.get().stream()
                    .filter(child -> childIds.contains(String.valueOf(child.getId())))
                    .forEach(childDetailsRevisedList::add);
            }
            for (Element<ChildDetailsRevised> childDetailsRevisedElement : childDetailsRevisedList) {
                //get the matched full name related result from childDetailsRevisedElement full name relation object
                //fix any exceptions for below method
                Optional<ChildrenAndOtherPeopleRelation> optionalChildrenAndOtherPeopleRelation = childrenAndOtherPeopleRelationList
                    .stream()
                    .filter(other -> other.getChildId().equals(String.valueOf(childDetailsRevisedElement.getId())))
                    .findFirst();
                if (optionalChildrenAndOtherPeopleRelation.isPresent()) {
                    ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation = optionalChildrenAndOtherPeopleRelation.get();
                    Element<OtherPersonConfidentialityDetails> tempOtherPersonConfidentialDetails =
                        getOtherPersonConfidentialDetails(childrenAndOtherPeopleRelation, objectPartyDetailsMap);
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
        return partyDetails.map(details -> Element.<OtherPersonConfidentialityDetails>builder()
            .value(OtherPersonConfidentialityDetails.builder()
                       .firstName(details.getFirstName())
                       .lastName(details.getLastName())
                       .previousName(details.getPreviousName())
                       .relationshipToChildDetails(childrenAndOtherPeopleRelation
                                                       .getChildAndOtherPeopleRelation().getDisplayedValue())
                       .gender(details.getGender())
                       .dateOfBirth(details.getDateOfBirth())
                       .address(details.getAddress())
                       .addressLivedLessThan5YearsDetails(details.getAddressLivedLessThan5YearsDetails())
                       .email(details.getEmail())
                       .phoneNumber(details.getPhoneNumber())
                       .build())
            .build())
            .orElse(null);
    }

    public List<Element<ApplicantConfidentialityDetails>> getConfidentialApplicantDetails(List<PartyDetails> currentApplicants) {
        List<Element<ApplicantConfidentialityDetails>> tempConfidentialApplicants = new ArrayList<>();
        for (PartyDetails applicant : currentApplicants) {

            ConfidentialFields confidentialFields;
            if (YesOrNo.Yes.equals(applicant.getIsPartyIdentityConfidential())) {
                confidentialFields = new ConfidentialFields(true, true, true);
            } else {
                confidentialFields = getConfidentialFields(applicant);
            }

            if (confidentialFields.isConfidentialFieldSet()) {
                tempConfidentialApplicants.add(getApplicantConfidentialityElement(confidentialFields, applicant));
            }
        }

        return tempConfidentialApplicants;
    }

    private ConfidentialFields getConfidentialFields(PartyDetails partyDetails) {
        boolean addressSet = false;
        boolean emailSet = false;
        boolean phoneSet = false;
        if (isNotEmpty(partyDetails.getAddress())) {
            addressSet = findIsConfidentialField(
                partyDetails.getLiveInRefuge(),
                partyDetails.getIsAddressConfidential()
            );
        }

        if (isNotEmpty(partyDetails.getEmail())) {
            emailSet = findIsConfidentialField(
                partyDetails.getLiveInRefuge(),
                partyDetails.getIsEmailAddressConfidential()
            );
        }

        if (isNotEmpty(partyDetails.getPhoneNumber())) {
            phoneSet = findIsConfidentialField(
                partyDetails.getLiveInRefuge(),
                partyDetails.getIsPhoneNumberConfidential()
            );
        }

        return new ConfidentialFields(addressSet, emailSet, phoneSet);
    }

    private boolean findIsConfidentialField(YesOrNo liveInRefuge, YesOrNo isFieldConfidential) {
        if ((YesOrNo.Yes).equals(liveInRefuge)) {
            return true;
        } else {
            return (YesOrNo.Yes).equals(isFieldConfidential);
        }
    }

    private Element<ApplicantConfidentialityDetails> getApplicantConfidentialityElement(ConfidentialFields confidentialFields,
                                                                                        PartyDetails applicant) {

        return Element
            .<ApplicantConfidentialityDetails>builder()
            .value(ApplicantConfidentialityDetails.builder()
                       .firstName(applicant.getFirstName())
                       .lastName(applicant.getLastName())
                       .address(confidentialFields.address ? applicant.getAddress() : null)
                       .phoneNumber(confidentialFields.phoneNumber ? applicant.getPhoneNumber() : null)
                       .email(confidentialFields.emailAddress ? applicant.getEmail() : null)
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
                if (child != null && YesOrNo.Yes.equals(child.getKeepChildrenInfoConfidential())) {
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
        updateOtherPeopleIdentityConfidentiality(childrenAndOtherPeopleRelations, otherPartyInTheCaseRevised);

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
                                                           .isPartyIdentityConfidential(YesOrNo.Yes)
                                                           .build())
                                                .id(partyDetails.getId())
                                                .build());
                    } else {
                        PartyDetails updatedPartyDetails = partyDetails.getValue().toBuilder().build();
                        confidentialityC8RefugeService.updateConfidentialityForPartiesLivingInRefuge("otherPeople",
                                                                                                     updatedPartyDetails,
                                                                                                     false);
                        otherPeopleList.add(Element.<PartyDetails>builder()
                                                .value(updatedPartyDetails)
                                                .id(partyDetails.getId())
                                                .build());
                    }
                }
                return otherPeopleList;
            })
            .orElse(null);
    }

    private void updateOtherPeopleIdentityConfidentiality(List<Element<ChildrenAndOtherPeopleRelation>> childrenAndOtherPeopleRelations,
                                                          List<Element<PartyDetails>> otherPartyInTheCaseRevised) {

        Optional.ofNullable(otherPartyInTheCaseRevised).ifPresent(parties -> parties
            .forEach(element -> {
                boolean identityConfidential = Optional.ofNullable(childrenAndOtherPeopleRelations)
                    .map(list -> list.stream()
                        .map(Element::getValue)
                        .filter(r -> r.getOtherPeopleId().equals(element.getId().toString()))
                        .anyMatch(r -> YesOrNo.Yes.equals(r.getIsOtherPeopleIdConfidential())))
                    .orElse(false);
                element.getValue().setIsPartyIdentityConfidential(identityConfidential ?  YesOrNo.Yes : YesOrNo.No);
            }));
    }

    private Map<String, Object> getC100ConfidentialityDetails(CaseData caseData) {
        List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails = getApplicantConfidentialDetails(caseData);
        List<Element<ApplicantConfidentialityDetails>> respondentsConfidentialDetails = getRespondentConfidentialDetails(caseData);
        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = getChildrenConfidentialDetails(caseData);
        List<Element<ApplicantConfidentialityDetails>> otherPeopleConfidentialDetails = getOtherPeopleConfidentialDetails(caseData);

        return Map.of(
            APPLICANTS_CONFIDENTIAL_DETAILS, applicantsConfidentialDetails,
            C100_CHILDREN_CONFIDENTIAL_DETAILS, childrenConfidentialDetails,
            RESPONDENT_CONFIDENTIAL_DETAILS, respondentsConfidentialDetails,
            OTHER_PEOPLE_CONFIDENTIAL_DETAILS, otherPeopleConfidentialDetails
        );
    }

    private Map<String, Object> getFL401ConfidentialityDetails(CaseData caseData) {
        List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails = new ArrayList<>();
        List<Element<ApplicantConfidentialityDetails>> respondentsConfidentialDetails = new ArrayList<>();

        if (null != caseData.getApplicantsFL401()) {
            List<PartyDetails> fl401Applicant = List.of(caseData.getApplicantsFL401());
            applicantsConfidentialDetails = getConfidentialApplicantDetails(fl401Applicant);
        }

        if (null != caseData.getRespondentsFL401()) {
            List<PartyDetails> fl401Respondent = List.of(caseData.getRespondentsFL401());
            respondentsConfidentialDetails = getConfidentialApplicantDetails(
                fl401Respondent);
        }

        List<Element<Fl401ChildConfidentialityDetails>> childrenConfidentialDetails = getFl401ChildrenConfidentialDetails(caseData);

        return Map.of(
            APPLICANTS_CONFIDENTIAL_DETAILS, applicantsConfidentialDetails,
            FL401_CHILDREN_CONFIDENTIAL_DETAILS, childrenConfidentialDetails,
            RESPONDENT_CONFIDENTIAL_DETAILS, respondentsConfidentialDetails
        );
    }

    private List<Element<ApplicantConfidentialityDetails>> getApplicantConfidentialDetails(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
        if (applicantList.isPresent()) {
            List<PartyDetails> applicants = caseData.getApplicants().stream()
                .map(Element::getValue)
                .toList();
            return getConfidentialApplicantDetails(applicants);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Element<ApplicantConfidentialityDetails>> getRespondentConfidentialDetails(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> respondentList = ofNullable(caseData.getRespondents());
        if (respondentList.isPresent()) {
            List<PartyDetails> respondents = caseData.getRespondents().stream()
                .map(Element::getValue)
                .toList();
            return getConfidentialApplicantDetails(respondents);
        } else {
            return Collections.emptyList();
        }
    }

    private List<Element<ApplicantConfidentialityDetails>> getOtherPeopleConfidentialDetails(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> otherPeopleList = ofNullable(caseData.getOtherPartyInTheCaseRevised());

        if (otherPeopleList.isPresent()) {
            List<PartyDetails> otherPeople = caseData.getOtherPartyInTheCaseRevised().stream()
                .map(Element::getValue)
                .toList();
            return getConfidentialApplicantDetails(otherPeople);
        } else {
            return Collections.emptyList();
        }
    }
}
