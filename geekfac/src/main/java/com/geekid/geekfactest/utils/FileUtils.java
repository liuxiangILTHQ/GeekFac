package com.geekid.geekfactest.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class FileUtils
{
	/**
	 * 复制文件
	 * @param sourceFile
	 * @param targetFile
	 * @return 返回值为0表示成功
	 */
	public static int copyFile(File sourceFile, File targetFile)
	{
		try
		{
			// 新建文件输入流并对它进行缓冲
			FileInputStream input = new FileInputStream(sourceFile);
			BufferedInputStream inBuff = new BufferedInputStream(input);

			// 新建文件输出流并对它进行缓冲
			FileOutputStream output = new FileOutputStream(targetFile);
			BufferedOutputStream outBuff = new BufferedOutputStream(output);

			// 缓冲数组
			byte[] b = new byte[1024];
			int len;
			while ((len = inBuff.read(b)) != -1)
			{
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();

			// 关闭流
			inBuff.close();
			outBuff.close();
			output.close();
			input.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/**
	 *  复制sourceDir文件夹里的内容到targetDir文件夹中
	 * @param sourceDir
	 * @param targetDir
	 * @return 返回值为0表示成功
	 */
	public static int copyDirectiory(String sourceDir, String targetDir)
	{
		// 新建目标目录
		new File(targetDir).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++)
		{
			if (file[i].isFile())
			{
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(
						new File(targetDir).getAbsolutePath() + File.separator
								+ file[i].getName());
				int result = copyFile(sourceFile, targetFile);
				if (result == -1)
				{
					return -1;
				}
			}
			if (file[i].isDirectory())
			{
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
		return 0;
	}

	/**
	 * 删除文件或文件夹
	 * 
	 * @param file
	 */
	public static void delete(File file)
	{
		if (file.isFile())
		{
			file.delete();
			return;
		}

		if (file.isDirectory())
		{
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0)
			{
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++)
			{
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	/**
	 * 以行为单位写文件
	 * @param file
	 * @param str
	 */
	public static void writeFileByLine(File file, String str)
	{
		BufferedWriter bw = null;
		try
		{
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(str);
			bw.newLine();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 * 
	 * @param file
	 */
	public static List<String> readFileByLines(File file)
	{
		List<String> listString = new ArrayList<String>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			int line = 0;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null)
			{
				// 显示行号
				// System.out.println("line " + line + ": " + tempString);
				listString.add(tempString.trim());
				line++;
			}
			reader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (IOException e1)
				{
				}
			}
		}
		return listString;
	}

	public static String readStringFromFile(String pathname)
	{
		String msg = null;
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try
		{
			fileReader = new FileReader(pathname);
			bufferedReader = new BufferedReader(fileReader);
			msg = bufferedReader.readLine();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			bufferedReader.close();
			fileReader.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return msg;
	}

	public static void clearFileContent(File file)
	{
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try
		{
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write("");
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void appandStringToFile2(String filename,String msg){
		FileReader fileReader=null;
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			fileReader=new FileReader(filename);
		} catch (FileNotFoundException e) {
			try {
				fileWriter = new FileWriter(filename, true);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
		bufferedWriter = new BufferedWriter(fileWriter);
		try {
			bufferedWriter.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void appandStringToFile(String pathname, String msg,
			boolean appand)
	{
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try
		{
			fileWriter = new FileWriter(pathname, appand);
			bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(msg);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			bufferedWriter.close();
			fileWriter.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static Object readObjectFromFile(Context context, String filename)
	{
		ObjectInputStream ois = null;
		try
		{
			FileInputStream fis = context.openFileInput(filename);
			try
			{
				ois = new ObjectInputStream(fis);
				try
				{
					return ois.readObject();

				} catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			} catch (StreamCorruptedException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void writeObjectToFile(Context context, String filename,
			Object object) throws Exception
	{
		ObjectOutputStream outputStream = null;

		try
		{
			// Construct the LineNumberReader object
			// FileInputStream file=openFileInput("program");
			FileOutputStream fos = context.openFileOutput(filename,
					Context.MODE_PRIVATE);
			outputStream = new ObjectOutputStream(fos);

			outputStream.writeObject(object);

		} catch (FileNotFoundException ex)
		{
			ex.printStackTrace();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		} finally
		{
			try
			{
				if (outputStream != null)
				{
					outputStream.flush();
					outputStream.close();
				}
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}

	}
	

}
