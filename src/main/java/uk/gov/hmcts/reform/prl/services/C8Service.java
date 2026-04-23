package uk.gov.hmcts.reform.prl.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

@Service
@AllArgsConstructor
public class C8Service {

    private final C100RespondentSolicitorService respondentSolicitorService;
    private final DocumentGenService documentGenService;

    public ResponseDocuments generateC8ForParty(CaseData caseData, Element<PartyDetails> partyDetails, PartyType partyType) {
        // respondentSolicitorService.populateDataMap(); todo - get data from CaseData, not callback request if possible

        // todo generate English and Welsh docs (if needed)
        return null;
    }

    public Map<String, Object> generateOtherPartiesC8s(CaseData caseDataBefore, CaseData caseData) {
        List<Element<PartyDetails>> othersAfter = nullSafeList(caseData.getOtherPartyInTheCaseRevised());

        List<Element<ResponseDocuments>> otherPartyC8Documents = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8Documents()));
        List<Element<ResponseDocuments>> otherPartyC8DocumentsArchived = new ArrayList<>(nullSafeList(caseData.getOtherPartyC8DocumentsArchived()));

        for (Element<PartyDetails> afterEl : othersAfter) {
            UUID partyId = afterEl.getId();
            PartyDetails afterDetails = afterEl.getValue();
            Optional<PartyDetails> beforeDetails = ElementUtils
                .findElement(partyId, nullSafeList(caseDataBefore.getOtherPartyInTheCaseRevised()))
                .map(Element::getValue);

            boolean hadConfidentialBefore = beforeDetails.isPresent() && (
                Yes.equals(beforeDetails.get().getIsAddressConfidential())
                || Yes.equals(beforeDetails.get().getIsPhoneNumberConfidential())
                || Yes.equals(beforeDetails.get().getIsEmailAddressConfidential())
                || Yes.equals(beforeDetails.get().getLiveInRefuge())
            );
            boolean hasConfidentialNow = afterDetails != null && (
                Yes.equals(afterDetails.getIsAddressConfidential())
                || Yes.equals(afterDetails.getIsPhoneNumberConfidential())
                || Yes.equals(afterDetails.getIsEmailAddressConfidential())
                || Yes.equals(afterDetails.getLiveInRefuge())
            );

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
                ResponseDocuments newC8 = generateC8ForParty(caseData, afterEl, PartyType.OTHER);
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

    @Getter
    @AllArgsConstructor
    public enum PartyType {
        RESPONDENT("Respondent", ""),
        OTHER("Other party", "");

        public final String labelEng;
        public final String labelWelsh;
    }

}
