package mcz.moveball;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mcz.view.ViewGame;
import mcz.view.ViewGame.ViewGameListener;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

public class GameActivity extends Activity implements ViewGameListener {

	ViewGame canvas;
	private final String BLUETOOTH_ADDRESS = "00:11:09:01:06:39";
	ArduinoReceiver receiver;
	IntentFilter intentFilter;
	TextView score;
	TextView time;

	private static final String patternStr = "(\\d*?),(\\d*?)";
	Pattern pattern;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		pattern = Pattern.compile(patternStr);

		score = (TextView) findViewById(R.id.txtScore);
		time = (TextView) findViewById(R.id.txtTime);
		FrameLayout frameLayout = (FrameLayout) findViewById(R.id.game);

		canvas = new ViewGame(this);
		canvas.setViewGameListener(this);
		frameLayout.addView(canvas);

		Amarino.connect(this, BLUETOOTH_ADDRESS);

		intentFilter = new IntentFilter();
		intentFilter.addAction(AmarinoIntent.ACTION_RECEIVED);
		intentFilter.addAction(AmarinoIntent.ACTION_CONNECTED);

		this.registerReceiver(receiver, intentFilter);
	}

	public void receive() {
		// canvas.moveBall(x, y);
	}

	private void changeScoreOnAndroid(final int scoreValue) {
		this.score.post(new Runnable() {

			@Override
			public void run() {
				score.setText("" + scoreValue);
			}
		});
	}

	@Override
	public void sendScore(int score) {
		Amarino.sendDataToArduino(this, BLUETOOTH_ADDRESS, 'S', score);
		Log.d("mcz.bruno", "sendScore " + score);
		changeScoreOnAndroid(score);
	}

	@Override
	public void sendNextLevel(int level) {
		Amarino.sendDataToArduino(this, BLUETOOTH_ADDRESS, 'G', "Level "
				+ level + "!");
	}

	@Override
	public void sendGameOver() {
		Log.d("mcz", "sendGameOver GAME OVER ");
		Amarino.sendDataToArduino(this, BLUETOOTH_ADDRESS, 'G', "Game Over");
		Intent it = new Intent(this, GameOverActivity.class);
		startActivity(it);
		finish();
	}

	@Override
	public void sendTime(int time) {
		time /= 1000;
		Log.d("mcz.bruno", "sendTime " + time);
		changeTimeOnAndroid(time);
		Amarino.sendDataToArduino(this, BLUETOOTH_ADDRESS, 'T', time);
	}

	private void changeTimeOnAndroid(final int timeValue) {
		this.time.post(new Runnable() {

			@Override
			public void run() {
				time.setText("" + timeValue);
			}
		});
	}
	private class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("mcz pos", "onReceive");

			if (intent.getAction() == AmarinoIntent.ACTION_RECEIVED) {
				
				int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
				if (dataType == AmarinoIntent.STRING_EXTRA){
					String pos = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
					Matcher matcher = pattern.matcher(pos);
					if (matcher.find()) {
						int x = Integer.parseInt(matcher.group(1));
						int y = Integer.parseInt(matcher.group(2));
						canvas.moveBall(x, y);
					}
					Log.d("mcz pos", "posicao array: " + pos);
				}
				
			}

			if (intent.getAction() == AmarinoIntent.ACTION_CONNECTED) {
				Log.d("mcz", "ACTION_CONNECTED ");
			}

		}

	}

}
