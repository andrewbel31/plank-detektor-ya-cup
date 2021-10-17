package com.andreibelous.plankdetektor.view.results

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andreibelous.plankdetektor.cast
import kotlin.math.ceil

class ResultsView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val itemsAdapter = Adapter(listOf())

    init {
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadius = context.dp(24f)
                outline.setRoundRect(
                    0,
                    0,
                    view.width,
                    (view.height + cornerRadius).toInt(),
                    cornerRadius
                )
            }
        }
        setBackgroundColor(Color.WHITE)
        adapter = itemsAdapter
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun bind(model: ResultsViewModel) {
        itemsAdapter.setItems(model.toItems())
    }

    private fun ResultsViewModel.toItems(): List<ResultListItem> {
        val items = mutableListOf<ResultListItem>()

        if (data.isEmpty()) {
            items.add(ResultListItem.EmptyItem)
        } else {
            items.add(ResultListItem.HeaderItem)
            data.forEach {
                items.add(
                    ResultListItem.PlankInfoItem(it)
                )
            }
        }
        return items
    }
}

sealed interface ResultListItem {

    object HeaderItem : ResultListItem

    data class PlankInfoItem(
        val attempt: StrAttempt
    ) : ResultListItem

    object EmptyItem : ResultListItem
}

private class Adapter(
    private var items: List<ResultListItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<ResultListItem>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            HEADER_VIEW_TYPE -> HeaderViewHolder(HeaderView(parent.context))
            PHASE_VIEW_TYPE -> PlankItemViewHolder(PlankInfoView(parent.context))
            EMPTY_VIEW_TYPE -> EmptyViewHolder(EmptyView(parent.context))
            else -> throw Exception("unsupported view type $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ResultListItem.HeaderItem -> Unit
            is ResultListItem.EmptyItem -> Unit
            is ResultListItem.PlankInfoItem -> holder.cast<PlankItemViewHolder>().bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].toViewType()

    override fun getItemCount(): Int = items.size

    private fun ResultListItem.toViewType() =
        when (this) {
            is ResultListItem.HeaderItem -> HEADER_VIEW_TYPE
            is ResultListItem.PlankInfoItem -> PHASE_VIEW_TYPE
            is ResultListItem.EmptyItem -> EMPTY_VIEW_TYPE
        }

    private class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class PlankItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(model: ResultListItem.PlankInfoItem) {
            itemView.cast<PlankInfoView>().bind(model.attempt)
        }
    }

    private class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private companion object {

        private const val HEADER_VIEW_TYPE = 1
        private const val PHASE_VIEW_TYPE = 2
        private const val EMPTY_VIEW_TYPE = 3
    }
}


data class ResultsViewModel(val data: List<StrAttempt>)

data class StrAttempt(
    val date: String,
    val duration: String
)

fun Context.dp(value: Float): Float {
    return if (value == 0f) {
        0f
    } else ceil(resources.displayMetrics.density * value)
}