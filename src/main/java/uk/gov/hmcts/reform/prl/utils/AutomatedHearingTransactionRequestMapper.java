package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.UUID;

@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingCaseData mappingAutomatedHearingTransactionRequest(CaseData caseData, UUID id) {
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder().build();
        ObjectMapper objectMappers = new ObjectMapper();
        objectMappers.registerModule(new JavaTimeModule());
        try {
            automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder()
                .orderId(id)
                .id(caseData.getId())
                .taskListVersion(caseData.getTaskListVersion())
                .createdDate(caseData.getCreatedDate())
                .familymanCaseNumber(caseData.getFamilymanCaseNumber())
                .dateSubmitted(caseData.getDateSubmitted())
                .caseTypeOfApplication(caseData.getCaseTypeOfApplication())
                .applicants(caseData.getApplicants())
                .respondents(caseData.getRespondents())
                .otherPartyInTheCaseRevised(caseData.getOtherPartyInTheCaseRevised())
                .applicantSolicitorEmailAddress(caseData.getApplicantSolicitorEmailAddress())
                .solicitorName(caseData.getSolicitorName())
                .courtName(caseData.getCourtName())
                .applicantsFL401(caseData.getApplicantsFL401())
                .caseManagementLocation(caseData.getCaseManagementLocation())
                .caseLinks(caseData.getCaseLinks())
                .applicantCaseName(caseData.getApplicantCaseName())
                .allPartyFlags(caseData.getAllPartyFlags())
                .manageOrders(caseData.getManageOrders())
                .attendHearing(caseData.getAttendHearing())
                .issueDate(caseData.getIssueDate())
                .build();
            String automatedHearingCaseDataJson = objectMappers.writerWithDefaultPrettyPrinter().writeValueAsString(automatedHearingCaseData);
            log.info("Automated Hearing Request Mapper: AutomatedHearingCaseData: {}", automatedHearingCaseDataJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return automatedHearingCaseData;
    }
}
