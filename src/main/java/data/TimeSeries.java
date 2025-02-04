package data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class TimeSeries {

    private JsonDataFileReader fileReader;
    private int noOfObservations;
    private double[] times;
    private double[] observations;
    private String name;

    private RangeTree rangeTree;


    /**
     * TimeSeries constructed from a data file with a given file path.
     * Data file must have same structure as example files given for the
     * project.
     *
     * Inspiration and code snippets from:
     * https://howtodoinjava.com/java/library/json-simple-read-write-json-examples/
     */
    public TimeSeries(String filePath) throws Exception {


        fileReader = new JsonDataFileReader(filePath);

        // Number of dimensions is the amount of values for each time stamp.
        // For some reason, the value cannot be converted to int right away
        int noOfDimensions = (int) ((long) fileReader.get("dimension"));
        if (noOfDimensions != 1)
            throw new InvalidDimensionException("Only data files with one value dimension are allowed.");

        name = (String) fileReader.get("timeseres");
        readObservations();
        rangeTree = new RangeTree(observations);

    }

    private void readObservations() throws Exception {

        JSONArray jsonObservations = (JSONArray) fileReader.get("observations");
        noOfObservations = jsonObservations.size();

        observations = new double[noOfObservations];
        times = new double[noOfObservations];
        for (int i = 0; i < noOfObservations; i++) {

            JSONObject jsonObservation = (JSONObject) jsonObservations.get(i);

            // Read time into 0th row of observations array
            times[i] = (double) jsonObservation.get("time");

            // Read corresponding values into following rows
            JSONArray values = (JSONArray) jsonObservation.get("values");
            observations[i] = (double) values.get(0);

        }
    }

    public MinMax getMinMax(int startIndex, int endIndex) throws Exception {
        return rangeTree.getMinMax(startIndex, endIndex);
    }

    public MinMax getMinMax() {
        try {
            return rangeTree.getMinMax(0, getLength() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // UNCOMMENT FOR TESTING SPEED GAINS
//    public MinMax getMinMax() {
//        return getMinMax(0, getLength() - 1);
//    }
//
//    public MinMax getMinMax(int lowerBound, int upperBound) {
//        double min = observations[lowerBound];
//        double max = observations[lowerBound];
//        for (int i = lowerBound; i <= upperBound; i++) {
//            double value = observations[i];
//            if (value < min) {
//                min = value;
//            } else if (value > max) {
//                max = value;
//            }
//        }
//        return new MinMax(min, max);
//    }

    public double[] getObservations() {
        return observations;
    }

    public double getTimeAtIndex(int index) {
        return times[index];
    }

    public int getLength() {
        return noOfObservations;
    }

    public String getName() {
        return name;
    }

    public double[] getTimes() {
        return times;
    }



}
