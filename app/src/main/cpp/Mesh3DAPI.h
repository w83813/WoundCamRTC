// Hi Max Depth image with RGB and Thermal Process Function
// Data: 2020.10.21


#ifndef ITRI_TRIAPI
#define ITRI_TRIAPI

#include "ITRITriMesh.h"
#include "BaseStruct.h"

struct Temp_info
{
	int location_x;
	int location_y;
	float temp;
};
struct WoundAreaInfo
{
	float wound_max_depth;
	float wound_area;
	Temp_info wound_high_temp;
	Temp_info wound_low_temp;
};

struct DepthCameraParam
{
	float FOCAL_LENGTH_X;
	float FOCAL_LENGTH_Y;
	float PRINCIPAL_X;
	float PRINCIPAL_Y;
};

struct RGBAttMeshParam
{
	int img_width;
	int img_height;
	int cx;
	int cy;
	float sx;
	float sy;
	float tx;
	float ty;
	float tz;
};

struct ThermalAttMeshParam
{
	int img_width;
	int img_height;
	int cx;
	int cy;
	float sx;
	float sy;
	float tx;
	float ty;
	float tz;
};

struct LTHVIndex
{
	int h1;
	int h2;
	int v1;
	int v2;
};

struct Thermal3D
{
	float thermal;
	float x;
	float y;
	float z;
};

struct CPt2D
{
	int x;
	int y;
};

struct CPt_DZ
{
	float d;
	float z;
};

struct Func_XY
{
	float a;
	float b;
	float c;
};// ax+by=c

struct Func_DZ
{
	float m;
	float c;
};// z= md+ c

void GetRotMatrix(int axis, double angle, double *rot);

Point3Df LTRotPoint(Point3Df pt, double *rot);
int BoundClipDepth(unsigned short *pDepth, int depth_width, int depth_height, unsigned short min_depth, unsigned short max_depth);

// fill the Depth Raw Data according to the empty gap
int FillDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int w_max_gap, int h_max_gap);

// smooth the Depth Raw Data
int SmoothDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int range, int times);

// remove the edge peak data of the Depth Raw Data
int RemoveEdgePeakDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int times);

// contruct depth data to triangle mesh
int DepthToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh);

int DepthNIRToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, char *nirfilename, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh);

// attached rgb image by hologram matrxi (RGB to NIR) to Depth data to construct triangle mesh with rgb texture
int DepthNir2RGBHoloMtxToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, char *rgbfilename, float* pHoloMtx, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh);

int TriMeshAttRGBIMG(ITRITriMesh *pMesh, RGBAttMeshParam m_param, char *rgbfilename, int img_width, int img_height);

int TriMeshAttRGBIMGEx(ITRITriMesh *pMesh, RGBAttMeshParam m_param, char *rgbfilename, int img_width, int img_height);

int TriMeshAttThermalRGBIMGEx(ITRITriMesh *pMesh, char *rgbfilename, int img_width, int img_height);

int TriMeshAttThermalIMG(ITRITriMesh *pMesh, ThermalAttMeshParam m_param, float *pThermalData, int img_width, int img_height);

int TriMeshAttThermalIMGEx(ITRITriMesh *pMesh, float *pThermalData, int img_width, int img_height);

int CalWoundAreaInfo(unsigned char *pMask, int img_width, int img_height, ITRITriMesh *pMesh, WoundAreaInfo *pInfo);

int LTCheckPtsInTriAngle2D(int px, int py, int p1x, int p1y, int p2x, int p2y, int p3x, int p3y);

// two points on the texture to get the mapping distance in 3D data
float GetTwoPointsDistOnTexture(ITRITriMesh *pMesh, int x1, int y1, int x2, int y2, int img_width, int img_height);

// get thermal information on the texture point
float GetPointThermalOnTexture(ITRITriMesh *pMesh, int x, int y, int img_width, int img_height);

// get theraml and 3D info on the texture point
Thermal3D GetPointThermal3DOnTexture(ITRITriMesh *pMesh, int x, int y, int img_width, int img_height);

// smooth triangle mesh
int SmoothTriMesh(ITRITriMesh *pMesh, int smooth_times);

// get 3D (x, y, z) points set data on the texture image of the triangle mesh
int Get3DPointSetOnTexture(ITRITriMesh *pMesh, int pt_num, int* px, int* py, float *p3Dx, float *p3Dy, float *p3Dz, int img_width, int img_height);

// get four 3D (x, y, z) points on the depth map with cross point regression morph
int GetFour3DPointsOnDepthWithCPTRegression(unsigned short *pDepth, DepthCameraParam m_param, int depth_width, int depth_height, int pt_num, int* px, int* py, float *p3Dx, float *p3Dy, float *p3Dz);

Func_XY TwoPointFunc(CPt2D p1, CPt2D p2);

CPt2D FindCrossXYPt(CPt2D p1, CPt2D p2, CPt2D p3, CPt2D p4);

Func_DZ RegressionDZ(CPt_DZ *ptBuff, int pt_num);

int GetSectPt(CPt2D start_pt, CPt2D end_pt, int sect_pt_num, CPt2D *ptSectBuff);

#endif