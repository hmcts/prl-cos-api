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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HISTORICAL_REFUGE_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REFUGE_DOCUMENTS;
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
        if (partyDetailsWrappedList.isPresent() && !partyDetailsWrappedList.get().isEmpty()) {
            List<PartyDetails> partyDetailsList = partyDetailsWrappedList.get().stream().map(Element::getValue).toList();
            for (PartyDetails partyDetails : partyDetailsList) {
                if (partyDetails.getIsCurrentAddressKnown() == null
                    || YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                    if (eligibleForRefuge(partyDetails)) {
                        forceConfidentialityChangeForRefuge(party, partyDetails);
                    } else if (cleanUpNeeded) {
                        partyDetails.setRefugeConfidentialityC8Form(null);
                    }
                } else if (cleanUpNeeded) {
                    partyDetails.setLiveInRefuge(null);
                    partyDetails.setRefugeConfidentialityC8Form(null);
                }
            }
            updatedCaseData.put(party, partyDetailsWrappedList);
        }
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
        if (optionalPartyDetails.isPresent()) {
            PartyDetails partyDetails = optionalPartyDetails.get();
            if (partyDetails.getIsCurrentAddressKnown() == null
                || YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                if (eligibleForRefuge(partyDetails)) {
                    forceConfidentialityChangeForRefuge(party, partyDetails);
                } else if (cleanUpNeeded) {
                    partyDetails.setRefugeConfidentialityC8Form(null);
                }
            } else if (cleanUpNeeded) {
                partyDetails.setLiveInRefuge(null);
                partyDetails.setRefugeConfidentialityC8Form(null);
            }
            updatedCaseData.put(party, optionalPartyDetails);
        }
    }

    private void forceConfidentialityChangeForRefuge(String party, PartyDetails partyDetails) {
        if (APPLICANTS.equals(party) || FL401_APPLICANTS.equalsIgnoreCase(party)) {
            partyDetails.setIsAddressConfidential(YesOrNo.Yes);
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
                partyDetails.setIsEmailAddressConfidential(YesOrNo.Yes);
            }
            partyDetails.setIsPhoneNumberConfidential(YesOrNo.Yes);
        } else {
            if (YesOrNo.Yes.equals(partyDetails.getIsCurrentAddressKnown())) {
                partyDetails.setIsAddressConfidential(YesOrNo.Yes);
            }
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
                partyDetails.setIsEmailAddressConfidential(YesOrNo.Yes);
            }
            if (YesOrNo.Yes.equals(partyDetails.getCanYouProvidePhoneNumber())) {
                partyDetails.setIsPhoneNumberConfidential(YesOrNo.Yes);
            }
        }
    }

    public RefugeConfidentialDocumentsRecord listRefugeDocumentsForConfidentialTab(
        CaseData caseData,
        PartyDetails partyDetails,
        int partyIndex,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord) {
        List<Element<RefugeConfidentialDocuments>> refugeDocuments
            = caseData.getRefugeDocuments() != null ? caseData.getRefugeDocuments() : new ArrayList<>();
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments
            = caseData.getHistoricalRefugeDocuments() != null ? caseData.getHistoricalRefugeDocuments() : new ArrayList<>();

        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            refugeConfidentialDocumentsRecord = processApplicantsForC100(
                partyDetails,
                partyIndex,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
            refugeConfidentialDocumentsRecord = processRespondentsForC100(
                partyDetails,
                partyIndex,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
            refugeConfidentialDocumentsRecord = processOtherPartiesForC100(
                partyDetails,
                partyIndex,
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                refugeDocuments,
                historicalRefugeDocuments
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
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
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processOtherPartiesForC100(
        PartyDetails partyDetails,
        int partyIndex,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForOtherPeople) {
            refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                refugeDocuments,
                historicalRefugeDocuments,
                String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_OTHER, partyIndex + 1),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                partyDetails
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processRespondentsForC100(
        PartyDetails partyDetails,
        int partyIndex,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForRespondent) {
            refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                refugeDocuments,
                historicalRefugeDocuments,
                String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_RESPONDENT, partyIndex + 1),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                partyDetails
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    private RefugeConfidentialDocumentsRecord processApplicantsForC100(
        PartyDetails partyDetails,
        int partyIndex,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (refugeDocumentHandlerParameters.forAllParties || refugeDocumentHandlerParameters.onlyForApplicant) {
            refugeConfidentialDocumentsRecord = listRefugeDocumentsPartyWiseForC100(
                refugeDocuments,
                historicalRefugeDocuments,
                String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_APPLICANT, partyIndex + 1),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord,
                partyDetails
            );
        }
        return refugeConfidentialDocumentsRecord;
    }

    public RefugeConfidentialDocumentsRecord listRefugeDocumentsPartyWiseForC100(
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        String party,
        RefugeDocumentHandlerParameters refugeDocumentHandlerParameters,
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord,
        PartyDetails partyDetails) {
        boolean newFileAdded = false;
        if (refugeDocumentHandlerParameters.removeDocument
            || refugeDocumentHandlerParameters.listHistoricalDocument) {
            findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, party);
        }
        if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge()) && refugeDocumentHandlerParameters.listDocument) {
            refugeDocuments = buildAndListRefugeDocumentsForConfidentialityTab(
                refugeDocuments,
                partyDetails,
                party
            );
            newFileAdded = true;

        }

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
        refugeDocuments = addToRefugeDocument(
            partyType,
            refugeDocuments,
            partyDetails
        );
        return refugeDocuments;
    }

    private void findAndMoveToHistoricalList(List<Element<RefugeConfidentialDocuments>> refugeDocuments,
                                             List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
                                             String partyType) {
        if (refugeDocuments != null && !refugeDocuments.isEmpty()) {
            for (Iterator<Element<RefugeConfidentialDocuments>> itr = refugeDocuments.iterator(); itr.hasNext(); ) {
                Element<RefugeConfidentialDocuments> refugeConfidentialDocumentsWrapped = itr.next();
                if (refugeConfidentialDocumentsWrapped.getValue() != null
                    && partyType.equalsIgnoreCase(refugeConfidentialDocumentsWrapped.getValue().getPartyType())) {
                    historicalRefugeDocuments.add(refugeConfidentialDocumentsWrapped);
                    itr.remove();
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
        if (partyDetailsOptional.isPresent()) {
            PartyDetails partyDetails = partyDetailsOptional.get();
            String partyType = String.format(PrlAppsConstants.FORMAT, party, 1);
            if (refugeDocumentHandlerParameters.removeDocument
                || refugeDocumentHandlerParameters.listHistoricalDocument) {
                findAndMoveToHistoricalList(refugeDocuments, historicalRefugeDocuments, partyType);
            }
            if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge()) && refugeDocumentHandlerParameters.listDocument) {
                refugeDocuments = buildAndListRefugeDocumentsForConfidentialityTab(
                    refugeDocuments,
                    partyDetails,
                    partyType
                );
                newFileAdded = true;
            }
        }
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
        boolean onlyForApplicant = CaseEvent.AMEND_APPLICANTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        boolean onlyForRespondent = CaseEvent.AMEND_RESPONDENTS_DETAILS.getValue().equalsIgnoreCase(eventId);
        boolean onlyForOtherPeople = CaseEvent.AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED.getValue().equalsIgnoreCase(
            eventId);
        RefugeConfidentialDocumentsRecord refugeConfidentialDocumentsRecord;
        if (onlyForApplicant) {
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
            RefugeDocumentHandlerParameters refugeDocumentHandlerParameters =
                RefugeDocumentHandlerParameters.builder()
                    .forAllParties(true)
                    .build();

            refugeConfidentialDocumentsRecord = new RefugeConfidentialDocumentsRecord(new ArrayList<>(), new ArrayList<>());

            processC8RefugeDocumentsChangesForC100OnSubmit(
                caseData,
                ofNullable(caseData.getApplicants()),
                refugeDocumentHandlerParameters,
                refugeConfidentialDocumentsRecord
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

    public RefugeConfidentialDocumentsRecord processC8RefugeDocumentsChangesForC100(
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
                PartyDetails partyDetailsBefore = partyDetailsListBefore.get(index);
                refugeConfidentialDocumentsRecord = processLogicalYesNoMapping(
                    caseData,
                    refugeDocumentHandlerParameters,
                    refugeConfidentialDocumentsRecord,
                    partyDetails,
                    partyDetailsBefore,
                    index
                );
            } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
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
                    partyDetails,
                    index,
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
        PartyDetails partyDetailsBefore,
        int partyIndex) {
        if ((YesOrNo.Yes.equals(partyDetailsBefore.getIsCurrentAddressKnown())
            && (YesOrNo.No.equals(partyDetails.getIsCurrentAddressKnown())))
            || (!YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
            && YesOrNo.Yes.equals(partyDetailsBefore.getLiveInRefuge()))) {
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
                partyDetails,
                partyIndex,
                handler,
                refugeConfidentialDocumentsRecord
            );
        } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())
            && !YesOrNo.Yes.equals(partyDetailsBefore.getLiveInRefuge())) {
            RefugeDocumentHandlerParameters handler =
                RefugeDocumentHandlerParameters.builder()
                    .onlyForApplicant(refugeDocumentHandlerParameters.onlyForApplicant)
                    .onlyForRespondent(refugeDocumentHandlerParameters.onlyForRespondent)
                    .onlyForOtherPeople(refugeDocumentHandlerParameters.onlyForOtherPeople)
                    .listDocument(true).build();
            refugeConfidentialDocumentsRecord = listRefugeDocumentsForConfidentialTab(
                caseData,
                partyDetails,
                partyIndex,
                handler,
                refugeConfidentialDocumentsRecord
            );
        } else if (YesOrNo.Yes.equals(partyDetails.getLiveInRefuge())) {
            if (partyDetails.getRefugeConfidentialityC8Form() != null
                && partyDetails.getRefugeConfidentialityC8Form().getDocumentFileName() != null
                && partyDetailsBefore.getRefugeConfidentialityC8Form() != null
                && partyDetailsBefore.getRefugeConfidentialityC8Form().getDocumentFileName() != null
                && partyDetails.getRefugeConfidentialityC8Form().getDocumentFileName()
                .equalsIgnoreCase(partyDetailsBefore.getRefugeConfidentialityC8Form().getDocumentFileName())) {
                log.trace("Amend event occurred, file name is unchanged. Assuming no changes made to file.");
            } else {
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
                    partyDetails,
                    partyIndex,
                    handler,
                    refugeConfidentialDocumentsRecord
                );
            }
        }
        return refugeConfidentialDocumentsRecord;
    }

    public void processRefugeDocumentsOnSubmit(Map<String, Object> caseDataUpdated,
                                               CaseData caseData) {
        List<Element<RefugeConfidentialDocuments>> refugeDocuments = null;
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            refugeDocuments = findAndAddRefugeDocsForC100OnSubmit(
                ofNullable(caseData.getApplicants()),
                SERVED_PARTY_APPLICANT,
                null
            );
            refugeDocuments = findAndAddRefugeDocsForC100OnSubmit(
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                SERVED_PARTY_OTHER,
                refugeDocuments
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            refugeDocuments = findAndAddRefugeDocsForFl401OnSubmit(
                ofNullable(caseData.getApplicantsFL401()),
                null
            );
        }
        if (refugeDocuments != null) {
            caseDataUpdated.put(REFUGE_DOCUMENTS, refugeDocuments);
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
        List<Element<RefugeConfidentialDocuments>> refugeDocuments) {
        if (partyDetailsWrapped.isPresent()) {
            PartyDetails partyDetails = partyDetailsWrapped.get();
            refugeDocuments = addToRefugeDocument(
                String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_APPLICANT, 1),
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
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            findAndAddRefugeDocsForC100OnReSubmit(
                ofNullable(caseData.getApplicants()),
                SERVED_PARTY_APPLICANT,
                refugeDocuments,
                historicalRefugeDocuments
            );
            findAndAddRefugeDocsForC100OnReSubmit(
                ofNullable(caseData.getOtherPartyInTheCaseRevised()),
                SERVED_PARTY_OTHER,
                refugeDocuments,
                historicalRefugeDocuments
            );
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            findAndAddRefugeDocsForFl401OnReSubmit(
                ofNullable(caseData.getApplicantsFL401()),
                refugeDocuments,
                historicalRefugeDocuments
            );
        }
        caseDataUpdated.put(REFUGE_DOCUMENTS, refugeDocuments);
        caseDataUpdated.put(HISTORICAL_REFUGE_DOCUMENTS, historicalRefugeDocuments);
    }

    public void processRefugeDocumentsC7ResponseSubmission(Map<String, Object> caseDataUpdated,
                                                           PartyDetails partyDetails,
                                                           List<Element<RefugeConfidentialDocuments>> refugeDocuments,
                                                           List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
                                                           int partyIndex) {
        findAndAddRefugeDocsForSolicitorResponseSubmit(
            partyDetails,
            refugeDocuments,
            historicalRefugeDocuments,
            partyIndex
        );
        caseDataUpdated.put(REFUGE_DOCUMENTS, refugeDocuments);
        caseDataUpdated.put(HISTORICAL_REFUGE_DOCUMENTS, historicalRefugeDocuments);
    }

    private void findAndAddRefugeDocsForSolicitorResponseSubmit(
        PartyDetails partyDetails,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments,
        int partyIndex) {
        String party = String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_RESPONDENT, partyIndex);
        updateHistoricalDocsAndRemoveFromCurrentList(
            party,
            refugeDocuments,
            historicalRefugeDocuments
        );
        addToRefugeDocument(
            party,
            refugeDocuments,
            partyDetails
        );
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
                String party = String.format(
                    PrlAppsConstants.FORMAT,
                    partyType,
                    partyDetailsList.indexOf(partyDetails) + 1
                );
                historicalRefugeDocuments = updateHistoricalDocsAndRemoveFromCurrentList(
                    party,
                    refugeDocuments,
                    historicalRefugeDocuments
                );
                addToRefugeDocument(
                    party,
                    refugeDocuments,
                    partyDetails
                );
            }
        }
    }

    private void findAndAddRefugeDocsForFl401OnReSubmit(
        Optional<PartyDetails> partyDetailsWrapped,
        List<Element<RefugeConfidentialDocuments>> refugeDocuments,
        List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments) {
        if (partyDetailsWrapped.isPresent()) {
            String party = String.format(PrlAppsConstants.FORMAT, SERVED_PARTY_APPLICANT, 1);
            updateHistoricalDocsAndRemoveFromCurrentList(
                party,
                refugeDocuments,
                historicalRefugeDocuments
            );
            PartyDetails partyDetails = partyDetailsWrapped.get();
            addToRefugeDocument(party, refugeDocuments, partyDetails);
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
        if (null != refugeDocuments) {
            for (Iterator<Element<RefugeConfidentialDocuments>> itr = refugeDocuments.iterator(); itr.hasNext(); ) {
                Element<RefugeConfidentialDocuments> refugeConfidentialDocumentsWrapped = itr.next();
                if (refugeConfidentialDocumentsWrapped.getValue() != null
                    && partyType.equalsIgnoreCase(refugeConfidentialDocumentsWrapped.getValue().getPartyType())) {
                    if (historicalRefugeDocuments == null) {
                        historicalRefugeDocuments = new ArrayList<>();
                    }
                    historicalRefugeDocuments.add(refugeConfidentialDocumentsWrapped);
                    itr.remove();
                }
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

