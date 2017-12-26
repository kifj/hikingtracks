package x1.hiking.oauth;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.jwt.JWT;
import org.apache.oltu.oauth2.jwt.io.JWTReader;

/**
 * OpenID Connect response
 * 
 * @author joe
 *
 */
public class OpenIdConnectResponse extends OAuthJSONAccessTokenResponse {
  public static final String ID_TOKEN = "id_token";
  private JWT idToken;

  @Override
  protected void init(String body, String contentType, int responseCode) throws OAuthProblemException {
    super.init(body, contentType, responseCode);
    idToken = new JWTReader().read(getParam(ID_TOKEN));
  }

  public final JWT getIdToken() {
    return idToken;
  }

  /**
   * ID Token Validation as per OpenID Connect Basic Client Profile 1.0 draft 22
   * Section 2.4
   *
   * @param issuer
   * @param audience
   */
  public boolean checkId(String issuer, String audience) {
    return idToken.getClaimsSet().getIssuer().equals(issuer) && idToken.getClaimsSet().getAudience().equals(audience)
        && idToken.getClaimsSet().getExpirationTime() < System.currentTimeMillis();
  }

}
