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
    double baseTime = System.currentTimeMillis();
    long maxTime = 200000;

    private TripRecorder recorder;
    private Context context;

    public LocalDataTester(TripRecorder analyst, Context context) {
        this.context = context;
        this.recorder = analyst;
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
//        readAndLoad("gps.txt", context, Reading.Type.gps);
//        readAndLoad("acceleration.txt", context, Reading.Type.acceleration);

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

        Log.v(TAG, "Timestamp range for loaded data: " + timestampRange[0] + ":" + timestampRange[1]);

        Log.d(TAG, "Sorting all data...");
        allData.sort();
        Log.v(TAG, "Size of queue after sort: " + allData.size());
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

        int counter = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
            Reading lastReading = null;

            line = reader.readLine();
            while (line != null) {
                Reading reading = new Reading(line, type);
                counter++;

                if (reading.timestamp > maxTime)
                    break;

                reading.timestamp += baseTime;

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
        Log.v(TAG, "Loaded " + counter + " lines");
        return result;
    }

    private void feed(TimestampQueue<Reading> data) {
        long lastLoad = data.peek().getTime();
        int feedCounter = 0;

        for (Reading reading: data) {
            if ((reading.getTime() - lastLoad) > recorder.period) {
                Log.d(TAG, "Fed " + feedCounter + " readings. Current time " + reading.getTime() + " last load: " + lastLoad);

                recorder.analyzePeriod();
                lastLoad = reading.getTime();
                feedCounter = 0;
            }

            recorder.newReading(reading);
            feedCounter++;
        }

        //analyze the last period near the end of the trip. May break things.
        recorder.analyzePeriod();
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