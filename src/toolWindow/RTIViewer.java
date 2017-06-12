package toolWindow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import openGLWindow.PTMWindow;
import openGLWindow.PTMWindowLRGB;
import openGLWindow.PTMWindowRGB;
import ptmCreation.PTMObject;
import ptmCreation.PTMObjectLRGB;
import ptmCreation.PTMObjectRGB;
import utils.Utils;

import java.util.ArrayList;

/**
 * Created by Jed on 29-May-17.
 */
public class RTIViewer extends Application {

    public Stage primaryStage;
    private Light.Point light;
    private Circle circle;

    public static Utils.Vector2f globalLightPos = new Utils.Vector2f(0, 0);
    public static double globalDiffGainVal = FilterParamsPane.INITIAL_DIFF_GAIN_VAL;
    public static double globalDiffColourVal = FilterParamsPane.INITIAL_DIFF_COLOUR_VAL;
    public static double globalSpecularityVal = FilterParamsPane.INITIAL_SPEC_VAL;
    public static double globalHighlightSizeVal = FilterParamsPane.INITIAL_HIGHLIGHT_VAL;

    private Spinner<Double> xPosBox, yPosBox;
    private FilterParamsPane paramsPane;

    private enum LightEditor{CIRCLE, XSPINNER, YSPINNER;}
    public enum GlobalParam{DIFF_GAIN, DIFF_COLOUR, SPECULARITY, HIGHTLIGHT_SIZE;}
    public enum ShaderProgram{DEFAULT, NORMALS, DIFF_GAIN, SPEC_ENHANCE;}

    public static ShaderProgram currentProgram = ShaderProgram.DEFAULT;

    public Alert entryAlert;
    public static Alert fileReadingAlert;
    public final FileChooser fileChooser = new FileChooser();

    private static ArrayList<PTMWindow> ptmWindows = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                //for(PTMWindow ptmWindow : ptmWindows){
                //    ptmWindow.setShouldClose(true);
                //}
                Platform.exit();
            }
        });

        primaryStage.setTitle("RTI Viewer");

        createAlerts();
        MenuBarListener.init(this);

        Scene scene = createScene(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    private void createAlerts(){
        entryAlert = new Alert(Alert.AlertType.WARNING);
        entryAlert.setTitle("Invalid Entry");

        fileReadingAlert = new Alert(Alert.AlertType.ERROR);
        entryAlert.setTitle("PTM File Reading Error");
    }

    private Scene createScene(Stage primaryStage){
        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(flowPane, 400, 600);

        MenuBar menuBar = createMenuBar(primaryStage);
        flowPane.getChildren().add(menuBar);

        Pane lightControlGroup = createLightControl(primaryStage, flowPane);
        flowPane.getChildren().add(lightControlGroup);

        ComboBox<String> comboBox = createFilterChoiceBox();
        flowPane.getChildren().add(comboBox);

        paramsPane = new FilterParamsPane(this, scene, comboBox);
        flowPane.getChildren().add(paramsPane);

        flowPane.setMargin(lightControlGroup, new Insets(20, 0, 0, 20));
        flowPane.setMargin(comboBox, new Insets(20, 0, 0, (scene.getWidth() / 2) - (comboBox.getPrefWidth() / 2) ));
        flowPane.setMargin(paramsPane, new Insets(20, 0, 0, 20));
        return scene;
    }


    private MenuBar createMenuBar(Stage primaryStage){
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");

        open.setOnAction(MenuBarListener.getInstance());
        open.setId("open");

        menuFile.getItems().add(open);

        Menu menuEdit = new Menu("Edit");
        Menu menuView = new Menu("View");
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        return menuBar;
    }


    private Pane createLightControl(Stage primaryStage, FlowPane parent){
        Pane pane = new Pane();

        Circle circle = createSpecularBall(primaryStage, pane);

        pane.getChildren().add(circle);
        circle.setLayoutX(circle.getRadius());
        circle.setLayoutY(circle.getRadius());

        Label xPosLabel = new Label("Light X:");
        xPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        xPosLabel.setLayoutY(40);

        Label yPosLabel = new Label("Light Y:");
        yPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        yPosLabel.setLayoutY(70);

        xPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        xPosBox.setEditable(true);
        xPosBox.setPrefWidth(80);
        xPosBox.setLayoutX(2 * circle.getRadius() + 90);
        xPosBox.setLayoutY(37);
        xPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Float value = Float.parseFloat(xPosBox.getEditor().getText());
                    updateGlobalLightPos(new Utils.Vector2f(value, globalLightPos.y), LightEditor.XSPINNER);
                }catch(NumberFormatException e){
                    entryAlert.setContentText("Invalid entry for light X position.");
                    entryAlert.showAndWait();
                }
            }
        });

       xPosBox.valueProperty().addListener(new ChangeListener<Double>() {
           @Override
           public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
               Float value = Float.parseFloat(xPosBox.getEditor().getText());
               updateGlobalLightPos(new Utils.Vector2f(value, globalLightPos.y), LightEditor.XSPINNER);
           }
       });

        yPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        yPosBox.setEditable(true);
        yPosBox.setPrefWidth(80);
        yPosBox.setLayoutX(2 * circle.getRadius() + 90);
        yPosBox.setLayoutY(67);
        yPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Float value = Float.parseFloat(yPosBox.getEditor().getText());
                    updateGlobalLightPos(new Utils.Vector2f(globalLightPos.x, value), LightEditor.YSPINNER);
                }catch(NumberFormatException e){
                    entryAlert.setContentText("Invalid entry for light Y position.");
                    entryAlert.showAndWait();
                }
            }
        });

        yPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                Float value = Float.parseFloat(yPosBox.getEditor().getText());
                updateGlobalLightPos(new Utils.Vector2f(globalLightPos.x, value), LightEditor.YSPINNER);
            }
        });

        pane.getChildren().addAll(xPosLabel, xPosBox, yPosLabel, yPosBox);

        return pane;
    }


    private Circle createSpecularBall(Stage primaryStage, Pane parentGroup){
        circle = new Circle();
        circle.setRadius(100.0);
        circle.setFill(Paint.valueOf("#d3d3d3"));
        light = new Light.Point();
        light.setColor(Color.WHITE);

        light.setX(circle.getRadius());
        light.setY(circle.getRadius());
        light.setZ(25);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        circle.setEffect(lighting);


        circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getX() > circle.getCenterX() - circle.getRadius()
                        && event.getX() < circle.getCenterX() + circle.getRadius()
                        && event.getY() > circle.getCenterY() - circle.getRadius()
                        && event.getY() < circle.getCenterY() + circle.getRadius()) {
                    light.setX(event.getX() + 100);
                    light.setY(event.getY() + 100);
                    Utils.Vector2f normalised = calculateNormalisedLight(light, circle);
                    updateGlobalLightPos(normalised, LightEditor.CIRCLE);
                }
            }
        });

        circle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getX() > circle.getCenterX() - circle.getRadius()
                    && event.getX() < circle.getCenterX() + circle.getRadius()
                    && event.getY() > circle.getCenterY() - circle.getRadius()
                    && event.getY() < circle.getCenterY() + circle.getRadius()) {
                    light.setX(event.getX() + 100);
                    light.setY(event.getY() + 100);
                }else{
                    double theta = Math.atan2(event.getY(), event.getX()) + Math.PI;
                    double x = circle.getRadius() - (circle.getRadius() * Math.cos(theta));
                    double y = circle.getRadius() - (circle.getRadius() * Math.sin(theta));
                    light.setX(x);
                    light.setY(y);
                }
                Utils.Vector2f normalised = calculateNormalisedLight(light, circle);
                updateGlobalLightPos(normalised, LightEditor.CIRCLE);
            }
        });

        return circle;
    }


    private void updateGlobalLightPos(Utils.Vector2f newLight, LightEditor source){
        if(newLight.length() > 1.0) {
            globalLightPos = newLight.normalise();
        }else{
            globalLightPos = newLight;
        }
        updateLightControls(source);
    }

    private void updateLightControls(LightEditor source){
        if(!source.equals(LightEditor.CIRCLE)) {
            light.setX(globalLightPos.x * circle.getRadius() + circle.getLayoutX());
            light.setY(-globalLightPos.y * circle.getRadius() + circle.getLayoutY());
        }
        if(!source.equals(LightEditor.XSPINNER)){
            xPosBox.getValueFactory().setValue((double)globalLightPos.x);
        }
        else if(!source.equals(LightEditor.YSPINNER)){
            yPosBox.getValueFactory().setValue((double)globalLightPos.y);
        }
    }

    private Utils.Vector2f calculateNormalisedLight(Light.Point light, Circle circle){
        double x = (light.getX() - circle.getRadius()) / circle.getRadius();
        double y = -(light.getY() - circle.getRadius()) / circle.getRadius();
        return new Utils.Vector2f((float)x, (float)y);
    }


    private ComboBox<String> createFilterChoiceBox(){
        ObservableList<String> options = FXCollections.observableArrayList(
                "Default view",
                "Normals visualisation",
                "Diffuse gain",
                "Specular enhancement"
        );
        ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().select(0);
        comboBox.setPrefWidth(180);
        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                paramsPane.setCurrentFilter(comboBox.getSelectionModel().getSelectedItem());
                updateWindowFilter(comboBox.getSelectionModel().getSelectedItem());
            }
        });

        //return selectorBox;
        return comboBox;
    }


    public static void main(String[] args) {
        launch(args);
    }


    public void setGlobalVal(GlobalParam param, double value){
        if(param.equals(GlobalParam.DIFF_GAIN)){globalDiffGainVal = value;}
        else if(param.equals(GlobalParam.DIFF_COLOUR)){globalDiffColourVal = value;}
        else if(param.equals(GlobalParam.SPECULARITY)){globalSpecularityVal = value;}
        else if(param.equals(GlobalParam.HIGHTLIGHT_SIZE)){globalHighlightSizeVal = value;}
    }

    public static void createNewPTMWindow(PTMObject ptmObject){
        try {
            if(ptmObject instanceof PTMObjectRGB) {
                PTMWindow ptmWindow = new PTMWindowRGB((PTMObjectRGB) ptmObject);
                Thread thread = new Thread(ptmWindow);
                thread.start();
                ptmWindows.add(ptmWindow);
            }else if(ptmObject instanceof PTMObjectLRGB){
                PTMWindow ptmWindow = new PTMWindowLRGB((PTMObjectLRGB) ptmObject);
                Thread thread = new Thread(ptmWindow);
                thread.start();
                ptmWindows.add(ptmWindow);
            }
        }catch(Exception e){
            fileReadingAlert.setContentText("Couldn't compile OpenGL shader: " + e.getMessage());
            fileReadingAlert.showAndWait();
        }
    }

    private void updateWindowFilter(String filterType){
        ShaderProgram programToSet = ShaderProgram.DEFAULT;

        if(filterType.equals("Default view")){programToSet = ShaderProgram.DEFAULT;}
        else if(filterType.equals("Normals visualisation")){programToSet = ShaderProgram.NORMALS;}
        else if(filterType.equals("Diffuse gain")){programToSet = ShaderProgram.DIFF_GAIN;}
        else if(filterType.equals("Specular enhancement")){programToSet = ShaderProgram.SPEC_ENHANCE;}

        currentProgram = programToSet;

        for(PTMWindow ptmWindow : ptmWindows){
            ptmWindow.setCurrentProgram(programToSet);
        }
    }


    public static void removeWindow(PTMWindow window){
        for(PTMWindow ptmWindow : ptmWindows){
            if (ptmWindow.equals(window)){
                ptmWindows.remove(window);
            }
        }
    }
}