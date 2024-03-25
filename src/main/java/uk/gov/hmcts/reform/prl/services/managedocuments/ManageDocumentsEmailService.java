package  uk.gov.hmcts.reform.prl.services.managedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ManageDocumentsEmailService {

    private final EmailService emailService;
    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String citizenUrl;

    private final SendgridService sendgridService;


    public EmailNotificationDetails sendEmailUsingTemplateWithAttachments(String authorization,
                                                                          String email,
                                                                          SendgridEmailTemplateNames template,
                                                                          Map<String, Object> dynamicData,
                                                                          String servedParty) {
        try {
            sendgridService.sendEmailUsingTemplateWithAttachments(template, authorization, SendgridEmailConfig.builder()
                    .toEmailAddress(email)
                    .dynamicTemplateData(dynamicData)
                   .languagePreference(LanguagePreference.english).build()
            );
        } catch (IOException e) {
            log.error("there is a failure in sending email for email {} with exception {}", email,e.getMessage());
        }
        return null;
    }

}

