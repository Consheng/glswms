package ykk.xc.com.glswms.util.smallchart.chart;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import ykk.xc.com.glswms.util.smallchart.interfaces.iChart.IBarChart;
import ykk.xc.com.glswms.util.smallchart.interfaces.iData.IBarData;
import ykk.xc.com.glswms.util.smallchart.render.BarChartRender;
import ykk.xc.com.glswms.util.smallchart.render.XAxisOffsetRender;
import ykk.xc.com.glswms.util.smallchart.render.YAxisRender;

/**
 * Created by Idtk on 2016/6/6.
 * Blog : http://www.idtkm.com
 * GitHub : https://github.com/Idtk
 * 描述 : 柱状图绘制类
 */
public class BarChart extends BarLineCurveChart<IBarData> implements IBarChart {

    private BarChartRender mBarChartRender;
    /**
     * 单个柱状图宽度
     */
    private float barWidth = 30;

    public BarChart(Context context) {
        super(context);
    }

    public BarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chartRenderList.clear();
        float offset ;
        for (int i=0; i<mDataList.size(); i++){
            offset = mXAxisData.getInterval()*mXAxisData.getAxisScale()/2-barWidth*mDataList.size()/2+barWidth*i;
            mBarChartRender = new BarChartRender(mDataList.get(i),mXAxisData,mYAxisData,offset,barWidth);
            chartRenderList.add(mBarChartRender);
        }
    }


    @Override
    protected void axisScale() {
        mXAxisData.setAxisScale(mXAxisData.getAxisLength()/(mXAxisData.getMaximum()-mXAxisData.getMinimum()+mXAxisData.getInterval()));
        mYAxisData.setAxisScale(mYAxisData.getAxisLength()/(mYAxisData.getMaximum()-mYAxisData.getMinimum()));
        mXAxisOffsetRender = new XAxisOffsetRender(mXAxisData);
        mYAxisRender = new YAxisRender(mYAxisData,mXAxisData);
    }


    @Override
    protected void axisRender(Canvas canvas) {
        mXAxisOffsetRender.drawGraph(canvas);
        mYAxisRender.drawGraph(canvas);
    }

    public void setBarWidth(float barWidth) {
        this.barWidth = barWidth;
    }


    @Override
    public void computeYAxis() {
        mComputeYAxis.computeYAxisMin(mDataList);
    }


}
