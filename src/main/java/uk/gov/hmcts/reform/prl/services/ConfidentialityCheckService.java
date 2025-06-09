package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentC8;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentC8Document;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Service
@Slf4j
public class ConfidentialityCheckService {

    public static final String RESP_AC_8_ENG_DOCUMENT = "respAC8EngDocument";
    public static final String RESP_AC_8_WEL_DOCUMENT = "respAC8WelDocument";

    public void processRespondentsC8Documents(Map<String, Object> caseDataMap, CaseData caseData) {

        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {

            caseData.getRespondents().forEach(eachRes -> {


                switch (caseData.getRespondents().indexOf(eachRes)) {
                    case 0 -> {
                        ResponseDocuments responseDocumentA = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 0));
                        caseDataMap.put(RESP_AC_8_ENG_DOCUMENT, responseDocumentA.getRespondentC8Document());
                        caseDataMap.put(RESP_AC_8_WEL_DOCUMENT, responseDocumentA.getRespondentC8DocumentWelsh());
                        findAndShowC8RefugeDocs(caseDataMap, eachRes.getValue(), "respAC8RefugeDocument");
                    }
                    case 1 -> {
                        ResponseDocuments responseDocumentB = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 1));
                        caseDataMap.put("respBC8EngDocument", responseDocumentB.getRespondentC8Document());
                        caseDataMap.put("respBC8WelDocument", responseDocumentB.getRespondentC8DocumentWelsh());
                        findAndShowC8RefugeDocs(caseDataMap, eachRes.getValue(), "respBC8RefugeDocument");
                    }
                    case 2 -> {
                        ResponseDocuments responseDocumentC = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 2));
                        caseDataMap.put("respCC8EngDocument", responseDocumentC.getRespondentC8Document());
                        caseDataMap.put("respCC8WelDocument", responseDocumentC.getRespondentC8DocumentWelsh());
                        findAndShowC8RefugeDocs(caseDataMap, eachRes.getValue(), "respCC8RefugeDocument");
                    }
                    case 3 -> {
                        ResponseDocuments responseDocumentD = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 3));
                        caseDataMap.put("respDC8EngDocument", responseDocumentD.getRespondentC8Document());
                        caseDataMap.put("respDC8WelDocument", responseDocumentD.getRespondentC8DocumentWelsh());
                        findAndShowC8RefugeDocs(caseDataMap, eachRes.getValue(), "respDC8RefugeDocument");
                    }
                    case 4 -> {
                        ResponseDocuments responseDocumentE = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 4));
                        caseDataMap.put("respEC8EngDocument", responseDocumentE.getRespondentC8Document());
                        caseDataMap.put("respEC8WelDocument", responseDocumentE.getRespondentC8DocumentWelsh());
                        findAndShowC8RefugeDocs(caseDataMap, eachRes.getValue(), "respEC8RefugeDocument");
                    }

                    default -> log.info("no respondent found here");

                }
            });
        } else {
            findAndShowC8RefugeDocs(caseDataMap, caseData.getRespondentsFL401(), "respAC8RefugeDocument");
            ResponseDocuments responseDocumentA = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                    caseData.getRespondentC8(), 0));
            caseDataMap.put(RESP_AC_8_ENG_DOCUMENT, responseDocumentA.getRespondentC8Document());
            caseDataMap.put(RESP_AC_8_WEL_DOCUMENT, responseDocumentA.getRespondentC8DocumentWelsh());

        }

    }

    public void processApplicantC8Documents(Map<String, Object> caseDataMap, CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData.getApplicants().forEach(eachApp -> {
                switch (caseData.getApplicants().indexOf(eachApp)) {
                    case 0 -> {
                        ResponseDocuments responseDocumentA = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 0));
                        caseDataMap.put("appAC8RefugeDocument", responseDocumentA.getRespondentC8Document());
                        findAndShowC8RefugeDocs(caseDataMap, eachApp.getValue(), "appAC8RefugeDocument");
                    }
                    case 1 -> {
                        ResponseDocuments responseDocumentB = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 1));
                        caseDataMap.put("appBC8RefugeDocument", responseDocumentB.getRespondentC8Document());
                        findAndShowC8RefugeDocs(caseDataMap, eachApp.getValue(), "appBC8RefugeDocument");
                    }
                    case 2 -> {
                        ResponseDocuments responseDocumentC = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                 caseData.getRespondentC8(), 2));
                        caseDataMap.put("appCC8RefugeDocument", responseDocumentC.getRespondentC8Document());
                        findAndShowC8RefugeDocs(caseDataMap, eachApp.getValue(), "appCC8RefugeDocument");
                    }
                    case 3 -> {
                        ResponseDocuments responseDocumentD = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 3));
                        caseDataMap.put("appDC8RefugeDocument", responseDocumentD.getRespondentC8Document());
                        findAndShowC8RefugeDocs(caseDataMap, eachApp.getValue(), "appDC8RefugeDocument");
                    }
                    case 4 -> {
                        ResponseDocuments responseDocumentE = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 4));
                        caseDataMap.put("appEC8RefugeDocument", responseDocumentE.getRespondentC8Document());
                        findAndShowC8RefugeDocs(caseDataMap, eachApp.getValue(), "appEC8RefugeDocument");
                    }
                    default -> log.info("Applicant is out of index here");
                }
            });
        } else if (FL401_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            findAndShowC8RefugeDocs(caseDataMap, caseData.getApplicantsFL401(), "appAC8RefugeDocument");
            ResponseDocuments responseDocumentA = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                    caseData.getRespondentC8(), 0));
            caseDataMap.put("appAC8RefugeDocument", responseDocumentA.getRespondentC8Document());
        }
    }

    private static void findAndShowC8RefugeDocs(Map<String, Object> caseDataMap, PartyDetails eachApp, String ccdCaseFieldId) {
        if (YesOrNo.Yes.equals(eachApp.getLiveInRefuge())
                && null != eachApp.getRefugeConfidentialityC8Form()) {
            caseDataMap.put(ccdCaseFieldId, eachApp.getRefugeConfidentialityC8Form());
        } else {
            caseDataMap.put(ccdCaseFieldId, null);
        }
    }

    public void processOtherC8Documents(Map<String, Object> caseDataMap, CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))
                && null != caseData.getOtherPartyInTheCaseRevised()) {
            caseData.getOtherPartyInTheCaseRevised().forEach(eachOther -> {
                switch (caseData.getOtherPartyInTheCaseRevised().indexOf(eachOther)) {
                    case 0 -> findAndShowC8RefugeDocs(caseDataMap, eachOther.getValue(), "otherAC8RefugeDocument");
                    case 1 -> findAndShowC8RefugeDocs(caseDataMap, eachOther.getValue(), "otherBC8RefugeDocument");
                    case 2 -> findAndShowC8RefugeDocs(caseDataMap, eachOther.getValue(), "otherCC8RefugeDocument");
                    case 3 -> findAndShowC8RefugeDocs(caseDataMap, eachOther.getValue(), "otherDC8RefugeDocument");
                    case 4 -> findAndShowC8RefugeDocs(caseDataMap, eachOther.getValue(), "otherEC8RefugeDocument");
                    default -> log.info("Other party is out of index here");
                }
            });
        }
    }

    private ResponseDocuments getRespondentDoc(ResponseDocuments latestC8Document) {
        if (latestC8Document != null) {
            Document welFromDoc = null;
            Document engFromDoc = latestC8Document.getRespondentC8Document() == null
                    ? latestC8Document.getCitizenDocument() : latestC8Document.getRespondentC8Document();
            if (latestC8Document.getRespondentC8DocumentWelsh() != null) {
                welFromDoc = latestC8Document.getRespondentC8DocumentWelsh();
            }
            return ResponseDocuments.builder().respondentC8Document(engFromDoc).respondentC8DocumentWelsh(welFromDoc).build();
        }
        return ResponseDocuments.builder().build();
    }


    private ResponseDocuments findLatestC8Document(RespondentC8Document respondentC8Document, RespondentC8 respondentC8, int index) {
        ResponseDocuments respondentDocument = getRespondentC8Document(respondentC8Document, index);
        ResponseDocuments respondentDoc = getRespondentC8(respondentC8, index);

        if (respondentDocument != null && respondentDoc != null) {
            if (respondentDoc.getDateTimeCreated().isAfter(respondentDocument.getDateTimeCreated())) {
                return respondentDoc;
            }
            return respondentDocument;
        } else if (respondentDocument != null) {
            return respondentDocument;

        } else if (respondentDoc != null) {
            return respondentDoc;
        }
        return null;
    }

    private ResponseDocuments getRespondentC8(RespondentC8 respondentC8, int index) {
        if (respondentC8 == null) {

            return null;
        }
        switch (index) {
            case 0 -> {
                return respondentC8.getRespondentAc8();
            }
            case 1 -> {
                return respondentC8.getRespondentBc8();
            }
            case 2 -> {
                return respondentC8.getRespondentCc8();
            }
            case 3 -> {
                return respondentC8.getRespondentDc8();
            }
            case 4 -> {
                return respondentC8.getRespondentEc8();
            }
            default -> log.info("no respondent found");
        }
        return null;
    }

    private ResponseDocuments getRespondentC8Document(RespondentC8Document respondentC8Document, int index) {

        if (respondentC8Document == null) {
            return null;
        }
        switch (index) {
            case 0 -> {
                return getRespondentAC8Document(respondentC8Document);
            }
            case 1 -> {
                if (respondentC8Document.getRespondentBc8Documents() == null || respondentC8Document.getRespondentBc8Documents().isEmpty()) {
                    return null;
                }
                return respondentC8Document.getRespondentBc8Documents().get(0).getValue();
            }
            case 2 -> {
                if (respondentC8Document.getRespondentCc8Documents() == null || respondentC8Document.getRespondentCc8Documents().isEmpty()) {
                    return null;
                }
                return respondentC8Document.getRespondentCc8Documents().get(0).getValue();
            }
            case 3 -> {
                if (respondentC8Document.getRespondentDc8Documents() == null || respondentC8Document.getRespondentDc8Documents().isEmpty()) {
                    return null;
                }
                return respondentC8Document.getRespondentDc8Documents().get(0).getValue();
            }
            case 4 -> {
                if (respondentC8Document.getRespondentEc8Documents() == null || respondentC8Document.getRespondentEc8Documents().isEmpty()) {
                    return null;
                }
                return respondentC8Document.getRespondentEc8Documents().get(0).getValue();
            }

            default -> log.info("no respondent found");
        }
        return null;
    }

    private static ResponseDocuments getRespondentAC8Document(RespondentC8Document respondentC8Document) {
        if (respondentC8Document.getRespondentAc8Documents() == null || respondentC8Document.getRespondentAc8Documents().isEmpty()) {
            return null;
        }
        return respondentC8Document.getRespondentAc8Documents().get(0).getValue();
    }

    public void clearRespondentsC8Documents(Map<String, Object> caseDataMap) {
        caseDataMap.put(RESP_AC_8_ENG_DOCUMENT, null);
        caseDataMap.put(RESP_AC_8_WEL_DOCUMENT, null);
        caseDataMap.put("respBC8EngDocument", null);
        caseDataMap.put("respBC8WelDocument", null);
        caseDataMap.put("respCC8EngDocument", null);
        caseDataMap.put("respCC8WelDocument", null);
        caseDataMap.put("respDC8EngDocument", null);
        caseDataMap.put("respDC8WelDocument", null);
        caseDataMap.put("respEC8EngDocument", null);
        caseDataMap.put("respEC8WelDocument", null);
    }

}
