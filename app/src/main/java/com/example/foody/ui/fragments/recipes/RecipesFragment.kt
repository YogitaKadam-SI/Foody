package com.example.foody.ui.fragments.recipes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.foody.viewmodels.MainViewModel
import com.example.foody.R
import com.example.foody.adapters.RecipesAdapter
import com.example.foody.databinding.FragmentRecipesBinding
import com.example.foody.util.Constants
import com.example.foody.util.Constants.Companion.API_KEY
import com.example.foody.util.Constants.Companion.DEFAULT_DIET_TYPE
import com.example.foody.util.Constants.Companion.DEFAULT_MEAL_TYPE
import com.example.foody.util.Constants.Companion.DEFAULT_RECIPES_NUMBER
import com.example.foody.util.NetworkResult
import com.example.foody.util.observeOnce
import com.example.foody.viewmodels.RecipesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipesFragment : Fragment() {

    private  var _binding : FragmentRecipesBinding? = null
    private val binding get() = _binding!!


    //private lateinit var mainViewModel: MainViewModel
    //private lateinit var recipesViewModel: RecipesViewModel
    private val mAdapter by lazy { RecipesAdapter() }
    //private lateinit var mView: View
    private var recyclerView: RecyclerView? = null

    private var loader: ProgressBar? = null
    private val mainViewModel : MainViewModel by viewModels()
    private val recipesViewModel : RecipesViewModel by viewModels()

    //override fun onCreate(savedInstanceState: Bundle?) {
      //  super.onCreate(savedInstanceState)
        //mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        //recipesViewModel = ViewModelProvider(requireActivity()).get(RecipesViewModel::class.java)
    //}

   // private var loader: ProgressBar? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
       Log.i("RecipesFragment", "onCreateView")
        binding.lifecycleOwner = this
        binding.mainViewModel = mainViewModel


       binding.recipesFab.setOnClickListener {
           findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
       }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.rv_recipes)
        loader = view.findViewById(R.id.loader)
        setupRecyclerView()
        readDatabase()

    }
    private fun setupRecyclerView() {
        val adapter = mAdapter
        recyclerView?.adapter = adapter
        //recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        //showLoaderEffect()
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observe(viewLifecycleOwner, { database ->
                if (database.isEmpty()) {
                    database.getOrNull(0)?.results?.let { mAdapter.setData(it) }
                    hideLoader()
                } else {
                    requestApiData()
                }
            })
        }
    }

    private fun requestApiData() {
        mainViewModel.getRecipes(recipesViewModel.applyQueries())
        mainViewModel.recipesResponse.observe(viewLifecycleOwner, { response ->
            when(response){
                is NetworkResult.Success -> {
                    hideLoader()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    hideLoader()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading ->{
                    showLoaderEffect()
                }
            }
        })
    }

    private fun loadDataFromCache() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observeOnce(viewLifecycleOwner, { database ->
                database
                if (database.isNotEmpty()) {
                    database.getOrNull(0)?.results?.let { mAdapter.setData(it) }
                }
            })
        }
    }

  // private fun applyQueries():HashMap<String,String>{
       // val queries: HashMap<String,String> =  HashMap()
        //queries["number"] = DEFAULT_RECIPES_NUMBER
        //queries["apiKey"] = API_KEY
        //queries["type"] = DEFAULT_MEAL_TYPE
        //queries["diet"] = DEFAULT_DIET_TYPE
        //queries["addRecipeInformation"] = "true"
        //queries["fillIngredients"] = "true"

        //return queries

  //  }
    //

    private fun hideLoader(){
        loader?.isVisible = false
    }
    private fun showLoaderEffect() {
        loader?.isVisible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}