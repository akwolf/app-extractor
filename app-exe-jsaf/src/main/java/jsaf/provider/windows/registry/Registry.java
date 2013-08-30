// Copyright (C) 2012 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.provider.windows.registry;

import java.net.UnknownHostException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.cal10n.LocLogger;

import jsaf.Message;
import jsaf.intf.util.ISearchable;
import jsaf.intf.windows.powershell.IRunspace;
import jsaf.intf.windows.registry.IExpandStringValue;
import jsaf.intf.windows.registry.IKey;
import jsaf.intf.windows.registry.ILicenseData;
import jsaf.intf.windows.registry.IRegistry;
import jsaf.intf.windows.registry.IStringValue;
import jsaf.intf.windows.registry.IValue;
import jsaf.intf.windows.system.IWindowsSession;
import jsaf.util.Base64;
import jsaf.util.StringTools;

/**
 * A class for accessing the Windows registry using Powershell.
 *
 * @author David A. Solin
 * @version %I% %G%
 */
public class Registry implements IRegistry {
    protected IWindowsSession.View view;
    protected IWindowsSession session;
    protected IRunspace runspace;
    protected RegistrySearcher searcher;
    protected ILicenseData license = null;
    protected LocLogger logger;
    protected IKey hklm, hku, hkcu, hkcr, hkcc;
    protected Map<String, IKey> keyMap;
    protected Map<String, IValue[]> valueMap;

    /**
     * Create a new Registry, connected to the default view.
     */
    public Registry(IWindowsSession session) throws Exception {
	this(session, session.getNativeView());
    }

    /**
     * Create a new Registry, connected to the specified view.
     */
    public Registry(IWindowsSession session, IWindowsSession.View view) throws Exception {
	this.session = session;
	logger = session.getLogger();
	this.view = view;
	for (IRunspace runspace : session.getRunspacePool().enumerate()) {
	    if (runspace.getView() == view) {
		this.runspace = runspace;
		break;
	    }
	}
	if (runspace == null) {
	    runspace = session.getRunspacePool().spawn(view);
	}
	runspace.loadModule(Registry.class.getResourceAsStream("Registry.psm1"));
	keyMap = new HashMap<String, IKey>();
	valueMap = new HashMap<String, IValue[]>();
    }

    // Implement ILoggable

    public LocLogger getLogger() {
	return logger;
    }

    public void setLogger(LocLogger logger) {
	this.logger = logger;
    }

    // Implement IRegistry

    public ILicenseData getLicenseData() throws Exception {
	if (license == null) {
	    license = new LicenseData(this);
	}
	return license;
    }

    public ISearchable<IKey> getSearcher() {
	if (searcher == null) {
	    try {
		searcher = new RegistrySearcher(session, runspace);
	    } catch (Exception e) {
		logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
	    }
	}
	return searcher;
    }

    public IKey getHive(Hive hive) {
	switch(hive) {
	  case HKLM:
	    if (hklm == null) {
		hklm = new Key(this, Hive.HKLM, null);
	    }
	    return hklm;

	  case HKU:
	    if (hku == null) {
		hku = new Key(this, Hive.HKU, null);
	    }
	    return hku;

	  case HKCU:
	    if (hkcu == null) {
		hkcu = new Key(this, Hive.HKCU, null);
	    }
	    return hkcu;

	  case HKCR:
	    if (hkcr == null) {
		try {
		    runspace.invoke("New-PSDrive -PSProvider registry -Root HKEY_CLASSES_ROOT -Name HKCR");
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
		hkcr = new Key(this, Hive.HKCR, null);
	    }
	    return hkcr;

	  case HKCC:
	    if (hkcc == null) {
		try {
		    runspace.invoke("New-PSDrive -PSProvider registry -Root HKEY_CURRENT_CONFIG -Name HKCC");
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
		hkcc = new Key(this, Hive.HKCC, null);
	    }
	    return hkcc;

	  default:
	    throw new RuntimeException(Message.getMessage(Message.ERROR_WINREG_HIVE, hive.getName()));
	}
    }

    public IKey getKey(String fullPath) throws NoSuchElementException, RegistryException {
	Hive hive = null;
	String path = null;
	int ptr = fullPath.indexOf(DELIM_STR);
	if (ptr == -1) {
	    hive = Hive.fromName(fullPath);
	} else {
	    hive = Hive.fromName(fullPath.substring(0, ptr));
	    path = fullPath.substring(ptr+1);
	}
	return getKey(hive, path);
    }

    public IKey getKey(Hive hive, String path) throws NoSuchElementException, RegistryException {
	IKey key = new Key(this, hive, path);
	if (keyMap.containsKey(key.toString())) {
	    return keyMap.get(key.toString());
	}
	getHive(hive); // idempotent hive initialization
	String result = null;
	try {
	    result = runspace.invoke("Test-Path -LiteralPath " + getItemPath(key));
	} catch (Exception e) {
	    throw new RegistryException(e);
	}
	if ("True".equalsIgnoreCase(result)) {
	    keyMap.put(key.toString(), key);
	    return key;
	} else {
	    throw new NoSuchElementException(key.toString());
	}
    }

    public IKey[] getKeys(Hive hive, String[] paths) throws RegistryException {
	Map<String, IKey> keys = new HashMap<String, IKey>();
	HashSet<String> uniquePaths = new HashSet<String>();
	for (String path : paths) {
	    IKey key = new Key(this, hive, path);
	    if (keyMap.containsKey(key.toString())) {
		keys.put(path, keyMap.get(key.toString()));
	    } else {
		uniquePaths.add(path);
	    }
	}
	if (uniquePaths.size() > 0) {
	    getHive(hive); // idempotent hive initialization
	    String[] testPaths = uniquePaths.toArray(new String[uniquePaths.size()]);
	    StringBuffer sb = new StringBuffer();
	    for (String path : testPaths) {
		if (sb.length() > 0) {
		    sb.append(",");
		}
		sb.append(getItemPath(new Key(this, hive, path)));
	    }
	    sb.append(" | %{Test-Path -LiteralPath $_} | Transfer-Encode");
	    try {
		String data = new String(Base64.decode(runspace.invoke(sb.toString())), StringTools.UTF8);
		int i=0;
		for (String result : data.split("\r\n")) {
		    if ("true".equalsIgnoreCase(result)) {
			IKey key = new Key(this, hive, testPaths[i]);
			keyMap.put(key.toString(), key);
		    }
		    i++;
		}
	    } catch (Exception e) {
		throw new RegistryException(e);
	    }
	}
	IKey[] results = new IKey[paths.length];
	for (int i=0; i < paths.length; i++) {
	    IKey key = new Key(this, hive, paths[i]);
	    results[i] = keyMap.get(key.toString());
	}
	return results;
    }

    public IKey[] enumSubkeys(IKey key) throws RegistryException {
	try {
	    StringBuffer sb = new StringBuffer("Get-Item -LiteralPath ").append(getItemPath(key));
	    sb.append(" | %{$_.GetSubKeyNames()}");
	    String data = runspace.invoke(sb.toString());
	    if (data == null) {
		return new IKey[0];
	    } else {
		String[] names = data.split("\r\n");
		IKey[] subkeys = new IKey[names.length];
		Hive hive = key.getHive();
		String path = key.getPath();
		for (int i=0; i < subkeys.length; i++) {
		    subkeys[i] = new Key(this, hive, path == null ? names[i] : path + DELIM_STR + names[i]);
		}
		return subkeys;
	    }
	} catch (Exception e) {
	    throw new RegistryException(e);
	}
    }

    public IValue getValue(IKey key, String name) throws NoSuchElementException, RegistryException {
	for (IValue value : enumValues(key)) {
	    if (value.getName().equalsIgnoreCase(name)) {
		return value;
	    }
	}
	throw new NoSuchElementException(name);
    }

    public IValue[] enumValues(IKey key) throws RegistryException {
	if (valueMap.containsKey(key.toString())) {
	    return valueMap.get(key.toString());
	}
	StringBuffer sb = new StringBuffer("Print-RegValues -Hive ");
	sb.append(key.getHive().getName());
	String path = key.getPath();
	if (path != null) {
	    sb.append(" -Key \"").append(path).append("\"");
	}
	sb.append(" | Transfer-Encode");
	ArrayList<IValue> values = new ArrayList<IValue>();
	String data = null;
	try {
	    data = new String(Base64.decode(runspace.invoke(sb.toString())), StringTools.UTF8);
	} catch (Exception e) {
	    throw new RegistryException(e);
	}
	if (data != null) {
	    Iterator<String> iter = StringTools.toList(data.split("\r\n")).iterator();
	    while(true) {
		try {
		    IValue value = nextValue(key, iter);
		    if (value == null) {
			break;
		    } else {
			values.add(value);
		    }
		} catch (Exception e) {
		    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
		}
	    }
	}
	IValue[] result = values.toArray(new IValue[values.size()]);
	valueMap.put(key.toString(), result);
	return result;
    }

    public IValue[] enumValues(Hive hive, String[] paths) throws RegistryException {
	List<IValue> results = new ArrayList<IValue>();
	HashSet<String> uniquePaths = new HashSet<String>();
	for (String path : paths) {
	    if (!uniquePaths.contains(path)) {
		Key key = new Key(this, hive, path);
		if (valueMap.containsKey(key.toString())) {
		    results.addAll(Arrays.asList(valueMap.get(key.toString())));
		} else {
		    uniquePaths.add(path);
		}
	    }
	}
	if (uniquePaths.size() > 0) {
	    StringBuffer sb = new StringBuffer();
	    for (String path : uniquePaths) {
		if (sb.length() > 0) {
		    sb.append(",");
		}
		sb.append("\"").append(path).append("\"");
	    }
	    sb.append(" | Print-RegValues -Hive ").append(hive.getName()).append(" | Transfer-Encode");
	    String data = null;
	    try {
		data = new String(Base64.decode(runspace.invoke(sb.toString())), StringTools.UTF8);
	    } catch (Exception e) {
		throw new RegistryException(e);
	    }
	    if (data != null) {
		Iterator<String> iter = StringTools.toList(data.split("\r\n")).iterator();
		IKey key = null;
		while((key = nextKey(iter)) != null) {
		    ArrayList<IValue> values = new ArrayList<IValue>();
		    while(true) {
			try {
			    IValue value = nextValue(key, iter);
			    if (value == null) {
				break;
			    } else {
				values.add(value);
			    }
			} catch (Exception e) {
			    logger.warn(Message.getMessage(Message.ERROR_EXCEPTION), e);
			}
		    }
		    valueMap.put(key.toString(), values.toArray(new IValue[values.size()]));
		    results.addAll(values);
		}
	    }
	}
	return results.toArray(new IValue[results.size()]);
    }

    public String getStringValue(Hive hive, String subkey, String value) throws Exception {
	IValue val = getKey(hive, subkey).getValue(value);
	switch(val.getType()) {
	  case REG_SZ:
	    return ((IStringValue)val).getData();
	  case REG_EXPAND_SZ:
	    return ((IExpandStringValue)val).getExpandedData(session.getEnvironment());
	  default:
	    return null;
	}
    }

    // Private

    /**
     * Returns the quoted String suitable for passing as a -LiteralPath to Test-Path or Get-Item.
     */
    private String getItemPath(IKey key) {
	StringBuffer sb = new StringBuffer("\"Registry::").append(key.getHive().getName());
	String path = key.getPath();
	if (path != null) {
	    sb.append(IRegistry.DELIM_STR).append(path);
	}
	sb.append("\"");
	return sb.toString();
    }

    static final String START = "{";
    static final String END = "}";
    static final String EOF = "[EOF]";

    /**
     * Generate the next key from the input.
     */
    private IKey nextKey(Iterator<String> input) {
	while(input.hasNext()) {
	    String line = input.next().trim();
	    if (line.startsWith("[") && line.endsWith("]")) {
		String fullPath = line.substring(1,line.length()-1);
		Hive hive = null;
		String path = null;
		int ptr = fullPath.indexOf(DELIM_STR);
		if (ptr == -1) {
		    hive = Hive.fromName(fullPath);
		} else {
		    hive = Hive.fromName(fullPath.substring(0, ptr));
		    path = fullPath.substring(ptr+1);
		}
		IKey key = new Key(this, hive, path);
		if (!keyMap.containsKey(key.toString())) {
		    keyMap.put(key.toString(), key);
		}
		return key;
	    }
	}
	return null;
    }

    /**
     * Generate the next value for the key from the input.
     */
    private IValue nextValue(IKey key, Iterator<String> input) throws Exception {
	boolean start = false;
	while(input.hasNext()) {
	    String line = input.next();
	    if (line.trim().equals(START)) {
		start = true;
		break;
	    } else if (line.startsWith("Error:")) {
		throw new RegistryException(line);
	    } else if (line.trim().equals(EOF)) {
		break;
	    }
	}
	if (start) {
	    String name = null;
	    IValue.Type type = null;
	    String data = null;
	    List<String> multiData = null;
	    while(input.hasNext()) {
		String line = input.next();
		if (line.equals(END)) {
		    break;
		} else if (line.startsWith("Kind: ")) {
		    type = IValue.Type.fromKind(line.substring(6));
		} else if (line.startsWith("Name: ")) {
		    name = line.substring(6);
		} else if (line.startsWith("Data: ")) {
		    if (type == IValue.Type.REG_MULTI_SZ) {
			if (multiData == null) {
			    multiData = new ArrayList<String>();
			}
			multiData.add(line.substring(6));
		    } else {
			data = line.substring(6);
		    }
		} else if (data != null) {
		    // continuation of data beyond a line-break
		    data = data + line;
		} else if (multiData != null) {
		    // continuation of last data entry beyond a line-break
		    multiData.add(new StringBuffer(multiData.remove(multiData.size() - 1)).append(line).toString());
		}
	    }
	    if (type != null) {
		IValue value = null;
		switch(type) {
		  case REG_NONE:
		    value = new NoneValue(key, name);
		    break;
		  case REG_SZ:
		    value = new StringValue(key, name, data);
		    break;
		  case REG_EXPAND_SZ:
		    value = new ExpandStringValue(key, name, data);
		    break;
		  case REG_MULTI_SZ:
		    value = new MultiStringValue(key, name, multiData == null ? null : multiData.toArray(new String[0]));
		    break;
		  case REG_DWORD:
		    value = new DwordValue(key, name, new BigInteger(data, 16));
		    break;
		  case REG_QWORD:
		    value = new QwordValue(key, name, new BigInteger(data, 16));
		    break;
		  case REG_BINARY:
		    value = new BinaryValue(key, name, Base64.decode(data, Base64.DONT_GUNZIP));
		    break;
		}
		logger.trace(Message.STATUS_WINREG_VALINSTANCE, value.toString());
		return value;
	    }
	}
	return null;
    }
}
