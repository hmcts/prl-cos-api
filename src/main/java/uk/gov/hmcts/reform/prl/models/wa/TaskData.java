package uk.gov.hmcts.reform.prl.models.wa;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskData {
    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;

    private String assignee;
    private String type;

    @JsonProperty("task_state")
    private String taskState;

    @JsonProperty("task_system")
    private String taskSystem;

    @JsonProperty("security_classification")
    private String securityClassification;

    @JsonProperty("task_title")
    private String taskTitle;
    
    private String createdDate;
    private String dueDate;

    @JsonProperty("location_name")
    private String locationName;

    private String location;

    @JsonProperty("execution_type")
    private String executionType;

    private String jurisdiction;

    private String region;

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("case_category")
    private String caseCategory;

    @JsonProperty("work_type_id")
    private String workTypeId;

    @JsonProperty("work_type_label")
    private String workTypeLabel;

    private String description;

    @JsonProperty("role_category")
    private String roleCategory;

    @JsonProperty("minor_priority")
    private int minorPriority;

    @JsonProperty("major_priority")
    private int majorPriority;

    private String priorityDate;
    private boolean completeTask;
    private Permissions permissions;

    @JsonProperty("additional_properties")
    private AdditionalProperties additionalProperties;

    @JsonProperty("warning_list")
    private WarningList warningList;

    @Data
    public static class WarningList {
        private List<String> values;
    }

    @Data
    public static class Permissions {
        private List<String> values;
    }
}
