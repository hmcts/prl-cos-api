package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.Opt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.OrdersToServeSA;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.toGeneratedDocumentInfo;

@Service
@RequiredArgsConstructor
public class ServiceOfApplicationPostService {

    @Autowired
    private BulkPrintService bulkPrintService;

    private static String LETTER_TYPE = "TEST";


    public void send(CaseData caseData, String authorisation) {
        bulkPrintService.send(String.valueOf(caseData.getId()), authorisation, LETTER_TYPE, getListOfDocumentInfo(caseData));
    }

    private GeneratedDocumentInfo getFinalDocument(CaseData caseData) {
        if (!welshCase(caseData)) {
             return toGeneratedDocumentInfo(caseData.getFinalDocument());
        }
        return toGeneratedDocumentInfo(caseData.getFinalWelshDocument());
    }

    private Optional<GeneratedDocumentInfo> getC1aDocument(CaseData caseData) {
        if (hasAllegationsOfHarm(caseData)) {
            if (!welshCase(caseData)) {
                return Optional.of(toGeneratedDocumentInfo(caseData.getC1ADocument()));
            }
            return Optional.of(toGeneratedDocumentInfo(caseData.getC1AWelshDocument()));
        }
        return Optional.empty();
    }

    private boolean welshCase(CaseData caseData) {
        return caseData.getFinalWelshDocument() != null;
    }

    private boolean hasAllegationsOfHarm(CaseData caseData) {
        return YesOrNo.Yes.equals(caseData.getAllegationOfHarm().getAllegationsOfHarmChildAbuseYesNo());
    }

    private List<GeneratedDocumentInfo> getListOfDocumentInfo(CaseData caseData) {
        List<GeneratedDocumentInfo> docs = new ArrayList<>();
        getC1aDocument(caseData).ifPresent(docs::add);
        docs.add(getFinalDocument(caseData));
        return docs;
    }

    private List<String> getSelectedOrders(CaseData caseData) {

        List<String> fields = new ArrayList<>();
        for (Field f : OrdersToServeSA.class.getDeclaredFields()) {
            if (f.)
        }


    }







}
