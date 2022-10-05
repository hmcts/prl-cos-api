package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Data
@Builder
public class OrdersToServeSA {

    @JsonUnwrapped
    @Builder.Default
    @JsonProperty("orderOptionsSoA")
    private final DynamicMultiSelectList orderOptionsSoA;

    public List<String> getSelectedOrders() {
        return Stream.of(OrdersToServeSA.class.getDeclaredFields()).filter(Objects::nonNull)
            .map(field -> {
                try {
                    return field.get(this);
                } catch (IllegalAccessException e) {
                    log.info("*** Error while returning selected orders ***");
                }
                return null;
            }).filter(Objects::nonNull)
            .map(Object::toString)
            .map(s -> s.substring(1, s.length() - 1))
            .collect(Collectors.toList());
    }
}
