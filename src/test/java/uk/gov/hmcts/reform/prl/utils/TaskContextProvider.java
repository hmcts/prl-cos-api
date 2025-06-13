package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.tasks.DefaultTaskContext;
import uk.gov.hmcts.reform.prl.tasks.TaskContext;

@NoArgsConstructor
public class TaskContextProvider {

    public static TaskContext empty() {
        return new DefaultTaskContext();
    }
}
