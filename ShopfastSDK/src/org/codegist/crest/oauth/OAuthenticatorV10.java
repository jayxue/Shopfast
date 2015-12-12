/*
 * Copyright 2010 CodeGist.org
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * ===================================================================
 *
 * More information at http://www.codegist.org.
 */

package org.codegist.crest.oauth;

import org.codegist.common.codec.Base64;
import org.codegist.common.collect.Maps;
import org.codegist.common.lang.Objects;
import org.codegist.common.lang.Pair;
import org.codegist.common.lang.Strings;
import org.codegist.common.lang.Validate;
import org.codegist.common.log.Logger;
import org.codegist.common.net.Urls;
import org.codegist.crest.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.*;

import static org.codegist.common.net.Urls.encode;

/**
 * OAuth v1.0 authentificator implementation
 * TODO : tidy up, explode in different specilized classes: more cohesion and less coupling please!!
 * @author Laurent Gilles (laurent.gilles@codegist.org)
 */
public class OAuthenticatorV10 implements OAuthenticator {

    public static final String CONFIG_TOKEN_ACCESS_REFRESH_URL = CRestProperty.OAUTH_ACCESS_TOKEN_REFRESH_URL;

    public static final String CONFIG_TOKEN_ACCESS_REFRESH_URL_METHOD = CRestProperty.OAUTH_TOKEN_ACCESS_REFRESH_URL_METHOD;

    public static final String CONFIG_OAUTH_PARAM_DEST = CRestProperty.OAUTH_PARAM_DEST;

    public static final String CONFIG_TOKEN_REQUEST_URL = "authentification.oauth.access.request-url";

    public static final String CONFIG_TOKEN_REQUEST_URL_METHOD = "authentification.oauth.access.request-url.method";

    public static final String CONFIG_TOKEN_ACCESS_URL = "authentification.oauth.access.access-url";

    public static final String CONFIG_TOKEN_ACCESS_URL_METHOD = "authentification.oauth.access.access-url.method";

    public static final String CONFIG_OAUTH_CALLBACK = "authentification.oauth.request.callback";

    private final static Logger LOGGER = Logger.getLogger(OAuthenticatorV10.class);

    private final static String ENC = "UTF-8";
    private final static String SIGN_METH = "HMAC-SHA1";
    private final static String SIGN_METH_4_J = "HmacSHA1";
    private final VariantProvider variant;

    private final Token consumerToken;
    private final RestService restService;
    private final String callback;
    private final boolean toHeaders;

    private final String requestTokenUrl;
    private final HttpMethod requestTokenMeth;

    private final String accessTokenUrl;
    private final HttpMethod accessTokenMeth;

    private final String refreshAccessTokenUrl;
    private final HttpMethod refreshAccessTokenMeth;

    public OAuthenticatorV10(RestService restService, Token consumerToken, VariantProvider variant) {
        this(restService, consumerToken, null, variant);
    }

    public OAuthenticatorV10(RestService restService, Token consumerToken, Map<String, Object> config, VariantProvider variant) {
        this.variant = variant;
        this.consumerToken = consumerToken;
        this.restService = restService;
        config = Maps.defaultsIfNull(config);
        this.callback = Strings.defaultIfBlank((String) config.get(CONFIG_OAUTH_CALLBACK), "oob");
        this.toHeaders = !"url".equals(config.get(CONFIG_OAUTH_PARAM_DEST));

        this.requestTokenUrl = (String) config.get(CONFIG_TOKEN_REQUEST_URL);
        if (Strings.isNotBlank((String) config.get(CONFIG_TOKEN_REQUEST_URL_METHOD)))
            requestTokenMeth = HttpMethod.valueOf((String) config.get(CONFIG_TOKEN_REQUEST_URL_METHOD));
        else
            requestTokenMeth = HttpMethod.POST;

        this.accessTokenUrl = (String) config.get(CONFIG_TOKEN_ACCESS_URL);
        if (Strings.isNotBlank((String) config.get(CONFIG_TOKEN_ACCESS_URL_METHOD)))
            accessTokenMeth = HttpMethod.valueOf((String) config.get(CONFIG_TOKEN_ACCESS_URL_METHOD));
        else
            accessTokenMeth = HttpMethod.POST;

        this.refreshAccessTokenUrl = (String) config.get(CONFIG_TOKEN_ACCESS_REFRESH_URL);
        if (Strings.isNotBlank((String) config.get(CONFIG_TOKEN_ACCESS_REFRESH_URL_METHOD)))
            refreshAccessTokenMeth = HttpMethod.valueOf((String) config.get(CONFIG_TOKEN_ACCESS_REFRESH_URL_METHOD));
        else
            refreshAccessTokenMeth = HttpMethod.POST;
    }

    public OAuthenticatorV10(RestService restService, Token consumerToken, Map<String,Object> config) {
        this(restService, consumerToken, config, new DefaultVariantProvider());
    }

    public OAuthenticatorV10(RestService restService, Token consumerToken) {
        this(restService, consumerToken, (Map<String,Object>) null);
    }


    public Token getRequestToken() {
        Validate.notEmpty(this.requestTokenUrl, "No request token url as been configured, please pass it in the config map, key=" + CONFIG_TOKEN_REQUEST_URL);
        HttpResponse refreshTokenResponse = null;
        try {
            HttpRequest.Builder request = new HttpRequest.Builder(this.requestTokenUrl, "utf-8").using(requestTokenMeth);

            Set<Pair<String,String>> oauthParams = newBaseOAuthParams();
            oauthParams.add(new Pair<String,String>("oauth_callback", callback));
            String sign = generateSignature(new Token("",""), request, oauthParams);
            oauthParams.add(new Pair<String,String>("oauth_signature", sign));

            if(HttpMethod.GET.equals(requestTokenMeth)) {
                request.addQueryParams(toParamMap(oauthParams));
            }else{
                request.addBodyParams((Map)toParamMap(oauthParams));
            }
            refreshTokenResponse = restService.exec(request.build());
            Map<String,String> result = Urls.parseQueryString(refreshTokenResponse.asString());
            Token token = new Token(
                    result.get("oauth_token"),
                    result.get("oauth_token_secret"),
                    Maps.filter(result, "oauth_token", "oauth_token_secret")
            );
            LOGGER.debug("Received request token=%s", token);
            return token;
        } catch (URISyntaxException e) {
            throw new OAuthException(e);
        } finally {
            if (refreshTokenResponse != null) {
                refreshTokenResponse.close();
            }
        }
    }

    public Token refreshAccessToken(Token requestToken, String... includeExtras) {
        Validate.notEmpty(this.refreshAccessTokenUrl, "No refresh access token url as been configured, please pass it in the config map, key=" + CONFIG_TOKEN_ACCESS_REFRESH_URL);
        Set<Pair<String,String>> params = new LinkedHashSet<Pair<String,String>>();
        for(String extra : includeExtras){
            if(requestToken.getExtra(extra) == null) continue;
            params.add(new Pair<String,String>(extra, requestToken.getExtra(extra)));
        }
        Token token = getAccessToken(this.refreshAccessTokenUrl, this.refreshAccessTokenMeth, requestToken, params);
        LOGGER.debug("Refreshed access token=%s", token);
        return token;
    }
    
    public Token getAccessToken(Token requestToken, String verifier) {
        Validate.notEmpty(this.accessTokenUrl, "No access token url as been configured, please pass it in the config map, key=" + CONFIG_TOKEN_ACCESS_URL);
        Set<Pair<String,String>> set = new LinkedHashSet<Pair<String,String>>();
        set.add(new Pair<String,String>("oauth_verifier", verifier));
        Token token = getAccessToken(this.accessTokenUrl, this.accessTokenMeth, requestToken, set);
        LOGGER.debug("Received access token=%s", token);
        return token;
    }

    private Token getAccessToken(String url, HttpMethod meth, Token requestToken, Set<Pair<String,String>> extras) {
        HttpResponse refreshTokenResponse = null;
        try {
            HttpRequest.Builder request = new HttpRequest.Builder(url, "utf-8").using(meth);

            Set<Pair<String,String>> oauthParams = newBaseOAuthParams();
            oauthParams.add(new Pair<String,String>("oauth_token", requestToken.getToken()));
            if(extras != null) oauthParams.addAll(extras);

            String sign = generateSignature(requestToken, request, oauthParams);
            oauthParams.add(new Pair<String,String>("oauth_signature", sign));

            if(HttpMethod.GET.equals(meth)) {
                request.addQueryParams(toParamMap(oauthParams));
            }else{
                request.addBodyParams((Map)toParamMap(oauthParams));
            }
            refreshTokenResponse = restService.exec(request.build());
            Map<String,String> result = Urls.parseQueryString(refreshTokenResponse.asString());
            return new Token(
                    result.get("oauth_token"),
                    result.get("oauth_token_secret"),
                    Maps.filter(result, "oauth_token", "oauth_token_secret")
            );
        } catch (URISyntaxException e) {
            throw new OAuthException(e);
        } finally {
            if (refreshTokenResponse != null) {
                refreshTokenResponse.close();
            }
        }
    }

    public void sign(Token accessToken, HttpRequest.Builder request, Pair<String,String>... extraHeaders) {
        Set<Pair<String,String>> extraHeadersList = new LinkedHashSet<Pair<String,String>>(Arrays.<Pair<String,String>>asList(Objects.defaultIfNull(extraHeaders, new Pair[0])));
        try {
            sign(accessToken, request, extraHeadersList);
        } catch (UnsupportedEncodingException e) {
            throw new OAuthException(e);
        }
    }

    private void sign(Token accessToken, HttpRequest.Builder request, Set<Pair<String,String>> extraHeaders) throws UnsupportedEncodingException {

        Set<Pair<String,String>> oauthParams = newBaseOAuthParams(); // generate base oauth params
        oauthParams.add(new Pair<String,String>("oauth_token", accessToken.getToken()));
        oauthParams.addAll(extraHeaders);

        // Generate params for signature, these params contains the query string and body params on top of the already existing one.
        Set<Pair<String,String>> signatureParams = new LinkedHashSet<Pair<String,String>>();
        signatureParams.addAll(oauthParams);
        signatureParams.addAll(extractOAuthParams(request));

        String signature = generateSignature(accessToken, request, signatureParams);

        // Add signature to the base param list
        oauthParams.add(new Pair<String,String>("oauth_signature", signature));

        if (toHeaders) {
            request.addHeader("Authorization", generateOAuthHeader(oauthParams));
        } else {
            for (Pair<String,String> p : oauthParams) {
                request.addQueryParam(p.getName(), p.getValue());
            }
        }
    }

    private String generateOAuthHeader(Set<Pair<String,String>> oauthParams) throws UnsupportedEncodingException {
        return "OAuth " + encodeParams(oauthParams, ",", true);
    }

    private Set<Pair<String,String>> newBaseOAuthParams() {
        return newBaseOAuthParams(SIGN_METH);
    }
    private Set<Pair<String,String>> newBaseOAuthParams(String signatureMethod) {
        Set<Pair<String,String>> params = new LinkedHashSet<Pair<String,String>>();
        params.add(new Pair<String,String>("oauth_consumer_key", consumerToken.getToken()));
        params.add(new Pair<String,String>("oauth_signature_method", signatureMethod));
        params.add(new Pair<String,String>("oauth_timestamp", variant.timestamp()));
        params.add(new Pair<String,String>("oauth_nonce", variant.nonce()));
        params.add(new Pair<String,String>("oauth_version", "1.0"));
        return params;
    }


    private static List<Pair<String,String>> extractOAuthParams(HttpRequest.Builder builder) {
        List<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
        if (builder.getQueryString() != null) {
            params.addAll(toParamSet(builder.getQueryString()));
        }
        if (builder.getBodyParams() != null)
            for (Map.Entry<String, Object> entry : builder.getBodyParams().entrySet()) {
                if (Params.isForUpload(entry.getValue())) continue;
                params.add(new Pair<String,String>(entry.getKey(), entry.getValue().toString()));
            }

        return params;
    }

    private static Set<Pair<String,String>> toParamSet(Map<String, String> params) {
        Set<Pair<String,String>> p = new LinkedHashSet<Pair<String,String>>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            p.add(new Pair<String,String>(param.getKey(), param.getValue()));
        }
        return p;
    }

    private static Map<String,String> toParamMap(Set<Pair<String,String>> params) {
        Map<String,String> map = new LinkedHashMap<String, String>();
        for(Pair<String,String> p : params){
            map.put(p.getName(), p.getValue());
        }
        return map;
    }


    /**
     * The Signature Base String includes the request absolute URL, tying the signature to a specific endpoint. The URL used in the Signature Base String MUST include the scheme, authority, and path, and MUST exclude the query and fragment as defined by [RFC3986] section 3.<br>
     * If the absolute request URL is not available to the Service Provider (it is always available to the Consumer), it can be constructed by combining the scheme being used, the HTTP Host header, and the relative HTTP request URL. If the Host header is not available, the Service Provider SHOULD use the host name communicated to the Consumer in the documentation or other means.<br>
     * The Service Provider SHOULD document the form of URL used in the Signature Base String to avoid ambiguity due to URL normalization. Unless specified, URL scheme and authority MUST be lowercase and include the port number; http default port 80 and https default port 443 MUST be excluded.<br>
     * <br>
     * For example, the request:<br>
     * HTTP://Example.com:80/resource?id=123<br>
     * Is included in the Signature Base String as:<br>
     * http://example.com/resource
     *
     * @param url the url to be normalized
     * @return the Signature Base String
     * @see <a href="http://oauth.net/core/1.0#rfc.section.9.1.2">OAuth Core - 9.1.2.  Construct Request URL</a>
     */
    private static String constructRequestURL(String url) {
        int index = url.indexOf("?");
        if (-1 != index) {
            url = url.substring(0, index);
        }
        int slashIndex = url.indexOf("/", 8);
        String baseURL = url.substring(0, slashIndex).toLowerCase();
        int colonIndex = baseURL.indexOf(":", 8);
        if (-1 != colonIndex) {
            // url contains port number
            if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
                // http default port 80 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            } else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
                // http default port 443 MUST be excluded
                baseURL = baseURL.substring(0, colonIndex);
            }
        }
        return baseURL + url.substring(slashIndex);
    }

    private static String encodeParams(Set<Pair<String,String>> httpParams, String sep, boolean quote) throws UnsupportedEncodingException {
        StringBuilder buf = new StringBuilder();
        String format = quote ? "\"%s\"" : "%s";
        for (Pair<String,String> p : httpParams) {
            if (buf.length() != 0) {
                buf.append(sep);
            }
            String name = Strings.defaultIfBlank(p.getName(),"");
            String value = Strings.defaultIfBlank(p.getValue(),"");

            buf.append(encode(name, ENC));
            buf.append("=");
            buf.append(String.format(format, encode(value, ENC)));
        }
        return buf.toString();
    }
    String generateSignature(Token accessToken, HttpRequest.Builder request, Set<Pair<String,String>> params) {
        try {
            // first, sort the list without changing the one given
            Set<Pair<String,String>> sorted = new TreeSet<Pair<String,String>>(PAIR_COMPARATOR);
            sorted.addAll(params);

            String signMeth = String.valueOf(request.getMeth());
            String signUri = constructRequestURL(request.getBaseUri());
            String signParams = encodeParams(sorted, "&", false);

            // format the signature content
            String data = signMeth + "&" + encode(signUri, ENC) + "&" + encode(signParams, ENC);

            Mac mac = Mac.getInstance(SIGN_METH_4_J);
            String signature = generateSignature(accessToken.getSecret());
            mac.init(new SecretKeySpec(signature.getBytes(ENC), SIGN_METH_4_J));

            String encoded = new String(Base64.encodeToByte(mac.doFinal(data.getBytes(ENC))), ENC);
            LOGGER.debug("Signature[data=\"%s\",signature=\"%s\",result=\"%s\"]", data, signature, encoded);
            return encoded;
        } catch (Exception e) {
            throw new OAuthException(e);
        }
    }
    String generateSignature(String tokenSecret){
        return (consumerToken.getSecret() + "&" + tokenSecret);
    }


    private static final Comparator<Pair<String,String>> PAIR_COMPARATOR = new Comparator<Pair<String, String>>() {
        public int compare(Pair<String, String> o1, Pair<String, String> o2) {
            int i = o1.getName().compareTo(o2.getName());
            return i != 0 ? i : o1.getValue().compareTo(o2.getValue());
        }
    };

    static interface VariantProvider {

        String timestamp();

        String nonce();

    }

    static class DefaultVariantProvider implements VariantProvider {
        private final Random RDM = new SecureRandom();

        public String timestamp() {
            return String.valueOf(System.currentTimeMillis() / 1000l);
        }

        public String nonce() {
            return String.valueOf(System.currentTimeMillis() + RDM.nextLong());
        }
    }

}
