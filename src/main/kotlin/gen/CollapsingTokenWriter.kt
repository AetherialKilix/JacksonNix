package net.aetherialkilix.jackson.nix.gen

import net.aetherialkilix.jackson.nix.NixGenerator
import java.util.*
import net.aetherialkilix.jackson.nix.gen.GeneratorToken.*

data class CollapsingTokenWriter(val generator: NixGenerator) : TokenWriter() {

    private val queue: LinkedList<GeneratorToken> = LinkedList()

    override fun writeToken(token: GeneratorToken) {
        queue.add(token)
        postAppend()
    }

    fun isComplete(): Boolean {
        var objects = 0
        var arrays = 0
        queue.forEach {
            when (it) {
                is START_OBJECT -> objects ++
                is START_ARRAY -> arrays ++
                is END_OBJECT -> objects --
                is END_ARRAY -> arrays --
                else -> {}
            }
        }
        return objects == 0 && arrays == 0
    }

    fun collapsable(): Boolean {
        // name, separator, obj start, name, separator, value, obj end
        if (queue.size < 7) return false
        val last = queue.peekLast()
        if (last !is END_OBJECT) return false
        return last.entries == 1
    }

    fun postAppend() {
        when {
            collapsable() -> {
                queue.pollLast() as END_OBJECT

                val value = mutableListOf<GeneratorToken>()
                var objects: Int = -1
                while (true) {
                    // take off the token
                    val token = queue.pollLast()
                    // count objects
                    when (token) {
                        null -> return flushTokens() // OH GOD OH FUCK WHAT DID I DO?
                        is END_OBJECT -> objects --
                        is START_OBJECT -> objects ++
                        else -> {}
                    }
                    // if we evened out, break
                    if (objects == 0) break
                    // otherwise be part of collapsable value
                    value.add(0, token)
                }
                queue.pollLast() as NAME_VALUE_SEPERATOR
                val me = value.removeFirst() as NAME
                value.removeFirst() as NAME_VALUE_SEPERATOR
                val parent = queue.pollLast() as NAME
                val new = NAME("${parent.name}.${me.name}")
                queue.add(new)
                queue.add(NAME_VALUE_SEPERATOR)
                queue.addAll(value)
            }
            isComplete() -> flushTokens()
        }
    }

    override fun flushTokens() {
        // iterate from beginning to end, writing the tokens in that order
        while (true) { queue.poll()?.write(generator) ?: break }
    }

}