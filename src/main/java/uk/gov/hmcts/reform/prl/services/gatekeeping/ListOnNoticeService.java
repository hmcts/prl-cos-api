package uk.gov.hmcts.reform.prl.services.gatekeeping;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;

import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;

@Slf4j
@Service
public class ListOnNoticeService {

    public String getReasonsSelected(Object listOnNoticeReasonsEnum,long caseId) {
        if (null != listOnNoticeReasonsEnum) {
            final String[] reasonsSelected = {BLANK_STRING};
            ((List<String>) listOnNoticeReasonsEnum).stream().forEach(reason ->
                reasonsSelected[0] = reasonsSelected[0].concat(ListOnNoticeReasonsEnum.getDisplayedValue(reason) + "\n\n"));
            return reasonsSelected[0];
        } else {
            log.info("***No Reasons selected for list on Notice for the case id: {}", caseId);
            return null;
        }
    }
}
