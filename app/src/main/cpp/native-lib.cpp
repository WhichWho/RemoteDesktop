#include <jni.h>
#include <string>
#include <iostream>

using namespace std;

//extern "C" JNIEXPORT jstring JNICALL
//Java_com_cnl_remotedesktop_MainActivity_nmsl(
//        JNIEnv *env,
//        jobject /* this */) {
//    std::string hello = "Hello NMSL!";
//    return env->NewStringUTF(hello.c_str());
//}

JNIEXPORT jstring JNICALL native_hello(JNIEnv *env, jclass clazz) {
    printf("hello in c native code.\n");
    return env->NewStringUTF("hello world returned.");
}

#define JNIREG_CLASS "com/cnl/remotedesktop/MainActivity"//指定要注册的类

/**
* Table of methods associated with a single class.
*/
static JNINativeMethod gMethods[] = {
        {"nmsl", "()Ljava/lang/String;", (void *) native_hello},//绑定
};

/*
* Register several native methods for one class.
*/
static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *methods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, methods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}


/*
* Register native methods for all classes we know about.
*/
static int registerNatives(JNIEnv *env) {
    if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

/*
* Set some test stuff up.
*
* Returns the JNI version on success, -1 on failure.
*/
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);

    if (!registerNatives(env)) {//注册
        return -1;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    return result;
}