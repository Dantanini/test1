package tw.com.gear.marcorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootToActService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED))
        {
           //here we start the service            
           Intent serviceIntent = new Intent(context, OrderAlermService.class);
           context.startService(serviceIntent);
       }

	}

}