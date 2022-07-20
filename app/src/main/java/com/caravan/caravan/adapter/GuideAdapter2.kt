package com.caravan.caravan.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.caravan.caravan.R
import com.caravan.caravan.databinding.ItemGuideBinding
import com.caravan.caravan.model.Language
import com.caravan.caravan.model.search.SearchGuide
import com.caravan.caravan.ui.fragment.BaseFragment

class GuideAdapter2(var context: BaseFragment, var items: ArrayList<SearchGuide>) :
    ListAdapter<SearchGuide, GuideAdapter2.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<SearchGuide>() {
        override fun areItemsTheSame(oldItem: SearchGuide, newItem: SearchGuide): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchGuide, newItem: SearchGuide): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val itemGuideBinding: ItemGuideBinding) :
        RecyclerView.ViewHolder(itemGuideBinding.root) {

        fun onBind(guide: SearchGuide) {

            Glide.with(itemGuideBinding.ivGuide).load(guide.profilePhoto)
                .placeholder(R.drawable.user)
                .into(itemGuideBinding.ivGuide)
            itemGuideBinding.tvGuidesFullname.text =
                guide.name.plus(" ").plus(guide.surname)
            itemGuideBinding.tvGuidesCities.text = provinces(guide)
            itemGuideBinding.tvGuidePrice.text = price(guide)
            itemGuideBinding.tvGuidesLanguages.text = getLanguages(guide.languages)
            itemGuideBinding.ratingBarGuide.rating = guide.rate.toFloat()
            itemGuideBinding.tvGuidesCommentsCount.text =
                "(".plus(guide.reviewCount.toString()).plus(")")


            itemView.setOnClickListener {
                context.goToDetailsActivity(guide)
            }

        }

        private fun getLanguages(languages: ArrayList<Language>?): String {
            var text = ""
            try {
                for (language in languages!!) {
                    text += "${language.name}, "
                }
                return text.substring(0, text.length - 2)
            } catch (e: Exception) {
                text = ""
            }
            return text
        }

        private fun price(guide: SearchGuide): Spannable {
            val text = "${guide.price.currency} ${guide.price.cost.toInt()}"
            val endIndex = text.length

            val outPutColoredText: Spannable =
                SpannableString("$text/${guide.price.type.lowercase()}")
            outPutColoredText.setSpan(RelativeSizeSpan(1.2f), 0, endIndex, 0)
            outPutColoredText.setSpan(
                ForegroundColorSpan(Color.parseColor("#167351")),
                0,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            return outPutColoredText
        }

        private fun provinces(guide: SearchGuide): Spannable {
            var text = ""
            try {
                for (province in guide.travelLocations) {
                    if (!text.contains(province.district)) {
                        text += province.district.plus(", ")
                    }
                }
                text = text.substring(0, text.length - 2)
                return colorMyText(text, 0, text.length, "#167351")
            } catch (e: Exception) {
                text = ""
            }

            return colorMyText(text, 0, text.length, "#167351")
        }

        private fun colorMyText(
            inputText: String,
            startIndex: Int,
            endIndex: Int,
            textColor: String
        ): Spannable {
            val outPutColoredText: Spannable = SpannableString(inputText)
            outPutColoredText.setSpan(
                Color.parseColor(textColor),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return outPutColoredText
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemGuideBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(currentList[position])

    }

    override fun getItemCount(): Int = currentList.size
}