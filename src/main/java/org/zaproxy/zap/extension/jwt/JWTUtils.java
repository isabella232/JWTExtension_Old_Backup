/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.jwt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** @author KSASAN preetkaran20@gmail.com */
public class JWTUtils {

    public static final char JWT_TOKEN_PERIOD_CHARACTER = '.';

    public static final String JWT_TOKEN_PERIOD_CHARACTER_REGEX =
            "[" + JWT_TOKEN_PERIOD_CHARACTER + "]";

    public static final Pattern JWT_TOKEN_REGEX_PATTERN =
            Pattern.compile(
                    "[a-zA-Z0-9_-]*"
                            + JWT_TOKEN_PERIOD_CHARACTER_REGEX
                            + "[a-zA-Z0-9_-]*"
                            + JWT_TOKEN_PERIOD_CHARACTER_REGEX
                            + "[a-zA-Z0-9_-]*$");

    public static final String BASE64_PADDING_CHARACTER_REGEX = "[=]";

    public static final String[] NONE_ALGORITHM_VARIANTS = {"none", "None", "NONE", "nOnE"};

    public static final String JWT_ALGORITHM_KEY_HEADER = "alg";

    public static final String JWT_RSA_ALGORITHM_IDENTIFIER = "RS";

    public static final String JWT_HMAC_ALGORITHM_IDENTIFIER = "HS";

    public static final String JWT_EXP_ALGORITHM_IDENTIFIER = "exp";

    public static final String JSON_WEB_KEY_HEADER = "jwk";

    public static final String JWT_HEADER_WITH_ALGO_PLACEHOLDER =
            "{\"typ\":\"JWT\",\"alg\":\"%s\"}";

    public static final String[] HEADER_FORMAT_VARIANTS = {
        JWT_HEADER_WITH_ALGO_PLACEHOLDER,
        "{\"alg\":\"%s\",\"typ\":\"JWT\"}",
        "{\"typ\":\"JWT\",\"alg\":\"\"}",
        "{\"typ\":\"JWT\"}",
        "{\"alg\":\"%s\"}",
    };

    public static final String HMAC_256 = "HS256";

    public static final String HS256_ALGO_JAVA = "HmacSHA256";

    public static final String NULL_BYTE_CHARACTER = String.valueOf((char) 0);

    public static final String BEARER_TOKEN_REGEX = "(?i)bearer";

    public static byte[] getBytes(String token) throws UnsupportedEncodingException {
        return token.getBytes(StandardCharsets.UTF_8);
    }

    private static String getString(byte[] tokenBytes) throws UnsupportedEncodingException {
        return new String(tokenBytes, StandardCharsets.UTF_8);
    }

    /**
     * we are using base64 Url Safe. because of JWT specifications <br>
     * <b> base64 and base64url encoding are different in the last two characters used, ie, base64
     * -> '+/', or base64url -> '-_' see https://en.wikipedia.org/wiki/Base64#URL_applications </b>
     * As per <a href="https://www.rfc-editor.org/rfc/rfc7515.txt">RFC 7515, Appendix C. Notes on
     * Implementing base64url Encoding without Padding</a> padding is not there in JWT.
     *
     * @param token
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getBase64UrlSafeWithoutPaddingEncodedString(String token)
            throws UnsupportedEncodingException {
        return JWTUtils.getBase64UrlSafeWithoutPaddingEncodedString(getBytes(token));
    }

    /**
     * we are using base64 Url Safe. because of JWT specifications <br>
     * <b> base64 and base64url encoding are different in the last two characters used, ie, base64
     * -> '+/', or base64url -> '-_' see https://en.wikipedia.org/wiki/Base64#URL_applications </b>
     * As per <a href="https://www.rfc-editor.org/rfc/rfc7515.txt">RFC 7515, Appendix C. Notes on
     * Implementing base64url Encoding without Padding</a> padding is not there in JWT.
     *
     * @param token
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String getBase64UrlSafeWithoutPaddingEncodedString(byte[] token)
            throws UnsupportedEncodingException {
        return JWTUtils.getString(Base64.getUrlEncoder().encode(token))
                .replaceAll(BASE64_PADDING_CHARACTER_REGEX, "");
    }

    /**
     * TODO Need to validate JWT Token using "Regex" Parses the JWT Token and then checks if token
     * structure is valid
     *
     * @param jwtToken
     * @return
     */
    public static boolean isTokenValid(String jwtToken) {
        if (Objects.isNull(jwtToken)) {
            return false;
        }
        return JWT_TOKEN_REGEX_PATTERN.matcher(jwtToken).matches();
    }

    /**
     * Parses JWT token and creates JWTTokenBean we are using base64 Url Safe. because of JWT
     * specifications <br>
     * <b> base64 and base64url encoding are different in the last two characters used, ie, base64
     * -> '+/', or base64url -> '-_' see https://en.wikipedia.org/wiki/Base64#URL_applications </b>
     *
     * @param jwtToken
     * @return JWTTokenBean
     * @throws UnsupportedEncodingException
     * @throws JWTExtensionValidationException
     */
    public static JWTTokenBean parseJWTToken(String jwtToken)
            throws JWTExtensionValidationException {
        if (!isTokenValid(jwtToken)) {
            throw new JWTExtensionValidationException("JWT token:" + jwtToken + " is not valid");
        }
        JWTTokenBean jwtTokenBean = new JWTTokenBean();
        String[] tokens = jwtToken.split(JWT_TOKEN_PERIOD_CHARACTER_REGEX);

        try {
            jwtTokenBean.setHeader(getString(Base64.getDecoder().decode(getBytes(tokens[0]))));
            jwtTokenBean.setPayload(getString(Base64.getDecoder().decode(getBytes(tokens[1]))));
            jwtTokenBean.setSignature(Base64.getDecoder().decode(getBytes(tokens[2])));
        } catch (UnsupportedEncodingException e) {
            throw new JWTExtensionValidationException(
                    "JWT Token:" + jwtToken + " parsing failed", e);
        }

        return jwtTokenBean;
    }

    /**
     * @returns RSA public key as per the JWT Configuration.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IOException
     */
    public static PublicKey getRSAPublicKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // TODO if public key at path is not present
        String publicKeyPath = JWTConfiguration.getInstance().getTrustStorePath();
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(publicKeyPath));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static String getBase64EncodedHMACSignedToken(byte[] token, byte[] secretKey)
            throws JWTExtensionValidationException, UnsupportedEncodingException {
        try {
            Mac hmacSHA256 = Mac.getInstance(HS256_ALGO_JAVA);
            SecretKeySpec hmacSecretKey = new SecretKeySpec(secretKey, HS256_ALGO_JAVA);
            hmacSHA256.init(hmacSecretKey);
            byte[] tokenSignature = hmacSHA256.doFinal(token);
            String base64EncodedSignature =
                    JWTUtils.getBase64UrlSafeWithoutPaddingEncodedString(tokenSignature);
            return base64EncodedSignature;
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            throw new JWTExtensionValidationException(
                    "Exception occurred while Signing token: " + getString(token), e);
        }
    }

    private static boolean hasBearerToken(String value) {
        return Pattern.compile(BEARER_TOKEN_REGEX).matcher(value).find();
    }

    public static String extractingJWTFromParamValue(String value) {
        if (hasBearerToken(value)) {
            value = value.replaceAll(BEARER_TOKEN_REGEX, "").trim();
        }
        return value;
    }

    public static String addingJWTToParamValue(String value, String jwtToken) {
        if (hasBearerToken(value)) {
            jwtToken = "Bearer " + jwtToken;
        }
        return jwtToken;
    }
}
