package top.defaults.camera;

import android.app.Activity;
import android.view.TextureView;

interface InternalPhotographer extends Photographer {

    void initWithViewfinder(Activity activity, TextureView preview);
}
