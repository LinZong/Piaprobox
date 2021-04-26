package com.nemesiss.dev.piaprobox.model.htmlparser

class RuleVersion(versionStr : String) : Comparable<RuleVersion> {
    var A = 0
    var B = 0
    var C = 0
    init {
        val versionInt = versionStr.split(".").map { it.toInt() }
        A = versionInt[0]
        B = versionInt[1]
        C = versionInt[2]
    }

    override fun compareTo(other: RuleVersion): Int {
        if(A == other.A && B == other.B) {
            return C - other.C
        }
        if(A == other.A) {
            return B - other.B
        }
        return A - other.A
    }
}