//#include "stdafx.h"
//#include <stdio.h>
//#include <math.h>
#include "ITRITriMesh.h"
#define FILE_ID1 0xAC38FB72
#define FILE_ID2 0xE451D609

#define DID_VERTEX 1//Data ID for vertex point
#define DID_VERTEX_ENC 11//Data ID for vertex point(encrypted)
#define DID_TRIANGLE 2//Data ID for triangle
#define DID_TRIANGLE_ENC 22//Data ID for triangle(encrypted)
#define DID_TEXTURE_IMAGE 3//Data ID for Texture image
#define DID_TEXTURE_COORD 4//Data ID for Texture coordinate
#define DID_TRITEXTURE_COORD 5//Data ID for Texture coordinate
#define DID_NEW_TRIANGLE 6//Data ID for new triangle compress
#define DID_TEXTURE_IMAGE_BUFFER 20// Data ID for Texture Image Buffer
#define DID_ATTRIB 101//Data ID for mesh attributes
#define DID_POINT_GROUP 102////Data ID for point group
#define DID_TEXTURE_IMAGE_BUFFER1 103// Data ID for Texture image buffer1

#define ITRI_MAX_FLOAT 3.402823466e+38F
#define ITRI_MIN_FLOAT -3.402823466e+38F

ITRITriMesh::ITRITriMesh()
{
	m_ErrCode = 0;
	m_PntCount = m_PntSize = 0;
	m_pPnt = NULL;
	m_pNrm = NULL;
	m_pTemp = NULL;
	m_TngCount = m_TngSize = 0;
	m_pTng = NULL;
	m_pTexBuff = NULL;
	m_pTexFilename = NULL;
	m_pTexFilename_ex = NULL;
	pImgPosIdx = NULL;
	m_pTexco = NULL;
	m_pTexco_ex = NULL;
	m_pTriTexco = NULL;
	m_pV2tIdx = NULL;
	m_pV2tBuf = NULL;
	m_SymmStt = m_SymmEnd = 0;
	m_GroupCnt = 0;
	ZeroMemory(m_Group, sizeof(VertexGroup) * 5);
}


ITRITriMesh::~ITRITriMesh()
{
	if (m_pTexBuff != NULL) delete m_pTexBuff;
	for (int i = 0; i<m_GroupCnt; i++)
	{
		delete[] m_Group[i].IdxPnt1;
		delete[] m_Group[i].IdxPnt2;
		delete[] m_Group[i].IdxLne;
	}
	if (m_pPnt != NULL) delete[] m_pPnt;
	if (m_pNrm != NULL) delete[] m_pNrm;
	if (m_pTemp!= NULL) delete[] m_pTemp;
	if (m_pTng != NULL) delete[] m_pTng;
	if (m_pV2tIdx != NULL) delete[] m_pV2tIdx;
	if (m_pV2tBuf != NULL) delete[] m_pV2tBuf;
	if (m_pTexco != NULL) delete[] m_pTexco;
	if (m_pTexco_ex != NULL) delete[] m_pTexco_ex;
	if (pImgPosIdx != NULL) delete[] pImgPosIdx;
	if (m_pTriTexco != NULL) delete[] m_pTriTexco;
	if (m_pTexFilename != NULL) delete[] m_pTexFilename;
	if (m_pTexFilename_ex != NULL) delete[] m_pTexFilename_ex;
}


Point3Df* ITRITriMesh::NewPointBuffer(int Size)
{
	if (Size>m_PntSize)
	{
		if (m_pPnt) delete[] m_pPnt;
		if (m_pNrm) { delete[] m_pNrm; m_pNrm = NULL; }
		if (m_pTemp) { delete[] m_pTemp; m_pTemp = NULL; }
		m_pPnt = new Point3Df[Size];
		if (m_pPnt) m_PntSize = Size;
		else m_PntSize = 0;
	}
	m_PntCount = m_PntSize;
	return m_pPnt;
}

Index3i* ITRITriMesh::NewTriangleBuffer(int Size)
{
	if (Size>m_TngSize)
	{
		if (m_pTng!=NULL) delete[] m_pTng;
		m_pTng = new Index3i[Size];
		if (m_pTng) m_TngSize = Size;
		else m_TngSize = 0;
	}
	m_TngCount = m_TngSize;
	return m_pTng;
}

Point2Df* ITRITriMesh::NewTexcoBuffer()
{
	if (m_pTexco!=NULL) delete[] m_pTexco;
	m_pTexco = new Point2Df[m_PntCount];
	return m_pTexco;
}

Point2Df* ITRITriMesh::NewTexcoBufferEx()
{
	if (m_pTexco_ex!=NULL) delete[] m_pTexco_ex;
	m_pTexco_ex = new Point2Df[m_PntCount];
	return m_pTexco_ex;
}

int* ITRITriMesh::NewTextPosIdxBuffer()
{
	if (pImgPosIdx) delete[] pImgPosIdx;
	pImgPosIdx = new int[m_PntCount];
	return pImgPosIdx;
}
TriTexCoord2f* ITRITriMesh::NewTriTexcoBuffer()
{
	if (m_pTriTexco) delete[] m_pTriTexco;
	m_pTriTexco = new TriTexCoord2f[m_TngCount];
	return m_pTriTexco;
}

float* ITRITriMesh::NewPointTempture(int Size)
{
	if (m_pTemp) delete[] m_pTemp;
	m_pTemp = new float[Size];
	return m_pTemp;
}
void ITRITriMesh::ConstructV2tBuf()
{
	int i, k, Num;
	if(m_pV2tIdx!=NULL) delete[] m_pV2tIdx;
	m_pV2tIdx = new int[m_PntCount + 1];
	if (m_pV2tIdx == NULL) { m_ErrCode = 5; return; }
	for (i = 0; i<m_PntCount; i++) m_pV2tIdx[i] = 0;
	for (i = 0; i<m_TngCount; i++)
	{
		m_pV2tIdx[m_pTng[i].v1]++;
		m_pV2tIdx[m_pTng[i].v2]++;
		m_pV2tIdx[m_pTng[i].v3]++;
	}
	for (i = 1; i<m_PntCount; i++) m_pV2tIdx[i] += m_pV2tIdx[i - 1];
	for (i = m_PntCount; i>0; i--) m_pV2tIdx[i] = m_pV2tIdx[i - 1];
	m_pV2tIdx[0] = 0;

	Num = m_pV2tIdx[m_PntCount];
	if(m_pV2tBuf!=NULL) delete[] m_pV2tBuf;
	m_pV2tBuf = new int[Num];
	if (m_pV2tBuf == NULL) { m_ErrCode = 5; return; }
	for (i = 0; i<Num; i++) m_pV2tBuf[i] = -1;
	for (i = 0; i<m_TngCount; i++)
	{
		k = m_pV2tIdx[m_pTng[i].v1];
		while (m_pV2tBuf[k] >= 0) k++;
		m_pV2tBuf[k] = i;
		k = m_pV2tIdx[m_pTng[i].v2];
		while (m_pV2tBuf[k] >= 0) k++;
		m_pV2tBuf[k] = i;
		k = m_pV2tIdx[m_pTng[i].v3];
		while (m_pV2tBuf[k] >= 0) k++;
		m_pV2tBuf[k] = i;
	}
}

void ITRITriMesh::ConstructNrmBuf()
{
	if (m_pNrm == NULL)
	{
		m_pNrm = new Point3Df[m_PntSize];
		if (m_pNrm == NULL) { m_ErrCode = 5; return; }
	}
}

void ITRITriMesh::UpdateNormal()
{
	int i;
	float dx1, dy1, dz1, dx2, dy2, dz2, Ax, Ay, Az;
	ZeroMemory(m_pNrm, sizeof(Point3Df)*m_PntCount);
	for (i = 0; i<m_TngCount; i++)
	{
		dx1 = m_pPnt[m_pTng[i].v2].x - m_pPnt[m_pTng[i].v1].x;
		dy1 = m_pPnt[m_pTng[i].v2].y - m_pPnt[m_pTng[i].v1].y;
		dz1 = m_pPnt[m_pTng[i].v2].z - m_pPnt[m_pTng[i].v1].z;
		dx2 = m_pPnt[m_pTng[i].v3].x - m_pPnt[m_pTng[i].v1].x;
		dy2 = m_pPnt[m_pTng[i].v3].y - m_pPnt[m_pTng[i].v1].y;
		dz2 = m_pPnt[m_pTng[i].v3].z - m_pPnt[m_pTng[i].v1].z;
		Ax = dy1*dz2 - dz1*dy2;
		Ay = dz1*dx2 - dx1*dz2;
		Az = dx1*dy2 - dy1*dx2;
		m_pNrm[m_pTng[i].v1].x += Ax;
		m_pNrm[m_pTng[i].v1].y += Ay;
		m_pNrm[m_pTng[i].v1].z += Az;
		m_pNrm[m_pTng[i].v2].x += Ax;
		m_pNrm[m_pTng[i].v2].y += Ay;
		m_pNrm[m_pTng[i].v2].z += Az;
		m_pNrm[m_pTng[i].v3].x += Ax;
		m_pNrm[m_pTng[i].v3].y += Ay;
		m_pNrm[m_pTng[i].v3].z += Az;
	}
	for (i = 0; i<m_PntCount; i++)
	{
		dx1 = (float)sqrt(m_pNrm[i].x*m_pNrm[i].x + m_pNrm[i].y*m_pNrm[i].y + m_pNrm[i].z*m_pNrm[i].z);
		if (dx1 != 0.f)
		{
			m_pNrm[i].x /= dx1;
			m_pNrm[i].y /= dx1;
			m_pNrm[i].z /= dx1;
		}
		else
		{
			m_pNrm[i].x = 1.f;
			m_pNrm[i].y = 0.f;
			m_pNrm[i].z = 0.f;
		}
	}
}

Point3Df* ITRITriMesh::GetNormalBuffer()
{
	if (m_pNrm == NULL)
	{
		ConstructNrmBuf();
		UpdateNormal();
	}
	return m_pNrm;
}

int* ITRITriMesh::GetTriangle(int PntIdx, int *pNum)
{
	if (m_pV2tBuf == NULL)
	{
		ConstructV2tBuf();
		if (m_pV2tBuf == NULL) return NULL;
	}

	if (pNum) *pNum = m_pV2tIdx[PntIdx + 1] - m_pV2tIdx[PntIdx];
	return m_pV2tBuf + m_pV2tIdx[PntIdx];
}

bool ITRITriMesh::GetTextFileName(char *Filename)
{
	if (m_pTexFilename != NULL)
	{
		sprintf(Filename, "%s", m_pTexFilename);
		return true;
	}
	return false;
}

VertexGroup* ITRITriMesh::AddGroup()
{
	if (m_GroupCnt >= 5) return NULL;
	else
	{
		ZeroMemory(m_Group + m_GroupCnt, sizeof(VertexGroup));
		m_GroupCnt++;
		return m_Group + (m_GroupCnt - 1);
	}
}

void ITRITriMesh::DeleteGroup(int Index)
{
	if(m_Group[Index].IdxPnt1!=NULL) delete[] m_Group[Index].IdxPnt1;
	if(m_Group[Index].IdxPnt2!=NULL) delete[] m_Group[Index].IdxPnt2;
	if(m_Group[Index].IdxLne!=NULL) delete[] m_Group[Index].IdxLne;
	for (int i = Index + 1; i<m_GroupCnt; i++)
	{
		m_Group[i - 1] = m_Group[i];
	}
	m_GroupCnt--;
}

void ITRITriMesh::SetTextFileName(char *Filename)
{
	if (Filename) if (Filename[0] != 0)
		{
			if (m_pTexFilename) delete m_pTexFilename;
			m_pTexFilename = new char[strlen(Filename) + 1];
			strcpy(m_pTexFilename, Filename);
		}
}

void ITRITriMesh::SetTextFileNameEx(char *Filename)
{
	if (Filename) if (Filename[0] != 0)
		{
			if (m_pTexFilename_ex) delete m_pTexFilename_ex;
			m_pTexFilename_ex = new char[strlen(Filename) + 1];
			strcpy(m_pTexFilename_ex, Filename);
		}
}

Color3i* ITRITriMesh::NewTextureImage(char* Filename)
{
	if (m_pTexBuff) { delete m_pTexBuff; m_pTexBuff = NULL; }
	if (Filename) if (Filename[0] != 0)
		{
			if (m_pTexFilename) delete m_pTexFilename;
			m_pTexFilename = new char[strlen(Filename) + 1];
			strcpy(m_pTexFilename, Filename);
			Mat image;
			int height, width, size;
			int i, j, pos_index;
			image = imread(m_pTexFilename);   // Read the file
			if (image.data)
			{
				// read image file
				width = image.cols;
				height = image.rows;
				size = width*height;
				if (size > 0)
				{
					m_pTexBuff = new Color3i[size];
					for (i = 0; i < height; i++)
					{
						for (j = 0; j < width; j++)
						{
							pos_index = i* width + j;
							m_pTexBuff[pos_index].r = image.data[pos_index* 3];
							m_pTexBuff[pos_index].g = image.data[pos_index * 3 + 1];
							m_pTexBuff[pos_index].b = image.data[pos_index * 3 + 2];
						}
					}
				}
			}
		}
	return m_pTexBuff;
}

int ITRITriMesh::MinSizePointBuffer(int Size)
{
	if (m_PntSize >= Size) return m_PntSize;
	Size = m_PntSize + 2 * (Size - m_PntSize);
	Point3Df* ptr;
	ptr = new Point3Df[Size];
	CopyMemory(ptr, m_pPnt, sizeof(Point3Df)*m_PntCount);
	if(m_pPnt!=NULL) delete[] m_pPnt;
	m_pPnt = ptr; m_PntSize = Size;
	if (m_pNrm)
	{
		ptr = new Point3Df[Size];
		CopyMemory(ptr, m_pNrm, sizeof(Point3Df)*m_PntCount);
		if(m_pNrm!=NULL) delete[] m_pNrm;
		m_pNrm = ptr;
	}
	if (m_pTexco)
	{
		Point2Df* ptr = new Point2Df[Size];
		CopyMemory(ptr, m_pTexco, sizeof(Point2Df)*m_PntCount);
		if(m_pTexco!=NULL) delete[] m_pTexco;
		m_pTexco = ptr;
	}
	return m_PntSize;
}

int ITRITriMesh::MinSizeTriangleBuffer(int Size)
{
	if (m_TngSize >= Size) return m_TngSize;
	Size = m_TngSize + 2 * (Size - m_TngSize);
	Index3i* ptr;
	ptr = new Index3i[Size];
	CopyMemory(ptr, m_pTng, sizeof(Index3i)*m_TngCount);
	if(m_pTng!=NULL) delete[] m_pTng;
	m_pTng = ptr; m_TngSize = Size;
	return m_TngSize;
}

int ITRITriMesh::DeleteTriangle(int *Index, int Num)
{
	int i, j, nd, Tnum;
	Tnum = Index[0];
	for (i = 0; i<Num; i++)
	{
		if (i<Num - 1) nd = Index[i + 1];
		else nd = m_TngCount;
		for (j = Index[i] + 1; j<nd; j++) { m_pTng[Tnum] = m_pTng[j]; Tnum++; }
	}
	m_TngCount = Tnum;
	return m_TngCount;
}

Color3i* ITRITriMesh::NewImageBuff(Color3i *clrBuff, int row, int col)
{
	int i, j, pos_idx;
	if (m_pTexBuff != NULL) delete[] m_pTexBuff;
	if ((row>0) && (col>0))
	{
		m_pTexBuff = new Color3i[row*col];
		if (clrBuff)
		{
			for (i = 0; i<row; i++)
			{
				for (j = 0; j<col; j++)
				{
					pos_idx = i*col + j;
					m_pTexBuff[pos_idx] = clrBuff[pos_idx];
				}
			}
		}
		img_width = col;
		img_height = row;
	}
	return m_pTexBuff;
}

Color3i* ITRITriMesh::GetImageBuffer(int *Row, int *Col)
{
	*Row = img_height;
	*Col = img_width;
	return m_pTexBuff;
}

void ITRITriMesh::GetBoundingBox(float *min_x, float *max_x, float *min_y, float *max_y, float *min_z, float *max_z)
{
	int i;
	float min[3], max[3];
	for (i = 0; i<3; i++)
	{
		min[i] = ITRI_MAX_FLOAT;
		max[i] = ITRI_MIN_FLOAT;
	}
	for (i = 0; i<m_PntCount; i++)
	{
		if (m_pPnt[i].x<min[0]) min[0] = m_pPnt[i].x;
		if (m_pPnt[i].y<min[1]) min[1] = m_pPnt[i].y;
		if (m_pPnt[i].z<min[2]) min[2] = m_pPnt[i].z;
		if (m_pPnt[i].x>max[0]) max[0] = m_pPnt[i].x;
		if (m_pPnt[i].y>max[1]) max[1] = m_pPnt[i].y;
		if (m_pPnt[i].z>max[2]) max[2] = m_pPnt[i].z;
	}
	*min_x = min[0];
	*min_y = min[1];
	*min_z = min[2];
	*max_x = max[0];
	*max_y = max[1];
	*max_z = max[2];
}

unsigned ITRITriMesh::SaveToObjFile(char* filetitle, char *file_dir)
{
	FILE *ofp = NULL;
	Point3Df *p3D = NULL;
	Point3Df *ptNormal = NULL;
	Index3i *pTri = NULL;
	int i, pt_num;
	int tri_num;
	Color3i *imgBuff = NULL;
	Point2Df* pTexCoord = NULL;
	char objfname[200];
	char mtlfname[200];
	pTri = GetTriangleBuffer(&tri_num);
	p3D = GetPointBuffer(&pt_num);
	if (pt_num <= 0) return -2;
	if (tri_num <= 0) return -3;
	sprintf(objfname, "%s/%s.obj", file_dir, filetitle);
	ofp = fopen(objfname, "wt");
	if (ofp == NULL) return -4;
	fprintf(ofp, "# File generated by Logistic Technology Corp.: www.ltech.com.tw\n\n");
	fprintf(ofp, "mtllib %s.mtl\n\n", filetitle);
	fprintf(ofp, "g %s\n", filetitle);
	fprintf(ofp, "usemtl %s\n\n", filetitle);

	fprintf(ofp, "# %d vertices\n", pt_num);
	pTexCoord = GetTexcoBuffer();
	if (pTexCoord != NULL)
	{
		fprintf(ofp, "# %d texture vertices\n", pt_num);
	}
	fprintf(ofp, "# %d vertex normals\n", pt_num);
	fprintf(ofp, "# %d facets\n\n", tri_num);

	// vertex point
	for (i = 0; i<pt_num; i++)
	{
		fprintf(ofp, "v %.5f %.5f %.5f\n", p3D[i].x, p3D[i].y, p3D[i].z);
	}
	// texture
	if (pTexCoord != NULL)
	{
		for (i = 0; i<pt_num; i++)
		{
			fprintf(ofp, "vt %.5f %.5f\n", pTexCoord[i].x, pTexCoord[i].y);
		}
	}
	// vertex normal
	ptNormal = GetNormalBuffer();
	for (i = 0; i<pt_num; i++)
	{
		fprintf(ofp, "vn %.5f %.5f %.5f\n", ptNormal[i].x, ptNormal[i].y, ptNormal[i].z);
	}

	// triangle data
	for (i = 0; i<tri_num; i++)
	{
		if (pTexCoord != NULL)
		{
			fprintf(ofp, "f %d/%d/%d %d/%d/%d %d/%d/%d\n", pTri[i].v1 + 1, pTri[i].v1 + 1, pTri[i].v1 + 1, pTri[i].v2 + 1, pTri[i].v2 + 1, pTri[i].v2 + 1, pTri[i].v3 + 1, pTri[i].v3 + 1, pTri[i].v3 + 1);
		}
	}
	if (ofp != NULL) fclose(ofp);
	// save the mtl file
	sprintf(mtlfname, "%s/%s.mtl", file_dir, filetitle);
	ofp = fopen(mtlfname, "wt");
	if (ofp == NULL) return -4;
	fprintf(ofp, "newmtl %s\n\n", filetitle);
	fprintf(ofp, "Ka 0.3 0.3 0.3\n");
	fprintf(ofp, "Kd 1.0 1.0 1.0\n");
	fprintf(ofp, "Ks 0 0 0\n");
	if (m_pTexFilename != NULL)
	{
		fprintf(ofp, "map_Kd %s\n", m_pTexFilename);
	}
	if (ofp != NULL) fclose(ofp);

	return 1;
}

unsigned ITRITriMesh::SaveToObjFileEx(char* filetitle, char *file_dir)
{
	FILE *ofp = NULL;
	Point3Df *p3D = NULL;
	Point3Df *ptNormal = NULL;
	Index3i *pTri = NULL;
	int i, pt_num;
	int tri_num;
	Color3i *imgBuff = NULL;
	Point2Df* pTexCoord = NULL;
	char objfname[200];
	char mtlfname[200];
	pTri = GetTriangleBuffer(&tri_num);
	p3D = GetPointBuffer(&pt_num);
	if (pt_num <= 0) return -2;
	if (tri_num <= 0) return -3;
	sprintf(objfname, "%s/%s.obj", file_dir, filetitle);
	ofp = fopen(objfname, "wt");
	if (ofp == NULL) return -4;
	fprintf(ofp, "# File generated by Logistic Technology Corp.: www.ltech.com.tw\n\n");
	fprintf(ofp, "mtllib %s.mtl\n\n", filetitle);
	fprintf(ofp, "g %s\n", filetitle);
	fprintf(ofp, "usemtl %s\n\n", filetitle);

	fprintf(ofp, "# %d vertices\n", pt_num);
	pTexCoord = GetTexcoBufferEx();
	if (pTexCoord != NULL)
	{
		fprintf(ofp, "# %d texture vertices\n", pt_num);
	}
	fprintf(ofp, "# %d vertex normals\n", pt_num);
	fprintf(ofp, "# %d facets\n\n", tri_num);

	// vertex point
	for (i = 0; i<pt_num; i++)
	{
		fprintf(ofp, "v %.5f %.5f %.5f\n", p3D[i].x, p3D[i].y, p3D[i].z);
	}
	// texture
	if (pTexCoord != NULL)
	{
		for (i = 0; i<pt_num; i++)
		{
			fprintf(ofp, "vt %.5f %.5f\n", pTexCoord[i].x, pTexCoord[i].y);
		}
	}
	// vertex normal
	ptNormal = GetNormalBuffer();
	for (i = 0; i<pt_num; i++)
	{
		fprintf(ofp, "vn %.5f %.5f %.5f\n", ptNormal[i].x, ptNormal[i].y, ptNormal[i].z);
	}

	// triangle data
	for (i = 0; i<tri_num; i++)
	{
		if (pTexCoord != NULL)
		{
			fprintf(ofp, "f %d/%d/%d %d/%d/%d %d/%d/%d\n", pTri[i].v1 + 1, pTri[i].v1 + 1, pTri[i].v1 + 1, pTri[i].v2 + 1, pTri[i].v2 + 1, pTri[i].v2 + 1, pTri[i].v3 + 1, pTri[i].v3 + 1, pTri[i].v3 + 1);
		}
	}
	if (ofp != NULL) fclose(ofp);
	// save the mtl file
	sprintf(mtlfname, "%s/%s.mtl", file_dir, filetitle);
	ofp = fopen(mtlfname, "wt");
	if (ofp == NULL) return -4;
	fprintf(ofp, "newmtl %s\n\n", filetitle);
	fprintf(ofp, "Ka 0.3 0.3 0.3\n");
	fprintf(ofp, "Kd 1.0 1.0 1.0\n");
	fprintf(ofp, "Ks 0 0 0\n");
	if (m_pTexFilename_ex != NULL)
	{
		fprintf(ofp, "map_Kd %s\n", m_pTexFilename_ex);
	}
	if (ofp != NULL) fclose(ofp);

	return 1;
}
