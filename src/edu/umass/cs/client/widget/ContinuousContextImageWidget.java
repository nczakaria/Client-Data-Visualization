package edu.umass.cs.client.widget;

import java.util.LinkedList;

import edu.umass.cs.client.widget.ContextImageWidget.RollingHistoryView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class ContinuousContextImageWidget extends ContextImageWidget {

	static int min = 0;
	static int max = 0;

	public ContinuousContextImageWidget(Context context,
			int min, int max, LinkedList<Integer> history) {
		super(context, 0, history);
		ContinuousContextImageWidget.min = min;
		ContinuousContextImageWidget.max = max;
	}
	
	private void drawHistory(LinkedList<Integer> history){
		history_view = new RollingHistoryView(context);
		if (history !=null) {
			history_view.setHistory(history);	
		}
		this.history_view.setId(HISTORY_ID);
    	LayoutParams params =new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
    	params.addRule(RelativeLayout.BELOW,CONTAINER_ID);
    	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		body.addView(this.history_view, params);
	}

	public static class ContinuousRollingHistoryView extends RollingHistoryView{

    	protected static final int SIZE = 60;
    	protected static final int DOT = 3;
		
    	protected static final int HEIGHT = 60;
		
    	protected int numStates = 0;
    	protected LinkedList<Integer> history = new LinkedList<Integer>();
    	protected   Paint paint = new Paint();
    	
    	protected int parentWidth = 0;
    	
    	
    	public ContinuousRollingHistoryView(Context context) {
			super(context);
//			setBackgroundColor(Color.WHITE);
			paint.setColor(Color.BLACK);
		}
    	
    	public void setHistory(LinkedList<Integer> history){
    		this.history = history;
    	}

    	public LinkedList<Integer> getHistory(){
    		return history;
    	}

    	
    	public static void add(LinkedList<Integer> history, int state){
    		synchronized (history){
	    		if (history.size() == SIZE){
	    			history.poll();
	    		}
	    		history.offer(state);
    		}    		
    	}
    	public void add(int state){
    		synchronized (history){
	    		if (history.size() == SIZE){
	    			history.poll();
	    		}
	    		history.offer(state);
    		}
    		invalidate(); // Tells the UI to redraw
    	}
    	
    	@Override
        public void onDraw(Canvas canvas) {
//    		Log.d("HISTORY", "DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING DRAWING ");
    		int xoffset =parentWidth/SIZE/2;
    		int prevX = -1;
    		int prevY= -1;
    		for(Integer state : history){
		    int y = HEIGHT - (state/max-min) * HEIGHT;
		    canvas.drawCircle(xoffset,y,DOT,paint);
		    if (prevX >0  && prevY >0){
		        canvas.drawLine(prevX, prevY, xoffset, y, paint);
		    }
		    prevX = xoffset;
		    prevY = y;
		    xoffset+=parentWidth/SIZE;
    		}
        }
    	
    	@Override
    	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
//    		Log.d("HISTORY", "MEASURING MEASURING MEASURING MEASURING MEASURING MEASURING MEASURING MEASURING MEASURING ");
    		parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//    		Log.d("ROLING", "Measured is : " + parentWidth + " height: " + parentHeight);
    		setMeasuredDimension(widthMeasureSpec,numStates * HEIGHT);
    	}
    	
    }

}
