package com.novayre.jidoka.robot.test;

import com.novayre.jidoka.client.api.appian.IAppian;
import com.novayre.jidoka.client.api.appian.webapi.IWebApiRequest;
import com.novayre.jidoka.client.api.appian.webapi.IWebApiRequestBuilderFactory;
import com.novayre.jidoka.client.api.appian.webapi.IWebApiResponse;
import com.novayre.jidoka.client.api.IKeyboard;
import com.novayre.jidoka.client.api.exceptions.JidokaQueueException;
import com.novayre.jidoka.client.api.queue.*;
import com.novayre.jidoka.data.provider.api.EExcelType;
import com.novayre.jidoka.data.provider.api.IExcel;
import com.novayre.jidoka.client.lowcode.IRobotVariable;
import com.novayre.jidoka.data.provider.api.IJidokaDataProvider;
import com.novayre.jidoka.data.provider.api.IJidokaExcelDataProvider;
import com.novayre.jidoka.client.api.queue.IQueueManager;
import com.sun.java.swing.plaf.windows.resources.windows;
import org.apache.commons.io.FileUtils;


import com.novayre.jidoka.browser.api.EBrowsers;
import com.novayre.jidoka.browser.api.IWebBrowserSupport;
import com.novayre.jidoka.client.api.IJidokaServer;
import com.novayre.jidoka.client.api.IRobot;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.annotations.Robot;
import com.novayre.jidoka.client.api.multios.IClient;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.formula.functions.Count;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.Select;

import java.awt.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static sun.tools.java.Constants.OR;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Browser robot template. 
 */
@Robot
public class RobotBrowserTemplate implements IRobot
{

/**
 * URL to navigate to.
 */
public String HOME_URL = "";

/** The Queue Manager instance. */
private IQueueManager qmanager;

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
private String maxCountReached ="";
private IKeyboard keyboard;
/** Browser type parameter **/
private String browserType = null;
/** The IQueueManager instance. *
 /** The queue commons. */
private QueueCommons queueCommons;
public  Integer CurrentSheetCount = 1;
private String queueID;
/** The selected queue ID. */
private String selectedQueueID;
/** The current item queue. */
private IQueueItem currentItemQueue;
private IJidokaExcelDataProvider<ExcelDSRow> dataProvider;
private Robot robot;
/** The current queue. */
private IQueue currentQueue;
private static final String EXCEL_FILENAME = "FILE_NAME";
private String excelFile;
private ExcelDSRow exr;
private IExcel excel;
private Boolean exceptionflag = false;
private Integer RetryCount = 0;
private String Sheetname;
private String documentId = null;
private boolean IfFlag = true;
private boolean CancelFlag= false;
private String fieldname;
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

		keyboard=client.getKeyboard();

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
		excel = IExcel.getExcelInstance(this);
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

		navigateToWeb();

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



/**
 * Close the browser.
 */
private void browserCleanUp() {

		// If the browser was initialized, close it
		if (browser != null) {
		try {
		browser.getDriver().quit();
		browser = null;

		} catch (Exception e) { // NOPMD
		// Ignore exception
		}
		}

		try {

		if(browserType != null) {

		switch (EBrowsers.valueOf(browserType)) {

		case CHROME:
		client.killAllProcesses("chrome.exe", 1000);
		//Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");

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

private void browserkill() throws Exception{
		try {

		if(browserType != null) {

		switch (EBrowsers.valueOf(browserType)) {

		case CHROME:
		client.killAllProcesses("chrome.exe", 1000);
		//Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");

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


public void PerformOperation() throws Exception {


		String fileNameInput = server.getParameters().get("regionDatasource");
		server.info("FileName  :"+fileNameInput);
		Path inputFile = Paths.get(server.getCurrentDir(), fileNameInput);
		String fileType = FilenameUtils.getExtension(inputFile.toString());
		String sourceDir =inputFile.toString();
		excelFile = sourceDir;
		String fileInput = Paths.get(excelFile).toFile().toString();
		server.info("File :" +fileInput);

		Sheetname = "Datasource"+ CurrentSheetCount;
		dataProvider = IJidokaDataProvider.getInstance(this, IJidokaDataProvider.Provider.EXCEL);
		dataProvider.init(fileInput, Sheetname, FIRST_ROW, new ExcelRowMapper());
		try {


		// Get the next row, each row is a item
		while (dataProvider.nextRow()) {

		ExcelDSRow exr = dataProvider.getCurrentItem();
		server.info("Operations --"+exr.getActions());
		server.info("getField_Name --"+exr.getField_Name());
		server.info("CancelFlag --"+CancelFlag);
		server.info("IfFlag --"+IfFlag);
		fieldname=exr.getField_Name();


		if (exr.getActions().contains("endIf") ||  IfFlag == true  && CancelFlag ==false)
		{

		if (exr.getActions().contains("Click")) {
		Click(exr.getXpath().trim(), exr.getValue().trim());
		} else if (exr.getActions().contains("Switch tab")) {
		NavigateTab(exr.getValue().trim());
		} else if (exr.getActions().contains("SendKey")) {
		SendKeys(exr.getValue().trim());
		} else if (exr.getActions().contains("URL")) {
		HOME_URL = exr.getValue().trim();
		openBrowser();
		} else if (exr.getActions().contains("Read")) {
		read(exr.getXpath().trim(), exr.getValue().trim());
		} else if (exr.getActions().contains("Write")) {
		write(exr.getXpath().trim(), exr.getValue().trim());
		} else if (exr.getActions().contains("Select")) {
		Select(exr.getXpath().trim(), exr.getValue().trim());
		} else if (exr.getActions().contains("Wait")) {
		Waittime(Integer.parseInt(exr.getValue().trim()));
		} else if (exr.getActions().contains("CopyDatatoExcel")) {
		CopyDatatoExcel();
		} else if (exr.getActions().contains("SetFilePath")) {
		getFileLocation(exr.getValue().trim());
		} else if (exr.getActions().contains("IfLesser")) {
		iflesser(exr.getValue().trim());
		} else if (exr.getActions().contains("IfGreater")) {
		ifGreater(exr.getValue().trim());
		} else if (exr.getActions().contains("IfEqual")) {
		ifEqual(exr.getValue().trim());
		} else if (exr.getActions().contains("endIf")) {
		IfFlag = true;
		}
		else if (exr.getActions().contains("Cancel")) {
		CancelFlag = true;
		}
		else if (exr.getActions().contains("IfNotEqual")) {
		ifNotEqual(exr.getValue().trim());
		}
		else if(exr.getActions().contains("checkElement")){
			checkElement(exr.getXpath().trim());
		}
		}

		}

		} catch (Exception e) {

		exceptionflag = true;
		keyboard.altF(4);

		}

		finally {

		try
		{
		dataProvider.close();
		}
		catch (IOException e)
		{
		dataProvider.flush();
		}
		}
		}

public String HasMoreSheets() {

		int sheetCount =dataProvider.getExcel().getWorkbook().getNumberOfSheets();
		server.info("sheetCount" + sheetCount);
		if(CurrentSheetCount<sheetCount){
		CurrentSheetCount=CurrentSheetCount+1;

		RetryCount = 1;
		documentId = null;
		CancelFlag= false;
		maxCountReached ="";
		keyboard.altF(4);

		return "yes";

		}
		return "no";
		}

/**
 * Method for If conditions
 */

public void ifEqual(String condition) {

		String[] arrOfStr = condition.split(",");
		String Value1 = arrOfStr[0];
		String Value2 = arrOfStr[1];


		if (Value1.toLowerCase().trim().contains("customercountry")
		||  Value1.toLowerCase().trim().contains("customername")
		||  Value1.toLowerCase().trim().contains("customerpassport"))

		{
		Value1=server.getParameters().get(Value1).toString();
		}

		if (Value2.toLowerCase().trim().contains("customercountry")
		||  Value2.toLowerCase().trim().contains("customername")
		||  Value2.toLowerCase().trim().contains("customerpassport"))

		{
		Value2=server.getParameters().get(Value2).toString();
		}

		if (Value1.contains("XXRead"))
		{

			Value1 = dict.get(Value1);

		}

		if (Value2.contains("XXRead"))
		{
		Value2 = dict.get(Value2);
		}



		if (! Value1.trim().equals(Value2.trim()))
		{


		IfFlag = false;
		}

		}

/**
 * Method for If conditions
 */

public void ifNotEqual(String condition) {

		String[] arrOfStr = condition.split(",");
		String Value1 = arrOfStr[0];
		String Value2 = arrOfStr[1];

		if (Value1.toLowerCase().trim().contains("customercountry")
		||  Value1.toLowerCase().trim().contains("customername")
		||  Value1.toLowerCase().trim().contains("customerpassport"))

		{
		Value1=server.getParameters().get(Value1).toString();
		}

		if (Value2.toLowerCase().trim().contains("customercountry")
		||  Value2.toLowerCase().trim().contains("customername")
		||  Value2.toLowerCase().trim().contains("customerpassport"))

		{
		Value2=server.getParameters().get(Value2).toString();
		}

		if (Value1.contains("XXRead"))
		{
		Value1 = dict.get(Value1);
		}

		if (Value2.contains("XXRead"))
		{
		Value2 = dict.get(Value2);
		}




		if (Value1.trim().contains(Value2.trim()))
		{
		IfFlag = false;
		}

		}


public void ifGreater(String condition) {

		String[] arrOfStr = condition.split(",");
		String Value1 = arrOfStr[0];
		String Value2 = arrOfStr[1];

		if (Value1.toLowerCase().trim().contains("customercountry")
		||  Value1.toLowerCase().trim().contains("customername")
		||  Value1.toLowerCase().trim().contains("customerpassport"))

		{
		Value1=server.getParameters().get(Value1).toString();
		}

		if (Value2.toLowerCase().trim().contains("customercountry")
		||  Value2.toLowerCase().trim().contains("customername")
		||  Value2.toLowerCase().trim().contains("customerpassport"))

		{
		Value2=server.getParameters().get(Value2).toString();
		}

		if (Value1.contains("XXRead"))
		{
		Value1 = dict.get(Value1);
		}

		if (Value2.contains("XXRead"))
		{
		Value2 = dict.get(Value2);
		}

		if (Integer.parseInt(Value1) <= Integer.parseInt(Value2))
		{
		IfFlag = false;
		}

		}

public void iflesser(String condition) {

		String[] arrOfStr = condition.split(",");
		String Value1 = arrOfStr[0];
		String Value2 = arrOfStr[1];

		if (Value1.toLowerCase().trim().contains("customercountry")
		||  Value1.toLowerCase().trim().contains("customername")
		||  Value1.toLowerCase().trim().contains("customerpassport"))

		{
		Value1=server.getParameters().get(Value1).toString();
		}

		if (Value2.toLowerCase().trim().contains("customercountry")
		||  Value2.toLowerCase().trim().contains("customername")
		||  Value2.toLowerCase().trim().contains("customerpassport"))

		{
		Value2=server.getParameters().get(Value2).toString();
		}

		if (Value1.contains("XXRead"))
		{
		Value1 = dict.get(Value1);
		}

		if (Value2.contains("XXRead"))
		{
		Value2 = dict.get(Value2);
		}

		if (Integer.parseInt(Value1) >= Integer.parseInt(Value2))
		{
		IfFlag = false;
		}

		}

/**
 * Method returns true if retry count is lesser than 3
 */

public String RetryRequired() throws Exception {

		//browserkill();

		if (exceptionflag) {
			exceptionflag = false;

		if (RetryCount < 3)
		{
		RetryCount = RetryCount + 1;
		return "yes";

		}
		else
		{
		maxCountReached="MaxCountReached";
		return maxCountReached;
		}
		}
		else{
		return "No";
		}

		}

	/**
	 * Method to update Item Queue
	 */
public void updateItemQueue() throws JidokaQueueException, InterruptedException {

		Map<String, String> funcData = currentItemQueue.functionalData();

		String firstKey = funcData.keySet().iterator().next();

		try {

		funcData.put(firstKey, funcData.get(firstKey) + " - Completed");

		// release the item. The queue item result will be the same

		ReleaseItemWithOptionalParameters rip = new ReleaseItemWithOptionalParameters();
		rip.functionalData(funcData);

		// Is mandatory to set the current item result before releasing the queue item
		server.setCurrentItemResultToOK(currentItemQueue.key());

		qmanager.releaseItem(rip);

		} catch (JidokaQueueException e) {
		throw e;
		} catch (Exception e) {
		throw new JidokaQueueException(e);
		}
		}


/**
 * Method to click element
 */

private void Click(String Path,String Value) {

		if (Path.contains("XXINPUTXX"))
		{
		String ReplacePath = server.getParameters().get(Value).toString();
		Path.replace("XXINPUTXX",ReplacePath);
		}

		if (Path.contains("XXRead"))
		{
		String ReplaceValue = dict.get(Value);
		Path.replace("XXRead",ReplaceValue);
		}

		if (Value.toLowerCase().trim().contains("customercountry")
		||  Value.toLowerCase().trim().contains("customername")
		||  Value.toLowerCase().trim().contains("customerpassport"))

		{
		Value=server.getParameters().get(Value).toString();
		}

		if (Value.contains("XXRead"))
		{
		Value = dict.get(Value);
		}

		browser.waitElement(By.xpath(Path),10);
		browser.clickOnElement(By.xpath(Path));

		}

/**
 * Method to read element
 */

private void read(String Path,String Key) {

		browser.waitElement(By.xpath(Path),10);
		String RedValue=browser.getDriver().findElement(By.xpath(Path)).getText();
		dict.put(Key, RedValue);

		}
/**
 * Method to write element
 */

private void write(String Path,String Value) {

		if (Path.contains("XXINPUTXX"))
		{
		String ReplacePath = server.getParameters().get(Value).toString();
		Path.replace("XXINPUTXX",ReplacePath);
		}

		if (Path.contains("XXRead"))
		{
		String ReplaceValue = dict.get(Value);
		Path.replace("XXRead",ReplaceValue);
		}

		if  (Value.toLowerCase().trim().contains("customercountry")
		||  Value.toLowerCase().trim().contains("customername")
		||  Value.toLowerCase().trim().contains("customerpassport"))

		{

		Value=server.getParameters().get(Value).toString();

		}

		if (Value.contains("XXRead"))
		{
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

private void Waittime(Integer time) throws InterruptedException {

		TimeUnit.SECONDS.sleep(time);

		}

/**
 * Method to send operations as keyboad strokes
 */

private void SendKeys(String Key) throws InterruptedException {

		if (Key.toLowerCase().trim().contains("copy"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "c");
		}
		if (Key.toLowerCase().trim().contains("selectall"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "a");
		}
		if (Key.toLowerCase().trim().contains("paste"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL + "v");

		}
		if (Key.toLowerCase().trim().contains("pagedown"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.PAGE_DOWN);

		}
		if (Key.toLowerCase().trim().contains("pageup"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.PAGE_UP);

		}
		if (Key.toLowerCase().trim().contains("home"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.HOME);

		}
		if (Key.toLowerCase().trim().contains("end"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.END);

		}
		if (Key.toLowerCase().trim().contains("enter"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.ENTER);

		}
		if (Key.toLowerCase().trim().contains("backspace"))
		{
		browser.getDriver().findElement(By.cssSelector("body")).sendKeys(Keys.BACK_SPACE);

		}
		if (Key.toLowerCase().trim().contains("ctrl"))
		{
		String[] arrOfStr = Key.toLowerCase().trim().split("\\+");
		String cntrlkey = arrOfStr[1];
		client.typeText(client.getKeyboardSequence().pressControl().type(cntrlkey).releaseControl());

		}
		if (Key.toLowerCase().trim().contains("alt"))
		{
		String[] arrOfStr = Key.toLowerCase().trim().split("\\+");
		String altkey = arrOfStr[1];
		client.typeText(client.getKeyboardSequence().pressAlt().type(altkey).releaseAlt());

		}
		}

/**
 * Method to select items
 */

private void Select(String Id,String Value) {

		//Selectselect= new Select (driver.findElement(locator));
		//select.selectByVisibleText(value);

		if (Value.toLowerCase().trim().contains("customercountry")
		||  Value.toLowerCase().trim().contains("customername")
		||  Value.toLowerCase().trim().contains("customerpassport"))
		{
		Value=server.getParameters().get(Value).toString();
		}

		if (Value.contains("XXRead"))
		{
		Value = dict.get(Value);
		}
		Select dropdown = new Select(browser.getDriver().findElement(By.id(Id)));
		dropdown.selectByVisibleText(Value);
		}
/**
 * Method to CopyDatatoExcel
 */

private void CopyDatatoExcel() throws Exception {

		createexcel(Sheetname);
		TimeUnit.SECONDS.sleep(5);
		Desktop.getDesktop().open(Paths.get(server.getCurrentDir(), Sheetname+ ".xlsx").toFile());
		TimeUnit.SECONDS.sleep(5);
		client.typeText(client.getKeyboardSequence().pressControl().type("v").releaseControl());
		TimeUnit.SECONDS.sleep(5);
		client.typeText(client.getKeyboardSequence().pressControl().type("a").releaseControl());
		TimeUnit.SECONDS.sleep(5);
		client.typeText(client.getKeyboardSequence().pressAlt().type("h").releaseAlt());
		TimeUnit.SECONDS.sleep(3);
		client.typeText(client.getKeyboardSequence().type("o"));
		TimeUnit.SECONDS.sleep(2);
		client.typeText(client.getKeyboardSequence().type("i"));
		client.typeText(client.getKeyboardSequence().pressControl().type("s").releaseControl());
		TimeUnit.SECONDS.sleep(3);
		Runtime.getRuntime().exec("taskkill /F /IM EXCEL.exe");
	    documentId=uploadExcel((Paths.get(server.getCurrentDir(), Sheetname+ ".xlsx")).toFile());
	    String path = Paths.get(server.getCurrentDir(), Sheetname+ ".xlsx").toString();
	    File obj = new File(path);
	    obj.delete(); //delete created file
}
	/**
	 * Method to create excel
	 */

		private void createexcel(String documentName) throws Exception {
		String robotDir = server.getCurrentDir();

		//String name = "Documents available for " + service + ".xls";

		String name = documentName+".XLSX";

		File file = Paths.get(robotDir, name).toFile();
		String excelPath = file.getAbsolutePath();

		server.info("Excel Path  :"+excelPath);

		//String sheet = "Source";
		excel.create(excelPath, EExcelType.XLSX);
		Row row = excel.getSheet().createRow(0);
		/*row.createCell(0).setCellValue(service);
		CellStyle cellStyle = excel.getWorkbook().createCellStyle();
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
		cellStyle.setFillBackgroundColor(IndexedColors.BLUE.getIndex());*/
		}
		/**
		 * Method to UploadfilestoAppian
		 */
/**
 * Method to getFileLocation
 */
		private void getFileLocation(String path) throws Exception {
		File attachmentsDir = new File(path);
		server.debug("Looking for files in: " + attachmentsDir.getAbsolutePath());
		//File[] filesToUpload = Objects.<File[]>requireNonNull(attachmentsDir.listFiles());
		//String filename = attachmentsDir.getAbsolutePath() + "\\Documents available for DataSource Result .xls";
		String filename = attachmentsDir.getAbsolutePath();
		server.info("Filename :"+filename);
		File fileUpload = new File(filename);
		documentId = uploadExcel(fileUpload);
		File myObj = new File(path);
		myObj.delete(); //delete downloaded file

		}
		private String uploadExcel(File file) throws Exception{
		String endpointUpload = ((String)this.server.getEnvironmentVariables().get("ExcelUploadEndpoint")).toString();
		File uploadFile = file;
		IAppian appianClient =IAppian.getInstance(this);
		IWebApiRequest request = IWebApiRequestBuilderFactory.getFreshInstance().uploadDocument(endpointUpload,uploadFile,"caseid-"+server.getParameters().get("caseId").toString() +" "+Sheetname+".xls").build();
		server.info("Request : "+request);

		String response = appianClient.callWebApi(request).getBodyString();

		server.info("response : "+response);

		/*String value = response.split(":")[1];
		String output = value.split(" -")[0];*/
		this.server.info("output:" + response.trim());
		return  response.trim();
		}

		public void checkElement(String checkElementValue){
		boolean resultFound = browser.existsElement(By.xpath(checkElementValue));

		server.info("resultFound"+resultFound);


		if (resultFound) {
			IfFlag =true;
		}
		else{

			IfFlag =false;
		}

		}

/**
 * Method to Set Appian Data
 */

		public void setAppianData() throws Exception{
		String executionId = server.getExecution(0).getRobotName() + "#" + server.getExecution(0).getCurrentExecution().getExecutionNumber();
		Map<String, IRobotVariable> variables = server.getWorkflowVariables();
		IRobotVariable dID = variables.get("documentID");
		dID.setValue(documentId);
		IRobotVariable execId = variables.get("executionId");
		execId.setValue(executionId);
		IRobotVariable sourceType = variables.get("sourceType");
		sourceType.setValue(Sheetname);
		IRobotVariable caseId = variables.get("caseid");


		Integer caseidInt = Integer.parseInt(server.getParameters().get("caseId").toString());
		caseId.setValue(caseidInt);
		IRobotVariable status = variables.get("status");
		if(maxCountReached.contains("MaxCountReached")){
			status.setValue("Failed" + Sheetname);

			IRobotVariable additionalDetails = variables.get("additionalDetails");
			additionalDetails.setValue(fieldname);

		}
		else{
		status.setValue("Success");
		}
		}

/**
 * Last action of the robot.
 */
		public void end()  {

		browserCleanUp();
		server.info("End process");
		}

/**
 * CleanUP method
 * */
		public String[] cleanUp() throws Exception
		{
			return  new String[0];
		}}


