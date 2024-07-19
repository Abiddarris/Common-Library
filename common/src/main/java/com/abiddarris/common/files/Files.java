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

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * Returns new file that has been created based on given file with different extension.
     *
     * @param extension New extension. Can include {@code .} on first character.
     * @return File that has been created based on given file with different extension.
     * @since 1.0
     */
    public static File changeExtension(File file, String extension) {
        if(!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return new File(
            file.getParent(), getNameWithoutExtension(file) + extension);
    }
    
    /**
     * Returns all files and folders from a directory/file
     *
     * <p>If {@code file} is file, This method will add the file.
     * If {@code file} is directory, This method will add the directory
     * and its children.
     * 
     * <p>Before the file/directory is added to list, {@code filter} is called
     * to determined if this file/directory should be added to the list
     *
     * @param file Directory/file to add
     * @param filter Filter that will be called before adding files to the list, May be {@code null}
     * @return {@code List} containing all files and folders from a directory/file
     * @throws NullPointerException if {@code file} is {@code null}.
     * @since 1.0
     */
    public static List<File> getFilesTree(File file, FileFilter filter) {
        List<File> result = new ArrayList<>();
        
        getFilesTree(result, file, filter);
        
        return result;
    }
    
    /**
     * Get all files and folders from a directory/file and add them to 
     * given list.
     *
     * <p>If {@code file} is file, This method will add the file.
     * If {@code file} is directory, This method will add the directory
     * and its children.
     * 
     * <p>Before the file/directory is added to list, {@code filter} is called
     * to determined if this file/directory should be added to the list
     *
     * @param result List to store the result
     * @param file Directory/file to add
     * @param filter Filter that will be called before adding files to the list, May be {@code null}
     * @throws NullPointerException if {@code file} is {@code null} or {@code result} is {@code null}
     * @since 1.0
     */
    public static void getFilesTree(List<File> result, File file, FileFilter filter) {
        checkNonNull(result);
        checkNonNull(file);
        
        getFilesTreeInternal(result, file, filter != null ? filter : (f) -> true);
    }
    
    private static void getFilesTreeInternal(List<File> result, File file, FileFilter filter) {
        boolean accept = filter.accept(file);
        if(!accept) {
            return;
        }
        
        result.add(file);
        
        File[] children = file.listFiles();
        if(children == null) {
            return;
        }
        
        for(var child : children) {
        	getFilesTreeInternal(result, child, filter);
        }
    }
}
