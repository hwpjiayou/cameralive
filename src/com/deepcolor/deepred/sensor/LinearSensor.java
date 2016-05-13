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

package com.deepcolor.deepred.sensor;

import com.deepcolor.deepred.shot.CameraInstance;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * ���м��� �ж��Ƿ�����Զ��Խ�
 * @author WSH
 *
 */
public class LinearSensor implements SensorEventListener
{
	/**
	 * ���ٵ���С����ֵ
	 */
	private static final float MAX_ACCELERATION_THRESHOLD = 4.0f;
	private static final float MIN_ACCELERATION_THRESHOLD = 1.0f;
	
	private SensorManager sensorManager;
	private Sensor sensor;
	private boolean autoFocus = false;

	public LinearSensor(Activity activity)
	{
		/**
		 * �õ�������
		 */
		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		
		/**
		 * ע�ᴫ����
		 */
		sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * ��ֵ�仯ʱ
	 */
	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		/**
		 * �������Լ��ټ� ֱ�ӷ���
		 */
		if(Sensor.TYPE_LINEAR_ACCELERATION != event.sensor.getType())
			return;
		
		/**
		 * �õ���������ļ���ֵ
		 */
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		/**
		 * �õ����ٶ�
		 */
		float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
		
		//---------------------------------------------
		//System.out.println(acceleration);
		
		if(acceleration > MAX_ACCELERATION_THRESHOLD)
		{
			autoFocus = true;
		}
		else if(acceleration < MIN_ACCELERATION_THRESHOLD && autoFocus)
		{
			autoFocus = false;
			
			/**
			 * �����Զ��Խ�
			 */
			CameraInstance.getInstance().autoFocus(null, null);
		}
	}

	/**
	 * ���ȱ仯ʱ ����
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		
	}
}




















