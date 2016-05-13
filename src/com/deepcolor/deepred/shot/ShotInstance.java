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

package com.deepcolor.deepred.shot;

import java.io.File;

import com.deepcolor.deepred.callback.EncodeCallback;
import com.deepcolor.deepred.util.FileUtil;
import com.deepcolor.deepred.view.FlashView;

import android.R.integer;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.View;

/**
 * ��Ӱ�� ���� ¼����Ƶ �� ����һ��ͼƬ�����еĹ�������Ĳ���
 * @author WSH
 *
 */
public class ShotInstance implements Camera.PreviewCallback, AudioInstance.RecordCallback
{
	/**
	 * �������ص�����
	 */
	private CameraInstance cameraInstance = null;	//���ʵ�� �������� ����Ĳ���
	private byte[] cameraBuffer = null;	//ÿһ֡��Ƶ���ݵĻ���
	private Size previewSize    = null;	//���Ԥ������Ĵ�С
	private float viewRatio     = -1;	//�ؼ� �� �Ӿ����� ���ָ��û��Ŀ����߶ȵı��� ��Ϊ�ֻ���Ļ�������Ԥ���ı����п��ܲ���ͬ
	
	/**
	 * ��¼����Ƶ��ص�����
	 */
	private AudioInstance audioInstance = null;	//��Ƶ¼����
		
	/**
	 * �Ƿ��livephoto
	 */
	boolean livePhotoChecked = true;	//�Ƿ���livephotoĬ�ϴ�
	
	public ShotInstance()
	{
		init();
	}
	
	/**
	 * ��ʼ����Ҫ�ĸ��ֲ���
	 */
	private void init()
	{
		/**
		 * ��ʼ������ͷԤ���ص�
		 */
		cameraInstance = CameraInstance.getInstance();
		initCameraParams();	//��ʼ������ͷ��صĲ���
		
		/**
		 * ��ʼ����Ƶ¼�ƺ���
		 */
		audioInstance  = AudioInstance.getInstance();
		audioInstance.setRecorderCallback(this);
		audioInstance.startRecording();	//��ʼ¼����Ƶ
	}
	
	/**
	 * ���ù�������Ͷ�Ĳ�����Ϣ
	 * ��һ�� �� ����ͷ�л���ʱ����Ҫ����
	 */
	public void initCameraParams()
	{
		/**
		 * �õ�Ԥ������Ĵ�С
		 */
		previewSize = cameraInstance.getPreviewSize();
		
		if(null == previewSize) return;
		
		/**
		 * ���û������ͻص�
		 */
		cameraBuffer = new byte[previewSize.width * previewSize.height * 3 / 2];
		cameraInstance.setPreviewCallback(this, cameraBuffer);
		
		/**
		 * Ԥ����С�ı���Ҫ��������c++��ĸ������Ų���
		 */
		jniSetPreviewSizeAndRatio(previewSize.width, previewSize.height, viewRatio);
		
		/**
		 * ��������е����� �� ������ǰ���еı������
		 */
		jniClearData();
		jniEndAllEncoder();
	}
	
	/**
	 * ���ÿؼ�����
	 * @param ratio
	 */
	public void setViewRatio(float ratio)
	{
		viewRatio = ratio;
		
		/**
		 * Ԥ����С�ı���Ҫ��������c++��ĸ������Ų���
		 */
		jniSetPreviewSizeAndRatio(previewSize.width, previewSize.height, viewRatio);
	}
	
	/**
	 * �Ƿ��� livephoto
	 * @param checked
	 */
	public void setLivePhotoChecked(boolean isChecked)
	{
		livePhotoChecked = isChecked;
	}	
	
	/**
	 * ִ���������
	 */
	public void takePicture()
	{
		/**
		 * -----------------------------------------
		 * ��������һ��ͼƬ �����ݱ������вü�
		 * -----------------------------------------
		 */
		
		//���ݵ�ǰʱ��õ���Ƶ��·�����ļ���
		long curTime = System.currentTimeMillis();
		String fileKey = String.valueOf(curTime);
		
		/**
		 * ����һ����Ƶ
		 */
		if(livePhotoChecked)
		{
			//�����߳̽��б���
			//new Thread(new LivePhotoRunnable(videoFilePath)).start();
			//��Ƶ·��
			String videoFilePath = FileUtil.getAppPath() + File.separator + fileKey + VIDEO_DIFF;
			
			/**
			 * �첽ִ�б������
			 */
			new LivePhotoAsyncTask(videoFilePath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
		}
	}
	
	/**
	 * ----------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------
	 * ----------------------------------------------------------------------------------------------------------------
	 */
	
	/**
	 * ����ص� ÿ�ν��б��붼���лص�����
	 */
	private EncodeCallback encodeCallback = null;
	public void setEncodeCallback(EncodeCallback e)
	{
		encodeCallback = e;
	}
	
	//��Ϣ�Ͷ�Ӧ���ӳ�ʱ��
	private static final int ADD_SECOND_PART_MSG = 0;	//��ӵڶ������ݵ���Ϣ
	private static final int ADD_SECOND_PART_MSG_DELAY_TIME = 1500;	//1500���� ���ӳ�ʱ��
	private static final String VIDEO_DIFF = ".mp4";	//�ļ��ĺ�׺��
	
	/**
	 * �����¼���Ϣ
	 */
	private Handler handler = new Handler()
	{
		@Override
		public void dispatchMessage(Message msg)
		{
			switch(msg.what)
			{
			case ADD_SECOND_PART_MSG:
				/*
				 * ��ӵڶ�������
				 */
				Integer encoderId = (Integer)msg.obj;
				if(null != encoderId)
				{
					//jniAddSecondPartToEncoder(encoderId.intValue());
					//new Thread(new AddSecondPartRunnable(encoderId.intValue())).start();
					//�첽��ӵڶ�������
					new AddSecondPartAsyncTask(encoderId.intValue()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 0);
				}
				
		        break;
			}
		}
	};
	
//	private class AddSecondPartRunnable implements Runnable
//	{
//		int encoderId = -1;
//		
//		AddSecondPartRunnable(int id)
//		{
//			encoderId = id;
//		}
//		
//		@Override
//		public void run() 
//		{
//			if(0 < encoderId)
//				jniAddSecondPartToEncoder(encoderId);
//		}
//		
//	}
	
	/**
	 * ��ӵڶ�������
	 * @author apple
	 *
	 */
	private class AddSecondPartAsyncTask extends AsyncTask<Integer, Integer, String>
	{
		int encoderId = -1;
		
		public AddSecondPartAsyncTask(int id)
		{
			encoderId = id;
		}

		@Override
		protected String doInBackground(Integer... params) 
		{
			if(0 < encoderId)
				jniAddSecondPartToEncoder(encoderId);

			return null;
		}
	}
	
	
	/**
	 * ʹ��AsyncTask���к�̨���� ��һ���̳߳� ���ٳ�ʼ���̵߳�ʱ�䵫�� ���ֻ��5�� ����ͬʱ����?
	 */
	private class LivePhotoAsyncTask extends AsyncTask<Integer, Integer, String>
	{
		/**
		 * ��Ƶ�����·�� һ��Ҫ��.mp4��β
		 */
		String filePath;
		
		public LivePhotoAsyncTask(String path)
		{
			filePath = path;
		}

		@Override
		protected String doInBackground(Integer... params) 
		{
			/**
			 * ------------------------------------
			 * ��¼ʱ��
			 */
			long time = System.currentTimeMillis();
			//
			
			/**
			 * ��ʼ�������
			 */
			if(null != encodeCallback)
				encodeCallback.startEncode();
			
			/**
			 * ��ʼ��һ�������� �����ر�������id
			 */
			int encoderId = jniInitEncoder(filePath);
			
			/**
			 * ��������ʼ��ʧ�� ֱ�ӷ���
			 */
			if(0 > encoderId)
			{
				//����ʧ��
				if(null != encodeCallback)
					encodeCallback.failEncode();
				
				return null;
			}
			
			/**
			 * ����һ���ӳ� ��Ϣ �����ڶ�������
			 */
			Message msg = new Message();
			msg.what = ADD_SECOND_PART_MSG;
			msg.obj  = new Integer(encoderId);
			
			handler.sendMessageDelayed(msg, ADD_SECOND_PART_MSG_DELAY_TIME);
			
			/**
			 * ��ʼ������� ��ʱ���� ����Ĺ����� ���յ��ڶ����ֵ�����
			 */
			int ret = jniStartEncoding(encoderId);
			
			if(0 > ret)
			{
				//����ʧ��
				if(null != encodeCallback)
					encodeCallback.failEncode();
				
				return null;
			}
			
			//����ɹ�
			if(null != encodeCallback)
				encodeCallback.finishEncode();
			
			
			/**
			 * ----------------------------------------------------------------------
			 */
			System.out.println("ʱ�䣺" + (System.currentTimeMillis() - time));
			
			return null;
		}
	}
	
	
	
//	/**
//	 * ���̵߳��� ����livephioto�ı���
//	 * @author apple
//	 *
//	 */
//	private class LivePhotoRunnable implements Runnable
//	{
//		/**
//		 * ��Ƶ�����·�� һ��Ҫ��.mp4��β
//		 */
//		String filePath;
//		
//		public LivePhotoRunnable(String path) 
//		{
//			filePath = path;
//		}
//		
//		@Override
//		public void run() 
//		{
//			/**
//			 * ------------------------------------
//			 */
//			long time = System.currentTimeMillis();
//			
//			
//			/**
//			 * ��ʼ�������
//			 */
//			if(null != encodeCallback)
//				encodeCallback.startEncode();
//			
//			/**
//			 * ��ʼ��һ�������� �����ر�������id
//			 */
//			int encoderId = jniInitEncoder(filePath);
//			
//			/**
//			 * ��������ʼ��ʧ�� ֱ�ӷ���
//			 */
//			if(0 > encoderId)
//			{
//				//����ʧ��
//				if(null != encodeCallback)
//					encodeCallback.failEncode();
//				
//				return;
//			}
//			
//			/**
//			 * ����һ���ӳ� ��Ϣ �����ڶ�������
//			 */
//			Message msg = new Message();
//			msg.what = ADD_SECOND_PART_MSG;
//			msg.obj  = new Integer(encoderId);
//			
//			handler.sendMessageDelayed(msg, ADD_SECOND_PART_MSG_DELAY_TIME);
//			
//			/**
//			 * ��ʼ������� ��ʱ���� ����Ĺ����� ���յ��ڶ����ֵ�����
//			 */
//			int ret = jniStartEncoding(encoderId);
//			
//			if(0 > ret)
//			{
//				//����ʧ��
//				if(null != encodeCallback)
//					encodeCallback.failEncode();
//				
//				return;
//			}
//			
//			//����ɹ�
//			if(null != encodeCallback)
//				encodeCallback.finishEncode();
//			
//			
//			/**
//			 * ----------------------------------------------------------------------
//			 */
//			System.out.println("ʱ�䣺" + (System.currentTimeMillis() - time));
//		}
//	}

	//long time;
	
	/**
	 * ���µ�����ͷ����ʱ ����
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) 
	{	
		//long time = System.currentTimeMillis();
		
		
		/**
		 * �����ݴ��뵽jni��
		 */
		jniOnReceiveVideoFrame(data, data.length, previewSize.width, previewSize.height, viewRatio, cameraInstance.getCameraFacing());
		
		/**
		 * ÿ�ζ�Ҫ���ã�
		 */
		cameraInstance.getCamera().addCallbackBuffer(cameraBuffer);
		
		/**
		 * ==================================================
		 * ʱ�����
		 */
		//long curTime = System.currentTimeMillis();
		
		//System.out.println("onPreviewFrame�����" + (curTime - time));
		//time = curTime;
	}
	
	/**
	 * �����µ���Ƶ����ʱ
	 */
	@Override
	public void onRecordFrame(byte[] data, int len) 
	{
		/**
		 * ����Ƶ���ݴ��뵽jni��
		 */
		jniOnReceiveAudioFrame(data, len);
	}
	
	/**
	 * ---------------------------------------------------------------
	 * jni����
	 * ------------------------------------------------------------------
	 */
	
	/**
	 * ��������ͷ��Ԥ����С�����ڵ�Ԥ����С
	 * @param width
	 * @param height
	 * @param ratio
	 * @return
	 */
	private native int jniSetPreviewSizeAndRatio(int width, int height, float ratio);
	
	
	/**
	 * ����Ƶ���ݴ��뵽 jni��
	 * @param data
	 * @param len
	 * @param width
	 * @param height
	 * @param ratio
	 * @param cameraFacing
	 */
	private native void jniOnReceiveVideoFrame(byte[] data, int len, int width, int height, float ratio, int cameraFacing);
	
	/**
	 * ����Ƶ���ݴ��䵽jni��
	 * @param data
	 * @param len
	 */
	private native void jniOnReceiveAudioFrame(byte[] data, int len);
	
	/**
	 * ��ʼ��һ�������� ���� ��������id
	 * @param path
	 * @return
	 */
	private native int jniInitEncoder(String path);
	
	/**
	 * ��ʼһ�������� �����������id
	 * @param id
	 * @return
	 */
	private native int jniStartEncoding(int id);
	
	/**
	 * ���ڶ����ֵ����ݿ����� ��Ӧ�ı�����
	 * @param id
	 * @return
	 */
	private native int jniAddSecondPartToEncoder(int id);
	
	/**
	 * �������еı����� 
	 */
	private native void jniEndAllEncoder();
	
	/**
	 * ������������
	 */
	private native void jniClearData();
	
	static
	{
		System.loadLibrary("livephoto");
	}
}








