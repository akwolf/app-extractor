// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.intf.windows.registry;

import java.math.BigInteger;

/**
 * Interface to a Windows registry QWORD value.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public interface IQwordValue extends IValue {
    /**
     * Get the data.
     *
     * @since 1.0
     */
    public BigInteger getData();
}
