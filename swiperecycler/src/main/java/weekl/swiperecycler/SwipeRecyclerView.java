package weekl.swiperecycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;

public class SwipeRecyclerView extends RecyclerView {

    //记录屏幕触摸位置
    private float startX, startY, lastX, lastY;

    //速度追踪器
    private VelocityTracker mVelocityTracker;

    //记录滑动位置
    private float currX;
    //记录菜单展开状态
    private boolean isOpen;

    //contentLayout为需要滑动的内容容器
    //menuWidth为菜单容器宽度，即contentLayout的最大滑动距离
    private ViewGroup contentLayout = null;
    private int menuWidth = 0;

    public SwipeRecyclerView(Context context) {
        this(context,null);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVelocityTracker = VelocityTracker.obtain();
    }

    private float lastInterceptX, lastInterceptY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        boolean intercepted = super.onInterceptTouchEvent(e);
        float x = e.getX();
        float y = e.getY();
        float dx = x - lastInterceptX;
        float dy = y - lastInterceptY;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchEvent(e);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(dx) > Math.abs(dy)) {
                    //拦截横向滑动的move事件
                    onTouchEvent(e);
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                onTouchEvent(e);
                break;
        }
        lastInterceptX = x;
        lastInterceptY = y;
        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //追踪本次触摸事件
        mVelocityTracker.addMovement(e);

        //计算偏移量
        float x = e.getX();
        float y = e.getY();
        //每一帧的横向偏移量
        float dx = x - lastX;
        //从点击到最后操作（滑动或抬起）的总位移（两个点之间的距离）
        float deltaX = lastX - startX;
        float deltaY = lastY - startY;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //获取item项的ViewHolder
                ViewHolder holder = (ViewHolder) getChildViewHolder(findChildViewUnder(x, y));
                //获取内容容器
                ViewGroup contentView = holder.contentLayout;
                if (contentView == null){
                    break;
                }
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
                startY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (contentLayout == null){
                    break;
                }
                if (Math.abs(deltaX) < Math.abs(deltaY)) {
                    //非横向滑动，跳出
                    break;
                }
                //滑动位置随偏移量变化
                currX += dx;
                if (currX > 0 || -currX > menuWidth) {
                    //边界控制，不让滑动位置出界
                    currX = dx > 0 ? 0 : -menuWidth;
                }
                //执行滑动操作
                contentLayout.setTranslationX(currX);
                break;
            case MotionEvent.ACTION_UP:
                if (contentLayout == null){
                    break;
                }
                if (Math.abs(deltaX) < Math.abs(deltaY) || deltaX == 0 ||
                        deltaX < 0 && isOpen || deltaX > 0 && !isOpen) {
                    //非横向滑动（包括点击），跳出
                    //隐藏状态右滑和展开状态左滑直接跳出（无效操作）
                    break;
                }
                //计算200ms内的滑动速度
                mVelocityTracker.computeCurrentVelocity(200);
                float xVelocity = mVelocityTracker.getXVelocity();
                if (deltaX > 0 && xVelocity > 50 || deltaX > 0 && deltaX >= menuWidth / 2 || deltaX < 0 && -deltaX < menuWidth / 2) {
                    //如果右滑速度 > 50 || 右滑距离 > 菜单宽度的一半 || 左滑距离 < 菜单宽度的一半
                    //item复位隐藏菜单
                    currX = 0;
                } else if (deltaX < 0 && xVelocity > 50 || deltaX > 0 && deltaX < menuWidth / 2 || deltaX < 0 && -deltaX >= menuWidth / 2) {
                    //如果左滑速度 > 50 || 右滑距离 < 菜单宽度的一半 || 左滑距离 > 菜单宽度的一半
                    //item展开菜单
                    currX = -menuWidth;
                }
                //执行滑动
                contentLayout.setTranslationX(currX);
                //状态更新
                if (Math.abs(deltaX) >= menuWidth / 2) {
                    isOpen = !isOpen;
                }
                break;
        }
        lastX = x;
        lastY = y;
        return super.onTouchEvent(e);
    }

    /**
     * SwipeRecyclerView专用的ViewHolder
     * 必须实现绑定内容容器和菜单容器的方法，这里要求返回的是控件id
     *
     * 建议自定义与SwipeRecyclerView配套的Adapter抽象类
     * 把ViewHolder封装在抽象类中，让使用者强制使用配套的ViewHolder
     */
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup contentLayout;
        ViewGroup menuLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            try{
                contentLayout = itemView.findViewById(bindContentLayout());
                menuLayout = itemView.findViewById(bindMenuLayout());
            }catch (Exception e){
                contentLayout = null;
                menuLayout = null;
            }
        }

        public abstract int bindContentLayout();

        public abstract int bindMenuLayout();
    }
}
