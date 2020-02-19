package com.iutbm.example.iutbm.couchot.meetit_1;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int REQUEST_LOCATION = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<Character> characterList = new ArrayList<>();
    private AppDatabase appdb;
    private CharacterDAO characterDAO;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private GoogleApiClient mGoogleApiClient = null;
    private Location mLastLocation = null;

    private Switch sp;

    private OnFragmentInteractionListener mListener;

    public MyMapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyMapFragment newInstance(String param1, String param2) {
        MyMapFragment fragment = new MyMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isChecked = sharedPreferences.getBoolean("key_notification_switch", false);
        System.out.println(isChecked);
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_my_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        appdb = AppDatabase.getDatabase(getContext());
        characterDAO = appdb.characterDAO();
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //boolean isChecked = sharedPreferences.getBoolean("key_notification_switch", false);
        //System.out.println(isChecked);

        (new MyMapFragment.GetAllCharactersAsyncTask(characterDAO)).execute();
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ArrayList<LatLng> listPos = new ArrayList<>();
        for (Character character: characterList) {
            LatLng pos = new LatLng(character.getLatitude(), character.getLongitude());
            listPos.add(pos);
            googleMap.addMarker(new MarkerOptions().position(pos).title(character.getFamilyname() + " " + character.getFirstname()).alpha(0.5f));
        }
        for (LatLng pos: listPos){
            builder.include(pos); // on ajoute les positions de nos markers dans le builder
        }
        LatLngBounds bounds = builder.build(); //on fait un build pour générer les bounds
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,200)); //on recentre la caméra en fonction des bounds build juste avant

    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
        else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient);
            if (mLastLocation != null) {
                String lats = "" + mLastLocation.getLatitude();
                String longs = "" + mLastLocation.getLongitude();
                Toast.makeText(getActivity(), lats + " " + longs, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void requestLocationPermission(){
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class GetAllCharactersAsyncTask extends AsyncTask<Void, Void, Void> {
        private CharacterDAO mAsyncTaskDao;
        ArrayList<Character> characters;

        GetAllCharactersAsyncTask(CharacterDAO dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            characters = new ArrayList<>(mAsyncTaskDao.getAllCharaters());
            characterList = characters;
            return null;
        }
        @Override
        protected void onPostExecute(Void voids){
            //characterList = characters;

        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
