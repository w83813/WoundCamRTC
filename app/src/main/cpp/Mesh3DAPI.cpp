//#include "stdafx.h"
#include "Mesh3DAPI.h"
#include "ITRITriMesh.h"

Point3Df UniformVet(Point3Df pt)
{
    Point3Df rsl;
    double deta;
    deta = sqrt(pt.x*pt.x + pt.y*pt.y + pt.z*pt.z);
    rsl.x = rsl.y = rsl.z = 0;
    if (deta != 0)
    {
        rsl.x = float(pt.x / deta);
        rsl.y = float(pt.y / deta);
        rsl.z = float(pt.z / deta);
    }
    return rsl;
}

Point3Df TriNormal(Point3Df p1, Point3Df p2, Point3Df p3)
{
    Point3Df v1_a, v1_b, v1, Rsl;
    v1_a.x = p2.x - p1.x;
    v1_a.y = p2.y - p1.y;
    v1_a.z = p2.z - p1.z;
    v1_b.x = p3.x - p2.x;
    v1_b.y = p3.y - p2.y;
    v1_b.z = p3.z - p2.z;
    v1.x = v1_a.y*v1_b.z - v1_a.z*v1_b.y;
    v1.y = v1_a.z*v1_b.x - v1_a.x*v1_b.z;
    v1.z = v1_a.x*v1_b.y - v1_a.y*v1_b.x;
    Rsl = UniformVet(v1);
    return Rsl;
}

double TriArea(Point3Df p1, Point3Df p2, Point3Df p3)
{
    Point3Df v1, v2, v_area;
    double result;

    v1.x = p2.x - p1.x;
    v1.y = p2.y - p1.y;
    v1.z = p2.z - p1.z;
    v2.x = p3.x - p2.x;
    v2.y = p3.y - p2.y;
    v2.z = p3.z - p2.z;


    v_area.x = v1.y*v2.z - v1.z*v2.y;
    v_area.y = v1.z*v2.x - v1.x*v2.z;
    v_area.z = v1.x*v2.y - v1.y*v2.x;
    result = 0.5* sqrt((double)(v_area.x*v_area.x + v_area.y*v_area.y + v_area.z*v_area.z));
    return result;
}

Point3Df TriCenter(Point3Df p1, Point3Df p2, Point3Df p3)
{
    Point3Df rslpt;
    rslpt.x = (p1.x + p2.x + p3.x) / 3.0;
    rslpt.y = (p1.y + p2.y + p3.y) / 3.0;
    rslpt.z = (p1.z + p2.z + p3.z) / 3.0;
    return rslpt;
}

float Height2Plane(Point3Df pt, Plane pln)
{
    return pln.a* pt.x + pln.b* pt.y + pln.c*pt.z + pln.d;
}

Plane ConstrutPlane(Point3Df p1, Point3Df p2, Point3Df p3, Point3Df p4)
{
    Plane ref_pln;
    Point3Df center;
    Point3Df normal_1, normal_2, normal_3, normal_4, avg_normal;
    center.x = (p1.x + p2.x + p3.x + p4.x) / 4.0;
    center.y = (p1.y + p2.y + p3.y + p4.y) / 4.0;
    center.z = (p1.z + p2.z + p3.z + p4.z) / 4.0;

    // calculate normal
    normal_1 = TriNormal(p1, p2, center);
    normal_2 = TriNormal(p2, p3, center);
    normal_3 = TriNormal(p3, p4, center);
    normal_4 = TriNormal(p4, p1, center);
    avg_normal.x = (normal_1.x + normal_2.x + normal_3.x + normal_4.x) / 4.0;
    avg_normal.y = (normal_1.y + normal_2.y + normal_3.y + normal_4.y) / 4.0;
    avg_normal.z = (normal_1.z + normal_2.z + normal_3.z + normal_4.z) / 4.0;
    avg_normal = UniformVet(avg_normal);
    ref_pln.a = avg_normal.x; ref_pln.b = avg_normal.y; ref_pln.c = avg_normal.z;
    ref_pln.d = -(ref_pln.a* center.x + ref_pln.b* center.y + ref_pln.c* center.z);
    return ref_pln;
}

void GetRotMatrix(int axis, double angle, double *rot)
{
    float R_Matrix[3][3];
    double rad = 57.2958;
    angle = float(angle / rad);
    R_Matrix[0][0] = 1;
    R_Matrix[0][1] = 0;
    R_Matrix[0][2] = 0;
    R_Matrix[1][0] = 0;
    R_Matrix[1][1] = 1;
    R_Matrix[1][2] = 0;
    R_Matrix[2][0] = 0;
    R_Matrix[2][1] = 0;
    R_Matrix[2][2] = 1;
    switch (axis)
    {
        case 1: // x axis
            rot[0] = R_Matrix[0][0];
            rot[1] = R_Matrix[0][1] * (float)cos(angle) + R_Matrix[0][2] * (float)sin(angle);
            rot[2] = R_Matrix[0][1] * (float)sin(angle)*(-1) + R_Matrix[0][2] * (float)cos(angle);
            rot[3] = R_Matrix[1][0];
            rot[4] = R_Matrix[1][1] * (float)cos(angle) + R_Matrix[1][2] * (float)sin(angle);
            rot[5] = R_Matrix[1][1] * (float)sin(angle)*(-1) + R_Matrix[1][2] * (float)cos(angle);
            rot[6] = R_Matrix[2][0];
            rot[7] = R_Matrix[2][1] * (float)cos(angle) + R_Matrix[2][2] * (float)sin(angle);
            rot[8] = R_Matrix[2][1] * (float)sin(angle)*(-1) + R_Matrix[2][2] * (float)cos(angle);
            break;
        case 2: // y axis
            rot[0] = R_Matrix[0][0] * (float)cos(angle) + R_Matrix[0][2] * (float)sin(angle);
            rot[1] = R_Matrix[0][1];
            rot[2] = R_Matrix[0][0] * (float)sin(angle)*(-1) + R_Matrix[0][2] * (float)cos(angle);
            rot[3] = R_Matrix[1][0] * (float)cos(angle) + R_Matrix[1][2] * (float)sin(angle);
            rot[4] = R_Matrix[1][1];
            rot[5] = R_Matrix[1][0] * (float)sin(angle)*(-1) + R_Matrix[1][2] * (float)cos(angle);
            rot[6] = R_Matrix[2][0] * (float)cos(angle) + R_Matrix[2][2] * (float)sin(angle);
            rot[7] = R_Matrix[2][1];
            rot[8] = R_Matrix[2][0] * (float)sin(angle)*(-1) + R_Matrix[2][2] * (float)cos(angle);
            break;
        case 3: // z axis
            rot[0] = R_Matrix[0][0] * (float)cos(angle) + R_Matrix[0][1] * (float)sin(angle)*(-1);
            rot[1] = R_Matrix[0][0] * (float)sin(angle) + R_Matrix[0][1] * (float)cos(angle);
            rot[2] = R_Matrix[0][2];
            rot[3] = R_Matrix[1][0] * (float)cos(angle) + R_Matrix[1][1] * (float)sin(angle)*(-1);
            rot[4] = R_Matrix[1][0] * (float)sin(angle) + R_Matrix[1][1] * (float)cos(angle);
            rot[5] = R_Matrix[1][2];
            rot[6] = R_Matrix[2][0] * (float)cos(angle) + R_Matrix[2][1] * (float)sin(angle)*(-1);
            rot[7] = R_Matrix[2][0] * (float)sin(angle) + R_Matrix[2][1] * (float)cos(angle);
            rot[8] = R_Matrix[2][2];
            break;
    }
}

Point3Df LTRotPoint(Point3Df pt, double *rot)
{
    Point3Df rsl;
    rsl.x = float(pt.x*rot[0] + pt.y*rot[1] + pt.z*rot[2]);
    rsl.y = float(pt.x*rot[3] + pt.y*rot[4] + pt.z*rot[5]);
    rsl.z = float(pt.x*rot[6] + pt.y*rot[7] + pt.z*rot[8]);
    return rsl;
}

int DepthToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh)
{
    int pt_num, tri_num;
    int i, j, pos_index, pos_index_ex;
    int width, height;
    int v1, v2, v3;
    int hole_size = 100;
    int *pVIdx = NULL;
    Index3i *pTri = NULL;
    Point3Df *p3D = NULL;
    Point3Df *p3DRaw = NULL;
    Point3Df *p3DBuff = NULL;
    if (pDepth == NULL) return -1;
    if (pMesh == NULL) return -2;
    double Kd[3][3] =
            {
                    { double(m_param.FOCAL_LENGTH_Y), double(0), double(m_param.PRINCIPAL_Y) },
                    { double(0), double(m_param.FOCAL_LENGTH_X), double(m_param.PRINCIPAL_X) },
                    { double(0), double(0), double(1) }
            };

    double det = Kd[0][0] * (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) -
                 Kd[0][1] * (Kd[1][0] * Kd[2][2] - Kd[1][2] * Kd[2][0]) +
                 Kd[0][2] * (Kd[1][0] * Kd[2][1] - Kd[1][1] * Kd[2][0]);

    double invdet = double(1) / det;
    double Kd_[3][3];
    Kd_[0][0] = (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) * invdet;
    Kd_[0][1] = (Kd[0][2] * Kd[2][1] - Kd[0][1] * Kd[2][2]) * invdet;
    Kd_[0][2] = (Kd[0][1] * Kd[1][2] - Kd[0][2] * Kd[1][1]) * invdet;
    Kd_[1][0] = (Kd[1][2] * Kd[2][0] - Kd[1][0] * Kd[2][2]) * invdet;
    Kd_[1][1] = (Kd[0][0] * Kd[2][2] - Kd[0][2] * Kd[2][0]) * invdet;
    Kd_[1][2] = (Kd[1][0] * Kd[0][2] - Kd[0][0] * Kd[1][2]) * invdet;
    Kd_[2][0] = (Kd[1][0] * Kd[2][1] - Kd[2][0] * Kd[1][1]) * invdet;
    Kd_[2][1] = (Kd[2][0] * Kd[0][1] - Kd[0][0] * Kd[2][1]) * invdet;
    Kd_[2][2] = (Kd[0][0] * Kd[1][1] - Kd[1][0] * Kd[0][1]) * invdet;

    // save parameters
    FILE *ofp = NULL;
    int rsl = -3;
    int data_count = 0;
    ofp = fopen("Trace_Data.txt", "wt");
    if (ofp != NULL)
    {
        fprintf(ofp, "KD[0][]: %.8f %.8f %.8f\r\n", Kd_[0][0], Kd_[0][1], Kd_[0][2]);
        fprintf(ofp, "KD[1][]: %.8f %.8f %.8f\r\n", Kd_[1][0], Kd_[1][1], Kd_[1][2]);
        fprintf(ofp, "KD[2][]: %.8f %.8f %.8f\r\n", Kd_[2][0], Kd_[2][1], Kd_[2][2]);
    }
    p3DBuff = new Point3Df[depth_width* depth_height];
    for (int j = 0; j<depth_height; j++)
    {
        for (int i = 0; i<depth_width; i++)
        {
            int loc = j*depth_width + i;
            double realdepth = float(pDepth[loc]) / double(16); // Unit mm

            double xz = Kd_[0][0] * double(i) + Kd_[0][1] * double(j) + Kd_[0][2];
            double yz = Kd_[1][0] * double(i) + Kd_[1][1] * double(j) + Kd_[1][2];
            double zz = Kd_[2][0] * double(i) + Kd_[2][1] * double(j) + Kd_[2][2];

            double Xnir = xz / zz*realdepth;
            double Ynir = yz / zz*realdepth;
            double Znir = realdepth;
            pos_index = j*depth_width + i;
            p3DBuff[pos_index].x = float(Xnir);
            p3DBuff[pos_index].y = float(Ynir);
            p3DBuff[pos_index].z = float(Znir);
            if (pDepth[loc]>0)
            {
                data_count++;
            }
        }
    }

    // resample point clouds
    if (sample_step<1) sample_step = 1;
    if (sample_step>16) sample_step = 16;
    width = int(depth_width) / int(sample_step);
    height = int(depth_height) / int(sample_step);
    if (width*height>0)
    {
        pVIdx = new int[width*height];
        pTri = new Index3i[width*height * 2];
        p3DRaw = new Point3Df[width*height];
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                pos_index_ex = (i*sample_step)*depth_width + j*sample_step;
                p3DRaw[pos_index].x = p3DBuff[pos_index_ex].x;
                p3DRaw[pos_index].y = p3DBuff[pos_index_ex].y;
                p3DRaw[pos_index].z = p3DBuff[pos_index_ex].z;
            }
        }
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "b_minx: %.2f b_maxx: %.2f\r\n", bbox.min_x, bbox.max_x);
        fprintf(ofp, "b_miny: %.2f b_maxy: %.2f\r\n", bbox.min_y, bbox.max_y);
        fprintf(ofp, "b_minz: %.2f b_maxz: %.2f\r\n", bbox.min_z, bbox.max_z);
    }
    // click bounding box
    tri_num = 0;
    pt_num = 0;
    for (i = 0; i<height; i++)
    {
        for (j = 0; j<width; j++)
        {
            pos_index = i*width + j;
            if ((p3DRaw[pos_index].x>bbox.min_x) && (p3DRaw[pos_index].x<bbox.max_x) && (p3DRaw[pos_index].y>bbox.min_y) && (p3DRaw[pos_index].y<bbox.max_y) && (p3DRaw[pos_index].z>bbox.min_z) && (p3DRaw[pos_index].z<bbox.max_z))
            {
                pVIdx[pos_index] = pt_num;
                pt_num++;
            }
            else
            {
                pVIdx[pos_index] = -1;
            }
        }
    }
    if (pt_num>0)
    {
        p3D = new Point3Df[pt_num];
        pt_num = 0;
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                if (pVIdx[pos_index] >= 0)
                {
                    // x, y, z
                    p3D[pt_num].x = p3DRaw[pos_index].x;
                    p3D[pt_num].y = p3DRaw[pos_index].y;
                    p3D[pt_num].z = p3DRaw[pos_index].z;
                    pt_num++;
                }
            }
        }
    }
    // rotate points in y axis 180
    double rot[9];
    GetRotMatrix(2, 180, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i]= LTRotPoint(p3D[i], rot);
    }
    // rotate points in x axis -90
    GetRotMatrix(3, 90, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i] = LTRotPoint(p3D[i], rot);
    }

    // construct triangles
    for (i = 0; i<(height - 1); i++)
    {
        for (j = 0; j<(width - 1); j++)
        {
            // triangle 1
            v1 = i*width + j;
            v2 = (i + 1)*width + j;
            v3 = (i + 1)*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
            // triangle 2
            v1 = i*width + j;
            v2 = (i + 1)*width + j + 1;
            v3 = i*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
        }
    }
    // add points cloud to mesh
    Point3Df *mp3D = NULL;
    if ((p3D != NULL) && (pt_num>0))
    {
        mp3D = pMesh->NewPointBuffer(pt_num);
        for (i = 0; i<pt_num; i++)
        {
            mp3D[i].x = p3D[i].x;
            mp3D[i].y = p3D[i].y;
            mp3D[i].z = p3D[i].z;
        }
    }
    // add triangles to mesh
    Index3i *mpTri = NULL;
    if ((pTri != NULL) && (tri_num>0))
    {
        mpTri = pMesh->NewTriangleBuffer(tri_num);
        for (i = 0; i<tri_num; i++)
        {
            mpTri[i].v1 = pTri[i].v1;
            mpTri[i].v2 = pTri[i].v2;
            mpTri[i].v3 = pTri[i].v3;
        }
        rsl = pt_num;
    }
    else
    {
        rsl = -3;
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "pt_num: %d tri_num: %d\r\n", pt_num, tri_num);
    }
    // delete new objects
    if (p3D != NULL) delete[] p3D;
    if (p3DRaw != NULL) delete[] p3DRaw;
    if (pVIdx != NULL) delete[] pVIdx;
    if (pTri != NULL) delete[] pTri;
    if (p3DBuff != NULL) delete[] p3DBuff;
    if (ofp != NULL) fclose(ofp);
    return data_count;
}

int DepthNIRToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, char *nirfilename, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh)
{
    int pt_num, tri_num;
    int i, j, pos_index, pos_index_ex;
    int width, height;
    int v1, v2, v3;
    int *pVIdx = NULL;
    Index3i *pTri = NULL;
    Point3Df *p3D = NULL;
    Point3Df *p3DRaw = NULL;
    Point3Df *p3DBuff = NULL;
    Point2Df *uvmap = NULL;
    int *pImgPosIdx = NULL;
    if (pDepth == NULL) return -1;
    if (pMesh == NULL) return -2;
    double Kd[3][3] =
            {
                    { double(m_param.FOCAL_LENGTH_Y), double(0), double(m_param.PRINCIPAL_Y) },
                    { double(0), double(m_param.FOCAL_LENGTH_X), double(m_param.PRINCIPAL_X) },
                    { double(0), double(0), double(1) }
            };

    double det = Kd[0][0] * (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) -
                 Kd[0][1] * (Kd[1][0] * Kd[2][2] - Kd[1][2] * Kd[2][0]) +
                 Kd[0][2] * (Kd[1][0] * Kd[2][1] - Kd[1][1] * Kd[2][0]);

    double invdet = double(1) / det;
    double Kd_[3][3];
    Kd_[0][0] = (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) * invdet;
    Kd_[0][1] = (Kd[0][2] * Kd[2][1] - Kd[0][1] * Kd[2][2]) * invdet;
    Kd_[0][2] = (Kd[0][1] * Kd[1][2] - Kd[0][2] * Kd[1][1]) * invdet;
    Kd_[1][0] = (Kd[1][2] * Kd[2][0] - Kd[1][0] * Kd[2][2]) * invdet;
    Kd_[1][1] = (Kd[0][0] * Kd[2][2] - Kd[0][2] * Kd[2][0]) * invdet;
    Kd_[1][2] = (Kd[1][0] * Kd[0][2] - Kd[0][0] * Kd[1][2]) * invdet;
    Kd_[2][0] = (Kd[1][0] * Kd[2][1] - Kd[2][0] * Kd[1][1]) * invdet;
    Kd_[2][1] = (Kd[2][0] * Kd[0][1] - Kd[0][0] * Kd[2][1]) * invdet;
    Kd_[2][2] = (Kd[0][0] * Kd[1][1] - Kd[1][0] * Kd[0][1]) * invdet;

    // save parameters
    FILE *ofp = NULL;
    int rsl = -3;
    int data_count = 0;
    ofp = fopen("Trace_Data.txt", "wt");
    if (ofp != NULL)
    {
        fprintf(ofp, "KD[0][]: %.8f %.8f %.8f\r\n", Kd_[0][0], Kd_[0][1], Kd_[0][2]);
        fprintf(ofp, "KD[1][]: %.8f %.8f %.8f\r\n", Kd_[1][0], Kd_[1][1], Kd_[1][2]);
        fprintf(ofp, "KD[2][]: %.8f %.8f %.8f\r\n", Kd_[2][0], Kd_[2][1], Kd_[2][2]);
    }
    p3DBuff = new Point3Df[depth_width* depth_height];
    for (int j = 0; j<depth_height; j++)
    {
        for (int i = 0; i<depth_width; i++)
        {
            int loc = j*depth_width + i;
            double realdepth = float(pDepth[loc]) / double(16); // Unit mm

            double xz = Kd_[0][0] * double(i) + Kd_[0][1] * double(j) + Kd_[0][2];
            double yz = Kd_[1][0] * double(i) + Kd_[1][1] * double(j) + Kd_[1][2];
            double zz = Kd_[2][0] * double(i) + Kd_[2][1] * double(j) + Kd_[2][2];

            double Xnir = xz / zz*realdepth;
            double Ynir = yz / zz*realdepth;
            double Znir = realdepth;
            pos_index = j*depth_width + i;
            p3DBuff[pos_index].x = float(Xnir);
            p3DBuff[pos_index].y = float(Ynir);
            p3DBuff[pos_index].z = float(Znir);
            if (pDepth[loc]>0)
            {
                data_count++;
            }
        }
    }

    // resample point clouds
    if (sample_step<1) sample_step = 1;
    if (sample_step>16) sample_step = 16;
    width = int(depth_width) / int(sample_step);
    height = int(depth_height) / int(sample_step);
    if (width*height>0)
    {
        pVIdx = new int[width*height];
        pTri = new Index3i[width*height * 2];
        p3DRaw = new Point3Df[width*height];
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                pos_index_ex = (i*sample_step)*depth_width + j*sample_step;
                p3DRaw[pos_index].x = p3DBuff[pos_index_ex].x;
                p3DRaw[pos_index].y = p3DBuff[pos_index_ex].y;
                p3DRaw[pos_index].z = p3DBuff[pos_index_ex].z;
            }
        }
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "b_minx: %.2f b_maxx: %.2f\r\n", bbox.min_x, bbox.max_x);
        fprintf(ofp, "b_miny: %.2f b_maxy: %.2f\r\n", bbox.min_y, bbox.max_y);
        fprintf(ofp, "b_minz: %.2f b_maxz: %.2f\r\n", bbox.min_z, bbox.max_z);
    }
    // click bounding box
    tri_num = 0;
    pt_num = 0;
    for (i = 0; i<height; i++)
    {
        for (j = 0; j<width; j++)
        {
            pos_index = i*width + j;
            if ((p3DRaw[pos_index].x>bbox.min_x) && (p3DRaw[pos_index].x<bbox.max_x) && (p3DRaw[pos_index].y>bbox.min_y) && (p3DRaw[pos_index].y<bbox.max_y) && (p3DRaw[pos_index].z>bbox.min_z) && (p3DRaw[pos_index].z<bbox.max_z))
            {
                pVIdx[pos_index] = pt_num;
                pt_num++;
            }
            else
            {
                pVIdx[pos_index] = -1;
            }
        }
    }
    if (pt_num>0)
    {
        p3D = new Point3Df[pt_num];
        uvmap = new Point2Df[pt_num];
        pImgPosIdx = new int[pt_num];
        pt_num = 0;
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                if (pVIdx[pos_index] >= 0)
                {
                    // x, y, z
                    p3D[pt_num].x = p3DRaw[pos_index].x;
                    p3D[pt_num].y = p3DRaw[pos_index].y;
                    p3D[pt_num].z = p3DRaw[pos_index].z;
                    // u, v
                    uvmap[pt_num].x = float(j) / float(width);
                    uvmap[pt_num].y = 1.0- float(i) / float(height);
                    // assign image pos index
                    pImgPosIdx[pt_num] = pos_index;
                    pt_num++;
                }
            }
        }
    }
    // rotate points in y axis 180
    double rot[9];
    GetRotMatrix(2, 180, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i] = LTRotPoint(p3D[i], rot);
    }
    // rotate points in x axis -90
    GetRotMatrix(3, 90, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i] = LTRotPoint(p3D[i], rot);
    }
    // construct triangles
    for (i = 0; i<(height - 1); i++)
    {
        for (j = 0; j<(width - 1); j++)
        {
            // triangle 1
            v1 = i*width + j;
            v2 = (i + 1)*width + j;
            v3 = (i + 1)*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
            // triangle 2
            v1 = i*width + j;
            v2 = (i + 1)*width + j + 1;
            v3 = i*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
        }
    }
    // add points cloud to mesh
    Point3Df *mp3D = NULL;
    Point2Df *mpUV = NULL;
    int* mpIPosIdx = NULL;
    if ((p3D != NULL) && (pt_num>0))
    {
        mp3D = pMesh->NewPointBuffer(pt_num);
        mpUV = pMesh->NewTexcoBuffer();
        mpIPosIdx = pMesh->NewTextPosIdxBuffer();
        for (i = 0; i<pt_num; i++)
        {
            mp3D[i].x = p3D[i].x;
            mp3D[i].y = p3D[i].y;
            mp3D[i].z = p3D[i].z;
            mpUV[i].x = uvmap[i].x;
            mpUV[i].y = uvmap[i].y;
            mpIPosIdx[i] = pImgPosIdx[i];
        }
        // set texture filename
        pMesh->SetTextFileName(nirfilename);
    }
    // add triangles to mesh
    Index3i *mpTri = NULL;
    if ((pTri != NULL) && (tri_num>0))
    {
        mpTri = pMesh->NewTriangleBuffer(tri_num);
        for (i = 0; i<tri_num; i++)
        {
            mpTri[i].v1 = pTri[i].v1;
            mpTri[i].v2 = pTri[i].v2;
            mpTri[i].v3 = pTri[i].v3;
        }
        rsl = pt_num;
    }
    else
    {
        rsl = -3;
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "pt_num: %d tri_num: %d\r\n", pt_num, tri_num);
    }
    // delete new objects
    if (p3D != NULL) delete[] p3D;
    if (p3DRaw != NULL) delete[] p3DRaw;
    if (pVIdx != NULL) delete[] pVIdx;
    if (pTri != NULL) delete[] pTri;
    if (p3DBuff != NULL) delete[] p3DBuff;
    if (uvmap != NULL) delete[] uvmap;
    if (ofp != NULL) fclose(ofp);
    return data_count;
}

int DepthNir2RGBHoloMtxToTriMesh(unsigned short *pDepth, int depth_width, int depth_height, char *rgbfilename, float* pHoloMtx, DepthCameraParam m_param, BoundingBox bbox, int sample_step, ITRITriMesh *pMesh)
{
    int pt_num, tri_num;
    int i, j, pos_index, pos_index_ex;
    int width, height;
    int v1, v2, v3;
    int *pVIdx = NULL;
    Index3i *pTri = NULL;
    Point3Df *p3D = NULL;
    Point3Df *p3DRaw = NULL;
    Point3Df *p3DBuff = NULL;
    Point2Df *uvmap = NULL;
    if (pDepth == NULL) return -1;
    if (pMesh == NULL) return -2;
    double Kd[3][3] =
            {
                    { double(m_param.FOCAL_LENGTH_Y), double(0), double(m_param.PRINCIPAL_Y) },
                    { double(0), double(m_param.FOCAL_LENGTH_X), double(m_param.PRINCIPAL_X) },
                    { double(0), double(0), double(1) }
            };

    double det = Kd[0][0] * (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) -
                 Kd[0][1] * (Kd[1][0] * Kd[2][2] - Kd[1][2] * Kd[2][0]) +
                 Kd[0][2] * (Kd[1][0] * Kd[2][1] - Kd[1][1] * Kd[2][0]);

    double invdet = double(1) / det;
    double Kd_[3][3];
    Kd_[0][0] = (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) * invdet;
    Kd_[0][1] = (Kd[0][2] * Kd[2][1] - Kd[0][1] * Kd[2][2]) * invdet;
    Kd_[0][2] = (Kd[0][1] * Kd[1][2] - Kd[0][2] * Kd[1][1]) * invdet;
    Kd_[1][0] = (Kd[1][2] * Kd[2][0] - Kd[1][0] * Kd[2][2]) * invdet;
    Kd_[1][1] = (Kd[0][0] * Kd[2][2] - Kd[0][2] * Kd[2][0]) * invdet;
    Kd_[1][2] = (Kd[1][0] * Kd[0][2] - Kd[0][0] * Kd[1][2]) * invdet;
    Kd_[2][0] = (Kd[1][0] * Kd[2][1] - Kd[2][0] * Kd[1][1]) * invdet;
    Kd_[2][1] = (Kd[2][0] * Kd[0][1] - Kd[0][0] * Kd[2][1]) * invdet;
    Kd_[2][2] = (Kd[0][0] * Kd[1][1] - Kd[1][0] * Kd[0][1]) * invdet;

    // save parameters
    FILE *ofp = NULL;
    int rsl = -3;
    int data_count = 0;
    ofp = fopen("Trace_Data.txt", "wt");
    if (ofp != NULL)
    {
        fprintf(ofp, "KD[0][]: %.8f %.8f %.8f\r\n", Kd_[0][0], Kd_[0][1], Kd_[0][2]);
        fprintf(ofp, "KD[1][]: %.8f %.8f %.8f\r\n", Kd_[1][0], Kd_[1][1], Kd_[1][2]);
        fprintf(ofp, "KD[2][]: %.8f %.8f %.8f\r\n", Kd_[2][0], Kd_[2][1], Kd_[2][2]);
    }
    p3DBuff = new Point3Df[depth_width* depth_height];
    for (int j = 0; j<depth_height; j++)
    {
        for (int i = 0; i<depth_width; i++)
        {
            int loc = j*depth_width + i;
            double realdepth = float(pDepth[loc]) / double(16); // Unit mm

            double xz = Kd_[0][0] * double(i) + Kd_[0][1] * double(j) + Kd_[0][2];
            double yz = Kd_[1][0] * double(i) + Kd_[1][1] * double(j) + Kd_[1][2];
            double zz = Kd_[2][0] * double(i) + Kd_[2][1] * double(j) + Kd_[2][2];

            double Xnir = xz / zz*realdepth;
            double Ynir = yz / zz*realdepth;
            double Znir = realdepth;
            pos_index = j*depth_width + i;
            p3DBuff[pos_index].x = float(Xnir);
            p3DBuff[pos_index].y = float(Ynir);
            p3DBuff[pos_index].z = float(Znir);
            if (pDepth[loc]>0)
            {
                data_count++;
            }
        }
    }

    // resample point clouds
    if (sample_step<1) sample_step = 1;
    if (sample_step>16) sample_step = 16;
    width = int(depth_width) / int(sample_step);
    height = int(depth_height) / int(sample_step);
    if (width*height>0)
    {
        pVIdx = new int[width*height];
        pTri = new Index3i[width*height * 2];
        p3DRaw = new Point3Df[width*height];
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                pos_index_ex = (i*sample_step)*depth_width + j*sample_step;
                p3DRaw[pos_index].x = p3DBuff[pos_index_ex].x;
                p3DRaw[pos_index].y = p3DBuff[pos_index_ex].y;
                p3DRaw[pos_index].z = p3DBuff[pos_index_ex].z;
            }
        }
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "b_minx: %.2f b_maxx: %.2f\r\n", bbox.min_x, bbox.max_x);
        fprintf(ofp, "b_miny: %.2f b_maxy: %.2f\r\n", bbox.min_y, bbox.max_y);
        fprintf(ofp, "b_minz: %.2f b_maxz: %.2f\r\n", bbox.min_z, bbox.max_z);
    }
    // click bounding box
    tri_num = 0;
    pt_num = 0;
    for (i = 0; i<height; i++)
    {
        for (j = 0; j<width; j++)
        {
            pos_index = i*width + j;
            if ((p3DRaw[pos_index].x>bbox.min_x) && (p3DRaw[pos_index].x<bbox.max_x) && (p3DRaw[pos_index].y>bbox.min_y) && (p3DRaw[pos_index].y<bbox.max_y) && (p3DRaw[pos_index].z>bbox.min_z) && (p3DRaw[pos_index].z<bbox.max_z))
            {
                pVIdx[pos_index] = pt_num;
                pt_num++;
            }
            else
            {
                pVIdx[pos_index] = -1;
            }
        }
    }
    int ux1, vy1;
    int ux2, vy2;
    if (pt_num>0)
    {
        p3D = new Point3Df[pt_num];
        uvmap = new Point2Df[pt_num];
        pt_num = 0;
        for (i = 0; i<height; i++)
        {
            for (j = 0; j<width; j++)
            {
                pos_index = i*width + j;
                if (pVIdx[pos_index] >= 0)
                {
                    // x, y, z
                    p3D[pt_num].x = p3DRaw[pos_index].x;
                    p3D[pt_num].y = p3DRaw[pos_index].y;
                    p3D[pt_num].z = p3DRaw[pos_index].z;
                    // hologram matrix apply to x y position
                    ux1 = j;
                    vy1 = i;
                    ux2 = int(pHoloMtx[0] * ux1 + pHoloMtx[1] * vy1 + pHoloMtx[2]);
                    vy2 = int(pHoloMtx[3] * ux1 + pHoloMtx[4] * vy1 + pHoloMtx[5]);
                    // calculate u, v
                    uvmap[pt_num].x = float(ux2) / float(width);
                    uvmap[pt_num].y = 1.0 - float(vy2) / float(height);
                    pt_num++;
                }
            }
        }
    }
    // rotate points in y axis 180
    double rot[9];
    GetRotMatrix(2, 180, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i] = LTRotPoint(p3D[i], rot);
    }
    // rotate points in x axis -90
    GetRotMatrix(3, 90, rot);
    for (i = 0; i < pt_num; i++)
    {
        p3D[i] = LTRotPoint(p3D[i], rot);
    }

    // construct triangles
    for (i = 0; i<(height - 1); i++)
    {
        for (j = 0; j<(width - 1); j++)
        {
            // triangle 1
            v1 = i*width + j;
            v2 = (i + 1)*width + j;
            v3 = (i + 1)*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
            // triangle 2
            v1 = i*width + j;
            v2 = (i + 1)*width + j + 1;
            v3 = i*width + j + 1;
            if ((pVIdx[v1] >= 0) && (pVIdx[v2] >= 0) && (pVIdx[v3] >= 0))
            {
                pTri[tri_num].v1 = pVIdx[v1];
                pTri[tri_num].v2 = pVIdx[v2];
                pTri[tri_num].v3 = pVIdx[v3];
                tri_num++;
            }
        }
    }
    // add points cloud to mesh
    Point3Df *mp3D = NULL;
    Point2Df *mpUV = NULL;
    if ((p3D != NULL) && (pt_num>0))
    {
        mp3D = pMesh->NewPointBuffer(pt_num);
        mpUV = pMesh->NewTexcoBuffer();
        for (i = 0; i<pt_num; i++)
        {
            mp3D[i].x = p3D[i].x;
            mp3D[i].y = p3D[i].y;
            mp3D[i].z = p3D[i].z;
            mpUV[i].x = uvmap[i].x;
            mpUV[i].y = uvmap[i].y;
        }
        // set texture filename
        pMesh->SetTextFileName(rgbfilename);
    }
    // add triangles to mesh
    Index3i *mpTri = NULL;
    if ((pTri != NULL) && (tri_num>0))
    {
        mpTri = pMesh->NewTriangleBuffer(tri_num);
        for (i = 0; i<tri_num; i++)
        {
            mpTri[i].v1 = pTri[i].v1;
            mpTri[i].v2 = pTri[i].v2;
            mpTri[i].v3 = pTri[i].v3;
        }
        rsl = pt_num;
    }
    else
    {
        rsl = -3;
    }
    if (ofp != NULL)
    {
        fprintf(ofp, "pt_num: %d tri_num: %d\r\n", pt_num, tri_num);
    }
    // delete new objects
    if (p3D != NULL) delete[] p3D;
    if (p3DRaw != NULL) delete[] p3DRaw;
    if (pVIdx != NULL) delete[] pVIdx;
    if (pTri != NULL) delete[] pTri;
    if (p3DBuff != NULL) delete[] p3DBuff;
    if (uvmap != NULL) delete[] uvmap;
    if (ofp != NULL) fclose(ofp);
    return data_count;
}

int TriMeshAttRGBIMG(ITRITriMesh *pMesh, RGBAttMeshParam m_param, char *rgbfilename, int img_width, int img_height)
{
    Point2Df *uvmap = NULL;
    Point3Df *p3DBuff = NULL;
    int pt_num;
    if (pMesh == NULL) return -1;
    if ((img_width != m_param.img_width) | (img_height != m_param.img_height)) return -2;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    // caicluate uv map
    uvmap = pMesh->NewTexcoBuffer();
    for (int i = 0; i < pt_num; i++)
    {
        uvmap[i].x = (m_param.cx + p3DBuff[i].x* (m_param.sx + (p3DBuff[i].z - m_param.tz)* m_param.tx)) / float(m_param.img_width);
        uvmap[i].y = (m_param.cy + p3DBuff[i].y* (m_param.sy + (p3DBuff[i].z - m_param.tz)* m_param.ty)) / float(m_param.img_height);
        /* image rotate 180
         uvmap[i].x = (m_param.cx - p3DBuff[i].x* m_param.sx - (p3DBuff[i].z - m_param.tz)* m_param.tx) / float(m_param.img_width);
         uvmap[i].y = (m_param.cy - p3DBuff[i].y* m_param.sy - (p3DBuff[i].z - m_param.tz)* m_param.ty) / float(m_param.img_height);
         */
    }
    // set texture filename

    pMesh->SetTextFileName(rgbfilename);
    return 1;
}

int TriMeshAttRGBIMGEx(ITRITriMesh *pMesh, RGBAttMeshParam m_param, char *rgbfilename, int img_width, int img_height)
{
    Point2Df *uvmap = NULL;
    Point3Df *p3DBuff = NULL;
    int pt_num;
    if (pMesh == NULL) return -1;
    if ((img_width != m_param.img_width) | (img_height != m_param.img_height)) return -2;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    // caicluate uv map
    uvmap = pMesh->NewTexcoBufferEx();
    for (int i = 0; i < pt_num; i++)
    {
        uvmap[i].x = (m_param.cx + p3DBuff[i].x* (m_param.sx + (p3DBuff[i].z - m_param.tz)* m_param.tx)) / float(m_param.img_width);
        uvmap[i].y = (m_param.cy + p3DBuff[i].y* (m_param.sy + (p3DBuff[i].z - m_param.tz)* m_param.ty)) / float(m_param.img_height);
        /* image rotate 180
         uvmap[i].x = (m_param.cx - p3DBuff[i].x* m_param.sx - (p3DBuff[i].z - m_param.tz)* m_param.tx) / float(m_param.img_width);
         uvmap[i].y = (m_param.cy - p3DBuff[i].y* m_param.sy - (p3DBuff[i].z - m_param.tz)* m_param.ty) / float(m_param.img_height);
         */
    }
    // set texture filename

    pMesh->SetTextFileNameEx(rgbfilename);
    return 1;
}

int TriMeshAttThermalRGBIMGEx(ITRITriMesh *pMesh, char *rgbfilename, int img_width, int img_height)
{
    Point2Df *uvmap = NULL;
    Point3Df *p3DBuff = NULL;
    Point2Df *pTexCoBuff = NULL;
    int pt_num;
    if (pMesh == NULL) return -1;
    pTexCoBuff = pMesh->GetTexcoBuffer();
    if (pTexCoBuff == NULL) return -2;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    // caicluate uv map
    uvmap = pMesh->NewTexcoBufferEx();
    for (int i = 0; i < pt_num; i++)
    {
        uvmap[i].x = pTexCoBuff[i].x;
        uvmap[i].y = pTexCoBuff[i].y;
    }
    // set texture filename
    pMesh->SetTextFileNameEx(rgbfilename);
    return 1;
}

int TriMeshAttThermalIMG(ITRITriMesh *pMesh, ThermalAttMeshParam m_param, float *pThermalData, int img_width, int img_height)
{
    Point3Df *p3DBuff = NULL;
    float *pTemp = NULL;
    int pt_num;
    int pos_x, pos_y, pos_index;
    if (pMesh == NULL) return -1;
    if ((img_width != m_param.img_width) | (img_height != m_param.img_height)) return -2;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    // caicluate uv map
    pTemp = pMesh->NewPointTempture(pt_num);
    for (int i = 0; i < pt_num; i++)
    {
        if (p3DBuff[i].x > 0)
        {
            pos_x = m_param.cx + p3DBuff[i].x* (m_param.sx + (p3DBuff[i].z - m_param.tz)* m_param.tx);
        }
        else
        {
            pos_x = m_param.cx + p3DBuff[i].x*( m_param.sx - (p3DBuff[i].z - m_param.tz)* m_param.tx);
        }
        if (p3DBuff[i].y > 0)
        {
            pos_y = m_param.cy - p3DBuff[i].y* (m_param.sy - (p3DBuff[i].z - m_param.tz)* m_param.ty);
        }
        else
        {
            pos_y = m_param.cy - p3DBuff[i].y* (m_param.sy + (p3DBuff[i].z - m_param.tz)* m_param.ty);
        }
        if (pos_x < 0) pos_x = 0;
        if (pos_x >= img_width) pos_x = img_width - 1;
        if (pos_y < 0) pos_y = 0;
        if (pos_y >= img_height) pos_y = img_height - 1;
        pos_index = pos_y*img_width + pos_x;
        pTemp[i] = pThermalData[pos_index];
    }
    return 1;
}

int TriMeshAttThermalIMGEx(ITRITriMesh *pMesh, float *pThermalData, int img_width, int img_height)
{
    int* pImgPosIdx = NULL;
    float *pTemp = NULL;
    int i, pt_num;
    Point3Df *p3DBuff = NULL;
    if (pMesh == NULL) return -1;
    if (pThermalData == NULL) return -2;
    pImgPosIdx = pMesh->GetImgPosIdxBuffer();
    if (pImgPosIdx == NULL) return -3;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    if (pt_num > 0)
    {
        // caicluate uv map
        pTemp = pMesh->NewPointTempture(pt_num);
        for (i = 0; i < pt_num; i++)
        {
            pTemp[i] = pThermalData[pImgPosIdx[i]];
        }
    }
    return 1;
}

int CalWoundAreaInfo(unsigned char *pMask, int img_width, int img_height, ITRITriMesh *pMesh, WoundAreaInfo *pInfo)
{
    int rsl = 1;
    Point2Df* pTexCo = NULL;
    Point3Df *p3DBuff = NULL;
    int pt_num, tri_num;
    Index3i* pTri = NULL;
    bool *pTriSel = NULL;
    bool *pPtSel = NULL;
    float *pTemp = NULL;
    int i, v1, v2, v3;
    Point3Df p1, p2, p3;
    int v1_img_pos, v2_img_pos, v3_img_pos;
    bool v1_sel, v2_sel, v3_sel;
    float depth;
    float wound_area, max_depth;
    float h_temp, l_temp;
    Temp_info high_temp, low_temp;
    // boundary plane
    Plane bdy_plane;

    int tri_sel_count = 0;
    int pt_sel_count = 0;

    // init value
    wound_area = max_depth = 0;
    h_temp = -1000;
    l_temp = 1000;
    max_depth = 0;

    high_temp.temp = 0;
    high_temp.location_x = 0;
    high_temp.location_y = 0;
    low_temp.temp = 0;
    low_temp.location_x = 0;
    low_temp.location_y = 0;

    pInfo->wound_area= 0;
    pInfo->wound_high_temp.location_x = 0;
    pInfo->wound_high_temp.location_y = 0;
    pInfo->wound_high_temp.temp = 0;
    pInfo->wound_low_temp.location_x = 0;
    pInfo->wound_low_temp.location_y = 0;
    pInfo->wound_low_temp.temp = 0;
    pInfo->wound_max_depth = 0;

    if (pMask == NULL) return -1;
    if (pMesh == NULL) return -2;
    pTexCo = pMesh->GetTexcoBuffer();
    if (pTexCo == NULL) return -3;// check the texture coordinate was exited
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    if (tri_num <= 0) return -4;// if there is no triangle return -4
    if (pt_num <= 0) return -5;
    pTemp= pMesh->GetTemperatureBuffer();
    if (pTemp == NULL) rsl= 0;// check temperature buffer
    pTriSel = new bool[tri_num];
    pPtSel = new bool[pt_num];
    for (i = 0; i < tri_num; i++)
    {
        pTriSel[i] = false;
    }
    for (i = 0; i < pt_num; i++)
    {
        pPtSel[i] = false;
    }
    // check triangle selected by mask image
    Point2Df uv1, uv2, uv3;
    int img_size;
    img_size = img_width*img_height;
    for (i = 0; i < tri_num; i++)
    {
        uv1.x = pTexCo[pTri[i].v1].x;
        uv1.y = 1.0 - pTexCo[pTri[i].v1].y;
        uv2.x = pTexCo[pTri[i].v2].x;
        uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
        uv3.x = pTexCo[pTri[i].v3].x;
        uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
        if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0)&& (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0)&& (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
        {
            v1_img_pos = int(uv1.y*img_height)* img_width + int(uv1.x*img_width);
            v2_img_pos = int(uv2.y*img_height)* img_width + int(uv2.x*img_width);
            v3_img_pos = int(uv3.y*img_height)* img_width + int(uv3.x*img_width);
            if ((pMask[v1_img_pos] > 0) && (pMask[v2_img_pos] > 0) && (pMask[v3_img_pos] > 0))
                //			if (((pMask[v1_img_pos]==1)||((pMask[v1_img_pos] == 3))) && ((pMask[v2_img_pos]==1)|| (pMask[v2_img_pos]==3)) && ((pMask[v3_img_pos]==1)|| (pMask[v3_img_pos]==3)))
                //			if ((pMask[v1_img_pos] == 1) && (pMask[v2_img_pos] == 1) && (pMask[v3_img_pos] == 1))
            {
                pTriSel[i] = true;
                pPtSel[pTri[i].v1] = true;
                pPtSel[pTri[i].v2] = true;
                pPtSel[pTri[i].v3] = true;
                tri_sel_count++;
            }
        }
    }
    // construct selected triangle boundary plane
    bdy_plane.a = 0; bdy_plane.b = 0; bdy_plane.c = 1; bdy_plane.d = 0;
    Point3Df pt, max_x_pt, min_x_pt, max_y_pt, min_y_pt;
    float max_x = -10000.0;
    float min_x = 10000.0;
    float max_y = -10000.0;
    float min_y = 10000.0;
    for (i = 0; i < pt_num; i++)
    {
        if (pPtSel[i])
        {
            pt = p3DBuff[i];
            if (pt.x > max_x) { max_x_pt = pt; max_x = pt.x;}
            if (pt.x < min_x) { min_x_pt = pt; min_x = pt.x;}
            if (pt.y > max_y) { max_y_pt = pt; max_y = pt.y;}
            if (pt.y < min_y) { min_y_pt = pt; min_y = pt.y;}
            pt_sel_count++;
        }
    }
    if (pt_sel_count > 0)
    {
        bdy_plane = ConstrutPlane(max_x_pt, max_y_pt, min_x_pt, min_y_pt);
    }
    else
    {
        rsl = -8;
    }
    // calculate all size
    if (tri_sel_count > 0)
    {
        for (int i = 0; i < tri_num; i++)
        {
            if (pTriSel[i])
            {
                v1 = pTri[i].v1; v2 = pTri[i].v2; v3 = pTri[i].v3;
                p1 = p3DBuff[v1]; p2 = p3DBuff[v2]; p3 = p3DBuff[v3];
                // calculate selected area
                wound_area = wound_area + TriArea(p1, p2, p3);
                // calculate highest temperature of selected area
                if (pTemp != NULL)
                {
                    if (pTemp[v1] > h_temp)
                    {
                        h_temp = pTemp[v1];
                        high_temp.location_x = pTexCo[v1].x * img_width;
                        high_temp.location_y = pTexCo[v1].y * img_height;
                        high_temp.temp = h_temp;
                    }
                    // calculate lowest temperature of selected area
                    if (pTemp[v1] < l_temp)
                    {
                        l_temp = pTemp[v1];
                        low_temp.location_x = pTexCo[v1].x * img_width;
                        low_temp.location_y = pTexCo[v1].y * img_height;
                        low_temp.temp = l_temp;
                    }
                }
            }
        }
    }
    else
    {
        rsl = -6;
    }
    // calculate selected depth
    if (pt_sel_count > 0)
    {
        for (i = 0; i < pt_num; i++)
        {
            if (pPtSel[i])
            {
                depth = float(fabs(Height2Plane(p3DBuff[i], bdy_plane)));
                if (depth > max_depth) max_depth = depth;
            }
        }
    }
    else
    {
        rsl = -7;
    }
    // reserved selected triangles
    int *pTriSelIdx= NULL;
    pTriSelIdx = new int[tri_sel_count];
    tri_sel_count = 0;
    for (int i = 0; i < tri_num; i++)
    {
        if (pTriSel[i])
        {
            pTriSelIdx[tri_sel_count] = i;
            tri_sel_count++;
        }
    }
    pMesh->DeleteTriangle(pTriSelIdx, tri_sel_count);
    if (pTriSelIdx != NULL) delete[] pTriSelIdx;

    if (pTriSel != NULL) delete[] pTriSel;
    if (pPtSel != NULL) delete[] pPtSel;
    pInfo->wound_area = wound_area;
    pInfo->wound_high_temp.location_x = high_temp.location_x;
    pInfo->wound_high_temp.location_y = high_temp.location_y;
    pInfo->wound_high_temp.temp = high_temp.temp;
    pInfo->wound_low_temp.location_x = low_temp.location_x;
    pInfo->wound_low_temp.location_y = low_temp.location_y;
    pInfo->wound_low_temp.temp = low_temp.temp;
    pInfo->wound_max_depth = max_depth;
    return rsl;
}

double TriArea2D(int p1x, int p1y, int p2x, int p2y, int p3x, int p3y)
{
    double result;

    result = 0.5*fabs(float(p1x*(p2y - p3y) - p1y*(p2x - p3x) + p2x*p3y - p3x*p2y));
    return result;
}

int LTCheckPtsInTriAngle2D(int px, int py, int p1x, int p1y, int p2x, int p2y, int p3x, int p3y)
{
    int rsl = -1;
    double area1, area2, area3, area;
    area1 = TriArea2D(px, py, p1x, p1y, p2x, p2y);
    area2 = TriArea2D(px, py, p2x, p2y, p3x, p3y);
    area3 = TriArea2D(px, py, p3x, p3y, p1x, p1y);
    area = TriArea2D(p1x, p1y, p2x, p2y, p3x, p3y);
    if(((area1 + area2 + area3)>(area - 0.1)) && ((area1 + area2 + area3)<(area + 0.1))) rsl = 1;
    return rsl;
}

// two points on the texture to get the mapping distance in 3D data
float GetTwoPointsDistOnTexture(ITRITriMesh *pMesh, int x1, int y1, int x2, int y2, int img_width, int img_height)
{
    float dist = 0;
    Point2Df* pTexCo = NULL;
    Point3Df *p3DBuff = NULL;
    int i, pt_num, tri_num;
    Index3i* pTri = NULL;

    Point3Df p1, p2;
    if (pMesh == NULL) return -1;
    pTexCo = pMesh->GetTexcoBuffer();
    if (pTexCo == NULL) return -2;// check the texture coordinate was exited
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    if (tri_num <= 0) return -3;// if there is no triangle return -4
    if (pt_num <= 0) return -4;
    //find p1 from x1, y1
    Point2Df uv1, uv2, uv3;
    int img_size;
    img_size = img_width*img_height;
    double r1, r2, r3;
    Point3Df vt1, vt2, vt3;
    int flag_p1 = -1;
    int flag_p2 = -1;
    int u1, v1, u2, v2, u3, v3;
    for (i = 0; i < tri_num; i++)
    {
        uv1.x = pTexCo[pTri[i].v1].x;
        uv1.y = 1.0- pTexCo[pTri[i].v1].y;
        uv2.x = pTexCo[pTri[i].v2].x;
        uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
        uv3.x = pTexCo[pTri[i].v3].x;
        uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
        if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0) && (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0) && (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
        {
            u1 = uv1.x* img_width;
            v1 = uv1.y* img_height;
            u2 = uv2.x* img_width;
            v2 = uv2.y* img_height;
            u3 = uv3.x* img_width;
            v3 = uv3.y* img_height;
            if (LTCheckPtsInTriAngle2D(x1, y1, u1, v1, u2, v2, u3, v3) > 0)
            {
                double area1, area2, area3, area;
                area1 = TriArea2D(x1, y1, u2, v2, u3, v3);
                area2 = TriArea2D(x1, y1, u1, v1, u3, v3);
                area3 = TriArea2D(x1, y1, u1, v1, u2, v2);
                area = TriArea2D(u1, v1, u2, v2, u3, v3);
                r1 = area1 / area;
                r2 = area2 / area;
                r3 = area3 / area;
                vt1 = p3DBuff[pTri[i].v1];
                vt2 = p3DBuff[pTri[i].v2];
                vt3 = p3DBuff[pTri[i].v3];
                p1.x = r1* vt1.x + r2* vt2.x + r3*vt3.x;
                p1.y = r1* vt1.y + r2* vt2.y + r3*vt3.y;
                p1.z = r1* vt1.z + r2* vt2.z + r3*vt3.z;
                flag_p1 = 1;
                break;
            }
        }
    }

    //find p2 from x2, y2
    for (i = 0; i < tri_num; i++)
    {
        uv1.x = pTexCo[pTri[i].v1].x;
        uv1.y = 1.0 - pTexCo[pTri[i].v1].y;
        uv2.x = pTexCo[pTri[i].v2].x;
        uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
        uv3.x = pTexCo[pTri[i].v3].x;
        uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
        if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0) && (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0) && (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
        {
            u1 = uv1.x* img_width;
            v1 = uv1.y* img_height;
            u2 = uv2.x* img_width;
            v2 = uv2.y* img_height;
            u3 = uv3.x* img_width;
            v3 = uv3.y* img_height;
            if (LTCheckPtsInTriAngle2D(x2, y2, u1, v1, u2, v2, u3, v3) > 0)
            {
                double area1, area2, area3, area;
                area1 = TriArea2D(x2, y2, u2, v2, u3, v3);
                area2 = TriArea2D(x2, y2, u1, v1, u3, v3);
                area3 = TriArea2D(x2, y2, u1, v1, u2, v2);
                area = TriArea2D(u1, v1, u2, v2, u3, v3);
                r1 = area1 / area;
                r2 = area2 / area;
                r3 = area3 / area;
                vt1 = p3DBuff[pTri[i].v1];
                vt2 = p3DBuff[pTri[i].v2];
                vt3 = p3DBuff[pTri[i].v3];
                p2.x = r1* vt1.x + r2* vt2.x + r3*vt3.x;
                p2.y = r1* vt1.y + r2* vt2.y + r3*vt3.y;
                p2.z = r1* vt1.z + r2* vt2.z + r3*vt3.z;
                flag_p2 = 1;
                break;
            }
        }
    }
    //  calculate two points distance
    if ((flag_p1 > 0) && (flag_p2 > 0))
    {
        dist = sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y) + (p1.z - p2.z)*(p1.z - p2.z));
    }
    return dist;
}

// get thermal information on the texture point
float GetPointThermalOnTexture(ITRITriMesh *pMesh, int x, int y, int img_width, int img_height)
{
    float pt_thermal = 0;
    Point2Df* pTexCo = NULL;
    Point3Df *p3DBuff = NULL;
    int i, pt_num, tri_num;
    Index3i* pTri = NULL;
    float *pTemp = NULL;
    if (pMesh == NULL) return -1;
    pTexCo = pMesh->GetTexcoBuffer();
    if (pTexCo == NULL) return -2;// check the texture coordinate was exited
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    pTemp = pMesh->GetTemperatureBuffer();
    if (tri_num <= 0) return -3;// if there is no triangle return -4
    if (pt_num <= 0) return -4;
    if (pTemp == NULL) return -5;// check temperature buff
    //find point temp from x, y
    Point2Df uv1, uv2, uv3;
    int img_size;
    img_size = img_width*img_height;
    double r1, r2, r3;
    Point3Df vt1, vt2, vt3;
    int u1, v1, u2, v2, u3, v3;
    for (i = 0; i < tri_num; i++)
    {
        uv1.x = pTexCo[pTri[i].v1].x;
        uv1.y = 1.0 - pTexCo[pTri[i].v1].y;
        uv2.x = pTexCo[pTri[i].v2].x;
        uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
        uv3.x = pTexCo[pTri[i].v3].x;
        uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
        if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0) && (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0) && (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
        {
            u1 = uv1.x* img_width;
            v1 = uv1.y* img_height;
            u2 = uv2.x* img_width;
            v2 = uv2.y* img_height;
            u3 = uv3.x* img_width;
            v3 = uv3.y* img_height;
            if (LTCheckPtsInTriAngle2D(x, y, u1, v1, u2, v2, u3, v3) > 0)
            {
                double area1, area2, area3, area;
                area1 = TriArea2D(x, y, u2, v2, u3, v3);
                area2 = TriArea2D(x, y, u1, v1, u3, v3);
                area3 = TriArea2D(x, y, u1, v1, u2, v2);
                area = TriArea2D(u1, v1, u2, v2, u3, v3);
                r1 = area1 / area;
                r2 = area2 / area;
                r3 = area3 / area;
                pt_thermal = r1* pTemp[pTri[i].v1]+ r2* pTemp[pTri[i].v2]+ r3* pTemp[pTri[i].v3];
                break;
            }
        }
    }
    return pt_thermal;
}

Thermal3D GetPointThermal3DOnTexture(ITRITriMesh *pMesh, int x, int y, int img_width, int img_height)
{
    Thermal3D rsl;
    float pt_thermal = 0;
    float px = 0;
    float py = 0;
    float pz = 0;
    Point2Df* pTexCo = NULL;
    Point3Df *p3DBuff = NULL;
    int i, pt_num, tri_num;
    Index3i* pTri = NULL;
    float *pTemp = NULL;
    rsl.thermal = pt_thermal;
    rsl.x = px;
    rsl.y = py;
    rsl.z = pz;
    if (pMesh == NULL) return rsl;
    pTexCo = pMesh->GetTexcoBuffer();
    if (pTexCo == NULL) return rsl;// check the texture coordinate was exited
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    pTemp = pMesh->GetTemperatureBuffer();
    if (tri_num <= 0) return rsl;// if there is no triangle return -4
    if (pt_num <= 0) return rsl;
    if (pTemp == NULL) return rsl;// check temperature buff
    //find point temp from x, y
    Point2Df uv1, uv2, uv3;
    int img_size;
    img_size = img_width*img_height;
    double r1, r2, r3;
    Point3Df vt1, vt2, vt3;
    int u1, v1, u2, v2, u3, v3;
    for (i = 0; i < tri_num; i++)
    {
        uv1.x = pTexCo[pTri[i].v1].x;
        uv1.y = 1.0 - pTexCo[pTri[i].v1].y;
        uv2.x = pTexCo[pTri[i].v2].x;
        uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
        uv3.x = pTexCo[pTri[i].v3].x;
        uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
        if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0) && (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0) && (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
        {
            u1 = uv1.x* img_width;
            v1 = uv1.y* img_height;
            u2 = uv2.x* img_width;
            v2 = uv2.y* img_height;
            u3 = uv3.x* img_width;
            v3 = uv3.y* img_height;
            if (LTCheckPtsInTriAngle2D(x, y, u1, v1, u2, v2, u3, v3) > 0)
            {
                double area1, area2, area3, area;
                area1 = TriArea2D(x, y, u2, v2, u3, v3);
                area2 = TriArea2D(x, y, u1, v1, u3, v3);
                area3 = TriArea2D(x, y, u1, v1, u2, v2);
                area = TriArea2D(u1, v1, u2, v2, u3, v3);
                r1 = area1 / area;
                r2 = area2 / area;
                r3 = area3 / area;
                pt_thermal = r1* pTemp[pTri[i].v1] + r2* pTemp[pTri[i].v2] + r3* pTemp[pTri[i].v3];
                px = r1* p3DBuff[pTri[i].v1].x + r2* p3DBuff[pTri[i].v2].x + r3* p3DBuff[pTri[i].v3].x;
                py = r1* p3DBuff[pTri[i].v1].y + r2* p3DBuff[pTri[i].v2].y + r3* p3DBuff[pTri[i].v3].y;
                pz = r1* p3DBuff[pTri[i].v1].z + r2* p3DBuff[pTri[i].v2].z + r3* p3DBuff[pTri[i].v3].z;
                break;
            }
        }
    }
    rsl.thermal = pt_thermal;
    rsl.x = px;
    rsl.y = py;
    rsl.z = pz;
    return rsl;
}
int FillDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int u_max_gap, int v_max_gap)
{
    int i, j;
    int pos_index, p_idx;
    int total_num;
    unsigned short *tempData = NULL;
    LTHVIndex *lnIdx = NULL;
    int ln_pos_idx;
    int start_v_idx, end_v_idx, diff_v_idx;
    int start_v_val, end_v_val, diff_v_val;
    int start_h_idx, end_h_idx, diff_h_idx;
    int start_h_val, end_h_val, diff_h_val;
    int r_v_idx, r_h_idx;
    float h_weight, v_weight;
    float v_value, h_value;
    if (!pDepth) return -1;
    total_num = depth_width* depth_height;
    if (total_num <= 0) return -2;
    // back up the data
    tempData = new unsigned short[total_num];
    lnIdx = new LTHVIndex[total_num];
    h_weight = float(0.5);
    v_weight = 1.0 - h_weight;
    for (i = 0; i<total_num; i++)
    {
        tempData[i] = pDepth[i];
    }
    // create the vertical line index
    for (i = 0; i<depth_width; i++)
    {
        ln_pos_idx = -1;
        for (j = 0; j<depth_height; j++)
        {
            // create the line index
            pos_index = j*depth_width + i;
            if (pDepth[pos_index]>0)
            {
                ln_pos_idx = j;
            }
            lnIdx[pos_index].v1 = ln_pos_idx;
        }
    }
    for (i = 0; i<depth_width; i++)
    {
        ln_pos_idx = -1;
        for (j = depth_height - 1; j >= 0; j--)
        {
            // create the line index
            pos_index = j*depth_width + i;
            if (pDepth[pos_index]>0)
            {
                ln_pos_idx = j;
            }
            lnIdx[pos_index].v2 = ln_pos_idx;
        }
    }
    // create the horizontal line index
    for (i = 0; i<depth_height; i++)
    {
        ln_pos_idx = -1;
        for (j = 0; j<depth_width; j++)
        {
            // create the line index
            pos_index = i*depth_width + j;
            if (pDepth[pos_index]>0)
            {
                ln_pos_idx = j;
            }
            lnIdx[pos_index].h1 = ln_pos_idx;
        }
    }
    for (i = 0; i<depth_height; i++)
    {
        ln_pos_idx = -1;
        for (j = depth_width - 1; j >= 0; j--)
        {
            // create the line index
            pos_index = i*depth_width + j;
            if (pDepth[pos_index]>0)
            {
                ln_pos_idx = j;
            }
            lnIdx[pos_index].h2 = ln_pos_idx;
        }
    }
    // re-fill the emnpty value
    for (i = 0; i<depth_height; i++)
    {
        for (j = 0; j<depth_width; j++)
        {
            pos_index = i*depth_width + j;
            diff_v_idx = -1;
            diff_h_idx = -1;
            if (pDepth[pos_index]<=0)
            {
                start_v_idx = lnIdx[pos_index].v1;
                end_v_idx = lnIdx[pos_index].v2;
                r_v_idx = end_v_idx - start_v_idx;
                start_h_idx = lnIdx[pos_index].h1;
                end_h_idx = lnIdx[pos_index].h2;
                r_h_idx = end_h_idx - start_h_idx;
                // calculate the v value
                v_value = -1;
                if ((start_v_idx >= 0) && (end_v_idx >= 0))
                {
                    diff_v_idx = end_v_idx - start_v_idx;
                    p_idx = start_v_idx*depth_width + j;
                    start_v_val = tempData[p_idx];
                    p_idx = end_v_idx* depth_width + j;
                    end_v_val = tempData[p_idx];
                    diff_v_val = end_v_val - start_v_val;
                    if (diff_v_idx<v_max_gap)
                    {
                        v_value = float(start_v_val) + float(i - start_v_idx) / float(diff_v_idx)* float(diff_v_val);
                    }
                }
                // calculate the h value
                h_value = -1;
                if ((start_h_idx >= 0) && (end_h_idx >= 0))
                {
                    diff_h_idx = end_h_idx - start_h_idx;
                    p_idx = i*depth_width + start_h_idx;
                    start_h_val = tempData[p_idx];
                    p_idx = i*depth_width + end_h_idx;
                    end_h_val = tempData[p_idx];
                    diff_h_val = end_h_val - start_h_val;
                    if (diff_h_idx<u_max_gap)
                    {
                        h_value = float(start_h_val) + float(j - start_h_idx) / float(diff_h_idx)* float(diff_h_val);
                    }
                }
                pDepth[pos_index] = 0;
                if ((v_value >= 0) && (h_value >= 0))
                {
                    pDepth[pos_index] = (unsigned short)(v_value + h_value) / 2;
                }
                else
                {
                    if (v_value >= 0) pDepth[pos_index] = (unsigned short)(v_value);
                    if (h_value >= 0) pDepth[pos_index] = (unsigned short)(h_value);
                }

            }
        }
    }
    if (lnIdx != NULL) delete[] lnIdx;
    if (tempData != NULL) delete[] tempData;
    return 1;
}

int SmoothDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int range, int times)
{
    int i, j, k, m;
    int p_index, pos_index, total_pt_num;
    unsigned short *pTemp = NULL;
    total_pt_num = depth_width* depth_height;
    pTemp = new unsigned short[total_pt_num];
    unsigned int sum;
    int count;
    for (int k = 0; k < times; k++)
    {
        for (i = 0; i < total_pt_num; i++)
        {
            pTemp[i] = pDepth[i];
        }
        // vertical
        for (i = 0; i < depth_width; i++)
        {
            for (j = range; j < depth_height - range; j++)
            {
                pos_index = j* depth_width + i;
                sum = 0; count = 0;
                for (m = -1 * range; m < 1 * range; m++)
                {
                    p_index = (j + m)* depth_width + i;
                    if (pTemp[p_index] > 0)
                    {
                        sum = sum + pTemp[p_index];
                        count++;
                    }
                }
                if (count > 0)
                {
                    pDepth[pos_index] = (unsigned short)(sum / count);
                }
            }
        }
        // horizontal
        for (i = 0; i < total_pt_num; i++)
        {
            pTemp[i] = pDepth[i];
        }
        for (i = 0; i < depth_height; i++)
        {
            for (j = range; j < depth_width - range; j++)
            {
                pos_index = i* depth_width + j;
                sum = 0; count = 0;
                for (m = -1 * range; m < 1 * range; m++)
                {
                    p_index = i* depth_width + j+ m;
                    if (pTemp[p_index] > 0)
                    {
                        sum = sum + pTemp[p_index];
                        count++;
                    }
                }
                if (count > 0)
                {
                    pDepth[pos_index] = (unsigned short)(sum / count);
                }
            }
        }

    }
    if (pTemp != NULL) delete[] pTemp;
    return 1;
}

int RemoveEdgePeakDepthHV(unsigned short *pDepth, int depth_width, int depth_height, int times)
{
    int i, j, k, m;
    int pos_index1, pos_index2, total_pt_num;
    total_pt_num = depth_width* depth_height;
    unsigned int depth_1, depth_2;
    int count;
    for (int k = 0; k < times; k++)
    {
        // vertical
        for (i = 0; i < depth_width; i++)
        {
            for (j = 0; j < depth_height-1 ; j++)
            {
                pos_index1 = j* depth_width + i;
                pos_index2 = (j+1)* depth_width + i;
                depth_1 = pDepth[pos_index1];
                depth_2 = pDepth[pos_index2];
                if ((depth_1 > 0) && (depth_2 > 0))
                {
                    if (((depth_1 - depth_2) > 160)||((depth_2- depth_1) > 160))
                    {
                        pDepth[pos_index1] = 0;
                        pDepth[pos_index2] = 0;
                    }
                }
            }
        }
        // horizontal
        for (i = 0; i < depth_height; i++)
        {
            for (j = 0; j < depth_width - 1; j++)
            {
                pos_index1 = i* depth_width + j;
                pos_index2 = i* depth_width + j+ 1;
                depth_1 = pDepth[pos_index1];
                depth_2 = pDepth[pos_index2];
                if ((depth_1 > 0) && (depth_2 > 0))
                {
                    if (((depth_1 - depth_2) > 160) || ((depth_2 - depth_1) > 160))
                    {
                        pDepth[pos_index1] = 0;
                        pDepth[pos_index2] = 0;
                    }
                }
            }
        }

    }
    return 1;
}

int BoundClipDepth(unsigned short *pDepth, int depth_width, int depth_height, unsigned short min_depth, unsigned short max_depth)
{
    unsigned short min;
    unsigned short max;
    min = min_depth * 16;
    max = max_depth * 16;
    if (pDepth == NULL) return -1;
    int i, count;
    count = depth_width*depth_height;
    for (i = 0; i < count; i++)
    {
        if ((pDepth[i] < min) || (pDepth[i] > max))
        {
            pDepth[i] = 0;
        }
    }
    return 1;
}

int SmoothTriMesh(ITRITriMesh *pMesh, int smooth_times)
{
    Point3Df *p3DBuff = NULL;
    int i, j, k, pt_num, tri_num;
    float sum_x, sum_y, sum_z;
    Index3i* pTri = NULL;
    int *pTriBdy = NULL;
    Point3Df p1, p2, p3, cpt, avg_pt;
    int bdy_tri_num;
    if (pMesh == NULL) return -1;
    p3DBuff = pMesh->GetPointBuffer(&pt_num);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    if (tri_num <= 0) return -2;// if there is no triangle return -4
    if (pt_num <= 0) return -3;
    for (k = 0; k < smooth_times; k++)
    {
        for (i = 0; i < pt_num; i++)
        {
            pTriBdy = pMesh->GetTriangle(i, &bdy_tri_num);
            sum_x = sum_y = sum_z = 0;
            for (j = 0; j < bdy_tri_num; j++)
            {
                p1 = p3DBuff[pTri[pTriBdy[j]].v1];
                p2 = p3DBuff[pTri[pTriBdy[j]].v2];
                p3 = p3DBuff[pTri[pTriBdy[j]].v3];
                cpt = TriCenter(p1, p2, p3);
                sum_x = sum_x + cpt.x;
                sum_y = sum_y + cpt.y;
                sum_z = sum_z + cpt.z;
            }
            if (bdy_tri_num > 0)
            {
                avg_pt.x = sum_x / float(bdy_tri_num);
                avg_pt.y = sum_y / float(bdy_tri_num);
                avg_pt.z = sum_z / float(bdy_tri_num);
                p3DBuff[i].x = p3DBuff[i].x* 0.5 + avg_pt.x *0.5;
                p3DBuff[i].y = p3DBuff[i].y* 0.5 + avg_pt.y *0.5;
                p3DBuff[i].z = p3DBuff[i].z* 0.5 + avg_pt.z *0.5;
            }
        }
    }
    return 1;
}

int Get3DPointSetOnTexture(ITRITriMesh *pMesh, int pt_num, int* px, int* py, float *p3Dx, float *p3Dy, float *p3Dz, int img_width, int img_height)
{
    int sel_pt_num = 0;
    if (pMesh == NULL) return -1;
    float pt_thermal = 0;
    Point2Df* pTexCo = NULL;
    Point3Df *p3DBuff = NULL;
    int i, j, pnum, tri_num;
    Index3i* pTri = NULL;
    pTexCo = pMesh->GetTexcoBuffer();
    if (pTexCo == NULL) return -2;// check the texture coordinate was exited
    p3DBuff = pMesh->GetPointBuffer(&pnum);
    pTri = pMesh->GetTriangleBuffer(&tri_num);
    if (tri_num <= 0) return -3;// if there is no triangle return -4
    if (pnum <= 0) return -4;
    Point2Df uv1, uv2, uv3;
    int img_size;
    img_size = img_width*img_height;
    double r1, r2, r3;
    Point3Df vt1, vt2, vt3;
    int u1, v1, u2, v2, u3, v3;
    int x, y;
    bool flag_pt_found = false;
    int dist_xy, min_dist_xy;
    for (j = 0; j < pt_num; j++)
    {
        x = px[j];
        y = py[j];
        flag_pt_found = false;
        for (i = 0; i < tri_num; i++)
        {
            uv1.x = pTexCo[pTri[i].v1].x;
            uv1.y = 1.0 - pTexCo[pTri[i].v1].y;
            uv2.x = pTexCo[pTri[i].v2].x;
            uv2.y = 1.0 - pTexCo[pTri[i].v2].y;
            uv3.x = pTexCo[pTri[i].v3].x;
            uv3.y = 1.0 - pTexCo[pTri[i].v3].y;
            if ((uv1.x >= 0) && (uv1.x < 1.0) && (uv1.y >= 0) && (uv1.y < 1.0) && (uv2.x >= 0) && (uv2.x < 1.0) && (uv2.y >= 0) && (uv2.y < 1.0) && (uv3.x >= 0) && (uv3.x < 1.0) && (uv3.y >= 0) && (uv3.y < 1.0))
            {
                u1 = uv1.x* img_width;
                v1 = uv1.y* img_height;
                u2 = uv2.x* img_width;
                v2 = uv2.y* img_height;
                u3 = uv3.x* img_width;
                v3 = uv3.y* img_height;
                if (LTCheckPtsInTriAngle2D(x, y, u1, v1, u2, v2, u3, v3) > 0)
                {
                    double area1, area2, area3, area;
                    area1 = TriArea2D(x, y, u2, v2, u3, v3);
                    area2 = TriArea2D(x, y, u1, v1, u3, v3);
                    area3 = TriArea2D(x, y, u1, v1, u2, v2);
                    area = TriArea2D(u1, v1, u2, v2, u3, v3);
                    r1 = area1 / area;
                    r2 = area2 / area;
                    r3 = area3 / area;
                    p3Dx[j] = r1* p3DBuff[pTri[i].v1].x + r2* p3DBuff[pTri[i].v2].x + r3* p3DBuff[pTri[i].v3].x;
                    p3Dy[j] = r1* p3DBuff[pTri[i].v1].y + r2* p3DBuff[pTri[i].v2].y + r3* p3DBuff[pTri[i].v3].y;
                    p3Dz[j] = r1* p3DBuff[pTri[i].v1].z + r2* p3DBuff[pTri[i].v2].z + r3* p3DBuff[pTri[i].v3].z;
                    sel_pt_num++;
                    flag_pt_found = true;
                    break;
                }
            }
        }
        // check_pt_found, if not find nearest point
        if (!flag_pt_found)
        {
            // find the nearest point to be target
            min_dist_xy = img_width* img_height;
            for (i = 0; i < pnum; i++)
            {
                uv1.x = pTexCo[i].x;
                uv1.y = pTexCo[i].y;
                dist_xy = int(sqrt((x - uv1.x)*(x - uv1.x) + (y - uv1.y)*(y - uv1.y)));
                if (dist_xy < min_dist_xy)
                {
                    p3Dx[j] = p3DBuff[i].x;
                    p3Dy[j] = p3DBuff[i].y;
                    p3Dz[j] = p3DBuff[i].z;
                    min_dist_xy = dist_xy;
                    flag_pt_found = true;
                }
            }
            if(flag_pt_found) sel_pt_num++;
        }
    }
    return sel_pt_num;
}

Func_DZ RegressionDZ(CPt_DZ *ptBuff, int pt_num)
{
    Func_DZ rsl;
    int i;
    float sum_d, sum_z;
    float u_d, u_z;
    float sum_dd, sum_ddz;
    sum_d = 0; sum_z = 0;
    for (i = 0; i < pt_num; i++)
    {
        sum_d = sum_d + ptBuff[i].d;
        sum_z = sum_z + ptBuff[i].z;
    }
    u_d = sum_d / float(pt_num);
    u_z = sum_z / float(pt_num);
    sum_dd = 0; sum_ddz = 0;
    for (i = 0; i < pt_num; i++)
    {
        sum_dd = sum_dd + (ptBuff[i].d- u_d)* (ptBuff[i].d - u_d);
        sum_ddz = sum_ddz + (ptBuff[i].d - u_d)* (ptBuff[i].z - u_z);
    }
    if (sum_dd != 0)
    {
        rsl.m = sum_ddz / sum_dd;
        rsl.c = u_z - rsl.m* u_d;
    }
    else
    {
        rsl.m = 0; rsl.c = 0;
    }
    return rsl;
}

int GetSectPt(CPt2D start_pt, CPt2D end_pt, int sect_pt_num, CPt2D *ptSectBuff)
{
    float dx, dy;
    if (sect_pt_num < 3) return -1;
    dx = float((end_pt.x - start_pt.x)) / float((sect_pt_num-1));
    dy = float((end_pt.y - start_pt.y)) / float((sect_pt_num-1));
    for (int i = 0; i < sect_pt_num; i++)
    {
        ptSectBuff[i].x = start_pt.x + int(dx* i);
        ptSectBuff[i].y = start_pt.y + int(dy* i);
    }
    return sect_pt_num;
}
// get four 3D (x, y, z) points on the depth map with cross point regression morph
int GetFour3DPointsOnDepthWithCPTRegression(unsigned short *pDepth, DepthCameraParam m_param, int depth_width, int depth_height, int pt_num, int* px, int* py, float *p3Dx, float *p3Dy, float *p3Dz)
{
    double Kd[3][3] =
            {
                    { double(m_param.FOCAL_LENGTH_Y), double(0), double(m_param.PRINCIPAL_Y) },
                    { double(0), double(m_param.FOCAL_LENGTH_X), double(m_param.PRINCIPAL_X) },
                    { double(0), double(0), double(1) }
            };

    double det = Kd[0][0] * (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) -
                 Kd[0][1] * (Kd[1][0] * Kd[2][2] - Kd[1][2] * Kd[2][0]) +
                 Kd[0][2] * (Kd[1][0] * Kd[2][1] - Kd[1][1] * Kd[2][0]);

    double invdet = double(1) / det;
    double Kd_[3][3];
    Kd_[0][0] = (Kd[1][1] * Kd[2][2] - Kd[2][1] * Kd[1][2]) * invdet;
    Kd_[0][1] = (Kd[0][2] * Kd[2][1] - Kd[0][1] * Kd[2][2]) * invdet;
    Kd_[0][2] = (Kd[0][1] * Kd[1][2] - Kd[0][2] * Kd[1][1]) * invdet;
    Kd_[1][0] = (Kd[1][2] * Kd[2][0] - Kd[1][0] * Kd[2][2]) * invdet;
    Kd_[1][1] = (Kd[0][0] * Kd[2][2] - Kd[0][2] * Kd[2][0]) * invdet;
    Kd_[1][2] = (Kd[1][0] * Kd[0][2] - Kd[0][0] * Kd[1][2]) * invdet;
    Kd_[2][0] = (Kd[1][0] * Kd[2][1] - Kd[2][0] * Kd[1][1]) * invdet;
    Kd_[2][1] = (Kd[2][0] * Kd[0][1] - Kd[0][0] * Kd[2][1]) * invdet;
    Kd_[2][2] = (Kd[0][0] * Kd[1][1] - Kd[1][0] * Kd[0][1]) * invdet;

    CPt2D p1, p2, p3, p4, cross_pt;
    if (pt_num != 4) return -1;
    // assigne point
    p1.x = px[0]; p1.y = py[0];
    p2.x = px[1]; p2.y = py[1];
    p3.x = px[2]; p3.y = py[2];
    p4.x = px[3]; p4.y = py[3];
    // find xy cross point
    cross_pt= FindCrossXYPt(p1, p2, p3, p4);
    // constrcut cpt to p1, p2, p3, p4 regression (check dx, dy which is bigger to be the main axis and inter 10 points)
    int sect_pt_num = 10;
    int i, pos_idx;
    float target_d, target_z;
    double realdepth, xz, yz, zz;
    double Xnir, Ynir, Znir;
    CPt2D *p2DSect = NULL;
    CPt_DZ *pDZBuff = NULL;
    Func_DZ fun_dz;
    p2DSect = new CPt2D[sect_pt_num];
    pDZBuff = new CPt_DZ[sect_pt_num];
    // p1
    if (GetSectPt(cross_pt, p1, sect_pt_num, p2DSect) > 0)
    {
        for (i = 0; i < sect_pt_num; i++)
        {
            pos_idx = p2DSect[i].y* depth_width + p2DSect[i].x;
            pDZBuff[i].d = sqrt((p2DSect[i].x - cross_pt.x)*(p2DSect[i].x - cross_pt.x) + (p2DSect[i].y - cross_pt.y)*(p2DSect[i].y - cross_pt.y));
            pDZBuff[i].z = float(pDepth[pos_idx]);
        }
        fun_dz= RegressionDZ(pDZBuff, sect_pt_num);
    }
    // calculate new depth of p1, then find x, y, z
    target_d = sqrt((p1.x - cross_pt.x)*(p1.x - cross_pt.x) + (p1.y - cross_pt.y)*(p1.y - cross_pt.y));
    target_z = fun_dz.m* target_d + fun_dz.c;

    realdepth = float(target_z) / double(16); // Unit mm

    xz = Kd_[0][0] * double(p1.x) + Kd_[0][1] * double(p1.y) + Kd_[0][2];
    yz = Kd_[1][0] * double(p1.x) + Kd_[1][1] * double(p1.y) + Kd_[1][2];
    zz = Kd_[2][0] * double(p1.x) + Kd_[2][1] * double(p1.y) + Kd_[2][2];

    p3Dx[0] = xz / zz*realdepth;
    p3Dy[0] = yz / zz*realdepth;
    p3Dz[0] = realdepth;

    // p2
    if (GetSectPt(cross_pt, p2, sect_pt_num, p2DSect) > 0)
    {
        for (i = 0; i < sect_pt_num; i++)
        {
            pos_idx = p2DSect[i].y* depth_width + p2DSect[i].x;
            pDZBuff[i].d = sqrt((p2DSect[i].x - cross_pt.x)*(p2DSect[i].x - cross_pt.x) + (p2DSect[i].y - cross_pt.y)*(p2DSect[i].y - cross_pt.y));
            pDZBuff[i].z = float(pDepth[pos_idx]);
        }
        fun_dz = RegressionDZ(pDZBuff, sect_pt_num);
    }
    // calculate new depth of p1, then find x, y, z
    target_d = sqrt((p2.x - cross_pt.x)*(p2.x - cross_pt.x) + (p2.y - cross_pt.y)*(p2.y - cross_pt.y));
    target_z = fun_dz.m* target_d + fun_dz.c;
    realdepth = float(target_z) / double(16); // Unit mm

    xz = Kd_[0][0] * double(p2.x) + Kd_[0][1] * double(p2.y) + Kd_[0][2];
    yz = Kd_[1][0] * double(p2.x) + Kd_[1][1] * double(p2.y) + Kd_[1][2];
    zz = Kd_[2][0] * double(p2.x) + Kd_[2][1] * double(p2.y) + Kd_[2][2];

    p3Dx[1] = xz / zz*realdepth;
    p3Dy[1] = yz / zz*realdepth;
    p3Dz[1] = realdepth;

    // p3
    if (GetSectPt(cross_pt, p3, sect_pt_num, p2DSect) > 0)
    {
        for (i = 0; i < sect_pt_num; i++)
        {
            pos_idx = p2DSect[i].y* depth_width + p2DSect[i].x;
            pDZBuff[i].d = sqrt((p2DSect[i].x - cross_pt.x)*(p2DSect[i].x - cross_pt.x) + (p2DSect[i].y - cross_pt.y)*(p2DSect[i].y - cross_pt.y));
            pDZBuff[i].z = float(pDepth[pos_idx]);
        }
        fun_dz = RegressionDZ(pDZBuff, sect_pt_num);
    }
    // calculate new depth of p1, then find x, y, z
    target_d = sqrt((p3.x - cross_pt.x)*(p3.x - cross_pt.x) + (p3.y - cross_pt.y)*(p3.y - cross_pt.y));
    target_z = fun_dz.m* target_d + fun_dz.c;
    realdepth = float(target_z) / double(16); // Unit mm

    xz = Kd_[0][0] * double(p3.x) + Kd_[0][1] * double(p3.y) + Kd_[0][2];
    yz = Kd_[1][0] * double(p3.x) + Kd_[1][1] * double(p3.y) + Kd_[1][2];
    zz = Kd_[2][0] * double(p3.x) + Kd_[2][1] * double(p3.y) + Kd_[2][2];

    p3Dx[2] = xz / zz*realdepth;
    p3Dy[2] = yz / zz*realdepth;
    p3Dz[2] = realdepth;

    // p4
    if (GetSectPt(cross_pt, p4, sect_pt_num, p2DSect) > 0)
    {
        for (i = 0; i < sect_pt_num; i++)
        {
            pos_idx = p2DSect[i].y* depth_width + p2DSect[i].x;
            pDZBuff[i].d = sqrt((p2DSect[i].x - cross_pt.x)*(p2DSect[i].x - cross_pt.x) + (p2DSect[i].y - cross_pt.y)*(p2DSect[i].y - cross_pt.y));
            pDZBuff[i].z = float(pDepth[pos_idx]);
        }
        fun_dz = RegressionDZ(pDZBuff, sect_pt_num);
    }
    // calculate new depth of p1, then find x, y, z
    target_d = sqrt((p4.x - cross_pt.x)*(p4.x - cross_pt.x) + (p4.y - cross_pt.y)*(p4.y - cross_pt.y));
    target_z = fun_dz.m* target_d + fun_dz.c;
    realdepth = float(target_z) / double(16); // Unit mm

    xz = Kd_[0][0] * double(p4.x) + Kd_[0][1] * double(p4.y) + Kd_[0][2];
    yz = Kd_[1][0] * double(p4.x) + Kd_[1][1] * double(p4.y) + Kd_[1][2];
    zz = Kd_[2][0] * double(p4.x) + Kd_[2][1] * double(p4.y) + Kd_[2][2];

    p3Dx[3] = xz / zz*realdepth;
    p3Dy[3] = yz / zz*realdepth;
    p3Dz[3] = realdepth;
    return 1;
}

Func_XY TwoPointFunc(CPt2D p1, CPt2D p2)
{
    Func_XY rsl;
    if ((p1.x - p2.x) != 0)
    {
        rsl.a = (p2.y - p1.y) / (p1.x - p2.x);
        rsl.b = 1;
        rsl.c = rsl.a*p1.x + rsl.b* p1.y;
    }
    else
    {
        rsl.a = 1;
        rsl.b = 0;
        rsl.c = p1.x;
    }
    return rsl;
}

// get the cross point
CPt2D FindCrossXYPt(CPt2D p1, CPt2D p2, CPt2D p3, CPt2D p4)
{
    CPt2D rsl;
    Func_XY f1 = TwoPointFunc(p1, p2);
    Func_XY f2 = TwoPointFunc(p3, p4);
    float delta = 0;
    delta = f1.a*f2.b - f2.a* f1.b;
    if(delta != 0)
    {
        rsl.x = (f2.b*f1.c - f1.b*f2.c) / delta;
        rsl.y = (f1.a*f2.c - f2.a*f1.c) / delta;
    }
    else
    {
        rsl.x = -1;
        rsl.y = -1;
    }
    return rsl;
}
