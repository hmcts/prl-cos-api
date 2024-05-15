package uk.gov.hmcts.reform.prl.services.gatekeeping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ListOnNoticeEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListOnNoticeService {

    private final EmailService emailService;

    public String getReasonsSelected(Object listOnNoticeReasonsEnum, long caseId) {
        if (null != listOnNoticeReasonsEnum) {
            final String[] reasonsSelected = {BLANK_STRING};
            ((List<String>) listOnNoticeReasonsEnum).stream().forEach(reason ->
                                                                          reasonsSelected[0] = reasonsSelected[0].concat(
                                                                              ListOnNoticeReasonsEnum.getDisplayedValue(
                                                                                  reason) + "\n\n"));
            return reasonsSelected[0];
        } else {
            log.info("***No Reasons selected for list on Notice for the case id: {}", caseId);
            return null;
        }
    }

    public void sendNotification(CaseData caseData, String selectedAndAdditionalReasons) {
        List<Element<PartyDetails>> applicantsInCase = caseData.getApplicants();
        if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
            StringBuilder finalSelectedAndAdditionalReasons
                = new StringBuilder(selectedAndAdditionalReasons.replace("\n\n", "\n• "))
                .insert(0,"• ");
            applicantsInCase.forEach(applicant -> {
                if (StringUtils.isNotEmpty(applicant.getValue().getSolicitorEmail())) {
                    log.info(
                        "Sending the email notification to applicant solicitor for List on Notice for caseId {}",
                        caseData.getId()
                    );
                    emailService.send(
                        applicant.getValue().getSolicitorEmail(),
                        EmailTemplateNames.LIST_ON_NOTICE_EMAIL_NOTIFICATION,
                        buildListOnNoticeEmail(
                            caseData,
                            applicant.getValue().getRepresentativeFirstName()
                                + EMPTY_SPACE_STRING
                                + applicant.getValue().getRepresentativeLastName(),
                            finalSelectedAndAdditionalReasons.toString()
                        ),
                        LanguagePreference.getPreferenceLanguage(caseData)
                    );
                } else {
                    log.info(
                        "Sending the email notification to applicant for List on Notice for caseId {}",
                        caseData.getId()
                    );
                    emailService.send(
                        applicant.getValue().getEmail(),
                        EmailTemplateNames.LIST_ON_NOTICE_EMAIL_NOTIFICATION,
                        buildListOnNoticeEmail(
                            caseData,
                            applicant.getValue().getFirstName()
                                + EMPTY_SPACE_STRING
                                + applicant.getValue().getLastName(),
                            finalSelectedAndAdditionalReasons.toString()
                        ),
                        LanguagePreference.getPreferenceLanguage(caseData)
                    );
                }
            });
        }
    }

    public EmailTemplateVars buildListOnNoticeEmail(CaseData caseData, String fullName, String selectedAndAdditionalReasons) {

        return ListOnNoticeEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .fullName(fullName)
            .caseNote(selectedAndAdditionalReasons)
            .build();
    }

    public void cleanUpListOnNoticeFields(Map<String, Object> caseDataUpdated) {
        String[] listOnNoticeFields = {SELECTED_AND_ADDITIONAL_REASONS, LIST_ON_NOTICE_REASONS_SELECTED};
        for (String field : listOnNoticeFields) {
            if (caseDataUpdated.containsKey(field)) {
                caseDataUpdated.put(field, null);
            }
        }
    }
}
