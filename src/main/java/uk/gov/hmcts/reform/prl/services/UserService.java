package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private final IdamClient idamClient;

    public UserDetails getUserDetails(String authorisation) {
        return idamClient.getUserDetails(authorisation);
    }

    public UserInfo getUserInfo(String authorisation, UserRoles roleName) {
        UserDetails idamClientUserDetails = idamClient.getUserDetails(authorisation);

        return UserInfo.builder()
            .idamId(idamClientUserDetails.getId())
            .firstName(idamClientUserDetails.getFullName())
            .lastName(idamClientUserDetails.getSurname().get())
            .emailAddress(idamClientUserDetails.getEmail())
            .role(roleName.name())
            .build();
    }
}
