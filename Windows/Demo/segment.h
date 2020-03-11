#pragma once

#include <inttypes.h>
#include <string.h>

struct segment_t {
	uint16_t what;
	uint16_t length;
	uint8_t* body;
};

static inline uint8_t *write_byte(uint8_t *p, uint8_t c)
{
	*(uint8_t*)p++ = c;
	return p;
}
static inline const uint8_t *read_byte(const uint8_t *p, uint8_t *c)
{
	*c = *p++;
	return p;
}
static inline uint8_t *write_u16(uint8_t *p, uint16_t w)
{
	p[0] = (uint8_t)(w >> 8);
	p[1] = (uint8_t)(w);
	return p + 2u;
}
static inline const uint8_t *read_u16(const uint8_t *p, uint16_t *w)
{
	*w = (uint16_t)(p[0] << 8) | (p[1]);
	return p + 2u;
}

static inline uint8_t* write_u24(uint8_t *p, uint32_t n)
{
	p[0] = (uint8_t)(n >> 16u);
	p[1] = (uint8_t)(n >> 8u);
	p[2] = (uint8_t)(n);
	return (p + 3u);
}
static inline const uint8_t *read_u24(const uint8_t *p, uint32_t *n)
{
	*n = (uint32_t)(p[0] << 16u) | (p[1] << 8u) | (p[2]);
	return (p + 3u);
}

static inline uint8_t *write_u32(uint8_t *p, uint32_t l)
{
	p[0] = (uint8_t)(l >> 24);
	p[1] = (uint8_t)(l >> 16);
	p[2] = (uint8_t)(l >> 8);
	p[3] = (uint8_t)(l);
	return p + 4u;
}
static inline const uint8_t *read_u32(const uint8_t *p, uint32_t *l)
{
	*l = (uint32_t)(p[0] << 24) | (p[1] << 16) | (p[2] << 8) | (p[3]);
	return p + 4u;
}
static inline uint8_t *write_u64(uint8_t *p, uint64_t n)
{
	uint32_t h = (uint32_t)(n >> 32);
	return write_u32(write_u32(p, h), (uint32_t)(n));
}
static inline const uint8_t* read_u64(const uint8_t *p, uint64_t *n)
{
	uint32_t hi, lo;
	p = read_u32(read_u32(p, &hi), &lo);
	*n = ((uint64_t)hi << 32u) | lo;
	return p;
}
static inline uint8_t *write_block(uint8_t *p, const void* src, const uint32_t len)
{
	memcpy(p, src, len);
	return (p + len);
}
static inline const uint8_t *read_block(const uint8_t* src, void *p, const uint32_t len)
{
	memcpy(p, src, len);
	return (src + len);
}

static inline const uint8_t* next(const uint8_t *cur, const uint8_t *end, segment_t* seg)
{
	if (cur+4u <= end)
	{
		uint16_t f_len;
		const uint8_t* ptr = read_u16(read_u16(cur, &seg->what), &f_len);
		cur = ptr + f_len;
		if (cur <= end) {
			seg->body = (uint8_t*)ptr;
			seg->length = f_len;
			return cur;
		}
	}
	return NULL;
}

static inline const uint8_t* seek(const uint8_t* cur, const uint8_t* end, segment_t* b)
{
	const uint16_t name = b->what;
	while (cur + 4u <= end)
	{
		uint16_t f_name, f_len;
		const uint8_t* ptr = read_u16(read_u16(cur, &f_name), &f_len);
		cur = ptr + f_len;
		if (f_name == name) {
			if (cur <= end) {
				b->length = f_len;
				b->body = (uint8_t*)ptr;
				return cur;
			}
			break;
		}
	}
	return NULL;
}
static inline uint8_t* write_tag(uint8_t buf[], uint16_t name, uint16_t payload)
{
	return write_u16(write_u16(buf, name), payload);	
}
static inline const uint8_t* read_tag(const uint8_t buf[], segment_t& block)
{
	return read_u16(read_u16(buf, &block.what), &block.length);
}
static inline uint8_t* write_segment(uint8_t buf[], segment_t& seg)
{
	uint8_t* ptr = write_tag(buf, seg.what, seg.length);
	return write_block(ptr, seg.body, seg.length);
}

static inline uint16_t parse_u16(const uint8_t ptr[])
{
	return (ptr[0] << 8) | (ptr[1]);
}
static inline uint32_t parse_u32(const uint8_t ptr[]) 
{
	return
		(ptr[0] << 24) |
		(ptr[1] << 16) |
		(ptr[2] << 8) |
		(ptr[3]);
}
static inline uint64_t parse_u64(const uint8_t ptr[]) 
{
	uint64_t h = parse_u32(ptr);
	return (h << 32) | parse_u32(ptr + 4u);
}
