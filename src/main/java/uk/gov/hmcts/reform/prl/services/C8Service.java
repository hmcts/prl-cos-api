package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnowV2;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_OTHER_DRAFT_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_OTHER_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILDREN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.enums.State.JUDICIAL_REVIEW;
import static uk.gov.hmcts.reform.prl.enums.State.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getC8FileName;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Service
@AllArgsConstructor
public class C8Service {

    private final ManageOrderService manageOrderService;
    private final DocumentLanguageService documentLanguageService;
    private final DocumentGenService documentGenService;
    private final ObjectMapper objectMapper;

    // All states after the case has been issued => seal, no watermark, not stored in draft field
    private static final List<State> SEALED_STATES = List.of(CASE_ISSUED, JUDICIAL_REVIEW, PREPARE_FOR_HEARING_CONDUCT_HEARING, DECISION_OUTCOME);

    private Map<String, Object> populateDataMap(CaseData caseData) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(COURT_NAME_FIELD, caseData.getCourtName());
        dataMap.put(CASE_DATA_ID, caseData.getId());
        dataMap.put(ISSUE_DATE_FIELD, caseData.getIssueDate());
        dataMap.put(COURT_SEAL_FIELD,
                    caseData.getCourtSeal() == null ? "[userImage:familycourtseal.png]" : caseData.getCourtSeal());
        if (caseData.getTaskListVersion() != null
            && (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion()))) {
            List<Element<ChildDetailsRevised>> listOfChildren = caseData.getNewChildDetails();
            dataMap.put(CHILDREN, listOfChildren);
        } else {
            List<Element<Child>> listOfChildren = caseData.getChildren();
            dataMap.put(CHILDREN, listOfChildren);

        }
        return dataMap;
    }

    private ResponseDocuments generateC8ForOtherParty(CaseData caseData, Element<PartyDetails> partyDetails, String authorisation) {
        String loggedInUserRole = isNotEmpty(caseData.getLoggedInUserRole())
            ? caseData.getLoggedInUserRole()
            : manageOrderService.getLoggedInUserType(authorisation);
        Map<String, Object> dataMap = populateDataMap(caseData);

        dataMap.put("party", objectMapper.convertValue(partyDetails.getValue(), new TypeReference<Map<String, Object>>() {}));
        dataMap.put("loggedInUserRole", loggedInUserRole);
        dataMap.put("repFullName", caseData.getCaseSolicitorName());
        dataMap.put("solicitorOrg", caseData.getCaseSolicitorOrgName());

        if (loggedInUserRole.equals(UserRoles.CITIZEN.name())) {
            // populate Citizen signing names -> no solicitors so the applicant C8 uses the _first_ applicant
            PartyDetails firstPartyDetails = caseData.getCaseTypeOfApplication().equals(C100_CASE_TYPE)
                ? caseData.getApplicants().getFirst().getValue()
                : caseData.getApplicantsFL401();
            dataMap.put("signedBy", firstPartyDetails.getLabelForDynamicList());
            dataMap.put("signedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }

        // Uses the same template, but a different filename to indicate draft files
        String templateHint = C8_OTHER_FINAL_HINT;
        if (SEALED_STATES.contains(caseData.getState())) {
            dataMap.put("sealed", true);
            dataMap.put("watermark", "");
        } else {
            dataMap.put("sealed", false);
            dataMap.put("watermark", "DRAFT");
            templateHint = C8_OTHER_DRAFT_HINT;
        }

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        Document c8Welsh = null;
        if (documentLanguage.isGenWelsh()) {
            dataMap.put("dynamic_fileName", getC8FileName(partyDetails.getValue(), true));
            c8Welsh = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                templateHint,
                true,
                dataMap
            );
        }

        dataMap.put("dynamic_fileName", getC8FileName(partyDetails.getValue(), false));
        dataMap.put("party", objectMapper.convertValue(partyDetails.getValue(), new TypeReference<Map<String, Object>>() {}));
        Document c8English = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            templateHint,
            false,
            dataMap
        );

        return ResponseDocuments.builder()
            .dateCreated(LocalDate.now())
            .dateTimeCreated(LocalDateTime.now())
            .partyName(partyDetails.getValue().getFirstName() + " " + partyDetails.getValue().getLastName())
            .respondentC8Document(c8English)
            .respondentC8DocumentWelsh(c8Welsh)
            .build();
    }

    public Map<String, Object> generateOtherPartiesC8s(CaseData caseData, CaseData caseDataBefore, String authorisation) {

        List<Element<PartyDetails>> othersAfter = nullSafeList(caseData.getOtherPartyInTheCaseRevised());

        List<Element<ResponseDocuments>> otherPartyC8Documents = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8Documents()));
        List<Element<ResponseDocuments>> otherPartyC8DocumentsArchived = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8DocumentsArchived()));

        for (Element<PartyDetails> afterEl : othersAfter) {
            UUID partyId = afterEl.getId();
            PartyDetails afterDetails = afterEl.getValue();
            Optional<PartyDetails> beforeDetails = ElementUtils
                .findElement(partyId, nullSafeList(caseDataBefore.getOtherPartyInTheCaseRevised()))
                .map(Element::getValue);

            boolean wasHiddenBefore = shouldHideInfo(beforeDetails);
            boolean shouldHideNow = shouldHideInfo(Optional.of(afterDetails));

            Optional<Element<ResponseDocuments>> existingC8Opt = ElementUtils.findElement(partyId, otherPartyC8Documents);
            if (shouldHideNow) {
                // Archive old C8 if present
                if (existingC8Opt.isPresent()) {
                    Element<ResponseDocuments> toArchive = existingC8Opt.get();
                    otherPartyC8Documents.remove(toArchive);
                    Element<ResponseDocuments> archived = ElementUtils.element(UUID.randomUUID(), toArchive.getValue());
                    otherPartyC8DocumentsArchived.add(archived);
                }
                // Generate and add new C8
                ResponseDocuments newC8 = generateC8ForOtherParty(caseData, afterEl, authorisation);
                otherPartyC8Documents.removeIf(el -> el.getId().equals(partyId));
                otherPartyC8Documents.add(ElementUtils.element(partyId, newC8));
            } else if (wasHiddenBefore && existingC8Opt.isPresent()) {
                // Archive old C8 if present and no longer confidential
                Element<ResponseDocuments> toArchive = existingC8Opt.get();
                otherPartyC8Documents.remove(toArchive);
                Element<ResponseDocuments> archived = ElementUtils.element(UUID.randomUUID(), toArchive.getValue());
                otherPartyC8DocumentsArchived.add(archived);
            }
        }
        Map<String, Object> updates = new HashMap<>();
        updates.put("otherPartyC8Documents", otherPartyC8Documents);
        updates.put("otherPartyC8DocumentsArchived", otherPartyC8DocumentsArchived);
        return updates;
    }

    private boolean hasConfidentialInfo(Optional<PartyDetails> partyDetails) {
        return partyDetails.isPresent() && (
            Yes.equals(partyDetails.get().getIsAddressConfidential())
                || Yes.equals(partyDetails.get().getIsPhoneNumberConfidential())
                || Yes.equals(partyDetails.get().getIsEmailAddressConfidential())
                || YesNoIDontKnowV2.Yes.equals(partyDetails.get().getLiveInRefuge()));
    }

    /**
     * Should hide info only if something is confidential (or refuge)
     * AND they have info to hide - address / email / phone number.
     * @param partyDetails - the other party to check
     * @return true if the C8 should be generated
     */
    private boolean shouldHideInfo(Optional<PartyDetails> partyDetails) {
        return hasConfidentialInfo(partyDetails)
                && partyDetails.isPresent()
                && (Yes.equals(partyDetails.get().getIsCurrentAddressKnown())
                    || Yes.equals(partyDetails.get().getCanYouProvideEmailAddress())
                    || Yes.equals(partyDetails.get().getCanYouProvidePhoneNumber()));
    }
}
