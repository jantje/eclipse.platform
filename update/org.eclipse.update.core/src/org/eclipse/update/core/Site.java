package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.model.*;
import org.eclipse.update.internal.core.*;

/**
 * Convenience implementation of a site.
 * <p>
 * This class may be instantiated or subclassed by clients.
 * </p> 
 * @see org.eclipse.update.core.ISite
 * @see org.eclipse.update.core.model.SiteModel
 * @since 2.0
 */
public class Site extends SiteModel implements ISite {

	/**
	 * Default installation path for features
	 * 
	 * @since 2.0
	 */
	public static final String DEFAULT_INSTALLED_FEATURE_PATH = "features/";
	//$NON-NLS-1$

	/**
	 * Default installation path for plug-ins and plug-in fragments
	 * 
	 * @since 2.0
	 */
	public static final String DEFAULT_PLUGIN_PATH = "plugins/"; //$NON-NLS-1$

	/**
	 * Default path on a site where packaged features are located
	 * 
	 * @since 2.0
	 */
	public static final String DEFAULT_FEATURE_PATH = "features/"; //$NON-NLS-1$

	/**
	 * Default site manifest file name
	 * 
	 * @since 2.0
	 */
	public static final String SITE_FILE = "site"; //$NON-NLS-1$

	/**
	 * Default site manifest extension
	 * 
	 * @since 2.0
	 */
	public static final String SITE_XML = SITE_FILE + ".xml"; //$NON-NLS-1$

	private ISiteContentProvider siteContentProvider;
	
	private Map featureCache = new HashMap(); // key=URLKey value=IFeature
	
	/**
	 * Constructor for Site
	 */
	public Site() {
		super();
	}

	/**
	 * Compares two sites for equality
	 * 
	 * @param object site object to compare with
	 * @return <code>true</code> if the two sites are equal, 
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ISite))
			return false;
		if (getURL() == null)
			return false;
		ISite otherSite = (ISite) obj;

		return UpdateManagerUtils.sameURL(getURL(), otherSite.getURL());
	}

	/**
	 * Returns the site URL
	 * 
	 * @see ISite#getURL()
	 * @since 2.0
	 */
	public URL getURL() {
		URL url = null;
		try {
			url = getSiteContentProvider().getURL();
		} catch (CoreException e) {
			UpdateManagerPlugin.warn(null, e);
		}
		return url;
	}

	/**
	 * Returns the site description.
	 * 
	 * @see ISite#getDescription()
	 * @since 2.0
	 */
	public IURLEntry getDescription() {
		return (IURLEntry) getDescriptionModel();
	}

	/**
	 * Returns an array of categories defined by the site.
	 * 
	 * @see ISite#getCategories()
	 * @since 2.0
	 */
	public ICategory[] getCategories() {
		CategoryModel[] result = getCategoryModels();
		if (result.length == 0)
			return new ICategory[0];
		else
			return (ICategory[]) result;
	}

	/**
	 * Returns the named site category.
	 * 
	 * @see ISite#getCategory(String)
	 * @since 2.0
	 */
	public ICategory getCategory(String key) {
		ICategory result = null;
		boolean found = false;
		int length = getCategoryModels().length;

		for (int i = 0; i < length; i++) {
			if (getCategoryModels()[i].getName().equals(key)) {
				result = (ICategory) getCategoryModels()[i];
				found = true;
				break;
			}
		}

		//DEBUG:
		if (!found) {
			String URLString = (this.getURL() != null) ? this.getURL().toExternalForm() : "<no site url>";
			UpdateManagerPlugin.warn(Policy.bind("Site.CannotFindCategory", key, URLString));
			//$NON-NLS-1$ //$NON-NLS-2$
			if (getCategoryModels().length <= 0)
				UpdateManagerPlugin.warn(Policy.bind("Site.NoCategories"));
			//$NON-NLS-1$
		}

		return result;
	}

	/**
	 * Returns an array of references to features on this site.
	 * 
	 * @see ISite#getFeatureReferences()
	 * @since 2.0
	 */
	public ISiteFeatureReference[] getRawFeatureReferences() {
		FeatureReferenceModel[] result = getFeatureReferenceModels();
		if (result.length == 0)
			return new ISiteFeatureReference[0];
		else
			return (ISiteFeatureReference[]) result;
	}

	/**
	 * @see org.eclipse.update.core.ISite#getRawIncludedFeatureReferences()
	 */
	public ISiteFeatureReference[] getFeatureReferences() {
		return filterFeatures(getRawFeatureReferences());
	}

	/*
	 * Method filterFeatures.
	 * Also implemented in Feature
	 *  
	 * @param list
	 * @return List
	 */
	private ISiteFeatureReference[] filterFeatures(ISiteFeatureReference[] allIncluded) {
		List list = new ArrayList();
		if (allIncluded!=null){
			for (int i = 0; i < allIncluded.length; i++) {
				ISiteFeatureReference included = allIncluded[i];
				if (UpdateManagerUtils.isValidEnvironment(included))
					list.add(included);
				else{
					if (UpdateManagerPlugin.DEBUG && UpdateManagerPlugin.DEBUG_SHOW_WARNINGS){
						UpdateManagerPlugin.warn("Filtered out feature reference:"+included);
					}
				}
			}
		}
		
		ISiteFeatureReference[] result = new ISiteFeatureReference[list.size()];
		if (!list.isEmpty()){
			list.toArray(result);
		}
		
		return result;	
	}

	/**
	 * Returns a reference to the specified feature on this site.
	 * 
	 * @see ISite#getFeatureReference(IFeature)
	 * @since 2.0
	 */
	public ISiteFeatureReference getFeatureReference(IFeature feature) {

		if (feature == null) {
			UpdateManagerPlugin.warn("Site:getFeatureReference: The feature is null");
			return null;
		}

		ISiteFeatureReference[] references = getFeatureReferences();
		ISiteFeatureReference currentReference = null;
		for (int i = 0; i < references.length; i++) {
			currentReference = references[i];
			if (UpdateManagerUtils.sameURL(feature.getURL(), currentReference.getURL()))
				return currentReference;
		}

		UpdateManagerPlugin.warn("Feature " + feature + " not found on site" + this.getURL());
		return null;
	}

	/**
	 * Returns an array of plug-in and non-plug-in archives located
	 * on this site
	 * 
	 * @see ISite#getArchives()
	 * @since 2.0
	 */
	public IArchiveReference[] getArchives() {
		ArchiveReferenceModel[] result = getArchiveReferenceModels();
		if (result.length == 0)
			return new IArchiveReference[0];
		else
			return (IArchiveReference[]) result;
	}

	/**
	 * Returns the content provider for this site.
	 * 
	 * @see ISite#getSiteContentProvider()
	 * @since 2.0
	 */
	public ISiteContentProvider getSiteContentProvider() throws CoreException {
		if (siteContentProvider == null) {
			throw Utilities.newCoreException(Policy.bind("Site.NoContentProvider"), null);
			//$NON-NLS-1$
		}
		return siteContentProvider;
	}

	/**
	 * Returns the default type for a packaged feature supported by this site
	 * 
	 * @see ISite#getDefaultInstallableFeatureType()
	 * @since 2.0
	 */
	public String getDefaultPackagedFeatureType() {
		return DEFAULT_PACKAGED_FEATURE_TYPE;
	}

	/**
	 * Returns an array of entries corresponding to plug-ins installed
	 * on this site.
	 * 
	 * @see IPluginContainer#getPluginEntries()
	 * @since 2.0
	 */
	public IPluginEntry[] getPluginEntries() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the number of plug-ins installed on this site
	 * 
	 * @see IPluginContainer#getPluginEntryCount()
	 * @since 2.0
	 */
	public int getPluginEntryCount() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an array of entries corresponding to plug-ins that are
	 * installed on this site and are referenced only by the specified
	 * feature. 
	 * 
	 * @see ISite#getPluginEntriesOnlyReferencedBy(IFeature)	 * 
	 * @since 2.0
	 */
	public IPluginEntry[] getPluginEntriesOnlyReferencedBy(IFeature feature) throws CoreException {

		IPluginEntry[] pluginsToRemove = new IPluginEntry[0];
		if (feature == null)
			return pluginsToRemove;

		// get the plugins from the feature
		IPluginEntry[] entries = feature.getPluginEntries();
		if (entries != null) {
			// get all the other plugins from all the other features
			Set allPluginID = new HashSet();
			ISiteFeatureReference[] features = getFeatureReferences();
			if (features != null) {
				for (int indexFeatures = 0; indexFeatures < features.length; indexFeatures++) {
					IFeature featureToCompare = null;
					try {
						featureToCompare = features[indexFeatures].getFeature();
					} catch (CoreException e) {
						UpdateManagerPlugin.warn(null, e);
					}
					if (!feature.equals(featureToCompare)) {
						IPluginEntry[] pluginEntries = features[indexFeatures].getFeature().getPluginEntries();
						if (pluginEntries != null) {
							for (int indexEntries = 0; indexEntries < pluginEntries.length; indexEntries++) {
								allPluginID.add(pluginEntries[indexEntries].getVersionedIdentifier());
							}
						}
					}
				}
			}

			// create the delta with the plugins that may be still used by other configured or unconfigured feature
			List plugins = new ArrayList();
			for (int indexPlugins = 0; indexPlugins < entries.length; indexPlugins++) {
				if (!allPluginID.contains(entries[indexPlugins].getVersionedIdentifier())) {
					plugins.add(entries[indexPlugins]);
				}
			}

			// move List into Array
			if (!plugins.isEmpty()) {
				pluginsToRemove = new IPluginEntry[plugins.size()];
				plugins.toArray(pluginsToRemove);
			}
		}

		return pluginsToRemove;
	}

	/**
	 * Adds a new plug-in entry to this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @see ISite#addPluginEntry(IPluginEntry)
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public void addPluginEntry(IPluginEntry pluginEntry) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get download size for the specified feature on this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @see ISite#getDownloadSizeFor(IFeature)
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public long getDownloadSizeFor(IFeature feature) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get install size for the specified feature on this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @see ISite#getInstallSizeFor(IFeature)
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public long getInstallSizeFor(IFeature feature) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Install the specified feature and all optional features on this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @see ISite#install(IFeature, IVerificationListener, IProgressMonitor)
	 * @exception InstallAbortedException when the user cancels the install
	 * @exception CoreException
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public IFeatureReference install(IFeature sourceFeature, IVerificationListener verificationListener, IProgressMonitor progress) throws InstallAbortedException, CoreException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Install the specified feature and listed optional features on this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @see ISite#install(IFeature, IVerificationListener, IProgressMonitor)
	 * @exception InstallAbortedException when the user cancels the install
	 * @exception CoreException
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public IFeatureReference install(IFeature sourceFeature, IFeatureReference[] optionalFeatures, IVerificationListener verificationListener, IProgressMonitor progress) throws InstallAbortedException, CoreException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Install the specified feature on this site using the content consumer as 
	 * a context to install the feature in.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the install action.
	 * 
	 * @param feature feature to install
	 * @param parentContentConsumer content consumer of the parent feature
	 * @param parentVerifier verifier of the parent feature
	 * @param verificationListener install verification listener
	 * @param monitor install monitor, can be <code>null</code>
	 * @exception InstallAbortedException when the user cancels the install
	 * @exception CoreException
	 * @exception java.jang.UnsupportedOperationException 
	 * @since 2.0 
	 */
	public IFeatureReference install(IFeature sourceFeature, IFeatureReference[] optionalFeatures, IFeatureContentConsumer parentContentConsumer, IVerifier parentVerifier, IVerificationListener verificationListener, IProgressMonitor progress)
		throws CoreException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Remove (uninstall) the specified feature from this site.
	 * This implementation always throws UnsupportedOperationException
	 * because this implementation does not support the remove action.
	 * 
	 * @see ISite#remove(IFeature, IProgressMonitor)
	 * @exception java.jang.UnsupportedOperationException
	 * @since 2.0
	 */
	public void remove(IFeature feature, IProgressMonitor progress) throws CoreException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the site content provider.
	 * 
	 * @see ISite#setSiteContentProvider(ISiteContentProvider)
	 * @since 2.0
	 */
	public void setSiteContentProvider(ISiteContentProvider siteContentProvider) {
		this.siteContentProvider = siteContentProvider;
	}
	/**
	 * @see org.eclipse.update.core.model.SiteModel#getConfiguredSite()
	 */
	public IConfiguredSite getCurrentConfiguredSite() {
		return (IConfiguredSite) getConfiguredSiteModel();
	}

	/**
	 * @see org.eclipse.update.core.ISite#createFeature(String, URL)
	 */
	public IFeature createFeature(String type, URL url) throws CoreException {
		return createFeature(type,url,null);
	}

	/**
	 * @see org.eclipse.update.core.ISite#createFeature(String, URL)
	 */
	public IFeature createFeature(String type, URL url, IProgressMonitor monitor) throws CoreException {

		// First check the cache
		URLKey key = new URLKey(url);
		IFeature feature = (IFeature) featureCache.get(key);
		if (feature != null) return feature;

		// Create a new one
		if (type == null || type.equals("")) { //$NON-NLS-1$
			// ask the Site for the default type
			type = getDefaultPackagedFeatureType();
		}

		IFeatureFactory factory = FeatureTypeFactory.getInstance().getFactory(type);
		feature = factory.createFeature(url, this);
		if (feature != null) {
			// Add the feature to the cache
			featureCache.put(key, feature);
		}
		return feature;
	}


}