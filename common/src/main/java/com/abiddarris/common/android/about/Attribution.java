/***********************************************************************************
 * Copyright 2024 Abiddarris
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
 ***********************************************************************************/

package com.abiddarris.common.android.about;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attribution {

    private static final Pattern ATTRIBUTION_PATTERN = Pattern.compile("LicenseText : (.*)");

    private String header;
    private String licenseTextAssets;

    public Attribution(String header, String licenseTextAssets) {
        this.header = header;
        this.licenseTextAssets = licenseTextAssets;
    }

    public String getHeader() {
        return this.header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getLicenseTextAssets() {
        return this.licenseTextAssets;
    }

    public void setLicenseTextAssets(String licenseTextAssets) {
        this.licenseTextAssets = licenseTextAssets;
    }
    
    public static List<Attribution> parse(String text) {
        Matcher matcher = ATTRIBUTION_PATTERN.matcher(text);
        int attributionEnd = 0;
        List<Attribution> attributions = new ArrayList<>();

        while (matcher.find()) {
            String header = text.substring(attributionEnd, matcher.start() - 1);
            String licenseTextAssets = matcher.group(1);

            attributions.add(new Attribution(header, licenseTextAssets));

            attributionEnd = matcher.end();
        }

        return attributions;
    }

}
