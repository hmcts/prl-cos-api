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
            "caseworker-privatelaw-judge",
            "caseworker-privatelaw-courtadmin",
            "caseworker-privatelaw-la",
            "caseworker-privatelaw-superuser",
            "caseworker-privatelaw-solicitor",
            "caseworker-privatelaw-systemupdate",
            "citizen",
            "courtnav",
            "payments",
            "caseworker-wa-task-configuration",
            "caseworker-ras-validation",
            "GS_profile",
            "caseworker-caa",
            "caseworker-privatelaw-bulkscansystemupdate",
            "caseworker-privatelaw-bulkscan",
            "pui-case-manager",
            "caseworker-approver",
            "caseworker-privatelaw-cafcass",
            "caseworker-privatelaw-externaluser-viewonly"
        );

        var def = Files.readAllBytes(Path.of("bin/ccd-config-PRL-local.xlsx"));
        lib.importDefinition(def);
    }
}

