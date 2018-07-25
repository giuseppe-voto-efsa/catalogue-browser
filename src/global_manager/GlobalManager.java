package global_manager;

import java.util.Observable;

import org.eclipse.swt.widgets.Display;

import catalogue.Catalogue;
import catalogue_browser_dao.CatalogueDAO;
import config.Config;
import config.Environment;
import dcf_manager.Dcf.DcfType;
import dcf_user.User;
import user_preferences.GlobalPreferenceDAO;

/**
 * Class used to store all the global variables of the
 * application and to access them in a safe way.
 * This class is a singleton!
 * @author avonva
 *
 */
public class GlobalManager extends Observable {

	private static GlobalManager manager;
	
	// the currently opened catalogue
	private Catalogue currentCatalogue;
	
	//block instantiation
	protected GlobalManager() {}
	
	/**
	 * Get an instance of the global manager
	 * @return
	 */
	public static GlobalManager getInstance() {
		
		// if no instance is already created
		// create it
		if ( manager == null )
			manager = new GlobalManager();
		
		return manager;
	}
	
	public static Catalogue getLastVersion(String catalogueCode) {
		Config config = new Config();
		Environment env = config.getEnvironment();
		CatalogueDAO dao = new CatalogueDAO();
		return dao.getLastVersionByCode(catalogueCode, DcfType.fromEnvironment(env));
	}
	
	/**
	 * Set the current catalogue
	 * @param currentCatalogue
	 */
	public void setCurrentCatalogue( final Catalogue currentCatalogue ) {
		
		this.currentCatalogue = currentCatalogue;
		
		refresh();
		
		// do not save the cat users catalogue, otherwise
		// we can open and see it also if we have not
		// the permissions
		if ( currentCatalogue != null && currentCatalogue.isCatUsersCatalogue() )
			return;

		// save main panel state
		GlobalPreferenceDAO prefDao = new GlobalPreferenceDAO();
		prefDao.saveOpenedCatalogue( currentCatalogue );
	}
	
	/**
	 * Refresh the UI
	 */
	public void refresh() {
		
		// guarantee that we are running in the 
		// display thread
		Display.getDefault().syncExec( new Runnable() {
			
			@Override
			public void run() {
				setChanged();
				notifyObservers( currentCatalogue );
			}
		});
	}
	
	/**
	 * Get the catalogue which is currently opened
	 * in the application
	 * @return
	 */
	public Catalogue getCurrentCatalogue() {
		return currentCatalogue;
	}
	
	/**
	 * Check if the application is in read
	 * only mode
	 * @return
	 */
	public boolean isReadOnly() {
		
		User user = User.getInstance();
		
		return currentCatalogue != null && !user.canEdit( currentCatalogue );
	}
}
