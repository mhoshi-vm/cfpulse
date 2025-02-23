package org.tanzu.cfpulse.cf;

import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.doppler.RecentLogsRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CfController {
    private final CfService cfService;
    private final ReactorDopplerClient reactorDopplerClient;

    public CfController(CfService cfService, ReactorDopplerClient reactorDopplerClient) {
        this.cfService = cfService;
        this.reactorDopplerClient = reactorDopplerClient;
    }

    @GetMapping("/orgs")
    public List<Org> orgs() {
        List<Org> result = new ArrayList<>();
        cfService.allOrgs().forEach(organizationSummary -> {
            result.add(new Org(organizationSummary.getName()));
        });
        result.sort((r1, r2) -> r1.name.compareToIgnoreCase(r2.name));
        return result;
    }

    public record Org(String name) {
    }

    @GetMapping("/spaces")
    public List<Space> spaces(@RequestParam("org") String org) {
        List<Space> result = new ArrayList<>();
        cfService.allSpaces(org).forEach(spaceSummary -> {
            result.add(new Space(spaceSummary.getName()));
        });
        result.sort((r1, r2) -> r1.name.compareToIgnoreCase(r2.name));
        return result;
    }

    public record Space(String name) {
    }

    @GetMapping("/logs/{org}/{space}")
    public List<LogMessage> logs(@PathVariable("org") String org, @PathVariable("space") String space) {
        CloudFoundryOperations cloudFoundryOperations = cfService.createCloudFoundryOperations(org, space);
        GetApplicationRequest getApplicationRequest = GetApplicationRequest.builder().name("joke").build();
        Mono<ApplicationDetail> applicationDetailMono = cloudFoundryOperations.applications().get(getApplicationRequest);
        ApplicationDetail applicationDetail = applicationDetailMono.block();
        String applicationId = applicationDetail.getId();

        RecentLogsRequest recentLogsRequest = RecentLogsRequest.builder().applicationId(applicationId).build();
        Flux<Envelope> envelopeFlux = reactorDopplerClient.recentLogs(recentLogsRequest);
        List<Envelope> envelopes = envelopeFlux.collectList().block();
        for (Envelope envelope : envelopes) {
            LogMessage logMessage = envelope.getLogMessage();
            System.out.println(logMessage.getMessage());
        }

        return new ArrayList<>();
    }
}
