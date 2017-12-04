# SelectPictureWeChat
一个仿照微信UI的照片选择和裁剪器

# Android Studio 引入
1. 在Project的build.gradle文件里面加入
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url  "https://dl.bintray.com/caijialib/caijiaLibray"
        }
    }
}
```

2. 在Module的build.gradle文件里面加入
```
compile 'com.caijia:selectPicture:1.1.3'
```

## 选择图片视频
1.调起选择器，内部没有进行权限控制，需要在调用前自己检查权限，可以根据IntentBuilder来选择条件
```
Intent i = new SelectMediaActivity.IntentBuilder(this)
                        .mediaType(MediaType.IMAGE)
                        .hasCamera(true)
                        .build();
                startActivityForResult(i,2000);
```       

IntentBuilder代码
```
public static class IntentBuilder {

        Intent i;

        public IntentBuilder(Context context) {
            i = new Intent(context, SelectMediaActivity.class);
        }

        public IntentBuilder mediaType(@FilterMediaType int type) {
            i.putExtra(PARAMS_MEDIA_TYPE, type);
            return this;
        }

        public IntentBuilder canMultiSelect(boolean canMultiSelect) {
            i.putExtra(PARAMS_MULTI_SELECT, canMultiSelect);
            return this;
        }

        public IntentBuilder hasCamera(boolean hasCamera) {
            i.putExtra(PARAMS_HAS_CAMERA, hasCamera);
            return this;
        }

        public IntentBuilder maxSelectNum(int maxSelectNum) {
            i.putExtra(PARAMS_MAX_SELECT_NUM, maxSelectNum);
            return this;
        }

        public Intent build() {
            return i;
        }
    }
```

IntentBuilder参数 | 含义
--- | --- 
mediaType | MediaType.VIDEO（视频），MediaType.VIDEO（图片），MediaType.VIDEO（图片和视频）
canMultiSelect | 是否可以多选
hasCamera | 是否有照相这个Item
maxSelectNum | 多选的最大个数

2. 获取选择结果
```
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode){
        //图片
            case 2000: {
                Bundle args = data.getExtras();
                if (args != null) {
                    MediaBean mediaBean = args.getParcelable(SelectMediaActivity.RESULT_MEDIA);
                    //这里是选择的结果
                }
                break;
            }
        }
 }
```

## 裁剪
1.调起裁剪框,可以根据IntentBuilder条件选择
```
Intent i = new ClipPictureActivity.IntentBuilder(this)
                .aspectX(1)
                .aspectY(1)
                .inputImagePath(imagePath)
                .build();
        startActivityForResult(i, REQ_CLIP_IMAGE);
```

ClipPictureActivity.IntentBuilder 代码
```
public static class IntentBuilder {

        Intent i;

        public IntentBuilder(Context context) {
            i = new Intent(context, ClipPictureActivity.class);
        }

        public IntentBuilder aspectX(int aspectX) {
            i.putExtra(CLIP_ASPECT_X, aspectX);
            return this;
        }

        public IntentBuilder aspectY(int aspectY) {
            i.putExtra(CLIP_ASPECT_Y, aspectY);
            return this;
        }

        public IntentBuilder inputImagePath(String inputImagePath) {
            i.putExtra(CLIP_INPUT_IMAGE_PATH, inputImagePath);
            return this;
        }

        /**
         * 裁剪结果的文件保存路径,如果不设置,通过{@link #CLIP_OUTPUT_IMAGE_PATH}来取路径
         * 该值可能为null
         * @param outputImagePath
         * @return
         */
        public IntentBuilder outputImagePath(String outputImagePath) {
            i.putExtra(CLIP_OUTPUT_IMAGE_PATH, outputImagePath);
            return this;
        }

        /**
         * 如果裁剪结果需要采样,设置targetWidth,默认为屏幕宽度
         * 使用该值时,请设置{@link #clipResultNeedSample(boolean)} 为true,否则无效
         * @param targetWidth
         * @return
         */
        public IntentBuilder targetWidth(int targetWidth) {
            i.putExtra(CLIP_TARGET_WIDTH, targetWidth);
            return this;
        }

        /**
         * 裁剪结果是否需要采样
         *
         * @param need 默认为false
         * @return
         */
        public IntentBuilder clipResultNeedSample(boolean need) {
            i.putExtra(CLIP_RESULT_NEED_SAMPLE, need);
            return this;
        }

        public Intent build() {
            return i;
        }
    }
```

2. 获取结果，这个的结果是一个本地的路径，如果裁剪第二张，那么第一张的裁剪图片会清除，如果用第三方图片框架显示，建议不使用缓存。因为使用缓存，那么显示的可能是
前一张图片裁剪的结果。
```
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode){
        //裁剪
            case REQ_CLIP_IMAGE: {
                Bundle args = data.getExtras();
                if (args != null) {
                    String imagePath = args.getString(CLIP_OUTPUT_IMAGE_PATH);
                  
                }
                break;
            }
        }
 }
```
