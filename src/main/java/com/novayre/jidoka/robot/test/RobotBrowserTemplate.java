package com.novayre.jidoka.robot.test;

import com.novayre.jidoka.client.api.queue.IQueueManager;
import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import static sun.tools.java.Constants.OR;

/**
 * Browser robot template. 
 */
@Robot
public class RobotBrowserTemplate implements IRobot {

	/**
	 * URL to navigate to.
	 */
	private static final String HOME_URL = "https://www.appian.com";

	/** The Queue Manager instance. */
	private IQueueManager qmanager;
	
	/** The JidokaServer instance. */
	private IJidokaServer<?> server;
	
	/** The IClient module. */
	private IClient client;
	
	/** WebBrowser module */
	private IWebBrowserSupport browser;

	/** Browser type parameter **/
	private String browserType = null;

	public  Dictionary<String, String> dict = new Hashtable<String, String>();


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

		qmanager = server.getQueueManager();



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

	private void Click(String Path,String Value) {

		if (Path.contains("XXINPUTXX")){
			String ReplaceValue = server.getParameters().get(Value).toString();
			Path.replace("XXINPUTXX",ReplaceValue);
		}

		browser.waitElement(By.xpath(Path),10);
		browser.clickOnElement(By.xpath(Path));

	}

	/**
	 * Method to read element
	 */

	private void read(String Path,String Key,Integer ValueKey) {

		//if (Path.contains("XXINPUTXX")){
		//	String Value = dict.get(ValueKey);
		//	Path.replace("XXINPUTXX",Value);
		//}

		browser.waitElement(By.xpath(Path),10);
		String RedValue=browser.getDriver().findElement(By.xpath(Path)).getText();
		dict.put(Key, RedValue);

	}

	/**
	 * Method to write element
	 */

	private void write(String Path,String Value,boolean dictionary) {

		if ((Value.toLowerCase()=="customercountry")
				||  (Value.toLowerCase()=="customer")
				||  (Value.toLowerCase()=="customerpassport"))

		{
			Value=server.getParameters().get(Value).toString();
		}

		if (dictionary){
			Value = dict.get(Value);
		}

		browser.waitElement(By.xpath(Path),10);
		browser.textFieldSet(By.xpath(Path),Value,true);
		//driver.findElement(By.xpath("//input[@name='FirstName']")).sendKeys("hi");

	}

	/**
	 * Method to navigate Tab
	 */

	private void NavigateTab(String Title) {

		ArrayList<String> tabs2 = new ArrayList<String>(browser.getDriver().getWindowHandles());
		for (int j = 1; j < tabs2.size(); j++) {
			browser.getDriver().switchTo().window(tabs2.get(j));
			String title = browser.getDriver().getTitle();

			if (title==Title){
				j=tabs2.size();
			}
		}
	}

	/**
	 * Method to wait for the element
	 */

	private void Wait(Integer time) throws InterruptedException {

		TimeUnit.SECONDS.sleep(time);

	}


	/**
	 * Method to send operations as keyboad strokes
	 */

	private void SendKeys(String Key) throws InterruptedException {

		//driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL +"\t");
		TimeUnit.SECONDS.sleep(10);
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Key);
		//browser.getDriver().findElement(By.xpath(Path)).sendKeys(Key);

	}

	/**
	 * Method to select items
	 */

	private void Select(String Id,String Value,Boolean dictionary) {

		if ((Value.toLowerCase()=="customercountry")
		||  (Value.toLowerCase()=="customer")
		||  (Value.toLowerCase()=="customerpassport"))

		{
			Value=server.getParameters().get(Value).toString();
		}

		if (dictionary){
			Value = dict.get(Value);
		}

		Select dropdown = new Select(browser.getDriver().findElement(By.id(Id)));
		dropdown.selectByVisibleText(Value);

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
