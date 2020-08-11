package com.thuglife.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.thuglife.R;
import com.thuglife.utils.BitmapUtils;
import com.thuglife.utils.FaceUtils;
import com.felipecsl.gifimageview.library.GifImageView;
import com.tenginekit.AndroidConfig;
import com.tenginekit.Face;
import com.tenginekit.model.FaceLandmarkInfo;
import com.tenginekit.model.FaceLandmarkPoint;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


public class ClassifierActivity extends AppCompatActivity {
    private static final String TAG = "ClassifierActivity";

    ImageView showImage;

    List<FaceLandmarkInfo> faceLandmarks;
    private final Paint circlePaint = new Paint();
    private Paint paint = new Paint();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);
        onInit();
    }

    public void onInit() {

        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStrokeWidth((float) 1);
        circlePaint.setStyle(Paint.Style.STROKE);

        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth((float) 5);
        paint.setStyle(Paint.Style.FILL);

        showImage = findViewById(R.id.show_image);

        Drawable d = null;
        Bitmap bb = null;

        Drawable d_glasses = null;
        Bitmap b_glasses = null;

        Drawable d_cigaret = null;
        Bitmap b_cigaret = null;

        Drawable d_decorate = null;
        Bitmap b_decorate = null;

        Drawable d_weapon = null;
        Bitmap b_weapon = null;



        try {
            d = Drawable.createFromStream(getAssets().open("girl.jpeg"), null);
            showImage.setImageDrawable(d);
            bb = ((BitmapDrawable)d).getBitmap();

            d_glasses = Drawable.createFromStream(getAssets().open("glasses_00001.png"), null);
            b_glasses = ((BitmapDrawable)d_glasses).getBitmap();

            d_cigaret = Drawable.createFromStream(getAssets().open("cigaret_00007.png"), null);
            b_cigaret = ((BitmapDrawable)d_cigaret).getBitmap();

            d_decorate = Drawable.createFromStream(getAssets().open("decorate_00022.png"), null);
            b_decorate = ((BitmapDrawable)d_decorate).getBitmap();

        }catch (Exception e){
            e.printStackTrace();
        }



        com.tenginekit.Face.init(getBaseContext(),
                AndroidConfig.create()
                        .setNormalMode()
                        .openFunc(AndroidConfig.Func.Detect)
                        .openFunc(AndroidConfig.Func.Landmark)
                        .setInputImageFormat(AndroidConfig.ImageFormat.RGBA)
                        .setInputImageSize(
                                showImage.getDrawable().getIntrinsicWidth(),
                                showImage.getDrawable().getIntrinsicHeight()
                        ).setOutputImageSize(
                                showImage.getDrawable().getIntrinsicWidth(),
                                showImage.getDrawable().getIntrinsicHeight()
                        )
        );

        byte[] girl = bitmap2Bytes(bb);

        Bitmap out_bitmap = Bitmap.createBitmap(
            showImage.getDrawable().getIntrinsicWidth(),
            showImage.getDrawable().getIntrinsicHeight(),
            Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(out_bitmap);
        canvas.drawBitmap(bb, 0,0 , null);

        Face.FaceDetect faceDetect = com.tenginekit.Face.detect(girl);
        if(faceDetect.getFaceCount() > 0){
            faceLandmarks = faceDetect.landmark2d();
            if(faceLandmarks != null){
                for(int i = 0; i < faceLandmarks.size(); i++){
                    Point leftEye = getLeftEyeCenter(faceLandmarks.get(i));
                    Point rightEye = getRightEyeCenter(faceLandmarks.get(i));
                    Point centerEye = new Point(
                            (leftEye.x + rightEye.x)/2,
                            (leftEye.y + rightEye.y)/2
                            );
                    canvas.drawBitmap(
                            b_glasses,
                            centerEye.x - b_glasses.getWidth() / 2,
                            centerEye.y - b_glasses.getHeight() / 2,
                            null);

                    Point mouthCenter = getMouthCenter(faceLandmarks.get(i));
                    canvas.drawBitmap(
                            b_cigaret,
                            mouthCenter.x - b_cigaret.getWidth(),
                            mouthCenter.y,
                            null);

                    Point thin = getChinPoint(faceLandmarks.get(i));
                    canvas.drawBitmap(
                            b_decorate,
                            thin.x - b_decorate.getWidth() / 2,
                            thin.y + b_decorate.getHeight() / 3,
                            null);
                }
//                for(int i = 0; i < faceLandmarks.size(); i++){
//                    for(FaceLandmarkPoint point : faceLandmarks.get(i).landmarks){
//                        canvas.drawPoint(point.X, point.Y, paint);
//                    }
//                }
            }
        }
        showImage.setImageBitmap(out_bitmap);

        BitmapUtils.saveBitmap(out_bitmap, "/sdcard/girls.png");
    }


    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        com.tenginekit.Face.release();
    }

    Point getLeftEyeCenter(FaceLandmarkInfo fi){
        FaceLandmarkPoint p1 = fi.landmarks.get(105);
        FaceLandmarkPoint p2 = fi.landmarks.get(113);
        return new Point((int)((p1.X + p2.X) / 2), (int)((p1.Y + p2.Y) / 2));
    }

    Point getRightEyeCenter(FaceLandmarkInfo fi){
        FaceLandmarkPoint p1 = fi.landmarks.get(121);
        FaceLandmarkPoint p2 = fi.landmarks.get(129);
        return new Point((int)((p1.X + p2.X) / 2), (int)((p1.Y + p2.Y) / 2));
    }

    Point getMouthCenter(FaceLandmarkInfo fi){
        FaceLandmarkPoint p1 = fi.landmarks.get(208);
        return new Point((int)p1.X, (int)p1.Y);
    }

    Point getChinPoint(FaceLandmarkInfo fi){
        FaceLandmarkPoint p1 = fi.landmarks.get(53);
        return new Point((int)p1.X, (int)p1.Y);
    }


    public Point getCenterPoint(FaceLandmarkInfo fi){
        return new Point((int)fi.landmarks.get(177).X, (int)fi.landmarks.get(177).Y);
    }

    public byte[] readStream(String fileName) {
        try{
            InputStream inStream = getResources().getAssets().open(fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while((len = inStream.read(buffer)) != -1){
                outStream.write(buffer, 0, len);
            }
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private byte[] bitmap2Bytes(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the
        return temp;
    }

    static private Bitmap bytes2bitmap(byte[] byteArray, int ImageW, int ImageH) {
        Bitmap image1 = Bitmap.createBitmap(ImageW,ImageH, Bitmap.Config.ARGB_8888);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        buffer.get(byteArray);
        Buffer temp = buffer.rewind();

        image1.copyPixelsFromBuffer(temp);
        return image1;
    }
}