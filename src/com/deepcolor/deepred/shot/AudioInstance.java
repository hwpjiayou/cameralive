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

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

/**
 * ¼����Ƶ��Ҫ�ĺ��� �� ��
 * @author WSH
 *
 */
public class AudioInstance implements Runnable
{
	/**
	 * ��ʼ���ɵ�����
	 */
	private static AudioInstance audioInstance = null;
	
	public static synchronized AudioInstance getInstance()
	{
		if(null == audioInstance)
		{
			audioInstance = new AudioInstance();
		}
		
		return audioInstance;
	}
	
	private AudioInstance(){}
	
	/**
	 * -----------------------------------------------------------------------
	 */
	private static final int SAMPLE_RATE    = 44100; //  ������
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;	//������
	private static final int AUDIO_FORMAT   = AudioFormat.ENCODING_PCM_16BIT;	//����Ϊ16bit
	private static final int AUDIO_SOURCE   = AudioSource.MIC;	//��Ƶ��Դ
	
	
	private AudioRecord audioRecord = null;	//¼����
	
	private byte[] sampleBuffers = null;	//�洢ÿ�β���������
	private int minBufferSize    = -1;		//��С�Ĳ��������С
	
	private boolean isRecording = false;	//��¼�Ƿ�����¼����Ƶ
	
	private RecordCallback recordCallback = null;	//������ʱ�ص�
	
	/**
	 * ������Ƶ�ص�
	 * @param r
	 */
	public void setRecorderCallback(RecordCallback r)
	{
		recordCallback = r;
	}
	
	/**
	 * ��ʼ¼����Ƶ
	 */
	public void startRecording()
	{
		isRecording = true;
		
		/**
		 * �����߳̽�����Ƶ¼��
		 */
		new Thread(this).start();
	}
	
	
	/**
	 * ����¼����Ƶ
	 */
	public void stopRecording()
	{
		isRecording = false;
	}
	
	@Override
	public void run() 
	{
		/**
		 * �õ���С�Ļ�������С
		 */
		minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
		
		if(0 >= minBufferSize)	return;
		
		/**
		 * ��ʼ����Ƶ¼����
		 */
		audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
		
		if(null == audioRecord)	return;
		
		/**
		 * ��ʼ¼����Ƶ
		 */
		try 
		{
			audioRecord.startRecording();
		} 
		catch (IllegalStateException e) 
		{
			return;
		}
		
		/**
		 * ��ʼ��������
		 */
		sampleBuffers = new byte[minBufferSize];
		
		while(isRecording)
		{
			int result = audioRecord.read(sampleBuffers, 0, minBufferSize);
			
			if(0 < result && null != recordCallback)
			{
				recordCallback.onRecordFrame(sampleBuffers, Math.min(result, minBufferSize));
			}
		}
		
		/**
		 * ����¼��
		 */
		audioRecord.release();
		audioRecord = null;
		sampleBuffers = null;
		minBufferSize = -1;
	}
	





	/**
	 * -----------------------------------------------------------------------
	 * �Զ���һ���ص� ��������ʱ����
	 * -------------------------------------------------------------------------
	 */
	interface RecordCallback
	{
		/**
		 * ��������ʱ����
		 * @param data
		 */
		public void onRecordFrame(byte[] data, int len);
	}
}




















