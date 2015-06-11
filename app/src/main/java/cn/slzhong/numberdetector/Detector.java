package cn.slzhong.numberdetector;

import android.graphics.Bitmap;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by SherlockZhong on 6/11/15.
 */
public class Detector {

    private Bitmap bitmap;
    private List<Bitmap> bitmapList;
    private List<int[][]> matrixList;

    private String result = "";

    public Detector(Bitmap b) {
        bitmap = b;
        bitmap = Processor.grayProcess(bitmap);
        bitmap = Processor.equalization(bitmap);
        bitmap = Processor.binarize(bitmap);
        bitmap = Processor.clip(bitmap);
        bitmapList = Processor.split(bitmap);

        matrixify();
        detect();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public List<Bitmap> getBitmapList() {
        return bitmapList;
    }

    public String getResult() {
        return result;
    }

    private void matrixify() {
        if (matrixList == null) {
            matrixList = new LinkedList<>();
        }

        for (int i = 0; i < bitmapList.size(); i++) {
            matrixList.add(Processor.bitmapToMatrix(bitmapList.get(i)));
        }
    }

    private void detect() {
        for (int i = 0; i < matrixList.size(); i++) {
            result += detectDigit(matrixList.get(i));
        }
    }

    private int detectDigit(int[][] matrix) {
        double ratio = (double) matrix[0].length / matrix.length;
        System.out.println("*****" + ratio);
        if (ratio < 1.0 / 3.0) {
            return 1;
        } else {
            return 0;
        }
    }

}
