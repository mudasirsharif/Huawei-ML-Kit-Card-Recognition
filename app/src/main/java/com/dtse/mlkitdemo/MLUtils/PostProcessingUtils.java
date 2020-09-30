/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dtse.mlkitdemo.MLUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defining post-processing rules and methods for Emirates Id Card .
 */
class PostProcessingUtils {

    // Filter strings based on regular expressions.
    static String filterString(String origin, String filterStr) {
        if (origin == null || origin.isEmpty()) {
            return "";
        }
        if (filterStr == null || filterStr.isEmpty()) {
            return origin;
        }

        Pattern pattern = Pattern.compile(filterStr);
        Matcher matcher = pattern.matcher(origin);
        return matcher.replaceAll("").trim();
    }

    static String tryGetIdNumber(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (originStr.length() == 18 && matchIdNumberRegex(originStr)) {
            return originStr;
        } else
            return "";
    }

    static boolean matchIdNumberRegex(String idNumber) {

        String regex = "\\b(?!000)[0-9]{3}-(?!0000)[0-9]{4}-(?!0000000)[0-9]{7}-[0-9]{1}\\b";
        Pattern r = Pattern.compile(regex);
        return r.matcher(idNumber).find();
    }

    static String tryGetName(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (originStr.contains("Name")) {
            return originStr.replace("Name", "");
        } else
            return "";
    }

    static String tryGetNationality(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (originStr.contains("Nationality")) {
            return originStr.replace("Nationality", "").replace(".", "");
        } else
            return "";
    }

    static String tryGetDob(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (originStr.contains("Date of Birth") && matchDOBRegex(originStr)) {

            String[] strArray = originStr.replace("Date of Birth ", "").split(" ");
            return strArray[0];
        } else
            return "";
    }

    static boolean matchDOBRegex(String expiryDate) {

        String regex = "\\b(?!00)[0-9]{2}\\/(?!00)[0-9]{2}\\/(?!0000)[0-9]{4}\\b";
        Pattern r = Pattern.compile(regex);
        return r.matcher(expiryDate).find();
    }

    static String tryGetGender(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (originStr.contains("Sex")) {
            String gender = originStr.replace("Sex", "").replace(" ", "");
            switch (gender) {
                case "M": {
                    return "Male";
                }
                case "F": {
                    return "Female";
                }
                default: {
                    return "";
                }
            }
        } else
            return "";
    }

    static String tryGetExpiryDate(String originStr) {

        if (originStr == null || originStr.isEmpty()) {
            return "";
        }

        if (matchExpiryDateRegex(originStr.replace(" ", ""))) {
            return originStr;
        } else
            return "";
    }

    static boolean matchExpiryDateRegex(String expiryDate) {

        String regex = "\\b(?!00)[0-9]{2}\\/(?!00)[0-9]{2}\\/(?!0000)[0-9]{4}\\b";
        Pattern r = Pattern.compile(regex);
        return r.matcher(expiryDate).find();
    }
}