package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.CaseViewField;
import uk.gov.hmcts.reform.ccd.client.model.metadatafields.definition.FieldTypeDefinition;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class InjectedCaseDataResponseService {
    private final FormattedTitleDisplayService formattedTitleDisplayService;
    public static final String INJECTED_FIELD_ID_CASE_DETAILS_TITLE = "injectedCaseDetailsTitle";
    public static final String FIELD_TYPE_TEXT = "Text";
    public static final String FIELD_TYPE_TEXT_ID = "Text";

    public GetCaseCallbackResponse getInjectedCallbackResponse(CaseData caseData) {
        String extendedCaseDetails = getInjectedCaseDetailsTitle(caseData);

        CaseViewField caseViewField = getNewCaseField(INJECTED_FIELD_ID_CASE_DETAILS_TITLE, extendedCaseDetails);
        List<CaseViewField> caseViewFields = new ArrayList<>();
        caseViewFields.add(caseViewField);
        GetCaseCallbackResponse getCaseCallbackResponse = new GetCaseCallbackResponse();
        getCaseCallbackResponse.setMetadataFields(caseViewFields);

        return getCaseCallbackResponse;
    }

    private CaseViewField getNewCaseField(String fieldId, String fieldValue) {
        FieldTypeDefinition fieldTypeDefinition = new FieldTypeDefinition();
        CaseViewField caseViewField = new CaseViewField();
        caseViewField.setId(fieldId);
        caseViewField.setFormattedValue(fieldValue);
        caseViewField.setValue(fieldValue);
        caseViewField.setMetadata(true);
        fieldTypeDefinition.setId(FIELD_TYPE_TEXT_ID);
        fieldTypeDefinition.setType(FIELD_TYPE_TEXT);
        caseViewField.setFieldTypeDefinition(fieldTypeDefinition);

        return caseViewField;
    }

    private String getInjectedCaseDetailsTitle(CaseData caseData) {
        String injectedCaseDetailsTitle = formattedTitleDisplayService.getFormattedTitleDisplay(
            caseData.getApplicantCaseName(),
            caseData.getCaseNameHmctsInternal(),
            caseData.getFamilymanCaseNumber(),
            caseData.getId(),
            caseData.getIssueDate()
        );
        log.info("injectedCaseDetailsTitle:{}", injectedCaseDetailsTitle);
        return injectedCaseDetailsTitle;
    }
}
