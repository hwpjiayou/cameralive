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

public class MathUtil 
{
	//��׼����ֵ
	public static int clamp(int val, int min, int max)
	{
		if(val < min)	return min;
		if(val > max)	return max;
		
		return val;
	}
	
	public static float clamp(float val, float min, float max)
	{
		if(val < min)	return min;
		if(val > max)	return max;
		
		return val;
	}
}
