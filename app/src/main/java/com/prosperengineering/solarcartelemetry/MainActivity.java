package com.prosperengineering.solarcartelemetry;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import com.ekndev.gaugelibrary.ArcGauge;
import com.ekndev.gaugelibrary.HalfGauge;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.prosperengineering.solarcartelemetry.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.prosperengineering.solarcartelemetry.databinding.FragmentFirstBinding;

public class MainActivity extends AppCompatActivity
    {
    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    // NOTE:  These must match the Arduino Transmit sketch (SC23Transmitter.ino)
    static final String KEY_SPEED         = "spd";
    static final String KEY_MAIN_VOLTAGE  = "mn";
    static final String KEY_AUX_VOLTAGE   = "aux";
    static final String KEY_SOLAR_CURRENT = "sol";
    static final String KEY_MOTOR_CURRENT = "mtr";
    static final String KEY_STATUS        = "status";
    static final String KEY_RSSI          = "rssi";

    public final String ACTION_USB_PERMISSION = "com.prosperengineering.solarcartelemetry.USB_PERMISSION";
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;

    String bufferedRead;

    // The Controls we will update with data
    TextView textView;
    ArcGauge gaugeSpeed;
    ArcGauge gaugeMainVoltage;
    ArcGauge gaugeAuxVoltage;
    ArcGauge gaugeSolarCurrent;
    ArcGauge gaugeMotorCurrent;

    int  iNumLinesStatusView = 2;
    String[]  arrayStatus = {"","","","",""};

    //Defining a Callback which triggers whenever data is read.
    //-------------------------------------------------------------------------
    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
        {
        @Override
        public void onReceivedData(byte[] arg0)
            {
            String data = null;
            try
                {
                data = new String(arg0, "UTF-8");

                // TODO: Tokenize by comma, then parse into KVP pairs.
                //  Feed to different controls/text fields based on Key.
                ProcessInput (data);

                //data.concat("/n");
                //tvAppend(textView, data);
                }
            catch (UnsupportedEncodingException e)
                {
                e.printStackTrace();
                }
            }
        };

    //Broadcast Receiver to automatically start and stop the Serial connection.
    //-------------------------------------------------------------------------
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
        @Override
        public void onReceive(Context context, Intent intent)
            {
            if (intent.getAction().equals(ACTION_USB_PERMISSION))
                {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted)
                    {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null)
                        {
                        //Set Serial Connection Parameters.
                        if (serialPort.open())
                            {
                            //setUiEnabled(true);
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            tvAppend(textView,"Serial Connection Opened!\n");

                            }
                        else
                            {
                            Log.d("SERIAL", "PORT NOT OPEN");
                            }
                        }
                    else
                        {
                        Log.d("SERIAL", "PORT IS NULL");
                        }
                    }
                else
                    {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                    }
                }
            else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED))
                {
                StartConnection();
                }
            else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED))
                {
                StopConnection();
                }
            };
        };





    //-------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);


        textView       = (TextView) findViewById(R.id.textview_first);
        gaugeMainVoltage  = (ArcGauge) findViewById(R.id.mainVoltageGauge);
        gaugeAuxVoltage   = (ArcGauge) findViewById(R.id.auxVoltageGauge);
        gaugeSolarCurrent = (ArcGauge) findViewById(R.id.solarCurrentGauge);
        gaugeMotorCurrent = (ArcGauge) findViewById(R.id.motorCurrentGauge);
        gaugeSpeed        = (ArcGauge)findViewById(R.id.speedGauge);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);


        /*
        binding.fab.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
                }
            });

         */
        }

    //-------------------------------------------------------------------------
    public void StartConnection()
        {
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty())
            {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
                {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if ((deviceVID == 0x2341) || //Arduino Vendor ID
                    (deviceVID == 0x239A))   //Adafruit Vendor ID
                    {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                    }
                else
                    {
                    connection = null;
                    device = null;
                    }

                if (!keep)
                    break;
                }
            }
        }

    //-------------------------------------------------------------------------
    public void StopConnection()
        {
        serialPort.close();
        // TODO: Set icon to show disconnected.
        }




    //-------------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
        }

    //-------------------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            {
            return true;
            }

        return super.onOptionsItemSelected(item);
        }

    //-------------------------------------------------------------------------
    @Override
    public boolean onSupportNavigateUp()
        {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
        }

    //-------------------------------------------------------------------------
    private void tvAppend(TextView tv, CharSequence text)
        {
        // TODO: This should only hold X lines of data, then scroll.

        final TextView ftv = tv;
        StringBuilder  sbTextOut = new StringBuilder();

        // Scroll text
        int  iIndex;
        for (iIndex = 0; iIndex < iNumLinesStatusView - 1; ++iIndex)
            {
            arrayStatus [iIndex] = arrayStatus [iIndex + 1];
            sbTextOut.append(arrayStatus [iIndex]);
            }
        arrayStatus[iNumLinesStatusView - 1] = text.toString();
        sbTextOut.append(arrayStatus [iNumLinesStatusView - 1]);

        final CharSequence ftext = sbTextOut.toString();

        runOnUiThread(new Runnable()
            {
            @Override
            public void run()
                {
                ftv.setText(ftext);
                }
            });
        }


    //-------------------------------------------------------------------------
    private void ProcessInput (String  strIn)
        {
        // NOTE: Not the fastest, but hopefully more stable than dealing with trying
        //  to split into lists.

        bufferedRead = bufferedRead + strIn;

        int iNextComma = bufferedRead.indexOf(',');

        while (iNextComma != -1)
          {
          String  strElement = bufferedRead.substring (0, iNextComma);
          bufferedRead = bufferedRead.substring (iNextComma + 1, bufferedRead.length());

          DisplayInput (strElement);

          iNextComma = bufferedRead.indexOf(',');
          };
        }

    //-------------------------------------------------------------------------
    private void DisplayInput (String  strIn)
        {
        //tvAppend(textView,"Parsing strIn into KVP "+strIn+"\n");
        String[] keyValuePair = strIn.split("[:]");
        if (keyValuePair.length == 2) // We only care about 2-element matches
            {
            //tvAppend(textView,"Redirecting key "+keyValuePair[0]+"\n");
            try
                {
                switch (keyValuePair[0])
                    {
                    case KEY_SPEED:
                        gaugeSpeed.setValue ((float)Math.floor (Double.parseDouble(keyValuePair[1])));
                        //tvAppend(textView,"Setting speed to "+keyValuePair[1]+"\n");
                        break;
                    case KEY_MAIN_VOLTAGE:
                        gaugeMainVoltage.setValue (Float.parseFloat(keyValuePair[1]));
                        break;
                    case KEY_AUX_VOLTAGE:
                        gaugeAuxVoltage.setValue (Float.parseFloat(keyValuePair[1]));
                        break;
                    case KEY_SOLAR_CURRENT:
                        gaugeSolarCurrent.setValue (Float.parseFloat(keyValuePair[1]));
                        break;
                    case KEY_MOTOR_CURRENT:
                        gaugeMotorCurrent.setValue (Float.parseFloat(keyValuePair[1]));
                        break;
                    case KEY_STATUS:
                        tvAppend(textView,keyValuePair[1]+"\n");
                        break;
                    case KEY_RSSI:
                        // TODO: Show and update an RSSI text field.
                        break;
                    }
                }
            catch (Exception e)
                {
                // Just ignore failed parses of potentially garbled input.  Update
                //  only with good info the next time it comes in.
                // TODO:  If there are too many instances of bad data in a row,
                //   you may want to indicate on the dash that telemetry is unreliable.
                tvAppend(textView,"Unable to parse: " + keyValuePair[0] + " : " +  keyValuePair[1] + "\n");

                }
            }
        else
            {
            tvAppend(textView,"Unable to split: " + strIn + "\n");
            }
        }



    }
