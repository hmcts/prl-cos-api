package uk.gov.hmcts.reform.prl.services.validators;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.prl.models.Address;

import java.util.stream.Stream;

public class EventCheckerHelper {

    private EventCheckerHelper() {
    }

    public static boolean isEmptyAddress(Address address) {
        return ObjectUtils.isEmpty(address) || allEmpty(
            address.getAddressLine1(),
            address.getAddressLine2(),
            address.getAddressLine3(),
            address.getPostTown(),
            address.getCounty(),
            address.getCountry(),
            address.getPostCode());
    }


    public static boolean allEmpty(Object... properties) {
        return Stream.of(properties).allMatch(ObjectUtils::isEmpty);
    }

    public static boolean allNonEmpty(Object... properties) {
        return Stream.of(properties).allMatch(ObjectUtils::isNotEmpty);
    }

    public static boolean anyEmpty(Object... properties) {
        return Stream.of(properties).anyMatch(ObjectUtils::isEmpty);
    }

    public static boolean anyNonEmpty(Object... properties) {
        return Stream.of(properties).anyMatch(ObjectUtils::isNotEmpty);
    }
}
