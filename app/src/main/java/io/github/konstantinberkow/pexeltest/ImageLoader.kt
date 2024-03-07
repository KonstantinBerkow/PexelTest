package io.github.konstantinberkow.pexeltest

import android.widget.ImageView

interface ImageLoader<R> {

    fun loadImage(url: String, target: ImageView): R

    fun cancelRequest(request: R)
}
