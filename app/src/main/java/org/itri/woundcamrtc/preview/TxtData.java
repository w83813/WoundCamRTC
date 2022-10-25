package org.itri.woundcamrtc.preview;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TxtData {

    private String evlId; // APP產生的UID: yyyy-MM-dd HH-mm-ss-SSS
    private String ownerId; // 識別碼: 身份證/病歷號
    private Map<String, JSONObject> info;

    public String getEvlId() {
        return evlId;
    }

    public void setEvlId(String evlId) {
        this.evlId = evlId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // K: ItemId, V: info(JSON)
    public Map<String, JSONObject> getInfo() {
        return info;
    }

    // 輸出成Txt檔案格式
    public String toTxtString() {
        StringBuilder sb = new StringBuilder();
        sb.append("evlId=").append(evlId).append("\n");
        sb.append("ownerId=");
        if (ownerId != null) {
            sb.append(ownerId);
        }
        sb.append("\n");
        sb.append("info\n");
        for (String itemId : info.keySet()) {
            JSONObject value = info.get(itemId);
            sb.append(value.toString()).append("\n");
        }
        return sb.toString();
    }

    public static TxtData parse(InputStreamReader input) throws IOException, JSONException {
        TxtData output = new TxtData();
        BufferedReader bfrd = new BufferedReader(input);
        String line = null;
        String[] temp = null;
        //
        line = bfrd.readLine();
        temp = line.split("=");
        if (temp.length > 1) {
            output.setEvlId(temp[1]);
        }
        //
        line = bfrd.readLine();
        temp = line.split("=");
        if (temp.length > 1) {
            output.setOwnerId(temp[1]);
        }
        //
        line = bfrd.readLine(); // info
        //
        output.info = new TreeMap<String, JSONObject>();
        while (true) {
            line = bfrd.readLine();
            if (line == null || line.trim().isEmpty()) {
                break;
            }
            JSONObject _info = new JSONObject(line);
            String itemId = _info.getString("itemId");
            if (itemId != null) {
                output.info.put(itemId, _info);
            }
        }
        //
        input.close();
        //
        return output;
    }

    // K: bodyPart, V: list of itemId
    public Map<String, List<String>> groupByBodyPart() {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (String itemId : this.info.keySet()) {
            JSONObject _info = this.info.get(itemId);
            String bodyPart = "";
            try {
                bodyPart = _info.getString("bodyPart");
            } catch (JSONException je) {}
            List<String> itemIds = null;
            if (map.containsKey(bodyPart)) {
                itemIds = map.get(bodyPart);
            } else {
                itemIds = new ArrayList<String>();
            }
            itemIds.add(itemId);
        }
        //
        return map;
    }

}
