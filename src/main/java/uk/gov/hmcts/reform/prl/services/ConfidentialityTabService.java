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
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.Fl401ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.OtherPersonConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeConfidentialDocumentsRecord;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeDocumentHandlerParameters;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityTabService {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

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

            List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = getChildrenConfidentialDetails(
                caseData);

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

            List<Element<Fl401ChildConfidentialityDetails>> childrenConfidentialDetails = getFl401ChildrenConfidentialDetails(
                caseData);

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
                elementList = getChildrenConfidentialDetails(children);
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
        List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails = new ArrayList<>();
        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> childrenAndOtherPeopleRelations =
            ofNullable(caseData.getRelations().getChildAndOtherPeopleRelations());
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
        Optional<List<Element<ChildDetailsRevised>>> children = ofNullable(caseData.getNewChildDetails());
        List<ChildDetailsRevised> childDetailsRevisedList = new ArrayList<>();
        if (children.isPresent()) {
            childDetailsRevisedList = children.get()
                .stream()
                .map(Element::getValue)
                .toList();
        }
        if (childrenAndOtherPeopleRelations.isPresent()) {
            List<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationList =
                childrenAndOtherPeopleRelations.get()
                    .stream()
                    .map(Element::getValue)
                    .toList()
                    .stream().filter(other -> ofNullable(other.getIsChildLivesWithPersonConfidential()).isPresent()
                        && other.getIsChildLivesWithPersonConfidential().equals(YesOrNo.Yes))
                    .toList();
            for (ChildDetailsRevised childDetailsRevised : childDetailsRevisedList) {
                List<Element<OtherPersonConfidentialityDetails>> tempOtherPersonConfidentialDetails =
                    getOtherPersonConfidentialDetails(childrenAndOtherPeopleRelationList, objectPartyDetailsMap);
                if (!tempOtherPersonConfidentialDetails.isEmpty()) {
                    Element<ChildConfidentialityDetails> childElement = Element
                        .<ChildConfidentialityDetails>builder()
                        .value(ChildConfidentialityDetails.builder()
                                   .firstName(childDetailsRevised.getFirstName())
                                   .lastName(childDetailsRevised.getLastName())
                                   .otherPerson(tempOtherPersonConfidentialDetails).build()).build();
                    childrenConfidentialDetails.add(childElement);
                }
            }
        }
        return childrenConfidentialDetails;
    }

    public List<Element<OtherPersonConfidentialityDetails>> getOtherPersonConfidentialDetails(
        List<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelationList, Map<Object, PartyDetails> objectPartyDetailsMap) {
        List<Element<OtherPersonConfidentialityDetails>> tempOtherPersonConfidentialDetails = new ArrayList<>();
        for (ChildrenAndOtherPeopleRelation childrenAndOtherPeopleRelation : childrenAndOtherPeopleRelationList) {
            Optional<PartyDetails> partyDetails = ofNullable(objectPartyDetailsMap.get(
                childrenAndOtherPeopleRelation.getOtherPeopleFullName()));
            if (partyDetails.isPresent()) {
                Element<OtherPersonConfidentialityDetails> otherElement = Element
                    .<OtherPersonConfidentialityDetails>builder()
                    .value(OtherPersonConfidentialityDetails.builder()
                               .firstName(partyDetails.get().getFirstName())
                               .lastName(partyDetails.get().getLastName())
                               .email(YesOrNo.Yes.equals(partyDetails.get().getIsEmailAddressConfidential()) ? "" : partyDetails.get().getEmail())
                               .phoneNumber(YesOrNo.Yes.equals(partyDetails.get().getIsPhoneNumberConfidential())
                                                ? "" : partyDetails.get().getPhoneNumber())
                               .relationshipToChildDetails(childrenAndOtherPeopleRelation
                                                               .getChildAndOtherPeopleRelation().getDisplayedValue())
                               .address(partyDetails.get().getAddress()).build()).build();

                tempOtherPersonConfidentialDetails.add(otherElement);
            }
        }
        return tempOtherPersonConfidentialDetails;
    }

    public List<Element<ApplicantConfidentialityDetails>> getConfidentialApplicantDetails(List<PartyDetails> currentApplicants) {
        List<Element<ApplicantConfidentialityDetails>> tempConfidentialApplicants = new ArrayList<>();
        for (PartyDetails applicant : currentApplicants) {
            boolean addressSet = false;
            boolean emailSet = false;
            boolean phoneSet = false;
            if (isNotEmpty(applicant.getAddress())) {
                addressSet = findIsConfidentialField(
                    applicant.getLiveInRefuge(),
                    applicant.getIsAddressConfidential()
                );
            }

            if (isNotEmpty(applicant.getEmail())) {
                emailSet = findIsConfidentialField(
                    applicant.getLiveInRefuge(),
                    applicant.getIsEmailAddressConfidential()
                );
            }

            if (isNotEmpty(applicant.getPhoneNumber())) {
                phoneSet = findIsConfidentialField(
                    applicant.getLiveInRefuge(),
                    applicant.getIsPhoneNumberConfidential()
                );
            }
            if (addressSet || emailSet || phoneSet) {
                tempConfidentialApplicants
                    .add(getApplicantConfidentialityElement(addressSet, emailSet, phoneSet, applicant));
            }
        }

        return tempConfidentialApplicants;
    }

    private boolean findIsConfidentialField(YesOrNo liveInRefuge, YesOrNo isFieldConfidential) {
        if ((YesOrNo.Yes).equals(liveInRefuge)) {
            return true;
        } else {
            return (YesOrNo.Yes).equals(isFieldConfidential);
        }
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

    public void processForcePartiesConfidentialityIfLivesInRefugeForC100(
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        Map<String, Object> updatedCaseData,
        String party,
        boolean cleanUpNeeded) {
        log.info("start processForcePartiesConfidentialityIfLivesInRefuge");
        log.info("party we got now: " + party);
        log.info("cleanUpNeeded we got now: " + cleanUpNeeded);
        if (partyDetailsWrappedList.isPresent() && !partyDetailsWrappedList.get().isEmpty()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream().map(Element::getValue).toList();
            log.info("inside party details list");
            for (PartyDetails partyDetails : partyDetailsList) {
                log.info("Is current address known: " + partyDetails.getIsCurrentAddressKnown());
                if (partyDetails.getIsCurrentAddressKnown() == null
                    || YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                    log.info("inside party details for loop");
                    if ((YesOrNo.Yes.equals(partyDetails.getLiveInRefuge()))
                        || (null != partyDetails.getResponse()
                        && null != partyDetails.getResponse().getCitizenDetails()
                        && YesOrNo.Yes.equals(partyDetails.getResponse().getCitizenDetails().getLiveInRefuge()))) {
                        log.info("says yes to refuge for the party::" + party);
                        forceConfidentialityChangeForRefuge(party, partyDetails);
                    } else if (cleanUpNeeded) {
                        log.info("says no to refuge for the party and clean up is marked as Yes::" + party);
                        partyDetails.setRefugeConfidentialityC8Form(null);
                    }
                } else if (cleanUpNeeded) {
                    log.info("says no to address knows::" + party);
                    partyDetails.setLiveInRefuge(null);
                    partyDetails.setRefugeConfidentialityC8Form(null);
                }
            }
            updatedCaseData.put(party, partyDetailsWrappedList);
        }
        log.info("end processForcePartiesConfidentialityIfLivesInRefuge");
    }

    public void processForcePartiesConfidentialityIfLivesInRefugeForFL401(
        Optional<PartyDetails> optionalPartyDetails,
        Map<String, Object> updatedCaseData,
        String party,
        boolean cleanUpNeeded) {
        log.info("start processForcePartiesConfidentialityIfLivesInRefugeForFL401");
        log.info("party we got now: " + party);
        log.info("cleanUpNeeded we got now: " + cleanUpNeeded);
        if (optionalPartyDetails.isPresent()) {
            PartyDetails partyDetails = optionalPartyDetails.get();
            log.info("inside party details for loop");
            if ((YesOrNo.Yes.equals(partyDetails.getLiveInRefuge()))
                || (null != partyDetails.getResponse()
                && null != partyDetails.getResponse().getCitizenDetails()
                && YesOrNo.Yes.equals(partyDetails.getResponse().getCitizenDetails().getLiveInRefuge()))) {
                log.info("says yes to refuge for the party::" + party);
                forceConfidentialityChangeForRefuge(party, partyDetails);
            } else if (cleanUpNeeded) {
                log.info("says no to refuge for the party and clean up is marked as Yes::" + party);
                partyDetails.setRefugeConfidentialityC8Form(null);
            }
            updatedCaseData.put(party, optionalPartyDetails);
        }
        log.info("end processForcePartiesConfidentialityIfLivesInRefuge");
    }

    private void forceConfidentialityChangeForRefuge(String party, PartyDetails partyDetails) {
        log.info("start forceConfidentialityChangeForRefuge");
        log.info("start forceConfidentialityChangeForRefuge for the party:" + party);
        if (APPLICANTS.equals(party) || FL401_APPLICANTS.equalsIgnoreCase(party)) {
            log.info("setting for applicants");
            partyDetails.setIsAddressConfidential(YesOrNo.Yes);
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
                log.info("current address is known");
                partyDetails.setIsEmailAddressConfidential(YesOrNo.Yes);
            }
            partyDetails.setIsPhoneNumberConfidential(YesOrNo.Yes);
        } else {
            log.info("setting for others");
            if (YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                log.info("current address is known");
                partyDetails.setIsAddressConfidential(YesOrNo.Yes);
            }
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
                log.info("email address is known");
                partyDetails.setIsEmailAddressConfidential(YesOrNo.Yes);
            }
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber())) {
                log.info("phone number is known");
                partyDetails.setIsPhoneNumberConfidential(YesOrNo.Yes);
            }
        }
        log.info("end forceConfidentialityChangeForRefuge");
    }

    public RefugeConfidentialDocumentsRecord listRefugeDocumentsForConfidentialTab(
        CaseData caseData,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        log.info("start listRefugeDocumentsForConfidentialTab");
        List<Element<RefugeConfidentialDocuments>> refugeDocuments
            = caseData.getRefugeDocuments() != null ? caseData.getRefugeDocuments() : new ArrayList<>();
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments
            = caseData.getHistoricalRefugeDocuments() != null ? caseData.getHistoricalRefugeDocuments() : new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab:: " + historicalRefugeDocuments.size());
            if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForApplicant) {
                refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                    refugeDocuments,
                    historicalRefugeDocuments,
                    ofNullable(caseData.getApplicants()),
                    SERVED_PARTY_APPLICANT,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord
                );
            }
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 1111:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 1111:: " + historicalRefugeDocuments.size());
            if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForRespondent) {
                refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                    refugeDocuments,
                    historicalRefugeDocuments,
                    ofNullable(caseData.getRespondents()),
                    SERVED_PARTY_RESPONDENT,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord
                );
            }
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 22222:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 22222:: " + historicalRefugeDocuments.size());
            if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForOtherPeople) {
                refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                    refugeDocuments,
                    historicalRefugeDocuments,
                    ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                    SERVED_PARTY_OTHER,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord
                );
            }
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 33333:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 3333:: " + historicalRefugeDocuments.size());
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 444444:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 44444:: " + historicalRefugeDocuments.size());
            refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForFl401(
                refugeDocuments,
                historicalRefugeDocuments,
                ofNullable(caseData.getApplicantsFL401()),
                SERVED_PARTY_APPLICANT,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 555555:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 5555555:: " + historicalRefugeDocuments.size());
            refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForFl401(
                refugeDocuments,
                historicalRefugeDocuments,
                ofNullable(caseData.getRespondentsFL401()),
                SERVED_PARTY_RESPONDENT,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
        }
        log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 6666666:: " + refugeDocuments.size());
        log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 6666666:: " + historicalRefugeDocuments.size());
        log.info("end listRefugeDocumentsForConfidentialTab");
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord listRefugeDocumentsPartyWiseForC100(
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        String party,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        log.info("start listRefugeDocumentsPartyWise");
        log.info("party we got now: " + party);
        if (partyDetailsWrappedList.isPresent() && !partyDetailsWrappedList.get().isEmpty()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream().map(Element::getValue).toList();
            log.info("inside party details list");
            for (PartyDetails partyDetails : partyDetailsList) {
                log.info("inside party details for loop");
                if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
                    log.info("Yes to refuge");
                    int partyIndex = partyDetailsList.indexOf(partyDetails);
                    String partyType = String.format(party, EMPTY_SPACE_STRING, partyIndex);
                    log.info("partyType found = " + partyType);
                    if (refugeDocumentHandlerParameters.removeDocument
                        || refugeDocumentHandlerParameters.listHistoricalDocument) {
                        findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, partyType);
                    }
                    if (refugeDocumentHandlerParameters.listDocument) {
                        log.info("Now building the new item for the refugeDocuments and current size is " + refugeDocuments.size());
                        refugeDocuments = buildAndListRefugeDocumentsForConfidentialityTab(
                            refugeDocuments,
                            partyDetails,
                            partyType
                        );
                    }
                }
                log.info("historicalRefugeDocuments are now :: " + historicalRefugeDocuments.size());
                log.info("refugeDocuments are now :: " + refugeDocuments.size());
            }
        }
        log.info("end listRefugeDocumentsPartyWise");

        if (refugeConfidentialDocumentsRecord != null) {
            refugeConfidentialDocumentsRecord.refugeDocuments().addAll(refugeDocuments);
            refugeConfidentialDocumentsRecord.historicalRefugeDocuments().addAll(historicalRefugeDocuments);
        } else {
            refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(
                refugeDocuments,
                historicalRefugeDocuments
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private List<Element<RefugeConfidentialDocuments>> buildAndListRefugeDocumentsForConfidentialityTab(
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        PartyDetails partyDetails,
        String partyType) {
        RefugeConfidentialDocuments refugeConfidentialDocuments
            = RefugeConfidentialDocuments
            .builder()
            .partyType(partyType)
            .partyName(partyDetails.getLabelForDynamicList())
            .documentDetails(DocumentDetails.builder()
                                 .documentName(partyDetails.getRefugeConfidentialityC8Form().getDocumentFileName())
                                 .documentUploadedDate(LocalDateTime.now(ZoneId.of(LONDON_TIME_ZONE)).format(
                                     dateTimeFormatter)).build())
            .document(partyDetails.getRefugeConfidentialityC8Form()).build();

        if (refugeDocuments != null) {
            refugeDocuments.add(ElementUtils.element(refugeConfidentialDocuments));
        } else {
            refugeDocuments = new ArrayList<>();
            refugeDocuments.add(ElementUtils.element(refugeConfidentialDocuments));
        }
        return refugeDocuments;
    }

    private void findAndMoveToHistoricalList(List<Element<RefugeConfidentialDocuments>> refugeDocuments,
                                             List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
                                             String partyType) {
        if (refugeDocuments != null && !refugeDocuments.isEmpty()) {
            log.info("refugeDocuments is present and size is " + refugeDocuments.size());
            log.info("historicalRefugeDocuments is present and size is " + historicalRefugeDocuments.size());

            for (Iterator<Element<RefugeConfidentialDocuments>> itr = refugeDocuments.iterator(); itr.hasNext(); ) {
                Element<RefugeConfidentialDocuments> refugeConfidentialDocumentsWrapped = itr.next();
                log.info(
                    "refugeConfidentialDocumentsWrapped is present and now iterating through items, position:: "
                        + refugeDocuments.indexOf(refugeConfidentialDocumentsWrapped));
                if (refugeConfidentialDocumentsWrapped.getValue() != null
                    && partyType.equalsIgnoreCase(refugeConfidentialDocumentsWrapped.getValue().getPartyType())) {
                    log.info("If condition satisfied for party type. doc is present");
                    historicalRefugeDocuments.add(refugeConfidentialDocumentsWrapped);
                    log.info("Added to historical list and now the size is " + historicalRefugeDocuments.size());
                    itr.remove();
                    log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
                }
            }
        }
    }

    private RefugeConfidentialDocumentsRecord listRefugeDocumentsPartyWiseForFl401(
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        Optional<PartyDetails> partyDetailsOptional,
        String party,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord
    ) {
        log.info("start listRefugeDocumentsPartyWise");
        log.info("refugeDocuments are at the start :: " + refugeDocuments.size());
        log.info("historicalRefugeDocuments are at the start :: " + historicalRefugeDocuments.size());
        log.info("party we got now: " + party);
        if (partyDetailsOptional.isPresent()) {
            log.info("inside party details for loop");
            PartyDetails partyDetails = partyDetailsOptional.get();
            if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
                log.info("Yes to refuge");
                if (refugeDocumentHandlerParameters.removeDocument
                    || refugeDocumentHandlerParameters.listHistoricalDocument) {
                    findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, party);
                }
                if (refugeDocumentHandlerParameters.listDocument) {
                    log.info("Now building the new item for the refugeDocuments and current size is " + refugeDocuments.size());
                    buildAndListRefugeDocumentsForConfidentialityTab(
                        refugeDocuments,
                        partyDetails,
                        party
                    );
                }
            }
            log.info("refugeDocuments are now :: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now :: " + historicalRefugeDocuments.size());
        }
        log.info("end listRefugeDocumentsPartyWise");
        if (refugeConfidentialDocumentsRecord != null) {
            refugeConfidentialDocumentsRecord.refugeDocuments().addAll(refugeDocuments);
            refugeConfidentialDocumentsRecord.historicalRefugeDocuments().addAll(historicalRefugeDocuments);
        } else {
            refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(
                refugeDocuments,
                historicalRefugeDocuments
            );
        }
        return refugeConfidentialDocumentsRecord;
    }
}

