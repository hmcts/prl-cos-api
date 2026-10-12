package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingTypeEnum {

    @JsonProperty("ABA5-ALL")
    ABA5ALL("ABA5-ALL","Allocation","Dyrannu"),
    @JsonProperty("ABA5-APL")
    ABA5APL("ABA5-APL","Appeal","Apêl"),
    @JsonProperty("ABA5-APP")
    ABA5APP("ABA5-APP","Application","Cais"),
    @JsonProperty("ABA5-BRE")
    ABA5BRE("ABA5-BRE","Breach","Torri Amodau"),
    @JsonProperty("ABA5-CMC")
    ABA5CMC("ABA5-CMC","Case Management Conference","Cynhadledd Rheoli Achos"),
    @JsonProperty("ABA5-CMH")
    ABA5CMH("ABA5-CMH","Case Management Hearing","Gwrandawiad Rheoli Achos"),
    @JsonProperty("ABA5-COM")
    ABA5COM("ABA5-COM","Committal","Traddodi"),
    @JsonProperty("ABA5-CON")
    ABA5CON("ABA5-CON","Conciliation","Cymodi"),
    @JsonProperty("ABA5-COS")
    ABA5COS("ABA5-COS","Costs","Costau"),
    @JsonProperty("ABA5-DIR")
    ABA5DIR("ABA5-DIR","Directions (First/Further)","Cyfarwyddiadau (Cyntaf/Pellach)"),
    @JsonProperty("ABA5-DRA")
    ABA5DRA("ABA5-DRA","Dispute Resolution Appointment","Apwyntiad Datrys Anghydfod"),
    @JsonProperty("ABA5-FOF")
    ABA5FOF("ABA5-FOF","Finding of Fact","Canfod y Ffeithiau"),
    @JsonProperty("ABA5-FHR")
    ABA5FHR("ABA5-FHR","First Hearing","Gwrandawiad Cyntaf"),
    @JsonProperty("ABA5-FFH")
    ABA5FFH("ABA5-FFH","Full/Final hearing","Gwrandawiad Llawn/Terfynol"),
    @JsonProperty("ABA5-FCM")
    ABA5FCM("ABA5-FCM","Further Case Management Hearing","Gwrandawiad Rheoli Achos Pellach"),
    @JsonProperty("ABA5-2GA")
    ABA52GA("ABA5-2GA","2nd Gatekeeping Appointment","2il Apwyntiad Neilltuo"),
    @JsonProperty("ABA5-GRH")
    ABA5GRH("ABA5-GRH","Ground Rules Hearing","Gwrandawiad Rheolau Sylfaenol"),
    @JsonProperty("ABA5-HRA")
    ABA5HRA("ABA5-HRA","Human Rights Act Application","Cais dan y Ddeddf Hawliau Dynol"),
    @JsonProperty("ABA5-JMT")
    ABA5JMT("ABA5-JMT","Judgment","Dyfarniad"),
    @JsonProperty("ABA5-NEH")
    ABA5NEH("ABA5-NEH","Neutral Evaluation Hearing","Gwrandawiad Gwerthusiad Niwtral"),
    @JsonProperty("ABA5-PER")
    ABA5PER("ABA5-PER","Permission Hearing","Gwrandawiad Caniatâd"),
    @JsonProperty("ABA5-PHR")
    ABA5PHR("ABA5-PHR","Pre Hearing Review","Adolygiad Cyn Gwrandawiad"),
    @JsonProperty("ABA5-REV")
    ABA5REV("ABA5-REV","Review","Adolygiad"),
    @JsonProperty("ABA5-SGA")
    ABA5SGA("ABA5-SGA","Safeguarding Gatekeeping Appointment","Apwyntiad Neilltuo Diogelwch"),
    @JsonProperty("ABA5-SCF")
    ABA5SCF("ABA5-SCF","Settlement Conference","Cynhadledd Setlo"),
    @JsonProperty("ABA5-FHD")
    ABA5FHD("ABA5-FHD","First Hearing Dispute Resolution Appointment (FHDRA)","Apwyntiad Datrys Anghydfod Gwrandawiad Cyntaf (FHDRA)"),
    @JsonProperty("ABA5-DCH")
    ABA5DCH("ABA5-DCH","Decision Hearing","Gwrandawiad Penderfynu");


    private final String id;
    private final String displayedValue;
    private final String displayedValueWelsh;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getDisplayedValueWelsh() {
        return displayedValueWelsh;
    }

    @JsonCreator
    public static HearingTypeEnum getValue(String key) {
        return HearingTypeEnum.valueOf(key);
    }

    public static HearingTypeEnum getIdFromValue(String value) {
        return Arrays.stream(HearingTypeEnum.values())
            .filter(i -> i.getDisplayedValue().equals(value))
            .findFirst().orElse(null);
    }

    public static String getDisplayedValueInWelshFromDisplayValueString(String enteredValue) {
        if (StringUtils.isEmpty(enteredValue)) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(HearingTypeEnum.values())
            .map(i -> HearingTypeEnum.getIdFromValue(enteredValue))
            .filter(Objects::nonNull)
            .map(i -> i.displayedValueWelsh)
            .findFirst().orElse(enteredValue);
    }
}


