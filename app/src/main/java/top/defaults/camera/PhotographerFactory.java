package top.defaults.camera;

import android.app.Activity;
import android.view.TextureView;

public class PhotographerFactory {

    public static Camera2Photographer createPhotographerWithCamera2(Activity activity, TextureView preview, int facing) {
        Camera2Photographer photographer = new Camera2Photographer();
        photographer.initWithViewfinder(activity, preview);
        photographer.facing = facing;
//        preview.assign(photographer);
        return photographer;
    }
}
