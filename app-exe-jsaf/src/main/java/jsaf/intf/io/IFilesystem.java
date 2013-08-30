// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.regex.Pattern;

import jsaf.intf.util.ILoggable;
import jsaf.intf.util.ISearchable;

/**
 * A platform-independent abstraction of a server filesystem.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IFilesystem extends ILoggable {
    /**
     * Property governing whether the filesystem cache layer should be JDBM-backed (true) or memory-backed (false).
     *
     * @since 1.0
     */
    String PROP_CACHE_JDBM = "fs.cache.useJDBM";

    /**
     * Condition field for a type (i.e., file/directory/link).
     *
     * @since 1.0
     */
    int FIELD_FILETYPE = 50;

    /**
     * Condition field for a file path pattern.
     *
     * @since 1.0
     */
    int FIELD_PATH = 51;

    /**
     * Condition field for a file dirname (directory path) pattern. For files of type FILETYPE_DIR, the dirname is
     * the same as the path.
     *
     * @since 1.0
     */
    int FIELD_DIRNAME = 52;

    /**
     * Condition field for a file basename (filename) pattern. Files of type FILETYPE_DIR have no basename.
     *
     * @since 1.0
     */
    int FIELD_BASENAME = 53;

    /**
     * Condition field for a filesystem type.
     *
     * @since 1.0.1
     */
    int FIELD_FSTYPE = 54;

    /**
     * A condition value indicating a regular file, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_FILE = "f";

    /**
     * A condition value indicating a directory, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_DIR = "d";

    /**
     * A condition value indicating a ling, for conditions of type FIELD_FILETYPE.
     *
     * @since 1.0
     */
    String FILETYPE_LINK = "l";

    /**
     * A search condition for only matching directories.
     *
     * @since 1.0
     */
    ISearchable.ICondition DIRECTORIES = new ISearchable.ICondition() {
	public int getType() { return ISearchable.TYPE_EQUALITY; }
	public int getField() { return FIELD_FILETYPE; }
	public Object getValue() { return FILETYPE_DIR; }
    };

    /**
     * Get the path delimiter character used by this filesystem.
     *
     * @since 1.0
     */
    String getDelimiter();

    /**
     * Access an ISearchable for the filesystem.
     *
     * @since 1.0
     */
    ISearchable<IFile> getSearcher() throws IOException;

    /**
     * Retrieve an IFile with default (IFile.READONLY) access.
     *
     * @since 1.0
     */
    IFile getFile(String path) throws IOException;

    /**
     * Retrieve an IFile with the specified flags.
     *
     * @arg flags IFile.READONLY, IFile.READWRITE, IFile.READVOLATILE, IFile.NOCACHE
     *
     * @since 1.0
     */
    IFile getFile(String path, IFile.Flags flags) throws IOException;

    /**
     * Retrieve multiple IFiles at once, all with default (IFile.READONLY) access. The order of the files corresponds to the
     * order of the path argument array.
     *
     * @since 1.0.1
     */
    IFile[] getFiles(String[] paths) throws IOException;

    /**
     * Retrieve multiple IFiles at once, all with the specified access. The order of the files corresponds to the
     * order of the path argument array.
     *
     * @arg flags IFile.READONLY, IFile.READWRITE, IFile.READVOLATILE, IFile.NOCACHE
     *
     * @since 1.1
     */
    IFile[] getFiles(String[] paths, IFile.Flags flags) throws IOException;

    /**
     * Get random access to an IFile.
     *
     * @since 1.0
     */
    IRandomAccess getRandomAccess(IFile file, String mode) throws IllegalArgumentException, IOException;

    /**
     * Get random access to a file given its path (such as would be passed into the getFile method).
     *
     * @since 1.0
     */
    IRandomAccess getRandomAccess(String path, String mode) throws IllegalArgumentException, IOException;

    /**
     * Read a file.
     *
     * @since 1.0
     */
    InputStream getInputStream(String path) throws IOException;

    /**
     * Write to a file.
     *
     * @since 1.0
     */
    OutputStream getOutputStream(String path, boolean append) throws IOException;

    /**
     * Get all mounts; shortcut for getMounts(null).
     *
     * @since 1.0.1
     */
    Collection<IMount> getMounts() throws IOException;

    /**
     * Shortcut for getMounts(typeFilter, false).
     *
     * @since 1.0
     */
    Collection<IMount> getMounts(Pattern typeFilter) throws IOException;

    /**
     * List the mounts on this filesystem, filtered by the specified pattern.
     *
     * @param typeFilter the pattern to use as a filter. Use null for no filtering.
     * @param include use true to return mounts matching the typeFilter, false to filter out mounts matching the typeFilter.
     *
     * @since 1.0.1
     */
    Collection<IMount> getMounts(Pattern typeFilter, boolean include) throws IOException;

    /**
     * An interface describing a filesystem mount point.
     *
     * @since 1.0
     */
    public interface IMount {
	/**
	 * Get the path of the mount.
	 *
	 * @since 1.0
	 */
	String getPath();

	/**
	 * Get the type of the mount. This is a platform-dependent String.
	 *
	 * @see jsaf.intf.windows.io.IWindowsFilesystem.FsType.value()
	 * @see <a href="http://www.kernel.org/doc/man-pages/online/pages/man2/mount.2.html">mount man page</a>
	 *
	 * @since 1.0
	 */
	String getType();
    }
}
