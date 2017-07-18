package com.caijia.selectpicture.utils;

/**
 * Created by cai.jia on 2017/6/28 0028
 */

public interface MediaType {

    int VIDEO = 1 << 1;
    int IMAGE = 1 << 2;
    int CAMERA = 1 << 3;
    int IMAGE_VIDEO = VIDEO | IMAGE;

}
