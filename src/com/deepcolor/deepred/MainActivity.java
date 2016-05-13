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

package com.deepcolor.deepred;

import java.util.HashMap;

import com.deepcolor.deepred.callback.CameraViewChangeCallback;
import com.deepcolor.deepred.callback.EncodeCallback;
import com.deepcolor.deepred.callback.FocusCameraCallback;
import com.deepcolor.deepred.callback.SwitchCameraCallback;
import com.deepcolor.deepred.callback.SwitchFlashModeCallback;
import com.deepcolor.deepred.sensor.LinearSensor;
import com.deepcolor.deepred.shot.CameraInstance;
import com.deepcolor.deepred.shot.ShotInstance;
import com.deepcolor.deepred.view.CameraView;
import com.deepcolor.deepred.view.FlashView;
import com.deepcolor.deepred.view.FocusView;
import com.deepcolor.deepred.view.LiveCheckBox;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;


public class MainActivity extends Activity 
{
	//����ʵ�� ����¼����Ƶ ����ͼƬ�Ȳ���
	private ShotInstance shotInstance = null;
	
	//���ټ� �ж��Ƿ��Զ��Խ�
	private LinearSensor linearSensor = null;
	
	private CameraView cameraView = null;	//����ͷԤ���ؼ�
	private ImageView  baffleView = null;
	private ImageView  baffleBgView = null;
	
	private FocusView  focusView  = null;	//�Խ��Ŀؼ�
	
	private LiveCheckBox liveCheckBox = null;	//live photo�Ŀ���
	private FlashView    flashView    = null;	//������л���ť
	
	private ImageButton shotButton = null;	//���㰴ť

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        initAttributes();
        initAttributesParams();	//��ʼ���� �� �ؼ��Ĳ���
        initViewsListener();	//��ʼ���ؼ����¼�����
        initEventCallback();	//��ʼ�����ֻص����¼�����
    }
    
    /**
     * ��ʼ���������� �������ֿؼ��ĳ�ʼ��
     */
    private void initAttributes()
    {
    	shotInstance = new ShotInstance();
    	linearSensor = new LinearSensor(this);
    	
    	cameraView   = (CameraView)  findViewById(R.id.camera_view);
    	baffleView   = (ImageView)   findViewById(R.id.baffle_view);
    	baffleBgView = (ImageView)   findViewById(R.id.baffle_bg_view);
    	focusView    = (FocusView)   findViewById(R.id.focus_view);
    	liveCheckBox = (LiveCheckBox)findViewById(R.id.live_photo_check_box);
    	flashView    = (FlashView)   findViewById(R.id.flash_view);
    	
    	shotButton = (ImageButton)findViewById(R.id.shot_button);
    }
    
    /**
     * ��ʼ���ؼ�������
     */
    private void initAttributesParams()
    {    	
    	/**
    	 * ��ʼ�� cameraview��Ҫ�ĵ���
    	 */
    	cameraView.setBaffleView(baffleView, baffleBgView);
    	
    	/**
    	 * flashͼ���Ƿ���ʾ
    	 */
    	flashView.setVisibleByFlashMode();
    }
    
    /**
     * ��ʼ���ؼ��ļ�������
     */
    private void initViewsListener()
    {
    	/**
    	 * �Ƿ��livephoto�Ļص�
    	 */
    	liveCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
    	{	
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
			{
				//�Ƿ��livephoto
				shotInstance.setLivePhotoChecked(isChecked);
			}
		});

    	/**
    	 * ���㰴ť���
    	 */
    	shotButton.setOnClickListener(new View.OnClickListener() 
    	{	
			@Override
			public void onClick(View v) 
			{
				//����ͼƬ������
				shotInstance.takePicture();
			}
		});
    }
    
    /**
     * ��ʼ�������¼��Ļص�
     */
    private void initEventCallback()
    {
    	/**
    	 * �õ����ʵ��
    	 */
    	CameraInstance cameraInstance = CameraInstance.getInstance();
    	
    	/**
    	 * -----------------------------------------------------------------
    	 * ���öԽ��Ļص�
    	 * -----------------------------------------------------------------
    	 */
    	cameraInstance.setFocusCallback(new FocusCameraCallback() 
    	{
			@Override
			public void startFocus(PointF touchPoint) 
			{
				/**
				 * ��ʼ�Խ� ����Խ���λ��
				 */
				focusView.startFocus(touchPoint);
			}
			
			@Override
			public void finishFocus() 
			{
				/**
				 * ��ɶԽ�
				 */
				focusView.finishFocus();
			}
			
			@Override
			public void cancelFocus() 
			{
				/**
				 * ȡ���Խ�
				 */
				focusView.cancelFocus();
			}
		});
    	
    	/**
    	 * -----------------------------------------------------
    	 * ����������л�ģʽ
    	 * -----------------------------------------------------
    	 */
    	cameraInstance.setSwitchFlashModeCallback(new SwitchFlashModeCallback() 
    	{
			@Override
			public void startSwitch() 
			{
			}
			
			@Override
			public void finishSwitch(String mode) 
			{
				/**
				 * �л����
				 */
				flashView.finishSwitchFlashMode(mode);
			}
			
			@Override
			public void failSwitch() {				
			}
		});
    	
    	/**
    	 * ---------------------------------------------------------------
    	 * �л�����ͷ�ص�
    	 * ------------------------------------------------------------
    	 */
    	cameraInstance.setSwitchCameraCallback(new SwitchCameraCallback() 
    	{
			@Override
			public void startSwitch() 
			{
				
			}
			
			@Override
			public void finishSwitch() 
			{
				/**
				 * �������ͷ�л�
				 */
				flashView.finishSwitchCamera();
				
				/**
				 * �������� Ԥ���ص� �õ�Ԥ����С��
				 */
				shotInstance.initCameraParams();
			}
			
			@Override
			public void failSwitch() 
			{

			}
		});
    	
    	/**
    	 * ------------------------------------------------------------
    	 * ���ڱ仯��ʱ��ص�
    	 * ------------------------------------------------------------
    	 */
    	cameraView.setCameraViewChangeCallback(new CameraViewChangeCallback() 
    	{	
			@Override
			public void onViewChanged(int width, int height) 
			{
				shotInstance.setViewRatio(1.0f * width / height);
			}
		});
    	
    	/**
    	 * ------------------------------------------------------------------------------------------------------------------------
    	 * ��ʼ����ص� �����б������ʱ ��ť������ת
    	 * ------------------------------------------------------------------------------------------------------------------------
    	 */
    	shotInstance.setEncodeCallback(new EncodeCallback() 
    	{
			@Override
			public void startEncode() 
			{
				liveCheckBox.startEncode();
			}
			
			@Override
			public void finishEncode() 
			{
				liveCheckBox.finishEncode();
			}
			
			@Override
			public void failEncode() 
			{
				liveCheckBox.failEncode();
			}
		});
    }
}

















