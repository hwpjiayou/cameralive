#include "recorder.h"

Recorder::Recorder():pre_video_frame_time(-1), audio_que(AUDIO_FRAME_NUM), encoder_id(0)
{
	/**
	 *��ʼ��ԭʼֵ
	 */
	src_width = -1;
	src_height = -1;
	src_frame_buffer = NULL;

	dst_width = -1;
	dst_height = -1;
	dst_frame_buffer = NULL;

	src_frame = NULL;
	dst_frame = NULL;

	sws_ctx = NULL;

	//��ʼ�� �߳���
	pthread_mutex_init(&video_queue_mutex, NULL);
	pthread_mutex_init(&audio_queue_mutex, NULL);
	pthread_mutex_init(&encoders_mutex, NULL);
}

//��������
Recorder::~Recorder()
{


	while (!video_que.empty())
	{
		VideoFrame* frame = video_que.front();
		video_que.pop();

		if(NULL != frame)
			delete frame;
	}

	//ɾ��hashmap�е�����
	hash_map<int, Encoder*>::iterator it = encoders.begin();
	for (; it != encoders.end(); ++it)
	{
		Encoder* encoder = it->second;

		if (NULL != encoder)
			delete encoder;
	}
	encoders.clear();

	/**
	 * �ͷź������йص�����
	 */
	if(NULL != src_frame_buffer)
		av_free(src_frame_buffer);

	if(NULL != dst_frame_buffer)
		av_free(dst_frame_buffer);

	if(NULL != src_frame)
		av_frame_free(&src_frame);

	if(NULL != dst_frame)
		av_frame_free(&dst_frame);

	if (NULL != sws_ctx)
		sws_freeContext(sws_ctx);

	//���� ������
	pthread_mutex_destroy(&video_queue_mutex);
	pthread_mutex_destroy(&audio_queue_mutex);
	pthread_mutex_destroy(&encoders_mutex);
}

/**
 * ���������Ԥ����С �� �û���Ԥ���ı���
 */
int Recorder::set_preview_size_and_ratio(int width, int height, float ratio)
{
	if(0 >= width || 0 >= height || 0 >= ratio)
		return -1;

	/**
	 * ���ݴ���Ĳ����������õ�ǰ���ŵ�����
	 */
	if(NULL != src_frame_buffer)
		av_free(src_frame_buffer);

	if(NULL != dst_frame_buffer)
		av_free(dst_frame_buffer);

	if(NULL != src_frame)
		av_frame_free(&src_frame);

	if(NULL != dst_frame)
		av_frame_free(&dst_frame);

	if (NULL != sws_ctx)
		sws_freeContext(sws_ctx);

	src_width  = -1;
	src_height = -1;
	src_frame_buffer = NULL;

	dst_width = -1;
	dst_height = -1;
	dst_frame_buffer = NULL;

	src_frame = NULL;
	dst_frame = NULL;

	sws_ctx = NULL;

	/**
	 * ����cameraԤ������ �� ���ڿ����ı������¼����С
	 */
	//ע��ͼƬ����Ҫ������ת camera��ͼƬ������û���Ԥ����һ��
	if (1.0 * height / width > ratio)
	{
		src_width  = (int)(1.0 * ratio * width);
		src_height = width;
	}
	else
	{
		src_width = height;
		src_height = (int)(1.0 * height / ratio);
	}

	//��׼���� 2�ı���
	src_width  -= src_width  % 2;
	src_height -= src_height % 2;

	/**
	 * ֻ����ת�� �����������Ų���
	 */
	dst_width  = src_width;
	dst_height = src_height;

	/**
	 * ��ʼ���洢ԭʼͼƬ���ڴ�
	 */
	src_frame = av_frame_alloc();
	if(!src_frame)
		return -1;

	int src_frame_size = avpicture_get_size(SRC_VIDEO_PIX_FMT, src_width, src_height);
	src_frame_buffer = (uint8_t*)av_malloc(src_frame_size);
	avpicture_fill((AVPicture *)src_frame, src_frame_buffer, SRC_VIDEO_PIX_FMT, src_width, src_height);

	src_frame->width  = src_width;
	src_frame->height = src_height;
	src_frame->format = SRC_VIDEO_PIX_FMT;

	/**
	 * ��ʼ���ڴ�
	 */
	dst_frame = av_frame_alloc();
	if(!dst_frame)
		return -1;

	int dst_frame_size = avpicture_get_size(VIDEO_PIX_FMT, dst_width, dst_height);
	dst_frame_buffer = (uint8_t*)av_malloc(dst_frame_size);
	avpicture_fill((AVPicture *)dst_frame, dst_frame_buffer, VIDEO_PIX_FMT, dst_width, dst_height);

	dst_frame->width = dst_width;
	dst_frame->height = dst_height;
	dst_frame->format = VIDEO_PIX_FMT;

	/**
	 * ���ź���
	 */
	//��ʼ��ͼƬ���ź��� ��ccut_video_height ���ŵ� FRAME_HEIGHT
	sws_ctx = sws_getContext(src_width, src_height, SRC_VIDEO_PIX_FMT,
			dst_width, dst_height, VIDEO_PIX_FMT,
		SWS_FAST_BILINEAR, NULL, NULL, NULL);

	if(!sws_ctx)
		return -1;

	return 1;
}

/************************************************************************/
/* ������Ƶ֡����ʱ  ��ǰ����Ƶ֡��Ϊ������ת�Ͳü�������Ƶ֡
������ǰ������ͷ���Ǻ�������ͷ����Ҫ����Ƶ֡���вü�����ת
�ü���Ҫ���� ����ͷ��Ԥ���������вü�
���ݸ�ʽΪnv21

ratio����Ƶ��android�ֻ���Ԥ���� �����߶ȵı��� ԭʼͼƬ��Ҫ��������������вü�
*/
/************************************************************************/
void Recorder::on_receive_video_frame(uint8_t* data, int len, int width, int height, float ratio, int camera_facing)
{
	if (NULL == data || 0 >= len || 0 >= width || 0 >= height || 0 >= ratio ||
		(CAMERA_FACING_FRONT != camera_facing && CAMERA_FACING_BACK != camera_facing))
		return;

	//�õ���ǰ��ʱ��
	long cur_time = get_cur_time();

	//���ʱ�� ���� �����Ƶֱ֡�Ӷ���
	if (-1 != pre_video_frame_time && (cur_time - pre_video_frame_time) < VIDEO_FRAME_INTER)
		return;

	/**
	 * �õ�ԭʼ��cameraԤ��֮����Ҫ���вü���ת ���ŵ� ����
	 */
	if(0 >= src_width || 0 >= src_height || NULL == src_frame_buffer || NULL == src_frame ||
	   0 >= dst_width || 0 >= dst_height || NULL == dst_frame_buffer || NULL == dst_frame ||
	   NULL == sws_ctx)
		return;

	//-------------------------------------------------------------
	//LOGD("camera width:%d", width);
	//LOGD("camera height:%d", height);
	//LOGD("camera facing:%d", camera_facing);
	//---------------------------------------------------------

	/**
	 * ��ͼƬ�ü����ź� �ŵ�src_frame_buffer��
	 */
	if(CAMERA_FACING_FRONT == camera_facing)
	{
		if (0 > rotate_acw90_cut_nv21(src_frame_buffer, src_width, src_height, data, width, height))
		{
			return;
		}
	}
	else if(CAMERA_FACING_BACK == camera_facing)
	{
		//LOGD("if(CAMERA_FACING_BACK == camera_facing)");

		/**
		 * ��ͼƬ����˳ʱ����ת90�� Ȼ��ü���src_frame_buffer��
		 */
		if(0 > rotate_cw90_cut_nv21(src_frame_buffer, src_width, src_height, data, width, height))
		{
			return;
		}
	}

	/**
	 * �Բü��õ�ͼƬ���и�ʽת�� ��ִ������
	 * ���źø���ʱ�����
	 */
	sws_scale(sws_ctx,
			src_frame->data, src_frame->linesize,
			0, src_frame->height,
			dst_frame->data, dst_frame->linesize);

	//��ʱdst_frame�д洢��˵�Ź���yuv420p����Ƶ֡���� ���뵽�����м���

	//����һ֡����
	VideoFrame* frame = new VideoFrame(dst_frame_buffer, dst_width, dst_height);

	/************************************************************************/
	/*����洢����Ƶ֡�Ѿ��������� ɾ��һ��                                                                      */
	/************************************************************************/
	pthread_mutex_lock(&video_queue_mutex);	//����Ƶ���н��м���
	if (video_que.size() >= VIDEO_FRAME_NUM)
	{
		VideoFrame* front_frame = video_que.front();
		video_que.pop();

		delete front_frame;
	}

	video_que.push(frame);	//����Ƶ֡���뵽������
	pthread_mutex_unlock(&video_queue_mutex); //�ͷ������������߳�ʹ��  

	//����ʱ��
	pre_video_frame_time = cur_time;

	//����
	//LOGD("video size:%d", video_que.size());
}

/************************************************************************/
/* ���յ� ��Ƶ����ʱ                                                                     */
/************************************************************************/
void Recorder::on_receive_audio_frame(uint8_t* data, int len)
{
	if (NULL == data || 0 >= len)
		return;

	pthread_mutex_lock(&audio_queue_mutex);	//����Ƶ���н��м���
	for (int i = 0; i < len; ++i)
	{
		//������������� ����һ������
		if (audio_que.full())
			audio_que.pop();

		audio_que.push(data[i]);
	}
	pthread_mutex_unlock(&audio_queue_mutex); //�ͷ������������߳�ʹ��  
}

/************************************************************************/
/* ��ʼ��һ�������� ��ǰ������ ���浱ǰ������
�����ر�������id
С��0��ʾ��������ʼ��ʧ��*/
/************************************************************************/
int Recorder::init_encoder(char* file_path)
{
	//����id
	encoder_id++;

	//��ʼ��һ��������
	Encoder* encoder = new Encoder(encoder_id, file_path);

	//����ǰ����Ƶ����Ƶ���� ���뵽 ��������ȥ
	pthread_mutex_lock(&video_queue_mutex);
	encoder->add_frame_to_video_que(video_que);
	pthread_mutex_unlock(&video_queue_mutex);

	pthread_mutex_lock(&audio_queue_mutex);	//����Ƶ���н��м���
	encoder->add_frame_to_audio_que(audio_que);
	pthread_mutex_unlock(&audio_queue_mutex); //�ͷ������������߳�ʹ�� 

	//����Ӧ�ı��������� �� ������ ��ӵ�hash_map��
	pthread_mutex_lock(&encoders_mutex);
	encoders.insert(make_pair(encoder->get_id(), encoder));
	pthread_mutex_unlock(&encoders_mutex);

	//���ر�������id
	return encoder->get_id();
}

/*
��id��Ӧ��encdoer��ʼ���б���  ���ʧ�ܷ���С��0
����һ����ʱ���� ��Ҫ�� �˲��������߳��н���  ͬʱ ��Ҫ�����ݽ��м���
��Ϊ �п��ܻ�õ�һ���ӳ���Ϣ ��ӵڶ����ֵ�����
*/
int Recorder::start_encoding(int id)
{
	//-----------------------------------------------
	//LOGD("Recorder::start_encoding");
	//-----------------------------------------------

	//��ǰid������ ֱ�ӷ���
	if (encoders.end() == encoders.find(id))
		return -1;

	//-----------------------------------------------
	//LOGD("Encoder* encoder = encoders[id];");
	//-----------------------------------------------

	Encoder* encoder = encoders[id];	//�õ���ǰ������
	//encoders.erase(id);	//����ɾ�� �п�����Ҫ����add_second_part_to_encoder �����������

	if (NULL == encoder)
	{
		encoders.erase(id);
		return -1;
	}

	//-----------------------------------------------
	//LOGD("int ret = encoder->start_encoding();");
	//-----------------------------------------------

	/*
	��ʼ���� ��һ����ʱ���� 
	�ڱ���Ĺ����� �п��ܵ���add_second_part_to_encoder �����������
	*/
	int ret = encoder->start_encoding();

	//-----------------------------------------------
	//LOGD("ret:%d", ret);
	//-----------------------------------------------

	//��������� ��Ҫ�� ������ɾ�� �������ڴ� 
	encoders.erase(id);
	delete encoder;	//�����ڴ�

	return ret;
}

/************************************************************************/
/* ���ڶ����ֵ�������ӵ� encoder��
��Ϊ��Ƶ��¼���� ǰ1.5s�ͺ�1.5s ��һ�θ��Ƶ�����Ϊǰ1.5s ��Ҫ����һ���ӳ���Ϣ ���ڶ����ֵ����� ��ӵ���Ӧ�ı�������*/
/************************************************************************/
int Recorder::add_second_part_to_encoder(int id)
{
	//��ǰid������ ֱ�ӷ���
	if (encoders.end() == encoders.find(id))
		return -1;
	
	Encoder* encoder = encoders[id];	//�õ���ǰ������
	//encoders.erase(id);	//����ɾ�� ��������ڴ�й©  ���������ſ���ɾ��

	if (NULL == encoder)
	{
		encoders.erase(id);
		return -1;
	}

	//�����־λ�Ѿ�������Ϊfalse���������
	if (!encoder->get_is_encoding())
		return -1;

	//����ǰ����Ƶ����Ƶ���� ���뵽 ��������ȥ
	pthread_mutex_lock(&video_queue_mutex);
	encoder->add_frame_to_video_que(video_que);
	pthread_mutex_unlock(&video_queue_mutex);

	pthread_mutex_lock(&audio_queue_mutex);	//����Ƶ���н��м���
	encoder->add_frame_to_audio_que(audio_que);
	pthread_mutex_unlock(&audio_queue_mutex); //�ͷ������������߳�ʹ�� 

	//���ñ�־Ϊ
	encoder->set_is_encoding(false);

	return encoder->get_id();
}

/************************************************************************/
/* �������еı��������� �����еı����� �ı�־λ������Ϊfalse
�л�����ͷ��ʱ����Ҫ���� ��ʱǰ������ͷ���ܻ���*/
/************************************************************************/
void Recorder::end_all_encoder()
{
    //����
    pthread_mutex_lock(&encoders_mutex);
	hash_map<int, Encoder*>::iterator it = encoders.begin();
	for (; it != encoders.end(); ++it)
	{
		Encoder* encoder = it->second;

		if (NULL != encoder)
			encoder->set_is_encoding(false);
	}
    pthread_mutex_unlock(&encoders_mutex);
}

/**
 * ������Ƶ �� ��Ƶ ���е�����
 */
void Recorder::clear_data()
{
	pthread_mutex_lock(&video_queue_mutex);
	while (!video_que.empty())
	{
		VideoFrame* frame = video_que.front();
		video_que.pop();

		if(NULL != frame)
			delete frame;
	}
	pthread_mutex_unlock(&video_queue_mutex);

	pthread_mutex_lock(&audio_queue_mutex);	//����Ƶ���н��м���
	audio_que.clear();
	pthread_mutex_unlock(&audio_queue_mutex); //�ͷ������������߳�ʹ��
}


//˳ʱ����ת90�� Ȼ��ü� ���ݸ�ʽΪnv21
int Recorder::rotate_cw90_cut_nv21(uint8_t* dst, int dst_w, int dst_h, uint8_t* src, int src_w, int src_h)
{
	if (dst_w > src_h || dst_h > src_w)
		return -1;

	int n = 0;
	int src_s = src_w * src_h;	//ԭʼ����ͼƬ��С
	int n_pos;

	//int src_hw = src_w >> 1;	//����2
	int src_hh = src_h >> 1;
	int src_hs = src_w * src_hh;	//ԭʼUV����ռ�õ�byte��

	int dst_hw = dst_w >> 1;
	//int dst_hh = dst_h >> 1;

	//����Y
	for (int i = 0; i < dst_h; ++i)
	{
		n_pos = src_s;
		for (int j = 0; j < dst_w; ++j)
		{
			n_pos -= src_w;

			dst[n++] = src[n_pos + i];
		}
	}

	//����UV
	uint8_t* temp = src + src_s;
	for (int i = 0; i < dst_h; i += 2)
	{
		n_pos = src_hs;
		for (int j = 0; j < dst_hw; ++j)
		{
			n_pos -= src_w;

			dst[n++] = temp[n_pos + i];
			dst[n++] = temp[n_pos + i + 1];
		}
	}

	return 1;
}

//��ʱ����ת90�� Ȼ����вü�
//����ǰ������ͷ��ͼƬ
int Recorder::rotate_acw90_cut_nv21(uint8_t* dst, int dst_w, int dst_h, uint8_t* src, int src_w, int src_h)
{
	if (dst_w > src_h || dst_h > src_w)
		return -1;

	int n = 0;
	int n_pos;

	int src_hh = src_h >> 1;
	int dst_hw = dst_w >> 1;

	//����Y
	for (int i = 0; i < dst_h; ++i)
	{
		n_pos = (src_h - dst_w) * src_w;
		for (int j = 0; j < dst_w; ++j)
		{
			dst[n++] = src[n_pos + (src_w - 1 - i)];

			n_pos += src_w;
		}
	}

	//����UV
	uint8_t* temp = src + src_w * src_h;
	int index;
	for (int i = 0; i < dst_h; i += 2)
	{
		n_pos = (src_hh - dst_hw) * src_w;
		index = src_w - i - 2;

		for (int j = 0; j < dst_hw; ++j)
		{
			dst[n++] = temp[n_pos + index];
			dst[n++] = temp[n_pos + index + 1];

			n_pos += src_w;
		}
	}

	return 1;
}

/************************************************************************/
/* �õ���ǰ��ϵͳʱ�� ����                                                                     */
/************************************************************************/
long Recorder::get_cur_time()
{
	struct timeval tv;
	gettimeofday(&tv, NULL);

	return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}
