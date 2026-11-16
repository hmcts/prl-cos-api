# Court allocation proof of concept

Court allocation uses OS Places, the existing postcode/local-authority mapping and Location RefData. There is no secondary court-directory client or county-code lookup.

## Court details

- C100 postcode allocation always uses the OS/local-authority route. Specific postcode mappings still take precedence over OS Places.
- Postcode override matching ignores letter case and surrounding whitespace. CSV prefixes are trimmed and uppercased when loaded; blank prefixes are ignored.
- Court onboarding returns HTTP 503 when the OS request fails, allowing the frontend to show its retry message. A successful lookup with no matching court returns HTTP 404.
- Submission, payment, resubmission, citizen court search and solicitor notifications use RefData `site_name`. Callers handle a missing or blank site name without falling back to `court_name`.
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

The companion CCD definition change removes the retired resend event. Import that definition with this service change. Historical case data is not purged.

The `courtCodeFromFact` text field and its existing CCD permissions remain for compatibility with historical cases. COS also retains the model field so that reading, updating and writing existing case data preserves the value. New court allocation does not populate or use it, and new cases omit it. Removing the field definition before historical data is migrated would cause CCD to reject callback responses containing the old field.

## Verification

The unit suite and Java Checkstyle tasks cover the remaining allocation, issue, transfer and notification behavior. `CourtAllocationIntegrationTest` exercises the real OS and RefData Feign clients against local HTTP stubs: C100 allocation with a configured email; onboarded, offline and unconfigured court decisions; postcode overrides with uppercase, lowercase and unspaced inputs; empty OS and venue results; OS service failure; and FL401 selected-court resolution without OS.

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
6. Update a historical case containing `courtCodeFromFact`; check that CCD accepts the callback and the existing value is preserved.

The allocation log is `Resolved court from OS and RefData: epimmsId=..., siteName=..., courtName=...`. Automated HTTP checks establish behavior against local stubs; live portal and notification delivery still need the demonstration above.
