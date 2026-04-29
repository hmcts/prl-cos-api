package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C8_RESP_FINAL_HINT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Service
@AllArgsConstructor
public class C8Service {

    private final C100RespondentSolicitorService respondentSolicitorService;
    private final ManageOrderService manageOrderService;
    private final DocumentLanguageService documentLanguageService;
    private final DocumentGenService documentGenService;
    private final ObjectMapper objectMapper;

    private ResponseDocuments generateC8ForOtherParty(CallbackRequest cb, CaseData caseData, Element<PartyDetails> partyDetails, String authorisation) {
        String loggedInUserRole = manageOrderService.getLoggedInUserType(authorisation);
        String requestOriginatedFrom = loggedInUserRole.equals(UserRoles.CITIZEN.name()) ? CITIZEN : "Other";
        Map<String, Object> dataMap = respondentSolicitorService.populateDataMap(cb, null, requestOriginatedFrom);

        dataMap.put("respondent", partyDetails.getValue());
        dataMap.put("loggedInUserRole", loggedInUserRole);
        dataMap.put("solicitorName", caseData.getCaseSolicitorName());
        dataMap.put("caseSolicitorOrgName", caseData.getCaseSolicitorOrgName());

        if (loggedInUserRole.equals(UserRoles.CITIZEN.name())) {
            // populate Citizen signing names -> no solicitors so the applicant C8 uses the _first_ applicant
            PartyDetails firstPartyDetails = caseData.getCaseTypeOfApplication().equals(FL401_CASE_TYPE)
                ? caseData.getApplicants().getFirst().getValue()
                : caseData.getApplicantsFL401();
            dataMap.put("signedBy", firstPartyDetails.getLabelForDynamicList());
            dataMap.put("signedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        }

        DocumentLanguage documentLanguage = documentLanguageService.docGenerateLang(caseData);
        Document c8Welsh = null;
        if (documentLanguage.isGenWelsh()) {
            c8Welsh = documentGenService.generateSingleDocument(
                authorisation,
                caseData,
                C8_RESP_FINAL_HINT,
                true,
                dataMap
            );
        }
        Document c8English = documentGenService.generateSingleDocument(
            authorisation,
            caseData,
            C8_RESP_FINAL_HINT,
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

    public Map<String, Object> generateOtherPartiesC8s(CallbackRequest callbackRequest, String authorisation) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);

        List<Element<PartyDetails>> othersAfter = nullSafeList(caseData.getOtherPartyInTheCaseRevised());

        List<Element<ResponseDocuments>> otherPartyC8Documents = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8Documents()));
        List<Element<ResponseDocuments>> otherPartyC8DocumentsArchived = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8DocumentsArchived()));

        for (Element<PartyDetails> afterEl : othersAfter) {
            UUID partyId = afterEl.getId();
            PartyDetails afterDetails = afterEl.getValue();
            Optional<PartyDetails> beforeDetails = ElementUtils
                .findElement(partyId, nullSafeList(caseDataBefore.getOtherPartyInTheCaseRevised()))
                .map(Element::getValue);

            boolean hadConfidentialBefore = hasConfidentialInfo(beforeDetails);
            boolean hasConfidentialNow = hasConfidentialInfo(Optional.of(afterDetails));

            Optional<Element<ResponseDocuments>> existingC8Opt = ElementUtils.findElement(partyId, otherPartyC8Documents);
            if (hasConfidentialNow) {
                // Archive old C8 if present
                if (existingC8Opt.isPresent()) {
                    Element<ResponseDocuments> toArchive = existingC8Opt.get();
                    otherPartyC8Documents.remove(toArchive);
                    Element<ResponseDocuments> archived = ElementUtils.element(UUID.randomUUID(), toArchive.getValue());
                    otherPartyC8DocumentsArchived.add(archived);
                }
                // Generate and add new C8
                ResponseDocuments newC8 = generateC8ForOtherParty(callbackRequest, caseData, afterEl, authorisation);
                otherPartyC8Documents.removeIf(el -> el.getId().equals(partyId));
                otherPartyC8Documents.add(ElementUtils.element(partyId, newC8));
            } else if (hadConfidentialBefore && existingC8Opt.isPresent()) {
                // Archive old C8 if present and no longer confidential
                Element<ResponseDocuments> toArchive = existingC8Opt.get();
                otherPartyC8Documents.remove(toArchive);
                Element<ResponseDocuments> archived = ElementUtils.element(UUID.randomUUID(), toArchive.getValue());
                otherPartyC8DocumentsArchived.add(archived);
            }
            // else: nothing to do
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
                || Yes.equals(partyDetails.get().getLiveInRefuge()));
    }
}
