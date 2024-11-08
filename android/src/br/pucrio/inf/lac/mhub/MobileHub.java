package br.pucrio.inf.lac.mhub;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;

import com.mygdx.game.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import br.pucrio.inf.lac.mhub.injection.component.ApplicationComponent;
//import br.pucrio.inf.lac.mhub.injection.component.DaggerApplicationComponent;
//import br.pucrio.inf.lac.mhub.injection.component.DaggerInjectionComponent;
import br.pucrio.inf.lac.mhub.injection.component.InjectionComponent;
import br.pucrio.inf.lac.mhub.injection.module.ApplicationModule;

@ReportsCrashes(formKey = "",
                formUri = "http://198.101.209.120/MAB-LAB/report/report.php",
                formUriBasicAuthLogin = "yodo",
                formUriBasicAuthPassword = "letryodo",
                httpMethod = org.acra.sender.HttpSender.Method.POST,
                reportType = org.acra.sender.HttpSender.Type.JSON,
                mode = ReportingInteractionMode.TOAST,
                resToastText = R.string.crash_toast_text
)
public class MobileHub extends Application {
    /** Component that build the dependencies */
    private static InjectionComponent mComponent;

	@Override
    public void onCreate() {
        super.onCreate();
        ACRA.init( this );
        MultiDex.install(this);

//        ApplicationComponent appComponent = DaggerApplicationComponent.builder()
//                .applicationModule( new ApplicationModule( this ) )
//                .build();
//
//        mComponent = DaggerInjectionComponent.builder()
//                .applicationComponent( appComponent )
//                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext( base );
        MultiDex.install( this );
    }

    public static InjectionComponent getComponent() {
        return mComponent;
    }


}
