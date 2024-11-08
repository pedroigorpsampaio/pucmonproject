package com.mygdx.game;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import br.pucrio.inf.lac.mhub.services.S2PAService;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		Main main = Main.getInstance(); // gets main reference
		// sets sensor interface implementation
		AndroidSensor aSensor = AndroidSensor.getInstance();
		aSensor.setApplication(this);
		main.setSensorInterface(aSensor);
		initialize(main, config);
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext( base );
		MultiDex.install( this );
	}

	@Override
	public void onDestroy() {
		stopService(new Intent(getBaseContext(), S2PAService.class));
		super.onDestroy();
	}
}
