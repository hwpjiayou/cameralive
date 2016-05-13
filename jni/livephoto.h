#include <jni.h>

//��ӡlog���е���
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "JNI_LOG", __VA_ARGS__)

//����ͷ�ļ�
#include "recorder/recorder.h"

#ifndef _LIVEPHOTO_H_
#define _LIVEPHOTO_H_
#ifdef __cplusplus
extern "C" {
#endif

//jni��java������ �������ɶ��д洢��Ƶ��Ƶ ����� ����
Recorder recorder;


/**
 * ���������Ԥ����С �� �û���Ԥ���ı���
 */
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniSetPreviewSizeAndRatio(JNIEnv *env, jobject obj,
		jint width,
		jint height,
		jfloat ratio);


/**
 * ���յ�һ֡��Ƶʱ����
 */
JNIEXPORT void JNICALL Java_com_deepcolor_deepred_shot_ShotInstance_jniOnReceiveVideoFrame(JNIEnv *env, jobject obj,
		jbyteArray data,
		jint len,
		jint width,
		jint height,
		jfloat ratio,
		jint camera_facing);

/**
 * ���յ���Ƶ����ʱ
 */
JNIEXPORT void JNICALL Java_com_deepcolor_deepred_shot_ShotInstance_jniOnReceiveAudioFrame(JNIEnv *env, jobject obj,
		jbyteArray data,
		jint len);


/************************************************************************/
/* ��ʼ��һ�������� ��ǰ������ ���浱ǰ������
�����ر�������id
С��0��ʾ��������ʼ��ʧ��*/
/************************************************************************/
//int init_encoder(char* file_path);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniInitEncoder(JNIEnv *env, jobject obj,
		jstring path);


/*
��id��Ӧ��encdoer��ʼ���б���  ���ʧ�ܷ���С��0
*/
//int start_encoding(int id);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniStartEncoding(JNIEnv *env, jobject obj,
		jint id);


/************************************************************************/
/* ���ڶ����ֵ�������ӵ� encoder��
��Ϊ��Ƶ��¼���� ǰ1.5s�ͺ�1.5s ��һ�θ��Ƶ�����Ϊǰ1.5s ��Ҫ����һ���ӳ���Ϣ ���ڶ����ֵ����� ��ӵ���Ӧ�ı�������*/
/************************************************************************/
//int add_second_part_to_encoder(int id);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniAddSecondPartToEncoder(JNIEnv *env, jobject obj,
		jint id);

/************************************************************************/
/* �������еı��������� �����еı����� �ı�־λ������Ϊfalse
�л�����ͷ��ʱ����Ҫ���� ��ʱǰ������ͷ���ܻ���*/
/************************************************************************/
//void end_all_encoder();
JNIEXPORT void Java_com_deepcolor_deepred_shot_ShotInstance_jniEndAllEncoder(JNIEnv *env, jobject obj);

/**
 * ������Ƶ �� ��Ƶ ���е�����
 */
//void clear_data();
JNIEXPORT void Java_com_deepcolor_deepred_shot_ShotInstance_jniClearData(JNIEnv *env, jobject obj);


#ifdef __cplusplus
}
#endif
#endif













