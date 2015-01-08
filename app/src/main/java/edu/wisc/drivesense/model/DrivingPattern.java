package edu.wisc.drivesense.model;

import java.util.ArrayList;
import java.util.List;

import edu.wisc.drivesense.scoring.neural.modelObjects.TimestampSortable;


/**
 * Used internally, not uploaded or saved!
 */
public class DrivingPattern extends TimestampSortable{
    public MappableEvent.Type type;

    public long start = -1;
    public long end = -1;

    public double score = -1.0;
    public int start_index = -1;
    public int end_index = -1;


    public DrivingPattern() { }

    //merge overlapping intervals into one interval. Score of the merged interval will be average of scores of the overlapping intervals
    public static ArrayList<DrivingPattern> reduceOverlapIntervals(ArrayList<DrivingPattern> intervals) {
        ArrayList<DrivingPattern> res = new ArrayList<DrivingPattern>();
        int sz = intervals.size();
        if (0 == sz) return intervals;

        DrivingPattern last = intervals.get(0);
        double avg = last.score;
        int count = 1;

        for (int i = 1; i < sz; ++i) {
            DrivingPattern cur = intervals.get(i);
            if (cur.start <= last.end) {
                last.end = cur.end;
                avg = avg + cur.score;
                count++;
            } else {
                avg = avg / count;
                last.score = avg;
                res.add(last);

                last = cur;
                avg = last.score;
                count = 1;
            }
        }
        res.add(last);
        return res;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current Pattern: \n ");
        sb.append("type:" + type).append("\n");
        sb.append("\tscore:").append(score).append("\n");
        return sb.toString();
    }
    public long getTime() { return start; }

//    public void setTimeInterval(long s, long e) {
//        start = s;
//        end = e;
//    }
//
//    public void getPattern(String line) {
//        String[] res = line.split(Constants.kInputSeperator);
//        end = Long.parseLong(res[0]);
//        start = end - 30 * 1000;
//
//        score = Double.parseDouble(res[1]);
//        type = res[2];
//    }

}
