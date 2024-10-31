package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.DocumentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeConfidentialDocumentsRecord;
import uk.gov.hmcts.reform.prl.models.refuge.RefugeDocumentHandlerParameters;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_RESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfidentialityC8RefugeService {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

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
                    if (eligibleForRefuge(partyDetails)) {
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

    private boolean eligibleForRefuge(PartyDetails partyDetails) {
        return (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge()))
            || (null != partyDetails.getResponse()
            && null != partyDetails.getResponse().getCitizenDetails()
            && YesOrNo.Yes.equals(partyDetails.getResponse().getCitizenDetails().getLiveInRefuge()));
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
            if (partyDetails.getIsCurrentAddressKnown() == null
                || YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                if (eligibleForRefuge(partyDetails)) {
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
            refugeConfidentialDocumentsRecord = processApplicantsForC100(
                caseData,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 1111:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 1111:: " + historicalRefugeDocuments.size());
            refugeConfidentialDocumentsRecord = processRespondentsForC100(
                caseData,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 22222:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 22222:: " + historicalRefugeDocuments.size());
            refugeConfidentialDocumentsRecord = processOtherPartiesForC100(
                caseData,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 33333:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 3333:: " + historicalRefugeDocuments.size());
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 444444:: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 44444:: " + historicalRefugeDocuments.size());
            if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForApplicant) {
                refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForFl401(
                    refugeDocuments,
                    historicalRefugeDocuments,
                    ofNullable(caseData.getApplicantsFL401()),
                    SERVED_PARTY_APPLICANT,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord
                );
            }
            if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForRespondent) {
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
        }
        log.info("refugeDocuments are now in listRefugeDocumentsForConfidentialTab 6666666:: " + refugeDocuments.size());
        log.info("historicalRefugeDocuments are now in listRefugeDocumentsForConfidentialTab 6666666:: " + historicalRefugeDocuments.size());
        log.info("end listRefugeDocumentsForConfidentialTab");
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processOtherPartiesForC100(
        CaseData caseData,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
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
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processRespondentsForC100(
        CaseData caseData,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
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
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processApplicantsForC100(
        CaseData caseData,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
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
        return refugeConfidentialDocumentsRecord;
    }

    public RefugeConfidentialDocumentsRecord listRefugeDocumentsPartyWiseForC100(
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        String party,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        log.info("start listRefugeDocumentsPartyWise");
        log.info("party we got now: " + party);
        boolean newFileAdded = false;
        if (partyDetailsWrappedList.isPresent() && !partyDetailsWrappedList.get().isEmpty()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream().map(Element::getValue).toList();
            log.info("inside party details list");
            for (PartyDetails partyDetails : partyDetailsList) {
                log.info("inside party details for loop");
                int partyIndex = partyDetailsList.indexOf(partyDetails);
                String partyType = String.format(PrlAppsConstants.FORMAT, party, partyIndex + 1);
                log.info("partyType found = " + partyType);
                if (refugeDocumentHandlerParameters.removeDocument
                    || refugeDocumentHandlerParameters.listHistoricalDocument) {
                    findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, partyType);
                }
                if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
                    log.info("Yes to refuge");
                    if (refugeDocumentHandlerParameters.listDocument) {
                        log.info("Now building the new item for the refugeDocuments and current size is " + refugeDocuments.size());
                        refugeDocuments = buildAndListRefugeDocumentsForConfidentialityTab(
                            refugeDocuments,
                            partyDetails,
                            partyType
                        );
                        newFileAdded = true;
                    }
                }
                log.info("historicalRefugeDocuments are now :: " + historicalRefugeDocuments.size());
                log.info("refugeDocuments are now :: " + refugeDocuments.size());
            }
        }
        log.info("end listRefugeDocumentsPartyWise");

        if (refugeConfidentialDocumentsRecord != null) {
            if (newFileAdded) {
                refugeConfidentialDocumentsRecord.refugeDocuments().addAll(refugeDocuments);
            }
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
        refugeDocuments = addToRefugeDocument(partyType,
            refugeDocuments,
            partyDetails
        );
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
        boolean newFileAdded = false;
        log.info("start listRefugeDocumentsPartyWise");
        log.info("refugeDocuments are at the start :: " + refugeDocuments.size());
        log.info("historicalRefugeDocuments are at the start :: " + historicalRefugeDocuments.size());
        log.info("party we got now: " + party);
        if (partyDetailsOptional.isPresent()) {
            log.info("inside party details for loop");
            PartyDetails partyDetails = partyDetailsOptional.get();
            party = String.format(PrlAppsConstants.FORMAT, party, 1);
            if (refugeDocumentHandlerParameters.removeDocument
                || refugeDocumentHandlerParameters.listHistoricalDocument) {
                findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, party);
            }
            if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
                log.info("Yes to refuge");
                if (refugeDocumentHandlerParameters.listDocument) {
                    log.info("Now building the new item for the refugeDocuments and current size is " + refugeDocuments.size());
                    refugeDocuments = buildAndListRefugeDocumentsForConfidentialityTab(
                        refugeDocuments,
                        partyDetails,
                        party
                    );
                    newFileAdded = true;
                }
            }
            log.info("refugeDocuments are now :: " + refugeDocuments.size());
            log.info("historicalRefugeDocuments are now :: " + historicalRefugeDocuments.size());
        }
        log.info("end listRefugeDocumentsPartyWise");
        if (refugeConfidentialDocumentsRecord != null) {
            if (newFileAdded) {
                refugeConfidentialDocumentsRecord.refugeDocuments().addAll(refugeDocuments);
            }
            refugeConfidentialDocumentsRecord.historicalRefugeDocuments().addAll(historicalRefugeDocuments);
        } else {
            refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(
                refugeDocuments,
                historicalRefugeDocuments
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    public RefugeConfidentialDocumentsRecord processC8RefugeDocumentsOnAmendForC100(CaseData caseDataBefore, CaseData caseData, String eventId) {
        log.info("Inside processC8RefugeDocumentsOnAmendForC100");
        boolean onlyForApplicant = CaseEvent.AMEND_APPLICANTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        boolean onlyForRespondent = CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        boolean onlyForOtherPeople = CaseEvent.AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED.getValue().equalsIgnoreCase(
            eventId);
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord;
        if (onlyForApplicant) {
            log.info("Its for applicant only");
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(true)
                    .build();
            Optional<List<Element<PartyDetails>>> applicantList = ofNullable(caseData.getApplicants());
            Optional<List<Element<PartyDetails>>> applicantListBefore = ofNullable(caseDataBefore.getApplicants());
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForC100(
                caseData,
                applicantList,
                applicantListBefore,
                refugeDocumentHandlerParameters,
                null
            );
        } else if (onlyForRespondent) {
            log.info("Its for respondent only");
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForRespondent(true)
                    .build();
            Optional<List<Element<PartyDetails>>> respondentsList = ofNullable(caseData.getRespondents());
            Optional<List<Element<PartyDetails>>> respondentsListBefore = ofNullable(caseDataBefore.getRespondents());
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForC100(
                caseData,
                respondentsList,
                respondentsListBefore,
                refugeDocumentHandlerParameters,
                null
            );
        } else if (onlyForOtherPeople) {
            log.info("Its for other people only");
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForOtherPeople(true)
                    .build();
            Optional<List<Element<PartyDetails>>> otherPartyList = ofNullable(caseData.getOtherPartyInTheCaseRevised());
            Optional<List<Element<PartyDetails>>> otherPartyListBefore = ofNullable(caseDataBefore.getOtherPartyInTheCaseRevised());
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForC100(
                caseData,
                otherPartyList,
                otherPartyListBefore,
                refugeDocumentHandlerParameters,
                null
            );
        } else {
            log.info("Its for anything else");
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .forAllParties(true)
                    .build();
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForC100OnSubmit(
                caseData,
                ofNullable(caseData.getApplicants()),
                refugeDocumentHandlerParameters,
                null
            );
            processC8RefugeDocumentsChangesForC100OnSubmit(
                caseData,
                ofNullable(caseData.getRespondents()),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
            processC8RefugeDocumentsChangesForC100OnSubmit(
                caseData,
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processC8RefugeDocumentsChangesForC100OnSubmit(
        CaseData caseData,
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        if (partyDetailsWrappedList.isPresent()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream()
                .map(Element::getValue)
                .toList();

            refugeConfidentialDocumentsRecord = compareAndCallService(
                caseData,
                partyDetailsList,
                null,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processC8RefugeDocumentsChangesForC100(
        CaseData caseData,
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        Optional<List<Element<PartyDetails>>> partyDetailsListWrappedBefore,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        if (partyDetailsWrappedList.isPresent()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream()
                .map(Element::getValue)
                .toList();

            if (partyDetailsListWrappedBefore.isPresent()) {
                List<PartyDetails> partyDetailsListBefore = partyDetailsListWrappedBefore.get().stream()
                    .map(Element::getValue)
                    .toList();

                refugeConfidentialDocumentsRecord = compareAndCallService(
                    caseData,
                    partyDetailsList,
                    partyDetailsListBefore,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord
                );
            }
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord compareAndCallService(CaseData caseData,
                                                                    List<PartyDetails> partyDetailsList,
                                                                    List<PartyDetails> partyDetailsListBefore,
                                                                    RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
                                                                    RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        for (PartyDetails partyDetails : partyDetailsList) {
            int index = partyDetailsList.indexOf(partyDetails);
            if (indexExists(partyDetailsListBefore, index)) {
                log.info("Index found. continue");
                PartyDetails partyDetailsBefore = partyDetailsListBefore.get(index);
                refugeConfidentialDocumentsRecord = processLogicalYesNoMapping(
                    caseData,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord,
                    partyDetails,
                    partyDetailsBefore
                );
            } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
                log.info("New Party added. may be submit or resubmission");
                RefugeDocumentHandlerParameters handler =
                    RefugeDocumentHandlerParameters.builder()
                        .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                        .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                        .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                        .forAllParties(refugeDocumentHandlerParameters.forAllParties)
                        .listDocument(true)
                        .removeDocument(false)
                        .listHistoricalDocument(false)
                        .build();
                refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                    caseData,
                    handler,
                    refugeConfidentialDocumentsRecord
                );
            }

        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processLogicalYesNoMapping(
        CaseData caseData,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        PartyDetails partyDetails,
        PartyDetails partyDetailsBefore) {
        if (YesOrNo.Yes.equals(partyDetailsBefore.getIsCurrentAddressKnown())
            && YesOrNo.No.equals(partyDetails.getIsCurrentAddressKnown())) {
            log.info("Current address changed from yes not no. So, refuge status changed from Yes to No");
            RefugeDocumentHandlerParameters handler =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                    .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                    .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                    .listDocument(false)
                    .removeDocument(true)
                    .listHistoricalDocument(true)
                    .build();
            refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                caseData,
                handler,
                refugeConfidentialDocumentsRecord
            );
        } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
            && !YesOrNo.Yes.equals(partyDetailsBefore.getLiveInRefuge())) {
            log.info("Refuge status changed from No to Yes");
            RefugeDocumentHandlerParameters handler =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                    .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                    .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                    .listDocument(true).build();
            refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                caseData,
                handler,
                refugeConfidentialDocumentsRecord
            );
        } else if (!YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
            && YesOrNo.Yes.equals(partyDetailsBefore.getLiveInRefuge())) {
            log.info("Refuge status changed from Yes to No");
            RefugeDocumentHandlerParameters handler =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                    .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                    .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                    .listDocument(false)
                    .removeDocument(true)
                    .listHistoricalDocument(true)
                    .build();
            refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                caseData,
                handler,
                refugeConfidentialDocumentsRecord
            );
        } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
            log.info("Refuge status remained from yes to yes");
            if (partyDetails.getRefugeConfidentialityC8Form() != null
                && partyDetails.getRefugeConfidentialityC8Form().getDocumentFileName() != null
                && partyDetailsBefore.getRefugeConfidentialityC8Form() != null
                && partyDetailsBefore.getRefugeConfidentialityC8Form().getDocumentFileName() != null
                && partyDetails.getRefugeConfidentialityC8Form().getDocumentFileName()
                .equalsIgnoreCase(partyDetailsBefore.getRefugeConfidentialityC8Form().getDocumentFileName())) {
                log.info("Refuge document file name is same, not listing again");
            } else {
                log.info("Refuge document file name is different, listing them");
                RefugeDocumentHandlerParameters handler =
                    RefugeDocumentHandlerParameters.builder()
                        .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                        .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                        .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                        .listDocument(true)
                        .removeDocument(true)
                        .listHistoricalDocument(true)
                        .build();
                refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                    caseData,
                    handler,
                    refugeConfidentialDocumentsRecord
                );
            }
        } else {
            log.info("Refuge status remained same, no to no");
        }
        return refugeConfidentialDocumentsRecord;
    }

    public void processRefugeDocumentsOnSubmit(Map<String, Object> caseDataUpdated,
                                               CaseData caseData) {
        List<Element<RefugeConfidentialDocuments>> refugeDocuments = null;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("Its for case submission");
            String partyType = SERVED_PARTY_APPLICANT;
            refugeDocuments = findAndAddRefugeDocsForC100OnSubmit(
                ofNullable(caseData.getApplicants()),
                partyType,
                refugeDocuments
            );
            partyType = SERVED_PARTY_OTHER;
            refugeDocuments = findAndAddRefugeDocsForC100OnSubmit(
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                partyType,
                refugeDocuments
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("Its for case submission");
            String partyType = SERVED_PARTY_APPLICANT;
            refugeDocuments = findAndAddRefugeDocsForFl401OnSubmit(
                ofNullable(caseData.getApplicantsFL401()),
                partyType,
                refugeDocuments
            );
        }
        if (refugeDocuments != null) {
            caseDataUpdated.put("refugeDocuments", refugeDocuments);
        }
    }

    private List<Element<RefugeConfidentialDocuments>> findAndAddRefugeDocsForC100OnSubmit(
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments) {
        if (partyDetailsWrappedList.isPresent()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream()
                .map(Element::getValue)
                .toList();
            for (PartyDetails partyDetails : partyDetailsList) {
                refugeDocuments = addToRefugeDocument(String.format(PrlAppsConstants.FORMAT, partyType,
                                                                    partyDetailsList.indexOf(partyDetails) + 1
                ), refugeDocuments, partyDetails);
            }
        }
        return refugeDocuments;
    }

    private List<Element<RefugeConfidentialDocuments>> findAndAddRefugeDocsForFl401OnSubmit(
        Optional<PartyDetails> partyDetailsWrapped,
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments) {
        if (partyDetailsWrapped.isPresent()) {
            PartyDetails partyDetails = partyDetailsWrapped.get();
            refugeDocuments = addToRefugeDocument(
                String.format(PrlAppsConstants.FORMAT, partyType, 1),
                refugeDocuments,
                partyDetails
            );
        }
        return refugeDocuments;
    }

    public void processRefugeDocumentsOnReSubmit(Map<String, Object> caseDataUpdated,
                                                 CaseData caseData) {
        List<Element<RefugeConfidentialDocuments>> refugeDocuments = caseData.getRefugeDocuments() != null
            ? caseData.getRefugeDocuments() : new ArrayList<>();
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments = caseData.getHistoricalRefugeDocuments() != null
            ? caseData.getHistoricalRefugeDocuments() : new ArrayList<>();
        log.info("historical list size check 11111" + historicalRefugeDocuments.size());
        log.info("refugeDocuments list size check 11111" + refugeDocuments.size());
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("Its for case submission");
            String partyType = SERVED_PARTY_APPLICANT;
            findAndAddRefugeDocsForC100OnReSubmit(
                ofNullable(caseData.getApplicants()),
                partyType,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("historical list size check 22222" + historicalRefugeDocuments.size());
            log.info("refugeDocuments list size check 2222" + refugeDocuments.size());
            partyType = SERVED_PARTY_OTHER;
            findAndAddRefugeDocsForC100OnReSubmit(
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                partyType,
                refugeDocuments,
                historicalRefugeDocuments
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            log.info("Its for case submission");
            String partyType = SERVED_PARTY_APPLICANT;
            findAndAddRefugeDocsForFl401OnReSubmit(
                ofNullable(caseData.getApplicantsFL401()),
                partyType,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("historical list size check 33333" + historicalRefugeDocuments.size());
            log.info("refugeDocuments list size check 33333" + refugeDocuments.size());
        }
        log.info("historical list size check 44444" + historicalRefugeDocuments.size());
        log.info("refugeDocuments list size check 44444" + refugeDocuments.size());
        caseDataUpdated.put("refugeDocuments", refugeDocuments);
        caseDataUpdated.put("historicalRefugeDocuments", historicalRefugeDocuments);
    }

    public void processRefugeDocumentsC7ResponseSubmission(Map<String, Object> caseDataUpdated,
                                                           PartyDetails partyDetails,
                                                           List<Element<RefugeConfidentialDocuments>> refugeDocuments,
                                                           List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
                                                           int partyIndex) {
        log.info("historical list size check 55555" + historicalRefugeDocuments.size());
        log.info("refugeDocuments list size check 5555" + refugeDocuments.size());
        log.info("Its for response submission");
        String partyType = SERVED_PARTY_RESPONDENT;
        findAndAddRefugeDocsForSolicitorResponseSubmit(
            partyDetails,
            partyType,
            refugeDocuments,
            historicalRefugeDocuments,
            partyIndex
        );
        log.info("historical list size check 666666" + historicalRefugeDocuments.size());
        log.info("refugeDocuments list size check 66666" + refugeDocuments.size());
        caseDataUpdated.put("refugeDocuments", refugeDocuments);
        caseDataUpdated.put("historicalRefugeDocuments", historicalRefugeDocuments);
    }

    private void findAndAddRefugeDocsForSolicitorResponseSubmit(
        PartyDetails partyDetails,
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        int partyIndex) {
        partyType = String.format(PrlAppsConstants.FORMAT, partyType, partyIndex);
        historicalRefugeDocuments = updateHistoricalDocsAndRemoveFromCurrentList(
            partyType,
            refugeDocuments,
            historicalRefugeDocuments
        );
        log.info("Added to historical list and now the size is " + historicalRefugeDocuments.size());
        log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
        refugeDocuments = addToRefugeDocument(
            partyType,
            refugeDocuments,
            partyDetails
        );
        log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
    }

    private void findAndAddRefugeDocsForC100OnReSubmit(
        Optional<List<Element<PartyDetails>>> partyDetailsWrappedList,
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (partyDetailsWrappedList.isPresent()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream()
                .map(Element::getValue)
                .toList();
            for (PartyDetails partyDetails : partyDetailsList) {
                partyType = String.format(PrlAppsConstants.FORMAT,partyType, partyDetailsList.indexOf(partyDetails) + 1);
                historicalRefugeDocuments = updateHistoricalDocsAndRemoveFromCurrentList(partyType, refugeDocuments, historicalRefugeDocuments);
                log.info("Added to historical list and now the size is " + historicalRefugeDocuments.size());
                log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
                refugeDocuments = addToRefugeDocument(
                    partyType,
                    refugeDocuments,
                    partyDetails
                );
                log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
            }
        }
    }

    private void findAndAddRefugeDocsForFl401OnReSubmit(
        Optional<PartyDetails> partyDetailsWrapped,
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (partyDetailsWrapped.isPresent()) {
            partyType = String.format(PrlAppsConstants.FORMAT, partyType, 1);
            historicalRefugeDocuments = updateHistoricalDocsAndRemoveFromCurrentList(
                partyType,
                refugeDocuments,
                historicalRefugeDocuments
            );
            log.info("Added to historical list and now the size is " + historicalRefugeDocuments.size());
            log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
            PartyDetails partyDetails = partyDetailsWrapped.get();
            refugeDocuments = addToRefugeDocument(partyType, refugeDocuments, partyDetails);
            log.info("removed from refugeDocuments and the size is now " + refugeDocuments.size());
        }
    }

    private List<Element<RefugeConfidentialDocuments>> addToRefugeDocument(String partyType,
                                                                           List<Element<RefugeConfidentialDocuments>> refugeDocuments,
                                                                           PartyDetails partyDetails) {
        if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
            && partyDetails.getRefugeConfidentialityC8Form() != null) {
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

            if (refugeDocuments == null) {
                refugeDocuments = new ArrayList<>();
            }
            refugeDocuments.add(ElementUtils.element(refugeConfidentialDocuments));
        }
        return refugeDocuments;
    }

    private static List<Element<RefugeConfidentialDocuments>> updateHistoricalDocsAndRemoveFromCurrentList(
        String partyType,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        for (Iterator<Element<RefugeConfidentialDocuments>> itr = refugeDocuments.iterator(); itr.hasNext(); ) {
            Element<RefugeConfidentialDocuments> refugeConfidentialDocumentsWrapped = itr.next();
            log.info(
                "refugeConfidentialDocumentsWrapped is present and now iterating through items, position:: "
                    + refugeDocuments.indexOf(refugeConfidentialDocumentsWrapped));
            if (refugeConfidentialDocumentsWrapped.getValue() != null
                && partyType.equalsIgnoreCase(refugeConfidentialDocumentsWrapped.getValue().getPartyType())) {
                log.info("If condition satisfied for party type. doc is present");
                if (historicalRefugeDocuments == null) {
                    historicalRefugeDocuments = new ArrayList<>();
                }
                historicalRefugeDocuments.add(refugeConfidentialDocumentsWrapped);
                itr.remove();
            }
        }
        return historicalRefugeDocuments;
    }

    public RefugeConfidentialDocumentsRecord processC8RefugeDocumentsOnAmendForFL401(CaseData caseDataBefore, CaseData caseData, String eventId) {
        boolean onlyForApplicant = CaseEvent.AMEND_APPLICANTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        boolean onlyForRespondent = CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord = null;
        if (onlyForApplicant) {
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(true)
                    .build();
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForFL401(
                caseData,
                ofNullable(caseData.getApplicantsFL401()),
                ofNullable(caseDataBefore.getApplicantsFL401()),
                refugeDocumentHandlerParameters,
                null
            );
        } else if (onlyForRespondent) {
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForRespondent(true)
                    .build();
            refugeConfidentialDocumentsRecord = processC8RefugeDocumentsChangesForFL401(
                caseData,
                ofNullable(caseData.getRespondentsFL401()),
                ofNullable(caseDataBefore.getRespondentsFL401()),
                refugeDocumentHandlerParameters,
                null
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processC8RefugeDocumentsChangesForFL401(
        CaseData caseData,
        Optional<PartyDetails> optionalPartyDetails,
        Optional<PartyDetails> optionalPartyDetailsBefore,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        if (optionalPartyDetails.isPresent() && optionalPartyDetailsBefore.isPresent()) {
            refugeConfidentialDocumentsRecord = compareAndCallService(
                caseData,
                List.of(optionalPartyDetails.get()),
                List.of(optionalPartyDetailsBefore.get()),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    public boolean indexExists(final List<?> list, final int index) {
        return list != null && index >= 0 && index < list.size();
    }
}

