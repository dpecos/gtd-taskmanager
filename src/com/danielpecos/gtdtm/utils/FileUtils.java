package com.danielpecos.gtdtm.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;

public class FileUtils {
	public static boolean storeByteImage(byte[] imageData, int quality, File file) {

		FileOutputStream fileOutputStream = null;
		try {

			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 5;

			Bitmap myImage = BitmapFactory.decodeByteArray(imageData, 0, imageData.length,options);

			fileOutputStream = new FileOutputStream(file);

			BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

			myImage.compress(CompressFormat.JPEG, quality, bos);

			bos.flush();
			bos.close();

		} catch(FileNotFoundException e)	{
			Log.e(TaskManager.TAG, "File not found: " + e.getMessage(), e);
		} catch(IOException ioe)	{
			Log.e(TaskManager.TAG, "Exception while writing the file " + ioe.getMessage(), ioe);
		}

		return true;
	}
	
	public static byte[] readByteImage(File file) {
		byte[] fileContent = null;
		
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(file);
			fileContent = new byte[(int)file.length()];
			fin.read(fileContent);
			fin.close();
		} catch(FileNotFoundException e)	{
			Log.e(TaskManager.TAG, "File not found: " + e.getMessage(), e);
		} catch(IOException ioe)	{
			Log.e(TaskManager.TAG, "Exception while reading the file " + ioe.getMessage(), ioe);
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
				}
			}
		}
		return fileContent;
	}
	
	public static String[] listFilesMatching(File root, String pattern) {
		return root.list(new WildcardFileFilter(pattern));
	}
}
