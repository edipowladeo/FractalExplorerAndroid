package com.exploradordefractais.android

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.annotation.RequiresApi
import androidx.core.view.GestureDetectorCompat
@RequiresApi(Build.VERSION_CODES.N)
class MyGLSurfaceView(context: Context, attrs:AttributeSet)  :
           GLSurfaceView(context,attrs),
           GestureDetector.OnGestureListener,
           GestureDetector.OnDoubleTapListener{

    private val DEBUG_TAG = "Gestos: "
    private var detectorDeGestos: GestureDetectorCompat

    val myRenderer:MyRenderer
    init {
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2)

        myRenderer = MyRenderer(context)
        setRenderer(myRenderer)

        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        detectorDeGestos = GestureDetectorCompat(context, this)
        detectorDeGestos.setOnDoubleTapListener(this)
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var mScaleFactor = 1f
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mScaleFactor *= detector.scaleFactor
            invalidate()
            return true
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)
        myRenderer.fractalJanela?.modificarMagnificacaoCamera(scaleListener.mScaleFactor)
        scaleListener.mScaleFactor = 1f; // Reseta
        return if (detectorDeGestos.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        ////Log.d(DEBUG_TAG, "onScroll: $event1 $event2")
        myRenderer.fractalJanela?.moverCamera(distanceX,distanceY)
        return true
    }


    override fun onDown(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onDown: $event")
        return true
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //Log.d(DEBUG_TAG, "onFling: $event1 $event2")
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        //Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onShowPress(event: MotionEvent) {
        //Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onDoubleTap: $event")
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onSingleTapConfirmed: $event")
        return true
    }
}

