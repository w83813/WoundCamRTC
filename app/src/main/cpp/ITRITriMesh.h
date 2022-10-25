#ifndef ITRI_TRIMESH
#define ITRI_TRIMESH
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
using namespace cv;
#include "BaseStruct.h"

struct VertexGroup
{
	int *IdxPnt1;
	int CntPnt1;
	int *IdxPnt2;
	int CntPnt2;
	int *IdxLne;
	int CntLne;
};

class ITRITriMesh
{
	unsigned m_ErrCode;
	int m_PntCount;
	Point3Df* m_pPnt;
	int m_PntSize;
	Point3Df* m_pNrm;
	Point2Df* m_pTexco;
	Point2Df* m_pTexco_ex;
	int *pImgPosIdx;
	TriTexCoord2f* m_pTriTexco;
	float *m_pTemp;
	Color3i* m_pTexBuff;
	int img_width, img_height;
	char* m_pTexFilename;
	char* m_pTexFilename_ex;
	int m_TngCount;
	Index3i* m_pTng;
	int m_TngSize;
	int *m_pV2tIdx;
	int *m_pV2tBuf;
	int m_SymmStt, m_SymmEnd;
	VertexGroup m_Group[5];
	int m_GroupCnt;
	void ConstructV2tBuf();
	void ConstructNrmBuf();
public:
	ITRITriMesh();
	~ITRITriMesh();
	unsigned Error() { return m_ErrCode; };

	Point3Df* NewPointBuffer(int Size);
	void SetPointCount(int Count) { m_PntCount = Count; };
	Index3i* NewTriangleBuffer(int Size);
	void SetTriangleCount(int Count) { m_TngCount = Count; };
	Point2Df* NewTexcoBuffer();
	Point2Df* NewTexcoBufferEx();
	TriTexCoord2f* NewTriTexcoBuffer();
	int* NewTextPosIdxBuffer();
	Color3i* NewImageBuff(Color3i *clrBuff, int row, int col);// new the image buffer
	float* NewPointTempture(int Size);// new point temperature

	int MinSizePointBuffer(int Size);
	int MinSizeTriangleBuffer(int Size);
	int DeleteTriangle(int *Index, int Num);

	Point3Df* GetPointBuffer(int *pNum) { if (pNum) *pNum = m_PntCount; return m_pPnt; };
	Point3Df* GetNormalBuffer();
	float* GetTemperatureBuffer() { return m_pTemp; };
	Point2Df* GetTexcoBuffer() { return m_pTexco; };
	Point2Df* GetTexcoBufferEx() { return m_pTexco_ex; };
	int* GetImgPosIdxBuffer() { return pImgPosIdx; }
	TriTexCoord2f* GetTriTexcoBuffer() { return m_pTriTexco; };
	Index3i* GetTriangleBuffer(int *pNum) { if (pNum) *pNum = m_TngCount; return m_pTng; };
	Color3i* GetImageBuffer(int *Row, int *Col);

	int* GetTriangle(int PntIdx, int *pNum);
	void UpdateNormal();
	void SetTextFileName(char *filename);
	void SetTextFileNameEx(char *filename);
	Color3i* NewTextureImage(char* Filename);
	Color3i* GetTextureImage() { return m_pTexBuff; };

	void GetBoundingBox(float *min_x, float *max_x, float *min_y, float *max_y, float *min_z, float *max_z);

	void SetSymmData(int Stt, int End) { m_SymmStt = Stt; m_SymmEnd = End; };
	void GetSymmData(int *Stt, int *End) { *Stt = m_SymmStt; *End = m_SymmEnd; };

	int GetGroupCount() { return m_GroupCnt; };
	VertexGroup* GetGroup(int Index) { return m_Group + Index; };
	VertexGroup* AddGroup();
	void DeleteGroup(int Index);
	void TmpSetGroupCount(int Count) { m_GroupCnt = Count; };

	unsigned SaveToObjFile(char* FileTtitle, char *file_dir);
	unsigned SaveToObjFileEx(char* FileTtitle, char *file_dir);

	//unsigned SaveToFile(char* Filename);
	// rot[9]: rotation matrix
	//unsigned SaveToFile(char* Filename, float *rot);// save the data in the file according to the rotation matrix

	bool GetTextFileName(char *Filename);// get the texture filename in the mesh3d file

};

#endif
