package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CourtOrderTypeEnum {
    changeChildrenNameSurname("Changing the children's names or surname"),
    allowMedicalTreatment("Allowing medical treatment to be carried out on the children"),
    takingChildOnHoliday("Taking the children on holiday"),
    relocateChildrenDifferentUkArea("Relocating the children to a different area in England and Wales"),
    relocateChildrenOutsideUk("Relocating the children outside of England and Wales (including Scotland and Northern Ireland)"),
    specificHoliday("A specific holiday or arrangement"),
    whatSchoolChildrenWillGoTo("What school the children will go to"),
    religiousIssue("A religious issue"),
    changeChildrenNameSurnameA("Changing the children's names or surname"),
    medicalTreatment("Medical treatment"),
    relocateChildrenDifferentUkAreaA("Relocating the children to a different area in England and Wales"),
    relocateChildrenOutsideUkA("Relocating the children outside of England and Wales (including Scotland and Northern Ireland)"),
    returningChildrenToYourCare("Returning the children to your care");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CourtOrderTypeEnum getValue(String key) {
        return CourtOrderTypeEnum.valueOf(key);
    }
}
