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

import android.graphics.Color;

/**
 * ������Ŀ����Ҫ����ɫ
 * @author WSH
 *
 */
public class ColorUtil 
{
	public static final int FOCUS_COLOR  = 0xFFFFA000;	//�Խ��ؼ���ɫ
	//public static final int BAFFLE_COLOR = 0xAAFF4545;	//�������ɫ
	
	/**
	 * �õ�һ����ɫ���෴ ��ɫ
	 * @param color
	 * @return
	 */
	public static int getOpposeColor(int color)
	{
		return Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
	}
}
