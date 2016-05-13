/**
* ���� ������  ����
* ���������ߩ��������ߩ�
* ������������������
* ���������� ��������
* ���������ש������ס���
* ���������������� ��
* ���������� �ߡ��� ��
* ���������������� ��
* ����������������������
* ����������������
* ����������������
* ������������������������
* ������������������ �ǩ�
* ������������������ ����
* �����������������ש�����
* ������ ���ϩϡ����ϩ�
* ������ ���ߩ������ߩ�
*
*-----���ޱ��ӣ�Զ��bug-----
*/

package com.deepcolor.deepred.util;

import java.io.File;

import android.os.Environment;

public class FileUtil 
{
	/** ��ȡsdcard·�� */
	public static String getExternalStorageDirectory() 
	{
		String path = Environment.getExternalStorageDirectory().getPath();
		
		if (DeviceUtil.isZte()) 
		{
			path = path.replace("/sdcard", "/sdcard-ext");
		}
		return path;
	}
	
	private static final String APP_PATH = "DeepRed";
	
	/**
	 * �õ� Ӧ�õĸ�Ŀ¼
	 * @return
	 */
	public static String getAppPath()
	{
		String appPath =  getExternalStorageDirectory() + File.separator + APP_PATH;
		
		isDirExitAndCreate(appPath);
		
		return appPath;
	}
	
	/**
	 * �жϵ�ǰĿ¼�Ƿ���� �����ھʹ���һ�� ���ھ�ɾ���ٴ���һ��Ŀ¼
	 * @param path
	 */
	public static void isDirExitDeleteCreate(String path)
	{
		File file = new File(path);
		
		//����ļ�������ΪĿ¼ ��ֱ��ɾ��
		if(file.exists() && file.isDirectory())
		{
			//ɾ��Ŀ¼ �� һ�� ���е��ļ�
			deleteDir(path);
		}
		
		//����һ��Ŀ¼
		file.mkdir();
	}
	
	/*
	 * �ж�Ŀ¼�Ƿ���� �������ֱ�ӷ��� ��������ڴ���һ��Ŀ¼
	 */
	public static void isDirExitAndCreate(String path)
	{
		File file = new File(path);
		
		//��������� ���� ����һ��Ŀ¼ �ʹ���һ��
		if(!file.exists() || (file.exists() && !file.isDirectory()))
		{
			file.mkdir();
		}
	}
	
	/**
	 * ɾ����Ӧ���ļ�����Ŀ¼ �����Ŀ¼��Ŀ¼�ڵ��ļ�ͬ��ɾ��
	 * @param path
	 */
	public static void deleteDir(String path)
	{
		File file = new File(path);
		
		//����Ŀ¼ֱ��ɾ��
		if(!file.isDirectory())
		{
			file.delete();
		}
		else if(file.isDirectory())
		{
			//��Ŀ¼�ݹ�ɾ��
			String[] filelist = file.list();
			for(int i = 0; i < filelist.length; ++i)
			{
				//��Ӧ���ļ�
				File delfile = new File(path + File.separator + filelist[i]);
				
				if(!delfile.isDirectory())
					delfile.delete();
				else if(delfile.isDirectory())
					deleteDir(path + File.separator + filelist[i]);
			}
			
			file.delete();
		}
	}
}
