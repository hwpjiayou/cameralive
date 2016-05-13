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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

/**
 * �õ������豸����Ϣ
 * @author WSH
 *
 */
public class DeviceUtil 
{
	/** >=2.2 */
    public static boolean hasFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /** >=2.3 */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /** >=3.0 LEVEL:11 */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /** >=3.1 */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /** >=4.0 14 */
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * >= 4.1 16
     *
     * @return
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /** >= 4.2 17 */
    public static boolean hasJellyBeanMr1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /** >= 4.3 18 */
    public static boolean hasJellyBeanMr2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    /** >=4.4 19 */
    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static int getSDKVersionInt() {
        return Build.VERSION.SDK_INT;
    }

    @SuppressWarnings("deprecation")
    public static String getSDKVersion() {
        return Build.VERSION.SDK;
    }

    /**
     * ����豸�Ĺ̼��汾��
     */
    public static String getReleaseVersion() {
        return StringUtil.makeSafe(Build.VERSION.RELEASE);
    }

    /** ����Ƿ������˻��� */
    public static boolean isZte() {
        return getDeviceModel().toLowerCase().indexOf("zte") != -1;
    }

    /** �ж��Ƿ������ǵ��ֻ� */
    public static boolean isSamsung() {
        return getManufacturer().toLowerCase().indexOf("samsung") != -1;
    }

    /** ����Ƿ�HTC�ֻ� */
    public static boolean isHTC() {
        return getManufacturer().toLowerCase().indexOf("htc") != -1;
    }

    /**
     * ��⵱ǰ�豸�Ƿ����ض����豸
     *
     * @param devices
     * @return
     */
    public static boolean isDevice(String... devices) {
        String model = DeviceUtil.getDeviceModel();
        if (devices != null && model != null) {
            for (String device : devices) {
                if (model.indexOf(device) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ����豸�ͺ�
     *
     * @return
     */
    public static String getDeviceModel() {
        return StringUtil.trim(Build.MODEL);
    }

    /** ��ȡ������Ϣ */
    public static String getManufacturer() {
        return StringUtil.trim(Build.MANUFACTURER);
    }

    /**
     * �ж��Ƿ���ƽ�����
     *
     * @param context
     * @return
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * ����Ƿ���ƽ�����
     *
     * @param context
     * @return
     */
    public static boolean isHoneycombTablet(Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    public static int dipToPX(final Context ctx, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
    }

    /**
     * ��ȡCPU����Ϣ
     *
     * @return
     */
    public static String getCpuInfo() {
        String cpuInfo = "";
        try {
            if (new File("/proc/cpuinfo").exists()) {
                FileReader fr = new FileReader("/proc/cpuinfo");
                BufferedReader localBufferedReader = new BufferedReader(fr, 8192);
                cpuInfo = localBufferedReader.readLine();
                localBufferedReader.close();

                if (cpuInfo != null) {
                    cpuInfo = cpuInfo.split(":")[1].trim().split(" ")[0];
                }
            }
        } catch (IOException e) {
        } catch (Exception e) {
        }
        return cpuInfo;
    }

    /** �ж��Ƿ�֧������� */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) //�ж��豸�Ƿ�֧�������
                        return true;
                }
            }
        }
        return false;
    }

    /** ����豸�Ƿ�֧����� */
    public static boolean isSupportCameraHardware(Context context) {
        if (context != null && context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** ��ȡ��Ļ��� */
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) 
    {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        
        return display.getWidth();
    }

    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context) 
    {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        
        return display.getHeight();
    }
}
