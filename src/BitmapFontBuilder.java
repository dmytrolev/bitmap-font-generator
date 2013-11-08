import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.Shadow;
import javafx.geometry.VPos;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.event.Event;
import java.util.Set;
import java.util.List;

public class BitmapFontBuilder extends Application {
  protected FontsList mFontsList;
  protected TextArea mText;
  protected Canvas mCanvas;
  protected ColorPicker mFillColor;
  protected CheckBox mFillCb;
  protected ColorPicker mStrokeColor;
  protected Slider mStrokeWidth;
  protected CheckBox mStrokeCb;
  protected CheckBox mShowBorderCb;
  protected String mFont = "Calibri";

  protected boolean mEffect = false;

  @Override
  public void start(Stage stage) {
    stage.setTitle("Bitmap Font Generator");

    // Fonts section
    mFontsList = new FontsList();
    mFontsList.loadSystemFonts();
    mFontsList.getSelectionModel().selectedItemProperty().addListener(this::onFontSelectChange);
    VBox.setVgrow(mFontsList, Priority.ALWAYS);

    // TitledPane fontsHost = new TitledPane("Fonts", mFontsList);
    // fontsHost.setPrefWidth(200d);
    // fontsHost.setPrefHeight(Double.MAX_VALUE);

    // Text section
    mText = new TextArea("dima loves alina");
    VBox.setVgrow(mText, Priority.ALWAYS);
    mText.setPrefWidth(200d);

    Button update = new Button("Update Glyphs");
    update.setOnAction(event -> drawText());

    VBox textBox = new VBox();
    textBox.setSpacing(15d);
    textBox.getChildren().addAll(mText, update);

    TitledPane textHost = new TitledPane("Glyphs", textBox);

    // Left section assemble
    VBox leftBox = new VBox();
    leftBox.setMinWidth(300d);
    leftBox.getChildren().addAll(textHost, mFontsList);

    // Fill section
    mFillColor = new ColorPicker();
    mFillColor.setOnAction(event -> drawText());

    mFillCb = new CheckBox("Use fill");
    mFillCb.setOnAction(event -> drawText());

    VBox fillBox = new VBox();
    fillBox.setSpacing(15d);
    fillBox.getChildren().addAll(mFillCb, mFillColor);

    TitledPane fillHost = new TitledPane("Fill", fillBox);

    // Stroke section
    mStrokeColor = new ColorPicker();
    mStrokeColor.setOnAction(event -> drawText());

    mStrokeCb = new CheckBox("Use Stroke");
    mStrokeCb.setOnAction(event -> drawText());

    mStrokeWidth = new Slider(0d, 10d, 1d);
    mStrokeWidth.setShowTickMarks(true);
    mStrokeWidth.setShowTickLabels(true);
    mStrokeWidth.setMajorTickUnit(2d);
    mStrokeWidth.setBlockIncrement(0.5d);
    mStrokeWidth.valueProperty().addListener((ov, oldv, newv) -> drawText());

    VBox strokeBox = new VBox();
    strokeBox.setSpacing(15d);
    strokeBox.getChildren().addAll(mStrokeCb, mStrokeWidth, mStrokeColor);

    TitledPane strokeHost = new TitledPane("Stroke", strokeBox);

    // Settings section
    mShowBorderCb = new CheckBox("Show glyph border");
    mShowBorderCb.setOnAction(event -> drawText());

    VBox settingsBox = new VBox();
    settingsBox.setSpacing(15d);
    settingsBox.getChildren().addAll(mShowBorderCb);

    TitledPane settingsHost = new TitledPane("Settings", settingsBox);

    // Right section assemble
    VBox rightBox = new VBox();
    rightBox.setMinWidth(200d);
    rightBox.getChildren().addAll(fillHost, strokeHost, settingsHost);

    // Center section
    mCanvas = new Canvas(320, 480);
    drawText();

    // All sections
    BorderPane root = new BorderPane();
    root.setLeft(leftBox);
    root.setCenter(new TitledPane("Image", mCanvas));
    root.setRight(rightBox);
    root.setPrefWidth(Double.MAX_VALUE);
    root.setPrefHeight(Double.MAX_VALUE);

    stage.setScene(new Scene(root, 960, 600));

    stage.show();
  }

  protected void onFontSelectChange(ObservableValue ov, Object oldv, Object newv) {
    mFont = (String)newv;
    List<String> names = Font.getFontNames(mFont);
    for(String name : names) System.out.println(name);
    drawText();
  }

  protected void drawText() {
    GraphicsContext gc = mCanvas.getGraphicsContext2D();
    gc.clearRect(0, 0, 320, 240);
    gc.setFill(Color.WHITE);
    gc.setLineWidth(1d);
    gc.setStroke(Color.BLACK);
    gc.fillRect(0,0,320,320);
    gc.strokeRect(0,0,320,320);
    gc.setFill(Color.RED);
    gc.setFont(Font.font(mFont, FontWeight.BLACK, 40d));
    GlyphBank gb = new GlyphBank();
    gb.setFont(mFont);
    gb.extract(mText.getText());
    Set<GlyphBank.Glyph> chars = gb.glyphs();
    double x = 0; double y = 0;
    for(GlyphBank.Glyph cc : chars) {
      if(x + cc.w > 320) {
        x = 0;
        y += cc.h;
      }
      gc.setTextBaseline(VPos.BOTTOM);
      if(mEffect) {
        gc.setEffect(new Shadow(5, Color.BLACK));
        gc.fillText(cc.c, x, y + cc.h);
        gc.setEffect(null);
      }
      if(mFillCb.isSelected()) {
        gc.setFill(mFillColor.getValue());
        gc.fillText(cc.c, x, y + cc.h);
      }
      if(mStrokeCb.isSelected() && mStrokeWidth.getValue() > 0) {
        gc.setLineWidth(mStrokeWidth.getValue());
        gc.setStroke(mStrokeColor.getValue());
        gc.strokeText(cc.c, x, y + cc.h);
      }
      if(mShowBorderCb.isSelected()) {
        gc.setLineWidth(1d);
        gc.strokeRect(x,y,cc.w,cc.h);
      }
      x += cc.w;
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
