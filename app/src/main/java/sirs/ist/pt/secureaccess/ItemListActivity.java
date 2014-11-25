package sirs.ist.pt.secureaccess;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import sirs.ist.pt.secureaccess.ListContent.Content;


/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ItemDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details
 * (if present) is a {@link ItemDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link ItemListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ItemListActivity extends Activity
        implements ItemListFragment.Callbacks {

    private static final int REQUEST_ENABLE_BT = 12;
    private boolean mTwoPane = false;

    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    public boolean deviceSupportsBluetooth(){
        return btAdapter != null;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON){
                    //populate list
                    for (BluetoothDevice bd : getPairedDevices()){
                        Content.addItem(new Content.Item(bd));
                    }

                    ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.item_list);
                    fragment.refreshList();
                }
                else{
                    Content.clean();
                    ItemListFragment fragment = (ItemListFragment) getFragmentManager().findFragmentById(R.id.item_list);
                    fragment.refreshList();
                }
            }

        }
    };

    public boolean isBluetoothActive(){
        if(!btAdapter.getDefaultAdapter().isEnabled()) {
            return false;
        }else {
            return true;
        }
    }

    public ArrayList<BluetoothDevice> getPairedDevices(){

        ArrayList<BluetoothDevice> result = new ArrayList<BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                result.add(device);
            }
        }
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        if(!deviceSupportsBluetooth()){
            Util.makeToast("Device doesn't support bluetooth... quitting", getApplicationContext());
            return;
        }

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        if(!this.isBluetoothActive()){
            Toast.makeText(getApplicationContext(), "Turn on Bluetooth...", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            for (BluetoothDevice bd : getPairedDevices()){
                Content.addItem(new Content.Item(bd));
            }
        }

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ItemListFragment) getFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
    }

    /**
     * Callback method from {@link ItemListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {

        Intent intent = new Intent(this, ItemDetailActivity.class);

        String deviceId = id;
        intent.putExtra("DEVICE_ID", id);
        startActivity(intent);
    }
}
