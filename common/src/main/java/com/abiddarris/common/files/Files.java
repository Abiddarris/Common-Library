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
     * Returns all files and folders from a directory/file.
     *
     * <p>If {@code file} is a file, this method will add the file.
     * If {@code file} is a directory, this method will add the directory
     * and its children.
     *
     * <p>Before the file/directory is added to the list, {@code filter} is called
     * to determine if this file/directory should be added to the list. If the {@code filter} is {@code null},
     * all files and directories are added by default.
     *
     * @param file The directory or file to add to the list.
     * @param filter The filter to apply before adding files or directories to the list. May be {@code null}.
     * @return A {@code List} containing all files and folders from the provided directory/file.
     * @throws NullPointerException If {@code file} is {@code null}.
     * @since 1.0
     */
    public static List<File> getFilesTree(File file, FileFilter filter) {
        List<File> result = new ArrayList<>();
        
        getFilesTree(result, file, filter);
        
        return result;
    }

    /**
     * Get all files and folders from a directory/file and add them to the given list.
     *
     * <p>If {@code file} is a file, this method will add the file to the list.
     * If {@code file} is a directory, this method will add the directory and its children to the list.
     *
     * <p>Before the file/directory is added to the list, {@code filter} is called
     * to determine if the file/directory should be added. If the {@code filter} is {@code null},
     * all files and directories are added by default.
     *
     * @param result The list to store the result of the traversal. It must not be {@code null}.
     * @param file The directory or file to add to the list. It must not be {@code null}.
     * @param filter The filter to apply before adding files or directories to the list. May be {@code null}.
     * @throws NullPointerException If {@code result} or {@code file} is {@code null}.
     * @since 1.0
     */
    public static void getFilesTree(List<File> result, File file, FileFilter filter) {
        checkNonNull(result);
        checkNonNull(file);

        if (filter == null) {
            filter = (f) -> true;
        }

        FileFilter filter0 = filter;
        traverseRecursively(file, (f) -> filter0.accept(f) && result.add(f));
    }

    /**
     * Traverses a file or directory recursively and applies the given visitor.
     *
     * <p>The traversal will visit the file/directory specified by {@code src}, then recursively visit
     * its children if it is a directory. The visitor's {@code onVisit} method will be called for each file/directory.
     * If the visitor's {@code onVisit} method returns {@code false}, the traversal will stop at that file or directory.
     *
     * @param src The source file or directory to start the traversal from. It must not be {@code null}.
     * @param visitor The visitor that will be called for each file/directory. It must not be {@code null}.
     * @throws NullPointerException If {@code src} or {@code visitor} is {@code null}.
     * @since 1.0
     */
    private static void traverse(File src, Visitor visitor) {
        checkNonNull(src, "src cannot be null");
        checkNonNull(visitor, "visitor cannot be null");

        traverseRecursively(src, visitor);
    }

    private static void traverseRecursively(File src, Visitor visitor) {
        boolean continue0 = visitor.onVisit(src);
        if(!continue0) {
            return;
        }

        File[] children = src.listFiles();
        if(children == null) {
            return;
        }

        for(var child : children) {
            traverseRecursively(child, visitor);
        }
    }

    /**
     * Utility method to ensure that the given {@code File} does not exist.
     * If the file exists, an {@code IOException} is thrown with the provided message.
     *
     * @param file The file to check for non-existence.
     * @param message The message to include in the exception if the file exists.
     * @throws IOException if the file exists.
     * @since 1.0
     */
    public static void requireNonExists(File file, String message) throws IOException {
        if (file.exists()) {
            throw new IOException(message);
        }
    }


}
