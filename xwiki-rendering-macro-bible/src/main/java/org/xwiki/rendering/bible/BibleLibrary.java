package org.xwiki.rendering.bible;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.crosswire.jsword.book.Book;
import org.crosswire.jsword.book.BookData;
import org.crosswire.jsword.book.BookException;
import org.crosswire.jsword.book.BookFilter;
import org.crosswire.jsword.book.BookFilters;
import org.crosswire.jsword.book.Books;
import org.crosswire.jsword.book.OSISUtil;
import org.crosswire.jsword.book.install.InstallException;
import org.crosswire.jsword.book.install.InstallManager;
import org.crosswire.jsword.book.install.Installer;
import org.crosswire.jsword.book.sword.ConfigEntryTable;
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.NoSuchKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BibleLibrary
{

    private static HashMap<String, String> initialsJSWORD2APP = new HashMap<String, String>();

    private static HashMap<String, String> initialsAPP2JSWORD = new HashMap<String, String>();

    public static void setInitialMap(String jswordInitials, String appInitials)
    {
        initialsJSWORD2APP.put(jswordInitials, appInitials);
        initialsAPP2JSWORD.put(appInitials, jswordInitials);
    }

    static {
        // java.util.logging.Logger log =
        // java.util.logging.LogManager.getLogManager().getLogger(" org.crosswire.jsword.book.sword.ConfigEntryTable");
        // for (java.util.logging.Handler h : log.getHandlers()) {
        // h.setLevel(java.util.logging.Level.OFF);
        // }
        try {
            // org.apache.log4j.Logger.getLogger("org.springframework").setLevel(org.apache.log4j.Level.OFF);
            java.util.logging.Logger.getLogger("org.crosswire").setLevel(java.util.logging.Level.OFF);
            // Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            // root.setLevel(org.slf4j.Level.DEBUG);
        } catch (Exception e) {
        }

        setInitialMap("ASV", "ASV");
        setInitialMap("Jubilee2000", "JUB");
        setInitialMap("ESV", "ESV");
        setInitialMap("GerElb1905", "ELB");
        setInitialMap("GerSch", "SCH");
        setInitialMap("GerHfa2002", "HFA");
        setInitialMap("GerLut1912", "LUT");
        setInitialMap("GerNeUe", "NEU");
    }

    public static List<Book> getBooks()
    {
        return Books.installed().getBooks(BookFilters.getBibles());
    }

    public static String getCanonicalText(Book book, String name)
    {
        try {
            Key key = book.getKey(name);
            if (book.contains(key)) {
                BookData data = new BookData(book, key);
                String text = OSISUtil.getCanonicalText(data.getOsisFragment());
                return text;
            }

        } catch (NoSuchKeyException e) {
            // error = true;
            return e.getMessage();
        } catch (BookException e) {
            // error = true;
            return e.getMessage();
        } catch (StringIndexOutOfBoundsException e) {
            // error = true;
            return "Not Found / Nicht gefunden";
        }
        return null;
    }

    public static String getCanonicalText(String bibleAndVerse)
    {
        // boolean error = false;

        if (bibleAndVerse != null) {

            if (!bibleAndVerse.contains("#")) {
                return "Symbol # between Bible and Verse is missing";
            }

            int index = bibleAndVerse.indexOf("#");
            String initials = bibleAndVerse.substring(0, index);
            String name = bibleAndVerse.substring(index + 1, bibleAndVerse.length());

            if (containsInitials(initials)) {
                if (getBook(initials) != null) {
                    return getCanonicalText(getBook(initials), name);
                } else {
                    // error = true;
                    return "book is not installed";
                }
            } else {
                // error = true;
                return "book with initials is not known, must be ASV, JUB, ESV, ELB, SCH, HFA, LUT, NEU";
            }

        } else {
            return "no key given";
        }

    }

    public static Book getBook(String bookInitials)
    {
        return Books.installed().getBook(initialsAPP2JSWORD.get(bookInitials));
    }

    private static boolean containsInitials(String initials)
    {
        return initialsAPP2JSWORD.containsKey(initials);
    }

    private static boolean isConnected()
    {
        boolean connected = false;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface interf = interfaces.nextElement();
                if (interf.isUp() && !interf.isLoopback()) {
                    List<InterfaceAddress> adrs = interf.getInterfaceAddresses();
                    for (Iterator<InterfaceAddress> iter = adrs.iterator(); iter.hasNext();) {
                        InterfaceAddress adr = iter.next();
                        InetAddress inadr = adr.getAddress();
                        if (inadr instanceof Inet4Address) {
                            connected = true;
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return connected;
    }

    public static void initialize()
    {
        if (isConnected()&&getBooks().size()==0) {
//            System.out.println("Online (x Books): " + getBooks().size());

            InstallManager imanager = new InstallManager();
            Map installers = imanager.getInstallers();
            Iterator iter = installers.entrySet().iterator();
            String name = null;
            while (iter.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iter.next();
                name = (String) mapEntry.getKey();
                Installer installer = (Installer) mapEntry.getValue();
                try {
                    installer.reloadBookList();
                    for (Object bookObject : installer.getBooks()) {
                        Book book = (Book) bookObject;

                        if (initialsJSWORD2APP.containsKey(book.getInitials())) {
                            if (Books.installed().getBook(book.getInitials()) == null) {
                                installer.install(book);
                                System.out.println("Install book '" + book.getInitials() + "'");
                            }
                        }
                    }

                } catch (InstallException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class SimpleBookFilter implements BookFilter
    {
        public SimpleBookFilter(String bookKey)
        {
            name = bookKey;
        }

        public boolean test(Book bk)
        {
            return bk.getInitials().equals(name);
        }

        private String name;
    }
}
