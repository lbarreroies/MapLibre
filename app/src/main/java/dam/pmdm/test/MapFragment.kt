package dam.pmdm.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        MapLibre.getInstance(requireContext())
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.mapLibreView.onCreate(savedInstanceState)

        mapSetup()

        return binding.root
    }

    private fun mapSetup() {
        binding.mapLibreView.getMapAsync { map ->
            val key = "tbHpRBP135tCsmnuZo9P"
            val style = "https://api.maptiler.com/maps/streets-v2/style.json?key=$key"

            map.setStyle(style) { style ->
                //Personalizamos el marcador
                val bitmap = ContextCompat.getDrawable(requireContext(), R.drawable.location)
                    ?.toBitmap(width = 100, height = 100)
                bitmap?.let {
                    style.addImage("red_marker", it)
                }

                val symbolManager = SymbolManager(binding.mapLibreView, map, style)
                symbolManager.iconAllowOverlap = true

                val marker = LatLng(36.8094419, -2.5830051)
                val options = SymbolOptions()
                    .withLatLng(marker)
                    .withIconImage("red_marker")
                    .withIconSize(0.5f)

                symbolManager.create(options)

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15.0))
            }
        }
    }

    // --- Mantenimiento del ciclo de vida (Crucial) ---

    override fun onStart() {
        super.onStart(); binding.mapLibreView.onStart()
    }

    override fun onResume() {
        super.onResume(); binding.mapLibreView.onResume()
    }

    override fun onPause() {
        super.onPause(); binding.mapLibreView.onPause()
    }

    override fun onStop() {
        super.onStop(); binding.mapLibreView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpieza para evitar memory leaks
        binding.mapLibreView.onDestroy()
        _binding = null
    }
}