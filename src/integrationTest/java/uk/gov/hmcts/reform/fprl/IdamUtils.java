package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fprl.model.CreateUserRequest;
import uk.gov.hmcts.reform.fprl.model.UserCode;

import java.util.Base64;
import java.util.UUID;

@Slf4j
public class IdamUtils {

    @Value("${idam.user.genericpassword}")
    private String genericPassword;

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${auth.idam.client.redirectUri}")
    private String idamRedirectUri;

    @Value("${idam.client.aat.authorize.context-path}")
    private String idamAuthorizeContextPath;

    @Value("${idam.client.aat.token.context-path}")
    private String idamTokenContextPath;

    @Value("${auth.idam.client.clientId}")
    private String idamAuthClientID;

    @Value("${auth.idam.client.secret}")
    private String idamSecret;

    @Value("${idam.s2s-auth.url}")
    private String idamS2sAuthUrl;

    @Value("${docmosis.service.base.url}")
    private String docmosisBaseUrl;

    @Value("${ccd.document.base.url}")
    private String ccdDocumentBaseUrl;

    private String idamUsername;

    private int responseCode;


    private void createUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .email(username)
            .password(password)
            .forename("Test")
            .surname("User")
            .roles(new UserCode[]{UserCode.builder().code("citizen").build()})
            .build();

        SerenityRest.given()
            .header("Content-Type", "application/json")
            .body(ResourceLoader.objectToJson(userRequest))
            .post(idamCreateUrl());
    }

    public void createCaseworkerUserInIdam(String username, String password) {
        CreateUserRequest userRequest = CreateUserRequest.builder()
            .email(username)
            .password(password)
            .forename("Henry")
            .surname("Harper")
            .roles(new UserCode[]{UserCode.builder().code("caseworker-privatelaw-solicitor").build()})
            .build();

        SerenityRest.given()
            .header("Content-Type", "application/json")
            .body(ResourceLoader.objectToJson(userRequest))
            .post(idamCreateUrl());
    }

    public String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));
        Response response = null;

        response = SerenityRest.given()
            .header("Authorization", authHeader)
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamCodeUrl());

        responseCode = response.getStatusCode();

        if (response.getStatusCode() >= 300) {
            throw new IllegalStateException("Token generation failed with code: " + response.getStatusCode()
                + " body: " + response.getBody().prettyPrint());
        }

        response = SerenityRest.given()
            .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .relaxedHTTPSValidation()
            .post(idamTokenUrl(response.getBody().path("code")));

        assert response.getStatusCode() == 200 : "Error generating code from IDAM: " + response.getStatusCode();

        String token = response.getBody().path("access_token");
        return "Bearer " + token;
    }

    public String generateUrlForValidMicroservice(String microservicename) {
        String generatedURL = null;

        if (microservicename.equals("IDAM")) {
            generatedURL = idamUserBaseUrl + "/health";
        } else if (microservicename.equals("DOCMOSIS")) {
            generatedURL = docmosisBaseUrl + "/health";
        } else if (microservicename.equals("CCDDOCUMENT")) {
            generatedURL = ccdDocumentBaseUrl + "/health";
        }
        return generatedURL;
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String idamCodeUrl() {

        System.out.println(idamUserBaseUrl + idamAuthorizeContextPath
            + "?response_type=code"
            + "&client_id=" + idamAuthClientID
            + "&redirect_uri=" + idamRedirectUri);

        return idamUserBaseUrl + idamAuthorizeContextPath
            + "?response_type=code"
            + "&client_id=" + idamAuthClientID
            + "&redirect_uri=" + idamRedirectUri;
    }

    private String idamTokenUrl(String code) {

        System.out.println(idamUserBaseUrl + idamTokenContextPath
            + "?code=" + code
            + "&client_id=" + idamAuthClientID
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code");

        return idamUserBaseUrl + idamTokenContextPath
            + "?code=" + code
            + "&client_id=" + idamAuthClientID
            + "&client_secret=" + idamSecret
            + "&redirect_uri=" + idamRedirectUri
            + "&grant_type=authorization_code";

    }

}
