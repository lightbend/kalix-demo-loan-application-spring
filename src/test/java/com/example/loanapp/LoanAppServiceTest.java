package com.example.loanapp;

import com.example.loanapp.api.LoanAppApi;
import com.example.loanapp.api.LoanAppService;
import com.example.loanapp.domain.LoanAppDomainEvent;
import com.example.loanapp.domain.LoanAppDomainState;
import com.example.loanapp.domain.LoanAppDomainStatus;
import kalix.javasdk.testkit.EventSourcedResult;
import kalix.springsdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class LoanAppServiceTest {

    @Test
    public void happyPath(){
        var loanAppId = UUID.randomUUID().toString();
        EventSourcedTestKit<LoanAppDomainState, LoanAppService> testKit = EventSourcedTestKit.of(loanAppId,LoanAppService::new);

        var submitRequest = new LoanAppApi.SubmitRequest(
                "clientId",
                5000,
                2000,
                36);
        EventSourcedResult<LoanAppApi.EmptyResponse> submitResult = testKit.call(service -> service.submit(submitRequest));
        LoanAppDomainEvent.Submitted submittedEvent = submitResult.getNextEventOfType(LoanAppDomainEvent.Submitted.class);
        assertEquals(loanAppId,submittedEvent.loanAppId());

        LoanAppDomainState updatedStat = (LoanAppDomainState)submitResult.getUpdatedState();
        assertEquals(LoanAppDomainStatus.STATUS_IN_REVIEW,updatedStat.status());

        EventSourcedResult<LoanAppApi.EmptyResponse> approveResponse = testKit.call(service -> service.approve());
        LoanAppDomainEvent.Approved approvedEvent = approveResponse.getNextEventOfType(LoanAppDomainEvent.Approved.class);
        assertEquals(loanAppId,approvedEvent.loanAppId());

        updatedStat = (LoanAppDomainState)approveResponse.getUpdatedState();
        assertEquals(LoanAppDomainStatus.STATUS_APPROVED,updatedStat.status());
    }
}
