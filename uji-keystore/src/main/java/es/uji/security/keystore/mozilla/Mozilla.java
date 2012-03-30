package es.uji.security.keystore.mozilla;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.uji.security.crypto.config.OperatingSystemUtils;

public class Mozilla
{
    private List<String> guessProfileDirectories;
    private String lockFile;

    public Mozilla()
    {
        String userHome = System.getProperty("user.home");

        retrieveConfigIfLinux(userHome);
        retrieveConfigIfWindows(userHome);
        retrieveConfigIfMacOSX(userHome);
    }

    private void retrieveConfigIfMacOSX(String userHome)
    {
        if (OperatingSystemUtils.isMac())
        {
            guessProfileDirectories = new ArrayList<String>();
            guessProfileDirectories.add(userHome + "/.mozilla/firefox/");
            guessProfileDirectories
                    .add(userHome + "/Library/Application Support/Firefox/Profiles/");
            lockFile = ".parentlock";
        }
    }

    private void retrieveConfigIfWindows(String userHome)
    {
        if (OperatingSystemUtils.isWindowsUpperEqualToNT())
        {
            guessProfileDirectories = Collections.singletonList(OperatingSystemUtils
                    .getCurrentUserApplicationDataDirectory() + "\\Mozilla\\Firefox\\Profiles\\");
            lockFile = "parent.lock";
        }
    }

    private void retrieveConfigIfLinux(String userHome)
    {
        if (OperatingSystemUtils.isLinux())
        {
            guessProfileDirectories = Collections.singletonList(userHome + "/.mozilla/firefox/");
            lockFile = ".parentlock";
        }
    }

    public String getCurrentProfileDirectory()
    {
        for (String baseDirectory : guessProfileDirectories)
        {
            for (String potentialProfileDirectory : new File(baseDirectory).list())
            {
                File currentDescriptor = new File(baseDirectory + potentialProfileDirectory);

                if (currentDescriptor.isDirectory())
                {
                    String[] pathsWithLockFile = currentDescriptor.list(new FilenameFilter()
                    {
                        @Override
                        public boolean accept(File dir, String name)
                        {
                            return name.equals(lockFile);
                        }
                    });

                    if (pathsWithLockFile != null && pathsWithLockFile.length > 0)
                    {
                        return potentialProfileDirectory;
                    }
                }
            }
        }

        return null;
    }
    /*
     * public ByteArrayInputStream getPkcs11ConfigInputStream() { String _pkcs11file =
     * getPkcs11FilePath(); String _currentprofile = getCurrentProfiledir(); ByteArrayInputStream
     * bais = null;
     * 
     * if (OperatingSystemUtils.isWindowsUpperEqualToNT()) { bais = new
     * ByteArrayInputStream(("name = NSS\r" + "library = " + _pkcs11file + "\r" +
     * "attributes= compatibility" + "\r" + "slot=2\r" + "nssArgs=\"" + "configdir='" +
     * _currentprofile.replace("\\", "/") + "' " + "certPrefix='' " + "keyPrefix='' " +
     * "secmod=' secmod.db' " + "flags=readOnly\"\r").getBytes()); } else if
     * (OperatingSystemUtils.isLinux() || OperatingSystemUtils.isMac()) { bais = new
     * ByteArrayInputStream(("name = NSS\r" + "library = " + _pkcs11file + "\n" +
     * "attributes= compatibility" + "\n" + "slot=2\n" + "nssArgs=\"" + "configdir='" +
     * _currentprofile + "' " + "certPrefix='' " + "keyPrefix='' " + "secmod=' secmod.db' " +
     * "flags=readOnly\"\n").getBytes()); }
     * 
     * return bais; }
     * 
     * public String getPkcs11InitArgsString() { return "configdir='" +
     * getCurrentProfiledir().replace("\\", "/") +
     * "' certPrefix='' keyPrefix='' secmod='secmod.db' flags="; }
     */

    public String getPKCS11Library()
    {
        List<String> guessPaths = new ArrayList<String>();
        guessPaths.add("/usr/lib/");
        guessPaths.add("/usr/lib/nss/");
        guessPaths.addAll(getLibraryPaths("/usr/lib/", "*-linux-gnu", "/nss/"));
        guessPaths.addAll(getLibraryPaths("/usr/lib/", "firefox-*", "/"));

        return getFirstExistingLibsoftokn3(guessPaths);
    }

    private String getFirstExistingLibsoftokn3(List<String> guessPaths)
    {
        for (String path : guessPaths)
        {
            if (libsoftokn3ExistsInPath(path))
            {
                return path;
            }
        }

        return null;
    }

    private boolean libsoftokn3ExistsInPath(String path)
    {
        File basePath = new File(path);
        String[] fileList = basePath.list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.equals("libsoftokn3.so");
            }
        });

        return (fileList != null && fileList.length > 0);
    }

    private List<String> getLibraryPaths(String basePath, final String expression, String subPath)
    {
        String[] firefoxPaths = new File(basePath).list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                if (expression.startsWith("*"))
                {
                    return name.endsWith(expression.substring(1));
                }
                else if (expression.endsWith("*"))
                {
                    return name.startsWith(expression.substring(0, expression.length() - 2));
                }
                else
                {
                    return name.equals(expression);
                }
            }
        });

        List<String> paths = new ArrayList<String>();

        for (String path : firefoxPaths)
        {
            paths.add("/usr/lib/" + path + subPath);
        }

        return paths;
    }
}
