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
package com.abiddarris.common.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

/**
 * Class that provide static utilities for {@code Locale}
 */
public final class Locales {
    
    private Locales() {
    }
    
    @SuppressWarnings("deprecation")
    public static Locale getPrimaryLocale(Context context) {
    	Configuration configuration = context.getResources()
                                .getConfiguration();
        
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N ?
                configuration.locale :
                configuration.getLocales().get(0);
    }
    
}
