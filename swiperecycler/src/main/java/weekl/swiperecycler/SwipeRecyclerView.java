package weekl.swiperecycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class SwipeRecyclerView extends RecyclerView {

    //记录屏幕触摸位置
    private float startX, lastX, lastY;

    //记录滑动位置
    private float currX;
    //记录菜单展开状态
    private boolean isOpen;

    //contentLayout为需要滑动的内容容器
    //menuWidth为菜单容器宽度，即contentLayout的最大滑动距离
    private ViewGroup contentLayout;
    private int menuWidth;

    public SwipeRecyclerView(Context context) {
        super(context);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchEvent(e);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchEvent(e);
                //此处直接拦截滑动事件，不传递给子View（不然滑动也会触发点击事件）
                //如需实现子View的滑动交互，需要重写拦截逻辑
                return true;
            case MotionEvent.ACTION_UP:
                onTouchEvent(e);
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        //计算偏移量
        float dx = x - lastX;
        float dy = y - lastY;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取item项的ViewHolder
                ViewHolder holder = (ViewHolder) getChildViewHolder(findChildViewUnder(x, y));
                //获取内容容器
                ViewGroup contentView = holder.contentLayout;
                if (contentLayout == null || !contentLayout.equals(contentView)) {
                    //如果之前没有操作过item，或者当前点击item的和上一次操作的不是同一个
                    //重置所有坐标位置
                    lastX = lastY = currX = 0;
                    if (contentLayout != null) {
                        //如果不是同一个，把上一个item复位
                        contentLayout.setTranslationX(0);
                    }
                    //重置全局变量
                    isOpen = false;
                    contentLayout = contentView;
                    contentLayout = holder.contentLayout;
                    menuWidth = holder.menuLayout.getWidth();
                }
                //记录起始位置
                startX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(dx) > Math.abs(dy)) {
                    //用偏移量判断是否是横向滑动
                    //滑动位置随偏移量变化
                    currX += dx;
                    if (currX > 0 || -currX > menuWidth) {
                        //边界控制，不让滑动位置出界
                        currX = dx > 0 ? 0 : -menuWidth;
                    }
                    //执行滑动操作
                    contentLayout.setTranslationX(currX);
                }
                break;
            case MotionEvent.ACTION_UP:
                //计算从按下到抬起的绝对偏移量
                float delta = lastX - startX;
                float target = 0;
                if (delta == 0 || (delta < 0 && isOpen) || (delta > 0 && !isOpen)) {
                    //点击事件直接跳出（子View处理）
                    //隐藏状态右滑和展开状态左滑直接跳出（无效操作）
                    break;
                }
                if (delta > 0 && delta >= menuWidth / 2 || delta < 0 && -delta < menuWidth / 2) {
                    //如果右滑距离 > 菜单宽度的一半或者左滑距离 < 菜单宽度的一半
                    //item复位隐藏菜单
                    target = 0;
                } else if (delta > 0 && delta < menuWidth / 2 || delta < 0 && -delta >= menuWidth / 2) {
                    //如果右滑距离 < 菜单宽度的一半或者左滑距离 > 菜单宽度的一半
                    //item展开菜单
                    target = -menuWidth;
                }
                currX = target;
                //执行滑动
                contentLayout.setTranslationX(currX);
                if (Math.abs(delta) > menuWidth / 2){
                    //状态更新
                    isOpen = !isOpen;
                }
                break;
        }
        lastX = x;
        lastY = y;
        //此处直接返回true（不会影响子View的事件传递）
        return true;
    }

    /**
     * SwipeRecyclerView专用的ViewHolder
     * 必须实现绑定内容容器和菜单容器的方法，这里要求返回的是控件id
     */
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup contentLayout;
        ViewGroup menuLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            contentLayout = itemView.findViewById(bindContentLayout());
            menuLayout = itemView.findViewById(bindMenuLayout());
        }

        public abstract int bindContentLayout();

        public abstract int bindMenuLayout();
    }
}
