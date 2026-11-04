# Court allocation proof of concept

Court allocation uses OS Places, the existing postcode/local-authority mapping and Location RefData. There is no secondary court-directory client or county-code lookup.

## Court details

- C100 postcode allocation always uses the OS/local-authority route. Specific postcode mappings still take precedence over OS Places.
- Submission, payment, resubmission, citizen court search and solicitor notifications use RefData `site_name`, falling back to `court_name` when the site name is blank.
- Issue and transfer continue to use RefData `court_name` and the existing region, seal and case-management-location mapping.
- `courtId` contains the RefData EPIMMS ID. It no longer contains a county court code.
- FL401 submission continues to use its court dropdown. Resolving an existing FL401 court uses its allocated location or court ID, without a postcode search.

## Email recipients and sender

C100 issue takes the court-admin email from the selected configured dropdown entry (`EPIMMS:email`). An empty email suffix remains empty. FL401 submission retains its configured dropdown email, and a manual offline transfer retains the address entered by the user.

The JSON export notification, resend endpoint, event listener and JSON mappers have been removed. Court-admin notifications and other SendGrid email functions remain.

Those other SendGrid functions require the shared sender address:

- Local environment: `SEND_GRID_FROM_EMAIL`.
- Application property: `send-grid.email.from`.
- Deployment secret: `send-grid-from-email`, exposed as `SEND_GRID_FROM_EMAIL`.

Before deploying, provision `send-grid-from-email` with the existing approved sender address. Renaming the configuration does not create a Key Vault secret. For local development, set the new variable in the ignored `.aat-env`.

The companion CCD definition change removes the retired resend event, obsolete county-code field and associated permissions. Import that definition with this service change. Historical case data is not purged.

## Verification

The full unit suite and Java Checkstyle tasks cover the remaining allocation, issue, transfer and notification behavior. `CourtAllocationIntegrationTest` exercises the real OS and RefData Feign clients against local HTTP stubs: C100 allocation with a configured email, an empty venue response, and FL401 selected-court resolution without OS.

```sh
./gradlew test checkstyleMain checkstyleTest checkstyleIntegrationTest checkstyleFunctionalTest checkstyleSmokeTest checkstyleContractTest
./gradlew integration --tests uk.gov.hmcts.reform.prl.clients.CourtAllocationIntegrationTest
```

For the portal demonstration:

1. Submit and pay for a C100 case; check the RefData site name and EPIMMS court ID in case data.
2. Issue to a configured court with a non-empty email; check `localCourtAdmin` and its court-admin notification.
3. Issue to a configured court with an empty email; check that no directory email is substituted.
4. Transfer using the manual offline option; check the entered `courtEmailAddress` and transfer notification.
5. Submit an FL401 case using its court dropdown and check its existing recipient/name behavior.

The allocation log is `Resolved court from OS and RefData: epimmsId=..., siteName=..., courtName=...`. Automated HTTP checks establish behavior against local stubs; live portal and notification delivery still need the demonstration above.
