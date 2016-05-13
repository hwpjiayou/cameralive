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

#ifndef _CONTENT_H_
#define _CONTENT_H_

extern "C"
{
#include "libavformat/avformat.h"
}

/************************************************************************/
/* �洢 ��Ҫ�ĳ���                                                                     */
/************************************************************************/

#define CAMERA_FACING_FRONT 0	//��־ǰ������ͷ   
#define CAMERA_FACING_BACK  1
//#define MIN_PREVIEW_WIDTH 1080	//ͼƬ����С��ȴ�android��������ͼƬ����С��� ����1080
//#define MIN_PREVIEW_HEIGHT 1080	//ͼƬ����С��ȴ�android��������ͼƬ����С��� ����1080

#define TIME_DURATION 1.5	//�洢����Ƶ����Ƶ���� 1.5s�ĳ��� ǰ��1.5s

#define VIDEO_FPS 15	//1s�洢������Ƶ֡ ͬʱ������Ƶ֡��
#define VIDEO_FRAME_INTER 1000/VIDEO_FPS	//������Ƶ֮֡���ʱ����
#define VIDEO_FRAME_NUM TIME_DURATION*VIDEO_FPS	//��Ҫ�洢����Ƶ֡�ĸ���
#define VIDEO_PIX_FMT AV_PIX_FMT_YUV420P	//��Ƶ������Ҫ�����ʽ
#define SRC_VIDEO_PIX_FMT AV_PIX_FMT_NV21    //��java��������ݾ���nv21��ʽ ��Ҫ����ת�����ŵȲ���


#define AUDIO_SAMPLE_RATE 44100	//������ �� 1s�������ٴ�
#define AUDIO_CHANNEL_NUM 1	//�������� 1 �� ��Ƶ�������
#define AUDIO_SAMPLE_BYTE_NUM 2	//ÿһ�������� ռ2byte �� ������ʽ��:����Ϊ16bit
#define AUDIO_FRAME_NUM TIME_DURATION*AUDIO_SAMPLE_RATE*AUDIO_CHANNEL_NUM*AUDIO_SAMPLE_BYTE_NUM	//1.5��Ҫ��byte���� ʱ��*������*��������*ÿ����������ռ��byte
#define AUDIO_PIX_FMT AV_SAMPLE_FMT_S16  //��������Ƶ���ݵĸ�ʽ
#define AUDIO_BIT_RATE 64000	//��Ƶbit��
#define AUDIO_CHANNEL_LAYOUT AV_CH_LAYOUT_MONO	//��Ƶ��������Ӧ��CHANNEL_LAYOUT ������?

/************************************************************************/
/* �洢һ֡ ��Ƶ����                                                                      */
/************************************************************************/
struct VideoFrame
{
	uint8_t* data;	//byte���� �洢һ֡����Ƶ����
	int width;	//��Ƶ�Ŀ����߶�
	int height;

	VideoFrame()
	{
		data = NULL;
		width  = -1;
		height = -1;
	}

	VideoFrame(VideoFrame* frame)
	{
		if (NULL == frame)	return;

		width  = frame->width;
		height = frame->height;

		int len = width * height * 3 / 2;

		data   = (uint8_t*)malloc(len);

		//��������
		for(int i = 0; i < len; ++i)
			data[i] = frame->data[i];
	}

	VideoFrame(uint8_t* d, int w, int h)
	{
		if(NULL == d || 0 >= w || 0 >= h)
			return;

		int len = w * h * 3 / 2;
		data = (uint8_t*)malloc(len);
		for(int i = 0; i < len; ++i)
			data[i] = d[i];

		width = w;
		height = h;
	}

	//�����ڴ�
	~VideoFrame()
	{
		if(NULL != data)
			free(data);
	}
};

#endif




















