package uk.gov.hmcts.reform.prl.courtnav.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.util.stream.Collectors;
import javax.json.JsonObject;

@Component
public class Fl401TypeOfApplicationMapper {

    public JsonObject map(CourtNavCaseData courtNavCaseData) {

        String orderAppliedForJson = null;

        if (courtNavCaseData.getOrdersAppliedFor() != null && !courtNavCaseData.getOrdersAppliedFor().isEmpty()) {
            orderAppliedForJson = courtNavCaseData.getOrdersAppliedFor()
                .stream()
                .map(FL401OrderTypeEnum::getDisplayedValue)
                .collect(Collectors.joining(", "));
        }


        return new NullAwareJsonObjectBuilder()
            .add("typeOfApplicationOrders", orderAppliedForJson)
            .build();
    }
}
