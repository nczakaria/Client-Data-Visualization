package edu.umass.cs.client.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.view.ViewGroup;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import java.util.LinkedList;

/**
 * ContextImageWidget.java
 */
public class ContextImageWidget extends WidgetBase
{
	protected static final String LOG_TAG = "ContextWidget";
	protected static final int CONTAINER_ID=35;
	protected static final int IMAGE_ID=40;
	protected static final int HISTORY_ID=50;
    
	protected RelativeLayout container;
	protected ImageView image_view;
	
	public RollingHistoryView history_view;
	
	protected String value;
	
	
	public ContextImageWidget(Context context,int numStates, LinkedList<Integer> history) {
    	super(context);
    	drawContainer();
    	drawImage();
    	drawHistory(history);
    	this.history_view.numStates = numStates;
    	
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
    
    private void drawContainer(){
    	container = new RelativeLayout(context);
    	container.setId(CONTAINER_ID);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		body.addView(container,params); // add to view
    }
    private void drawImage(){
    	
    	image_view = new ImageView(context);
    	image_view.setId(IMAGE_ID);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    	container.addView(image_view, params);
    }
    
    public void addOrRemoveTitleViewAsNecessary(){
    	if (title != null && title.length() > 0) drawTitle();
    	else {
    		Log.v(LOG_TAG,"removing the title label");
    		this.removeView(title_view);
    		title_view = null;
    	}
    }
    
    public void addOrRemoveDescriptionViewAsNecessary(){
    	if (description != null && description.length() > 0) drawDescription();
    	else {
    		Log.v(LOG_TAG,"removing the description label");
    		this.removeView(description_view);
    		description_view = null;
    	}
    }
    
    @Override
    protected void drawTitle(){
    	super.drawTitle();
    	this.removeView(title_view);
    	container.removeView(title_view);
    	title_view.setPadding(0, 10, 10, 10);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    	params.addRule(RelativeLayout.LEFT_OF,IMAGE_ID);
    	container.addView(title_view, params);
    }
    
    public void setImage(String label){
    	if(label.equals("STATIONARY")){
//    		image_view.setImageResource(R.drawable.stat);
    	} else if(label.equals("JUMPING")){
//    		image_view.setImageResource(R.drawable.drive);
    	} else if(label.equals("WALK")){
//    		image_view.setImageResource(R.drawable.walk);
    	}
    }
    
    public void setImage(int label){
    	if(label==0){
//    		image_view.setImageResource(R.drawable.stat);
    	} else if(label==2){
//    		image_view.setImageResource(R.drawable.drive);
    	} else if(label==1){
//    		image_view.setImageResource(R.drawable.walk);
    	}
    }

	@Override
	public String getValue() {
		// NOOP
		return null;
	}
	public void loadFromDB(int field_id){
    	// NO OP
    }
    
    public void dbUpdateValue(){
    	// NO OP
    }
    
    
    public static class RollingHistoryView extends View{

    	protected static final int SIZE = 60;
    	protected static final int DOT = 3;
		
    	protected static final int HEIGHT = 60;
		
    	protected int numStates = 0;
    	protected LinkedList<Integer> history = new LinkedList<Integer>();
    	protected   Paint paint = new Paint();
    	
    	protected int parentWidth = 0;
    	
    	
    	public RollingHistoryView(Context context) {
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
		    int y = (numStates * HEIGHT - HEIGHT/2) - state * HEIGHT;
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