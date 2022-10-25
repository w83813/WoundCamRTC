package org.itri.woundcamrtc.data;


import android.os.Environment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;

import static org.itri.woundcamrtc.AppResultReceiver.SAVE_DIR;

//try {
//        EvalGroup evalGroup = new EvalGroup("2020-09-25 04-58-33-261", "A12345678");
//        evalGroup.updateEvalItem("1","John1");
//        evalGroup.updateEvalItem("2","John2");
//        evalGroup.updateEvalItem("3","John3");
//        String json =  evalGroup.toJson();
//        evalGroup.writeToFile();
//
//        Log.d(TAG,"");
//
////            EvalGroup evalGroup2 = EvalGroup.fromJson(json);
//        EvalGroup evalGroup2 = EvalGroup.readFromFile();
//
//        EvalItem item = (EvalItem)evalGroup2.getEvalItem("1");
//
//        if (evalGroup2.containsEvalItem("1"))
//        Log.d(TAG,"found");
//
//        for (Object itemId : evalGroup2.getItemIdSet()) {
//        System.out.println(itemId + " : " + evalGroup2.getEvalItem((String) itemId).toString());
//        evalGroup2.removeEvalItem((String) itemId);
//        }
//        } catch (Exception e) {
//        e.printStackTrace();
//        }

public class EvalGroup {
    private String evlId = "";
    private String ownerId = "";
    private String dataVer = "5";
    private HashMap<String, EvalItem> info = new HashMap<String, EvalItem>();
    private String outFilename = "test_2020-01-01_0_data.txt";

    @JsonCreator
    public EvalGroup(@JsonProperty("evlId") String evlId, @JsonProperty("ownerId") String ownerId, @JsonProperty("info") HashMap<String, EvalItem> info) {
        this.evlId = evlId;
        this.ownerId = ownerId;
        this.info = info;
        this.outFilename = evlId + "_2020-01-01_0_data.txt";
    }

    public EvalGroup(String evlId, String ownerId) {
        this.evlId = evlId;
        this.ownerId = ownerId;
        this.outFilename = evlId + "_2020-01-01_0_data.txt";
    }

    public EvalGroup(String evlId) {
        this.evlId = evlId;
        this.outFilename = evlId + "_2020-01-01_0_data.txt";
    }

    public EvalGroup() {
        super();
    }

    public String getEvlId() {
        return evlId;
    }

    public void setEvlId(String evlId) {
        this.evlId = evlId;
        this.outFilename = evlId + "_2020-01-01_0_data.txt";
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public HashMap<String, EvalItem> getInfo() {
        return info;
    }

    @JsonIgnore
    public String getFileMainName() {
        return evlId + "_2020-01-01_";
    }

    @JsonIgnore
    public Object[] getItemIdSet() {
        try {
            if (info == null)
                info = new HashMap<String, EvalItem>();
            return info.keySet().toArray();
        } catch (Exception ex) {
            return null;
        }
    }

    @JsonIgnore
    public boolean putEvalItem(String itemId, EvalItem value) {
        try {
            if (info == null)
                info = new HashMap<String, EvalItem>();
            EvalItem item = (EvalItem) info.get(itemId);
            if (item != null)
                info.remove(item);
            value.setItemId(itemId);
            info.put(itemId, value);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @JsonIgnore
    public boolean containsEvalItem(String itemId) {
        try {
            if (info == null)
                info = new HashMap<String, EvalItem>();
            return info.containsKey(itemId);
        } catch (Exception ex) {
            return false;
        }
    }

    @JsonIgnore
    public EvalItem getEvalItem(String itemId) {
        try {
            if (info == null)
                info = new HashMap<String, EvalItem>();
            EvalItem item = (EvalItem) info.get(itemId);
            return item;
        } catch (Exception ex) {
            return null;
        }
    }

    @JsonIgnore
    public boolean removeEvalItem(String itemId) {
        try {
            EvalItem item = (EvalItem) info.get(itemId);
            if (item != null)
                info.remove(item.getItemId());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @JsonIgnore
    public boolean updateEvalItem(String itemId, String bodyPart) {
        try {
            EvalItem item = (EvalItem) info.get(itemId);
            if (item != null) {
                item.setBodyPart(bodyPart);
            } else {
                EvalItem evalItem = new EvalItem(itemId, bodyPart);
                info.put(itemId, evalItem);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @JsonIgnore
    public synchronized int getNextItemId() {
        int nextItemId = 1;
        int maxItemId = 0;
        try {
            for (Object itemId : info.keySet()) {
                EvalItem item = (EvalItem) info.get(itemId);
                int curItemId = Integer.parseInt(item.getItemId());
                if (maxItemId < curItemId)
                    maxItemId = curItemId;
            }
            nextItemId = maxItemId +1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextItemId;
    }

    @JsonIgnore
    public static EvalGroup fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, EvalGroup.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @JsonIgnore
    public String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    @JsonIgnore
    public void setStoreFilename(String value) {
        outFilename = value;
    }

    @JsonIgnore
    public synchronized void writeToFile() {
        BufferedWriter writer = null;
        try {
            String msg = toJson();
//            if (dataEncrypt)
//                msg = StringUtils.encryptByDES(msg);
            //建立檔名
            String outFilename = getFileMainName();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/WoundCamRtc/"+outFilename);

            writer = new BufferedWriter(new FileWriter(file, false));
            writer.write(msg);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 關閉BufferedWriter
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    @JsonIgnore
    public synchronized EvalGroup readFromFile() {
        FileInputStream fis = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            String outFilename = getFileMainName();
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/WoundCamRtc/"+outFilename);

            fis = new FileInputStream(file);
            br = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (br!=null) {
                    br.close();
                }
            } catch (Exception e) {
            }
            try {
                if (fis!=null) {
                    fis.close();
                }
            } catch (Exception e) {
            }
        }

        String result = sb.toString();
//        if (!result.startsWith("{") && !result.startsWith("evl")) {
//            try {
//                result = StringUtils.decryptByDES(result);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        return EvalGroup.fromJson(result);
    }
}
