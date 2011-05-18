package com.danielpecos.gtm.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class FileUtils {
	public static boolean StoreByteImage(Context mContext, byte[] imageData, int quality, String nameFile) {

		File sdImageMainDirectory = new File("/sdcard/myImages");
		FileOutputStream fileOutputStream = null;
		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;

			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length,options);

			fileOutputStream = new FileOutputStream(sdImageMainDirectory.toString() +"/" + nameFile + ".jpg");

			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}
