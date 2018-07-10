package instep

import instep.cache.Cache
import instep.cache.driver.MemoryCache
import instep.reflection.JMirror
import instep.reflection.Mirror
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.ServiceNotFoundException
import instep.servicecontainer.impl.MemoryServiceContainer
import instep.typeconversion.DefaultTypeConversion
import instep.typeconversion.TypeConversion
import instep.typeconversion.TypeConversionException
import kotlin.reflect.KClass

@Suppress("unused")
object Instep {
    var serviceContainer: ServiceContainer = MemoryServiceContainer(MemoryCache())

    init {
        serviceContainer.bind(Cache::class.java, MemoryCache())
        serviceContainer.bind(TypeConversion::class.java, DefaultTypeConversion())
    }

    /**
     * @see ServiceContainer.bind
     */
    fun <T : Any> bind(cls: Class<T>, obj: T, tag: String = "") {
        serviceContainer.bind(cls, obj, tag)
    }

    /**
     * @see ServiceContainer.make
     */
    @Throws(ServiceNotFoundException::class)
    fun <T : Any> make(cls: Class<T>, tag: String = ""): T {
        return serviceContainer.make(cls, tag)
    }

    /**
     * @see TypeConversion.convert
     */
    @Throws(TypeConversionException::class)
    fun <From : Any, To, T : From> convert(instance: T, from: Class<From>, to: Class<To>): To {
        val typeConvert = make(TypeConversion::class.java)
        return typeConvert.convert(instance, from, to)
    }

    /**
     * Reflect class.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> reflect(cls: Class<T>): JMirror<T> {
        val cache = make(Cache::class.java)
        return if (cache.containsKey(cls.name)) {
            cache[cls.name] as JMirror<T>
        }
        else {
            val m = JMirror(cls)
            cache[cls.name!!] = m
            m
        }
    }

    /**
     * Reflect instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> reflect(instance: T): JMirror<T> {
        return reflect(instance.javaClass)
    }

    /**
     * Reflect class.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> kReflect(cls: KClass<T>): Mirror<T> {
        val cache = make(Cache::class.java)
        return if (cache.containsKey(cls.qualifiedName)) {
            cache[cls.qualifiedName] as Mirror<T>
        }
        else {
            val m = Mirror(cls)
            cache[cls.qualifiedName!!] = m
            m
        }
    }

    /**
     * Reflect instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> kReflect(instance: T): Mirror<T> {
        return kReflect(instance.javaClass.kotlin)
    }
}
