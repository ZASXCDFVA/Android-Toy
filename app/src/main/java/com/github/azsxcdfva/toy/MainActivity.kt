package com.github.azsxcdfva.toy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.github.azsxcdfva.toy.databinding.ActivityMainBinding
import com.github.azsxcdfva.toy.utils.fetch
import com.github.azsxcdfva.toy.utils.process
import com.github.azsxcdfva.toy.utils.startActivityForResult
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.concurrent.CopyOnWriteArrayList

class MainActivity : BaseActivity() {
    private val self = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            val result = startActivityForResult(
                ActivityResultContracts.StartActivityForResult(),
                Intent(self, LoginActivity::class.java)
            )

            if (result.resultCode != Activity.RESULT_OK) {
                return@launch finish()
            }

            val binding = ActivityMainBinding
                .inflate(layoutInflater, findViewById(android.R.id.content), false)
                .apply {
                    main.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                }

            setContentView(binding.root)

            val modelIdentifier = try {
                DigitalInkRecognitionModelIdentifier.fromLanguageTag("en")
            } catch (e: Exception) {
                Toast.makeText(self, e.toString(), Toast.LENGTH_LONG).show()

                return@launch finish()
            }

            val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            val recognizer = DigitalInkRecognition.getClient(
                DigitalInkRecognizerOptions.builder(model).build()
            )

            try {
                RemoteModelManager.getInstance().fetch(model)
            } catch (e: Exception) {
                Toast.makeText(self, e.toString(), Toast.LENGTH_LONG).show()

                return@launch finish()
            }

            val strokes: MutableList<Ink.Stroke> = CopyOnWriteArrayList()
            val invalidate = Channel<Unit>(Channel.CONFLATED)

            binding.apply {
                main.visibility = View.VISIBLE
                loading.visibility = View.GONE
                expression.text = ""

                clear.setOnClickListener {
                    this.drawView.reset()
                    this.result.text = ""
                    this.expression.text = ""
                }

                drawView.callback = object : DrawableView.Callback {
                    private var stroke: Ink.Stroke.Builder? = null

                    override fun onDown(x: Float, y: Float) {
                        stroke = Ink.Stroke.builder()
                    }

                    override fun onMove(x: Float, y: Float) {
                        stroke?.addPoint(Ink.Point.create(x, y))
                    }

                    override fun onUp(x: Float, y: Float) {
                        strokes.add(stroke?.build() ?: return)

                        invalidate.trySend(Unit)
                    }

                    override fun onReset() {
                        strokes.clear()
                    }
                }
            }

            while (isActive) {
                invalidate.receive()

                val ink = Ink.builder()

                for (stroke in strokes) {
                    ink.addStroke(stroke)
                }

                val text = try {
                    withContext(Dispatchers.Default) {
                        val r = recognizer.process(ink.build())

                        r.candidates
                            .map { it.text }
                            .firstOrNull { RegexExpress.matches(it) }
                    }
                } catch (e: Exception) {
                    null
                }

                if (text != null) {
                    binding.expression.text = text

                    try {
                        binding.result.text = withContext(Dispatchers.Default) {
                            ExpressionBuilder(text).build().evaluate().toString()
                        }
                    } catch (e: Exception) {
                        binding.result.text = "N/A"
                    }
                } else {
                    binding.expression.text = "?"
                    binding.result.text = "N/A"
                }
            }
        }
    }

    companion object {
        private val RegexExpress = Regex("[0-9+\\-*/()]+")
    }
}