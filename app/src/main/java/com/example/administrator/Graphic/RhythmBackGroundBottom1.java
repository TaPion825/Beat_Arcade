package com.example.administrator.Graphic;

import com.example.administrator.framework.AppManager;
import com.example.administrator.framework.Graphic;
import com.example.administrator.framework.R;

/**
 * Created by Administrator on 2017-11-29.
 */

//1번 버튼을 그려주는 객체
public class RhythmBackGroundBottom1 extends Graphic {
    public RhythmBackGroundBottom1 () {
        super((AppManager.getInstance().getBitmap(R.drawable.rhythmbutton1)));
        this.InitSpriteData(250, 170, 1);
        setPosition(0, 830);
    }
}