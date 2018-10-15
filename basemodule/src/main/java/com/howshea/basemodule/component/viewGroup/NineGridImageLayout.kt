package com.howshea.basemodule.component.viewGroup

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Color
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.howshea.basemodule.R
import com.howshea.basemodule.component.view.RoundCornerImageView
import com.howshea.basemodule.utils.dp
import kotlin.math.ceil

/**
 * Created by Howshea
 * on 2018/9/4
 */
class NineGridImageLayout : ViewGroup {
    //行
    private var rowCount = 3
    //列
    private var columnCount = 3
    //间距
    var spacing = context.dp(6)
        set(value) {
            field = value
            requestLayout()
        }
    //单张图片最大宽高
    var singleImgSize = context.dp(210)
        set(value) {
            field = value
            requestLayout()
        }
    //单网格宽高
    private var gridSize = 0
    //url list
    private var imageList = arrayListOf<String>()
    //单图宽高比
    var radio = 0f

    private var itemClickListener: ((v: View, position: Int) -> Unit)? = null
    var loadImageListener: ((v: ImageView, url: String) -> Unit)? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val attributes = context?.obtainStyledAttributes(attrs, R.styleable.NineGridImageLayout)
        attributes?.apply {
            spacing = getDimension(R.styleable.NineGridImageLayout_spacing, spacing.toFloat()).toInt()
            singleImgSize = getDimension(R.styleable.NineGridImageLayout_singleSize, singleImgSize.toFloat()).toInt()
        }
        attributes?.recycle()

    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = width - paddingLeft - paddingRight
        gridSize = (availableWidth - spacing * 2) / 3
        val height =
            if (imageList.size == 1) {
                if (radio > 1)
                    (singleImgSize / radio).toInt() + paddingTop + paddingBottom
                else
                    singleImgSize + paddingTop + paddingBottom
            } else if (imageList.size > 1) {
                gridSize * rowCount + spacing * (rowCount - 1) + paddingTop + paddingBottom
            } else {
                MeasureSpec.getSize(heightMeasureSpec)
            }

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, p0: Int, p1: Int, p2: Int, p3: Int) {
        if (imageList.isEmpty())
            return
        var left: Int
        var top: Int
        var right: Int
        var bottom: Int
        when (imageList.size) {
            1 -> {
                val view = getChildAt(0) as RoundCornerImageView
                right = paddingLeft + view.layoutParams.width
                bottom = paddingTop + view.layoutParams.height
                view.layout(paddingLeft, paddingTop, right, bottom)
                view.setOnClickListener {
                    itemClickListener?.invoke(it, 0)
                }
                loadImageListener?.invoke(view, imageList[0])
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.transitionName = "Image 0"
                }
            }
            else -> {
                var row: Int
                var column: Int
                imageList.forEachIndexed { index, s ->
                    val view = getChildAt(index) as RoundCornerImageView

                    //图片数量为4的时候第二张需要换行
                    row = index / (if (imageList.size == 4) 2 else 3)

                    column = index % columnCount
                    left = (gridSize + spacing) * column + paddingLeft
                    top = (gridSize + spacing) * row + paddingTop
                    right = left + gridSize
                    bottom = top + gridSize
                    view.layout(left, top, right, bottom)
                    view.setOnClickListener {
                        itemClickListener?.invoke(it, index)
                    }
                    loadImageListener?.invoke(view, s)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.transitionName = "Image $index"
                    }
                }
            }
        }
    }


    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private fun View.addSystemView() {
        val vlp = this.layoutParams
        val lp: LayoutParams
        lp = if (vlp == null) {
            generateDefaultLayoutParams()
        } else if (!checkLayoutParams(vlp)) {
            generateLayoutParams(vlp) as LayoutParams
        } else {
            vlp as LayoutParams
        }
        addView(this, lp)
    }

    @SuppressLint("InflateParams")
    fun setData(imageList: List<String>, ratio: Float) {
        this.imageList = ArrayList(imageList)
        this.radio = ratio
        //清除所有子view ，避免 recyclerView 复用导致错乱问题
        removeAllViews()
        //行数
        rowCount = ceil(imageList.size / 3f).toInt()
        //列数
        columnCount = if (imageList.size == 4) 2 else 3
        if (imageList.size == 1) {
            RoundCornerImageView(context)
                .apply {
                    layoutParams = if (ratio > 1) {
                        val height = (singleImgSize / ratio).toInt()
                        val width = singleImgSize
                        LayoutParams(width, height)
                    } else {
                        val height = singleImgSize
                        val width = (singleImgSize * ratio).toInt()
                        LayoutParams(width, height)
                    }
                    borderColor = Color.parseColor("#DBDBDB")
                    borderWidth = dp(0.4f).toFloat()
                    radius = dp(3).toFloat()
                }
                .addSystemView()
        } else {
            imageList.forEach { _ ->
                RoundCornerImageView(context).apply {
                    borderColor = Color.parseColor("#DBDBDB")
                    borderWidth = dp(0.4f).toFloat()
                    radius = dp(3).toFloat()
                }.addSystemView()
            }
        }
        requestLayout()
    }

    fun onItemClick(click: (v: View, position: Int) -> Unit) {
        itemClickListener = click
    }
}

@BindingAdapter("app:imageList", "app:ratio")
fun setImageList(view: NineGridImageLayout, imageList: List<String>?, ratio: Float) {
    //如果为空或者长度为0，就什么都不做
    imageList?.isNotEmpty()?.let {
        if (imageList.size > 9)
        //最多九张
            view.setData(imageList.subList(0, 8), ratio)
        else
            view.setData(imageList, ratio)
    }
}