package org.xwiki.rendering.bible;

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
import org.crosswire.jsword.passage.Key;
import org.crosswire.jsword.passage.NoSuchKeyException;

public class BibleLibrary {

	private static HashMap<String, String> initialsJSWORD2APP = new HashMap<String, String>();

	private static HashMap<String, String> initialsAPP2JSWORD = new HashMap<String, String>();

	public static void setInitialMap(String jswordInitials, String appInitials) {
		initialsJSWORD2APP.put(jswordInitials, appInitials);
		initialsAPP2JSWORD.put(appInitials, jswordInitials);
	}

	static {
		setInitialMap("ASV", "ASV");
		setInitialMap("Jubilee2000", "JUB");
		setInitialMap("ESV", "ESV");
		setInitialMap("GerElb1905", "ELB");
		setInitialMap("GerSch", "SCH");
		setInitialMap("GerHfa2002", "HFA");
		setInitialMap("GerLut1912", "LUT");
		setInitialMap("GerNeUe", "NEU");
	}

	public static List<Book> getBooks() {
		return Books.installed().getBooks(BookFilters.getBibles());
	}

	public static String getCanonicalText(Book book, String name) {
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

	public static String getCanonicalText(String bibleAndVerse) {
		// boolean error = false;

		if (bibleAndVerse != null) {

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

	public static Book getBook(String bookInitials) {
		return Books.installed().getBook(initialsAPP2JSWORD.get(bookInitials));
	}

	private static boolean containsInitials(String initials) {
		return initialsAPP2JSWORD.containsKey(initials);
	}

	public static void initialize() {
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
//				installer.getBooks();
			} catch (InstallException e) {
				e.printStackTrace();
			}
		}

	}

	private static class SimpleBookFilter implements BookFilter {
		public SimpleBookFilter(String bookKey) {
			name = bookKey;
		}

		public boolean test(Book bk) {
			return bk.getInitials().equals(name);
		}

		private String name;
	}
}
