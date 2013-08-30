// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

/**
 * Representation of a Windows Access Control Entity (ACE), including various Windows constants.
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa374896%28v=vs.85%29.aspx">Access Mask Format (Windows)</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa374892%28v=vs.85%29.aspx">ACCESS_MASK (Windows)</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/aa364399%28v=vs.85%29.aspx">File Security and Access Rights (Windows)</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/windows/desktop/ms685981%28v=vs.85%29.aspx">Service Security and Access Rights (Windows)</a>
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IACE {
    int KEY_QUERY_VALUE		= 0x0001;
    int KEY_SET_VALUE		= 0x0002;
    int KEY_CREATE_SUB_KEY	= 0x0004;
    int KEY_ENUMERATE_SUB_KEYS	= 0x0008;
    int KEY_NOTIFY		= 0x0010;
    int KEY_CREATE_LINK		= 0x0020;
    int KEY_WOW64_64_KEY	= 0x0100;
    int KEY_WOW64_32_KEY	= 0x0200;
    int KEY_WOW64_RES		= 0x0300;
    int KEY_WRITE		= 0x20006;
    int KEY_READ		= 0x20019;
    int KEY_EXECUTE		= 0x20019;
    int KEY_ALL_ACCESS		= 0xF003F;

    int FILE_READ_DATA		= 1;
    int FILE_WRITE_DATA		= 2;
    int FILE_APPEND_DATA	= 4;
    int FILE_READ_EA		= 8;
    int FILE_WRITE_EA		= 16;
    int FILE_EXECUTE		= 32;
    int FILE_DELETE		= 64;
    int FILE_READ_ATTRIBUTES	= 128;
    int FILE_WRITE_ATTRIBUTES	= 256;

    int GENERIC_ALL		= 0x10000000;
    int GENERIC_EXECUTE		= 0x20000000;
    int GENERIC_WRITE		= 0x40000000;
    int GENERIC_READ		= 0x80000000;

    int DELETE 			= 0x10000;
    int READ_CONTROL		= 0x20000;
    int WRITE_DAC		= 0x40000;
    int WRITE_OWNER		= 0x80000;
    int SYNCHRONIZE		= 0x100000;

    int STANDARD_RIGHTS_REQUIRED= 0x000F0000;
    int STANDARD_RIGHTS_READ	= READ_CONTROL;
    int STANDARD_RIGHTS_WRITE	= READ_CONTROL;
    int STANDARD_RIGHTS_EXECUTE	= READ_CONTROL;
    int STANDARD_RIGHTS_ALL	= 0x001F0000;
    int SPECIFIC_RIGHTS_ALL	= 0x0000FFFF;

    int FLAGS_OBJECT_INHERIT	= 1;
    int FLAGS_CONTAINER_INHERIT	= 2;
    int FLAGS_NO_PROPAGATE	= 4;
    int FLAGS_INHERIT_ONLY	= 8;
    int FLAGS_INHERITED		= 16;

    int ACCESS_SYSTEM_SECURITY	= 0x1000000;

    int FILE_GENERIC_READ = FILE_READ_ATTRIBUTES | FILE_READ_DATA | FILE_READ_EA | STANDARD_RIGHTS_READ | SYNCHRONIZE;
    int FILE_GENERIC_WRITE = FILE_APPEND_DATA | FILE_WRITE_ATTRIBUTES | FILE_WRITE_DATA | FILE_WRITE_EA | STANDARD_RIGHTS_WRITE | SYNCHRONIZE;
    int FILE_GENERIC_EXECUTE = FILE_EXECUTE | FILE_READ_ATTRIBUTES | STANDARD_RIGHTS_EXECUTE | SYNCHRONIZE;
    int FILE_GENERIC_ALL = FILE_GENERIC_READ | FILE_GENERIC_WRITE | FILE_GENERIC_EXECUTE;

    //
    // Since 1.0.1
    //
    int SC_MANAGER_ALL_ACCESS		= 0xF003F;
    int SC_MANAGER_CREATE_SERVICE	= 0x002;
    int SC_MANAGER_CONNECT		= 0x0001;
    int SC_MANAGER_ENUMERATE_SERVICE	= 0x0004;
    int SC_MANAGER_LOCK			= 0x0008;
    int SC_MANAGER_MODIFY_BOOT_CONFIG	= 0x0020;
    int SC_MANAGER_QUERY_LOCK_STATUS	= 0x0010;
    int SERVICE_ALL_ACCESS		= 0xF01FF;
    int SERVICE_CHANGE_CONFIG		= 0x0002;
    int SERVICE_ENUMERATE_DEPENDENTS	= 0x0008;
    int SERVICE_INTERROGATE		= 0x0080;
    int SERVICE_PAUSE_CONTINUE		= 0x0040;
    int SERVICE_QUERY_CONFIG		= 0x0001;
    int SERVICE_QUERY_STATUS		= 0x0004;
    int SERVICE_START			= 0x0010;
    int SERVICE_STOP			= 0x0020;
    int SERVICE_USER_DEFINED_CONTROL	= 0x0100;

    /**
     * Get the entry access flags.
     *
     * @since 1.0
     */
    int getAccessMask();

    /**
     * Get the Security IDentifier string associated with this entry.
     *
     * @since 1.0
     */
    String getSid();
}
