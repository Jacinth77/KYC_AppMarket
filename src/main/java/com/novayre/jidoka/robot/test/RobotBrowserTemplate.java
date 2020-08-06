package com.novayre.jidoka.robot.test;

import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.*;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider;
import com.novayre.jidoka.data.provider.api.IJidokaExcelDataProvider;
import org.apache.commons.lang.StringUtils;

import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
	private static final int FIRST_ROW = 0;
	/** The IClient module. */
	private IClient client;
	private ExcelDSRow excelDSRow;
	/** WebBrowser module */
	private IWebBrowserSupport browser;
	/** The current item index. */
	private int currentItemIndex;
	/** Browser type parameter **/
	private String browserType = null;
	/** The IQueueManager instance. */
	private IQueueManager qmanager;
	/** The queue commons. */
	private QueueCommons queueCommons;
	private String queueID;
	/** The selected queue ID. */
	private String selectedQueueID;
	/** The current item queue. */
	private IQueueItem currentItemQueue;
	private IJidokaExcelDataProvider<ExcelDSRow> dataProvider;
	/** The current queue. */
	private IQueue currentQueue;
	private static final String EXCEL_FILENAME = "FILE_NAME";
	private String excelFile;

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
		qmanager = server.getQueueManager();
		queueCommons = new QueueCommons();
		excelDSRow = new ExcelDSRow();
		queueCommons.init(qmanager);
		dataProvider = IJidokaDataProvider.getInstance(this, IJidokaDataProvider.Provider.EXCEL);
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

	public void ReadAddQueue() throws Exception {
		String fileNameInput = server.getParameters().get("regionDatasource");
		Path inputFile = Paths.get(server.getCurrentDir(), fileNameInput);
		String fileType = FilenameUtils.getExtension(inputFile.toString());
		String sourceDir =inputFile.toString();
//		File sourceFile = new File(sourceDir);
//		server.info("sourceFile"+ sourceFile);

		if (StringUtils.isBlank(qmanager.preselectedQueue())) {
			excelFile = sourceDir;
			server.info("Keyvaue: " + excelFile );
			selectedQueueID = queueCommons.createQueue(excelFile);
			server.info("Queue ID: " + selectedQueueID);
			addItemsToQueue();

		} else {

			selectedQueueID = qmanager.preselectedQueue();
			server.info("Selected queue ID: " + selectedQueueID);

		}
		currentQueue = queueCommons.getQueueFromId(selectedQueueID);
		server.info("queue name: " + currentQueue);

		if (currentQueue == null) {
			server.debug("Queue not found");
			return;
		}

		server.setNumberOfItems(currentQueue.pendingItems());
	}

	private void addItemsToQueue() throws Exception {
		server.info("add items ");
		String fileInput = Paths.get(excelFile).toFile().toString();
		dataProvider = IJidokaDataProvider.getInstance(this, IJidokaDataProvider.Provider.EXCEL);
		dataProvider.init(fileInput, null, FIRST_ROW, new ExcelRowMapper());
		try {


			// Get the next row, each row is a item
			while (dataProvider.nextRow()) {
				server.info("Inside while");

				ExcelDSRow er = dataProvider.getCurrentItem();


				CreateItemParameters itemParameters = new CreateItemParameters();

				// Set the item parameters
				itemParameters.setKey(er.getField_Name());
				server.info("Key " + er.getField_Name());
				itemParameters.setPriority(EPriority.NORMAL);
				itemParameters.setQueueId(selectedQueueID);
				itemParameters.setReference(String.valueOf(dataProvider.getCurrentItemNumber()));

				Map<String, String> functionalData = new HashMap<>();
				functionalData.put(ExcelRowMapper.Field_Name,er.getField_Name());
				functionalData.put(ExcelRowMapper.Xpath,er.getXpath());
				functionalData.put(ExcelRowMapper.Value,er.getValue());
				functionalData.put(ExcelRowMapper.Actions,er.getActions());
				itemParameters.setFunctionalData(functionalData);
				qmanager.createItem(itemParameters);
				server.debug(String.format("Added item to queue %s with id %s", itemParameters.getQueueId(), itemParameters.getKey()));
			}

		} catch (Exception e) {
			throw new JidokaQueueException(e);
		} finally {

			try {
				// Close the excel file
				dataProvider.close();
			} catch (IOException e) {
				throw new JidokaQueueException(e);
			}
		}
	}

	/**
	 * Method returns true if there are items in Queue
	 */

	public String HasMoreItems() throws Exception {
		currentItemQueue = queueCommons.getNextItem(currentQueue);

		if (currentItemQueue != null) {

			// set the stats for the current item
			server.setCurrentItem(currentItemIndex++, currentItemQueue.key());
			ExcelDSRow exr = new ExcelDSRow();
			//server.info("first name" + currentItemQueue.functionalData().get(TestPOC.First_Namne));
			exr.setField_Name(currentItemQueue.functionalData().get(ExcelRowMapper.Field_Name));
			exr.setXpath(currentItemQueue.functionalData().get(ExcelRowMapper.Xpath));
			exr.setValue(currentItemQueue.functionalData().get(ExcelRowMapper.Value));
			exr.setActions(currentItemQueue.functionalData().get(ExcelRowMapper.Actions));
			return "Yes";
		}

		return "No";
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
