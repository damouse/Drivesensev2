package edu.wisc.drivesense.scoring.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;
import edu.wisc.drivesense.businessLogic.TripRecorder;
import edu.wisc.drivesense.model.Reading;

import edu.wisc.drivesense.model.ReadingHolder;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;
import edu.wisc.drivesense.scoring.neural.modelObjects.TrainingSet;

import static edu.wisc.drivesense.scoring.neural.utils.Timestamp.completeTimestampRangeInDataSet;
import static edu.wisc.drivesense.scoring.neural.utils.Timestamp.dequeueBeforeTimestamp;

/**
 * Created by Damouse on 12/15/2014.
 * <p/>
 * Loads local data (stored in the application directory) and feeds it into ScoreKeeper.
 */
public class LocalDataTester {
    private static final String TAG = "LocalDataTester";

    static boolean lock = false;
    long maxTime = 400000;

    private TripRecorder recorder;
    private Context context;

    public LocalDataTester(TripRecorder analyst, Context context) {
        this.context = context;
        this.recorder = analyst;
    }

    /**
     * Loads the data from the test directory and feeds it into the BackgroundRecordingService
     * method.
     */
    public void feedTestData() {
        lock = false;
        int limit = 10000;
        int iterations = 10;

        Log.d("Reader name " , ReadingHolder.getTableName(ReadingHolder.class));

        for (int i = 0; i < iterations; i++) {
            List<ReadingHolder> holders = ReadingHolder.findWithQuery(ReadingHolder.class, "SELECT * FROM READING_HOLDER LIMIT ?", "" + limit);
            for (ReadingHolder holder: holders)
                recorder.newReading(holder.getReading());

            Log.d("Loader", "Iteration: " + i);
        }
    }

    public void saveTestData(Context context) {
        readFile("magnet.txt", context, Reading.Type.MAGNETIC);
        readFile("gyro.txt", context, Reading.Type.GYROSCOPE);
        readFile("gravity.txt", context, Reading.Type.GRAVITY);
        readFile("gps.txt", context, Reading.Type.GPS);
        readFile("acceleration.txt", context, Reading.Type.ACCELERATION);
    }

    public void readAndLoadTestData(Context context) {


//        readAndLoad("magnet.txt", context, Reading.Type.MAGNETIC);
//        readAndLoad("gyro.txt", context, Reading.Type.GYROSCOPE);
//        readAndLoad("gravity.txt", context, Reading.Type.GRAVITY);
//        readAndLoad("gps.txt", context, Reading.Type.GPS);
//        readAndLoad("acceleration.txt", context, Reading.Type.ACCELERATION);

        TimestampQueue<Reading> allData = new TimestampQueue<Reading>();
        ArrayList<TimestampQueue<Reading>> allQueus = new ArrayList<>();

        allQueus.add(read("magnet.txt", context, Reading.Type.MAGNETIC));
        allQueus.add(read("gyro.txt", context, Reading.Type.GYROSCOPE));
        allQueus.add(read("gravity.txt", context, Reading.Type.GRAVITY));
        allQueus.add(read("gps.txt", context, Reading.Type.GPS));
        allQueus.add(read("acceleration.txt", context, Reading.Type.ACCELERATION));

        long timestampRange[] = completeTimestampRangeInDataSet(allQueus);

        for (TimestampQueue<Reading> queue: allQueus)
            allData.addQueue(queue);

        Log.d(TAG, "Sorting all data...");
        allData.sort();
        allData.trimInPlace(timestampRange[0], timestampRange[1]);
        Log.d(TAG, "Feeding data...");

        feed(allData);
    }

    private void readAndLoad(String name, Context context, Reading.Type type) {
        Log.d("LocalReader", "Starting load for " + name);
        long maxTime = 400000;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
            Reading lastReading = null;

            String line = reader.readLine();
            while (line != null) {
                Reading reading = new Reading(line, type);

                if (reading.timestamp > maxTime)
                    break;

                //exclude duplicates
                if (lastReading != null && reading.timestamp != lastReading.timestamp)
                    recorder.newReading(reading);

                lastReading = reading;
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading file\n");
            e.printStackTrace();
        }
    }

    /* Read and Feed method */
    /**
     * Does not push the data to the receiver right away, returns a list of loaded readings
     */
    private TimestampQueue<Reading> read(String name, Context context, Reading.Type type) {
        Log.d("LocalReader", "Starting load for " + name);
        String line;
        TimestampQueue<Reading> result = new TimestampQueue<Reading>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
            Reading lastReading = null;

            line = reader.readLine();
            while (line != null) {
                Reading reading = new Reading(line, type);

                if (reading.timestamp > maxTime)
                    break;

                //exclude duplicates
                if (lastReading != null && reading.timestamp != lastReading.timestamp)
                    result.push(reading);

                lastReading = reading;
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading file\n");
            e.printStackTrace();
        }

        return result;
    }

    private void feed(TimestampQueue<Reading> data) {
        for (Reading reading: data)
            recorder.newReading(reading);
    }


    /* Orphaned Code */
    private static void readFile(String name, Context context, Reading.Type type) {
//        lock = false;
//
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
//            ArrayList<ReadingHolder> readings = new ArrayList<ReadingHolder>();
//            Reading lastReading = null;
//
//            int counter = 0;
//            int maxItems = 100000;
//
//            String line = reader.readLine();
//            while (line != null) {
//                Reading reading = new Reading(line, type);
//                ReadingHolder holder = new ReadingHolder(reading);
//
//                //exclude duplicates
//                if (lastReading != null && reading.timestamp != lastReading.timestamp)
//                    readings.add(holder);
//
//                lastReading = reading;
//                line = reader.readLine();
//
//                counter += 1;
//
//                if (counter >= maxItems && lock == false) {
//                    lock = true;
//                    new SaveAsyncTask() {
//                        @Override
//                        protected void onPostExecute(Boolean aBoolean) {
//                            lock = false;
//                        }
//                    }.execute(readings);
//                    counter = 0;
//                    ReadingHolder.saveInTx(readings);
//                    readings = new ArrayList<ReadingHolder>();
//                }
//            }
//
//            reader.close();
//        } catch (IOException e) {
//            System.out.println("Error reading file\n");
//            e.printStackTrace();
//        }
    }

}