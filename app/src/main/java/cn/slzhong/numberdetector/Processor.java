package cn.slzhong.numberdetector;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by SherlockZhong on 6/11/15.
 */
public class Processor {

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return bytes;
    }

    public static int[][] bitmapToMatrix(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[][] matrix = new int[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = Color.red(bitmap.getPixel(i, j));
                matrix[j][i] = val == 0 ? 1 : 0;
            }
        }
        return matrix;
    }

    public static Bitmap grayProcess(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(bitmap);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r = Color.red(bitmap.getPixel(i, j));
                int g = Color.green(bitmap.getPixel(i, j));
                int b = Color.blue(bitmap.getPixel(i, j));
                int val = (int) (r * 0.3 + g * 0.59 + b * 0.11);
                result.setPixel(i, j, Color.rgb(val, val, val));
            }
        }
        return result;
    }

    public static Bitmap equalization(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(bitmap);

        int[] count = new int[256];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = Color.red(bitmap.getPixel(i, j));
                count[val]++;
            }
        }

        int[] lut = new int[256];
        int sum;
        sum = lut[0] = count[0];
        for (int i = 1; i < 256; i++) {
            sum += count[i];
            lut[i] = 255 * sum / width / height;
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = Color.red(bitmap.getPixel(i, j));
                result.setPixel(i, j, Color.rgb(lut[val], lut[val], lut[val]));
            }
        }

        return result;
    }

    public static Bitmap binarize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(bitmap);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = Color.red(bitmap.getPixel(i, j));
                if (val > 35) {
                    result.setPixel(i, j, Color.rgb(255, 255, 255));
                } else {
                    result.setPixel(i, j, Color.rgb(0, 0, 0));
                }
            }
        }
        return result;
    }

    public static Bitmap clip(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        HashMap<String, int[]> projection = project(bitmap);
        int[] x = projection.get("x");
        int[] y = projection.get("y");

        int left, right;
        left = right = -1;
        for (int i = 0; i < x.length; i++) {
            if (x[i] == 1) {
                left = left == -1 ? i : i > left ? left : i;
                right = i > right ? i : right;
            }
        }

        int top, bottom;
        top = bottom = -1;
        for (int i = 0; i < y.length; i++) {
            if (y[i] == 1) {
                top = top == -1 ? i : i > top ? top : i;
                bottom = i > bottom ? i : bottom;
            }
        }
        int w = right - left;
        int h = bottom - top;
        width = w > 0 ? w : width;
        height = h > 0 ? h : height;
        Bitmap result = Bitmap.createBitmap(bitmap, left, top, width, height);
        return result;
    }

    public static List<Bitmap> split(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        HashMap<String, int[]> projection = Processor.project(bitmap);
        int[] x = projection.get("x");
        int[] y = projection.get("y");

        List<int[]> list = new LinkedList<>();
        int start, end;
        start = end = 0;
        while (start < x.length && end < x.length) {
            if (x[start] == 0) {
                start++;
            } else {
                int[] point = new int[2];
                point[0] = start;
                end = start + 1;
                while (end < x.length && x[end] == 1) {
                    end++;
                }
                point[1] = end;
                list.add(point);
                start = end = end + 1;
            }
        }

        List<Bitmap> result = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            int[] point = list.get(i);
            Bitmap digit = Bitmap.createBitmap(bitmap, point[0], 0, point[1] - point[0], height);
            digit = clip(digit);
            result.add(digit);
        }
        return result;
    }

    public static HashMap<String, int[]> project(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int x[] = new int[width];
        int y[] = new int[height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = Color.red(bitmap.getPixel(i, j));
                if (val == 0) {
                    x[i] = 1;
                    y[j] = 1;
                }
            }
        }
        HashMap<String, int[]> hashMap = new HashMap<>();
        hashMap.put("x", x);
        hashMap.put("y", y);
        return hashMap;
    }

}
