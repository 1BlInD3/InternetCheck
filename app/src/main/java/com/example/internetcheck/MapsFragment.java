package com.example.internetcheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment {
    private GoogleMap mMap;
    private ArrayList<WeightedLatLng> list  = new ArrayList<>();
    private ArrayList<LatLng> arrayList = new ArrayList<>();

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            LatLng kaposvar = new LatLng(46.3659055,17.7840263);
           // googleMap.addMarker(new MarkerOptions().position(kaposvar).title("Otthon"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kaposvar,15));
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15),2000,null);
           // LoadCorrdintaes(googleMap);
            LoadWeightedCoordinates();
            AddHeatmap(list);

        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
    private  void AddHeatmap(List<WeightedLatLng> points)
    {
        HeatmapTileProvider heatmapTileProvider = new HeatmapTileProvider.Builder()
                .weightedData(points)
                .build();
        TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(heatmapTileProvider));

    }
    private void LoadWeightedCoordinates()
    {
        ArrayList<WeightedLatLng> latLngs = (ArrayList<WeightedLatLng>) getArguments().getSerializable("COORDINATES");
        for (int i = 0; i < latLngs.size(); i++)
        {
            list.add(latLngs.get(i));
        }
    }
    private void LoadCorrdintaes(GoogleMap googleMap)
    {
        ArrayList<LatLng> latLngs = (ArrayList<LatLng>) getArguments().getSerializable("Coord");
        ArrayList<String> asd = (ArrayList<String>) getArguments().getSerializable("dec");

        for (int i = 0; i<latLngs.size(); i++)
        {
            googleMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title(asd.get(i)));
        }

    }
}