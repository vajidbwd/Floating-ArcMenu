package com.bvapp.arcmenulibrary;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Mohsen on 2/4/2017.
 */

public class FloatingActionButton extends ImageButton {
	private static final int TRANSLATE_DURATION_MILLIS = 200;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({TYPE_NORMAL, TYPE_MINI})
	public @interface TYPE {
	}

	public static final int TYPE_LARGE = 0;
	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_MINI = 2;

	private boolean mVisible;

	private int mColorNormal;
	private int mColorPressed;
	private int mColorRipple;
	private int mColorDisabled;
	private boolean mShadow;
	private int mType;

	private int mShadowSize;

	private boolean mMarginsSet;

	private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

	public FloatingActionButton(Context context) {
		this(context, null);
	}

	public FloatingActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int size = 0;
		switch (mType){
			case TYPE_LARGE:
				size = getDimension(R.dimen.fab_size_large);
				break;
			case TYPE_NORMAL:
				size = getDimension(R.dimen.fab_size_normal);
				break;
			case TYPE_MINI:
				size = getDimension(R.dimen.fab_size_mini);
				break;
			default:
				size = getDimension(R.dimen.fab_size_normal);
		}
		if (mShadow && !hasLollipopApi()) {
			size += mShadowSize * 2;
			setMarginsWithoutShadow();
		}
		setMeasuredDimension(size, size);
	}

	@SuppressLint("NewApi")
	private void init(Context context, AttributeSet attributeSet) {
		mVisible = true;
		mColorNormal = Color.BLUE;
		mColorPressed = darkenColor(mColorNormal);
		mColorRipple = lightenColor(mColorNormal);
		mColorDisabled = getColor(android.R.color.darker_gray);
		mType = TYPE_NORMAL;
		mShadow = true;
		mShadowSize = getDimension(R.dimen.fab_shadow_size);
		if (hasLollipopApi()) {
			StateListAnimator stateListAnimator = AnimatorInflater.loadStateListAnimator(context,
					R.anim.fab_press_elevation);
			setStateListAnimator(stateListAnimator);
		}
		if (attributeSet != null) {
			initAttributes(context, attributeSet);
		}
		updateBackground();
	}

	private void initAttributes(Context context, AttributeSet attributeSet) {
		TypedArray attr = getTypedArray(context, attributeSet, R.styleable.FloatingActionButton);
		if (attr != null) {
			try {
				mColorNormal = Color.BLUE;
				mColorPressed = attr.getColor(R.styleable.FloatingActionButton_fab_colorPressed,
						darkenColor(mColorNormal));
				mColorRipple = attr.getColor(R.styleable.FloatingActionButton_fab_colorRipple,
						lightenColor(mColorNormal));
				mColorDisabled = attr.getColor(R.styleable.FloatingActionButton_fab_colorDisabled,
						mColorDisabled);
				mShadow = attr.getBoolean(R.styleable.FloatingActionButton_fab_shadow, true);
				mType = attr.getInt(R.styleable.FloatingActionButton_fab_type, TYPE_NORMAL);
			} finally {
				attr.recycle();
			}
		}
	}

	private void updateBackground() {
		StateListDrawable drawable = new StateListDrawable();
		drawable.addState(new int[]{android.R.attr.state_pressed}, createDrawable(mColorPressed));
		drawable.addState(new int[]{-android.R.attr.state_enabled}, createDrawable(mColorDisabled));
		drawable.addState(new int[]{}, createDrawable(mColorNormal));
		setBackgroundCompat(drawable);
	}

	private Drawable createDrawable(int color) {
		OvalShape ovalShape = new OvalShape();
		ShapeDrawable shapeDrawable = new ShapeDrawable(ovalShape);
		shapeDrawable.getPaint().setColor(color);

		if (mShadow && !hasLollipopApi()) {
			Drawable shadowDrawable = getResources().getDrawable(mType == TYPE_NORMAL ? R.drawable.fab_shadow
					: R.drawable.fab_shadow_mini);
			LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{shadowDrawable, shapeDrawable});
			layerDrawable.setLayerInset(1, mShadowSize, mShadowSize, mShadowSize, mShadowSize);
			return layerDrawable;
		} else {
			return shapeDrawable;
		}
	}

	private TypedArray getTypedArray(Context context, AttributeSet attributeSet, int[] attr) {
		return context.obtainStyledAttributes(attributeSet, attr, 0, 0);
	}

	private int getColor(@ColorRes int id) {
		return getResources().getColor(id);
	}

	private int getDimension(@DimenRes int id) {
		return getResources().getDimensionPixelSize(id);
	}

	private void setMarginsWithoutShadow() {
		if (!mMarginsSet) {
			if (getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
				ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
				int leftMargin = layoutParams.leftMargin - mShadowSize;
				int topMargin = layoutParams.topMargin - mShadowSize;
				int rightMargin = layoutParams.rightMargin - mShadowSize;
				int bottomMargin = layoutParams.bottomMargin - mShadowSize;
				layoutParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

				requestLayout();
				mMarginsSet = true;
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundCompat(Drawable drawable) {
		if (hasLollipopApi()) {
			float elevation;
			if (mShadow) {
				elevation = getElevation() > 0.0f ? getElevation()
						: getDimension(R.dimen.fab_elevation_lollipop);
			} else {
				elevation = 0.0f;
			}
			setElevation(elevation);
			RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{{}},
					new int[]{mColorRipple}), drawable, null);
			setOutlineProvider(new ViewOutlineProvider() {
				@Override
				public void getOutline(View view, Outline outline) {
					int size = getDimension(mType == TYPE_NORMAL ? R.dimen.fab_size_normal
							: R.dimen.fab_size_mini);
					outline.setOval(0, 0, size, size);
				}
			});
			setClipToOutline(true);
			setBackground(rippleDrawable);
		} else if (hasJellyBeanApi()) {
			setBackground(drawable);
		} else {
			setBackgroundDrawable(drawable);
		}
	}

	private int getMarginBottom() {
		int marginBottom = 0;
		final ViewGroup.LayoutParams layoutParams = getLayoutParams();
		if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
			marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
		}
		return marginBottom;
	}

	public void setColorNormal(int color) {
		if (color != mColorNormal) {
			mColorNormal = color;
			updateBackground();
		}
	}

	public void setColorNormalResId(@ColorRes int colorResId) {
		setColorNormal(getColor(colorResId));
	}

	public int getColorNormal() {
		return mColorNormal;
	}

	public void setColorPressed(int color) {
		if (color != mColorPressed) {
			mColorPressed = color;
			updateBackground();
		}
	}

	public void setColorPressedResId(@ColorRes int colorResId) {
		setColorPressed(getColor(colorResId));
	}

	public int getColorPressed() {
		return mColorPressed;
	}

	public void setColorRipple(int color) {
		if (color != mColorRipple) {
			mColorRipple = color;
			updateBackground();
		}
	}

	public void setColorRippleResId(@ColorRes int colorResId) {
		setColorRipple(getColor(colorResId));
	}

	public int getColorRipple() {
		return mColorRipple;
	}

	public void setShadow(boolean shadow) {
		if (shadow != mShadow) {
			mShadow = shadow;
			updateBackground();
		}
	}

	public void setFabSize(int type) {
		if (type != mType) {
			mType = type;
			requestLayout();
		}
	}

	public int getEleationSize() {
		return mShadowSize;
	}

	public void setCompatElevation(int size) {
		if(size > 0){
			mShadow = true;
			if (size != mShadowSize) {
				mShadowSize = size;
			}
		}else if(size == 0){
			mShadow = false;
			mShadowSize = 0;
		}
		//requestLayout();
		updateBackground();
	}

	public boolean hasShadow() {
		return mShadow;
	}

	public void setType(@TYPE int type) {
		if (type != mType) {
			mType = type;
			updateBackground();
		}
	}

	@TYPE
	public int getType() {
		return mType;
	}

	public boolean isVisible() {
		return mVisible;
	}

	private boolean hasLollipopApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	private boolean hasJellyBeanApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
	}

	private boolean hasHoneycombApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private static int darkenColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 0.9f;
		return Color.HSVToColor(hsv);
	}

	private static int lightenColor(int color) {
		float[] hsv = new float[3];
		Color.colorToHSV(color, hsv);
		hsv[2] *= 1.1f;
		return Color.HSVToColor(hsv);
	}

}