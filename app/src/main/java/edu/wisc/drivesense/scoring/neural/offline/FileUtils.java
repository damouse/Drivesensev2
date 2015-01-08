package edu.wisc.drivesense.scoring.neural.offline;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.wisc.drivesense.model.Reading;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampQueue;
import edu.wisc.drivesense.scoring.neural.modelObjects.TrainingSet;

import static edu.wisc.drivesense.scoring.neural.offline.OfflineWrapper.log;


/**
 * Created by Damouse on 12/13/2014.
 */
public class FileUtils {

    /* Loading */

    /**
     * Load the data file at the given path
     */
    public static TimestampQueue loadDataFile(String path, Reading.Type type) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            return readFile(br, type);
        } catch (Exception e) {
            System.out.println("Error reading file\n");
            e.printStackTrace();
        }

        return null;
    }

    private static TimestampQueue readFile(BufferedReader reader, Reading.Type type) throws IOException {
        TimestampQueue ret = new TimestampQueue();
        Reading lastReading = null;

        String line = reader.readLine();
        while (line != null) {
            Reading reading = new Reading(line, type);

            //exclude duplicates
            if (lastReading != null && reading.timestamp != lastReading.timestamp)
                ret.push(reading);

            lastReading = reading;
            line = reader.readLine();
        }

        reader.close();
        return ret;
    }

    /**
     * Loads all of the data in the given directory
     */
    public static TrainingSet loadDataAtPath(String path) {
        TrainingSet allData = new TrainingSet();

        allData.magnet = loadDataFile(path + "magnet.txt", Reading.Type.MAGNETIC);
        allData.gyroscope = loadDataFile(path + "gyro.txt", Reading.Type.GYROSCOPE);
        allData.gravity = loadDataFile(path + "gravity.txt", Reading.Type.GRAVITY);
        allData.gps = loadDataFile(path + "gps.txt", Reading.Type.GPS);
        allData.labels = loadDataFile(path + "labels.txt", Reading.Type.LABEL);
        allData.acceleration = loadDataFile(path + "acceleration.txt", Reading.Type.ACCELERATION);

        return allData;
    }

    /* Saving */
    public static void save(String path, String contents) {
        try {
            File file = new File(path);

            if (file.exists())
                file.delete();

            file.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.write(contents);

            writer.close();
        } catch (Exception e) {
            log("Can't write file.");
            e.printStackTrace();
        }
    }
}
