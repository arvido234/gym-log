package fm.mrc.gymlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable Edge-to-Edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        // Apply Theme
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String theme = prefs.getString(SettingsActivity.KEY_THEME, "blue");
        boolean isBlackBg = prefs.getBoolean(SettingsActivity.KEY_BACKGROUND_BLACK, false);

        int infoThemeId = R.style.Theme_GymLog; // Default Blue
        switch (theme) {
            case "red": infoThemeId = R.style.Theme_GymLog_Red; break;
            case "green": infoThemeId = R.style.Theme_GymLog_Green; break;
            case "orange": infoThemeId = R.style.Theme_GymLog_Orange; break;
            case "purple": infoThemeId = R.style.Theme_GymLog_Purple; break;
        }
        setTheme(infoThemeId);

        super.onCreate(savedInstanceState);
        
        // Handle Background
        String bgType = prefs.getString(SettingsActivity.KEY_BACKGROUND_TYPE, "charcoal");
        
        android.view.View rootView = getWindow().getDecorView();
        
        switch (bgType) {
            case "black":
                rootView.setBackgroundColor(android.graphics.Color.BLACK);
                getWindow().setStatusBarColor(android.graphics.Color.BLACK);
                break;
            case "midnight":
                rootView.setBackgroundResource(R.drawable.bg_gradient_midnight);
                // For gradients, we might want a translucent status bar or specific color
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT); 
                getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
                break;
            case "sunrise":
                rootView.setBackgroundResource(R.drawable.bg_gradient_sunrise);
                getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
                break;
            case "custom":
                java.io.File imgFile = new java.io.File(getFilesDir(), "custom_bg.jpg");
                if (imgFile.exists()) {
                     try {
                         // 1. Get Screen Dimensions
                         android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
                         getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                         int screenWidth = metrics.widthPixels;
                         int screenHeight = metrics.heightPixels;

                         // 2. Decode Bounds Only First
                         android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                         options.inJustDecodeBounds = true;
                         android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                         
                         // 3. Calculate inSampleSize
                         options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);
                         
                         // 4. Decode Actual Bitmap with Subsampling
                         options.inJustDecodeBounds = false;
                         android.graphics.Bitmap originalBitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                         
                         if (originalBitmap != null) {
                             // 5. Handle EXIF Rotation
                             android.media.ExifInterface exif = new android.media.ExifInterface(imgFile.getAbsolutePath());
                             int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL);
                             
                             int rotationDegrees = 0;
                             switch (orientation) {
                                 case android.media.ExifInterface.ORIENTATION_ROTATE_90: rotationDegrees = 90; break;
                                 case android.media.ExifInterface.ORIENTATION_ROTATE_180: rotationDegrees = 180; break;
                                 case android.media.ExifInterface.ORIENTATION_ROTATE_270: rotationDegrees = 270; break;
                             }

                             // 6. Calculate Center Crop Scale and Matrix
                             int bitmapWidth = originalBitmap.getWidth();
                             int bitmapHeight = originalBitmap.getHeight();
                             
                             // Adjust dimensions if rotated 90 or 270 degrees for calculation
                             if (rotationDegrees == 90 || rotationDegrees == 270) {
                                 int temp = bitmapWidth;
                                 bitmapWidth = bitmapHeight;
                                 bitmapHeight = temp;
                             }
                             
                             float scale;
                             float dx = 0, dy = 0;
                             
                             if (bitmapWidth * screenHeight > screenWidth * bitmapHeight) {
                                 scale = (float) screenHeight / (float) bitmapHeight; 
                                 dx = (screenWidth - bitmapWidth * scale) * 0.5f;
                             } else {
                                 scale = (float) screenWidth / (float) bitmapWidth;
                                 dy = (screenHeight - bitmapHeight * scale) * 0.5f;
                             }
                             
                             android.graphics.Matrix drawMatrix = new android.graphics.Matrix();
                             if (rotationDegrees != 0) {
                                 drawMatrix.postRotate(rotationDegrees);
                             }
                             drawMatrix.postScale(scale, scale);
                             drawMatrix.postTranslate(Math.round(dx), Math.round(dy));
                             
                             // 7.Create Final Bitmap matching Screen Size
                             // We use ARGB_8888 or even RGB_565 to save memory if needed, but 8888 is standard
                             android.graphics.Bitmap finalBitmap = android.graphics.Bitmap.createBitmap(screenWidth, screenHeight, android.graphics.Bitmap.Config.ARGB_8888);
                             android.graphics.Canvas canvas = new android.graphics.Canvas(finalBitmap);
                             android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG);
                             
                             // We need to account for the pivot point during rotation if we rotate the source bitmap directly in the draw command
                             // Actually, drawing a bitmap with a matrix handles the source coordinate transformation.
                             // But wait, if we rotate 90 degrees, the source bitmap center changes.
                             // Simpler approach for matrix construction:
                             // Reset matrix for new logic
                             drawMatrix.reset();
                             // Center source for rotation
                             drawMatrix.postTranslate(-originalBitmap.getWidth() / 2f, -originalBitmap.getHeight() / 2f);
                             // Rotate
                             drawMatrix.postRotate(rotationDegrees);
                             // Scale
                             drawMatrix.postScale(scale, scale);
                             // Translate to center of screen
                             drawMatrix.postTranslate(screenWidth / 2f, screenHeight / 2f);

                             canvas.drawBitmap(originalBitmap, drawMatrix, paint);
                             
                             android.graphics.drawable.Drawable d = new android.graphics.drawable.BitmapDrawable(getResources(), finalBitmap);
                             rootView.setBackground(d);
                             getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
                             getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);
                             
                             // Cleanup
                             originalBitmap.recycle();
                         }
                     } catch (Exception e) {
                         e.printStackTrace();
                         // Fallback
                         rootView.setBackgroundColor(getResources().getColor(R.color.gym_background, getTheme()));
                     } catch (OutOfMemoryError e) {
                         e.printStackTrace();
                         // OOM Fallback
                         rootView.setBackgroundColor(getResources().getColor(R.color.gym_background, getTheme()));
                     }
                } else {
                    // Fallback if file missing
                    rootView.setBackgroundColor(getResources().getColor(R.color.gym_background, getTheme()));
                }
                break;
            default: // charcoal
                 // Default theme handles it, but ensures reset if switching back
                 rootView.setBackgroundColor(getResources().getColor(R.color.gym_background, getTheme()));
                 // Reset status bar to theme default? Or just set to background color
                 getWindow().setStatusBarColor(getResources().getColor(R.color.gym_background, getTheme()));
                 break;
        }
    }

    public static int calculateInSampleSize(
            android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
