// Copyright (C) 2011 jOVAL.org.  All rights reserved.
// This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt

package jsaf.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import jsaf.intf.util.IProperty;

/**
 * A class for interpreting an ini-style config file.  Each header is treated as a section full of properties.  Comment
 * lines begin with a ';'.  Delimiters can be either ':' or '=', and can appear in the name of a Key if escaped in the file
 * using a \ character.  Key and value names are trimmed of leading and trailing white-space.  Multi-line values can be
 * created by ending the line with the \ character, in which case whitespace is not trimmed.
 *
 * @author David A. Solin
 * @version %I% %G%
 * @since 1.0
 */
public class IniFile {
    private static final String SEMICOLON	= ";";
    private static final String HASH		= "#";
    private static final String ESC		= "\\";
    private static final String COLON		= ":";
    private static final String EQUAL		= "=";

    private Hashtable<String, IProperty> sections;

    /**
     * Create an empty IniFile.
     */
    public IniFile() {
	sections = new Hashtable<String, IProperty>();
    }

    /**
     * Create a new IniFile from a File using the default encoding.
     */
    public IniFile(File f) throws IOException {
	this();
	load(f);
    }

    /**
     * Create a new IniFile from a stream using the default encoding..
     */
    public IniFile(InputStream in) throws IOException {
	this();
	load(in);
    }

    /**
     * Create a new IniFile from a stream using the specified encoding.
     *
     * @since 1.0
     */
    public IniFile(InputStream in, Charset encoding) throws IOException {
	this();
	load(in, encoding);
    }

    /**
     * A convenience method for loading files, using the default encoding.
     *
     * @since 1.0
     */
    public void load(File f) throws IOException {
	load(new FileInputStream(f));
    }

    /**
     * Add configuration data from a stream.  If the IniFile already contains configuration information,
     * the information from the stream is added.
     *
     * @since 1.0
     */
    public void load(InputStream in) throws IOException {
	load(in, Charset.defaultCharset());
    }

    /**
     * Load using a specific encoding.
     *
     * @since 1.0
     */
    public void load(InputStream in, Charset encoding) throws IOException {
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(in, encoding));
	    String line = null;
	    IProperty section = null;
	    String name = null;
	    int ptr;
	    while ((line = br.readLine()) != null) {
		if (line.startsWith("[") && line.trim().endsWith("]")) {
		    name = line.substring(1, line.length() - 1);
		    section = sections.get(name);
		    if (section == null) {
			section = new PropertyUtil();
			sections.put(name, section);
		    }
		} else if (line.startsWith(SEMICOLON) || line.startsWith(HASH)) {
		    // skip comment
		} else if ((ptr = delimIndex(line)) > 0) {
		    if (section != null) {
			String rawKey = line.substring(0,ptr).trim();
			StringTokenizer tok = new StringTokenizer(rawKey, ESC);
			StringBuffer sb = new StringBuffer(tok.nextToken());
			while(tok.hasMoreTokens()) {
			    String token = tok.nextToken();
			    switch((int)token.charAt(0)) {
			      case ':':
			      case '=':
				break;
			      default:
				sb.append(ESC);
			    }
			    sb.append(token);
			}
			String key = sb.toString();
			String rawVal = line.substring(ptr+1);
			if (rawVal.endsWith(ESC)) {
			    StringBuffer val = new StringBuffer(rawVal.substring(0, rawVal.length() - 1));
			    while ((rawVal = br.readLine()) != null) {
				val.append("\n");
				if (rawVal.endsWith(ESC)) {
				    val.append(rawVal.substring(0, rawVal.length() - 1));
				} else {
				    val.append(rawVal.trim());
				    break;
				}
			    }
			    section.setProperty(key, val.toString());
			} else {
			    section.setProperty(key, rawVal.trim());
			}
		    }
		}
	    }
	} finally {
	    if (br != null) {
		try {
		    br.close();
		} catch (IOException e) {
		}
	    }
	}
    }

    /**
     * Write the INI file to a stream.
     *
     * @since 1.0
     */
    public synchronized void save(OutputStream out) throws IOException {
	PrintWriter writer = new PrintWriter(out);
	writer.println("; Saved " + new java.util.Date().toString());
	for (String header : listSections()) {
	    writer.println("[" + header + "]");
	    IProperty props = getSection(header);
	    for (String key : props) {
		String escapedKey = key.replace(COLON, ESC + COLON).replace(EQUAL, ESC + EQUAL);
		String escapedValue = props.getProperty(key).replace("\r\n", "\n").replace("\n", ESC + "\n");
		writer.println(escapedKey + COLON + escapedValue);
	    }
	}
	writer.close();
    }

    /**
     * Return a collection of all the sections.
     *
     * @since 1.0
     */
    public Collection<String> listSections() {
	return sections.keySet();
    }

    /**
     * Test for the existence of a section.
     *
     * @since 1.0
     */
    public boolean containsSection(String name) {
	return sections.containsKey(name);
    }

    /**
     * Get a section of the INI file.
     *
     * @since 1.0
     */
    public IProperty getSection(String name) throws NoSuchElementException {
	IProperty section = sections.get(name);
	if (section == null) {
	    throw new NoSuchElementException(name);
	} else {
	    return section;
	}
    }

    /**
     * Remove a section from the INI file.
     *
     * @since 1.0
     */
    public void deleteSection(String name) throws NoSuchElementException {
	if (sections.containsKey(name)) {
	    sections.remove(name);
	} else {
	    throw new NoSuchElementException(name);
	}
    }

    /**
     * Get the section with the given name, or create it if it doesn't already exist.
     *
     * @since 1.0
     */
    public IProperty getCreateSection(String name) {
	if (!sections.containsKey(name)) {
	    sections.put(name, new PropertyUtil());
	}
	return sections.get(name);
    }

    /**
     * Shortcut for getSection(section).getProperty(key).
     *
     * @since 1.0
     */
    public String getProperty(String section, String key) throws NoSuchElementException {
	return getSection(section).getProperty(key);
    }

    // Private

    private int delimIndex(String line) {
	int i1 = -1;
	while ((i1 = (line.indexOf(COLON, i1 + 1))) > 0) {
	    if (line.charAt(i1 - 1) != '\\') {
		break;
	    }
	}

	int i2 = -1;
	while ((i2 = (line.indexOf(EQUAL, i2 + 1))) > 0) {
	    if (line.charAt(i2 - 1) != '\\') {
		break;
	    }
	}

	if (i1 == -1) {
	    return i2;
	} else if (i2 == -1) {
	    return i1;
	} else {
	    return Math.min(i1, i2);
	}
    }
}
