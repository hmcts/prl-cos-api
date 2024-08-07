package uk.gov.hmcts.reform.prl.services.gatekeeping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.ListOnNoticeEmail;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ListOnNoticeService {

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
