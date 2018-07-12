package instep.servicecontainer.impl

import instep.servicecontainer.ServiceBinding
import instep.servicecontainer.ServiceContainer
import instep.servicecontainer.ServiceNotFoundException

/**
 * Combine services in multi containers.
 */
@Suppress("CanBeParameter")
open class CompositeServiceContainer<T : Any>(val primary: ServiceContainer<T>, vararg val secondary: ServiceContainer<T>) : AbstractServiceContainer<T>() {
    private val containers = listOf(primary) + secondary

    override fun serviceBinds(): List<ServiceBinding<out T>> {
        return containers.map { it.serviceBinds() }.flatten()
    }

    override fun <T : Any> make(cls: Class<T>, tag: String): T {
        containers.forEach {
            it.make(cls, tag).let { return it }
        }

        throw ServiceNotFoundException(getKey(cls, tag))
    }

    override fun bind(cls: Class<out T>, instance: T, tag: String) {
        primary.bind(cls, instance, tag)
    }

    override fun bind(binding: ServiceBinding<out T>) {
        primary.bind(binding)
    }

    override fun bindInstance(key: String, instance: T) {
        throw UnsupportedOperationException("composite service container don't actually need this.")
    }

    override fun hasKey(key: String): Boolean {
        throw UnsupportedOperationException("composite service container don't actually need this.")
    }

    override fun <T : Any> remove(cls: Class<T>, tag: String): T? {
        return primary.remove(cls, tag)
    }

    override fun removeAll(instance: T) {
        primary.removeAll(instance)
    }

    override fun clear() {
        primary.clear()
    }

    override fun copyServices(container: ServiceContainer<T>) {
        primary.copyServices(container)
    }

    override fun copy(container: ServiceContainer<T>) {
        primary.copy(container)
    }
}