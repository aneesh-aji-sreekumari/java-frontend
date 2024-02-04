package backend.frontmatterapi.services;

import javafx.scene.control.Alert;

import java.io.File;
public class StringPatternMatcher {
    public static void main(String[] args) {
        System.out.println(createTheRequiredFolderInDesktop("FrontMatter-DO NOT DELETE"));
    }

    public static String  createTheRequiredFolderInDesktop(String folderName) {
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
    private static void showErrorIfOutputFolderAlreadyExists() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Create The Output Folder");
        alert.setHeaderText(null);
        alert.setContentText("There is a folder available in Desktop with name: FrontMatter-DO NOT DELETE"+ "\n"
                +"Please Delete/Rename that folder and try again");
        alert.showAndWait();
    }
    private static void showErrorWhenTryingCreateOutputFolder() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Couldn't Create The Output Folder");
        alert.setHeaderText(null);
        alert.setContentText("There was an issue while trying to create FrontMatter-DO NOT DELETE Folder, Please try again!");
        alert.showAndWait();
    }
}