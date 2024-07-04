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
package com.abiddarris.common.files;
import java.io.File;

/**
 * Class that provides utilities for files
 *
 * @since 1.0
 * @author Abiddarris
 */
public final class Files {
    
    /**
     * Returns file's name without its extension.
     * 
     * @param file File
     * @return file's name without its extension.
     * @since 1.0
     */
    public static String getNameWithoutExtension(File file) {
    	String fileName = file.getName();
        int extensionSeparator = fileName.lastIndexOf(".");
        
        if(extensionSeparator <= 0) return fileName;
        
        return fileName.substring(0, extensionSeparator);
    }
    
    /**
     * Returns file's extension if exists, otherwise return empty {@code String}.
     * 
     * @param file File
     * @return File's extension.
     * @since 1.0
     */
    public static String getExtension(File file) {
    	String fileName = file.getName();
        int extensionSeparator = fileName.lastIndexOf(".");
        
        if(extensionSeparator <= 0) return "";
        
        return fileName.substring(extensionSeparator + 1);
    }
    
    
}
