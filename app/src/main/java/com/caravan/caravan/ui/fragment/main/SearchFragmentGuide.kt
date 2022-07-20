package com.caravan.caravan.ui.fragment.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.caravan.caravan.R
import com.caravan.caravan.adapter.GuideAdapter2
import com.caravan.caravan.databinding.FragmentSearchGuideBinding
import com.caravan.caravan.manager.SharedPref
import com.caravan.caravan.model.search.SearchGuide
import com.caravan.caravan.model.search.SearchGuideSend
import com.caravan.caravan.network.ApiService
import com.caravan.caravan.network.RetrofitHttp
import com.caravan.caravan.ui.fragment.BaseFragment
import com.caravan.caravan.utils.OkInterface
import com.caravan.caravan.utils.UiStateObject
import com.caravan.caravan.viewmodel.main.search.SearchGuideRepository
import com.caravan.caravan.viewmodel.main.search.SearchGuideViewModel
import com.caravan.caravan.viewmodel.main.search.SearchGuideViewModelFactory
import com.caravan.caravan.viewmodel.main.search.SearchSharedVM

class SearchFragmentGuide : BaseFragment() {
    private lateinit var binding: FragmentSearchGuideBinding
    private val sharedViewModel: SearchSharedVM by activityViewModels()
    private lateinit var viewModel: SearchGuideViewModel

    private var currentPage: Int = 1
    private var totalPage: Int = 1
    private var guides: ArrayList<SearchGuide> = ArrayList()
    private lateinit var adapter: GuideAdapter2
    private lateinit var searchGuideSend: SearchGuideSend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchGuideBinding.inflate(layoutInflater)

        initViews()
        return binding.root
    }

    private fun initViews() {
        setUpViewModel()
        setUpObservers()

        binding.apply {
            adapter = GuideAdapter2(this@SearchFragmentGuide, guides)
            recyclerView.adapter = adapter

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    closeKeyboard(recyclerView)
                }
            })
        }

        sharedViewModel.guideSearch.observe(viewLifecycleOwner) {
            searchGuideSend = it
            currentPage = 1
            totalPage = 1
            guides = ArrayList()
            viewModel.searchGuide(currentPage, it)
        }

        binding.apply {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView1: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView1.canScrollVertically(RecyclerView.VERTICAL) && newState == RecyclerView.SCROLL_STATE_IDLE) {
                        if(currentPage < totalPage){
                            viewModel.searchGuide(++currentPage, searchGuideSend)
                        }
                    }
                }

            })
        }
    }

    private fun setUpObservers() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.guides.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        Log.d("SearchFragmentGuide", "Success:${it.toString()}")
                        guides.addAll(it.data.guideList)
                        totalPage = it.data.totalPage!!
                        adapter.submitList(guides)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        Log.d("SearchFragmentGuide", "ERROR: ${it.message}")
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
//                                    requireActivity().finish()
                                    return
                                }
                            })
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(
            this, SearchGuideViewModelFactory(
                SearchGuideRepository(
                    RetrofitHttp.createServiceWithAuth(
                        SharedPref(requireContext()), ApiService::class.java
                    )
                )
            )
        )[SearchGuideViewModel::class.java]
    }
}