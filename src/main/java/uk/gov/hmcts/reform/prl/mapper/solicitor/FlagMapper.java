package uk.gov.hmcts.reform.prl.mapper.solicitor;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Component
public class FlagMapper {

    ReasonableAdjustmentsFlagMapper reasonableAdjustmentsFlagMapper;

    private FlagMapper() {

    }

    public Flags buildCaseFlags(CaseData caseData, Flags caseFlags) {

        AttendHearing attendHearing = caseData.getAttendHearing();

        if (!ObjectUtils.isEmpty(attendHearing)) {
            caseFlags = reasonableAdjustmentsFlagMapper.reasonableAdjustmentFlags(caseFlags, attendHearing);
        }

        return caseFlags;
    }
}
