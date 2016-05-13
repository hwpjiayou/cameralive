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

#ifndef _ENCODER_H_
#define _ENCODER_H_

#include <stdio.h>
#include <iostream>
#include <string>
#include <queue>
#include <pthread.h>

#include "content.h"
#include "loop_queue.h"

using namespace std;

//��ӡlog���е���
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "JNI_LOG", __VA_ARGS__)

extern "C"
{
#include <libavutil/opt.h>
#include "libavutil/channel_layout.h"
#include "libavutil/mathematics.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
};




typedef struct OutputStream
{
    AVStream *stream;    //��Ӧ����
    AVCodec *pCodec;    //������
    AVCodecContext *pCodecCtx;   //������������
    
    int64_t next_pts;   //��Ƶ������Ƶ��Ӧ����һ֡�ĸ��� ���� �Ѿ����ڵĸ���
    int64_t samples_count;  //��Ҳ��֪��Ϊʲô����� ���񲻼� ������
    
    /************************************************************************/
    /* ����ԭʼ����Ƶ���ݸ�ʽ��nv21�����ڶ�ȡԭʼ���ݵ���������� ����Ҫ���и�ʽ��ת��
     ����Ӧ����Ƶ��ʽ��16λ ���б���ʱ���ܲ�֧�� ���� ��Ҫ������Ƶ��ʽ��ת��*/
    /************************************************************************/
    
    //�洢δ����� ��Ƶ����Ƶ ����
    //������Ƶ������ԭʼ��YUV420����
    //��������Ƶ���Ǿ���16λת������Ƶ���� �����ʽ��ȷ�� ��Ҫ����find����codec����ȷ��
    AVFrame *frame;         //������Ƶ�洢yuv���� ������Ƶ��ȷ��
    AVFrame *temp_frame;	//ֻ�����Ƶ������Ч �洢ԭʼ��16λ��Ƶ����
    
    //������Ƶ��ת�� ������Ƶ��˵����Ҫ
    /* swr_init()����ʼ��libswresample�е�SwrContext��libswresample������Ƶ�����������ݣ�PCM����ת����
     swr_convert()��ת����Ƶ�����ʵ��ʺ�ϵͳ���ŵĸ�ʽ��
     swr_free()���ͷ�SwrContext��                                                                     */
    struct SwrContext *swr_ctx;
    
} OutputStream;


/************************************************************************/
/* �����ࣺ����Ӷ����п������� Ȼ�� ����ffmpeg������Ƶ�ı���                                                                     */
/************************************************************************/
class Encoder
{
private:
	int id;	//id���ڱ�ʶ��ǰ�ı����� ����ͬʱ������������� һ����Ҫһ��id���б�־

	queue<VideoFrame*> video_que;	//�洢��Ƶ֡����
	LoopQueue audio_que;			//�洢��Ƶ����

	//���ڶԶ��м���
	pthread_mutex_t video_queue_mutex;	//��Ƶ������
	pthread_mutex_t audio_queue_mutex;	//��Ƶ������

	bool is_encoding;	//һ����־Ϊ ����Ϊtrue��ʱ���ʾ ���Խ��б��� ����������Ϊ����is_encodingΪfalse��ʱ�� ���Ƴ�ѭ������

    string file_path;   //��������Ƶ�洢��λ�� ��һ��·�� Ӧ����mp4��β ��java�㴫�� jni���ı�
    
    float video_bit_rates[2][2];    //�洢��Ƶbit�ʵķ�Χ ���ݸ߶ȼ����Ӧ��bit��

public:
	/************************************************************************/
	/* ���캯�� ����һ��id                                                                     */
	/************************************************************************/
	Encoder(int, char*);

	//��������
	~Encoder();

	//�õ���ǰ��������id
	int get_id();

	/************************************************************************/
	/* ������������Ƶ֡                                                                     */
	/************************************************************************/
	void add_frame_to_video_que(queue<VideoFrame*>& que);

	/************************************************************************/
	/* ������������Ƶ֡����                                                                     */
	/************************************************************************/
	void add_frame_to_audio_que(LoopQueue& que);

	//���ñ����־λ
	void set_is_encoding(bool);

	/************************************************************************/
	/* �õ������־λ                                                                     */
	/************************************************************************/
	bool get_is_encoding();

	/************************************************************************/
	/* ��ʼ���� �������е����ݱ������Ƶ�洢����Ӧ��λ��                                                                      */
	/************************************************************************/
	int start_encoding();
    
private:
    //������Ƶ�߶ȵõ���Ӧ��bitrate
    int get_bit_rate_by_height(int height);
    
    //�����Ƶ��
    int add_video_stream(OutputStream* oStream, AVFormatContext *pFormatCtx, enum AVCodecID codec_id, int video_width, int video_height, float bit_rate);
    
    //�����Ƶ��
    int add_audio_stream(OutputStream* oStream, AVFormatContext *pFormatCtx, enum AVCodecID codec_id);

    //����Ƶ����
    int open_video(OutputStream* oStream);

	//����Ƶ����
    int open_audio(OutputStream* oStream, int* temp_frame_size, uint8_t** temp_frame_buffer, int* frame_size, uint8_t** frame_buffer);

    /**
     * ��һ֡��Ƶ���б��� ���ȴӶ����ж�ȡһ֡���� Ȼ����б���
     */
    bool write_video_frame(OutputStream* oStream, AVFormatContext *pFormatCtx, int width, int height);

    /**
     * ��ȡ��Ƶ ���ݲ�����
     */
    bool write_audio_frame(OutputStream* oStream, AVFormatContext *pFormatCtx, uint8_t* buffer, int buffer_size);

    //������������
    void flush_video_encoder(OutputStream* oStream, AVFormatContext *pFormatCtx);
    void flush_audio_encoder(OutputStream* oStream, AVFormatContext *pFormatCtx);
};

#endif
