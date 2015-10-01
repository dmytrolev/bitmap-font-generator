import java.io.IOException;
import java.nio.file.Path;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;

import java.util.EventListener;

public class CustomImageSymbolController extends VBox {
  @FXML private Canvas image;
  @FXML private Label letter;

  public Image img;
  public String letterSymbol;

  public static interface DelEventListener extends EventListener {
    public void run(CustomImageSymbolController me);
  }

  DelEventListener callback;

  public CustomImageSymbolController(String url, String letter) {
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CustomImageSymbol.fxml"));
    fxmlLoader.setController(this);
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }

    this.letter.setText(letter);
    letterSymbol = letter;

    img = new Image(url, false);
    if(img.getWidth() > image.getWidth() || img.getHeight() > image.getHeight()) {
      image.getGraphicsContext2D().drawImage(img, 0,0,image.getWidth(),image.getHeight());
    } else image.getGraphicsContext2D().drawImage(img, Math.floor((image.getWidth() - img.getWidth()) / 2), Math.floor((image.getHeight() - img.getHeight()) / 2));
  }

  @FXML
  protected void deleteSymbol() {
    callback.run(this);
  }
}
