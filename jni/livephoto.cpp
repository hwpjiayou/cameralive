#include "livephoto.h"

/**
 * ���������Ԥ����С �� �û���Ԥ���ı���
 */
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniSetPreviewSizeAndRatio(JNIEnv *env, jobject obj,
		jint width,
		jint height,
		jfloat ratio)
{
	int ret = recorder.set_preview_size_and_ratio(width, height, ratio);

	return ret;
}


/**
 * ���յ�һ֡��Ƶʱ����
 */
JNIEXPORT void JNICALL Java_com_deepcolor_deepred_shot_ShotInstance_jniOnReceiveVideoFrame(JNIEnv *env, jobject obj,
		jbyteArray data,
		jint len,
		jint width,
		jint height,
		jfloat ratio,
		jint camera_facing)
{
	uint8_t* temp_data = (uint8_t*)(env->GetByteArrayElements(data, NULL));

	recorder.on_receive_video_frame(temp_data, len, width, height, ratio, camera_facing);

	env->ReleaseByteArrayElements(data, (jbyte*)temp_data, 0);
}

/**
 * ���յ���Ƶ����ʱ
 */
JNIEXPORT void JNICALL Java_com_deepcolor_deepred_shot_ShotInstance_jniOnReceiveAudioFrame(JNIEnv *env, jobject obj,
		jbyteArray data,
		jint len)
{
	uint8_t* temp_data = (uint8_t*)(env->GetByteArrayElements(data, NULL));

	recorder.on_receive_audio_frame(temp_data, len);

	env->ReleaseByteArrayElements(data, (jbyte*)temp_data, 0);
}

/************************************************************************/
/* ��ʼ��һ�������� ��ǰ������ ���浱ǰ������
�����ر�������id
С��0��ʾ��������ʼ��ʧ��*/
/************************************************************************/
//int init_encoder(char* file_path);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniInitEncoder(JNIEnv *env, jobject obj,
		jstring path)
{
	char* file_path = (char*)env->GetStringUTFChars(path, NULL);

	int ret = recorder.init_encoder(file_path);

	env->ReleaseStringUTFChars(path, file_path);

	return ret;
}


/*
��id��Ӧ��encdoer��ʼ���б���  ���ʧ�ܷ���С��0
*/
//int start_encoding(int id);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniStartEncoding(JNIEnv *env, jobject obj,
		jint id)
{
	int ret = recorder.start_encoding(id);

	return ret;
}


/************************************************************************/
/* ���ڶ����ֵ�������ӵ� encoder��
��Ϊ��Ƶ��¼���� ǰ1.5s�ͺ�1.5s ��һ�θ��Ƶ�����Ϊǰ1.5s ��Ҫ����һ���ӳ���Ϣ ���ڶ����ֵ����� ��ӵ���Ӧ�ı�������*/
/************************************************************************/
//int add_second_part_to_encoder(int id);
JNIEXPORT jint Java_com_deepcolor_deepred_shot_ShotInstance_jniAddSecondPartToEncoder(JNIEnv *env, jobject obj,
		jint id)
{
	int ret = recorder.add_second_part_to_encoder(id);

	return ret;
}

/************************************************************************/
/* �������еı��������� �����еı����� �ı�־λ������Ϊfalse
�л�����ͷ��ʱ����Ҫ���� ��ʱǰ������ͷ���ܻ���*/
/************************************************************************/
//void end_all_encoder();
JNIEXPORT void Java_com_deepcolor_deepred_shot_ShotInstance_jniEndAllEncoder(JNIEnv *env, jobject obj)
{
	recorder.end_all_encoder();
}

/**
 * ������Ƶ �� ��Ƶ ���е�����
 */
//void clear_data();
JNIEXPORT void Java_com_deepcolor_deepred_shot_ShotInstance_jniClearData(JNIEnv *env, jobject obj)
{
	recorder.clear_data();
}











