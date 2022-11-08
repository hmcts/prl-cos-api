package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildMiamElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherChildrenDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildUrgencyElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataApplicantElementsMapper.updateApplicantElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataChildDetailsElementsMapper.updateChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataHwnElementsMapper.updateHearingWithoutNoticeElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataInternationalElementsMapper.updateInternationalElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMiamElementsMapper.updateMiamElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataOtherChildrenDetailsElementsMapper.updateOtherChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataOtherProceedingsElementsMapper.updateOtherProceedingsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;
import static uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataUrgencyElementsMapper.updateUrgencyElementsForCaseData;

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

        C100RebuildUrgencyElements c100RebuildUrgencyElements = mapper
                .readValue(caseData.getC100RebuildHearingUrgency(), C100RebuildUrgencyElements.class);
        updateUrgencyElementsForCaseData(caseDataBuilder, c100RebuildUrgencyElements);

        C100RebuildMiamElements c100RebuildMiamElements = mapper
                .readValue(caseData.getC100RebuildMaim(), C100RebuildMiamElements.class);
        updateMiamElementsForCaseData(caseDataBuilder, c100RebuildMiamElements);

        C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = mapper
                .readValue(caseData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
        updateApplicantElementsForCaseData(caseDataBuilder, c100RebuildApplicantDetailsElements);

        C100RebuildChildDetailsElements c100RebuildChildDetailsElements = mapper
            .readValue(caseData.getC100RebuildChildDetails(), C100RebuildChildDetailsElements.class);
        updateChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildChildDetailsElements);

        C100RebuildOtherChildrenDetailsElements c100RebuildOtherChildrenDetailsElements = mapper
            .readValue(caseData.getC100RebuildOtherChildrenDetails(), C100RebuildOtherChildrenDetailsElements.class);
        updateOtherChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildOtherChildrenDetailsElements);

        return caseDataBuilder.build();
    }
}
