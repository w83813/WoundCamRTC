package org.itri.woundcamrtc.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("info")
public class EvalItem {
    private String itemId = "";
    private double heightPixel = 0.0;
    private double widthPixel = 0.0;
    private double depth = 0.0;
    private double distance = 0.0;
    private double blueArea = 0.0;
    private String bodyPart = "";

    private int calibrationColor = 0;
    private int bValue = 0;
    private int gValue = 0;
    private int rValue = 0;

    @JsonCreator
    public EvalItem(@JsonProperty("itemId") String itemId, @JsonProperty("heightPixel") double heightPixel, @JsonProperty("widthPixel") double widthPixel, @JsonProperty("depth") double depth, @JsonProperty("distance") double distance, @JsonProperty("blueArea") double blueArea, @JsonProperty("bodyPart") String bodyPart) {
        this.itemId = itemId;
        this.heightPixel = heightPixel;
        this.widthPixel = widthPixel;
        this.depth = depth;
        this.distance = distance;
        this.blueArea = blueArea;
        this.bodyPart = bodyPart;
    }

    public EvalItem(String itemId, String bodyPart) {
        this.itemId = itemId;
        this.bodyPart = bodyPart;
    }

    public EvalItem(String itemId) {
        this.itemId = itemId;
    }

    public EvalItem() {
        super();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getHeightPixel() {
        return heightPixel;
    }

    public void setHeightPixel(double heightPixel) {
        this.heightPixel = heightPixel;
    }

    public double getWidthPixel() {
        return widthPixel;
    }

    public void setWidthPixel(double widthPixel) {
        this.widthPixel = widthPixel;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getBlueArea() {
        return blueArea;
    }

    public void setBlueArea(double blueArea) {
        this.blueArea = blueArea;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public int getCalibrationColor() {
        return calibrationColor;
    }

    public void setCalibrationColor(int calibrationColor) {
        this.calibrationColor = calibrationColor;
    }

    public int getbValue() {
        return bValue;
    }

    public void setbValue(int bValue) {
        this.bValue = bValue;
    }

    public int getgValue() {
        return gValue;
    }

    public void setgValue(int gValue) {
        this.gValue = gValue;
    }

    public int getrValue() {
        return rValue;
    }

    public void setrValue(int rValue) {
        this.rValue = rValue;
    }
}
