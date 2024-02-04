package com.office.frontend;

import backend.frontmatterapi.controllers.FrontMatterComparisonController;
import backend.frontmatterapi.dtos.RevisedTitleChangeList;
import backend.frontmatterapi.models.*;
import backend.frontmatterapi.services.AutomationService;
import backend.frontmatterapi.services.ExcelService;
import backend.frontmatterapi.services.UtilityFunctions;
import backend.taggenerator.SupsdInfoChanger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.*;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashController implements Initializable {
    private final ExcelService excelService = new ExcelService();
    private final FrontMatterComparisonController frontMatterComparisonController = new FrontMatterComparisonController();
    private Stage pleaseWaitStage; //Newly Added

    @FXML
    public TextField textWorkingPath;
    @FXML
    public Button btnWorkingPath;
    @FXML
    public DatePicker revdatePicker;
    @FXML
    public Button nextButton;
    @FXML
    public Button oldRevisionBtn;
    @FXML
    public Button newRevisionButton;
    @FXML
    public TextField oldRevTextView;
    @FXML
    public Button oldRevBtn;
    @FXML
    public TextField newRevTextView;
    @FXML
    public Button newRevBtn;
    @FXML
    public Button getResultBtn;
    @FXML
    public TextField txtFileTextField;
    @FXML
    public Button generateTagBtn;
    @FXML
    private Pane pane1;
    @FXML
    private Pane pane2;
    @FXML
    private Pane pane3;

    @FXML
    private TextField oldRevisionFileTextField;
    @FXML
    private TextField newRevisionFileTextField;
    private File oldFile;
    private File newFile;
    private File txtFile;
    private BooleanProperty arePDFFilesSelected = new SimpleBooleanProperty(false);
    private BooleanProperty arePDFFilesSelectedForPane2 = new SimpleBooleanProperty(false);
    private BooleanProperty isWorkingPathSelected = new SimpleBooleanProperty(false);
    private BooleanProperty isDatePicked = new SimpleBooleanProperty(false);
    private BooleanProperty isTxtFileSelected = new SimpleBooleanProperty(false);

    public static DownloadResults downloadResults;
    public String workingFolderPath;

    @FXML
    private void selectOldRevisionFile(ActionEvent event) {
        handleFileSelection(oldRevisionFileTextField, (Button) event.getSource());
    }


    @FXML
    private void selectNewRevisionFile(ActionEvent event) {
        handleFileSelection(newRevisionFileTextField, (Button) event.getSource());
    }

    @FXML
    private void selectWorkingPath(ActionEvent event) {
        handleFolderSelection(textWorkingPath, (Button) event.getSource());
    }

    private void bindShowWorkingFolderPathButton() {
        btnWorkingPath.disableProperty().bind(arePDFFilesSelected.not());
    }

    private void bindShowDatePicker() {
        revdatePicker.disableProperty().bind(isWorkingPathSelected.not());
    }

    private void bindShowNextButton() {

        nextButton.disableProperty().bind(isDatePicked.not());
    }

    private void bindShowGetResultBtn() {

        getResultBtn.disableProperty().bind(arePDFFilesSelectedForPane2.not());
    }

    private void bindShowGenerateTagBtn() {

        generateTagBtn.disableProperty().bind(isTxtFileSelected.not());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enableAndShowPane(pane1);
        bindShowWorkingFolderPathButton();
        bindShowDatePicker();
        bindShowNextButton();
        bindShowGetResultBtn();
        bindShowGenerateTagBtn();
    }

    private void updatePDFFilesSelectedStatus() {
        boolean pdfFilesSelected = (isTextFieldNotEmpty(oldRevisionFileTextField)
                && (isTextFieldNotEmpty(newRevisionFileTextField)));

        arePDFFilesSelected.set(pdfFilesSelected);

    }

    private void updatePDFFilesSelectedStatusForPane2() {
        boolean pdfFilesSelected = (isTextFieldNotEmpty(oldRevTextView)
                && (isTextFieldNotEmpty(newRevTextView)));

        arePDFFilesSelectedForPane2.set(pdfFilesSelected);

    }

    private void updateDatePickerStatus() {
        isDatePicked.set(true);
    }

    private void updateWorkingFolderPathSelectStatus() {
        boolean workingPathSelected = (isTextFieldNotEmpty(textWorkingPath));

        isWorkingPathSelected.set(workingPathSelected);
    }

    public void handleTextFileSelection(TextField textField, Button clickedButton) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select *.txt File");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".txt")) {
                showTXTAlert();
            } else {
                textField.setText(selectedFile.getName());
                txtFile = selectedFile;
                updateTxtFileSelectedStatus();
            }
        }

    }

    private void updateTxtFileSelectedStatus() {
        isTxtFileSelected.set(isTextFieldNotEmpty(txtFileTextField));
    }

    private void handleFileSelection(TextField textField, Button clickedButton) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File");
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            if (!selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                showPDFAlert();
            } else {
                textField.setText(selectedFile.getName());
                if (clickedButton.getId().equals("oldRevisionBtn") || clickedButton.getId().equals("oldRevBtn"))
                    oldFile = selectedFile;
                else if (clickedButton.getId().equals("newRevisionButton") || clickedButton.getId().equals("newRevBtn"))
                    newFile = selectedFile;
                updatePDFFilesSelectedStatus();
                updatePDFFilesSelectedStatusForPane2();
            }
        }
    }

    @FXML
    private void handleDatePickerAction(ActionEvent event) {
        LocalDate selectedDate = revdatePicker.getValue();

        if (selectedDate != null) {
            updateDatePickerStatus();

        } else {
            // Handle the case where no date is selected
            System.out.println("No date selected");
        }
    }

    private void handleFolderSelection(TextField textField, Button clickedButton) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select The Working Folder");
        File selectedFolder = directoryChooser.showDialog(null);
        if (selectedFolder != null) {
            textWorkingPath.setText(selectedFolder.getAbsolutePath());
            workingFolderPath = selectedFolder.getAbsolutePath();
            System.out.println("The selected WF Path is: " + workingFolderPath);
            updateWorkingFolderPathSelectStatus();
        }

    }

    private boolean isTextFieldNotEmpty(TextField textField) {
        return !textField.getText().trim().isEmpty();
    }

    private void showPDFAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid File Type");
        alert.setHeaderText(null);
        alert.setContentText("Please select a PDF file.");
        alert.showAndWait();
    }

    private void showTXTAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid File Type");
        alert.setHeaderText(null);
        alert.setContentText("Please select a *.txt file.");
        alert.showAndWait();
    }

    private void showPDFAlertToSelectPDFFile() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Please Select Required PDF File");
        alert.setHeaderText(null);
        alert.setContentText("Please select a PDF file.");
        alert.showAndWait();
    }

    @FXML
    private void enableAndShowPane1() {
        enableAndShowPane(pane1);
    }

    @FXML
    private void enableAndShowPane2() {
        enableAndShowPane(pane2);
    }

    @FXML
    private void enableAndShowPane3() {
        enableAndShowPane(pane3);
    }

    private void enableAndShowPane(Pane pane) {
        pane1.setDisable(true);
        pane1.setVisible(false);
        pane2.setDisable(true);
        pane2.setVisible(false);
        pane3.setDisable(true);
        pane3.setVisible(false);
        pane.setDisable(false);
        pane.setVisible(true);
    }

    private void showPDFAlertSameFilesSelected() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Please Select Old and New Revision PDFs");
        alert.setHeaderText(null);
        alert.setContentText("U have selected same files for Old and new revisions");
        alert.showAndWait();
    }

    private void openNewWindowIfNeeded(List<String> items, String title) {
        if (!items.isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(DashController.class.getResource("jfx-list-view.fxml"));
                Stage newWindowStage = new Stage();
                //newWindowStage.setFullScreen(true);
                newWindowStage.setMaximized(true);
                newWindowStage.setTitle(title);
                newWindowStage.initModality(Modality.APPLICATION_MODAL);

                Scene scene = new Scene(loader.load());
                newWindowStage.setScene(scene);

                ListViewController newWindowController = loader.getController();
                newWindowController.setItems(items);

                newWindowStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void selectNextBtn() {
        showPleaseWaitDialog();
        String revDate = getDateFromDatePicker(revdatePicker.getValue());
        String wfPath = textWorkingPath.getText();
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            showPDFAlertSameFilesSelected();
            closePleaseWaitDialog();
            makeTheAutomationViewToInitialState();
            return;
        }
        String folderInDesktop = null;
        boolean isExcelAvailable = false;
        boolean isFirstTime = showIsThisTheFirstTimeGeneratingRevisionbar();
        //The below code will only run if it's not first time
        if (!isFirstTime) {
            folderInDesktop = System.getProperty("user.home") + "/Desktop" + "/FrontMatter-DO NOT DELETE";
            String filesDeleted = onlySpecifiedFileIsAvailableInGivenFolder(folderInDesktop);
            if (filesDeleted == null){
                closePleaseWaitDialog();
                return;
            }
            isExcelAvailable = true;
        }
        HashMap<String, String> pgnumDateDeletedMap = null;
        HashMap<String, String> fileNamesMap = null;
        //The below piece of code will run if not first time and output.xlsx file is available
        if (isExcelAvailable) {
            HashMap<String, RevisedTitleChangeList> pgblkWiseReviseChangesMap = excelService.extractOutputExcelContents(folderInDesktop);
            if (pgblkWiseReviseChangesMap.isEmpty()) {
                closePleaseWaitDialog();
                showErrorWhenTryingToReadOutputExcelFile();
                makeTheAutomationViewToInitialState();
                return;
            }
            UtilityFunctions utilityFunctions = new UtilityFunctions();
            fileNamesMap = utilityFunctions.getFilename(wfPath, pgblkWiseReviseChangesMap.keySet());
            if(fileNamesMap == null){
                closePleaseWaitDialog();
                makeTheAutomationViewToInitialState();
                return;
            }
            AutomationService automationService = new AutomationService();
            Optional<HashMap<String, String>> deletedInput = automationService.deletePreviouslyAddedPgnumDateFromSGMLFile(pgblkWiseReviseChangesMap, wfPath, fileNamesMap);
            if (deletedInput.isEmpty()) {
                closePleaseWaitDialog();
                makeTheAutomationViewToInitialState();
                return;
            }
            pgnumDateDeletedMap = deletedInput.get();

        }
        Optional<HashMap<String, ArrayList<FmChangeItem>>> fmChangeItemsMap = frontMatterComparisonController.processPDFAutomation(oldFile, newFile);
        if (fmChangeItemsMap.isEmpty()) {
            closePleaseWaitDialog();
            showFmChangeItemMapEmpty();
            makeTheAutomationViewToInitialState();
            return;
        }
        if (fmChangeItemsMap.get().containsKey("empty") && isFirstTime) {
            closePleaseWaitDialog();
            showNoChangesRequired();
            makeTheAutomationViewToInitialState();
            return;
        }
        AutomationService automationService = new AutomationService();
        if(isFirstTime){
            folderInDesktop = createTheRequiredFolderInDesktop("FrontMatter-DO NOT DELETE");
            if(folderInDesktop == null){
                closePleaseWaitDialog();
                //makeTheAutomationViewToInitialState();
                return;
            }
        }
        String excelFilePath = System.getProperty("user.home") + "/Desktop" + "/FrontMatter-DO NOT DELETE" + "/output.xlsx";
        Workbook workbook = excelService.createExcelWorkbook();
        Optional<HashMap<String, OutputStringDto>> outPutString = automationService.addPageNumChangeDateToRequiredTitles(fmChangeItemsMap.get(), wfPath, revDate, pgnumDateDeletedMap, workbook);
        if (outPutString.isEmpty()) {
            closePleaseWaitDialog();
            showUnSuccessfull();
            makeTheAutomationViewToInitialState();
            return;
        }
        if (pgnumDateDeletedMap != null && !pgnumDateDeletedMap.isEmpty()) {
            for (String s : pgnumDateDeletedMap.keySet()) {
                outPutString.get().put(s, new OutputStringDto(pgnumDateDeletedMap.get(s), fileNamesMap.get(s)));
            }
        }
        SaveFilesResult saveFilesResult = automationService.saveAllTheFilesIntoSGMLFile(System.getProperty("user.home") + "/Desktop" + "/FrontMatter-DO NOT DELETE", outPutString.get());
        if (outPutString.get().size() == saveFilesResult.getCompletedList().size()) {
            try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
                workbook.write(fileOut);
                closePleaseWaitDialog();
                showSuccessfull();
                makeTheAutomationViewToInitialState();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                closePleaseWaitDialog();
                showSuccessfullButExcelNotSaved();
                makeTheAutomationViewToInitialState();
                return;
            }
        }
        if (outPutString.get().size() == saveFilesResult.getIncompletedList().size()) {
            closePleaseWaitDialog();
            showUnSuccessfull();
            makeTheAutomationViewToInitialState();
            return;
        }
        showPartiallySaveSuccessfull(saveFilesResult.getCompletedList(), saveFilesResult.getIncompletedList());
        closePleaseWaitDialog();
        makeTheAutomationViewToInitialState();
    }
    private void showPartiallySaveSuccessfull(ArrayList<String> completedList, ArrayList<String> incompltedList) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Process Partially Successful");
        alert.setHeaderText(null);
        StringBuilder comp = new StringBuilder();
        StringBuilder incomp = new StringBuilder();
        for (String s : completedList) {
            comp.append(s);
            if (completedList.iterator().hasNext()) {
                comp.append(", ");
            }
        }
        for (String s : incompltedList) {
            comp.append(s);
            if (incompltedList.iterator().hasNext()) {
                comp.append(", ");
            }
        }
        alert.setContentText("The process was partially Successfull\n " +
                "Revbar successfully added in : " + comp.toString() + "\n" +
                "Couldn't add Revbar in : " + incomp.toString() + ".");
        alert.showAndWait();
    }

    public void oldRevFileSelect(ActionEvent event) {
        handleFileSelection(oldRevTextView, (Button) event.getSource());
    }

    public void newRevFileSelect(ActionEvent event) {
        handleFileSelection(newRevTextView, (Button) event.getSource());
    }

    public void getResult(ActionEvent actionEvent) {
        showPleaseWaitDialog();
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            showPDFAlertSameFilesSelected();
            makeTheDisplayViewToInitialState();
            return;
        }
        Optional<Workbook> outputExcel = frontMatterComparisonController.processPDFDisplayInExcel(oldFile, newFile);
        if(outputExcel.isEmpty()){
            showNotAbleToFindChangesForDisplay();
            makeTheDisplayViewToInitialState();
            return;
        }
        String excelFilePath = System.getProperty("user.home") + "/Desktop" + "/FM_RevisionChanges.xlsx";
        boolean isAvailable = isFileAvailable(System.getProperty("user.home") + "/Desktop", "FM_RevisionChanges.xlsx");
        if(!isAvailable){
            closePleaseWaitDialog();
            showFM_RevisionChangesExcelIsOpened();
            makeTheAutomationViewToInitialState();
            return;
        }
        try (FileOutputStream fileOut = new FileOutputStream(excelFilePath)) {
            outputExcel.get().write(fileOut);
            closePleaseWaitDialog();
            makeTheDisplayViewToInitialState();
            showSuccessfullDisplayChanges("FM_RevisionChanges.xlsx");
           return;
        } catch (IOException e) {
            e.printStackTrace();
            closePleaseWaitDialog();
            showUnSuccessfullDisplayChanges("FM_RevisionChanges.xlsx");
            makeTheDisplayViewToInitialState();
            return;
        }


    }

    public void selectTxtFile(ActionEvent event) {
        handleTextFileSelection(txtFileTextField, (Button) event.getSource());
    }

    public void generateTags(ActionEvent event) {
        try {
            // Read the contents of the file into a byte array
            byte[] fileBytes = Files.readAllBytes(Paths.get(txtFile.getAbsolutePath()));
            // Convert the byte array to a String using the default charset (UTF-8)
            String fileContent = new String(fileBytes);
            String output = SupsdInfoChanger.addSuperSedeInfoToIPLFigure(fileContent);
            if (output.equals("Not a valid file")) {
                showSelectRequiredIplTextFileAlert();
                txtFile = null;
                txtFileTextField.clear();
                updateTxtFileSelectedStatus();
                return;
            }
            String fileName = txtFile.getName();
            int n = fileName.length();
            String targetPath = txtFile.getAbsolutePath() + fileName.substring(0, n - 4) + "_output.txt";
            try {
                Path path = Paths.get(targetPath);
                Files.write(path, output.getBytes());
                showTagGeneratorSuccessAlert(path.toAbsolutePath().toString());
                txtFile = null;
                txtFileTextField.clear();
                updateTxtFileSelectedStatus();
            } catch (Exception e) {
                showFileWriteErrorAlert();
                txtFile = null;
                txtFileTextField.clear();
                updateTxtFileSelectedStatus();
            }

        } catch (IOException e) {
            showFileReadErrorAlert();
            txtFile = null;
            txtFileTextField.clear();
            updateTxtFileSelectedStatus();
        }

    }

    private void showFileReadErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Read The .txt File");
        alert.setHeaderText(null);
        alert.setContentText("There was some issue while trying to read the .txt File, Please try again");
        alert.showAndWait();
    }

    private void showFileWriteErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Create Output .txt File");
        alert.setHeaderText(null);
        alert.setContentText("There was some issue while trying to create the output .txt File, Please try again");
        alert.showAndWait();
    }

    private void showTagGeneratorSuccessAlert(String absolutePath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Output File Saved Successfully");
        alert.setContentText("The output file is placed in path: " + absolutePath);
        alert.showAndWait();
    }

    private void showSelectRequiredIplTextFileAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("The Selected Text File Doesn't Meet IPL Standards");
        alert.setHeaderText(null);
        alert.setContentText("The file you have selected is not a txt file for IPL creation. Please select the correct file and try again");
        alert.showAndWait();
    }

    private String getDateFromDatePicker(LocalDate localDate) {
        // Define the desired date pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // Format the selected date using the specified pattern
        return localDate.format(formatter);
    }

    private void showFmChangeItemMapEmpty() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error! Please Try Again");
        alert.setHeaderText(null);
        alert.setContentText("There was an error occurred while trying to get the revision changes, Please try again.");
        alert.showAndWait();
    }

    private void showUnSuccessfull() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Process Unsuccessfull");
        alert.setHeaderText(null);
        alert.setContentText("There was an error occurred while trying to add revision bars, Please try again.");
        alert.showAndWait();
    }

    private void showSuccessfull() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Process Completed");
        alert.setHeaderText(null);
        alert.setContentText("Revision bar added Successfully");
        alert.showAndWait();
    }
    private void showSuccessfullDisplayChanges(String fileName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Process Completed");
        alert.setHeaderText(null);
        alert.setContentText("Revision Changes are identified." + "\n"+ "Result excel is saved in Desktop with File Name: " + fileName);
        alert.showAndWait();
    }
    private void showUnSuccessfullDisplayChanges(String fileName) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Save Result Excel File");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to save result Excel File" + "\n"+
                "Please make sure you haven't open an excel file with name: " + fileName + " available in your Desktop" + "\n" +
                "If the file is open please close the file and try again");
        alert.showAndWait();
    }

    private void showPleaseWaitDialog() {
        pleaseWaitStage = new Stage();
        pleaseWaitStage.initModality(Modality.APPLICATION_MODAL);
        pleaseWaitStage.initStyle(StageStyle.UNDECORATED);

        Label label = new Label("Please wait...");
        VBox vBox = new VBox(label);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox, 200, 100);
        pleaseWaitStage.setScene(scene);

        // Show the "Please Wait" dialog
        pleaseWaitStage.show();
    }

    private void closePleaseWaitDialog() {
        if (pleaseWaitStage != null) {
            // Close the "Please Wait" dialog
            pleaseWaitStage.close();
        }
    }

    private boolean showIsThisTheFirstTimeGeneratingRevisionbar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please Confirm");
        alert.setHeaderText("Did you previously added revision bar in this book using this tool?");
        alert.setContentText("Please Select The Correct Option:");

        // Customize the buttons in the alert
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No");
        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        // Show the alert and wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();

        // Process the user's choice
        if (result.isPresent() && result.get() == buttonTypeYes) {
            // User clicked "Yes" - perform the corresponding action
            return false;
        } else {
            // User clicked "No" or closed the dialog - perform the corresponding action
            return true;
        }
    }
    private void showDoNotUseThisToolAsOutputFileNotAvailable(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("output.xlsx File Not Available in "+s);
        alert.setHeaderText(null);
        alert.setContentText("Since output.xlsx file is not available we can't add revision bar automatically. Please try to add manually.");
        alert.showAndWait();
    }

    private void showSuccessfullButExcelNotSaved() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Process Completed");
        alert.setHeaderText(null);
        alert.setContentText("Revision bar added Successfully, but Couldn't create output.xlsx file" + "\n");
        alert.showAndWait();
    }

    private void showNoChangesRequired() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("No Changes Required");
        alert.setHeaderText(null);
        alert.setContentText("All page numbers are same as in the previous revision. So no changes required. Enjoy!");
        alert.showAndWait();
    }

    private void makeTheAutomationViewToInitialState() {
        oldRevisionFileTextField.clear();
        oldRevisionFileTextField.setDisable(false);
        newRevisionFileTextField.clear();
        newRevisionFileTextField.setDisable(false);
        textWorkingPath.clear();
        revdatePicker.setValue(null);
        arePDFFilesSelected.set(false);
        isWorkingPathSelected.set(false);
        isDatePicked.set(false);
    }
    private void showErrorWhenTryingToReadOutputExcelFile() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Read output.xlsx File");
        alert.setHeaderText(null);
        alert.setContentText("There was an error occurred while trying to read the output.xlsx file" + "\n" +"Please make sure u have the output.xlsx file available in the same path where you have placed the old revision PDF file");
        alert.showAndWait();
    }
    public String  createTheRequiredFolderInDesktop(String folderName) {
        String desktopPath = System.getProperty("user.home") + "/Desktop";
        File existingFolder = new File(desktopPath, folderName);
        // Check if the folder already exists
        if (existingFolder.exists()) {
            showErrorIfOutputFolderAlreadyExists();
            return null;
        } else {
            // Create a new folder
            boolean created = existingFolder.mkdir();
            if (created) {
                return existingFolder.getAbsolutePath();
            } else {
                showErrorWhenTryingCreateOutputFolder();
                return null;
            }
        }
    }
    private void showErrorIfOutputFolderAlreadyExists() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Create The Output Folder");
        alert.setHeaderText(null);
        alert.setContentText("There is a folder available in Desktop with name: FrontMatter-DO NOT DELETE"+ "\n"
                +"Please Delete/Rename that folder and try again");
        alert.showAndWait();
    }
    private void showErrorWhenTryingCreateOutputFolder() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Create The Output Folder");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to create FrontMatter-DO NOT DELETE Folder, Please try again!");
        alert.showAndWait();
    }
    public String onlySpecifiedFileIsAvailableInGivenFolder(String folderPath) {
        File folder = new File(folderPath);
        // Check if the folder exists
        if (!folder.exists() || !folder.isDirectory()) {
            showFrontMatterFolderNotAvailable();
            return null;
        }
        // Get list of files in the folder
        File[] files = folder.listFiles();
        // Check if files is null or empty
        boolean isOutputExcelAvailable = false;
        if (files == null || files.length == 0) {
            showDoNotUseThisToolAsOutputFileNotAvailable(System.getProperty("user.home") + "/Desktop" + "/FrontMatter-DO NOT DELETE");
            return null;
        }
        // Iterate through files and delete all except "output.xlsx"
        for (File file : files) {
            if (file.getName().endsWith(".txt")) {
                if (!file.delete()) {
                    showNotAbleToDeleteFile(file.getName());
                    return null;
                }
            }
           else if(file.getName().equals("output.xlsx")){
                isOutputExcelAvailable = true;
            }
        }
        if (!isOutputExcelAvailable) {
            showDoNotUseThisToolAsOutputFileNotAvailable(System.getProperty("user.home") + "/Desktop" + "/FrontMatter-DO NOT DELETE");
            return null;
        } else {
            boolean openedOrNot = checkIfFileisOpenedOrNot(folderPath, "output.xlsx");
            if(!openedOrNot){
                showOutputExcelIsOpened();
                return null;
            }
            else
                return "Deleted";
        }
    }
    public boolean checkIfFileisOpenedOrNot(String deskTopFolder, String fileName){
        Path path = Paths.get(deskTopFolder + "\\" + fileName);
        try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "rw");
             FileChannel fileChannel = file.getChannel()) {
            // Attempt to obtain an exclusive lock on the file
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock != null) {
                fileLock.release();
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }
    private void showFrontMatterFolderNotAvailable() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FrontMatter-DO NOT DELETE Folder Not Available");
        alert.setHeaderText(null);
        alert.setContentText("\"FrontMatter-DO NOT DELETE\" Folder is Missing in your Desktop." +"\n"+
                "Hence we can't proceed with the FrontMatter Automation Tool. Please update Manually.");
        alert.showAndWait();
    }
    private void showNotAbleToDeleteFile(String fileName) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Delete File: " + fileName);
        alert.setHeaderText(null);
        alert.setContentText("Please check if file with name '"+fileName+"' is opened or not."+"\n"
        + "If open please close the file and try again");
        alert.showAndWait();
    }
    private void showOutputExcelIsOpened() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("output.xlsx File Is Opened By Someone");
        alert.setHeaderText(null);
        alert.setContentText("Please check if output.xlsx is already open by you." +"\n"
                + "If open please close the file and try again");
        alert.showAndWait();
    }
    private void showNotAbleToFindChangesForDisplay() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could Not Identify The FrontMatter Changes");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to compare Old and New PDFs." +"\n"
                + "Please try again");
        alert.showAndWait();
    }
    public static boolean isFileAvailable(String path, String filename) {
        File file = new File(path, filename);
        // Check if the file exists
        if (!file.exists()) {
            return true; // File does not exist
        }

        // Check if the file is currently open
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            // Try to acquire a lock on the file
            FileLock lock = channel.tryLock();

            if (lock == null) {
                // File is open
                return false;
            }

            // Release the lock
            lock.release();
        } catch (IOException e) {
            // Handle IOException if needed
            e.printStackTrace();
            return false;
        }

        return true; // File exists but is not open
    }

    private void showFM_RevisionChangesExcelIsOpened() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FM_RevisionChanges.xlsx File Is Opened By You");
        alert.setHeaderText(null);
        alert.setContentText("Please check if FM_RevisionChanges.xlsx is already open by you." +"\n"
                + "If open please close the file and try again");
        alert.showAndWait();
    }
    private void makeTheDisplayViewToInitialState() {
        oldRevTextView.clear();
        oldRevTextView.setDisable(false);
        newRevTextView.clear();
        newRevTextView.setDisable(false);
        arePDFFilesSelectedForPane2.set(false);
    }

}
