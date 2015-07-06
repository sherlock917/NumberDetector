package cn.slzhong.numberdetector;

import android.graphics.Bitmap;

import java.util.HashMap;
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

    public double asert(String value) {
        int correct = 0;
        int total = value.length();

        if (result == null || result.length() != value.length()) {
            return 0.0;
        }

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == result.charAt(i)) {
                correct++;
            }
        }
        return (double) correct / total;
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
            int digit = detectDigit(matrixList.get(i));
            result += digit > -1 ? digit : "*";
        }
    }

    private int detectDigit(int[][] matrix) {
        int size = 50;
        int[][] square = squarify(matrix);
        int[][] scaled = scale(square, size);

        HashMap<String, Integer> loop = countLoop(scaled);
        int count = loop.get("count");
        int start = (loop.get("start") == null) ? 0 : loop.get("start");
        int end = (loop.get("end") == null) ? 0 : loop.get("end");

        if (count == 2) {
            return 8;
        }

        if (count == 1) {
            int span = (start > 0 && end > 0) ? end - start : 0;
            if (span >= scaled.length * 2 / 3) {
                return 0;
            }

            if (start < scaled.length / 3 && end < scaled.length * 2 / 3) {
                return 9;
            }

            if (Math.abs(scaled.length / 2 - start) <= scaled.length / 3) {
                return 6;
            }
        }

        if (count == 0) {
            if (matrix.length > 3 * matrix[0].length) {
                return 1;
            }

            HashMap<String, Integer> hor = countHorizontalLine(scaled);
            int horCount = hor.get("count");
            int horPos = hor.get("pos");

            if (horCount == 1) {
                if (horPos < scaled.length / 5) {
                    return 7;
                }
            }
        }

        return -1;
    }

    private int[][] squarify(int[][] matrix) {
        int size = (matrix[0].length > matrix.length) ? matrix[0].length : matrix.length;
        int newLeft = (size - matrix[0].length) / 2;
        int newTop = (size - matrix.length) / 2;
        int[][] newMatrix = new int[size][size];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                newMatrix[newTop + i][newLeft + j] = matrix[i][j];
            }
        }
        return newMatrix;
    }

    private int[][] scale(int[][] matrix, int size) {
        int span = matrix.length / size;
        int x = 0, y = 0;
        int[][] scaled = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                scaled[i][j] = matrix[y][x];
                x += span;
            }
            x = 0;
            y += span;
        }
        return scaled;
    }

    private HashMap<String, Integer> countLoop(int[][] matrix) {
        HashMap<String, Integer> result = new HashMap<>();
        int count = 0;
        boolean same = false;
        boolean loopStart = false;
        for (int i = 0; i < matrix.length; i++) {
            int line = 0;
            boolean detected = false;
            int start = -1;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 1 && !detected) {
                    line++;
                    detected = true;
                    if (start == -1) {
                        start = j;
                    }
                } else if (matrix[i][j] == 0 && detected) {
                    detected = false;
                }
            }
            if (line == 1) {
                loopStart = true;
            }
            if (loopStart) {
                if (line > 1 && !same) {
                    result.put("start", i);
                    if (!same) {
                        count++;
                        same = true;
                    }
                } else {
                    if (same) {
                        result.put("end", i);
                    }
                    if (line <= 1) {
                        same = false;
                    }
                }
            }
        }
        result.put("count", count);
        return result;
    }

    private HashMap<String, Integer> countHorizontalLine(int[][] matrix) {
        HashMap<String, Integer> result = new HashMap<>();
        result.put("count", 0);
        result.put("pos", 0);
        int count = 0;
        boolean same = false;
        int lastStart = 0, lastEnd = 0;
        for (int i = 0; i < matrix.length; i++) {
            int start = 0, end = 0;
            boolean isLine = false;
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == 1) {
                    if (!isLine) {
                        start = j;
                    }
                    end = j;
                    isLine = true;
                }
                if (matrix[i][j] == 0 && isLine) {
                    isLine = false;
                }
            }
            int span = end - start;

            if (span > matrix[i].length / 2 && !same) {
                count++;
                result.put("pos", i);
                same = true;
            } else
                same = Math.abs(start - lastStart) < matrix.length / 10 && Math.abs(end - lastEnd) < matrix.length / 10;
            lastStart = start;
            lastEnd = end;
        }
        result.put("count", count);

        return result;
    }

    private void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j]);
            }
            System.out.print("\n");
        }
    }

}
