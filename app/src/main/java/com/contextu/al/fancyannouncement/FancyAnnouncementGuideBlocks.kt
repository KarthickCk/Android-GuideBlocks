package com.contextu.al.fancyannouncement

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.contextu.al.model.customguide.ContextualContainer
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.contextu.al.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class FancyAnnouncementGuideBlocks(private val activity: Activity): Dialog(activity) {

    private var isShowing: Boolean = false

    fun show(contextualContainer: ContextualContainer) {
        if (contextualContainer.guidePayload.guide.guideBlock.contentEquals("FancyAnnouncement") &&
            !this.isShowing) {
            this.isShowing = true
            val title = contextualContainer.guidePayload.guide.titleText.text ?: ""
            val message = contextualContainer.guidePayload.guide.contentText.text ?: ""

            val buttons = contextualContainer.guidePayload.guide.buttons
            var prevButtonText = "back"
            var nextButtonText = "next"

            buttons.prevButton?.let { button ->
                prevButtonText = button.text ?: "back"
            }

            buttons.nextButton?.let { button ->
                nextButtonText = button.text ?: "next"
            }
            val negativeText = prevButtonText
            val positiveText = nextButtonText

            var imageURL: String? = null

            val images = contextualContainer.guidePayload.guide.images
            if (images.isNotEmpty()) {
                imageURL = images[0].resource
            }

            this.show(
                title,
                message,
                negativeText,
                { v: View? ->
                    // "prevStep" informs the Contextual SDK that the user is dismissing/cancelling or trying to go to previous step (tapping Back, Cancel, etc)
                    // If there is no previous step in the guide, then the guide will be "rejected" (and dismissed)
                    // This provides an analytics update
                    contextualContainer.guidePayload.prevStep.onClick(v)
                    this.dismiss()
                    this.isShowing = false
                    contextualContainer.tagManager.setStringTag("test_key", "test_value")
                    CoroutineScope(Dispatchers.IO).launch {
                        // An example of how to get a tag
                        contextualContainer.tagManager.getTag("test_key").collectLatest { tags ->
                            if (tags != null) {
                                activity.runOnUiThread {
                                    AlertDialog.Builder(activity)
                                        .setTitle("Tagged value")
                                        .setMessage("test_key value is: " + tags.tagStringValue)
                                        .setPositiveButton("OK") { dialog, which ->
                                            dialog.dismiss()
                                        }
                                        .create()
                                        .show()
                                }
                            }
                        }
                    }
                },
                positiveText,
                { v: View? ->
                    // "nextStep" informs the Contextual SDK that the user is accepting the guide or trying to go to next step (tapping Next or OK, etc)
                    // If there is no next step in the guide, then the guide will be "complete" (and dismissed)
                    // This provides an analytics update
                    contextualContainer.guidePayload.nextStep.onClick(v)
                    this.dismiss()
                    this.isShowing = false
                },
                imageURL ?: ""
            )
        }
    }

    fun show(title: String,
             content: String,
             negativeText: String,
             negativeButtonListener: View.OnClickListener,
             positiveText: String,
             positiveButtonListener: View.OnClickListener,
             imageUrl: String) {

        this.setContentView(R.layout.fancy_announcement)
        this.window?.setLayout((activity.resources.displayMetrics.widthPixels * 0.90).toInt(),
            (activity.resources.displayMetrics.heightPixels * 0.55).toInt())
        this.show()

        val fancyAnnouncementImage = this.findViewById<ImageView>(R.id.announcementImage)
        fancyAnnouncementImage?.let {
            Glide.with(activity.baseContext).load(imageUrl).into(fancyAnnouncementImage)
        }

        val fancyAnnouncementTitle = this.findViewById<TextView>(R.id.title)
        fancyAnnouncementTitle?.text = title

        val fancyAnnouncementContent = this.findViewById<TextView>(R.id.content)
        fancyAnnouncementContent?.text = content

        val createAccountButton = this.findViewById<Button>(R.id.create_button)
        createAccountButton?.text = positiveText
        createAccountButton.setOnClickListener(positiveButtonListener)

        val cancelButton = this.findViewById<Button>(R.id.cancel_button)
        cancelButton?.text = negativeText
        cancelButton.setOnClickListener(negativeButtonListener)
    }
}