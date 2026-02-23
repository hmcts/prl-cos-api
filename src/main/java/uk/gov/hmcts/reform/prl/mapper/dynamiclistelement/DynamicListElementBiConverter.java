package uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;

/**
 * Generic interface for converting between DynamicListElement and objects of type T and R.
 *
 * @param <T> the type of the source object to be converted to DynamicListElement
 * @param <R> the type of the object to be converted from DynamicListElement
 */
public interface DynamicListElementBiConverter<T, R> extends DynamicListElementConverter<T> {
    /**
     * Converts a DynamicListElement to an object of type R.
     *
     * @param element the DynamicListElement to be converted
     * @return the resulting object of type R
     */
    R convertFromDynamicListElement(DynamicListElement element);
}
