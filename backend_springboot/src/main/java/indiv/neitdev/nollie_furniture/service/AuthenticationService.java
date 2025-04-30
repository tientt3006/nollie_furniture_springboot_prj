package indiv.neitdev.nollie_furniture.service;


import com.nimbusds.jose.JOSEException;
import indiv.neitdev.nollie_furniture.dto.request.AuthenticationRequest;
import indiv.neitdev.nollie_furniture.dto.request.IntrospectRequest;
import indiv.neitdev.nollie_furniture.dto.response.AuthenticationResponse;
import indiv.neitdev.nollie_furniture.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);
    IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException;
}
