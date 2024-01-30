package com.example.pt3api;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final float DEFAULT_ZOOM = 15.0f; // Adjust the zoom level as needed
    private static final LatLng defaultLocation = new LatLng(41.12, 1.243988); // Set a default location

    private FloatingActionButton mAddAlarmFab, mAddPersonFab;
    private ExtendedFloatingActionButton mAddFab;
    private FloatingActionButton yourLocation;
    private String apiKey;
    private Boolean isAllFabsVisible;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mapa;
    private final int PERMISO_LOCALIZACION = 1;
    private Location lastKnownLocation;
    private boolean locationPermissionGranted = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiKey = getResources().getString(R.string.google_maps_key);

        Places.initialize(getApplicationContext(), apiKey );
        PlacesClient placesClient = Places.createClient(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAddFab = findViewById(R.id.add_fab);
        mAddAlarmFab = findViewById(R.id.add_alarm_fab);
        mAddPersonFab = findViewById(R.id.add_person_fab);
        yourLocation = findViewById(R.id.yourLocation);

        mAddAlarmFab.setVisibility(View.GONE);
        mAddPersonFab.setVisibility(View.GONE);
        yourLocation.setVisibility(View.GONE);

        isAllFabsVisible = false;

        mAddFab.shrink();

        mAddFab.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!isAllFabsVisible) {

                            mAddAlarmFab.show();
                            mAddPersonFab.show();
                            yourLocation.show();

                            mAddAlarmFab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MapDialogFragment dialog = new MapDialogFragment();
                                    dialog.show(getSupportFragmentManager(), "MapDialogFragment");
                                }
                            });

                            mAddPersonFab.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    MapDialogFragment2 dialog = new MapDialogFragment2();
                                    dialog.show(getSupportFragmentManager(), "MapDialogFragment2");
                                }
                            });

                            yourLocation.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    getDeviceLocation();
                                }
                            });

                            mAddFab.extend();

                            isAllFabsVisible = true;
                        } else {
                            mAddAlarmFab.hide();
                            mAddPersonFab.hide();
                            yourLocation.hide();

                            mAddFab.shrink();

                            isAllFabsVisible = false;

                        }
                    }
                });
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mapa.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mapa.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void moveMapCamera(LatLng newLatLng) {
        if (mapa != null) {
            mapa.moveCamera(CameraUpdateFactory.newLatLng(newLatLng));
        }
    }

    ;

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

    }
    public void habilitaLocalitzacio() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mapa.setMyLocationEnabled(true);
        } else {
            // Demanem a l'usuari que ens doni permís per localitzar-se a ell mateix
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISO_LOCALIZACION);
        }
    }

    private void afegirMarcador(LatLng latitudLongitud, String titol) {
        // Possibles colors: HUE_RED, HUE_AZURE, HUE_BLUE, HUE_CYAN, HUE_GREEN, HUE_MAGENTA, HUE_ORANGEHUE_ROSE, HUE_VIOLET, HUE_YELLOW
        mapa.addMarker(new MarkerOptions().position(latitudLongitud).title(titol)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapa = googleMap;

        LatLng tgn = new LatLng(41.12, 1.243988);

        // Afegim l'institut F. Vidal i Barraquer
        afegirMarcador(tgn, "INS F. Vidal i Barraquer");

        // Podem canviar el tipus de mapa que volem (per defecte, tipus normal)
        mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(tgn)   // Centrem el mapa a Tarragona
                .zoom(17)       // Establim el zoom en 17
                .bearing(0)     // Establim l'orientació amb el nordest dalt
                .tilt(70)       // Baixem el punt de vista de la càmera 70 graus
                .build();

        // Creem un CameraUpdate que crea un moviment cap al marcador
        CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(camPos);
        mapa.animateCamera(camUpdate);

        // Escoltem els clicks normals i clicks llargs damunt del mapa
        mapa.setOnMapClickListener(this);
        mapa.setOnMapLongClickListener(this);
        habilitaLocalitzacio();
    }

    public static class MapDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle("Coordenadas");
            LinearLayout layout = new LinearLayout(requireActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            EditText latitudeEditText = new EditText(requireActivity());
            latitudeEditText.setHint("Latitud");
            layout.addView(latitudeEditText);

            EditText longitudeEditText = new EditText(requireActivity());
            longitudeEditText.setHint("Longitud");
            layout.addView(longitudeEditText);

            LinearLayout layout2 = new LinearLayout(requireActivity());
            layout2.setOrientation(LinearLayout.HORIZONTAL);

            Button aceptar = new Button(requireActivity());
            aceptar.setText("Aceptar");
            LinearLayout.LayoutParams paramsAceptar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout2.addView(aceptar);

            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String lt = latitudeEditText.getText().toString();
                    String lg = longitudeEditText.getText().toString();

                    // Move the map camera to the entered coordinates
                    if (!lt.isEmpty() && !lg.isEmpty()) {
                        double latitude = Double.parseDouble(lt);
                        double longitude = Double.parseDouble(lg);

                        // Call the moveCamera method in MainActivity
                        if (getActivity() != null && getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).moveMapCamera(new LatLng(latitude, longitude));
                            dismiss();
                        }
                    }
                }
            });


            Button cancelar = new Button(requireActivity());
            cancelar.setText("Cancelar");
            LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            paramsCancelar.setMargins(50, 0, 0, 0);
            cancelar.setLayoutParams(paramsCancelar);
            layout2.addView(cancelar);

            LinearLayout containerLayout = new LinearLayout(requireActivity());
            containerLayout.setOrientation(LinearLayout.VERTICAL);
            containerLayout.addView(layout);
            containerLayout.addView(layout2);

            builder.setView(containerLayout);
            return builder.create();
        }
    }

    public static class MapDialogFragment2 extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

            builder.setTitle("Poblacion");
            LinearLayout layout = new LinearLayout(requireActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            EditText poblacionText = new EditText(requireActivity());
            poblacionText.setHint("Poblacion");
            layout.addView(poblacionText);

            LinearLayout layout2 = new LinearLayout(requireActivity());
            layout2.setOrientation(LinearLayout.HORIZONTAL);

            Button aceptar = new Button(requireActivity());
            aceptar.setText("Aceptar");
            LinearLayout.LayoutParams paramsAceptar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout2.addView(aceptar);

            aceptar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String poblacion = poblacionText.getText().toString();

                    if (!poblacion.isEmpty()) {
                        Geocoder geocoder = new Geocoder(requireContext());
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(poblacion, 1);

                            if (!addresses.isEmpty()) {
                                double latitude = addresses.get(0).getLatitude();
                                double longitude = addresses.get(0).getLongitude();

                                if (getActivity() != null && getActivity() instanceof MainActivity) {
                                    ((MainActivity) getActivity()).moveMapCamera(new LatLng(latitude, longitude));
                                    dismiss();
                                }
                            } else {

                                Toast.makeText(requireContext(), "Poblacion no encontrada", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });



            Button cancelar = new Button(requireActivity());
            cancelar.setText("Cancelar");
            LinearLayout.LayoutParams paramsCancelar = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            cancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

            paramsCancelar.setMargins(50, 0, 0, 0);
            cancelar.setLayoutParams(paramsCancelar);
            layout2.addView(cancelar);

            LinearLayout containerLayout = new LinearLayout(requireActivity());
            containerLayout.setOrientation(LinearLayout.VERTICAL);
            containerLayout.addView(layout);
            containerLayout.addView(layout2);

            builder.setView(containerLayout);
            return builder.create();
        }
    }
}