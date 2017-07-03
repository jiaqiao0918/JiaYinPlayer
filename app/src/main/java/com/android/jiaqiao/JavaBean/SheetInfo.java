package com.android.jiaqiao.JavaBean;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class SheetInfo {
    private String sheet_id;
    private String sheet_name;


    public SheetInfo(String sheet_id, String sheet_name) {
        this.sheet_id = sheet_id;
        this.sheet_name = sheet_name;
    }

    public String getSheet_name() {
        return sheet_name;
    }

    public String getSheet_id() {
        return sheet_id;
    }

    public void setSheet_name(String sheet_name) {
        this.sheet_name = sheet_name;
    }

    public void setSheet_id(String sheet_id) {
        this.sheet_id = sheet_id;
    }
}
