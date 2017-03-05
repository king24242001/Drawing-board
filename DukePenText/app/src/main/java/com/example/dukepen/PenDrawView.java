/**   
 * Copyright © 2014 All rights reserved.
 * 
 * @Title: PenDrawView.java 
 * @Prject: DukePen
 * @Package: com.example.dukepen 
 * @Description: TODO
 * @author: raot raotao.bj@cabletech.com.cn 
 * @date: 2014年10月10日 上午11:11:05 
 * @version: V1.0   
 */
package com.example.dukepen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class PenDrawView extends View {

	//畫筆顏色
	private int penColor = Color.BLACK;
	// 是否清除了界面
	private boolean isClear = false;
	/// 是否是橡皮擦
	private boolean isEraser = false;
	//畫筆大小
	private int penWidth = 5;
	private Paint paint = new Paint();
	private Path path = new Path();
	//是否第一次繪製
	private boolean isFirst = true;
	//是否多點觸控
	private boolean isOtherTouch = false;
	//當前觸摸位置
	private float currentX, currentY;
	private Bitmap cacheBitmap;
	//創建畫布
	private Canvas cacheCanvas;
	//保存每一筆繪製紀錄
	private ArrayList<Bitmap> bitmapList;
	//當前展示的繪製紀錄下表
	private int position;

	public PenDrawView(Context context) {
		super(context);
		init();
	}

	public PenDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		bitmapList = new ArrayList<Bitmap>();//保存繪製紀錄
		paint.setAntiAlias(true);			//抗鋸齒
		paint.setStrokeWidth(penWidth);		//筆寬
		paint.setStyle(Paint.Style.STROKE); //畫筆樣式為空心，只顯示線
		paint.setColor(penColor);
	}

	//清除介面
	public void clear() {
		if (cacheCanvas != null && !isClear) {
			paint.setColor(Color.WHITE);
			cacheCanvas.drawPaint(paint); //使用畫筆去填充整個Bitmap
			paint.setColor(penColor);
			cacheCanvas.drawColor(Color.WHITE);
			invalidate();
//			while (position + 1 < bitmapList.size()) {
//				bitmapList.remove(bitmapList.size() - 1);
//			}
			addBitmap();
			isClear = true;
			if (isEraser) {
				paint.setColor(Color.WHITE);
			}

		}
	}

	//重新繪製
	public void redraw() {
		if (cacheCanvas != null) {
			paint.setColor(Color.WHITE);
			cacheCanvas.drawPaint(paint);
			paint.setColor(penColor);
			cacheCanvas.drawColor(Color.WHITE);
			invalidate();
			bitmapList.clear();
			addBitmap();
			changePen();
		}
	}

	//得到畫筆顏色
	public int getPenColor() {
		return penColor;
	}

	//設置畫筆顏色
	public void setPenColor(int penColor) {
		this.penColor = penColor;
		changePen();
	}

	//得到畫筆寬度
	public int getPenWidth() {
		return penWidth;
	}

	//設置畫筆寬度
	public void setPenWidth(int penWidth) {
		this.penWidth = penWidth;
		changePen();
	}

	//使用橡皮擦
	public void changeEraser() {
		isEraser = true;
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(40);

	}

	//使用畫筆
	public void changePen() {
		isEraser = false;
		paint.setStrokeWidth(penWidth);
		paint.setColor(penColor);
	}


	//得到當前展示內容
	public Bitmap getBitmap() {
		return bitmapList.get(position);
	}

	//上一步
	public void lastStep() {
		if (position > 0) {
			isClear = false;
			position--;
			cacheBitmap = selectBitmap();
			cacheCanvas.setBitmap(cacheBitmap);
			invalidate();
		}
	}

	//下一步
	public void nextStep() {
		if (position < bitmapList.size() - 1) {
			isClear = false;
			position++;
			cacheBitmap = selectBitmap();
			cacheCanvas.setBitmap(cacheBitmap);
			invalidate();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isFirst) {
			cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
					Config.ARGB_8888);
			cacheCanvas = new Canvas(cacheBitmap);
			cacheCanvas.drawColor(Color.WHITE);
			addBitmap();
			isFirst = false;
		}
		canvas.drawBitmap(cacheBitmap, 0, 0, null);
		canvas.drawPath(path, paint);//路徑
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			isClear = false;
			currentX = event.getX(event.getActionIndex());
			currentY = event.getY(event.getActionIndex());
			path.moveTo(currentX, currentY);
			break;
		case MotionEvent.ACTION_MOVE:
			if (isOtherTouch) {
				return true;
			}
			float x = event.getX(event.getActionIndex());
			float y = event.getY(event.getActionIndex());
			path.quadTo(currentX, currentY, x, y);
			//quadTo代表的是從目前繪製路徑中的最後一個節點畫到下一個路徑節點之間，增加一個加上「quadratic bezier」，
			//用以讓二個節點之間增加數個節點，連結起來更平滑，不會有急轉彎的線條出現。
			currentX = x;
			currentY = y;
			break;
		case MotionEvent.ACTION_UP:
			isOtherTouch = false;
			cacheCanvas.drawPath(path, paint);
			path.reset();//避免下次畫線仍接續上次的線
			while (position + 1 < bitmapList.size()) {
				bitmapList.remove(bitmapList.size() - 1);
			}
			addBitmap();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			isOtherTouch = true;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			break;

		}
		invalidate();
		return true;
	}
	//每一筆結束都是一個新的Bitmap
	private void addBitmap() {
		Bitmap newBitmap = Bitmap.createBitmap(cacheBitmap.getWidth(),
				cacheBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		newCanvas.drawBitmap(cacheBitmap, 0, 0, null);
		//資料保存最多25筆　避免累積過多的資源
		if (bitmapList.size() == 25) {
			bitmapList.remove(0);//移除第0筆資料
		}
		bitmapList.add(newBitmap);
		position = bitmapList.size() - 1;
	}
	//調出bitmapList的紀錄(position)
	private Bitmap selectBitmap() {
		Bitmap newBitmap = Bitmap.createBitmap(bitmapList.get(position)
				.getWidth(), bitmapList.get(position).getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		newCanvas.drawBitmap(bitmapList.get(position), 0, 0, null);
		return newBitmap;
	}
}
