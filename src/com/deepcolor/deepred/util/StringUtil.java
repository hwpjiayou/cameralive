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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import android.text.TextPaint;
import android.text.TextUtils;

public class StringUtil {
	public static final String EMPTY = "";

	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
	private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd hh:mm:ss";
	/** ���������ļ� */
	private static final String DEFAULT_FILE_PATTERN = "yyyy-MM-dd-HH-mm-ss";
	private static final double KB = 1024.0;
	private static final double MB = 1048576.0;
	private static final double GB = 1073741824.0;
	public static final SimpleDateFormat DATE_FORMAT_PART = new SimpleDateFormat(
			"HH:mm");

	public static String currentTimeString() {
		return DATE_FORMAT_PART.format(Calendar.getInstance().getTime());
	}

	public static char chatAt(String pinyin, int index) {
		if (pinyin != null && pinyin.length() > 0)
			return pinyin.charAt(index);
		return ' ';
	}

	/** ��ȡ�ַ������ */
	public static float GetTextWidth(String Sentence, float Size) {
		if (isEmpty(Sentence))
			return 0;
		TextPaint FontPaint = new TextPaint();
		FontPaint.setTextSize(Size);
		return FontPaint.measureText(Sentence.trim()) + (int) (Size * 0.1); // �������
	}

	/**
	 * ��ʽ�������ַ���
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);
	}

	public static String formatDate(long date, String pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(new Date(date));
	}

	/**
	 * ��ʽ�������ַ���
	 * 
	 * @param date
	 * @return ����2011-3-24
	 */
	public static String formatDate(Date date) {
		return formatDate(date, DEFAULT_DATE_PATTERN);
	}

	public static String formatDate(long date) {
		return formatDate(new Date(date), DEFAULT_DATE_PATTERN);
	}

	/**
	 * ��ȡ��ǰʱ�� ��ʽΪyyyy-MM-dd ����2011-07-08
	 * 
	 * @return
	 */
	public static String getDate() {
		return formatDate(new Date(), DEFAULT_DATE_PATTERN);
	}

	/** ����һ���ļ�����������׺ */
	public static String createFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat(DEFAULT_FILE_PATTERN);
		return format.format(date);
	}

	/**
	 * ��ȡ��ǰʱ��
	 * 
	 * @return
	 */
	public static String getDateTime() {
		return formatDate(new Date(), DEFAULT_DATETIME_PATTERN);
	}

	/**
	 * ��ʽ������ʱ���ַ���
	 * 
	 * @param date
	 * @return ����2011-11-30 16:06:54
	 */
	public static String formatDateTime(Date date) {
		return formatDate(date, DEFAULT_DATETIME_PATTERN);
	}

	public static String formatDateTime(long date) {
		return formatDate(new Date(date), DEFAULT_DATETIME_PATTERN);
	}

	/**
	 * ������ʱ��ת��
	 * 
	 * @param gmt
	 * @return
	 */
	public static String formatGMTDate(String gmt) {
		TimeZone timeZoneLondon = TimeZone.getTimeZone(gmt);
		return formatDate(Calendar.getInstance(timeZoneLondon)
				.getTimeInMillis());
	}

	/**
	 * ƴ������
	 * 
	 * @param array
	 * @param separator
	 * @return
	 */
	public static String join(final ArrayList<String> array,
			final String separator) {
		StringBuffer result = new StringBuffer();
		if (array != null && array.size() > 0) {
			for (String str : array) {
				result.append(str);
				result.append(separator);
			}
			result.delete(result.length() - 1, result.length());
		}
		return result.toString();
	}

	public static String join(final Iterator<String> iter,
			final String separator) {
		StringBuffer result = new StringBuffer();
		if (iter != null) {
			while (iter.hasNext()) {
				String key = iter.next();
				result.append(key);
				result.append(separator);
			}
			if (result.length() > 0)
				result.delete(result.length() - 1, result.length());
		}
		return result.toString();
	}

	/**
	 * �ж��ַ����Ƿ�Ϊ��
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0 || str.equalsIgnoreCase("null");
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 
	 * @param str
	 * @return
	 */
	public static String trim(String str) {
		return str == null ? EMPTY : str.trim();
	}

	/**
	 * ת��ʱ����ʾ
	 * 
	 * @param time
	 *            ����
	 * @return
	 */
	public static String generateTime(long time) {
		int totalSeconds = (int) (time / 1000);
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes,
				seconds) : String.format("%02d:%02d", minutes, seconds);
	}

	public static boolean isBlank(String s) {
		return TextUtils.isEmpty(s);
	}

	/** �������ٻ�ȡʱ���ʽ */
	public static String gennerTime(int totalSeconds) {
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}

	/**
	 * ת���ļ���С
	 * 
	 * @param size
	 * @return
	 */
	public static String generateFileSize(long size) {
		String fileSize;
		if (size < KB)
			fileSize = size + "B";
		else if (size < MB)
			fileSize = String.format("%.1f", size / KB) + "KB";
		else if (size < GB)
			fileSize = String.format("%.1f", size / MB) + "MB";
		else
			fileSize = String.format("%.1f", size / GB) + "GB";

		return fileSize;
	}

	/** �����ַ������ҵ����أ�û�ҵ����ؿ� */
	public static String findString(String search, String start, String end) {
		int start_len = start.length();
		int start_pos = StringUtil.isEmpty(start) ? 0 : search.indexOf(start);
		if (start_pos > -1) {
			int end_pos = StringUtil.isEmpty(end) ? -1 : search.indexOf(end,
					start_pos + start_len);
			if (end_pos > -1)
				return search.substring(start_pos + start.length(), end_pos);
		}
		return "";
	}

	/**
	 * ��ȡ�ַ���
	 * 
	 * @param search
	 *            ���������ַ���
	 * @param start
	 *            ��ʼ�ַ��� ���磺<title>
	 * @param end
	 *            �����ַ��� ���磺</title>
	 * @param defaultValue
	 * @return
	 */
	public static String substring(String search, String start, String end,
			String defaultValue) {
		int start_len = start.length();
		int start_pos = StringUtil.isEmpty(start) ? 0 : search.indexOf(start);
		if (start_pos > -1) {
			int end_pos = StringUtil.isEmpty(end) ? -1 : search.indexOf(end,
					start_pos + start_len);
			if (end_pos > -1)
				return search.substring(start_pos + start.length(), end_pos);
			else
				return search.substring(start_pos + start.length());
		}
		return defaultValue;
	}

	/**
	 * ��ȡ�ַ���
	 * 
	 * @param search
	 *            ���������ַ���
	 * @param start
	 *            ��ʼ�ַ��� ���磺<title>
	 * @param end
	 *            �����ַ��� ���磺</title>
	 * @return
	 */
	public static String substring(String search, String start, String end) {
		return substring(search, start, end, "");
	}

	/**
	 * ƴ���ַ���
	 * 
	 * @param strs
	 * @return
	 */
	public static String concat(String... strs) {
		StringBuffer result = new StringBuffer();
		if (strs != null) {
			for (String str : strs) {
				if (str != null)
					result.append(str);
			}
		}
		return result.toString();
	}

	/**
	 * Helper function for making null strings safe for comparisons, etc.
	 * 
	 * @return (s == null) ? "" : s;
	 */
	public static String makeSafe(String s) {
		return (s == null) ? "" : s;
	}
}
