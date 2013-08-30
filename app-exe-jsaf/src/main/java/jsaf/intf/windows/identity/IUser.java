// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.identity;

import java.util.Collection;

import jsaf.identity.IdentityException;

/**
 * The IUser interface provides information about a Windows user.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IUser extends IPrincipal {
    /**
     * Returns the Netbios names (DOMAIN\NAME) of all groups of which the user is a member.  Non-recursive (i.e., only
     * groups containing this user, not groups containing groups containing this user, etc.).
     *
     * @since 1.0
     */
    Collection<String> getGroupNetbiosNames() throws IdentityException;

    /**
     * Is the user account enabled or disabled?
     *
     * @since 1.0
     */
    boolean isEnabled() throws IdentityException;
}
