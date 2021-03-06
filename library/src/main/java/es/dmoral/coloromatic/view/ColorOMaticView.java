package es.dmoral.coloromatic.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.coloromatic.ColorOMaticUtil;
import es.dmoral.coloromatic.IndicatorMode;
import es.dmoral.coloromatic.R;
import es.dmoral.coloromatic.colormode.Channel;
import es.dmoral.coloromatic.colormode.ColorMode;

/**
 * Color-O-Matic
 * Copyright (C) 2016 - GrenderG
 *
 * This file is part of Color-O-Matic.
 *
 * Color-O-Matic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Color-O-Matic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Color-O-Matic.  If not, see <http://www.gnu.org/licenses/>.
 */

public class ColorOMaticView extends RelativeLayout {

    public final static int DEFAULT_COLOR = Color.GRAY;
    public final static ColorMode DEFAULT_MODE = ColorMode.RGB;
    public final static IndicatorMode DEFAULT_INDICATOR = IndicatorMode.DECIMAL;
    public final static boolean DEFAULT_TEXT_INDICATOR_STATE = false;

    private final ColorMode colorMode;
    private
    @ColorInt
    int currentColor;
    private IndicatorMode indicatorMode;
    private boolean showTextIndicator;

    public ColorOMaticView(Context context) {
        this(DEFAULT_COLOR, DEFAULT_TEXT_INDICATOR_STATE, DEFAULT_MODE, DEFAULT_INDICATOR, context);
    }

    public ColorOMaticView(@ColorInt int initialColor, ColorMode colorMode, Context context) {
        this(initialColor, false, colorMode, DEFAULT_INDICATOR, context);
    }

    public ColorOMaticView(@ColorInt int initialColor, boolean showTextIndicator, ColorMode colorMode, IndicatorMode indicatorMode, Context context) {
        super(context);
        this.indicatorMode = indicatorMode;
        this.colorMode = colorMode;
        this.currentColor = initialColor;
        this.showTextIndicator = showTextIndicator;
        init();
    }

    void init() {
        inflate(getContext(), R.layout.chroma_view, this);
        setClipToPadding(false);

        final View colorView = findViewById(R.id.color_view);
        colorView.setBackgroundColor(currentColor);
        final TextView colorTextIndicator = (TextView) findViewById(R.id.tv_color_indicator);
        if (showTextIndicator)
            colorTextIndicator.setVisibility(View.VISIBLE);

        List<Channel> channels = colorMode.getColorMode().getChannels();
        final List<ChannelView> channelViews = new ArrayList<>();
        for (Channel c : channels) {
            channelViews.add(new ChannelView(c, currentColor, getContext()));
        }

        updateText(colorView, colorTextIndicator, channelViews, channels);

        ChannelView.OnProgressChangedListener seekBarChangeListener = new ChannelView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged() {
                List<Channel> channels = new ArrayList<>();
                for (ChannelView chan : channelViews)
                    channels.add(chan.getChannel());
                updateText(colorView, colorTextIndicator, channelViews, channels);
            }
        };

        ViewGroup channelContainer = (ViewGroup) findViewById(R.id.channel_container);
        for (ChannelView c : channelViews) {
            channelContainer.addView(c);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) c.getLayoutParams();
            params.topMargin =
                    getResources().getDimensionPixelSize(R.dimen.channel_view_margin_top);
            params.bottomMargin =
                    getResources().getDimensionPixelSize(R.dimen.channel_view_margin_bottom);

            c.registerListener(seekBarChangeListener);
        }
    }

    private void updateText(View colorView, TextView colorTextIndicator,
                            List<ChannelView> channelViews, List<Channel> channels) {
        currentColor = colorMode.getColorMode().evaluateColor(channels);
        colorView.setBackgroundColor(currentColor);

        if (indicatorMode == IndicatorMode.HEX)
            colorTextIndicator.setText(ColorOMaticUtil.getFormattedColorString(currentColor, colorMode == ColorMode.ARGB));
        else {
            String decText = "";
            for (ChannelView chan : channelViews)
                decText += chan.getChannel().getProgress() + " ";
            colorTextIndicator.setText(String.valueOf(decText.trim()));
        }

        if (!getResources().getBoolean(R.bool.tablet_mode))
            colorTextIndicator.setTextColor(getInverseColor(currentColor));
    }

    // Based on http://stackoverflow.com/a/5761067/4208583
    private int getInverseColor(int color) {
        return Color.argb(Color.alpha(color), 255 - Color.red(color), 255 -
                Color.green(color), 255 - Color.blue(color));
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public IndicatorMode getIndicatorMode() {
        return indicatorMode;
    }

    public boolean isShowTextIndicator() {
        return showTextIndicator;
    }

    public void enableButtonBar(final ButtonBarListener listener) {
        LinearLayout buttonBar = (LinearLayout) findViewById(R.id.button_bar);
        TextView positiveButton = (TextView) buttonBar.findViewById(R.id.positive_button);
        TextView negativeButton = (TextView) buttonBar.findViewById(R.id.negative_button);

        if (listener != null) {
            buttonBar.setVisibility(VISIBLE);
            positiveButton.setTextColor(ColorOMaticUtil.getThemeAccentColor(getContext()));
            negativeButton.setTextColor(ColorOMaticUtil.getThemeAccentColor(getContext()));
            positiveButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onPositiveButtonClick(currentColor);
                }
            });

            negativeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onNegativeButtonClick();
                }
            });
        } else {
            buttonBar.setVisibility(GONE);
            positiveButton.setOnClickListener(null);
            negativeButton.setOnClickListener(null);
        }
    }

    public interface ButtonBarListener {
        void onPositiveButtonClick(int color);

        void onNegativeButtonClick();
    }
}
