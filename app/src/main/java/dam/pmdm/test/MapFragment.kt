package dam.pmdm.test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import dam.pmdm.test.databinding.FragmentMapBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val _apiKey= "tbHpRBP135tCsmnuZo9P"

    // Bandera para evitar doble carga del mapa
    private var isMapLoaded = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            // El permiso se acaba de conceder, intentamos configurar el mapa
            mapSetup()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicialización del motor ANTES de inflar la vista
        MapLibre.getInstance(
            requireContext(),
            _apiKey,
            org.maplibre.android.WellKnownTileServer.MapTiler
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // IMPORTANTE: Notificar la creación a la vista
        binding.mapLibreView.onCreate(savedInstanceState)

        checkLocationPermission()

        return binding.root
    }

    private fun checkLocationPermission() {
        val fineLoc = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fineLoc) == PackageManager.PERMISSION_GRANTED) {
            mapSetup()
        } else {
            getPermissions()
        }
    }

    private fun getPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    private fun mapSetup() {
        // Verificamos que la vista exista y que no se haya cargado ya
        val mapView = _binding?.mapLibreView ?: return
        if (isMapLoaded) return

        mapView.getMapAsync { map ->
            val styleURL = "https://api.maptiler.com/maps/streets-v4/style.json?key=$_apiKey"

            map.setStyle(styleURL) { style ->
                val context = context ?: return@setStyle
                isMapLoaded = true // Evitamos que se repita este proceso

                // 1. Preparar icono
                val bitmap = ContextCompat.getDrawable(context, R.drawable.location)
                    ?.toBitmap(width = 100, height = 100)

                bitmap?.let { style.addImage("red_marker", it) }

                // 2. Gestionar anotaciones
                val symbolManager = SymbolManager(mapView, map, style)
                symbolManager.iconAllowOverlap = true
                symbolManager.textAllowOverlap = true

                // 3. Crear marcador en IES Aguadulce
                val marker = LatLng(36.8094419, -2.5830051)
                val options = SymbolOptions()
                    .withLatLng(marker)
                    .withIconImage("red_marker")
                    .withIconSize(0.5f)
                    .withTextField("IES Aguadulce")
                    .withTextOffset(arrayOf(0f, 1.5f))
                    .withTextSize(12f)

                symbolManager.create(options)

                // 4. Mover cámara
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 16.0))
            }
        }
    }

    // --- MANTENIMIENTO DEL CICLO DE VIDA ---
    override fun onStart() {
        super.onStart()
        _binding?.mapLibreView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        _binding?.mapLibreView?.onResume()
        // Si el mapa se quedó en blanco por el diálogo de permisos, esto lo rescata
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mapSetup()
        }
    }

    override fun onPause() {
        super.onPause()
        _binding?.mapLibreView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        _binding?.mapLibreView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.mapLibreView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapLibreView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.mapLibreView?.onDestroy()
        _binding = null
    }
}