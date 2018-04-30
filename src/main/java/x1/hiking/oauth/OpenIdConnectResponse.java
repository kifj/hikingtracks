package x1.hiking.oauth;

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

  /*
   * (non-Javadoc)
   * @see org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse#init(java.lang.String, java.lang.String, int)
   */
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
   * @param issuer the issuer
   * @param audience the audience
   */
  public boolean checkId(String issuer, String audience) {
    return idToken.getClaimsSet().getIssuer().equals(issuer) && idToken.getClaimsSet().getAudience().equals(audience)
        && idToken.getClaimsSet().getExpirationTime() < System.currentTimeMillis();
  }

}
