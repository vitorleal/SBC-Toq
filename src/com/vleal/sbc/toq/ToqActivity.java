package com.vleal.sbc.toq;

import java.io.InputStream;

import org.json.JSONException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.toq.smartwatch.api.v1.deckofcards.Constants;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManager;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.DeckOfCardsManagerListener;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCards;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteDeckOfCardsException;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.remote.RemoteResourceStore;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.resource.DeckOfCardsLauncherIcon;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.util.ParcelableUtil;
import com.vleal.sbc.api.Assets;
import com.vleal.sbctoq.R;


public class ToqActivity extends Activity {
	private final static String SBC_PREFS_FILE            = "sbc_prefs_file";
    private final static String DECK_OF_CARDS_KEY         = "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY = "deck_of_cards_version_key";
    
    private static DeckOfCardsManager deckOfCardsManager;
        
    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    
    private ToqAppletInstallBroadcastReciever toqAppStateReceiver;
    private static RemoteResourceStore resourceStore;   
    private static RemoteDeckOfCards deckOfCards;

    private Button installDeckOfCardsButton;
    private Button updateDeckOfCardsButton;
    private Button uninstallDeckOfCardsButton;
    
    private Button sendNotificationButton;
    private TextView statusTextView;
    
    
	//On Create the activity
	public void onCreate(Bundle icicle) { 
        super.onCreate(icicle);

        setContentView(R.layout.activity_toq);
        
        deckOfCardsManager         = DeckOfCardsManager.getInstance(getApplicationContext());
        deckOfCardsManagerListener = new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener   = new DeckOfCardsEventListenerImpl();
        toqAppStateReceiver        = new ToqAppletInstallBroadcastReciever();
        
        initDeckOfCards();
        initUI();
        
        startService(new Intent(this, ToqUpdateService.class));
    }
    

    protected void onStart() {
        super.onStart();    
        
        // Add the listeners
        deckOfCardsManager.addDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);
        
        registerToqAppStateReceiver();

        // If not connected, try to connect
        if (!deckOfCardsManager.isConnected()) {
            setStatus(getString(R.string.status_connecting));  

            try {
                deckOfCardsManager.connect();
                
            } catch (RemoteDeckOfCardsException e) {
                Toast.makeText(this, getString(R.string.error_connecting_to_service), Toast.LENGTH_SHORT).show();

            }
            
        } else {
            setStatus(getString(R.string.status_connected));
            refreshUI();
        }

    }    
    

    public void onStop() {  
        super.onStop();

        unregisterStateReceiver();
        
        // Remove listeners
        deckOfCardsManager.removeDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.removeDeckOfCardsEventListener(deckOfCardsEventListener);
    }
    
    
    public void onDestroy() {  
        super.onDestroy();

        deckOfCardsManager.disconnect();
    }
    
    
    /*
     * Private classes
     */
    // Handle service connection lifecycle and installation events
    private class DeckOfCardsManagerListenerImpl implements DeckOfCardsManagerListener {

        public void onConnected() {            
            runOnUiThread(new Runnable() {
                public void run() {                   
                    setStatus(getString(R.string.status_connected));
                    refreshUI();
                }
            });
        }

        public void onDisconnected() {
            runOnUiThread(new Runnable() {
                public void run() {                    
                    setStatus(getString(R.string.status_disconnected));
                    disableUI();
                }
            });
        }

        public void onInstallationSuccessful() {            
            runOnUiThread(new Runnable() {
                public void run() {                   
                    setStatus(getString(R.string.status_installation_successful));
                    updateUIInstalled();
                }
            });           
        }

        public void onInstallationDenied() {
            runOnUiThread(new Runnable() {
                public void run() {                    
                    setStatus(getString(R.string.status_installation_denied));
                    updateUINotInstalled();
                }
            });
        }

        public void onUninstalled() {
            runOnUiThread(new Runnable() {
                public void run() { 
                    setStatus(getString(R.string.status_uninstalled));                    
                    updateUINotInstalled();
                }
            });
        }
        
    }
    
    
    // Handle card events triggered by the user interacting with a card in the installed deck of cards
    private class DeckOfCardsEventListenerImpl implements DeckOfCardsEventListener {
        public void onCardOpen(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                	//ListCard listCard   = deckOfCards.getListCard();
                	//SimpleTextCard card = (SimpleTextCard) listCard.get(cardId);
                	
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_open) + cardId, Toast.LENGTH_SHORT).show();               
                }
            });
        }

        public void onCardVisible(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_visible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onCardInvisible(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_invisible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onCardClosed(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_closed) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onMenuOptionSelected(final String cardId, final String menuOption) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption +"]", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
        
    // Toq app state receiver
    private class ToqAppletInstallBroadcastReciever extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            
            Log.e("action", action);

            if (action == null) {
                Log.w(Constants.TAG, "ToqApiDemo.ToqAppStateBroadcastReceiver.onReceive - action is null, returning");
                return;
            }
            
            // If watch is now connected, refresh UI
            if (action.equals(Constants.TOQ_WATCH_CONNECTED_INTENT)) { 
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_connected), Toast.LENGTH_SHORT).show();
                refreshUI(); 
                
            } else if (action.equals(Constants.TOQ_WATCH_DISCONNECTED_INTENT)) { 
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
                disableUI();
            }    
        }
    }
    
    
    /*
     * Private API
     */
    private void refreshUI() {
        try { 
            if (deckOfCardsManager.isToqWatchConnected()) {
                if (deckOfCardsManager.isInstalled()) {
                    updateUIInstalled();
                    
                } else {
                    updateUINotInstalled();
                }
                
            } else {
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
                disableUI();
            }

        } catch (RemoteDeckOfCardsException e) {
            Toast.makeText(this, getString(R.string.error_checking_status), Toast.LENGTH_SHORT).show();
        }
        
    }
    
    
    private void disableUI() {
    	installDeckOfCardsButton.setEnabled(false);
        updateDeckOfCardsButton.setEnabled(false);
        uninstallDeckOfCardsButton.setEnabled(false);
        sendNotificationButton.setEnabled(false);
        
        setStatus(getString(R.string.status_disconnected));
    }
    
    
    private void updateUIInstalled() {
        installDeckOfCardsButton.setEnabled(false);
        updateDeckOfCardsButton.setEnabled(true);
        uninstallDeckOfCardsButton.setEnabled(true); 
        sendNotificationButton.setEnabled(true);

        updateDeckOfCardsButton.requestFocus();
        
        setStatus(getString(R.string.status_connected));
    }
    
    
    private void updateUINotInstalled() {        
        installDeckOfCardsButton.setEnabled(true);
        updateDeckOfCardsButton.setEnabled(false);
        uninstallDeckOfCardsButton.setEnabled(false);
        sendNotificationButton.setEnabled(false);

        installDeckOfCardsButton.requestFocus();
        
        setStatus(getString(R.string.status_disconnected));
    }
    
    
    // Register state receiver
    private void registerToqAppStateReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        
        intentFilter.addAction(Constants.BLUETOOTH_ENABLED_INTENT);
        intentFilter.addAction(Constants.BLUETOOTH_DISABLED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_PAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_UNPAIRED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_CONNECTED_INTENT);
        intentFilter.addAction(Constants.TOQ_WATCH_DISCONNECTED_INTENT);
        
        getApplicationContext().registerReceiver(toqAppStateReceiver, intentFilter);
    }
    
    
    private void unregisterStateReceiver() {
        getApplicationContext().unregisterReceiver(toqAppStateReceiver);
    }
    
    
    private void setStatus(String msg) {
        statusTextView.setText(msg);
    }
    
    
    // Init deck of cards
    private void initDeckOfCards() {    
        try {                   
          deckOfCards = createDeckOfCards();
          storeDeckOfCards(getApplicationContext());                
   
        } catch (Throwable th) {
            Log.w(Constants.TAG, "ToqApiDemo.initDeckOfCards - error occurred retrieving the stored deck of cards: " + th.getMessage());
            deckOfCards = null; // Reset to force recreate
        }
        
        // Make sure in usable state
        if (deckOfCards == null) {
            deckOfCards = createDeckOfCards();
        }
        
        // Get the icons
        resourceStore = new RemoteResourceStore();

        try {              
            DeckOfCardsLauncherIcon whiteIcon = new DeckOfCardsLauncherIcon("white", getIcon("white.png"), DeckOfCardsLauncherIcon.WHITE);
            DeckOfCardsLauncherIcon colorIcon = new DeckOfCardsLauncherIcon("color", getIcon("color.png"), DeckOfCardsLauncherIcon.COLOR);

            // Re-add the icons
            deckOfCards.setLauncherIcons(resourceStore, new DeckOfCardsLauncherIcon[]{whiteIcon, colorIcon});
            
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_initialising_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.initDeckOfCards - error occurred parsing the icons", e);
        }
    }
    
    // Store deck of cards
    private static void storeDeckOfCards(Context context) throws Exception {        
        SharedPreferences prefs = context.getSharedPreferences(SBC_PREFS_FILE, Context.MODE_PRIVATE);
        Editor editor           = prefs.edit();
        
        editor.putString(DECK_OF_CARDS_KEY, ParcelableUtil.marshall(deckOfCards));
        editor.putInt(DECK_OF_CARDS_VERSION_KEY, Constants.VERSION_CODE);
        editor.commit();       
    }
    
    // Create some cards with example content
    private RemoteDeckOfCards createDeckOfCards() {
        
        ListCard listCard = new ListCard();
        
        try {
        	Assets assets = new Assets(ToqActivity.this);
			assets.makeCardList(listCard, false);
			
		} catch (JSONException e) {}

        return new RemoteDeckOfCards(this, listCard);  
    }

    
    // Initialise the UI
    private void initUI() {
        // Buttons
        installDeckOfCardsButton = (Button) findViewById(R.id.doc_install_button);
        installDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                installDeckOfCards();
            }
        });
        
        updateDeckOfCardsButton = (Button) findViewById(R.id.doc_update_button);
        updateDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                updateDeckOfCards(getApplicationContext());
            }
        });
        
        uninstallDeckOfCardsButton = (Button) findViewById(R.id.doc_uninstall_button);
        uninstallDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                uninstallDeckOfCards();
            }
        });
        
        sendNotificationButton = (Button) findViewById(R.id.send_notification_button);
        sendNotificationButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                sendNotification();
            }
        });
        
        
        // Deck of cards
        ListCard listCard = deckOfCards.getListCard();
        
        try {
        	Assets assets = new Assets(ToqActivity.this);
			assets.makeCardList(listCard, false);
			
		} catch (JSONException e) {}

        // Status
        statusTextView = (TextView)findViewById(R.id.status_text);
        statusTextView.setText("Initialised");        
    }
    
    
    // Install deck of cards applet
    private void installDeckOfCards(){
        updateDeckOfCardsFromUI(getApplicationContext());
        
        try{                      
            deckOfCardsManager.installDeckOfCards(deckOfCards, resourceStore);
            
        } catch (RemoteDeckOfCardsException e) {
            Toast.makeText(this, getString(R.string.error_installing_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.installDeckOfCards - error installing deck of cards applet", e);
        }
        
        try{
            storeDeckOfCards(getApplicationContext());
            
        } catch (Exception e){
            Log.e(Constants.TAG, "ToqApiDemo.installDeckOfCards - error storing deck of cards applet", e);
        }

    }
    
    
    // Update deck of cards applet
    public static void updateDeckOfCards(Context context) {        
        updateDeckOfCardsFromUI(context);
        
        try {            
            deckOfCardsManager.updateDeckOfCards(deckOfCards, resourceStore);
            
        } catch (RemoteDeckOfCardsException e) {
            Log.e(Constants.TAG, "ToqApiDemo.updateDeckOfCards - error updating deck of cards applet", e);
        }
        
        try {
            storeDeckOfCards(context);
            
        } catch (Exception e) {
            Log.e(Constants.TAG, "ToqApiDemo.updateDeckOfCards - error storing deck of cards applet", e);
        }

    }
    
    
    // Uninstall deck of cards applet
    private void uninstallDeckOfCards() {
        try {                        
            deckOfCardsManager.uninstallDeckOfCards();
            
        } catch (RemoteDeckOfCardsException e) {
            Toast.makeText(this, getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.uninstallDeckOfCards - error uninstalling deck of cards applet applet", e);
        }
    }
    
    
    // Send notification button
    private void sendNotification() {
        Log.d(Constants.TAG, "ToqApiDemo.sendNotification");
      
        // Create notification text card from UI values
        /*NotificationTextCard notificationCard= new NotificationTextCard(System.currentTimeMillis(), 
                ((EditText)findViewById(R.id.notification_title_text)).getText().toString(), 
                splitString(((EditText)findViewById(R.id.notification_message_text)).getText().toString())); 
        
        notificationCard.setInfoText(((EditText)findViewById(R.id.notification_info_text)).getText().toString());
        notificationCard.setReceivingEvents(((CheckBox)findViewById(R.id.notification_events_checkbox)).isChecked());
        notificationCard.setMenuOptions(splitString(((EditText)findViewById(R.id.notification_menu_options_text)).getText().toString()));
        notificationCard.setShowDivider(((CheckBox)findViewById(R.id.notification_divider_checkbox)).isChecked());
        notificationCard.setVibeAlert(((CheckBox)findViewById(R.id.notification_vibe_checkbox)).isChecked());

        RemoteToqNotification notification = new RemoteToqNotification(this, notificationCard);

        try {            
            deckOfCardsManager.sendNotification(notification);
            
        } catch (RemoteDeckOfCardsException e) {
            Toast.makeText(this, getString(R.string.error_sending_notification), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.sendNotification - error sending notification", e);
        }      
		*/
    }
  
    // Read an icon from assets and return as a bitmap
    private Bitmap getIcon(String fileName) throws Exception {

        try {
            InputStream is = getAssets().open(fileName);
            return BitmapFactory.decodeStream(is);
            
        } catch (Exception e) {
            throw new Exception("An error occurred getting the icon: " + fileName, e);
        }

    }
    
    
    // Parse the UI to update the deck of cards contents
    public static void updateDeckOfCardsFromUI(Context context) {
        ListCard listCard = deckOfCards.getListCard();
        
        try {
        	Assets assets = new Assets(context);
			assets.updateCardList(listCard, true);
			
		} catch (JSONException e) {}
    }
}
