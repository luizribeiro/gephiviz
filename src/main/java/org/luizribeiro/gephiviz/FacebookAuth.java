package org.luizribeiro.gephiviz;

import com.restfb.json.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;

public class FacebookAuth {

    private static Cookie getCookie(HttpServletRequest request) {
        Cookie cookies[] = request.getCookies();

        for (Cookie c : cookies) {
            if (c.getName().equals("fbsr_" + Settings.getApiKey())) {
                return c;
            }
        }

        return null;
    }

    public static String getAccessToken(HttpServletRequest request)
            throws Exception {
        Cookie cookie = getCookie(request);

        if (cookie == null) {
            return null;
        }

        String cookieValue = cookie.getValue();
        String stringArgs[] = cookieValue.split("\\.");
        String encodedPayload = stringArgs[1];
        String payload = base64UrlDecode(encodedPayload);

        JsonObject data = new JsonObject(payload);
        URL authUrl = new URL(getAuthURL(data.getString("code")));
        URI authUri = new URI(authUrl.getProtocol(), authUrl.getHost(),
                authUrl.getPath(), authUrl.getQuery(), null);
        String result = readURL(authUri.toURL());

        String resultSplited[] = result.split("&");

        return resultSplited[0].split("=")[1];
    }

    private static String base64UrlDecode(String input) {
        Base64 decoder = new Base64();
        byte decodedBytes[] = decoder.decode(input.getBytes());
        return new String(decodedBytes);
    }

    private static String getAuthURL(String authCode) {
        return "https://graph.facebook.com/oauth/access_token?client_id="
                + Settings.getApiKey()
                + "&redirect_uri=&client_secret="
                + Settings.getAppSecret() + "&code="
                + authCode;
    }

    private static String readURL(URL url) throws IOException {
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        String s = "";

        while (is.read() != -1) {
            s = reader.readLine();
        }

        reader.close();

        return s;
    }
}
