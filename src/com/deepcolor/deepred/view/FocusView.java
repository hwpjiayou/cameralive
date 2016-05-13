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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * ����ؼ� ��Ҫ����������Խ���ʱ�� ��ʾ����
 * @author WSH
 */
public class FocusView extends ImageView
{
	private static final float START_SCALE  = 1.0f;	//���Ų���
	private static final float END_SCALE    = 0.8f;	//���Ų���
	private static final int SCALE_DURATION = 150;	//���Ŷ���ʱ��
	private static final int COLOR_DURATION = 200;	//�ı���ɫ��ʱ��
	
	//��϶��� ���ڿؼ�������
	AnimatorSet animSet = new AnimatorSet(); 	

	public FocusView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		
		init();
	}

	public FocusView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		init();
	}

	public FocusView(Context context) 
	{
		super(context);
		
		init();
	}
	
	/**
	 * ��ʼ������
	 */
	private void init()
	{
		//x y�����ϵ�����
		ObjectAnimator xScaleAnimator = ObjectAnimator.ofFloat(this, "scaleX", START_SCALE, END_SCALE);
		ObjectAnimator yScaleAnimator = ObjectAnimator.ofFloat(this, "scaleY", START_SCALE, END_SCALE);
		
		//��϶���
		animSet = new AnimatorSet();
		animSet.play(xScaleAnimator).with(yScaleAnimator);
		animSet.setDuration(SCALE_DURATION);
	}

	/**
	 * ��ʼ�Խ����� ��ʼ�Խ��� �ؼ�������Ӧ��λ�� ���ҿ�ʼ���Ŷ���
	 * 1:���ÿؼ��ɼ�
	 * 2��������Ӧ��λ�� 
	 * 3����ʼ����
	 * 
	 * --------------------------------------------
	 * focusview��cameraview��ͬһ�������� ��� �����������������cameraview���ؼ������� 
	 * Ҳ���൱��focusview���ؼ�������
	 */
	public void startFocus(PointF touchPoint)
	{
		//��װһ����Ϣ
		Message msg = new Message();
		msg.what = START_FOCUS_MSG;
		msg.obj  = touchPoint;
		
		//ɾ����ʧ��Ϣ
		handler.removeMessages(FINISH_FOCUS_MSG);
		handler.removeMessages(DISAPPEAR_MSG);
		
		//������Ϣ
		handler.sendMessage(msg);
	}
	
	/**
	 * ��ɶԽ�֮��ĵ���
	 * ��ɶԽ�֮�� ���ؼ���Ϊ�趨����ɫ Ȼ��һ��ʱ�����ʧ
	 */
	public void finishFocus()
	{
		//������ɶԽ�����Ϣ �ı�ͼ�����ɫ
		handler.sendEmptyMessage(FINISH_FOCUS_MSG);

		//����һ���ӳ���Ϣ ���ؼ����ɼ�
		handler.sendEmptyMessageDelayed(DISAPPEAR_MSG, COLOR_DURATION);
	}
	
	/**
	 * �����Խ����� ���л�����ͷ��ʱ����Ҫֱ�ӽ�����ǰ�ĶԽ�����
	 */
	public void cancelFocus()
	{
		/**
		 * ɾ����Ϣ�����е�������Ϣ
		 */
		handler.removeMessages(START_FOCUS_MSG);
		handler.removeMessages(FINISH_FOCUS_MSG);
		handler.removeMessages(DISAPPEAR_MSG);
		
		/**
		 * ����ȡ���Խ���Ϣ
		 */
		handler.sendEmptyMessage(CANCEL_FOCUS_MSG);
	}

	/**
	 * ˽�еĴ���ʼ�Խ��Ļص����� ��ui�߳���ִ��
	 * @param touchPoint
	 */
	private void _startFocus(PointF touchPoint)
	{
		if(null == touchPoint)
		{
			//�������Ĵ�����Ϊnull ֱ����Ϊ��Ļ���м�λ��
			Point screenSize = new Point();
			WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getSize(screenSize);
			
			touchPoint = new PointF(screenSize.x / 2, screenSize.y / 2);
		}
		
		//���õ�ǰ�ؼ���λ��
		RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)this.getLayoutParams();
		layout.leftMargin = (int) (touchPoint.x - this.getWidth()  / 2);
		layout.topMargin  = (int) (touchPoint.y - this.getHeight() / 2);
		this.setLayoutParams(layout);
		
		this.setVisibility(View.VISIBLE);	//���õ�ǰ�ؼ��ɼ�
		this.clearColorFilter();	//�����ɫ����
		
		//��ʼ���Ŷ���
		animSet.start();
	}
	
	
	/**
	 * ---------------------------------------------------------------------------
	 * ������Ӧ �Խ��ĸ��ֲ��� ���ڶԽ��������ܲ���ui�߳��� �����Ҫ ʹ��handler���в���
	 * ----------------------------------------------------------------------------
	 */
	private static final int START_FOCUS_MSG     = 0;
	private static final int FINISH_FOCUS_MSG    = 1;
	private static final int CANCEL_FOCUS_MSG 	 = 2;
	private static final int DISAPPEAR_MSG       = 3;
	
	
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
			case START_FOCUS_MSG:
				//��ʼ�Խ�����
				_startFocus((PointF)msg.obj);
				
				break;
				
			case FINISH_FOCUS_MSG:
				
				animSet.end();	//��������
				FocusView.this.setColorFilter(ColorUtil.FOCUS_COLOR);	//�ı�ͼ�����ɫ
				
				break;
				
			case DISAPPEAR_MSG:
				//���ؼ���ʧ
				FocusView.this.clearColorFilter();	//�����ɫ����
				FocusView.this.setVisibility(View.GONE);	//�ؼ����ɼ�
				
				break;
			case CANCEL_FOCUS_MSG:
				/**
				 * ֹͣ�Խ�����
				 */
				
				animSet.cancel();	//ȡ������
			
				FocusView.this.clearColorFilter();	//�����ɫ����
				FocusView.this.setVisibility(View.GONE);	//�ؼ����ɼ�
				
				break;
			}
		}
	};
}
















