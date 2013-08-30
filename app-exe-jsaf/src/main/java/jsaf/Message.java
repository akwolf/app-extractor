// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.IMessageConveyor;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;
import ch.qos.cal10n.MessageConveyor;
import ch.qos.cal10n.MessageConveyorException;
import ch.qos.cal10n.MessageParameterObj;

import org.slf4j.cal10n.LocLogger;
import org.slf4j.cal10n.LocLoggerFactory;

/**
 * Uses cal10n to define localized messages for jSAF.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
@BaseName("jsafmsg")
@LocaleData(
  defaultCharset="ASCII",
  value = { @Locale("en_US") }
)
public enum Message {
    ERROR_AD_BAD_OU,
    ERROR_AD_DOMAIN_REQUIRED,
    ERROR_AD_DOMAIN_UNKNOWN,
    ERROR_AD_INIT,
    ERROR_AD_SID,
    ERROR_ASCII_CONVERSION,
    ERROR_EOF,
    ERROR_EOS,
    ERROR_EXCEPTION,
    ERROR_FACTORY_CLASS,
    ERROR_FACTORY_INITIALIZER,
    ERROR_FS_LSTAT,
    ERROR_FS_SEARCH,
    ERROR_FS_SEARCH_LINE,
    ERROR_GROUP_SUBGROUP,
    ERROR_GROUP_USER,
    ERROR_IO,
    ERROR_IO_NOT_DIR,
    ERROR_IO_NOT_FILE,
    ERROR_LINK_NOWHERE,
    ERROR_MACHINENAME,
    ERROR_MESSAGE_CONVEYOR,
    ERROR_MISSING_RESOURCE,
    ERROR_POWERSHELL_STOPPED,
    ERROR_POWERSHELL_TIMEOUT,
    ERROR_PROCESS_RETRY,
    ERROR_PROCESS_RUNNING,
    ERROR_PROCESS_STOPPED,
    ERROR_PROTOCOL,
    ERROR_SESSION_CREDENTIAL_PASSWORD,
    ERROR_SESSION_INTEGRITY,
    ERROR_UNIX_FLAVOR,
    ERROR_UNIXFILEINFO,
    ERROR_UNSUPPORTED_UNIX_FLAVOR,
    ERROR_WINREG_HIVE,
    STATUS_AD_DOMAIN_ADD,
    STATUS_AD_DOMAIN_SKIP,
    STATUS_AD_GROUP_SKIP,
    STATUS_COMMAND_OUTPUT_PROGRESS,
    STATUS_COMMAND_OUTPUT_TEMP,
    STATUS_CONFIG_OVERLAY,
    STATUS_CONFIG_SESSION,
    STATUS_FS_CACHE_RETRIEVE,
    STATUS_FS_CACHE_STORE,
    STATUS_FS_MOUNT_ADD,
    STATUS_FS_MOUNT_FILTER,
    STATUS_FS_MOUNT_SKIP,
    STATUS_FS_SEARCH_CACHED,
    STATUS_FS_SEARCH_DONE,
    STATUS_FS_SEARCH_GUESS,
    STATUS_FS_SEARCH_GUESS_OVERFLOW,
    STATUS_FS_SEARCH_MATCH,
    STATUS_FS_SEARCH_PROGRESS,
    STATUS_FS_SEARCH_START,
    STATUS_FS_SEARCH_TEMP,
    STATUS_NAME_DOMAIN_ERR,
    STATUS_NAME_DOMAIN_OK,
    STATUS_POWERSHELL_ASSEMBLY_LOAD,
    STATUS_POWERSHELL_ASSEMBLY_SKIP,
    STATUS_POWERSHELL_EXIT,
    STATUS_POWERSHELL_INVOKE,
    STATUS_POWERSHELL_MODULE_LOAD,
    STATUS_POWERSHELL_MODULE_SKIP,
    STATUS_POWERSHELL_SPAWN,
    STATUS_PROCESS_END,
    STATUS_PROCESS_RETRY,
    STATUS_PROCESS_START,
    STATUS_SESSION_DISPOSE,
    STATUS_UPN_CONVERT,
    STATUS_WINDOWS_BITNESS,
    STATUS_WINREG_VALINSTANCE,
    STATUS_WMI_CONNECT,
    STATUS_WMI_DISCONNECT,
    STATUS_WMI_QUERY,
    WARNING_COMMAND_OUTPUT,
    WARNING_MISSING_OUTPUT,
    WARNING_PERISHABLEIO_INTERRUPT;

    private static IMessageConveyor baseConveyor;
    private static MultiConveyor conveyor;
    private static LocLoggerFactory loggerFactory;
    private static LocLogger sysLogger;

    static {
	baseConveyor = new MessageConveyor(java.util.Locale.getDefault());
	try {
	    //
	    // Get a message to test whether localized messages are available for the default Locale
	    //
	    baseConveyor.getMessage(ERROR_EXCEPTION);
	} catch (MessageConveyorException e) {
	    //
	    // The test failed, so set the message Locale to English
	    //
	    baseConveyor = new MessageConveyor(java.util.Locale.ENGLISH);
	}
	conveyor = new MultiConveyor();
	loggerFactory = new LocLoggerFactory(conveyor);
	sysLogger = loggerFactory.getLocLogger(Message.class);
    }

    /**
     * Extend Message to be able to provide messages for the specified Enum class, using the specified IMessageConveyor.
     */
    public static void extend(Class<? extends Enum<?>> clazz, IMessageConveyor mc) {
	conveyor.conveyors.put(clazz, mc);
    }

    /**
     * Retrieve the default localized system logger used by the jSAF library.
     */
    public static LocLogger getLogger() {
	return sysLogger;
    }

    /**
     * Retrieve a localized String, given the key and substitution arguments.
     */
    public static String getMessage(Enum<?> key, Object... args) {
	return conveyor.getMessage(key, args);
    }

    public static Set<Map.Entry<Class<? extends Enum<?>>, IMessageConveyor>> getConveyors() {
	return conveyor.conveyors.entrySet();
    }

    // Internal

    /**
     * An IMessageConveyor that consolidates multiple IMessageConveyors.
     */
    static class MultiConveyor implements IMessageConveyor {
	HashMap<Class<? extends Enum<?>>, IMessageConveyor> conveyors;

	MultiConveyor() {
	    conveyors = new HashMap<Class<? extends Enum<?>>, IMessageConveyor>();
	    conveyors.put(Message.class, baseConveyor);
	}

	public <E extends Enum<?>>String getMessage(E key, Object... args) throws MessageConveyorException {
	    IMessageConveyor mc = conveyors.get(key.getDeclaringClass());
	    if (mc == null) {
		throw new MessageConveyorException(baseConveyor.getMessage(ERROR_MESSAGE_CONVEYOR, key.getClass().getName()));
	    } else {
		return mc.getMessage(key, args);
	    }
	}

	public String getMessage(MessageParameterObj mpo) throws MessageConveyorException {
	    return getMessage(mpo.getKey(), mpo.getArgs());
	}
     }
}
