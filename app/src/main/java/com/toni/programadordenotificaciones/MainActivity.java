package com.toni.programadordenotificaciones;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mScheduler;
    private static final int JOB_ID = 0;

    private Switch swInactivo;
    private Switch swCargando;
    private TextView txtsBarProgress;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swInactivo = findViewById(R.id.swInactivo);
        swCargando = findViewById(R.id.swCargando);
        mSeekBar = findViewById(R.id.sbarProgress);
        txtsBarProgress = findViewById(R.id.txtsbarProgress);

        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress > 0){
                    txtsBarProgress.setText(progress + " s");
                }else{
                    txtsBarProgress.setText(R.string.sin_seleccionar);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void anularTrabajo(View view) {
        if(mScheduler != null){
            mScheduler.cancelAll();
            //mScheduler = null;
            Toast.makeText(this, "Trabajo cancelado", Toast.LENGTH_SHORT).show();
        }
    }

    public void programarTrabajo(View view) {
        RadioGroup rgOpcionesRed = findViewById(R.id.rgOpcionesRed);
        int selectedNetworkID = rgOpcionesRed.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        int tiempo = mSeekBar.getProgress();
        boolean esTiempoSeleccionado = tiempo > 0;


        switch (selectedNetworkID){
            case R.id.rbNinguna:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.rbAlguna:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.rbWifi:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName);
        builder.setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(swInactivo.isChecked())
                .setRequiresCharging(swCargando.isChecked());

        if(esTiempoSeleccionado){
            builder.setOverrideDeadline(tiempo * 1000);
        }

        boolean constraintSet = selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE || swCargando.isChecked() || swInactivo.isChecked() || esTiempoSeleccionado;
        if (constraintSet){
            JobInfo myJobInfo = builder.build();
            mScheduler.schedule(myJobInfo);

            Toast.makeText(this,
                    R.string.trabajo_programado,
                    Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this, "Selecciona al menos una condición", Toast.LENGTH_SHORT).show();
        }


    }
}