package uk.gov.hmcts.reform.prl.mapper.staffresponse;

import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.Optional;

/**
 * Generic interface for filtering StaffResponse objects and transforming them into another type R.
 *
 * @param <R>
 */
public interface StaffResponseFilter<R> {

    /**
     * Filters the given StaffResponse and transforms it into an Optional of type R.
     *
     * @param source the StaffResponse to be filtered
     * @return an Optional containing the transformed object of type R if the filter criteria are met,
     * otherwise an empty Optional
     */
    Optional<R> filter(StaffResponse source);
}
