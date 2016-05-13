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

package com.deepcolor.deepred.view;

import java.util.HashMap;

import com.deepcolor.deepred.R;
import com.deepcolor.deepred.shot.CameraInstance;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * ��ʾ����Ƶ�״̬ ��������״̬ �� �Զ� �� �ر� �����е�����ͷ��֧��
 * @author WSH
 *
 */
public class FlashView extends ImageView
{
	/**
	 * �洢flash��Ӧ��ģʽ �� ��Դid
	 */
	private static HashMap<String, Integer> FLASHMODES = new HashMap<String, Integer>();
	static
	{
		FLASHMODES.put(Parameters.FLASH_MODE_OFF, R.drawable.flash_off);
		FLASHMODES.put(Parameters.FLASH_MODE_AUTO, R.drawable.flash_auto);
		FLASHMODES.put(Parameters.FLASH_MODE_ON, R.drawable.flash_on);
	}
	
	public FlashView(Context context) 
	{
		super(context);
	}

	public FlashView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}

	public FlashView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	/**
	 * ���������ģʽ �����Ƿ�ɼ�
	 */
	public void setVisibleByFlashMode()
	{
		if(CameraInstance.getInstance().supportFlashMode())
		{
			this.setVisibility(View.VISIBLE);
			
			String flashMode = CameraInstance.getInstance().getCurFlashMode();
			
			if(null != flashMode && FLASHMODES.containsKey(flashMode))
			{
				this.setImageResource(FLASHMODES.get(flashMode));
			}
			else
			{
				this.setVisibility(View.INVISIBLE);
			}
		}
		else
			this.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * ������� ����л�ʱ����� �п��ܲ�����ui�߳����л���
	 */
	public void finishSwitchFlashMode(String flashMode)
	{
		Message msg = new Message();
		msg.what = FINISH_SWITCH_FLAH_MODE_MSG;
		msg.obj  = flashMode;
		
		handler.sendMessage(msg);
	}
	
	/**
	 * ���������ͷ��ʱ����� �п��ܲ���ui�߳��е���
	 */
	public void finishSwitchCamera()
	{
		handler.sendEmptyMessage(FINISH_SWITCH_CAMERA_MSG);
	}
	
	/**
	 * -----------------------------------------------------------------
	 */
	private static final int FINISH_SWITCH_FLAH_MODE_MSG = 0;	//��������ģʽ�л���Ϣ
	private static final int FINISH_SWITCH_CAMERA_MSG = 1;	//�������ͷ�л���Ϣ
	
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
			case FINISH_SWITCH_FLAH_MODE_MSG:
				/**
				 * �л������
				 */
				String flashMode = (String)msg.obj;
				
				if(null != flashMode && FLASHMODES.containsKey(flashMode))
				{
					FlashView.this.setImageResource(FLASHMODES.get(flashMode));
					FlashView.this.setVisibility(View.VISIBLE);
				}
				else
				{
					FlashView.this.setVisibility(View.INVISIBLE);
				}
				
		        break;
		 
			case FINISH_SWITCH_CAMERA_MSG:
				/**
				 * �Ƿ�ɼ�
				 */
				setVisibleByFlashMode();
				
				break;
			}
		}
	};

	
	/**
	 * ---------------------------------------------
	 * ��Ӧ����
	 */
	
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		if(MotionEvent.ACTION_DOWN == event.getAction())
		{
			/**
			 * �л������ģʽ
			 */
			CameraInstance.getInstance().switchFlashMode();
		}
		return super.onTouchEvent(event);
	}
	
	
	
	
}


















