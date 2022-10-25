#if !defined(MESH3D_POINT_3F)
#define  MESH3D_POINT_3F

#define ZeroMemory(Destination,Length) memset((Destination),0,(Length))
#define CopyMemory(Destination, Source, Length) memcpy((Destination),(Source),(Length))
typedef unsigned short WORD;

#include <jni.h>
#include <cmath>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <android/log.h>
#include "utilbase.h"

struct Point3Df
{
	float x;
	float y;
	float z;
};

struct Point2Df
{
	float x;
	float y;
};

struct Point2i
{
	int x;
	int y;
};

struct TriTexCoord2f
{
	float c1;
	float r1;
	float c2;
	float r2;
	float c3;
	float r3;
};

struct Index3i
{
	int v1;
	int v2;
	int v3;
};

struct Color3i
{
	unsigned char r;
	unsigned char g;
	unsigned char b;
};

struct IndexW3i
{
	WORD v1;
	WORD v2;
	WORD v3;
};

struct BoundingBox
{
	float min_x;
	float min_y;
	float min_z;
	float max_x;
	float max_y;
	float max_z;
};

struct Plane
{
	float a;
	float b;
	float c;
	float d;
};

#endif

