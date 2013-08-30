// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi.scripting;

import java.math.BigInteger;
import java.util.Iterator;

import com.jacob.com.Dispatch;
import com.jacob.com.SafeArray;
import com.jacob.com.Variant;
import com.jacob.com.VariantUtilities;

import jsaf.intf.windows.wmi.ISWbemProperty;
import jsaf.io.LittleEndian;
import jsaf.provider.windows.Timestamp;
import jsaf.provider.windows.wmi.WmiException;

/*
 * Wrapper for an SWbemProperty.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SWbemProperty implements ISWbemProperty {
    private Dispatch dispatch;
    private String name;
    private Variant value;

    SWbemProperty(Dispatch dispatch) {
	this.dispatch = dispatch;
	name = Dispatch.call(dispatch, "Name").toString();
	value = Dispatch.call(dispatch, "Value");
    }

    // Implement ISWbemProperty

    public String getName() throws WmiException {
	return name;
    }

    public Object getValue() throws WmiException {
	return value.isNull() ? null : value;
    }

    public Integer getValueAsInteger() throws WmiException {
	return value.getInt();
    }
    
    public Long getValueAsLong() throws WmiException {
	return value.getLong();
    }

    public BigInteger getValueAsTimestamp() throws WmiException {
	if (value.isNull()) {
	    return null;
	} else {
	    switch(value.getvt()) {
	      case Variant.VariantDate:
		return new BigInteger(Double.toString(value.getDate()));

	      default:
		try {
		    return Timestamp.toWindowsTimestamp(getValueAsString());
		} catch (Exception e) {
		    throw new WmiException(e);
		}
	    }
	}
    }

    public Boolean getValueAsBoolean() throws WmiException {
	value.changeType(Variant.VariantBoolean);
	return value.getBoolean();
    }

    /**
     * Returns null if the value is not a String.
     */
    public String getValueAsString() throws WmiException {
	if (value.isNull()) {
	    return null;
	} else {
	    return getString(value);
	}
    }

    /**
     * Returns null if the value is not an Array.
     */
    public String[] getValueAsArray() throws WmiException {
	if (value.isNull()) {
	    return null;
	} else {
	    return value.toSafeArray().toStringArray();
	}
    }

    // Private

    private static String getString(Variant var) {
	if (var.isNull()) {
	    return null;
	} else {
	    int type = var.getvt();
	    switch(type) {
	      case Variant.VariantString:
		return var.getString();

	      case Variant.VariantInt:
		return Integer.toString(var.getInt());

	      case Variant.VariantObject:
		return var.toString();

	      case Variant.VariantByte:
		return LittleEndian.toHexString(var.getByte());

	      case Variant.VariantDispatch:
		return getString(Dispatch.get(var.toDispatch(), "value"));

	      default:
		if (Variant.VariantArray == (Variant.VariantArray & type)) {
		    SafeArray sa = var.toSafeArray();
		    int arrayType = (Variant.VariantTypeMask & type);
		    switch(arrayType) {
		      case Variant.VariantByte:
			return new String(sa.toByteArray());

		      //
		      // Represent the array as a string
		      //
		      default: {
			Variant[] va = sa.toVariantArray();
			StringBuffer sb = new StringBuffer("{");
			for (Variant v : sa.toVariantArray()) {
			    if (sb.length() > 1) {
				sb.append(",");
			    }
			    sb.append(getString(v));
			}
			return sb.append("}").toString();
		      }
		    }
		} else {
		    return var.toString();
		}
	    }
	}
    }
}
