package x1.oauth;

import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;

/**
 * OAuth 2 parameters
 * 
 * @author joe
 *
 */
public class OAuthHelper {

  public void validateAuthorizationParams(OAuthParams oauthParams) throws OAuthProblemException {
    String authzEndpoint = oauthParams.getAuthzEndpoint();
    String tokenEndpoint = oauthParams.getTokenEndpoint();
    String clientId = oauthParams.getClientId();
    String clientSecret = oauthParams.getClientSecret();
    String redirectUri = oauthParams.getRedirectUri();

    StringBuilder sb = new StringBuilder();

    if (StringUtils.isEmpty(authzEndpoint)) {
      sb.append("Authorization Endpoint ");
    }

    if (StringUtils.isEmpty(tokenEndpoint)) {
      sb.append("Token Endpoint ");
    }

    if (StringUtils.isEmpty(clientId)) {
      sb.append("Client ID ");
    }

    if (StringUtils.isEmpty(clientSecret)) {
      sb.append("Client Secret ");
    }

    if (StringUtils.isEmpty(redirectUri)) {
      sb.append("Redirect URI");
    }

    String incorrectParams = sb.toString();
    if ("".equals(incorrectParams)) {
      return;
    }
    throw OAuthProblemException.error("Incorrect parameters: " + incorrectParams);

  }

  public void validateTokenParams(OAuthParams oauthParams) throws OAuthProblemException {
    String authzEndpoint = oauthParams.getAuthzEndpoint();
    String tokenEndpoint = oauthParams.getTokenEndpoint();
    String clientId = oauthParams.getClientId();
    String clientSecret = oauthParams.getClientSecret();
    String redirectUri = oauthParams.getRedirectUri();
    String authzCode = oauthParams.getAuthzCode();

    StringBuilder sb = new StringBuilder();

    if (StringUtils.isEmpty(authzCode)) {
      sb.append("Authorization Code ");
    }

    if (StringUtils.isEmpty(authzEndpoint)) {
      sb.append("Authorization Endpoint ");
    }

    if (StringUtils.isEmpty(tokenEndpoint)) {
      sb.append("Token Endpoint ");
    }

    if (StringUtils.isEmpty(clientId)) {
      sb.append("Client ID ");
    }

    if (StringUtils.isEmpty(clientSecret)) {
      sb.append("Client Secret ");
    }

    if (StringUtils.isEmpty(redirectUri)) {
      sb.append("Redirect URI");
    }

    String incorrectParams = sb.toString();
    if ("".equals(incorrectParams)) {
      return;
    }
    throw OAuthProblemException.error("Incorrect parameters: " + incorrectParams);
  }

}
