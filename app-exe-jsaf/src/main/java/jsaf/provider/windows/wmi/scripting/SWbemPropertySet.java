// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.wmi.scripting;

import java.util.Iterator;
import java.util.ArrayList;

import com.jacob.com.Dispatch;
import com.jacob.com.EnumVariant;

import jsaf.intf.windows.wmi.ISWbemProperty;
import jsaf.intf.windows.wmi.ISWbemPropertySet;
import jsaf.provider.windows.wmi.WmiException;

/**
 * Wrapper for an SWbemPropertySet.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class SWbemPropertySet implements ISWbemPropertySet {
    private Dispatch dispatch;
    private ArrayList<ISWbemProperty> properties;

    SWbemPropertySet(Dispatch dispatch) {
	this.dispatch = dispatch;
	EnumVariant enumVariant = new EnumVariant(dispatch);
	properties = new ArrayList<ISWbemProperty>();
	while(enumVariant.hasMoreElements()) {
	    properties.add(new SWbemProperty(enumVariant.nextElement().toDispatch()));
	}
    }

    // Implement ISWbemProperties

    public Iterator<ISWbemProperty> iterator() {
	return properties.iterator();
    }

    public int getSize() {
	return properties.size();
    }

    public ISWbemProperty getItem(String itemName) throws WmiException {
	Iterator<ISWbemProperty> iter = iterator();
	while(iter.hasNext()) {
	    ISWbemProperty prop = iter.next();
	    if (itemName.equalsIgnoreCase(prop.getName())) {
		return prop;
	    }
	}
	return null;
    }
}
