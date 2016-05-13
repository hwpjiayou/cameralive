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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.deepcolor.deepred.R;
import com.deepcolor.deepred.callback.CameraViewChangeCallback;
import com.deepcolor.deepred.callback.FocusCameraCallback;
import com.deepcolor.deepred.shot.CameraInstance;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * �̳�glsurfaceviewʵ���Լ���view��������ͷ��Ԥ��
 * @author WSH
 */

/**
 * ��ͼƬԤ����ʱ�� ͼƬԤ���Ĵ�С����Ļ��С�ı�������ͬ �����Ҫ��Ԥ��ͼƬ���вü�
 * 
 * �ü���ԭ���� ��ͼƬ������ת������ʾ�����Ͻ� ��ʼ�ü�
 * @author WSH
 *
 */
public class CameraView extends GLSurfaceView implements Renderer, OnFrameAvailableListener
{
	int textureId = -1;	//����id
	
	private SurfaceTexture surfaceTexture = null;	//������ͼ
	private TextureDraw    textureDraw    = null;	//���ڻ�������
	
	private float[] mvpMatrix     = new float[16];	//����任 ���� �ӽ����� ͶӰ�����
	private float[] textureMatrix = new float[16];	//�洢�������� ÿ��SurfaceTexture���������� ����һ������任
	
	private CameraViewChangeCallback cameraViewChangeCallback = null;	//���ڱ任ʱ����

	public CameraView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		/**
		 * ��ʼ������
		 */
		init();
	}
	
	/**
	 * ��ʼ������
	 */
	private void init()
	{
		//opengles 2.0
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);	//
		
		/**
		 * ��������ص�����
		 */
		detector = new GestureDetector(getContext(), new GestureListener());
		
		/**
         * ��ʼ��renderscript
         */
        renderScript = RenderScript.create(getContext());
        blurScript   = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{
		//����id
		textureId = createTextureId();	
		
		//����һ������id ����һ������
		surfaceTexture = new SurfaceTexture(textureId);
		surfaceTexture.setOnFrameAvailableListener(this);
		
		//��ʼ������������
		textureDraw = new TextureDraw(textureId);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		GLES20.glViewport(0, 0, width, height);
		
		float ratio = 1.0f * width / height;	//�ؼ���С�Ŀ�߱��� 
		screenWidth  = width;
		screenHeight = height;
		
		//�ӽ����������ֵ
		float cx = 0, cy = 0, cz = 3;
		float tx = 0, ty = 0, tz = 0;
		float ux = 0, uy = 1, uz = 0;
		
		//ͶӰ���������ֵ
		float left   = -1, right = 1;
		float bottom = -1.0f / ratio, top = 1.0f / ratio;
		float near   = 1, far = 10;
		
		float[] viewMatrix = new float[16];	//�ӽ�����
		float[] projMatrix = new float[16];	//ͶӰ����
		
		Matrix.setLookAtM(
				viewMatrix,
				0,
				cx, cy, cz,
				tx, ty, tz,
				ux, uy, uz);

		Matrix.orthoM(
				projMatrix,
				0,
				left, right,
				bottom, top,
				near, far);

		//�ӽ�����ϵ��ͶӰ����ϵ ��ɵ�ϵͳ����ϵ
		Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0);
		
		//========================================================================
		//��ʼ��texturedraw��Ҫ�Ĳ���
		textureDraw.setRatio(ratio);	//���ÿؼ���С����
		startPreview();	//��ʼԤ������
		
		/**
		 * --------------------------------------------------
		 * ���ڱ仯�ص�
		 */
		if(null != cameraViewChangeCallback)
			cameraViewChangeCallback.onViewChanged(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl)
	{
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		//����Ԥ��ͼƬ �� �õ��任����
		surfaceTexture.updateTexImage();
		surfaceTexture.getTransformMatrix(textureMatrix);
		
		//����Ԥ��ͼƬ
		textureDraw.onDraw(mvpMatrix, textureMatrix);
		
		/**
		 * ��glsurfaceive����Ҫ���Ƶ���Դ����һ��bitmap��ͼƬ �������и�˹ģ��
		 */
		blurringBimtap();
	}

	/**
	 * ��surfacetext���µ����ݵ�ʱ�����
	 */
	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture)
	{
		//�����µ�����ͷ����ʱ  ˢ�½���
		this.requestRender();
	}
	
	/**
	 * ���ô��ڱ仯�ص�
	 * @param c
	 */
	public void setCameraViewChangeCallback(CameraViewChangeCallback c)
	{
		cameraViewChangeCallback = c;
	}
	/**
	 * ����һ������
	 * @return
	 */
	private int createTextureId()
	{
		int[] texture = new int[1];

		GLES20.glGenTextures(1, texture, 0);

		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		return texture[0];
	}
	
	/**
	 * ��ʼԤ��
	 */
	public void startPreview()
	{
		//�õ�����ͷʵ��
		CameraInstance cameraInstance = CameraInstance.getInstance();
		
		//�õ�����ͷ��Ԥ����С
		Camera.Size previewSize = cameraInstance.getPreviewSize();
		
		//����Ԥ����С
		textureDraw.setPreviewSize(previewSize.width, previewSize.height);
		
		//��ʼԤ��
		cameraInstance.startPreview(surfaceTexture);
		
		//���жԽ�
		cameraInstance.autoFocus(null, null);
	}

	/**
	 * --------------------------------------------------------------------------------
	 * ������صĺ��� ��Ӧ ��صĴ������� ���� ���� ˫�� ���һ� �Ȳ��� 
	 * -------------------------------------------------------------------------------
	 */
	
	private static final int FLING_MIN_DISTANCE   = 15;	//��ָ����������� 
	private static final float FLING_MIN_VELOCITY = 300;	//��ָ����������ٶ�
	
	/**
	 * ����ʶ���� ����ʶ���������
	 */
	private GestureDetector detector = null;
	
	/**
	 * �����ص�
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		//����ʶ��
		return detector.onTouchEvent(event);
	}
	
	class GestureListener extends SimpleOnGestureListener
	{
		/**
		 * e1��һ�δ������ʱ��λ��
		 * e2�ڵ�ǰ�¼��д�����λ��
		 * velocityX velocityY �������ٶ� ÿ������ظ���
		 */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) 
		{
			//�����ķ�����������һ���
			if(Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY()))
			{
				//�����ľ�����ٶȷ���Ҫ��ʱ ���������¼�
				if(Math.abs(e1.getX() - e2.getX()) >= FLING_MIN_DISTANCE && Math.abs(velocityX) >= FLING_MIN_VELOCITY)
				{
					/**
					 * ���һ��� �л�����ͷ
					 */
					switchCamera();
					
					return true;
				}
			}
			
			return false;
		}
				
		@Override
		public boolean onSingleTapUp(MotionEvent e) 
		{
			/**
			 * �������� �Զ��Խ�
			 */
			autoFocus(e.getX(), e.getY());
			
			return true;
		}

		@Override
		public boolean onDown(MotionEvent e) 
		{
			return true;
		}
	}
	
	/**
	 * ������ʱ���������ĶԽ�����
	 * @param x	������
	 * @param y
	 */
	private void autoFocus(float x, float y)
	{
		//Ԥ��ͼƬ�Ĵ�С
		CameraInstance cameraInstance = CameraInstance.getInstance();
		
		/**
		 * ��֧�ֶԽ� ֱ�ӷ���
		 */
		if(!cameraInstance.supportFocusCamera())
			return;
		
		
		Camera.Size previewSize = cameraInstance.getPreviewSize();
		int previewWidth  = previewSize.width;	//Ԥ��ͼƬ�Ĵ�С
		int previewHeight = previewSize.height;
		
		int viewWidth  = this.getWidth();
		int viewHeight = this.getHeight();
		
		int focusX;	//�Խ�������
		int focusY;
		
		/**
		 * -------------------------------------------------------
		 * ��ǰ��Ԥ����������ת��90�ȵ�
		 * -------------------------------------------------------
		 */
		//���ݱ��� �жϴ������λ��
		if(1.0f * previewWidth / previewHeight > 1.0f * viewHeight / viewWidth)
		{
			/*float l = (1.0f * previewWidth / previewHeight * viewWidth - 1.0f * viewHeight) / 2.0f;
			
			focusX = Float.valueOf((l + y) / (2.0f * l + viewHeight)*2000 - 1000).intValue();
			focusY = Float.valueOf(1000 - 1.0f * x / viewWidth*2000).intValue();*/
			
			float l = 1.0f * previewWidth / previewHeight * viewWidth - 1.0f * viewHeight;
			
			focusX = Float.valueOf(y / (l + viewHeight) * 2000 - 1000).intValue();
			focusY = Float.valueOf(1000 - 1.0f * x / viewWidth * 2000).intValue();
		}
		else
		{
			/*float l = (1.0f * previewHeight / previewWidth * viewHeight - 1.0f * viewWidth) / 2.0f;
			
			focusX = Float.valueOf(1.0f * y / viewHeight * 2000 - 1000).intValue();
			focusY = Float.valueOf(1000 - (l + x) / (2.0f * l + viewWidth)*2000).intValue();*/
			float l = 1.0f * previewHeight / previewWidth * viewHeight - 1.0f * viewWidth;
			
			focusX = Float.valueOf(1.0f * y / viewHeight * 2000 - 1000).intValue();
			focusY = Float.valueOf(1000 - x / (l + viewWidth)*2000).intValue();
		}
		
		float touchX = this.getX() + x;	//���ݵ�ǰ�ؼ�������ʹ���λ�� �õ�����������ڸ��ؼ�������
		float touchY = this.getY() + y;
		
		//���жԽ�����
		cameraInstance.autoFocus(new Point(focusX, focusY), new PointF(touchX, touchY));
	}

	
	
	/**
	 * ------------------------------------------------------------------------------
	 * ���һ����л�ǰ������ͷ��ʱ����д���
	 * --------------------------------------------------------------------------------
	 */
	
	/**
	 * ͸���ȶ�����͸����
	 */
	private static final float START_ALPHA  = 0.4f;
	private static final float END_ALPHA    = 1.0f;
	private static final int ALPHA_DURATION = 450;	//����ʱ��
	
	private static final int DOWNSAMPLE_FACTOR = 10;	//ͼƬ�����ű���
	
	/**
	 * opengl��Ļ�Ĵ�С
	 */
	private int screenWidth;
	private int screenHeight;
	
	/**
	 * ����view Ĭ�ϲ���ʾ ���л�����ͷ��ʱ����ʾһ��ģ������ƬȻ�� ��������
	 */
	ImageView baffleView = null;
	ImageView baffleBgView = null;
	ObjectAnimator baffleAnimator = null;	//͸������ ���л�����ͷ��ʱ����͸���ȵı仯
	
	/**
	 * ����ͼƬ mBitmapToBlur��Ҫģ����ͼƬ 
	 * mBlurredBitmap ģ������ͼƬ
	 * 
	 * originBitmap��ԭʼ��δѹ����ͼƬ
	 */
	private Bitmap originBitmap;
	private Bitmap bitmapToBlur;
	private Bitmap blurredBitmap;
	
	/**
	 * �Ƿ���Ļ����Ϊһ��ģ����ͼƬ
	 */
	private boolean blurSurfaceToBitmap = false;
	
	private boolean isBlurring  = false;	//�Ƿ����ڽ���ͼƬ��ģ��
	private boolean isSwitching = false;	//�Ƿ����ڽ�������ͷ���л�
    
    /**
     * ��˹ģ����Ҫ������
     */
    private RenderScript renderScript;	//ʹ��renderscript������˹ģ��
    private ScriptIntrinsicBlur blurScript;
    private Allocation blurInput, blurOutput;	//renderscript����������
    
    /**
     * ��ʼ������ؼ� ����ǰ���ͱ���
     * ��ʼ������
     * @param context
     */
    public void setBaffleView(ImageView bView, ImageView bBgView)
    {
    	baffleView   = bView;
    	baffleBgView = bBgView;
    	
    	/**
		 * ��ʼ������
		 */
		baffleAnimator = ObjectAnimator.ofFloat(baffleView, "alpha", START_ALPHA, END_ALPHA);
		baffleAnimator.setDuration(ALPHA_DURATION);
		
		baffleAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			
			@Override
			public void onAnimationEnd(Animator animation) 
			{
				isBlurring = false;
				
				/**
				 * �������ͷ�л� �� �������Ѿ����ֱ�ӽ����� ��Ϊ���ɼ�
				 */
				if(!isSwitching)
				{
					baffleView.setVisibility(View.GONE);
					baffleBgView.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
    }
	
	
	/**
	 * ��glsurfaceview�еõ�һ��ͼƬȻ����и�˹ģ��
	 * ��ģ���õ�ͼƬ�洢 ������ʾ
	 */
	private void blurringBimtap()
	{
		if(!blurSurfaceToBitmap)	
			return;
	    
		blurSurfaceToBitmap = false;
		
		/**
		 * �洢ԭʼ����������
		 */
		int bitmapBuffer[] = new int[screenWidth * screenHeight];
	    int bitmapSource[] = new int[screenWidth * screenHeight];
		
	    IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
	    intBuffer.position(0);

	    /**
	     * ��ȡopengl����������
	     */
	    GLES20.glReadPixels(0, 0, screenWidth, screenHeight, GL10.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
	    
	    /**
	     * ��ͼƬ�������Ŵ��� �洢��������
	     */
		int offset1, offset2;
	    for(int i = 0; i < screenHeight; ++i)
	    {
	    	offset1 = i  * screenWidth;
	    	offset2 = (screenHeight - i - 1) * screenWidth;
	    	
	    	for(int j = 0; j < screenWidth; ++j)
	    	{
	    		int texturePixel = bitmapBuffer[offset1 + j];
                int blue = (texturePixel >> 16) & 0xff;
                int red  = (texturePixel << 16) & 0x00ff0000;
                int pixel = (texturePixel & 0xff00ff00) | red | blue;
                
                bitmapSource[offset2 + j] = pixel;
	    	}
	    }
	    
	    /**
	     * ������洢��һ��ԭʼ��ͼƬ
	     */
	    if(null != originBitmap)
	    {
	    	originBitmap.recycle();
	    	originBitmap = null;
	    }
	    originBitmap = Bitmap.createBitmap(bitmapSource, screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
	    
	    /**
	     * ��һ��ͼƬ��������
	     */
	    android.graphics.Matrix matrix = new android.graphics.Matrix(); 
	    matrix.postScale(1f / DOWNSAMPLE_FACTOR, 1f / DOWNSAMPLE_FACTOR); //���Ϳ�Ŵ���С�ı���
	    
	    if(null != bitmapToBlur)
        {
        	bitmapToBlur.recycle();
        	bitmapToBlur = null;
        }
        bitmapToBlur = Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), originBitmap.getHeight(), matrix, false);
        
        /**
         * ��������ĸ�˹ģ�����ͼƬ
         */
        if(null != blurredBitmap)
        {
        	blurredBitmap.recycle();
        	blurredBitmap = null;
        }
        blurredBitmap = Bitmap.createBitmap(bitmapToBlur.getWidth(), bitmapToBlur.getHeight(), Bitmap.Config.ARGB_8888);
		
        /**
         * render����������
         */
        blurInput  = Allocation.createFromBitmap(renderScript, bitmapToBlur, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
        blurOutput = Allocation.createTyped(renderScript, blurInput.getType());
        
        /**
		 * ��ͼƬ����ģ�� Ȼ�� ���Ƶ�blurredBitmap��
		 */
		blurInput.copyFrom(bitmapToBlur);
        blurScript.setInput(blurInput);
        blurScript.forEach(blurOutput);
        blurOutput.copyTo(blurredBitmap);

		/**
		 * ���������Ϣ
		 */
		handler.sendEmptyMessage(FINISH_BLUR_MSG);
	}
	
	/**
	 * �л�ǰ������ͷ
	 */
	private void switchCamera()
	{
		/**
		 * �����ǰ�����л�����ͷ ֱ�ӷ���
		 */
		if(isBlurring || isSwitching)
			return;
		
		CameraInstance cameraInstance = CameraInstance.getInstance();
		
		//��֧���л�ֱ�ӷ���
		if(!cameraInstance.supportSwitchCamera())	
			return;
		
		/**
		 * ȡ���Խ�
		 */
		cameraInstance.cancelFocus();
		
		/**
		 * �õ�һ��ģ����ͼƬ
		 */
		blurSurfaceToBitmap = true;
		isBlurring  = true;	//��־λ ��ʾ��ǰ����ģ����Ļ���洢��һ��ͼƬ Ȼ����ж�������
		isSwitching = true;	//��־λ ��ʾ�����л�����ͷ
		
		/**
		 * ����ˢ�½��� ֪ͨ ����һ��ģ����δģ���ı�ֽ
		 */
		this.requestRender();
		
        /**
         * �첽���л�����ͷ
         */
		new SwitchAsyncTask().execute(null, null);
	}
	
	/**
	 * --------------------------------------------------------------------
	 * opengl���̺߳�ui����ͬһ���߳� ��Ҫ�첽����
	 * ----------------------------------------------------------------------
	 */
	private static final int FINISH_BLUR_MSG = 0;	//��ɸ�˹ģ��
	
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
			case FINISH_BLUR_MSG:
				baffleBgView.setImageBitmap(bitmapToBlur);	//δģ����ͼƬ
		        baffleBgView.setVisibility(View.VISIBLE);
				
				baffleView.setImageBitmap(blurredBitmap);	//����ͼƬ
		        baffleView.setVisibility(View.VISIBLE);	//�ɼ�

		        baffleAnimator.start();	//��ʼ͸���ȵı仯

		        break;
			}
		}
	};
	
	/**
	 * �첽�Ŀ�������ͷ
	 */
	class SwitchAsyncTask extends AsyncTask<Integer, Integer, String>
	{
		/**  
	     * �����String������ӦAsyncTask�еĵ�����������Ҳ���ǽ���doInBackground�ķ���ֵ��  
	     * ��doInBackground����ִ�н���֮�������У�����������UI�̵߳��� ���Զ�UI�ռ��������  
	     */  
	    @Override  
	    protected void onPostExecute(String result) 
	    {
			/**
			 * ���ñ�־λ
			 */
	    	isSwitching = false;
	    	
	    	/**
	    	 * �л�����ͷ��ɺ� ���������ڲ��ɼ�
	    	 */
	    	if(!isBlurring)
	    	{
	    		baffleView.setVisibility(View.GONE);
	    		baffleBgView.setVisibility(View.GONE);
	    	}
	    }

		@Override
		protected String doInBackground(Integer... params) 
		{			
			/**
			 * �л�����ͷ
			 */
			CameraInstance cameraInstance = CameraInstance.getInstance();
			cameraInstance.switchCamera();	//�л�����ͷ
			
			/**
			 * �л���ɺ�ʼԤ��
			 */
			startPreview();
			
			return null;
		}
	}
	
}


/**
 * -----------------------------------------------------------------------------------------------
 * -------------------------------------------------------------------------------------------------------
 */
/**
 * ���ƺ��� ���ݴ����textureid �� �������Ϣ ���� ����ͷ��Ԥ������
 * @author WSH
 *
 */
class TextureDraw
{
	//������ɫ��
	private final String vertexShaderCode =
			//������������ vPosition inputTextureCoordinate
			"uniform mat4 uMVPMatrix;" + 	//�ܵı任����
			"uniform mat4 uTexMatrix;" +	//����任����
			"uniform float uAlpha;" + 
			
            "attribute vec4 aPosition;" +			//λ������
            "attribute vec4 aTextureCoordinate;" +	//��������
            
            "varying vec2 vTextureCoordinate;" +	//���뵽ƬԴ��ɫ������������
            "varying float vAlpha;" + 
            
            "void main()" +
            "{"+
                "gl_Position = uMVPMatrix * aPosition;"+	//λ������
                "vTextureCoordinate = (uTexMatrix * aTextureCoordinate).xy;" +	//��������
                //"vTextureCoordinate = aTextureCoordinate.xy;" + 
                "vAlpha = uAlpha;" +	//͸����
            "}";
	
	//ƬԴ��ɫ������
	private final String fragmentShaderCode =
            "#extension GL_OES_EGL_image_external : require\n"+
            "precision mediump float;" +
            		
            //���մӶ�����ɫ����������������
            "varying vec2 vTextureCoordinate;" +
            "varying float vAlpha;" + 
            
            "uniform samplerExternalOES s_texture;" +
            
            "void main() {" +
            	"gl_FragColor = texture2D(s_texture, vTextureCoordinate);" +
            	"gl_FragColor.a = vAlpha;" + 
            "}";
	
	//=====================================================================
	private FloatBuffer vertexVerticesBuffer;	//��������
	private FloatBuffer textureVerticesBuffer;	//��������
    private ShortBuffer drawListBuffer;	//����˳��
    
    private int program;				//shader����id
    
    private int mvpMatrixHandle;			//�任������
    private int textureMatrixHandle;		//����任����
    private int alphaHandle;			//͸���Ⱦ��
    
    private int positionHandle;		//����λ�� ���
    private int textureCoordHandle;	//������
    
    
    //�������˳��
    private short drawOrder[] = {0, 1, 2, 0, 2, 3};
    
    //����id
  	private int textureId = -1;
  	
  	//ÿ���� ��һ������ĸ���(x, y)������
    private static final int COORDS_PER_VERTEX = 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    
    private static final float alpha = 1.0f;	//͸���� ��ʱ������
    
    //opengl���ڵĿ�͸߱���
    private float ratio = -1;
    private int previewWidth  = -1;	//����ͷ��Ԥ��ͼƬ��С
    private int previewHeight = -1;
    
    /*
     * ������������
     */
    public TextureDraw(int textureId)
    {
    	this.textureId = textureId;
    	
    	//��ʼ������
    	init();
    }
    
    //��ʼ����������Ϣ ���� shader�ĳ�ʼ��
    private void init()
    {
    	//����˳��
        ByteBuffer orderBB = ByteBuffer.allocateDirect(drawOrder.length * 2);
        orderBB.order(ByteOrder.nativeOrder());
        drawListBuffer = orderBB.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++
        //����shader
        int vertexShader    = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader  = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
        //glCreaterProgram()����һ������ɫ���������
        program = GLES20.glCreateProgram();
        
        //glAttachShader()�ֱ𽫶�����ɫ�������Ƭ����ɫ�����󸽼ӵ�����ɫ����������ϣ�
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        
        //glLinkProgram()�ԣ���ɫ���������ִ�����Ӳ���
        GLES20.glLinkProgram(program);
        
        //�õ����ֲ����ľ��
        mvpMatrixHandle        = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        textureMatrixHandle    = GLES20.glGetUniformLocation(program, "uTexMatrix");
        alphaHandle            = GLES20.glGetUniformLocation (program, "uAlpha");
        
        positionHandle     = GLES20.glGetAttribLocation (program, "aPosition");
        textureCoordHandle = GLES20.glGetAttribLocation (program, "aTextureCoordinate");
    }
    
    /**
     * ����shader����
     * @param type
     * @param shaderCode
     */
    private int loadShader(int type, String shaderCode)
    {
    	//glCreateshader()�ֱ𴴽�һ��������ɫ�������һ��Ƭ����ɫ������
        int shader = GLES20.glCreateShader(type);

        //glShaderSource()�ֱ𽫶�����ɫ�����Դ�����ַ�����󶨵�������ɫ������
        //��Ƭ����ɫ�����Դ�����ַ�����󶨵�Ƭ����ɫ������
        GLES20.glShaderSource(shader, shaderCode);
        
        //glCompileShader()�ֱ���붥����ɫ�������Ƭ����ɫ������
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    /**
     * ����opengl�ؼ����߱��� �Ӷ����ö�������
     */
    public void setRatio(float r)
    {
    	//���ڿؼ��Ŀ���ߵı���
    	if(ratio != r)
    	{
    		ratio = r;
        	float inverseRatio = 1.0f / ratio;
        	
        	float vertexCoords[] = 
    	    	{
    	         
    	         -1.0f, -inverseRatio,
                  1.0f, -inverseRatio,
                 -1.0f,  inverseRatio,
                  1.0f,  inverseRatio,
    	    };
        	
        	//���������� vertexVerticesBuffer
    		ByteBuffer vertexBB = ByteBuffer.allocateDirect(vertexCoords.length * 4);
    		vertexBB.order(ByteOrder.nativeOrder());
    		vertexVerticesBuffer = vertexBB.asFloatBuffer();
    		vertexVerticesBuffer.put(vertexCoords);
    		vertexVerticesBuffer.position(0);
    	}
    }
    
    /**
     * �������Ԥ��ͼƬ�Ĵ�С
     * @param width
     * @param height
     */
    public void setPreviewSize(int width, int height)
    {
    	if(previewWidth != width || previewHeight != height)
    	{
    		//ͼƬԤ���Ĵ�С
    		previewWidth  = width;
    		previewHeight = height;
    		
    		/**
    		 * ����ͷ��ͼƬ����һ��90����ת ���Ա����Ϳؼ��Ŀ���ߵı��� �ǵ�����ϵ
    		 * 
    		 * �����Ͻǿ�ʼԤ�� ���ڲü�
    		 */
    		float previewRatio = 1.0f * previewHeight / previewWidth;
    		
    		//��������ķ�Χ
    		float startU = 0, endU = 1;
    		float startV = 0, endV = 1;
    		
    		if(previewRatio > ratio)
    		{
    			startU = 0;
    			endU   = 1;
    			
    			startV = (previewHeight - ratio * previewWidth) / (1.0f * previewHeight);
    			endV   = 1.0f;
    			
    			//startV = (previewHeight - ratio * previewWidth) / (2.0f * previewHeight);
    			//endV   = 1.0f - startV;
    			
    		}
    		else if(previewRatio < ratio)
    		{
    			//startU = (previewWidth - previewHeight / ratio) / (2.0f * previewWidth);
    			//endU   = 1.0f - startU;
    			startU = 0;
    			endU   = previewHeight / ratio / previewWidth;
    			
    			startV = 0;
    			endV   = 1;
    		}
    		
    		//��������
    		float textureVertices[] = 
	        	{
    				endU, startV,
    				endU, endV,
    				startU, startV,
    				startU, endV,
	        	};
    		
    		//��������
	  		ByteBuffer textureBB = ByteBuffer.allocateDirect(textureVertices.length * 4);
	  		textureBB.order(ByteOrder.nativeOrder());
	  		textureVerticesBuffer = textureBB.asFloatBuffer();
	  	    textureVerticesBuffer.put(textureVertices);
	  	    textureVerticesBuffer.position(0);
    	}
    }
    
    /**
     * ��������
     * @param mvpMatrix
     * @param textureMatrix
     */
  	public void onDraw(float[] mvpMatrix, float[] textureMatrix)
  	{
  		//ʹ��glUseProgram()��OpenGL��Ⱦ�ܵ��л�����ɫ��ģʽ����ʹ�øղ����õģ���ɫ���������
        GLES20.glUseProgram(program);
        
        //������
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        
        //�� ���� ��������
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        
        /*
         * void glUniformMatrix4fv(GLint location,  GLsizei count,  GLboolean transpose,  const GLfloat *value); 
			location:ָ��Ҫ���ĵ�uniform������λ��
			count:ָ��Ҫ���ĵľ������
			transpose:ָ���Ƿ�Ҫת�þ��󣬲�������Ϊuniform������ֵ������ΪGL_FALSE��
			value:ָ��һ��ָ��count��Ԫ�ص�ָ�룬��������ָ����uniform������
         */
        //��������任���� 
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        
        //��������任����
        GLES20.glUniformMatrix4fv(textureMatrixHandle, 1, false, textureMatrix, 0);
        
        //������������
        GLES20.glVertexAttribPointer(textureCoordHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);
        
        //���ö�������
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexVerticesBuffer);
        
        //���������͸����
        GLES20.glUniform1f(alphaHandle, alpha);
        
        //��������
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        
        //�رն������������
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
  	}
}






























