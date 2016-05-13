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

import com.deepcolor.deepred.util.ColorUtil;

import android.R.integer;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

/**
 * �Ƿ�� live photo ���ܿ���
 * @author WSH
 *
 */
public class LiveCheckBox extends CheckBox
{
	private static final float START_ROTATE = 0f;	//��ת�Ŀ�ʼ�ͽ����Ƕ�
	private static final float END_ROTATE   = 360f;
	private static final int ROTATE_DURATION = 1200;	//��ת����ʱ��
	
	
	//��ʼ��һ����ת���� ����̨���б���ʱ ��ִ����ת����
	ObjectAnimator rorateAnimator = null;
	
	public LiveCheckBox(Context context) 
	{
		super(context);
		
		init();
	}

	public LiveCheckBox(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		init();
	}

	public LiveCheckBox(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		
		init();
	}
	
	private void init()
	{
		rorateAnimator = ObjectAnimator.ofFloat(this, "rotation", START_ROTATE, END_ROTATE); 
		rorateAnimator.setDuration(ROTATE_DURATION);
		rorateAnimator.setRepeatMode(ValueAnimator.INFINITE);	//��Ϊ�����ظ�����ת
		rorateAnimator.setRepeatCount(-1);
	}
	
	/**
	 * ------------------------------------------
	 * ��һ����־Ϊ��ʾ��ǰ���ڱ���ĵ�livephoto�ĸ���
	 */
	private int encodingNum = 0;
	
	/**
	 * ��ʼ����
	 */
	public void startEncode()
	{
		handler.sendEmptyMessage(START_ENCODE_MSG);
	}
	
	/**
	 * ��ɱ���
	 */
	public void finishEncode()
	{
		handler.sendEmptyMessage(FINISH_ENCODE_MSG);
	}
	
	/**
	 * ����ʧ��
	 */
	public void failEncode()
	{
		handler.sendEmptyMessage(FAIL_ENCODE_MSG);
	}
	
	/**
	 * ----------------------------------------------------------------------------------
	 * ����Ļص�������ui�߳��� ��Ҫʹ��handle����
	 * --------------------------------------------------------------------------------------
	 */
	private static final int START_ENCODE_MSG     = 0;
	private static final int FINISH_ENCODE_MSG    = 1;
	private static final int FAIL_ENCODE_MSG 	  = 2;
	
	
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
			case START_ENCODE_MSG:
				//��һ��livephoto��ʼ����
				encodingNum++;
				
				/**
				 * �����ǰû�п�ʼ ���� ��ͣ ֱ�ӿ�ʼ����
				 */
				if(!rorateAnimator.isStarted() || !rorateAnimator.isRunning())
					rorateAnimator.start();
				
				break;
			case FINISH_ENCODE_MSG:	//һ���Ѿ���ɱ���
			case FAIL_ENCODE_MSG:	//����ʧ��
				
				encodingNum--;
				if(0 > encodingNum)
					encodingNum = 0;
		
				if(0 == encodingNum)
					rorateAnimator.end();
				
				break;
			}
		}
	};
}













