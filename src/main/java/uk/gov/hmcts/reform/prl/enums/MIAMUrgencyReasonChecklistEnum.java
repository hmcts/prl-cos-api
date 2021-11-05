package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MIAMUrgencyReasonChecklistEnum {

    MIAMUrgencyReasonChecklistEnum_Value_1("There is risk to the life, liberty or physical safety of the prospective applicant or his or her family or his or her home; or"),
    MIAMUrgencyReasonChecklistEnum_Value_2("Any delay caused by MIAM would cause significant risk of a miscarriage of justice"),
    MIAMUrgencyReasonChecklistEnum_Value_3("Any delay caused by MIAM would cause unreasonable hardship to the prospective applicant"),
    MIAMUrgencyReasonChecklistEnum_Value_4("Any delay caused by MIAM would cause irretrievable problems in dealing with the dispute (including the irretrievable loss of significant evidence)"),
    MIAMUrgencyReasonChecklistEnum_Value_5("There  is a significant risk that in the period necessary to schedule and attend a MIAM, proceedings relating to the dispute will be brought in another state in which a valid claim to jurisdiction may exist, such that a court in that other State would be seized of the dispute before a court in England and Wales."),

    private final String displayedValue;


}
