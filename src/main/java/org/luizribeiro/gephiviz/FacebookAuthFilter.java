package org.luizribeiro.gephiviz;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class FacebookAuthFilter implements Filter {

    public static final String FB_ACCESS_TOKEN = "facebook_access_token";
    private String apiKey;
    private String appSecret;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {      
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession(true);
            OAuthService service = new ServiceBuilder()
                                        .provider(FacebookApi.class)
                                        .apiKey(apiKey)
                                        .apiSecret(appSecret)
                                        .callback(httpRequest.getRequestURL().toString())
                                        .build();

            if (session != null) {
                Token fbAccessToken = (Token) session.getAttribute(FB_ACCESS_TOKEN);
                String code = request.getParameter("code");

                if (fbAccessToken == null && code != null) {
                    Verifier verifier = new Verifier(code);
                    fbAccessToken = service.getAccessToken(null, verifier);
                    session.setAttribute(FB_ACCESS_TOKEN, fbAccessToken);
                } else if (fbAccessToken == null) {
                    String authorizationUrl = service.getAuthorizationUrl(null);
                    ((HttpServletResponse) response).sendRedirect(authorizationUrl);
                }
            }
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig fc) throws ServletException {
        apiKey = fc.getInitParameter("api_key");
        appSecret = fc.getInitParameter("app_secret");
    }

    public void destroy() {
    }
}
