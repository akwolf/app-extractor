package jpe.test;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import jpe.header.Header;
import jpe.resource.version.VsVersionInfo;
import jsaf.intf.io.IFile;
import jsaf.intf.io.IFilesystem;
import jsaf.intf.system.ISession;
import jsaf.provider.unix.system.UnixSession;

/**
 * @author zhangh
 * @createTime 2013-8-29 下午4:03:33
 */
public class Main {

	// Linux OS直接运行即可
	// Windows OS将jacob的dll添加到类路径下
	public static void main(String[] args) throws IOException {
		String path = "/home/justsy/samba/setup1.0.0.10.exe";
		File file = new File(path);
		ISession session = new UnixSession(file);
		if (session.connect()) {
			IFilesystem fs = session.getFilesystem();
			IFile f = fs.getFile(path);
			Header header = new Header(f);
			VsVersionInfo versionInfo = header.getVersionInfo();
			String key = VsVersionInfo.LANGID_KEY;
			Hashtable<String, String> stringTable = versionInfo.getStringTable(key);
			if (stringTable.containsKey("ProductVersion")) {
				System.out.println("Product Version: " + stringTable.get("ProductVersion"));
			}
		}

	}
}
