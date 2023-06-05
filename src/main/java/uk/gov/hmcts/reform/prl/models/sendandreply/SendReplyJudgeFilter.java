package uk.gov.hmcts.reform.prl.models.sendandreply;

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
        if (judicialUser == null) {
            return false;
        }
        return (judicialUser.getIdamId() != null && judicialUser.getPersonalCode() != null);
    }

}
