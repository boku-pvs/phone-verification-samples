package com.danalinc.samples.phoneverification;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

public class VerifyPhone extends AppCompatActivity implements OnHttpCallCompleted {
    private static final String AUTH_URL = "https://MY_SERVER_URL";
    private Map<String, Object> apiURLs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupButtons();
    }

    private void getAuthorization() {
        setProgressVisibility(true);
        EditText mnView = findViewById(R.id.mobileNumberText);
        String mobileNumber = mnView.getText().toString().trim();
        if (mobileNumber.length() != 10) {
            showAlert("Please enter a valid 10-digit number");
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("mobileNumber", "+1" + mobileNumber);

        HttpHelper httpHelper = HttpHelper.builder()
                .parameters(parameters)
                .listener(this)
                .build();
        httpHelper.execute(AUTH_URL, "getAuthorization");
    }

    private void verifyPhone() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("appName", "My Application");
        parameters.put("fallback", "1");
        parameters.put("smsMessage", "Please visit www.example.com for assistance.");

        String url = (String) apiURLs.get("verifyPhoneNumber");
        HttpHelper httpHelper = HttpHelper.builder()
                .parameters(parameters)
                .listener(this)
                .build();
        httpHelper.execute(url, "verifyPhoneNumber");
    }

    private void verifySMSCode() {
        setProgressVisibility(true);
        EditText pinView = findViewById(R.id.pinText);
        String pin = pinView.getText().toString().trim();
        if (pin.length() > 10) {
            //pin is currently length of 6, but here's some buffer in case that ever changes
            showAlert("Please enter a valid PIN");
            return;
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put("code", pin);
        String url = (String) apiURLs.get("verifySMSCode");

        HttpHelper httpHelper = HttpHelper.builder()
                .parameters(parameters)
                .listener(this)
                .build();
        httpHelper.execute(url, "verifySMSCode");
    }

    @Override
    public void onHttpCallCompleted(HttpResponse httpResponse) {
        try {
            snack("RESPONSE: " + httpResponse.response);
            if (httpResponse.response != null) {
                if ("getAuthorization".equalsIgnoreCase(httpResponse.debug)) {
                    //now have the three API URLs (verifyPhoneNumber, verifySMSCode, resendCode)
                    apiURLs = httpResponse.response;
                    verifyPhone();
                } else if ("verifyPhoneNumber".equalsIgnoreCase(httpResponse.debug)) {
                    handleDanalResponse(httpResponse);
                } else if ("verifySMSCode".equalsIgnoreCase(httpResponse.debug)) {
                    handleDanalResponse(httpResponse);
                } else {
                    //unhandled response
                    showAlert(httpResponse.response.toString());
                }
            } else {
                setProgressVisibility(false);
                showAlert("Server response was null");
            }
        } catch (Exception ex) {
            setProgressVisibility(false);
            System.err.println("httpResponse=" + httpResponse + ", ex=" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleDanalResponse(HttpResponse httpResponse) {
        setProgressVisibility(false);
        Object objStatus = httpResponse.response.get("status");
        if (objStatus instanceof Double) {
            //gson can convert to a double
            objStatus = ((Double) objStatus).intValue();
        }
        DanalResponseStatus status = DanalResponseStatus.fromValue(objStatus.toString());
        if (status == DanalResponseStatus.SUCCESS) {
            //phone verified
            setVerificationFailedVisibility(false);
            setVerificationPassedVisibility(true);
            resetApp();
        } else if (status == DanalResponseStatus.FAILURE) {
            //phone not verified
            setVerificationPassedVisibility(false);
            setVerificationFailedVisibility(true);
            resetApp();
        } else if (status == DanalResponseStatus.SWITCH_TO_FALLBACK) {
            //sms fallback, enter pin
            setMobileNumberVisibility(false);
            setPinVisibility(true);
        } else {
            //error, try again later
            setVerificationPassedVisibility(false);
            setVerificationFailedVisibility(true);
            resetApp();
        }
    }

    private void resetApp() {
        setProgressVisibility(false);
        EditText pinView = findViewById(R.id.pinText);
        pinView.setText("");
        EditText mobileNumberView = findViewById(R.id.mobileNumberText);
        mobileNumberView.setText("");
        setMobileNumberVisibility(true);
        setPinVisibility(false);
    }

    private void setMobileNumberVisibility(boolean isVisible) {
        int visibility = View.VISIBLE;
        if (!isVisible) {
            visibility = View.GONE;
        }
        findViewById(R.id.mobileNumberLabel).setVisibility(visibility);
        findViewById(R.id.mobileNumberText).setVisibility(visibility);
        findViewById(R.id.mobileNumberFAB).setVisibility(visibility);
    }

    private void setPinVisibility(boolean isVisible) {
        int visibility = View.VISIBLE;
        if (!isVisible) {
            visibility = View.GONE;
        }
        findViewById(R.id.pinLabel).setVisibility(visibility);
        findViewById(R.id.pinText).setVisibility(visibility);
        findViewById(R.id.pinFAB).setVisibility(visibility);
    }

    private void setProgressVisibility(boolean isVisible) {
        int visibility = View.VISIBLE;
        if (!isVisible) {
            visibility = View.GONE;
        }
        findViewById(R.id.loadingPanel).setVisibility(visibility);
    }

    private void setVerificationPassedVisibility(boolean isVisible) {
        int visibility = View.VISIBLE;
        if (!isVisible) {
            visibility = View.GONE;
        }
        findViewById(R.id.verificationPassedIcon).setVisibility(visibility);
        findViewById(R.id.verificationPassedLabel).setVisibility(visibility);
    }

    private void setVerificationFailedVisibility(boolean isVisible) {
        int visibility = View.VISIBLE;
        if (!isVisible) {
            visibility = View.GONE;
        }
        findViewById(R.id.verificationFailedIcon).setVisibility(visibility);
        findViewById(R.id.verificationFailedLabel).setVisibility(visibility);
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(VerifyPhone.this);
        builder.setMessage(msg);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void snack(String msg) {
        Snackbar.make(findViewById(R.id.coordinatorLayout),
                "RESPONSE: " + apiURLs, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void setupButtons() {
        FloatingActionButton mobileNumberFab = (FloatingActionButton) findViewById(R.id.mobileNumberFAB);
        mobileNumberFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    getAuthorization();
                    snack("Request sent");
                } catch (Exception e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            }
        });

        FloatingActionButton pinFab = (FloatingActionButton) findViewById(R.id.pinFAB);
        pinFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    verifySMSCode();
                    snack("Request sent");
                } catch (Exception e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_verify_phone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
