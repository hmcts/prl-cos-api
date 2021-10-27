package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.framework.context.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;

@NoArgsConstructor
public class TaskContextProvider {

    public static TaskContext empty() {
        return new DefaultTaskContext();
    }
}
