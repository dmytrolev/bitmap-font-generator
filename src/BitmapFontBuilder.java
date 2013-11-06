import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.geometry.Pos;

public class BitmapFontBuilder extends Application {
    @Override
    public void start(Stage stage) {
        VBox box = new VBox();
        Scene scene = new Scene(box, 600, 400);
        stage.setScene(scene);
        stage.setTitle("ListViewSample");
        FontsList fl = new FontsList();
        fl.loadSystemFonts();
        box.getChildren().addAll(fl);
        VBox.setVgrow(fl, Priority.ALWAYS);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
