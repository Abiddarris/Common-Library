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

import static com.abiddarris.common.stream.InputStreams.writeAllTo;
import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        try {
            traverseRecursively(file, (f) -> filter0.accept(f) && result.add(f));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    private static void traverse(File src, Visitor visitor) throws IOException {
        checkNonNull(src, "src cannot be null");
        checkNonNull(visitor, "visitor cannot be null");

        traverseRecursively(src, visitor);
    }

    private static void traverseRecursively(File src, Visitor visitor) throws IOException {
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

    /**
     * Utility method to ensure that the given {@code File} exists.
     * If the file does not exist, an {@code IOException} is thrown with the provided message.
     *
     * @param file The file to check for existence.
     * @param message The message to include in the exception if the file does not exist.
     * @throws IOException if the file does not exist.
     * @since 1.0
     */
    public static void requireExists(File file, String message) throws IOException {
        if (!file.exists()) {
            throw new IOException(message);
        }
    }

    /**
     * Creates the specified directory, including its parent directories if it does not exist.
     *
     * <p>If a file or directory already exists at the specified path, an {@link IOException} is thrown.
     *
     * @param folder the {@link File} object representing the directory to be created.
     * @throws IOException if the folder already exists as a file, or if the directory cannot be created
     *                     for any other reason.
     * @throws NullPointerException if the {@code folder} parameter is {@code null}.
     */
    public static void makeDirectories(File folder) throws IOException {
        checkNonNull(folder, "folder cannot be null");
        requireNonExists(folder, folder + " already exist");

        if (!folder.mkdirs()) {
            throw new IOException(String.format("Cannot make directory : %s", folder));
        }
    }

    /**
     * Copies the contents of a source directory or file to a destination.
     *
     * <p>If the source is a directory, it recursively copies all its contents, including subdirectories, to the destination.
     * If the destination already exists, an exception will be thrown.
     *
     * @param src the source {@link File} (can be a file or directory) to copy.
     * @param dest the destination {@link File} where the contents of the source will be copied.
     * @throws IOException if an I/O error occurs during copying, or if the source doesn't exist or the destination already exists.
     */
    public static void copy(File src, File dest) throws IOException {
        requireExists(src, "src does not exist");
        requireNonExists(dest, "dest exists");

        traverse(src, file -> {
            File destination = file.equals(src) ? dest : new File(dest, file.getPath().substring(src.getPath().length()));
            if (file.isDirectory()) {
                makeDirectories(destination);
                return true;
            }

            copyFile(file, destination);
            return true;
        });
    }

    private static void copyFile(File src, File dest) throws IOException {
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dest)) {
            writeAllTo(in, out);
        }
    }

    /**
     * Deletes the specified file or directory and its contents.
     *
     * <p>If the provided {@link File} is a directory, the method recursively deletes
     * all files and subdirectories within the directory before deleting the directory itself.
     * If the file does not exist, an {@link IOException} will be thrown.
     *
     * <p>The deletion of each file or directory is attempted using the {@link File#delete()} method.
     * If any deletion fails, an {@link IOException} is thrown.
     *
     * <p>Important: This method does not handle file system-specific issues such as permissions.
     * It assumes that the current process has the appropriate permissions to delete the files and directories.
     *
     * @param file the {@link File} to delete (can be a file or directory).
     * @throws IOException if an I/O error occurs during the deletion process, or if the file does not exist.
     * @throws NullPointerException if the provided {@code file} is {@code null}.
     */
    public static void delete(File file) throws IOException {
        checkNonNull(file, "file cannot be null");
        requireExists(file, String.format("Cannot delete non exist file : %s", file));

        List<File> directories = new ArrayList<>();
        Visitor deleteOrThrow = (f) -> {
            if (!f.delete()) {
                throw new IOException("Cannot delete " + f);
            }
            return true;
        };

        traverse(file, (f) -> {
            if (f.isDirectory()) {
                directories.add(f);
                return true;
            }

            return deleteOrThrow.onVisit(f);
        });

        for (int i = directories.size() - 1; i >= 0; i--) {
            deleteOrThrow.onVisit(directories.get(i));
        }
    }

    /**
     * Moves a file or directory from the source location to the destination.
     *
     * <p>This method first attempts to move the file or directory by renaming it using the {@link File#renameTo(File)} method.
     * If the move operation fails (likely due to the source and destination being on different file systems), it will fall back to copying
     * the file or directory to the destination and then deleting the original.
     *
     * <p>If the source does not exist, or if the destination already exists, an {@link IOException} will be thrown.
     * If the file or directory cannot be renamed, copied, or deleted, an exception will be thrown.
     *
     * @param src the source {@link File} (can be a file or directory) to move.
     * @param dest the destination {@link File} where the source will be moved to.
     * @throws IOException if an I/O error occurs during the move operation, or if the source does not exist,
     *         or the destination already exists.
     */
    public static void move(File src, File dest) throws IOException {
        requireExists(src, "src does not exist");
        requireNonExists(dest,"dest exist");

        if (src.renameTo(dest)) {
            return;
        }

        copy(src, dest);
        delete(src);
    }

}
