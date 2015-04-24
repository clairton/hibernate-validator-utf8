package br.eti.clairton.validation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UTF8ResourceBundleLocator implements ResourceBundleLocator {
	private static final Logger logger = LoggerFactory
			.getLogger(UTF8ResourceBundleLocator.class);

	protected static final ResourceBundle.Control UTF8_CONTROL = new UTF8Control();

	private final String bundleName;

	UTF8ResourceBundleLocator(String bundleName) {
		this.bundleName = bundleName;
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found,
	 * search validator (this) classloader.
	 *
	 * @param locale
	 *            The locale of the bundle to load.
	 * @return the resource bundle or <code>null</code> if none is found.
	 */
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle rb = null;
		ClassLoader classLoader = GetClassLoader.fromContext();
		if (classLoader != null) {
			rb = loadBundle(classLoader, locale, bundleName
					+ " not found by thread local classloader");
		}
		if (rb == null) {
			classLoader = GetClassLoader
					.fromClass(PlatformResourceBundleLocator.class);
			rb = loadBundle(classLoader, locale, bundleName
					+ " not found by validator classloader");
		}

		return rb;
	}

	private ResourceBundle loadBundle(ClassLoader classLoader, Locale locale,
			String message) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle(bundleName, locale, classLoader,
					UTF8_CONTROL);
		} catch (MissingResourceException ignored) {
			logger.trace(message);
		}
		return rb;
	}

	private static class GetClassLoader implements
			PrivilegedAction<ClassLoader> {
		private final Class<?> clazz;

		private static ClassLoader fromContext() {
			final GetClassLoader action = new GetClassLoader(null);
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(action);
			} else {
				return action.run();
			}
		}

		private static ClassLoader fromClass(Class<?> clazz) {
			if (clazz == null) {
				throw new IllegalArgumentException("Class is null");
			}
			final GetClassLoader action = new GetClassLoader(clazz);
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(action);
			} else {
				return action.run();
			}
		}

		private GetClassLoader(Class<?> clazz) {
			this.clazz = clazz;
		}

		@Override
		public ClassLoader run() {
			if (clazz != null) {
				return clazz.getClassLoader();
			} else {
				return Thread.currentThread().getContextClassLoader();
			}
		}
	}

	protected static class UTF8Control extends Control {
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException,
				IOException {
			// The below code is copied from default Control#newBundle()
			// implementation.
			// Only the PropertyResourceBundle line is changed to read the file
			// as UTF-8.
			String bundleName = toBundleName(baseName, locale);
			String resourceName = toResourceName(bundleName, "properties");
			ResourceBundle bundle = null;
			InputStream stream = null;
			if (reload) {
				URL url = loader.getResource(resourceName);
				if (url != null) {
					URLConnection connection = url.openConnection();
					if (connection != null) {
						connection.setUseCaches(false);
						stream = connection.getInputStream();
					}
				}
			} else {
				stream = loader.getResourceAsStream(resourceName);
			}
			if (stream != null) {
				try {
					bundle = new PropertyResourceBundle(new InputStreamReader(
							stream, "UTF-8"));
				} finally {
					stream.close();
				}
			}
			return bundle;
		}
	}
}
