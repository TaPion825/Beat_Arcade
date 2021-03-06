package com.example.administrator.state;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.example.administrator.game.RhythmeEffect;
import com.example.administrator.framework.AppManager;
import com.example.administrator.activities.GameActivity;
import com.example.administrator.framework.IState;
import com.example.administrator.framework.ObjectManager;
import com.example.administrator.framework.R;
import com.example.administrator.framework.SoundManager;
import com.example.administrator.game.HP_Black;
import com.example.administrator.game.HP_Red;
import com.example.administrator.game.HP_Yellow;
import com.example.administrator.framework.NoteMakeThread;
import com.example.administrator.game.Player;
import com.example.administrator.game.RhythmCombo;

/**
 * Created by Administrator on 2017-11-28.
 */

public class GameState implements IState {
    private GameActivity gameActivity;
    private NoteMakeThread noteMakeThread = new NoteMakeThread();;

    //비트맵 데이터
    public static Bitmap[] judge_Bitmap = new Bitmap[5];
    public static Bitmap[] note_Bitmap = new Bitmap[3];
    public static Bitmap[] effect_Bitmap = new Bitmap[3];
    public static Bitmap[] player_Bitmap = new Bitmap[2];
    public static Bitmap[] laser_Bitmap = new Bitmap[2];
    public static Bitmap slow_Bitmap;
    public static Bitmap combo_Bitmap;
    public static Bitmap comboNumber_Bitmap;
    public static Bitmap bullet_Bitmap;
    public static Bitmap damage_Bitmap;
    public static Bitmap trap_Bitmap;
    public static Bitmap blue_Bitmap;
    public static Bitmap onepiece_Bitmap;

    private int t2_x = 0, t2_y = 0;     //D-Pad좌표
    private double angle;               //각도
    private double dx, dy;              //D-Pad의 중심과 사용자가 누른곳 사이의 거리

    private long time_GameStart;
    private boolean check_GameStart = false;


    //플레이어번호
    private static int player_Num = -1;

    public static int getPlayer_Num() {
        return player_Num;
    }


    //적플레이어 번호
    private static int enemy_Num = -1;

    public static int getEnemy_Num() {
        return enemy_Num;
    }


    //시작될때 여러 비트맵 데이터나 객체들을 메모리에 올림
    @Override
    public void Init(Context context) {
        //게임액티비티를 저장
        gameActivity = (GameActivity) context;

        //게임액티비티에 있는 값으로 플레이어 넘버를 정해줌
        player_Num = gameActivity.getMyPlayerNum();

        //나의 플레이어 넘버에 따라 적의 플레이어 넘버도 정해줌
        if (player_Num == 0) {
            enemy_Num = 1;
        } else {
            enemy_Num = 0;
        }

        //각각의 비트맵에 비트맵 데이터를 넣어줌
        judge_Bitmap[0] = AppManager.getInstance().getBitmap(R.drawable.miss);
        judge_Bitmap[1] = AppManager.getInstance().getBitmap(R.drawable.poor);
        judge_Bitmap[2] = AppManager.getInstance().getBitmap(R.drawable.bad);
        judge_Bitmap[3] = AppManager.getInstance().getBitmap(R.drawable.good);
        judge_Bitmap[4] = AppManager.getInstance().getBitmap(R.drawable.perfect);
        note_Bitmap[0] = AppManager.getInstance().getBitmap(R.drawable.note1);
        note_Bitmap[1] = AppManager.getInstance().getBitmap(R.drawable.note2);
        note_Bitmap[2] = AppManager.getInstance().getBitmap(R.drawable.note3);
        effect_Bitmap[0] = AppManager.getInstance().getBitmap(R.drawable.rhythme_effect1);
        effect_Bitmap[1] = AppManager.getInstance().getBitmap(R.drawable.rhythme_effect2);
        effect_Bitmap[2] = AppManager.getInstance().getBitmap(R.drawable.rhythme_effect3);
        player_Bitmap[0] = AppManager.getInstance().getBitmap(R.drawable.player1);
        player_Bitmap[1] = AppManager.getInstance().getBitmap(R.drawable.player2);
        laser_Bitmap[0] = AppManager.getInstance().getBitmap(R.drawable.laser_sprite);
        laser_Bitmap[1] = AppManager.getInstance().getBitmap(R.drawable.laser_sprite2);
        slow_Bitmap = AppManager.getInstance().getBitmap(R.drawable.slow_effect);
        combo_Bitmap = AppManager.getInstance().getBitmap(R.drawable.combo);
        comboNumber_Bitmap = AppManager.getInstance().getBitmap(R.drawable.number_sprite);
        bullet_Bitmap = AppManager.getInstance().getBitmap(R.drawable.bullet);
        damage_Bitmap = AppManager.getInstance().getBitmap(R.drawable.number_sprite2);

        //리듬콤보를 관리 출력할 리듬콤보객체를 생성
        ObjectManager.rhythmCombo = new RhythmCombo(combo_Bitmap);

        //플레이어 객체를 두개 생성함
        ObjectManager.player_Vector.add(new Player(player_Bitmap[0], 0));
        ObjectManager.player_Vector.add(new Player(player_Bitmap[1], 1));

        //체력바 (배경)을 생성함
        ObjectManager.hp_Black_Vector.add(new HP_Black(0));
        ObjectManager.hp_Black_Vector.add(new HP_Black(700));

        //체력바 (체력표시)를 생성함
        ObjectManager.hp_Red_Vector.add(new HP_Red(ObjectManager.hp_Black_Vector.get(0)));
        ObjectManager.hp_Red_Vector.add(new HP_Red(ObjectManager.hp_Black_Vector.get(1)));

        //체력바 (체력표시)를 생성함
        ObjectManager.hp_Yellow_Vector.add(new HP_Yellow(ObjectManager.hp_Black_Vector.get(0)));
        ObjectManager.hp_Yellow_Vector.add(new HP_Yellow(ObjectManager.hp_Black_Vector.get(1)));

        ObjectManager.rhythmeEffect[0] = new RhythmeEffect(effect_Bitmap[0], 0);
        ObjectManager.rhythmeEffect[1] = new RhythmeEffect(effect_Bitmap[1],1);
        ObjectManager.rhythmeEffect[2] = new RhythmeEffect(effect_Bitmap[2],2);

        time_GameStart = System.currentTimeMillis();
    }

    @Override
    public void Destroy() {

    }


    //TODO 매 프레임마다 할일들
    @Override
    public void Update() {
        //GameTime에 현재 시간을 넣어줌
        long GameTime = System.currentTimeMillis();

        if(!check_GameStart && GameTime - time_GameStart > 2000) {
            SoundManager.getInstance().MediaPlay();
            noteMakeThread.start();
            check_GameStart = true;
        }

        //note_Vector 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.note_Vector.size(); i++) {
            ObjectManager.note_Vector.get(i).Update(GameTime);
        }

        //리듬이펙트 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.rhythmeEffect.length; i++) {
            ObjectManager.rhythmeEffect[i].Update(GameTime);
        }

        //judge_Vector 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.judge_Vector.size(); i++) {
            ObjectManager.judge_Vector.get(i).Update(GameTime);
        }

        //모든 slow 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.slowEffect_Vector.size(); i++) {
            ObjectManager.slowEffect_Vector.get(i).Update(GameTime);
        }

        //플레이어와 레이저를 업데이트함
        for (int i = 0; i < ObjectManager.player_Vector.size(); i++) {
            ObjectManager.player_Vector.get(i).Update(GameTime);
            for (int j = 0; j < ObjectManager.player_Vector.get(i).laser_Vector.size(); j++) {
                ObjectManager.player_Vector.get(i).laser_Vector.get(j).Update(GameTime);
            }
        }

        //모든 Bullet 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.bullet_Vector.size(); i++) {
            ObjectManager.bullet_Vector.get(i).Update(GameTime);
        }

        //hp벡터 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.hp_Black_Vector.size(); i++) {
            ObjectManager.hp_Black_Vector.get(i).Update(GameTime);
        }

        //hp벡터 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.hp_Red_Vector.size(); i++) {
            ObjectManager.hp_Red_Vector.get(i).Update(GameTime);
        }

        //hp벡터 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.hp_Yellow_Vector.size(); i++) {
            ObjectManager.hp_Yellow_Vector.get(i).Update(GameTime);
        }

        //damage벡터 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.damageNumber_Vector.size(); i++) {
            ObjectManager.damageNumber_Vector.get(i).Update(GameTime);
        }
    }


    //TODO 객체를 렌더링
    @Override
    public void Render(Canvas canvas) {
        //슈팅게임의 배경을 그려줌
        ObjectManager.shootingBackground.Draw(canvas);

        //모든 slow 객체를 드로우
        for (int i = 0; i < ObjectManager.slowEffect_Vector.size(); i++) {
            ObjectManager.slowEffect_Vector.get(i).Draw(canvas);
        }

        //플레이어와 레이저를 그림
        for (int i = 0; i < ObjectManager.player_Vector.size(); i++) {
            ObjectManager.player_Vector.get(i).Draw(canvas);
            for (int j = 0; j < ObjectManager.player_Vector.get(i).laser_Vector.size(); j++) {
                ObjectManager.player_Vector.get(i).laser_Vector.get(j).Draw(canvas);
            }
        }

        //모든 Bullet 객체를 드로우
        for (int i = 0; i < ObjectManager.bullet_Vector.size(); i++) {
            ObjectManager.bullet_Vector.get(i).Draw(canvas);
        }

        //노트가 내려오는 부분의 배경을 그려줌
        ObjectManager.rhythmBackGourndTop.Draw(canvas);

        //nv벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.note_Vector.size(); i++) {
            ObjectManager.note_Vector.get(i).Draw(canvas);
        }

        //리듬이펙트 안에 있는 모든 객체의 Update를 호출
        for (int i = 0; i < ObjectManager.rhythmeEffect.length; i++) {
            ObjectManager.rhythmeEffect[i].Draw(canvas);
        }

        //jv벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.judge_Vector.size(); i++) {
            ObjectManager.judge_Vector.get(i).Draw(canvas);
        }

        //콤보가 2보다 크면 콤보와 관련된 오브젝트들을 렌더링함
        if (ObjectManager.rhythmCombo.getCombo() > 2) {
            ObjectManager.rhythmCombo.Draw(canvas);
            for (int i = 0; i < ObjectManager.comboNumber_Vector.size(); i++) {
                ObjectManager.comboNumber_Vector.get(i).Draw(canvas);
            }
        }
        //hp벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.hp_Black_Vector.size(); i++) {
            ObjectManager.hp_Black_Vector.get(i).Draw(canvas);
        }

        //hp벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.hp_Yellow_Vector.size(); i++) {
            ObjectManager.hp_Yellow_Vector.get(i).Draw(canvas);
        }

        //hp벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.hp_Red_Vector.size(); i++) {
            ObjectManager.hp_Red_Vector.get(i).Draw(canvas);
        }

        //damage벡터 안에 있는 모든 객체를 그려줌
        for (int i = 0; i < ObjectManager.damageNumber_Vector.size(); i++) {
            ObjectManager.damageNumber_Vector.get(i).Draw(canvas);
        }

        //버튼들을 그려줌
        ObjectManager.rhythmBackGroundBottom1.Draw(canvas);
        ObjectManager.rhythmBackGroundBottom2.Draw(canvas);
        ObjectManager.rhythmBackGroundBottom3.Draw(canvas);

        //D-Pad를 그려줌
        ObjectManager.dpad_Circle.Draw(canvas);
        ObjectManager.dpad_MiniCircle.Draw(canvas);
    }


    @Override
    public boolean onKeyDwon(int Keycode, KeyEvent event) {
        return false;
    }


    //TODO 터치입력에 대한 반응
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();                      //터치액션을 담음
        int t1_x = 0, t1_y = 0;                              //리듬게임 부분의 터치를 저장
        int note_i = 0;                                      //노트의 인덱스
        int note_line = 0;                                   //노트의 라인
        int line = (int) (500 * GameActivity.SIZE_X);        //리듬게임과 슈팅게임의 경계선

        //터치가능 범위를 지정해줄 렉트
        Rect rt = new Rect();
        Rect rt1 = new Rect();
        Rect rt2 = new Rect();
        Rect rt3 = new Rect();


        //렉트에 D-Pad위치를 넣어줌
        rt.set(ObjectManager.dpad_Circle.getX_R(), ObjectManager.dpad_Circle.getY_R(), ObjectManager.dpad_Circle.getX_R() + ObjectManager.dpad_Circle.getSpriteWidth(), ObjectManager.dpad_Circle.getY_R() + ObjectManager.dpad_Circle.getSpriteHeight());
        //렉트에 1번 버튼을 넣어줌
        rt1.set(ObjectManager.rhythmBackGroundBottom1.getX_R(), ObjectManager.rhythmBackGroundBottom1.getY_R(), ObjectManager.rhythmBackGroundBottom1.getX_R() + ObjectManager.rhythmBackGroundBottom1.getSpriteWidth(), ObjectManager.rhythmBackGroundBottom1.getY_R() + ObjectManager.rhythmBackGroundBottom1.getSpriteHeight());
        //렉트에 2번 버튼을 넣어줌
        rt2.set(ObjectManager.rhythmBackGroundBottom2.getX_R(), ObjectManager.rhythmBackGroundBottom2.getY_R(), ObjectManager.rhythmBackGroundBottom2.getX_R() + ObjectManager.rhythmBackGroundBottom2.getSpriteWidth(), ObjectManager.rhythmBackGroundBottom2.getY_R() + ObjectManager.rhythmBackGroundBottom2.getSpriteHeight());
        //렉트에 3번 버튼을 넣어줌
        rt3.set(ObjectManager.rhythmBackGroundBottom3.getX_R(), ObjectManager.rhythmBackGroundBottom3.getY_R(), ObjectManager.rhythmBackGroundBottom3.getX_R() + ObjectManager.rhythmBackGroundBottom3.getSpriteWidth(), ObjectManager.rhythmBackGroundBottom3.getY_R() + ObjectManager.rhythmBackGroundBottom3.getSpriteHeight());


        //터치받은 액션으로 부터 좌표를 받아옴
        if (event.getPointerCount() > 1) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                if (event.getX(i) < line) {
                    t1_x = (int) event.getX(i);
                    t1_y = (int) event.getY(i);
                } else {
                    t2_x = (int) event.getX(i);
                    t2_y = (int) event.getY(i);
                }
            }
        } else {
            if (event.getX() < line) {
                t1_x = (int) event.getX();
                t1_y = (int) event.getY();
            } else {
                t2_x = (int) event.getX();
                t2_y = (int) event.getY();
            }
        }

        //리듬게임쪽을 터치했다면 리듬노트 검사
        if (t1_x != 0 && t1_y != 0 && ObjectManager.note_Vector.size() > 0) {
            //1번버튼이 눌려졌다면 nv벡터에서 노트넘버가 0인것을 찾아 그 인덱스를 note_i에 저장
            if (rt1.contains(t1_x, t1_y)) {
                note_line = 0;
            }
            //2번버튼이 눌려졌다면 nv벡터에서 노트넘버가 0인것을 찾아 그 인덱스를 note_i에 저장
            else if (rt2.contains(t1_x, t1_y)) {
                note_line = 1;
            }
            //3번버튼이 눌려졌다면 nv벡터에서 노트넘버가 0인것을 찾아 그 인덱스를 note_i에 저장
            else if (rt3.contains(t1_x, t1_y)) {
                note_line = 2;
            }
            //이후 해당 라인에 맞는 번호에 있는 노트중 가장 먼저 있는 노트를 찾음
            for (int i = 0; i < ObjectManager.note_Vector.size(); i++) {
                if (ObjectManager.note_Vector.get(i).getNoteNum() == note_line) {
                    note_i = i;
                    break;
                }
            }
            //그 노트를 판정함
            ObjectManager.rhythmeEffect[note_line].setNote(System.currentTimeMillis());
            ObjectManager.note_Vector.get(note_i).noteJudge(note_line);
        }


        //D-Pad에 터치좌표가 있을경우
        if (rt.contains(t2_x, t2_y)) {
            //D-Pad의 버튼을 누른곳으로 움직여줌
            ObjectManager.dpad_MiniCircle.setPosition((int) ((t2_x - 75) / GameActivity.SIZE_X), (int) ((t2_y - 75) / GameActivity.SIZE_Y));
            //각도계산기를 호출
            calcAngle();
            //상태를 무브로 바꿈
            ObjectManager.player_Vector.get(player_Num).setMove_Check(true);
        }

        //손을 땔때 dpad의 값들을 초기화 시켜줌
        if (action == MotionEvent.ACTION_UP) {
            t2_x = 0;
            t2_y = 0;
            ObjectManager.dpad_MiniCircle.setPosition(ObjectManager.dpad_MiniCircle.x_ori, ObjectManager.dpad_MiniCircle.y_ori);
            ObjectManager.player_Vector.get(player_Num).setMove_Check(false);
        }

        //몹에 무빙을 위한 판정요소들을 넣어줌
        ObjectManager.player_Vector.get(player_Num).setDis(dx, dy);
        ObjectManager.player_Vector.get(player_Num).setTouch(t2_x, t2_y);
        ObjectManager.player_Vector.get(player_Num).setDpadCircle(ObjectManager.dpad_Circle.getX_R(), ObjectManager.dpad_Circle.getY_R());

        return true;
    }


    //D-Pad에서 사용자가 누르고 있는 곳과 D-Pad의 중심의 각을 구하는 메소드
    public void calcAngle() {
        dx = t2_x - (ObjectManager.dpad_Circle.getX_R() + 200 * GameActivity.SIZE_X);
        dy = t2_y - (ObjectManager.dpad_Circle.getY_R() + 200 * GameActivity.SIZE_Y);

        angle = 90 + Math.toDegrees(Math.atan2(dy, dx));
        ObjectManager.player_Vector.get(player_Num).setAngle(angle);
    }
}