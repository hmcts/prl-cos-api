package uk.gov.hmcts.reform.prl.services.noc;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.RespondentSolicitor;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ChangeOfRepresentationService {

    private final IdentityService identityService;

    public List<Element<ChangeOfRepresentation>> changeRepresentative(
        ChangeOfRepresentationRequest changeOfRepresentationRequest) {

        List<Element<ChangeOfRepresentation>> changeOfRepresentatives = Lists.newArrayList(ElementUtils.nullSafeList(
            changeOfRepresentationRequest.getCurrent()));

        changeOfRepresentatives.add(ElementUtils.element(identityService.generateId(),
            ChangeOfRepresentation.builder()
                .via(changeOfRepresentationRequest.getMethod().getLabel())
                .by(changeOfRepresentationRequest.getBy())
                .date(LocalDateTime.now().toLocalDate())
                .removed(Optional.ofNullable(changeOfRepresentationRequest.getRemovedRepresentative())
                    .map(this::from)
                    .orElse(null))
                .added(Optional.ofNullable(changeOfRepresentationRequest.getAddedRepresentative())
                    .map(this::from)
                    .orElse(null))
                .build()));

        changeOfRepresentatives.sort(Comparator.comparing(e -> e.getValue().getDate()));

        return changeOfRepresentatives;
    }

    private ChangedRepresentative from(RespondentSolicitor solicitor) {
        return ChangedRepresentative.builder()
            .firstName(solicitor.getFirstName())
            .lastName(solicitor.getLastName())
            .email(solicitor.getEmail())
            .organisation(solicitor.getOrganisation())
            .build();
    }
}
