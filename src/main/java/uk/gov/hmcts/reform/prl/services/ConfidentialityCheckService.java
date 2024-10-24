package uk.gov.hmcts.reform.prl.services;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentC8;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespondentC8Document;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

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
                        if (null != eachRes.getValue().getRefugeConfidentialityC8Form()
                            && null != eachRes.getValue().getLiveInRefuge()
                            && eachRes.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("respAC8RefugeDocument", eachRes.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("respAC8RefugeDocument", null);
                        }
                    }
                    case 1 -> {
                        ResponseDocuments responseDocumentB = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 1));
                        caseDataMap.put("respBC8EngDocument", responseDocumentB.getRespondentC8Document());
                        caseDataMap.put("respBC8WelDocument", responseDocumentB.getRespondentC8DocumentWelsh());
                        if (null != eachRes.getValue().getRefugeConfidentialityC8Form()
                            && null != eachRes.getValue().getLiveInRefuge()
                            && eachRes.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("respBC8RefugeDocument", eachRes.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("respBC8RefugeDocument", null);
                        }
                    }
                    case 2 -> {
                        ResponseDocuments responseDocumentC = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 2));
                        caseDataMap.put("respCC8EngDocument", responseDocumentC.getRespondentC8Document());
                        caseDataMap.put("respCC8WelDocument", responseDocumentC.getRespondentC8DocumentWelsh());
                        if (null != eachRes.getValue().getRefugeConfidentialityC8Form()
                            && null != eachRes.getValue().getLiveInRefuge()
                            && eachRes.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("respCC8RefugeDocument", eachRes.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("respCC8RefugeDocument", null);
                        }
                    }
                    case 3 -> {
                        ResponseDocuments responseDocumentD = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 3));
                        caseDataMap.put("respDC8EngDocument", responseDocumentD.getRespondentC8Document());
                        caseDataMap.put("respDC8WelDocument", responseDocumentD.getRespondentC8DocumentWelsh());
                        if (null != eachRes.getValue().getRefugeConfidentialityC8Form()
                            && null != eachRes.getValue().getLiveInRefuge()
                            && eachRes.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("respDC8RefugeDocument", eachRes.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("respDC8RefugeDocument", null);
                        }
                    }
                    case 4 -> {
                        ResponseDocuments responseDocumentE = getRespondentDoc(findLatestC8Document(caseData.getRespondentC8Document(),
                                caseData.getRespondentC8(), 4));
                        caseDataMap.put("respEC8EngDocument", responseDocumentE.getRespondentC8Document());
                        caseDataMap.put("respEC8WelDocument", responseDocumentE.getRespondentC8DocumentWelsh());
                        if (null != eachRes.getValue().getRefugeConfidentialityC8Form()
                            && null != eachRes.getValue().getLiveInRefuge()
                            && eachRes.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("respEC8RefugeDocument", eachRes.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("respEC8RefugeDocument", null);
                        }
                    }

                    default -> log.info("no respondent found here");

                }
            });
        } else {
            if (null != caseData.getRespondentsFL401().getRefugeConfidentialityC8Form()
                && null != caseData.getRespondentsFL401().getLiveInRefuge()
                && caseData.getRespondentsFL401().getLiveInRefuge().equals(YesOrNo.Yes)) {
                caseDataMap.put("respAC8RefugeDocument", caseData.getRespondentsFL401().getRefugeConfidentialityC8Form());
            } else {
                caseDataMap.put("respAC8RefugeDocument", null);
            }
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
                        if (null != eachApp.getValue().getRefugeConfidentialityC8Form()
                            && null != eachApp.getValue().getLiveInRefuge()
                            && eachApp.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("appAC8RefugeDocument", eachApp.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("appAC8RefugeDocument", null);
                        }
                    }
                    case 1 -> {
                        if (null != eachApp.getValue().getRefugeConfidentialityC8Form()
                            && null != eachApp.getValue().getLiveInRefuge()
                            && eachApp.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("appBC8RefugeDocument", eachApp.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("appBC8RefugeDocument", null);
                        }
                    }
                    case 2 -> {
                        if (null != eachApp.getValue().getRefugeConfidentialityC8Form()
                            && null != eachApp.getValue().getLiveInRefuge()
                            && eachApp.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("appCC8RefugeDocument", eachApp.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("appCC8RefugeDocument", null);
                        }
                    }
                    case 3 -> {
                        if (null != eachApp.getValue().getRefugeConfidentialityC8Form()
                            && null != eachApp.getValue().getLiveInRefuge()
                            && eachApp.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("appDC8RefugeDocument", eachApp.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("appDC8RefugeDocument", null);
                        }
                    }
                    case 4 -> {
                        if (null != eachApp.getValue().getRefugeConfidentialityC8Form()
                            && null != eachApp.getValue().getLiveInRefuge()
                            && eachApp.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("appEC8RefugeDocument", eachApp.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("appEC8RefugeDocument", null);
                        }
                    }

                    default -> log.info("no applicant found here");

                }
            });
        } else {
            if (null != caseData.getApplicantsFL401().getRefugeConfidentialityC8Form()
                && null != caseData.getApplicantsFL401().getLiveInRefuge()
                && caseData.getApplicantsFL401().getLiveInRefuge().equals(YesOrNo.Yes)) {
                caseDataMap.put("appAC8RefugeDocument", caseData.getApplicantsFL401().getRefugeConfidentialityC8Form());
            } else {
                caseDataMap.put("appAC8RefugeDocument", null);
            }
        }
    }

    public void processOtherC8Documents(Map<String, Object> caseDataMap, CaseData caseData) {
        if (C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            caseData.getOtherPartyInTheCaseRevised().forEach(eachOther -> {
                switch (caseData.getOtherPartyInTheCaseRevised().indexOf(eachOther)) {
                    case 0 -> {
                        if (null != eachOther.getValue().getRefugeConfidentialityC8Form()
                            && null != eachOther.getValue().getLiveInRefuge()
                            && eachOther.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("otherAC8RefugeDocument", eachOther.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("otherAC8RefugeDocument", null);
                        }
                    }
                    case 1 -> {
                        if (null != eachOther.getValue().getRefugeConfidentialityC8Form()
                            && null != eachOther.getValue().getLiveInRefuge()
                            && eachOther.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("otherBC8RefugeDocument", eachOther.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("otherBC8RefugeDocument", null);
                        }
                    }
                    case 2 -> {
                        if (null != eachOther.getValue().getRefugeConfidentialityC8Form()
                            && null != eachOther.getValue().getLiveInRefuge()
                            && eachOther.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("otherCC8RefugeDocument", eachOther.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("otherCC8RefugeDocument", null);
                        }
                    }
                    case 3 -> {
                        if (null != eachOther.getValue().getRefugeConfidentialityC8Form()
                            && null != eachOther.getValue().getLiveInRefuge()
                            && eachOther.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("otherDC8RefugeDocument", eachOther.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("otherDC8RefugeDocument", null);
                        }
                    }
                    case 4 -> {
                        if (null != eachOther.getValue().getRefugeConfidentialityC8Form()
                            && null != eachOther.getValue().getLiveInRefuge()
                            && eachOther.getValue().getLiveInRefuge().equals(YesOrNo.Yes)) {
                            caseDataMap.put("otherEC8RefugeDocument", eachOther.getValue().getRefugeConfidentialityC8Form());
                        } else {
                            caseDataMap.put("otherEC8RefugeDocument", null);
                        }
                    }

                    default -> log.info("no other person found here");

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
