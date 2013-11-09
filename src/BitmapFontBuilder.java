import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.SnapshotResult;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TextArea;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Button;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ComboBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.scene.effect.Shadow;
import javafx.scene.image.WritableImage;
import javafx.geometry.VPos;
import javafx.geometry.Insets;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.event.Event;
import java.util.Set;
import java.util.List;
import java.io.File;

public class BitmapFontBuilder extends Application {
  protected FontsList mFontsList;
  protected ComboBox mSubFont;
  protected Slider mFontSize;
  protected TextArea mText;
  protected Canvas mCanvas;
  protected ColorPicker mFillColor;
  protected CheckBox mFillCb;
  protected ColorPicker mStrokeColor;
  protected Slider mStrokeWidth;
  protected CheckBox mStrokeCb;
  protected CheckBox mShowBorderCb;
  protected String mFont = "Calibri";
  protected String mFamily = "Calibri";

  protected GlyphBank mGlyphBank = new GlyphBank();

  protected boolean mEffect = false;

  protected Stage mStage;

  @Override
  public void start(Stage stage) {
    stage.setTitle("Bitmap Font Generator");

    // Top section
    Button saveBtn = new Button("");
    saveBtn.setFont(Font.loadFont("file:///media/stuff/docs/java/bitmap-font-generator/fontello.ttf", 30));
    saveBtn.setTooltip(new Tooltip("Save Bitmap Font to File"));
    saveBtn.setOnAction(event -> saveFont());

    HBox topBox = new HBox(15d);
    topBox.setPadding(new Insets(5d));
    topBox.getChildren().addAll(saveBtn);

    // Fonts section
    mFontsList = new FontsList();
    mFontsList.loadSystemFonts();
    mFontsList.getSelectionModel().selectedItemProperty().addListener(this::onFontSelectChange);
    VBox.setVgrow(mFontsList, Priority.ALWAYS);

    // Sub fonts section
    mSubFont = new ComboBox<String>();
    mSubFont.getSelectionModel().selectedItemProperty().addListener(this::onSubFontSelectChange);

    // Font Size section
    mFontSize = new Slider(5d, 120d, 24d);
    mFontSize.setShowTickMarks(true);
    mFontSize.setShowTickLabels(true);
    mFontSize.setMajorTickUnit(15d);
    mFontSize.setBlockIncrement(2d);
    mFontSize.valueProperty().addListener((ov, oldv, newv) -> drawText());

    VBox fontBox = new VBox(15d);
    fontBox.setMinWidth(300d);
    fontBox.getChildren().addAll(mSubFont, mFontSize, mFontsList);
    TitledPane fontHost = new TitledPane("Font", fontBox);

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
    leftBox.getChildren().addAll(textHost, fontHost);

    // Fill section
    mFillColor = new ColorPicker(Color.BLACK);
    mFillColor.setOnAction(event -> drawText());

    mFillCb = new CheckBox("Use fill");
    mFillCb.setSelected(true);
    mFillCb.setOnAction(event -> drawText());

    VBox fillBox = new VBox();
    fillBox.setSpacing(15d);
    fillBox.getChildren().addAll(mFillCb, mFillColor);

    TitledPane fillHost = new TitledPane("Fill", fillBox);

    // Stroke section
    mStrokeColor = new ColorPicker(Color.RED);
    mStrokeColor.setOnAction(event -> drawText());

    mStrokeCb = new CheckBox("Use Stroke");
    mStrokeCb.setSelected(true);
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
    mCanvas = new Canvas(512, 512);
    drawText();

    // All sections
    BorderPane root = new BorderPane();
    root.setTop(topBox);
    root.setLeft(leftBox);
    root.setCenter(new TitledPane("Image", mCanvas));
    root.setRight(rightBox);
    root.setPrefWidth(Double.MAX_VALUE);
    root.setPrefHeight(Double.MAX_VALUE);

    mFontsList.getSelectionModel().selectFirst();

    stage.setScene(new Scene(root, 960, 600));

    stage.show();

    mStage = stage;
  }

  protected void saveFont() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Font File", "*.fnt"));
    fileChooser.setTitle("Save bitmap font");
    fileChooser.setInitialFileName("font.fnt");
    File saveFile = fileChooser.showSaveDialog(mStage);
    if(null == saveFile) return;

    Canvas toSave = new Canvas(mGlyphBank.size, mGlyphBank.size);
    toSave.getGraphicsContext2D().clearRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    drawGlyphs(toSave.getGraphicsContext2D(), mGlyphBank);
    SnapshotParameters pp = new SnapshotParameters();
    pp.setFill(new Color(0d, 0d, 0d, 0d));
    toSave.snapshot(result -> {
        new BitmapSaver(result.getImage(), saveFile);
        new XmlSaver(mGlyphBank.glyphs(), saveFile);
        return null;
      }, pp, new WritableImage((int)mGlyphBank.size, (int)mGlyphBank.size));
  }

  protected void onFontSelectChange(ObservableValue ov, Object oldv, Object newv) {
    mFamily = (String)newv;
    List<String> names = Font.getFontNames(mFamily);
    mSubFont.getItems().clear();
    for(String name : names) {
      String shortName = name.startsWith(mFamily) ? name.substring(mFamily.length()) : name;
      if(shortName.isEmpty()) shortName = " default";
      mSubFont.getItems().add(shortName);
    }
    if(null == mSubFont.getSelectionModel().getSelectedItem())
      mSubFont.getSelectionModel().selectFirst();
    drawText();
  }

  protected void onSubFontSelectChange(ObservableValue ov, Object oldv, Object newv) {
    String subtype = (String)newv;
    mFont = mFamily + (" default".equals(subtype) ? "" : subtype);
    System.out.println(mFont);
    drawText();
  }

  protected void drawText() {
    GraphicsContext gc = mCanvas.getGraphicsContext2D();
    // build glyphs
    mGlyphBank.setFont(getFont());
    mGlyphBank.extract(mText.getText());
    // prepare background
    gc.clearRect(0, 0, Double.MAX_VALUE, Double.MAX_VALUE);
    gc.setFill(Color.WHITE);
    gc.setLineWidth(1d);
    gc.setStroke(Color.BLACK);
    gc.fillRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    gc.strokeRect(0, 0, mGlyphBank.size, mGlyphBank.size);
    // print glyphs
    drawGlyphs(gc, mGlyphBank);
  }

  protected Font getFont() {
    boolean bold = mFont.indexOf("Bold") >= 0;
    boolean oblique = mFont.indexOf("Oblique") >= 0 || mFont.indexOf("Italic") >= 0;
    return Font.font(mFont, bold ? FontWeight.BOLD : FontWeight.NORMAL,
                     oblique ? FontPosture.ITALIC : FontPosture.REGULAR, mFontSize.getValue());
  }

  protected void drawGlyphs(GraphicsContext gc, GlyphBank gb) {
    gc.setFont(getFont());
    Set<GlyphBank.Glyph> chars = gb.glyphs();
    double x = 0; double y = 0;
    for(GlyphBank.Glyph cc : chars) {
      if(x + cc.w > mGlyphBank.size) {
        x = 0;
        y += cc.h;
      }
      cc.x = x;
      cc.y = y;
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
