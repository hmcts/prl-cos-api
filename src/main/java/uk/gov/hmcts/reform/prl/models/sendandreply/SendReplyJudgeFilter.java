package uk.gov.hmcts.reform.prl.models.sendandreply;

import io.micrometer.core.instrument.util.StringUtils;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

public class SendReplyJudgeFilter {

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JudicialUser)) {
            return false;
        }

        JudicialUser judicialUser = (JudicialUser) obj;
        System.out.println("judicialUser.getIdamId() " + judicialUser.getIdamId());
        System.out.println("judicialUser.getPersonalCode() " + judicialUser.getPersonalCode());
        if (StringUtils.isEmpty(judicialUser.getIdamId()) || StringUtils.isEmpty(judicialUser.getPersonalCode())) {
            return false;
        }
        return true;
    }

}
