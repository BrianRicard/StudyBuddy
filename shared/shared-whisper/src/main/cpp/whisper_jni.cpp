#include <jni.h>
#include <android/log.h>
#include <string>
#include <sys/sysinfo.h>
#include "whisper.h"
#include "ggml.h"

#define TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_studybuddy_shared_whisper_WhisperJni_initContext(
        JNIEnv *env, jobject thiz, jstring model_path_str) {
    const char *model_path = env->GetStringUTFChars(model_path_str, nullptr);
    LOGI("Loading model from: %s", model_path);

    struct whisper_context_params cparams = whisper_context_default_params();
    struct whisper_context *context = whisper_init_from_file_with_params(model_path, cparams);

    env->ReleaseStringUTFChars(model_path_str, model_path);

    if (context == nullptr) {
        LOGW("Failed to load model");
        return 0;
    }

    LOGI("Model loaded successfully");
    return (jlong) context;
}

JNIEXPORT void JNICALL
Java_com_studybuddy_shared_whisper_WhisperJni_freeContext(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    auto *context = (struct whisper_context *) context_ptr;
    if (context != nullptr) {
        whisper_free(context);
        LOGI("Context freed");
    }
}

JNIEXPORT jstring JNICALL
Java_com_studybuddy_shared_whisper_WhisperJni_fullTranscribe(
        JNIEnv *env, jobject thiz, jlong context_ptr, jfloatArray samples,
        jint num_samples, jstring language_str, jstring initial_prompt_str,
        jint num_threads) {
    auto *context = (struct whisper_context *) context_ptr;
    if (context == nullptr) {
        return env->NewStringUTF("{\"text\":\"\",\"segments\":[]}");
    }

    jfloat *samples_arr = env->GetFloatArrayElements(samples, nullptr);
    const char *language = env->GetStringUTFChars(language_str, nullptr);
    const char *initial_prompt = env->GetStringUTFChars(initial_prompt_str, nullptr);

    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.translate = false;
    params.language = language;
    params.n_threads = num_threads;
    params.offset_ms = 0;
    params.no_context = true;
    params.single_segment = false;
    params.initial_prompt = initial_prompt;

    LOGI("Starting transcription: lang=%s, samples=%d, threads=%d", language, num_samples, num_threads);

    whisper_reset_timings(context);
    int result = whisper_full(context, params, samples_arr, num_samples);

    std::string json;
    if (result != 0) {
        LOGW("Transcription failed with code %d", result);
        json = "{\"text\":\"\",\"segments\":[]}";
    } else {
        whisper_print_timings(context);

        int n_segments = whisper_full_n_segments(context);
        std::string full_text;
        std::string segments_json = "[";

        for (int i = 0; i < n_segments; i++) {
            const char *seg_text = whisper_full_get_segment_text(context, i);
            int64_t t0 = whisper_full_get_segment_t0(context, i);
            int64_t t1 = whisper_full_get_segment_t1(context, i);

            full_text += seg_text;

            if (i > 0) segments_json += ",";
            segments_json += "{\"text\":\"";

            // Escape special JSON characters in segment text
            for (const char *p = seg_text; *p; p++) {
                switch (*p) {
                    case '"': segments_json += "\\\""; break;
                    case '\\': segments_json += "\\\\"; break;
                    case '\n': segments_json += "\\n"; break;
                    case '\r': segments_json += "\\r"; break;
                    case '\t': segments_json += "\\t"; break;
                    default: segments_json += *p; break;
                }
            }

            segments_json += "\",\"t0\":" + std::to_string(t0);
            segments_json += ",\"t1\":" + std::to_string(t1) + "}";
        }

        segments_json += "]";

        // Build full JSON
        json = "{\"text\":\"";
        for (char c : full_text) {
            switch (c) {
                case '"': json += "\\\""; break;
                case '\\': json += "\\\\"; break;
                case '\n': json += "\\n"; break;
                case '\r': json += "\\r"; break;
                case '\t': json += "\\t"; break;
                default: json += c; break;
            }
        }
        json += "\",\"segments\":" + segments_json + "}";

        LOGI("Transcription complete: %d segments", n_segments);
    }

    env->ReleaseFloatArrayElements(samples, samples_arr, JNI_ABORT);
    env->ReleaseStringUTFChars(language_str, language);
    env->ReleaseStringUTFChars(initial_prompt_str, initial_prompt);

    return env->NewStringUTF(json.c_str());
}

} // extern "C"
