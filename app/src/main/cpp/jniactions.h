#ifndef JNIACTIONS_H
#define JNIACTIONS_H

namespace  JniActions{

    enum MotionEvent {
        /**
      * Constant for {@link #getActionMasked}: A pressed gesture has started, the
      * motion contains the initial starting location.
      * <p>
      * This is also a good time to check the button state to distinguish
      * secondary and tertiary button clicks and handle them appropriately.
      * Use {@link #getButtonState} to retrieve the button state.
      * </p>
      */
         ACTION_DOWN             = 0,

        /**
         * Constant for {@link #getActionMasked}: A pressed gesture has finished, the
         * motion contains the final release location as well as any intermediate
         * points since the last down or move event.
         */
          ACTION_UP               = 1,

        /**
         * Constant for {@link #getActionMasked}: A change has happened during a
         * press gesture (between {@link #ACTION_DOWN} and {@link #ACTION_UP}).
         * The motion contains the most recent point, as well as any intermediate
         * points since the last down or move event.
         */
          ACTION_MOVE             = 2,

        /**
         * Constant for {@link #getActionMasked}: The current gesture has been aborted.
         * You will not receive any more points in it.  You should treat this as
         * an up event, but not perform any action that you normally would.
         */
          ACTION_CANCEL           = 3,

        /**
         * Constant for {@link #getActionMasked}: A movement has happened outside of the
         * normal bounds of the UI element.  This does not provide a full gesture,
         * but only the initial location of the movement/touch.
         * <p>
         * Note: Because the location of any event will be outside the
         * bounds of the view hierarchy, it will not get dispatched to
         * any children of a ViewGroup by default. Therefore,
         * movements with ACTION_OUTSIDE should be handled in either the
         * root {@link View} or in the appropriate {@link Window.Callback}
         * (e.g. {@link android.app.Activity} or {@link android.app.Dialog}).
         * </p>
         */
         ACTION_OUTSIDE          = 4,

        /**
         * Constant for {@link #getActionMasked}: A non-primary pointer has gone down.
         * <p>
         * Use {@link #getActionIndex} to retrieve the index of the pointer that changed.
         * </p><p>
         * The index is encoded in the {@link #ACTION_POINTER_INDEX_MASK} bits of the
         * unmasked action returned by {@link #getAction}.
         * </p>
         */
        ACTION_POINTER_DOWN     = 5,

        /**
         * Constant for {@link #getActionMasked}: A non-primary pointer has gone up.
         * <p>
         * Use {@link #getActionIndex} to retrieve the index of the pointer that changed.
         * </p><p>
         * The index is encoded in the {@link #ACTION_POINTER_INDEX_MASK} bits of the
         * unmasked action returned by {@link #getAction}.
         * </p>
         */
        ACTION_POINTER_UP       = 6,

        /**
         * Constant for {@link #getActionMasked}: A change happened but the pointer
         * is not down (unlike {@link #ACTION_MOVE}).  The motion contains the most
         * recent point, as well as any intermediate points since the last
         * hover move event.
         * <p>
         * This action is always delivered to the window or view under the pointer.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_HOVER_MOVE       = 7,

        /**
         * Constant for {@link #getActionMasked}: The motion event contains relative
         * vertical and/or horizontal scroll offsets.  Use {@link #getAxisValue(int)}
         * to retrieve the information from {@link #AXIS_VSCROLL} and {@link #AXIS_HSCROLL}.
         * The pointer may or may not be down when this event is dispatched.
         * <p>
         * This action is always delivered to the window or view under the pointer, which
         * may not be the window or view currently touched.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_SCROLL           = 8,

        /**
         * Constant for {@link #getActionMasked}: The pointer is not down but has entered the
         * boundaries of a window or view.
         * <p>
         * This action is always delivered to the window or view under the pointer.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_HOVER_ENTER      = 9,

        /**
         * Constant for {@link #getActionMasked}: The pointer is not down but has exited the
         * boundaries of a window or view.
         * <p>
         * This action is always delivered to the window or view that was previously under the pointer.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_HOVER_EXIT       = 10,

        /**
         * Constant for {@link #getActionMasked}: A button has been pressed.
         *
         * <p>
         * Use {@link #getActionButton()} to get which button was pressed.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_BUTTON_PRESS   = 11,

        /**
         * Constant for {@link #getActionMasked}: A button has been released.
         *
         * <p>
         * Use {@link #getActionButton()} to get which button was released.
         * </p><p>
         * This action is not a touch event so it is delivered to
         * {@link View#onGenericMotionEvent(MotionEvent)} rather than
         * {@link View#onTouchEvent(MotionEvent)}.
         * </p>
         */
         ACTION_BUTTON_RELEASE  = 12,
    };
}
#endif

