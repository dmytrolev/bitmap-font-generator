import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.geometry.Pos;
import java.util.Map;
import java.util.HashMap;

public class FontsList extends ListView {

  public FontsList() {
    setCellFactory(list -> new FontNameCell());
    getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  public void loadSystemFonts() {
    ObservableList fonts = FXCollections.observableArrayList(Font.getFamilies());
    setItems(fonts);
  }

  static class FontNameCell extends ListCell<String> {
    public FontNameCell() {
      setPrefHeight(60);
      setAlignment(Pos.CENTER);
    }

    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setFont(Font.font(item, FontWeight.NORMAL, 12));
      setTextAlignment(TextAlignment.CENTER);
      setText(item);
    }
  }
}
