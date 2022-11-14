package com.example.loanapp.api;

import com.example.loanapp.domain.LoanAppDomainState;

public sealed interface LoanAppApi {
    record SubmitRequest(String clientId,
                         Integer clientMonthlyIncomeCents,
                         Integer loanAmountCents,
                         Integer loanDurationMonths) implements LoanAppApi{}
    record DeclineRequest(String reason) implements LoanAppApi{}

    record EmptyResponse()implements LoanAppApi{
        public static EmptyResponse of(){
            return new EmptyResponse();
        }
    }

    record GetResponse(LoanAppDomainState state) implements LoanAppApi{}
}
