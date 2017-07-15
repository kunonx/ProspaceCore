/*
Copyright (c) 2017 ProspaceCraft Team
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package net.prospacecraft.ProspaceCore.data

import java.util.ArrayList

/**
 * MultipleHashMap is a data structure base of HashMap<K, V> with one key and a different value.
 * If you need additional complements, override and reconfigure.
 * @author Kunonx
 * @since  1.0
 * @param  Key Type to use as key
 * @param  Value Type to use for multiple values
 */
class MultipleHashMap<Key, Value> : HashMap<Key, ArrayList<Value>>(), MultipleMap<Key, Value>
{
    override fun containsSingleValue(key: Key, value: Value) : Boolean           = this[key]!!.contains(value)
    override fun put(key: Key, value: ArrayList<Value>?)     : ArrayList<Value>? = super.put(key, value!!)
    override fun remove(key: Key)                            : ArrayList<Value>? = super.remove(key)
    override fun get(key: Key)                               : ArrayList<Value>? = super.get(key)

    override fun removeFirst(key: Key) : ArrayList<Value>?
    {
        val list : ArrayList<Value>? = this[key]
        if(!list!!.remove(list[0])) return list
        return this[key]
    }

    override fun removeLast(key: Key) : ArrayList<Value>?
    {
        val list : ArrayList<Value>? = this[key]
        if(!list!!.remove(list[list.lastIndex])) return list
        return this[key]
    }

    override fun putSingleFirst(key : Key, value : Value) : ArrayList<Value>?
    {
        val list : ArrayList<Value>? = this[key]
        if(list!!.contains(value)) return list

        list.add(0, value)

        this.put(key, list)
        return list
    }

    override fun putSingleLast(key: Key, value: Value) : ArrayList<Value>?
    {
        val list: ArrayList<Value>? = this[key]
        if (list!!.contains(value)) return list

        list.add(list.lastIndex, value)

        this.put(key, list)
        return list
    }
}