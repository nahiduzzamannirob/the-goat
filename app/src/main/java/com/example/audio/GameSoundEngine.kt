package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Low-latency procedural PCM sound synthesizer + optional meme voice shout engine.
 * Synthesizes authentic mosquito buzz, crisp comic slap, splat pop, snore, bite alarm,
 * and SIUUU stadium fanfare without requiring external audio files.
 */
class GameSoundEngine(context: Context) {
  companion object {
    private const val SAMPLE_RATE = 22050
    private const val BUFFER_SAMPLES = 512
  }

  private class ActiveClip(
    val samples: FloatArray,
    val gain: Float,
    var cursor: Int = 0,
  )

  private val activeClips = CopyOnWriteArrayList<ActiveClip>()

  @Volatile var soundEnabled: Boolean = true
  @Volatile var buzzIntensity: Float = 0f
  @Volatile private var isReleased = false
  @Volatile private var isAudioPaused = false

  private val slapSamples = buildSlapBuffer()
  private val sandalWhooshSamples = buildSandalWhooshBuffer()
  private val splatSamples = buildSplatBuffer()
  private val siuuuFanfareSamples = buildFanfareBuffer()
  private val biteOuchSamples = buildBiteAlarmBuffer()
  private val shieldPickupSamples = buildShieldBuffer()
  private val snoreSamples = buildSnoreBuffer()
  private val gameOverSamples = buildGameOverBuffer()

  private var audioTrack: AudioTrack? = null
  private var mixerThread: Thread? = null

  private var tts: TextToSpeech? = null
  @Volatile private var ttsReady = false
  private var lastSpeechMs = 0L

  init {
    startAudioMixer()
    initTts(context.applicationContext)
  }

  private fun initTts(appContext: Context) {
    try {
      tts =
        TextToSpeech(appContext) { status ->
          if (status == TextToSpeech.SUCCESS && !isReleased) {
            tts?.language = Locale.US
            tts?.setPitch(1.22f)
            tts?.setSpeechRate(1.18f)
            ttsReady = true
          }
        }
    } catch (_: Throwable) {
      ttsReady = false
    }
  }

  private fun startAudioMixer() {
    val minBufferBytes =
      AudioTrack.getMinBufferSize(
        SAMPLE_RATE,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
      )
    val trackBufferBytes = maxOf(minBufferBytes, BUFFER_SAMPLES * 2 * 2)

    val track =
      AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLE_RATE)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(trackBufferBytes)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()

    audioTrack = track
    track.play()

    val thread =
      Thread {
        val pcmOut = ShortArray(BUFFER_SAMPLES)
        var carrierPhase = 0.0
        var lfoPhase = 0.0
        var wingPhase = 0.0
        var smoothedBuzz = 0f

        while (!isReleased) {
          if (isAudioPaused || !soundEnabled) {
            pcmOut.fill(0)
            activeClips.clear()
            try {
              Thread.sleep(24)
            } catch (_: InterruptedException) {
              break
            }
            continue
          }

          val targetBuzz = buzzIntensity.coerceIn(0f, 1f)
          for (i in 0 until BUFFER_SAMPLES) {
            smoothedBuzz += (targetBuzz - smoothedBuzz) * 0.004f
            var mix = 0f

            if (smoothedBuzz > 0.01f) {
              // Authentic mosquito buzz: FM-modulated sawtooth + wing-flutter AM
              lfoPhase += (2.0 * PI * 6.5) / SAMPLE_RATE
              wingPhase += (2.0 * PI * 32.0) / SAMPLE_RATE
              val freq = 355.0 + 70.0 * smoothedBuzz + 24.0 * sin(lfoPhase)
              carrierPhase += freq / SAMPLE_RATE
              if (carrierPhase >= 1.0) carrierPhase -= 1.0
              val saw = (2.0 * carrierPhase - 1.0).toFloat()
              val harmonic = sin(carrierPhase * 2.0 * PI * 2.0).toFloat() * 0.45f
              val flutter = (0.68f + 0.32f * sin(wingPhase).toFloat())
              mix += (saw * 0.55f + harmonic) * flutter * smoothedBuzz * 0.20f
            }

            if (activeClips.isNotEmpty()) {
              val iter = activeClips.iterator()
              while (iter.hasNext()) {
                val clip = iter.next()
                val idx = clip.cursor
                if (idx < clip.samples.size) {
                  mix += clip.samples[idx] * clip.gain
                  clip.cursor = idx + 1
                } else {
                  activeClips.remove(clip)
                }
              }
            }

            val clamped = mix.coerceIn(-0.95f, 0.95f)
            pcmOut[i] = (clamped * 32767f).toInt().toShort()
          }

          try {
            track.write(pcmOut, 0, BUFFER_SAMPLES)
          } catch (_: Throwable) {
            break
          }
        }
      }
        .apply {
          name = "MessiSlapAudioMixer"
          priority = Thread.MAX_PRIORITY
          isDaemon = true
          start()
        }
    mixerThread = thread
  }

  private fun triggerClip(samples: FloatArray, gain: Float) {
    if (!soundEnabled || isReleased || isAudioPaused) return
    if (activeClips.size < 14) {
      activeClips.add(ActiveClip(samples, gain))
    }
  }

  fun playSandalThrow() {
    triggerClip(sandalWhooshSamples, 0.65f)
  }

  fun playMissSlap() {
    triggerClip(slapSamples, 0.72f)
  }

  fun playMosquitoSlap(isBossOrSpecial: Boolean = false) {
    triggerClip(slapSamples, 0.85f)
    triggerClip(splatSamples, 0.85f)
    if (isBossOrSpecial) {
      triggerClip(siuuuFanfareSamples, 0.72f)
    }
  }

  fun playComboFanfare() {
    triggerClip(siuuuFanfareSamples, 0.65f)
  }

  fun playBiteWakeUp() {
    triggerClip(biteOuchSamples, 0.90f)
  }

  fun playShieldPickup() {
    triggerClip(shieldPickupSamples, 0.75f)
  }

  fun playSnore() {
    triggerClip(snoreSamples, 0.25f)
  }

  fun playGameOver() {
    triggerClip(gameOverSamples, 0.85f)
  }

  fun speakMemeLine(line: String, force: Boolean = false) {
    if (!soundEnabled || !ttsReady || isReleased || isAudioPaused) return
    val now = System.currentTimeMillis()
    if (!force && now - lastSpeechMs < 2400L) return
    lastSpeechMs = now
    try {
      tts?.speak(line, TextToSpeech.QUEUE_FLUSH, null, "meme_${now}")
    } catch (_: Throwable) {}
  }

  fun setPaused(paused: Boolean) {
    isAudioPaused = paused
    if (paused) {
      buzzIntensity = 0f
      try {
        audioTrack?.pause()
        audioTrack?.flush()
      } catch (_: Throwable) {}
    } else {
      try {
        audioTrack?.play()
      } catch (_: Throwable) {}
    }
  }

  fun release() {
    isReleased = true
    buzzIntensity = 0f
    try {
      mixerThread?.interrupt()
    } catch (_: Throwable) {}
    try {
      audioTrack?.stop()
      audioTrack?.release()
    } catch (_: Throwable) {}
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (_: Throwable) {}
  }

  // --- Procedural PCM Waveform Generators ---

  private fun buildSlapBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.13f).toInt()
    val out = FloatArray(len)
    val rng = Random(42)
    var lp = 0f
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val noiseEnv = exp(-t * 48f)
      val white = rng.nextFloat() * 2f - 1f
      lp += (white - lp) * 0.65f
      val snap = (white - lp * 0.3f) * noiseEnv * 0.95f
      val thudFreq = 180.0 * exp(-t * 22.0) + 48.0
      val thud = sin(2.0 * PI * thudFreq * t).toFloat() * exp(-t * 26f) * 0.65f
      out[i] = (snap + thud).coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildSandalWhooshBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.14f).toInt()
    val out = FloatArray(len)
    val rng = Random(77)
    var lp = 0f
    var phase = 0.0
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val env = sin(PI * (t / 0.14f)).toFloat()
      val white = rng.nextFloat() * 2f - 1f
      lp += (white - lp) * 0.25f
      val freq = 260.0 + 380.0 * (t / 0.14f)
      phase += freq / SAMPLE_RATE
      val swish = sin(2.0 * PI * phase).toFloat() * 0.35f + lp * 0.75f
      out[i] = (swish * env * 0.85f).coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildSplatBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.22f).toInt()
    val out = FloatArray(len)
    val rng = Random(99)
    var phase = 0.0
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val freq = 760.0 * exp(-t * 9.5) + 140.0
      phase += freq / SAMPLE_RATE
      val pop = sin(2.0 * PI * phase).toFloat() * exp(-t * 14f) * 0.75f
      val squish = (rng.nextFloat() * 2f - 1f) * exp(-t * 30f) * 0.4f
      out[i] = (pop + squish).coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildFanfareBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.42f).toInt()
    val out = FloatArray(len)
    val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val noteIdx = ((t / 0.08f).toInt()).coerceIn(0, freqs.lastIndex)
      val f = freqs[noteIdx]
      val env = exp(-t * 5.2f)
      val tone =
        (sin(2.0 * PI * f * t) + 0.35 * sin(2.0 * PI * f * 2.0 * t)).toFloat() * env * 0.55f
      out[i] = tone.coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildBiteAlarmBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.38f).toInt()
    val out = FloatArray(len)
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      // Cartoon referee whistle + comical boing
      val whistle = sin(2.0 * PI * 2450.0 * t + 1.8 * sin(2.0 * PI * 34.0 * t)).toFloat()
      val boingFreq = 220.0 + 180.0 * sin(2.0 * PI * 7.0 * t)
      val boing = sin(2.0 * PI * boingFreq * t).toFloat()
      val env = exp(-t * 6.0f)
      out[i] = ((whistle * 0.35f + boing * 0.60f) * env).coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildShieldBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.30f).toInt()
    val out = FloatArray(len)
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val freq = 440.0 + 660.0 * (t / 0.30f)
      val tone = sin(2.0 * PI * freq * t).toFloat() * exp(-t * 6.5f) * 0.65f
      out[i] = tone.coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildSnoreBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.45f).toInt()
    val out = FloatArray(len)
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val window = sin(PI * (t / 0.45f)).toFloat()
      val rattle = sin(2.0 * PI * 115.0 * t).toFloat() * (0.65f + 0.35f * sin(2.0 * PI * 22.0 * t).toFloat())
      out[i] = (rattle * window * 0.45f).coerceIn(-1f, 1f)
    }
    return out
  }

  private fun buildGameOverBuffer(): FloatArray {
    val len = (SAMPLE_RATE * 0.65f).toInt()
    val out = FloatArray(len)
    val freqs = doubleArrayOf(392.0, 349.23, 311.13, 261.63)
    for (i in 0 until len) {
      val t = i.toFloat() / SAMPLE_RATE
      val idx = ((t / 0.15f).toInt()).coerceIn(0, freqs.lastIndex)
      val env = exp(-(t - idx * 0.15f).coerceAtLeast(0f) * 7f)
      val tone = sin(2.0 * PI * freqs[idx] * t).toFloat() * env * 0.65f
      out[i] = tone.coerceIn(-1f, 1f)
    }
    return out
  }
}
