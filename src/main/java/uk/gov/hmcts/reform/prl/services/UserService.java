package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.models.user.UserRoles;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private final IdamClient idamClient;

    public UserDetails getUserDetails(String authorisation) {
        return idamClient.getUserDetails(authorisation);
    }

    public UserInfo getUserInfo(String authorisation, UserRoles roleName) {
        UserDetails idamClientUserDetails = idamClient.getUserDetails(authorisation);
        Optional<String> surname = idamClientUserDetails.getSurname();
        return UserInfo.builder()
            .idamId(idamClientUserDetails.getId())
            .firstName(idamClientUserDetails.getFullName())
            .lastName(surname.orElse("Surname not present"))
            .emailAddress(idamClientUserDetails.getEmail())
            .role(roleName.name())
            .build();
    }

    public UserDetails getUserByUserId(String bearerToken, String userId) {
        return idamClient.getUserByUserId(bearerToken, userId);
    }

    public List<UserDetails> getUserByEmailId(String bearerToken, String emailId) {
        String searchQuery = "email:".concat(emailId);
        return idamClient.searchUsers(bearerToken, searchQuery);
    }
}
