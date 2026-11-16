# Retire the RPA email export

Issuing a C100 case no longer publishes the JSON export notification. The manual resend endpoint, export mappers and their JSON-P dependencies have been removed. The existing court-admin notification remains for consent and non-consent applications, and the other solicitor and SendGrid notifications continue to use their existing flows.

This change is independent of court lookup. FaCT clients, feature toggles, court codes and allocation remain in place on this branch.

## Shared SendGrid sender

The remaining SendGrid notifications still need a sender address. Their configuration is now:

- Application property: `send-grid.email.from`.
- Environment variable: `SEND_GRID_FROM_EMAIL`.
- Existing Key Vault secret: `rpa-notify-from-email` (unchanged).

Jenkins and Helm read the existing `rpa-notify-from-email` secret and expose it as `SEND_GRID_FROM_EMAIL`. The legacy secret name remains because it supplies the shared sender used by the other notifications. No Key Vault secret or sender-address change is required. The retired recipient secret is no longer read. For local development, provide the existing sender address as `SEND_GRID_FROM_EMAIL`.

Deploy the companion CCD definition that removes the retired resend event before, or alongside, this service change so the event is no longer offered in XUI.

## Validation

The issue-notification tests verify that consent and non-consent applications each publish only the existing court-admin notification. The remaining SendGrid tests verify that template and transfer requests retain their configured sender and case-reference metadata.

```sh
./gradlew test checkstyleMain checkstyleTest checkstyleIntegrationTest checkstyleFunctionalTest checkstyleSmokeTest checkstyleContractTest
./gradlew integration --tests uk.gov.hmcts.reform.prl.controllers.CallbackControllerIntegrationTest --tests uk.gov.hmcts.reform.prl.controllers.C100IssueCaseControllerIntegrationTest
```

After this change merges, merge the updated master into the FaCT retirement branch so the shared removals drop out of that PR's diff. Bump that branch's chart version again if it makes further Helm changes.
