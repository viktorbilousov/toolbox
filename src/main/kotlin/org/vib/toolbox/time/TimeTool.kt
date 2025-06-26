package org.vib.toolbox.time

import org.vib.toolbox.containsAllExactly
import org.vib.toolbox.logger
import java.time.Instant
import java.util.Collections
import kotlin.collections.joinToString

object TimeTool {

    private val logger = logger<TimeTool>()


   private class MeasurementGroup(
        val name: String,
        val context: String?,
        var childs: MutableList<MeasurementGroup> = mutableListOf(),
        var parent: MeasurementGroup? = null,
        var enabled: Boolean = false,
        var openedMeasurement: Long? = null,
        val measurements: MutableList<Pair<Long, Long>> = Collections.synchronizedList(mutableListOf())
    ) {


       override fun toString(): String {
           return name
       }

        fun addChild(measurementGroup: MeasurementGroup){
            childs.add(measurementGroup);
            measurementGroup.parent = this
        }

        fun getFullNameAsList(): List<String>{
            val list = mutableListOf<String>()
            list.add(name)
            var parent: MeasurementGroup? = this.parent;
            while (parent != null){
                list.add(0, parent.name)
                parent = parent.parent
            }
            return list
        }

        fun getChild(groups: Array<String>): MeasurementGroup? {
            if(childs.isEmpty()) return this
            var group : MeasurementGroup? = null;
            var found = false;
            for (name in groups) {
                for (measurementGroup in childs) {
                    if(measurementGroup.name == name){
                        group = measurementGroup;
                        found = true
                        break
                    }
                }
                if(found){
                    found = false
                }
                else{
                    return null
                }
            }
            return group;
        }


        fun getOrCreateChild(groups: Collection<String>, context: String): MeasurementGroup {
            if(groups.isEmpty()) return this
            var thisGroup : MeasurementGroup = this;
            var found = false;
            var skipSearch = false;
            var skipFirst = groups.first() == name
            for (name in groups) {
                if(skipFirst){
                    skipFirst = false
                    continue
                }
                if(skipSearch){
                    val newGroup = MeasurementGroup(name, context)
                    thisGroup.addChild(newGroup)
                    thisGroup = newGroup
                    continue
                }
                skipSearch = true;
                for (measurementGroup in thisGroup.childs) {
                    if(measurementGroup.name == name){
                        thisGroup = measurementGroup;
                        skipSearch = false
                        break
                    }
                }
                if(skipSearch){
                    val newGroup = MeasurementGroup(name, context)
                    thisGroup.addChild(newGroup)
                    thisGroup = newGroup
                    continue
                }
            }
            return thisGroup;
        }

        fun getFullName(): String {
           return getFullNameAsList().joinToString(".")
        }


        fun measureStart(now: Instant){
            openedMeasurement = now.toEpochMilli()
        }

        fun measureEnd(now: Instant){
            if(openedMeasurement == null){
                TimeTool.logger.warn("Cannot Measure End for ${getFullName()}: no measurement start!")
                return
            }
            this.measurements.add(Pair(openedMeasurement!!, now.toEpochMilli()))
        }
    }


    var enabled : Boolean
        set(value) {
            root.enabled = value
        }
        get() = root.enabled

    private val root: MeasurementGroup = MeasurementGroup("root", null)
    private var lastOpenedGroups: MutableList<MeasurementGroup> = mutableListOf()

    init {
        enabled = false
    }

    private fun anyToKey(any: Any): String {
        return if(any is String) any else any::class.java.simpleName
    }

    private fun updateIfEnabled(group: MeasurementGroup): Boolean {
        val name = group.getFullNameAsList().joinToString("@@%%")

        for (disabledGroup in disabledGroups) {
            val expectedName = disabledGroup.joinToString("@@%%")
            if(name.contains(expectedName)) {
                group.enabled = false
                return false
            }
        }

        for (endbledGroup in enabledGroups) {
            val expectedName = endbledGroup.joinToString("@@%%")
            if(name.contains(expectedName)) {
                group.enabled = true
                return true
            }
        }

        return group.enabled
    }

    fun beginMeasure(context: Any, vararg group: String){
        if(!enabled) return
        val now = Instant.now()
        val keys = mutableListOf<String>()
        val lastOpenedGroup = lastOpenedGroups.lastOrNull() ?: root
        keys.addAll(lastOpenedGroup.getFullNameAsList())

        if(lastOpenedGroup.context == null){
            keys.add(anyToKey(context))
        }
        keys.addAll(group)
        val group = root.getOrCreateChild(keys, anyToKey(context))
        updateIfEnabled(group)
        if(!group.enabled) return
        group.measureStart(now)
        lastOpenedGroups.add(group)
    }

    fun endMeasure(context: Any, vararg group: String){
        if(!enabled) return
        val now = Instant.now()
        val keys = mutableListOf<String>()
        val context = anyToKey(context)
        val lastOpenedGroupIndex = lastOpenedGroups
            .indexOfLast {
                it.getFullName().endsWith(group.joinToString("."))
                        && it.context == context
            }
        if(lastOpenedGroupIndex == -1){
            logger.warn("Cannot find opened group ${group}")
            return
        }
        val measurementGroup = lastOpenedGroups.removeAt(lastOpenedGroupIndex)
        updateIfEnabled(measurementGroup)
        if(!measurementGroup.enabled) return
        measurementGroup.measureEnd(now)
    }

    fun Double.round(decimals: Int = 2): Double = "%.${decimals}f".format(this).toDouble()

    private fun collectChildsRec(group: MeasurementGroup, list: MutableList<MeasurementGroup>){
        list.add(group)
        for (measurementGroup in group.childs) {
            collectChildsRec(measurementGroup, list)
        }
    }

    private fun collectChilds(): MutableList<MeasurementGroup> {
        val list = mutableListOf<MeasurementGroup>()
        collectChildsRec(root,list)
        return list;
    }

    fun getAllGroups(): List<String>{
        return collectChilds().map { it.getFullName() }
    }

    private val enabledGroups = mutableListOf<List<String>>()
    private val disabledGroups = mutableListOf< List<String>>()

    private fun doAfterGroup(state: Boolean, group: Array<out String>){
        if(group.isEmpty()) return
        for (measurementGroup in collectChilds()) {
            val name = measurementGroup.getFullNameAsList().joinToString("@@%%")
            val expectedName = group.joinToString("@@%%")
            if(name.contains(expectedName)) {
                measurementGroup.enabled = state
            }
        }
    }

    fun enableAfterGroup(vararg group: String){
       doAfterGroup(true, group)
        val l = group.toList()
        enabledGroups.add(l)
        disabledGroups.removeIf { it -> it.containsAllExactly(l) }
    }

    fun dinableAfterGroup(vararg group: String){
       doAfterGroup(false, group)
        val l = group.toList()
        disabledGroups.add(l)
       enabledGroups.removeIf { it -> it.containsAllExactly(l) }
    }

    fun enableAll(){
        enableAfterGroup(root.name)
    }
    fun disableAll(){
        dinableAfterGroup(root.name)
    }


    fun reset(){
        root.childs.clear()
        lastOpenedGroups.clear()
    }

    fun printTotal(print: (String) -> Unit = { logger.info(it) }){
        printForGroup(root, print)
    }

    private fun List<String>.toGroup() = this.joinToString(".")

    private val Long.printTime get() =  "$this mils"


    private fun collectAllMeasurements(g: MeasurementGroup, list:  MutableList<Pair<Long, Long>>){
        list.addAll(g.measurements)
        for (group in g.childs) {
            collectAllMeasurements(group, list)
        }
    }

    private fun printForGroup(group: MeasurementGroup, print: (String) -> Unit){
        val level = group.getFullNameAsList().size - 1
        val measurements = group.measurements.map { it.second - it.first }
        val total = measurements.sum()
        val average = measurements.average().round(5)
        val times = measurements.count()
        print("   ".repeat(level) +" [" +group.context + "] "+ group.name + " " + total.printTime  + " ($average) [$times times]" )
        for (child in group.childs) {
            printForGroup(child, print)
        }
    }

}