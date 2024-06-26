package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildConsentOrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildCourtOrderElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildHearingWithoutNoticeElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildInternationalElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildMiamElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherChildrenDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherPersonDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherProceedingsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildReasonableAdjustmentsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildUrgencyElements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataApplicantElementsMapper.updateApplicantElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataChildDetailsElementsMapper.updateChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataConsentOrderDetailsElementsMapper.updateConsentOrderDetailsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataHwnElementsMapper.updateHearingWithoutNoticeElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataInternationalElementsMapper.updateInternationalElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMiamElementsMapper.updateMiamElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherChildrenDetailsElementsMapper.updateOtherChildDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherPersonsElementsMapper.updateOtherPersonDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataOtherProceedingsElementsMapper.updateOtherProceedingsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataReasonableAdjustmentsElementsMapper.updateReasonableAdjustmentsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataRespondentDetailsElementsMapper.updateRespondentDetailsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataSafetyConcernsElementsMapper.updateSafetyConcernsElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataUrgencyElementsMapper.updateUrgencyElementsForCaseData;


@Component
public class CaseDataMapper {

    private CaseDataMapper() {
    }

    public static final String COMMA_SEPARATOR = ", ";
    public static final String HYPHEN_SEPARATOR = " - ";

    public CaseData buildUpdatedCaseData(CaseData caseData) throws JsonProcessingException {
        C100RebuildChildDetailsElements c100RebuildChildDetailsElements = null;
        ObjectMapper mapper = new ObjectMapper();
        CaseData.CaseDataBuilder<?,?> caseDataBuilder = CaseData.builder();

        C100RebuildData c100RebuildData = caseData.getC100RebuildData();

        if (isNotEmpty(c100RebuildData.getC100RebuildInternationalElements())) {
            C100RebuildInternationalElements c100RebuildInternationalElements = mapper
                    .readValue(c100RebuildData.getC100RebuildInternationalElements(), C100RebuildInternationalElements.class);
            updateInternationalElementsForCaseData(caseDataBuilder, c100RebuildInternationalElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildHearingWithoutNotice())) {
            C100RebuildHearingWithoutNoticeElements c100RebuildHearingWithoutNoticeElements = mapper
                    .readValue(c100RebuildData.getC100RebuildHearingWithoutNotice(), C100RebuildHearingWithoutNoticeElements.class);
            updateHearingWithoutNoticeElementsForCaseData(caseDataBuilder, c100RebuildHearingWithoutNoticeElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildTypeOfOrder())) {
            C100RebuildCourtOrderElements c100RebuildCourtOrderElements = mapper
                    .readValue(c100RebuildData.getC100RebuildTypeOfOrder(), C100RebuildCourtOrderElements.class);
            updateTypeOfOrderElementsForCaseData(caseDataBuilder, c100RebuildCourtOrderElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildOtherProceedings())) {
            C100RebuildOtherProceedingsElements c100RebuildOtherProceedingsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildOtherProceedings(), C100RebuildOtherProceedingsElements.class);
            updateOtherProceedingsElementsForCaseData(caseDataBuilder, c100RebuildOtherProceedingsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildHearingUrgency())) {
            C100RebuildUrgencyElements c100RebuildUrgencyElements = mapper
                    .readValue(c100RebuildData.getC100RebuildHearingUrgency(), C100RebuildUrgencyElements.class);
            updateUrgencyElementsForCaseData(caseDataBuilder, c100RebuildUrgencyElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildMaim())) {
            C100RebuildMiamElements c100RebuildMiamElements = mapper
                    .readValue(c100RebuildData.getC100RebuildMaim(), C100RebuildMiamElements.class);
            updateMiamElementsForCaseData(caseDataBuilder, c100RebuildMiamElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildChildDetails())) {
            c100RebuildChildDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildChildDetails(), C100RebuildChildDetailsElements.class);
            updateChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildChildDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
            C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            updateApplicantElementsForCaseData(caseDataBuilder, c100RebuildApplicantDetailsElements, c100RebuildChildDetailsElements,
                                               c100RebuildData.getApplicantPcqId());
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
            C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            updateRespondentDetailsElementsForCaseData(caseDataBuilder, c100RebuildRespondentDetailsElements, c100RebuildChildDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildOtherPersonsDetails())) {
            C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements = mapper
                .readValue(c100RebuildData.getC100RebuildOtherPersonsDetails(), C100RebuildOtherPersonDetailsElements.class);
            updateOtherPersonDetailsElementsForCaseData(caseDataBuilder,
                                                        c100RebuildOtherPersonDetailsElements, c100RebuildChildDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildOtherChildrenDetails())) {
            C100RebuildOtherChildrenDetailsElements c100RebuildOtherChildrenDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildOtherChildrenDetails(), C100RebuildOtherChildrenDetailsElements.class);
            updateOtherChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildOtherChildrenDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildReasonableAdjustments())) {
            C100RebuildReasonableAdjustmentsElements c100RebuildReasonableAdjustmentsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildReasonableAdjustments(), C100RebuildReasonableAdjustmentsElements.class);
            updateReasonableAdjustmentsElementsForCaseData(caseDataBuilder, c100RebuildReasonableAdjustmentsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildConsentOrderDetails())) {
            C100RebuildConsentOrderDetails c100RebuildConsentOrderDetails = mapper
                    .readValue(c100RebuildData.getC100RebuildConsentOrderDetails(), C100RebuildConsentOrderDetails.class);
            updateConsentOrderDetailsForCaseData(caseDataBuilder, c100RebuildConsentOrderDetails);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildSafetyConcerns())) {
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            C100RebuildSafetyConcernsElements c100C100RebuildSafetyConcernsElements = mapper
                .readValue(c100RebuildData.getC100RebuildSafetyConcerns(), C100RebuildSafetyConcernsElements.class);
            updateSafetyConcernsElementsForCaseData(caseDataBuilder,
                                                    c100C100RebuildSafetyConcernsElements,
                                                    c100RebuildChildDetailsElements);
        }

        return caseDataBuilder.build();
    }
}
