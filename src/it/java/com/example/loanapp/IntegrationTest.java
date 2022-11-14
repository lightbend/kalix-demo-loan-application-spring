package com.example.loanapp;

import kalix.springsdk.testkit.KalixIntegrationTestKitSupport;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.loanapp.api.LoanAppApi;
import com.example.loanapp.domain.LoanAppDomainStatus;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This is a skeleton for implementing integration tests for a Kalix application built with the Spring SDK.
 *
 * This test will initiate a Kalix Proxy using testcontainers and therefore it's required to have Docker installed
 * on your machine. This test will also start your Spring Boot application.
 *
 * Since this is an integration tests, it interacts with the application using a WebClient
 * (already configured and provided automatically through injection).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class)
public class IntegrationTest extends KalixIntegrationTestKitSupport {

  @Autowired
  private WebClient webClient;

  private Duration timeout = Duration.of(5, ChronoUnit.SECONDS);
  @Test
  public void loanAppHappyPath() throws Exception {
      var loanAppId = UUID.randomUUID().toString();
      var submitRequest = new LoanAppApi.SubmitRequest(
              "clientId",
              5000,
              2000,
              36);

      ResponseEntity<LoanAppApi.EmptyResponse> emptyRes =
              webClient.post()
                      .uri("/loanapp/"+loanAppId+"/submit")
                      .bodyValue(submitRequest)
                      .retrieve()
                      .toEntity(LoanAppApi.EmptyResponse.class)
                      .block(timeout);

      assertEquals(HttpStatus.OK,emptyRes.getStatusCode());

      LoanAppApi.GetResponse getRes =
              webClient.get()
                      .uri("/loanapp/"+loanAppId)
                      .retrieve()
                      .bodyToMono(LoanAppApi.GetResponse.class)
                      .block(timeout);

      assertEquals(LoanAppDomainStatus.STATUS_IN_REVIEW,getRes.state().status());

      emptyRes =
              webClient.post()
                      .uri("/loanapp/"+loanAppId+"/approve")
                      .retrieve()
                      .toEntity(LoanAppApi.EmptyResponse.class)
                      .block(timeout);

      getRes =
              webClient.get()
                      .uri("/loanapp/"+loanAppId)
                      .retrieve()
                      .bodyToMono(LoanAppApi.GetResponse.class)
                      .block(timeout);

      assertEquals(LoanAppDomainStatus.STATUS_APPROVED,getRes.state().status());
  }

}