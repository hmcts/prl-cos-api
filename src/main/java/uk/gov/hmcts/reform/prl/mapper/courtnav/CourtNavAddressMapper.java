package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavAddress;

@Mapper(componentModel = "spring")
public interface CourtNavAddressMapper {
    Address map(CourtNavAddress courtNavAddress);
}
