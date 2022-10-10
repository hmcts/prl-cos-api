package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataHwnElementsMapper.updateHearingWithoutNoticeElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataInternationalElementsMapper.updateInternationalElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataOtherProceedingsElementsMapper.updateOtherProceedingsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;

@Component
public class CaseDataMapper {

    public static final String COMMA_SEPARATOR = ", ";
    public static final String HYPHEN_SEPARATOR = " - ";

    public CaseData buildUpdatedCaseData(CaseData caseData) throws JsonProcessingException {
        C100RebuildInternationalElements c100RebuildInternationalElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildInternationalElements(), C100RebuildInternationalElements.class);

        C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildHearingWithoutNotice(), C100RebuildHearingWithoutNoticeElements.class);

        C100RebuildCourtOrderElements c100RebuildCourtOrderElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildTypeOfOrder(), C100RebuildCourtOrderElements.class);

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        updateInternationalElementsForCaseData(caseDataBuilder, c100RebuildInternationalElements);
        updateTypeOfOrderElementsForCaseData(caseDataBuilder, c100RebuildCourtOrderElements);
        updateHearingWithoutNoticeElementsForCaseData(caseDataBuilder, c100RebuildHearingWithoutNoticeElements);

        C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements = new ObjectMapper()
                .readValue(caseData.getC100RebuildOtherProceedings(), C100RebuildOtherProceedingsElements.class);

        updateOtherProceedingsElementsForCaseData(caseDataBuilder, c100RebuildOtherProceedingsElements);
        return caseDataBuilder.build();
    }
}
