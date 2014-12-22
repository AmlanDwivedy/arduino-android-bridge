package com.amlan.hack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.amlan.hack.R;

public class IntroActivity extends Activity implements OnClickListener {
	private Button connect,color,sendButton;
	private EditText send,receive;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.intro);
		
		connect = (Button)findViewById(R.id.connect);
		color = (Button)findViewById(R.id.color);
		sendButton  = (Button)findViewById(R.id.sendbtn);
		send = (EditText)findViewById(R.id.sendet);
		receive = (EditText)findViewById(R.id.receive);
		
		connect.setOnClickListener(this);
		color.setOnClickListener(this);
		sendButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.connect){
			startActivity(new Intent(IntroActivity.this, BluetoothChat.class));
		}else if(v.getId() == R.id.color){
			startActivity(new Intent(IntroActivity.this, MainActivity.class));
		}else if(v.getId() == R.id.sendbtn){
			
		}
	}

}
