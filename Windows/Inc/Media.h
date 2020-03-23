#pragma once

// ý����ؽӿ�

#ifdef _WIN32

struct rc_device_list
{
#define MaxDeviceCount (10u)
	wchar_t* list[MaxDeviceCount];
	uint32_t total;
	uint32_t selected; //��ǰѡ����豸
};

// �豸����
struct rc_device_manager
{
	// ����ͷö�� / ѡ��
	virtual uint32_t camera_flush(void) = 0;
	virtual rc_bool camera_list(rc_device_list*) = 0; // �����б�
	virtual rc_bool camera_select(uint32_t index) = 0; // �ı�ѡ��

	// ����ö�� / ѡ��
	virtual uint32_t sound_playback_flush(void) = 0;
	virtual rc_bool sound_playback_list(rc_device_list*) = 0;
	virtual rc_bool sound_playback_select(const wchar_t*) = 0;

	virtual uint32_t sound_record_flush(void) = 0;
	virtual rc_bool sound_record_list(rc_device_list*) = 0;
	virtual rc_bool sound_record_select(const wchar_t*) = 0;

	// ���� (ȡֵ��Χ: 0-100)
	virtual void set_playback_volume(int vol) = 0; //�����������
	virtual int get_playback_volume() = 0;

	virtual void set_record_volume(int vol) = 0; //����ɼ�����
	virtual int get_record_volume() = 0;

	virtual BOOL set_mic_volume(int vol) = 0; //Ӳ���ɼ�����
	virtual int get_mic_volume() = 0;
};
#endif

// ��Ƶ��Ϣ
struct rc_video_info
{
	uint32_t width, height; //�ֱ���

	uint32_t fps; //����֡��

	char codec[8u]; //��������: "h265" or "h264"
};

// ����ָʾ
struct rc_sound_wave
{
	uint8_t wave[12]; //range: [0-255]
} ;

// �źŲ� (һ���ź�Դ����������)
enum rc_layer
{
	video_layer_lowest, //����� (144P)
	video_layer_low, //���� (240P)
	video_layer_medium, //�е� (480P)
	video_layer_high, //���� (720P)
	video_layer_highest, //����� (1080P)

	audio_layer_high =8u,	//��������
};

enum rc_layer_bit
{
	layer_bit_video_lowest		= (1u << video_layer_lowest),
	layer_bit_video_low			= (1u << video_layer_low),
	layer_bit_video_medium		= (1u << video_layer_medium),
	layer_bit_video_high		= (1u << video_layer_high),
	layer_bit_video_highest		= (1u << video_layer_highest),

	layer_bit_audio				= (1u << audio_layer_high),
};
typedef void* signal_handle;

// �ź�Դ�б� (�����ж���������Ϻ��������)
typedef struct source_list
{
	uint32_t count;
	rc_userid list[10u];
} source_list;

// ������
struct rc_video_player
{
	// ɾ������
	virtual void release() = 0;

	// ����Դ (�ź���˭ ?)
	virtual rc_bool get_source(source_list* src) = 0;

	// �����ź�
	// fvideo:// ... 
	// rtmp:// ...
	virtual rc_bool load(const char* url) = 0;

	// ���� / ֹͣ
	// mask: �ź�����
	//		���ֻ�������� (layer_bit_audio)
	//		���ֻ������Ƶ(����) (layer_bit_video_low)
	//		�����������Ƶ(�е�) (layer_bit_audio | layer_bit_video_medium)
	// ���ֹͣ (0)
	// ����ʱ�����л�����������
	virtual void play(uint32_t mask) = 0;

	// ���Ե���������Ƶ�ź�
	virtual void set_video_signal(signal_handle) = 0;

	// ������Ƶ�ź�
	// index: 
	//	0 = ԭʼ��, load���ź�, ���δload�򷵻� null
	//	1 = ��ǰ��, set_video_signal���ź�
	virtual signal_handle get_video_signal(int index) = 0;

	// ������Ƶ��Ϣ
	// ���� FALSE ��ʾ��δ�յ���Ƶ
	virtual BOOL get_video_info(rc_video_info*) = 0;

	// ��������ָʾ
	virtual void get_sound_wave(rc_sound_wave*) = 0;

	// �ػ洰�� (������С���ָ�ʱ��Ҫ���øýӿ������ػ棬�������ݵĿհ�)
	virtual void flush_view(void) = 0;

	// ���ò������� (������) [0-100]
	virtual void set_volume(int vol) = 0;
};

// ������Ƶ������ (��Ҫ�ṩһ�����ھ��������Ⱦ��Ƶ)
SDKAPI rc_video_player* createPlayer(HWND hwnd);

// ���ÿհ���Ƶ����
SDKAPI rc_bool setBlankVideo(const uint8_t data[], const uint32_t size);


// ��ƵԴ
enum rc_video_source {
	video_src_null,		//��
	video_src_camera,	//����ͷ
	video_src_screen,	//��Ļ
};

// ý��ɼ�
struct rc_video_capturer
{
	// ����/�ر� ����
	// uri: fvideo://192.168.1.122:10002/13509391992
	// index: ���������� [0-1]������ͬʱ����2·��
	virtual rc_bool push(const uint32_t index, const char* uri) = 0;

	// ��������ͷ�ź� (����Ԥ��)
	virtual signal_handle get_camera_signal(void) = 0;

	// ����/�ر� ����ͷ/��Ļ�ɼ�
	virtual void set_video_source(rc_video_source) = 0;
	virtual rc_video_source get_video_source(void) const = 0;

	// ��������ͷ������
	virtual void set_camera_resolution(rc_layer) = 0;
	virtual rc_layer get_camera_resolution(void) = 0;

	// ����/�ر� ��˷�
	virtual void set_mic_state(rc_onoff) = 0;
	virtual rc_onoff get_mic_state(void) const = 0;
	virtual void get_mic_wave(rc_sound_wave*) = 0;
};
