package uk.gov.hmcts.reform.prl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLib;
import uk.gov.hmcts.rse.ccd.lib.api.CFTLibConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class CftLibConfig implements CFTLibConfigurer {
    @Override
    public void configure(CFTLib lib) throws Exception {
        lib.createRoles(
            "citizen",
            "caseworker-privatelaw",
            "caseworker-privatelaw-bulkscan",
            "caseworker-privatelaw-bulkscansystemupdate",
            "caseworker-privatelaw-courtadmin",
            "caseworker-privatelaw-judge",
            "caseworker-privatelaw-la",
            "caseworker-privatelaw-solicitor",
            "caseworker-privatelaw-superuser",
            "caseworker-privatelaw-systemupdate",
            "payments",
            "pui-case-manager",
            "courtnav",
            "caseworker-wa-task-configuration",
            "caseworker-ras-validation",
            "GS_profile",
            );

        //var def = Files.readAllBytes(Path.of("build/ccd-config/ccd-A58-dev.xlsx"));
        //lib.importDefinition(def);
    }
}
