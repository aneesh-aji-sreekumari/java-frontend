package com.office.frontend;
import backend.controllers.FrontMatterComparisonController;
import backend.models.FrontmatterComparisonResult;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class DashController implements Initializable {
    private final FrontMatterComparisonController frontMatterComparisonController = new FrontMatterComparisonController();
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
    private Pane pane1;
    @FXML
    private Pane pane2;
    @FXML
    private Pane pane3;

    @FXML
    private TextField oldRevisionFileTextField;
    @FXML
    private TextField newRevisionFileTextField;

    @FXML
    private TextField oldRevisionFileTextField1;
    @FXML
    private TextField newRevisionFileTextField1;

    @FXML
    private TextField oldRevisionFileTextField2;
    @FXML
    private TextField newRevisionFileTextField2;
    private File oldFile;
    private File newFile;
    private BooleanProperty arePDFFilesSelected = new SimpleBooleanProperty(false);
    private BooleanProperty isWorkingPathSelected = new SimpleBooleanProperty(false);
    private BooleanProperty isDatePicked = new SimpleBooleanProperty(false);
    @FXML
    private void selectOldRevisionFile(ActionEvent event) {
        handleFileSelection(oldRevisionFileTextField, (Button) event.getSource());
    }

    @FXML
    private void selectNewRevisionFile(ActionEvent event) {
        handleFileSelection(newRevisionFileTextField, (Button) event.getSource());
    }

    @FXML
    private void selectOldRevisionFile1(ActionEvent event) {
        handleFileSelection(oldRevisionFileTextField1, (Button) event.getSource());
    }

    @FXML
    private void selectNewRevisionFile1(ActionEvent event) {
        handleFileSelection(newRevisionFileTextField1, (Button) event.getSource());
    }

    @FXML
    private void selectOldRevisionFile2(ActionEvent event) {
        handleFileSelection(oldRevisionFileTextField2, (Button) event.getSource());
    }

    @FXML
    private void selectNewRevisionFile2(ActionEvent event) {
        handleFileSelection(newRevisionFileTextField2, (Button) event.getSource());
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
    private void bindShowNextButton(){

        nextButton.disableProperty().bind(isDatePicked.not());
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        enableAndShowPane(pane1);
        bindShowWorkingFolderPathButton();
        bindShowDatePicker();
        bindShowNextButton();
    }
    private void updatePDFFilesSelectedStatus() {
        boolean pdfFilesSelected = (isTextFieldNotEmpty(oldRevisionFileTextField)
                && (isTextFieldNotEmpty(newRevisionFileTextField)));

       arePDFFilesSelected.set(pdfFilesSelected);

    }

    private void updateDatePickerStatus(){
        isDatePicked.set(true);
    }

    private void updateWorkingFolderPathSelectStatus() {
        boolean workingPathSelected = (isTextFieldNotEmpty(textWorkingPath));

        isWorkingPathSelected.set(workingPathSelected);
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
                if(clickedButton.getId().equals("oldRevisionBtn"))
                    oldFile = selectedFile;
                else if(clickedButton.getId().equals("newRevisionButton"))
                    newFile = selectedFile;
                updatePDFFilesSelectedStatus();
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
            updateWorkingFolderPathSelectStatus();
        }

    }

//    private boolean isPDFFilesSelected() {
//        return arePDFFilesSelected.get() &&
//                (isTextFieldNotEmpty(oldRevisionFileTextField) || isTextFieldNotEmpty(oldRevisionFileTextField1) || isTextFieldNotEmpty(oldRevisionFileTextField2)) &&
//                (isTextFieldNotEmpty(newRevisionFileTextField) || isTextFieldNotEmpty(newRevisionFileTextField1) || isTextFieldNotEmpty(newRevisionFileTextField2));
//    }

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
        if(oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())){
            oldRevisionFileTextField.clear();
            newRevisionFileTextField.clear();
            arePDFFilesSelected.set(false);
            isWorkingPathSelected.set(false);
            isDatePicked.set(false);
            showPDFAlertSameFilesSelected();
            return;
        }
        System.out.println(oldFile.getAbsolutePath());
        System.out.println(newFile.getAbsolutePath());
        FrontmatterComparisonResult output = frontMatterComparisonController.processPDF(oldFile, newFile);
       if(output.getLoiComparisonResult().isPresent())
           System.out.println(output.getLoiComparisonResult().get());
        if(output.getLotComparisonResult().isPresent())
            System.out.println(output.getLotComparisonResult().get());
        if(output.getTocComparisonResult().isPresent())
            System.out.println(output.getTocComparisonResult().get());
        openNewWindowIfNeeded(output.getLoiComparisonResult().get(), "Changes in List Of Illustrations");
        openNewWindowIfNeeded(output.getLotComparisonResult().get(), "Changes in List Of Tables");
        openNewWindowIfNeeded(output.getTocComparisonResult().get(), "Changes in Table Of Contents");
    }

}
