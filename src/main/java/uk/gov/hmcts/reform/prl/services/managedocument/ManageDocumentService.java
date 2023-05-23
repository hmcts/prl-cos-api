package uk.gov.hmcts.reform.prl.services.managedocument;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarentineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts.restrictToGroup;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentService {


    public void copyDocument(CaseData caseData, List<Element<ManageDocuments>> manageDocuments) {

        if (manageDocuments != null) {
            List<Element<QuarentineLegalDoc>> quarantineDocs = caseData.getLegalProfQuarentineDocsList() != null
                ? caseData.getLegalProfQuarentineDocsList() : new ArrayList<>();
            log.info("*** manageDocuments List *** {}", manageDocuments);

            Predicate<Element<ManageDocuments>> restricted = manageDocumentsElement -> manageDocumentsElement.getValue()
                .getDocumentRestrictCheckbox().contains(restrictToGroup);

            // if restricted then add to quarantine docs list
            quarantineDocs.addAll(getManageDocumentList(manageDocuments, restricted));

            log.info("*** quarantineDocs Manage documents *** {}", quarantineDocs);
            caseData.setLegalProfQuarentineDocsList(quarantineDocs);

            // Predicate<Element<ManageDocuments>> notRestricted = manageDocumentsElement -> !manageDocumentsElement.getValue()
            //   .getDocumentRestrictCheckbox().contains(restrictToGroup);
            List<Element<QuarentineLegalDoc>> legalProfUploadDocListDocTab = new ArrayList<>();
            // If not restricted access then add to below list
            legalProfUploadDocListDocTab.addAll(getManageDocumentList(manageDocuments, Predicate.not(restricted)));

            log.info("*** legalProfUploadDocListDocTab Manage documents *** {}", quarantineDocs);


            caseData.getReviewDocuments().setLegalProfUploadDocListDocTab(legalProfUploadDocListDocTab);
        }
    }

    private List<Element<QuarentineLegalDoc>> getManageDocumentList(List<Element<ManageDocuments>> manageDocuments,
                                                                    Predicate<Element<ManageDocuments>> predicate) {
        return manageDocuments.stream()
            .filter(predicate)
            .map(element -> Element.<QuarentineLegalDoc>builder()
                .value(QuarentineLegalDoc.builder().document(element.getValue().getDocument())
                           .documentParty(element.getValue().getDocumentParty().getDisplayedValue())
                           .restrictCheckboxCorrespondence(element.getValue().getDocumentRestrictCheckbox())
                           .notes(element.getValue().getDocumentDetails())
                           .category(element.getValue().getDocumentCategories().getValueCode())
                           .build())
                .id(element.getId()).build())
            .collect(Collectors.toList());
    }

    private boolean isRestricted(Element<ManageDocuments> manageDocumentsElement) {
        return manageDocumentsElement.getValue().getDocumentRestrictCheckbox().contains(restrictToGroup);
    }
}
