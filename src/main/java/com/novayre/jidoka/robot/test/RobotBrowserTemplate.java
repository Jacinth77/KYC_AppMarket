package com.novayre.jidoka.robot.test;

import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;

/**
 * Browser robot template. 
 */
@Robot
public class RobotBrowserTemplate implements IRobot {

	/**
	 * URL to navigate to.
	 */
	private static final String HOME_URL = "https://www.appian.com";
	
	/** The JidokaServer instance. */
	private IJidokaServer<?> server;
	
	/** The IClient module. */
	private IClient client;
	
	/** WebBrowser module */
	private IWebBrowserSupport browser;

	/** Browser type parameter **/
	private String browserType = null;

	/**
	 * Action "startUp".
	 * <p>
	 * This method is overrriden to initialize the Appian RPA modules instances.
	 */
	@Override
	public boolean startUp() throws Exception {
		
		server = (IJidokaServer< ? >) JidokaFactory.getServer();

		client = IClient.getInstance(this);
		
		browser = IWebBrowserSupport.getInstance(this, client);

		return IRobot.super.startUp();

	}
	
	/**
	 * Action "start".
	 */
	public void start() {
		server.setNumberOfItems(1);
	}


	/**
	 * Open Web Browser
	 * @throws Exception
	 */
	public void openBrowser() throws Exception  {

		browserType = server.getParameters().get("Browser");
		
		// Select browser type
		if (StringUtils.isBlank(browserType)) {
			server.info("Browser parameter not present. Using the default browser CHROME");
			browser.setBrowserType(EBrowsers.CHROME);
			browserType = EBrowsers.CHROME.name();
		} else {
			EBrowsers selectedBrowser = EBrowsers.valueOf(browserType);
			browserType = selectedBrowser.name();
			browser.setBrowserType(selectedBrowser);
			server.info("Browser selected: " + selectedBrowser.name());
		}
		
		// Set timeout to 60 seconds
		browser.setTimeoutSeconds(60);

		// Init the browser module
		browser.initBrowser();

		//This command is uses to make visible in the desktop the page (IExplore issue)
		if (EBrowsers.INTERNET_EXPLORER.name().equals(browserType)) {
			client.clickOnCenter();
			client.pause(3000);
		}

	}

	/**
	 * Navigate to Web Page
	 * 
	 * @throws Exception
	 */
	public void navigateToWeb() throws Exception  {
		
		server.setCurrentItem(1, HOME_URL);
		
		// Navegate to HOME_URL address
		browser.navigate(HOME_URL);

		// we save the screenshot, it can be viewed in robot execution trace page on the console
		server.sendScreen("Screen after load page: " + HOME_URL);
		
		server.setCurrentItemResultToOK("Success");
	}

	/**
	 * @see com.novayre.jidoka.client.api.IRobot#cleanUp()
	 */
	@Override
	public String[] cleanUp() throws Exception {
		
		browserCleanUp();
		return null;
	}

	/**
	 * Close the browser.
	 */
	private void browserCleanUp() {

		// If the browser was initialized, close it
		if (browser != null) {
			try {
				browser.close();
				browser = null;

			} catch (Exception e) { // NOPMD
			// Ignore exception
			}
		}

		try {
			
			if(browserType != null) {
				
				switch (EBrowsers.valueOf(browserType)) {

				case CHROME:
					client.killAllProcesses("chromedriver.exe", 1000);
					break;

				case INTERNET_EXPLORER:
					client.killAllProcesses("IEDriverServer.exe", 1000);
					break;

				case FIREFOX:
					client.killAllProcesses("geckodriver.exe", 1000);
					break;

				default:
					break;

				}
			}

		} catch (Exception e) { // NOPMD
		// Ignore exception
		}

	}

	/**
	 * Read Excel and Add to Queue operations
	 */

	public void ReadAddQueue() {

	}

	/**
	 * Method returns true if there are items in Queue
	 */

	public boolean HasMoreItems() {
		Boolean Flag = true;
		return Flag;

	}

	/**
	 * Method returns true if there are data present in sheets
	 */

	public boolean HasMoreSheets() {
		Boolean Flag = true;
		return Flag;

	}

	/**
	 * Method returns true if retry count is lesser than 3
	 */

	public boolean RetryRequired() {
		Boolean Flag = true;
		return Flag;

	}

	/**
	 * Method clear all items in the given queue
	 */

	public void  clearqueue() {


	}

	/**
	 * Method will call corresponding methods based on queue operation
	 */

	public void  QueueOperations() {


	}
	/**
	 * Method to click element
	 */

	private void Click() {

	}

	/**
	 * Method to read element
	 */

	private void read() {

	}

	/**
	 * Method to write element
	 */

	private void write() {

	}

	/**
	 * Method to navigate Tab
	 */

	private void NavigateTab() {

	}

	/**
	 * Method to wait for the element
	 */

	private void Wait() {

	}


	/**
	 * Method to send operations as keyboad strokes
	 */

	private void SendKeys() {

	}

	/**
	 * Method to select items
	 */

	private void Select() {

	}

	/**
	 * Method to CopyDatatoExcel
	 */

	private void CopyDatatoExcel() {

	}

	/**
	 * Method to UploadfilestoAppian
	 */

	private void UploadfilestoAppian() {

	}

	/**
	 * Method to UpdateAppianDB
	 */

	private void UpdateAppianDB() {

	}

	/**
	 * Last action of the robot.
	 */
	public void end()  {
		server.info("End process");
	}
	
}
