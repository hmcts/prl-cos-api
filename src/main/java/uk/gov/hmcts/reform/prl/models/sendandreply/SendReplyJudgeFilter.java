package uk.gov.hmcts.reform.prl.models.sendandreply;

import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;

public class SendReplyJudgeFilter {

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof JudicialUser)) {
            return false;
        }

        JudicialUser judicialUser = (JudicialUser) obj;
        return judicialUser.getIdamId().length() > 1 && judicialUser.getPersonalCode().length() > 1;
    }

}
