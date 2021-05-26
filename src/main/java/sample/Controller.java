package sample;

import bp.BreakPointAlgorithm;
import bp.Statics;
import data.InvalidDimensionException;
import data.TimeSeries;
import fitness.FitnessRectangle;
import fitness.FitnessStringCodes;
import ga.Individual;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Controller {

    /* ====================================================================== */
    /* FXML elements                                                          */
    /* ====================================================================== */

    @FXML private AnchorPane anchorPaneRoot;

    /* ----- Left Button-Menu ----- */

    @FXML private AnchorPane leftMenuPane;
    @FXML private Button loadTimeSeriesSmallBtn;
    @FXML private Button btnSettingsPane;
    @FXML private Button runSmallBtn;


    /* ----- Settings Pane ----- */

    @FXML private ScrollPane scrollPaneSettings;

    @FXML private Text currentDataFile;

    @FXML private ChoiceBox<String> displayModeChooser;

    @FXML private ChoiceBox<String> fitnessMethodChooser;

    @FXML private Button runAlgorithmBtn;

    // Sliders
    @FXML private Slider popSz;
    @FXML private Slider maxBPSlider;
    @FXML private Slider mutationProbInput;
    @FXML private Slider onePointCrossInput;
    @FXML private Slider uniCrossInput;
    @FXML private Slider alphaInput;

    // Text fields showing the values of their respective sliders
    @FXML private Text popSizeVal;
    @FXML private Text maxBPVal;
    @FXML private Text mutationProbVal;
    @FXML private Text onePointCrossVal;
    @FXML private Text uniCrossVal;
    @FXML private Text alphaVal;


    /* ----- Line Chart / Data Graph ----- */

    @FXML private LineChart graphPlaceHolder;


    /* ====================================================================== */
    /* Other Controller Class Fields                                          */
    /* ====================================================================== */

    private FileChooser fileChooser;
    private BreakPointAlgorithm algorithm;

    // The actual Object which will show the time series and fitness
    private DataGraph<Number, Number> dataGraph;

    public Controller() {
        algorithm = new BreakPointAlgorithm();
    }

    @FXML
    public void initialize() {

        // Hide settings panel on launch. Disable run buttons (until file is loaded)
        scrollPaneSettings.setVisible(false);
        scrollPaneSettings.setManaged(false);
        runSmallBtn.setDisable(true);
        runAlgorithmBtn.setDisable(true);

        // Replace "Placeholder graph" with actual Data Graph Object
        dataGraph = new DataGraph<>(new NumberAxis(), new NumberAxis());
        AnchorPane.setLeftAnchor(dataGraph, AnchorPane.getLeftAnchor(graphPlaceHolder));
        AnchorPane.setTopAnchor(dataGraph, 0.);
        AnchorPane.setBottomAnchor(dataGraph, 0.);
        AnchorPane.setRightAnchor(dataGraph, 0.);
        anchorPaneRoot.getChildren().add(dataGraph);
        anchorPaneRoot.getChildren().remove(graphPlaceHolder);

        displayModeChooser.getItems().addAll(DataGraph.DISPLAY_MODES);
        displayModeChooser.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldVal, newVal) -> {
                    String mode = DataGraph.DISPLAY_MODES[newVal.intValue()];
                    dataGraph.setDisplayMode(mode);
                });
        displayModeChooser.setValue(DataGraph.DISPLAY_MODES[0]);

        // Add fitness methods to drop-down menu.
        fitnessMethodChooser.getItems().addAll(FitnessStringCodes.getAllCodes());
        fitnessMethodChooser.getSelectionModel().selectedIndexProperty().addListener(
                (obs, oldVal, newVal) -> {
                    String code = FitnessStringCodes.getAllCodes()[newVal.intValue()];
                    algorithm.setFitness(code);
                });
        fitnessMethodChooser.setValue(FitnessStringCodes.getAllCodes()[0]);


        // Add tooltips to buttons in the Left Menu.
        loadTimeSeriesSmallBtn.setTooltip(new Tooltip("Load time series data file"));
        btnSettingsPane.setTooltip(new Tooltip("Parameter settings (open/close)"));
        runSmallBtn.setTooltip(new Tooltip("Run algorithm"));

        // Update value next to sliders for Population Size and Maximum Number
        // of Break Points.
        popSz.valueProperty().addListener((obs, oldVal, newVal) -> {
            popSizeVal.setText("" + newVal.intValue());
            algorithm.setNoOfIndividuals(newVal.intValue());
        });

        maxBPSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxBPVal.setText("" + newVal.intValue());
            algorithm.setMaxNoOfBreakPoints(newVal.intValue());
        });

        // When moving the slider for Mutation Probability, the text showing the
        // value changes, and the slider for the two other probabilities becomes
        // half of the Mutation Probability.
        mutationProbInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            int roundedVal = newVal.intValue();
            mutationProbVal.setText(roundedVal + "%");
            int diff = 100 - roundedVal;
            int newOp = (int) Math.ceil(diff / 2.);
            int newUni = diff / 2;
            onePointCrossInput.adjustValue(newOp);
            uniCrossInput.adjustValue(newUni);
            onePointCrossInput.setDisable(diff == 0);
            algorithm.setMutateProb(newVal.intValue() / 100.);
        });

        // When moving the slider for One Point Crossover Probability, the text
        // showing the value is updated, and the slider for Uniform Crossover
        // Probability is moved in the reverse direction.
        onePointCrossInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            onePointCrossVal.setText(newVal.intValue() + "%");
            int diff = 100 - ((int) mutationProbInput.getValue()) - newVal.intValue();
            int maxValue = 100 - ((int) mutationProbInput.getValue());
            onePointCrossInput.adjustValue(Math.min(newVal.intValue(), maxValue));
            uniCrossInput.adjustValue(Math.max(diff, 0));
            algorithm.setOnePointCrossoverProb(newVal.intValue() / 100.);
        });

        // The slider for Uniform Crossover Probability is never enabled and can
        // thus only be updated through the two other Probability Sliders. When
        // the slider moves (from the other slider), the text with the value is
        // updated.
        uniCrossInput.setDisable(true);
        uniCrossInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            uniCrossVal.setText(newVal.intValue() + "%");
            algorithm.setUniformCrossoverProb(newVal.intValue() / 100.);
        });

        // Update the value text for the alpha parameter when the slider is
        // moved. Show two decimal places.
        alphaInput.valueProperty().addListener((obs, oldVal, newVal) -> {
            alphaVal.setText(String.format(Locale.US, "%.2f", newVal.doubleValue()));
            algorithm.setAlpha(newVal.doubleValue());
        });

        // Setup file chooser to look for JSON files (time series data files)
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select time series data file");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json")
        );
    }


    @FXML
    public void openFileChooser(MouseEvent mouseEvent) {

        File dataFile = fileChooser.showOpenDialog(Main.getPrimaryStage());
        if (dataFile != null) {
            try {

                TimeSeries timeSeries = new TimeSeries(dataFile.getAbsolutePath());
                algorithm.setTimeSeries(timeSeries);

                dataGraph.getData().clear();
                dataGraph.clearFitnessMarkers();
                dataGraph.setTimeSeries(timeSeries);

                currentDataFile.setText("Current: " + dataFile.getName());

                // Activate Run Algorithm buttons
                runAlgorithmBtn.setDisable(false);
                runSmallBtn.setDisable(false);

            } catch (InvalidDimensionException e) {
                showPopup("error", e.getMessage());
            }


        }
    }

    /**
     * Display a popup with a given style and message. Disables the root node
     * when the popup is showing; enables root node when popup is closed.
     * @param popupStyle A string representing the style of the popup. Currently
     *                  only "warning" or "error" are allowed
     * @param message A string of the message in the popup
     */
    private void showPopup(String popupStyle, String message) {
        Popup popup = new Popup();
        popup.setHideOnEscape(true);
        popup.setOnHidden(e -> anchorPaneRoot.setDisable(false));

        Label label = new Label(message);
        label.setId(popupStyle + "-popup-label");

        Button button = new Button();
        button.setId("popup-button");
        button.getStyleClass().add(popupStyle + "-popup-button");
        button.setOnMouseClicked(e -> popup.hide());

        HBox hBox = new HBox();
        hBox.setId("popup-hbox");
        hBox.getStyleClass().add(popupStyle + "-popup-hbox");
        hBox.getChildren().addAll(label, button);

        popup.getContent().add(hBox);
        popup.show(Main.getPrimaryStage());

        anchorPaneRoot.setDisable(true);
    }

    /**
     * Run the algorithm on press of a button.
     * @param mouseEvent
     */
    @FXML
    public void runAlgorithm(MouseEvent mouseEvent) {
        dataGraph.clearFitnessMarkers();
        Individual solution = algorithm.findBreakPoints();
        showFitness(solution, algorithm.getTimeSeries());
    }

    /**
     * Help from here: https://stackoverflow.com/questions/28558165/javafx-setvisible-hides-the-element-but-doesnt-rearrange-adjacent-nodes
     * @param mouseEvent
     */
    @FXML
    public void toggleSettingsMenu(MouseEvent mouseEvent) throws InterruptedException {

        // Hide or show Settings Panel depending on current visibility
        boolean toggleBool = !scrollPaneSettings.isVisible();
        scrollPaneSettings.setVisible(toggleBool);
        scrollPaneSettings.setManaged(toggleBool);
        scrollPaneSettings.toFront();

        // Match left edge of data graph to the left edge of the program
        double chartAnchor = leftMenuPane.getWidth();
        if (toggleBool)
            chartAnchor += scrollPaneSettings.getPrefWidth();
        AnchorPane.setLeftAnchor(dataGraph, chartAnchor);

    }

    public void showFitness(Individual individual, TimeSeries timeSeries) {

        List<Integer> breakPointIndexes = findBreakPointIndexes(individual);
        for (int i = 0; i < breakPointIndexes.size() - 1; i++) {
            int startIndex = 1 + breakPointIndexes.get(i);
            int endIndex = breakPointIndexes.get(i + 1);

            double[] values = timeSeries.getObservations();

            double[] minAndMaxValues = getMinAndMaxInInterval(values, startIndex, endIndex);
            double minValue = minAndMaxValues[0];
            double maxValue = minAndMaxValues[1];

            FitnessRectangle fitnessRectangle = new FitnessRectangle(startIndex, endIndex, minValue, maxValue);
            Rectangle rectangle = new Rectangle();
            rectangle.setId("fitness-box");
            dataGraph.addRectangle(fitnessRectangle.getxMin(), fitnessRectangle.getxMax(), fitnessRectangle.getyMin()
                    , fitnessRectangle.getyMax());

        }

    }

    private static ArrayList<Integer> findBreakPointIndexes(Individual individual) {
        ArrayList<Integer> breakPointIndexes = new ArrayList<>();
        for (int i = 0; i < individual.getNoOfGenes(); i++) {
            char allele = individual.getAllele(i);
            if (allele == Statics.breakPointAllele)
                breakPointIndexes.add(i);
        }
        return breakPointIndexes;
    }

    private static double[] getMinAndMaxInInterval(double[] array, int lowerBound, int upperBound) {
        double min = array[lowerBound];
        double max = array[upperBound];
        for (int i = lowerBound; i <= upperBound; i++) {
            double value = array[i];
            if (value < min) {
                min = value;
            } else if (value > max) {
                max = value;
            }
        }
        return new double[] {min, max};
    }


}
