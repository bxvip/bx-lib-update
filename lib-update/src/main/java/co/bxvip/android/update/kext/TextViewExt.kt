package co.bxvip.android.update.kext

import android.widget.TextView
import co.bxvip.android.update.utils.ColorUtil
import co.bxvip.android.update.utils.DrawableUtil

/**
 * Created by Vector
 * on 2017/7/18 0018.
 */

fun TextView.setSolidTheme(color: Int = ColorUtil.getRandomColor(), strokeWidth: Int = 6, cornerRadius: Int = 10) {
    DrawableUtil.setTextSolidTheme(this, strokeWidth, cornerRadius, color)
}

fun TextView.setStrokeTheme(color: Int = ColorUtil.getRandomColor(), strokeWidth: Int = 6, cornerRadius: Int = 10) {
    DrawableUtil.setTextStrokeTheme(this, strokeWidth, cornerRadius, color)
}
