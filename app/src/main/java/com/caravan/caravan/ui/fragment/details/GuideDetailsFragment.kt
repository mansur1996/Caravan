package com.caravan.caravan.ui.fragment.details

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caravan.caravan.R
import com.caravan.caravan.adapter.CommentsAdapter
import com.caravan.caravan.adapter.TravelLocationsAdapter
import com.caravan.caravan.databinding.FragmentGuideDetailsBinding
import com.caravan.caravan.manager.SharedPref
import com.caravan.caravan.model.*
import com.caravan.caravan.model.hire.Hire
import com.caravan.caravan.model.more.ActionMessage
import com.caravan.caravan.model.review.Review
import com.caravan.caravan.network.ApiService
import com.caravan.caravan.network.RetrofitHttp
import com.caravan.caravan.ui.fragment.BaseFragment
import com.caravan.caravan.utils.Extensions.toast
import com.caravan.caravan.utils.OkInterface
import com.caravan.caravan.utils.UiStateObject
import com.caravan.caravan.viewmodel.details.guide.GuideDetailsRepository
import com.caravan.caravan.viewmodel.details.guide.GuideDetailsViewModel
import com.caravan.caravan.viewmodel.details.guide.GuideDetailsViewModelFactory
import com.stfalcon.imageviewer.StfalconImageViewer


class GuideDetailsFragment : BaseFragment() {
    private lateinit var guideDetailsBinding: FragmentGuideDetailsBinding
    private var guideId: String = "null"

    private var guideProfile: GuideProfile? = null

    private lateinit var viewModel: GuideDetailsViewModel

    private var page = 1
    private var allPages = 1

    private var isComment = false
    private val comments: ArrayList<Comment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            guideId = it.getString("guideId").toString()
        }

        setUpViewModel()

        viewModel.getGuideProfile(guideId)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        guideDetailsBinding = FragmentGuideDetailsBinding.inflate(layoutInflater)

        return guideDetailsBinding.root
    }

    private fun setUpObserves() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.guideProfile.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        guideDetailsBinding.apply {
                            llRoot.visibility = View.GONE
                        }
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        guideDetailsBinding.apply {
                            llRoot.visibility = View.VISIBLE
                        }
                        dismissLoading()
                        guideProfile = it.data
                        guideId = it.data.id
                        setData(it.data)
                        viewModel.getReviews(1, guideId)
                    }
                    is UiStateObject.ERROR -> {
                        guideDetailsBinding.apply {
                            llRoot.visibility = View.GONE
                        }
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    requireActivity().onBackPressed()
                                }
                            })
                    }
                    else -> Unit
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.hire.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:${guideProfile?.profile?.phoneNumber}")
                        requireActivity().startActivity(callIntent)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    return
                                }
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun setData(data: GuideProfile) {
        isComment = data.isComment
        guideDetailsBinding.apply {
            tvGuideFullName.text = data.profile.name.plus(" ").plus(data.profile.surname)
            tvGuideLanguages.text = setLanguages(data.languages)
            tvGuideDescription.text = data.biography
            ratingBarGuide.rating = data.rate.toFloat()
        }
        setProfilePhoto(data.profile.photo)
        setTravelLocations(data.travelLocations)
        guideDetailsBinding.tvGuidePrice.text = setPrice(data.price)
    }

    private fun setLanguages(languages: ArrayList<Language>): String {
        var text = ""
        for (language in 0..languages.size - 2) {
            text += "${
                languages[language].name.substring(
                    0,
                    1
                )
            }${languages[language].name.lowercase().substring(1)}"
            text += ","
        }
        text += languages[languages.size - 1].name.substring(
            0,
            1
        ) + languages[languages.size - 1].name.lowercase().substring(1)
        return text
    }

    private fun setUpViewModel() {
        viewModel = ViewModelProvider(
            this, GuideDetailsViewModelFactory(
                GuideDetailsRepository(
                    RetrofitHttp.createServiceWithAuth(
                        SharedPref(requireContext()), ApiService::class.java
                    )
                )
            )
        )[GuideDetailsViewModel::class.java]
    }


    private fun initViews() {

        guideDetailsBinding.apply {

            fragmentTripCommentsRV.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                nestedScroll.setOnScrollChangeListener { v, _, _, _, _ ->
                    if (etLeaveComment.isFocused) {
                        val outRect = Rect()
                        etLeaveComment.getGlobalVisibleRect(outRect)
                        etLeaveComment.clearFocus()
                        val imm: InputMethodManager =
                            v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }

            guideTrips.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("guideId", guideId)
                Navigation.findNavController(requireActivity(), R.id.details_nav_fragment)
                    .navigate(R.id.action_guideDetailsFragment_to_guideTrips, bundle)
            }

            guideProfilePhoto.setOnClickListener {
                guideProfile?.profile?.photo?.let {
                    StfalconImageViewer.Builder<String?>(
                        requireContext(),
                        arrayOf(guideProfile?.profile?.photo)
                    ) { view, _ ->
                        Glide.with(guideDetailsBinding.root).load(guideProfile?.profile?.photo)
                            .into(view)
                    }.withHiddenStatusBar(false)
                        .withDismissListener {
                        }.show()

                }
            }

            btnApplyGuide.setOnClickListener {
                viewModel.hire(Hire("GUIDE", guideId))
            }

            btnSendComment.setOnClickListener {
                if (etLeaveComment.text.isNotEmpty()) {
                    val rate = rateTheGuide.rating
                    val review =
                        Review(
                            rate.toInt(),
                            etLeaveComment.text.toString(),
                            "GUIDE",
                            null,
                            guideId
                        )

                    viewModel.postReview(review)

                } else {
                    toast(getString(R.string.str_send_message))
                }
            }

            llCallToGuide.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL)
                callIntent.data = Uri.parse("tel:${guideProfile?.profile?.phoneNumber}")
                requireActivity().startActivity(callIntent)
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObserves()
        setUpObservesReview()
        setUpObservesReviews()
        initViews()

    }

    private fun setUpObservesReview() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.review.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        setStatus(it.data)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    return
                                }
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }

    }

    private fun setStatus(data: ActionMessage) {
        if (!data.status) {
            showDialogWarning(data.title!!, data.message!!, object : OkInterface {
                override fun onClick() {
                }
            })
        } else {
            guideDetailsBinding.apply {
                etLeaveComment.setText("")
                leaveCommentPart.visibility = View.GONE

                viewModel.getReviews(1, guideId)

            }
        }
    }

    private fun setUpObservesReviews() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.reviews.collect {
                when (it) {
                    is UiStateObject.LOADING -> {
                        showLoading()
                    }
                    is UiStateObject.SUCCESS -> {
                        dismissLoading()
                        allPages = it.data.totalPage

                        comments.addAll(it.data.comments)

                        Log.d("TAG", "setUpObservesReviews: Gladi nichcha marta")

                        setCommentsRv(comments)
                        setLeaveCommentsPart(guideProfile?.attendances, isComment)
                    }
                    is UiStateObject.ERROR -> {
                        dismissLoading()
                        showDialogWarning(
                            getString(R.string.str_no_connection),
                            getString(R.string.str_try_again),
                            object : OkInterface {
                                override fun onClick() {
                                    return
                                }
                            }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun setPrice(price: Price): Spannable {
        val text = "${price.currency} ${price.cost.toInt()}"
        val endIndex = text.length

        val outPutColoredText: Spannable = SpannableString("$text/${price.type.lowercase()}")
        outPutColoredText.setSpan(RelativeSizeSpan(1.2f), 0, endIndex, 0)
        outPutColoredText.setSpan(
            ForegroundColorSpan(Color.parseColor("#167351")),
            0,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return outPutColoredText
    }

    private fun setLeaveCommentsPart(ids: ArrayList<ProfileId>?, isComment: Boolean) {
        val profileId = SharedPref(requireContext()).getString("profileId")

        if (ids != null && ids.contains(ProfileId(profileId!!)) && guideProfile?.id != SharedPref(requireContext()).getString("guideId")) {
            if (isComment)
                guideDetailsBinding.leaveCommentPart.visibility = View.GONE
            else
                guideDetailsBinding.leaveCommentPart.visibility = View.VISIBLE

        } else {
            guideDetailsBinding.leaveCommentPart.visibility = View.GONE
        }
    }

    private fun setProfilePhoto(photo: String?) {
        Glide.with(guideDetailsBinding.root)
            .load(photo)
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .into(guideDetailsBinding.guideProfilePhoto)

    }

    private fun setCommentsRv(reviews: ArrayList<Comment>?) {
        guideDetailsBinding.apply {

            if (fragmentTripCommentsRV.adapter == null) {
                fragmentTripCommentsRV.adapter = reviews?.let {
                    CommentsAdapter(it)
                }
            } else {
                if (!reviews.isNullOrEmpty()){
                    (fragmentTripCommentsRV.adapter as CommentsAdapter).items = reviews
                    fragmentTripCommentsRV.adapter!!.notifyDataSetChanged()
                }
            }

            nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (nestedScroll.getChildAt(nestedScroll.childCount - 1) != null) {
                    if (scrollY >= nestedScroll.getChildAt(nestedScroll.childCount - 1).measuredHeight - nestedScroll.measuredHeight &&
                        scrollY > oldScrollY
                    ) {
                        if (page + 1 <= allPages)
                            viewModel.getReviews(++page, guideId)
                    }
                }
            })
        }
    }

    private fun setTravelLocations(travelLocations: ArrayList<Location>) {
        guideDetailsBinding.apply {
            travelLocationsRV.adapter = TravelLocationsAdapter(travelLocations)
        }
    }

}