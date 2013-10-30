package mcz.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;

public abstract class ViewBase extends android.view.View implements
		OnTouchListener {

	public static int backgroundColor = Color.BLACK;
	public static int ballColor = Color.WHITE;
	private boolean alreadyDrawed = false;
	//
	protected Ball ball = new Ball(ballColor);
	protected Hole holeBelowBall = null;
	protected ArrayList<Hole> holes = new ArrayList<Hole>();

	public ViewBase(Context context) {
		super(context);
		Log.d("mcz", "ViewBase");
		ball.setRadius(50);
		this.setOnTouchListener(this);
	}

	protected void firstDraw(Canvas canvas) {
		alreadyDrawed = true;
		ball.setX(getWidth() / 2);
		ball.setY(getHeight() / 2);
		onFirstDraw(canvas);
	}

	protected abstract void onFirstDraw(Canvas canvas);

	@Override
	protected void onDraw(Canvas canvas) {
		// Log.d("mcz", "onDraw " + ball.getX() + " " + ball.getY());
		super.onDraw(canvas);
		canvas.drawColor(backgroundColor);
		if (!alreadyDrawed) {
			firstDraw(canvas);
		}
		drawHoles(canvas);
		ball.draw(canvas);
	}

	private void drawHoles(Canvas canvas) {
		try {
			for (Ball hole : holes) {
				hole.draw(canvas);
			}
		} catch (Exception e) {
			sleep(10);
			draw(canvas);
		}
	}

	public static void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}

	public void moveBall(float x, float y) {
		// Log.d("mcz", String.format("X = %s Y = %s", x, y));
		ball.setX(x);
		ball.setY(y);
		postInvalidate();
		checkHole();
		onMoveBall();
	}

	public abstract void onMoveBall();

	protected abstract void onBallOverHole(Hole hole);

	public void checkHole() {
		try {
			for (Hole hole : holes) {
				if (hole.isOver(ball)) {
					holeBelowBall = hole;
					onBallOverHole(hole);
				}
			}
		} catch (Exception e) {
			sleep(10);
			checkHole();
		}
	}

	@Override
	public boolean onTouch(android.view.View view, MotionEvent motionEvent) {
		float x = motionEvent.getX();
		float y = motionEvent.getY();
		moveBall(x, y);
		return true;
	}

}
