package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
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
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildUrgencyElements;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.APPLICANT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.MIAM_CERTIFICATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
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
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataTypeOfOrderElementsMapper.updateTypeOfOrderElementsForCaseData;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataUrgencyElementsMapper.updateUrgencyElementsForCaseData;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.getCitizenQuarantineDocument;
import static uk.gov.hmcts.reform.prl.utils.DocumentUtils.getExistingCitizenQuarantineDocuments;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@Slf4j
@Component
public class CaseDataMapper {

    private CaseDataMapper() {
    }

    public static final String COMMA_SEPARATOR = ", ";
    public static final String HYPHEN_SEPARATOR = " - ";

    public CaseData buildUpdatedCaseData(CaseData caseData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        C100RebuildData c100RebuildData = caseData.getC100RebuildData();

        List<Element<QuarantineLegalDoc>> quarantineDocList = new ArrayList<>();

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
            updateOtherProceedingsElementsForCaseData(caseDataBuilder, c100RebuildOtherProceedingsElements, quarantineDocList);
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

            // for miam
            Optional.of(getCitizenQuarantineDocuments(caseData, c100RebuildMiamElements.getMiamCertificate(),
                                                              MIAM_CERTIFICATE,"MIAM Certificate"))
                .ifPresent(quarantineDocList::addAll);

        }

        if (isNotEmpty(c100RebuildData.getC100RebuildApplicantDetails())) {
            C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildApplicantDetails(), C100RebuildApplicantDetailsElements.class);
            updateApplicantElementsForCaseData(caseDataBuilder, c100RebuildApplicantDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildChildDetails())) {
            C100RebuildChildDetailsElements c100RebuildChildDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildChildDetails(), C100RebuildChildDetailsElements.class);
            updateChildDetailsElementsForCaseData(caseDataBuilder, c100RebuildChildDetailsElements);
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

        if (isNotEmpty(c100RebuildData.getC100RebuildOtherPersonsDetails())) {
            C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildOtherPersonsDetails(), C100RebuildOtherPersonDetailsElements.class);
            updateOtherPersonDetailsElementsForCaseData(caseDataBuilder, c100RebuildOtherPersonDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildRespondentDetails())) {
            C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements = mapper
                    .readValue(c100RebuildData.getC100RebuildRespondentDetails(), C100RebuildRespondentDetailsElements.class);
            updateRespondentDetailsElementsForCaseData(caseDataBuilder, c100RebuildRespondentDetailsElements);
        }

        if (isNotEmpty(c100RebuildData.getC100RebuildConsentOrderDetails())) {
            C100RebuildConsentOrderDetails c100RebuildConsentOrderDetails = mapper
                    .readValue(c100RebuildData.getC100RebuildConsentOrderDetails(), C100RebuildConsentOrderDetails.class);
            updateConsentOrderDetailsForCaseData(caseDataBuilder, c100RebuildConsentOrderDetails);

            //for c100Rebuild of Consent Documents
            Optional.of(getCitizenQuarantineDocuments(caseData, c100RebuildConsentOrderDetails.getConsentOrderCertificate(),
                                                              APPLICANT_APPLICATION,"Applicant Application"))
                .ifPresent(quarantineDocList::addAll);
        }

        caseDataBuilder.citizenQuarantineDocsList(quarantineDocList);

        return caseDataBuilder.build();
    }

    public static List<Element<QuarantineLegalDoc>> getCitizenQuarantineDocuments(CaseData caseData,
                                                                                  uk.gov.hmcts.reform.prl.models.c100rebuild.Document uploadedDoc,
                                                                                  String categoryId,
                                                                                  String categoryName) {
        List<Element<QuarantineLegalDoc>> citizenQuarantineDocs = getExistingCitizenQuarantineDocuments(caseData);

        if (uploadedDoc != null) {
            QuarantineLegalDoc quarantineLegalDoc = getCitizenQuarantineDocument(uploadedDoc, SERVED_PARTY_APPLICANT, categoryId, categoryName, null);
            citizenQuarantineDocs.add(element(quarantineLegalDoc));
        }
        return citizenQuarantineDocs;
    }
}
