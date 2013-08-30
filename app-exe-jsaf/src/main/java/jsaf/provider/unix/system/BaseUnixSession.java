// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.unix.system;

import java.util.Arrays;
import java.util.List;

import jsaf.Message;
import jsaf.intf.unix.system.IUnixSession;
import jsaf.provider.AbstractSession;
import jsaf.util.SafeCLI;

/**
 * A simple session implementation for Unix machines.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public abstract class BaseUnixSession extends AbstractSession implements IUnixSession {
    protected Flavor flavor = Flavor.UNKNOWN;

    protected BaseUnixSession() {
	super();
    }

    protected List<String> getBaseCommand() {
	return Arrays.asList("/bin/sh", "-c");
    }

    @Override
    protected String getOverrideKey(String key) {
	if (flavor == null) {
	    // during initialization of the super-class
	    return null;
	} else {
	    switch(flavor) {
	      case UNKNOWN:
		return null;

	      default:
		return new StringBuffer(flavor.value()).append(".").append(key).toString();
	    }
	}
    }

    // Implement ISession

    public Type getType() {
	return Type.UNIX;
    }

    @Override
    public String getMachineName() {
	if (isConnected()) {
	    try {
		return SafeCLI.exec("hostname", this, Timeout.S);
	    } catch (Exception e) {
		logger.warn(Message.ERROR_MACHINENAME, e.getMessage());
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
	return getHostname();
    }

    // Implement IUnixSession

    public Flavor getFlavor() {
	return flavor;
    }
}
