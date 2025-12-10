package uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;

/**
 * Generic interface for converting objects of type T to DynamicListElement.
 *
 * @param <T> the type of the source object to be converted
 */
public interface DynamicListElementConverter<T> {
    /**
     * Converts an object of type T to a DynamicListElement.
     *
     * @param source the source object to be converted
     * @return the resulting DynamicListElement
     */
    DynamicListElement convert(T source);
}
