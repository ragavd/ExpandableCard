package `in`.ragavd.expandablecard

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.layout_expandablecard.view.*

class ExpandableCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var startingAngle: Float = ROTATE_ANGLE_START
    private var isExpanded: Boolean? = false
    private var cardTitleText: String? = null
    private var cardIconDrawable: Drawable? = null
    private var isExpandableCard: Boolean? = true
    private var cardContentView: View? = null
    private var cardContentViewId: Int? = null

    var onExpandListener: (() -> Unit)? = null
    var onClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_expandablecard, this)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableCard)
            cardTitleText = typedArray.getString(R.styleable.ExpandableCard_cardTitle)
            cardIconDrawable = typedArray.getDrawable(R.styleable.ExpandableCard_cardIcon)
            isExpandableCard =
                typedArray.getBoolean(R.styleable.ExpandableCard_isCardExpandable, true)
            isExpanded = typedArray.getBoolean(R.styleable.ExpandableCard_isCardExpanded, false)
            isExpandableCard?.let { isExpandable ->
                if (isExpandable) {
                    cardContentViewId =
                        typedArray.getResourceId(R.styleable.ExpandableCard_cardContent, View.NO_ID)
                }
            }
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        cardTitle.text = cardTitleText
        cardIconDrawable?.let {
            ViewCompat.setBackground(cardIcon, it)
        }
        isExpandableCard?.let { isExpandable ->
            if (isExpandable) {
                setupContentView()
                cardTitleGroup.setOnClickListener {
                    toggleCardContent()
                }
                cardIcon.setOnClickListener {
                    toggleCardContent()
                }
            } else {
                cardTitleGroup.setOnClickListener {
                    onClickListener?.invoke()
                }
                cardIcon.setOnClickListener {
                    onClickListener?.invoke()
                }
            }
        }
    }

    private fun setupContentView() {
        cardContentViewId?.let {
            cardStub.layoutResource = it
            cardContentView = cardStub.inflate()
            isExpanded?.let { isExpanded ->
                cardContentView?.apply {
                    visibility = if (isExpanded) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun showContent() {
        startingAngle = ROTATE_ANGLE_START
        expand()
        rotateIcon()
    }

    fun hideContent() {
        cardContentView?.apply {
            if (visibility == View.VISIBLE) {
                startingAngle = ROTATE_ANGLE_END
                collapse()
                rotateIcon()
            }
        }
    }

    private fun rotateIcon() {
        ObjectAnimator.ofFloat(cardIcon, View.ROTATION, startingAngle, startingAngle + ROTATE_ANGLE_END)
            .apply {
                duration =
                    ROTATE_ANIMATION_DURATION
                start()
            }
    }

    private fun toggleCardContent() {
        cardContentView?.apply {
            if (visibility == View.VISIBLE) {
                hideContent()
            } else {
                onExpandListener?.invoke()
                showContent()
            }
        }
    }


    private fun expand() {
        cardContentView?.apply {
            val matchParentMeasureSpec = MeasureSpec.makeMeasureSpec(
                (parent as View).width, MeasureSpec.EXACTLY
            )
            val wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(
                0,
                MeasureSpec.UNSPECIFIED
            )
            measure(matchParentMeasureSpec, wrapContentMeasureSpec)
            val targetHeight = measuredHeight
            layoutParams.height = 1
            visibility = View.VISIBLE
            val animation: Animation = object : Animation() {
                override fun applyTransformation(
                    interpolatedTime: Float,
                    t: Transformation
                ) {
                    layoutParams.height =
                        if (interpolatedTime == 1f) LayoutParams.WRAP_CONTENT else (targetHeight * interpolatedTime).toInt()
                    requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            animation.duration =
                (targetHeight / context.resources
                    .displayMetrics.density).toLong()
            startAnimation(animation)
        }
    }


    private fun collapse() {
        cardContentView?.apply {
            val initialHeight = measuredHeight
            val animation: Animation = object : Animation() {
                override fun applyTransformation(
                    interpolatedTime: Float,
                    t: Transformation
                ) {
                    if (interpolatedTime == 1f) {
                        visibility = View.GONE
                    } else {
                        layoutParams.height =
                            initialHeight - (initialHeight * interpolatedTime).toInt()
                        requestLayout()
                    }
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }
            animation.duration =
                (initialHeight / context.resources.displayMetrics.density).toLong()
            startAnimation(animation)
        }
    }

    private companion object {
        private const val ROTATE_ANGLE_END: Float = 180f
        private const val ROTATE_ANGLE_START: Float = 0f
        private const val ROTATE_ANIMATION_DURATION: Long = 300
    }

}