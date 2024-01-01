package com.office.frontend;

import com.jfoenix.controls.JFXListView;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.List;

public class ListViewController {
    @FXML
    private JFXListView<String> newWindowListView;

    // Other fields and methods

    public void initialize() {
        // Configure the cell factory for the JFXListView
        newWindowListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty) {
                            setText(item);
                            // Enable text wrapping
                            setStyle("-fx-font-size: 15;");

//                            setStyle("-fx-font-size: 15;");
//                            setText(item);
//                            setWrapText(true);
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });
    }

    public void setItems(List<String> items) {
        newWindowListView.getItems().setAll(items);
    }

    // Other methods and event handlers
}

