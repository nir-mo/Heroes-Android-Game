package com.nirmo.heroesgame;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
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
    private static final int NUMBER_OF_VIRUSES = 4;
    private static final int NUMBER_OF_BUTTERFLIES = 4;
    private static final int BUTTERFLIES_OPTIONS[] = {
            R.raw.butterfly1,
            R.raw.butterfly3
    };

    private View viruses[] = new View[NUMBER_OF_VIRUSES];
    private Map<View, Boolean> isVirusAlive = new HashMap();
    private GifView butterflies[] = new GifView[NUMBER_OF_BUTTERFLIES];
    private GameViewState gameViewState = GameViewState.IDLE;
    private float dX, dY;
    private Point initialButterflyPosition;
    private View draggedVirus;
    private View draggedButterfly;


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
        initGame(board, NUMBER_OF_VIRUSES, NUMBER_OF_BUTTERFLIES);
    }

    private void initGame(FrameLayout board, int numberOfViruses, int numberOfButterflies) {
        viruses = new View[numberOfViruses];
        butterflies = new GifView[numberOfButterflies];

        // TODO: Choose different image on every time we start the game.
        ImageView background = findViewById(R.id.background);

        for (int i = 0; i < viruses.length; i++) {
            viruses[i] = generateVirus(background, board);
            isVirusAlive.put(viruses[i], true);
        }

        for (int i = 0; i < butterflies.length; i++) {
            butterflies[i] = generateButterfly(board);
        }
    }

    private View generateVirus(View innerView, FrameLayout outerView) {
        int virusWidth  = outerView.getMeasuredWidth();
        ImageView virus = (ImageView) getLayoutInflater().inflate(R.layout.virus_layout, null);
        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(virusWidth / 5, virusWidth / 5);
        virus.setLayoutParams(layoutParams);
        virus.setX(MathUtils.getRandomInRange(innerView.getLeft(), innerView.getRight()));
        virus.setY(MathUtils.getRandomInRange(innerView.getTop(), innerView.getBottom()));
        outerView.addView(virus);
        return virus;
    }

    private GifView generateButterfly(FrameLayout outerView) {
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
        } while (hasCollisions(butterfly));
        return butterfly;
    }

    private boolean hasCollisions(View butterfly) {
        Rect butterflyRect = getViewBoundingBox(butterfly);
        for (View virus: viruses) {
            if (virus == null) {
                continue;
            }

            if (butterflyRect.intersect(getViewBoundingBox(virus))) {
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
                    // check we butterfly finds a virus
                    draggedVirus = doesButterflyTouchingAVirus(draggedButterfly, viruses);
                    if (draggedVirus != null) {
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
                                killVirus(draggedVirus);

                                // check if game is over.
                                // TODO: Decide what to do when game is over.
                                isGameOver();

                                draggedButterfly = null;
                            }

                            gameViewState = GameViewState.IDLE;
                        }
                    });

                    if (gameViewState == GameViewState.DRAGGING_BALL) {
                        // Animate the virus..  make him follow the butterfly.
                        ObjectAnimator animator3 = ObjectAnimator.ofFloat(draggedVirus, "x", "y", path);
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
        if (virus == null) {
            return;
        }

        virus.setVisibility(View.INVISIBLE);
        isVirusAlive.put(virus, false);
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
