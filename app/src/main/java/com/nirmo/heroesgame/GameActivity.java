package com.nirmo.heroesgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.nirmo.heroesgame.utils.GifView;
import com.nirmo.heroesgame.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private static final int NUMBER_OF_CCEllS = 18;
    private static final int NUMBER_OF_BUTTERFLIES = 18;
    private static final int BUTTERFLIES_OPTIONS[] = {
            R.raw.butterfly1,
            R.raw.butterfly2,
            R.raw.butterfly3,
            R.raw.butterfly4
    };

    private View ccells[] = new View[NUMBER_OF_CCEllS];
    private Map<View, Boolean> isCcellAlive = new HashMap();
    private GifView butterflies[] = new GifView[NUMBER_OF_BUTTERFLIES];
    private GameViewState gameViewState = GameViewState.IDLE;
    private float dX, dY;
    private Point initialButterflyPosition;
    private View draggedCcell;
    private View draggedButterfly;
    private Context context;

    private static final int BOMBA_OPTIONS[] = {
            R.raw.bomba1,
            R.raw.bomba2,
            R.raw.bomba3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);
        context = this;
        FrameLayout board = (FrameLayout) findViewById(R.id.game_board);

        ViewTreeObserver vto = board.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                board.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                onRenderingReady(board);
            }
        });
    }

    public void PlayBackgroundSound(View view) {
        Intent intent = new Intent(GameActivity.this, BackgroundSoundService.class);
        startService(intent);
    }
    private void onRenderingReady(FrameLayout board) {
        initGame(board, NUMBER_OF_CCEllS, NUMBER_OF_BUTTERFLIES);
    }

    private void initGame(FrameLayout board, int numberOfCcells, int numberOfButterflies) {
        ccells = new View[numberOfCcells];
        butterflies = new GifView[numberOfButterflies];

        // TODO: Choose different image on every time we start the game.
        ImageView background = findViewById(R.id.background);

        for (int i = 0; i < ccells.length; i++) {
            ccells[i] = generateCcell(background, board);
            isCcellAlive.put(ccells[i], true);
        }

        ImageView facebox = findViewById(R.id.facebox);

        for (int i = 0; i < butterflies.length; i++) {
            butterflies[i] = generateButterfly(facebox, board);
        }
    }

    private View generateCcell(View innerView, FrameLayout outerView) {
        int ccellWidth  = outerView.getMeasuredWidth();
        ImageView ccell = (ImageView) getLayoutInflater().inflate(R.layout.ccell_layout, null);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(ccellWidth / 5, ccellWidth / 5);
        ccell.setLayoutParams(layoutParams);
        ccell.setX(MathUtils.getRandomInRange(innerView.getLeft(), innerView.getRight()));
        ccell.setY(MathUtils.getRandomInRange(innerView.getTop(), innerView.getBottom()));
        outerView.addView(ccell);
        return ccell;
    }

    private GifView generateButterfly(View innerView, FrameLayout outerView) {
        GifView butterfly = (GifView) getLayoutInflater().inflate(R.layout.butterfly_layout, null);

        // Choose random butterfly image.
        butterfly.setGif(BUTTERFLIES_OPTIONS[MathUtils.getRandomInRange(0, BUTTERFLIES_OPTIONS.length)]);
        outerView.addView(butterfly);

        int butterflyWidth  = outerView.getMeasuredWidth() / 4;
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(butterflyWidth, butterflyWidth);
        butterfly.setLayoutParams(layoutParams);
        int x,y;
        do {
            x = MathUtils.getRandomInRange(outerView.getLeft() + butterflyWidth, outerView.getRight() - butterflyWidth);
            y = MathUtils.getRandomInRange(outerView.getTop() + butterflyWidth, outerView.getBottom() - butterflyWidth);
            butterfly.setTranslationX(x);
            butterfly.setTranslationY(y);
        } while (hasCollisions(butterfly, innerView));
        return butterfly;
    }

    private boolean hasCollisions(View butterfly, View facebox) {
        Rect butterflyRect = getViewBoundingBox(butterfly);
        for (View ccell: ccells) {
            if (ccell == null) {
                continue;
            }

            if (butterflyRect.intersect(getViewBoundingBox(ccell))) {
                return true;
            }
        }

        for (View b : butterflies) {
            if (b == null) {
                continue;
            }

            if (butterflyRect.intersect(getViewBoundingBox(b))) {
                return true;
            }
        }
        if (butterflyRect.intersect(getViewBoundingBox(facebox))) {
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // check if touching butterfly
                draggedButterfly = doesUserTouchingButterfly(event, butterflies);
                if (draggedButterfly != null && gameViewState == GameViewState.IDLE) {
                    gameViewState = GameViewState.DRAGGING_BUTTERFLY;
                    dX = draggedButterfly.getX() - event.getRawX();
                    dY = draggedButterfly.getY() - event.getRawY();
                    initialButterflyPosition = new Point(
                            (int) draggedButterfly.getX(),
                            (int) draggedButterfly.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (gameViewState == GameViewState.DRAGGING_BUTTERFLY) {
                    // Now we are dragging a butterfly...
                    draggedButterfly.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                }
                break;
            case MotionEvent.ACTION_UP:
                // Drop the butterfly.
                if (gameViewState == GameViewState.DRAGGING_BUTTERFLY) {
                    // check we butterfly finds a ccell
                    draggedCcell = doesButterflyTouchingAccell(draggedButterfly, ccells);
                    if (draggedCcell != null) {
                        gameViewState = GameViewState.DRAGGING_BALL;
                    }

                    // Bring the butterfly back to its initial place.
                    Path path = new Path();
                    path.moveTo(draggedButterfly.getX(), draggedButterfly.getY());
                    path.lineTo(initialButterflyPosition.x, initialButterflyPosition.y);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(draggedButterfly, "x", "y", path);
                    animator.setDuration(2000);
                    animator.start();
                    animator.addListener(new DefaultAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            if (gameViewState == GameViewState.DRAGGING_BALL) {
                                killCcell(draggedCcell);
                                draggedButterfly = null;
                                // check if game is over.
                                if (isGameOver()) {
                                    new CountDownTimer(15000, 1000) {

                                        public void onTick(long millisUntilFinished) {
                                            //mTextField.setText("Clean");
                                        }

                                        public void onFinish() {
                                            setContentView(R.layout.game_end_board);
                                            VideoView videoView = (VideoView) findViewById(R.id.VideoView);  //casting to VideoView is not Strictly required above API level 26
                                            videoView.setVideoPath("android.resource://" + getPackageName() + "/" + R.raw.gamefinish); //set the path of the video that we need to use in our VideoView
                                            videoView.start();  //start() method of the VideoView class will start the video to play
                                            MediaController mediaController = new MediaController(context);
                                            //link mediaController to videoView
                                            mediaController.setAnchorView(videoView);
                                            //allow mediaController to control our videoView
                                            videoView.setMediaController(mediaController);
                                            videoView.start();

                                        }
                                    }.start();

                                }


                            }

                            gameViewState = GameViewState.IDLE;
                        }
                    });

                    if (gameViewState == GameViewState.DRAGGING_BALL) {
                        // Animate the ccell..  make him follow the butterfly.
                        ObjectAnimator animator3 = ObjectAnimator.ofFloat(draggedCcell, "x", "y", path);
                        animator3.setDuration(2000);
                        animator3.start();
                    }
                }
                break;
        }
        return true;
    }

    private View doesUserTouchingButterfly(MotionEvent event, View butterflies[]) {
        for (View butterfly : butterflies) {
            if (getViewBoundingBox(butterfly).contains((int) event.getX(), (int) event.getY())) {
                return butterfly;
            }
        }
        return null;
    }

    private View doesButterflyTouchingAccell(View butterfly, View ccells[]) {
        Rect butterflyRect = getViewBoundingBox(butterfly);
        for (View ccell: ccells) {
            if (butterflyRect.intersect(getViewBoundingBox(ccell))) {
                return ccell;
            }
        }
        return null;
    }

    private Rect getViewBoundingBox(View v) {
        int[] l = new int[2];
        v.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = v.getWidth();
        int h = v.getHeight();
        return new Rect(x, y, x + w, y + h);
    }

    private void killCcell(View ccell) {
        if (ccell == null) {
            return;
        }

        ccell.setVisibility(View.INVISIBLE);
        isCcellAlive.put(ccell, false);
        playRandomSound(context, BOMBA_OPTIONS);
    }



    public static void playRandomSound(Context context, int[] sounds) {
        int soundID = sounds[new Random().nextInt(sounds.length)];
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundID);
        mediaPlayer.start();
//        playRandomSound(context, soundID);
    }

//    private static void playRandomSound(Context context, int soundID) {
//        // Only play if the user has sounds enabled.
//        if (Setting.getSafeBoolean(SyncStateContract.Constants.BOMBA_OPTIONS)) {
//            try {
//                MediaPlayer mediaPlayer = MediaPlayer.create(context, soundID);
//                mediaPlayer.start();
//            } catch (Exception e) {
//                Log.d("Blacksmith", e.toString());
//            }
//        }
//    }
    private boolean isGameOver() {
        for (View ccell : ccells) {
            if (isCcellAlive.get(ccell)) {
                return false;
            }
        }
        return true;
    }

    enum GameViewState {
        IDLE,
        DRAGGING_BUTTERFLY,
        DRAGGING_BALL
    }
}
