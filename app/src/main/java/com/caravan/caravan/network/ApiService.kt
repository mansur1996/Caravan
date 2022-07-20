package com.caravan.caravan.network

import com.caravan.caravan.model.*
import com.caravan.caravan.model.auth.LoginRespond
import com.caravan.caravan.model.auth.LoginSend
import com.caravan.caravan.model.auth.RegisterRespond
import com.caravan.caravan.model.auth.RegisterSend
import com.caravan.caravan.model.create_trip.FirstSend
import com.caravan.caravan.model.create_trip.PhotoRespond
import com.caravan.caravan.model.create_trip.SecondSend
import com.caravan.caravan.model.create_trip.TripUploadPhoto
import com.caravan.caravan.model.hire.Hire
import com.caravan.caravan.model.home.HomeRespond
import com.caravan.caravan.model.home.HomeTrip
import com.caravan.caravan.model.more.ActionMessage
import com.caravan.caravan.model.review.Answer
import com.caravan.caravan.model.review.Review
import com.caravan.caravan.model.review.ReviewsByPagination
import com.caravan.caravan.model.search.SearchGuideResponse
import com.caravan.caravan.model.search.SearchGuideSend
import com.caravan.caravan.model.search.SearchTripResponse
import com.caravan.caravan.model.search.SearchTripSend
import com.caravan.caravan.model.upgrade.UpgradeSend
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Login
    @POST("/api/v1/auth/send")
    suspend fun sendSmsCode(@Body loginSend: LoginSend): Response<ActionMessage>

    @POST("/api/v1/auth/login")
    suspend fun checkSmsCode(@Body loginSend: LoginSend): Response<LoginRespond>

    @POST("/api/v1/auth/registration")
    suspend fun registerUser(@Body registerSend: RegisterSend): Response<RegisterRespond>

    // Profile
    @GET("/api/v1/profile/{profileId}")
    suspend fun getProfile(@Path("profileId") profileId: String): Response<Profile>

    @PUT("/api/v1/profile")
    suspend fun updateProfile(@Body profile: Profile): Response<Profile>

    @POST("/api/v1/guide")
    suspend fun upgradeToGuide(@Body upgradeSend: UpgradeSend): GuideProfile

    @GET("/api/v1/profile/language")
    suspend fun getAppLanguage(): Response<String>

    @PUT("/api/v1/profile/language")
    suspend fun updateAppLanguage(@Query("lang") lang: String): Response<ActionMessage>

    @DELETE("/api/v1/profile")
    suspend fun deleteAccount(): Response<ActionMessage>

    // Guide Account
    @GET("/api/v1/guide/{guideId}")
    suspend fun getGuideProfile(@Path("guideId") guideId: String): Response<GuideProfile>

    @PUT("/api/v1/guide")
    suspend fun updateGuideProfile(@Body guideProfile: UpgradeSend): Response<ActionMessage>

    @DELETE("/api/v1/guide")
    suspend fun deleteGuideProfile(): Response<ActionMessage>

    @GET("/api/v1/guide/status")
    suspend fun getGuideStatus(): Response<Boolean>

    @PUT("/api/v1/guide/status")
    suspend fun changeGuideStatus(): Response<Boolean>

    // Trip
    @POST("/api/v1/trip")
    suspend fun createTrip(@Body firstSend: FirstSend): Response<FirstSend> // Trip Id

    @GET("/api/v1/trip/{tripId}")
    suspend fun getTrip(@Path("tripId") tripId: String): Response<Trip>

    @GET("/api/v1/trip/{guideId}/trips")
    suspend fun getMyTrips(
        @Path("guideId") guideId: String,
        @Query("page") page: Int
    ): Response<TripRes>

    @DELETE("/api/v1/trip/{tripId}")
    suspend fun deleteTrip(@Path("tripId") tripId: String): Response<ActionMessage>

    @GET("api/v1/trip/trip-photo/{tripId}")
    suspend fun getTripPhoto(@Path("tripId") tripId: String) : Response<ArrayList<TourPhoto>>

    @Multipart
    @POST("/api/v1/attach/upload")
    suspend fun uploadPhoto(@Part image: MultipartBody.Part): Response<PhotoRespond>

    @DELETE("/api/v1/attach/delete/{id}")
    suspend fun deletePhoto(@Path("id") id:String):Response<Boolean>

    @PUT("/api/v1/trip/photo/{tripId}")
    suspend fun completeTrip(
        @Path("tripId") tripId: String,
        @Body secondSend: SecondSend
    ): Response<ActionMessage>

    @POST("/api/v1/trip/trip-upload")
    suspend fun uploadTripPhoto(@Body tripPhoto: TripUploadPhoto): Response<TripUploadPhoto>

    @Multipart
    @PUT("/api/v1/profile/photo")
    suspend fun uploadProfilePhoto(@Part image: MultipartBody.Part): Response<PhotoRespond>

    @GET("/api/v1/district/{region}")
    suspend fun getDistrict(@Path("region") region: String): Response<ArrayList<String>>

    // Review
    @POST("/api/v1/review")
    suspend fun postReview(@Body review: Review): Response<ActionMessage>

    @POST("/api/v1/review/answer")
    suspend fun answerReview(@Body answer: Answer) : Response<ActionMessage>

    @GET("/api/v1/review/trip-review/{tripId}")
    suspend fun getTripComments(
        @Path("tripId") tripId: String,
        @Query("page") page: Int
    ): Response<ReviewsByPagination>

    @GET("/api/v1/review/{guideId}/guide-feedback")
    suspend fun getGuideComments(
        @Path("guideId") guideId: String,
        @Query("page") page: Int
    ): Response<ReviewsByPagination>

    @GET("/api/v1/review/all")
    suspend fun getGuidesAllComments(@Query("page") page: Int): Response<ReviewsByPagination>

    // Search
    @POST("/api/v1/trip/search")
    suspend fun searchTrip(
        @Query("page") page: Int,
        @Body searchTripSend: SearchTripSend
    ): Response<SearchTripResponse>

    @POST("/api/v1/guide/search")
    suspend fun searchGuide(
        @Query("page") page: Int,
        @Body searchGuideSend: SearchGuideSend
    ): Response<SearchGuideResponse>

    // Home
    @GET("/api/v1/main")
    suspend fun getHomeData(): Response<HomeRespond>

    // Hiring
    @POST("/api/v1/attendances")
    suspend fun attend(@Body hire: Hire): Response<ActionMessage>

}