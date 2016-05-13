#ifndef _RECORDER_H_
#define _RECORDER_H_

#include <iostream>
#include <queue>

#include <stdio.h>    
#include <sys/time.h>
#include <time.h>

#include <pthread.h>

#include <map>  

/*
#ifdef __GNUC__  
#include <ext/hash_map>  
#else  
#include <hash_map>  
#endif 
*/

#include <hash_map>

#include "content.h"
#include "loop_queue.h"
#include "encoder.h"

//��ӡlog���е���
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "JNI_LOG", __VA_ARGS__)

using namespace std;

/************************************************************************/
/* ¼����Ƶ�� ��ʼ���������� ������Ƶ����Ƶ���� 
ÿ�����µ����ݹ���ʱ  ���뵽������ 
���еĳ����� 1.5s ��������Ƶ����Ƶ����*/
/************************************************************************/
class Recorder
{
private:
	long pre_video_frame_time;	//�ϴδ洢�������е���Ƶ֡��ʱ�� ������Ƶ֮֡���ʱ������Ҫ>= VIDEO_FRAME_INTER
	queue<VideoFrame*> video_que;	//һ������ �洢 ��Ƶ֡����

	LoopQueue audio_que;	//�洢��Ƶ���� ����

	//���ڶԶ��м���
	pthread_mutex_t video_queue_mutex;	//��Ƶ������
	pthread_mutex_t audio_queue_mutex;	//��Ƶ������

private:
	int encoder_id;	//������id������
	hash_map<int, Encoder*> encoders;	//id �Ͷ�Ӧ�ı�����

	//hashmap����
	pthread_mutex_t encoders_mutex;	//encoders��

private:
	/**
	 * ����Ƶ�������й�
	 */

	/**
	 * ���ϲ㴫��һ��ͼƬ �����ü���ת֮����뵽src_frame_buffer��
	 * ����src�Ŀ�߱������û�Ԥ�����ı�����ͬ
	 */
	int src_width;
	int src_height;
	uint8_t* src_frame_buffer;

	/**
	 * ͼƬֻ����ת�� ��������
	 */
	int dst_width;
	int dst_height;
	uint8_t* dst_frame_buffer;

	/**
	 * ����ͼƬ������
	 */
	AVFrame* src_frame;
	AVFrame* dst_frame;

	//����ͼƬ������ ��ͼƬ��src_frame���ŵ� dst_frame
	struct SwsContext *sws_ctx;

public:
	Recorder();	//���캯��
	~Recorder();

	/**
	 * ���������Ԥ����С �� �û���Ԥ���ı���
	 */
	int set_preview_size_and_ratio(int width, int height, float ratio);

	/************************************************************************/
	/* ������Ƶ֡����ʱ  ��ǰ����Ƶ֡��Ϊ������ת�Ͳü�������Ƶ֡ 
	������ǰ������ͷ���Ǻ�������ͷ����Ҫ����Ƶ֡���вü�����ת
	�ü���Ҫ���� ����ͷ��Ԥ���������вü�
	���ݸ�ʽΪnv21*/
	/************************************************************************/
	void on_receive_video_frame(uint8_t* data, int len, int width, int height, float ratio, int camera_facing);

	/************************************************************************/
	/* ���յ� ��Ƶ����ʱ                                                                     */
	/************************************************************************/
	void on_receive_audio_frame(uint8_t* data, int len);

	/************************************************************************/
	/* ��ʼ��һ�������� ��ǰ������ ���浱ǰ������    
	�����ر�������id
	С��0��ʾ��������ʼ��ʧ��*/
	/************************************************************************/
	int init_encoder(char* file_path);

	/*
	��id��Ӧ��encdoer��ʼ���б���  ���ʧ�ܷ���С��0
	*/
	int start_encoding(int id);

	/************************************************************************/
	/* ���ڶ����ֵ�������ӵ� encoder��
	��Ϊ��Ƶ��¼���� ǰ1.5s�ͺ�1.5s ��һ�θ��Ƶ�����Ϊǰ1.5s ��Ҫ����һ���ӳ���Ϣ ���ڶ����ֵ����� ��ӵ���Ӧ�ı�������*/
	/************************************************************************/
	int add_second_part_to_encoder(int id);

	/************************************************************************/
	/* �������еı��������� �����еı����� �ı�־λ������Ϊfalse
	�л�����ͷ��ʱ����Ҫ���� ��ʱǰ������ͷ���ܻ���*/
	/************************************************************************/
	void end_all_encoder();

	/**
	 * ������Ƶ �� ��Ƶ ���е�����
	 */
	void clear_data();
private:
	//˳ʱ����ת90�� Ȼ��ü� ���ݸ�ʽΪnv21
	//�����������ͷͼƬ
	//�����Ͻǿ�ʼ�ü�
	int rotate_cw90_cut_nv21(uint8_t* dst, int dst_w, int dst_h, uint8_t* src, int src_w, int src_h);

	//��ʱ����ת90�� Ȼ����вü�
	//����ǰ������ͷ��ͼƬ
	//�����Ͻǿ�ʼ�ü�
	int rotate_acw90_cut_nv21(uint8_t* dst, int dst_w, int dst_h, uint8_t* src, int src_w, int src_h);

	/************************************************************************/
	/* �õ���ǰ��ϵͳʱ�� ����                                                                     */
	/************************************************************************/
	long get_cur_time();
};

#endif
