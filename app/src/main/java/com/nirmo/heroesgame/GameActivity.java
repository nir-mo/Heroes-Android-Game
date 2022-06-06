package com.nirmo.heroesgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
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

public class GameActivity extends AppCompatActivity {
    private static final int NUMBER_OF_VIRUSES = 3;

    private View viruses[] = new View[NUMBER_OF_VIRUSES];
    private Map<View, Boolean> isVirusAlive = new HashMap();
    private GifView butterfly;
    private GameViewState gameViewState = GameViewState.IDLE;
    private float dX, dY;
    private Point initialButterflyPosition;
    private View draggedVirus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);

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

    private void onRenderingReady(FrameLayout board) {
        initGame(board, NUMBER_OF_VIRUSES);
    }

    private void initGame(FrameLayout board, int numberOfViruses) {
        viruses = new View[numberOfViruses];

        // TODO: Choose different image on every time we start the game.
        ImageView background = findViewById(R.id.background);

        for (int i = 0; i < viruses.length; i++) {
            viruses[i] = generateVirus(background, board);
            isVirusAlive.put(viruses[i], true);
        }

        // TODO: Make arbitrary number of butterflies.
        butterfly = generateButterfly(board);
    }

    private View generateVirus(View innerView, FrameLayout outerView) {
        int virusWidth  = outerView.getMeasuredWidth();
        ImageView virus = (ImageView) getLayoutInflater().inflate(R.layout.virus_layout, null);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(virusWidth / 5, virusWidth / 5);
        virus.setLayoutParams(layoutParams);
        virus.setTranslationX(MathUtils.getRandomInRange(innerView.getLeft(), innerView.getRight()));
        virus.setTranslationY(MathUtils.getRandomInRange(innerView.getTop(), innerView.getBottom()));
        outerView.addView(virus);
        return virus;
    }

    private GifView generateButterfly(FrameLayout outerView) {
        GifView butterfly = (GifView) getLayoutInflater().inflate(R.layout.butterfly_layout, null);
        // TODO: Choose randomly an image of butterfly from a collection of images...
        butterfly.setGif(R.raw.animated_gif);

        // TODO: Set butterfly random position.
        outerView.addView(butterfly);
        return butterfly;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (butterfly == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // check if touching butterfly
                if (doesUserTouchingButterfly(event, butterfly) && gameViewState == GameViewState.IDLE) {
                    gameViewState = GameViewState.DRAGGING_BUTTERFLY;
                    dX = butterfly.getX() - event.getRawX();
                    dY = butterfly.getY() - event.getRawY();
                    initialButterflyPosition = new Point(
                            (int) butterfly.getX(),
                            (int) butterfly.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (gameViewState == GameViewState.DRAGGING_BUTTERFLY) {
                    // Now we are dragging a butterfly...
                    butterfly.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                }
                break;
            case MotionEvent.ACTION_UP:
                // Drop the butterfly.
                if (gameViewState == GameViewState.DRAGGING_BUTTERFLY) {
                    // check we butterfly finds a virus
                    draggedVirus = doesButterflyTouchingAVirus(butterfly, viruses);
                    if (draggedVirus != null) {
                        gameViewState = GameViewState.DRAGGING_BALL;
                    }

                    // Bring the butterfly back to its initial place.
                    Path path = new Path();
                    path.moveTo(butterfly.getX(), butterfly.getY());
                    path.lineTo(initialButterflyPosition.x, initialButterflyPosition.y);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(butterfly, "x", "y", path);
                    animator.setDuration(2000);
                    animator.start();
                    animator.addListener(new DefaultAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            killVirus(draggedVirus);

                            // play some random sound.
                            playRandomSound();

                            // check if game is over.
                            isGameOver();
                        }
                    });

                    if (gameViewState == GameViewState.DRAGGING_BALL) {
                        // Animate the virus..  make him follow the butterfly.
                        ObjectAnimator animator3 = ObjectAnimator.ofFloat(draggedVirus, "x", "y", path);
                        animator3.setDuration(2000);
                        animator3.start();
                    }

                    gameViewState = GameViewState.IDLE;
                }
                break;
        }
        return true;
    }

    private boolean doesUserTouchingButterfly(MotionEvent event, View butterfly) {
        return getViewBoundingBox(butterfly).contains((int) event.getX(), (int) event.getY());
    }

    private View doesButterflyTouchingAVirus(View butterfly, View viruses[]) {
        Rect butterflyRect = getViewBoundingBox(butterfly);
        for (View virus: viruses) {
            if (butterflyRect.intersect(getViewBoundingBox(virus))) {
                return virus;
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

    private void killVirus(View virus) {
        virus.setVisibility(View.INVISIBLE);
        isVirusAlive.put(virus, false);
    }

    private void playRandomSound() {
        // TODO: play music.
    }

    private boolean isGameOver() {
        for (View virus : viruses) {
            if (isVirusAlive.get(virus)) {
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
