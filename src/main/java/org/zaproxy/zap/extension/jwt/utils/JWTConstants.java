/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.jwt.utils;

import java.util.regex.Pattern;

public interface JWTConstants {

    char JWT_TOKEN_PERIOD_CHARACTER = '.';

    String JWT_TOKEN_PERIOD_CHARACTER_REGEX = "[" + JWT_TOKEN_PERIOD_CHARACTER + "]";

    Pattern JWT_TOKEN_REGEX_PATTERN =
            Pattern.compile(
                    "[a-zA-Z0-9_-]*"
                            + JWT_TOKEN_PERIOD_CHARACTER_REGEX
                            + "[a-zA-Z0-9_-]*"
                            + JWT_TOKEN_PERIOD_CHARACTER_REGEX
                            + "[a-zA-Z0-9_-]*$");

    String BASE64_PADDING_CHARACTER_REGEX = "[=]";

    String[] NONE_ALGORITHM_VARIANTS = {"none", "None", "NONE", "nOnE"};

    String JWT_ALGORITHM_KEY_HEADER = "alg";

    String JWT_RSA_ALGORITHM_IDENTIFIER = "RS";

    String JWT_HMAC_ALGORITHM_IDENTIFIER = "HS";

    String JWT_EXP_ALGORITHM_IDENTIFIER = "exp";

    String JSON_WEB_KEY_HEADER = "jwk";

    String JWT_HEADER_WITH_ALGO_PLACEHOLDER = "{\"typ\":\"JWT\",\"alg\":\"%s\"}";

    String[] HEADER_FORMAT_VARIANTS = {
        JWT_HEADER_WITH_ALGO_PLACEHOLDER,
        "{\"alg\":\"%s\",\"typ\":\"JWT\"}",
        "{\"typ\":\"JWT\",\"alg\":\"\"}",
        "{\"typ\":\"JWT\"}",
        "{\"alg\":\"%s\"}",
    };

    String HMAC_256 = "HS256";

    String HS256_ALGO_JAVA = "HmacSHA256";

    String NULL_BYTE_CHARACTER = String.valueOf((char) 0);

    String BEARER_TOKEN_REGEX = "(?i)bearer";

    String BEARER_TOKEN_KEY = "Bearer";
}