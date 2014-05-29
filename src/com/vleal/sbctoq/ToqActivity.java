package com.vleal.sbctoq;

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
import android.view.ViewGroup;
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
import com.vleal.sbcapi.Assets;

public class ToqActivity extends Activity {
	
	private final static String SBC_PREFS_FILE= "sbc_prefs_file";
    private final static String DECK_OF_CARDS_KEY= "deck_of_cards_key";
    private final static String DECK_OF_CARDS_VERSION_KEY= "deck_of_cards_version_key";
    
    private DeckOfCardsManager deckOfCardsManager;
        
    private DeckOfCardsManagerListener deckOfCardsManagerListener;
    private DeckOfCardsEventListener deckOfCardsEventListener;
    
    private ToqAppStateBroadcastReceiver toqAppStateReceiver;
    private RemoteResourceStore resourceStore;   
    private RemoteDeckOfCards deckOfCards;
        
    private ViewGroup deckOfCardsPanel;
    private Button installDeckOfCardsButton;
    private Button updateDeckOfCardsButton;
    private Button uninstallDeckOfCardsButton;
    
    private ViewGroup notificationPanel;
    private Button sendNotificationButton;
    private TextView statusTextView;

    
	//On Create the activity
	public void onCreate(Bundle icicle) {
        
        super.onCreate(icicle);
        
        Log.d(Constants.TAG, "ToqApiDemo.onCreate");

        setContentView(R.layout.activity_toq);
        
        // Get the reference to the deck of cards manager
        deckOfCardsManager= DeckOfCardsManager.getInstance(getApplicationContext());

        // Create listeners
        deckOfCardsManagerListener = new DeckOfCardsManagerListenerImpl();
        deckOfCardsEventListener   = new DeckOfCardsEventListenerImpl();
        // Create the state receiver
        toqAppStateReceiver        = new ToqAppStateBroadcastReceiver();
        
        initDeckOfCards();       
        initUI();
    }
    

    protected void onStart() {

        super.onStart();
        
		Log.d(Constants.TAG, "ToqApiDemo.onStart");        
        
        // Add the listeners
        deckOfCardsManager.addDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.addDeckOfCardsEventListener(deckOfCardsEventListener);
        
        registerToqAppStateReceiver();

        // If not connected, try to connect
        if (!deckOfCardsManager.isConnected()) {
            setStatus(getString(R.string.status_connecting));
            
            Log.d(Constants.TAG, "ToqApiDemo.onStart - not connected, connecting...");   

            try{
                deckOfCardsManager.connect();
                
            } catch (RemoteDeckOfCardsException e) {
                Toast.makeText(this, getString(R.string.error_connecting_to_service), Toast.LENGTH_SHORT).show();
                Log.e(Constants.TAG, "ToqApiDemo.onStart - error connecting to Toq app service", e);
            }
            
        } else {
            Log.d(Constants.TAG, "ToqApiDemo.onStart - already connected");
            setStatus(getString(R.string.status_connected));
            refreshUI();
        }

    }    
    

    public void onStop() {  
        super.onStop();

        Log.d(Constants.TAG, "ToqApiDemo.onStop");

        unregisterStateReceiver();
        
        // Remove listeners
        deckOfCardsManager.removeDeckOfCardsManagerListener(deckOfCardsManagerListener);
        deckOfCardsManager.removeDeckOfCardsEventListener(deckOfCardsEventListener);
    }
    
    
    public void onDestroy() {  
        super.onDestroy();
        
        Log.d(Constants.TAG, "ToqApiDemo.onDestroy");

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

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardInvisible(java.lang.String)
         */
        public void onCardInvisible(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_invisible) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onCardClosed(java.lang.String)
         */
        public void onCardClosed(final String cardId) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_card_closed) + cardId, Toast.LENGTH_SHORT).show();
                }
            });
        }

        /**
         * @see com.qualcomm.toq.smartwatch.api.v1.deckofcards.DeckOfCardsEventListener#onMenuOptionSelected(java.lang.String, java.lang.String)
         */
        public void onMenuOptionSelected(final String cardId, final String menuOption) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ToqActivity.this, getString(R.string.event_menu_option_selected) + cardId + " [" + menuOption +"]", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    
    
    // Toq app state receiver
    private class ToqAppStateBroadcastReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            String action= intent.getAction();

            if (action == null) {
                Log.w(Constants.TAG, "ToqApiDemo.ToqAppStateBroadcastReceiver.onReceive - action is null, returning");
                return;
            }
            
            Log.d(Constants.TAG, "ToqApiDemo.ToqAppStateBroadcastReceiver.onReceive - action: " + action);            
            
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
    // Connected to Toq app service, so refresh the UI
    private void refreshUI() {

        try { 
            // If Toq watch is connected
            if (deckOfCardsManager.isToqWatchConnected()) {

                // If the deck of cards applet is already installed
                if (deckOfCardsManager.isInstalled()) {
                    Log.d(Constants.TAG, "ToqApiDemo.refreshUI - already installed");
                    updateUIInstalled();
                    
                } else {
                    Log.d(Constants.TAG, "ToqApiDemo.refreshUI - not installed"); 
                    updateUINotInstalled();
                }
                
            } else {
                Log.d(Constants.TAG, "ToqApiDemo.refreshUI - Toq watch is disconnected");
                Toast.makeText(ToqActivity.this, getString(R.string.intent_toq_watch_disconnected), Toast.LENGTH_SHORT).show();
                disableUI();
            }

        } catch (RemoteDeckOfCardsException e) {
            Toast.makeText(this, getString(R.string.error_checking_status), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.refreshUI - error checking if Toq watch is connected or deck of cards is installed", e);
        }
        
    }
    
    
    // Disable all UI components
    private void disableUI() {       
        // Disable everything
        //setChildrenEnabled(deckOfCardsPanel, false); 
        //setChildrenEnabled(notificationPanel, false);
    }
    
    
    // Set up UI for when deck of cards applet is already installed
    private void updateUIInstalled() {
        
        // Enable everything
        //setChildrenEnabled(deckOfCardsPanel, true);
        //setChildrenEnabled(notificationPanel, true);
        
        // Install disabled; update, uninstall enabled
        installDeckOfCardsButton.setEnabled(false);
        updateDeckOfCardsButton.setEnabled(true);
        uninstallDeckOfCardsButton.setEnabled(true); 
        
        // Focus
        updateDeckOfCardsButton.requestFocus();
    }
    
    
    // Set up UI for when deck of cards applet is not installed
    private void updateUINotInstalled() {
        // Disable notification panel
        //setChildrenEnabled(notificationPanel, false);

        // Enable deck of cards panel
        //setChildrenEnabled(deckOfCardsPanel, true);
        
        // Install enabled; update, uninstall disabled
        installDeckOfCardsButton.setEnabled(true);
        updateDeckOfCardsButton.setEnabled(false);
        uninstallDeckOfCardsButton.setEnabled(false);
        
        // Focus
        installDeckOfCardsButton.requestFocus();
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
    
    
    // Unregister state receiver 
    private void unregisterStateReceiver() {
        getApplicationContext().unregisterReceiver(toqAppStateReceiver);
    }
    
    
    // Set status bar message
    private void setStatus(String msg) {
        statusTextView.setText(msg);
    }
    
    
    // Initialise deck of cards
    private void initDeckOfCards() {    
        // Try to retrieve a stored deck of cards
        try{
            
            // If there is no stored deck of cards or it is unusable, then create new and store
            if ((deckOfCards = getStoredDeckOfCards()) == null) {               
                deckOfCards = createDeckOfCards();
                storeDeckOfCards();                
            }
            
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

    
    // Get stored deck of cards if one exists
    private RemoteDeckOfCards getStoredDeckOfCards() throws Exception {
        
        if (!isValidDeckOfCards()) {
            Log.w(Constants.TAG, "ToqApiDemo.getStoredDeckOfCards - stored deck of cards not valid for this version of the demo, recreating...");
            return null;
        }
        
        SharedPreferences prefs = getSharedPreferences(SBC_PREFS_FILE, Context.MODE_PRIVATE);
        String deckOfCardsStr   = prefs.getString(DECK_OF_CARDS_KEY, null);
        
        if (deckOfCardsStr == null) {
            return null;
            
        } else {
            return ParcelableUtil.unmarshall(deckOfCardsStr, RemoteDeckOfCards.CREATOR);
        }
        
    }
    
    // Store deck of cards
    private void storeDeckOfCards() throws Exception {        
        SharedPreferences prefs = getSharedPreferences(SBC_PREFS_FILE, Context.MODE_PRIVATE);
        Editor editor           = prefs.edit();
        
        editor.putString(DECK_OF_CARDS_KEY, ParcelableUtil.marshall(deckOfCards));
        editor.putInt(DECK_OF_CARDS_VERSION_KEY, Constants.VERSION_CODE);
        editor.commit();       
    }
    
    // Check if the stored deck of cards is valid for this version of the demo
    private boolean isValidDeckOfCards() {
        
        SharedPreferences prefs = getSharedPreferences(SBC_PREFS_FILE, Context.MODE_PRIVATE);
        int deckOfCardsVersion  = prefs.getInt(DECK_OF_CARDS_VERSION_KEY, 0);

        if (deckOfCardsVersion < Constants.VERSION_CODE) {
            return false;
        }
        
        return true;
    }
    
    
    // Create some cards with example content
    private RemoteDeckOfCards createDeckOfCards() {
        
        ListCard listCard = new ListCard();
        
        try {
        	Assets assets = new Assets(ToqActivity.this);
			assets.makeCardList(listCard);
			
		} catch (JSONException e) {}

        return new RemoteDeckOfCards(this, listCard);  
    }

    
    // Initialise the UI
    private void initUI() {
        // Buttons
        installDeckOfCardsButton = (Button)findViewById(R.id.doc_install_button);
        installDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                installDeckOfCards();
            }
        });
        
        updateDeckOfCardsButton = (Button)findViewById(R.id.doc_update_button);
        updateDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                updateDeckOfCards();
            }
        });
        
        uninstallDeckOfCardsButton= (Button)findViewById(R.id.doc_uninstall_button);
        uninstallDeckOfCardsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                uninstallDeckOfCards();
            }
        });
        
        sendNotificationButton= (Button)findViewById(R.id.send_notification_button);
        sendNotificationButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v){            
                sendNotification();
            }
        });
        
        
        // Deck of cards
        ListCard listCard = deckOfCards.getListCard();
        
        try {
        	Assets assets = new Assets(ToqActivity.this);
			assets.makeCardList(listCard);
			
		} catch (JSONException e) {}

        // Status
        statusTextView = (TextView)findViewById(R.id.status_text);
        statusTextView.setText("Initialised");        
    }
    
    
    // Install deck of cards applet
    private void installDeckOfCards(){
        
        Log.d(Constants.TAG, "ToqApiDemo.installDeckOfCards");
        
        updateDeckOfCardsFromUI();
        
        try{                      
            deckOfCardsManager.installDeckOfCards(deckOfCards, resourceStore);
        }
        catch (RemoteDeckOfCardsException e){
            Toast.makeText(this, getString(R.string.error_installing_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.installDeckOfCards - error installing deck of cards applet", e);
        }
        
        try{
            storeDeckOfCards();
        }
        catch (Exception e){
            Log.e(Constants.TAG, "ToqApiDemo.installDeckOfCards - error storing deck of cards applet", e);
        }

    }
    
    
    // Update deck of cards applet
    private void updateDeckOfCards(){
        
        Log.d(Constants.TAG, "ToqApiDemo.updateDeckOfCards");
        
        updateDeckOfCardsFromUI();
        
        try{            
            deckOfCardsManager.updateDeckOfCards(deckOfCards, resourceStore);
            
        } catch (RemoteDeckOfCardsException e){
            Toast.makeText(this, getString(R.string.error_updating_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.updateDeckOfCards - error updating deck of cards applet", e);
        }
        
        try{
            storeDeckOfCards();
            
        } catch (Exception e){
            Log.e(Constants.TAG, "ToqApiDemo.updateDeckOfCards - error storing deck of cards applet", e);
        }

    }
    
    
    // Uninstall deck of cards applet
    private void uninstallDeckOfCards(){
        
        Log.d(Constants.TAG, "ToqApiDemo.uninstallDeckOfCards");
        
        try{                        
            deckOfCardsManager.uninstallDeckOfCards();
        }
        catch (RemoteDeckOfCardsException e){
            Toast.makeText(this, getString(R.string.error_uninstalling_deck_of_cards), Toast.LENGTH_SHORT).show();
            Log.e(Constants.TAG, "ToqApiDemo.uninstallDeckOfCards - error uninstalling deck of cards applet applet", e);
        }

    }
    
    
    // Send notification button
    private void sendNotification(){
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
 
    // Enable/Disable a view group's children and nested children
    private void setChildrenEnabled(ViewGroup viewGroup, boolean isEnabled){

        for (int i = 0; i < viewGroup.getChildCount();  i++){

            View view= viewGroup.getChildAt(i);

            if (view instanceof ViewGroup){
                setChildrenEnabled((ViewGroup)view, isEnabled);
            }
            else{
                view.setEnabled(isEnabled);
            }

        }       
        
    }
  
  
    // Read an icon from assets and return as a bitmap
    private Bitmap getIcon(String fileName) throws Exception {

        try {
            InputStream is= getAssets().open(fileName);
            return BitmapFactory.decodeStream(is);
            
        } catch (Exception e) {
            throw new Exception("An error occurred getting the icon: " + fileName, e);
        }

    }
    
    
    // Parse the UI to update the deck of cards contents
    private void updateDeckOfCardsFromUI() {
        ListCard listCard = deckOfCards.getListCard();

        /* // Card 1
        SimpleTextCard simpleTextCard= (SimpleTextCard)listCard.childAtIndex(0);        
        simpleTextCard.setHeaderText(((EditText)findViewById(R.id.doc1_header_text)).getText().toString());
        simpleTextCard.setTitleText(((EditText)findViewById(R.id.doc1_title_text)).getText().toString());
        simpleTextCard.setMessageText(splitString(((EditText)findViewById(R.id.doc1_message_text)).getText().toString()));
        simpleTextCard.setInfoText(((EditText)findViewById(R.id.doc1_info_text)).getText().toString());
        simpleTextCard.setReceivingEvents(((CheckBox)findViewById(R.id.doc1_events_checkbox)).isChecked());
        simpleTextCard.setShowDivider(((CheckBox)findViewById(R.id.doc1_divider_checkbox)).isChecked());
        simpleTextCard.setTimeMillis(System.currentTimeMillis());
        
        if (((EditText)findViewById(R.id.doc1_menu_options_text)).getText().length() == 0) {
            simpleTextCard.setMenuOptions(null); // If all menu options deleted, reset
            
        } else {
            simpleTextCard.setMenuOptions(splitString(((EditText)findViewById(R.id.doc1_menu_options_text)).getText().toString()));
        }     
                
        // Card 2
        simpleTextCard= (SimpleTextCard)listCard.childAtIndex(1);        
        simpleTextCard.setHeaderText(((EditText)findViewById(R.id.doc2_header_text)).getText().toString());
        simpleTextCard.setTitleText(((EditText)findViewById(R.id.doc2_title_text)).getText().toString());
        simpleTextCard.setMessageText(splitString(((EditText)findViewById(R.id.doc2_message_text)).getText().toString()));
        simpleTextCard.setInfoText(((EditText)findViewById(R.id.doc2_info_text)).getText().toString());
        simpleTextCard.setReceivingEvents(((CheckBox)findViewById(R.id.doc2_events_checkbox)).isChecked());
        simpleTextCard.setShowDivider(((CheckBox)findViewById(R.id.doc2_divider_checkbox)).isChecked());
        simpleTextCard.setTimeMillis(System.currentTimeMillis());
        
        if (((EditText)findViewById(R.id.doc2_menu_options_text)).getText().length() == 0) {
            simpleTextCard.setMenuOptions(null); // If all menu options deleted, reset
            
        } else {
            simpleTextCard.setMenuOptions(splitString(((EditText)findViewById(R.id.doc2_menu_options_text)).getText().toString()));
        }
        
        // Card 3
        simpleTextCard= (SimpleTextCard)listCard.childAtIndex(2);        
        simpleTextCard.setHeaderText(((EditText)findViewById(R.id.doc3_header_text)).getText().toString());
        simpleTextCard.setTitleText(((EditText)findViewById(R.id.doc3_title_text)).getText().toString());
        simpleTextCard.setMessageText(splitString(((EditText)findViewById(R.id.doc3_message_text)).getText().toString()));
        simpleTextCard.setInfoText(((EditText)findViewById(R.id.doc3_info_text)).getText().toString());
        simpleTextCard.setReceivingEvents(((CheckBox)findViewById(R.id.doc3_events_checkbox)).isChecked());
        simpleTextCard.setShowDivider(((CheckBox)findViewById(R.id.doc3_divider_checkbox)).isChecked());
        simpleTextCard.setTimeMillis(System.currentTimeMillis());
        
        if (((EditText)findViewById(R.id.doc3_menu_options_text)).getText().length() == 0) {
            simpleTextCard.setMenuOptions(null); // If all menu options deleted, reset
            
        } else {
            simpleTextCard.setMenuOptions(splitString(((EditText)findViewById(R.id.doc3_menu_options_text)).getText().toString()));
        } 
		*/
    }
    
    
    private String concatStrings(String[] textStrs) {
        StringBuilder buffy= new StringBuilder();
        
        for (int i= 0; i < textStrs.length; i++) {
            
            buffy.append(textStrs[i]);
            
            if (i < (textStrs.length - 1)) {
                buffy.append("\n");
            }
        }
        
        return buffy.toString();        
    }
    
    private String[] splitString(String textStr) {       
        return textStr.split("\n");
    }
}
