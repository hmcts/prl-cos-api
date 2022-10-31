package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataChildDetailsElementsMapper.updateChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataHwnElementsMapper.updateHearingWithoutNoticeElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataInternationalElementsMapper.updateInternationalElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataOtherProceedingsElementsMapper.updateOtherProceedingsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;

@Component
public class CaseDataMapper {

    public static final String COMMA_SEPARATOR = ", ";
    public static final String HYPHEN_SEPARATOR = " - ";

    public CaseData buildUpdatedCaseData(CaseData caseData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        C100RebuildInternationalElements c100RebuildInternationalElements = mapper
                .readValue(caseData.getC100RebuildInternationalElements(), C100RebuildInternationalElements.class);
        updateInternationalElementsForCaseData(caseDataBuilder, c100RebuildInternationalElements);

        C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements = mapper
                .readValue(caseData.getC100RebuildHearingWithoutNotice(), C100RebuildHearingWithoutNoticeElements.class);
        updateHearingWithoutNoticeElementsForCaseData(caseDataBuilder, c100RebuildHearingWithoutNoticeElements);

        C100RebuildCourtOrderElements c100RebuildCourtOrderElements = mapper
                .readValue(caseData.getC100RebuildTypeOfOrder(), C100RebuildCourtOrderElements.class);
        updateTypeOfOrderElementsForCaseData(caseDataBuilder, c100RebuildCourtOrderElements);

        C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements = mapper
                .readValue(caseData.getC100RebuildOtherProceedings(), C100RebuildOtherProceedingsElements.class);
        updateOtherProceedingsElementsForCaseData(caseDataBuilder, c100RebuildOtherProceedingsElements);

        C100RebuildChildDetailsElements c100RebuildChildDetailsElements = mapper
            .readValue(caseData.getC100RebuildChildDetails(), C100RebuildChildDetailsElements.class);
        updateChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildChildDetailsElements);


        return caseDataBuilder.build();
    }
}
