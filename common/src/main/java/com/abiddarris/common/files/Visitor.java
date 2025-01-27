package com.abiddarris.common.files;

import java.io.File;

/**
 * Interface for visiting files and directories during a traversal.
 *
 * <p>This interface is used in the visitor pattern to apply a custom operation on
 * each {@link File} during a traversal of a directory structure. The {@code onVisit}
 * method is called for each file or directory encountered, and its return value
 * determines whether the traversal should continue.
 *
 * <p>Implementers of this interface can define the logic for how to handle each file
 * or directory in the {@code onVisit} method.
 *
 * @since 1.0
 */
public interface Visitor {

    /**
     * Called when a file or directory is visited during the traversal.
     *
     * <p>This method is invoked for each {@link File} encountered while traversing
     * a directory structure. If this method returns {@code false}, the traversal
     * will stop visiting that file or directory, and any subdirectories under it
     * will not be visited.
     *
     * @param src The file or directory being visited.
     * @return {@code true} to continue the traversal, or {@code false} to stop
     *         traversing this file/directory and its subdirectories.
     * @since 1.0
     */
    boolean onVisit(File src);
}