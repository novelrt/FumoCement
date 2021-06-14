// Copyright © Matt Jones and Contributors. Licensed under the MIT License (MIT). See LICENCE.md in the repository root for more information.

#ifndef FUMOCEMENT_LIB
#define FUMOCEMENT_LIB

#include "jni.h"
#include <optional>
#include <iostream>
#include <string>
#include <vector>

namespace FumoCement
{
    /**
     * Caching
     */

    // This is template magic to store strings at compile-time. This way,
    // we can have a string such as TemplateString<'a', 'b', 'c'> and use Value to get
    // the actual char array. While this is annoying to write, this is fortunately only used
    // in the code generator which writes this mess for us.
    //
    // This enables compile-time caching as, for instance, multiple calls to
    // getCachedClass<SomeString>() will *always* use the same local static variable
    // containing the cached jclass. It's like generating a function for each different TemplateString.
    template <const char... Chars>
    struct TemplateString
    {
        static constexpr char Value[] = { Chars..., 0 };
    };

    template <
        typename Class
    >
    jclass getCachedClass(JNIEnv* env) noexcept
    {
        static jclass value = reinterpret_cast<jclass>(env->NewGlobalRef(env->FindClass(Class::Value)));
        return value;
    }

    template <
        typename Class,
        typename FieldName,
        typename Signature
    >
    jfieldID getCachedField(JNIEnv* env) noexcept
    {
        static jfieldID value = [env]()
        {
          const jclass containerClass = getCachedClass<Class>(env);
          return env->GetFieldID(containerClass, FieldName::Value, Signature::Value);
        }();
        return value;
    }

    template <
        typename Class,
        typename MethodName,
        typename Signature
    >
    jmethodID getCachedMethod(JNIEnv* env) noexcept
    {
        static jmethodID value = [env]()
        {
          const jclass containerClass = getCachedClass<Class>(env);
          return env->GetMethodID(containerClass, MethodName::Value, Signature::Value);
        }();
        return value;
    }

    template <
        typename Class,
        typename MethodName,
        typename Signature
    >
    jmethodID getCachedStaticMethod(JNIEnv* env) noexcept
    {
        static jmethodID value = [env]()
        {
          const jclass containerClass = getCachedClass<Class>(env);
          return env->GetStaticMethodID(containerClass, MethodName::Value, Signature::Value);
        }();
        return value;
    }

    /*
     * String tools
     */

    inline std::optional<std::string> toCppString(JNIEnv* env, jbyteArray javaStringBytes) noexcept
    {
        if (javaStringBytes == nullptr)
        {
            return std::nullopt;
        }

        const jsize byteArraySize = env->GetArrayLength(javaStringBytes);
        jbyte* bytePointer = env->GetByteArrayElements(javaStringBytes, JNI_FALSE);

        std::string result(byteArraySize, '.');
        for (int i = 0; i < byteArraySize; ++i)
        {
            result[i] = static_cast<char>(*(bytePointer + i));
        }

        env->ReleaseByteArrayElements(javaStringBytes, bytePointer, 0);

        return { result }; // New std::optional
    }

    inline jbyteArray toJavaStringBytes(JNIEnv* env, const char* cString, bool deleteString) noexcept
    {
        if (cString == nullptr)
        {
            return nullptr;
        }

        const auto stringLength = strlen(cString);
        std::vector<jbyte> byteBuffer(stringLength);
        for (int i = 0; cString[i] != '\0'; i++)
        {
            byteBuffer[i] = (static_cast<jbyte>(cString[i]));
        }

        const jbyteArray charactersArray = env->NewByteArray(stringLength);
        env->SetByteArrayRegion(charactersArray, 0, stringLength, byteBuffer.data());

        if (deleteString)
        {
            delete[] cString;
        }

        return charactersArray;
    }

    /**
     * Type conversions
     */

    template <typename N>
    N toJavaPrimitive(N&& native) noexcept
    {
        return native;
    }

    inline jbyte toJavaPrimitive(const char native) noexcept
    {
        return static_cast<jbyte>(native);
    }

    template <typename J>
    J toNativePrimitive(J&& java) noexcept
    {
        return java;
    }

    inline char toNativePrimitive(const jchar java) noexcept
    {
        return static_cast<char>(java);
    }

    /**
     * Pointer conversions
     */

    template <typename T>
    jlong toJavaPointer(const T* nativePointer) noexcept
    {
        // TODO: Better cross-architecture.
        return reinterpret_cast<jlong>(nativePointer);
    }

    template <typename T>
    T* toNativePointer(jlong javaPointer) noexcept
    {
        // TODO: Better cross-architecture.
        return reinterpret_cast<T*>(static_cast<std::size_t>(javaPointer));
    }

    /**
    * C++ to C
    */

    template <typename T>
    T passAsC(const T& value) noexcept
    {
        return value;
    }

    inline char* passAsC(const std::optional<std::string>& cppString) noexcept
    {
        if (!cppString)
        {
            return nullptr;
        }
        return const_cast<char*>(cppString.value().c_str()); // This const_cast is needed for char*.
    }

    inline JavaVM* getJavaVM(JNIEnv* env)
    {
        JavaVM* vm = nullptr;
        if (const auto result = env->GetJavaVM(&vm); result == JNI_OK)
        {
            return vm;
        }

        throw new std::runtime_error("Failed to get the JavaVM.");
    }

    struct FunctionPointerContext
    {
        JavaVM* javaVm;
        jobject globalObjectRef;

        [[ nodiscard ]] JNIEnv* getEnv() const
        {
            JNIEnv* env = nullptr; // TODO: cache this?
            const auto result = javaVm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_2);
            if (result == JNI_OK)
            {
                return env;
            }
            if (result == JNI_EDETACHED)
            {
                // TODO: Detach... but when?
                if (const auto attachResult = javaVm->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&env), nullptr);
                    attachResult == JNI_OK)
                {
                    return env;
                }

                throw std::runtime_error("JNI: Failed to attach to current thread.");

            }
            throw std::runtime_error("JNI: Failed to get an instance of JNIEnv");
        }
    };
}

extern "C"
{
#pragma region DoublePointer

/*
* Class:     com_github_novelrt_fumocement_builtin_DoublePointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_DoublePointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new double);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_DoublePointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_DoublePointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<double>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_DoublePointer
 * Method:    getValue
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_com_github_novelrt_fumocement_builtin_DoublePointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return *FumoCement::toNativePointer<double>(handle);
}

/*
* Class:     com_github_novelrt_fumocement_builtin_DoublePointer
* Method:    setValue
* Signature: (JD)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_DoublePointer_setValue
    (JNIEnv*, jclass, jlong handle, jdouble value)
{
    *FumoCement::toNativePointer<double>(handle) = value;
}

#pragma endregion

#pragma region FloatPointer
/*
* Class:     com_github_novelrt_fumocement_builtin_FloatPointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_FloatPointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new float);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_FloatPointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_FloatPointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<float>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_FloatPointer
 * Method:    getValue
 * Signature: (J)F
 */
JNIEXPORT jfloat JNICALL Java_com_github_novelrt_fumocement_builtin_FloatPointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return *FumoCement::toNativePointer<float>(handle);
}

JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_FloatPointer_setValue
    (JNIEnv*, jclass, jlong handle, jfloat value)
{
    *FumoCement::toNativePointer<float>(handle) = value;
}
#pragma endregion

#pragma region Int8Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_Int8Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_Int8Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::int8_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int8Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int8Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::int8_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int8Pointer
 * Method:    getValue
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_com_github_novelrt_fumocement_builtin_Int8Pointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jbyte>(*FumoCement::toNativePointer<std::int8_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_Int8Pointer
* Method:    setValue
* Signature: (JB)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int8Pointer_setValue
    (JNIEnv*, jclass, jlong handle, jbyte value)
{
    *FumoCement::toNativePointer<std::int8_t>(handle) = static_cast<std::int8_t>(value);
}
#pragma endregion

#pragma region Int16Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_Int16Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_Int16Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::int16_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int16Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int16Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::int16_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int16Pointer
 * Method:    getValue
 * Signature: (J)S
 */
JNIEXPORT jshort JNICALL Java_com_github_novelrt_fumocement_builtin_Int16Pointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jshort>(*FumoCement::toNativePointer<std::int16_t>(handle));
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int16Pointer
 * Method:    setValue
 * Signature: (JS)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int16Pointer_setValue
    (JNIEnv*, jclass, jlong handle, jshort value)
{
    *FumoCement::toNativePointer<std::int16_t>(handle) = static_cast<std::int16_t>(value);
}
#pragma endregion

#pragma region Int32Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_Int32Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_Int32Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::int32_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int32Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int32Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::int32_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int32Pointer
 * Method:    getValue
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_github_novelrt_fumocement_builtin_Int32Pointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jint>(*FumoCement::toNativePointer<std::int32_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_Int32Pointer
* Method:    setValue
* Signature: (JI)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int32Pointer_setValue
    (JNIEnv*, jclass, jlong handle, jint value)
{
    *FumoCement::toNativePointer<std::int32_t>(handle) = static_cast<std::int32_t>(value);
}
#pragma endregion

#pragma region Int64Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_Int64Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_Int64Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::int64_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int64Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int64Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::int64_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_Int64Pointer
 * Method:    getValue
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_Int64Pointer_getValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jlong>(*FumoCement::toNativePointer<std::int64_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_Int64Pointer
* Method:    setValue
* Signature: (JJ)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_Int64Pointer_setValue
    (JNIEnv*, jclass, jlong handle, jlong value)
{
    *FumoCement::toNativePointer<std::int64_t>(handle) = static_cast<std::int64_t>(value);
}
#pragma endregion

#pragma region UInt8Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_UInt8Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_UInt8Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::uint8_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt8Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt8Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::uint8_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt8Pointer
 * Method:    getUnsignedValue
 * Signature: (J)B
 */
JNIEXPORT jbyte JNICALL Java_com_github_novelrt_fumocement_builtin_UInt8Pointer_getUnsignedValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jbyte>(*FumoCement::toNativePointer<std::uint8_t>(handle));
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt8Pointer
 * Method:    setUnsignedValue
 * Signature: (JB)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt8Pointer_setUnsignedValue
    (JNIEnv*, jclass, jlong handle, jbyte value)
{
    *FumoCement::toNativePointer<std::uint8_t>(handle) = static_cast<std::uint8_t>(value);
}
#pragma endregion

#pragma region UInt16Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_UInt16Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_UInt16Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::uint16_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt16Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt16Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::uint16_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt16Pointer
 * Method:    getUnsignedValue
 * Signature: (J)C
 */
JNIEXPORT jchar JNICALL Java_com_github_novelrt_fumocement_builtin_UInt16Pointer_getUnsignedValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jchar>(*FumoCement::toNativePointer<std::uint16_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_UInt16Pointer
* Method:    setUnsignedValue
* Signature: (JC)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt16Pointer_setUnsignedValue
    (JNIEnv*, jclass, jlong handle, jchar value)
{
    *FumoCement::toNativePointer<std::uint16_t>(handle) = static_cast<std::uint16_t>(value);
}
#pragma endregion

#pragma region UInt32Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_UInt32Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_UInt32Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::uint32_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt32Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt32Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::uint32_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt32Pointer
 * Method:    getUnsignedValue
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_github_novelrt_fumocement_builtin_UInt32Pointer_getUnsignedValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jint>(*FumoCement::toNativePointer<std::uint32_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_UInt32Pointer
* Method:    setUnsignedValue
* Signature: (JI)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt32Pointer_setUnsignedValue
    (JNIEnv*, jclass, jlong handle, jint value)
{
    *FumoCement::toNativePointer<std::uint32_t>(handle) = static_cast<std::uint32_t>(value);
}
#pragma endregion

#pragma region UInt64Pointer
/*
* Class:     com_github_novelrt_fumocement_builtin_UInt64Pointer
* Method:    allocatePointer
* Signature: ()J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_UInt64Pointer_allocatePointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new std::uint64_t);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt64Pointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt64Pointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<std::uint64_t>(handle);
}

/*
 * Class:     com_github_novelrt_fumocement_builtin_UInt64Pointer
 * Method:    getUnsignedValue
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_builtin_UInt64Pointer_getUnsignedValue
    (JNIEnv*, jclass, jlong handle)
{
    return static_cast<jlong>(*FumoCement::toNativePointer<std::uint64_t>(handle));
}

/*
* Class:     com_github_novelrt_fumocement_builtin_UInt64Pointer
* Method:    setUnsignedValue
* Signature: (JJ)V
*/
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_builtin_UInt64Pointer_setUnsignedValue
    (JNIEnv*, jclass, jlong handle, jlong value)
{
    *FumoCement::toNativePointer<std::uint64_t>(handle) = static_cast<std::uint64_t>(value);
}
#pragma endregion

#pragma region IndirectedPointer
/*
* Class:     com_github_novelrt_fumocement_IndirectedPointer
* Method:    getNativeUnderlyingHandle
* Signature: (J)J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_IndirectedPointer_getNativeUnderlyingHandle
    (JNIEnv*, jclass, jlong handle)
{
    return FumoCement::toJavaPointer(*FumoCement::toNativePointer<void*>(handle));
}

/*
 * Class:     com_github_novelrt_fumocement_IndirectedPointer
 * Method:    setNativeUnderlyingHandle
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_IndirectedPointer_setNativeUnderlyingHandle
    (JNIEnv*, jclass, jlong handle, jlong value)
{
    *FumoCement::toNativePointer<void*>(handle) = FumoCement::toNativePointer<void>(value);
}

/*
 * Class:     com_github_novelrt_fumocement_IndirectedPointer
 * Method:    createPointer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_IndirectedPointer_createPointer
    (JNIEnv*, jclass)
{
    return FumoCement::toJavaPointer(new void**);
}

/*
 * Class:     com_github_novelrt_fumocement_IndirectedPointer
 * Method:    destroyPointer
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_IndirectedPointer_destroyPointer
    (JNIEnv*, jclass, jlong handle)
{
    delete FumoCement::toNativePointer<void*>(handle);
}
#pragma endregion

#pragma region FunctionPointer
/*
* Class:     com_github_novelrt_fumocement_FunctionPointer
* Method:    createPointerContext
* Signature: (Ljava/lang/Object;)J
*/
JNIEXPORT jlong JNICALL Java_com_github_novelrt_fumocement_FunctionPointer_createPointerContext
    (JNIEnv* env, jclass, jobject obj)
{
    return toJavaPointer(new FumoCement::FunctionPointerContext{ FumoCement::getJavaVM(env), env->NewGlobalRef(obj) });
}

/*
 * Class:     com_github_novelrt_fumocement_FunctionPointer
 * Method:    destroyPointerContext
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_github_novelrt_fumocement_FunctionPointer_destroyPointerContext
    (JNIEnv* env, jclass, jlong handle)
{
    const auto* context = FumoCement::toNativePointer<FumoCement::FunctionPointerContext>(handle);
    env->DeleteGlobalRef(context->globalObjectRef);
    delete context;
}
#pragma endregion

#pragma region PointerOperations
/*
* Class:     com_github_novelrt_fumocement_PointerOperations
* Method:    getNativeLongSize
* Signature: ()I
*/
JNIEXPORT jint JNICALL Java_com_github_novelrt_fumocement_PointerOperations_getNativeLongSize
    (JNIEnv*, jclass)
{
    static jint value = static_cast<jint>(sizeof(std::uintptr_t));
    return value;
}
#pragma endregion
}


#endif
