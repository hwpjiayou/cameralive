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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.deepcolor.deepred.callback.FocusCameraCallback;
import com.deepcolor.deepred.callback.SwitchCameraCallback;
import com.deepcolor.deepred.callback.SwitchFlashModeCallback;
import com.deepcolor.deepred.util.MathUtil;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;

/**
 * camera�ĵ����� ��������ͷ���л� Ԥ�� �Խ��Ȳ���
 * @author WSH
 *
 */
public class CameraInstance 
{
	//һ��ʵ�� ����ģʽ
	private static CameraInstance cameraInstance = null;
	
	public static synchronized CameraInstance getInstance()
	{
		if(null == cameraInstance)
		{
			cameraInstance = new CameraInstance();
		}
		
		return cameraInstance;
	}
	
	private CameraInstance()
	{
		/**
		 * ��һ�����
		 */
		openCamera();
	}
	
	//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	private Camera camera = null;	//����ͷ
	private int cameraId  = -1;		//����ͷid
	private Camera.Size previewSize = null;	//Ԥ����С ��ǰ����ͷ��Ӧ��ͼƬ��Ԥ����С
	
	private boolean isPreviewing = false;	//��־��ǰ�Ƿ�����Ԥ������ͷ	
	
	public Camera getCamera() 
	{
		return camera;
	}

	public int getCameraId() 
	{
		return cameraId;
	}

	public Camera.Size getPreviewSize() 
	{
		return previewSize;
	}

	public boolean isPreviewing()
	{
		return isPreviewing;
	}

	/**
	 * ��һ��Ĭ�ϵ����
	 */
	public boolean openCamera()
	{
		return openCamera(0);
	}
	
	/**
	 * ��һ����Ӧ��id��camera
	 * ���û������ͷ����false
	 * @param id
	 */
	public boolean openCamera(int id)
	{
		//�����ǰ����Ѿ���
		if(null != camera)
		{
			//�Ѿ����˶�Ӧid����� ֱ�ӷ���
			if(id == cameraId)	
				return true;	
			
			//�ͷŵ�ǰ���
			releaseCamera();
		}
		
		//���û������ͷֱ�ӷ���false
		int numberOfCameras = Camera.getNumberOfCameras();
		if(0 == numberOfCameras)
			return false;
		
		if(0 > id || id >= numberOfCameras)
			id = 0;
		
		camera   = Camera.open(id);
		cameraId = id;
		
		//��ʼ������ͷ����ز���
		initCameraParams(camera, cameraId);
		
		return true;
	}
	
	/**
	 * �ͷŵ�ǰ���
	 */
	public void releaseCamera()
	{
		if(null != camera)
		{
			stopPreview();	//����ֹͣԤ��
			
			camera.release();	//�ͷ�����ͷ
						
			camera      = null;
			cameraId    = -1;
			previewSize = null;
		}
	}
	
	/**
	 * ֹͣԤ������ͷ
	 */
	private void stopPreview()
	{
		if(isPreviewing)
		{
			if(null != camera)
			{
				//camera.cancelAutoFocus();	//ֹͣ�Խ�
				camera.stopPreview();
				camera.addCallbackBuffer(null);
				camera.setPreviewCallbackWithBuffer(null);
			}
			
			isPreviewing = false;
		}
	}
	
	/**
	 * ��ʼԤ�����
	 */
	public void startPreview(SurfaceTexture surfaceTexture)
	{
		//ֹͣ��ǰ��Ԥ��
		if(isPreviewing)
		{
			stopPreview();
		}
		
		if(null == camera)	return;
		
		//����Ԥ��
		try 
		{
			camera.setPreviewTexture(surfaceTexture);
			camera.startPreview();
			
			isPreviewing = true;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * ---------------------------------------------------------------
	 * ��С��� Ԥ���Ĵ�С�Ǵ�����С��ȵ���Сֵ �������źͷ��ʵ�ʱ��
	 * ---------------------------------------------------------------
	 */
	private static final int MIN_PREVIEW_WIDTH = 400;	//����ֵ
	
	/**
	 * ��ʼ������ͷ����ز���
	 * @param camera
	 * @param cameraId
	 */
	private void initCameraParams(Camera camera, int cameraId)
	{
		if(null == camera)	return;
		
		//�õ�����ͷ��������Ϣ
		Parameters parameters = camera.getParameters();
		
		/**
		 * Ԥ����֡��
		 */
		int[] range = getPreviewFpsRange(parameters);
		parameters.setPreviewFpsRange(range[0], range[1]);
		
		/**
		 * Ԥ����ʽ ���ԣ���������������������������������
		 */
		//parameters.setPreviewFormat(ImageFormat.YV12);	
		//parameters.getS
		
		//�õ�֧�ֵĶԽ�ģʽ
		List<String> focusMode = parameters.getSupportedFocusModes();
		if(focusMode.contains(Parameters.FOCUS_MODE_AUTO))
		{
			//����Ϊ �Զ��Խ�ģʽ
			parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}
		
		/**
		 * ����ͼƬ��Ԥ����С
		 */
		previewSize = getPreviewSize(parameters, MIN_PREVIEW_WIDTH);
		parameters.setPreviewSize(previewSize.width, previewSize.height);
		
		//�����������
		camera.setParameters(parameters);
	}
	
	/**
	 * ����֧�ֵ�Ԥ��֡������ ֡��
	 */
	private int[] getPreviewFpsRange(Parameters parameters)
	{
		List<int[]> ranges = parameters.getSupportedPreviewFpsRange();
		int[] range = ranges.get(0);
		
		//���б��� 
		for(int[] r : ranges)
		{
			if(r[1] > range[1] || (r[1] == range[1] && r[0] > range[0]))
			{
				range = r;
			}
		}
		
		return range;
	}
	
	/**
	 * ���ݵ�ǰ֧�ֵ�Ԥ����С �õ����ڵ���width����СԤ����С
	 * @param parameters
	 * @param width
	 * @return
	 */
	private Camera.Size getPreviewSize(Parameters parameters, int width)
	{
		/**
		 * ���Ҵ��ڵ���width����Сֵ ������Ҳ���ֱ�ӷ��ص�һ��Ԥ����С
		 */
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		Camera.Size curSize = previewSizes.get(0);
		
		boolean bigger = curSize.width >= width;
		int diff = Math.abs(curSize.width - width);
		
		int num = previewSizes.size();
		for(int i = 0; i < num; ++i)
		{
			Camera.Size size = previewSizes.get(i);
			boolean tBigger = size.width >= width;
			int tDiff = Math.abs(size.width - width);
			
			if((!bigger && tBigger) || 
			    (bigger && tBigger && (tDiff < diff)) ||
			    (!bigger && !tBigger && (tDiff < diff)))
			{
				bigger = tBigger;
				diff   = tDiff;
				curSize = size;
			}
		}
		
		return curSize;
	}
	
	/**
	 * ----------------------------------------------------------------------------------------
	 * �Խ���صĺ��� 
	 * ---------------------------------------------------------------------------------------
	 */
	private static final int FOCUS_AREA_SIZE = 300;	//�Խ�����Ĵ�С
	
	private FocusCameraCallback focusCameraCallback  = null;	//�Զ���ĶԽ��ص�
	private AutoFocusCallback autoFacusCallback      = null;	//ϵͳ�Դ��ĶԽ��ص� ��ɶԽ���ʱ�����
	
	/**
	 * ���öԽ��ص�
	 * @param callback
	 */
	public void setFocusCallback(FocusCameraCallback f)
	{
		if(null == f)	return;
		
		//�Զ���ĶԽ��ص�
		focusCameraCallback = f;
		
		autoFacusCallback = new AutoFocusCallback() 
		{
			@Override
			public void onAutoFocus(boolean success, Camera camera) 
			{
				if(null != focusCameraCallback)
				{
					/**
					 * ��ɶԽ�
					 */
					if(success)
						focusCameraCallback.finishFocus();
					else
						focusCameraCallback.cancelFocus();
				}
			}
		};
	}
	
	/**
	 * �ж��Ƿ�֧�� �Զ��Խ�
	 * @return
	 */
	public boolean supportFocusCamera()
	{
		if(null == camera)	return false;
		if(!isPreviewing)   return false;
		
		Parameters parameters = camera.getParameters();
		if(null == parameters || 0 >= parameters.getMaxNumFocusAreas())	
			return false;	
		
		return true;
	}
	
	/**
	 * �Զ��Խ� �Զ��Խ������Ӱ�ui�߳�ִ�� Ҳ�����ڷ�ui�߳�ִ��  ע������ı�ui����ʾ ��Ҫע��
	 * @param focusPoint �Խ�������
	 * @param touchPoint �����������
	 */
	public void autoFocus(Point focusPoint, PointF touchPoint)
	{
		/**
		 * ��֧�ֶԽ� ֱ�ӷ���
		 */
		if(!supportFocusCamera())	return;
		
		/**
		 * ����ȡ����ǰ�ĶԽ� ������ڵĻ�
		 */
		cancelFocus();
		
		//�õ��������
		Parameters parameters = camera.getParameters();
				
		//����Խ���Ϊnull ֱ��������λ�ý��жԽ�
		if(null == focusPoint)
		{
			focusPoint = new Point(0, 0);
		}
		
		int left = focusPoint.x - FOCUS_AREA_SIZE / 2;
		int top  = focusPoint.y - FOCUS_AREA_SIZE / 2;
		
		left = MathUtil.clamp(left, -1000, 1000 - FOCUS_AREA_SIZE);
		top  = MathUtil.clamp(top , -1000, 1000 - FOCUS_AREA_SIZE);
		
		Rect focusRect = new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
		List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
		focusAreas.add(new Camera.Area(focusRect, 1000));
		
		//���öԽ�����
		parameters.setFocusAreas(focusAreas);
		
		//�����������
		camera.setParameters(parameters);
		
		//��ʼ����ĶԽ��ص�
		if(null != focusCameraCallback)
		{
			focusCameraCallback.startFocus(touchPoint);
		}
		
		//�����Զ��Խ�
		camera.autoFocus(autoFacusCallback);
	}
	
	/**
	 * ȡ���Խ�����
	 */
	public void cancelFocus()
	{
		if(null != camera)
			camera.cancelAutoFocus();
		
		if(null != focusCameraCallback)
			focusCameraCallback.cancelFocus();
	}
	
	
	
	
	/**
	 * -------------------------------------------------------------------
	 * �л�����ͷ���
	 * ---------------------------------------------------------------------
	 */
	private SwitchCameraCallback switchCameraCallback = null;
	
	/**
	 * ��������ͷ���л��ص�����
	 * @param s
	 */
	public void setSwitchCameraCallback(SwitchCameraCallback s)
	{
		switchCameraCallback = s;
	}

	/**
	 * �Ƿ�֧������ͷ���л�
	 * @return
	 */
	public boolean supportSwitchCamera()
	{
		int numberOfCameras = Camera.getNumberOfCameras();
		
		return 1 < numberOfCameras;
	}
	
	/**
	 * �л�����ͷ
	 */
	public void switchCamera()
	{
		/**
		 * ��֧��ֱ�ӷ���
		 */
		if(!supportSwitchCamera())	
			return;
		
		/**
		 * �л�����ͷ�ص� ��ʼ�л�����ͷ
		 */
		if(null != switchCameraCallback)
		{
			switchCameraCallback.startSwitch();
		}
				
		/**
		 * �л�����ͷ
		 */
		int changeId = -1;
		int numberOfCameras = Camera.getNumberOfCameras();
		for(int i = 0; i < numberOfCameras; ++i)
		{
			if(i != cameraId)
			{
				changeId = i;
				break;
			}
		}
		
		/**
		 * ����ͷ�л�ʧ��
		 */
		if(-1 == changeId)
		{
			/**
			 * ����ͷ�л�ʧ��
			 */
			if(null != switchCameraCallback)
			{
				switchCameraCallback.failSwitch();
			}
			
			return;
		}
		
		/**
		 * ����һ������ͷ
		 */
		openCamera(changeId);
		
		/**
		 * ����ͷ�л��ɹ�
		 */
		if(null != switchCameraCallback)
		{
			switchCameraCallback.finishSwitch();
		}
	}

	/**
	 * ----------------------------------------------------------------
	 * �������صĲ���
	 * ----------------------------------------------------------------
	 */
	private SwitchFlashModeCallback switchFlashModeCallback = null;
	
	/**
	 * �����л������ģʽ �ص�
	 * @param s
	 */
	public void setSwitchFlashModeCallback(SwitchFlashModeCallback s)
	{
		switchFlashModeCallback = s;
	}
	
	
	
	/**
	 * �洢��������� ģʽ �� �ر� �� �Զ� 
	 * ����ͬʱ֧������ģʽ�ű��� ������Կ���
	 */
	private static final String[] FLASH_MODES = 
		{
			Parameters.FLASH_MODE_OFF, 
			Parameters.FLASH_MODE_AUTO, 
			Parameters.FLASH_MODE_ON
		};
	
	/**
	 * �Ƿ�֧�������
	 * @return
	 */
	public boolean supportFlashMode()
	{
		if(null == camera)	
			return false;
		
		Parameters parameters = camera.getParameters();
		if (parameters == null)	
			return false;
		
		/**
		 * �õ�flashmodes����Ϣ
		 */
		List<String> flashModes = parameters.getSupportedFlashModes();
		if(null == flashModes || 0 == flashModes.size())	
			return false;
		
		for(int i = 0; i < FLASH_MODES.length; ++i)
		{
			if(!flashModes.contains(FLASH_MODES[i]))
				return false;
		}
		
		return true;
	}
	
	/**
	 * �õ���ǰ�������ģʽ
	 * @return
	 */
	public String getCurFlashMode()
	{
		if(!supportFlashMode())
			return null;
		
		return camera.getParameters().getFlashMode();
	}
	
	/**
	 * �л���ǰ�������ģʽ 
	 * �����֧������� ���� ���δ�� ֱ�ӷ���null
	 * @return ��������Ƶ�ģʽ
	 */
	public void switchFlashMode()
	{
		if(null == camera || !supportFlashMode())	return;
		
		/**
		 * ִ�лص�
		 */
		if(null != switchFlashModeCallback)
		{
			switchFlashModeCallback.startSwitch();
		}
		
		Parameters parameters = camera.getParameters();
		
		/**
		 * �õ���ǰ�������ģʽ
		 */
		String curMode = parameters.getFlashMode();
		String flashMode = null;
		
		/**
		 * �л�����һ��ģʽ
		 */
		for(int i = 0; i < FLASH_MODES.length; ++i)
		{
			if(curMode.equals(FLASH_MODES[i]))
			{
				flashMode = FLASH_MODES[(i + 1) % FLASH_MODES.length];
				
				break;
			}
		}
		
		/**
		 * �л�ʧ��
		 */
		if(null == flashMode)
		{
			if(null != switchFlashModeCallback)
			{
				switchFlashModeCallback.failSwitch();
			}
			
			return;
		}
		
		//�л������ģʽ
		parameters.setFlashMode(flashMode);
		camera.setParameters(parameters);
		
		/**
		 * �ɹ��ص�
		 */
		if(null != switchFlashModeCallback)
		{
			switchFlashModeCallback.finishSwitch(flashMode);
		}
	}
	
	
	/**
	 * ---------------------------------------------------------
	 * ��Ԥ����صĺ��� 
	 * ---------------------------------------------------------------------
	 */
	
	/**
	 * ����Ԥ�� �ص����� �� ������
	 * @param callback
	 * @param data
	 */
	public void setPreviewCallback(Camera.PreviewCallback callback, byte[] buffer)
	{
		if(null == camera)	return;
		
		camera.addCallbackBuffer(buffer);
		camera.setPreviewCallbackWithBuffer(callback);
	}
	
	/**
	 * ----------------------------------------------------------
	 * �õ�����ͷ��ǰ�� 
	 * ----------------------------------------------------------
	 */
	private static final int CAMERA_FACING_FRONT = 0;
	private static final int CAMERA_FACING_BACK = 1;
	private static final int CAMERA_FACING_ERROR = -1;
	
	/**
	 * �õ�ǰ������ͷ��Ϣ
	 * @return
	 */
	public int getCameraFacing()
	{
		if(null == camera || 0 > cameraId)
			return CAMERA_FACING_ERROR;

		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(cameraId, cameraInfo);
		
		if(CameraInfo.CAMERA_FACING_FRONT == cameraInfo.facing)
			return CAMERA_FACING_FRONT;
		else if(CameraInfo.CAMERA_FACING_BACK == cameraInfo.facing)
			return CAMERA_FACING_BACK;
		else
			return CAMERA_FACING_ERROR;
	}
}





















