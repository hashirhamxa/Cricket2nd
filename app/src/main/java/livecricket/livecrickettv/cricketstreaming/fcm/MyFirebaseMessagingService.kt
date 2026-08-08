package livecricket.livecrickettv.cricketstreaming.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.activities.SplashActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle data payload if present
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
        }

        // 1. Extract Title, Description (body), and Image URL from both payloads
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body
        var imageUrl = remoteMessage.notification?.imageUrl?.toString()

        if (title.isNullOrEmpty()) {
            title = remoteMessage.data["title"]
        }
        if (body.isNullOrEmpty()) {
            body = remoteMessage.data["body"]
        }
        if (imageUrl.isNullOrEmpty()) {
            imageUrl = remoteMessage.data["image"]
        }

        Log.d(TAG, "Extracted Title: $title, Body: $body, ImageUrl: $imageUrl")

        // Only send notification if there is a title or body to display
        if (!title.isNullOrEmpty() || !body.isNullOrEmpty()) {
            var bitmap: Bitmap? = null

            // 2. Download the image synchronously in the background thread if URL is present
            if (!imageUrl.isNullOrEmpty()) {
                try {
                    bitmap = Glide.with(applicationContext)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get()
                } catch (e: Exception) {
                    Log.e(TAG, "Error downloading push notification image: ${e.message}", e)
                }
            }

            // 3. Build and show the notification
            sendNotification(title, body, bitmap)
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
    }

    private fun sendNotification(title: String?, messageBody: String?, bitmap: Bitmap?) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // 3. Build the notification using NotificationCompat.Builder
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // 4. If image downloaded successfully, use BigPictureStyle and setLargeIcon
        if (bitmap != null) {
            notificationBuilder.setLargeIcon(bitmap)
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null as Bitmap?) // hides the large icon thumbnail when the notification is expanded
            )
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
