package com.liu.airhockey.util;

import android.content.Context;
import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TextResourceReader {

    public static String readTextFileFromResource(Context context, int resourceId) {
        StringBuffer body = new StringBuffer();

        try {
            InputStream inputStream =
                    context.getResources().openRawResource(resourceId);

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("不能打开资源：" + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("资源未找到：" + resourceId, nfe);
        }

        return body.toString();
    }

}
