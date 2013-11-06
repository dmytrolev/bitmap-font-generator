import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import java.util.Map;
import java.util.HashMap;

public class FontsList extends ListView {

  public FontsList() {
    setCellFactory(list -> new FontNameCell());

                   // new Callback<ListView<String>,
                   // ListCell<String>>() {
                   //   @Override
                   //     public ListCell<String> call(ListView<String> list) {
                   //     return 
                   //   }
                   // }
                   // );
  }

  public void loadSystemFonts() {
    ObservableList fonts = FXCollections.observableArrayList(Font.getFontNames());
    setItems(fonts);
  }

  static class FontNameCell extends ListCell<String> {
    public static Map<String, Text> texts = new HashMap<String, Text>();

    @Override
      public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      Text fontInstance;
      if(texts.containsKey(item)) fontInstance = texts.get(item);
      else {
        fontInstance = new Text(item);
        fontInstance.setFont(Font.font(item, FontWeight.NORMAL, 20));
        texts.put(item, fontInstance);
      }
      getChildren().clear();
      getChildren().add(fontInstance);
      // setPrefHeight(100);
    }
  }
}
