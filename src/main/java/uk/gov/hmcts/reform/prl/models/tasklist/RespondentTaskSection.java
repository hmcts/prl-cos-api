package uk.gov.hmcts.reform.prl.models.tasklist;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Getter
public class RespondentTaskSection {

    private final String name;
    private List<RespondentTask> respondentTasks;
    private String hint;
    private String info;

    private RespondentTaskSection(String name, List<RespondentTask> respondentTasks) {
        this.name = name;
        this.respondentTasks = respondentTasks;
    }

    public static RespondentTaskSection newSection(String name) {
        return new RespondentTaskSection(name, new ArrayList<>());
    }

    public RespondentTaskSection withTask(RespondentTask task) {
        respondentTasks.add(task);
        return this;
    }

    public RespondentTaskSection withHint(String hint) {
        this.hint = hint;
        return this;
    }

    public RespondentTaskSection withInfo(String info) {
        this.info = info;
        return this;
    }

    public Optional<String> getHint() {
        return Optional.ofNullable(hint);
    }

    public Optional<String> getInfo() {
        return Optional.ofNullable(info);
    }

    public boolean hasAnyTask() {
        return isNotEmpty(respondentTasks);
    }
}
