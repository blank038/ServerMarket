package com.blank038.servermarket.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Blank038
 */
public class CommonUtil {

    /**
     * 将 String 转换为槽位数组
     *
     * @param text 目标文本
     * @return 槽位数组
     */
    public static Integer[] formatSlots(String text) {
        if (text.contains(",")) {
            List<Integer> list = new ArrayList<>();
            for (String s : text.split(",")) {
                list.addAll(Arrays.asList(formatSlots(s)));
            }
            return list.toArray(new Integer[0]);
        } else if (text.contains("-")) {
            String[] split = text.split("-");
            int n1 = Integer.parseInt(split[0]), n2 = Integer.parseInt(split[1]);
            int min = Math.min(n1, n2), max = Math.max(n1, n2);
            Integer[] result = new Integer[max - min + 1];
            for (int index = 0, temp = min; temp <= max; temp++, index++) {
                result[index] = temp;
            }
            return result;
        } else {
            return new Integer[]{Integer.parseInt(text)};
        }
    }

    /**
     * 对文件写入内容
     *
     * @param in   输入流
     * @param file 文件对象
     */
    public static void outputFile(InputStream in, File file) {
        if (in == null) {
            return;
        }
        file.getParentFile().mkdir();
        try {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int length;
            while ((length = in.read(b)) != -1) {
                out.write(b, 0, length);
            }
            out.close();
            in.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
